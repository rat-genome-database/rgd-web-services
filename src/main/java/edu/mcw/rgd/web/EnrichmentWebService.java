package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AnnotationDAO;
import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.dao.impl.GeneEnrichmentDAO;
import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.ontologyx.Aspect;
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
    GeneDAO gdao = new GeneDAO();
    GeneOntologyEnrichmentProcess process = new GeneOntologyEnrichmentProcess();
    AnnotationDAO adao = new AnnotationDAO();
    OntologyXDAO oDao = new OntologyXDAO();
    @RequestMapping(value = "/enrichment/chart/{speciesTypeKey}/{genes}/{aspect}", method = RequestMethod.POST)
    @ApiOperation(value = "Return a chart of ontology terms annotated to the genes.Genes are rgdids separated by comma.Species type is an integer value.Aspect is the Ontology group")
    public List getChart(@ApiParam(value = "Species Type Key - 3=rat ", required = true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                            @ApiParam(value = "List of RGDids", required = true) @PathVariable(value = "genes") String genes,
                            @ApiParam(value = "List of ontology groups", required = true) @PathVariable(value = "aspect") String aspect)
                             throws Exception {

        String[] geneSymbols = genes.split(",");

        List<Integer> geneRgdIds = new ArrayList<>();
        List<String> termSet = new ArrayList<>();


        for (int i = 0; i < geneSymbols.length; i++) {
            Gene g = new Gene();
            g = gdao.getGenesBySymbol(geneSymbols[i],speciesTypeKey);
            if(g != null)
                geneRgdIds.add(g.getRgdId());
        }


        ArrayList<String> aspects = new ArrayList<>();
        aspects.add(aspect);

        int refGenes = dao.getReferenceGeneCount(speciesTypeKey);
        int inputGenes = geneRgdIds.size();
int count =0;

            List result = new ArrayList<>();
            LinkedHashMap<String, Integer> geneCounts = adao.getGeneCounts(geneRgdIds, termSet, aspects);

            BigDecimal numberOfTerms = new BigDecimal(geneCounts.keySet().size());
            Iterator tit = geneCounts.keySet().iterator();
            while (tit.hasNext()  && count++ < 200) {
                HashMap data = new HashMap();
                String acc = (String) tit.next();
                String term = oDao.getTermByAccId(acc).getTerm();
                data.put("acc", acc);
                data.put("term", term);
                int refs = geneCounts.get(acc);
                data.put("count", refs);
                BigDecimal pvalue = process.calculatePValue(inputGenes, refGenes, acc, refs, speciesTypeKey);
                data.put("pvalue", pvalue);
                BigDecimal bonferroni = process.calculateBonferroni(pvalue, numberOfTerms);
                data.put("correctedpvalue", bonferroni);
                result.add(data);
            }



        return result;
    }

}