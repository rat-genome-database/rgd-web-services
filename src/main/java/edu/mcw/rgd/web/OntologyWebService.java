package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by mtutaj on 11/1/2016.
 */
@RestController
@Api(tags="Ontology")
@RequestMapping(value = "/ontology")
public class OntologyWebService {

    OntologyXDAO oDAO = new OntologyXDAO();

    @RequestMapping(value="/term/{accId}", method=RequestMethod.GET)
    @ApiOperation(value="Returns term for Accession ID", tags="Ontology")
    public Term getTerm(
            @ApiParam(value="Term Accession ID", required=true)
            @PathVariable(value = "accId") String accId

    ) throws Exception{


        return oDAO.getTermByAccId(accId);

    }
}
