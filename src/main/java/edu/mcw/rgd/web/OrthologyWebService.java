package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.QTLDAO;
import edu.mcw.rgd.dao.impl.SyntenyDAO;
import edu.mcw.rgd.datamodel.QTL;
import edu.mcw.rgd.datamodel.SyntenicRegion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by jdepons on 5/31/2016.
 */
@RestController
@Api(tags="Orthology")
@RequestMapping(value = "/orthology")
public class OrthologyWebService {

    SyntenyDAO sdao = new SyntenyDAO();

    /*
    @RequestMapping(value="/synteny/{chromosome}/{mapKey1}/{mapKey2}", method= RequestMethod.GET)
    @ApiOperation(value="Return a QTL for provided RGD ID", tags = "QTL")
    public List<SyntenicRegion> getQTLByRgdId(@ApiParam(value="chromosome", required=true) @PathVariable(value = "chromosome") String chromosome,
                                              @ApiParam(value="mapKey1", required=true) @PathVariable(value = "mapKey1") int mapKey1,
                                              @ApiParam(value="mapKey2", required=true) @PathVariable(value = "mapKey2") int mapKey2
                             ) throws Exception{

        return sdao.get(mapKey1,chromosome,mapKey2);
    }
    */


}

