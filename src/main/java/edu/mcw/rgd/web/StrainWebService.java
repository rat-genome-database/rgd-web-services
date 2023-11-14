package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.StrainDAO;
import edu.mcw.rgd.dao.impl.AnnotationDAO;
import edu.mcw.rgd.dao.spring.RatModelWebServiceQuery;
import edu.mcw.rgd.datamodel.MappedStrain;
import edu.mcw.rgd.datamodel.Strain;

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
 * Created by jdepons on 5/31/2016.
 */

@RestController
@Tag(name="Rat Strain")
@RequestMapping(value = "/strains")
public class StrainWebService {

    StrainDAO sdao = new StrainDAO();
    AccessLogDAO ald = new AccessLogDAO();

    AnnotationDAO annotdao = new AnnotationDAO();

    @RequestMapping(value="/{rgdId}", method= RequestMethod.GET)
    @Operation(summary="Return a strain by RGD ID",tags = "Rat Strain")
    public Strain getStrainByRgdId(HttpServletRequest request, @Parameter(name="rgdId", description="RGD ID of the strain", required=true) @PathVariable(value = "rgdId") int rgdId ) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return sdao.getStrain(rgdId);
    }

    @RequestMapping(value="/all", method= RequestMethod.GET)
    @Operation(summary="Return all active strains in RGD",tags = "Rat Strain")
    public List<Strain> getAllStrains(HttpServletRequest request) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return sdao.getActiveStrains();
    }

    @RequestMapping(value="/{chr}/{start}/{stop}/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Return all active strains by position",tags = "Rat Strain")
    public List<Strain> getStrainsByPosition(HttpServletRequest request,@Parameter(name="chr",description="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                             @Parameter(name="start", description="Start Position", required=true) @PathVariable(value = "start") long start,
                                             @Parameter(name="stop", description="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                             @Parameter(name="mapKey", description="RGD Map Key (available through lookup service)", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return sdao.getActiveStrainsSortedBySymbol(chr.toUpperCase(),start,stop,mapKey);
    }

    @RequestMapping(value="/mapped/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of strains position and map key", tags="Rat Strain")
    public List<MappedStrain> getMappedGenesByPosition(HttpServletRequest request,@Parameter(description="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                                       @Parameter(description="Start Position", required=true) @PathVariable(value = "start") long start,
                                                       @Parameter(description="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                                       @Parameter(description="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return sdao.getActiveMappedStrainPositions(chr.toUpperCase(), start,stop, mapKey);
    }

    @RequestMapping(value="term/{term}",method = RequestMethod.GET)
    @Operation(summary="Return a list of Rat strain models based on the disease/phenotype search term", tags="Rat Strain")
    public List<RatModelWebServiceQuery.test> getRatStrainModelsByTerm(HttpServletRequest request, @Parameter(description = "Disease/Phenotype Search Term",required=true) @PathVariable(value="term")String term) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return annotdao.getAnnotationsByTermAndStrainType(term,null);
    }

    @RequestMapping(value="term/{term}/{strainType}",method=RequestMethod.GET)
    @Operation(summary="Return a list of Rat strain models based on the disease/phenotype search term and Strain type", tags="Rat Strain")
    public List<RatModelWebServiceQuery.test> getRatStrainModelsByTermAndStrainType(HttpServletRequest request,@Parameter(description = "Disease/Phenotype Search Term",required=true) @PathVariable(value="term")String term,
                                                                                    @Parameter(description = "Strain type",required = true) @PathVariable(value = "strainType")String strainType)throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return annotdao.getAnnotationsByTermAndStrainType(term,strainType);
    }


}
