package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.MapDAO;
import edu.mcw.rgd.datamodel.Chromosome;
import edu.mcw.rgd.datamodel.Map;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.util.TreeMap;
import java.util.List;
import java.util.Set;
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

    @RequestMapping(value="/chr/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of chromosomes", tags="Chromosome")
    public Set<String> getChromosomesByAssembly(
            @ApiParam(value="mapKey", required=true) @PathVariable(value = "mapKey") int mapKey

    ) throws Exception{

        MapDAO mdao= new MapDAO();
       Set<String> chromosomes = ((TreeMap)mdao.getChromosomeSizes(mapKey)).keySet();
        return chromosomes;
    }

    @RequestMapping(value="/chr/{chromosome}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of chromosomes", tags="Chromosome")
    public Chromosome getChromosomeByAssembly(
            @ApiParam(value="chromosome", required=true) @PathVariable(value = "chromosome") String chromosome,
            @ApiParam(value="mapKey", required=true) @PathVariable(value = "mapKey") int mapKey


    ) throws Exception{

        MapDAO mdao= new MapDAO();

        return mdao.getChromosome(mapKey,chromosome);
    }
}
