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
package workbench.api.input.knowtator;

import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Type;


public class KTAttribute {

	String attribute = null;
	String value = null;

	public KTAttribute(String attribute, String value) {
		this.attribute = attribute;
		this.value = value;
	}
	
	public void addToTypeSystem(Type parent) throws Exception {
		new Attribute(parent, this.getAttribute());
	}

	// Before 9/18/2014
//	public void addToTypeSystem(TypeObject parent) throws Exception {
//		Attribute attribute = new Attribute(parent.getTypeSystem(),
//				this.getAttribute(), this.getAttribute(), null);
//		attribute.setParentTypeObject(parent);
//		parent.addAttribute(attribute);
//		TypeSystem.addTypeObjectByID(attribute);
//		TypeSystem.addTypeObjectByUima(attribute);
//	}

	public String toString() {
		return "<Attribute:" + this.attribute + "=" + this.value + ">";
	}

	public String getAttribute() {
		return attribute;
	}

	public String getValue() {
		return value;
	}

}
