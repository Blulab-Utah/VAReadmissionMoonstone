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
package moonstone.rulebuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

//import edu.stanford.smi.protegex.owl.ProtegeOWL;
//import edu.stanford.smi.protegex.owl.model.OWLModel;
//import edu.stanford.smi.protegex.owl.model.RDFProperty;
//import edu.stanford.smi.protegex.owl.model.RDFResource;
//import edu.stanford.smi.protegex.owl.model.RDFSClass;
//import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
//import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFProperty;

import moonstone.rule.Rule;

import tsl.expression.term.relation.PatternRelationSentence;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.ontology.Ontology;
import tsl.knowledge.ontology.TypeRelationSentence;
import tsl.tsllisp.Sexp;
import tsl.utilities.FUtils;
import tsl.utilities.VUtils;

public class RuleGeneration {

	private Vector<Rule> allRules = null;
	private Hashtable<Vector<TypeConstant>, Vector<TypeRelationSentence>> allArgumentSentenceHash = new Hashtable();
	private Hashtable<TypeConstant, Vector<TypeRelationSentence>> singleArgumentSentenceHash = new Hashtable();
	private static RuleGeneration RG = new RuleGeneration();

	// public static void main(String[] args) {
	// printHomelessnessLexicon();
	// }

	public static void main(String[] args) {
		try {
			// OWLOntologyManager manager =
			// OWLManager.createOWLOntologyManager();
			// IRI iri = IRI
			// .create("http://blulab.chpc.utah.edu/ontologies/SchemaOntology.owl");
			// OWLOntology ontology = manager
			// .loadOntologyFromOntologyDocument(iri);
			//
			// OWLDataFactory factory = manager.getOWLDataFactory();

			KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine();
			Ontology ontology = new Ontology("owl");
			ke.setCurrentOntology(ontology);

//			RG.extractOWLOntology(
//					"/Users/leechristensen/Desktop/MelissaOntologies/RDF/SchemaOntology.rdf",
//					ontology);
//			RG.extractOWLOntology(
//					"/Users/leechristensen/Desktop/MelissaOntologies/RDF/ModifierOntology.rdf",
//					ontology);

			Vector<Rule> rules = RG.extractRulesFromOntology(ontology);

			if (rules != null) {
				for (Rule rule : rules) {
					String lstr = rule.getSexp().toNewlinedString(1);
					lstr = "\n\n" + lstr;
					System.out.println(lstr);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public void extractOWLOntology(String filename, Ontology ontology) {
//		try {
//			File file = new File(filename);
//			FileReader reader = new FileReader(file);
//			OWLModel model = ProtegeOWL.createJenaOWLModelFromReader(reader);
//			RDFSClass root = (RDFSClass) model.getRootCls();
//			Vector<RDFSClass> classes = null;
//			for (Iterator it = root.getSubclasses(true).iterator(); it
//					.hasNext();) {
//				Object o = it.next();
//				if (!(o instanceof RDFSClass)) {
//					continue;
//				}
//				RDFSClass sc = (RDFSClass) o;
//				String fullName = sc.getName();
//				String shortName = extractShortName(fullName);
//				if (!fullName.contains("blulab")) {
//					continue;
//				}
//				classes = VUtils.add(classes, sc);
//				TypeConstant type = TypeConstant.createTypeConstant(shortName,
//						fullName);
//				type.setOntology(ontology);
//				Collection c = sc.getOwnSlots();
//				if (c != null) {
//					for (Iterator i = c.iterator(); i.hasNext();) {
//						o = i.next();
//						if (o instanceof DefaultRDFProperty) {
//							DefaultRDFProperty p = (DefaultRDFProperty) o;
//							String pname = p.getName();
//							if (p.getName().contains("blulab")) {
//								Collection values = (Collection) sc
//										.getPropertyValues(p);
//								if (values != null && !values.isEmpty()) {
//									for (Iterator pi = values.iterator(); pi
//											.hasNext();) {
//										Object value = pi.next();
//										String vname = value.toString();
//										if (pname.contains("altLabel")
//												|| pname.contains("prefLabel")
//												|| pname.contains("abrLabel")) {
//											type.addMoonstoneLabel(vname
//													.toLowerCase());
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			if (classes != null) {
//				for (RDFSClass sc : classes) {
//					TypeConstant type = TypeConstant
//							.getType(extractShortName(sc.getName()));
//					if (type != null) {
//						Collection c = sc.getSuperclasses(false);
//						for (Iterator sci = c.iterator(); sci.hasNext();) {
//							RDFSClass psc = (RDFSClass) sci.next();
//							String parentName = extractShortName(psc.getName());
//							TypeConstant ptype = TypeConstant
//									.getType(parentName);
//							if (ptype != null) {
//								type.addParent(ptype);
//							}
//						}
//					}
//				}
//			}
//
//			Collection props = model.getRDFProperties();
//			for (Iterator it = props.iterator(); it.hasNext();) {
//				RDFProperty rp = (RDFProperty) it.next();
//				String rfn = rp.getName();
//				String rsn = extractShortName(rfn);
//
//				if (!rfn.contains("blulab")) {
//					continue;
//				}
//
//				RelationConstant rc = RelationConstant
//						.createRelationConstant(rsn);
//				for (Iterator i = rp.getSuperproperties(false).iterator(); i
//						.hasNext();) {
//					RDFProperty parent = (RDFProperty) i.next();
//					String prfn = parent.getName();
//					String prsn = extractShortName(prfn);
//					RelationConstant prc = RelationConstant
//							.createRelationConstant(prsn);
//					if (prc != null && !prc.equals(rc)) {
//						rc.addParent(prc);
//					}
//				}
//
//				TypeConstant subject = null;
//				TypeConstant modifier = null;
//				RDFSClass domain = rp.getDomain(true);
//				if (domain != null) {
//					String sn = extractShortName(domain.getName());
//					subject = TypeConstant.findByName(sn);
//				}
//				RDFResource range = rp.getRange(true);
//				if (range != null) {
//					String sn = extractShortName(range.getName());
//					modifier = TypeConstant.findByName(sn);
//				}
//				if (modifier == null && range instanceof RDFSDatatype) {
//					RDFSDatatype dt = (RDFSDatatype) range;
//					String name = extractShortName(dt.getName());
//					if (Character.isLetter(name.charAt(0))) {
//						modifier = TypeConstant.createTypeConstant(name, name);
//					}
//				}
//				if (subject != null && modifier != null) {
//					TypeRelationSentence trs = new TypeRelationSentence(rc,
//							subject, modifier);
//					this.storeTypeRelationSentence(trs);
//					ontology.addSentence(trs);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public Vector<TypeRelationSentence> getAbstractTypeRelationSentences() {
		Vector<TypeRelationSentence> trsv = null;
		for (Enumeration e = this.allArgumentSentenceHash.keys(); e
				.hasMoreElements();) {
			Object key = e.nextElement();
			Vector<TypeRelationSentence> v = this.allArgumentSentenceHash
					.get(key);
			trsv = VUtils.append(trsv, v);
		}
		return trsv;
	}

	private String extractShortName(String fullName) {
		int index = -1;
		String shortName = fullName;
		if ((index = fullName.indexOf("#")) > 0) {
			shortName = fullName.substring(index + 1);
		}
		return shortName;
	}

	public void storeTypeRelationSentence(TypeRelationSentence trs) {
		VUtils.pushHashVector(this.singleArgumentSentenceHash,
				trs.getSubjectType(), trs);
		VUtils.pushHashVector(this.singleArgumentSentenceHash,
				trs.getModifierType(), trs);
		Vector<TypeConstant> key = new Vector(0);
		key.add(trs.getSubjectType());
		key.add(trs.getModifierType());
		Vector<TypeRelationSentence> trsv = this.allArgumentSentenceHash
				.get(key);
		if (trsv == null) {
			VUtils.pushHashVector(this.allArgumentSentenceHash, key, trs);
		} else {
			TypeRelationSentence ancestor = null;
			TypeRelationSentence descendant = null;
			for (TypeRelationSentence otrs : trsv) {
				if (otrs.getRelation().isAncestor(trs.getRelation())) {
					ancestor = otrs;
				} else if (trs.getRelation().isAncestor(otrs.getRelation())) {
					descendant = otrs;
				}
			}
			if (descendant != null) {
				trsv.remove(descendant);
			}
			if (ancestor == null) {
				trsv.add(trs);
			}
		}
	}

	public TypeConstant getRelationLevelType(TypeConstant type) {
		if (this.singleArgumentSentenceHash.get(type) != null) {
			return type;
		}
		if (type.getParents() != null) {
			return getRelationLevelType((TypeConstant) type.getParents().firstElement());
		}
		return null;
	}

	public Vector<Rule> extractRulesFromOntology(Ontology ontology) {
		Vector<Rule> rules = null;
		if (ontology.getAllTypeConstants() != null) {
			for (TypeConstant type : ontology.getAllTypeConstants()) {
				if (type.getMoonstoneLabels() != null) {
					Rule rule = generateRuleFromType(type);
					rules = VUtils.add(rules, rule);
				}
			}
		}
		Vector<TypeRelationSentence> trsv = getAbstractTypeRelationSentences();
		if (trsv != null) {
			for (TypeRelationSentence trs : trsv) {
				Rule rule = generateRuleFromTypeRelationSentence(trs);
				rules = VUtils.add(rules, rule);
			}
		}
		return rules;
	}

	public Rule generateRuleFromType(TypeConstant type) {
		Rule rule = null;
		if (type != null && type.getMoonstoneLabels() != null) {
			rule = new Rule();
			String ruleid = type.getName() + "_rule";
			rule.setRuleID(ruleid);
			Vector<Vector> wlists = VUtils.listify(type
					.getMoonstoneLabels());
			rule.setPatternLists(wlists);
			rule.setType(type);
			TypeConstant gtype = this.getRelationLevelType(type);
			if (gtype == null && type.getParents() != null) {
				gtype = (TypeConstant) type.getParents().firstElement();
			}
			if (gtype != null) {
				rule.setGeneralType(gtype);
			}
			Sexp sexp = rule.toSexp();
			rule.setSexp(sexp);
			allRules = VUtils.add(allRules, rule);
			String lstr = rule.getSexp().toNewlinedString(0);
			lstr = "\n\n" + lstr;
			System.out.println(lstr);
		}
		return rule;
	}

	public Rule generateRuleFromTypeRelationSentence(TypeRelationSentence trs) {
		Rule rule = new Rule();
		String ruleid = convertTRSToRuleID(trs);
		rule.setRuleID(ruleid);
		String sstr = "<" + trs.getSubject().getName().toUpperCase() + ">";
		String mstr = "<" + trs.getModifier().getName().toUpperCase() + ">";
		Vector<Vector> v = new Vector(0);
		v.add(VUtils.listify(sstr));
		v.add(VUtils.listify(mstr));
		Vector<Vector> plists = v;
		rule.setPatternLists(plists);
		PatternRelationSentence prs = new PatternRelationSentence(trs, "?0",
				"?1");
		rule.addSemanticRelation(prs);
		Sexp sexp = rule.toSexp();
		rule.setSexp(sexp);
		String lstr = rule.getSexp().toNewlinedString(0);
		lstr = "\n\n" + lstr;
		System.out.println(lstr);
		return rule;
	}

	private String convertTRSToRuleID(TypeRelationSentence trs) {
		StringBuffer sb = new StringBuffer();
		String trsstr = trs.toShortString();
		for (int i = 0; i < trsstr.length(); i++) {
			char c = trsstr.charAt(i);
			if (Character.isLetter(c)) {
				sb.append(c);
			}
		}
		sb.append(":rule");
		return sb.toString();
	}

	public void printHomelessnessLexicon() {
		File f = FUtils.chooseFile("/", "Homelessness Lexicon File:");
		if (f.exists()) {
			try {
				int ruleindex = 0;
				Hashtable<String, Vector<String>> categoryHash = new Hashtable();
				Hashtable<String, Vector<String>> conceptHash = new Hashtable();
				Hashtable<String, String> cuiHash = new Hashtable();
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = in.readLine()) != null) {
					String[] strs = line.split("\\|");
					String words = null, category = null, cui = null, concept = null, tui = null;
					if (strs.length >= 7) {
						words = strs[1];
						category = strs[6];
					}
					if (strs.length >= 8) {
						cui = strs[7];
					}
					if (strs.length >= 9) {
						concept = strs[8];
					}
					if (strs.length >= 10) {
						tui = strs[9];
					}
					if (concept != null) {
						VUtils.pushIfNotHashVector(categoryHash, category,
								concept);
						VUtils.pushIfNotHashVector(conceptHash, concept, words);
						if (cui != null) {
							cuiHash.put(concept, cui);
						}
					}
				}
				for (Enumeration<String> e = categoryHash.keys(); e
						.hasMoreElements();) {
					String category = e.nextElement();
					Vector<String> concepts = categoryHash.get(category);
					for (String concept : concepts) {
						Vector<String> words = conceptHash.get(concept);
						concept = getConceptName(concept);
						String rid = "homelessness-rule-" + ruleindex++;
						String rstr = "((ruleid " + rid + ")\n  "
								+ "(stype \"<" + category.toUpperCase()
								+ ">\")\n" + "  (concept \"" + concept
								+ "\")\n  (words (";
						for (String word : words) {
							rstr += "\"" + word.toLowerCase() + "\" ";
						}
						rstr += ")))\n";
						System.out.println(rstr);
					}
				}
				for (Enumeration<String> e = conceptHash.keys(); e
						.hasMoreElements();) {
					String concept = e.nextElement();
					concept = getConceptName(concept);
					String cui = cuiHash.get(concept);
					if (cui == null) {
						cui = getCUIName(concept);
					}
					String cstr = "(\"" + concept + "\" \"" + cui + "\")";
					System.out.println(cstr);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getConceptName(String cstr) {
		cstr = cstr.toUpperCase();
		String newcstr = ":";
		for (int i = 0; i < cstr.length(); i++) {
			char c = cstr.charAt(i);
			if (Character.isWhitespace(c)) {
				newcstr += "_";
			} else if (Character.isLetter(c)) {
				newcstr += c;
			}
		}
		newcstr += ":";
		return newcstr;
	}

	public String getCUIName(String cstr) {
		cstr = cstr.toUpperCase();
		String newcstr = "X_";
		for (int i = 0; i < cstr.length(); i++) {
			char c = cstr.charAt(i);
			if (Character.isWhitespace(c)) {
				newcstr += "_";
			} else if (Character.isLetter(c)) {
				newcstr += c;
			}
		}
		return newcstr;
	}

}
