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

import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.term.Term;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.utilities.VUtils;

public class UMLSTypeConstant extends tsl.expression.term.type.TypeConstant {

	// public UMLSTypeInfo typeInfo = null;

	private static Vector<String> conditionTUIs = VUtils.arrayToVector(new String[] { "t007", "t019", "t020", "t033",
			"t034", "t037", "t038", "t039", "t040", "t042", "t046", "t047", "t048", "t050", "t059", "t060", "t061",
			"t067", "t074", "t162", "t163", "t182", "t184", "t190", "t191", "t033", "t201" });

	private static Vector<String> locationTUIs = VUtils
			.arrayToVector(new String[] { "t017", "t021", "t022", "t023", "t024", "t029", "t030" });

	private static Vector<String> medicationTUIs = VUtils.arrayToVector(new String[] { "t121" });

	private static Vector<String> relevantTUIs = null;

	public static Vector relevantTypeNames = null;
	private static String diseaseOrSyndromeTUI = "t047";
	private static String signOrSymptomTUI = "t184";
	private static String findingTUI = "t033";
	private static String testOrResultTUI = "t034";
	private static Hashtable<String, String> conditionTUIHash = null;
	private static Hashtable<String, String> locationTUIHash = null;
	private static Hashtable<String, String> relevantTUIHash = null;
	private static Hashtable<String, String> medicationTUIHash = null;

	public static void initialize() {
		relevantTUIs = new Vector(conditionTUIs);
		relevantTUIs = VUtils.appendIfNot(relevantTUIs, locationTUIs);
		relevantTUIs = VUtils.appendIfNot(relevantTUIs, medicationTUIs);

		if (conditionTUIHash == null) {
			conditionTUIHash = new Hashtable();
			locationTUIHash = new Hashtable();
			medicationTUIHash = new Hashtable();
			relevantTUIHash = new Hashtable();
			for (String tui : conditionTUIs) {
				conditionTUIHash.put(tui, tui);
			}
			for (String tui : locationTUIs) {
				locationTUIHash.put(tui, tui);
			}
			for (String tui : medicationTUIs) {
				medicationTUIHash.put(tui, tui);
			}
			for (String tui : relevantTUIs) {
				relevantTUIHash.put(tui, tui);
			}
		}
	}

	public UMLSTypeConstant(UMLSTypeInfo typeInfo) {
		super(typeInfo.getName());
		this.setTypeInfo(typeInfo);
	}

	public UMLSTypeConstant(String name) {
		super(name);
	}

	public static UMLSTypeConstant createUMLSTypeConstant(UMLSTypeInfo typeInfo) {
		Ontology ontology = KnowledgeEngine.getCurrentKnowledgeEngine().getUMLSOntology();
		TypeConstant tc = KnowledgeBase.getCurrentKnowledgeBase().getNameSpace().getTypeConstant(typeInfo.getName());
		UMLSTypeConstant utc = null;
		// PROBLEM: UMLS TypeConstants overwrite pre-existing TCs belonging to
		// other ontologies...
		if (tc != null && tc instanceof UMLSTypeConstant) {
			utc = (UMLSTypeConstant) tc;
		} else {
			utc = new UMLSTypeConstant(typeInfo);
			ontology.addTypeConstant(utc);
		}
		if (relevantTUIHash.get(typeInfo.getUI()) != null) {
			relevantTypeNames = VUtils.addIfNot(relevantTypeNames, typeInfo.getName());
		}
		return utc;
	}

	public static UMLSTypeConstant createUMLSTypeConstant(String name) {
		UMLSTypeConstant tc = (UMLSTypeConstant) findByName(name);
		if (tc == null) {
			UMLSTypeInfo typeInfo = UMLSTypeInfo.findByName(name);
			if (typeInfo != null) {
				tc = new UMLSTypeConstant(typeInfo);
			} else {
				tc = new UMLSTypeConstant(name);
			}
		}
		return tc;
	}

	public static boolean isRelevant(String tui) {
		return relevantTUIHash.get(tui) != null;
	}

	// Before 8/15/2015
	// public static boolean isRelevant(String tui) {
	// return conditionTUIHash.get(tui) != null
	// || locationTUIHash.get(tui) != null
	// || medicationTUIHash.get(tui) != null;
	// }

	public static boolean isRelevant(TypeConstant type) {
		if (type instanceof UMLSTypeConstant) {
			UMLSTypeConstant utype = (UMLSTypeConstant) type;
			String tui = utype.getTypeInfo().getUI();
			if (utype.getTypeInfo() != null && isRelevant(tui)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCondition(String tui) {
		Hashtable h = conditionTUIHash;
		return conditionTUIHash.get(tui) != null;
	}

	public static boolean isCondition(TypeConstant type) {
		if (type instanceof UMLSTypeConstant) {
			UMLSTypeConstant utype = (UMLSTypeConstant) type;
			String tui = utype.getTypeInfo().getUI();
			if (utype.getTypeInfo() != null && isCondition(tui)) {
				return true;
			}
		}
		return false;
	}

	public static boolean bothAreConditionOrLocation(TypeConstant type1, TypeConstant type2) {
		return ((isCondition(type1) && isCondition(type2)) || (isLocation(type1) && isLocation(type2)));
	}

	public static boolean isLocation(String tui) {
		if (locationTUIHash.get(tui) != null) {
			return true;
		}
		return false;
	}

	public static boolean isLocation(TypeConstant type) {
		if (type instanceof UMLSTypeConstant) {
			UMLSTypeConstant utype = (UMLSTypeConstant) type;
			String tui = utype.getTypeInfo().getUI();
			if (utype.getTypeInfo() != null && isLocation(tui)) {
				return true;
			}
		}
		return false;
	}

	public boolean isCondition() {
		UMLSTypeConstant utype = (UMLSTypeConstant) this.getType();
		if (utype.getTypeInfo() != null && isCondition(utype)) {
			return true;
		}
		if (utype.getParents() != null) {
			for (Term ptype : this.getType().getParents()) {
				if (((TypeConstant) ptype).isCondition()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isLocation() {
		UMLSTypeConstant utype = (UMLSTypeConstant) this.getType();
		if (isLocation(utype)) {
			return true;
		}
		if (utype.getParents() != null) {
			for (Term ptype : this.getType().getParents()) {
				if (((TypeConstant) ptype).isLocation()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isRelevantUMLSCondition() {
		return isDiseaseOrSyndrome() || isSignOrSymptom() || isFinding() || isTestOrResult();
	}

	public boolean isDiseaseOrSyndrome() {
		return diseaseOrSyndromeTUI.equals(this.getTypeInfo().getUI());
	}

	public boolean isSignOrSymptom() {
		return signOrSymptomTUI.equals(this.getTypeInfo().getUI());
	}

	public boolean isFinding() {
		return findingTUI.equals(this.getTypeInfo().getUI());
	}

	public boolean isTestOrResult() {
		return testOrResultTUI.equals(this.getTypeInfo().getUI());
	}

	// public UMLSTypeInfo getTypeInfo() {
	// return typeInfo;
	// }
	//
	// public void setTypeInfo(UMLSTypeInfo typeInfo) {
	// this.typeInfo = typeInfo;
	// }

	public String toString() {
		String str = "<" + this.getName();
		if (this.getTypeInfo() != null) {
			str += ":" + this.getTypeInfo().getUI();
		}
		str += ">";
		return str;
	}

	// 6/18/2015
	public boolean isConnectedToOntology() {
		return this.getParents() != null;
	}

}
