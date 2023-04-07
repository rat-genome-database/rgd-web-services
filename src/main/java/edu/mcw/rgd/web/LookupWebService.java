package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.*;
import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.Map;
import edu.mcw.rgd.datamodel.SpeciesType;
import edu.mcw.rgd.datamodel.XdbId;
import edu.mcw.rgd.domain.AnnotatedGeneRequest;
import edu.mcw.rgd.domain.RGDIDListRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jdepons on 7/27/2016.
 */
@RestController
@Api(tags="Lookup")
@RequestMapping(value = "/lookup")

public class LookupWebService {

    AccessLogDAO ald = new AccessLogDAO();

    private  HashMap<String, String> getIDMapping(Integer rgdId, int xdbId) throws Exception{
        HashMap<String, String> mapping = new HashMap<String, String>();

        XdbIdDAO xidao = new XdbIdDAO();

            List<XdbId> xids = xidao.getXdbIdsByRgdId(xdbId, rgdId);

            if (xids.size() > 0) {
                mapping.put(rgdId + "", xidao.getXdbIdsByRgdId(xdbId, rgdId).get(0).getAccId());
            }
        return mapping;
    }

    private  HashMap<String, String>  getIDMapping(RGDIDListRequest data, int xdbId) throws Exception {
        HashMap<String, String> mapping = new HashMap<String, String>();

        XdbIdDAO xidao = new XdbIdDAO();

        for (Integer id : data.rgdIds) {
            List<XdbId> xids = xidao.getXdbIdsByRgdId(xdbId, id);
            if (xids.size() > 0) {
                mapping.put(id + "", xidao.getXdbIdsByRgdId(xdbId, id).get(0).getAccId());
            }
        }
        return mapping;
    }

    //UniProt Service
    @RequestMapping(value="/id/map/UniProt", method=RequestMethod.POST)
    @ApiOperation(value="Translate RGD IDs to UniProt IDs", tags="Lookup")
    public HashMap<String, String> getUniProtMapping(HttpServletRequest request,
                                                     @RequestBody(required = false) RGDIDListRequest data

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(data,14);
    }

    @RequestMapping(value="id/map/UniProt/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Translate an RGD ID to a UniProt ID", tags="Lookup")
    public HashMap<String, String> getUniProtMapping(HttpServletRequest request,
            @ApiParam(value="RGD ID", required=false)
            @PathVariable(value = "rgdId") Integer rgdId

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(rgdId, 14);
    }

    //GenBank Nucleotide Service
    @RequestMapping(value="/id/map/GenBankNucleotide", method=RequestMethod.POST)
    @ApiOperation(value="Translate RGD IDs to GenBank Nucleotide IDs", tags="Lookup")
    public HashMap<String, String> getGenBankNucleotideMapping(HttpServletRequest request,
            @RequestBody(required = false) RGDIDListRequest data

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(data,1);
    }


    @RequestMapping(value="id/map/GenBankNucleotide/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Translate an RGD ID to a GenBank Nucleotide ID", tags="Lookup")
    public HashMap<String, String> getGenBankNucleotideMapping(HttpServletRequest request,
            @ApiParam(value="RGD ID", required=false)
            @PathVariable(value = "rgdId") Integer rgdId

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(rgdId, 1);
    }

    //NCBI Gene Service
    @RequestMapping(value="/id/map/NCBIGene", method=RequestMethod.POST)
    @ApiOperation(value="Translate RGD IDs to NCBI Gene IDs", tags="Lookup")
    public HashMap<String, String> getNCBIGeneMapping(HttpServletRequest request,
            @RequestBody(required = false) RGDIDListRequest data

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(data,3);
    }


    @RequestMapping(value="id/map/NCBIGene/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Translate an RGD ID to an NCBI Gene ID", tags="Lookup")
    public HashMap<String, String> getNCBIGeneMapping(HttpServletRequest request,
            @ApiParam(value="RGD ID", required=false)
            @PathVariable(value = "rgdId") Integer rgdId

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(rgdId, 3);
    }

    //EnsemblGene Service
    @RequestMapping(value="/id/map/EnsemblGene", method=RequestMethod.POST)
    @ApiOperation(value="Translate RGD IDs to Ensembl Gene IDs", tags="Lookup")
    public HashMap<String, String> getEnsemblGeneMapping(HttpServletRequest request,
            @RequestBody(required = false) RGDIDListRequest data

    ) throws Exception{
       ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);

        return this.getIDMapping(data,20);
    }
    @RequestMapping(value="id/map/EnsemblGene/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Translate an RGD ID to an Ensembl Gene  ID", tags="Lookup")
    public HashMap<String, String> getEnsemblGeneMapping(HttpServletRequest request,
            @ApiParam(value="RGD ID", required=false)
            @PathVariable(value = "rgdId") Integer rgdId

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(rgdId, 20);
    }

    //GenBankProtein Service
    @RequestMapping(value="/id/map/GenBankProtein", method=RequestMethod.POST)
    @ApiOperation(value="Translate RGD IDs to GenBank Protein IDs", tags="Lookup")
    public HashMap<String, String> getGenBankProteinMapping(HttpServletRequest request,
            @RequestBody(required = false) RGDIDListRequest data

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(data,7);
    }


    @RequestMapping(value="id/map/GenBankProtein/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Translate an RGD ID to a GenBank Protein ID", tags="Lookup")
    public HashMap<String, String> getGenBankProteinMapping(HttpServletRequest request,
            @ApiParam(value="RGD ID", required=false)
            @PathVariable(value = "rgdId") Integer rgdId

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(rgdId, 7);
    }

    //EnsemblProtein Service
    @RequestMapping(value="/id/map/EnsemblProtein", method=RequestMethod.POST)
    @ApiOperation(value="Translate RGD IDs to Ensembl Protein IDs", tags="Lookup")
    public HashMap<String, String> getEnsemblProteinMapping(HttpServletRequest request,
            @RequestBody(required = false) RGDIDListRequest data

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(data,27);
    }


    @RequestMapping(value="id/map/EnsemblProtein/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Translate an RGD ID to an Ensembl Protein ID", tags="Lookup")
    public HashMap<String, String> getEnsemblProteinMapping(HttpServletRequest request,
            @ApiParam(value="RGD ID", required=false)
            @PathVariable(value = "rgdId") Integer rgdId

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(rgdId, 27);
    }

    //EnsemblTranscript Service
    @RequestMapping(value="/id/map/EnsemblTranscript", method=RequestMethod.POST)
    @ApiOperation(value="Translate RGD IDs to Ensembl Transcript IDs", tags="Lookup")
    public HashMap<String, String> getEnsemblTranscriptMapping(HttpServletRequest request,
            @RequestBody(required = false) RGDIDListRequest data

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(data,42);
    }


    @RequestMapping(value="id/map/EnsemblTranscript/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Translate an RGD ID to an Ensembl Transcript ID", tags="Lookup")
    public HashMap<String, String> getEnsemblTranscriptMapping(HttpServletRequest request,
            @ApiParam(value="RGD ID", required=false)
            @PathVariable(value = "rgdId") Integer rgdId

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(rgdId, 42);
    }

    //MGI Service
    @RequestMapping(value="/id/map/MGI", method=RequestMethod.POST)
    @ApiOperation(value="Translate RGD IDs to MGI IDs", tags="Lookup")
    public HashMap<String, String> getMGIMapping(HttpServletRequest request,
            @RequestBody(required = false) RGDIDListRequest data

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(data,5);
    }


    @RequestMapping(value="id/map/MGI/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Translate an RGD ID to an MGI ID", tags="Lookup")
    public HashMap<String, String> getMGIMapping(HttpServletRequest request,
            @ApiParam(value="RGD ID", required=false)
            @PathVariable(value = "rgdId") Integer rgdId

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(rgdId, 5);
    }

    @RequestMapping(value="/id/map/GTEx", method=RequestMethod.POST)
    @ApiOperation(value="Translate RGD IDs to GTEx IDs", tags="Lookup")
    public HashMap<String, String> getGTEXMapping(HttpServletRequest request,
            @RequestBody(required = false) RGDIDListRequest data

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(data,65);
    }


    @RequestMapping(value="id/map/GTEx/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Translate an RGD ID to an GTEx ID", tags="Lookup")
    public HashMap<String, String> getGTEXMapping(HttpServletRequest request,
            @ApiParam(value="RGD ID", required=false)
            @PathVariable(value = "rgdId") Integer rgdId

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(rgdId,65);
    }

    @RequestMapping(value="/id/map/HGNC", method=RequestMethod.POST)
    @ApiOperation(value="Translate RGD IDs to HGNC IDs", tags="Lookup")
    public HashMap<String, String> getHGNCMapping(HttpServletRequest request,
            @RequestBody(required = false) RGDIDListRequest data

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(data,21);
    }


    @RequestMapping(value="id/map/HGNC/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Translate an RGD ID to an HGNC ID", tags="Lookup")
    public HashMap<String, String> getHGNCMapping(HttpServletRequest request,
            @ApiParam(value="RGD ID", required=false)
            @PathVariable(value = "rgdId") Integer rgdId

    ) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return this.getIDMapping(rgdId,21);
    }


    @RequestMapping(value="/maps/{speciesTypeKey}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list assembly maps for a species", tags = "Lookup")
    public List<Map> getMaps(HttpServletRequest request,@ApiParam(value="RGD species type key. A full list of keys is available throught the lookup service.  1=human, 2=mouse, 3=rat,ect", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        MapDAO g = new MapDAO();
        return g.getMaps(speciesTypeKey);
    }

    @RequestMapping(value="/speciesTypeKeys", method= RequestMethod.GET)
    @ApiOperation(value="Return a Map of species type keys available in RGD", tags = "Lookup")
    public java.util.Map getSpeciesTypes(HttpServletRequest request) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<Integer> species = new ArrayList<Integer>(SpeciesType.getSpeciesTypeKeys());
        HashMap hm = new HashMap();
        for(Integer s: species) {
            if( SpeciesType.isSearchable(s.intValue()) )
                hm.put(SpeciesType.getCommonName(s), s);
        }
        return hm;
    }

    @ApiOperation(value="Returns a list of gene types avialable in RGD", tags = "Lookup")
    @RequestMapping(value="/geneTypes", method=RequestMethod.GET)
    public List<String> getGeneTypes(HttpServletRequest request) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        GeneDAO g = new GeneDAO();
        return g.getTypes();
    }

    @RequestMapping(value="/standardUnit/{accId}", method= RequestMethod.GET)
    @ApiOperation(value="Return a standardUnit for an ontology if it exists", tags = "Lookup")
    public String getMaps(HttpServletRequest request,@ApiParam(value="RGD term acc", required=true) @PathVariable(value = "accId") String accId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        PhenominerDAO pdao = new PhenominerDAO();
        return pdao.getStandardUnit(accId);
    }
}
