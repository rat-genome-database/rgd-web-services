package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.AbstractDAO;
import edu.mcw.rgd.dao.impl.MapDAO;
import edu.mcw.rgd.dao.impl.SyntenyDAO;
import edu.mcw.rgd.dao.spring.MappedGeneQuery;
import edu.mcw.rgd.datamodel.Chromosome;
import edu.mcw.rgd.datamodel.MappedGene;
import edu.mcw.rgd.datamodel.SpeciesType;
import edu.mcw.rgd.datamodel.SyntenicRegion;
import edu.mcw.rgd.domain.vcmap.ChromosomeEx;
import edu.mcw.rgd.domain.vcmap.SpeciesMaps;
import edu.mcw.rgd.process.Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by mtutaj on 11/18/2021
 */

@RestController
@Api(tags="VCMap")
@RequestMapping(value = "/vcmap")
public class VcmapWebService {

    MapDAO mapDAO = new MapDAO();
    SyntenyDAO sdao = new SyntenyDAO();

    @RequestMapping(value="/blocks/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return all synteny blocks for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getBlocks2(
            @ApiParam(value="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
            @ApiParam(value="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
            @ApiParam(value="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
            @ApiParam(value="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
            @ApiParam(value="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value="Minimum Backbone Block Size (optional)") @RequestParam(required = false) Integer threshold
    ) throws Exception {

        if( threshold==null ) {
            return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
        } else {
            return sdao.getSizedBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey);
        }
    }

    @RequestMapping(value="/blocks/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method= RequestMethod.GET)
    @ApiOperation(value="Return all synteny blocks for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getBlocks(
            @ApiParam(value="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
            @ApiParam(value="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
            @ApiParam(value="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
            @ApiParam(value="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
            @ApiParam(value="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value="Chain Level (1, 2, etc or range: 1-2)", required=true) @PathVariable(value = "chainLevel") String chainLevel,
            @ApiParam(value="Minimum Backbone Block Size (optional)") @RequestParam(required = false) Integer threshold ) throws Exception {

        int dashPos = chainLevel.indexOf('-');
        if( dashPos<0 ) {
            int level = Integer.parseInt(chainLevel);
            if( threshold!=null ) {
                return sdao.getSizedBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, level);
            } else {
                return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, level);
            }
        }
        int minLevel = Integer.parseInt(chainLevel.substring(0, dashPos));
        int maxLevel = Integer.parseInt(chainLevel.substring(dashPos+1));
        if( threshold!=null ) {
            return sdao.getSizedBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, minLevel, maxLevel);
        } else {
            return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, minLevel, maxLevel);
        }
    }

    @RequestMapping(value="/gaps/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return gaps for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getGaps(
            @ApiParam(value="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
            @ApiParam(value="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
            @ApiParam(value="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
            @ApiParam(value="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
            @ApiParam(value="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value="Minimum Backbone Gap Size (optional)") @RequestParam(required = false) Integer threshold ) throws Exception {

        if( threshold!=null ) {
            return sdao.getSizedGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey);
        } else {
            return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
        }
    }

    @RequestMapping(value="/gaps/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method= RequestMethod.GET)
    @ApiOperation(value="Return gaps for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getGaps(
            @ApiParam(value="Backbone Species Map Key (available through lookup service)", required=true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
            @ApiParam(value="Backbone Chromosome", required=true) @PathVariable(value = "backboneChr") String backboneChr,
            @ApiParam(value="Backbone Start Position", required=true) @PathVariable(value = "backboneStart") int backboneStart,
            @ApiParam(value="Backbone Stop Position", required=true) @PathVariable(value = "backboneStop") int backboneStop,
            @ApiParam(value="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value="Chain Level (1, 2, etc, or range: '1-2')", required=true) @PathVariable(value = "chainLevel") String chainLevel,
            @ApiParam(value="Minimum Backbone Gap Size (optional)") @RequestParam(required = false) Integer threshold ) throws Exception {

        int dashPos = chainLevel.indexOf('-');
        if( dashPos<0 ) {
            int level = Integer.parseInt(chainLevel);
            if( threshold!=null ) {
                return sdao.getSizedGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, level);
            } else {
                return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, level);
            }
        }
        int minLevel = Integer.parseInt(chainLevel.substring(0, dashPos));
        int maxLevel = Integer.parseInt(chainLevel.substring(dashPos+1));
        if( threshold!=null ) {
            return sdao.getSizedGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, minLevel, maxLevel);
        } else {
            return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, minLevel, maxLevel);
        }
    }

    @RequestMapping(value="/species", method= RequestMethod.GET)
    @ApiOperation(value="Return genomic maps for public species in RGD", tags = "VCMap")
    public List<SpeciesMaps> getSpeciesMaps() throws Exception {

        List<SpeciesMaps> results = new ArrayList<>();

        for( int speciesTypeKey: SpeciesType.getSpeciesTypeKeys() ) {
            if( SpeciesType.isSearchable(speciesTypeKey) ) {

                SpeciesMaps sm = new SpeciesMaps();
                sm.speciesTypeKey = speciesTypeKey;
                sm.name = SpeciesType.getCommonName(speciesTypeKey);
                sm.maps = mapDAO.getMaps(speciesTypeKey, "bp", "NCBI");
                results.add(sm);
            }
        }

        return results;
    }

    @RequestMapping(value="/maps/{mapKey}/chromosomes", method= RequestMethod.GET)
    @ApiOperation(value="Return chromosome hashmap for given map key", tags = "VCMap")
    public Map<String, ChromosomeEx> getChrMaps(@ApiParam(value="Map Key", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception {

        Map<String, ChromosomeEx> results = new HashMap<>();

        List<Chromosome> chrList = mapDAO.getChromosomes(mapKey);
        for( Chromosome c: chrList ) {

            ChromosomeEx chr = new ChromosomeEx(c);
            results.put(chr.getChromosome(), chr);
        }
        return results;
    }

    @RequestMapping(value="/genes/map/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return genes with position given symbol prefix, f.e. symbol=hox returns all HOXxxx genes", tags = "VCMap")
    public List<MappedGene> getGenesForSymbol(
            @ApiParam(value="Map Key", required=true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value="Gene symbol prefix (optional, case insensitive)") @RequestParam(required = false) String symbolPrefix ) throws Exception {

        return getActiveGenes(symbolPrefix, mapKey);
    }

    List<MappedGene> getActiveGenes(String symbolPrefix, int mapKey) throws Exception {

        String sql;
        if( Utils.isStringEmpty(symbolPrefix) ) {
            sql = "SELECT g.*,m.*,r.species_type_key FROM genes g, rgd_ids r, maps_data m " +
                    "WHERE r.object_status='ACTIVE' AND g.rgd_id=m.rgd_id AND m.map_key=? " +
                    " AND NVL(gene_type_lc,'*') NOT IN('splice','allele') " +
                    " AND r.rgd_id=g.rgd_id";
            return MappedGeneQuery.run(mapDAO, sql, mapKey);
        } else {
            sql = "SELECT g.*,m.*,r.species_type_key FROM genes g, rgd_ids r, maps_data m " +
                    "WHERE r.object_status='ACTIVE' AND g.rgd_id=m.rgd_id AND m.map_key=? " +
                    " AND NVL(gene_type_lc,'*') NOT IN('splice','allele') " +
                    " AND r.rgd_id=g.rgd_id AND g.gene_symbol_lc LIKE ?";
            return MappedGeneQuery.run(mapDAO, sql, mapKey, symbolPrefix.toLowerCase()+"%");
        }
    }
}
