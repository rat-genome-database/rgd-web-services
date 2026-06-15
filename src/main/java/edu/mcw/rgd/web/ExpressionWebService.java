package edu.mcw.rgd.web;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.GeneExpressionDAO;
import edu.mcw.rgd.dao.impl.PhenominerDAO;
import edu.mcw.rgd.datamodel.GeneExpression;
import edu.mcw.rgd.datamodel.expression.ExpressionDataIndexObject;
import edu.mcw.rgd.datamodel.pheno.Experiment;
import edu.mcw.rgd.datamodel.pheno.GeneExpressionRecord;
import edu.mcw.rgd.datamodel.pheno.GeneExpressionRecordValue;
import edu.mcw.rgd.datamodel.pheno.Record;
import edu.mcw.rgd.datamodel.pheno.Study;
import edu.mcw.rgd.services.ClientInit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name="Expression")
@RequestMapping(value = "/expression")
public class ExpressionWebService {
    AccessLogDAO ald = new AccessLogDAO();
    GeneExpressionDAO gdao = new GeneExpressionDAO();
    PhenominerDAO pdao = new PhenominerDAO();

    private static final String EXPRESSION_INDEX = "expressiondata-dn_index_dev1";
    private static final int DEFAULT_PAGE_SIZE = 1000;
    // Elasticsearch's default index.max_result_window: from + size cannot exceed this for offset paging.
    private static final int MAX_RESULT_WINDOW = 10000;

    @RequestMapping(value="/{ontTerm}/{rgdId}/{unit}", method = RequestMethod.GET)
    @Operation(summary="return a list of Expression Values", tags="Expression")
    public List<GeneExpression> getGeneExpressionValuesByOntTermRgdIdUnit(HttpServletRequest request,
                                                                          @Parameter(description = "Ontology Term Id", required = true) @PathVariable(name = "ontTerm") String ontTerm,
                                                                          @Parameter(description = "Gene RGD ID", required = true) @PathVariable(name = "rgdId") int rgdId,
                                                                          @Parameter(description = "Associated unit: TPM|FPKM",required = true) @PathVariable(name = "unit") String unit) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return gdao.getGeneExpressionObjectsByTermRgdIdUnit(ontTerm,rgdId,unit.toUpperCase());
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

    @RequestMapping(value = "/index/records/{tissueId}", method = RequestMethod.GET)
    @Operation(summary = "return a page of expression records for a tissue from the Elasticsearch expression index", tags = "Expression")
    public List<ExpressionDataIndexObject> getExpressionIndexRecordsByTissue(HttpServletRequest request,
                                                                             @Parameter(description = "Tissue ontology term accession id (e.g. UBERON:0002107)", required = true) @PathVariable(name = "tissueId") String tissueId,
                                                                             @Parameter(description = "Optional expression level filter (e.g. high|medium|low)") @RequestParam(name = "expressionLevel", required = false) String expressionLevel,
                                                                             @Parameter(description = "Zero-based page number") @RequestParam(name = "page", defaultValue = "0") int page,
                                                                             @Parameter(description = "Page size (max 10000)") @RequestParam(name = "size", defaultValue = "1000") int size) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return searchExpressionIndex(termQuery("tissueAcc.keyword", tissueId), expressionLevel, page, size);
    }

    @RequestMapping(value = "/index/records/strain/{strainAcc}", method = RequestMethod.GET)
    @Operation(summary = "return a page of expression records for a strain from the Elasticsearch expression index", tags = "Expression")
    public List<ExpressionDataIndexObject> getExpressionIndexRecordsByStrain(HttpServletRequest request,
                                                                             @Parameter(description = "Strain ontology term accession id (e.g. RS:0000029)", required = true) @PathVariable(name = "strainAcc") String strainAcc,
                                                                             @Parameter(description = "Optional expression level filter (e.g. high|medium|low)") @RequestParam(name = "expressionLevel", required = false) String expressionLevel,
                                                                             @Parameter(description = "Zero-based page number") @RequestParam(name = "page", defaultValue = "0") int page,
                                                                             @Parameter(description = "Page size (max 10000)") @RequestParam(name = "size", defaultValue = "1000") int size) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return searchExpressionIndex(termQuery("strainAcc.keyword", strainAcc), expressionLevel, page, size);
    }

    @RequestMapping(value = "/index/records/gene/{geneRgdId}", method = RequestMethod.GET)
    @Operation(summary = "return a page of expression records for a gene from the Elasticsearch expression index", tags = "Expression")
    public List<ExpressionDataIndexObject> getExpressionIndexRecordsByGene(HttpServletRequest request,
                                                                           @Parameter(description = "Gene RGD ID", required = true) @PathVariable(name = "geneRgdId") int geneRgdId,
                                                                           @Parameter(description = "Optional expression level filter (e.g. high|medium|low)") @RequestParam(name = "expressionLevel", required = false) String expressionLevel,
                                                                           @Parameter(description = "Zero-based page number") @RequestParam(name = "page", defaultValue = "0") int page,
                                                                           @Parameter(description = "Page size (max 10000)") @RequestParam(name = "size", defaultValue = "1000") int size) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return searchExpressionIndex(termQuery("geneRgdId.keyword", String.valueOf(geneRgdId)), expressionLevel, page, size);
    }

    @RequestMapping(value = "/index/records/genes", method = RequestMethod.GET)
    @Operation(summary = "return a page of expression records for a list of genes from the Elasticsearch expression index", tags = "Expression")
    public List<ExpressionDataIndexObject> getExpressionIndexRecordsByGenes(HttpServletRequest request,
                                                                            @Parameter(description = "Comma-separated list of Gene RGD IDs (e.g. 2004,1303,69417)", required = true) @RequestParam(name = "rgdIds") List<Integer> rgdIds,
                                                                            @Parameter(description = "Optional expression level filter (e.g. high|medium|low)") @RequestParam(name = "expressionLevel", required = false) String expressionLevel,
                                                                            @Parameter(description = "Zero-based page number") @RequestParam(name = "page", defaultValue = "0") int page,
                                                                            @Parameter(description = "Page size (max 10000)") @RequestParam(name = "size", defaultValue = "1000") int size) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<String> values = new ArrayList<>();
        for (Integer rgdId : rgdIds) {
            values.add(String.valueOf(rgdId));
        }
        return searchExpressionIndex(termsQuery("geneRgdId.keyword", values), expressionLevel, page, size);
    }

    @RequestMapping(value = "/index/records/{tissueId}/{strainAcc}", method = RequestMethod.GET)
    @Operation(summary = "return a page of expression records for a tissue and strain from the Elasticsearch expression index", tags = "Expression")
    public List<ExpressionDataIndexObject> getExpressionIndexRecordsByTissueAndStrain(HttpServletRequest request,
                                                                                      @Parameter(description = "Tissue ontology term accession id (e.g. UBERON:0002107)", required = true) @PathVariable(name = "tissueId") String tissueId,
                                                                                      @Parameter(description = "Strain ontology term accession id (e.g. RS:0000029)", required = true) @PathVariable(name = "strainAcc") String strainAcc,
                                                                                      @Parameter(description = "Optional expression level filter (e.g. high|medium|low)") @RequestParam(name = "expressionLevel", required = false) String expressionLevel,
                                                                                      @Parameter(description = "Zero-based page number") @RequestParam(name = "page", defaultValue = "0") int page,
                                                                                      @Parameter(description = "Page size (max 10000)") @RequestParam(name = "size", defaultValue = "1000") int size) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        Query query = boolFilter(
                termQuery("tissueAcc.keyword", tissueId),
                termQuery("strainAcc.keyword", strainAcc));
        return searchExpressionIndex(query, expressionLevel, page, size);
    }

    @RequestMapping(value = "/index/records/gene/{geneRgdId}/tissue/{tissueId}", method = RequestMethod.GET)
    @Operation(summary = "return a page of expression records for a gene and tissue from the Elasticsearch expression index", tags = "Expression")
    public List<ExpressionDataIndexObject> getExpressionIndexRecordsByGeneAndTissue(HttpServletRequest request,
                                                                                    @Parameter(description = "Gene RGD ID", required = true) @PathVariable(name = "geneRgdId") int geneRgdId,
                                                                                    @Parameter(description = "Tissue ontology term accession id (e.g. UBERON:0002107)", required = true) @PathVariable(name = "tissueId") String tissueId,
                                                                                    @Parameter(description = "Optional expression level filter (e.g. high|medium|low)") @RequestParam(name = "expressionLevel", required = false) String expressionLevel,
                                                                                    @Parameter(description = "Zero-based page number") @RequestParam(name = "page", defaultValue = "0") int page,
                                                                                    @Parameter(description = "Page size (max 10000)") @RequestParam(name = "size", defaultValue = "1000") int size) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        Query query = boolFilter(
                termQuery("geneRgdId.keyword", String.valueOf(geneRgdId)),
                termQuery("tissueAcc.keyword", tissueId));
        return searchExpressionIndex(query, expressionLevel, page, size);
    }

    @RequestMapping(value = "/index/records/gene/{geneRgdId}/strain/{strainAcc}", method = RequestMethod.GET)
    @Operation(summary = "return a page of expression records for a gene and strain from the Elasticsearch expression index", tags = "Expression")
    public List<ExpressionDataIndexObject> getExpressionIndexRecordsByGeneAndStrain(HttpServletRequest request,
                                                                                    @Parameter(description = "Gene RGD ID", required = true) @PathVariable(name = "geneRgdId") int geneRgdId,
                                                                                    @Parameter(description = "Strain ontology term accession id (e.g. RS:0000029)", required = true) @PathVariable(name = "strainAcc") String strainAcc,
                                                                                    @Parameter(description = "Optional expression level filter (e.g. high|medium|low)") @RequestParam(name = "expressionLevel", required = false) String expressionLevel,
                                                                                    @Parameter(description = "Zero-based page number") @RequestParam(name = "page", defaultValue = "0") int page,
                                                                                    @Parameter(description = "Page size (max 10000)") @RequestParam(name = "size", defaultValue = "1000") int size) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        Query query = boolFilter(
                termQuery("geneRgdId.keyword", String.valueOf(geneRgdId)),
                termQuery("strainAcc.keyword", strainAcc));
        return searchExpressionIndex(query, expressionLevel, page, size);
    }

    @RequestMapping(value = "/index/records/gene/{geneRgdId}/strain/{strainAcc}/tissue/{tissueId}", method = RequestMethod.GET)
    @Operation(summary = "return a page of expression records for a gene, strain and tissue from the Elasticsearch expression index", tags = "Expression")
    public List<ExpressionDataIndexObject> getExpressionIndexRecordsByGeneStrainAndTissue(HttpServletRequest request,
                                                                                          @Parameter(description = "Gene RGD ID", required = true) @PathVariable(name = "geneRgdId") int geneRgdId,
                                                                                          @Parameter(description = "Strain ontology term accession id (e.g. RS:0000029)", required = true) @PathVariable(name = "strainAcc") String strainAcc,
                                                                                          @Parameter(description = "Tissue ontology term accession id (e.g. UBERON:0002107)", required = true) @PathVariable(name = "tissueId") String tissueId,
                                                                                          @Parameter(description = "Optional expression level filter (e.g. high|medium|low)") @RequestParam(name = "expressionLevel", required = false) String expressionLevel,
                                                                                          @Parameter(description = "Zero-based page number") @RequestParam(name = "page", defaultValue = "0") int page,
                                                                                          @Parameter(description = "Page size (max 10000)") @RequestParam(name = "size", defaultValue = "1000") int size) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        Query query = boolFilter(
                termQuery("geneRgdId.keyword", String.valueOf(geneRgdId)),
                termQuery("strainAcc.keyword", strainAcc),
                termQuery("tissueAcc.keyword", tissueId));
        return searchExpressionIndex(query, expressionLevel, page, size);
    }

    /**
     * Combine the base query with an optional expression-level filter, then run the paged search.
     * When expressionLevel is null or blank, the base query is used unchanged.
     */
    private List<ExpressionDataIndexObject> searchExpressionIndex(Query query, String expressionLevel, int page, int size) throws Exception {
        if (expressionLevel != null && !expressionLevel.isBlank()) {
            query = boolFilter(query, termQuery("expressionLevel.keyword", expressionLevel));
        }
        return searchExpressionIndex(query, page, size);
    }

    /** Run a paged term/bool query against the expression index and return the page of source documents. */
    private List<ExpressionDataIndexObject> searchExpressionIndex(Query query, int page, int size) throws Exception {
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
                        .from(fromOffset)
                        .size(pageSize)
                        .query(query),
                ExpressionDataIndexObject.class);

        List<ExpressionDataIndexObject> records = new ArrayList<>();
        for (Hit<ExpressionDataIndexObject> hit : response.hits().hits()) {
            if (hit.source() != null) {
                records.add(hit.source());
            }
        }
        return records;
    }

    /** Exact-match term query on a keyword field. */
    private static Query termQuery(String field, String value) {
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

    /** Combine term queries as filter clauses (exact match, no scoring). */
    private static Query boolFilter(Query... filters) {
        return Query.of(q -> q.bool(b -> b.filter(List.of(filters))));
    }

}
