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
package tsl.expression.term.constant;

import java.util.Vector;

import tsl.expression.term.Term;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.StrUtils;

/*
 * For now, assume all sentences are binary.
 */
public class ObjectConstant extends Constant {
	private Object object = null;

	public ObjectConstant() {
	}

	public ObjectConstant(Object value) {
		this.setObject(value);
	}

	public ObjectConstant(TypeConstant type, String name, Object value) {
		if (type == null) {
			type = KnowledgeBase.getCurrentKnowledgeBase().getNameSpace()
					.getTypeConstant(value.toString());
		}
		if (type != null) {
			this.setType(type);
		}
		if (name != null) {
			this.setName(name);
			KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
			kb.getNameSpace().addConstant(this);
		}
		this.setObject(value);
	}

	public Object eval() {
		return this.getObject();
	}

	public boolean equals(Object o) {
		if (o instanceof ObjectConstant) {
			ObjectConstant oc = (ObjectConstant) o;
			if (this.getObject() instanceof Term
					&& oc.getObject() instanceof Term) {
				return this == oc;
			}
			if (this.getObject() != null
					&& this.getObject().equals(oc.getObject())) {
				return true;
			}
		}
		// Match a JavaObjectWrapper with a TypeConstant
		if (o instanceof TypeConstant) {
			if (o.equals(this.getType())) {
				return true;
			}
		}
		// 2/17/2011
		// Match a JavaObjectWrapper that wraps a String with any Term whose
		// name
		// encompasses that string, e.g. a BNTemplate whose name is
		// "<*pneumonia>"
		// matches the string "pneumonia".
		if (this.getObject() instanceof String && o instanceof Term) {
			String str = (String) this.getObject();
			str = str.toLowerCase();
			Term term = (Term) o;
			String name = term.getName();
			if (name != null && name.toLowerCase().equals(str)) {
				return true;
			}
		}
		return false;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public String toString() {
		if (this.getObject() != null) {
			return this.getObject().toString();
		}
		return "*";
	}

	// 2/12/2016
	public String toLisp() {
		Object o = this.getObject();
		String str = o.toString();
		if (o instanceof Vector) {
			str = "(";
			Vector v = (Vector) o;
			for (int i = 0; i < v.size(); i++) {
				Object so = v.elementAt(i);
				str += so.toString();
				if (i < v.size() - 1) {
					str += " ";
				}
			}
			str += ")";
		}
		return str;
	}

	public String getTuffyString() {
		if (this.getObject() != null) {
			String cname = this.getObject().toString();
			cname = Character.toUpperCase(cname.charAt(0)) + cname.substring(1);
			return cname;
		}
		return "UnknownObjectConstant";
	}

}
