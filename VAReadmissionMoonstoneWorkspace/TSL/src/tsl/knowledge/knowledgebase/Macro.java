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
package tsl.knowledge.knowledgebase;

import java.util.Vector;

import tsl.expression.term.variable.Variable;
import tsl.utilities.VUtils;

public class Macro {

	private String name = null;
	private Vector<Variable> variables = null;
	private Vector definition = null;

	public Macro(Vector v) {
		this.name = (String) v.elementAt(1);
		Vector<String> vnames = (Vector<String>) v.elementAt(2);
		this.variables = Variable.createVariables(vnames);
		this.definition = (Vector) v.elementAt(3);
	}

	public static Macro createMacro(Vector v) {
		if (isMacroDefinition(v)) {
			return new Macro(v);
		}
		return null;
	}

	public Object expand(Object item) {
		if (Variable.isVariable(item)) {
			Variable var = Variable.find(this.variables, (String) item);
			if (var != null) {
				return var.getValue();
			} else {
				return item;
			}
		} else if (item instanceof Vector) {
			Vector v = new Vector(0);
			for (Object o : (Vector) item) {
				Object rv = expand(o);
				v = VUtils.add(v, rv);
			}
			return v;
		}
		return item;
	}

	public static boolean isMacroDefinition(Vector v) {
		return (v != null && v.size() == 4
				&& "defmacro".equals(v.firstElement())
				&& v.elementAt(1) instanceof String
				&& Variable.isVariable(v.elementAt(2)) && v.elementAt(3) instanceof Vector);
	}

	public String getName() {
		return name;
	}

	public Vector<Variable> getVariables() {
		return this.variables;
	}
	
	

	public void setVariables(Vector<Variable> variables) {
		this.variables = variables;
	}

	public Vector getDefinition() {
		return definition;
	}
	
	

}
