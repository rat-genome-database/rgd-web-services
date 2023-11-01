package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.datamodel.ontologyx.TermDagEdge;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mtutaj on 11/1/2016.
 */
@RestController
@Tag(name="Ontology")
@RequestMapping(value = "/ontology")
public class OntologyWebService {

    OntologyXDAO oDAO = new OntologyXDAO();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/term/{accId}", method=RequestMethod.GET)
    @Operation(summary="Returns term for Accession ID", tags="Ontology")
    public Term getTerm(HttpServletRequest request,
                        @Parameter(description="Term Accession ID", required=true)
            @PathVariable(name = "accId") String accId

    ) throws Exception{


        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return oDAO.getTermByAccId(accId);

    }
    @RequestMapping(value="/term/{accId1}/{accId2}", method=RequestMethod.GET)
    @Operation(summary="Returns true or false for terms", tags="Ontology")
    public boolean isDescendantOf(HttpServletRequest request,
            @Parameter(description="Child Term Accession ID", required=true)
            @PathVariable(name = "accId1") String accId1,
            @Parameter(description="Parent Term Accession ID", required=true)
            @PathVariable(name = "accId2") String accId2

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return oDAO.isDescendantOf(accId1,accId2);

    }

    @RequestMapping(value="/ont/{accId}", method=RequestMethod.GET)
    @Operation(summary="Returns child and parent terms for Accession ID", tags="Ontology")
    public HashMap<String,List<String>> getOntDags(HttpServletRequest request,
            @Parameter(description="Accession ID", required=true)
            @PathVariable(name = "accId") String accId

    ) throws Exception{


        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<String> childTerms = oDAO.getAllActiveTermDescendantAccIds(accId);
        List<String> parentTerms = oDAO.getAllActiveTermAncestorAccIds(accId);
        HashMap<String,List<String>> data = new HashMap<>();
        data.put("childTerms",childTerms);
        data.put("parentTerms",parentTerms);
        return data;
    }
}
