package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.MappedGene;
import edu.mcw.rgd.datamodel.MappedGenePosition;
import edu.mcw.rgd.domain.AnnotatedGeneRequest;
import edu.mcw.rgd.domain.OrthologRequest;
import edu.mcw.rgd.domain.RGDIDListRequest;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jdepons on 5/31/2016.
 */
@RestController
@Api(tags="Gene")
@RequestMapping(value = "/genes")
public class GeneWebService {

    GeneDAO geneDAO = new GeneDAO();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/orthologs/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of gene orthologs", tags="Gene")
    public List<Gene> getGeneOrthologs(@ApiParam(value="RGD ID of a gene", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{
        return geneDAO.getActiveOrthologs(rgdId);
    }

    @RequestMapping(value="/orthologs", method=RequestMethod.POST)
    @ApiOperation(value="Return a list of gene orthologs", tags="Gene")
    public HashMap<String, List<Gene>> getOrthologsByList(
            @RequestBody(required = true) OrthologRequest orthologRequest

    ) throws Exception{

        HashMap<String, List<Gene>> retList= new HashMap<String, List<Gene>>();

        for (Integer id: orthologRequest.rgdIds) {
            retList.put(id + "", geneDAO.getActiveOrthologs(id, orthologRequest.speciesTypeKeys));
        }

        return retList;
    }

    @RequestMapping(value="/annotation/{accId}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of genes annotated to an ontology term", tags="Gene")
    public List<Gene> getAllAnnotatedGenes(
            @ApiParam(value="Accesstion ID", required=true)
            @PathVariable(value = "accId") String accId

    ) throws Exception{


        List<Gene> genes = geneDAO.getAnnotatedGenes(accId);
        return genes;
    }


    @RequestMapping(value="/annotation", method=RequestMethod.POST)
    @ApiOperation(value="Return a list of genes annotated to an ontology term", tags="Gene")
    public List<Gene> getAnnotatedGenes(
            @RequestBody(required = false) AnnotatedGeneRequest data

    ) throws Exception{

            List<Gene> genes = geneDAO.getAnnotatedGenes(data.accId,data.speciesTypeKeys,data.evidenceCodes);
            return genes;
    }



    @RequestMapping(value="/annotation/{accId}/{speciesTypeKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of genes annotated to an ontology term", tags="Gene")
    public List<Gene> getGenesAnnotated(@ApiParam(value="Ontology term accession ID", required=true) @PathVariable(value = "accId") String accId,@ApiParam(value="Species type key.  A list of species type keys can be found in the lookup service", required=true)   @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        return geneDAO.getActiveAnnotatedGenes(accId.toUpperCase(),speciesTypeKey);
    }


    @RequestMapping(value="/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Get a gene record by RGD ID", tags="Gene")
    public Gene getGeneByRgdId(@ApiParam(value="The RGD ID of a Gene in RGD", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        return geneDAO.getGene(rgdId);
    }


    @RequestMapping(value="/{symbol}/{speciesTypeKey}", method=RequestMethod.GET)
    @ApiOperation(value="Get a gene record by symbol and species type key", tags="Gene")
    public Gene getGeneBySymbol(@ApiParam(value="Gene Symbol", required=true) @PathVariable(value = "symbol") String symbol,@ApiParam(value="Species type key.  A list of species type keys can be found in the lookup service", required=true)  @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        return geneDAO.getGenesBySymbol(symbol,speciesTypeKey);
    }

    @RequestMapping(value="/keyword/{keyword}/{speciesTypeKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of genes by keyword and species type key", tags="Gene")
    public List<Gene> getGenesByKeyword(@ApiParam(value="Search keyword", required=true) @PathVariable(value = "keyword") String keyword,@ApiParam(value="Species type key.  A list of species type keys can be found in the lookup service", required=true)  @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        return geneDAO.getActiveGenes(keyword,speciesTypeKey);
    }

    @RequestMapping(value="/region/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of genes in region", tags="Gene")
    public List<MappedGenePosition> getGenesInRegion(@ApiParam(value="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                                     @ApiParam(value="Start Position", required=true) @PathVariable(value = "start") long start,
                                                     @ApiParam(value="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                                     @ApiParam(value="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        return geneDAO.getActiveMappedGenePositions(chr.toUpperCase(), start, stop, mapKey);
    }
    
    @RequestMapping(value="/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of genes position and map key", tags="Gene")
    public List<Gene> getGenesByPosition(@ApiParam(value="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                         @ApiParam(value="Start Position", required=true) @PathVariable(value = "start") long start,
                                         @ApiParam(value="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                         @ApiParam(value="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        return geneDAO.getActiveGenes(chr.toUpperCase(), start,stop, mapKey);
    }
    @RequestMapping(value="/mapped/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of genes position and map key", tags="Gene")
    public List<MappedGene> getMappedGenesByPosition(@ApiParam(value="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                         @ApiParam(value="Start Position", required=true) @PathVariable(value = "start") long start,
                                         @ApiParam(value="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                         @ApiParam(value="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        return geneDAO.getActiveMappedGenes(chr.toUpperCase(), start,stop, mapKey);
    }
    @RequestMapping(value="/map/{mapKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of all genes with position information for an assembly", tags="Gene")
    public List<MappedGene> getGeneByMapKey(@ApiParam(value="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{
        return geneDAO.getActiveMappedGenes(mapKey);
    }

    @RequestMapping(value="/species/{speciesTypeKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of all genes for a species in RGD", tags="Gene")
    public List<Gene> getGenesBySpecies(@ApiParam(value="A list of RGD species type keys can be found in the lookup service", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        return geneDAO.getActiveGenes(speciesTypeKey);
    }

    @RequestMapping(value="/alias/{aliasSymbol}/{speciesTypeKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of genes for an alias and species", tags="Gene")
    public List<Gene> getGenesByAliasSymbol(@ApiParam(value="Gene alias symbol", required=true) @PathVariable(value = "aliasSymbol") String alias, @ApiParam(value="A list of RGD species type keys can be found in the lookup service", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        return geneDAO.getGenesByAlias(alias,speciesTypeKey);
    }

    @RequestMapping(value="/affyId/{affyId}/{speciesTypeKey}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of genes for an affymetrix ID", tags="Gene")
    public List<Gene> getGenesByAffyId(@ApiParam(value="Affymetrix ID", required=true) @PathVariable(value = "affyId") String affyId,@ApiParam(value="A list of RGD species type keys can be found in the lookup service", required=true)  @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        return geneDAO.getGenesForAffyId(affyId,speciesTypeKey);
    }

    @RequestMapping(value="/allele/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Return a list of gene alleles", tags="Gene")
    public List<Gene> getGeneAlleles(@ApiParam(value="RGD ID of gene", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{
        return geneDAO.getVariantFromGene(rgdId);
    }
}
