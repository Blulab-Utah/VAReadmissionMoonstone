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

import tsl.information.TSLInformation;

public class TLObject {

	public TLObject() {
	}

	public boolean isNil() {
		return TLUtils.isNil(this);
	}

	public boolean isT() {
		return TLUtils.isT(this);
	}
	
	public boolean isTrue() {
		return TLUtils.isTrue(this);
	}

	public boolean isBoolean() {
		return TLUtils.isNil(this) || TLUtils.isT(this);
	}

	public boolean isAtom() {
		return !(this instanceof Sexp) || this.isBoolean();
	}

	public boolean isCons() {
		return this instanceof Sexp && !this.isBoolean();
	}

	public boolean isPair() {
		return this instanceof Sexp && ((Sexp) this).getLength() == 2;
	}

	public boolean isSexp() {
		return this.isCons();
	}

	public boolean isNonNilSexp() {
		return this.isCons();
	}

	public boolean isJavaObject() {
		return this instanceof JavaObject;
	}
	
	public boolean isTSLInformation() {
		return this instanceof TSLInformation;
	}

	public boolean isSymbol() {
		return this instanceof Symbol;
	}

	public boolean isFunctionSymbol() {
		if (this instanceof Symbol) {
			Symbol sym = (Symbol) this;
			return sym.getValue() instanceof Function;
		}
		return false;
	}

	public boolean isQuoteRelatedSymbol() {
		return (this == Symbol.quasiQuoteSym || this == Symbol.quoteSym
				|| this == Symbol.unquoteSym || this == Symbol.unquoteSplicingSym);
	}

	public boolean isString() {
		return this instanceof JavaObject && ((JavaObject) this).isString();
	}

	public boolean isFloat() {
		return this instanceof JavaObject && ((JavaObject) this).isFloat();
	}

	public boolean isNonNilAtom() {
		return !(this instanceof Sexp) && !this.isNil();
	}

	public boolean isParameterPair() {
		return ((this.isCons() && ((Sexp) this).getLength() == 2 && ((Sexp) this)
				.getFirst().isSymbol()) || this.isSymbol());
	}

	public boolean isAtomList() {
		return TLUtils.isAtomList(this);
	}
	
	public boolean isParamValueList() {
		return TLUtils.isParamValueList(this);
	}
	
	public boolean isConseList() {
		return TLUtils.isConsList(this);
	}

	public String toString() {
		return "*";
	}

}
