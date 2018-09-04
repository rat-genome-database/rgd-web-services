package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AnnotationDAO;
import edu.mcw.rgd.dao.impl.StrainDAO;
import edu.mcw.rgd.dao.spring.StringMapQuery;
import edu.mcw.rgd.datamodel.Strain;
import edu.mcw.rgd.datamodel.ontology.Annotation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by jdepons on 5/31/2016.
 */
@RestController
@RequestMapping(value = "/annotations")
public class AnnotationWebService {

    @RequestMapping(value="/reference/{refRgdId}", method= RequestMethod.GET)
    @ApiOperation(value="Returns a list of annotations for a reference",tags = "Annotation")
    public List<Annotation> getAnnotsByRefrerence(@ApiParam(value="Reference RGD ID", required=true) @PathVariable(value = "refRgdId") int refRgdId) throws Exception{

        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationsByReference(refRgdId);

    }

    @RequestMapping(value="/{accId}/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Returns a list of annotations by RGD ID and ontology term accession ID",tags = "Annotation")
    public List<Annotation> getAnnotationsByAccIdAndRgdId(@ApiParam(value="Ontology Term Accession ID", required=true) @PathVariable(value = "accId") String accId,
                                                          @ApiParam(value="RGD ID", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{

        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotations(rgdId, accId.toUpperCase());

    }

    @RequestMapping(value="/rgdId/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Returns a list of annotations by RGD ID",tags = "Annotation")
    public List<Annotation> getAnnotationsByRgdId(@ApiParam(value="RGD ID", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{

        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotations(rgdId);

    }

    @RequestMapping(value="/accId/{rgdId}", method=RequestMethod.GET)
    @ApiOperation(value="Returns a list ontology term accession IDs annotated to an rgd object",tags = "Annotation")
    public List<StringMapQuery.MapPair> getTermAccIds(@ApiParam(value="RGD ID", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{

        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationTermAccIds(rgdId);

    }

    @RequestMapping(value="/{accId}/{speciesTypeKey}/{includeChildren}", method=RequestMethod.GET)
    @ApiOperation(value="Returns a list annotations for an ontology term or a term and it's children",tags = "Annotation")
    public List<Annotation> getAnnotations(@ApiParam(value="Ontology term accession ID", required=true) @PathVariable(value = "accId") String accId,
                                           @ApiParam(value="A list of species type keys can be found using the lookup service", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                           @ApiParam(value="true: return annotations for the term and children, false: return annotations for the term only ", required=true) @PathVariable(value = "includeChildren") boolean includeChildren) throws Exception{

        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotations(accId.toUpperCase(),includeChildren,speciesTypeKey);

    }

    @RequestMapping(value="/count/{accId}/{speciesTypeKey}/{includeChildren}", method=RequestMethod.GET)
    @ApiOperation(value="Returns annotation count for ontology accession ID and speicies",tags = "Annotation")
    public Integer getAnnotationCountByAccIdAndSpecies(@ApiParam(value="Ontology term accession ID", required=true) @PathVariable(value = "accId") String accId,
                                                       @ApiParam(value="A list of species type keys can be found using the lookup service", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                       @ApiParam(value="true: return annotations for the term and children, false: return annotations for the term only ", required=true) @PathVariable(value = "includeChildren") boolean includeChildren) throws Exception{

        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationCount(accId.toUpperCase(),includeChildren,speciesTypeKey);

    }

    @RequestMapping(value="/count/{accId}/{includeChildren}", method=RequestMethod.GET)
    @ApiOperation(value="Returns annotation count for ontology accession ID",tags = "Annotation")
    public Integer getAnnotationCountByAccId(@ApiParam(value="Ontology term accession ID", required=true) @PathVariable(value = "accId") String accId,
                                             @ApiParam(value="true: return annotations for the term and children, false: return annotations for the term only ", required=true) @PathVariable(value = "includeChildren") boolean includeChildren) throws Exception{

        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationCount(accId.toUpperCase(),includeChildren);

    }

    @RequestMapping(value="/count/{accId}/{speciesTypeKey}/{includeChildren}/{objectType}", method=RequestMethod.GET)
    @ApiOperation(value="Returns annotation count for ontology accession ID and object type",tags = "Annotation")
    public Integer getAnnotationCountByAccIdAndObjectType(@ApiParam(value="Ontology term accession ID", required=true) @PathVariable(value = "accId") String accId,
                                                          @ApiParam(value="A list of species type keys can be found using the lookup service", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                          @ApiParam(value="true: return annotations for the term and children, false: return annotations for the term only ", required=true) @PathVariable(value = "includeChildren") boolean includeChildren,
                                                          @ApiParam(value="A list of object types can be found using the lookup service", required=true) @PathVariable(value = "objectType") int objectType) throws Exception{

        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationCount(accId.toUpperCase(),includeChildren,speciesTypeKey,objectType);

    }

    @RequestMapping(value="/rgdId/{rgdId}/{ontologyPrefix}", method=RequestMethod.GET)
    @ApiOperation(value="Returns a list of annotations by RGD ID and ontology prefix",tags = "Annotation")
    public List<Annotation> getAnnotationsByRgdIdAndOntology(@ApiParam(value="RGD ID", required=true) @PathVariable(value = "rgdId") int rgdId,
                                                             @ApiParam(value="Ontology Prefix.  The prefix can be found left of the semicolon in an ontology term accession ID.  As an example, term accession PW:0000034 has the ontology prefix PW", required=true) @PathVariable(value = "ontologyPrefix") String prefix) throws Exception{

        AnnotationDAO adao = new AnnotationDAO();

        return adao.getAnnotationsForOntology(rgdId,prefix.toUpperCase());

    }


}
