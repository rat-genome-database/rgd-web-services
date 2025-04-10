package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.*;
import edu.mcw.rgd.datamodel.*;
import edu.mcw.rgd.datamodel.Map;
import edu.mcw.rgd.datamodel.ontology.Annotation;
import edu.mcw.rgd.process.FileDownloader;
import edu.mcw.rgd.process.Utils;
import edu.mcw.rgd.process.mapping.MapManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@Tag(name="AGR")
@RequestMapping(value = "/agr")
public class AGRWebService {

    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/{taxonId}", method= RequestMethod.GET)
    @Operation(summary="Get gene records submitted by RGD to AGR by taxonId", tags="AGR")
    public HashMap getGenesForLatestAssembly(HttpServletRequest request, @Parameter(description="The taxon ID for species", required=true) @PathVariable(name = "taxonId") String taxonId) throws Exception{

        //rat taxon : 10116
        //human taxon : 9606

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        boolean debug = true;
        int multis = 0;
        int genesWithoutPos = 0;

        ArrayList geneList = new ArrayList();

        AliasDAO adao = new AliasDAO();
        GeneDAO geneDAO = new GeneDAO();
        MapDAO mdao = new MapDAO();
        XdbIdDAO xdao = new XdbIdDAO();

        final String[] aliasTypes = {"old_gene_name","old_gene_symbol"};
        final List<Xdb> xdbs = xdao.getActiveXdbs();
        final List<Integer> xdbKeyList = Arrays.asList(21, 20, 3, 14, 34);

        // generic 'pages' object for cross references for gene rgd id
        final List<String> crossRefPagesForGeneRgdId = new ArrayList<>();
        crossRefPagesForGeneRgdId.add("gene");
        crossRefPagesForGeneRgdId.add("gene/references");

        int speciesTypeKey = 0;
        int mapKey1 = 0, mapKey2 = 0;
        if (taxonId.equals("9606")) {
            speciesTypeKey = SpeciesType.HUMAN;
            mapKey1 = 38;
            mapKey2 = 40;
        }else if (taxonId.equals("10116")) {
            speciesTypeKey = SpeciesType.RAT;
            mapKey1 = 372;
            mapKey2 = 373;
        }else {
            throw new Exception("Genes for Taxon ID " + taxonId + " not found");
        }
        final String assembly = MapManager.getInstance().getMap(mapKey1).getName();

        List<Gene> genes = geneDAO.getActiveGenes(speciesTypeKey);
        for (Gene g: genes ) {

            // sanity check
            if( g.getSoAccId()==null ) {
                continue;
            }

            // do not submit genes without positions on primary assembly
            List<MapData> mds = getLoci(g.getRgdId(), mapKey1, mapKey2, mdao);
            if( mds.isEmpty() ) {
                genesWithoutPos++;
            }
            // display genes with multiple loci
            if( debug && mds.size()>1) {
                multis++;
                System.out.println(multis+" RGD:"+g.getRgdId()+" "+g.getSymbol());
                for( MapData md: mds ) {
                    System.out.println("    " +md.toString());
                }
            }

            HashMap map = new HashMap();

            int rgdId = g.getRgdId();


            List crossList = new ArrayList();
            List<XdbId> xdbIds = xdao.getXdbIdsByRgdId(xdbKeyList, rgdId);
            dropTremblIfSwissProtAvailable(xdbIds);

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


            HashMap basicGeneticEntity = new HashMap();
            map.put("basicGeneticEntity", basicGeneticEntity);

            basicGeneticEntity.put("crossReferences", crossList);

            basicGeneticEntity.put("taxonId","NCBITaxon:" + taxonId);


            // human taxon : 9606
            if (taxonId.equals("9606")) {
                basicGeneticEntity.put("primaryId", hgncId);

                List secondaryIds = new ArrayList();
                secondaryIds.add("RGD:" + rgdId);
                basicGeneticEntity.put("secondaryIds", secondaryIds);

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

            } else { //rat taxon : 10116
                basicGeneticEntity.put("primaryId", "RGD:"+g.getRgdId());
                if (hgncId != null) {
                    List secondaryIds = new ArrayList();
                    secondaryIds.add(hgncId);
                    basicGeneticEntity.put("secondaryIds", secondaryIds);
                }

                // add a cross-reference object for gene rgd id
                HashMap crossRef = new HashMap();
                crossRef.put("id", "RGD:"+g.getRgdId());
                crossRef.put("pages", crossRefPagesForGeneRgdId);
                crossList.add(crossRef);
            }

            if( g.getName()!=null ) {
                map.put("name", g.getName());
            }
            map.put("symbol", g.getSymbol());

            String geneSynopsis;
            // emit merged-descriptions (AGR automated desc merged with RGD automated desc) for rat genes
            if( speciesTypeKey==SpeciesType.RAT ) {
                geneSynopsis = g.getMergedDescription();
            } else { // and RGD automated desc for human genes
                geneSynopsis = Utils.getGeneDescription(g);
            }
            if( !Utils.isStringEmpty(geneSynopsis) ) {
                map.put("geneSynopsis", geneSynopsis);
            }

            //get out of gene types
            map.put("soTermId", g.getSoAccId());

            TreeSet<String> synonyms = new TreeSet<>();
            List<Alias> aliases = adao.getAliases(rgdId, aliasTypes);
            for (Alias a : aliases) {
                if (a.getValue() != null) {
                    synonyms.add(a.getValue());
                }
            }
            if( !synonyms.isEmpty() ) {
                basicGeneticEntity.put("synonyms", synonyms);
            }

            if( !mds.isEmpty() ) {
                List genomeLocations = new ArrayList();

                for (MapData md : mds) {
                    HashMap hm = new HashMap();
                    hm.put("assembly", assembly);
                    hm.put("startPosition", md.getStartPos());
                    hm.put("endPosition", md.getStopPos());
                    hm.put("chromosome", md.getChromosome());
                    hm.put("strand", md.getStrand());
                    genomeLocations.add(hm);
                }

                basicGeneticEntity.put("genomeLocations", genomeLocations);
            }

            geneList.add(map);
        }

        System.out.println("multis: "+multis);
        System.out.println("genes without pos"+genesWithoutPos);

        HashMap returnMap = new HashMap();
        returnMap.put("data",geneList);
        returnMap.put("metaData", getMetaData());
        return returnMap;
    }

    // get gene loci from NCBI and Ensembl assemblies, and merge the loci that overlap
    List<MapData> getLoci(int rgdId, int mapKey1, int mapKey2, MapDAO mdao) throws Exception {

        List<MapData> mds1 = mdao.getMapData(rgdId, mapKey1);
        List<MapData> mds2 = mdao.getMapData(rgdId, mapKey2);

        List<MapData> mds = new ArrayList<>();
        mergeLoci(mds, mds1);
        mergeLoci(mds, mds2);

        return mds;
    }

    void mergeLoci(List<MapData> mds1, List<MapData> mds2) {

        for( MapData md2: mds2 ) {

            // look for overlapping positions
            boolean overlappingPos = false;
            for( MapData md1: mds1 ) {
                if( !md1.getChromosome().equals(md2.getChromosome()) ) {
                    continue;
                }
                // same chr:
                if( md2.getStartPos()<=md1.getStopPos()  &&  md1.getStartPos()<=md2.getStopPos() ) {
                    // positions overlap: update 'md1'
                    md1.setStartPos(Math.min(md1.getStartPos(), md2.getStartPos()));
                    md1.setStopPos(Math.max(md1.getStopPos(), md2.getStopPos()));
                    overlappingPos = true;
                    break;
                }
            }
            if( !overlappingPos ) {
                mds1.add(md2);
            }
        }
    }

    void dropTremblIfSwissProtAvailable(List<XdbId> xdbIds) {

        // check if there are any SwissProt entries
        boolean isSwissProt = false;
        for( XdbId id: xdbIds ) {
            if( id.getXdbKey()==14 && Utils.stringsAreEqualIgnoreCase(id.getSrcPipeline(), "UniProtKB/Swiss-Prot") ) {
                isSwissProt = true;
                break;
            }
        }

        if( isSwissProt ) {
            // remove all non Swiss-Prot entries
            xdbIds.removeIf(id -> id.getXdbKey() == 14 && !Utils.stringsAreEqualIgnoreCase(id.getSrcPipeline(), "UniProtKB/Swiss-Prot"));
        }
    }


    @RequestMapping(value="/alleles/{taxonId}", method= RequestMethod.GET)
    @Operation(summary="Get gene allele records submitted by RGD to AGR by taxonId", tags="AGR")
    public HashMap getAllelesForTaxon(HttpServletRequest request,@Parameter(description="The taxon ID for species", required=true) @PathVariable(name = "taxonId") String taxonId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
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

            HashMap map = new HashMap();

            map.put("primaryId", "RGD:" + g.getRgdId());
            map.put("taxonId", "NCBITaxon:" + taxonId);
            map.put("symbol", g.getSymbol());

            // allele description: combine allele name with allele description
            String desc = g.getName();
            if( !Utils.isStringEmpty(g.getDescription()) ) {
                if( desc==null ) {
                    desc = g.getDescription();
                } else {
                    desc += "; "+g.getDescription();
                }
            }
            if( !Utils.isStringEmpty(desc) ) {
                map.put("description", desc);
            }

            // 'symbolText'
            // the symbol in text format, replacing all html tags with <>.
            // There may be more than one set of <> in the symbol.
            String symbolText = g.getSymbol()
                .replaceAll("<i>|<sup>","<").replaceAll("<<","<")
                .replaceAll("</i>|</sup>",">").replaceAll(">>",">");
            map.put("symbolText", symbolText);

            // parent gene for the allele
            List<Gene> parentGenes = geneDAO.getGeneFromVariant(g.getRgdId());
            List alleleObjectRelations = new ArrayList();
            for( Gene parentGene: parentGenes ) {
                HashMap objRel = new HashMap();
                objRel.put("associationType", "allele_of");
                objRel.put("gene", "RGD:" + parentGenes.get(0).getRgdId());

                HashMap alleleObjRel = new HashMap();
                alleleObjRel.put("objectRelation", objRel);
                alleleObjectRelations.add(alleleObjRel);
            }
            if( !alleleObjectRelations.isEmpty() ) {
                map.put("alleleObjectRelations", alleleObjectRelations);
            }

            // synonyms
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
            pages.add("allele/references");
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

    @RequestMapping(value="/affectedGenomicModels/{taxonId}", method= RequestMethod.GET)
    @Operation(summary="Get affected genomic models (rat strains with gene alleles) submitted by RGD to AGR by taxonId", tags="AGR")
    public HashMap getAffectedGenomicModels(HttpServletRequest request,@Parameter(description="The taxon ID for species", required=true) @PathVariable(name = "taxonId") String taxonId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        //rat taxon : 10116
        int speciesTypeKey = SpeciesType.parse("taxon:"+taxonId);
        if ( speciesTypeKey!=3 ) {
            throw new Exception("Affected genomic models for Taxon ID " + taxonId + " not supported");
        }

        AliasDAO aliasDAO = new AliasDAO();
        AssociationDAO assocDAO = new AssociationDAO();
        RGDManagementDAO idDAO = new RGDManagementDAO();
        StrainDAO strainDAO = assocDAO.getStrainDAO();

        List<Strain> strains = strainDAO.getActiveStrains();

        ArrayList resultList = new ArrayList();

        for( Strain strain: strains ) {
            HashMap map = new HashMap();

            map.put("primaryID", "RGD:" + strain.getRgdId());
            map.put("name", strain.getSymbol());
            map.put("subtype", "strain");
            map.put("taxonId", "NCBITaxon:10116"); // rat taxon id

            // cross reference: back link to RGD
            HashMap crossRef = new HashMap();
            List<String> pages = new ArrayList<>();
            pages.add("strain");
            crossRef.put("id", "RGD:"+strain.getRgdId());
            crossRef.put("pages", pages);
            map.put("crossReference", crossRef);

            // synonyms
            List<Alias> aliases = aliasDAO.getAliases(strain.getRgdId());
            if( !aliases.isEmpty() ) {
                List<String> synonyms = new ArrayList<String>();
                for (Alias alias : aliases) {
                    synonyms.add(alias.getValue());
                }
                map.put("synonyms", synonyms);
            }

            // secondary ids
            List<Integer> secondaryRgdIds = idDAO.getOldRgdIds(strain.getRgdId());
            if( !secondaryRgdIds.isEmpty() ) {
                List<String> secondaryIds = new ArrayList<String>();
                for( Integer rgdId: secondaryRgdIds ) {
                    secondaryIds.add("RGD:"+rgdId);
                }
                map.put("secondaryIds", secondaryIds);
            }

            // parental population ids (background ids)
            if( strain.getBackgroundStrainRgdId()!=null && strain.getBackgroundStrainRgdId()!=0 ) {
                List<String> backgroundStrainRgdIds = new ArrayList<String>();
                backgroundStrainRgdIds.add("RGD:"+strain.getBackgroundStrainRgdId());
                map.put("parentalPopulationIDs", backgroundStrainRgdIds);
            }

            // gene alleles for the strain
            List<Strain2MarkerAssociation> geneAlleles = assocDAO.getStrain2GeneAssociations(strain.getRgdId());
            Iterator<Strain2MarkerAssociation> it = geneAlleles.iterator();
            while( it.hasNext() ) {
                Strain2MarkerAssociation i = it.next();
                if( !Utils.stringsAreEqual(Utils.NVL(i.getMarkerType(),"allele"), "allele") ) {
                    it.remove();
                }
            }
            if( !geneAlleles.isEmpty() ) {
                List affectedGenomicModelComponents = new ArrayList();
                for( Strain2MarkerAssociation assoc: geneAlleles ) {
                    HashMap<String,String> component = new HashMap<String, String>();
                    component.put("alleleID", "RGD:"+assoc.getMarkerRgdId());

                    // convert strain genetic status to zygosity
                    String zygosity = "GENO:0000137"; // unspecified
                    if( strain.getGeneticStatus()!=null ) {
                        if( strain.getGeneticStatus().equals("Wild Type") || strain.getGeneticStatus().equals("Homozygous") ) {
                            zygosity = "GENO:0000136"; // homozygous
                        } else
                        if( strain.getGeneticStatus().equals("Heterozygous") ) {
                            zygosity = "GENO:0000135"; // heterozygous
                        } else
                        if( strain.getGeneticStatus().equals("Hemizygous") ) {
                            zygosity = "GENO:0000134"; // hemizygous
                        }
                    }
                    component.put("zygosity", zygosity);

                    affectedGenomicModelComponents.add(component);
                }
                map.put("affectedGenomicModelComponents", affectedGenomicModelComponents);
            }

            resultList.add(map);
        }

        HashMap returnMap = new HashMap();
        returnMap.put("data", resultList);
        returnMap.put("metaData", getMetaData());

        return returnMap;
    }

    @RequestMapping(value="/variants/{taxonId}", method= RequestMethod.GET)
    @Operation(summary="Get basic variant records submitted by RGD to AGR by taxonId", tags="AGR")
    public HashMap getVariantsForTaxon(HttpServletRequest request,@Parameter(description="The taxon ID for species", required=true) @PathVariable(name = "taxonId") String taxonId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        //rat taxon : 10116
        int speciesTypeKey = SpeciesType.parse("taxon:"+taxonId);

        HashMap returnMap = new HashMap();
        ArrayList variantList = new ArrayList();

        final int mapKey = 372;
        Map map = MapManager.getInstance().getMap(mapKey);
        RgdVariantDAO vdao = new RgdVariantDAO();
        MapDAO mdao = new MapDAO();
        FileDownloader fd = new FileDownloader();
        GeneDAO gdao = new GeneDAO();

        List<RgdVariant> variants = vdao.getVariantsForSpecies(speciesTypeKey);
        for( RgdVariant var: variants ) {

            List<Gene> geneList = gdao.getAssociatedGenes(var.getRgdId());
            List<MapData> mds = mdao.getMapData(var.getRgdId(), map.getKey());
            for( MapData md: mds ) {

                for( Gene g: geneList ) {
                    String alleleId = "RGD:" + g.getRgdId();
                    String type = var.getType();
                    String assembly = map.getName();
                    String chromosome = md.getChromosome();
                    int start = md.getStartPos();
                    int end = md.getStopPos();
                    String genomicReferenceSequence = Utils.NVL(var.getRefNuc(), "N/A");
                    String genomicVariantSequence = Utils.NVL(var.getVarNuc(), "N/A");

                    String paddedBase = null;
                    Integer insertionLength = null;
                    String consequence = null;

                    switch(type) {
                        // types directly supported by AGR schema
                        case "SO:1000008": System.out.println("snv"); break;
                        case "SO:0000667": System.out.println("ins"); break;
                        case "SO:0000159": System.out.println("del"); break;
                        case "SO:0002007": System.out.println("mnv"); break;
                        case "SO:1000032": System.out.println("delin"); break;

                        // types to be converted
                        case "SO:1000011": System.out.println("C_to_T_transition");
                            consequence = type;
                            type = "SO:1000008"; // SNV
                            break;

                        // types not covered yet
                        default: System.out.println("problem");
                    }

                    // emit 'paddedBase' for insertions and deletions
                    if( type.equals("SO:0000667") || type.equals("SO:0000159") ) {
                        int paddedBasePos = 0;
                        if( type.equals("SO:0000159") ) { // deletion
                            paddedBasePos = start - 1;
                        } else if( type.equals("SO:0000667") ) { // insertion: ref nuc is the padding base
                            paddedBasePos = start;
                            insertionLength = genomicVariantSequence.length();
                        }
                        String url = "https://pipelines.rgd.mcw.edu/rgdweb/seqretrieve/retrieve.html?mapKey=" + map.getKey() +
                                "&chr=" + chromosome + "&startPos=" + paddedBasePos + "&stopPos=" + paddedBasePos + "&format=text";
                        fd.setExternalFile(url);
                        paddedBase = fd.download();
                    }

                    Chromosome c = mdao.getChromosome(map.getKey(), chromosome);

                    HashMap rec = new HashMap();
                    rec.put("alleleId", alleleId);
                    rec.put("type", type);
                    rec.put("assembly", assembly);
                    rec.put("chromosome", chromosome);
                    rec.put("start", start);
                    rec.put("end", end);
                    rec.put("genomicReferenceSequence", genomicReferenceSequence);
                    rec.put("genomicVariantSequence", genomicVariantSequence);
                    if( paddedBase!=null ) {
                        rec.put("paddedBase", paddedBase);
                    }
                    if( consequence!=null ) {
                        rec.put("consequence", consequence);
                    }
                    if( insertionLength!=null ) { // added in schema 1.0.0.9
                        rec.put("insertionLength", insertionLength);
                    }
                    rec.put("sequenceOfReferenceAccessionNumber", "RefSeq:"+c.getRefseqId());

                    variantList.add(rec);
                }
            }
        }

        returnMap.put("data", variantList);
        returnMap.put("metaData", getMetaData());

        return returnMap;
    }

    @RequestMapping(value="/phenotypes/{taxonId}", method= RequestMethod.GET)
    @Operation(summary="Get phenotype annotations submitted by RGD to AGR by taxonId", tags="AGR")
    public HashMap getPhenotypesForTaxon(HttpServletRequest request,@Parameter(description="The taxon ID for species", required=true) @PathVariable(name = "taxonId") String taxonId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
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
        StrainDAO sdao = new StrainDAO();
        XdbIdDAO xdao = new XdbIdDAO();

        List<Annotation> annots = adao.getAnnotationsBySpeciesAspectAndSource(speciesTypeKey, aspect, "RGD");
        if( aspect.equals("H") ) {
            // for human HPO, in addition to manual RGD annots, also load the other annots
            annots.addAll(adao.getAnnotationsBySpeciesAspectAndSource(speciesTypeKey, aspect, "HPO"));
        }
        for( Annotation a: annots ) {
            // handle only GENES and STRAINS
            if( !(a.getRgdObjectKey()==RgdId.OBJECT_KEY_GENES || a.getRgdObjectKey()==RgdId.OBJECT_KEY_STRAINS) ) {
                continue;
            }
            // STRAINS must be of mutant type
            if( a.getRgdObjectKey()==RgdId.OBJECT_KEY_STRAINS ) {
                Strain strain = sdao.getStrain(a.getAnnotatedObjectRgdId());
                if( !Utils.stringsAreEqual(strain.getStrainTypeName(), "mutant") ) {
                    continue; // strain type is different than 'mutant'
                }
            }

            // skip annotations with qualifiers 'NOT','no_association'
            if( a.getQualifier()!=null ) {
                if( a.getQualifier().equals("no_association") || a.getQualifier().equals("NOT") ) {
                    continue;
                }
            }

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

            List<HashMap> evidenceList = handleEvidence(xdao, a);
            for( HashMap evidenceMap: evidenceList ) {

                HashMap phenotype = new HashMap();
                phenotype.put("evidence", evidenceMap);

                phenotype.put("objectId", objectId);

                // phenotype identifiers
                List phenotypeIds = new ArrayList<>();
                HashMap phenotypeId = new HashMap();
                phenotypeId.put("termId", a.getTermAcc());
                phenotypeId.put("termOrder", 1);
                phenotypeIds.add(phenotypeId);
                phenotype.put("phenotypeTermIdentifiers", phenotypeIds);

                phenotype.put("phenotypeStatement", a.getTerm());

                phenotype.put("dateAssigned", formatDate(a.getCreatedDate()));

                phenotypes.add(phenotype);
            }
        }


        returnMap.put("data", phenotypes);

        returnMap.put("metaData", getMetaData());

        return returnMap;
    }

    List<HashMap> handleEvidence(XdbIdDAO xdao, Annotation a) throws Exception {

        List<HashMap> evidenceList = new ArrayList<>();

        if( a.getDataSrc().equals("HPO") ) {

            if( Utils.isStringEmpty(a.getXrefSource()) ) {
                return evidenceList;
            }
            // notes contain multiple space separated OMIM:xxx or ORPHA:xxxx ids
            String[] omimOrphaIds = a.getXrefSource().split("[|,\\s]");
            for( String omimOrphaId: omimOrphaIds ) {

                if( omimOrphaId.startsWith("MIM:") || omimOrphaId.startsWith("PMID:") || omimOrphaId.startsWith("ORPHA:") ) {
                    HashMap evidence = new HashMap<>();
                    evidenceList.add(evidence);

                    evidence.put("publicationId", omimOrphaId);

                    HashMap crossRef = new HashMap<>();
                    crossRef.put("id", omimOrphaId);
                    List<String> pages = new ArrayList<>();
                    pages.add("disease");
                    crossRef.put("pages", pages);

                    evidence.put("crossReference", crossRef);
                }
            }
            return evidenceList;
        }

        // one evidence for RAT and HUMAN manual RGD annotations
        HashMap evidence = handleEvidence(xdao, a.getRefRgdId());
        evidenceList.add(evidence);

        return evidenceList;
    }

    HashMap handleEvidence(XdbIdDAO xdao, int refRgdId) throws Exception {

        // one evidence for RAT and HUMAN manual RGD annotations
        HashMap evidence = new HashMap<>();

        List<XdbId> pmidIds = xdao.getXdbIdsByRgdId(XdbId.XDB_KEY_PUBMED, refRgdId);
        String pmid = null;
        if( !pmidIds.isEmpty() ) {
            pmid = "PMID:"+pmidIds.get(0).getAccId();
        }

        // look for a PMID
        if( pmid!=null ) {
            evidence.put("publicationId", pmid);

            if( refRgdId>0 ) {
                HashMap crossRef = new HashMap<>();
                crossRef.put("id", "RGD:" + refRgdId);
                List<String> pages = new ArrayList<>();
                pages.add("reference");
                crossRef.put("pages", pages);

                evidence.put("crossReference", crossRef);
            }
        }

        // no PMID available -- set reference to REF_RGD_ID
        else if( refRgdId>0 ) {
            evidence.put("publicationId", "RGD:" + refRgdId);

            HashMap crossRef = new HashMap<>();
            crossRef.put("id", "RGD:" + refRgdId);
            List<String> pages = new ArrayList<>();
            pages.add("reference");
            crossRef.put("pages", pages);

            evidence.put("crossReference", crossRef);

        } else {
            System.out.println("*** WARN *** unexpected ref rgd id: "+refRgdId);
        }

        return evidence;
    }

    @RequestMapping(value="/expression/{taxonId}", method= RequestMethod.GET)
    @Operation(summary="Get expression annotations submitted by RGD to AGR by taxonId", tags="AGR")
    public HashMap getExpressionForTaxon(HttpServletRequest request,@Parameter(description="The taxon ID for species", required=true) @PathVariable(name = "taxonId") String taxonId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
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

            // as of Sep 2024, we don't submit RGD qualifiers; AGR qualifiers have different semantics
            String qualifier = null;

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
            HashMap evidenceMap = handleEvidence(xdao, a.getRefRgdId());
            if( evidenceMap!=null ) {
                record.put("evidence", evidenceMap);
            }

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
        metadata.put("release", "RGD-2024-SEP-12");
        return metadata;
    }

    String getDateProduced() {
        return formatDate(new Date());
    }

    String formatDate(Date dt) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(dt);
    }
}
