package edu.mcw.rgd.web;


import edu.mcw.rgd.dao.impl.SSLPDAO;
import edu.mcw.rgd.datamodel.MappedSSLP;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Api(tags="SSLP")
@RequestMapping(value = "/sslps")
public class SSLPWebService {

    SSLPDAO sdao = new SSLPDAO();

    @RequestMapping(value = "/mapped/{chr}/{start}/{stop}/{mapKey}", method = RequestMethod.GET)
    @ApiOperation(value = "Returns a list SSLP for given position and assembly map", tags = "SSLP")
    public List<MappedSSLP> getMappedSSLPByPosition(@ApiParam(value = "Chromosome", required = true) @PathVariable(value = "chr") String chr,
                                                    @ApiParam(value = "Start Position", required = true) @PathVariable(value = "start") long start,
                                                    @ApiParam(value = "Stop Position", required = true) @PathVariable(value = "stop") long stop,
                                                    @ApiParam(value = "A list of assembly map keys can be found using the lookup service", required = true) @PathVariable(value = "mapKey") int mapKey) throws Exception {

        return sdao.getActiveMappedSSLPs(chr.toUpperCase(), start, stop, mapKey);
    }
}