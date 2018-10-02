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
package tsl.documentanalysis.lexicon;

import java.util.Enumeration;
import java.util.Vector;

import tsl.utilities.VUtils;


public class MorphRule {

	boolean isSuffix = false;

	boolean isPrefix = false;

	String toDelete = null;

	int toDeleteLength = 0;

	String toAdd = null;

	String oldPOS = null;

	String newPOS = null;

	String formType = null;

	String formValue = null;

	public static String[][] ruleDefs = {
//			{ "suffix", "ly", null, "adj", "adv" },
//
//			{ "suffix", "ily", "y", "adj", "adv" },
//
//			{ "suffix", "ed", null, "verb", "adj", },
//
//			{ "suffix", "ed", null, "verb", "verb", "tense", "past" },
//
//			{ "suffix", "ed", "e", "verb", "adj", },
//
//			{ "suffix", "ed", "e", "verb", "verb", "tense", "past" },
//
//			{ "suffix", "nned", "n", "verb", "adj", },
//
//			{ "suffix", "nned", "n", "verb", "verb", "tense", "past" },
//
//			{ "suffix", "ied", "y", "verb", "adj", },
//
//			{ "suffix", "ied", "y", "verb", "verb", "tense", "past" },
//
//			{ "prefix", "re", null, "verb", "verb", },
//
//			{ "prefix", "pre", null, "verb", "verb", },
//
//			{ "suffix", "ness", null, "adj", "noun", },
//
//			{ "suffix", "iness", "y", "adj", "noun", },

			{ "suffix", "s", null, "noun", "noun", "number", "plural" },

			{ "suffix", "s", null, "verb", "verb", "tense", "present" },
//
//			{ "suffix", "es", null, "noun", "noun", "number", "plural" },
//
//			{ "suffix", "es", null, "verb", "verb", "tense", "present" },
//
//			{ "suffix", "er", null, "verb", "noun", },
//
//			{ "suffix", "er", "e", "verb", "noun", },
//
//			{ "suffix", "er", null, "adj", "adj", },
//
//			{ "suffix", "ier", "y", "adj", "adj", },
//
//			{ "suffix", "est", null, "adj", "adj", },
//
//			{ "suffix", "iest", "y", "adj", "adj", },
//
//			{ "suffix", "y", null, "noun", "adj", },
//
//			{ "suffix", "y", "e", "noun", "adj", },
//
//			{ "suffix", "ing", null, "verb", "verb", "tense", "pres_participle" },
//
//			{ "suffix", "ing", null, "verb", "gerund" },
//
			{ "suffix", "ing", "e", "verb", "verb", "tense", "pres_participle" },
//
//			{ "suffix", "ing", "e", "verb", "gerund" },
//
//			{ "suffix", "nning", "n", "verb", "verb", "tense",
//					"pres_participle" },
//
//			{ "suffix", "nning", "n", "verb", "gerund" },
//
//			{ "suffix", "less", null, "noun", "adj", },
//
//			{ "suffix", "ful", null, "noun", "adj", },
//
//			{ "prefix", "super", null, "noun", "noun", },
//
//			{ "suffix", "ic", null, "noun", "adj", },
//
//			{ "suffix", "tion", "e", "verb", "noun", },
//
//			{ "suffix", "er", null, "noun", "verb", } 
			
	};

	static Vector allRules = new Vector(0);

	public MorphRule(String[] def) {
		this.isPrefix = ("prefix".equals(def[0]) ? true : false);
		this.isSuffix = ("suffix".equals(def[0]) ? true : false);
		this.toAdd = def[1];
		this.toDelete = def[2];
		this.oldPOS = def[3];
		this.newPOS = def[4];
		if (def.length > 5) {
			this.formType = def[5];
			this.formValue = def[6];
		}
		if (this.toDelete != null) {
			this.toDeleteLength = this.toDelete.length();
		}
		allRules.add(this);
	}

	String[] match(String word, String oldPOS) {
		String modifiedWord = null;
		if (oldPOS.equals(this.oldPOS)) {
			String toAdd = (this.toAdd != null ? this.toAdd : "");
			if (this.isPrefix
					&& (this.toDelete == null || word.startsWith(this.toDelete))) {
				modifiedWord = toAdd + word.substring(this.toDeleteLength);
			} else if (this.isSuffix
					&& (this.toDelete == null || word.endsWith(this.toDelete))) {
				modifiedWord = word.substring(0,
						(word.length() - this.toDeleteLength));
				modifiedWord += toAdd;
			}
		}
		if (modifiedWord != null) {
			return new String[] { modifiedWord, this.newPOS, this.formType,
					this.formValue };
		}
		return null;
	}

	public String toString() {
		String str = "<" + (isSuffix ? "SUFFIX" : "PREFIX") + ",OldPOS="
				+ oldPOS + ",NewPOS=" + newPOS + ",Delete=" + toDelete
				+ ",Add=" + toAdd + ",FormType=" + formType + ",FormValue="
				+ formValue + ">";
		return str;
	}

	public static Vector<String[]> generateLexicalVariants(String word,
			String pos) {
		Vector variants = null;
		for (Enumeration e = allRules.elements(); e.hasMoreElements();) {
			MorphRule rule = (MorphRule) e.nextElement();
			String[] variant = rule.match(word, pos);
			if (variant != null) {
				variants = VUtils.add(variants, variant);
			}
		}
		return variants;
	}

	public static void initialize() {
		for (int i = 0; i < ruleDefs.length; i++) {
			new MorphRule(ruleDefs[i]);
		}
	}

}
