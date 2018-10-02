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
package moonstone.workfromhome.ontology.owl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

//import edu.utah.blulab.domainontology.DomainOntology;
//import edu.utah.blulab.domainontology.LexicalItem;
//import edu.utah.blulab.domainontology.LogicExpression;
//import edu.utah.blulab.domainontology.Modifier;
//import edu.utah.blulab.domainontology.NumericModifier;
//import edu.utah.blulab.domainontology.Term;
//import edu.utah.blulab.domainontology.Variable;
//import edu.utah.blulab.domainontology.DomainOntology;
//import edu.utah.blulab.domainontology.LexicalItem;
//import edu.utah.blulab.domainontology.LogicExpression;
//import edu.utah.blulab.domainontology.Modifier;
//import edu.utah.blulab.domainontology.NumericModifier;
//import edu.utah.blulab.domainontology.Term;
//import edu.utah.blulab.domainontology.Variable;
//import edu.utah.blulab.domainontology.DomainOntology;
//import edu.utah.blulab.domainontology.LexicalItem;
//import edu.utah.blulab.domainontology.LogicExpression;
//import edu.utah.blulab.domainontology.Modifier;
//import edu.utah.blulab.domainontology.NumericModifier;
//import edu.utah.blulab.domainontology.Term;
//import edu.utah.blulab.domainontology.Variable;
import moonstone.grammar.Grammar;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.utilities.ListUtils;
import tsl.utilities.TimeUtils;
import tsl.utilities.VUtils;

public class ExtractOWLOntology {
//	private DomainOntology domain = null;
//	private MoonstoneRuleInterface moonstoneRuleInterface = null;
//	private Vector<Rule> newrules = null;
//
//	public ExtractOWLOntology(MoonstoneRuleInterface msri) {
//		this.moonstoneRuleInterface = msri;
//	}
//
//	public void setDomain(String uri) {
//		try {
//			this.domain = new DomainOntology(uri, false);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void playAround() {
//
//	}
//	
//	public void printGrammarRules() {
//		if (this.newrules != null) {
//			for (Rule rule : this.newrules) {
//				System.out.println(rule.getSexp().toNewlinedString() + "\n");
//			}
//		}
//	}
//
//	public void analyze() {
//		KnowledgeBase kb = KnowledgeEngine.getCurrentKnowledgeEngine().getCurrentKnowledgeBase();
//		ArrayList<Variable> domainVariables = domain.getAllVariables();
//		Grammar grammar = this.moonstoneRuleInterface.getSentenceGrammar();
//		char[] trimchars = new char[] { '[', ']' };
//		String timestr = TimeUtils.getDateTimeString();
//		String rname = null;
//		int x = 1;
//
//		ArrayList<Modifier> modifierDictionary = null;
//		try {
//			modifierDictionary = domain.createModifierDictionary();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		// System.out.println("********** Domain Variables: **********");
//		this.extractTypeConstants(domainVariables);
//
//		for (Modifier modifier : modifierDictionary) {
//			String modname = modifier.getModName();
//			String sconstname = this.extractStringConstantName(modname);
//			if (":NOTRETRIEVED:".equals(sconstname)) {
//				x = 1;
//			}
//			StringConstant msc = StringConstant.createStringConstant(sconstname, null);
//			Vector patternList = this.extractPatternList(modifier);
//			if (patternList != null && !patternList.isEmpty()) {
//				Vector<Vector> wlst = VUtils.listify(patternList);
//				rname = "OWL-MODIFIER-" + modname + "-" + timestr;
//				Rule rule = this.extractGrammarRule(rname, msc, wlst);
//			} else {
//				x = 1;
//			}
//		}
//
//		ArrayList<Term> targetDictionary = domain.createAnchorDictionary();
//		for (Term target : targetDictionary) {
//
//			Object o1 = target.getSubjExp();
//			Object o2 = target.getAbbreviation();
//			Object o3 = target.getAllParents();
//			Object o4 = target.getPrefCode();
//			Object o5 = target.getSemanticType();
//			ArrayList<String> synonyms = target.getSynonym();
//			Object o7 = target.getURI();
//
//			String prefcode = target.getPrefCode();
//			String prefterm = target.getPrefTerm();
//
//			if (target.getSemanticType().size() > 0) {
//				String tname = (String) target.getSemanticType().get(0);
//				String stname = this.extractShortTypeName(tname);
//
//				TypeConstant tc = TypeConstant.createTypeConstant(stname);
//				String scname = this.extractStringConstantName(prefterm);
//				StringConstant asc = StringConstant.createStringConstant(scname, tc);
//				asc.setCui(prefcode);
//
//				Vector patternList = this.extractPatternList(target);
//				if (patternList != null) {
//					Vector<Vector> wlst = VUtils.listify(patternList);
//					rname = "OWL-TARGET-" + tname + "-" + timestr;
//					this.extractGrammarRule(rname, asc, wlst);
//				}
//			} else {
//				x = 1;
//			}
//		}
//
//		for (Variable var : domainVariables) {
//			String vname = var.getVarName();
//			String sconststr = this.extractStringConstantName(vname);
//			StringConstant sc = this.extractStringConstant(var);
//			x = 1;
//			for (LogicExpression<Term> le : var.getAnchor()) {
//				for (Term term : le) {
//					ArrayList<String> semanticTypes = term.getSemanticType();
//					if (semanticTypes != null && semanticTypes.size() > 0) {
//						String stypestr = semanticTypes.get(0);
//						TypeConstant stype = TypeConstant.createTypeConstant(stypestr);
//						Vector patternList = this.extractPatternList(term);
//						if (patternList != null) {
//							Vector<Vector> wlst = VUtils.listify(patternList);
//							rname = "OWL-VARIABLE-" + vname + "-" + timestr;
//							this.extractGrammarRule(rname, sc, wlst);
//						}
//					}
//				}
//			}
//
//			// ArrayList<NumericModifier> nmos = var.getNumericModifiers();
//
//			if (!var.getModifiers().isEmpty()) {
//				int len = var.getModifiers().size();
//				for (int i = 0; i < var.getModifiers().size(); i++) {
//					LogicExpression<Modifier> mod = var.getModifiers().get(i);
//					for (int j = 0; j < mod.size(); j++) {
//						Modifier cmod = mod.get(j);
//						String modname = cmod.getModName();
//						String scmodname = this.extractStringConstantName(modname);
//						Vector<Vector> wlsts = null;
//
//						// Has this already been created?
//						// Vector patternList = this.extractPatternList(cmod);
//						// wlsts = VUtils.listify(patternList);
//						// this.extractGrammarRule("", sc, wlsts);
//
//						// 7/28/2016
//						StringConstant msc = kb.getNameSpace().getStringConstant(scmodname);
//						if (sc != null && msc != null) {
//							Vector v1 = VUtils.listify(msc);
//							Vector v2 = VUtils.listify(sc);
//							wlsts = VUtils.listify(v1, v2);
//							rname = "OWL-VARIABLE-" + vname + "-MODIFIER-" + modname + "-" + timestr;
//							this.extractGrammarRule(rname, sc, wlsts);
//						}
//					}
//				}
//			}
//
//			if (!var.getNumericModifiers().isEmpty()) {
//				int len = var.getNumericModifiers().size();
//				for (int i = 0; i < var.getNumericModifiers().size(); i++) {
//					NumericModifier nmod = var.getNumericModifiers().get(i);
//					System.out.println("Numeric Mod:" + nmod.getModName());
//				}
//			}
//
//		}
//	}
//
//	private void extractTypeConstants(ArrayList<Variable> domainVariables) {
//		for (Variable var : domainVariables) {
//			ArrayList<String> parents = var.getAllParents();
//			String firstpname = this.extractShortTypeName(parents.get(0));
//			TypeConstant firstParentType = TypeConstant.createTypeConstant(firstpname);
//			TypeConstant lastType = null;
//			for (String parent : parents) {
//				String pname = this.extractShortTypeName(parent);
//				TypeConstant tc = TypeConstant.createTypeConstant(pname);
//				if (lastType != null) {
//					lastType.addParent(tc);
//				}
//				lastType = tc;
//			}
//			if (lastType != null) {
//				TypeConstant medthing = TypeConstant.createTypeConstant("medical_thing");
//				lastType.addParent(medthing);
//			}
//		}
//	}
//
//	private StringConstant extractStringConstant(Variable var) {
//		String varname = var.getVarName();
//		String vscname = this.extractStringConstantName(varname);
//		ArrayList<String> parents = var.getAllParents();
//		String firstpname = this.extractShortTypeName(parents.get(0));
//		TypeConstant tc = TypeConstant.createTypeConstant(firstpname);
//		HashMap<String, Variable> relationMap = var.getRelationships();
//		ArrayList<LogicExpression<Modifier>> mos = var.getModifiers();
//		if (!mos.isEmpty()) {
//			for (LogicExpression le : mos) {
//				int x = 1;
//			}
//		}
//		ArrayList<NumericModifier> nmos = var.getNumericModifiers();
//
//		ArrayList<Variable> directParents = var.getDirectParents();
//		ArrayList<Variable> directChildren = var.getDirectParents();
//		StringConstant sc = StringConstant.createStringConstant(vscname, tc);
//		return sc;
//	}
//
//	private Vector<Vector<String>> extractPatternList(Modifier modifier) {
//		String modname = modifier.getModName();
//		ArrayList<Modifier> parents = modifier.getDirectParents();
//		ArrayList<Modifier> children = modifier.getDirectChildren();
//		LogicExpression<LogicExpression<Modifier>> moddef = modifier.getDefaultDefintion();
//		ArrayList<String> wlist = new ArrayList();
//		Vector newv = null;
//		for (LexicalItem li : modifier.getItems()) {
//			String pt = li.getPrefTerm();
//			ArrayList sy = li.getSynonym();
//			ArrayList ms = li.getMisspelling();
//			if (pt != null && pt.length() > 0) {
//				String cname = pt.getClass().getName();
//				wlist = (ArrayList) ListUtils.add(wlist, li.getPrefTerm());
//			}
//			if (!sy.isEmpty()) {
//				wlist = (ArrayList) ListUtils.appendIfNot(wlist, li.getSynonym());
//			}
//			if (!ms.isEmpty()) {
//				wlist = (ArrayList) ListUtils.appendIfNot(wlist, li.getMisspelling());
//			}
//		}
//		if (!wlist.isEmpty()) {
//			Vector v = VUtils.listToVector(wlist);
//			v = VUtils.flatten(v);
//			newv = new Vector(0);
//			for (String str : (Vector<String>) v) {
//				newv.add(str.toLowerCase());
//			}
//		}
//		return newv;
//	}
//
//	private Vector<Vector<String>> extractPatternList(Term term) {
//		ArrayList<String> wlist = new ArrayList();
//		wlist = (ArrayList) ListUtils.add(wlist, term.getPrefTerm());
//		wlist = (ArrayList) ListUtils.appendIfNot(wlist, term.getSynonym());
//		wlist = (ArrayList) ListUtils.appendIfNot(wlist, term.getMisspelling());
//		Vector v = VUtils.listToVector(wlist);
//		v = VUtils.flatten(v);
//		Vector newv = new Vector(0);
//		for (String str : (Vector<String>) v) {
//			newv = VUtils.addIfNot(newv, str.toLowerCase());
//		}
//		return newv;
//	}
//
//	private moonstone.rule.Rule extractGrammarRule(String ruleID, StringConstant sc, Vector<Vector> wlsts) {
//		moonstone.rule.Rule newrule = null;
//		if (sc != null) {
//			newrule = new moonstone.rule.Rule();
//			newrule.setRuleID(ruleID);
//			newrule.setType(sc.getType());
//			newrule.setResultConcept(sc);
//			newrule.setPatternLists(wlsts);
//			Sexp sexp = newrule.toSexp(false);
//			newrule.setSexp(sexp);
//			this.newrules = VUtils.add(this.newrules, newrule);
//			System.out.println(sexp.toNewlinedString() + "\n\n");
//		}
//		return newrule;
//	}
//
//	private String extractStringConstantName(String cname) {
//		cname = cname.toUpperCase().trim();
//		String str = ":";
//		for (int i = 0; i < cname.length(); i++) {
//			char c = cname.charAt(i);
//			if (!Character.isLetterOrDigit(c)) {
//				str += '_';
//			} else {
//				str += c;
//			}
//		}
//		str += ':';
//		return str;
//	}
//
//	private String extractShortTypeName(String uri) {
//		int index = uri.lastIndexOf('#');
//		if (index > 0) {
//			String shortname = uri.substring(index + 1);
//			return shortname;
//		}
//		return uri;
//	}

}
