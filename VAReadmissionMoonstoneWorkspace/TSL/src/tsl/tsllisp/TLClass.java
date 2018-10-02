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

// (defclass name parent ((slot value) (slot value) �))

public class TLClass extends TLObject {
	private String name = null;
	private TLClass parent = null;
	private Vector<TLSlot> slots = null;

	public TLClass(Sexp sexp) throws Exception {
		if (!(sexp.getLength() >= 1 && sexp.getFirst().isSymbol())) {
			throw new Exception(
					"DefClass: (defclass <cname> <parent> (slot1 form1) (slot2 form2) ...)");
		}
		this.name = ((Symbol) sexp.getFirst()).getName();
		TLObject pto = sexp.getSecond();
		int cdrindex = 1;
		if (pto.isNonNilAtom()) {
			cdrindex = 2;
			String pname = ((Symbol) pto).getName();
			Symbol pcsym = TLisp.tLisp.globalSymbolTable.findSymbol(pname);
			if (pcsym == null || pcsym.getValue() == null) {
				throw new Exception("DefClass: Parent class not found: " + pto);
			}
			this.parent = (TLClass) pcsym.getValue();
		}
		// Find parent class...
		Sexp args = (Sexp) sexp.getNthCdr(cdrindex);
		for (Enumeration<TLObject> e = args.elements(); e.hasMoreElements();) {
			TLObject o = e.nextElement();
			if (!o.isParameterPair()) {
				throw new Exception("DefClass: Invalid expression: " + sexp);
			}
			Sexp csexp = (Sexp) o;
			Symbol csym = (Symbol) csexp.getFirst();
			TLSlot slot = new TLSlot(csym.getName(), TLisp.eval(csexp.getSecond()));
			this.slots = VUtils.add(this.slots, slot);
		}
		new Symbol(TLisp.tLisp.globalSymbolTable, this.name, this);
	}

	public TLObject getSlotDefaultValue(Symbol csym) {
		TLSlot slot = TLSlot.getSlot(this.slots, csym.getName());
		if (slot != null) {
			return slot.getValue();
		}
		if (this.parent != null) {
			return this.parent.getSlotDefaultValue(csym);
		}
		return null;
	}

	public TLSlot getSlot(Symbol csym) {
		return TLSlot.getSlot(this.slots, csym.getName());
	}
	
	public String toString() {
		String str = "<Class: " + this.name + " ";
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

	public String getName() {
		return name;
	}

	public TLClass getParent() {
		return parent;
	}

	public Vector<TLSlot> getSlots() {
		return slots;
	}

}
