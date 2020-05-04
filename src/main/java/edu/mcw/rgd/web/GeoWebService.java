package edu.mcw.rgd.web;


import edu.mcw.rgd.dao.impl.PhenominerDAO;
import edu.mcw.rgd.dao.impl.SSLPDAO;
import edu.mcw.rgd.datamodel.MappedSSLP;
import edu.mcw.rgd.datamodel.pheno.Study;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@RestController
@Api(tags="Geo")
@RequestMapping(value = "/geo")
public class GeoWebService {

    PhenominerDAO pdao = new PhenominerDAO();

    @RequestMapping(value = "/{species}", method = RequestMethod.GET)
    @ApiOperation(value = "Returns a list experiments in geo for species", tags = "Geo")
    public List<Study> getGeoStudies(@ApiParam(value = "Species", required = true) @PathVariable(value = "species") String species) throws Exception {

        List<Study> result = new ArrayList<Study>();
        HashMap<String,Study> r = pdao.getGeoStudies(species);
        for(String gse:r.keySet()){
            result.add(r.get(gse));
        }
        return result;
    }
}