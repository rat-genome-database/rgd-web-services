package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.AnnotationDAO;
import edu.mcw.rgd.dao.impl.GeneDAO;
import edu.mcw.rgd.dao.impl.StrainDAO;
import edu.mcw.rgd.dao.spring.StringMapQuery;
import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.Strain;
import edu.mcw.rgd.datamodel.ontology.Annotation;
import edu.mcw.rgd.domain.AnnotatedGeneRequest;
import edu.mcw.rgd.domain.AnnotationRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by jdepons on 5/31/2016.
 */
@RestController
@Tag(name="Annotation")
@RequestMapping(value = "/annotations")
public class AnnotationWebService {


    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/", method=RequestMethod.POST)
    @Operation(summary="Return a list of genes annotated to an ontology term", tags="Annotation")
    public List<Annotation> getAnnotations(HttpServletRequest request,
                                           @RequestBody(required = false) AnnotationRequest data

    ) throws Exception{


        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();
        List<Annotation> annotations = adao.getAnnotations(data.termAcc, data.ids,data.speciesTypeKeys,data.evidenceCodes);

        return annotations;
    }



    @RequestMapping(value="/reference/{refRgdId}", method= RequestMethod.GET)
    @Operation(summary="Returns a list of annotations for a reference",tags = "Annotation")
    public List<Annotation> getAnnotsByRefrerence(HttpServletRequest request,@Parameter(description="Reference RGD ID", required=true) @PathVariable(value = "refRgdId") int refRgdId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationsByReference(refRgdId);

    }

    @RequestMapping(value="/{accId}/{rgdId}", method=RequestMethod.GET)
    @Operation(summary="Returns a list of annotations by RGD ID and ontology term accession ID",tags = "Annotation")
    public List<Annotation> getAnnotationsByAccIdAndRgdId(HttpServletRequest request,@Parameter(description="Ontology Term Accession ID", required=true) @PathVariable(value = "accId") String accId,
                                                          @Parameter(description="RGD ID", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotations(rgdId, accId.toUpperCase());

    }

    @RequestMapping(value="/rgdId/{rgdId}", method=RequestMethod.GET)
    @Operation(summary="Returns a list of annotations by RGD ID",tags = "Annotation")
    public List<Annotation> getAnnotationsByRgdId(HttpServletRequest request,@Parameter(description="RGD ID", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotations(rgdId);

    }

    @RequestMapping(value="/disease/{rgdId}", method=RequestMethod.GET)
    @Operation(summary="Returns a list of disease annotations with top-level disease category by RGD ID",tags = "Annotation")
    public List<Annotation> getAnnotationsWithDiseaseCategoryByRgdId(HttpServletRequest request,@Parameter(description="RGD ID", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationsWithDiseaseCategory(rgdId);

    }

    @RequestMapping(value="/accId/{rgdId}", method=RequestMethod.GET)
    @Operation(summary="Returns a list ontology term accession IDs annotated to an rgd object",tags = "Annotation")
    public List<StringMapQuery.MapPair> getTermAccIds(HttpServletRequest request,@Parameter(description="RGD ID", required=true) @PathVariable(value = "rgdId") int rgdId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationTermAccIds(rgdId);

    }

    @RequestMapping(value="/{accId}/{speciesTypeKey}/{includeChildren}", method=RequestMethod.GET)
    @Operation(summary="Returns a list annotations for an ontology term or a term and it's children",tags = "Annotation")
    public List<Annotation> getAnnotations(HttpServletRequest request,@Parameter(description="Ontology term accession ID", required=true) @PathVariable(value = "accId") String accId,
                                           @Parameter(description="A list of species type keys can be found using the lookup service", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                           @Parameter(description="true: return annotations for the term and children, false: return annotations for the term only ", required=true) @PathVariable(value = "includeChildren") boolean includeChildren) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotations(accId.toUpperCase(),includeChildren,speciesTypeKey);

    }

    @RequestMapping(value="/count/{accId}/{speciesTypeKey}/{includeChildren}", method=RequestMethod.GET)
    @Operation(summary="Returns annotation count for ontology accession ID and speicies",tags = "Annotation")
    public Integer getAnnotationCountByAccIdAndSpecies(HttpServletRequest request,@Parameter(description="Ontology term accession ID", required=true) @PathVariable(value = "accId") String accId,
                                                       @Parameter(description="A list of species type keys can be found using the lookup service", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                       @Parameter(description="true: return annotations for the term and children, false: return annotations for the term only ", required=true) @PathVariable(value = "includeChildren") boolean includeChildren) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationCount(accId.toUpperCase(),includeChildren,speciesTypeKey);

    }

    @RequestMapping(value="/count/{accId}/{includeChildren}", method=RequestMethod.GET)
    @Operation(summary="Returns annotation count for ontology accession ID",tags = "Annotation")
    public Integer getAnnotationCountByAccId(HttpServletRequest request,@Parameter(description="Ontology term accession ID", required=true) @PathVariable(value = "accId") String accId,
                                             @Parameter(description="true: return annotations for the term and children, false: return annotations for the term only ", required=true) @PathVariable(value = "includeChildren") boolean includeChildren) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationCount(accId.toUpperCase(),includeChildren);

    }

    @RequestMapping(value="/count/{accId}/{speciesTypeKey}/{includeChildren}/{objectType}", method=RequestMethod.GET)
    @Operation(summary="Returns annotation count for ontology accession ID and object type",tags = "Annotation")
    public Integer getAnnotationCountByAccIdAndObjectType(HttpServletRequest request,@Parameter(description="Ontology term accession ID", required=true) @PathVariable(value = "accId") String accId,
                                                          @Parameter(description="A list of species type keys can be found using the lookup service", required=true) @PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                          @Parameter(description="true: return annotations for the term and children, false: return annotations for the term only ", required=true) @PathVariable(value = "includeChildren") boolean includeChildren,
                                                          @Parameter(description="A list of object types can be found using the lookup service", required=true) @PathVariable(value = "objectType") int objectType) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();
        return adao.getAnnotationCount(accId.toUpperCase(),includeChildren,speciesTypeKey,objectType);

    }

    @RequestMapping(value="/rgdId/{rgdId}/{ontologyPrefix}", method=RequestMethod.GET)
    @Operation(summary="Returns a list of annotations by RGD ID and ontology prefix",tags = "Annotation")
    public List<Annotation> getAnnotationsByRgdIdAndOntology(HttpServletRequest request,@Parameter(description="RGD ID", required=true) @PathVariable(value = "rgdId") int rgdId,
                                                             @Parameter(description="Ontology Prefix.  The prefix can be found left of the colon in an ontology term accession ID.  As an example, term accession PW:0000034 has the ontology prefix PW", required=true) @PathVariable(value = "ontologyPrefix") String prefix) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        AnnotationDAO adao = new AnnotationDAO();

        return adao.getAnnotationsForOntology(rgdId,prefix.toUpperCase());

    }


}
