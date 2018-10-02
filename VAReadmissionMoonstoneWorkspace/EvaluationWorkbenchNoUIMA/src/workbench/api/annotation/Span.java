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

import java.util.Comparator;

public class Span {

	private Annotation annotation = null;
	private int start = -1;
	private int end = -1;
	
	public Span(Annotation annotation, int start, int end) {
		this.annotation = annotation;
		this.start = start;
		this.end = end;
	}
	
	public Span(Annotation annotation, annotation.Span aspan) {
		this.annotation = annotation;
		this.setStart(aspan.getTextStart());
		this.setEnd(aspan.getTextEnd());
	}
	

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
	
	public int getLength() {
		return this.end - this.start + 1;
	}

	public Annotation getAnnotation() {
		return annotation;
	}
	
	public String toString() {
		String str = "<" + this.start + "-" + this.end + ">";
		return str;
	}
	
	public static class PositionSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			Span s1 = (Span) o1;
			Span s2 = (Span) o2;
			if (s1.getStart() < s2.getStart()) {
				return -1;
			}
			if (s2.getStart() < s1.getStart()) {
				return 1;
			}
			if (s1.getEnd() > s2.getEnd()) {
				return -1;
			}
			if (s2.getEnd() > s1.getEnd()) {
				return 1;
			}
			return 0;
		}
	}
}
