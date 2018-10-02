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


import tsl.documentanalysis.document.DocumentAccess;
import tsl.knowledge.ontology.umls.RequiredCUIs;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class WordEntry {

	DocumentAccess documentAccess = null;

	WordEntry baseWordEntry = null;

	Vector<WordEntry> variantWordEntries = null;

	String word = null;

	String base = null;

	Vector<String> partsOfSpeech = null;

	Vector<String> variants = null;

	Vector<String> formTypes = null;

	Vector<String> formValues = null;

	Vector<String> spellingVariants = null;

	Vector<String> nominalizations = null;

	Vector<String> nominalizationOf = null;

	int sourceType = IsSpecialistLexiconBaseForm;

	static int IsSpecialistLexiconBaseForm = 0;

	static int IsSpecialistLexiconVariant = 1;

	static int IsSpecialistLexiconIrregular = 2;

	static int isAutomaticallyGeneratedVariant = 3;

	public WordEntry(DocumentAccess documentAccess, String word) {
		this.word = word;
		this.documentAccess = documentAccess;
	}

	public void store() {
		if (this.existsInDocumentCorpus()) {
			this.documentAccess.wordEntryHash.put(this.word, this);
		}
	}

	boolean existsInDocumentCorpus() {
		if (documentAccess.wordExistsInCorpus(this.word)
				|| RequiredCUIs.isRequiredWord(this.word)) {
			return true;
		}
		if (this.baseWordEntry != null
				&& (this.documentAccess
						.wordExistsInCorpus(this.baseWordEntry.word) || RequiredCUIs
						.isRequiredWord(this.baseWordEntry.word))) {
			return true;
		}
		if (this.variantWordEntries != null) {
			for (WordEntry vwe : this.variantWordEntries) {
				if (this.documentAccess.wordExistsInCorpus(vwe.word)
						|| RequiredCUIs.isRequiredWord(vwe.word)) {
					return true;
				}
			}
		}
		return false;
	}

	public void assignValueFromSpecialistLexicon(String str) {
		Vector v = StrUtils.stringList(str, '=');
		if (v.size() == 2) {
			String attribute = (String) v.elementAt(0);
			String value = (String) v.elementAt(1);
			if ("spelling_variant".equals(attribute)) {
				this.spellingVariants = VUtils.addIfNot(this.spellingVariants,
						value);
			} else if ("cat".equals(attribute)) {
				assignPOS(value);
				Word existing = Lexicon.currentLexicon.getWord(this.word);
				if (existing != null && existing.getPartsOfSpeech() != null) {
					for (Enumeration e = existing.getPartsOfSpeech().elements(); e
							.hasMoreElements();) {
						String epos = (String) e.nextElement();
						if (!this.partsOfSpeech.contains(epos)) {
							assignPOS(epos);
						}
					}
				}
			} else if ("variants".equals(attribute)) {
				this.variants = VUtils.addIfNot(this.variants, value);
				Vector irregs = irregularForms(value);
				if (irregs != null) {
					String lastpos = (String) this.partsOfSpeech.lastElement();
					for (Enumeration e = irregs.elements(); e.hasMoreElements();) {
						String irreg = (String) e.nextElement();
						if (!irreg.equals(this.word)) {
							WordEntry ientry = this.documentAccess
									.createWordEntry(irreg);
							if (ientry != null) {
								ientry.baseWordEntry = this;
								this.variantWordEntries = VUtils.add(
										this.variantWordEntries, ientry);
								ientry.base = this.word;
								ientry.assignPOS(lastpos);
								ientry.sourceType = IsSpecialistLexiconIrregular;
								ientry.store();
							}
						}
					}
				}
			} else if ("nominalization_of".equals(attribute)) {
				this.nominalizationOf = VUtils.addIfNot(this.nominalizationOf,
						value);
			} else if ("variant".equals(attribute)) {
				Vector vinfo = StrUtils.stringList(value, ';');
				String variant = (String) vinfo.firstElement();
				if (!variant.equals(this.word)
						&& Character.isLetter(variant.charAt(0))) {
					String lastpos = (String) this.partsOfSpeech.lastElement();
					WordEntry ventry = this.documentAccess
							.createWordEntry(variant);
					if (ventry != null) {
						ventry.baseWordEntry = this;
						this.variantWordEntries = VUtils.add(
								this.variantWordEntries, ventry);
						ventry.base = this.word;
						ventry.assignPOS(lastpos);
						ventry.sourceType = IsSpecialistLexiconVariant;
						ventry.store();
					}
				}
			}
		}
	}

	void assignPOS(String pos) {
		if (this.partsOfSpeech == null || !this.partsOfSpeech.contains(pos)) {
			this.partsOfSpeech = VUtils.add(this.partsOfSpeech, pos);
			this.formTypes = VUtils.add(this.formTypes, "*");
			this.formValues = VUtils.add(this.formValues, "*");
		}
	}

	public boolean isRegular() {
		return this.variants != null && this.variants.contains("reg");
	}

	static Vector irregularForms(String vstr) {
		if (vstr.startsWith("irreg")) {
			Vector forms = StrUtils.stringList(vstr, '|');
			return VUtils.removeDuplicates(VUtils.subVector(forms, 1));
		}
		return null;
	}

	public String toString() {
		String str = "<Word=" + this.word + ",Base=" + this.base
				+ ",PartsOfSpeech=" + this.partsOfSpeech + ",Variants="
				+ this.variants + ",FormTypes=" + this.formTypes
				+ ",FormValues=" + this.formValues + ",SpellingVariants="
				+ this.spellingVariants + ",Nominalizations="
				+ this.nominalizations + ",NominalizationsOf="
				+ this.nominalizationOf + ">";
		return str;
	}

	public WordEntry getBaseWordEntry() {
		return baseWordEntry;
	}

	public void setBaseWordEntry(WordEntry baseWordEntry) {
		this.baseWordEntry = baseWordEntry;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public Vector<String> getPartsOfSpeech() {
		return partsOfSpeech;
	}

	public void addPartOfSpeech(String pos) {
		this.partsOfSpeech = VUtils.addIfNot(this.partsOfSpeech, pos);
	}

	public Vector getFormTypes() {
		return formTypes;
	}

	public void addFormType(String ftype) {
		this.formTypes = VUtils.addIfNot(this.formTypes, ftype);
	}

	public Vector getFormValues() {
		return formValues;
	}

	public void addFormValue(String fvalue) {
		this.formValues = VUtils.addIfNot(this.formValues, fvalue);
	}

	public Vector getSpellingVariants() {
		return spellingVariants;
	}

	public void addSpellingVariant(String variant) {
		this.spellingVariants = VUtils.add(this.spellingVariants, variant);
	}

	public Vector getVariants() {
		return variants;
	}

	public void setVariants(Vector variants) {
		this.variants = variants;
	}

	public DocumentAccess getDocumentAccess() {
		return documentAccess;
	}

	public void setDocumentAccess(DocumentAccess documentAccess) {
		this.documentAccess = documentAccess;
	}

	public Vector<WordEntry> getVariantWordEntries() {
		return variantWordEntries;
	}

	public void addVariantWordEntry(WordEntry we) {
		this.variantWordEntries = VUtils.add(this.variantWordEntries, we);
	}

	public Vector<String> getNominalizations() {
		return nominalizations;
	}

	public void setNominalizations(Vector<String> nominalizations) {
		this.nominalizations = nominalizations;
	}

	public Vector<String> getNominalizationOf() {
		return nominalizationOf;
	}

	public void setNominalizationOf(Vector<String> nominalizationOf) {
		this.nominalizationOf = nominalizationOf;
	}

	public int getSourceType() {
		return sourceType;
	}

	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	public static int getIsSpecialistLexiconBaseForm() {
		return IsSpecialistLexiconBaseForm;
	}

	public static void setIsSpecialistLexiconBaseForm(
			int isSpecialistLexiconBaseForm) {
		IsSpecialistLexiconBaseForm = isSpecialistLexiconBaseForm;
	}

	public static int getIsSpecialistLexiconVariant() {
		return IsSpecialistLexiconVariant;
	}

	public static void setIsSpecialistLexiconVariant(
			int isSpecialistLexiconVariant) {
		IsSpecialistLexiconVariant = isSpecialistLexiconVariant;
	}

	public static int getIsSpecialistLexiconIrregular() {
		return IsSpecialistLexiconIrregular;
	}

	public static void setIsSpecialistLexiconIrregular(
			int isSpecialistLexiconIrregular) {
		IsSpecialistLexiconIrregular = isSpecialistLexiconIrregular;
	}

	public static int getIsAutomaticallyGeneratedVariant() {
		return isAutomaticallyGeneratedVariant;
	}

	public static void setIsAutomaticallyGeneratedVariant(
			int isAutomaticallyGeneratedVariant) {
		WordEntry.isAutomaticallyGeneratedVariant = isAutomaticallyGeneratedVariant;
	}

}
