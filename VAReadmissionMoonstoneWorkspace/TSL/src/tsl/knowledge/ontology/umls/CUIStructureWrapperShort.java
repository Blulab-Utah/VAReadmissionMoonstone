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
package tsl.knowledge.ontology.umls;

import java.util.Comparator;
import java.util.Vector;

import tsl.documentanalysis.tokenizer.Token;
import tsl.utilities.SetUtils;
import tsl.utilities.VUtils;

public class CUIStructureWrapperShort {

	CUIStructureShort cuiStructure = null;

	int textStart = -1;

	int textEnd = -1;

	int tokenStart = -1;

	int tokenEnd = -1;

	int textLength = -1;

	Vector<Token> tokens = null;

	String text = null;

	boolean visited = false;

	public CUIStructureWrapperShort(CUIStructureShort cs, Vector<Token> tokens,
			int tokenStart, int tokenEnd) {
		this.cuiStructure = cs;
		this.tokens = tokens;
		this.textStart = tokens.firstElement().getStart();
		this.textEnd = tokens.lastElement().getEnd();
		this.textLength = this.textEnd - this.textStart;
		this.tokenStart = tokenStart;
		this.tokenEnd = tokenEnd;
		if (tokens.firstElement().getDocument() != null) {
			this.text = tokens.firstElement().getDocument().getText()
					.substring(this.textStart, this.textEnd + 1);
		}
	}

	public String toString() {
		return "<" + this.cuiStructure + "[" + this.textStart + "-"
				+ this.textEnd + "]>";
	}

	public int hashCode() {
		return this.cuiStructure.hashCode() | this.textStart | this.textEnd;
	}

	public boolean equals(Object o) {
		if (o instanceof CUIStructureWrapperShort) {
			CUIStructureWrapperShort cpw = (CUIStructureWrapperShort) o;
			return (this.cuiStructure.equals(cpw.cuiStructure)
					&& this.textStart == cpw.textStart && this.textEnd == cpw.textEnd);
		}
		return false;
	}

	public boolean covers(CUIStructureWrapperShort wrapper) {
		if (this != wrapper && this.textStart <= wrapper.textStart
				&& this.textEnd >= wrapper.textEnd
				&& this.tokens.size() >= wrapper.tokens.size()) {
			return true;
		}
		return false;
	}

	public static Vector<CUIStructureWrapperShort> getSmallerCoveredEquivalentWrappers(
			CUIStructureWrapperShort cpw, Vector<CUIStructureWrapperShort> cpws) {
		Vector<CUIStructureWrapperShort> covered = null;
		if (cpw != null && cpws != null) {
			for (CUIStructureWrapperShort ocpw : cpws) {
				if (cpw.coversSmallerTokenEquivalent(ocpw)) {
					covered = VUtils.add(covered, ocpw);
				}
			}
		}
		return covered;
	}

	// E.g. "right and left lower lobe" covers "right lower lobe", but
	// equivalently
	// covers the smaller wrapper for "left lower lobe"
	public boolean coversSmallerTokenEquivalent(CUIStructureWrapperShort wrapper) {
		if (this.covers(wrapper)
				&& this.getTextLength() > wrapper.getTextLength()
				&& this.getTokens().size() == wrapper.getTokens().size()
				&& SetUtils.intersects(this.getTokens(), wrapper.getTokens())
				&& !SetUtils.same(this.getTokens(), wrapper.getTokens())) {
			return true;
		}
		return false;
	}
	
	public CUIStructureShort getCuiStructure() {
		return cuiStructure;
	}

	public void setCuiStructure(CUIStructureShort cuiStructure) {
		this.cuiStructure = cuiStructure;
	}

	public int getTextStart() {
		return textStart;
	}

	public void setTextStart(int textStart) {
		this.textStart = textStart;
	}

	public int getTextEnd() {
		return textEnd;
	}

	public void setTextEnd(int textEnd) {
		this.textEnd = textEnd;
	}

	public int getTokenStart() {
		return tokenStart;
	}

	public void setTokenStart(int tokenStart) {
		this.tokenStart = tokenStart;
	}

	public int getTokenEnd() {
		return tokenEnd;
	}
	
	public int getNumTokens() {
		return this.tokenEnd - this.tokenStart + 1;
	}

	public void setTokenEnd(int tokenEnd) {
		this.tokenEnd = tokenEnd;
	}

	public int getTextLength() {
		return textLength;
	}

	public void setTextLength(int length) {
		this.textLength = length;
	}

	public Vector<Token> getTokens() {
		return tokens;
	}

	public void setTokens(Vector<Token> tokens) {
		this.tokens = tokens;
	}

	public String getText() {
		return text;
	}
	
	public Token getFirstToken() {
		if (this.tokens != null) {
			return this.tokens.firstElement();
		}
		return null;
	}
	
	public Token getLastToken() {
		if (this.tokens != null) {
			return this.tokens.lastElement();
		}
		return null;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public static void resetVisited(Vector<CUIStructureWrapperShort> wrappers) {
		if (wrappers != null) {
			for (CUIStructureWrapperShort wrapper : wrappers) {
				wrapper.setVisited(false);
			}
		}
	}
	
	public static class TextPositionSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			CUIStructureWrapperShort cp1 = (CUIStructureWrapperShort) o1;
			CUIStructureWrapperShort cp2 = (CUIStructureWrapperShort) o2;
			if (cp1.textStart < cp2.textStart) {
				return -1;
			}
			if (cp1.textStart > cp2.textStart) {
				return 1;
			}
			return 0;
		}
	}

	public static class WordLengthSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			CUIStructureWrapperShort cw1 = (CUIStructureWrapperShort) o1;
			CUIStructureWrapperShort cw2 = (CUIStructureWrapperShort) o2;
			if (cw1.getCuiStructure().getWords().length > cw2.getCuiStructure()
					.getWords().length) {
				return -1;
			}
			if (cw2.getCuiStructure().getWords().length < cw1.getCuiStructure()
					.getWords().length) {
				return 1;
			}
			return 0;
		}
	}


}
