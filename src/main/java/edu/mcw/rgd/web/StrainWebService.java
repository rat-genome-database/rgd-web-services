package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.StrainDAO;
import edu.mcw.rgd.datamodel.MappedGene;
import edu.mcw.rgd.datamodel.MappedStrain;
import edu.mcw.rgd.datamodel.Strain;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by jdepons on 5/31/2016.
 */

@RestController
@Api(tags="Rat Strain")
@RequestMapping(value = "/strains")
public class StrainWebService {

    StrainDAO sdao = new StrainDAO();

    @RequestMapping(value="/{rgdId}", method= RequestMethod.GET)
    @ApiOperation(value="Return a strain by RGD ID",tags = "Rat Strain")
    public Strain getStrainByRgdId(@ApiParam(value="RGD ID of the strain", required=true) @PathVariable(value = "rgdId") int rgdId ) throws Exception{
        return sdao.getStrain(rgdId);
    }

    @RequestMapping(value="/all", method= RequestMethod.GET)
    @ApiOperation(value="Return all active strains in RGD",tags = "Rat Strain")
    public List<Strain> getAllStrains() throws Exception{
        return sdao.getActiveStrains();
    }

    @RequestMapping(value="/{chr}/{start}/{stop}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return all active strains by position",tags = "Rat Strain")
    public List<Strain> getStrainsByPosition(@ApiParam(value="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                             @ApiParam(value="Start Position", required=true) @PathVariable(value = "start") long start,
                                             @ApiParam(value="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                             @ApiParam(value="RGD Map Key (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{
        return sdao.getActiveStrainsSortedBySymbol(chr.toUpperCase(),start,stop,mapKey);
    }

    @RequestMapping(value="/mapped/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of strains position and map key", tags="Rat Strain")
    public List<MappedStrain> getMappedGenesByPosition(@ApiParam(value="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                                       @ApiParam(value="Start Position", required=true) @PathVariable(value = "start") long start,
                                                       @ApiParam(value="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                                       @ApiParam(value="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        return sdao.getActiveMappedStrainPositions(chr.toUpperCase(), start,stop, mapKey);
    }

}
