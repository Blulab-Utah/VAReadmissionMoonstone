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
package workbench.api.typesystem;

import java.util.Vector;

import tsl.utilities.VUtils;
import workbench.api.WorkbenchAPIObject;

public class Attribute extends WorkbenchAPIObject {
	protected String fullName = null;
	protected Type parent = null;
	protected Vector values = null;
	
	public Attribute(Type parent, String aname) {
		super(aname);
		this.parent = parent;
		this.fullName = parent.getFullname() + ":" + aname;
		parent.getTypeSystem().putObjectHash(this.fullName, this);
		parent.addAttribute(this);
	}
	
	public static Attribute createAttribute(Type parent, String aname) {
		Attribute attr = parent.getAttribute(aname);
		if (attr == null) {
			attr = new Attribute(parent, aname);
		}
		return attr;
	}
	
	public void addValues(Vector values) {
		this.values = VUtils.appendIfNot(this.values, values);
	}
	
	public void addValue(Object value) {
		this.values = VUtils.addIfNot(this.values, value);
	}

	public Vector<String> getValues() {
		return values;
	}

	public void setValues(Vector values) {
		this.values = values;
	}

	public String getFullName() {
		return this.fullName;
	}
	
	public Type getParent() {
		return parent;
	}
	
	public void setParent(Type parent) {
		this.parent = parent;
		
		// 10/12/2015
		this.fullName = parent.getFullname() + ":" + this.getName();
	}

	public boolean isClassification() {
		return this instanceof Classification;
	}
	
	public String toString() {
		String str = "<Attribute: Name=" + this.fullName + ">";
		return str;
	}
	
	public String toXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("  <attribute name=\"" + this.getName() + "\">\n");
		if (this.getValues() != null) {
			for (Object value : this.getValues()) {
				sb.append("    <value> " + value + "</value>\n");
			}
		}
		sb.append("  </attribute>\n");
		return sb.toString();
	}
	
	public Vector getAlternativeValues() {
		if (this.values != null) {
			return new Vector(this.values);
		}
		return null;
	}


}
