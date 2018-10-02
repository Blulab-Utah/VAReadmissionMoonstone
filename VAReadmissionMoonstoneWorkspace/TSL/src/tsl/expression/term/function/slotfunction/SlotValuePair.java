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
package tsl.expression.term.function.slotfunction;

import java.util.Vector;
import tsl.utilities.VUtils;

public class SlotValuePair {
	private String name = null;
	private Object value = null;
	
	// Like variables, without the "?"...
	
	public SlotValuePair(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public static Vector<SlotValuePair> createSlotValuePairs(Vector<String> names,
			Vector<Object> values) {
		Vector<SlotValuePair> pairs = null;
		if (names != null && values != null && names.size() == values.size()) {
			for (int i = 0; i < names.size(); i++) {
				String name = names.elementAt(i);
				Object value = values.elementAt(i);
				SlotValuePair pair = new SlotValuePair(name, value);
				pairs = VUtils.add(pairs, pair);
			}
		}
		return pairs;
	}

}
