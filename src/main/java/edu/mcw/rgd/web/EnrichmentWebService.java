package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AnnotationDAO;
import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.dao.impl.GeneEnrichmentDAO;
import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.ontologyx.Aspect;
import edu.mcw.rgd.domain.EnrichmentRequest;
import edu.mcw.rgd.process.enrichment.geneOntology.GeneOntologyEnrichmentProcess;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

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
    @RequestMapping(value = "/data", method = RequestMethod.POST)
    @ApiOperation(value = "Return a chart of ontology terms annotated to the genes.Genes are rgdids separated by comma.Species type is an integer value.Aspect is the Ontology group")
    public List getChart( @RequestBody(required = true) EnrichmentRequest enrichmentRequest)
                             throws Exception {
        List<String> geneSymbols = enrichmentRequest.genes;
        int speciesTypeKey = enrichmentRequest.speciesTypeKey;
        String aspect = enrichmentRequest.aspect;
        List<Integer> geneRgdIds = gdao.getActiveGeneRgdIdsBySymbols(geneSymbols,speciesTypeKey);
        List<String> termSet = new ArrayList<>();
        ArrayList<String> aspects = new ArrayList<>();
        aspects.add(aspect);

        int refGenes = dao.getReferenceGeneCount(speciesTypeKey);
        int inputGenes = geneRgdIds.size();
int count =0;

            List result = new ArrayList<>();
            LinkedHashMap<String, Integer> geneCounts = adao.getGeneCounts(geneRgdIds, termSet, aspects);

            BigDecimal numberOfTerms = new BigDecimal(geneCounts.keySet().size());
            Iterator tit = geneCounts.keySet().iterator();
        while(tit.hasNext() && count++ < 200 ) {
            try {
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
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }



        return result;
    }

}