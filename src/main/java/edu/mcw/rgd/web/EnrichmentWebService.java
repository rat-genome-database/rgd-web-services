package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AnnotationDAO;
import edu.mcw.rgd.dao.impl.GeneEnrichmentDAO;
import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.process.enrichment.geneOntology.GeneOntologyEnrichmentProcess;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by hsnalabolu on 1/8/2019.
 */
@RestController
@RequestMapping(value = "/enrichment")
public class EnrichmentWebService {

    GeneEnrichmentDAO dao = new GeneEnrichmentDAO();
    GeneOntologyEnrichmentProcess process = new GeneOntologyEnrichmentProcess();
    AnnotationDAO adao = new AnnotationDAO();
    OntologyXDAO oDao = new OntologyXDAO();
    @RequestMapping(value = "/enrichment/chart/{speciesTypeKey}/{genes}/{aspect}", method = RequestMethod.GET)
    @ApiOperation(value = "Return a chart of ontology terms annotated to the genes.Genes are rgdids separated by comma.Species type is an integer value.Aspect is the Ontology group")
    public HashMap getChart(@ApiParam(value = "Species Type Key - 3=rat ", required = true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                            @ApiParam(value = "List of RGDids", required = true) @PathVariable(value = "genes") String genes,
                            @ApiParam(value = "List of ontology groups", required = true) @PathVariable(value = "aspect") String aspect) throws Exception {

        String[] geneIds = genes.split(",");

        List<Integer> geneRgdIds = new ArrayList<>();
        List<String> termSet = new ArrayList<>();
        List<String> aspects = new ArrayList<>();
        aspects.add(aspect);
        for (int i = 0; i < geneIds.length; i++) {
            geneRgdIds.add(Integer.parseInt(geneIds[i]));
        }
        LinkedHashMap<String, Integer> geneCounts = adao.getGeneCounts(geneRgdIds, termSet, aspects);
        HashMap<String,BigDecimal> pvalues = new HashMap<String,BigDecimal>();
        HashMap<String,BigDecimal> correctedpvalues = new HashMap<String,BigDecimal>();
       HashMap counts = new HashMap();
        int refGenes = dao.getReferenceGeneCount(speciesTypeKey);
        int inputGenes = geneIds.length;
        BigDecimal numberOfTerms = new BigDecimal(geneCounts.keySet().size());
        Iterator tit = geneCounts.keySet().iterator();
        while (tit.hasNext() ) {
            String acc = (String) tit.next();
            int refs = geneCounts.get(acc);
            counts.put(acc,refs);
            BigDecimal pvalue =  process.calculatePValue(inputGenes, refGenes, acc, refs, speciesTypeKey);
            pvalues.put(acc, pvalue);
            BigDecimal bonferroni = process.calculateBonferroni(pvalue,numberOfTerms);
            correctedpvalues.put(acc,bonferroni);
        }

        HashMap result = new HashMap();
        result.put("pvalues",pvalues);
        result.put("correctedPvalues",correctedpvalues);
        result.put("counts",counts);
        return result;
    }

}