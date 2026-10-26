package edu.mcw.rgd.web;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.GeneExpressionDAO;
import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.dao.impl.PhenominerDAO;
import edu.mcw.rgd.datamodel.GeneExpression;
import edu.mcw.rgd.datamodel.expression.ExpressionDataIndexObject;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.datamodel.pheno.Experiment;
import edu.mcw.rgd.datamodel.pheno.GeneExpressionRecord;
import edu.mcw.rgd.datamodel.pheno.Record;
import edu.mcw.rgd.datamodel.pheno.Study;
import edu.mcw.rgd.services.ClientInit;
import edu.mcw.rgd.services.RgdContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.mcw.rgd.datamodel.pheno.Sample;
import edu.mcw.rgd.datamodel.pheno.GeneExpressionRecordValue;
import edu.mcw.rgd.process.mapping.MapManager;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Tag(name="Expression")
@RequestMapping(value = "/expression")
public class ExpressionWebService {
    AccessLogDAO ald = new AccessLogDAO();
    GeneExpressionDAO gdao = new GeneExpressionDAO();
    PhenominerDAO pdao = new PhenominerDAO();
    OntologyXDAO odao = new OntologyXDAO();

    // The expression index stores tissueTerm/strainTerm as null, so accession -> name is resolved
    // here at query time. Ontology term names are effectively static, so cache them across requests;
    // a null cache value records "looked up, no term found" so we do not re-query a bad accession.
    private final Map<String, String> termLabelCache = new ConcurrentHashMap<>();
    private static final String NO_LABEL = " "; // sentinel: resolved to nothing (ConcurrentHashMap forbids null values)

    //private static final String EXPRESSION_INDEX = "expressiontool_index_dev";
    private static final String EXPRESSION_INDEX = RgdContext.getESIndexName("expressiontool");
    private static final int DEFAULT_PAGE_SIZE = 1000;
    // Elasticsearch's default index.max_result_window: from + size cannot exceed this for offset paging.
    private static final int MAX_RESULT_WINDOW = 10000;
    // Max number of buckets returned per terms aggregation.
    private static final int AGG_BUCKET_SIZE = 1000;
    // How long the point in time backing a cursor walk stays alive, refreshed on every page. Long enough
    // that a consumer processing a page between requests does not lose the cursor, short enough that
    // abandoned walks release their search context promptly.
    private static final String PIT_KEEP_ALIVE = "5m";
    // Cap on a single cursor page. from/size paging is capped by MAX_RESULT_WINDOW instead; a cursor walk
    // has no depth limit, so only the per-request payload needs bounding.
    private static final int MAX_CURSOR_PAGE_SIZE = 10000;
    // Fields indexed for querying that an API consumer has no use for, so they are never fetched from
    // _source: parentTermAccIds is the ontology ancestors that let a term query match descendants (and is
    // large), geneSymbolWithRgdId is the "Symbol-RGD:id" form backing the gene facet, redundant next to
    // geneSymbol and geneRgdId. ExpressionJsonConfig also keeps them out of the serialized response.
    // Aggregations read doc_values rather than _source, so the gene facet is unaffected.
    private static final List<String> SOURCE_EXCLUDES = List.of("parentTermAccIds", "geneSymbolWithRgdId");
    // The ontology ancestors of every term on a record. Excluding it from _source only stops it being
    // returned; it stays in the inverted index, which is what makes the descendant matching below work.
    private static final String PARENT_TERM_FIELD = "parentTermAccIds.keyword";

    @RequestMapping(value="/{ontTerm}/{rgdId}/{unit}", method = RequestMethod.GET)
    @Operation(summary="return a list of Expression Values", tags="Expression")
    public List<GeneExpression> getGeneExpressionValuesByOntTermRgdIdUnit(HttpServletRequest request,
                                                                          @Parameter(description = "Ontology Term Id", required = true) @PathVariable(name = "ontTerm") String ontTerm,
                                                                          @Parameter(description = "Gene RGD ID", required = true) @PathVariable(name = "rgdId") int rgdId,
                                                                          @Parameter(description = "Associated unit: TPM|FPKM",required = true) @PathVariable(name = "unit") String unit) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return gdao.getGeneExpressionObjectsByTermRgdIdUnit(ontTerm,rgdId,unit.toUpperCase());
    }

    /**
     * Enriched variant of the /{ontTerm}/{rgdId}/{unit} endpoint used by the
     * gene report page. Returns already-resolved strain/tissue term names,
     * assembly names, and study references so the client can render each row
     * with a single REST call instead of the four-nested-AJAX explosion that
     * previously issued ~4 requests per record.
     */
    @RequestMapping(value="/{ontTerm}/{rgdId}/{unit}/rows", method = RequestMethod.GET)
    @Operation(summary="return expression rows with strain/tissue/assembly names and refs pre-joined", tags="Expression")
    public List<Map<String, Object>> getGeneExpressionRowsByOntTermRgdIdUnit(HttpServletRequest request,
                                                                             @Parameter(description = "Ontology Term Id", required = true) @PathVariable(name = "ontTerm") String ontTerm,
                                                                             @Parameter(description = "Gene RGD ID", required = true) @PathVariable(name = "rgdId") int rgdId,
                                                                             @Parameter(description = "Associated unit: TPM|FPKM", required = true) @PathVariable(name = "unit") String unit) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(), request);
        List<GeneExpression> raw = gdao.getGeneExpressionObjectsByTermRgdIdUnit(ontTerm, rgdId, unit.toUpperCase());
        return buildEnrichedRows(raw, unit.toUpperCase());
    }

    private List<Map<String, Object>> buildEnrichedRows(List<GeneExpression> raw, String unit) throws Exception {
        // Batch: unique study IDs and map keys, resolved once each rather
        // than per-record. Strain/tissue term names go through resolveTermLabel
        // which already caches across requests.
        Set<Integer> studyIds = new HashSet<>();
        Set<Integer> mapKeys = new HashSet<>();
        for (GeneExpression r : raw) {
            if (r.getStudyId() != null) studyIds.add(r.getStudyId());
            GeneExpressionRecordValue v = r.getGeneExpressionRecordValue();
            if (v != null) mapKeys.add(v.getMapKey());
        }

        Map<Integer, String> assemblyNames = new HashMap<>();
        for (Integer mk : mapKeys) {
            try {
                edu.mcw.rgd.datamodel.Map m = MapManager.getInstance().getMap(mk);
                if (m != null) assemblyNames.put(mk, m.getName());
            } catch (Exception ignored) { /* fall back to mapKey number */ }
        }

        Map<Integer, List<Integer>> studyRefs = new HashMap<>();
        for (Integer sid : studyIds) {
            try {
                studyRefs.put(sid, pdao.getStudyReferences(sid));
            } catch (Exception ignored) {
                studyRefs.put(sid, Collections.<Integer>emptyList());
            }
        }

        List<Map<String, Object>> out = new ArrayList<>(raw.size());
        for (GeneExpression r : raw) {
            Map<String, Object> row = new LinkedHashMap<>();
            Sample sample = r.getSample();
            GeneExpressionRecordValue v = r.getGeneExpressionRecordValue();

            String strainAcc = sample != null ? sample.getStrainAccId() : null;
            String tissueAcc = sample != null ? sample.getTissueAccId() : null;
            Integer mapKey = v != null ? v.getMapKey() : null;
            Integer studyId = r.getStudyId();

            String strainName = !isBlank(strainAcc) ? resolveTermLabel(strainAcc) : "";
            String strainHtml;
            if (!isBlank(strainAcc)) {
                String link = strainAcc.startsWith("RS:")
                        ? "/rgdweb/report/strainOnt/main.html?acc=" + strainAcc
                        : "/rgdweb/ontology/view.html?acc_id=" + strainAcc;
                strainHtml = "<a href=\"" + link + "\">" + strainName + "</a>";
            } else {
                strainHtml = "";
            }

            row.put("strain", strainHtml);
            row.put("sex", sample != null && sample.getSex() != null ? sample.getSex() : "");
            row.put("computedSex", sample != null ? sample.getComputedSex() : null);
            row.put("age", computeDisplayAge(sample, mapKey));
            row.put("tissue", !isBlank(tissueAcc) ? resolveTermLabel(tissueAcc) : "");
            row.put("GeoSampleId", sample != null ? sample.getGeoSampleAcc() : null);
            row.put("tpmValue", v != null ? v.getTpmValue() : null);
            row.put("unit", unit);
            row.put("assembly", mapKey != null && assemblyNames.get(mapKey) != null
                    ? assemblyNames.get(mapKey) : (mapKey != null ? String.valueOf(mapKey) : ""));
            row.put("refRgd", studyId != null && studyRefs.get(studyId) != null
                    ? studyRefs.get(studyId) : Collections.<Integer>emptyList());
            row.put("level", v != null ? v.getExpressionLevel() : null);
            row.put("geoStudyAcc", r.getGeoSeriesAcc());
            out.add(row);
        }
        return out;
    }

    /**
     * Mirrors the age-formatting logic that previously lived in
     * expressionDataNew.jsp. Kept server-side so the client just displays
     * the string.
     */
    private static String computeDisplayAge(Sample sample, Integer mapKey) {
        if (sample == null) return "";
        Double high = sample.getAgeDaysFromHighBound();
        Double low = sample.getAgeDaysFromLowBound();
        if (high == null || low == null) return "";
        if (high == 0.0 && low == 0.0) return "not available";
        if (low < 0 || high < 0) {
            if (mapKey != null && (mapKey == 37 || mapKey == 38)) {
                double lo = low + 280;
                double hi = high + 280;
                return hi == lo ? formatDays(lo) + " days post conception"
                        : formatDays(lo) + " - " + formatDays(hi) + " days post conception";
            }
            double lo = low + 22;
            double hi = high + 22;
            return lo == hi ? formatDays(lo) + " embryonic days"
                    : formatDays(lo) + " - " + formatDays(hi) + " embryonic days";
        }
        return high.equals(low) ? formatDays(high) + " days"
                : formatDays(low) + " - " + formatDays(high) + " days";
    }

    private static String formatDays(double d) {
        return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
    }

    @RequestMapping(value = "/experiment/{expId}", method = RequestMethod.GET)
    @Operation(summary = "return an experiment", tags = "Expression")
    public Experiment getExperiment(HttpServletRequest request,
                                    @Parameter(description = "Experiment Id", required = true) @PathVariable(name = "expId") int expId) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return pdao.getExperiment(expId);
    }

    @RequestMapping(value = "/study/{studyId}", method = RequestMethod.GET)
    @Operation(summary = "return a study", tags = "Expression")
    public Study getStudy(HttpServletRequest request,
                          @Parameter(description = "Study Id", required = true) @PathVariable(name = "studyId") int studyId) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return pdao.getStudy(studyId);
    }

    @RequestMapping(value = "/study/references/{studyId}", method = RequestMethod.GET)
    @Operation(summary = "return a study's references", tags = "Expression")
    public List<Integer> getStudyReferences(HttpServletRequest request,
                          @Parameter(description = "Study Id", required = true) @PathVariable(name = "studyId") int studyId) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return pdao.getStudyReferences(studyId);
    }

    @RequestMapping(value = "/expressionRecord/{ontTerm}/{rgdId}/{unit}", method = RequestMethod.GET)
    @Operation(summary = "return a list of Gene Expression Records", tags = "Expression")
    public List<GeneExpressionRecord> getGeneExpressionRecordsByExpressionValues(HttpServletRequest request,
                                                                                 @Parameter(description = "Ontology Term Id", required = true) @PathVariable(name = "ontTerm") String ontTerm,
                                                                                 @Parameter(description = "Gene RGD ID", required = true) @PathVariable(name = "rgdId") int rgdId,
                                                                                 @Parameter(description = "Associated unit: TPM|FPKM",required = true) @PathVariable(name = "unit") String unit) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return gdao.getGeneExpressionRecordsByRecordValues(rgdId,unit.toUpperCase(),ontTerm);
    }

    @RequestMapping(value = "record/{expId}", method = RequestMethod.GET)
    @Operation(summary = "return a record object associated with experiment", tags = "Expression")
    public List<Record> getExpressionRecords(HttpServletRequest request,
                                             @Parameter(description = "Experiment Id", required = true) @PathVariable(name = "expId") int expId) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return pdao.getRecordsForGeneExpressionExpRecord(expId);
    }

    @RequestMapping(value = "/expressionRecord/{expRecId}", method = RequestMethod.GET)
    @Operation(summary = "return a Gene Expression Record", tags = "Expression")
    public GeneExpressionRecord getGeneExpressionRecordsById(HttpServletRequest request,
                                                                                 @Parameter(description = "Gene Expression Record Id", required = true) @PathVariable(name = "expRecId") int expRecId) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return gdao.getGeneExpressionRecordById(expRecId);
    }

    // /records/search is the original path, kept mapped so existing callers do not break; /index/records/search
    // is the canonical one, matching every other endpoint that queries the expression index.
    @RequestMapping(value = "/index/records/search", method = RequestMethod.GET)
    @Operation(summary = "return a page of expression records matching any combination of tissue, strain, condition, "
            + "gene, assembly, expression level and unit. Every filter is optional; the supplied ones are combined with AND, "
            + "and within a single filter any of the values match. "
            + "At least one filter is required.", tags = "Expression")
    public ExpressionIndexPage searchExpressionIndexRecords(HttpServletRequest request,
                                                                 @Parameter(description = "Optional comma-separated list of Tissue ontology term accession ids (e.g. UBERON:0002107,UBERON:0000948)") @RequestParam(name = "tissueIds", required = false) List<String> tissueIds,
                                                                 @Parameter(description = "Optional comma-separated list of Strain ontology term accession ids (e.g. RS:0000134,RS:0000681)") @RequestParam(name = "strainAccIds", required = false) List<String> strainAccIds,
                                                                 @Parameter(description = "Optional comma-separated list of Condition ontology term accession ids (e.g. XCO:0000099,XCO:0000105)") @RequestParam(name = "conditionIds", required = false) List<String> conditionIds,
                                                                 @Parameter(description = "Optional comma-separated list of Gene RGD IDs (e.g. 3001,3876)") @RequestParam(name = "rgdIds", required = false) List<Integer> rgdIds,
                                                                 @Parameter(description = "Optional assembly map key filter (e.g. 372)") @RequestParam(name = "mapKey", required = false) Integer mapKey,
                                                                 @Parameter(description = "Optional comma-separated list of expression levels (e.g. high,medium)") @RequestParam(name = "expressionLevel", required = false) List<String> expressionLevel,
                                                                 @Parameter(description = "Optional comma-separated list of expression units (e.g. TPM,FPKM)") @RequestParam(name = "units", required = false) List<String> units,
                                                                 @Parameter(description = "Zero-based page number") @RequestParam(name = "page", defaultValue = "0") int page,
                                                                 @Parameter(description = "Page size (max 10000)") @RequestParam(name = "size", defaultValue = "100") int size) throws Exception {
//        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(), request);
        List<Query> filters = buildOptionalFilters(tissueIds, strainAccIds, conditionIds, rgdIds, mapKey, expressionLevel, units);
        if (filters.isEmpty()) {
            // Without a filter the query is match_all: a paged walk of the entire index that the max result
            // window truncates at 10000 documents anyway, so it cannot even serve as an export.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one filter is required: "
                    + "tissueIds, strainAccIds, conditionIds, rgdIds, mapKey, expressionLevel or units.");
        }
        return searchExpressionIndex(boolFilter(filters.toArray(new Query[0])), page, size);
    }

    /**
     * Assemble the optional filters shared by /index/records/search and /index/facets: one AND clause per
     * supplied parameter, with the values inside a parameter OR-ed together. Absent and empty parameters
     * contribute nothing, so an all-absent request yields an empty list -- which the two callers treat
     * differently: facets falls back to match_all, the records search rejects the request.
     */
    private static List<Query> buildOptionalFilters(List<String> tissueIds, List<String> strainAccIds,
                                                    List<String> conditionIds, List<Integer> rgdIds,
                                                    Integer mapKey, List<String> expressionLevel, List<String> units) {
        List<Query> filters = new ArrayList<>();
        if (tissueIds != null && !tissueIds.isEmpty()) {
            filters.add(termsWithDescendants("tissueAcc.keyword", tissueIds));
        }
        if (strainAccIds != null && !strainAccIds.isEmpty()) {
            filters.add(termsWithDescendants("strainAcc.keyword", strainAccIds));
        }
        if (conditionIds != null && !conditionIds.isEmpty()) {
            filters.add(termsWithDescendants("conditions.accId.keyword", conditionIds));
        }
        if (rgdIds != null && !rgdIds.isEmpty()) {
            filters.add(termsQuery("geneRgdId", toStrings(rgdIds)));
        }
        if (mapKey != null) {
            filters.add(termQuery("mapKey", mapKey.longValue()));
        }
        // Levels and units are free-text labels, not ontology terms, so they match exactly -- no
        // descendant lookup.
        if (expressionLevel != null && !expressionLevel.isEmpty()) {
            filters.add(termsQuery("expressionLevel.keyword", trimAll(expressionLevel)));
        }
        if (units != null && !units.isEmpty()) {
            filters.add(termsQuery("expressionUnit.keyword", trimAll(units)));
        }
        return filters;
    }


    // /index/records/search caps out at MAX_RESULT_WINDOW documents because offset paging makes
    // Elasticsearch collect from + size hits per shard. This endpoint walks a result set of any size by
    // paging with search_after against a point in time instead: each response carries an opaque cursor
    // that the next request echoes back. The point in time also pins the concrete index behind the
    // expressiontool alias, so a walk is not disturbed by a reindex swapping the alias underneath it.
    @RequestMapping(value = "/index/records/search/cursor", method = RequestMethod.GET)
    @Operation(summary = "walk every expression record matching any combination of tissue, strain, condition, "
            + "gene, assembly, expression level and unit, without the 10000 document limit of offset paging. Every "
            + "filter is optional; the supplied ones are combined with AND, and within a single filter any of the "
            + "values match. Call once without a cursor, then pass the cursor from each response back on the next "
            + "request until the response returns a null cursor, which marks the end of the walk.", tags = "Expression")
    public ExpressionIndexCursorPage walkExpressionIndexRecords(HttpServletRequest request,
                                                                 @Parameter(description = "Optional comma-separated list of Tissue ontology term accession ids (e.g. UBERON:0002107,UBERON:0000948)") @RequestParam(name = "tissueIds", required = false) List<String> tissueIds,
                                                                 @Parameter(description = "Optional comma-separated list of Strain ontology term accession ids (e.g. RS:0000134,RS:0000681)") @RequestParam(name = "strainAccIds", required = false) List<String> strainAccIds,
                                                                 @Parameter(description = "Optional comma-separated list of Condition ontology term accession ids (e.g. XCO:0000099,XCO:0000105)") @RequestParam(name = "conditionIds", required = false) List<String> conditionIds,
                                                                 @Parameter(description = "Optional comma-separated list of Gene RGD IDs (e.g. 3001,3876)") @RequestParam(name = "rgdIds", required = false) List<Integer> rgdIds,
                                                                 @Parameter(description = "Optional assembly map key filter (e.g. 372)") @RequestParam(name = "mapKey", required = false) Integer mapKey,
                                                                 @Parameter(description = "Optional comma-separated list of expression levels (e.g. high,medium)") @RequestParam(name = "expressionLevel", required = false) List<String> expressionLevel,
                                                                 @Parameter(description = "Optional comma-separated list of expression units (e.g. TPM,FPKM)") @RequestParam(name = "units", required = false) List<String> units,
                                                                 @Parameter(description = "Records per page (max 10000)") @RequestParam(name = "size", defaultValue = "1000") int size,
                                                                 @Parameter(description = "Cursor returned by the previous call; omit to start a new walk") @RequestParam(name = "cursor", required = false) String cursor) throws Exception {
//        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(), request);
        List<Query> filters = buildOptionalFilters(tissueIds, strainAccIds, conditionIds, rgdIds, mapKey, expressionLevel, units);
        // Unlike the offset endpoint an unfiltered walk is allowed: it is a whole-index export, which
        // search_after can actually deliver.
        Query query = filters.isEmpty()
                ? Query.of(q -> q.matchAll(m -> m))
                : boolFilter(filters.toArray(new Query[0]));
        return walkExpressionIndex(query, size, cursor);
    }



    @RequestMapping(value = "/index/facets", method = RequestMethod.GET)
    @Operation(summary = "return the available filter values (tissues, strains, conditions, genes, assemblies, units, levels) with "
            + "record counts, for building an expression query. All parameters are optional; when supplied they narrow "
            + "the returned facets so a query builder can offer only combinations that have data.", tags = "Expression")
    public ExpressionFacets getExpressionIndexFacets(HttpServletRequest request,
            @Parameter(description = "Optional comma-separated list of Tissue accession ids to filter by (e.g. UBERON:0002107)") @RequestParam(name = "tissueIds", required = false) List<String> tissueIds,
            @Parameter(description = "Optional comma-separated list of Strain accession ids to filter by (e.g. RS:0000134)") @RequestParam(name = "strainAccIds", required = false) List<String> strainAccIds,
            @Parameter(description = "Optional comma-separated list of Condition accession ids to filter by (e.g. XCO:0000025)") @RequestParam(name = "conditionIds", required = false) List<String> conditionIds,
            @Parameter(description = "Optional comma-separated list of Gene RGD IDs to filter by (e.g. 3001,3876)") @RequestParam(name = "rgdIds", required = false) List<Integer> rgdIds,
            @Parameter(description = "Optional assembly map key filter (e.g. 372)") @RequestParam(name = "mapKey", required = false) Integer mapKey,
            @Parameter(description = "Optional comma-separated list of expression levels to filter by (e.g. high,medium)") @RequestParam(name = "expressionLevel", required = false) List<String> expressionLevel,
            @Parameter(description = "Optional comma-separated list of expression units to filter by (e.g. TPM,FPKM)") @RequestParam(name = "units", required = false) List<String> units) throws Exception {
     ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(), request);

        List<Query> filters = buildOptionalFilters(tissueIds, strainAccIds, conditionIds, rgdIds, mapKey, expressionLevel, units);
        Query query = filters.isEmpty()
                ? Query.of(q -> q.matchAll(m -> m))
                : boolFilter(filters.toArray(new Query[0]));
        return computeFacets(query);
    }


    /** Run a paged term/bool query against the expression index and return the page of source documents
     *  together with the total number of matching documents. */
    private ExpressionIndexPage searchExpressionIndex(Query query, int page, int size) throws Exception {
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = DEFAULT_PAGE_SIZE;
        }
        int from = page * size;
        if (from + size > MAX_RESULT_WINDOW) {
            throw new IllegalArgumentException("Requested page is beyond the maximum result window: page*size + size ("
                    + (from + size) + ") must not exceed " + MAX_RESULT_WINDOW + ". Use a smaller page/size.");
        }

        final int fromOffset = from;
        final int pageSize = size;
        ElasticsearchClient client = ClientInit.getClient();
        SearchResponse<ExpressionDataIndexObject> response = client.search(s -> s
                        .index(EXPRESSION_INDEX)
                        .trackTotalHits(t -> t.enabled(true))
                        .from(fromOffset)
                        .size(pageSize)
                        .source(src -> src.filter(f -> f.excludes(SOURCE_EXCLUDES)))
                        .query(query),
                ExpressionDataIndexObject.class);

        List<ExpressionDataIndexObject> records = new ArrayList<>();
        for (Hit<ExpressionDataIndexObject> hit : response.hits().hits()) {
            if (hit.source() != null) {
                records.add(hit.source());
            }
        }
        long total = response.hits().total() != null ? response.hits().total().value() : records.size();
        return new ExpressionIndexPage(total, records);
    }

    /**
     * A page of expression index records. {@code total} is the total number of documents matching the
     * query across all pages; {@code count} is the number of records returned on this page.
     */
    public static class ExpressionIndexPage {
        private final long total;
        private final int count;
        private final List<ExpressionDataIndexObject> records;

        public ExpressionIndexPage(long total, List<ExpressionDataIndexObject> records) {
            this.total = total;
            this.count = records.size();
            this.records = records;
        }

        public long getTotal() {
            return total;
        }

        public int getCount() {
            return count;
        }

        public List<ExpressionDataIndexObject> getRecords() {
            return records;
        }
    }

    /**
     * One page of a search_after walk. A page with no cursor to follow opens a fresh point in time and
     * starts at the beginning; a page with one resumes immediately after the last record of the previous
     * page. Sorting is by _shard_doc, the cheapest total order Elasticsearch can offer and unique within
     * a point in time, which is what lets search_after skip straight to the right spot rather than
     * counting past every preceding document the way offset paging does.
     */
    private ExpressionIndexCursorPage walkExpressionIndex(Query query, int size, String cursor) throws Exception {
        if (size < 1) {
            size = DEFAULT_PAGE_SIZE;
        }
        if (size > MAX_CURSOR_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size (" + size + ") must not exceed "
                    + MAX_CURSOR_PAGE_SIZE + ".");
        }
        ElasticsearchClient client = ClientInit.getClient();

        Cursor position = Cursor.decode(cursor);
        final String pitId = position == null
                ? client.openPointInTime(p -> p.index(EXPRESSION_INDEX).keepAlive(t -> t.time(PIT_KEEP_ALIVE))).id()
                : position.pitId;
        final int pageSize = size;
        final Long after = position == null ? null : position.sortValue;

        SearchResponse<ExpressionDataIndexObject> response = client.search(s -> {
            // A point in time already identifies the index, and naming both is rejected.
            s.pit(p -> p.id(pitId).keepAlive(t -> t.time(PIT_KEEP_ALIVE)))
                    .size(pageSize)
                    .source(src -> src.filter(f -> f.excludes(SOURCE_EXCLUDES)))
                    .sort(so -> so.field(f -> f.field("_shard_doc").order(SortOrder.Asc)))
                    // Counting all matches is the expensive part of the query, so it is paid once, on the
                    // opening page, and then carried along in the cursor.
                    .trackTotalHits(t -> t.enabled(after == null))
                    .query(query);
            if (after != null) {
                s.searchAfter(FieldValue.of(after));
            }
            return s;
        }, ExpressionDataIndexObject.class);

        List<Hit<ExpressionDataIndexObject>> hits = response.hits().hits();
        List<ExpressionDataIndexObject> records = new ArrayList<>();
        for (Hit<ExpressionDataIndexObject> hit : hits) {
            if (hit.source() != null) {
                records.add(hit.source());
            }
        }
        long total = position != null ? position.total
                : (response.hits().total() != null ? response.hits().total().value() : records.size());

        // A short page is the last page: there is nothing left for a follow-up request to return. Closing
        // the point in time here releases its search context rather than leaving it to expire on its own.
        List<FieldValue> lastSort = hits.isEmpty() ? List.of() : hits.get(hits.size() - 1).sort();
        if (hits.size() < pageSize || lastSort.isEmpty()) {
            closeQuietly(client, pitId);
            return new ExpressionIndexCursorPage(total, records, null);
        }
        // Elasticsearch can hand back a different id than the one supplied, and the newest one is the one
        // that stays valid, so the next cursor is always built from the response.
        String livePitId = response.pitId() != null ? response.pitId() : pitId;
        return new ExpressionIndexCursorPage(total, records,
                new Cursor(livePitId, lastSort.get(0).longValue(), total).encode());
    }

    /** Releasing a point in time is cleanup: it expires on its own, so a failure must not fail the page. */
    private static void closeQuietly(ElasticsearchClient client, String pitId) {
        try {
            client.closePointInTime(c -> c.id(pitId));
        } catch (Exception ignored) {
            // keep_alive will reap it
        }
    }

    /**
     * The position of a cursor walk: which point in time it reads, where in that point in time the next
     * page starts, and the match count established when the walk opened. Handed to consumers as one
     * opaque base64 token so the three cannot be recombined into a position that was never valid -- a
     * sort value from one point in time means nothing in another.
     */
    private static class Cursor {
        private final String pitId;
        private final long sortValue;
        private final long total;

        Cursor(String pitId, long sortValue, long total) {
            this.pitId = pitId;
            this.sortValue = sortValue;
            this.total = total;
        }

        /** Newline-separated rather than JSON: a point in time id is base64, so it cannot contain one. */
        String encode() {
            String raw = pitId + "\n" + sortValue + "\n" + total;
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }

        /** @return null for an absent cursor, meaning "start a new walk". */
        static Cursor decode(String cursor) {
            if (cursor == null || cursor.isBlank()) {
                return null;
            }
            String[] parts;
            try {
                parts = new String(Base64.getUrlDecoder().decode(cursor.trim()), StandardCharsets.UTF_8).split("\n");
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed cursor.");
            }
            if (parts.length != 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed cursor.");
            }
            try {
                return new Cursor(parts[0], Long.parseLong(parts[1]), Long.parseLong(parts[2]));
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed cursor.");
            }
        }
    }

    /**
     * A page of a cursor walk. {@code total} is the number of documents matching the query, counted when
     * the walk opened; {@code count} is the number of records on this page; {@code cursor} is the token to
     * pass back for the next page, and is null on the last page.
     *
     * <p>A walk that is abandoned rather than read to its end leaves a point in time behind, which
     * Elasticsearch reclaims once PIT_KEEP_ALIVE lapses without a request.
     */
    public static class ExpressionIndexCursorPage {
        private final long total;
        private final int count;
        private final String cursor;
        private final List<ExpressionDataIndexObject> records;

        public ExpressionIndexCursorPage(long total, List<ExpressionDataIndexObject> records, String cursor) {
            this.total = total;
            this.count = records.size();
            this.cursor = cursor;
            this.records = records;
        }

        public long getTotal() {
            return total;
        }

        public int getCount() {
            return count;
        }

        public String getCursor() {
            return cursor;
        }

        public List<ExpressionDataIndexObject> getRecords() {
            return records;
        }
    }

    /**
     * Run a filtered, zero-hit search that only computes terms aggregations, and return the bucket
     * counts as aggName -> (bucketKey -> docCount). Buckets preserve Elasticsearch's count-descending order.
     */
    private Map<String, Map<String, Long>> aggregateExpressionIndex(Query query, Map<String, String> aggFields) throws Exception {
        ElasticsearchClient client = ClientInit.getClient();
        SearchResponse<Void> response = client.search(s -> {
            s.index(EXPRESSION_INDEX).size(0).query(query);
            for (Map.Entry<String, String> agg : aggFields.entrySet()) {
                String field = agg.getValue();
                s.aggregations(agg.getKey(), a -> a.terms(t -> t.field(field).size(AGG_BUCKET_SIZE)));
            }
            return s;
        }, Void.class);

        Map<String, Map<String, Long>> result = new LinkedHashMap<>();
        for (String aggName : aggFields.keySet()) {
            Map<String, Long> buckets = new LinkedHashMap<>();
            for (StringTermsBucket bucket : response.aggregations().get(aggName).sterms().buckets().array()) {
                buckets.put(bucket.key().stringValue(), bucket.docCount());
            }
            result.put(aggName, buckets);
        }
        return result;
    }

    /** Exact-match term query on a keyword field. */
    private static Query termQuery(String field, String value) {
        return Query.of(q -> q.term(t -> t.field(field).value(value)));
    }

    /** Exact-match term query on a numeric (long) field. */
    private static Query termQuery(String field, long value) {
        return Query.of(q -> q.term(t -> t.field(field).value(value)));
    }

    /** Exact-match terms query on a keyword field (matches any of the supplied values). */
    private static Query termsQuery(String field, List<String> values) {
        List<FieldValue> fieldValues = new ArrayList<>();
        for (String value : values) {
            fieldValues.add(FieldValue.of(value));
        }
        TermsQueryField terms = TermsQueryField.of(t -> t.value(fieldValues));
        return Query.of(q -> q.terms(t -> t.field(field).terms(terms)));
    }

    /**
     * Match an ontology accession either on the record's own field or on its indexed ancestors, so a
     * query for a parent term (e.g. UBERON:0000949 endocrine system) also returns the records annotated
     * to its descendants. Values are trimmed here, so callers need not pre-trim.
     *
     * parentTermAccIds holds the ancestors of every ontology field on a record, not just this one, so in
     * principle a tissue filter could match on a strain ancestor -- in practice accessions are namespaced
     * per ontology (UBERON:/RS:/XCO:), so a tissue accession never appears among a strain's ancestors.
     */
    private static Query termsWithDescendants(String accField, List<String> accIds) {
        List<String> values = trimAll(accIds);
        return Query.of(q -> q.bool(b -> b
                .should(termsQuery(accField, values))
                .should(termsQuery(PARENT_TERM_FIELD, values))
                .minimumShouldMatch("1")));
    }

    /** Single-accession form of {@link #termsWithDescendants(String, List)}. */
    private static Query termWithDescendants(String accField, String accId) {
        return termsWithDescendants(accField, List.of(accId));
    }

    /** Trim each value of a comma-separated request parameter, so " UBERON:0002107" still matches. */
    private static List<String> trimAll(List<String> values) {
        List<String> out = new ArrayList<>();
        for (String value : values) {
            out.add(value == null ? null : value.trim());
        }
        return out;
    }

    /** RGD ids arrive as ints but the terms query builder takes string field values. */
    private static List<String> toStrings(List<Integer> values) {
        List<String> out = new ArrayList<>();
        for (Integer value : values) {
            out.add(String.valueOf(value));
        }
        return out;
    }

    /** Combine term queries as filter clauses (exact match, no scoring). */
    private static Query boolFilter(Query... filters) {
        return Query.of(q -> q.bool(b -> b.filter(List.of(filters))));
    }

    /**
     * Run a single zero-hit search that computes terms aggregations for every query dimension, then
     * shape the buckets into the facet lists a query builder needs. Tissue, strain and condition buckets
     * are resolved to ontology term names; gene buckets are split from the indexed "Symbol-RGD:id" form.
     */
    private ExpressionFacets computeFacets(Query query) throws Exception {
        ElasticsearchClient client = ClientInit.getClient();
        SearchResponse<Void> resp = client.search(s -> s
                        .index(EXPRESSION_INDEX)
                        .size(0)
                        .query(query)
                        .aggregations("tissues", a -> a.terms(t -> t.field("tissueAcc.keyword").size(AGG_BUCKET_SIZE)))
                        .aggregations("strains", a -> a.terms(t -> t.field("strainAcc.keyword").size(AGG_BUCKET_SIZE)))
                        // A record carries a list of conditions, so it lands in one bucket per condition and the
                        // condition bucket counts sum to more than the total hit count -- unlike the single-valued
                        // tissue/strain facets.
                        .aggregations("conditions", a -> a.terms(t -> t.field("conditions.accId.keyword").size(AGG_BUCKET_SIZE)))
                        .aggregations("genes",   a -> a.terms(t -> t.field("geneSymbolWithRgdId.keyword").size(AGG_BUCKET_SIZE)))
                        .aggregations("mapKeys", a -> a.terms(t -> t.field("mapKey").size(AGG_BUCKET_SIZE)))
                        .aggregations("units",   a -> a.terms(t -> t.field("expressionUnit.keyword").size(AGG_BUCKET_SIZE)))
                        .aggregations("levels",  a -> a.terms(t -> t.field("expressionLevel.keyword").size(AGG_BUCKET_SIZE))),
                Void.class);

        ExpressionFacets facets = new ExpressionFacets();
        for (Bucket b : stringBuckets(resp, "tissues")) {
            facets.tissues.add(new FacetValue(b.key, resolveTermLabel(b.key), b.count));
        }
        for (Bucket b : stringBuckets(resp, "strains")) {
            facets.strains.add(new FacetValue(b.key, resolveTermLabel(b.key), b.count));
        }
        for (Bucket b : stringBuckets(resp, "conditions")) {
            facets.conditions.add(new FacetValue(b.key, resolveTermLabel(b.key), b.count));
        }
        for (Bucket b : stringBuckets(resp, "genes")) {
            // indexed as "Lepr-RGD:3001": acc is the RGD id (what the records endpoint wants), label is the symbol
            String acc = b.key, labelText = b.key;
            int marker = b.key.indexOf("-RGD:");
            if (marker > 0) {
                labelText = b.key.substring(0, marker);
                acc = b.key.substring(marker + "-RGD:".length());
            }
            facets.genes.add(new FacetValue(acc, labelText, b.count));
        }
        for (Bucket b : numericBuckets(resp, "mapKeys")) {
            facets.mapKeys.add(new FacetValue(b.key, b.key, b.count));
        }
        for (Bucket b : stringBuckets(resp, "units")) {
            facets.units.add(new FacetValue(b.key, b.key, b.count));
        }
        for (Bucket b : stringBuckets(resp, "levels")) {
            facets.levels.add(new FacetValue(b.key, b.key, b.count));
        }
        return facets;
    }

//    /** Fill tissueTerm/strainTerm on a page of records from the ontology, so consumers get names, not just accessions. */
//    private void fillOntologyLabels(List<ExpressionDataIndexObject> records) {
//        for (ExpressionDataIndexObject r : records) {
//            if (isBlank(r.getTissueTerm()) && !isBlank(r.getTissueAcc())) {
//                String label = resolveTermLabel(r.getTissueAcc());
//                if (!label.equals(r.getTissueAcc())) {
//                    r.setTissueTerm(label);
//                }
//            }
//            if (isBlank(r.getStrainTerm()) && !isBlank(r.getStrainAcc())) {
//                String label = resolveTermLabel(r.getStrainAcc());
//                if (!label.equals(r.getStrainAcc())) {
//                    r.setStrainTerm(label);
//                }
//            }
//        }
//    }

    /**
     * Resolve an ontology accession to its term name, cached across requests. Returns the accession
     * itself when no term is found, so callers always have something displayable.
     */
    private String resolveTermLabel(String accId) {
        if (isBlank(accId)) {
            return accId;
        }
        String cached = termLabelCache.get(accId);
        if (cached != null) {
            return NO_LABEL.equals(cached) ? accId : cached;
        }
        String resolved = null;
        try {
            Term term = odao.getTermByAccId(accId);
            if (term != null && !isBlank(term.getTerm())) {
                resolved = term.getTerm();
            }
        } catch (Exception e) {
            resolved = null; // a lookup failure is not fatal to the query -- fall back to the accession
        }
        termLabelCache.put(accId, resolved != null ? resolved : NO_LABEL);
        return resolved != null ? resolved : accId;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** A single aggregation bucket, normalized so string- and numeric-keyed aggregations read the same. */
    private static final class Bucket {
        final String key;
        final long count;
        Bucket(String key, long count) { this.key = key; this.count = count; }
    }

    private static List<Bucket> stringBuckets(SearchResponse<Void> resp, String aggName) {
        List<Bucket> out = new ArrayList<>();
        Aggregate agg = resp.aggregations().get(aggName);
        if (agg != null && agg.isSterms()) {
            for (StringTermsBucket b : agg.sterms().buckets().array()) {
                out.add(new Bucket(b.key().stringValue(), b.docCount()));
            }
        }
        return out;
    }

    private static List<Bucket> numericBuckets(SearchResponse<Void> resp, String aggName) {
        List<Bucket> out = new ArrayList<>();
        Aggregate agg = resp.aggregations().get(aggName);
        if (agg == null) {
            return out;
        }
        if (agg.isLterms()) {
            for (LongTermsBucket b : agg.lterms().buckets().array()) {
                out.add(new Bucket(String.valueOf(b.key()), b.docCount()));
            }
        } else if (agg.isSterms()) { // defensive: if mapKey is ever indexed as a keyword
            for (StringTermsBucket b : agg.sterms().buckets().array()) {
                out.add(new Bucket(b.key().stringValue(), b.docCount()));
            }
        }
        return out;
    }

    /** One selectable value in a facet: the accession/id to send back in a query, a display label, and its record count. */
    public static class FacetValue {
        private final String acc;
        private final String label;
        private final long count;

        public FacetValue(String acc, String label, long count) {
            this.acc = acc;
            this.label = label;
            this.count = count;
        }

        public String getAcc() { return acc; }
        public String getLabel() { return label; }
        public long getCount() { return count; }
    }

    /** The set of facet lists returned to the query builder, each ordered by descending record count. */
    public static class ExpressionFacets {
        private final List<FacetValue> tissues = new ArrayList<>();
        private final List<FacetValue> strains = new ArrayList<>();
        private final List<FacetValue> conditions = new ArrayList<>();
        private final List<FacetValue> genes = new ArrayList<>();
        private final List<FacetValue> mapKeys = new ArrayList<>();
        private final List<FacetValue> units = new ArrayList<>();
        private final List<FacetValue> levels = new ArrayList<>();

        // Every field needs a getter: Jackson serializes by getter, so dropping one silently omits the
        // facet from the response even though computeFacets fills the field.
        public List<FacetValue> getTissues() { return tissues; }
        public List<FacetValue> getStrains() { return strains; }
        public List<FacetValue> getConditions() { return conditions; }
        public List<FacetValue> getGenes() { return genes; }
        public List<FacetValue> getMapKeys() { return mapKeys; }
        public List<FacetValue> getUnits() { return units; }
        public List<FacetValue> getLevels() { return levels; }
    }

}
