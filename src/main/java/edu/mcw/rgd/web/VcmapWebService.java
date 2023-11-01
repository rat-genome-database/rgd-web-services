package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.DataSourceFactory;
import edu.mcw.rgd.dao.impl.*;
import edu.mcw.rgd.dao.impl.variants.VariantDAO;
import edu.mcw.rgd.dao.spring.MappedGeneQuery;
import edu.mcw.rgd.datamodel.*;
import edu.mcw.rgd.datamodel.variants.VariantMapData;
import edu.mcw.rgd.domain.vcmap.ChromosomeEx;
import edu.mcw.rgd.domain.vcmap.MappedGeneEx;
import edu.mcw.rgd.domain.vcmap.MappedGeneQueryEx;
import edu.mcw.rgd.domain.vcmap.SpeciesMaps;
import edu.mcw.rgd.process.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.Map;

/**
 * Created by mtutaj on 11/18/2021
 */

@RestController
@Tag(name="VCMap")
@RequestMapping(value = "/vcmap")
public class VcmapWebService {

    GeneDAO geneDAO = new GeneDAO();
    MapDAO mapDAO = new MapDAO();
    SyntenyDAO sdao = new SyntenyDAO();

   GenomeSignalDAO gsdao = new GenomeSignalDAO();

    VariantDAO vdao = new VariantDAO();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value = "/blocks/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}", method = RequestMethod.GET)
    @Operation(summary = "Return all synteny blocks for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getBlocks2(HttpServletRequest request,
                                           @Parameter(description = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(name = "backboneMapKey") int backboneMapKey,
                                           @Parameter(description = "Backbone Chromosome", required = true) @PathVariable(name = "backboneChr") String backboneChr,
                                           @Parameter(description = "Backbone Start Position", required = true) @PathVariable(name = "backboneStart") int backboneStart,
                                           @Parameter(description = "Backbone Stop Position", required = true) @PathVariable(name = "backboneStop") int backboneStop,
                                           @Parameter(description = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(name = "mapKey") int mapKey,
                                           @Parameter(description = "Minimum Backbone Block Size (optional)") @RequestParam(required = false) Integer threshold
    ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        if (threshold == null) {
            return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
        } else {
            return sdao.getSizedBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey);
        }
    }

    @RequestMapping(value = "/blocks/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method = RequestMethod.GET)
    @Operation(summary = "Return all synteny blocks for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getBlocks(HttpServletRequest request,
            @Parameter(description = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(name = "backboneMapKey") int backboneMapKey,
            @Parameter(description = "Backbone Chromosome", required = true) @PathVariable(name = "backboneChr") String backboneChr,
            @Parameter(description = "Backbone Start Position", required = true) @PathVariable(name = "backboneStart") int backboneStart,
            @Parameter(description = "Backbone Stop Position", required = true) @PathVariable(name = "backboneStop") int backboneStop,
            @Parameter(description = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(name = "mapKey") int mapKey,
            @Parameter(description = "Chain Level (1, 2, etc or range: 1-2)", required = true) @PathVariable(name = "chainLevel") String chainLevel,
            @Parameter(description = "Minimum Backbone Block Size (optional)") @RequestParam(required = false) Integer threshold) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        int dashPos = chainLevel.indexOf('-');
        if (dashPos < 0) {
            int level = Integer.parseInt(chainLevel);
            if (threshold != null) {
                return sdao.getSizedBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, level);
            } else {
                return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, level);
            }
        }
        int minLevel = Integer.parseInt(chainLevel.substring(0, dashPos));
        int maxLevel = Integer.parseInt(chainLevel.substring(dashPos + 1));
        if (threshold != null) {
            return sdao.getSizedBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, minLevel, maxLevel);
        } else {
            return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, minLevel, maxLevel);
        }
    }

    @RequestMapping(value = "/gaps/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}", method = RequestMethod.GET)
    @Operation(summary = "Return gaps for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getGaps(HttpServletRequest request,
            @Parameter(description = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(name = "backboneMapKey") int backboneMapKey,
            @Parameter(description = "Backbone Chromosome", required = true) @PathVariable(name = "backboneChr") String backboneChr,
            @Parameter(description = "Backbone Start Position", required = true) @PathVariable(name = "backboneStart") int backboneStart,
            @Parameter(description = "Backbone Stop Position", required = true) @PathVariable(name = "backboneStop") int backboneStop,
            @Parameter(description = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(name = "mapKey") int mapKey,
            @Parameter(description = "Minimum Backbone Gap Size (optional)") @RequestParam(required = false) Integer threshold) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        if (threshold != null) {
            return sdao.getSizedGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey);
        } else {
            return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
        }
    }

    @RequestMapping(value = "/gaps/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method = RequestMethod.GET)
    @Operation(summary = "Return gaps for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getGaps(HttpServletRequest request,
            @Parameter(description = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(name = "backboneMapKey") int backboneMapKey,
            @Parameter(description = "Backbone Chromosome", required = true) @PathVariable(name = "backboneChr") String backboneChr,
            @Parameter(description = "Backbone Start Position", required = true) @PathVariable(name = "backboneStart") int backboneStart,
            @Parameter(description = "Backbone Stop Position", required = true) @PathVariable(name = "backboneStop") int backboneStop,
            @Parameter(description = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(name = "mapKey") int mapKey,
            @Parameter(description = "Chain Level (1, 2, etc, or range: '1-2')", required = true) @PathVariable(name = "chainLevel") String chainLevel,
            @Parameter(description = "Minimum Backbone Gap Size (optional)") @RequestParam(required = false) Integer threshold) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        int dashPos = chainLevel.indexOf('-');
        if (dashPos < 0) {
            int level = Integer.parseInt(chainLevel);
            if (threshold != null) {
                return sdao.getSizedGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, level);
            } else {
                return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, level);
            }
        }
        int minLevel = Integer.parseInt(chainLevel.substring(0, dashPos));
        int maxLevel = Integer.parseInt(chainLevel.substring(dashPos + 1));
        if (threshold != null) {
            return sdao.getSizedGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, minLevel, maxLevel);
        } else {
            return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, minLevel, maxLevel);
        }
    }


    @RequestMapping(value = "/synteny/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}", method = RequestMethod.GET)
    @Operation(summary = "Return all synteny blocks and gaps for given backbone region", tags = "VCMap")
    public List<Map<String, Object>> getSynteny(HttpServletRequest request,
            @Parameter(description = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(name = "backboneMapKey") int backboneMapKey,
            @Parameter(description = "Backbone Chromosome", required = true) @PathVariable(name = "backboneChr") String backboneChr,
            @Parameter(description = "Backbone Start Position", required = true) @PathVariable(name = "backboneStart") int backboneStart,
            @Parameter(description = "Backbone Stop Position", required = true) @PathVariable(name = "backboneStop") int backboneStop,
            @Parameter(description = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(name = "mapKey") int mapKey,
            @Parameter(description = "Minimum Backbone Block/Gap Size (optional)") @RequestParam(required = false) Integer threshold,
            @Parameter(description = "Minimum Backbone Block/Gap Size (optional)") @RequestParam(required = false) Integer thresholdStart,
            @Parameter(description = "Maximum Backbone Block/Gap Size (optional)") @RequestParam(required = false) Integer thresholdEnd,
            @Parameter(description = "Include Genes, if set to 1 (optional)") @RequestParam(required = false) Integer includeGenes,
            @Parameter(description = "Include Orthologs, if set to 1 (optional)") @RequestParam(required = false) Integer includeOrthologs,
            @Parameter(description = "Include Orthologs for the map keys specified (optional)") @RequestParam(required = false) String orthologMapKeys
    ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        boolean withGenes = includeGenes!=null && includeGenes>0;
        boolean withOrthologs = includeOrthologs!=null && includeOrthologs>0;

        List<Integer> orthologMapKeyList = null;
        if( !Utils.isStringEmpty(orthologMapKeys) ) {
            try {
                String[] orthos = orthologMapKeys.split("[,]");
                List<Integer> orthoValues = new ArrayList<>();
                for( String ortho: orthos ) {
                    int orthoMapKey = Integer.parseInt(ortho);
                    orthoValues.add(orthoMapKey);
                }
                orthologMapKeyList = orthoValues;
                withOrthologs = false;
            } catch( Exception ignore ) {} // ignore if not a list of integers
        }

        List<SyntenicRegion> blocks;
        List<SyntenicRegion> gaps;

        int minSize = 0, maxSize = 0;
        if( threshold!=null && threshold>0 ) {
            minSize = threshold;
        }
        if( thresholdStart!=null && thresholdStart>0 ) {
            minSize = thresholdStart;
        }
        if( thresholdEnd!=null && thresholdEnd>0 ) {
            maxSize = thresholdEnd;
        }

        if( minSize>0 && maxSize==0 ) {
            maxSize = Integer.MAX_VALUE;
        }

        if( minSize==0 && maxSize==0 ) {
            blocks = sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
            gaps = sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
        } else {
            blocks = sdao.getBlocksInRange(backboneMapKey, backboneChr, backboneStart, backboneStop, minSize, maxSize, mapKey);
            gaps = sdao.getGapsInRange(backboneMapKey, backboneChr, backboneStart, backboneStop, minSize, maxSize, mapKey);
        }

        List<Map<String, Object>> results = combineBlocksAndGaps(blocks, gaps, withGenes, withOrthologs, orthologMapKeyList);
        return results;
    }

    @RequestMapping(value = "/synteny/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method = RequestMethod.GET)
    @Operation(summary = "Return all synteny blocks and gaps for given backbone region", tags = "VCMap")
    public List<Map<String, Object>> getSynteny(HttpServletRequest request,
            @Parameter(description = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(name = "backboneMapKey") int backboneMapKey,
            @Parameter(description = "Backbone Chromosome", required = true) @PathVariable(name = "backboneChr") String backboneChr,
            @Parameter(description = "Backbone Start Position", required = true) @PathVariable(name = "backboneStart") int backboneStart,
            @Parameter(description = "Backbone Stop Position", required = true) @PathVariable(name = "backboneStop") int backboneStop,
            @Parameter(description = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(name = "mapKey") int mapKey,
            @Parameter(description = "Chain Level (1, 2, etc, or range: '1-2')", required = true) @PathVariable(name = "chainLevel") String chainLevel,
            @Parameter(description = "Minimum Backbone Block/Gap Size (optional)") @RequestParam(required = false) Integer threshold
    ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<SyntenicRegion> blocks;
        List<SyntenicRegion> gaps;

        int dashPos = chainLevel.indexOf('-');
        if (dashPos < 0) {
            int level = Integer.parseInt(chainLevel);
            if (threshold != null) {
                blocks = sdao.getSizedBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, level);
                gaps = sdao.getSizedGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, level);
            } else {
                blocks = sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, level);
                gaps = sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, level);
            }
        } else {
            int minLevel = Integer.parseInt(chainLevel.substring(0, dashPos));
            int maxLevel = Integer.parseInt(chainLevel.substring(dashPos + 1));
            if (threshold != null) {
                blocks = sdao.getSizedBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, minLevel, maxLevel);
                gaps = sdao.getSizedGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey, minLevel, maxLevel);
            } else {
                blocks = sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, minLevel, maxLevel);
                gaps = sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey, minLevel, maxLevel);
            }
        }

        List<Map<String, Object>> results = combineBlocksAndGaps(blocks, gaps, false, false, null);
        return results;
    }

    List<Map<String, Object>> combineBlocksAndGaps(List<SyntenicRegion> blocks, List<SyntenicRegion> gaps, boolean withGenes,
                                                   boolean withOrthologs, List<Integer> orthologMapKeys) throws Exception {

        List<Map<String, Object>> results = new ArrayList<>();

        for( SyntenicRegion block: blocks ) {

            Map<String, Object> synteny = new HashMap<>();
            results.add(synteny);

            synteny.put("block", block);

            ArrayList<SyntenicRegion> gapsWithinBlock = new ArrayList<>();

            // skip gaps with start position lower than block start pos
            int i;
            for (i = 0; i < gaps.size(); i++) {
                if (gaps.get(i).getBackboneStart() >= block.getBackboneStart()) {
                    break;
                }
            }
            // add gaps that are within block
            for (; i < gaps.size(); i++) {
                SyntenicRegion gap = gaps.get(i);
                if (gap.getBackboneStop() <= block.getBackboneStop()) {
                    gapsWithinBlock.add(gap);
                } else {
                    break;
                }
            }
            if (gapsWithinBlock.size() > 0) {
                synteny.put("gaps", gapsWithinBlock);
            }


            // any genes in the synteny target block?
            if( withGenes ) {
                List<MappedGeneEx> genes = MappedGeneEx.getActiveGenesInRegion(mapDAO, block.getChromosome(), block.getStart(), block.getStop(), block.getMapKey());

                if( orthologMapKeys!=null ) {

                    // note: 'genes' object is coming from a shared cache -- if you modify any fields, like orthologs,
                    //       you have to create a copy first
                    List<MappedGeneEx> genes2 = cloneGeneList(genes);

                    for( int orthologMapKey: orthologMapKeys ) {
                        Map<Integer, List<Integer>> orthoMap = MappedGeneEx.getOrthologMap(mapDAO, block.getMapKey(), orthologMapKey);

                        for (MappedGeneEx g : genes2) {
                            List<Integer> orthoGenes = orthoMap.get(g.geneRgdId);
                            if( orthoGenes!=null ) {
                                HashMap<Integer, List<Integer>> omap = (HashMap<Integer, List<Integer>>) g.orthologs;
                                if (omap == null) {
                                    omap = new HashMap<>();
                                    g.orthologs = omap;
                                }
                                omap.put(orthologMapKey, orthoGenes);
                            }
                        }
                    }
                    synteny.put("genes", genes2);
                }
                else if( withOrthologs ) {
                    // note: 'genes' object is coming from a shared cache -- if you modify any fields, like orthologs,
                    //       you have to create a copy first
                    List<MappedGeneEx> genes2 = cloneGeneList(genes);

                    Map<Integer, List<Integer>> orthoMap = MappedGeneEx.getOrthologMap(mapDAO, block.getMapKey(), block.getBackboneMapKey());

                    for( MappedGeneEx g: genes2 ) {
                        g.orthologs = orthoMap.get(g.geneRgdId);
                    }
                    synteny.put("genes", genes2);

                } else {
                    synteny.put("genes", genes);
                }
            }
        }
        return results;
    }

    List<MappedGeneEx> cloneGeneList(List<MappedGeneEx> genes) throws CloneNotSupportedException {
        List<MappedGeneEx> result = new ArrayList<>(genes.size());
        for( int i=0; i<genes.size(); i++ ) {
            result.add( (MappedGeneEx) genes.get(i).clone() );
        }
        return result;
    }

    @RequestMapping(value="/species", method= RequestMethod.GET)
    @Operation(summary="Return genomic maps for public species in RGD", tags = "VCMap")
    public List<SpeciesMaps> getSpeciesMaps(HttpServletRequest request) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
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
    @Operation(summary="Return chromosome hashmap for given map key", tags = "VCMap")
    public Map<String, ChromosomeEx> getChrMaps(HttpServletRequest request,@Parameter(description="Map Key", required=true) @PathVariable(name = "mapKey") int mapKey) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        Map<String, ChromosomeEx> results = new HashMap<>();

        List<Chromosome> chrList = mapDAO.getChromosomes(mapKey);
        for( Chromosome c: chrList ) {

            ChromosomeEx chr = new ChromosomeEx(c);
            results.put(chr.getChromosome(), chr);
        }
        return results;
    }

    @RequestMapping(value="/genes/map/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return genes with position given symbol prefix, f.e. symbol=hox returns all HOXxxx genes", tags = "VCMap")
    public List<MappedGene> getGenesForSymbol(HttpServletRequest request,
            @Parameter(description="Map Key", required=true) @PathVariable(name = "mapKey") int mapKey,
            @Parameter(description="Gene symbol prefix (optional, case insensitive)") @RequestParam(required = false) String symbolPrefix ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
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

    @RequestMapping(value="/genes/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return genes with position given symbol prefix, f.e. symbol=hox returns all HOXxxx genes", tags = "VCMap")
    public List<MappedGeneEx> getGenesForSymbol2(HttpServletRequest request,
            @Parameter(description="Map Key", required=true) @PathVariable(name = "mapKey") int mapKey,
            @Parameter(description="Gene symbol prefix (optional, case insensitive)") @RequestParam(required = false) String symbolPrefix ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        String sql;
        if( Utils.isStringEmpty(symbolPrefix) ) {
            sql = "SELECT g.rgd_id,g.gene_symbol,g.full_name,g.gene_type_lc,m.map_key,m.chromosome,m.start_pos,m.stop_pos,m.strand "+
                    "FROM genes g, rgd_ids r, maps_data m " +
                    "WHERE r.object_status='ACTIVE' AND g.rgd_id=m.rgd_id AND m.map_key=? " +
                    " AND NVL(gene_type_lc,'*') NOT IN('splice','allele') " +
                    " AND r.rgd_id=g.rgd_id";
            return MappedGeneQueryEx.execute(mapDAO, sql, mapKey);
        } else {
            sql = "SELECT g.rgd_id,g.gene_symbol,g.full_name,g.gene_type_lc,m.map_key,m.chromosome,m.start_pos,m.stop_pos,m.strand "+
                    "FROM genes g, rgd_ids r, maps_data m " +
                    "WHERE r.object_status='ACTIVE' AND g.rgd_id=m.rgd_id AND m.map_key=? " +
                    " AND NVL(gene_type_lc,'*') NOT IN('splice','allele') " +
                    " AND r.rgd_id=g.rgd_id AND g.gene_symbol_lc LIKE ?";
            return MappedGeneQueryEx.execute(mapDAO, sql, mapKey, symbolPrefix.toLowerCase()+"%");
        }
    }

    @RequestMapping(value="/genes/mapped/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of genes position and map key", tags="VCMap")
    public List<MappedGene> getMappedGenesByPosition(HttpServletRequest request,
        @Parameter(description="Chromosome", required=true) @PathVariable(name = "chr") String chr,
        @Parameter(description="Start Position", required=true) @PathVariable(name = "start") int start,
        @Parameter(description="Stop Position", required=true) @PathVariable(name = "stop") int stop,
        @Parameter(description="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(name = "mapKey") int mapKey,
        @Parameter(description = "Minimum Gene Size (optional)") @RequestParam(required = false) Integer threshold) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        if( threshold==null ) {
            return geneDAO.getActiveMappedGenes(chr.toUpperCase(), start, stop, mapKey);
        } else {
            return getActiveMappedGenes(chr.toUpperCase(), start, stop, mapKey, threshold);
        }
    }

    List<MappedGene> getActiveMappedGenes(String chr, int startPos, int stopPos, int mapKey, int minLen) throws Exception {
        String query = "SELECT g.*, r.species_type_key, g.gene_symbol as symbol, r.species_type_key, md.* "+
                "FROM genes g, rgd_ids r, maps_data md "+
                "WHERE r.object_status='ACTIVE' AND r.rgd_id=g.rgd_id AND md.rgd_id=g.rgd_id "+
                "AND md.chromosome=? AND md.start_pos<=? AND md.stop_pos>=? AND md.map_key=? AND md.stop_pos-md.start_pos>? "+
                "ORDER BY md.start_pos";

        return MappedGeneQuery.run(mapDAO, query, chr, stopPos, startPos, mapKey, minLen);
    }

    @RequestMapping(value="/genes/{mapKey}/{chr}/{start}/{stop}", method=RequestMethod.GET)
    @Operation(summary="Return a list of genes with positions in a given region", tags="VCMap")
    public List<MappedGeneEx> getMappedGenesByPosition2(HttpServletRequest request,
            @Parameter(description="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(name = "mapKey") int mapKey,
            @Parameter(description="Chromosome", required=true) @PathVariable(name = "chr") String chr,
            @Parameter(description="Start Position", required=true) @PathVariable(name = "start") int start,
            @Parameter(description="Stop Position", required=true) @PathVariable(name = "stop") int stop,
            @Parameter(description="Minimum Gene Size (optional)") @RequestParam(required = false) Integer threshold,
            @Parameter(description="Include rgd ids for ortholog genes given a list of comma separated map keys (optional)") @RequestParam(required = false) String orthologMapKeys) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<MappedGeneEx> genes;
        if( threshold==null ) {
            String query = "SELECT g.rgd_id,g.gene_symbol,g.full_name,g.gene_type_lc,m.map_key,m.chromosome,m.start_pos,m.stop_pos,m.strand "+
                    "FROM genes g, rgd_ids r, maps_data m "+
                    "WHERE r.object_status='ACTIVE' AND r.rgd_id=g.rgd_id AND m.rgd_id=g.rgd_id "+
                    "AND m.chromosome=? AND m.start_pos<=? AND m.stop_pos>=? AND m.map_key=? "+
                    "ORDER BY m.start_pos";

            genes = MappedGeneQueryEx.execute(mapDAO, query, chr, stop, start, mapKey);
        } else {
            String query = "SELECT g.rgd_id,g.gene_symbol,g.full_name,g.gene_type_lc,m.map_key,m.chromosome,m.start_pos,m.stop_pos,m.strand "+
                    "FROM genes g, rgd_ids r, maps_data m "+
                    "WHERE r.object_status='ACTIVE' AND r.rgd_id=g.rgd_id AND m.rgd_id=g.rgd_id "+
                    "AND m.chromosome=? AND m.start_pos<=? AND m.stop_pos>=? AND m.map_key=? AND m.stop_pos-m.start_pos>? "+
                    "ORDER BY m.start_pos";

            genes = MappedGeneQueryEx.execute(mapDAO, query, chr, stop, start, mapKey, threshold);
        }

        if( orthologMapKeys!=null ) {
            String[] mapKeyArr = orthologMapKeys.split("[,]");
            int[] mapKeys = new int[mapKeyArr.length];
            for( int i=0; i<mapKeys.length; i++ ) {
                try {
                    mapKeys[i] = Integer.parseInt(mapKeyArr[i]);
                } catch( NumberFormatException ignore ) {
                }
            }

            for( int oMapKey: mapKeys ) {
                if( oMapKey==0 ) {
                    continue;
                }
                Map<Integer, List<Integer>> orthos = MappedGeneEx.getOrthologMap(geneDAO, mapKey, oMapKey);
                for( MappedGeneEx g: genes ) {
                    List<Integer> oList = orthos.get(g.geneRgdId);
                    if( oList!=null ) {
                        if( g.orthologs==null ) {
                            g.orthologs = new HashMap();
                        }
                        HashMap h = (HashMap) g.orthologs;
                        h.put(oMapKey, oList);
                    }
                }
            }
        }
        return genes;
    }

    @RequestMapping(value="/genes/{sourceGeneId}/orthologs", method=RequestMethod.GET)
    @Operation(summary="Return orthologs for a given source gene identified by gene rgd id", tags="VCMap")
    public Map<Integer, List<MappedGeneEx>> getGeneOrthologs(HttpServletRequest request,
            @Parameter(description="RGD ID of source gene", required=true) @PathVariable(name = "sourceGeneId") int sourceGeneRgdId,
            @Parameter(description = "comma separated list of map keys for ortholog genes (optional)") @RequestParam(required = false) String mapKeys) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return getMappedOrthologs(sourceGeneRgdId, mapKeys);
    }

    Map<Integer, List<MappedGeneEx>> getMappedOrthologs(int srcGeneRgdId, String destMapKeyStr) throws Exception {

        String sql1 = "SELECT g2.rgd_id,g2.gene_symbol,g2.full_name,g2.gene_type_lc,map_key,chromosome,start_pos,stop_pos,strand " +
                "FROM genes g,genetogene_rgd_id_rlt o,maps_data md,genes g2 " +
                "WHERE g.rgd_id=? AND g.rgd_id=src_rgd_id AND dest_rgd_id=md.rgd_id AND md.map_key IN("+destMapKeyStr+") " +
                " AND dest_rgd_id=g2.rgd_id "+
                "ORDER BY md.map_key,g.rgd_id";
        String sql2 = "SELECT g2.rgd_id,g2.gene_symbol,g2.full_name,g2.gene_type_lc,map_key,chromosome,start_pos,stop_pos,strand " +
                "FROM genes g,genetogene_rgd_id_rlt o,maps_data md,genes g2 " +
                "WHERE g.rgd_id=? AND g.rgd_id=src_rgd_id AND dest_rgd_id=md.rgd_id " +
                " AND dest_rgd_id=g2.rgd_id "+
                "ORDER BY md.map_key,g.rgd_id";

        String sql = Utils.isStringEmpty(destMapKeyStr) ? sql2 : sql1;

        Map<Integer, List<MappedGeneEx>> results = new HashMap<>();

        try( Connection conn = DataSourceFactory.getInstance().getDataSource().getConnection() ) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, srcGeneRgdId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MappedGeneEx mg = new MappedGeneEx();
                mg.geneRgdId = rs.getInt("rgd_id");
                mg.geneSymbol = rs.getString("gene_symbol");
                mg.geneName = rs.getString("full_name");
                mg.geneType = rs.getString("gene_type_lc");

                mg.mapKey = rs.getInt("map_key");
                mg.chr = rs.getString("chromosome");
                mg.startPos = rs.getInt("start_pos");
                mg.stopPos = rs.getInt("stop_pos");
                mg.strand = rs.getString("strand");

                // skip rows with missing chr, start or stop pos
                if( Utils.isStringEmpty(mg.chr) || mg.startPos<=0 || mg.stopPos<=0 ) {
                    continue;
                }

                List<MappedGeneEx> genesForMapKey = results.get(mg.mapKey);
                if( genesForMapKey==null ) {
                    genesForMapKey = new ArrayList<>();
                    results.put(mg.mapKey, genesForMapKey);
                }
                genesForMapKey.add(mg);
            }
        }

        return results;
    }

    @RequestMapping(value="/genes/orthologs/{sourceMapKey}/{sourceChr}/{sourceStart}/{sourceStop}", method=RequestMethod.GET)
    @Operation(summary="Return array of source genes with orthologs in a given region", tags="VCMap")
    public List<Map<String, Object>>  getGenesWithOrthologs(HttpServletRequest request,
            @Parameter(description="source map key", required=true) @PathVariable(name = "sourceMapKey") int sourceMapKey,
            @Parameter(description="source chromosome", required=true) @PathVariable(name = "sourceChr") String sourceChr,
            @Parameter(description="source start pos", required=true) @PathVariable(name = "sourceStart") int sourceStart,
            @Parameter(description="source end pos", required=true) @PathVariable(name = "sourceStop") int sourceStop,
            @Parameter(description = "source gene size threshold (minimum gene size) (optional)") @RequestParam(required = false) Integer geneSizeThreshold,
            @Parameter(description = "comma separated list of map keys for ortholog genes (optional)") @RequestParam(required = false) String mapKeys) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return getMappedOrthologs(sourceMapKey, sourceChr, sourceStart, sourceStop, geneSizeThreshold, mapKeys);
    }

    List<Map<String, Object>> getMappedOrthologs(int mapKey, String chr, int startPos, int stopPos, Integer minGeneSize, String destMapKeyStr) throws Exception {

        // any source gene size, any orthologs
        String sql = "SELECT g1.rgd_id id1,g1.gene_symbol symbol1,g1.full_name name1,g1.gene_type_lc type1,md1.map_key mapkey1,md1.chromosome chr1,md1.start_pos start1,md1.stop_pos stop1,md1.strand strand1," +
                " g2.rgd_id id2,g2.gene_symbol symbol2,g2.full_name name2,g2.gene_type_lc type2,md2.map_key mapkey2,md2.chromosome chr2,md2.start_pos start2,md2.stop_pos stop2,md2.strand strand2 " +
                "FROM maps_data md1,genes g1,genetogene_rgd_id_rlt o,genes g2,maps_data md2 " +
                "WHERE md1.map_key=? AND md1.chromosome=? AND md1.stop_pos>=? AND md1.start_pos<=? " +
                " AND g1.rgd_id=md1.rgd_id AND g1.rgd_id=src_rgd_id AND dest_rgd_id=g2.rgd_id AND g2.rgd_id=md2.rgd_id ";
        if( minGeneSize!=null && minGeneSize>0 ) {
            sql += " AND md1.stop_pos-md1.start_pos>=? ";
        }
        if( !Utils.isStringEmpty(destMapKeyStr) ) {
            sql += " AND md2.map_key IN("+destMapKeyStr+") ";
        }
        sql += " ORDER BY md1.start_pos,md2.map_key,md2.chromosome,md2.start_pos";

        List<Map<String, Object>> results = new ArrayList<>();
        MappedGeneEx g = null;
        Map<Integer, List<MappedGeneEx>> orthologs = new HashMap<>();

        try( Connection conn = DataSourceFactory.getInstance().getDataSource().getConnection() ) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, mapKey);
            ps.setString(2, chr);
            ps.setInt(3, startPos);
            ps.setInt(4, stopPos);
            if( minGeneSize!=null && minGeneSize>0 ) {
                ps.setInt(5, minGeneSize);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MappedGeneEx o = new MappedGeneEx();
                o.geneRgdId = rs.getInt("id2");
                o.geneSymbol = rs.getString("symbol2");
                o.geneName = rs.getString("name2");
                o.geneType = rs.getString("type2");

                o.mapKey = rs.getInt("mapkey2");
                o.chr = rs.getString("chr2");
                o.startPos = rs.getInt("start2");
                o.stopPos = rs.getInt("stop2");
                o.strand = rs.getString("strand2");

                // skip rows with missing chr, start or stop pos
                if( Utils.isStringEmpty(o.chr) || o.startPos<=0 || o.stopPos<=0 ) {
                    continue;
                }

                int id1 = rs.getInt("id1");
                if( g==null || g.geneRgdId!=id1 ) {
                    g = new MappedGeneEx();
                    g.geneRgdId = rs.getInt("id1");
                    g.geneSymbol = rs.getString("symbol1");
                    g.geneName = rs.getString("name1");
                    g.geneType = rs.getString("type1");

                    g.mapKey = rs.getInt("mapkey1");
                    g.chr = rs.getString("chr1");
                    g.startPos = rs.getInt("start1");
                    g.stopPos = rs.getInt("stop1");
                    g.strand = rs.getString("strand1");

                    orthologs = new HashMap<>();
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("gene", g);
                    entry.put("orthologs", orthologs);
                    results.add(entry);
                }

                List<MappedGeneEx> genesForMapKey = orthologs.get(o.mapKey);
                if( genesForMapKey==null ) {
                    genesForMapKey = new ArrayList<>();
                    orthologs.put(o.mapKey, genesForMapKey);
                }
                genesForMapKey.add(o);
            }
        }

        return results;
    }
    @RequestMapping(value="/variants/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of variants and map key", tags="VCMap")
    public List<VariantMapData> getVariantsByPositionAndMapKey(HttpServletRequest request,@Parameter(description="Chromosome", required=true) @PathVariable(name = "chr") String chr,
                                                         @Parameter(description="Start Position", required=true) @PathVariable(name = "start") int start,
                                                         @Parameter(description="Stop Position", required=true) @PathVariable(name = "stop") int stop,
                                                         @Parameter(description="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(name = "mapKey") int mapKey) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return vdao.getVariantsWithGeneLocation(mapKey, chr.toUpperCase(), start,stop);
    }
    @RequestMapping(value="variants/gene/{rgdId}/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return a list of variants on Gene rgdID", tags="VCMap")
    public List<VariantMapData> getVariantsByGeneAndMapKey(HttpServletRequest request,@Parameter(description="RGD Id of the Gene", required=true) @PathVariable(name = "rgdId") int geneRgdId,
                                                           @Parameter(description="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(name = "mapKey") int mapKey) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        Gene g = geneDAO.getGene(geneRgdId);
        MapData md = mapDAO.getMapData(g.getRgdId(),mapKey).get(0);
        return vdao.getVariantsWithGeneLocation(mapKey, md.getChromosome(), md.getStartPos(),md.getStopPos());
    }

    @RequestMapping(value="/variants/position/{chr}/{start}/{stop}/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return a list of positions for variants", tags="VCMap")
    public List<Long> getVariantPositionsByPositionAndMapKey(HttpServletRequest request,@Parameter(description="Chromosome", required=true) @PathVariable(name = "chr") String chr,
                                                             @Parameter(description="Start Position", required=true) @PathVariable(name = "start") int start,
                                                             @Parameter(description="Stop Position", required=true) @PathVariable(name = "stop") int stop,
                                                             @Parameter(description="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(name = "mapKey") int mapKey) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return vdao.getVariantStartPositionByPositionAndMapKey(mapKey, chr.toUpperCase(), start,stop);
    }


    @RequestMapping(value="/signal/position/{chr}/{start}/{stop}/{setId}", method= RequestMethod.GET)
    @Operation(summary="Return a list of signal values", tags="VCMap")
    public List<Long> getSignalByPositionAndMapKey(HttpServletRequest request,@Parameter(description="Chromosome", required=true) @PathVariable(name = "chr") String chr,
                                                   @Parameter(description="Start Position", required=true) @PathVariable(name = "start") int start,
                                                   @Parameter(description="Stop Position", required=true) @PathVariable(name = "stop") int stop,
                                                   @Parameter(description="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(name = "setId") int setId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);

        List<GenomeSignal> signal = gsdao.getGenomeSignal(chr,setId,stop,start);

        List<Long> lst = new ArrayList<Long>();

        for (GenomeSignal gs: signal) {

            for (int i=0; i< gs.getSignalValue(); i++) {
                lst.add(gs.getPosition());
            }
        }

        return lst;
    }

}
