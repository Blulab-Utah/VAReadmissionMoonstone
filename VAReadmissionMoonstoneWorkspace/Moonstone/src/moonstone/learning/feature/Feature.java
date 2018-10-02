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
package moonstone.learning.feature;

import java.util.Comparator;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.javafunction.JavaFunctions;
import tsl.documentanalysis.document.Document;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class Feature {

	protected int index = -1;
	protected String content = null;
	protected int count = 0;

	public Feature(int index, String content, int count) {
		this.index = index;
		this.content = content;
		this.count = count;
	}

	public String getContent() {
		return content;
	}

	public int getCount() {
		return count;
	}

	public void incrementCount() {
		this.count++;
	}

	public int getIndex() {
		return index;
	}

	public static String generatePropositionalFeature(Annotation annotation) {
		Document doc = annotation.getDocument();
		int daydiff = doc.getAdmitDictationDayDifference();
		String daystr = (daydiff < 30 ? "new" : "old");
		int rindex = doc.getAdmitDateRangeIndex();
		int tindex = doc.getGeneralDictationType();
		String negstr = JavaFunctions.isNegated(annotation) ? "negated"
				: "affirmed";
		String concept = annotation.getConcept().toString();
		String content = concept + ":" + negstr + ":" + rindex + ":" + tindex;
		// 10/5/2016- Just for now
		content = concept + ":" + negstr + ":" + daystr;
		content = content.toUpperCase();
		return content;
	}

	public String toString() {
		String str = "<Content= " + this.content + ", index= " + this.index
				+ ", Count=" + this.count + ">";
		return str;
	}

	public static class FeatureIndexSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Feature f1 = (Feature) o1;
			Feature f2 = (Feature) o2;
			int i1 = f1.getIndex();
			int i2 = f2.getIndex();
			if (i1 < i2) {
				return -1;
			}
			if (i2 < i1) {
				return 1;
			}
			return 0;
		}
	}

}
