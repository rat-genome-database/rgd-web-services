package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.MapDAO;
import edu.mcw.rgd.datamodel.Chromosome;
import edu.mcw.rgd.datamodel.Map;
import edu.mcw.rgd.process.mapping.MapManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    MapDAO mdao= new MapDAO();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/{speciesTypeKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of assemblies", tags="Map")
    public List<Map> getMapsBySpecies(HttpServletRequest request,
                                      @ApiParam(value="species Key", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<Map> maps = mdao.getMaps(speciesTypeKey,"bp");
        return maps;
    }

    @RequestMapping(value="/chr/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of chromosomes", tags="Chromosome")
    public Set<String> getChromosomesByAssembly(HttpServletRequest request,
            @ApiParam(value="mapKey", required=true) @PathVariable(value = "mapKey") int mapKey

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return getChromosomesByMapKey(mapKey);
    }

    @RequestMapping(value="/chrForSpecies/{speciesTypeKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of chromosomes for primary assembly of given species", tags="Chromosome")
    public Set<String> getChromosomesByPrimaryAssembly(HttpServletRequest request,
            @ApiParam(value="Species Type Key", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        int mapKey = MapManager.getInstance().getReferenceAssembly(speciesTypeKey).getKey();
        return getChromosomesByMapKey(mapKey);
    }

    private Set<String> getChromosomesByMapKey(int mapKey)  throws Exception {
        Set<String> chromosomes = ((TreeMap)mdao.getChromosomeSizes(mapKey)).keySet();
        return chromosomes;
    }

    @RequestMapping(value="/chr/{chromosome}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of chromosomes", tags="Chromosome")
    public Chromosome getChromosomeByAssembly(HttpServletRequest request,
            @ApiParam(value="chromosome", required=true) @PathVariable(value = "chromosome") String chromosome,
            @ApiParam(value="mapKey", required=true) @PathVariable(value = "mapKey") int mapKey


    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return mdao.getChromosome(mapKey,chromosome);
    }
}
