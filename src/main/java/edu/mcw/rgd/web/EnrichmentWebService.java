
package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.*;
import edu.mcw.rgd.datamodel.Ortholog;
import edu.mcw.rgd.datamodel.RgdId;
import edu.mcw.rgd.datamodel.SpeciesType;
import edu.mcw.rgd.datamodel.annotation.GeneWrapper;
import edu.mcw.rgd.datamodel.annotation.OntologyEnrichment;
import edu.mcw.rgd.datamodel.annotation.TermWrapper;
import edu.mcw.rgd.datamodel.ontologyx.Aspect;
import edu.mcw.rgd.datamodel.ontologyx.Ontology;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.datamodel.ontologyx.TermWithStats;
import edu.mcw.rgd.domain.EnrichmentGeneRequest;
import edu.mcw.rgd.domain.EnrichmentRequest;
import edu.mcw.rgd.process.enrichment.geneOntology.GeneOntologyEnrichmentProcess;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    public Map getEnrichmentData(@RequestBody(required = true) EnrichmentRequest enrichmentRequest)
            throws Exception {

        int speciesTypeKey = SpeciesType.parse(enrichmentRequest.species);
        int originalSpeciesTypeKey=SpeciesType.parse(enrichmentRequest.originalSpecies);

        //List<Integer> ids = gdao.getActiveGeneRgdIdsBySymbols(enrichmentRequest.genes,)

        List<Integer> originalGeneRgdIds = gdao.getActiveGeneRgdIdsBySymbols(enrichmentRequest.genes, originalSpeciesTypeKey);

        List<Integer> geneRgdIds = new ArrayList<Integer>();

        if (originalSpeciesTypeKey == speciesTypeKey) {
            geneRgdIds = originalGeneRgdIds;
        }else {
            OrthologDAO odao = new OrthologDAO();
            List<Ortholog> orthologs = odao.getOrthologsForSourceRgdIds(originalGeneRgdIds, speciesTypeKey);

            for (Ortholog ortholog : orthologs) {
                geneRgdIds.add(ortholog.getDestRgdId());
            }
        }

        List<String> termSet = new ArrayList<>();
        ArrayList<String> aspects = new ArrayList<>();
        Ontology ont = oDao.getOntology(enrichmentRequest.aspect);

        String aspect = ont.getAspect();
        if(aspect.equalsIgnoreCase(Aspect.MAMMALIAN_PHENOTYPE) && speciesTypeKey == SpeciesType.HUMAN) {
            aspects.add(Aspect.HUMAN_PHENOTYPE);
            enrichmentRequest.aspect = "HP";// To get human phenotype for human species
        } else aspects.add(aspect);
        String rootTerm = oDao.getRootTerm(enrichmentRequest.aspect);

        NumberFormat formatter = new DecimalFormat("0.0E0");
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setMinimumFractionDigits(2);

        int refGenes = dao.getReferenceGeneCount(speciesTypeKey,aspects.get(0));
        Map result = new ConcurrentHashMap();
        List geneData = gdao.getGeneByRgdIds(geneRgdIds);
        List<ConcurrentHashMap> enrichmentData = Collections.synchronizedList(new ArrayList<>());
        LinkedHashMap<String, Integer> geneCounts = adao.getGeneCounts(geneRgdIds, termSet, aspects);
        int inputGenes = geneCounts.get(rootTerm);

        //BigDecimal numberOfTerms = new BigDecimal(geneCounts.keySet().size());
        Iterator tit = geneCounts.keySet().iterator();
        geneCounts.keySet().parallelStream().forEach(i -> {
            try {
                synchronized (result) {
                    ConcurrentHashMap data = new ConcurrentHashMap();
                    String acc = (String) tit.next();
                    String term = oDao.getTermByAccId(acc).getTerm();
                    int inputAnnotGenes = geneCounts.get(acc);
                    TermWithStats ts = oDao.getTermWithStatsCached(acc);
                    int withChildren = 1;
                    int refAnnotGenes = ts.getStat("annotated_object_count", speciesTypeKey, RgdId.OBJECT_KEY_GENES, withChildren);
                    //check to remove infinte and negative odds ratio
                    if(refAnnotGenes > inputAnnotGenes) {
                        String pvalue = process.calculatePValue(inputGenes, refGenes, inputAnnotGenes, refAnnotGenes);
                        if (pvalue != null) {
                            // String bonferroni = process.calculateBonferroni(pvalue, numberOfTerms);
                            float oddsRatio = process.calculateOddsRatio(inputGenes, refGenes, inputAnnotGenes, refAnnotGenes);
                            data.put("acc", acc);
                            data.put("term", term);
                            data.put("count", inputAnnotGenes);
                            data.put("refCount", refAnnotGenes);
                            data.put("pvalue", pvalue);
                            data.put("oddsratio", oddsRatio);
                            enrichmentData.add(data);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        BigDecimal numberOfTerms = new BigDecimal(enrichmentData.size());
        List<ConcurrentHashMap> resultData = new ArrayList<>();
        for(ConcurrentHashMap d:enrichmentData){
            String pvalue = (String)(d.get("pvalue"));
            String bonferroni = process.calculateBonferroni(pvalue, numberOfTerms);
            d.put("correctedpvalue", bonferroni);
            resultData.add(d);
        }

        Collections.sort(resultData, new SortbyPvalue());

        result.put("enrichment",resultData);
        result.put("geneSymbols",geneData);
        return result;
    }

    @RequestMapping(value = "/annotatedGenes", method = RequestMethod.POST)
    @ApiOperation(value = "Return a list of genes annotated to the term.Genes are rgdids separated by comma.Species type is an integer value.term is the ontology")
    public Map getEnrichmentData(@RequestBody(required = true) EnrichmentGeneRequest geneRequest)
            throws Exception {

        int speciesTypeKey = SpeciesType.parse(geneRequest.species);
        Map result = new ConcurrentHashMap();
        List geneData = Collections.synchronizedList(new ArrayList<>());
        List genes = Collections.synchronizedList(new ArrayList<>());

        List<Integer> geneRgdIds = gdao.getActiveGeneRgdIdsBySymbols(geneRequest.geneSymbols, speciesTypeKey);
        List<String> termSet = new ArrayList<>();
        termSet.add(geneRequest.accId);
        ArrayList<String> aspects = new ArrayList<>();
        OntologyEnrichment oe = adao.getOntologyEnrichment(geneRgdIds, termSet, aspects);
        TermWrapper tw = null;
        tw = (TermWrapper) oe.termMap.get(geneRequest.accId);
        Iterator git = tw.refs.iterator();
        boolean first = true;
        while (git.hasNext()) {
            ConcurrentHashMap data = new ConcurrentHashMap();
            GeneWrapper gw = (GeneWrapper) git.next();
            Iterator bIt = gw.getRoots(tw).iterator();
            List<String> terms = Collections.synchronizedList(new ArrayList<>());
            while (bIt.hasNext()) {
                Term baseTerm = (Term) bIt.next();
                terms.add(baseTerm.getTerm());
            }
            if(terms.size() == 0)
                terms.add((oDao.getTermByAccId(geneRequest.accId)).getTerm());

            data.put("gene", gw.getGene().getSymbol());
            data.put("terms", terms);
            geneData.add(data);
            genes.add(gw.getGene().getSymbol());
        }

        result.put("geneData", geneData);
        result.put("genes", genes);
        return result;
    }


}

class SortbyPvalue implements Comparator<ConcurrentHashMap> {

    public int compare(ConcurrentHashMap s1, ConcurrentHashMap s2) {
        if (new BigDecimal((String)(s1.get("pvalue"))).compareTo(new BigDecimal((String)( s2.get("pvalue")))) == 0)
            return Integer.compare(((int) s2.get("count")), ((int) s1.get("count")));
        else
            return (new BigDecimal((String)(s1.get("pvalue"))).compareTo(new BigDecimal((String)( s2.get("pvalue")))));
    }
}

