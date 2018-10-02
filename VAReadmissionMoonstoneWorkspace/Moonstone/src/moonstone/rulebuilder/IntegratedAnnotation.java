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
package moonstone.rulebuilder;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.utilities.VUtils;

import moonstone.annotation.Annotation;
import moonstone.annotation.MissingAnnotation;

public class IntegratedAnnotation extends Annotation {

	private String concept = null;
	private String cui = null;
	private String status = null;
	private String experiencer = null;
	private String temporality = null;
	private int textStart = -1;
	private int textEnd = -1;

	static Vector<String> acuteStrings = VUtils
			.arrayToVector(new String[] { "acute" });
	static Vector<String> chronicStrings = VUtils
			.arrayToVector(new String[] { "chronic" });
	static Vector<String> missingStrings = VUtils
			.arrayToVector(new String[] { "missing" });
	static Vector<String> absentStrings = VUtils.arrayToVector(new String[] {
			"absent", "negated" });
	static Vector<String> presentStrings = VUtils.arrayToVector(new String[] {
			"affirmed", "present" });
	static Vector<String> recentStrings = VUtils.arrayToVector(new String[] {
			"recent", "acute" });
	static Vector<String> historicalStrings = VUtils
			.arrayToVector(new String[] { "historical" });
	static Vector<String> patientStrings = VUtils
			.arrayToVector(new String[] { "patient" });
	static String acuteString = "acute";
	static String chronicString = "chronic";
	static String missingString = "missing";
	static String absentString = "negated";
	static String presentString = "affirmed";
	static String affirmedString = "affirmed";
	static String recentString = "recent";
	static String historicalString = "historical";
	static String patientString = "patient";

	String typeSystemFilename = "TypeSystemSpecsGraf";
	String typeSystemFilenameLisp = "TypeSystemSpecsLisp";

	public IntegratedAnnotation(String cui, String concept, String status,
			String experiencer, String temporality, int start, int end) {
		this.cui = cui;
		this.concept = concept;
		this.status = status;
		this.experiencer = experiencer;
		this.temporality = temporality;
		this.textStart = start;
		this.textEnd = end;
	}

	public static Vector<IntegratedAnnotation> process(
			Vector<Annotation> allAnnotations) {
		Hashtable<String, Vector<Annotation>> annotationHash = new Hashtable();
		Vector<IntegratedAnnotation> ias = null;
		if (allAnnotations != null) {
			for (Annotation annotation : allAnnotations) {
				if (!(annotation instanceof MissingAnnotation)) {
					VUtils.pushHashVector(annotationHash,
							annotation.getConcept(), annotation);
				}
			}
			for (Enumeration<String> e = annotationHash.keys(); e
					.hasMoreElements();) {
				String concept = e.nextElement();
				Vector<Annotation> annotations = annotationHash.get(concept);
				Collections.sort(annotations,
						new Annotation.StartPositionSorter());
				Annotation first = annotations.firstElement();
				Annotation last = annotations.lastElement();
				String cui = getIntegratedCui(annotations);
				String status = getIntegratedStatus(annotations);
				String experiencer = getIntegratedExperiencer(annotations);
				String temporality = getIntegratedTemporality(annotations);
				int start = first.getTextStart();
				int end = last.getTextEnd();
				IntegratedAnnotation ia = new IntegratedAnnotation(cui,
						concept, status, experiencer, temporality, start, end);
				ias = VUtils.add(ias, ia);
			}
		}
		return ias;
	}

	public static void writeToStringBuffer(Vector<IntegratedAnnotation> ias,
			StringBuffer sb) {
		if (ias != null) {
			for (IntegratedAnnotation ia : ias) {
				sb.append("DOCUMENT:Concept=" + ia.getConcept() + ",");
				sb.append("Cui=" + ia.getCui() + ",");
				sb.append("Status=" + ia.getStatus() + ",");
				sb.append("Experiencer=" + ia.getExperiencer() + ",");
				sb.append("Rule=*,");
				sb.append("Temporality=" + ia.getTemporality() + ",");
				sb.append("Start=" + ia.getTextStart() + ",");
				sb.append("End=" + ia.getTextEnd() + "\n");
			}
		}
	}

	private static String getIntegratedCui(Vector<Annotation> annotations) {
		for (Annotation ias : annotations) {
			if (ias.getCui() != null) {
				return ias.getCui();
			}
		}
		return null;
	}

	private static String getIntegratedStatus(Vector<Annotation> annotations) {
		boolean hasAbsent = false;
		boolean hasAcute = false;
		boolean hasChronic = false;
		for (Annotation annotation : annotations) {
			if (isAffirmed(annotation) && isRecent(annotation)
					&& isPatient(annotation)) {
				hasAcute = true;
			}
			if (isAffirmed(annotation) && isHistorical(annotation)
					&& isPatient(annotation) && !isAcute(annotation)) {
				hasChronic = true;
			}
			if (isAbsent(annotation)) {
				hasAbsent = true;
			}
		}
		if (hasAcute) {
			return acuteString;
		}
		if (hasChronic) {
			return chronicString;
		}
		if (hasAbsent) {
			return absentString;
		}
		return missingString;
	}

	private static String getIntegratedTemporality(
			Vector<Annotation> annotations) {
		for (Annotation annotation : annotations) {
			return annotation.getTemporality().toLowerCase();
		}
		return acuteString;
	}

	private static String getIntegratedExperiencer(
			Vector<Annotation> annotations) {
		for (Annotation annotation : annotations) {
			return annotation.getExperiencer().toLowerCase();
		}
		return patientString;
	}

	private static boolean isAcute(Annotation annotation) {
		String temporality = annotation.getTemporality().toLowerCase();
		return acuteStrings.contains(temporality);
	}

	private static boolean isChronic(Annotation annotation) {
		String temporality = annotation.getTemporality().toLowerCase();
		return chronicStrings.contains(temporality);
	}

	private static boolean isMissing(Annotation annotation) {
		String directionality = annotation.getDirectionality().toLowerCase();
		return missingStrings.contains(directionality);
	}

	private static boolean isAbsent(Annotation annotation) {
		String directionality = annotation.getDirectionality().toLowerCase();
		return absentStrings.contains(directionality);
	}

	private static boolean isAffirmed(Annotation annotation) {
		String directionality = annotation.getDirectionality().toLowerCase();
		return presentStrings.contains(directionality);
	}

	private static boolean isRecent(Annotation annotation) {
		String temporality = annotation.getTemporality().toLowerCase();
		return recentStrings.contains(temporality);
	}

	private static boolean isHistorical(Annotation annotation) {
		String temporality = annotation.getTemporality().toLowerCase();
		return historicalStrings.contains(temporality);
	}

	private static boolean isPatient(Annotation annotation) {
		String experiencer = annotation.getExperiencer().toLowerCase();
		return patientStrings.contains(experiencer);
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public String getCui() {
		return cui;
	}

	public void setCui(String cui) {
		this.cui = cui;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExperiencer() {
		return experiencer;
	}

	public void setExperiencer(String experiencer) {
		this.experiencer = experiencer;
	}

	public String getTemporality() {
		return temporality;
	}

	public void setTemporality(String temporality) {
		this.temporality = temporality;
	}

	public int getTextStart() {
		return textStart;
	}

	public void setTextStart(int textStart) {
		this.textStart = textStart;
	}

	public int getTextEnd() {
		return textEnd;
	}

	public void setTextEnd(int textEnd) {
		this.textEnd = textEnd;
	}

}
