package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.datamodel.ontologyx.TermDagEdge;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
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
    @RequestMapping(value="/term/{accId1}/{accId2}", method=RequestMethod.GET)
    @ApiOperation(value="Returns true or false for terms", tags="Ontology")
    public boolean isDescendantOf(
            @ApiParam(value="Child Term Accession ID", required=true)
            @PathVariable(value = "accId1") String accId1,
            @ApiParam(value="Parent Term Accession ID", required=true)
            @PathVariable(value = "accId2") String accId2

    ) throws Exception{

        return oDAO.isDescendantOf(accId1,accId2);

    }

    @RequestMapping(value="/ont/{ontId}", method=RequestMethod.GET)
    @ApiOperation(value="Returns term for Accession ID", tags="Ontology")
    public HashMap<String,ArrayList<String>> getOntDags(
            @ApiParam(value="Ontology ID", required=true)
            @PathVariable(value = "ontId") String ontId

    ) throws Exception{


        List<TermDagEdge> d = oDAO.getAllDagsForOntology(ontId);
        HashMap<String,ArrayList<String>> data = new HashMap<>();
        ArrayList<String> terms;
        for(TermDagEdge t: d){
            if(data.size() == 0 || !data.containsKey(t.getChildTermAcc()))
                terms = new ArrayList<>();
            else terms = data.get(t.getChildTermAcc());
            terms.add(t.getParentTermAcc());

            data.put(t.getChildTermAcc(),terms);
        }
        return data;
    }
}
