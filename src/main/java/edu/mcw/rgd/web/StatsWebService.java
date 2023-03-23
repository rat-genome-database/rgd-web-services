package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.AccessLogDAO;
import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.datamodel.ontologyx.TermWithStats;
import edu.mcw.rgd.stats.ScoreBoardManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mtutaj on 10/4/2016.
 */
@RestController
@Api(tags="Statistics")
@RequestMapping(value = "/stats")
public class StatsWebService {

    ScoreBoardManager sbm = new ScoreBoardManager();
    static SimpleDateFormat sdt = new SimpleDateFormat("yyyyMMdd");
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/count/objectStatus/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of objects with given status, for specified species and date",tags="Statistics")
    public Map<String,String> getObjectStatusCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                               @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getRGDObjectCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/objectStatus/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of objects with given status, for specified species and date range",tags="Statistics")
    public Map<String,String> getObjectStatusDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                              @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                              @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffRGDObjectCount(speciesTypeKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/activeObject/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of active objects by type, for specified species and date",tags="Statistics")
    public Map<String,String> getActiveObjectCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                   @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getActiveCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/activeObject/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of active objects, by type, for specified species and date range",tags="Statistics")
    public Map<String,String> getActiveObjectDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                  @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                                  @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffActiveCount(speciesTypeKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/withdrawnObject/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of withdrawn objects by type, for specified species and date",tags="Statistics")
    public Map<String,String> getWithdrawnObjectCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                   @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getWithdrawnCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/withdrawnObject/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of withdrawn objects, by type, for specified species and date range",tags="Statistics")
    public Map<String,String> getWithdrawnObjectDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                  @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                                  @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffWithdrawnCount(speciesTypeKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/retiredObject/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of retired objects by type, for specified species and date",tags="Statistics")
    public Map<String,String> getRetiredObjectCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                    @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getRetiredCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/retiredObject/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of retired objects, by type, for specified species and date range",tags="Statistics")
    public Map<String,String> getRetiredObjectDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                   @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                                   @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffRetiredCount(speciesTypeKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/proteinInteraction/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of protein interactions, for specified species and date",tags="Statistics")
    public Map<String,String> getProteinInteractionCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                    @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getProteinInteractionCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/proteinInteraction/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of protein interactions, for specified species and date range",tags="Statistics")
    public Map<String,String> getProteinInteractionDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                   @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                                   @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffProteinInteractionCount(speciesTypeKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/geneType/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of gene types, for specified species and date",tags="Statistics")
    public Map<String,String> getGeneTypeCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                               @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getGeneTypeCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/geneType/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of gene types, for specified species and date range",tags="Statistics")
    public Map<String,String> getGeneTypeDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                               @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                               @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffGeneTypeCount(speciesTypeKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/strainType/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of strain types, for specified species and date",tags="Statistics")
    public Map<String,String> getStrainTypeCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                               @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getStrainTypeCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/strainType/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of strain types, for specified species and date range",tags="Statistics")
    public Map<String,String> getStrainTypeDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                              @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                              @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffStrainTypeCount(speciesTypeKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/qtlInheritanceType/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of strains, by qtl inheritance type, for specified species and date",tags="Statistics")
    public Map<String,String> getQtlInheritanceTypeCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                 @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getQTLInheritanceTypeCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/qtlInheritanceType/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of strains, by qtl inheritance type, for specified species and date range",tags="Statistics")
    public Map<String,String> getQtlInheritanceTypeDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                                @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffQTLInheritanceTypeCount(speciesTypeKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/objectWithReference/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of objects with reference, by object type, for specified species and date",tags="Statistics")
    public Map<String,String> getObjectsWithReferenceCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                         @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getObjectReferenceCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/objectWithReference/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of objects with reference, by object type, for specified species and date range",tags="Statistics")
    public Map<String,String> getObjectsWithReferenceDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                        @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                                        @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffObjectReferenceCount(speciesTypeKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/objectWithRefSeq/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of objects with reference sequence(s), by object type, for specified species and date",tags="Statistics")
    public Map<String,String> getObjectsWithRefSeqCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                        @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getObjectWithReferenceSequenceCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/objectWithRefSeq/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of objects with reference sequence(s), by object type, for specified species and date range",tags="Statistics")
    public Map<String,String> getObjectsWithRefSeqDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                                       @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                                       @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffObjectWithReferenceSequenceCount(speciesTypeKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/objectWithXdb/{speciesTypeKey}/{objectKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of objects with external database ids, by database id, for specified species, object type and date",tags="Statistics")
    public Map<String,String> getObjectsWithXDBsCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                               @PathVariable(value = "objectKey") int objectKey,
                                               @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getObjectsWithXDBsCount(speciesTypeKey, objectKey, date);
    }

    @RequestMapping(value="/diff/objectWithXdb/{speciesTypeKey}/{objectKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of objects with external database ids, by database id, for specified species, object type and date range",tags="Statistics")
    public Map<String,String> getObjectsWithXDBsDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                              @PathVariable(value = "objectKey") int objectKey,
                                              @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                              @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffObjectsWithXDBsCount(speciesTypeKey, objectKey, dateFrom, dateTo);
    }


    @RequestMapping(value="/count/xdb/{speciesTypeKey}/{dateYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count of external database ids, for specied species and date",tags="Statistics")
    public Map<String,String> getXdbsCount(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                          @PathVariable(value = "dateYYYYMMDD") String dateStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date date = sdt.parse(dateStr);
        return sbm.getXDBsCount(speciesTypeKey, date);
    }

    @RequestMapping(value="/diff/xdb/{speciesTypeKey}/{dateFromYYYYMMDD}/{dateToYYYYMMDD}", method=RequestMethod.GET)
    @ApiOperation(value="Count difference of external database ids, for specified species and date range",tags="Statistics")
    public Map<String,String> getXdbsDiff(@PathVariable(value = "speciesTypeKey") int speciesTypeKey,
                                          @PathVariable(value = "dateFromYYYYMMDD") String dateFromStr,
                                          @PathVariable(value = "dateToYYYYMMDD") String dateToStr) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        Date dateFrom = sdt.parse(dateFromStr);
        Date dateTo = sdt.parse(dateToStr);
        return sbm.diffXDBsCount(speciesTypeKey, dateFrom, dateTo);
    }

    @RequestMapping(value="/term/{accId}/{filterAccId}", method=RequestMethod.GET)
    @ApiOperation(value="",tags="Statistics")
    public Map<String, Integer> getTermStats(@PathVariable(value = "accId") String accId,
                                          @PathVariable(value = "filterAccId") String filterAccId) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName());
        //Map<String, Integer>
        OntologyXDAO xdao = new OntologyXDAO();
        TermWithStats tws = xdao.getTermWithStats(accId,null,filterAccId);

        HashMap<String, Integer> hm = new HashMap();

        Map<String,Integer> stats = tws.getStats();

        for (String key: stats.keySet()) {

            String[] keyArray = key.split("\\|");

            String newKey= "";
            for (int i=0; i<keyArray.length -1; i++) {
                if (i==0) {
                    newKey += keyArray[i];
                }else {
                    newKey += "|" + keyArray[i];
                }

            }

            hm.put(newKey,stats.get(key));

        }

        for (int i=1; i<15;i++) {
            if (!hm.containsKey("annotated_object_count|" + i + "|1|1")) {
                hm.put("annotated_object_count|" + i + "|1|1",0);
            }
            if (!hm.containsKey("annotated_object_count|" + i + "|5|1")) {
                hm.put("annotated_object_count|" + i + "|5|1",0);

            }
            if (!hm.containsKey("annotated_object_count|" + i + "|6|1")) {
                hm.put("annotated_object_count|" + i + "|6|1",0);

            }

        }



        return hm;
    }



}
