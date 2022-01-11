package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.DataSourceFactory;
import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.dao.impl.MapDAO;
import edu.mcw.rgd.dao.impl.SyntenyDAO;
import edu.mcw.rgd.dao.spring.MappedGeneQuery;
import edu.mcw.rgd.datamodel.Chromosome;
import edu.mcw.rgd.datamodel.MappedGene;
import edu.mcw.rgd.datamodel.SpeciesType;
import edu.mcw.rgd.datamodel.SyntenicRegion;
import edu.mcw.rgd.domain.vcmap.ChromosomeEx;
import edu.mcw.rgd.domain.vcmap.MappedGeneEx;
import edu.mcw.rgd.domain.vcmap.SpeciesMaps;
import edu.mcw.rgd.process.Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by mtutaj on 11/18/2021
 */

@RestController
@Api(tags="VCMap")
@RequestMapping(value = "/vcmap")
public class VcmapWebService {

    GeneDAO geneDAO = new GeneDAO();
    MapDAO mapDAO = new MapDAO();
    SyntenyDAO sdao = new SyntenyDAO();

    @RequestMapping(value = "/blocks/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}", method = RequestMethod.GET)
    @ApiOperation(value = "Return all synteny blocks for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getBlocks2(
            @ApiParam(value = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
            @ApiParam(value = "Backbone Chromosome", required = true) @PathVariable(value = "backboneChr") String backboneChr,
            @ApiParam(value = "Backbone Start Position", required = true) @PathVariable(value = "backboneStart") int backboneStart,
            @ApiParam(value = "Backbone Stop Position", required = true) @PathVariable(value = "backboneStop") int backboneStop,
            @ApiParam(value = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value = "Minimum Backbone Block Size (optional)") @RequestParam(required = false) Integer threshold
    ) throws Exception {

        if (threshold == null) {
            return sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
        } else {
            return sdao.getSizedBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey);
        }
    }

    @RequestMapping(value = "/blocks/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method = RequestMethod.GET)
    @ApiOperation(value = "Return all synteny blocks for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getBlocks(
            @ApiParam(value = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
            @ApiParam(value = "Backbone Chromosome", required = true) @PathVariable(value = "backboneChr") String backboneChr,
            @ApiParam(value = "Backbone Start Position", required = true) @PathVariable(value = "backboneStart") int backboneStart,
            @ApiParam(value = "Backbone Stop Position", required = true) @PathVariable(value = "backboneStop") int backboneStop,
            @ApiParam(value = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value = "Chain Level (1, 2, etc or range: 1-2)", required = true) @PathVariable(value = "chainLevel") String chainLevel,
            @ApiParam(value = "Minimum Backbone Block Size (optional)") @RequestParam(required = false) Integer threshold) throws Exception {

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
    @ApiOperation(value = "Return gaps for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getGaps(
            @ApiParam(value = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
            @ApiParam(value = "Backbone Chromosome", required = true) @PathVariable(value = "backboneChr") String backboneChr,
            @ApiParam(value = "Backbone Start Position", required = true) @PathVariable(value = "backboneStart") int backboneStart,
            @ApiParam(value = "Backbone Stop Position", required = true) @PathVariable(value = "backboneStop") int backboneStop,
            @ApiParam(value = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value = "Minimum Backbone Gap Size (optional)") @RequestParam(required = false) Integer threshold) throws Exception {

        if (threshold != null) {
            return sdao.getSizedGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey);
        } else {
            return sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
        }
    }

    @RequestMapping(value = "/gaps/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method = RequestMethod.GET)
    @ApiOperation(value = "Return gaps for given backbone region", tags = "VCMap")
    public List<SyntenicRegion> getGaps(
            @ApiParam(value = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
            @ApiParam(value = "Backbone Chromosome", required = true) @PathVariable(value = "backboneChr") String backboneChr,
            @ApiParam(value = "Backbone Start Position", required = true) @PathVariable(value = "backboneStart") int backboneStart,
            @ApiParam(value = "Backbone Stop Position", required = true) @PathVariable(value = "backboneStop") int backboneStop,
            @ApiParam(value = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value = "Chain Level (1, 2, etc, or range: '1-2')", required = true) @PathVariable(value = "chainLevel") String chainLevel,
            @ApiParam(value = "Minimum Backbone Gap Size (optional)") @RequestParam(required = false) Integer threshold) throws Exception {

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
    @ApiOperation(value = "Return all synteny blocks and gaps for given backbone region", tags = "VCMap")
    public List<Map<String, Object>> getSynteny(
            @ApiParam(value = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
            @ApiParam(value = "Backbone Chromosome", required = true) @PathVariable(value = "backboneChr") String backboneChr,
            @ApiParam(value = "Backbone Start Position", required = true) @PathVariable(value = "backboneStart") int backboneStart,
            @ApiParam(value = "Backbone Stop Position", required = true) @PathVariable(value = "backboneStop") int backboneStop,
            @ApiParam(value = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value = "Minimum Backbone Block/Gap Size (optional)") @RequestParam(required = false) Integer threshold
    ) throws Exception {

        List<SyntenicRegion> blocks;
        List<SyntenicRegion> gaps;

        if( threshold==null ) {
            blocks = sdao.getBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
            gaps = sdao.getGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, mapKey);
        } else {
            blocks = sdao.getSizedBlocks(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey);
            gaps = sdao.getSizedGaps(backboneMapKey, backboneChr, backboneStart, backboneStop, threshold, mapKey);
        }

        List<Map<String, Object>> results = combineBlocksAndGaps(blocks, gaps);
        return results;
    }

    @RequestMapping(value = "/synteny/{backboneMapKey}/{backboneChr}/{backboneStart}/{backboneStop}/{mapKey}/{chainLevel}", method = RequestMethod.GET)
    @ApiOperation(value = "Return all synteny blocks and gaps for given backbone region", tags = "VCMap")
    public List<Map<String, Object>> getSynteny(
            @ApiParam(value = "Backbone Species Map Key (available through lookup service)", required = true) @PathVariable(value = "backboneMapKey") int backboneMapKey,
            @ApiParam(value = "Backbone Chromosome", required = true) @PathVariable(value = "backboneChr") String backboneChr,
            @ApiParam(value = "Backbone Start Position", required = true) @PathVariable(value = "backboneStart") int backboneStart,
            @ApiParam(value = "Backbone Stop Position", required = true) @PathVariable(value = "backboneStop") int backboneStop,
            @ApiParam(value = "Map Key for Comparative Species (available through lookup service)", required = true) @PathVariable(value = "mapKey") int mapKey,
            @ApiParam(value = "Chain Level (1, 2, etc, or range: '1-2')", required = true) @PathVariable(value = "chainLevel") String chainLevel,
            @ApiParam(value = "Minimum Backbone Block/Gap Size (optional)") @RequestParam(required = false) Integer threshold
    ) throws Exception {

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

        List<Map<String, Object>> results = combineBlocksAndGaps(blocks, gaps);
        return results;
    }

    List<Map<String, Object>> combineBlocksAndGaps(List<SyntenicRegion> blocks, List<SyntenicRegion> gaps) {

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
        }
        return results;
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

    @RequestMapping(value="/genes/mapped/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of genes position and map key", tags="Gene")
    public List<MappedGene> getMappedGenesByPosition(
        @ApiParam(value="Chromosome", required=true) @PathVariable(value = "chr") String chr,
        @ApiParam(value="Start Position", required=true) @PathVariable(value = "start") int start,
        @ApiParam(value="Stop Position", required=true) @PathVariable(value = "stop") int stop,
        @ApiParam(value="Map Key for Comparative Species (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey,
        @ApiParam(value = "Minimum Gene Size (optional)") @RequestParam(required = false) Integer threshold) throws Exception{

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


    @RequestMapping(value="/genes/{sourceGeneId}/orthologs", method=RequestMethod.GET)
    @ApiOperation(value="Return orthologs for a given source gene identified by gene rgd id", tags="VCMap")
    public Map<Integer, List<MappedGeneEx>> getGeneOrthologs(
            @ApiParam(value="RGD ID of source gene", required=true) @PathVariable(value = "sourceGeneId") int sourceGeneRgdId,
            @ApiParam(value = "comma separated list of map keys for ortholog genes (optional)") @RequestParam(required = false) String mapKeys) throws Exception{

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
                "WHERE g.rgd_id=? AND g.rgd_id=src_rgd_id AND dest_rgd_id=md.rgd_id AND md." +
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
}
