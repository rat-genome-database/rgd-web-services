package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AliasDAO;
import edu.mcw.rgd.dao.impl.AnnotationDAO;
import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.dao.impl.XdbIdDAO;
import edu.mcw.rgd.datamodel.*;
import edu.mcw.rgd.datamodel.ontology.Annotation;
import edu.mcw.rgd.process.Utils;
import edu.mcw.rgd.process.mapping.MapManager;
import io.swagger.annotations.Api;
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
@Api(tags="AGR")
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

				// add a cross-references for human genes:
				// 1) RGD id mapped to 'gene/references' page (to link back to RGD via Literature link an AGR page)
				HashMap crossRef = new HashMap();
				crossRef.put("id", "RGD:"+g.getRgdId());
				List<String> pages = new ArrayList<>();
				pages.add("gene/references");
				crossRef.put("pages", pages);
				crossList.add(crossRef);

				// 2) HGNC id used in the species section of the page to link back to HGNC gene page
				crossRef = new HashMap();
				crossRef.put("id", hgncId);
				pages = new ArrayList<>();
				pages.add("gene");
				crossRef.put("pages", pages);
				crossList.add(crossRef);

				// 3) RGD id (without pages) default_url page
				crossRef = new HashMap();
				crossRef.put("id", "RGD:"+g.getRgdId());
				crossList.add(crossRef);

				}else { //rat taxon : 10116
                map.put("primaryId", "RGD:"+g.getRgdId());
                if (hgncId != null) {
                    List secondaryIds = new ArrayList();
                    secondaryIds.add(hgncId);
                    map.put("secondaryIds", secondaryIds);
                }

				// add a cross-reference object for gene rgd id
				HashMap crossRef = new HashMap();
				crossRef.put("id", "RGD:"+g.getRgdId());
				crossRef.put("pages", crossRefPagesForGeneRgdId);
				crossList.add(crossRef);
            }


            map.put("name", g.getName());
            map.put("symbol", g.getSymbol());
			String geneSynopsis;
			// emit merged-descriptions (AGR automated desc merged with RGD automated desc) for rat genes
			if( mapKey==360 ) {
				geneSynopsis = g.getMergedDescription();
			} else { // and RGD automated desc for human genes
				geneSynopsis = Utils.getGeneDescription(g);
			}
			if( !Utils.isStringEmpty(geneSynopsis) ) {
				map.put("geneSynopsis", geneSynopsis);
			}

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

        returnMap.put("metaData", getMetaData());

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
            crossRef.put("id", "RGD:"+g.getRgdId());
            crossRef.put("pages", pages);
            crossList.add(crossRef);
            map.put("crossReferences", crossList);


            alleleList.add(map);
        }

        returnMap.put("data", alleleList);

        returnMap.put("metaData", getMetaData());

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

        List<Annotation> annots = adao.getAnnotationsBySpeciesAspectAndSource(speciesTypeKey, aspect, "RGD");
        for( Annotation a: annots ) {
            // handle only GENES
            if( a.getRgdObjectKey()!=RgdId.OBJECT_KEY_GENES ) {
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

            HashMap evidenceMap = new HashMap();
            evidenceMap.put("modPublicationId", "RGD:"+a.getRefRgdId());
            evidenceMap.put("pubMedId", "PMID:"+pmedIds.get(0).getAccId());
            phenotype.put("evidence",evidenceMap);

            phenotype.put("dateAssigned", formatDate(a.getCreatedDate()));

            phenotypes.add(phenotype);
        }


        returnMap.put("data", phenotypes);

        returnMap.put("metaData", getMetaData());

        return returnMap;
    }

    @RequestMapping(value="/expression/{taxonId}", method= RequestMethod.GET)
    @ApiOperation(value="Get expression annotations submitted by RGD to AGR by taxonId", tags="AGR")
    public HashMap getExpressionForTaxon(@ApiParam(value="The taxon ID for species", required=true) @PathVariable(value = "taxonId") String taxonId) throws Exception{

        HashMap returnMap = new HashMap();

        int speciesTypeKey = SpeciesType.parse("taxon:"+taxonId);

        ArrayList records = new ArrayList();

        AnnotationDAO adao = new AnnotationDAO();
        List<Annotation> annots = adao.getAnnotationsBySpeciesAspectAndSource(speciesTypeKey, "C", "RGD");
        XdbIdDAO xdao = new XdbIdDAO();

        for( Annotation a: annots ) {
            // handle only GENES
            if( a.getRgdObjectKey()!=RgdId.OBJECT_KEY_GENES ) {
                continue;
            }
            // process only original annotations - WITH_INFO must be NULL
            if( a.getWithInfo()!=null ) {
                continue;
            }
            // allowed evidence codes: IDA, IMP, IPI
            if( !(a.getEvidence().equals("IDA") || a.getEvidence().equals("IMP") || a.getEvidence().equals("IPI")) ) {
                continue;
            }
            // the only qualifier we support is 'colocalizes_with'
            String qualifier = a.getQualifier();
            if( !Utils.isStringEmpty(qualifier) ) {
                if( !qualifier.equals("colocalizes_with") ) {
                    continue;
                }
                qualifier = "RO:0002325";
            }

            // get pubmed id
            List<XdbId> pmedIds = xdao.getXdbIdsByRgdId(XdbId.XDB_KEY_PUBMED, a.getRefRgdId());
            if( pmedIds.isEmpty() ) {
                System.out.println("no PMID id for REF_RGD_ID "+a.getRefRgdId());
                continue;
            }

            // special rule: if NOTES field contains an MMO:xxxxxxx term acc id, it should be used to override
            // the default assay term

            HashMap record = new HashMap();

            // gene id
            String geneId;
            if( speciesTypeKey==SpeciesType.RAT ) {
                geneId = "RGD:"+a.getAnnotatedObjectRgdId();
            } else {
                List<XdbId> hgncIds = xdao.getXdbIdsByRgdId(XdbId.XDB_KEY_HGNC, a.getAnnotatedObjectRgdId());
                if( hgncIds.size()!=1 ) {
                    continue;
                }
                geneId = hgncIds.get(0).getAccId();
            }
            record.put("geneId", geneId);

            record.put("dateAssigned", formatDate(a.getCreatedDate()));

            // evidence
            HashMap evidenceMap = new HashMap();
            evidenceMap.put("modPublicationId", "RGD:"+a.getRefRgdId());
            evidenceMap.put("pubMedId", "PMID:"+pmedIds.get(0).getAccId());
            record.put("evidence",evidenceMap);

            // expression ids
            String assay = "MMO:0000640"; // expression assay
            HashMap whereExpressed = new HashMap();
            whereExpressed.put("cellularComponentTermId", a.getTermAcc());
            if( !Utils.isStringEmpty(qualifier) ) {
                whereExpressed.put("cellularComponentQualifierTermId", qualifier);
            }
            String whereExpressedStmt = a.getTerm();
            if( !Utils.isStringEmpty(a.getNotes()) ) {
				int mmoTermPos = a.getNotes().indexOf("MMO:");
				if( mmoTermPos>=0 && mmoTermPos+11<=a.getNotes().length() ) {
                    assay = a.getNotes().substring(mmoTermPos, mmoTermPos+11);
                } else {
                    whereExpressedStmt += "; "+a.getNotes();
                }
            }
            whereExpressed.put("whereExpressedStatement", whereExpressedStmt);

            record.put("whereExpressed", whereExpressed);

            HashMap whenExpressed = new HashMap();
            whenExpressed.put("stageName", "N/A");
            record.put("whenExpressed", whenExpressed);

            record.put("assay", assay);

            records.add(record);
        }

        returnMap.put("data", records);

        returnMap.put("metaData", getMetaData());

        return returnMap;
    }

    HashMap getDataProviderForMetaData() {

        HashMap crossReference = new HashMap();
        crossReference.put("id", "RGD");
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        crossReference.put("pages", pages);

        HashMap dataProvider = new HashMap();
        dataProvider.put("type", "curated");
        dataProvider.put("crossReference", crossReference);

        return dataProvider;
    }

    HashMap getMetaData() {
        HashMap metadata = new HashMap();
        String date = getDateProduced();

        metadata.put("dateProduced", date);
        metadata.put("dataProvider", getDataProviderForMetaData());
        metadata.put("release", "RGD-1.0.0.7");
        return metadata;
    }

    String getDateProduced() {
        return formatDate(new Date());
    }

    String formatDate(Date dt) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(dt);
    }
}
