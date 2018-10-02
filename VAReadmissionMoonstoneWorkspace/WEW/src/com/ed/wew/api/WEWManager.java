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

import tsl.expression.form.sentence.constraint.Constraint;
import tsl.knowledge.engine.*;
import tsl.utilities.HUtils;
import workbench.api.*;
import workbench.api.annotation.*;
import workbench.api.constraint.ConstraintMatch;
import workbench.api.constraint.ConstraintPacket;
import workbench.api.input.knowtator.Knowtator;
import workbench.api.input.knowtator.KnowtatorIO;

public class WEWManager {

	public static final String SCHEMA_FILE = "resources/example1/knowtator/SHARPSchema.xml";
	public static final String DOCUMENTS_DIR = "resources/example1/knowtator/knowtator_corpus/";
	public static final String PRIMARY_ANNOTATION_DIR = "resources/example1/knowtator/knowtator_primary/";
	public static final String SECONDARY_ANNOTATION_DIR = "resources/example1/knowtator/knowtator_secondary/";
	public static final int NUM_EXAMPLES = 26;

	// 9/27/2014: Changed to "public" since I am moving the references to the
	// old WB
	// to an outside project.
	public static String FormatType = "FormatType";
	public static String FormatTypeGRAF = "GrAF";
	public static String FormatTypeKnowtator = "Knowtator";

	private static int NUM_CALCULATION_TYPES = 8;
	private static String ConstraintName = "AnnotationHasClassification";

	/**
	 * This is the main method that will load all the documents and calculate
	 * all the statistics.
	 * 
	 * @param schema
	 *            TODO
	 * @param documents
	 * @param primaryAnnotators
	 * @param secondaryAnnotators
	 * 
	 * @return
	 */

	public static ResultTable load(final DocumentReference schema,
			final List<DocumentReference> documents,
			final List<AnnotatorReference> primaryAnnotators,
			final List<AnnotatorReference> secondaryAnnotators,
			final Params params) throws Exception {
		KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine();
		storeParamsAsKnowledgeEngineProperties(params, ke);
//		ke.setPropertyValue(
//				"JavaPackages",
//				"workbench.api.constraint.JavaFunctions,tsl.expression.term.function.javafunction.JavaFunctions,moonstone.java.JavaFunctions,moonstone.java.JavaRelations,tsl.expression.term.function.javafunction.JavaFunctions");
//		ke.setPropertyValue(
//				"DefaultClassificationProperties",
//				"associatedCode,distal_or_proximal_normalization,negation_indicator_normalization,negation_indicator_normalization,severity_normalization,course_normalization,subject_normalization_CU");
		Analysis analysis = new Analysis();
		Hashtable<String, Integer> annotationFileNameIndexHash = new Hashtable();
		Hashtable<DocumentReference, String> documentTextHash = new Hashtable();

		KnowtatorIO kio = null;
		if (FormatTypeKnowtator.equals(params.getParams().get("format"))) {
			kio = Knowtator.createKnowtatorIO(analysis);
			WEWManager.readKnowtatorSchemaFile(schema, kio);
		}
		for (int i = 0; i < documents.size(); i++) {
			DocumentReference dr = documents.get(i);
			AnnotatorReference primary = primaryAnnotators.get(i);
			AnnotatorReference secondary = secondaryAnnotators.get(i);
			annotationFileNameIndexHash.put(dr.getName(), new Integer(i));
			annotationFileNameIndexHash.put(primary.getName(), new Integer(i));
			annotationFileNameIndexHash
					.put(secondary.getName(), new Integer(i));
			String dtext = WEWManager.readDocumentReference(dr, false);
			documentTextHash.put(dr, dtext);
			WEWManager.readAnnotationCollection(analysis, dr, primary, params,
					kio, null);
			WEWManager.readAnnotationCollection(analysis, dr, secondary,
					params, kio, null);
		}

		if (kio != null) {
			Knowtator.postProcess(kio, analysis);
		}

		analysis.postProcessAnnotationCollections();
		HashMap rmap = new HashMap();
		ResultTable rt = new ResultTable();

		if (analysis.getAllAnnotationEvents() != null) {
			for (AnnotationEvent ae : analysis.getAllAnnotationEvents()) {
				AnnotationCollection ac = ae.getPrimaryAnnotationCollection();
				if (ac.getAnnotations() != null) {
					for (workbench.api.annotation.Annotation wba : ac
							.getAnnotations()) {
						if ("DocumentClass".equals(wba.getType().getName())) {
							continue;
						}
						AnnotationImpl ai = new AnnotationImpl();
						String cstr = (String) wba.getClassificationValue();
						ConceptImpl cimpl = getConceptImpl(cstr, rmap);
						String dname = ac.getAnnotationEvent()
								.getDocumentName();
						Integer index = annotationFileNameIndexHash.get(dname);
						int i = index.intValue();
						DocumentReference dr = documents.get(i);
						AnnotatorReference primary = primaryAnnotators.get(i);
						String atext = WEWManager.getAnnotationText(
								documentTextHash.get(dr), wba);
						ai.setText(atext);
						ai.setSemanticType(wba.getType().getName());
						ai.setClassification(cstr);
						ai.setStart(wba.getStart());
						ai.setEnd(wba.getEnd());
						ai.setDocumentReference(dr);
						ai.setConceptReference(cimpl);
						ai.setAnnotatorReference(primary);
						rmap.put(wba, ai);
					}
				}
				ac = ae.getSecondaryAnnotationCollection();
				if (ac.getAnnotations() != null) {
					for (workbench.api.annotation.Annotation wba : ac
							.getAnnotations()) {
						if ("DocumentClass".equals(wba.getType().getName())) {
							continue;
						}
						AnnotationImpl ai = new AnnotationImpl();
						String cstr = (String) wba.getClassificationValue();
						ConceptImpl cimpl = getConceptImpl(cstr, rmap);
						String dname = ac.getAnnotationEvent()
								.getDocumentName();
						Integer index = annotationFileNameIndexHash.get(dname);
						int i = index.intValue();
						DocumentReference dr = documents.get(i);
						AnnotatorReference secondary = secondaryAnnotators
								.get(i);

						String atext = WEWManager.getAnnotationText(
								documentTextHash.get(dr), wba);

						ai.setText(atext);
						ai.setSemanticType(wba.getType().getName());
						ai.setClassification(cstr);
						ai.setStart(wba.getStart());
						ai.setEnd(wba.getEnd());
						ai.setDocumentReference(dr);
						ai.setConceptReference(cimpl);
						ai.setAnnotatorReference(secondary);
						rmap.put(wba, ai);
					}
				}
			}
		}
		Constraint.initialize();
		analysis.initializeAllDefinedConstraintMatches();

		Map<ConceptReference, Map<OutcomeType, List<Annotation>>> resultsSet = rt
				.getResults();
		Map<ConceptReference, Map<CalculationType, Double>> statisticalSet = rt
				.getStatisticsalSet();
		Map<CalculationType, Double> statisticalSummary = rt
				.getStatisticalSummary();
		Map<OutcomeType, List<Annotation>> summary = rt.getSummary();

		double[] CTSums = new double[NUM_CALCULATION_TYPES];
		double[] CTCounts = new double[NUM_CALCULATION_TYPES];
		analysis.setSelectedConstraintPacket(ConstraintName);
		analysis.updateStatistics();
		ConstraintMatch cm = analysis.getConstraintMatch(ConstraintName);
		if (cm != null) {
			Vector<String> values = cm.getAlternativeValues();
			int numberOfValues = values.size();
			for (int i = 0; i < numberOfValues; i++) {
				String concept = values.elementAt(i);
				ConceptImpl cimpl = (ConceptImpl) rmap.get(concept);

				if (cimpl == null) {
					System.out.println("UNABLE TO FIND CONCEPTIMPL FOR \""
							+ concept + "\"");
				}

				MatchedValueStatistics mvs = cm.getMatchedValueStatistics(i);
				Map<OutcomeType, List<Annotation>> imap = new HashMap();
				for (OutcomeType ot : OutcomeType.values()) {
					OutcomeResult or = WEWManager.getOutcomeResult(ot);
					List<workbench.api.annotation.Annotation> wl = new ArrayList();
					List<Annotation> l = new ArrayList();
					wl = mvs.getAnnotationHashResults(or,
							workbench.api.AnnotatorType.primary);
					if (wl != null) {
						for (workbench.api.annotation.Annotation a : wl) {
							AnnotationImpl ai = (AnnotationImpl) rmap.get(a);
							if (ai != null) {
								l.add(ai);
								List<Annotation> alist = summary.get(ot);
								if (alist == null) {
									alist = new ArrayList();
									summary.put(ot, alist);
								}
								alist.add(ai);
							}
						}
					}
					wl = mvs.getAnnotationHashResults(or,
							workbench.api.AnnotatorType.secondary);
					if (wl != null) {
						for (workbench.api.annotation.Annotation a : wl) {
							AnnotationImpl ai = (AnnotationImpl) rmap.get(a);
							if (ai != null) {
								l.add(ai);
								List<Annotation> alist = summary.get(ot);
								if (alist == null) {
									alist = new ArrayList();
									summary.put(ot, alist);
								}
								alist.add(ai);
							}
						}
					}
					if (!l.isEmpty()) {
						imap.put(ot, l);
					}
				}

				resultsSet.put(cimpl, imap);

				// Before 10/6/2014
				// if (!imap.isEmpty()) {
				// resultsSet.put(cimpl, imap);
				// }

				Map<CalculationType, Double> cmap = new HashMap();
				statisticalSet.put(cimpl, cmap);
				for (CalculationType ct : CalculationType.values()) {
					double value = 0;
					switch (ct) {
					case Acc:
						value = cm.getAccuracy(i);
						break;
					case PPV:
						value = cm.getPPV(i);
						break;
					case Sen:
						value = cm.getSensitivity(i);
						break;
					case NPV:
						value = cm.getNPV(i);
						break;
					case Spec:
						value = cm.getSpecificity(i);
						break;
					case Scott:
						value = cm.getScottsPi(i);
						break;
					case Cohen:
						value = cm.getCohensKappa(i);
						break;
					case Fmeas:
						value = cm.getFmeasure(i);
						break;
					}
					CTSums[ct.ordinal()] += value;
					CTCounts[ct.ordinal()] += 1;
					cmap.put(ct, value);
				}
			}
			for (CalculationType ct : CalculationType.values()) {
				int ord = ct.ordinal();
				double sum = CTSums[ord];
				double counts = CTCounts[ord];
				double value = counts / numberOfValues;
				statisticalSummary.put(ct, value);
			}

			// 10/6/2014 TEST
			// System.out.println("ResultSet");
			// Iterator<ConceptReference> li = resultsSet.keySet().iterator();
			// while (li.hasNext()) {
			// ConceptReference cr = li.next();
			// System.out.println("\t" + cr.getCUI());
			// }
			//
			// System.out.println("StatisticsSet");
			// li = statisticalSet.keySet().iterator();
			// while (li.hasNext()) {
			// ConceptReference cr = li.next();
			// System.out.println("\t" + cr.getCUI());
			// }

		}
		return rt;
	}

	private static ConceptImpl getConceptImpl(String concept, HashMap map) {
		ConceptImpl cimpl = (ConceptImpl) map.get(concept);
		if (cimpl == null) {
			cimpl = new ConceptImpl(concept, concept);
			map.put(concept, cimpl);
		}
		return cimpl;
	}

	public static void readKnowtatorSchemaFile(final DocumentReference dr,
			KnowtatorIO kio) throws Exception {
		boolean isXML = dr.getName().contains("xml");
		String fstr = WEWManager.readDocumentReference(dr, true);
		Knowtator.readSchema(kio, fstr, isXML);
	}

	public static void readAnnotationCollection(final Analysis analysis,
			final DocumentReference dr, final AnnotatorReference ai,
			final Params params, KnowtatorIO kio, String doctext)
			throws Exception {
		String format = (String) params.getParams().get("format");
		String afilestr = WEWManager.readDocumentReference(ai, true);
		boolean isPrimary = WEWManager
				.annotatorIsPrimary(ai.getAnnotatorType());
		analysis.addAnnotationCollection(dr.getName(), ai.getName(), afilestr,
				format, isPrimary, kio, doctext);
	}

	private static String getAnnotationText(final String dtext,
			final workbench.api.annotation.Annotation wba) {
		String atext = "*";
		if (dtext != null) {
			int start = wba.getStart();
			int end = wba.getEnd() + 1;
			if (end >= dtext.length()) {
				end = dtext.length() - 1;
			}

			if (start > 0 && start < end) {
				atext = dtext.substring(start, end);
			}
		}
		return atext;
	}

	public static String readDocumentReference(final DocumentReference dr,
			final boolean doTrim) throws Exception {
		Reader r = dr.createReader();
		char[] cbuf = new char[1000000];
		int size = r.read(cbuf);
		cbuf[size - 1] = '\0';
		String text = String.valueOf(cbuf, 0, size + 1);
		if (doTrim) {
			text = text.trim();
		}
		dr.createReader().close();
		return text;
	}

	protected static OutcomeResult getOutcomeResult(final OutcomeType ot) {
		OutcomeResult or = null;
		switch (ot) {
		case TP:
			or = OutcomeResult.TP;
			break;
		case FP:
			or = OutcomeResult.FP;
			break;
		case TN:
			or = OutcomeResult.TN;
			break;
		case FN:
			or = OutcomeResult.FN;
			break;
		}
		return or;
	}

	protected static boolean formatTypeisGRAF(final Params params) {
		if (params != null) {
			String str = (String) params.getParams().get(WEWManager.FormatType);
			return (str == null || str.toLowerCase().equals(
					WEWManager.FormatTypeGRAF.toString().toLowerCase()));
		}
		return false;
	}

	protected static boolean formatTypeisKnowtator(final Params params) {
		if (params != null) {
			String str = (String) params.getParams().get(WEWManager.FormatType);
			return (str == null || str.toLowerCase().equals(
					WEWManager.FormatTypeKnowtator.toString().toLowerCase()));
		}
		return false;
	}

	protected static boolean annotatorIsPrimary(final AnnotatorType at) {
		return at == AnnotatorType.Primary;
	}

	protected static void storeParamsAsKnowledgeEngineProperties(Params params,
			KnowledgeEngine ke) {
		for (String property : params.getParams().keySet()) {
			Object value = params.getParams().get(property);
//			ke.setPropertyValue(property, value);
		}
	}

}