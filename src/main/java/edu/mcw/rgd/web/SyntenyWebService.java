package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.SyntenyDAO;
import edu.mcw.rgd.datamodel.SyntenicRegion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by mtutaj on 11/08/2021
 */

@RestController
@Tag(name="Synteny")
@RequestMapping(value = "/synteny")
public class SyntenyWebService {

    SyntenyDAO sdao = new SyntenyDAO();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/blocks/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return all synteny blocks for given backbone region", tags = "Synteny")
    public List<SyntenicRegion> getBlocks(HttpServletRequest request, @Parameter(description="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
                                          @Parameter(description="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
                                          @Parameter(description="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
                                          @Parameter(description="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
                                          @Parameter(description="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
    }

    @RequestMapping(value="/blocks/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method= RequestMethod.GET)
    @Operation(summary="Return all synteny blocks for given backbone region", tags = "Synteny")
    public List<SyntenicRegion> getBlocks(HttpServletRequest request,@Parameter(description="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
                                          @Parameter(description="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
                                          @Parameter(description="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
                                          @Parameter(description="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
                                          @Parameter(description="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey,
                                          @Parameter(description="Chain Level (1, 2, etc or range: 1-2)", required=true) @PathVariable(value = "chainLevel") String chainLevel ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        int dashPos = chainLevel.indexOf('-');
        if( dashPos<0 ) {
            int level = Integer.parseInt(chainLevel);
            return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, level);
        }
        int minLevel = Integer.parseInt(chainLevel.substring(0, dashPos));
        int maxLevel = Integer.parseInt(chainLevel.substring(dashPos+1));
        return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, minLevel, maxLevel);
    }

    @RequestMapping(value="/gaps/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return gaps for given backbone region", tags = "Synteny")
    public List<SyntenicRegion> getGaps(HttpServletRequest request,@Parameter(description="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
                                          @Parameter(description="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
                                          @Parameter(description="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
                                          @Parameter(description="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
                                          @Parameter(description="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
    }

    @RequestMapping(value="/gaps/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method= RequestMethod.GET)
    @Operation(summary="Return gaps for given backbone region", tags = "Synteny")
    public List<SyntenicRegion> getGaps(HttpServletRequest request,@Parameter(description="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
                                          @Parameter(description="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
                                          @Parameter(description="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
                                          @Parameter(description="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
                                          @Parameter(description="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey,
                                          @Parameter(description="Chain Level (1, 2, etc, or range: '1-2')", required=true) @PathVariable(value = "chainLevel") String chainLevel ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        int dashPos = chainLevel.indexOf('-');
        if( dashPos<0 ) {
            int level = Integer.parseInt(chainLevel);
            return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, level);
        }
        int minLevel = Integer.parseInt(chainLevel.substring(0, dashPos));
        int maxLevel = Integer.parseInt(chainLevel.substring(dashPos+1));
        return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, minLevel, maxLevel);
    }
}
