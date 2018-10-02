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

import java.util.Enumeration;
import java.util.Vector;

import tsl.utilities.VUtils;

// (definstance classname :slot val :slot val ...)

public class TLClassInstance extends TLObject {
	private TLClass pclass = null;
	private Vector<TLSlot> slots = null;

	public TLClassInstance(Sexp sexp) throws Exception {
		if (!(sexp.getLength() >= 1 && sexp.getFirst().isSymbol())) {
			throw new Exception(
					"Format: (definstance <classname> (slot1 val1) (slot2 val2) ...)");
		}
		Symbol sym = (Symbol) sexp.getFirst();
		Symbol csym = TLisp.tLisp.globalSymbolTable.findSymbol(sym.getName());
		if (csym == null || !(csym.getValue() instanceof TLClass)) {
			throw new Exception("Class name not found: " + sym);
		}
		this.pclass = (TLClass) csym.getValue();
		Sexp args = (Sexp) sexp.getCdr();
		if (!args.isNil()) {
			for (Enumeration<TLObject> e = args.elements(); e
					.hasMoreElements();) {
				TLObject to = e.nextElement();
				if (!to.isParameterPair()) {
					throw new Exception(sexp + ": Slot names must be symbols");
				}
				Sexp arg = (Sexp) to;
				TLSlot slot = this.pclass.getSlot((Symbol) arg.getFirst());
				if (slot == null) {
					throw new Exception("Unknown slot name: " + arg);
				}
				TLSlot cslot = new TLSlot(slot.getName(), TLisp.eval(arg.getSecond()));
				this.slots = VUtils.add(this.slots, cslot);
			}
		}
	}

	public TLSlot getSlot(Symbol csym) {
		return TLSlot.getSlot(this.slots, csym.getName());
	}

	// (get-slot-value ?x :slotname)
	public TLObject getSlotValue(Symbol csym) {
		TLSlot slot = this.getSlot(csym);
		if (slot != null) {
			return slot.getValue();
		}
		return this.pclass.getSlotDefaultValue(csym);
	}

	// (set-slot-value ?x :slotname 'value)
	public void setSlotValue(Symbol csym, TLObject value) throws Exception {
		TLSlot slot = this.getSlot(csym);
		if (slot == null) {
			if (this.getPclass().getSlot(csym) != null) {
				slot = new TLSlot(csym.getName(), value);
				this.slots = VUtils.add(this.slots, slot);
			} else {
				throw new Exception("Unknown slot name: " + csym);
			}
		}
		slot.setValue(value);
	}
	
	public TLClass getPclass() {
		return pclass;
	}

	public Vector<TLSlot> getSlots() {
		return slots;
	}

	public String toString() {
		String str = "<ClassInstance: " + this.pclass.getName();
		if (this.slots != null) {
			str += " (";
			for (TLSlot slot : this.slots) {
				str += slot.getName() + "=" + slot.getValue() + ":";
			}
			str += ")";
		}
		str += ">";
		return str;
	}
}
