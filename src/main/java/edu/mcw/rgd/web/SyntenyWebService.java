package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.SyntenyDAO;
import edu.mcw.rgd.datamodel.SyntenicRegion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by mtutaj on 11/08/2021
 */

@RestController
@Api(tags="Synteny")
@RequestMapping(value = "/synteny")
public class SyntenyWebService {

    SyntenyDAO sdao = new SyntenyDAO();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/blocks/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return all synteny blocks for given backbone region", tags = "Synteny")
    public List<SyntenicRegion> getBlocks(HttpServletRequest request, @ApiParam(value="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
                                          @ApiParam(value="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
                                          @ApiParam(value="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
                                          @ApiParam(value="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
                                          @ApiParam(value="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
    }

    @RequestMapping(value="/blocks/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method= RequestMethod.GET)
    @ApiOperation(value="Return all synteny blocks for given backbone region", tags = "Synteny")
    public List<SyntenicRegion> getBlocks(HttpServletRequest request,@ApiParam(value="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
                                          @ApiParam(value="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
                                          @ApiParam(value="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
                                          @ApiParam(value="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
                                          @ApiParam(value="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey,
                                          @ApiParam(value="Chain Level (1, 2, etc or range: 1-2)", required=true) @PathVariable(value = "chainLevel") String chainLevel ) throws Exception {

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
    @ApiOperation(value="Return gaps for given backbone region", tags = "Synteny")
    public List<SyntenicRegion> getGaps(HttpServletRequest request,@ApiParam(value="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
                                          @ApiParam(value="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
                                          @ApiParam(value="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
                                          @ApiParam(value="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
                                          @ApiParam(value="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
    }

    @RequestMapping(value="/gaps/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method= RequestMethod.GET)
    @ApiOperation(value="Return gaps for given backbone region", tags = "Synteny")
    public List<SyntenicRegion> getGaps(HttpServletRequest request,@ApiParam(value="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
                                          @ApiParam(value="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
                                          @ApiParam(value="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
                                          @ApiParam(value="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
                                          @ApiParam(value="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey,
                                          @ApiParam(value="Chain Level (1, 2, etc, or range: '1-2')", required=true) @PathVariable(value = "chainLevel") String chainLevel ) throws Exception {

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
