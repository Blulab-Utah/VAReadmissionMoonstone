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
package workbench.api.annotation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import workbench.api.AnnotatorType;
import workbench.api.OutcomeResult;
import workbench.api.constraint.ConstraintMatch;

public class MatchedValueStatistics {
	private ConstraintMatch matchedConstraintAnnotations = null;
	private String value = null;
	private int truePositive = 0;
	private int trueNegative = 0;
	private int falsePositive = 0;
	private int falseNegative = 0;

	// 7/5/2014
	private Map<OutcomeResult, Map<AnnotatorType, List<Annotation>>> annotationMap = new HashMap();

	// ADD OTHER STATISTICS

	public MatchedValueStatistics(ConstraintMatch mca, String value) {
		this.matchedConstraintAnnotations = mca;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public ConstraintMatch getMatchedConstraintAnnotations() {
		return matchedConstraintAnnotations;
	}

	public int getTruePositive() {
		return truePositive;
	}

	// private Map<OutcomeResult, Map<AnnotatorType, List<Annotation>>>
	// annotationMap = new HashMap();
	public void incrementTruePositive(Annotation primary, Annotation secondary) {
		this.truePositive++;

		// 7/5/2014
		addAnnotationHash(OutcomeResult.TP, AnnotatorType.primary, primary);
		addAnnotationHash(OutcomeResult.TP, AnnotatorType.secondary, secondary);
	}

	public int getTrueNegative() {
		return trueNegative;
	}

	public void incrementTrueNegative(Annotation primary, Annotation secondary) {
		this.trueNegative++;

		// 7/5/2014
		addAnnotationHash(OutcomeResult.TN, AnnotatorType.primary, primary);
		addAnnotationHash(OutcomeResult.TN, AnnotatorType.secondary, secondary);
	}

	public int getFalsePositive() {
		return falsePositive;
	}

	public void incrementFalsePositive(Annotation primary, Annotation secondary) {
		this.falsePositive++;
		if (primary != null) {
			addAnnotationHash(OutcomeResult.FP, AnnotatorType.primary, primary);
		}
		addAnnotationHash(OutcomeResult.FP, AnnotatorType.secondary, secondary);
	}

	public int getFalseNegative() {
		return falseNegative;
	}

	public void incrementFalseNegative(Annotation primary, Annotation secondary) {
		this.falseNegative++;
		addAnnotationHash(OutcomeResult.FN, AnnotatorType.primary, primary);
		if (secondary != null) {
			addAnnotationHash(OutcomeResult.FN, AnnotatorType.secondary, secondary);
		}
	}

	public String toString() {
		String str = "<MVS: Value=" + this.value + ",TP=" + this.truePositive
				+ ",TN=" + this.trueNegative + ",FP=" + this.falsePositive
				+ ",FN=" + this.falseNegative + ">";
		return str;
	}

	private void addAnnotationHash(OutcomeResult result, AnnotatorType atype,
			Annotation annotation) {
		if (annotation != null) {
			Map<AnnotatorType, List<Annotation>> imap = this.annotationMap
					.get(result);
			List<Annotation> l = null;
			if (imap == null) {
				imap = new HashMap();
				this.annotationMap.put(result, imap);
			}
			l = imap.get(atype);
			if (l == null) {
				l = new ArrayList(0);
				imap.put(atype, l);
			}
			l.add(annotation);
		}
	}

	public List<Annotation> getAnnotationHashResults(OutcomeResult result,
			AnnotatorType atype) {
		Map<AnnotatorType, List<Annotation>> imap = this.annotationMap
				.get(result);
		if (imap != null) {
			return imap.get(atype);
		}
		return null;
	}
	


}
