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
package tsl.inference.forwardchaining;

public class SentenceObject {
	
	protected NamedSentence namedSentence = null;
	protected Object[] bindings = null;
	protected SentenceInstance[] childSentenceInstances = null;
	protected int depth = 0;
	
	public NamedSentence getNamedSentence() {
		return namedSentence;
	}

	public void setNamedSentence(NamedSentence namedSentence) {
		this.namedSentence = namedSentence;
	}

	public Object[] getBindings() {
		return bindings;
	}

	public void setBindings(Object[] bindings) {
		this.bindings = bindings;
	}

	public SentenceInstance[] getChildSentenceInstances() {
		return childSentenceInstances;
	}

	public void setChildSentenceInstances(SentenceInstance[] childSentenceInstances) {
		this.childSentenceInstances = childSentenceInstances;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	

}
