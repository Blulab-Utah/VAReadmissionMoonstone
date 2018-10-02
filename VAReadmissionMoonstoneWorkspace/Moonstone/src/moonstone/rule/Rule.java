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
package moonstone.rule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.annotation.AnnotationIntegrationMaps;
import moonstone.annotation.WordSequenceAnnotation;
import moonstone.grammar.Grammar;
import moonstone.grammar.StructureGrammar;
import moonstone.information.Information;
import tsl.documentanalysis.lexicon.Lexicon;
import tsl.documentanalysis.lexicon.Word;
import tsl.documentanalysis.tokenizer.Token;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.relation.PatternRelationSentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.information.TSLInformation;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.knowledge.ontology.umls.CUIStructureWrapperShort;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;
import tsl.knowledge.ontology.umls.UMLSTypeInfo;
import tsl.startup.StartupParameters;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLObject;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.HUtils;
import tsl.utilities.SetUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class Rule extends Information {
	private String ruleID = "ruleid";
	private int ruleIDNum = 0;
	private int patternListCount = 0;
	private Vector<Vector> patternLists = null;
	private Vector<Vector> subpatternLists = null;
	private String resultCUI = null;
	private Object resultConcept = null;
	private String resultMacro = null;
	private Object resultType = null;
	private boolean ordered = false;
	private boolean juxtaposed = false;
	private int window = -1;
	private Vector<String> headerStrings = null;
	private int wordOnlyPatternCount = 0;
	private SyntacticTypeConstant phraseType = null;
	private Vector<Constraint> testPredicates = null;
	private Vector<Vector> propertyPredicates = null;
	private Vector<Variable> localVariables = null;
	private Vector<CUIStructureShort> cuiStructures = null;
	private Vector<RelationSentence> semanticInterpPatterns = null;
	private Vector<String> flags = null;
	private WordSequenceAnnotation relevantSentenceAnnotation = null;
	private String ruleType = null;
	private String sourceFilePath = null;
	private Hashtable<String, Vector<Integer>> ruleTokenPositionHash = new Hashtable();
	private Vector<String> stopWords = null;
	private boolean isTentative = false;
	private boolean isConjunct = false;
	private Vector<InferenceRule> inferenceRules = null;
	private Vector<PatternRelationSentence> semanticRelations = null;
	private String modifierDirection = null;
	private boolean doDebug = false;
	private String sourceID = null;
	private boolean permitInterstitialAnnotations = false;
	private boolean inhibitInterstitial = true;
	private boolean inheritProperties = true;
	private Vector<String> propertiesToRemove = null;
	private boolean usesVariables = false;
	private Sentence querySentence = null;
	private boolean automaticallyGenerated = false;
	private int patternHeadIndex = 0;
	private boolean isTerminal = false;
	private int ruleDisposition = DispositionCreateAnnotation;
	private Vector<Constraint> actions = null;
	private boolean doBalance = false;
	private Rule sourceRule = null;
	private boolean isSpecialized = false;
	private boolean isIntermediate = false;
	private boolean isComplexConcept = false;
	private String exampleSnippet = null;
	private boolean containsTargetConcept = false;
	private boolean useForwardChainingInference = false;
	private boolean isSingletonWordInput = false;
	private boolean isBagOfConceptsRule = false;
	private boolean isCaptureRule = false;
	private boolean statesTargetConceptDirectly = false;

	private static int numRules = 0;
	public static int DispositionCreateAnnotation = 1;
	public static int DispositionExecuteActions = 2;
	public static String ExcelDelimiter = "^";
	public static String ExcelPatternDelimiter = "\\^";

	public Rule() {
	}

	public Rule(Grammar grammar, Sexp sexp, Vector pattern, Vector patternLists) {
		this(grammar, sexp, pattern, patternLists, null, null);
	}

	public Rule(Grammar grammar, Sexp sexp, Vector pattern, Vector patternLists, Sexp ruleFileProperties, String filepath) {
		super(grammar, pattern);
		incrementNumRules();
		this.setSexp(sexp);
		String str = this.getStringProperty("ruleid");
		
		if (grammar.getRuleByID(str) != null) {
			System.out.println("DUPLICATE RULE: " + str + " [" + filepath + "]");
		}
		
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		this.ruleIDNum = getNumRules();
		if (str == null || "ruleid".equals(str)) {
			str = "ruleID" + this.ruleIDNum;
		}
		this.setRuleID(str);

		if (Sexp.doAssoc(sexp, "debug") != null) {
			this.doDebug = true;
		}

		if (this.doDebug) {
			int x = 1;
		}

		if (Sexp.doAssoc(sexp, "specialized") != null) {
			this.isSpecialized = true;
		}
		if (Sexp.doAssoc(sexp, "complex-concept") != null) {
			this.isComplexConcept = true;
		}
		if (Sexp.doAssoc(sexp, "intermediate") != null) {
			this.isIntermediate = true;
		}
		if (Sexp.doAssoc(sexp, "contains-target") != null) {
			this.containsTargetConcept = true;
		}
		
		if (Sexp.doAssoc(sexp, "use-inference") != null) {
			this.useForwardChainingInference = true;
		}
		
		if (Sexp.doAssoc(sexp, "states-target") != null) {
			this.statesTargetConceptDirectly = true;
		}
		
		if (patternLists == null) {
			patternLists = extractPatternLists(pattern);
		}
		this.patternLists = extractEmbeddedPatternConstants(patternLists);
		if (grammar.getRuleByID(str) != null) {
			// System.out.println(str + ": DUPLICATE RULES");
			// 10/3/2014: Temporarily disabling this as I use Topaz, which
			// permits duplicate rulenames
			// System.exit(-1);
		}

		// 6/26/2015
		Vector<Vector<String>> specialization = (Vector<Vector<String>>) this.getProperty("specialization");
		if (specialization == null) {
			specialization = (Vector<Vector<String>>) this.getProperty("subpattern");
		}
		if (specialization != null) {
			// 5/20/2016: This blocks the formation of EBL rules...
			// this.isSpecialized = true;
			this.subpatternLists = extractEmbeddedPatternConstants(specialization);
		}

		String ostr = (String) VUtils.assocValue("ontology", pattern);
		if (ostr != null) {
			this.setOntology(KnowledgeEngine.getCurrentKnowledgeEngine().findOntology(ostr));
		}
		if (VUtils.assoc("contextlexicon", pattern) != null) {
			this.readPatternListsFromFromConTextLexicon();
		}
		if (this.patternLists == null) {
			this.readPatternListsFromFile();
		}
		if (this.patternLists != null) {
			this.patternListCount = this.patternLists.size();
		}
		String ptypestr = this.getStringProperty("ptype");
		if (ptypestr != null) {
			SyntacticTypeConstant ptype = SyntacticTypeConstant.createSyntacticTypeConstant(ptypestr);
			ptype.setUsedInGrammarRulePattern(true);
			this.setPhraseType(ptype);
		}
		this.ordered = this.getBooleanProperty("ordered");
		this.juxtaposed = this.getBooleanProperty("juxtaposed");
		this.window = this.getIntProperty("window");
		this.resultCUI = this.getStringProperty("cui");

		Object c = VUtils.assocValue("concept", pattern);
		String cstr = this.getStringProperty("concept");
		this.resultConcept = Constant.extractConstant(this.getKnowledgeBase(), cstr);

		if (Variable.isVariable(this.resultConcept)) {
			this.usesVariables = true;

			// 4/22/2015: Index of head pattern element.
			int cindex = Variable.getPositionVariableIndex(this.resultConcept);
			if (cindex >= 0) {
				this.patternHeadIndex = cindex;
			}
		}

		if (this.resultConcept == null && c instanceof Vector) {
			Vector cv = (Vector) c;
			Constraint ct = Constraint.createConstraint(kb, cv);
			this.resultConcept = (ct != null ? ct : c);
			this.usesVariables = true;
		}

		// 5/7/2016
		if (this.doDebug) {
			int x = 1;
		}
		Object token = this.getProperty("stype");
		if (token != null) {
			this.resultType = Constant.extractConstant(this.getKnowledgeBase(), token);
			if (this.resultType instanceof TypeConstant) {
				this.setType((TypeConstant) this.resultType);
			} else {
				this.usesVariables = true;
			}
		}

		// Before 4/7/2016
		// this.setType(extractType(tstr));

		// if (this.getType() == null && this.resultConcept instanceof Constant)
		// {
		// this.setType(((Constant) this.resultConcept).getType());
		// }

		this.permitInterstitialAnnotations = this.getBooleanProperty("permit-interstitial");
		this.inhibitInterstitial = this.getBooleanProperty("inhibit-interstitial");
		
		if (this.inhibitInterstitial) {
			int x = 1;
		}

		if (this.resultCUI == null && this.resultConcept != null) {
			this.resultCUI = AnnotationIntegrationMaps.getCUI(this.resultConcept.toString());
		} else if (this.resultCUI != null && this.resultConcept == null) {
			this.resultConcept = AnnotationIntegrationMaps.getName(this.resultCUI, null);
		}
		this.resultMacro = extractMacro(this.getStringProperty("macro"));
		if (this.getProperty("header_strings") != null) {
			this.headerStrings = (Vector<String>) this.getProperty("header_strings");
		}
		this.flags = (Vector<String>) this.getProperty("flags");
		if (this.flags != null && this.flags.contains("conj")) {
			this.isConjunct = true;
		}
		this.stopWords = (Vector<String>) this.getProperty("stopword");

		if (this.stopWords != null) {
			for (String sword : this.stopWords) {
				this.getGrammar().addStopWord(this, sword);
			}
		}

		Vector<Vector> lvvs = (Vector) this.getProperty("localvar");
		if (lvvs != null) {
			this.usesVariables = true;
			for (Vector lvv : lvvs) {
				String vname = (String) lvv.firstElement();
				Vector lv = (Vector) lvv.elementAt(1);
				Variable var = new Variable(vname);
				Constraint pt = (Constraint) Constraint.createConstraint(kb, lv);
				Object value = (pt != null ? pt : lv);
				var.bind(value);
				this.localVariables = VUtils.add(this.localVariables, var);
			}
		}

		Vector<Vector> ptv = (Vector) this.getProperty("tests");
		if (ptv != null) {
			this.usesVariables = true;
			for (Vector<String> v : ptv) {
				Constraint pt = Constraint.createConstraint(kb, v);
				this.testPredicates = VUtils.add(this.testPredicates, pt);
			}
		}

		Vector<Vector> av = (Vector) this.getProperty("actions");
		if (av != null) {
			this.usesVariables = true;
			this.ruleDisposition = DispositionExecuteActions;
			for (Vector<String> v : av) {
				Constraint action = Constraint.createConstraint(kb, v);
				this.actions = VUtils.add(this.actions, action);
			}
		}

		Vector<Vector> avv = (Vector) this.getProperty("properties");
		if (avv != null) {
			for (Vector avpair : avv) {
				Vector pp = new Vector(0);
				String aname = (String) avpair.firstElement();
				Object o = avpair.lastElement();
				if (Variable.isVariable(o) || o instanceof Vector) {
					this.usesVariables = true;
				}
				if (o instanceof Vector) {
					Vector pv = (Vector) avpair.lastElement();
					o = Constraint.createConstraint(kb, pv);
					// e.g. "(tense (tense ?0))"
					if (o == null) {
						o = pv;
					}
				} else if (o instanceof String) {
					o = (String) o;
				}
				pp.add(aname);
				pp.add(o);
				this.propertyPredicates = VUtils.add(this.propertyPredicates, pp);
				
				// 6/25/2016: Not sure I need this...
				if ("contains-target".equals(aname)) {
					Vector targets = VUtils.flatten(this.getPatternLists());
					this.grammar.storeTargetConcepts(targets);
				}
			}
		}

		Vector<Vector> srv = (Vector<Vector>) this.getProperty("relations");
		if (srv != null) {
			this.usesVariables = true;
			for (Vector v : srv) {
				PatternRelationSentence prs = new PatternRelationSentence(v);
				this.addSemanticRelation(prs);
			}
		}

		this.isTerminal = this.getBooleanProperty("terminal");

		Vector<Vector> qrv = (Vector<Vector>) this.getProperty("query");
		if (qrv != null) {
			this.usesVariables = true;
			this.querySentence = Sentence.createSentence(qrv.elementAt(0));
			kb.initializeForm(this.querySentence);
		}

		this.inheritProperties = this.getBooleanProperty("inheritproperties");
		this.propertiesToRemove = (Vector<String>) this.getProperty("removeproperties");
		
		if (this.propertiesToRemove != null) {
			int x = 1;
		}
		
		addInferenceRules();
		extractCUIStructs();
		calculateWordOnlyPatternCount();

		// 12/10/2014, for TempEVAL
		TLObject sid = Sexp.doAssocValue(sexp, "sourceid");
		if (sid != null) {
			this.sourceID = sid.toString();
		}

		this.doBalance = this.getBooleanProperty("balance");

		// 4/3/2015: Since I am stll using RuleExpansion, which applies all
		// narrative-type constraints.
		if (grammar instanceof StructureGrammar) {
			this.ordered = true;
			this.juxtaposed = true;
			this.window = 10000;
		}

		this.exampleSnippet = this.getStringProperty("example-snippet");
		this.determineIsSingletonWordInput();
	}

	// 9/27/2015
	public Rule clone() {
		Rule clone = new Rule();
		clone.sourceRule = this;
		clone.ruleID = this.ruleID;
		clone.patternListCount = this.patternListCount;
		clone.patternLists = VUtils.clone(this.patternLists);
		clone.subpatternLists = VUtils.clone(this.subpatternLists);
		clone.resultCUI = this.resultCUI;
		clone.resultConcept = this.resultConcept;
		clone.resultMacro = this.resultMacro;
		clone.ordered = this.ordered;
		clone.juxtaposed = this.juxtaposed;
		clone.window = this.window;
		clone.isBagOfConceptsRule = this.isBagOfConceptsRule;
		clone.headerStrings = VUtils.clone(this.headerStrings);
		clone.wordOnlyPatternCount = this.wordOnlyPatternCount;
		clone.phraseType = this.phraseType;
		clone.testPredicates = this.testPredicates;
		clone.propertyPredicates = this.propertyPredicates;
		clone.localVariables = this.localVariables;
		clone.cuiStructures = this.cuiStructures;
		clone.semanticInterpPatterns = this.semanticInterpPatterns;
		clone.flags = this.flags;
		clone.relevantSentenceAnnotation = this.relevantSentenceAnnotation;
		clone.ruleType = this.ruleType;
		clone.sourceFilePath = this.sourceFilePath;
		clone.ruleTokenPositionHash = HUtils.clone(this.ruleTokenPositionHash);
		clone.stopWords = this.stopWords;
		clone.isTentative = this.isTentative;
		clone.isConjunct = this.isConjunct;
		clone.inferenceRules = this.inferenceRules;
		// Note: for tests, relations, properties, this will include a pointer
		// to the source rule's Sexps. I should write Sexp.clone().
		clone.semanticRelations = this.semanticRelations;
		clone.modifierDirection = this.modifierDirection;
		clone.doDebug = this.doDebug;
		clone.sourceID = this.sourceID;
		clone.permitInterstitialAnnotations = this.permitInterstitialAnnotations;
		clone.inhibitInterstitial = this.inhibitInterstitial;
		clone.inheritProperties = this.inheritProperties;
		clone.propertiesToRemove = this.propertiesToRemove;
		clone.usesVariables = this.usesVariables;
		clone.querySentence = this.querySentence;
		clone.automaticallyGenerated = this.automaticallyGenerated;
		clone.patternHeadIndex = this.patternHeadIndex;
		clone.isTerminal = this.isTerminal;
		int ruleDisposition = this.ruleDisposition;
		clone.actions = this.actions;
		clone.doBalance = this.doBalance;
		return clone;
	}

	public Vector<Vector> extractEmbeddedPatternConstants(Vector<Vector<String>> pattern) {
		Vector<Vector> newpattern = null;
		try {
			for (Vector<String> sp : pattern) {
				Vector newsp = extractPatternConstants(sp);
				newpattern = VUtils.add(newpattern, newsp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newpattern;
	}

	public Vector<Vector> extractPatternConstants(Vector<String> pattern) {
		Vector<Vector> newpattern = null;
		for (Object o : pattern) {
			if (!(o instanceof String)) {
				int x = 1;
			}
		}
		for (String token : pattern) {
			Object c = Constant.extractConstant(this.getKnowledgeBase(), token);
			if (c instanceof Term) {
				((Term) c).setUsedInGrammarRulePattern(true);
			}
			newpattern = VUtils.add(newpattern, c);
		}
		return newpattern;
	}

	private void addInferenceRules() {
		Sexp cdr = Sexp.doAssoc(this.getSexp(), "inference");
		if (cdr != null) {
			KnowledgeBase kb = KnowledgeEngine.getCurrentKnowledgeEngine().getRootKnowledgeBase();
			cdr = (Sexp) cdr.getCdr();
			int i = 0;
			for (Enumeration<Sexp> e = cdr.elements(); e.hasMoreElements();) {
				Sexp rs = e.nextElement();
				String rid = this.getRuleID() + "_inference_rule_" + i++;
				InferenceRule irule = new InferenceRule(kb, rs, rid);
				this.inferenceRules = VUtils.add(this.inferenceRules, irule);
			}
		}
	}

	public Sexp createSexp(String ruleidstr, String stypestr, String conceptstr, Vector<Vector<String>> wlists,
			boolean ordered, boolean juxtaposed, Vector<Vector<String>> plists) {
		Sexp sexp = null;
		String ss = "((ruleid " + ruleidstr + ") ";

		ss += "(words " + wlists + ") ";
		if (ordered) {
			ss += "(ordered true) ";
		}
		if (juxtaposed) {
			ss += "(juxtaposed true) ";
		}
		if (concept != null) {
			ss += "(concept " + "\":" + concept + ":\") ";
		}
		if (stypestr != null) {
			ss += "(stype " + "\"<" + stypestr.toUpperCase() + ">\") ";
		}
		if (plists != null) {
			ss += "(properties " + plists + ")";
		}
		ss += ")";
		try {
			sexp = (Sexp) TLisp.getTLisp().evalString(ss);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sexp;
	}

	void readPatternListsFromFile() {
		String filename = this.getStringProperty("sourcefile");
		StartupParameters sp = this.getGrammar().getGrammarModule().getKnowledgeEngine().getStartupParameters();
		if (filename != null) {
			try {
				Vector<String> fileStrings = null;
				String fullname = sp.getFileName(filename);
				File f = new File(fullname);
				if (f.exists()) {
					BufferedReader in = new BufferedReader(new FileReader(f));
					String line = null;
					while ((line = in.readLine()) != null) {
						if (line.length() > 0 && Character.isLetter(line.charAt(0))) {
							String str = line.trim().toLowerCase();
							fileStrings = VUtils.add(fileStrings, str);
						}
					}
				}
				if (fileStrings != null) {
					if (this.patternLists != null) {
						Vector<Vector> newPatternLists = null;
						Vector<String> wlist = null;
						for (int i = 0; i < this.patternLists.size(); i++) {
							Vector<String> strings = this.patternLists.elementAt(i);
							wlist = strings;
							if (strings.size() == 1) {
								String str = strings.firstElement();
								if ("?filestring".equals(str)) {
									wlist = fileStrings;
								}
							}
							newPatternLists = VUtils.add(newPatternLists, wlist);
						}
						this.patternLists = newPatternLists;
					} else {
						this.patternLists = VUtils.listify(fileStrings);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void readPatternListsFromFromConTextLexicon() {
		Vector<String> headers = (Vector<String>) VUtils.assocValue("contextlexicon", this.getPattern());
		if (headers != null) {
			for (String header : headers) {
				String[] hstrs = header.split("=");
				if (header.startsWith("rule=")) {
					this.modifierDirection = hstrs[1];
				}
			}
			Vector<String> fstrs = this.getGrammar().getGrammarModule().getConTextLexicon().getConTextItems(headers);
			Vector<Vector> newPatternLists = null;
			for (int i = 0; i < this.patternLists.size(); i++) {
				Vector elements = this.patternLists.elementAt(i);
				Vector wlist = null;
				for (Object element : elements) {
					boolean added = false;
					if (element instanceof String) {
						String str = (String) element;
						if (Variable.isVariable(str) && str.contains("file")) {
							wlist = VUtils.append(wlist, fstrs);
							added = true;
						}
					}
					if (!added) {
						wlist = VUtils.add(wlist, element);
					}
				}
				newPatternLists = VUtils.add(newPatternLists, wlist);
			}
			this.patternLists = newPatternLists;
		}
	}
	
	private void readPatternListsFromFromConTextLexiconOLD() {
		Vector<String> headers = (Vector<String>) VUtils.assocValue("contextlexicon", this.getPattern());
		if (headers != null) {
			for (String header : headers) {
				String[] hstrs = header.split("=");
				if (header.startsWith("rule=")) {
					this.modifierDirection = hstrs[1];
				}
			}
			Vector<String> strs = this.getGrammar().getGrammarModule().getConTextLexicon().getConTextItems(headers);
			if (strs != null) {
				if (this.patternLists != null) {
					Vector<Vector> newPatternLists = null;
					Vector<String> wlist = null;
					for (int i = 0; i < this.patternLists.size(); i++) {
						Vector<String> strings = this.patternLists.elementAt(i);
						wlist = strings;
						if (strings.size() == 1) {
							String str = strings.firstElement().toLowerCase();
							if (Variable.isVariable(str) && str.contains("file")) {
								wlist = strs;
							}
						}
						newPatternLists = VUtils.add(newPatternLists, wlist);
					}
					this.patternLists = newPatternLists;
				} else {
					this.patternLists = VUtils.listify(strs);
				}
			}
		}
	}

	public Sexp toSexp() {
		return toSexp(true);
	}
	
	// 6/12/2017
		public String toSexpString() {
			int x = 1;
			if (this.getSexp() == null) {
				Sexp sexp = this.toSexp();
				this.setSexp(sexp);
			}
			return this.getSexp().toNewlinedString();
		}


	// 2/12/2016: Not complete. Doesn't write subpattern, for instance.
	public Sexp toSexp(boolean useOrigSexp) {
		Sexp sexp = null;
		try {
			TLisp l = TLisp.getTLisp();
			Sexp origsexp = (this.getOriginalSourceRule() != null ? this.getOriginalSourceRule().getSexp()
					: this.getSexp());
			String lstr = "'((ruleid \"" + this.ruleID + "\") ";
			if (this.isSpecialized()) {
				lstr += "(specialized) ";
			}
			if (this.isIntermediate()) {
				lstr += "(intermediate) ";
			}
			if (this.isComplexConcept()) {
				lstr += "(complex-concept) ";
			}
			if (this.isContainsTargetConcept()) {
				lstr += "(contains-target true) ";
			}
			if (this.getExampleSnippet() != null) {
				lstr += "(example-snippet \"" + this.getExampleSnippet() + "\") ";
			}
			if (this.isOrdered()) {
				lstr += "(ordered true) ";
			}
			if (this.isJuxtaposed()) {
				lstr += "(juxtaposed true) ";
			}
			if (this.isPermitInterstitialAnnotations()) {
				lstr += "(permit-interstitial true) ";
			}
			if (this.sourceID != null) {
				lstr += "(sourceID \"" + this.sourceID + "\") ";
			}
			if (this.resultConcept != null) {
				lstr += "(concept \"" + this.resultConcept + "\") ";
			}
			if (this.resultCUI != null) {
				lstr += "(cui \"" + this.resultCUI + "\") ";
			}
			if (this.getType() != null) {
				lstr += "(stype \"" + this.getType().getFormalName() + "\") ";
			}
			if (this.getWindow() > 0) {
				lstr += "(window " + this.getWindow() + ") ";
			}
			if (this.isBagOfConceptsRule()) {
				lstr += "(bagofconcepts true) ";
			}

			if (this.patternLists != null) {
				String wstr = "(words ";
				for (Vector v : this.patternLists) {
					wstr += "(";
					for (Object o : v) {
						String ostr = o.toString();
						if (o instanceof Constant) {
							ostr = ((Constant) o).getFormalName();
						}
						wstr += "\"" + ostr + "\" ";
					}
					wstr += ") ";
				}
				wstr += ") ";
				lstr += wstr;
			}
			if (useOrigSexp) {
				if (this.getWindow() < 0 && Sexp.doAssoc(origsexp, "window") != null) {
					lstr += Sexp.doAssoc(origsexp, "window").toNewlinedString();
				}
				if (Sexp.doAssoc(origsexp, "localvar") != null) {
					lstr += Sexp.doAssoc(origsexp, "localvar").toNewlinedString();
				}
				if (Sexp.doAssoc(origsexp, "properties") != null) {
					lstr += Sexp.doAssoc(origsexp, "properties").toNewlinedString();
				}
				if (Sexp.doAssoc(origsexp, "tests") != null) {
					lstr += Sexp.doAssoc(origsexp, "tests").toNewlinedString();
				}
			}
			if (this.semanticRelations != null) {
				String prsstr = "(relations ";
				for (PatternRelationSentence prs : this.semanticRelations) {
					prsstr += prs.toLisp() + " ";
				}
				prsstr += ") ";
				lstr += prsstr + " ";
			}
			if (this.propertiesToRemove != null) {
				lstr += " (removeproperties ";
				for (String prop : this.propertiesToRemove) {
					lstr += prop;
				}
				lstr += ") ";
			}
			lstr += " )";
			sexp = (Sexp) l.evalString(lstr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sexp;
	};

	public static String getRuleIdFromString(String rstr) {
		String ruleid = null;
		try {
			if (rstr != null) {
				Object o = TLisp.getTLisp().evalString(rstr);
				if (o instanceof Sexp) {
					Sexp s = (Sexp) o;
					Vector v = TLUtils.convertSexpToJVector(s);
					Vector<Vector> avpairs = Information.getAVPairs(v);
					ruleid = (String) VUtils.assocValue("ruleid", avpairs);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ruleid;
	}

	public String toString() {
		return "<Rule=" + this.getRuleID() + ",File=" + this.getSourceFilePath() + ",Pattern=" + this.getPatternLists()
				+ ">";
	}

	public boolean equals(Object o) {
		if (o instanceof Rule) {
			Rule rule = (Rule) o;
			if (rule.getRuleID().equals(this.getRuleID())) {
				return true;
			}
		}
		return false;
	}

	public String getRuleID() {
		return this.ruleID;
	}

	public int getRuleIDNum() {
		return this.ruleIDNum;
	}

	public void setRuleID(String id) {
		this.ruleID = id;
	}

	public boolean isOrdered() {
		return ordered;
	}

	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	public boolean isJuxtaposed() {
		return juxtaposed;
	}

	public int getWindow() {
		return window;
	}

	public void setWindow(int window) {
		this.window = window;
	}

	public boolean isApplicableToSentence() {
		if (!(this instanceof LabelRule)
				&& (this.wordOnlyPatternCount == 0 || !this.getGrammar().ruleIsInvalid(this))) {
			return true;
		}
		return false;
	}

	public boolean hasOnlyWords() {
		return this.patternLists != null && this.patternLists.size() == this.wordOnlyPatternCount;
	}

	public void calculateWordOnlyPatternCount() {
		try {
			if (this.patternLists != null) {
				for (Vector plist : this.patternLists) {
					boolean onlyWords = true;
					for (Object token : plist) {
						char c = token.toString().charAt(0);
						if (!Character.isLetter(c) || !(token instanceof String)) {
							onlyWords = false;
							break;
						}
					}
					if (onlyWords) {
						this.wordOnlyPatternCount++;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getwordOnlyPatternCount() {
		return this.wordOnlyPatternCount;
	}

	public Vector<Vector> getPatternLists() {
		return patternLists;
	}

	public String getResultCUI() {
		return resultCUI;
	}

	public void setResultCUI(String resultCUI) {
		this.resultCUI = resultCUI;
	}

	public Object getResultConcept() {
		return resultConcept;
	}

	public void setResultConcept(Object resultConcept) {
		this.resultConcept = resultConcept;
	}

	public String getResultMacro() {
		return resultMacro;
	}

	public void setResultMacro(String macro) {
		this.resultMacro = macro;
	}

	public SyntacticTypeConstant getPhraseType() {
		return phraseType;
	}

	public void setPhraseType(SyntacticTypeConstant phraseType) {
		this.phraseType = phraseType;
	}

	// How to include rule properties?
	public void extractCUIStructs() {
		String rcui = this.getResultCUI();
		if (this.hasOnlyWords() && rcui != null) {
			rcui = rcui.toLowerCase();
			UMLSStructuresShort umlss = UMLSStructuresShort.getUMLSStructures();
			Ontology tontology = this.getGrammar().getKnowledgeEngine().getUMLSOntology();
			Vector<Vector<String>> sets = SetUtils.cartesianProduct(this.getPatternLists());
			for (Vector<String> set : sets) {
				String str = StrUtils.stringListConcat(set, " ");
				Vector<Token> tokens = Token.readTokensFromString(str);
				Vector<CUIStructureWrapperShort> cws = umlss.getCUIStructureWrappers(tokens, null, true);
				if (cws == null) {
					continue;
				}
				boolean found = false;
				Collections.sort(cws, new CUIStructureWrapperShort.WordLengthSorter());
				for (CUIStructureWrapperShort cw : cws) {
					if (cw.getTokens().size() == set.size()
							&& rcui.equals(cw.getCuiStructure().getCui().toLowerCase())) {
						found = true;
						break;
					}
				}
				if (!found) {
					CUIStructureShort target = umlss.getCUIStructure(rcui);
					if (target != null) {
						UMLSTypeInfo tinfo = target.getTypeInfo();
						Vector<Word> words = Lexicon.currentLexicon.getWords(set);
						CUIStructureShort cs = CUIStructureShort.create(umlss, words, rcui, tinfo, tontology);
						this.addCuiStructureShort(cs);
						System.out.println("Adding Rule CUIStructure: " + cs);
					}
				}
			}
		}
	}

	public WordSequenceAnnotation getRelevantSentenceAnnotation() {
		return relevantSentenceAnnotation;
	}

	public void setRelevantSentenceAnnotation(WordSequenceAnnotation relevantSentenceAnnotation) {
		this.relevantSentenceAnnotation = relevantSentenceAnnotation;
	}

	public Vector<Constraint> getTestPredicates() {
		return testPredicates;
	}

	public Vector<Vector> getPropertyPredicates() {
		return this.propertyPredicates;
	}

	public void setTestPredicates(Vector<Constraint> testPredicates) {
		this.testPredicates = testPredicates;
	}

	public void setPropertyPredicates(Vector<Vector> propertyPredicates) {
		this.propertyPredicates = propertyPredicates;
	}

	public void setLocalVariables(Vector<Variable> localVariables) {
		this.localVariables = localVariables;
	}

	public void setSemanticRelations(Vector<PatternRelationSentence> semanticRelations) {
		this.semanticRelations = semanticRelations;
	}

	public void setActions(Vector<Constraint> actions) {
		this.actions = actions;
	}

	public Vector<Variable> getLocalVariables() {
		return localVariables;
	}

	public Vector<CUIStructureShort> getCuiStructures() {
		return this.cuiStructures;
	}

	public void addCuiStructureShort(CUIStructureShort cs) {
		this.cuiStructures = VUtils.add(this.cuiStructures, cs);
	}

	public Vector<RelationSentence> getSemanticInterpPatterns() {
		return this.semanticInterpPatterns;
	}

	public boolean ruleIDContains(String str) {
		return this.ruleID.toLowerCase().contains(str.toLowerCase());
	}

	public Vector<String> getFlags() {
		return this.flags;
	}

	public void setPatternLists(Vector<Vector> patternLists) {
		this.patternLists = patternLists;
		this.patternListCount = patternLists.size();
	}

	public int getPatternListCount() {
		return patternListCount;
	}

	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

	public String getSourceFilePath() {
		return sourceFilePath;
	}

	public String getSourceFileName() {
		return new File(this.sourceFilePath).getName();
	}

	public boolean hasPatternListsFromSourceFile() {
		return this.getPatternLists() != null && this.getSourceFilePath() != null;
	}

	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
		if (sourceFilePath.toLowerCase().contains("capture")) {
			this.isCaptureRule = true;
		}
	}

	public static TypeConstant extractType(String tstr) {
		TypeConstant type = null;
		String tname = tstr;
		if (tstr != null && tstr.charAt(0) == '<' && tstr.charAt(tstr.length() - 1) == '>') {
			tname = tstr.substring(1, tstr.length() - 1);
		}
		if (tname != null) {
			type = TypeConstant.createTypeConstant(tname.toLowerCase());
		}
		return type;
	}

	public static String extractMacro(String mstr) {
		if (mstr != null && mstr.charAt(0) == '_' && mstr.charAt(mstr.length() - 1) == '_') {
			mstr = mstr.toUpperCase();
			return mstr;
		}
		return null;
	}

	public Hashtable<String, Vector<Integer>> getRuleTokenPositionHash() {
		return ruleTokenPositionHash;
	}

	public Vector<String> getStopWords() {
		if (this.stopWords != null) {
			int x = 1;
			x = x;
		}
		return stopWords;
	}

	public boolean isTentative() {
		return isTentative;
	}

	public void setTentative(boolean isTentative) {
		this.isTentative = isTentative;
	}

	public TypeConstant getGeneralType() {
		return generalType;
	}

	public void setGeneralType(TypeConstant generalType) {
		this.generalType = generalType;
	}

	public boolean isConjunct() {
		return this.isConjunct;
	}

	public boolean usesSimplifiedFormat() {
		return "simplified".equals(this.getRuleType());
	}

	public boolean containsFlag(String flag) {
		return this.flags != null && this.flags.contains(flag);
	}

	public Vector<PatternRelationSentence> getSemanticRelations() {
		return semanticRelations;
	}

	public void addSemanticRelation(PatternRelationSentence prs) {
		this.semanticRelations = VUtils.add(this.semanticRelations, prs);
	}

	public boolean isDoDebug() {
		return this.doDebug;
	}

	public void setDoDebug(boolean doDebug) {
		this.doDebug = doDebug;
	}

	public Vector<InferenceRule> getInferenceRules() {
		return inferenceRules;
	}

	public String getModifierDirection() {
		return this.modifierDirection;
	}

	public String getSourceID() {
		return sourceID;
	}

	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
	}

	public boolean isPermitInterstitialAnnotations() {
		return permitInterstitialAnnotations;
	}

	public boolean isInheritProperties() {
		return inheritProperties;
	}

	public Vector<String> getPropertiesToRemove() {
		return this.propertiesToRemove;
	}
	
	public void addPropertyToRemove(String property) {
		this.propertiesToRemove = VUtils.addIfNot(this.propertiesToRemove, property);
	}

	public boolean isPropertyToRemove(String property) {
		if (this.isDoDebug() && this.propertiesToRemove != null) {
			int x = 1;
		}
		return (this.propertiesToRemove != null && this.propertiesToRemove.contains(property));
	}

	public Vector<String> getHeaderStrings() {
		return headerStrings;
	}

	public boolean usesVariables() {
		return this.usesVariables;
	}

	public Sentence getQuerySentence() {
		return querySentence;
	}

	public boolean isAutomaticallyGenerated() {
		return automaticallyGenerated;
	}

	public void setAutomaticallyGenerated(boolean automaticallyGenerated) {
		this.automaticallyGenerated = automaticallyGenerated;
	}

	public static int getNumRules() {
		return numRules;
	}

	public static void incrementNumRules() {
		Rule.numRules++;
	}

	public boolean isSingleton() {
		return this.patternListCount == 1;
	}

	public int getPatternHeadIndex() {
		return patternHeadIndex;
	}

	public boolean isTerminal() {
		return isTerminal;
	}

	public Vector<Vector> getSubpatternLists() {
		return this.subpatternLists;
	}

	public boolean isDispositionCreateAnnotation() {
		return this.ruleDisposition == DispositionCreateAnnotation;
	}

	public boolean isDispositionExecuteActions() {
		return this.ruleDisposition == DispositionExecuteActions;
	}

	public Vector<Constraint> getActions() {
		return actions;
	}

	// 8/8/2015. Klooge. Need to find a way to do this that doesn't rely on
	// hard-coded semantic knowledge.
	public boolean permitAnnotationDuplicates() {
		return this.isConjunct();
	}

	public boolean isInhibitInterstitial() {
		return inhibitInterstitial;
	}

	public boolean isDoBalance() {
		return doBalance;
	}

	private Vector<Vector> extractPatternLists(Vector pattern) {
		Vector<Vector> patternLists = null;
		Vector wv = VUtils.assoc("words", pattern);
		if (wv == null) {
			wv = VUtils.assoc("pattern", pattern);
		}
		if (wv != null && wv.size() > 1) {
			patternLists = VUtils.rest(wv);
			if (patternLists != null) {
				for (Vector<String> vs : patternLists) {
					Collections.sort(vs, new StrUtils.LengthSorterDescending());
				}
			}
		}
		return patternLists;
	}

	// Before 5/13/2016
	// private Vector<Vector> extractPatternLists(Vector pattern) {
	// Vector<Vector> patternLists = null;
	// Vector wv = VUtils.assoc("words", pattern);
	// if (wv != null && wv.size() > 1) {
	// patternLists = VUtils.rest(VUtils.assoc("words", pattern));
	// if (patternLists != null) {
	// for (Vector<String> vs : patternLists) {
	// Collections.sort(vs, new StrUtils.LengthSorterDescending());
	// }
	// }
	// }
	// return patternLists;
	// }

	public Rule getSourceRule() {
		return sourceRule;
	}

	public Rule getOriginalSourceRule() {
		if (this.sourceRule != null) {
			return this.sourceRule.getOriginalSourceRule();
		}
		return this;
	}

	public void setSourceRule(Rule sourceRule) {
		this.sourceRule = sourceRule;
	}

	// /// FROM HOME, 2/11/2016:

	public String getRulenamePatternToken() {
		return '@' + this.getRuleID() + '@';
	}

	public boolean nameContainsString(String str) {
		return this.getRuleID().toLowerCase().contains(str.toLowerCase());
	}

	public boolean isSpecialized() {
		return isSpecialized;
	}

	public void setSpecialized(boolean isSpecialized) {
		this.isSpecialized = isSpecialized;
	}

	public boolean isSpecializedLowerLevel() {
		return this.isSpecialized && this.semanticRelations == null;
	}

	public boolean isSpecializedTopLevel() {
		return this.isSpecialized && this.semanticRelations != null;
	}

	public boolean isSingletonWordInput() {
		return isSingletonWordInput;
	}

	private static char[] ValidWordPunctuation = { ' ', '\'', '/' };

	// 12/6/2015: To determine whether a grammar rule contains word strings
	// only.
	private static boolean isWordPatternString(Object o) {
		if (!(o instanceof String)) {
			return false;
		}
		String str = (String) o;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!Character.isLetter(c)) {
				boolean isValidPunctuation = false;
				for (int j = 0; j < ValidWordPunctuation.length; j++) {
					if (c == ValidWordPunctuation[j]) {
						isValidPunctuation = true;
						break;
					}
				}
				if (!isValidPunctuation) {
					return false;
				}
			}
		}
		return true;
	}

	public String getExampleSnippet() {
		return exampleSnippet;
	}

	public void setExampleSnippet(String exampleSnippet) {
		this.exampleSnippet = exampleSnippet;
	}

	public boolean isContainsTargetConcept() {
		return containsTargetConcept;
	}

	public void setContainsTargetConcept(boolean containsTargetConcept) {
		this.containsTargetConcept = containsTargetConcept;
	}

	// 7/7/2016
	public boolean isUseForwardChainingInference() {
		return this.useForwardChainingInference;
	}

	private void determineIsSingletonWordInput() {
		if (this.isSingleton()) {
			Vector plist = this.getPatternLists().firstElement();
			for (Object o : plist) {
				if (isWordPatternString(o)) {
					this.isSingletonWordInput = true;
					return;
				}
			}
		}
	}
	
	public boolean isSingletonAllWordInput() {
		if (this.isSingleton()) {
			Vector plist = this.getPatternLists().firstElement();
			for (Object o : plist) {
				if (!isWordPatternString(o)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean hasSubpattern() {
		return this.subpatternLists != null;
	}

	public boolean isIntermediate() {
		return this.isIntermediate;
	}

	public void setIntermediate(boolean isIntermediate) {
		this.isIntermediate = isIntermediate;
	}

	public boolean isComplexConcept() {
		return isComplexConcept;
	}

	// 3/4/2016: Generate Excel-format string
	// Order: ruleid, concept, window, ordered, words/pattern, properties, test,
	// relations.
	public String toExcelString() {
		StringBuffer sb = new StringBuffer();
		Sexp sexp = this.getSexp();
		for (int i = 0; i <= TSLInformation.getExcelAttributeMaxIndex(); i++) {
			Vector<String> attributes = TSLInformation.getExcelIndexAttributes(i);
			if (attributes != null) {
				String cdrstr = null;
				Sexp sub = null;
				for (String attribute : attributes) {
					sub = Sexp.doAssoc(sexp, attribute);
					if (sub != null) {
						cdrstr = sub.getCdr().toString();
						if (cdrstr.charAt(0) == '(' && cdrstr.charAt(cdrstr.length() - 1) == ')') {
							cdrstr = cdrstr.substring(1, cdrstr.length() - 1);
						}
						break;
					}
				}
				if (cdrstr != null) {
					sb.append(cdrstr);
				} else {
					sb.append("*");
				}
				if (i < TSLInformation.getExcelAttributeMaxIndex()) {
					sb.append(ExcelDelimiter);
				}
			}
		}
		return sb.toString();
	}

	public static Rule readFromExcelString(Grammar grammar, String estr, String sourcefile) {
		Rule rule = null;
		try {
			StringBuffer sb = new StringBuffer();
			if (!"*".equals(estr)) {
				sb.append("'(");
				Vector<String> valuestrs = StrUtils.stringList(estr, Rule.ExcelPatternDelimiter);
				for (int i = 0; i < TSLInformation.getExcelAttributeMaxIndex(); i++) {
					String value = valuestrs.elementAt(i);
					if (value != null && value.length() > 1) {
						Vector<String> attributes = TSLInformation.getExcelIndexAttributes(i);
						if (attributes != null) {
							for (String attribute : attributes) {
								String str = "(" + attribute + " " + value + ")";
								sb.append(str);
							}
						}
					}
				}
				sb.append(")");
				Sexp sexp = (Sexp) TLisp.getTLisp().evalString(sb.toString());
				rule = grammar.createRule(sexp, "wordrule", sourcefile, null);
			}
		} catch (Exception e) {
			System.out.println("Error reading Excel rule definition: File=" + sourcefile + ",ExcelString=" + estr);
			e.printStackTrace();
		}
		return rule;
	}

	public Object getResultType() {
		return resultType;
	}

	public static class RuleNameSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Rule r1 = (Rule) o1;
			Rule r2 = (Rule) o2;
			return r1.getRuleID().compareTo(r2.getRuleID());
		}
	}

	public boolean isBagOfConceptsRule() {
		return isBagOfConceptsRule;
	}

	public void setBagOfConceptsRule(boolean isBagOfConceptsRule) {
		this.isBagOfConceptsRule = isBagOfConceptsRule;
	}

	public boolean isCaptureRule() {
		return isCaptureRule;
	}
	
	// 5/29/2018: Whether rule is directly about concept, for determining 
	// the amount of interence that goes into conclusions.
	public boolean isStatesTargetConceptDirectly() {
		return this.statesTargetConceptDirectly;
	}
	

}
