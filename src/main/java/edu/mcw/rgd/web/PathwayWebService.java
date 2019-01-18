package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.PathwayDAO;
import edu.mcw.rgd.datamodel.Pathway;
import edu.mcw.rgd.process.Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by mtutaj on 11/1/2016.
 */
@RestController
@Api(tags="Pathway")
@RequestMapping(value = "/pathways")
public class PathwayWebService {

    PathwayDAO pathwayDAO = new PathwayDAO();


    @RequestMapping(value="/diagrams/search/{searchString}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of pathways based on search term", tags = "Pathway")
    public List<Pathway> searchPathways(@ApiParam(value="Free text search string", required=true) @PathVariable(value = "searchString") String searchString) throws Exception{
        return pathwayDAO.searchPathways(searchString);
    }

    @RequestMapping(value="/diagramsForCategory/{category}", method= RequestMethod.GET)
    @ApiOperation(value="Return a list of pathways based on category provided", tags = "Pathway")
    public List<Pathway> getPathwaysWithDiagramsForCategory(@ApiParam(value="Pathway Category", required=true) @PathVariable(value = "category") String category) throws Exception{
        if( Utils.NVL(category, "all").equals("all") ) {
            category = null;
        }
        return pathwayDAO.getPathwaysForCategory(category);
    }
}
