package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.QTLDAO;
import edu.mcw.rgd.datamodel.MappedQTL;
import edu.mcw.rgd.datamodel.QTL;

import io.swagger.annotations.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jdepons on 5/31/2016.
 */
@RestController
@Api(tags="QTL")
@RequestMapping(value = "/qtls")
public class QTLWebService {

    QTLDAO qdao = new QTLDAO();

    @RequestMapping(value="/{rgdId}", method= RequestMethod.GET)
    @ApiOperation(value="Return a QTL for provided RGD ID", tags = "QTL")
    public QTL getQTLByRgdId(@ApiParam(value="RGD ID", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{

        return qdao.getQTL(rgdId);
    }


    @RequestMapping( value="/{chr}/{start}/{stop}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Returns a list QTL for given position and assembly map", tags = "QTL")
    public List<QTL> getQtlListByPosition(@ApiParam(value="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                          @ApiParam(value="Start Position", required=true) @PathVariable(value = "start") long start,
                                          @ApiParam(value="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                          @ApiParam(value="A list of assembly map keys can be found using the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        return qdao.getActiveQTLs(chr.toUpperCase(), start,stop,mapKey);
    }
    @RequestMapping( value="/mapped/{chr}/{start}/{stop}/{mapKey}", method= RequestMethod.GET)
    @ApiOperation(value="Returns a list QTL for given position and assembly map", tags = "QTL")
    public List<MappedQTL> getMappedQTLByPosition(@ApiParam(value="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                                @ApiParam(value="Start Position", required=true) @PathVariable(value = "start") long start,
                                                @ApiParam(value="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                                @ApiParam(value="A list of assembly map keys can be found using the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        return qdao.getActiveMappedQTLs(chr.toUpperCase(), start,stop,mapKey);
    }
    /*
    Map<Long, NewsEntry> entries = new ConcurrentHashMap<Long, NewsEntry>();

    @RequestMapping(value = "/qtl/hey", method = RequestMethod.GET)
    @ApiOperation(value = "Get News", notes = "Returns news items")
    Collection<NewsEntry> entries() {
        return this.entries.values();
    }
*/

}


/*
    @ApiOperation(value = "Finds Pets by status", notes = "Multiple status values can be provided with comma separated strings", response = QTL.class, responseContainer = "List", authorizations = {
            @Authorization(value = "petstore_auth", scopes = {
                    @AuthorizationScope(scope = "write:pets", description = "modify pets in your account"),
                    @AuthorizationScope(scope = "read:pets", description = "read your pets")
            })
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = QTL.class),
            @ApiResponse(code = 400, message = "Invalid status value", response = QTL.class) })
    @RequestMapping(value = "/pet/findByStatus",
            produces = "application/json",
            consumes = "application/json",
            method = RequestMethod.GET)
    ResponseEntity<List<QTL>> findPetsByStatus(@ApiParam(value = "Status values that need to be considered for filter", required = true) @RequestParam(value = "status", required = true) List<String> status) throws Exception{

        ArrayList al = new ArrayList();
        QTL q = new QTL();
        q.setSymbol("hey");
        al.add((q));
        HttpHeaders responseHeaders = new HttpHeaders();
        URI location = new URI("laksjdf");
        responseHeaders.setLocation(location);
        responseHeaders.set("MyResponseHeader", "MyValue");
        return new ResponseEntity<List<QTL>>(al, responseHeaders, HttpStatus.CREATED);


    }
*/