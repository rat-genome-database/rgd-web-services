package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.MapDAO;
import edu.mcw.rgd.datamodel.Chromosome;
import edu.mcw.rgd.datamodel.Map;
import edu.mcw.rgd.process.mapping.MapManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.TreeMap;
import java.util.List;
import java.util.Set;
/**
 * Created by hsnalabolu on 4/9/2019.
 */
@RestController
@Tag(name="Map")
@RequestMapping(value = "/maps")
public class MapWebService {

    MapDAO mdao= new MapDAO();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/{speciesTypeKey}", method= RequestMethod.GET)
    @Operation(summary="Return a list of assemblies", tags="Map")
    public List<Map> getMapsBySpecies(HttpServletRequest request,
                                      @Parameter(description="species Key", required=true) @PathVariable(name = "speciesTypeKey") int speciesTypeKey

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<Map> maps = mdao.getMaps(speciesTypeKey,"bp");
        return maps;
    }

    @RequestMapping(value="/chr/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return a list of chromosomes", tags="Chromosome")
    public Set<String> getChromosomesByAssembly(HttpServletRequest request,
            @Parameter(description="mapKey", required=true) @PathVariable(value = "mapKey") int mapKey

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return getChromosomesByMapKey(mapKey);
    }

    @RequestMapping(value="/chrForSpecies/{speciesTypeKey}", method= RequestMethod.GET)
    @Operation(summary="Return a list of chromosomes for primary assembly of given species", tags="Chromosome")
    public Set<String> getChromosomesByPrimaryAssembly(HttpServletRequest request,
            @Parameter(description="Species Type Key", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey

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
    @Operation(summary="Return a list of chromosomes", tags="Chromosome")
    public Chromosome getChromosomeByAssembly(HttpServletRequest request,
            @Parameter(description="chromosome", required=true) @PathVariable(value = "chromosome") String chromosome,
            @Parameter(description="mapKey", required=true) @PathVariable(value = "mapKey") int mapKey


    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return mdao.getChromosome(mapKey,chromosome);
    }

    @RequestMapping(value="/assembly/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return a mapped assembly", tags="Map")
    public Map getMapByAssembly(HttpServletRequest request,
                                                @Parameter(description="mapKey", required=true) @PathVariable(value = "mapKey") int mapKey

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return mdao.getMap(mapKey);
    }
}
