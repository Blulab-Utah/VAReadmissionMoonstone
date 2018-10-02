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
package moonstone.learning.ncbo;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

//import com.hp.hpl.jena.graph.query.Domain;

import tsl.documentanalysis.lexicon.Lexicon;
import tsl.documentanalysis.lexicon.Word;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class MSConcept {
	String conceptID = null;
	String preferredName = null;
	String fullID = null;
	String ontologyID = null;
	Vector<String> semanticTypes = null;
	int from = 0;
	int to = 0;
	Vector<String> synonyms = null;
	Vector<CUIStructureShort> cuiStructures = null;
//	Domain domain = null;

	public MSConcept(String cid, String pname) {
		this.conceptID = cid;
		this.preferredName = pname;
	}

	public MSConcept(String cid, String fid, String oid,
			String pname, Vector<String> stypes, Vector<String> synonyms,
			int from, int to) {
		this.conceptID = cid.toLowerCase();
		this.fullID = fid.toLowerCase();
		this.ontologyID = oid.toLowerCase();
		this.preferredName = pname.toLowerCase();
		for (String stype : stypes) {
			stype = stype.toLowerCase();
		}
		if (synonyms != null) {
			for (String synonym : synonyms) {
				synonym = synonym.toLowerCase();
			}
		}
		this.semanticTypes = stypes;
		this.synonyms = synonyms;
		this.from = from - 1;
		this.to = to - 1;
	}

	public int addCUIStructures() {
		Vector<Vector<Word>> wordLists = null;
		int count = 0;
		Vector<String> v = StrUtils.stringList(this.preferredName, ' ');
		Vector<Word> words = Lexicon.currentLexicon.getWords(v);
		wordLists = VUtils.add(wordLists, words);
		UMLSStructuresShort us = UMLSStructuresShort.getUMLSStructures();
		if (this.synonyms != null) {
			for (String synonym : this.synonyms) {
				v = StrUtils.stringList(synonym, ' ');
				words = Lexicon.currentLexicon.getWords(v);
				wordLists = VUtils.add(wordLists, words);
			}
		}
		for (Vector<Word> ws : wordLists) {
			Collections.sort(ws, new Word.WordSorter());
			Vector<CUIStructureShort> cps = us.getCoveringCUIStructures(ws);
			if (cps == null) {
				String tstr = this.getSemanticTypes().firstElement();
				String typeName = this.getOntologyID() + ":" + tstr;
				TypeConstant tc = TypeConstant.createTypeConstant(typeName);
				CUIStructureShort cp = new CUIStructureShort(us, ws,
						null, null, null);
				cps = VUtils.listify(cp);
				this.cuiStructures = VUtils.append(this.cuiStructures, cps);
				count++;
			}
		}
		return count;
	}

	protected void addToHash(Hashtable hash, Vector<MSConcept> concepts) {
		if (!this.existsInHash(hash)) {
			hash.put(this.getConceptID(), this);
			hash.put(this.getPreferredName(), this);
			if (this.getSynonyms() != null) {
				for (String synonym : this.getSynonyms()) {
					hash.put(synonym, this);
				}
			}
			concepts.add(this);
		}
	}

	protected boolean existsInHash(Hashtable hash) {
		if (hash.get(this.conceptID) != null) {
			return true;
		}
		if (hash.get(this.getPreferredName()) != null) {
			return true;
		}
		if (this.getSynonyms() != null) {
			for (String synonym : this.getSynonyms()) {
				if (hash.get(synonym) != null) {
					return true;
				}
			}
		}
		return false;
	}

	public String toString() {
		return "<cid=" + this.conceptID + ":prefName=" + this.preferredName
				+ ":from=" + from + "to=" + to + ":synonyms=" + synonyms + ">";
	}

	public String getConceptID() {
		return conceptID;
	}

	public void setConceptID(String conceptID) {
		this.conceptID = conceptID;
	}

	public String getPreferredName() {
		return preferredName;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	public String getOntologyID() {
		return ontologyID;
	}

	public void setOntologyID(String ontologyID) {
		this.ontologyID = ontologyID;
	}

	public Vector<String> getSemanticTypes() {
		return semanticTypes;
	}

	public void setSemanticTypes(Vector<String> semanticTypes) {
		this.semanticTypes = semanticTypes;
	}

	public String getFullID() {
		return fullID;
	}

	public void setFullID(String fullID) {
		this.fullID = fullID;
	}

	public Vector<String> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(Vector<String> synonyms) {
		this.synonyms = synonyms;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

//	public Domain getDomain() {
//		return domain;
//	}
//
//	public void setDomain(Domain domain) {
//		this.domain = domain;
//	}

}
