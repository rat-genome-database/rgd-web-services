package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.dao.impl.MapDAO;
import edu.mcw.rgd.dao.impl.StrainDAO;
import edu.mcw.rgd.dao.impl.variants.VariantDAO;
import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.MapData;
import edu.mcw.rgd.datamodel.Strain;
import edu.mcw.rgd.datamodel.variants.VariantMapData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name="Variants")
@RequestMapping(value = "/variants")
public class VariantWebService {
    VariantDAO vdao = new VariantDAO();
    GeneDAO gdao = new GeneDAO();
    StrainDAO sdao = new StrainDAO();
    MapDAO mdao = new MapDAO();
    AccessLogDAO ald = new AccessLogDAO();

//    @RequestMapping(value="/{rgdId}", method= RequestMethod.GET)
//    @Operation(summary="Return a list of variants on variant rgdID", tags="Variants")
//    public List<VariantMapData> getVariantsByVariantRgdId(@Parameter(description="Variant RGD Id", required=true) @PathVariable(name = "rgdId") int variantRgdId) throws Exception{
//
//        return vdao.getVariantsByRgdId(variantRgdId);
//    }

    @RequestMapping(value="/{rsId}", method= RequestMethod.GET)
    @Operation(summary="Return a list of variants based off of rsID", tags="Variants")
    public List<VariantMapData> getVariantsByRsId(HttpServletRequest request, @Parameter(description="rsId", required=true) @PathVariable(name = "rsId") String rsId) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return vdao.getAllVariantByRsId(rsId);
    }

    @RequestMapping(value="/gene/{rgdId}/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return a list of variants on Gene rgdID", tags="Variants")
    public List<VariantMapData> getVariantsByGeneAndMapKey(HttpServletRequest request,@Parameter(description="RGD Id of the Gene", required=true) @PathVariable(name = "rgdId") int geneRgdId,
                                                           @Parameter(description="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(name = "mapKey") int mapKey) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        Gene g = gdao.getGene(geneRgdId);
        MapData md = mdao.getMapData(g.getRgdId(),mapKey).get(0);
        return vdao.getVariantsWithGeneLocation(mapKey, md.getChromosome(), md.getStartPos(),md.getStopPos());
    }

    @RequestMapping(value="/strain/{rgdId}/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return a list of variants on Strain rgdID", tags="Variants")
    public List<VariantMapData> getVariantsByStrainAndMapKey(HttpServletRequest request,@Parameter(description="RGD ID of the Strain", required=true) @PathVariable(name = "rgdId") int strainRgdId,
                                                               @Parameter(description="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(name = "mapKey") int mapKey) throws Exception{
//        Strain s = sdao.getStrain(strainRgdId);
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        MapData md = mdao.getMapData(strainRgdId,mapKey).get(0);
        return vdao.getVariantsWithGeneLocation(mapKey, md.getChromosome(), md.getStartPos(),md.getStopPos());
    }

    @RequestMapping(value="/{chr}/{start}/{stop}/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return a list of variants in a given position", tags="Variants")
    public List<VariantMapData> getVariantsByPositionAndMapKey(HttpServletRequest request,@Parameter(description="Chromosome", required=true) @PathVariable(name = "chr") String chr,
                                                               @Parameter(description="Start Position", required=true) @PathVariable(name = "start") int start,
                                                               @Parameter(description="Stop Position", required=true) @PathVariable(name = "stop") int stop,
                                                               @Parameter(description="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(name = "mapKey") int mapKey) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return vdao.getVariantsWithGeneLocation(mapKey, chr.toUpperCase(), start,stop);
    }
}
