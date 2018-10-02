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
package typesystem;

import java.util.Vector;

import tsl.utilities.VUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class Attribute.
 */
public class Attribute extends TypeObject {

	/** The values. */
	Vector values = null;
	
	/** The is display. */
	boolean isDisplay = false;

	/**
	 * Instantiates a new attribute.
	 *
	 * @param ts the ts
	 * @param id the id
	 * @param name the name
	 * @param uima the uima
	 */
	public Attribute(TypeSystem ts, String id, String name, String uima) {
		super(ts, id, name, uima);
	}
	// SPM 12/16/2011  Add UIMA type system parameter
	/**
	 * Instantiates a new attribute.
	 *
	 * @param ts the ts
	 * @param id the id
	 * @param name the name
	 * @param uima the uima
	 * @param type the type
	 */
	public Attribute(TypeSystem ts, String id, String name, String uima, String type) {
		super(ts, id, name, uima, type);
	}
	
	// TEST:  10/31/2013
	public int hashCode() {
		return this.getName().hashCode();
	}
	
	/**
	 * Adds the value.
	 *
	 * @param value the value
	 */
	public void addValue(Object value) {
		if (value != null && !"EMPTY".equals(value)) {
			this.values = VUtils.addIfNot(this.values, value);
		}
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public Vector getValues() {
		return this.values;
	}

	// 3/1/2012 Test...
	/**
	 * Checks if is display.
	 *
	 * @return true, if is display
	 */
	public boolean isDisplay() {
		return isDisplay;
	}
	
	/**
	 * Sets the checks if is display.
	 *
	 * @param isDisplay the new checks if is display
	 */
	public void setIsDisplay(boolean isDisplay) {
		this.isDisplay = isDisplay;
	}
	
	String toLisp(int depth) {
		StringBuffer sb = new StringBuffer();
		addSpaces(sb, depth);
		sb.append("(\"" + this.name + "\"");
		if (this.isDisplay) {
			sb.append(" (display true)");
		}
		sb.append(")");
		return sb.toString();
	}
	
	public String toString() {
		return this.getName();
	}
	
	
	
}
