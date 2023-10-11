package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.MappedGene;
import edu.mcw.rgd.datamodel.MappedGenePosition;
import edu.mcw.rgd.domain.AnnotatedGeneRequest;
import edu.mcw.rgd.domain.OrthologRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

//@Tag(name = "Rat Genome Database", description = "The RGD REST API provides programmatic access to data stored in the Rat Genome Database")
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@Tag(name="Gene")
@RequestMapping("/gene")
public class GeneController {
    GeneDAO geneDAO=new GeneDAO();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/orthologs/{rgdId}", method=RequestMethod.GET)
    @Operation(summary="Return a list of gene orthologs", tags="Gene")
    public List<Gene> getGeneOrthologs(HttpServletRequest request, @Parameter(name="rgdId",description="RGD ID of a gene", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getActiveOrthologs(rgdId);
    }

    @RequestMapping(value="/orthologs", method=RequestMethod.POST)
    @Operation(summary="Return a list of gene orthologs", tags="Gene")
    public HashMap<String, List<Gene>> getOrthologsByList(HttpServletRequest request,
                                                          @RequestBody(required = true) OrthologRequest orthologRequest

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        HashMap<String, List<Gene>> retList= new HashMap<String, List<Gene>>();

        for (Integer id: orthologRequest.rgdIds) {
            retList.put(id + "", geneDAO.getActiveOrthologs(id, orthologRequest.speciesTypeKeys));
        }

        return retList;
    }

    @RequestMapping(value="/annotation/{accId}", method=RequestMethod.GET)
    @Operation(summary="Return a list of genes annotated to an ontology term", tags="Gene")
    public List<Gene> getAllAnnotatedGenes(HttpServletRequest request,
                                           @Parameter(in = ParameterIn.PATH, name ="accId" ,schema = @Schema(type = "string"), description = "Accesstion ID", required = true)
                                           @PathVariable(value = "accId") String accId

    ) throws Exception{


        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<Gene> genes = geneDAO.getAnnotatedGenes(accId);
        return genes;
    }


    @RequestMapping(value="/annotation", method=RequestMethod.POST)
    @Operation(summary="Return a list of genes annotated to an ontology term", tags="Gene")
    public List<Gene> getAnnotatedGenes(HttpServletRequest request,
                                        @RequestBody(required = false) AnnotatedGeneRequest data

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<Gene> genes = geneDAO.getAnnotatedGenes(data.accId,data.speciesTypeKeys,data.evidenceCodes);
        return genes;
    }



    @RequestMapping(value="/annotation/{accId}/{speciesTypeKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of genes annotated to an ontology term", tags="Gene")
    public List<Gene> getGenesAnnotated(HttpServletRequest request,@Parameter(name="accId", description="Ontology term accession ID", required=true) @PathVariable(value = "accId") String accId,@Parameter(name="speciesTypeKey", description="Species type key.  A list of species type keys can be found in the lookup service", required=true)   @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getActiveAnnotatedGenes(accId.toUpperCase(),speciesTypeKey);
    }


    @RequestMapping(value="/{rgdId}", method=RequestMethod.GET)
    @Operation(summary="Get a gene record by RGD ID", tags="Gene")
    public Gene getGeneByRgdId(HttpServletRequest request,@Parameter(name="rgdId", description="The RGD ID of a Gene in RGD", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);

        return geneDAO.getGene(rgdId);
    }


    @RequestMapping(value="/{symbol}/{speciesTypeKey}", method=RequestMethod.GET)
    @Operation(summary="Get a gene record by symbol and species type key", tags="Gene")
    public Gene getGeneBySymbol(HttpServletRequest request,@Parameter(name="Gene Symbol", required=true) @PathVariable(value = "symbol") String symbol,@Parameter(name="Species type key.  A list of species type keys can be found in the lookup service", required=true)  @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getGenesBySymbol(symbol,speciesTypeKey);
    }

    @RequestMapping(value="/keyword/{keyword}/{speciesTypeKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of genes by keyword and species type key", tags="Gene")
    public List<Gene> getGenesByKeyword(HttpServletRequest request,@Parameter(name="Search keyword", required=true) @PathVariable(value = "keyword") String keyword,@Parameter(name="Species type key.  A list of species type keys can be found in the lookup service", required=true)  @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getActiveGenes(keyword,speciesTypeKey);
    }

    @RequestMapping(value="/region/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of genes in region", tags="Gene")
    public List<MappedGenePosition> getGenesInRegion(HttpServletRequest request, @Parameter(name="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                                     @Parameter(name="Start Position", required=true) @PathVariable(value = "start") long start,
                                                     @Parameter(name="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                                     @Parameter(name="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getActiveMappedGenePositions(chr.toUpperCase(), start, stop, mapKey);
    }

    @RequestMapping(value="/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of genes position and map key", tags="Gene")
    public List<Gene> getGenesByPosition(HttpServletRequest request,@Parameter(name="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                         @Parameter(name="Start Position", required=true) @PathVariable(value = "start") long start,
                                         @Parameter(name="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                         @Parameter(name="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getActiveGenes(chr.toUpperCase(), start,stop, mapKey);
    }
    @RequestMapping(value="/mapped/{chr}/{start}/{stop}/{mapKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of genes position and map key", tags="Gene")
    public List<MappedGene> getMappedGenesByPosition(HttpServletRequest request, @Parameter(name="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                                     @Parameter(name="Start Position", required=true) @PathVariable(value = "start") long start,
                                                     @Parameter(name="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                                     @Parameter(name="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getActiveMappedGenes(chr.toUpperCase(), start,stop, mapKey);
    }
    @RequestMapping(value="/map/{mapKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of all genes with position information for an assembly", tags="Gene")
    public List<MappedGene> getGeneByMapKey(HttpServletRequest request,@Parameter(name="A list of RGD assembly map keys can be found in the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getActiveMappedGenes(mapKey);
    }

    @RequestMapping(value="/species/{speciesTypeKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of all genes for a species in RGD", tags="Gene")
    public List<Gene> getGenesBySpecies(HttpServletRequest request,@Parameter(name="A list of RGD species type keys can be found in the lookup service", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getActiveGenes(speciesTypeKey);
    }

    @RequestMapping(value="/alias/{aliasSymbol}/{speciesTypeKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of genes for an alias and species", tags="Gene")
    public List<Gene> getGenesByAliasSymbol(HttpServletRequest request,@Parameter(name="Gene alias symbol", required=true) @PathVariable(value = "aliasSymbol") String alias, @Parameter(name="A list of RGD species type keys can be found in the lookup service", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getGenesByAlias(alias,speciesTypeKey);
    }

    @RequestMapping(value="/affyId/{affyId}/{speciesTypeKey}", method=RequestMethod.GET)
    @Operation(summary="Return a list of genes for an affymetrix ID", tags="Gene")
    public List<Gene> getGenesByAffyId(HttpServletRequest request,@Parameter(name="Affymetrix ID", required=true) @PathVariable(value = "affyId") String affyId,@Parameter(name="A list of RGD species type keys can be found in the lookup service", required=true)  @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getGenesForAffyId(affyId,speciesTypeKey);
    }

    @RequestMapping(value="/allele/{rgdId}", method=RequestMethod.GET)
    @Operation(summary="Return a list of gene alleles", tags="Gene")
    public List<Gene> getGeneAlleles(HttpServletRequest request,@Parameter(name="RGD ID of gene", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return geneDAO.getVariantFromGene(rgdId);
    }
}
