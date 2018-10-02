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
package tsl.expression.term.template;

import java.util.Vector;

import tsl.expression.term.constant.Constant;
import tsl.expression.term.variable.Variable;

// I use the Term's variables to store parameters, but sometimes need to know that a Term is
// being used as a Template of a Type; hence the specialization.

public class Template extends Constant {
	
	public Template(String name) {
		super(name);
	}
	
	public Vector<Variable> getParameters() {
		return this.getVariables();
	}

}
