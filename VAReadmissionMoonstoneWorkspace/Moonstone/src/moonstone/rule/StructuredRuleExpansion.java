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
package moonstone.rule;

import moonstone.annotation.Annotation;

public class StructuredRuleExpansion extends RuleExpansion {
	private int lastMatchedIndex = 0;

	public StructuredRuleExpansion(Rule rule, Annotation matched) {
		super(rule, matched, 0);
	}

	public StructuredRuleExpansion(StructuredRuleExpansion expansion,
			Annotation matched) {
		super(expansion, matched, (expansion.lastMatchedIndex + 1));
		this.lastMatchedIndex = (expansion.lastMatchedIndex + 1);
	}

	public int getLastMatchedIndex() {
		return lastMatchedIndex;
	}

//	public boolean checkIsFullyMatched() {
//		return this.lastMatchedIndex == this.getRule().getWordListCount() - 1;
//	}
	
//	public boolean checkIsValid() {
//		return true;
//	}

	public Annotation getLastMatchedAnnotation() {
		return this.matchedAnnotations[this.lastMatchedIndex];
	}
	
	public Annotation getFirstMatchedAnnotation() {
		return this.matchedAnnotations[0];
	}
	
	public int getStartToken() {
		Annotation annotation = this.getFirstMatchedAnnotation();
		return annotation.getTokenStart();
	}
	
	public int getEndToken() {
		Annotation annotation = this.getLastMatchedAnnotation();
		return annotation.getTokenEnd();
	}

}
