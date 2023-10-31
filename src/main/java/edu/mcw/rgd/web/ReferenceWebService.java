//package edu.mcw.rgd.web;
//
//import edu.mcw.rgd.dao.impl.AccessLogDAO;
//import edu.mcw.rgd.dao.impl.GeneDAO;
//import edu.mcw.rgd.dao.impl.ReferenceDAO;
//import edu.mcw.rgd.datamodel.Gene;
//import edu.mcw.rgd.datamodel.Reference;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiParam;
//import io.swagger.v3.oas.annotations.Operation;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@Tag(name="Reference")
//@RequestMapping(value = "/references")
//public class ReferenceWebService {
//    ReferenceDAO referenceDAO = new ReferenceDAO();
//    AccessLogDAO ald = new AccessLogDAO();
//    @RequestMapping(value="/{rgdId}", method= RequestMethod.GET)
//    @Operation(summary="Return a list of rgd object references", tags="Reference")
//    public List<Reference> getGeneOrthologs(HttpServletRequest request, @Parameter(description="RGD ID of RGD object", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{
//        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
//        return referenceDAO.getReferencesForObject(rgdId);
//    }
//}
