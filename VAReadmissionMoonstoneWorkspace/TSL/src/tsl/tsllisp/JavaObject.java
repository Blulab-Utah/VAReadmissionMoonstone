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
package tsl.tsllisp;

import java.util.Vector;

public class JavaObject extends TLObject {
	private Object object = null;

	public JavaObject() {
	}
	
	public JavaObject(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return this.object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public boolean isString() {
		return this.object instanceof String;
	}

	public boolean isFloat() {
		return this.object instanceof Float;
	}
	
	public boolean isVector() {
		return this.object instanceof Vector;
	}
	
	public boolean equals(Object o) {
		if (o instanceof JavaObject) {
			JavaObject jo = (JavaObject) o;
			return this.getObject().equals(jo.getObject());
		}
		return false;
	}
	
	public String toString() {
		return this.object.toString();
	}

}
