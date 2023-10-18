package edu.mcw.rgd.web;


import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.SSLPDAO;
import edu.mcw.rgd.datamodel.MappedSSLP;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Tag(name="SSLP")
@RequestMapping(value = "/sslps")
public class SSLPWebService {

    SSLPDAO sdao = new SSLPDAO();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value = "/mapped/{chr}/{start}/{stop}/{mapKey}", method = RequestMethod.GET)
    @Operation(summary = "Returns a list SSLP for given position and assembly map", tags = "SSLP")
    public List<MappedSSLP> getMappedSSLPByPosition(HttpServletRequest request, @Parameter(description = "Chromosome", required = true) @PathVariable(value = "chr") String chr,
                                                    @Parameter(description = "Start Position", required = true) @PathVariable(value = "start") long start,
                                                    @Parameter(description = "Stop Position", required = true) @PathVariable(value = "stop") long stop,
                                                    @Parameter(description = "A list of assembly map keys can be found using the lookup service", required = true) @PathVariable(value = "mapKey") int mapKey) throws Exception {

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return sdao.getActiveMappedSSLPs(chr.toUpperCase(), start, stop, mapKey);
    }
}