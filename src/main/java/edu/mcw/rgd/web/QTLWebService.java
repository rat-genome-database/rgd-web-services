package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.QTLDAO;
import edu.mcw.rgd.datamodel.MappedQTL;
import edu.mcw.rgd.datamodel.QTL;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@Tag(name="QTL")
@RequestMapping(value = "/qtls")
public class QTLWebService {

    QTLDAO qdao = new QTLDAO();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/{rgdId}", method= RequestMethod.GET)
    @Operation(summary="Return a QTL for provided RGD ID", tags = "QTL")
    public QTL getQTLByRgdId(HttpServletRequest request, @Parameter(description="RGD ID", required=true) @PathVariable(name = "rgdId") int rgdId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return qdao.getQTL(rgdId);
    }


    @RequestMapping( value="/{chr}/{start}/{stop}/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Returns a list QTL for given position and assembly map", tags = "QTL")
    public List<QTL> getQtlListByPosition(HttpServletRequest request,@Parameter(description="Chromosome", required=true) @PathVariable(name = "chr") String chr,
                                          @Parameter(description="Start Position", required=true) @PathVariable(name = "start") long start,
                                          @Parameter(description="Stop Position", required=true) @PathVariable(name = "stop") long stop,
                                          @Parameter(description="A list of assembly map keys can be found using the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return qdao.getActiveQTLs(chr.toUpperCase(), start,stop,mapKey);
    }
    @RequestMapping( value="/mapped/{chr}/{start}/{stop}/{mapKey}", method= RequestMethod.GET)
    @Operation(summary="Returns a list QTL for given position and assembly map", tags = "QTL")
    public List<MappedQTL> getMappedQTLByPosition(HttpServletRequest request,@Parameter(description="Chromosome", required=true) @PathVariable(value = "chr") String chr,
                                                @Parameter(description="Start Position", required=true) @PathVariable(value = "start") long start,
                                                @Parameter(description="Stop Position", required=true) @PathVariable(value = "stop") long stop,
                                                @Parameter(description="A list of assembly map keys can be found using the lookup service", required=true) @PathVariable(value = "mapKey") int mapKey) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return qdao.getActiveMappedQTLs(chr.toUpperCase(), start,stop,mapKey);
    }
    /*
    Map<Long, NewsEntry> entries = new ConcurrentHashMap<Long, NewsEntry>();

    @RequestMapping(value = "/qtl/hey", method = RequestMethod.GET)
    @Operation(summary = "Get News", notes = "Returns news items")
    Collection<NewsEntry> entries() {
        return this.entries.values();
    }
*/

}


/*
    @Operation(summary = "Finds Pets by status", notes = "Multiple status values can be provided with comma separated strings", response = QTL.class, responseContainer = "List", authorizations = {
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
    ResponseEntity<List<QTL>> findPetsByStatus(@Parameter(description = "Status values that need to be considered for filter", required = true) @RequestParam(value = "status", required = true) List<String> status) throws Exception{

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