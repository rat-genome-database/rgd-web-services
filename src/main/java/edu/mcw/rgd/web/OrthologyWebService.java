package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.QTLDAO;
import edu.mcw.rgd.dao.impl.SyntenyDAO;
import edu.mcw.rgd.datamodel.QTL;
import edu.mcw.rgd.datamodel.SyntenicRegion;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by jdepons on 5/31/2016.
 */
@RestController
@Tag(name="Orthology")
@RequestMapping(value = "/orthology")
public class OrthologyWebService {

    SyntenyDAO sdao = new SyntenyDAO();
    AccessLogDAO ald = new AccessLogDAO();

    /*
    @RequestMapping(value="/synteny/{chromosome}/{mapKey1}/{mapKey2}", method= RequestMethod.GET)
    @Operation(summary="Return a QTL for provided RGD ID", tags = "QTL")
    public List<SyntenicRegion> getQTLByRgdId(@Parameter(description="chromosome", required=true) @PathVariable(value = "chromosome") String chromosome,
                                              @Parameter(description="mapKey1", required=true) @PathVariable(value = "mapKey1") int mapKey1,
                                              @Parameter(description="mapKey2", required=true) @PathVariable(value = "mapKey2") int mapKey2
                             ) throws Exception{

          ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
      return sdao.get(mapKey1,chromosome,mapKey2);
    }
    */


}

