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
package tsl.api;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Vector;

import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.tokenizer.Token;
import tsl.knowledge.ontology.umls.CUIStructureWrapperShort;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;

public class NamedEntityWrapper {
	private String cui = null;
	private String tui = null;
	private String text = null;
	private int start = 0;
	private int end = 0;

	public NamedEntityWrapper(String cui, String tui, String text, int start,
			int end) {
		this.cui = cui;
		this.tui = tui;
		this.text = text;
		this.start = start;
		this.end = end;
	}

	public static ArrayList<NamedEntityWrapper> getNamedEntities(String text) {
		ArrayList<NamedEntityWrapper> results = new ArrayList();
		Document document = new Document(text);
		document.analyzeSentencesNoHeader();
		if (document.getAllSentences() != null) {
			for (Sentence sentence : document.getAllSentences()) {
				Vector<Token> wordTokens = sentence.gatherWordTokens();
				Vector cws = UMLSStructuresShort.getUMLSStructures()
						.getCUIStructureWrappers(wordTokens, null, true);
				if (cws != null) {
					for (ListIterator li = cws.listIterator(); li.hasNext();) {
						CUIStructureWrapperShort cw = (CUIStructureWrapperShort) li
								.next();
						NamedEntityWrapper wrapper = new NamedEntityWrapper(cw
								.getCuiStructure().getCui(), cw
								.getCuiStructure().getTUI(), cw.getText(),
								cw.getTextStart(), cw.getTextEnd() + 1);
						results.add(wrapper);
					}
				}
			}
		}
		return results;
	}

	public String getCui() {
		return this.cui;
	}

	public String getTui() {
		return tui;
	}

	public String getText() {
		return this.text;
	}

	public int getStart() {
		return this.start;
	}

	public int getEnd() {
		return this.end;
	}

	public String toString() {
		String str = "<CUI=" + this.cui + ",Text=\"" + this.text + "\",Start="
				+ this.start + ",End=" + this.end + ">";
		return str;
	}

}
