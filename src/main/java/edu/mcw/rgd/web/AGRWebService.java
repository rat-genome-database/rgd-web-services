package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AliasDAO;
import edu.mcw.rgd.dao.impl.AnnotationDAO;
import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.dao.impl.XdbIdDAO;
import edu.mcw.rgd.datamodel.*;
import edu.mcw.rgd.datamodel.ontology.Annotation;
import edu.mcw.rgd.process.Utils;
import edu.mcw.rgd.process.mapping.MapManager;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jdepons on 2/6/2017.
 */

@RestController
@RequestMapping(value = "/agr")

public class AGRWebService {


    @RequestMapping(value="/{taxonId}", method= RequestMethod.GET)
    @ApiOperation(value="Get gene records submitted by RGD to AGR by taxonId", tags="AGR")
    public HashMap getGenesForLatestAssembly(@ApiParam(value="The taxon ID for species", required=true) @PathVariable(value = "taxonId") String taxonId) throws Exception{

        //rat taxon : 10116
        //human taxon : 9606

        HashMap returnMap = new HashMap();


        ArrayList geneList = new ArrayList();

        AliasDAO adao = new AliasDAO();
        GeneDAO geneDAO = new GeneDAO();
        XdbIdDAO xdao = new XdbIdDAO();

        final String[] aliasTypes = {"old_gene_name","old_gene_symbol"};
        final List<Xdb> xdbs = xdao.getActiveXdbs();
        final List<Integer> xdbKeyList = Arrays.asList(21, 20, 3, 14, 34);

        // generic 'pages' object for cross references for gene rgd id
        final List<String> crossRefPagesForGeneRgdId = new ArrayList<>();
        crossRefPagesForGeneRgdId.add("gene");
        crossRefPagesForGeneRgdId.add("gene/references");

        int mapKey = -1;
        if (taxonId.equals("9606")) {
            mapKey=38;
        }else if (taxonId.equals("10116")) {
            mapKey=360;
        }else {
            throw new Exception("Genes for Taxon ID " + taxonId + " not found");
        }
        final String assembly = MapManager.getInstance().getMap(mapKey).getName();

        List<MappedGene> mappedGenes = geneDAO.getActiveMappedGenes(mapKey);

        for (MappedGene mg: mappedGenes) {

            HashMap map = new HashMap();

            Gene g = mg.getGene();
            int rgdId = mg.getGene().getRgdId();


            List crossList = new ArrayList();
            List<XdbId> xdbIds = xdao.getXdbIdsByRgdId(xdbKeyList, rgdId);

            HashMap found = new HashMap();

            String hgncId = null;

            for (XdbId xdbId : xdbIds) {

                if (xdbId.getXdbKey()==21) {
                    hgncId = xdbId.getAccId();
                    continue;
                }

                if (found.containsKey(xdbId.getAccId())) {
                    continue;
                }

                // NCBI Gene, Ensembl Gene and UniProtKB.

                if (xdbId.getXdbKey() == 20 || xdbId.getXdbKey() == 3 || xdbId.getXdbKey() == 14 || xdbId.getXdbKey() == 34) {

                    String dataProvider="";
                    String xdbIdStr="";
                    for (Xdb xdb: xdbs) {
                        if (xdb.getKey() ==xdbId.getXdbKey()) {

                            dataProvider = xdb.getName();

                            if (dataProvider.equals(("NCBI Gene"))) {
                                dataProvider="NCBI_Gene";
                                xdbIdStr=xdbId.getAccId();
                            }else if (dataProvider.equals("UniProt")) {
                                dataProvider="UniProtKB";
                                xdbIdStr=xdbId.getAccId();
                            }else if (dataProvider.equals("Ensembl Genes")) {
                                dataProvider="ENSEMBL";
                                xdbIdStr=xdbId.getAccId();
                            }else if (dataProvider.equals("PANTHER")) {
                                dataProvider="PANTHER";
                                xdbIdStr=xdbId.getAccId().split(":")[0];
                            }

                        }
                    }

                    if (found.containsKey(xdbIdStr)) {
                        continue;
                    }

                    // add a cross-reference object
                    HashMap crossRef = new HashMap();
                    crossRef.put("id", dataProvider+":"+xdbIdStr);
                    crossList.add(crossRef);


                    found.put(xdbIdStr,null);
                }
            }

            if (taxonId.equals("9606") && hgncId == null) {
                continue;
            }


            map.put("crossReferences", crossList);

            map.put("taxonId","NCBITaxon:" + taxonId);


            // human taxon : 9606
            if (taxonId.equals("9606")) {
                map.put("primaryId", hgncId);
                List secondaryIds = new ArrayList();
                secondaryIds.add("RGD:" + rgdId);
                map.put("secondaryIds", secondaryIds);

            }else { //rat taxon : 10116
                map.put("primaryId", "RGD:"+g.getRgdId());
                if (hgncId != null) {
                    List secondaryIds = new ArrayList();
                    secondaryIds.add(hgncId);
                    map.put("secondaryIds", secondaryIds);
                }
            }

            // add a cross-reference object for gene rgd id
            HashMap crossRef = new HashMap();
            crossRef.put("id", "RGD:"+g.getRgdId());
            crossRef.put("pages", crossRefPagesForGeneRgdId);
            crossList.add(crossRef);


            map.put("name", g.getName());
            map.put("symbol", g.getSymbol());
            map.put("geneSynopsis", Utils.getGeneDescription(g));

            //get out of gene types
            map.put("soTermId", mg.getGene().getSoAccId());

            TreeSet<String> synonyms = new TreeSet<>();
            List<Alias> aliases = adao.getAliases(rgdId, aliasTypes);
            for (Alias a : aliases) {
                if (a.getValue() != null) {
                    synonyms.add(a.getValue());
                }
            }
            if( !synonyms.isEmpty() ) {
                map.put("synonyms", synonyms);
            }

            List genomeLocations = new ArrayList();

                HashMap hm = new HashMap();
                hm.put("assembly", assembly);
                hm.put("startPosition", mg.getStart());
                hm.put("endPosition", mg.getStop());
                hm.put("chromosome", mg.getChromosome());
                hm.put("strand", mg.getStrand());
                genomeLocations.add(hm);

            map.put("genomeLocations", genomeLocations);

            geneList.add(map);
        }

        returnMap.put("data",geneList);

        HashMap metadata = new HashMap();
        String date = getDateProduced();

        metadata.put("dateProduced", date);
        metadata.put("dataProvider", getDataProviderForMetaData());
        metadata.put("release", "RGD-1.0.0.3");
        returnMap.put("metaData", metadata);

        return returnMap;
    }


    @RequestMapping(value="/alleles/{taxonId}", method= RequestMethod.GET)
    @ApiOperation(value="Get gene allele records submitted by RGD to AGR by taxonId", tags="AGR")
    public HashMap getAllelesForTaxon(@ApiParam(value="The taxon ID for species", required=true) @PathVariable(value = "taxonId") String taxonId) throws Exception{

        //rat taxon : 10116
        int speciesTypeKey = SpeciesType.parse("taxon:"+taxonId);
        if ( speciesTypeKey!=3 ) {
            throw new Exception("Gene alleles for Taxon ID " + taxonId + " not supported");
        }

        GeneDAO geneDAO = new GeneDAO();
        AliasDAO adao = new AliasDAO();

        HashMap returnMap = new HashMap();
        ArrayList alleleList = new ArrayList();

        List<Gene> alleles = geneDAO.getActiveGenesByType("allele", speciesTypeKey);
        for( Gene g: alleles ) {

            List<Gene> parentGenes = geneDAO.getGeneFromVariant(g.getRgdId());
            if( parentGenes.size()!=1 ) {
                continue;
            }

            HashMap map = new HashMap();

            map.put("primaryId", "RGD:" + g.getRgdId());
            map.put("taxonId", "NCBITaxon:" + taxonId);
            map.put("symbol", g.getSymbol());

            // 'symbolText'
            // the symbol in text format, replacing all html tags with <>.
            // There may be more than one set of <> in the symbol.
            String symbolText = g.getSymbol()
                .replaceAll("<i>|<sup>","<").replaceAll("<<","<")
                .replaceAll("</i>|</sup>",">").replaceAll(">>",">");
            map.put("symbolText", symbolText);

            map.put("gene", "RGD:"+parentGenes.get(0).getRgdId());

            Set<String> synonyms = new TreeSet<>();
            List<Alias> aliases = adao.getAliases(g.getRgdId());
            for( Alias a : aliases ) {
                if( a.getValue() != null ) {
                    synonyms.add(a.getValue());
                }
            }
            if( !synonyms.isEmpty() ) {
                map.put("synonyms", synonyms);
            }

            // cross reference to parent gene
            List crossList = new ArrayList();
            HashMap crossRef = new HashMap();
            List<String> pages = new ArrayList<>();
            pages.add("allele");
            crossRef.put("id", "RGD:"+parentGenes.get(0).getRgdId());
            crossRef.put("pages", pages);
            crossList.add(crossRef);
            map.put("crossReferences", crossList);


            alleleList.add(map);
        }

        returnMap.put("data", alleleList);

        HashMap metadata = new HashMap();
        String date = getDateProduced();

        metadata.put("dateProduced", date);
        metadata.put("dataProvider", getDataProviderForMetaData());
        metadata.put("release", "RGD-1.0.0.3");
        returnMap.put("metaData", metadata);

        return returnMap;
    }

    @RequestMapping(value="/phenotypes/{taxonId}", method= RequestMethod.GET)
    @ApiOperation(value="Get phenotype annotations submitted by RGD to AGR by taxonId", tags="AGR")
    public HashMap getPhenotypesForTaxon(@ApiParam(value="The taxon ID for species", required=true) @PathVariable(value = "taxonId") String taxonId) throws Exception{

        HashMap returnMap = new HashMap();

        String aspect = null;
        int speciesTypeKey = SpeciesType.parse("taxon:"+taxonId);
        if( speciesTypeKey==SpeciesType.RAT ) {
            aspect = "N"; // MP ontology
        } else if( speciesTypeKey==SpeciesType.HUMAN ) {
            aspect = "H"; // HP ontology
        }
        if( aspect==null ) {
            throw new Exception("Phenotype annotations for Taxon ID " + taxonId + " not supported");
        }

        ArrayList phenotypes = new ArrayList();

        AnnotationDAO adao = new AnnotationDAO();
        XdbIdDAO xdao = new XdbIdDAO();

        List<Annotation> annots = adao.getAnnotationsBySpecies(speciesTypeKey, aspect);
        for( Annotation a: annots ) {
            // handle only GENES
            if( a.getRgdObjectKey()!=RgdId.OBJECT_KEY_GENES ) {
                continue;
            }
            // handle only manual annotations
            if( !a.getDataSrc().equals("RGD") ) {
                continue;
            }

            // get pubmed id
            List<XdbId> pmedIds = xdao.getXdbIdsByRgdId(XdbId.XDB_KEY_PUBMED, a.getRefRgdId());
            if( pmedIds.isEmpty() ) {
                System.out.println("no PMID id for REF_RGD_ID "+a.getRefRgdId());
                continue;
            }

            HashMap phenotype = new HashMap();

            // object id
            String objectId;
            if( speciesTypeKey==SpeciesType.RAT ) {
                objectId = "RGD:"+a.getAnnotatedObjectRgdId();
            } else {
                List<XdbId> hgncIds = xdao.getXdbIdsByRgdId(XdbId.XDB_KEY_HGNC, a.getAnnotatedObjectRgdId());
                if( hgncIds.size()!=1 ) {
                    continue;
                }
                objectId = hgncIds.get(0).getAccId();
            }
            phenotype.put("objectId", objectId);

            // phenotype identifiers
            List phenotypeIds = new ArrayList<>();
            HashMap phenotypeId = new HashMap();
            phenotypeId.put("termId", a.getTermAcc());
            phenotypeId.put("termOrder", 1);
            phenotypeIds.add(phenotypeId);
            phenotype.put("phenotypeTermIdentifiers", phenotypeIds);

            phenotype.put("phenotypeStatement", a.getTerm());

            phenotype.put("pubModId", "RGD:"+a.getRefRgdId());

            phenotype.put("pubMedId", "PMID:"+pmedIds.get(0).getAccId());

            phenotype.put("dateAssigned", formatDate(a.getCreatedDate()));

            phenotypes.add(phenotype);
        }


        returnMap.put("data", phenotypes);

        HashMap metadata = new HashMap();
        String date = getDateProduced();

        metadata.put("dateProduced", date);
        metadata.put("dataProvider", getDataProviderForMetaData());
        metadata.put("release", "RGD-1.0.0.3");
        returnMap.put("metaData", metadata);

        return returnMap;
    }

    List getDataProviderForMetaData() {
        List dataProviderArray = new ArrayList();

        HashMap crossReference = new HashMap();
        crossReference.put("id", "RGD");
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        crossReference.put("pages", pages);

        HashMap dataProvider = new HashMap();
        dataProvider.put("type", "curated");
        dataProvider.put("crossReference", crossReference);

        dataProviderArray.add(dataProvider);
        return dataProviderArray;
    }

    String getDateProduced() {
        return formatDate(new Date());
    }

    String formatDate(Date dt) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(dt);
    }
}
