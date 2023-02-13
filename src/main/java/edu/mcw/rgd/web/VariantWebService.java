package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.dao.impl.MapDAO;
import edu.mcw.rgd.dao.impl.StrainDAO;
import edu.mcw.rgd.dao.impl.variants.VariantDAO;
import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.MapData;
import edu.mcw.rgd.datamodel.Strain;
import edu.mcw.rgd.datamodel.variants.VariantMapData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags="Variants")
@RequestMapping(value = "/variants")
public class VariantWebService {
    VariantDAO vdao = new VariantDAO();
    GeneDAO gdao = new GeneDAO();
    StrainDAO sdao = new StrainDAO();
    MapDAO mdao = new MapDAO();

//    @RequestMapping(value="/{rgdId}", method= RequestMethod.GET)
//    @ApiOperation(value="Return a list of variants on variant rgdID", tags="Variants")
//    public List<VariantMapData> getVariantsByVariantRgdId(@ApiParam(value="Variant RGD Id", required=true) @PathVariable(value = "rgdId") int variantRgdId) throws Exception{
//
//        return vdao.getVariantsByRgdId(variantRgdId);
//    }

    @RequestMapping(value="/{rsId}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of variants based off of rsID", tags="Variants")
    public List<VariantMapData> getVariantsByRsId(@ApiParam(value="rsId", required=true) @PathVariable(value = "rsId") String rsId) throws Exception{
        return vdao.getAllVariantByRsId(rsId);
    }

    @RequestMapping(value="/gene/{rgdId}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of variants on Gene rgdID", tags="Variants")
    public List<VariantMapData> getVariantsByGeneAndMapKey(@ApiParam(value="RGD Id of the Gene", required=true) @PathVariable(value = "rgdId") int geneRgdId,
                                                           @ApiParam(value="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{
        Gene g = gdao.getGene(geneRgdId);
        MapData md = mdao.getMapData(g.getRgdId(),mapKey).get(0);
        return vdao.getVariantsWithGeneLocation(mapKey, md.getChromosome(), md.getStartPos(),md.getStopPos());
    }

    @RequestMapping(value="/strain/{rgdId}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of variants on Strain rgdID", tags="Variants")
    public List<VariantMapData> getVariantsByStrainAndMapKey(@ApiParam(value="RGD ID of the Strain", required=true) @PathVariable(value = "rgdId") int strainRgdId,
                                                               @ApiParam(value="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{
//        Strain s = sdao.getStrain(strainRgdId);
        MapData md = mdao.getMapData(strainRgdId,mapKey).get(0);
        return vdao.getVariantsWithGeneLocation(mapKey, md.getChromosome(), md.getStartPos(),md.getStopPos());
    }

    @RequestMapping(value="/{chr}/{start}/{stop}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of variants in a given position", tags="Variants")
    public List<VariantMapData> getVariantsByPositionAndMapKey(@ApiParam(value="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                                               @ApiParam(value="Start Position", required=true) @PathVariable(value = "start") int start,
                                                               @ApiParam(value="Stop Position", required=true) @PathVariable(value = "stop") int stop,
                                                               @ApiParam(value="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        return vdao.getVariantsWithGeneLocation(mapKey, chr.toUpperCase(), start,stop);
    }
}
