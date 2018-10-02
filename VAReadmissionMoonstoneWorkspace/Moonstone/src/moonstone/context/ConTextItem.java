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
package moonstone.context;

import java.util.Enumeration;
import java.util.Hashtable;


public class ConTextItem {

	private String string = null;
	private ConText conText = null;
	private Hashtable<String, String> properties = new Hashtable();
	private boolean isMatch = true;

	public ConTextItem(ConText ct) {
		this.conText = ct;
	}

	public String toString() {
		String str = "<" + this.getString() + ":";
		for (Enumeration<String> e = this.getProperties().keys(); e
				.hasMoreElements();) {
			String header = e.nextElement();
			String value = this.getProperty(header);
			String hvstr = header + "=" + value;
			str += hvstr;
			if (e.hasMoreElements()) {
				str += ",";
			}
		}
		str += ">";
		return str;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public ConText getConText() {
		return conText;
	}

//	public String getCategory() {
//		return this.properties.get(ConText.exCategoryHeader);
//	}
//
//	public String getClosure() {
//		return this.properties.get(ConText.exClosureHeader);
//	}
//
//	public String getAction() {
//		return this.properties.get(ConText.exActionHeader);
//	}

	public String getProperty(String property) {
		return this.properties.get(property);
	}

	public void setProperty(String property, String value) {
		if (value != null && value.length() > 0
				&& Character.isLetter(value.charAt(0))) {
			this.properties.put(property, value);
		}
	}

	public boolean isMatch() {
		return isMatch;
	}

	public void setMatch(boolean isMatch) {
		this.isMatch = isMatch;
	}

	public Hashtable<String, String> getProperties() {
		return properties;
	}

}

