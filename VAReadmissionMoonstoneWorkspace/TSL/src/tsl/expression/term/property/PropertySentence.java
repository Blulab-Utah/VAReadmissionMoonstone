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
package tsl.expression.term.property;

import tsl.expression.term.Term;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.relation.BinaryRelationSentence;

public class PropertySentence extends BinaryRelationSentence {
	
	public PropertySentence(PropertyConstant pc, Term domain,
			Object range) {
		super(pc);
		if (!(range instanceof ObjectConstant)) {
			range = new ObjectConstant(range);
		}
		this.addTerm(domain);
		this.addTerm(range);
		
		// 4/12/2013:  This is redundant:  Storing the property as a straight-up 
		// Java Object in the Information.property table.  Do I need this?
		domain.setProperty(pc.getName(), range);
		this.getSubject();
		this.getModifier();
	}
	
	public static PropertySentence createPropertySentence(PropertyConstant pc,
			Term domain, Object range) {
		if (pc.testAssignments(domain, range)) {
			return new PropertySentence(pc, domain, range);
		}
		return null;
	}
	
	public ObjectConstant getRange() {
		if (this.getTerms() != null) {
			ObjectConstant oc = (ObjectConstant) this.getTerms().elementAt(1);
			return oc;
		}
		return null;
	}
	
	public Object getRangeValue() {
		ObjectConstant range = this.getRange();
		return (range != null ? range.getObject() : null);
	}

}
