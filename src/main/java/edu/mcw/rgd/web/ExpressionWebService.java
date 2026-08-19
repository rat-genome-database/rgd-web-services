package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.GeneExpressionDAO;
import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.dao.impl.PhenominerDAO;
import edu.mcw.rgd.datamodel.GeneExpression;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.datamodel.pheno.Experiment;
import edu.mcw.rgd.datamodel.pheno.GeneExpressionRecord;
import edu.mcw.rgd.datamodel.pheno.GeneExpressionRecordValue;
import edu.mcw.rgd.datamodel.pheno.Record;
import edu.mcw.rgd.datamodel.pheno.Sample;
import edu.mcw.rgd.datamodel.pheno.Study;
import edu.mcw.rgd.process.mapping.MapManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    // Ontology term names are effectively static, so cache accession -> name
    // across requests. A sentinel value records "looked up, no term found" so
    // a bad accession is not re-queried on every hit.
    private final Map<String, String> termLabelCache = new ConcurrentHashMap<>();
    private static final String NO_LABEL = " "; // ConcurrentHashMap forbids null values

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

    private String resolveTermLabel(String accId) {
        if (isBlank(accId)) return accId;
        String cached = termLabelCache.get(accId);
        if (cached != null) return NO_LABEL.equals(cached) ? accId : cached;
        String resolved = null;
        try {
            Term term = odao.getTermByAccId(accId);
            if (term != null && !isBlank(term.getTerm())) {
                resolved = term.getTerm();
            }
        } catch (Exception e) {
            resolved = null;
        }
        termLabelCache.put(accId, resolved != null ? resolved : NO_LABEL);
        return resolved != null ? resolved : accId;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
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

}
