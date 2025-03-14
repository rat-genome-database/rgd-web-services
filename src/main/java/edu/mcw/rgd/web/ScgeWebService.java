package edu.mcw.rgd.web;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.dao.impl.MapDAO;
import edu.mcw.rgd.dao.impl.TranscriptDAO;
import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.MapData;
import edu.mcw.rgd.datamodel.Transcript;
import edu.mcw.rgd.process.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;


@RestController
@Tag(name="SCGE")
@RequestMapping(value = "/scge")
public class ScgeWebService {

    AccessLogDAO ald = new AccessLogDAO();

    GeneDAO geneDAO = new GeneDAO();
    MapDAO mapDAO = new MapDAO();
    TranscriptDAO trDAO = new TranscriptDAO();

    @RequestMapping(value="/track/{species}/All Genes/{chr}:{startPos}..{stopPos}.json", method= RequestMethod.GET)
    @Operation(summary="Return a a full gene model given gene coordinates, f.e. human  19:55090918..55117637", tags = "SCGE")
    public Object getApollo(HttpServletRequest request,
                            @Parameter(description="Species, f.e human", required=true) @PathVariable(name = "species") String species,
                            @Parameter(description="Chromosome, f.e 19", required=true) @PathVariable(name = "chromosome") String chr,
                            @Parameter(description="Start Pos, f.e. 55090918", required=true) @PathVariable(name = "startPos") Integer startPos,
                            @Parameter(description="Stop Pos, f.e. 55117637", required=true) @PathVariable(name = "stopPos") Integer stopPos,
                            @Parameter(description="Ignore cache", required=false) @PathVariable(name = "ignoreCache") Boolean ignoreCache
                            ) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(), request);

        JsonObj obj = getGeneModel( chr, startPos, stopPos );
        return obj;
    }

    @RequestMapping(value="/gene/{geneCoordinates}", method= RequestMethod.GET)
    @Operation(summary="Return a a full gene model given gene coordinates, f.e. 19:55090918..55117637", tags = "SCGE")
    public Object getGeneModel(HttpServletRequest request, @Parameter(description="Gene coordinates, f.e. 19:55090918..55117637", required=true) @PathVariable(name = "geneCoordinates") String geneCoordinates) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);

        String args = geneCoordinates;
        String chr = null;
        int startPos = 0;
        int stopPos = 0;

        int colonPos = geneCoordinates.indexOf(':');
        if( colonPos > 0 ) {
            chr = geneCoordinates.substring(0, colonPos);

            int doubleDotPos = args.indexOf("..");
            if( doubleDotPos > 0 ) {
                startPos = Integer.parseInt(args.substring(colonPos+1, doubleDotPos));
                stopPos = Integer.parseInt(args.substring(doubleDotPos+2));
            }
        }

        if( chr!=null && startPos>0 && stopPos>0 ) {
            JsonObj jsonObj = getGeneModel(chr, startPos, stopPos);
            return jsonObj;
        }
        return null;
    }


    JsonObj getGeneModel( String chr, int startPos, int stopPos ) throws Exception {

        int MAP_KEY = 38;

        List<Gene> genes = geneDAO.getActiveGenes( chr, startPos, stopPos, MAP_KEY );
        genes.removeIf( g -> Utils.NVL(g.getType(),"").equals("biological-region") );
        if( genes.isEmpty() ) {
            return null;
        }
        Gene gene = genes.get(0);

        List<MapData> mds = mapDAO.getMapData( gene.getRgdId(), MAP_KEY );
        if( mds.isEmpty() ) {
            return null;
        }
        MapData md = mds.get(0);

        List<Transcript> trs = trDAO.getTranscriptsForGene(gene.getRgdId(), MAP_KEY);

        JsonObj obj = new JsonObj();
        obj.sourceUrl = "http://www.alliancegenome.org/apollo/track/human/All Genes/"+chr+"/"+gene.getSymbol()+".json";
        obj.strand = Utils.NVL(md.getStrand(), "+").equals("-") ? -1 : +1;

        obj.name = gene.getSymbol();
        obj.id = "RGD:"+gene.getRgdId();
        obj.fmin = startPos;
        obj.fmax = stopPos;
        obj.seqId = chr;

        CdsUtils utils = new CdsUtils(this.trDAO, MAP_KEY);

        for( Transcript tr: trs ) {
            MapData trmd = tr.getGenomicPositions().get(0);

            TrInfo info = new TrInfo();
            obj.children.add(info);

            info.type = tr.getType();
            info.name = tr.getAccId();
            info.source = tr.getAccId().startsWith("ENS") ? "ENSEMBL" : "NCBI";
            info.strand = trmd.getStrand().equals("-") ? -1 : +1;
            info.id = "RGD:"+tr.getRgdId();
            info.seqId = trmd.getChromosome();
            info.fmin = trmd.getStartPos();
            info.fmax = trmd.getStopPos();

            List<TrFeature> trFeatures = new ArrayList<>();
            info.children = trFeatures;

            for (MapData trMd : tr.getGenomicPositions()) {
                // skip positions from other assemblies
                if( !(trMd.getMapKey()==MAP_KEY) ) {
                    continue;
                }

                if( !positionsOverlap(trMd, md))
                    continue;

                List<CodingFeature> cfList = utils.buildCfList(trMd);

                for (CodingFeature cf : cfList) {

                    TrFeature tf = new TrFeature();
                    tf.type = cf.getCanonicalName();
                    if( tf.type.equals("CDS") ) {
                        tf.phase = cf.getCodingPhase();
                    }
                    tf.seqId = cf.getChromosome();
                    tf.fmin = cf.getStartPos();
                    tf.fmax = cf.getStopPos();
                    tf.strand = cf.getStrand().equals("-")?-1:1;
                    tf.source = info.source;

                    trFeatures.add(tf);
                }

            }

        }

        return obj;
    }

    boolean positionsOverlap(MapData md1, MapData md2) {
        // chromosomes must match
        if( !Utils.stringsAreEqualIgnoreCase(md1.getChromosome(), md2.getChromosome()) )
            return false;
        // positions must overlap
        if( md1.getStopPos() < md2.getStartPos() )
            return false;
        if( md2.getStopPos() < md1.getStartPos() )
            return false;
        return true;
    }


    class JsonObj {

        public String sourceUrl = "http://www.alliancegenome.org/apollo/track/human/All Genes/19/PPP1R12C.json";
        public int strand;
        public String type; // "gene";
        public String name; // "PPP1R12C";
        public String source = "RGD";
        public String id; // = "RGD:1343542";
        public int fmin; // = 55090913;
        public int fmax; // = 55117637;
        public String seqId; // = "19";

        public List<TrInfo> children = new ArrayList<>();

        /*
        void dump( String fileName )  {

            ObjectMapper json = new ObjectMapper();
            // do not export fields with NULL values
            json.setSerializationInclusion(JsonInclude.Include.NON_NULL);



            // dump records to a file in JSON format
            try {
                String jsonFileName = fileName;
                BufferedWriter jsonWriter = Utils.openWriter(jsonFileName);

                jsonWriter.write(json.writerWithDefaultPrettyPrinter().writeValueAsString(this));

                jsonWriter.close();
            } catch(IOException ignore) {
            }
        }
        */
    }

    class TrInfo {

        public int strand; // -1 or 1
        public String name; // "ENST00000591938.5";
        public String source; // "ENSEMBL";
        public String id; // "rna276827";
        public String type; // "mRNA";
        public String seqId; // "19";
        public int fmin; // 55090913;
        public int fmax; // 55094796;
        public List<TrFeature> children; // exons / utrs / cdss
    }

    class TrFeature {

        public Integer phase = null;
        public int strand = 0;
        public String source;
        public String id = null;
        public String type;
        public String seqId; // chr
        public int fmin; // start pos
        public int fmax; // stop pos
    }
}
