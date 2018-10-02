/*
Copyright 2018 Wendy Chapman (wendy.chapman\@utah.edu) & Lee Christensen (leenlp\@q.com)

Licensed under the Apache License, Version 2.0 (the \"License\");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an \"AS IS\" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.ed.wew.api;

import java.io.*;
import java.util.*;

/**
 * 
 * An instance of this object contains the entire result of the evaluation. Please read descriptions of individual Maps for details.
 * 
 * NOTE: This data structure and ALL the objects it contains MUST be truly Serializable. Among other things, this implies that all the storeable fields in all
 * POJOs have to have getters and setters named according to Java conventions and all fields that are transient by nature have to be marked as such.
 * 
 * @author Andrey Santrosyan
 * 
 */
public class ResultTable implements Serializable {

    /**
     * This Map corresponds to the first part of the resulting table. +-------+--+--+--+--+ |Concept|TP|FP|TN|FN| +-------+--+--+--+--+ |Concep1|<>|<>| *|<>|
     * +-------+--+--+--+--+ |Concep2|<>|<>|<>|<>| +-------+--+--+--+--+ ..... +-------+--+--+--+--+
     * 
     * Keys of this Map are instances of ConceptReference. Values (inner maps) are Maps with up to 4 pairs (one for each TP,FP,TN,FN) Keys of the inner Map are
     * instances of OutcomeType (TP,FP,TN,FN) Values of the inner Map are Lists of Annotations.
     * 
     */
    private Map<ConceptReference, Map<OutcomeType, List<Annotation>>> results = new LinkedHashMap();

    private Map<ConceptReference, Map<CalculationType, Double>> statisticalSet = new LinkedHashMap();

    /**
     * Similar to results Map, but only contains precalculated totals.
     */
    private Map<OutcomeType, List<Annotation>> summary = new LinkedHashMap();

    private Map<CalculationType, Double> statisticalSummary = new LinkedHashMap();

    /**
     * This method adds the annotation to the corresponding list and updates summary map as well
     * 
     */
    protected void addAnnotation(final OutcomeType outcomeType, final Annotation annotation) {
        // TODO: Implement this method.

        // Lee
        ConceptReference cr = annotation.getConceptReference();
        Map<OutcomeType, List<Annotation>> amap = results.get(cr);
        ArrayList<Annotation> l = null;
        if (amap == null) {
            amap = new HashMap();
            results.put(cr, amap);
        }
        l = (ArrayList)amap.get(outcomeType);
        if (l == null) {
            l = new ArrayList();
            amap.put(outcomeType, l);
        }
        l.add(annotation);
    }

    /**
     * This method sets the calculated values to the corresponding concept and updates statisticalSummary map as well
     * 
     */
    protected void setStatisticalSet(final ConceptReference conceptReference, final Map<CalculationType, Double> stats) {
        // TODO: Implement this method.
    }

    // TODO:Add methods for accessing the results.
    public Map<ConceptReference, Map<OutcomeType, List<Annotation>>> getResults() {
        return results;
    }

    public Map<ConceptReference, Map<CalculationType, Double>> getStatisticsalSet() {
        return statisticalSet;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Results: \n");
        for (ConceptReference cr : results.keySet()) {
            sb.append("\tConcept = " + cr + "\n\t\tMap of Outcome Annotations = " + results.get(cr) + "\n");
        }
        sb.append("\nStatistical Set: \n");
        for (ConceptReference cr : statisticalSet.keySet()) {
            sb.append("\tConcept = " + cr + "\n\t\tMap of Outcome Annotations = " + statisticalSet.get(cr) + "\n");
        }
        sb.append("\nSummary: \n");
        for (OutcomeType ot : summary.keySet()) {
            sb.append("\t" + ot + " " + summary.get(ot) + "\n");
        }
        sb.append("\nStatistical Summarys: \n");
        for (CalculationType ct : statisticalSummary.keySet()) {
            sb.append("\t" + ct + " " + statisticalSummary.get(ct) + "\n");
        }

        return sb.toString();
    }

    public Map<OutcomeType, List<Annotation>> getSummary() {
        return summary;
    }

    public Map<CalculationType, Double> getStatisticalSummary() {
        return statisticalSummary;
    }

}
