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

public class TLSlot {
	private String name = null;
	private TLObject value = null;

	public TLSlot(String name, TLObject value) {
		this.name = name;
		this.value = value;
	}

	public TLSlot(String name) {
		this.name = name;
	}
	
	public static TLSlot getSlot(Vector<TLSlot> slots, String name) {
		if (slots != null && name != null) {
			for (TLSlot slot : slots) {
				if (name.equals(slot.name)) {
					return slot;
				}
			}
		}
		return null;
	}

	public TLObject getValue() {
		return value;
	}

	public void setValue(TLObject value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return "<Name=" + this.name + ",Value=" + this.value + ">";
	}

}
