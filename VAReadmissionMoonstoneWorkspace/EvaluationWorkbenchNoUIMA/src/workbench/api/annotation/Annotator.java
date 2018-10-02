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

import java.util.Vector;

import tsl.utilities.VUtils;

public class Annotator {

	private Vector<String> names = null;
	private boolean isPrimary = false;

	public Annotator(String nstr, boolean isPrimary) {
		String[] nlst = nstr.split(",");
		this.names = VUtils.arrayToVector(nlst);
		this.isPrimary = isPrimary;
	}
	
	// Before 11/10/2015
//	public Annotator(String name, boolean isPrimary) {
//		this.name = name;
//		this.isPrimary = isPrimary;
//	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
	
	public Vector<String> getNames() {
		return this.names;
	}
	
	public boolean containsName(String name) {
		return this.names.contains(name);
	}
	
	public String getName() {
		return this.names.firstElement();
	}

	public String toString() {
		String str = "<" + this.names + "="
				+ (this.isPrimary ? "primary" : "secondary") + ">";
		return str;
	}


}
