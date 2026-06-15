package edu.mcw.rgd.web;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
                                                                             @Parameter(description = "Zero-based page number") @RequestParam(name = "page", defaultValue = "0") int page,
                                                                             @Parameter(description = "Page size (max 10000)") @RequestParam(name = "size", defaultValue = "1000") int size) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);

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
                        .query(q -> q.term(t -> t.field("tissueAcc.keyword").value(tissueId))),
                ExpressionDataIndexObject.class);

        List<ExpressionDataIndexObject> records = new ArrayList<>();
        for (Hit<ExpressionDataIndexObject> hit : response.hits().hits()) {
            if (hit.source() != null) {
                records.add(hit.source());
            }
        }
        return records;
    }

}
