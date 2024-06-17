package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.GeneExpressionDAO;
import edu.mcw.rgd.dao.impl.PhenominerDAO;
import edu.mcw.rgd.datamodel.pheno.Experiment;
import edu.mcw.rgd.datamodel.pheno.GeneExpressionRecord;
import edu.mcw.rgd.datamodel.pheno.GeneExpressionRecordValue;
import edu.mcw.rgd.datamodel.pheno.Study;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name="Expression")
@RequestMapping(value = "/expression")
public class ExpressionWebService {
    AccessLogDAO ald = new AccessLogDAO();
    GeneExpressionDAO gdao = new GeneExpressionDAO();
    PhenominerDAO pdao = new PhenominerDAO();

    @RequestMapping(value="/{ontTerm}/{rgdId}/{unit}", method = RequestMethod.GET)
    @Operation(summary="return a list of Expression Values", tags="Expression")
    public List<GeneExpressionRecordValue> getGeneExpressionValuesByOntTermRgdIdUnit(HttpServletRequest request,
                                                                                     @Parameter(description = "Ontology Term Id", required = true) @PathVariable(name = "ontTerm") String ontTerm,
                                                                                     @Parameter(description = "Gene RGD ID", required = true) @PathVariable(name = "rgdId") int rgdId,
                                                                                     @Parameter(description = "Associated unit: TPM|FPKM",required = true) @PathVariable(name = "unit") String unit) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return gdao.getGeneExprRecordValuesForGeneByTermRgdIdUnit(rgdId,unit.toUpperCase(),ontTerm);
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

    @RequestMapping(value = "/ExpressionRecord/{ontTerm}/{rgdId}/{unit}", method = RequestMethod.GET)
    @Operation(summary = "return a list of Gene Expression Records", tags = "Expression")
    public List<GeneExpressionRecord> getGeneExpressionRecordsByExpressionValues(HttpServletRequest request,
                                                                                 @Parameter(description = "Ontology Term Id", required = true) @PathVariable(name = "ontTerm") String ontTerm,
                                                                                 @Parameter(description = "Gene RGD ID", required = true) @PathVariable(name = "rgdId") int rgdId,
                                                                                 @Parameter(description = "Associated unit: TPM|FPKM",required = true) @PathVariable(name = "unit") String unit) throws Exception {
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return gdao.getGeneExpressionRecordsByRecordValues(rgdId,unit.toUpperCase(),ontTerm);
    }

}
