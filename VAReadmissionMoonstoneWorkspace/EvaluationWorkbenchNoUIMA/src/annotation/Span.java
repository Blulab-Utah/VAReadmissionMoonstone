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
package annotation;

import java.util.Comparator;

import tsl.utilities.SeqUtils;

public class Span extends EVAnnotation {
	EVAnnotation annotation = null;
	int textStart = 0;
	int textEnd = 0;
	String text = null;
	String id = null;
	
	public Span(int start, int end) {
		this(null, start, end);
	}
	
	public Span(EVAnnotation annotation, int start, int end) {
		this.annotation = annotation;
		this.textStart = start;
		this.textEnd = end;
		this.setId(null);
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public int getLength() {
		return this.textEnd - this.textStart;
	}
	
	public String toString() {
		return "<" + this.textStart + "-" + this.textEnd + ">";
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		if (id == null && this.annotation != null) {
			int num = this.annotation.getAnnotationCollection().getNumberOfSpans();
			id = "span_" + num;
			this.annotation.getAnnotationCollection().setNumberOfSpans(num+1);
		}
		this.id = id;
	}
	
	public static int getOverlap(Span s1, Span s2) {
		int overlap = SeqUtils.amountOverlap(s1.getTextStart(),
				s1.getTextEnd(), s2.getTextStart(), 
				s2.getTextEnd());
		return overlap;
	}
	
	// 2/23/2013
	public boolean coversPosition(int position) {
		return this.textStart <= position && position <= this.textEnd;
	}
	
	public static class PositionSorter implements Comparator {
		
		public int compare(Object o1, Object o2) {
			Span s1 = (Span) o1;
			Span s2 = (Span) o2;
			if (s1.getTextStart() < s2.getTextStart()) {
				return -1;
			}
			if (s1.getTextStart() > s2.getTextStart()) {
				return 1;
			}
			return 0;
		}
	}

	
}
