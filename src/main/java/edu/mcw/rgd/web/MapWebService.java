package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.MapDAO;
import edu.mcw.rgd.datamodel.Map;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by hsnalabolu on 4/9/2019.
 */
@RestController
@Api(tags="Map")
@RequestMapping(value = "/maps")
public class MapWebService {

    @RequestMapping(value="/{speciesTypeKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of assemblies", tags="Map")
    public List<Map> getMapsBySpecies(
            @ApiParam(value="species Key", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey

    ) throws Exception{

        MapDAO mdao= new MapDAO();
        List<Map> maps = mdao.getMaps(speciesTypeKey,"bp");
        return maps;
    }

}
