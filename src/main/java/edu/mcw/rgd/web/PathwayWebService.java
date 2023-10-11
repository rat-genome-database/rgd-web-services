package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.PathwayDAO;
import edu.mcw.rgd.datamodel.Pathway;
import edu.mcw.rgd.process.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by mtutaj on 11/1/2016.
 */
@RestController
@Tag(name="Pathway")
@RequestMapping(value = "/pathways")
public class PathwayWebService {

    PathwayDAO pathwayDAO = new PathwayDAO();
    AccessLogDAO ald = new AccessLogDAO();


    @RequestMapping(value="/diagrams/search/{searchString}", method= RequestMethod.GET)
    @Operation(summary="Return a list of pathways based on search term", tags = "Pathway")
    public List<Pathway> searchPathways(HttpServletRequest request, @Parameter(description="Free text search string", required=true) @PathVariable(value = "searchString") String searchString) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        return pathwayDAO.searchPathways(searchString);
    }

    @RequestMapping(value="/diagramsForCategory/{category}", method= RequestMethod.GET)
    @Operation(summary="Return a list of pathways based on category provided", tags = "Pathway")
    public List<Pathway> getPathwaysWithDiagramsForCategory(HttpServletRequest request,@Parameter(description="Pathway Category", required=true) @PathVariable(value = "category") String category) throws Exception{
        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        if( Utils.NVL(category, "all").equals("all") ) {
            category = null;
        }
        return pathwayDAO.getPathwaysForCategory(category);
    }
}
