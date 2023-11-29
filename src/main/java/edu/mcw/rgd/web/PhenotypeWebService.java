package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.*;
import edu.mcw.rgd.datamodel.Pathway;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.datamodel.pheno.Condition;
import edu.mcw.rgd.datamodel.pheno.Record;
import edu.mcw.rgd.datamodel.phenominerExpectedRange.PhenominerExpectedRange;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by mtutaj on 11/1/2016.
 */
@RestController
@Tag(name="Quantitative Phenotype")
@RequestMapping(value = "/phenotype")
public class PhenotypeWebService {

    PhenominerDAO phenominerDAO = new PhenominerDAO();
    PhenominerExpectedRangeDao pedao = new PhenominerExpectedRangeDao();
    AccessLogDAO ald = new AccessLogDAO();

    @RequestMapping(value="/phenominer/chart/{speciesTypeKey}/{termString}", method= RequestMethod.GET)
    @Operation(summary="Return a list of quantitative phenotypes values based on a combination of Clinical Measurement, Experimental Condition, Rat Strain, and/or Measurement Method ontology terms.  Results will be all records that match all terms submitted.  Ontology ids should be submitted as a comma delimited list (ex. RS:0000029,CMO:0000155,CMO:0000139).  Species type is an integer value (3=rat, 4=chinchilla)", tags = "Quantitative Phenotype")
    public HashMap getChartInfo(HttpServletRequest request, @Parameter(description="Species Type Key - 3=rat 4=chinchilla ", required=true) @PathVariable(name = "speciesTypeKey") int speciesTypeKey,
                                @Parameter(description="List of term accession IDs", required=true) @PathVariable(name = "termString") String termString) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<String> sampleIds = new ArrayList<String>();
        List mmIds = new ArrayList<String>();
        List cmIds = new ArrayList<String>();
        List ecIds = new ArrayList<String>();

        String[] terms = termString.split(",");

        for (int j=0; j< terms.length; j++) {
            String[] termParts = terms[j].split(":");

            while (termParts[1].length()<7) {
                termParts[1]="0" + termParts[1];
            }

            terms[j] = termParts[0] + ":" + termParts[1];
        }

        for (int i=0; i< terms.length; i++) {
            if (terms[i].startsWith("RS") || terms[i].startsWith("CS")) {
                sampleIds.add(terms[i]);
            }else if (terms[i].startsWith("CMO")) {
                cmIds.add(terms[i]);
            }else if (terms[i].startsWith("MMO")) {
                mmIds.add(terms[i]);
            }else if (terms[i].startsWith("XCO")) {
                ecIds.add(terms[i]);
            }
        }

        List<Record> records = phenominerDAO.getFullRecords(sampleIds, mmIds, cmIds, ecIds, speciesTypeKey);

        return getChartInfoForRecords(records);
    }

    @RequestMapping(value="/phenominer/chart/{speciesTypeKey}/{refRgdId}/{termString}", method= RequestMethod.GET)
    @Operation(summary="Return a list of quantitative phenotypes values based on a combination of Clinical Measurement, Experimental Condition, Rat Strain, and/or Measurement Method ontology terms.  Results will be all records that match all terms submitted.  Ontology ids should be submitted as a comma delimited list (ex. RS:0000029,CMO:0000155,CMO:0000139).  Species type is an integer value (3=rat, 4=chinchilla).  Reference RGD ID for a study works like a filter.", tags = "Quantitative Phenotype")
    public HashMap getChartInfo(HttpServletRequest request,@Parameter(description="Species Type Key - 3=rat 4=chinchilla ", required=true) @PathVariable(name = "speciesTypeKey") int speciesTypeKey,
			@Parameter(description="Reference RGD ID for a study", required=true) @PathVariable(name = "refRgdId") int refRgdId,
            @Parameter(description="List of term accession IDs", required=true) @PathVariable(name = "termString") String termString) throws Exception{

        ald.log("RESTAPI", this.getClass().getName() + ":" + new Throwable().getStackTrace()[0].getMethodName(),request);
        List<String> sampleIds = new ArrayList<String>();
        List mmIds = new ArrayList<String>();
        List cmIds = new ArrayList<String>();
        List ecIds = new ArrayList<String>();

        String[] terms = termString.split(",");

        for (int j=0; j< terms.length; j++) {
            String[] termParts = terms[j].split(":");

            while (termParts[1].length()<7) {
                termParts[1]="0" + termParts[1];
            }

            terms[j] = termParts[0] + ":" + termParts[1];
        }

        for (int i=0; i< terms.length; i++) {
            if (terms[i].startsWith("RS") || terms[i].startsWith("CS")) {
                sampleIds.add(terms[i]);
            }else if (terms[i].startsWith("CMO")) {
                cmIds.add(terms[i]);
            }else if (terms[i].startsWith("MMO")) {
                mmIds.add(terms[i]);
            }else if (terms[i].startsWith("XCO")) {
                ecIds.add(terms[i]);
            }
        }

        List<Record> records = phenominerDAO.getFullRecords(sampleIds, mmIds, cmIds, ecIds, speciesTypeKey, refRgdId);

        return getChartInfoForRecords(records);
    }

    HashMap getChartInfoForRecords(List<Record> records) throws Exception {

        double min = 1000000000;
        double max = -1000000000;

        List ageRanges= new ArrayList();
        HashMap uniqueRanges=new HashMap();

        HashMap<String, Term> termResolver = new HashMap<String, Term>();
        List<String> termList = new ArrayList<String>();
        HashMap<String,String> measurements = new HashMap<String,String>();
        HashMap<String,String> methods = new HashMap<String,String>();
        HashMap<String,String> samples = new HashMap<String,String>();
        HashMap<String,String> conditions =  new HashMap<String,String>();

        for (Record r: records) {

            termList.add(r.getSample().getStrainAccId());
            samples.put(r.getSample().getStrainAccId(), null);
            termList.add(r.getClinicalMeasurement().getAccId());
            measurements.put(r.getClinicalMeasurement().getAccId(), null);
            termList.add(r.getMeasurementMethod().getAccId());
            methods.put(r.getMeasurementMethod().getAccId(), null);

            for (Condition c : r.getConditions()) {
                termList.add(c.getOntologyId());
                conditions.put(c.getOntologyId(), null);
            }

            double thisVal = Double.parseDouble(r.getMeasurementValue());

            if (thisVal < min) {
                min = thisVal;
            }

            if (thisVal > max) {
                max = thisVal;
            }

            if (r.getSample().getAgeDaysFromLowBound() != null) {

                String range = r.getSample().getAgeDaysFromLowBound() + " days - " + r.getSample().getAgeDaysFromHighBound() + " days";
                if (!uniqueRanges.containsKey(range)) {
                    HashMap thisRange = new HashMap();
                    thisRange.put("range", range);
                    uniqueRanges.put(range, null);

                    ageRanges.add(thisRange);
                }
            }
        }

        String[] termIds = new String[termList.size()];
        termIds = termList.toArray(termIds);

        OntologyXDAO xdao = new OntologyXDAO();
        List<Term> ontTerms = xdao.getTermByAccId(termIds);

        for (Term term: ontTerms) {
            termResolver.put(term.getAccId(),term);
        }


        LinkedHashMap conditionSet = new LinkedHashMap();

        for (Record r: records) {

            if (!conditionSet.containsKey(r.getClinicalMeasurement().getAccId())) {
                HashSet hm = new HashSet();
                hm.add(r.getConditionDescription());
                conditionSet.put(r.getClinicalMeasurement().getAccId(),hm);
            }else {
                HashSet hm = (HashSet) conditionSet.get(r.getClinicalMeasurement().getAccId());
                hm.add(r.getConditionDescription());
                conditionSet.put(r.getClinicalMeasurement().getAccId(),hm);

            }

        }
        HashMap hm = new HashMap();
        List measurementList = new ArrayList();
        List methodList = new ArrayList();
        List sampleList = new ArrayList();

        for (String sample: samples.keySet()) {

            HashMap map = new HashMap();
            map.put("accId", sample);
            map.put("term", termResolver.get(sample).getTerm());

            sampleList.add(map);
        }
        hm.put("samples", sampleList);

        for (String measurement: measurements.keySet()) {
            HashMap map = new HashMap();
            List<PhenominerExpectedRange> normalRanges = pedao.getNormalRangesByCMId(measurement);
            map.put("accId", measurement);
            map.put("term", termResolver.get(measurement).getTerm());

            HashMap ranges = new HashMap();
            for(PhenominerExpectedRange range: normalRanges){
                HashMap rmap = new HashMap();
                rmap.put("mean",range.getRangeValue());
                rmap.put("low",range.getRangeLow());
                rmap.put("high",range.getRangeHigh());
                rmap.put("sd",range.getRangeSD());
                ranges.put(range.getSex(),rmap);
            }
            map.put("normalRanges",ranges);
            measurementList.add(map);
        }
        hm.put("measurements", measurementList);

        for (String method: methods.keySet()) {
            HashMap map = new HashMap();
            map.put("accId", method);
            map.put("term", termResolver.get(method).getTerm());

            methodList.add(map);
        }
        hm.put("methods", methodList);


        hm.put("records", records);

        hm.put("termResolver",termResolver);

        hm.put("ageRanges",ageRanges);


        HashMap minMax = new HashMap();
        minMax.put("min", min);
        minMax.put("max", max);

        hm.put("valueRange", minMax);
        hm.put("conditionSet",conditionSet);

        return hm;
    }

    public double round(double value, int numberOfDigitsAfterDecimalPoint) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(numberOfDigitsAfterDecimalPoint,
                BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }

    public String round(String value, int numberOfDigitsAfterDecimalPoint) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(numberOfDigitsAfterDecimalPoint,
                BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue() + "";
    }

}
