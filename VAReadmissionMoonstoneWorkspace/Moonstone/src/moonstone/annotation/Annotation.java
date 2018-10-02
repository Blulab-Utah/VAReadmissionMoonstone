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
package moonstone.annotation;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.grammar.DocumentGrammar;
import moonstone.grammar.Grammar;
import moonstone.information.Information;
import moonstone.io.readmission.Readmission;
import moonstone.javafunction.JavaFunctions;
import moonstone.learning.ebl.GrammarEBL;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import moonstone.semantic.Interpretation;
import moonstone.syntactic.Syntax;
import moonstone.utility.ThreadUtils;
import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.Header;
import tsl.documentanalysis.lexicon.Word;
import tsl.documentanalysis.tokenizer.Token;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.relation.PatternRelationSentence;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.inference.backwardchaining.Query;
import tsl.information.TSLInformation;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;
import tsl.utilities.HUtils;
import tsl.utilities.SeqUtils;
import tsl.utilities.SetUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class Annotation extends Information {

	// 7/18/2014 NOTES: Remove concept, cui and macro from special handling in
	// the future. Handle them explicitly ine
	// property management syntax...

	// 2/23/2015: Replace all the tokenIndex-related numerals with pointers to
	// the tokens, and use methods to return those values.

	protected WordSequenceAnnotation sentenceAnnotation = null;
	protected Rule rule = null;
	protected String string = null;

	// 11/13/2016
	protected String text = null;

	protected Vector<Annotation> lexicallySortedSourceAnnotations = null;
	protected Vector<Annotation> textuallySortedSourceAnnotations = null;
	protected int numberOfChildren = 0;

	// 2/23/2015: Remove these:
	protected int tokenStart = -1;
	protected int tokenEnd = -1;
	protected int textStart = -1;
	protected int textEnd = -1;
	protected int textlength = 0;
	protected int wordTokenCount = 0;

	protected int sentenceTokenStart = -1;
	protected int sentenceTokenEnd = -1;
	protected int relativeTokenStart = -1;
	protected int relativeTokenEnd = -1;

	protected Object value = null;
	protected boolean usedInHigherAnnotation = false;

	// 2/23/2015
	protected int wordTokenStart = -1;
	protected int wordTokenEnd = -1;

	protected SyntacticTypeConstant phraseType = null;
	protected String phraseToken = null;

	protected Vector<String> indexTokens = null;
	protected Vector<Annotation> parentAnnotations = null;
	protected Interpretation semanticInterpretation = null;
	protected Vector<String> flags = null;
	protected Vector<Token> tokens = null;
	protected Vector<Word> coveredWords = null;
	protected String signature = null;
	protected int signatureID = 0;
	protected boolean containsConjunct = false;
	protected String annotationID = "*";
	protected long numericAnnotationID = -1;
	protected Object userObject = null;
	protected boolean isTSL = false;
	protected int coveredTextLength = 0;
	protected float depth = 0;
	protected float leftDepth = 0;
	protected float rightDepth = 0;
	protected StructureAnnotation structureAnnotation = null;
	protected int totalTargetConceptCount = -1;

	// For Dan riskin's output
	public boolean issub = false;

	// 8/13/2015
	protected double coveredTextPercent = 0f;
	protected double symmetryFactor = 0f;

	protected String invalidReason = null;
	protected String FCIETargetRuleName = null;
	protected Vector<Annotation> nestedTargetAnnotations = null;
	protected Vector<Annotation> topmostNestedTargetAnnotations = null;
	protected Vector nestedTargetConcepts = null;
	private boolean garbageCollected = false;

	protected static String[] conjunctiveWords = new String[] { "and", "or" };
	protected static long AnnotationIDIndex = 0;

	protected static String[] SpecialPunctuation = { "%", "-" };

	protected static String[] IndexTokenProperties = { "object" };

	public static String FailSpecialization = "FailSpecialization";
	public static String FailTypeCheck = "FailTypeCheck";
	public static String FailDepth = "FailDepth";
	public static String ContainsStopword = "ContainsStopword";
	public static String FailTest = "FailTest";
	public static String FailPhraseTypeCheck = "FailPhraseTypeCheck";

	public Annotation() {
		super();
	}

	public Annotation(WordSequenceAnnotation sentenceAnnotation) {
		super();
		this.sentenceAnnotation = sentenceAnnotation;
		if (sentenceAnnotation != null) {
			this.grammar = sentenceAnnotation.getGrammar();
		}
		this.getMoonstoneRuleInterface().storeForGarbageCollection(this);
	}

	// 6/9/2014: For expanding conjuncts...
	public Annotation(Annotation source, Vector<Annotation> children) {
		this(source.getSentenceAnnotation(), source.getRule(), children, false);
	}

	public Annotation(WordSequenceAnnotation sentenceAnnotation, Rule rule,
			Vector<Annotation> sources, boolean doExpand) {
		super();
		this.initialize(sentenceAnnotation, rule, sources, doExpand);
		this.getMoonstoneRuleInterface().storeForGarbageCollection(this);
	}

	public void initialize(WordSequenceAnnotation sentenceAnnotation,
			Rule rule, Vector<Annotation> sources, boolean doExpand) {
		if (rule != null && rule.isDoDebug()) {
			int x = 1;
		}
		this.numericAnnotationID = AnnotationIDIndex++;
		this.annotationID = "A:" + this.numericAnnotationID;
		this.sentenceAnnotation = sentenceAnnotation;
		this.grammar = Grammar.CurrentGrammar;
		this.rule = rule;
		if (sources != null) {
			this.lexicallySortedSourceAnnotations = sources;
			this.textuallySortedSourceAnnotations = new Vector(sources);
			Collections.sort(this.textuallySortedSourceAnnotations,
					new Annotation.TokenIndexSorter());
		}
		if (rule != null) {
			if (!processQuery()) {
				return;
			}
			if (rule.usesVariables()) {
				this.setVariables(gatherPositionalPhraseVariables(sources));
				Variable var = new Variable("?*");
				var.bind(this);
				this.addVariable(var);

				// 3/12/2016
				var = new Variable("?rule");
				var.bind(rule);
				this.addVariable(var);

				if (rule.getLocalVariables() != null) {
					for (Variable lvar : rule.getLocalVariables()) {
						Object value = this.evalPatternRecursive(
								lvar.getValue(), this.getVariables());

						// 8/6/2015: PROBLEM: If the pattern method can't
						// succeed with a vector, it returns
						// that vector, e.g. (modifier (event ?0)). I need some
						// way to succeed for valid
						// vectors, but not for property vectors.

						// Before 5/5/2015
						// Object value = this.evalPattern(lvar.getValue(),
						// this.getVariables());

						if (value == null) {
							return;
						}
						Variable newvar = new Variable(lvar);
						newvar.bind(value);
						this.addVariable(newvar);
					}
				}
				if (rule.getFlags() != null) {
					this.flags = new Vector(rule.getFlags());
				}
			}
		}

		if (this.textuallySortedSourceAnnotations != null) {
			this.numberOfChildren = this.textuallySortedSourceAnnotations
					.size();
			Annotation start = this.textuallySortedSourceAnnotations
					.firstElement();
			Annotation end = this.textuallySortedSourceAnnotations
					.lastElement();
			this.tokenStart = start.tokenStart;
			this.tokenEnd = end.tokenEnd;
			this.textStart = start.textStart;
			this.textEnd = end.textEnd;
			this.sentenceTokenStart = start.sentenceTokenStart;
			this.sentenceTokenEnd = end.sentenceTokenEnd;
			this.relativeTokenStart = start.getRelativeTokenStart();
			this.relativeTokenEnd = end.getRelativeTokenEnd();
			this.string = this.sentenceAnnotation.getDocumentText().substring(
					this.textStart, this.textEnd + 1);

			// 11/13/2016: Prior to Document parse this string is ending up
			// NULL.
			// Am I releasing the document text before the decision list?

			// this.getText();

			this.wordTokenStart = start.wordTokenStart;
			this.wordTokenEnd = end.wordTokenEnd;
			this.getWordTokenCount();
			assignRelativeOffsets();
		}
		getOntology();
		this.assignPhraseType(null);
		Interpretation si = Interpretation.create(this, this.getCui(),
				this.getConcept(), this.getMacro(), this.getRule(), this.type);
		this.setSemanticInterpretation(si);
		// this.generateIndexTokens();
		this.textlength = (this.textEnd - this.textStart) + 1;
		this.getCoveredTextLength();
		this.getDepth();
		this.getLeftDepth();
		this.getRightDepth();

		// this.getIndexTokens();
		// this.setSignature();

		if (!this.checkIsValid()) {
			String reason = this.getInvalidReason();
			if (this.getRule().isDoDebug()) {
				int x = 1;
			}
			return;
		}
		if (sources != null) {
			// 3/3/2015: Could pointing narrative-level annotations to parent
			// annotations
			// belonging to a different grammar cause problems?
			for (Annotation child : sources) {
				child.addParent(this);
				if (child.isConjunct() || child.containsConjunct()) {
					this.setContainsConjunct(true);
				}
			}
		}

		// 7/23/2016: TEST!! (May be too expensive!)
		// this.setInferredTargetConcept();

		this.getIndexTokens();
		this.setSignature();

		this.getGoodness();

		if (this.getGrammar().isDoDebug()) {
			System.out.println("NEW: " + this);
		}
		this.gatherNestedTargetAnnotations();
		this.gatherTopmostNestedTargetAnnotations();
		this.doPostProcessing();
	}

	public void assignPhraseType(Vector<Token> tokens) {
		if (this.getRule() != null) {
			this.phraseType = this.getRule().getPhraseType();
		}
		if (this.phraseType == null && tokens != null && !tokens.isEmpty()) {
			this.phraseType = Syntax.getTerminalPhraseType(tokens);
		}
	}

	public static Vector<Variable> gatherPositionalPhraseVariables(
			Vector<Annotation> sources) {
		Vector<Variable> vars = new Vector();
		for (int i = 0; i < sources.size(); i++) {
			Variable var = new Variable("?" + i);
			Annotation source = sources.elementAt(i);
			Object value = (source.getValue() != null ? source.getValue()
					: source);
			var.bind(value);
			vars = VUtils.add(vars, var);
		}
		return vars;
	}

	// 6/20/2015
	public void doPostProcessing() {
		Interpretation interp = this.getSemanticInterpretation();
		if (interp != null) {
			if (this.headerContainsString("histor")
					|| this.headerContainsString("past")) {
				interp.setProperty("header", "historical");
			}
			if (this.headerContainsString("recommend")) {
				interp.setProperty("header", "recommendation");
			}
			if (this.headerContainsString("review")) {
				interp.setProperty("header", "review");
			}
			if (this.headerContainsString("diagnos")) {
				interp.setProperty("header", "diagnosis");
			}
			if (this.headerContainsString("plan")) {
				interp.setProperty("header", "plan");
			}
			if (this.headerContainsString("reason")) {
				interp.setProperty("header", "reason");
			}
			if (this.headerContainsString("diagnos")) {
				interp.setProperty("header", "diagnosis");
			}
			if (this.headerContainsString("diagnos")) {
				interp.setProperty("header", "diagnosis");
			}
		}
	}

	public void assignRelativeOffsets() {

		// Before 4/17/2015
		// Annotation start =
		// this.textuallySortedSourceAnnotations.firstElement();
		// Annotation end = this.textuallySortedSourceAnnotations.lastElement();
		// this.relativeTokenStart = start.getRelativeTokenStart();
		// this.relativeTokenEnd = end.getRelativeTokenEnd();
		//
		// 4/17/2015
		int ssi = this.getSentenceAnnotation().getSentence()
				.getTokenStartIndex();
		int asi = this.getTokenStart();
		int aei = this.getTokenEnd();
		this.relativeTokenStart = asi - ssi;
		this.relativeTokenEnd = aei - ssi;

	}

	// 10/16/2014: For HeidelTime and others of its ilk...
	public Annotation(tsl.documentanalysis.document.Sentence sentence,
			TypeConstant gtype, TypeConstant stype, Vector<Token> tokens,
			Hashtable properties) {
		this.annotationID = "A:" + AnnotationIDIndex++;
		Token ftoken = tokens.firstElement();
		Token ltoken = tokens.lastElement();
		this.tokenStart = ftoken.getIndex();
		this.tokenEnd = ltoken.getIndex();

		this.textStart = ftoken.getStart();
		this.textEnd = ltoken.getEnd();

		// In HeidelTime, start/end is relative to document, not sentence.
		this.string = sentence.getDocument().getText()
				.substring(this.textStart, this.textEnd + 1);

		// How to set phrase type?

		Interpretation si = new Interpretation(this);
		si.setProperties(properties);
		si.setType(stype);
		si.setGeneralType(gtype);
		this.setSemanticInterpretation(si);

		this.getIndexTokens();
		this.setSignature();
	}

	public Annotation createTSLAnnotation(Grammar grammar, Annotation a1,
			Annotation a2, String atype, String relation, String property,
			String value) {
		if (a1 != null && a2 != null
				&& a1.getSentenceAnnotation() == a2.getSentenceAnnotation()) {
			return new Annotation(grammar, a1, a2, atype, relation, property,
					value);
		}
		return null;
	}

	// 12/5/2014: For TSL-created annotations
	public Annotation(Grammar grammar, Annotation a1, Annotation a2,
			String atype, String relation, String property, String value) {
		Document doc = a1.getDocument();
		Interpretation si = new Interpretation(this);
		this.setSemanticInterpretation(si);
		if (atype != null) {
			TypeConstant type = TypeConstant.createTypeConstant(atype);
			si.setType(type);
		}
		this.grammar = grammar;
		Annotation first = (a1.getTextStart() < a2.getTextStart() ? a1 : a2);
		Annotation second = (a1 == first ? a2 : a1);
		Vector<Annotation> sources = VUtils.listify(first, second);
		this.textuallySortedSourceAnnotations = this.lexicallySortedSourceAnnotations = sources;
		this.tokens = VUtils.append(first.getTokens(), second.getTokens());
		this.tokenStart = first.getTokenStart();
		this.tokenEnd = second.getTokenEnd();
		this.textStart = first.getTextStart();
		this.textEnd = second.getTextEnd();
		this.sentenceTokenStart = this.textuallySortedSourceAnnotations
				.firstElement().sentenceTokenStart;
		this.sentenceTokenEnd = this.textuallySortedSourceAnnotations
				.lastElement().sentenceTokenEnd;
		this.relativeTokenStart = this.textuallySortedSourceAnnotations
				.firstElement().getRelativeTokenStart();
		this.relativeTokenEnd = this.textuallySortedSourceAnnotations
				.lastElement().getRelativeTokenEnd();
		String text = doc.getText().substring(this.textStart, this.textEnd + 1);
		this.setString(text);
		if (first.getSentenceAnnotation() == second.getSentenceAnnotation()) {
			this.setSentenceAnnotation(first.getSentenceAnnotation());
		}
		if (property != null && value != null) {
			si.setProperty(property, value);
		}
		if (relation != null) {
			RelationSentence rs = new RelationSentence(relation, a1, a2);
			si.setRelationSentences(VUtils.listify(rs));
		}
		this.getIndexTokens();
		this.setSignature();
		for (Annotation child : sources) {
			child.addParent(this);
		}
	}

	// 4/7/2015. Not tested...
	public boolean processQuery() {
		if (this.getRule() != null && this.getRule().getQuerySentence() != null
				&& KnowledgeBase.getCurrentKnowledgeBase() != null) {
			KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
			Sentence qs = this.getRule().getQuerySentence();
			KnowledgeEngine.setBreakAtFirstProof(true);
			// kb.initializeForm(qs);
			Vector<RelationSentence> localSentences = Annotation
					.getRelationSentences(this
							.getTextuallySortedSourceAnnotations());
			if (localSentences != null) {
				Vector<Vector<Variable>> results = Query.doQuery(kb, qs, null,
						localSentences, true);
				if (results != null && results.size() > 0) {
					Vector<Variable> vars = Variable.createBoundVariables(
							qs.getVariables(), results.firstElement());
					this.addVariables(vars);
				}
			}
			KnowledgeEngine.restoreBreakAtFirstProof();
		}
		return true;
	}

	public Annotation(String type) {
		super();
		this.setPropertyType(type);
	}

	public boolean isTerminal() {
		return this instanceof TerminalAnnotation;
	}

	public boolean isLeaf() {
		return this.getLexicallySortedSourceAnnotations() == null
				&& this.isUsedInHigherAnnotation();
	}

	public boolean isRoot() {
		return this.getLexicallySortedSourceAnnotations() != null
				&& !this.isUsedInHigherAnnotation();
	}

	public Vector<Annotation> getLeafSourceAnnotations() {
		if (this.getLexicallySortedSourceAnnotations() == null) {
			return VUtils.listify(this);
		}
		Vector leafs = null;
		for (Annotation source : this.getLexicallySortedSourceAnnotations()) {
			if (source.isLeaf()) {
				leafs = VUtils.add(leafs, source);
			} else {
				leafs = VUtils.append(leafs, source.getLeafSourceAnnotations());
			}
		}
		return leafs;
	}

	// public SnippetAnnotation createEVAnnotation(AnnotationCollection ac)
	// throws Exception {
	// SnippetAnnotation ev = (SnippetAnnotation) ac
	// .createAnnotation("SnippetAnnotation");
	// ev.setId(null);
	// ev.setText(getString());
	// ev.addSpan(getTextStart(), getTextEnd());
	// typesystem.Classification classification = ac.getAnalysis()
	// .getArrTool().getTypeSystem()
	// .getUimaClassification("CoreConcept");
	// Classification c = new Classification(ac, ev, classification,
	// "SnippetConcept", this.getConcept().toString(), null);
	// c.setProperty("SnippetCUI", this.getCui());
	// ev.setClassification(c);
	// ev.setAttribute("state", this.getValue());
	// for (int i = 0; i < this.getRelevantFeatures().length; i++) {
	// String attribute = this.getRelevantFeatures()[i];
	// Object value = this.getProperty(attribute);
	// if (value != null && !(value instanceof Annotation)) {
	// ev.setAttribute(attribute, value);
	// }
	// }
	// return ev;
	// }

	public Ontology getOntology() {
		if (this.ontology == null && this.rule != null) {
			this.ontology = this.rule.getOntology();
		}
		return this.ontology;
	}

	public boolean isTerminalWithInterpretation() {
		if (this.getProperty("cui") != null
				|| this.getProperty("concept") != null
				|| this.getProperty("macro") != null
				|| (this.getRule() != null && (this.getRule()
						.getResultConcept() != null
						|| this.getRule().getResultCUI() != null || this
						.getRule().getResultMacro() != null))) {
			return true;
		}
		return false;
	}

	public boolean isNonTerminalWithInterpretation() {
		if (!this.isTerminal()) {
			for (Annotation child : this.getChildAnnotations()) {
				if (child.getSemanticInterpretation() != null) {
					return true;
				}
			}
		}
		return false;
	}

	public String getMacro() {
		if (this.semanticInterpretation != null) {
			return this.semanticInterpretation.getMacro();
		}
		return null;
	}

	public boolean hasMacro() {
		return this.semanticInterpretation != null
				&& this.semanticInterpretation.hasMacro();
	}

	public boolean hasCui() {
		if (this.semanticInterpretation != null) {
			return this.semanticInterpretation.hasCui();
		}
		return false;
	}

	public String getCui() {
		if (this.semanticInterpretation != null) {
			return this.semanticInterpretation.getCui();
		}
		return null;
	}

	public boolean hasConcept() {
		if (this.semanticInterpretation != null) {
			return this.semanticInterpretation.hasConcept();
		}
		return false;
	}

	public Object getConcept() {
		if (this.concept == null && this.semanticInterpretation != null) {
			this.concept = this.semanticInterpretation.getConcept();
		}
		return this.concept;
	}

	public Object getExtendedConcept() {
		if (this.semanticInterpretation != null) {
			return this.semanticInterpretation.getExtendedConcept();
		}
		return null;
	}

	public TypeConstant getType() {
		if (this.semanticInterpretation != null) {
			return this.semanticInterpretation.getType();
		}
		return this.type;
	}

	public boolean hasTypeName(String tname) {
		return this.getSemanticInterpretation() != null
				&& this.getSemanticInterpretation().hasTypeName(tname);
	}

	public TypeConstant getGeneralType() {
		if (this.semanticInterpretation != null) {
			return this.semanticInterpretation.getGeneralType();
		}
		return null;
	}

	public boolean isInterpreted() {
		if (this.semanticInterpretation != null) {
			Interpretation si = this.semanticInterpretation;
			if (si.getCui() != null || si.getConcept() != null
					|| si.getType() != null
			// 9/10/2015: Don't store annotations that have only a macro, e.g.
			// prepositions
			// || si.getMacro() != null
			) {
				return true;
			}
		}
		return false;
	}

	// Before 11/10/2014 -- I am importing intepreted annotations from outside
	// rule system...
	// public boolean isInterpreted() {
	// return (this.rule != null && this.semanticInterpretation != null &&
	// (this.semanticInterpretation
	// .getCui() != null
	// || this.semanticInterpretation.getConcept() != null
	// || this.semanticInterpretation.getType() != null ||
	// this.semanticInterpretation
	// .getGeneralType() != null));
	// }

	public Vector<Annotation> getSourceAnnotations() {
		return textuallySortedSourceAnnotations;
	}

	public Vector<Annotation> getLexicallySortedSourceAnnotations() {
		return lexicallySortedSourceAnnotations;
	}

	public void setLexicallySortedSourceAnnotations(
			Vector<Annotation> lexicallySortedSourceAnnotations) {
		this.lexicallySortedSourceAnnotations = lexicallySortedSourceAnnotations;
	}

	public Vector<Annotation> getTextuallySortedSourceAnnotations() {
		return textuallySortedSourceAnnotations;
	}

	// public Annotation getChild(int index) {
	// return getTextualChild(index);
	// }

	public Annotation getTextualChild(int index) {
		if (index < this.textuallySortedSourceAnnotations.size()) {
			return this.textuallySortedSourceAnnotations.elementAt(index);
		}
		return null;
	}

	public Annotation getLexicalChild(int index) {
		if (index < this.lexicallySortedSourceAnnotations.size()) {
			return this.lexicallySortedSourceAnnotations.elementAt(index);
		}
		return null;
	}

	public void setTextuallySortedSourceAnnotations(
			Vector<Annotation> textuallySortedSourceAnnotations) {
		this.textuallySortedSourceAnnotations = textuallySortedSourceAnnotations;
	}

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}

	public boolean isUsedInHigherAnnotation() {
		return usedInHigherAnnotation;
	}

	public void setUsedInHigherAnnotation(boolean usedInHigherAnnotation) {
		this.usedInHigherAnnotation = usedInHigherAnnotation;
	}

	static int indent = 0;

	public String toString() {
		String text = this.getText();
		String str = "<";
		str += "\"";
		if (text.length() > 40) {
			str += text.substring(0, 20);
			str += "...";
			str += text.substring(text.length() - 20, text.length());
		} else {
			str += text;
		}
		str += "\":" + this.getTextStart() + "-" + this.getTextEnd() + ">";
		str += "<" + this.getAnnotationID() + ">";
		if (this.getRule() != null) {
			str += "[" + this.getRule().getRuleID() + "]";
			if (this.getRule().getPhraseType() != null) {
				str += "[" + this.getRule().getPhraseType() + "]";
			}
		}
		if (this.getConcept() != null) {
			str += "[" + this.getConcept() + "]";
		}
		if (this.getDocument() != null
				&& this.getDocument().getPatientName() != null) {
			str += "(" + this.getDocument().getPatientName() + ")";
		}
		str += "<"
				+ Annotation.getShortenedPercentString(this.getGoodness(), 4)
				+ ">";

		// 3/12/2016
		if (this.isInterpreted()) {
			str += "<Properties:"
					+ this.getSemanticInterpretation().getAllPropertyString()
					+ ">";
		}

		// 3/14/2016
		if (!this.isValid()) {
			str += "[INVALID]";
		}
		return str;
	}

	public String toExpandedString() {
		String str = "";
		str += "\"" + this.getText() + "\"";
		if (this.getType() != null) {
			str += "<" + this.getType() + ">";
		}
		if (this.getPhraseType() != null) {
			str += ":" + this.getPhraseType() + ":";
		}
		if (this.getSemanticInterpretation() != null) {
			String pstr = this.getSemanticInterpretation()
					.getCommaDelimitedPredString();
			if (pstr != null) {
				str += "[Properties: " + pstr + "]";
			} else if (this.isInterpreted()) {
				str += "[cui=" + this.getCui() + ",concept="
						+ this.getConcept() + "]";
			}
			if (this.getSemanticInterpretation().getRelationSentences() != null) {
				Vector<RelationSentence> rsv = this.getSemanticInterpretation()
						.getRelationSentences();
				Vector<RelationSentence> relevant = null;
				for (RelationSentence rs : rsv) {
					if (!"type-of".equals(rs.getRelation().getName())) {
						relevant = VUtils.add(relevant, rs);
					}
				}
				if (relevant != null) {
					String rstr = RelationSentence.toString(relevant);
					str += "[Relations=" + rstr + "]";
				}
			}
		}
		return str;
	}

	public String getText() {
		if (this.text == null && this.string != null) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < this.string.length(); i++) {
				char c = this.string.charAt(i);

				if (!(Character.isWhitespace(c) && !Character.isSpace(c))) {
					sb.append(c);
				} else {
					sb.append(" ");
				}
				// if (!Character.isWhitespace(c) || Character.isSpace(c)) {
				// sb.append(c);
				// }
			}
			this.text = sb.toString();
		}
		return this.text;
	}

	public static Vector<Annotation> getNonNestedTargetDocumentAnnotations(
			Vector<Annotation> annotations) {
		Vector<Annotation> targets = null;
		annotations = getNonNestedAnnotations(annotations);
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.containsTargetConcept()) {
					targets = VUtils.add(targets, annotation);
				}
			}
		}
		return targets;
	}

	public static Vector<Annotation> getNonNestedNonCoincidingAnnotations(
			Vector<Annotation> annotations, boolean interpretedOnly) {
		annotations = getNonNestedAnnotations(annotations);
		annotations = getNonCoincidingAnnotations(annotations, interpretedOnly);
		return annotations;
	}

	public static Vector<Annotation> getNonNestedAnnotations(
			Vector<Annotation> annotations) {
		Vector<Annotation> ancestors = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof StructureAnnotation) {
					ancestors = VUtils.add(ancestors, annotation);
				} else {
					Vector<Annotation> v = annotation.getNonNestedAncestors();
					if (v != null) {
						for (Annotation ancestor : v) {
							if (!ancestor.isVisited()) {
								ancestors = VUtils.add(ancestors, ancestor);
								ancestor.setVisited(true);
							}
						}
					}
				}
			}
			if (ancestors != null) {
				for (Annotation ancestor : ancestors) {
					ancestor.setVisited(false);
				}
			}
		}
		return ancestors;
	}

	// 11/12/2016: I am trying moving test for one phrase containing another
	// within the
	// decision list.
	public static Vector<Annotation> getNonCoincidingAnnotations(
			Vector<Annotation> annotations, boolean interpretedOnly) {
		int x = 1;
		if (annotations == null) {
			return null;
		}
		Vector<Annotation> largest = null;
		Hashtable<Annotation, Annotation> rhash = new Hashtable();

		// Collections.sort(annotations, new Annotation.TextLengthSorter());

		for (int i = 0; i < annotations.size() - 1; i++) {
			Annotation a1 = annotations.elementAt(i);

			if (rhash.get(a1) != null) {
				continue;
			}
			for (int j = i + 1; j < annotations.size(); j++) {
				Annotation a2 = annotations.elementAt(j);

				// if (a1.getText() == null || a2.getText() == null) {
				// System.out
				// .println("Annotation.getNonCoincidingAnnotations():  Annotation text is null!");
				// }

				if (rhash.get(a2) != null) {
					continue;
				}

				Annotation remove = null;

				if (eitherCointainsOrCoincides(a1, a2)) {
					if (a1.getMoonstoneRuleInterface()
							.isCompareAnnotationsTargetsAndGoodness()) {
						remove = selectRemovableTargetPlusGoodness(a1, a2);
					} else {
						remove = selectRemovableGoodnessOnly(a1, a2);
					}
				}
				if (remove != null) {
					if (remove.containsTargetConcept()
							&& remove.getConcept().toString().toLowerCase()
									.contains("support")) {
						x = 1;
					}
					rhash.put(remove, remove);
				}
			}
		}
		for (Annotation ca : annotations) {
			if (rhash.get(ca) == null) {
				if (!interpretedOnly || ca.isInterpreted()) {
					largest = VUtils.add(largest, ca);
				}
			}
		}
		return largest;
	}

	private static Annotation selectRemovableGoodnessOnly(Annotation a1,
			Annotation a2) {
		Annotation remove = null;
		if (annotationContains(a1, a2)) {
			remove = a2;
		} else if (annotationContains(a2, a1)) {
			remove = a1;
		} else if (annotationsCoincide(a1, a2)) {
			Annotation best = annotationComparisonDecisionList(a1, a2);
			if (best != null) {
				remove = (best == a1 ? a2 : a1);
			}
		}
		return remove;
	}

	private static Annotation selectRemovableTargetPlusGoodness(Annotation a1,
			Annotation a2) {
		Annotation remove = null;

		if (a1.containsTargetConcept() && !a2.containsTargetConcept()) {
			remove = a2;
		} else if (a2.containsTargetConcept() && !a1.containsTargetConcept()) {
			remove = a1;
		} else {
			// Original before 11/13/2016
			if (annotationContains(a1, a2)) {
				remove = a2;
			} else if (annotationContains(a2, a1)) {
				remove = a1;
			} else if (annotationsCoincide(a1, a2)) {
				Annotation best = annotationComparisonDecisionList(a1, a2);
				if (best != null) {
					remove = (best == a1 ? a2 : a1);
				}
			}
		}
		return remove;
	}

	// 3/20/2015: Return all annotations without parents.
	public static Vector<Annotation> getParentlessAnnotations(
			Vector<Annotation> annotations) {
		Vector<Annotation> parentless = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.parentAnnotations == null) {
					parentless = VUtils.add(parentless, annotation);
				}
			}
		}
		return parentless;
	}

	public Vector<Annotation> getNonNestedAncestors() {
		Vector<Annotation> ancestors = null;
		if (this.parentAnnotations == null) {
			if (this.getRule() != null && this.getRule().isIntermediate()) {
				return null;
			}
			return VUtils.listify(this);
		}
		for (Annotation parent : this.parentAnnotations) {
			if (parent.isValid()) {
				ancestors = VUtils.append(ancestors,
						parent.getNonNestedAncestors());
			}
		}
		return ancestors;
	}

	// 4/21/2015
	public boolean isAncestorOf(Annotation other) {
		if (this.contains(other)) {
			if (this.getChildAnnotations() != null) {
				if (this.getChildAnnotations().contains(other)) {
					return true;
				}
				for (Annotation child : this.getChildAnnotations()) {
					if (child.isAncestorOf(other)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean containsTargetConcept() {
		Object property = this.getProperty("contains-target");
		return (property != null);
	}

	public Vector getNestedTargetConcepts() {
		return nestedTargetConcepts;
	}

	public int getNestedTargetConceptCount() {
		return VUtils.size(this.nestedTargetConcepts);
	}

	public String getNestedTargetConceptString() {
		String str = "";
		if (this.nestedTargetConcepts != null) {
			for (Object c : this.nestedTargetConcepts) {
				str += c.toString() + ",";
			}
		}
		return str;
	}

	// 12/1/2017: Nested targets sometimes carry contradictory states...
	public static Vector<Annotation> getNonNegativeNestedTargetAnnotations(
			MoonstoneRuleInterface msri, Vector<Annotation> annotations) {
		Vector<Annotation> nonnegs = null;
		Hashtable<String, String> chash = new Hashtable();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				String astr = annotation.getConcept().toString();
				if (nonnegs == null) {
					nonnegs = VUtils.add(nonnegs, annotation);
					chash.put(astr, astr);
				} else {
					boolean found = false;
					for (Annotation nonneg : nonnegs) {
						String nnstr = nonneg.getConcept().toString();
						if (msri.getReadmission().conceptsAreNegative(nnstr,
								astr)) {
							found = true;
							break;
						}
					}
					if (!found && chash.get(astr) == null) {
						nonnegs = VUtils.add(nonnegs, annotation);
						chash.put(astr, astr);
					}
				}
			}
		}
		return nonnegs;
	}

	// 12/1/2017: NEED TO STORE NESTED ANNOTATIONS THAT DON'T CONTRADICT,
	// e.g. shouldn't store "no family at bedside" and "family at bedside"
	public void gatherNestedTargetAnnotations() {
		boolean neg = !JavaFunctions.isNegated(this);
		if (this.containsTargetConcept() && !JavaFunctions.isNegated(this)) {
			this.nestedTargetAnnotations = VUtils.listify(this);
			this.nestedTargetConcepts = VUtils.listify(this.getConcept());
		}
		if (this.hasChildren()) {
			for (Annotation child : this.getChildAnnotations()) {
				if (child.nestedTargetAnnotations != null
						&& !(child.getRule() != null && child.getRule()
								.isCaptureRule())) {
					this.nestedTargetConcepts = VUtils.appendIfNot(
							this.nestedTargetConcepts,
							child.nestedTargetConcepts);
					this.nestedTargetAnnotations = VUtils.append(
							this.nestedTargetAnnotations,
							child.nestedTargetAnnotations);
				}
			}
		}
	}

	public void gatherNestedTargetAnnotations_BEFORE_12_1_2017() {
		boolean neg = !JavaFunctions.isNegated(this);
		if (this.containsTargetConcept() && !JavaFunctions.isNegated(this)) {
			this.nestedTargetAnnotations = VUtils.listify(this);
			this.nestedTargetConcepts = VUtils.listify(this.getConcept());
		}
		if (this.hasChildren()) {
			for (Annotation child : this.getChildAnnotations()) {
				if (child.nestedTargetAnnotations != null
						&& !(child.getRule() != null && child.getRule()
								.isCaptureRule())) {
					this.nestedTargetConcepts = VUtils.appendIfNot(
							this.nestedTargetConcepts,
							child.nestedTargetConcepts);
					this.nestedTargetAnnotations = VUtils.append(
							this.nestedTargetAnnotations,
							child.nestedTargetAnnotations);
				}
			}
		}
	}

	public void gatherNestedTargetAnnotations_BEFORE_6_22_2017() {
		if (this.containsTargetConcept() && !JavaFunctions.isNegated(this)) {
			this.nestedTargetAnnotations = VUtils.listify(this);
			this.nestedTargetConcepts = VUtils.listify(this.getConcept());
		}
		if (this.hasChildren()) {
			for (Annotation child : this.getChildAnnotations()) {
				if (child.nestedTargetAnnotations != null
						&& !(child.getRule() != null && child.getRule()
								.isCaptureRule())) {
					this.nestedTargetConcepts = VUtils.appendIfNot(
							this.nestedTargetConcepts,
							child.nestedTargetConcepts);
					this.nestedTargetAnnotations = VUtils.append(
							this.nestedTargetAnnotations,
							child.nestedTargetAnnotations);
				}
			}
		}
	}

	public Vector<Annotation> getNestedTargetAnnotations() {
		return nestedTargetAnnotations;
	}

	public int getNestedTargetAnnotationCount() {
		return VUtils.size(this.nestedTargetAnnotations);
	}

	public void gatherTopmostNestedTargetAnnotations() {
		if (this.containsTargetConcept() && !JavaFunctions.isNegated(this)) {
			this.topmostNestedTargetAnnotations = VUtils.listify(this);
		} else if (this.hasChildren()) {
			for (Annotation child : this.getChildAnnotations()) {
				if (child.nestedTargetAnnotations != null
						&& !child.getRule().isCaptureRule()) {
					this.topmostNestedTargetAnnotations = VUtils.append(
							this.topmostNestedTargetAnnotations,
							child.topmostNestedTargetAnnotations);
				}
			}
		}
	}

	public Vector<Annotation> getTopmostNestedTargetAnnotations() {
		return this.topmostNestedTargetAnnotations;
	}

	public Vector<Annotation> getTopmostNestedTargetAnnotations_BEFORE_11_7_2016() {
		if (this.topmostNestedTargetAnnotations == null) {
			this.topmostNestedTargetAnnotations = new Vector(0);
			if (this.containsTargetConcept() && !JavaFunctions.isNegated(this)) {
				this.topmostNestedTargetAnnotations.add(this);
			} else if (this.hasChildren()) {
				for (Annotation child : this.getChildAnnotations()) {
					if (child.nestedTargetAnnotations != null
							&& !(child.getRule() != null && child.getRule()
									.isCaptureRule())) {
						this.topmostNestedTargetAnnotations = VUtils.append(
								this.topmostNestedTargetAnnotations,
								child.getTopmostNestedTargetAnnotations());
					}
				}
			}
		}
		Vector v = this.topmostNestedTargetAnnotations;
		return this.topmostNestedTargetAnnotations;
	}

	public static class TokenIndexSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Annotation a1 = (Annotation) o1;
			Annotation a2 = (Annotation) o2;
			if (a1.getTokenStart() < a2.getTokenStart()) {
				return -1;
			}
			if (a2.getTokenStart() < a1.getTokenStart()) {
				return 1;
			}
			if (a1.getTokenEnd() > a2.getTokenEnd()) {
				return -1;
			}
			if (a2.getTokenEnd() > a1.getTokenEnd()) {
				return 1;
			}
			return 0;
		}
	}

	public static class TextCoverageSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Annotation a1 = (Annotation) o1;
			Annotation a2 = (Annotation) o2;
			if (a1.isWord()) {
				return -1;
			}
			if (a2.isWord()) {
				return 1;
			}
			if (a1.getCoveredTextLength() > a2.getCoveredTextLength()) {
				return -1;
			}
			if (a1.getCoveredTextLength() < a2.getCoveredTextLength()) {
				return 1;
			}
			return 0;
		}
	}

	public static class GoodnessSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Annotation a1 = (Annotation) o1;
			Annotation a2 = (Annotation) o2;
			double g1 = a1.getGoodness();
			double g2 = a2.getGoodness();
			if (g1 > g2) {
				return -1;
			}
			if (g1 < g2) {
				return 1;
			}
			return 0;
		}
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getTokenDistance(Annotation other) {
		if (this.before(other)) {
			return other.getTokenStart() - this.getTokenEnd();
		}
		if (other.before(this)) {
			return this.getTokenStart() - other.getTokenEnd();
		}
		return 0;
	}

	public boolean containsConcept() {
		return this.getCui() != null || this.getConcept() != null;
	}

	public Document getDocument() {
		if (this.getSentenceAnnotation() != null) {
			return this.getSentenceAnnotation().getDocument();
		}
		return null;
	}

	public boolean hasSameConcept(Annotation other) {
		return ((this.getConcept() != null && this.getConcept().equals(
				other.getConcept())) || (this.getCui() != null && this.getCui()
				.equals(other.getCui())));
	}

	public static class TextStartSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Annotation a1 = (Annotation) o1;
			Annotation a2 = (Annotation) o2;
			if (a1.getTextStart() < a2.getTextStart()) {
				return -1;
			}
			if (a2.getTextStart() < a1.getTextStart()) {
				return 1;
			}
			if (a1.getTextEnd() > a2.getTextEnd()) {
				return -1;
			}
			if (a2.getTextEnd() > a1.getTextEnd()) {
				return 1;
			}
			if (a1.getGoodness() > a2.getGoodness()) {
				return -1;
			}
			if (a2.getGoodness() > a1.getGoodness()) {
				return 1;
			}
			return 0;
		}
	}

	public static class StartPositionSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Annotation a1 = (Annotation) o1;
			Annotation a2 = (Annotation) o2;
			if (a1.getTokenStart() < a2.getTokenStart()) {
				return -1;
			}
			if (a2.getTokenStart() < a1.getTokenStart()) {
				return 1;
			}
			if (a1.getTokenEnd() > a2.getTokenEnd()) {
				return -1;
			}
			if (a2.getTokenEnd() > a1.getTokenEnd()) {
				return 1;
			}
			return 0;
		}
	}

	public static class TextLengthSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			try {
				Annotation a1 = (Annotation) o1;
				Annotation a2 = (Annotation) o2;
				if (a1.getTextlength() > a2.getTextLength()) {
					return -1;
				}
				if (a2.getTextLength() > a1.getTextLength()) {
					return 1;
				}
				return 0;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
	}

	public boolean before(Annotation annotation) {
		return SeqUtils.before(this.getTokenStart(), this.getTokenEnd(),
				annotation.getTokenStart(), annotation.getTokenEnd());
	}

	public boolean after(Annotation annotation) {
		return SeqUtils.after(this.getTokenStart(), this.getTokenEnd(),
				annotation.getTokenStart(), annotation.getTokenEnd());
	}

	public boolean disjoint(Annotation annotation) {
		int dist = this.getTokenDistance(annotation);
		return dist > 0;
	}

	public boolean contains(Annotation annotation) {
		return !this.equals(annotation)
				&& SeqUtils.contains(this.getTokenStart(), this.getTokenEnd(),
						annotation.getTokenStart(), annotation.getTokenEnd());
	}

	public boolean covers(Annotation other) {
		return (!this.equals(other) && this.tokenStart <= other.tokenStart && this.tokenEnd >= other.tokenEnd);
	}

	public boolean strictlyCovers(Annotation other) {
		return this.covers(other)
				&& (this.tokenStart < other.tokenStart || this.tokenEnd > other.tokenEnd);
	}

	public boolean coincides(Annotation other) {
		if (!this.equals(other) && this.tokenStart == other.tokenStart
				&& this.tokenEnd == other.tokenEnd) {
			return true;
		}
		return false;
	}

	boolean isNonNestedConcept() {
		UMLSStructuresShort umlss = UMLSStructuresShort.getUMLSStructures();
		String concept = (String) this.getConcept();
		String cui = this.getCui();
		CUIStructureShort cp = umlss.getCUIStructure(cui);
		boolean hasConcept = (concept != null);
		boolean usedInHigher = this.isUsedInHigherAnnotation();
		boolean isCore = (hasConcept && AnnotationIntegrationMaps
				.getCUI(concept) != null);
		boolean useOnlyCore = this.grammar.isUseOnlyCoreConceptAnnotations();
		boolean hasCui = (cui != null);
		if (this instanceof WordSequenceAnnotation) {
			return false;
		}
		if (usedInHigher) {
			return false;
		}

		// 7/2/2013: Using rules imported from Onyx training cases
		if (this.getRule() != null && this.getRule().getOntology() != null
				&& this.getRule().getOntology().isOnyx() && this.hasMacro()) {
			return true;
		}

		if (useOnlyCore && !isCore) {
			return false;
		}
		if (!hasCui) {
			return false;
		}
		if (cp != null && cp.getType() != null
				&& !(cp.getType().isCondition() || cp.getType().isLocation())) {
			return false;
		}
		// 3/25/2014
		// if (ptype != null && !("NP".equals(ptype) || "AP".equals(ptype))) {
		// return false;
		// }
		return true;
	}

	public WordSequenceAnnotation getSentenceAnnotation() {
		if (this.sentenceAnnotation == null
				&& this instanceof WordSequenceAnnotation) {
			this.sentenceAnnotation = (WordSequenceAnnotation) this;
		}
		return sentenceAnnotation;
	}

	public void setSentenceAnnotation(WordSequenceAnnotation sentenceAnnotation) {
		this.sentenceAnnotation = sentenceAnnotation;
	}

	// public void resetSentenceAnnotation(
	// WordSequenceAnnotation sentenceAnnotation) {
	// this.setSentenceAnnotation(sentenceAnnotation);
	// if (this.textuallySortedSourceAnnotations != null) {
	// Token first = this.textuallySortedSourceAnnotations.firstElement()
	// .getTokens().firstElement();
	// Token last = this.textuallySortedSourceAnnotations.lastElement()
	// .getTokens().lastElement();
	// this.relativeTokenStart = first.getIndex();
	// this.relativeTokenEnd = last.getIndex();
	// }
	// }

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
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

	public void setTokenEnd(int tokenEnd) {
		this.tokenEnd = tokenEnd;
	}

	public int getTokenLength() {
		return (this.tokenEnd - this.tokenStart) + 1;
	}

	public int getRelativeTokenStart() {
		return relativeTokenStart;
	}

	public void setRelativeTokenStart(int relativeTokenStart) {
		this.relativeTokenStart = relativeTokenStart;
	}

	public int getRelativeTokenEnd() {
		return relativeTokenEnd;
	}

	public void setRelativeTokenEnd(int relativeTokenEnd) {
		this.relativeTokenEnd = relativeTokenEnd;
	}

	public Vector<Token> getTokens() {
		Document document = this.getDocument();
		if (this.tokens == null && document != null
				&& document.getTokens() != null && this.getTokenStart() >= 0) {
			this.tokens = VUtils.subVector(document.getTokens(),
					this.getTokenStart(), this.getTokenEnd() + 1);
		}
		return this.tokens;
	}

	public Vector<Word> getCoveredWords() {
		if (this.coveredWords == null) {
			Vector<Token> tokens = this.getTokens();
			this.coveredWords = Token.gatherWords(tokens, true);
		}
		return this.coveredWords;
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

	public int getTextLength() {
		return this.textEnd - this.textStart;
	}

	public boolean sameOntology(Annotation other) {
		return ((this.ontology == null && other.ontology == null) || (this.ontology != null && this.ontology
				.equals(other.ontology)));
	}

	public Vector<Annotation> getChildAnnotations() {
		return this.textuallySortedSourceAnnotations;
	}

	public boolean hasChildren() {
		return this.textuallySortedSourceAnnotations != null;
	}

	public Annotation getSingleChild() {
		if (this.textuallySortedSourceAnnotations != null
				&& this.getTextuallySortedSourceAnnotations().size() == 1) {
			return this.textuallySortedSourceAnnotations.firstElement();
		}
		return null;
	}

	// FROM PROFILER: TAKES 50% OF THE TIME
	// 3/26/2015: Need to rethink this...
	public Object getPropertyFromRuleOrSourceAnnotations(String attribute) {
		Object value = null;
		Rule rule = this.getRule();
		if (rule != null) {
			value = rule.getProperty(attribute);

			// 6/21/2016 TEST: CREATE COMPLEX CONCEPTS ON THE FLY
			if (value == null && "concept".equals(attribute)
					&& rule.isComplexConcept()) {
				String cname = "";
				for (Annotation child : this.getChildAnnotations()) {
					if (child.getConcept() != null) {
						cname += child.getConcept().toString();
					}
				}
				TypeConstant root = rule.getGrammar().getGrammarModule()
						.getKnowledgeEngine().getCurrentOntology()
						.getRootType();
				StringConstant sc = StringConstant.createStringConstant(cname,
						root, true);
				value = sc;
				int x = 1;
			}

			if (value == null && this.getSourceAnnotations() != null
					&& rule.isInheritProperties()
					&& !rule.isPropertyToRemove(attribute)) {
				for (Annotation source : this.getSourceAnnotations()) {
					Interpretation si = source.getSemanticInterpretation();
					if (si != null) {
						value = si.getProperty(attribute);
						if (value != null) {
							break;
						}
					}
				}
			}
			if (value != null && value instanceof String
					&& ((String) value).length() > 0) {
				value = this.evalPattern(value, this.getVariables());
			}
		}
		return value;
	}

	// Before 6/21/2016
	// public Object getPropertyFromRuleOrSourceAnnotations(String attribute) {
	// Object value = null;
	// if (this.getRule() != null) {
	// value = this.getRule().getProperty(attribute);
	// }
	// if (value == null && this.getSourceAnnotations() != null &&
	// this.getRule().isInheritProperties()
	// && !this.getRule().isPropertyToRemove(attribute)) {
	// for (Annotation source : this.getSourceAnnotations()) {
	// Interpretation si = source.getSemanticInterpretation();
	// if (si != null) {
	// value = si.getProperty(attribute);
	// if (value != null) {
	// break;
	// }
	// }
	// }
	// }
	// if (value != null && value instanceof String && ((String) value).length()
	// > 0) {
	// value = this.evalPattern(value, this.getVariables());
	// }
	// return value;
	// }

	public Vector<String> getIndexTokens() {
		if (this.indexTokens == null) {
			this.generateIndexTokens();
		}
		return this.indexTokens;
	}

	public void setIndexTokens(Vector<String> indexTokens) {
		this.indexTokens = indexTokens;
	}

	// 8/10/2016: Adding indices for all phraseType/UnifiabloeSemanticType
	// combinations. (Not yet tested...)
	// THIS MAY BE TOO EXPENSIVE!!!
	public void generateIndexTokens() {
		Rule rule = this.getRule();
		if (this.getRule() != null && this.getRule().isDoDebug()) {
			int x = 1;
		}

		// 5/19/2016
		if (rule != null && rule.isIntermediate()) {
			String rid = '@' + this.getRule().getRuleID() + '@';
			this.indexTokens = VUtils.add(this.indexTokens, rid);
			return;
		}

		this.indexTokens = null;
		Object concept = this.getConcept();
		if (this.isTerminal() || (concept instanceof Constant)) {
			this.indexTokens = VUtils.add(this.indexTokens, concept);
		}

		if (this.getExtendedConcept() instanceof Constant && rule != null
				&& rule.isComplexConcept()) {
			// Object ec = this.getExtendedConcept();
			// this.indexTokens = VUtils.add(this.indexTokens, ec);
		}

		if (this.getCui() != null) {
			this.indexTokens = VUtils.add(this.indexTokens, this.getCui());
		}

		this.indexTokens = VUtils.add(this.indexTokens, this.getMacro());

		if (this.getType() != null) {
			TypeConstant type = this.getType();
			if (type.getUnifiableTypes() == null) {
				this.indexTokens = VUtils.add(this.indexTokens, type);

				// 8/10/2016
				if (this.getPhraseType() != null) {
					this.indexTokens = VUtils.add(
							this.indexTokens,
							Annotation.getPhrasePlusTypeIndex(
									this.getPhraseType(), this.getType()));
				}

			} else {
				for (TypeConstant unifier : this.getType().getUnifiableTypes()) {
					if (!unifier.isRoot()) {
						this.indexTokens = VUtils
								.add(this.indexTokens, unifier);

						// 8/10/2016
						if (this.getPhraseType() != null) {
							this.indexTokens = VUtils.add(
									this.indexTokens,
									Annotation.getPhrasePlusTypeIndex(
											this.getPhraseType(), unifier));
						}
					}
				}
			}
		}

		// 9/13/2015, e.g. "object=:TRANSPORTATION:"
		// for (int i = 0; i < IndexTokenProperties.length; i++) {
		// String property = IndexTokenProperties[i];
		// Object value = this.getProperty(property);
		// Object c = null;
		// if (value instanceof Annotation) {
		// c = ((Annotation) value).getConcept();
		// } else if (value instanceof Constant || value instanceof String) {
		// c = value.toString();
		// }
		// if (c != null) {
		// String token = property + "=" + c.toString();
		// this.indexTokens = VUtils.add(this.indexTokens, token);
		// }
		// }

		if (this.getPhraseType() != null) {
			this.indexTokens = VUtils.add(this.indexTokens,
					this.getPhraseType());
		}

		// 8/15/2015: Don't need these tokens.
		// this.indexTokens = VUtils.add(this.indexTokens,
		// this.getPhrasePlusTypeIndex());

		if (this.isInterpreted()) {
			this.indexTokens = VUtils.add(this.indexTokens, "?interpreted");
		}

		// 5/16/2015
		if (this.getRule() != null) {
			String rid = '@' + this.getRule().getRuleID() + '@';
			this.indexTokens = VUtils.add(this.indexTokens, rid);
		}

		// 8/3/2016: For access to partial complex concepts, e.g. "%:HOME:%"
		if (rule != null && rule.isComplexConcept()) {
			// for (Annotation child : this.getChildAnnotations()) {
			// Object cconcept = child.getConcept();
			// if (cconcept != null) {
			// String ccid = '%' + cconcept.toString() + '%';
			// this.indexTokens = VUtils.add(this.indexTokens, ccid);
			// }
			// }
		}

		if (rule != null && rule.isDoDebug()) {
			if (this.indexTokens != null) {
				for (Object token : this.indexTokens) {
					if (token.toString().toLowerCase()
							.equals(":LIVE_IN_NURSING_HOME")) {
						int x = 1;
					}
				}
			}
			int x = 1;
		}
	}

	public void generateIndexTokens_BEFORE_8_10_2016() {
		Rule rule = this.getRule();
		if (this.getRule() != null && this.getRule().isDoDebug()) {
			int x = 1;
		}

		// 5/19/2016
		if (rule != null && rule.isIntermediate()) {
			String rid = '@' + this.getRule().getRuleID() + '@';
			this.indexTokens = VUtils.add(this.indexTokens, rid);
			return;
		}

		this.indexTokens = null;
		Object concept = this.getConcept();
		if (this.isTerminal() || (concept instanceof Constant)) {
			this.indexTokens = VUtils.add(this.indexTokens, concept);
		}

		// 6/28/2016
		if (this.getExtendedConcept() instanceof Constant && rule != null
				&& rule.isComplexConcept()) {
			Object ec = this.getExtendedConcept();
			this.indexTokens = VUtils.add(this.indexTokens, ec);
		}

		if (this.getCui() != null) {
			this.indexTokens = VUtils.add(this.indexTokens, this.getCui());
		}

		this.indexTokens = VUtils.add(this.indexTokens, this.getMacro());

		if (this.getType() != null) {
			TypeConstant type = this.getType();
			if (type.getUnifiableTypes() == null) {
				this.indexTokens = VUtils.add(this.indexTokens, type);

			} else {
				for (TypeConstant unifier : this.getType().getUnifiableTypes()) {
					if (!unifier.isRoot()) {
						this.indexTokens = VUtils
								.add(this.indexTokens, unifier);
					}
				}
			}
		}

		// 9/13/2015, e.g. "object=:TRANSPORTATION:"
		// for (int i = 0; i < IndexTokenProperties.length; i++) {
		// String property = IndexTokenProperties[i];
		// Object value = this.getProperty(property);
		// Object c = null;
		// if (value instanceof Annotation) {
		// c = ((Annotation) value).getConcept();
		// } else if (value instanceof Constant || value instanceof String) {
		// c = value.toString();
		// }
		// if (c != null) {
		// String token = property + "=" + c.toString();
		// this.indexTokens = VUtils.add(this.indexTokens, token);
		// }
		// }

		if (this.getPhraseType() != null) {
			this.indexTokens = VUtils.add(this.indexTokens,
					this.getPhraseType());
		}

		// 8/15/2015: Don't need these tokens.
		this.indexTokens = VUtils.add(this.indexTokens,
				this.getPhrasePlusTypeIndex());

		if (this.isInterpreted()) {
			this.indexTokens = VUtils.add(this.indexTokens, "?interpreted");
		}

		// 5/16/2015
		if (this.getRule() != null) {
			String rid = '@' + this.getRule().getRuleID() + '@';
			this.indexTokens = VUtils.add(this.indexTokens, rid);
		}

		// 8/3/2016: For access to partial complex concepts, e.g. "%:HOME:%"
		if (rule != null && rule.isComplexConcept()) {
			for (Annotation child : this.getChildAnnotations()) {
				Object cconcept = child.getConcept();
				if (cconcept != null) {
					String ccid = '%' + cconcept.toString() + '%';
					this.indexTokens = VUtils.add(this.indexTokens, ccid);
				}
			}
		}

		if (rule != null && rule.isDoDebug()) {
			int x = 1;

		}
	}

	public SyntacticTypeConstant getPhraseType() {
		return this.phraseType;
	}

	public String getPhraseToken() {
		return this.phraseToken;
	}

	public void setPhraseType(SyntacticTypeConstant phraseType) {
		if (phraseType != null) {
			int x = 1;
		}
		this.phraseType = phraseType;
	}

	public String getPhrasePlusTypeIndex() {
		if (this.getPhraseType() != null && this.getType() != null) {
			String str = this.getPhraseType().getFormalName() + "="
					+ this.getType().getFormalName();
			return str;
		}
		return null;
	}

	public static String getPhrasePlusTypeIndex(SyntacticTypeConstant ptc,
			TypeConstant tc) {
		if (ptc != null && tc != null) {
			String str = ptc.getFormalName() + "=" + tc.getFormalName();
			return str;
		}
		return null;
	}

	public int getSentenceTokenStart() {
		return sentenceTokenStart;
	}

	public void setSentenceTokenStart(int sentenceTokenStart) {
		this.sentenceTokenStart = sentenceTokenStart;
	}

	public int getSentenceTokenEnd() {
		return sentenceTokenEnd;
	}

	public void setSentenceTokenEnd(int sentenceTokenEnd) {
		this.sentenceTokenEnd = sentenceTokenEnd;
	}

	public void addParentAnnotation(Annotation parent) {
		this.setUsedInHigherAnnotation(true);
		this.parentAnnotations = VUtils.add(this.parentAnnotations, parent);
	}

	public CUIStructureShort getCuiStruct() {
		return UMLSStructuresShort.getUMLSStructures().getCUIStructure(
				this.getCui());
	}

	public TypeConstant getUMLSType() {
		CUIStructureShort cs = getCuiStruct();
		if (cs != null) {
			return (TypeConstant) cs.getType();
		}
		return null;
	}

	public Interpretation getSemanticInterpretation() {
		return this.semanticInterpretation;
	}

	public Interpretation getSemanticInterpretation(KnowledgeBase kb) {
		return this.semanticInterpretation;
	}

	public boolean isSemanticTypeCondition() {
		TypeConstant type = (TypeConstant) this.getUMLSType();
		if (type != null) {
			return type.isCondition();
		}
		return false;
	}

	public boolean isSemanticTypeLocation() {
		TypeConstant type = (TypeConstant) this.getUMLSType();
		if (type != null) {
			return type.isLocation();
		}
		return false;
	}

	public void addFlag(String flag) {
		this.flags = VUtils.add(this.flags, flag);
	}

	public boolean flagExists(String flag) {
		return this.flags != null && this.flags.contains(flag);
	}

	public boolean isTagged() {
		return this.flagExists("tagged");
	}

	public void setSemanticInterpretation(Interpretation semanticInterpretation) {
		this.semanticInterpretation = semanticInterpretation;
	}

	public String getSignature() {
		return this.signature;
	}

	public int getSignatureID() {
		return this.signatureID;
	}

	public static int LastSignatureID = 0;

	public void setSignature() {
		if (this.signature == null) {
			this.signatureID = LastSignatureID++;
			StringBuffer sb = new StringBuffer();
			sb.append("[");

			String startend = "(" + this.getTokenStart() + "-"
					+ this.getTokenEnd() + ")";
			sb.append(startend + "^");

			String rulestr = (this.getRule() != null ? String.valueOf(this
					.getRule().getRuleIDNum()) : "*");
			sb.append(rulestr + "^");

			String cuistr = (this.getCui() != null ? this.getCui() : "*");
			sb.append(cuistr + "^");

			// 6/23/2016
			String conceptstr = (this.getConcept() != null ? this.getConcept()
					.toString() : "*");
			sb.append(conceptstr + "^");

			// 6/28/2016
			// String ecstr = (this.getExtendedConcept() != null ?
			// this.getExtendedConcept().toString() : "*");
			// sb.append(ecstr + "^");

			String typestr = (this.getType() != null ? String.valueOf(this
					.getType().getNumericID()) : "*");
			sb.append(typestr + "^");

			String ptypestr = (this.getPhraseType() != null ? String
					.valueOf(this.getPhraseType().getNumericID()) : "*");
			sb.append(ptypestr + "^");

			if (this.hasChildren()) {
				sb.append("(");
				for (int i = 0; i < this.getNumberOfChildren(); i++) {
					Annotation child = this.getLexicalChild(i);

					// sb.append(child.getSignatureID());

					if (child.getType() != null || child.getRule() != null) {
						String cstr = "[";
						if (child.getType() != null) {
							String cts = String.valueOf(child.getType()
									.getNumericID());
							cstr += cts + "-";
						}
						if (child.getRule() != null) {
							String crs = String.valueOf(child.getRule()
									.getRuleIDNum());
							cstr += crs;
						}
						cstr += "]";
						sb.append(cstr);
					}
					if (i < this.getNumberOfChildren() - 1) {
						sb.append(",");
					}
				}
				sb.append(")");
			}
			sb.append("]");
			this.signature = sb.toString();
		}
	}

	public boolean checkIsValid() {
		return true;
	}

	protected boolean typeLoopCheck(Annotation parent) {
		Rule rule = this.rule;
		if (this.isSingleton()) {
			Annotation child = this.textuallySortedSourceAnnotations
					.firstElement();
			if (parent.getRule() == child.getRule()
					&& parent.getConcept().equals(child.getConcept())) {
				return true;
			}
			// If there is just one child, and the type is specialized visavis
			// the component
			// type, then it needs to add specialization via tests.
			if (this.getType() != null && child.getType() != null
					&& this.getType().isSubsumedBy(child.getType())
					&& !this.containsTests()) {
				return true;
			}
			return child.typeLoopCheck(parent);
		}
		return false;
	}

	// 5/14/2015: To tell whether a rule tests (and therefore specializes)
	// it's elements.
	protected boolean containsTests() {
		Rule rule = this.getRule();
		if (rule != null
				&& (rule.getLocalVariables() != null
						|| rule.getTestPredicates() != null || rule
						.getSubpatternLists() != null)) {
			return true;
		}
		return false;
	}

	protected boolean isSingleton() {
		return this.textuallySortedSourceAnnotations != null
				&& this.textuallySortedSourceAnnotations.size() == 1;
	}

	// 12/7/2014: For use from outside parser, e.g. TSL
	public static boolean intervalContainsStrings(Annotation a1, Annotation a2,
			Vector<String> strs) {
		if (a1 != null && a2 != null
				&& a1.getSentenceAnnotation() == a2.getSentenceAnnotation()
				&& strs != null) {
			Vector<Token> tokens = a1.getSentenceAnnotation().getSentence()
					.getTokens();
			Annotation first = (a1.getTextStart() < a2.getTextStart() ? a1 : a2);
			Annotation second = (a1 == first ? a2 : a1);
			if (first.getTokenEnd() < second.getTokenStart()) {
				for (int i = first.getRelativeTokenEnd() + 1; i < second
						.getRelativeTokenStart() - 1; i++) {
					Token token = tokens.elementAt(i);
					if (strs.contains(token.getString())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public String getConjunct() {
		for (int i = 0; i < Annotation.conjunctiveWords.length; i++) {
			String cstr = Annotation.conjunctiveWords[i];
			if (this.getString().contains(cstr)) {
				return cstr;
			}
		}
		return null;
	}

	public static String getTypeIndex(TypeConstant type) {
		if (type != null) {
			return type.getFormalName();
		}
		return null;
	}

	public String getTypeIndex() {
		return getTypeIndex(this.getType());
	}

	public String getGeneralTypeIndex() {
		return getTypeIndex(this.getGeneralType());
	}

	public Vector<String> getParentTypeIndices() {
		return getAllParentTypeIndices(this.getSemanticInterpretation()
				.getGeneralType());
	}

	public static Vector<String> getAllParentTypeIndices(TypeConstant type) {
		Vector<String> indices = null;
		if (type != null) {
			String tstr = getTypeIndex(type);
			indices = VUtils.add(indices, tstr);
			if (type.getParents() != null) {
				TypeConstant ptype = (TypeConstant) type.getParents()
						.firstElement();
				indices = VUtils
						.append(indices, getAllParentTypeIndices(ptype));
			}
		}
		return indices;
	}

	public static boolean isSpecialAnnotation(String str) {
		for (int i = 0; i < SpecialPunctuation.length; i++) {
			if (SpecialPunctuation[i].equals(str)) {
				return true;
			}
		}
		return false;
	}

	// 10/2/2014: Moved to TSLInformation, to support patternEval() within TSL
	// public static boolean isConceptString(String token) {
	// return (token != null && token.length() > 2 && token.charAt(0) == ':'
	// && Character.isUpperCase(token.charAt(1)) && token.charAt(token
	// .length() - 1) == ':');
	// }
	//
	// public static boolean isTypeString(String token) {
	// return (token != null && token.length() > 2 && token.charAt(0) == '<'
	// && Character.isUpperCase(token.charAt(1)) && token.charAt(token
	// .length() - 1) == '>');
	// }
	//
	// public static boolean isMacroString(String token) {
	// return (token != null && token.length() > 2 && token.charAt(0) == '_'
	// && Character.isUpperCase(token.charAt(1)) && token.charAt(token
	// .length() - 1) == '_');
	// }
	//
	// public static boolean isCUIString(String token) {
	// return (token != null && token.length() > 2
	// && Character.isLetter(token.charAt(0)) && (token.charAt(1) == '_' ||
	// Character
	// .isDigit(token.charAt(1))));
	// }
	//
	// public static boolean isPhraseTypeString(String token) {
	// return (token != null && token.length() > 2 && token.charAt(0) == '#'
	// && Character.isUpperCase(token.charAt(1)) && token.charAt(token
	// .length() - 1) == '#');
	// }

	public TSLInformation getPropertySource() {
		return (this.getSemanticInterpretation() != null ? this
				.getSemanticInterpretation() : this);
	}

	public String getDirectionality() {
		return this.getPropertySource().getStringProperty("directionality");
	}

	public String getTemporality() {
		return this.getPropertySource().getStringProperty("temporality");
	}

	public String getExperiencer() {
		return this.getPropertySource().getStringProperty("experiencer");
	}

	public boolean isConjunct() {
		return (this.getRule() != null && this.getRule().isConjunct());
	}

	// 7/7/2016
	public Vector<RelationSentence> getRelationSentences(boolean useInference) {
		Vector<RelationSentence> rsents = null;
		if (this.getSemanticInterpretation() != null) {
			rsents = (useInference ? this.getSemanticInterpretation()
					.getAllRelationSentences() : this
					.getSemanticInterpretation().getRelationSentences());
		}
		return rsents;
	}

	// public Vector<RelationSentence> getRelationSentences(boolean
	// useInference) {
	// Vector<RelationSentence> rsents = null;
	// if (this.getSemanticInterpretation() != null) {
	// Vector<RelationSentence> v =
	// this.getSemanticInterpretation().getRelationSentences();
	// rsents = VUtils.append(rsents, v);
	// }
	// if (useInference) {
	// ForwardChainingInferenceEngine fcie =
	// MoonstoneRuleInterface.RuleEditor.getForwardChainingInferenceEngine();
	// Vector<RelationSentence> isents = (Vector<RelationSentence>)
	// fcie.getAllInferredRelationSentences(rsents);
	// rsents = VUtils.append(rsents, isents);
	// }
	// return rsents;
	// }

	public static Vector<RelationSentence> getRelationSentences(
			Vector<Annotation> annotations) {
		Vector<RelationSentence> rsents = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.getSemanticInterpretation() != null) {
					Vector<RelationSentence> rs = annotation
							.getSemanticInterpretation().getRelationSentences();
					rsents = VUtils.append(rsents, rs);
				}
			}
		}
		return rsents;
	}

	public boolean containsConjunct() {
		return containsConjunct;
	}

	public void setContainsConjunct(boolean containsConjunct) {
		this.containsConjunct = containsConjunct;
	}

	public String getAnnotationID() {
		return annotationID;
	}

	public String getAnnotationIDOrText() {
		String aid = this.getAnnotationID();
		if (Character.isLetter(aid.charAt(0))) {
			return aid;
		}
		return "\"" + this.getText() + "\"";
	}

	public void setAnnotationID(String aid) {
		this.annotationID = aid;
	}

	public boolean isSourceLearned() {
		return this.getSemanticInterpretation() != null
				&& this.getSemanticInterpretation().isSourceLearned();
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public boolean containsRuleFlag(String flag) {
		return this instanceof TagAnnotation && this.getRule() != null
				&& this.getRule().containsFlag(flag);
	}

	public boolean isTSL() {
		return isTSL;
	}

	public void setTSL(boolean isTSL) {
		this.isTSL = isTSL;
	}

	// public RelationSentence findRelationSentence(String rname) {
	// if (this.getRelationSentences() != null) {
	// for (RelationSentence rs : this.getRelationSentences()) {
	// if (rs.getSubject() instanceof Annotation
	// && rs.getModifier() instanceof Annotation) {
	// if (rname == null) {
	// return rs;
	// }
	// if (rname.equals(rs.getRelation().getName())) {
	// return rs;
	// }
	// }
	// }
	// }
	// return null;
	// }

	public float getDepth() {
		if (this.depth == 0 && this.textuallySortedSourceAnnotations != null) {
			float deepest = 0;
			for (Annotation child : this.textuallySortedSourceAnnotations) {
				deepest = Math.max(deepest, child.getDepth());
			}
			// 8/26/2015: Should not count target annotations as adding any
			// information...
			this.depth = deepest + 1;
		}
		return this.depth;
	}

	public float getLeftDepth() {
		if (this.leftDepth == 0
				&& this.textuallySortedSourceAnnotations != null) {
			this.leftDepth = this.textuallySortedSourceAnnotations
					.firstElement().getLeftDepth() + 1;
		}
		return this.leftDepth;
	}

	public float getRightDepth() {
		if (this.rightDepth == 0
				&& this.textuallySortedSourceAnnotations != null) {
			this.rightDepth = this.textuallySortedSourceAnnotations
					.lastElement().getRightDepth() + 1;
		}
		return this.rightDepth;
	}

	public int getWordTokenStart() {
		return wordTokenStart;
	}

	public int getWordTokenEnd() {
		return wordTokenEnd;
	}

	public void setWordTokenStart(int wordTokenStart) {
		this.wordTokenStart = wordTokenStart;
	}

	public void setWordTokenEnd(int wordTokenEnd) {
		this.wordTokenEnd = wordTokenEnd;
	}

	public int getNumberOfChildren() {
		return numberOfChildren;
	}

	// 3/24/2015: Should I not test this inside the rule's test section?
	public boolean testHeader() {
		Rule rule = this.getRule();
		if (rule != null && rule.getHeaderStrings() != null
				&& this.getSentenceAnnotation() != null
				&& this.getSentenceAnnotation().getSentence() != null) {
			Header header = this.getSentenceAnnotation().getSentence()
					.getHeader();
			if (header != null && header.getText() != null) {
				String headerText = header.getText();
				for (String str : rule.getHeaderStrings()) {
					if (headerText.indexOf(str) >= 0) {
						return true;
					}
				}
				return false;
			}
		}
		return true;
	}

	// 3/25/2015
	public boolean isWord() {
		return this.getTokenLength() == 1
				&& this.getTokens().firstElement().isWord();
	}

	public boolean isPossessiveWord() {
		if (this.isWord()) {
			Token token = tokens.firstElement();
			String str = token.getString();
			int len = token.getString().length();
			return len > 4 && str.charAt(len - 1) == '\''
					&& str.charAt(len - 1) == 's';
		}
		return false;
	}

	public String getPossessiveRoot() {
		if (this.isPossessiveWord()) {
			String str = this.getString();
			int len = str.length();
			return str.substring(0, len - 2);
		}
		return null;
	}

	// 3/26/2015: Not sure if this is a good idea... Expression.evalPattern()
	// uses
	// Annotation's properties, which currently don't exist.
	// Need to sort out the relationship between the annotation and the
	// intepretation.
	public Hashtable getProperties() {
		if (this.properties == null && this.semanticInterpretation != null) {
			this.properties = this.semanticInterpretation.getProperties();
		}
		return this.properties;
	}

	public Object getProperty(String attribute) {
		if (this.getProperties() != null) {
			Object o = this.getProperties().get(attribute);
			return o;
		}
		return null;
	}

	public void setProperty(String attribute, Object value) {
		if (this.getProperties() != null) {
			this.getProperties().put(attribute, value);
		}
	}

	public StructureAnnotation getStructureAnnotation() {
		return structureAnnotation;
	}

	public void setStructureAnnotation(StructureAnnotation structureAnnotation) {
		this.structureAnnotation = structureAnnotation;
	}

	// 4/20/2015
	public String getPatientName() {
		Document doc = this.getDocument();
		if (doc != null) {
			return doc.getPatientName();
		}
		return null;
	}

	public Vector<Annotation> getParentAnnotations() {
		return this.parentAnnotations;
	}

	// 4/23/2015
	public Word getHeadWord() {
		if (this.isTerminal()) {
			Token token = this.getTokens().firstElement();
			if (token.isWord() && token.getWord() != null) {
				return token.getWord();
			}
		} else if (this.getChildAnnotations() != null && this.getRule() != null) {
			int hindex = this.getRule().getPatternHeadIndex();
			Annotation child = this.getChildAnnotations().elementAt(hindex);
			return child.getHeadWord();
		}
		return null;
	}

	// 4/24/2015. Format: Headword:Concept:Type
	public String getLPCFGMeaningToken(boolean includeRule) {
		Object ruleid = this.getRule().getRuleID();
		Object head = this.getHeadWord();
		Object concept = this.getConcept();
		TypeConstant type = this.getType();
		String str = "";
		str += (includeRule ? ruleid : "*");
		str += ":";
		str += (head != null ? head.toString() : "*");
		str += ":";
		str += (concept != null ? concept.toString() : "*");
		str += ":";
		str += (type != null ? type.getName() : "*:");
		return str;
	}

	public String getHeaderString() {
		Header h = this.getSentenceAnnotation().getSentence().getHeader();
		String hstr = h.getText().toLowerCase();
		return hstr;
	}

	public boolean headerContainsString(String str) {
		String hstr = getHeaderString();
		return hstr.contains(str);
	}

	public boolean headerContainsStrings(Vector<String> strs) {
		String hstr = this.getHeaderString();
		for (String vstr : strs) {
			if (!hstr.contains(vstr)) {
				return false;
			}
		}
		return true;
	}

	// 6/20/2015: Gathering annotations with distinct property sets.
	public Hashtable getAllProperties() {
		if (this.isInterpreted()) {
			Hashtable props = new Hashtable();
			return getAllProperties(props);
		}
		return null;
	}

	public Hashtable getAllProperties(Hashtable props) {
		if (this.isInterpreted()) {
			Annotation.copyProperties(this.getProperties(), props);
			if (this.hasChildren()) {
				for (Annotation child : this.getChildAnnotations()) {
					child.getAllProperties(props);
				}
			}
		}
		return props;
	}

	public boolean containsPunctuation() {
		for (Token token : this.getTokens()) {
			if (token.isPunctuation()) {
				return true;
			}
		}
		return false;
	}

	public boolean validateRuleSubpattern() {
		Rule rule = this.getRule();
		Vector<Vector> subpatternLists = this.getRule().getSubpatternLists();
		Annotation singleton = this.getSingleChild();
		if (subpatternLists == null || singleton == null
				|| singleton.getNumberOfChildren() != subpatternLists.size()) {
			return true;
		}
		if (rule != null && rule.isDoDebug()
				&& rule.getSubpatternLists() != null) {
			int x = 1;
			x = x;
		}
		for (int i = 0; i < singleton.getNumberOfChildren(); i++) {
			Annotation schild = singleton.getLexicalChild(i);
			Vector sptokens = subpatternLists.elementAt(i);
			boolean found = false;
			Interpretation si = schild.getSemanticInterpretation();
			for (Object indextoken : schild.getIndexTokens()) {
				if (sptokens.contains(indextoken)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	// Before 9/9/2015
	// public boolean validateRuleSubpattern() {
	// Rule rule = this.getRule();
	// Vector<Vector> subpatternLists = this.getRule().getSubpatternLists();
	// Annotation singleton = this.getSingleChild();
	// if (subpatternLists == null || singleton == null
	// || singleton.getNumberOfChildren() != subpatternLists.size()) {
	// return true;
	// }
	// if (rule != null && rule.isDoDebug()
	// && rule.getSubpatternLists() != null) {
	// int x = 1;
	// x = x;
	// }
	// for (int i = 0; i < singleton.getNumberOfChildren(); i++) {
	// Annotation schild = singleton.getChild(i);
	// Vector sptokens = subpatternLists.elementAt(i);
	// boolean found = false;
	// Interpretation si = schild.getSemanticInterpretation();
	// for (Object indextoken : schild.getIndexTokens()) {
	// for (Object element : sptokens) {
	// if (indextoken.equals(element)) {
	// found = true;
	// break;
	// }
	// }
	// if (found) {
	// break;
	// }
	// }
	// if (!found) {
	// return false;
	// }
	// }
	// return true;
	// }

	public String getStartEndString() {
		String ckey = "<" + this.getTextStart() + "-" + this.getTextEnd() + ">";
		return ckey;
	}

	// 8/11/2015: Need to keep this when I import latest Annotation code from
	// VINCI...

	public int getWordTokenCount() {
		if (this.wordTokenCount == 0) {
			for (Token t : this.getTokens()) {
				if (t.isWordSurrogate()) {
					this.wordTokenCount++;
				}
			}
		}
		return this.wordTokenCount;
	}

	public static Vector<String> IrrelevantPropertyNames = VUtils
			.arrayToVector(new String[] { "ruleid", "concept", "cui", "macro",
					"type" });

	public static boolean PropertyIsRelevant(String property) {
		return property != null && !IrrelevantPropertyNames.contains(property);
	}

	public int getTextlength() {
		return textlength;
	}

	public String toHTML() {
		Interpretation si = this.getSemanticInterpretation();
		String str = "<html>";
		if (si != null) {
			str += "Text=\"" + this.getText() + "\"<br>";
			str += "Offset=" + this.getTextStart() + "-" + this.getTextEnd()
					+ "<br>";
			if (this.getRule() != null) {
				str += "Rule=" + this.getRule().getRuleID() + "("
						+ this.getRule().getSourceFilePath() + ")" + "<br>";
			}
			if (si.getType() != null) {
				str += "semanticType=" + si.getType().getName() + "<br>";
			}
			if (this.getRule().getPhraseType() != null) {
				str += "phraseType=" + this.getRule().getPhraseType().getName()
						+ "<br>";
			}
			if (si.getConcept() != null) {
				str += "concept=" + si.getConcept() + "<br>";
			}
			if (this.FCIETargetRuleName != null) {
				str += "&nbsp;&nbsp;&nbsp;&nbsp;**Deduced target from rule="
						+ this.FCIETargetRuleName + "**<br>";
			}
			str += "CoveredTargetConcepts="
					+ this.getNestedTargetConceptString() + "<br>";
			if (si.getExtendedConcept() != null) {
				str += "ExtendedConcept=" + si.getExtendedConcept() + "<br>";
			}
			if (si.getCUI() != null) {
				str += "CUI=" + si.getCUI() + "<br>";
			}
			str += "Properties:" + "<br>";
			for (String property : si.getPropertyNames()) {
				if (PropertyIsRelevant(property)) {
					Object value = this.getProperty(property);
					if (!(value instanceof Annotation)) {
						str += "&nbsp;&nbsp;&nbsp;&nbsp;" + property + "="
								+ value.toString() + "<br>";
					}
				}
			}
			if (si.getRelationSentences() != null) {
				str += "Relations:" + "<br>";
				for (RelationSentence rs : si.getRelationSentences()) {

					if ("concept".equals(rs.getRelation().getName())) {
						int x = 1;
					}
					if (rs.getSubject() instanceof Annotation
							&& rs.getModifier() instanceof Annotation) {
						Annotation subject = (Annotation) rs.getSubject();
						Annotation modifier = (Annotation) rs.getModifier();
						String sstr = (subject == this ? "?*" : "\""
								+ subject.getText() + "\"");
						String mstr = "\"" + modifier.getText() + "\"";
						String rsstr = "(" + rs.getRelation().toString() + " "
								+ sstr + " " + mstr + ")";
						str += "&nbsp;&nbsp;&nbsp;&nbsp;" + rsstr + "<br>";
					}
				}
			}
			str += "Goodness="
					+ getShortenedPercentString(this.getGoodness(), 4) + "<br>";
			str += "LCPFG="
					+ getShortenedPercentString(this.getLPCFGProbability(), 4)
					+ "<br>";
			str += "Coverage="
					+ getShortenedPercentString(this.getCoveredTextPercent(), 4)
					+ "<br>";
			str += "SymmetryFactor="
					+ getShortenedPercentString(this.getSymmetryFactor(), 4)
					+ "<br>";
			str += "TokenIndices=" + this.getIndexTokens().toString() + "<br>";
			str += "Signature=" + this.getSignature() + "<br>";
			if (!this.isValid()) {
				str += "**[INVALID:  " + this.invalidReason + "]<br>";
			}
		}
		str += "</html>";
		return str;
	}

	public String toDescription(int spaces) {
		Interpretation si = this.getSemanticInterpretation();
		String str = "";
		if (si != null) {
			String text = StrUtils.trimAllWhiteSpace(this.getText());
			str += getSpaces(spaces) + "Text=\"" + text + "\"\n";
			str += getSpaces(spaces) + "Offset=" + this.getTextStart() + "-"
					+ this.getTextEnd() + "\n";
			if (this.getRule() != null) {
				str += getSpaces(spaces) + "Rule=" + this.getRule().getRuleID()
						+ "(" + this.getRule().getSourceFilePath() + ")" + "\n";
			}
			if (si.getType() != null) {
				str += getSpaces(spaces) + "Type=" + si.getType().getName()
						+ "\n";
			}
			if (si.getConcept() != null) {
				str += getSpaces(spaces) + "Concept=" + si.getConcept() + "\n";
			}
			if (si.getCUI() != null) {
				str += getSpaces(spaces) + "CUI=" + si.getCUI() + "\n";
			}
			str += getSpaces(spaces) + "Properties:" + "\n";
			for (String property : si.getPropertyNames()) {
				if (PropertyIsRelevant(property)) {
					Object value = this.getProperty(property);
					if (!(value instanceof Annotation)) {
						str += getSpaces(spaces) + "    " + property + "="
								+ value.toString() + "\n";
					}
				}
			}
			if (si.getRelationSentences() != null) {
				str += getSpaces(spaces) + "Relations:" + "\n";
				for (RelationSentence rs : si.getRelationSentences()) {
					if (rs.getSubject() instanceof Annotation
							&& rs.getModifier() instanceof Annotation) {
						Annotation subject = (Annotation) rs.getSubject();
						Annotation modifier = (Annotation) rs.getModifier();
						String sstr = (subject == this ? "?*" : "\""
								+ subject.getText() + "\"");
						String mstr = "\"" + modifier.getText() + "\"";
						String rsstr = "(" + rs.getRelation().toString() + " "
								+ sstr + " " + mstr + ")";
						str += getSpaces(spaces) + "    " + rsstr + "\n";
					}
				}
			}
			str += getSpaces(spaces) + "Goodness="
					+ getShortenedPercentString(this.getGoodness(), 4) + "\n";
		}
		str += "\n";
		return str;
	}

	private String getSpaces(int number) {
		String spaces = "";
		for (int i = 0; i < number; i++) {
			spaces += "  ";
		}
		return spaces;
	}

	public static String getAnnotationStrings(Vector<Annotation> v) {
		String str = "";
		for (Annotation a : v) {
			str += a.getString() + ":";
		}
		return str;
	}

	public double getCoveredTextPercent() {
		return coveredTextPercent;
	}

	public static String getShortenedPercentString(double value, int len) {
		String gstr = String.valueOf(value);
		if (gstr.length() > 6) {
			gstr = gstr.substring(0, len);
		}
		return gstr;
	}

	// 8/12/2015
	private float calculateSymmetry() {
		float symmetry = 1f;
		if (this.isConjunct() && this.getChildAnnotations().size() == 3) {
			Vector v = this.getTextuallySortedSourceAnnotations();
			Annotation c1 = this.getLexicalChild(0);
			Annotation c2 = this.getLexicalChild(2);
			int c1len = c1.getWordTokenCount();
			int c2len = c2.getWordTokenCount();
			float totalCovered = c1len + c2len;
			symmetry = 1 - (Math.abs(c1len - c2len) / totalCovered);
		} else if (this.getRule() != null && this.getRule().isDoBalance()) {
			float lowest = 1000;
			float highest = 0;
			float total = 0;
			Vector v = this.getTextuallySortedSourceAnnotations();
			for (Annotation child : this.getChildAnnotations()) {
				float wc = child.getWordTokenCount();
				total += wc;
				if (wc < lowest) {
					lowest = wc;
				}
				if (wc > highest) {
					highest = wc;
				}
			}
			symmetry = 1 - ((highest - lowest) / total);
		}
		return symmetry;
	}

	public double getSymmetryFactor() {
		return symmetryFactor;
	}

	public int getCoveredTextLength() {
		if (this.coveredTextLength == 0) {
			if (this.getChildAnnotations() != null) {
				int clen = 0;
				for (Annotation child : this.getChildAnnotations()) {
					clen += child.getCoveredTextLength();
				}
				this.coveredTextLength = clen;
			} else {
				this.coveredTextLength = (this.getTextEnd() - this
						.getTextStart()) + 1;
			}
			this.coveredTextPercent = (double) this.coveredTextLength
					/ (double) this.textlength;
		}
		return this.coveredTextLength;
	}

	public String toXML() {
		StringBuffer sb = new StringBuffer();
		Rule rule = this.getRule();
		if (!this.issub) {
			sb.append("<annotation>\n");
		} else {
			sb.append("<annotation=\"sub\">\n");
		}
		sb.append("<id>" + this.getAnnotationID() + "</id>\n");
		sb.append("<text>" + this.getText() + "</text>\n");
		sb.append("<position>" + this.getTextStart() + "-" + this.getTextEnd()
				+ "</position>\n");
		String type = (this.getType() != null ? this.getType().getFormalName()
				: "*");
		String concept = (this.getConcept() != null ? this.getConcept()
				.toString() : "*");
		sb.append("<concept>" + concept + "</concept>\n");
		sb.append("<type>" + type + "</type>\n");
		if (this.getRule() != null) {
			sb.append("<ruleID>" + this.getRule().getRuleID() + "</ruleID>\n");
		}
		if (this.isInterpreted()
				&& this.getSemanticInterpretation().getPropertyNames() != null) {
			sb.append("<properties>\n");
			Hashtable props = this.getAllProperties();
			for (Enumeration<String> e = props.keys(); e.hasMoreElements();) {
				String property = e.nextElement();
				if (!VUtils.containedIn(property, new String[] { "ruleid",
						"concept", "macro", "contains-target" })) {
					Object value = props.get(property);
					String vstr = null;
					if (value instanceof Annotation) {
						Annotation child = (Annotation) value;
						vstr = child.getAnnotationID();
					} else {
						vstr = value.toString();
					}
					sb.append("<property=\"" + property + "\">" + vstr
							+ "</property>\n");
				}
			}
			sb.append("</properties>\n");

			if (rule != null && rule.getSemanticRelations() != null) {
				sb.append("<relations>\n");
				for (PatternRelationSentence prs : this.getRule()
						.getSemanticRelations()) {
					String rname = prs.getRelation().getName();
					Object o1 = this.evalPattern(prs.getSubject(),
							this.getVariables());
					Object o2 = this.evalPattern(prs.getModifier(),
							this.getVariables());
					if (o1 instanceof Annotation && o2 instanceof Annotation) {
						Annotation subject = (Annotation) o1;
						Annotation modifier = (Annotation) o2;
						if (subject != null && modifier != null) {
							sb.append("<relation=\"" + rname + "\">\n");
							sb.append("<subject=\""
									+ subject.getAnnotationIDOrText() + "\">\n");
							sb.append("<modifier=\""
									+ modifier.getAnnotationIDOrText()
									+ "\">\n");
							sb.append("<relation>\n");
						}
					}
				}
				sb.append("</relations>\n");
			}
		}
		sb.append("</annotation>\n");
		return sb.toString();
	}

	public static Vector<Annotation> getNonNestedAnnotationsWithoutDuplicateConcepts(
			Vector<Annotation> annotations) {
		Hashtable<Object, Annotation> ahash = new Hashtable();
		Vector<Annotation> v = getNonNestedAnnotations(annotations);
		if (v != null) {
			for (Annotation annotation : v) {
				Object concept = annotation.getConcept();
				if (concept != null) {
					Annotation other = ahash.get(concept);
					if (other == null
							|| other.getGoodness() < annotation.getGoodness()) {
						ahash.put(concept, annotation);
					}
				}
			}
		}
		Vector<Annotation> results = HUtils.getElements(ahash);
		if (results != null) {
			Collections.sort(results, new Annotation.GoodnessSorter());
			Collections.sort(results, new Annotation.TokenIndexSorter());
		}
		return results;
	}

	// 8/26/2015: Let an annotation equal a string if its concept is the same.
	public boolean hasConcept(Object o) {
		return this.hasConcept() && this.getConcept().equals(o);
	}

	public boolean hasRule() {
		return this.getRule() != null;
	}

	public boolean isValid() {
		return this.invalidReason == null;
	}

	public String getInvalidReason() {
		return this.invalidReason;
	}

	public void setInvalidReason(String reason) {
		this.invalidReason = reason;
	}

	public static Vector<String> extractConcepts(Vector<Annotation> annotations) {
		Vector<String> concepts = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Object concept = annotation.getConcept();
				if (concept != null) {
					annotations = VUtils.addIfNot(concepts, concept.toString());
				}
			}
		}
		return concepts;
	}

	public static Vector<String> getAllConceptStrings(
			Vector<Annotation> annotations) {
		Vector<String> concepts = null;
		Hashtable<String, String> hash = new Hashtable();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				annotation.getAllConceptStrings(hash);
			}
			concepts = HUtils.getKeys(hash);
		}
		return concepts;
	}

	public void getAllConceptStrings(Hashtable hash) {
		if (this.getConcept() != null) {
			hash.put(this.getConcept().toString(), this.getConcept().toString());
		}
		if (this.getChildAnnotations() != null) {
			for (Annotation child : this.getChildAnnotations()) {
				child.getAllConceptStrings(hash);
			}
		}
	}

	public static Vector<Annotation> getTargetAnnotations(
			Vector<Annotation> annotations) {
		Vector<Annotation> targets = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.containsTargetConcept()) {
					targets = VUtils.add(targets, annotation);
				}
			}
		}
		return targets;
	}

	public static Vector<Annotation> removeSameOffsetDuplicates(
			Vector<Annotation> annotations) {
		Annotation last = null;
		Vector<Annotation> nodups = null;
		for (Annotation annotation : annotations) {
			boolean isdup = false;
			if (last != null
					&& last.getTextStart() == annotation.getTextStart()
					&& last.getTextEnd() == annotation.getTextEnd()) {
				isdup = true;
			}
			if (!isdup) {
				nodups = VUtils.add(nodups, annotation);
			} else {
				int x = 1;
			}
			last = annotation;
		}
		return nodups;
	}

	// 2/11/2016: From home
	public Variable getParsetreePathName(Annotation descendant) {
		String pname = getParsetreePathName(this, descendant, -1);
		if (pname != null) {
			return new Variable(pname);
		}
		return null;
	}

	private static String getParsetreePathName(Annotation ancestor,
			Annotation descendent, int offset) {
		if (ancestor == descendent) {
			if (offset == -1) {
				return "*";
			} else {
				return String.valueOf(offset);
			}
		}
		if (ancestor.hasChildren()) {
			for (int i = 0; i < ancestor.getNumberOfChildren(); i++) {
				Annotation child = ancestor
						.getLexicallySortedSourceAnnotations().elementAt(i);
				String cpath = getParsetreePathName(child, descendent, i);
				if (cpath != null) {
					return (offset >= 0 ? offset + ":" + cpath : cpath);
				}
			}
		}
		return null;
	}

	// DOESN'T YET WORK. I NEED TO UNIFY AT HIGHER SEMANTIC LEVEL.
	// 2/8/2016: Discriminates between "((right lower lobe) (opacity)) and
	// ((right lower) (lobe opacity))".
	public boolean hasImproperSemanticSegmentation() {
		if (1 == 1) {
			return false;
		}
		if (this.isInterpreted() && this.getNumberOfChildren() == 2) {
			Annotation cphrase1 = this.getTextualChild(0);
			Annotation cphrase2 = this.getTextualChild(1);
			if (cphrase1.getType() != null
					&& !cphrase1.getType().equals(cphrase2.getType())
					&& cphrase1.hasChildren() && cphrase2.hasChildren()) {
				Annotation ccphrase1 = cphrase1.getChildAnnotations()
						.lastElement();
				Annotation ccphrase2 = cphrase2.getChildAnnotations()
						.firstElement();
				if (ccphrase1.getType() != null
						&& ccphrase1.getType().equals(ccphrase2.getType())) {
					return true;
				}

				// if
				// (UMLSTypeConstant.bothAreConditionOrLocation(ccphrase1.getType(),
				// ccphrase2.getType())) {
				// return true;
				// }

			}
		}
		return false;
	}

	public boolean isWordString() {
		char c = this.getText().charAt(0);
		return Character.isLetter(c);
	}

	public boolean isValidPerRuleConstraints() {
		Rule rule = this.getRule();
		if (rule != null && rule.getTestPredicates() != null) {
			if (rule.isDoDebug()) {
				int x = 1;
			}
			for (Constraint c : rule.getTestPredicates()) {
				Boolean rv = (Boolean) c.evalConstraint(this.getVariables());
				if (rv == null || !rv) {
					this.invalidReason = Annotation.FailTest;
					return false;
				}
			}
		}
		return true;
	}

	public void setLpcfgProbability(Double lpcfgProbability) {
		this.lpcfgProbability = lpcfgProbability;
	}

	public static Vector<Annotation> flatten(Vector<Annotation> annotations) {
		Vector<Annotation> all = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				all = VUtils.add(all, annotation);
				all = VUtils.append(all,
						flatten(annotation.getChildAnnotations()));
			}
		}
		return all;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PORT TO VINCI!!

	private Double lpcfgProbability = null;
	public Double sumOfLPCFGProbabilities = null;
	private Integer numberOfLPCFGPaths = null;
	protected Double goodness = null;

	private Double pcfgProbability = null;
	public Double sumOfPCFGProbabilities = null;
	private Integer numberOfPCFGPaths = null;

	public double getGoodness() {
		try {
			if (this.goodness == null) {
				double childSymmetryFactorProduct = 1f;
				double conjunctSymmetry = this.calculateSymmetry();
				double goodness = 1f;

				double formweight = 0.5;
				double pcfgweight = 0.5;

				// 6/18/2016: Let specialization / abstraction be multiplied
				// through
				// the parse tree via lpcfg.
				double specweight = 0.0;
				double abstractweight = 0.0;

				if (this.hasChildren()) {
					for (Annotation child : this.getChildAnnotations()) {
						// Problem: This multiplies the coverage deficit
						// upwards.
						childSymmetryFactorProduct *= child.getSymmetryFactor();
					}
				}
				double sf = this.symmetryFactor = conjunctSymmetry
						* childSymmetryFactorProduct;
				double ctp = this.getCoveredTextPercent();
				double formresult = ctp * sf;

				double pcfgprob = (this.getMoonstoneRuleInterface().getLpcfg()
						.isUseRuleConditionalsOnlyInProbabilityCalculation() ? this
						.getPCFGProbability() : this.getLPCFGProbability());

				double specfactor = (this.hasRule()
						&& this.getRule().isSpecialized() ? 1 : 0);
				double abstractfactor = (this.hasRule()
						&& this.getRule().isComplexConcept() ? 1 : 0);
				double pvalue = (formresult * formweight)
						+ (pcfgprob * pcfgweight) + (specfactor * specweight)
						+ (abstractweight * abstractfactor);
				goodness = this.goodness = new Double(pvalue);
				int x = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.goodness.doubleValue();
	}

	public double getPCFGProbability() {
		if (this.pcfgProbability == null) {
			this.pcfgProbability = new Double(1);
			if (this.grammar.getGrammarModule().getMoonstoneRuleInterface()
					.getLpcfg() != null) {
				this.grammar.getGrammarModule().getMoonstoneRuleInterface()
						.getLpcfg().calculatePCFGProbability(this);
			}
		}
		return this.pcfgProbability.doubleValue();
	}

	public void setPCFGProbability(double value) {
		this.pcfgProbability = new Double(value);
	}

	public double getLPCFGProbability() {
		if (this.lpcfgProbability == null) {
			this.lpcfgProbability = new Double(1);
			if (this.grammar.getGrammarModule().getMoonstoneRuleInterface()
					.getLpcfg() != null) {
				this.grammar.getGrammarModule().getMoonstoneRuleInterface()
						.getLpcfg().calculateLPCFGProbability(this);
			}
		}
		return this.lpcfgProbability.doubleValue();
	}

	public void setLPCFGProbability(double value) {
		this.lpcfgProbability = new Double(value);
	}

	// 4/8/2016
	public boolean isNumber() {
		if (this.getConcept() != null
				&& ":NUMBER:".equals(this.getConcept().toString())) {
			return true;
		}
		return false;
	}

	public boolean isValidPerTrainingOffsets() {
		Vector<int[]> offsets = this.getGrammar().getGrammarModule()
				.getMoonstoneRuleInterface().getAnnotationAttachments();
		if (offsets.size() > 0) {
			for (int[] t : offsets) {
				int tstart = this.getTextStart();
				int tend = this.getTextEnd();
				if (SeqUtils.overlaps(tstart, tend, t[0], t[1])
						&& !SeqUtils.contains(tstart, tend, t[0], t[1])
						&& (tstart < t[0] || tend > t[1])) {
					this.invalidReason = "ViolatesAnnotationAttachment";
					this.setValid(false);
					return false;
				}
			}
		}
		return true;
	}

	// 5/9/2016: If a phrase contains both attached positions, it must contain a
	// phrase
	// that exactly matches those positions.

	private boolean matchesTrainingOffsets(int tstart, int tend) {
		if (this.getTextStart() == tstart && this.getTextEnd() == tend) {
			return true;
		} else if (this.getTextStart() < tstart || this.getTextEnd() > tend) {
			if (this.hasChildren()) {
				for (Annotation child : this.getChildAnnotations()) {
					if (child.matchesTrainingOffsets(tstart, tend)) {
						return true;
					}
				}
			}
			return false;
		} else {
			return true;
		}
	}

	public boolean isTypeSubsumedBy(TypeConstant type) {
		return type != null && this.getType() != null
				&& this.getType().isSubsumedBy(type);
	}

	private static double SignificantAnnotationDifference = 0.05;

	// 11/12/2016
	// &&&&&
	protected static Annotation annotationComparisonDecisionList(Annotation a1,
			Annotation a2) {
		MoonstoneRuleInterface msri = a1.getMoonstoneRuleInterface();
		if (a1.containsTargetConcept() && a2.containsTargetConcept()) {
			int x = 1;
		}

		if (a1.isInterpreted() && !a2.isInterpreted()) {
			return a1;
		} else if (!a1.isInterpreted() && a2.isInterpreted()) {
			return a2;
		}

		// 7/18/2016
		if (a1 instanceof DocumentAnnotation
				&& a2 instanceof NarrativeAnnotation) {
			return a1;
		}
		if (a2 instanceof DocumentAnnotation
				&& a1 instanceof NarrativeAnnotation) {
			return a2;
		}

		// 11/30/2017
		if (!a1.isNegated() && a2.isNegated()) {
			return a1;
		}
		if (a1.isNegated() && !a2.isNegated()) {
			return a2;
		}

		// 12/1/2017: If abstract rule produces target concept, don't reject it.
		if (!a1.containsTargetConcept() && !a2.containsTargetConcept()) {
			boolean a1specialized = (a1.getRule() != null && a1.getRule()
					.isSpecialized());
			boolean a2specialized = (a2.getRule() != null && a2.getRule()
					.isSpecialized());
			if (a1specialized && !a2specialized) {
				return a1;
			}
			if (!a1specialized && a2specialized) {
				return a2;
			}
		}

		// Prefer annotation containing the most target concepts
		if (msri.isCompareAnnotationsTargetsAndGoodness()) {
			if (a1.containsTargetConcept() && !a2.containsTargetConcept()) {
				return a1;
			}

			if (a2.containsTargetConcept() && !a1.containsTargetConcept()) {
				return a2;
			}

			if (a1.containsTargetConcept() && a2.containsTargetConcept()
					&& !a1.getConcept().equals(a2.getConcept())) {
				return null;
			}

			Vector targets1 = a1.getNestedTargetConcepts();
			Vector targets2 = a2.getNestedTargetConcepts();

			int totalTarget1 = VUtils.size(targets1);
			int totalTarget2 = VUtils.size(targets2);

			if (totalTarget1 > 0 || totalTarget2 > 0) {
				if (totalTarget1 > 0 && totalTarget2 == 0) {
					return a1;
				}
				if (totalTarget1 == 0 && totalTarget2 > 0) {
					return a2;
				}
				if (SetUtils.isStrictSubset(targets1, targets2)) {
					return a2;
				}
				if (SetUtils.isStrictSubset(targets2, targets1)) {
					return a1;
				}

				// 11/1/2016 -- This seems too obvious. Why didn't I do it
				// before?
				if (totalTarget1 > totalTarget2) {
					return a1;
				}
				if (totalTarget1 < totalTarget2) {
					return a2;
				}
			}
		}

		Rule r1 = a1.getRule();
		Rule r2 = a2.getRule();
		if (r1 != null && r2 != null) {
			if (!r1.isComplexConcept() && r2.isComplexConcept()) {
				return a1;
			}
			if (r1.isComplexConcept() && !r2.isComplexConcept()) {
				return a2;
			}
		}

		double g1 = a1.getGoodness();
		double g2 = a2.getGoodness();
		double tc1 = a1.getCoveredTextPercent();
		double tc2 = a2.getCoveredTextPercent();
		double gdiff = Math.abs(g1 - g2);
		double tcdiff = Math.abs(tc1 - tc2);

		if (gdiff > SignificantAnnotationDifference) {
			return (g1 > g2 ? a1 : a2);
		}

		if (tcdiff > SignificantAnnotationDifference) {
			return (tc1 > tc2 ? a1 : a2);
		}

		if (tc1 > tc2) {
			return a1;
		} else if (tc2 > tc1) {
			return a2;
		}

		// Prefer more specific (informative) semantic type
		if (a1.getType() != a2.getType() && a1.getType() != null
				&& a2.getType() != null) {
			if (a1.getType().isSubsumedBy(a2.getType())) {
				return a1;
			}
			if (a2.getType().isSubsumedBy(a1.getType())) {
				return a2;
			}
		}

		// 9/28/2015: Keep both annotations if they have a different meaning but
		// are otherwise
		// of equal worth.
		if (a1.getConcept() != null && !a1.getConcept().equals(a2.getConcept())) {
			return null;
		}

		return a1;
	}

	public double getSumOfLPCFGProbabilities() {
		return this.sumOfLPCFGProbabilities;
	}

	public void setSumOfLPCFGProbabilities(double value) {
		this.sumOfLPCFGProbabilities = new Double(value);
	}

	public double getSumOfPCFGProbabilities() {
		return this.sumOfPCFGProbabilities;
	}

	public void setSumOfPCFGProbabilities(double value) {
		this.sumOfPCFGProbabilities = new Double(value);
	}

	public int getNumberOfPCFGPaths() {
		if (this.numberOfPCFGPaths == null) {
			this.numberOfPCFGPaths = new Integer(0);
			if (this.hasChildren()) {
				for (Annotation child : this.getChildAnnotations()) {
					this.numberOfPCFGPaths += child.getNumberOfPCFGPaths();
					this.numberOfPCFGPaths++;
				}
			}
		}
		return this.numberOfPCFGPaths;
	}

	public void setNumberOfPCFGPaths(int value) {
		this.numberOfPCFGPaths = new Integer(value);
	}

	public int getNumberOfLPCFGPaths() {
		if (this.numberOfLPCFGPaths == null) {
			this.numberOfLPCFGPaths = new Integer(0);
			if (this.hasChildren()) {
				for (Annotation child : this.getChildAnnotations()) {
					this.numberOfLPCFGPaths += child.getNumberOfLPCFGPaths();
					this.numberOfLPCFGPaths++;
				}
			}
		}
		return this.numberOfLPCFGPaths;
	}

	public void setNumberOfLPCFGPaths(int value) {
		this.numberOfLPCFGPaths = new Integer(value);
	}

	// 7/7/2016
	public void setInferredTargetConcept() {

		if (this.getRule() != null
				&& this.isInterpreted()
				&& !this.containsTargetConcept()
				&& rule.isComplexConcept()
				&& !this.getSemanticInterpretation().isInvokedInference()
				&& this.getMoonstoneRuleInterface()
						.isUseFCIEToInferTargetConcept()) {
			Vector<RelationSentence> isents = this.getSemanticInterpretation()
					.getInferredRelationSentences();
			if (isents != null) {
				for (RelationSentence isent : isents) {
					RelationConstant rc = isent.getRelation();
					if (GrammarEBL.InferredTargetRelationName.equals(rc
							.getName())) {
						Object concept = isent.getModifier();
						if (concept != null) {
							this.setConcept(concept);
							this.getSemanticInterpretation()
									.setConcept(concept);
							// Note: The "concept" RelSent is unchanged..
							this.setProperty("contains-target", true);
							this.indexTokens = VUtils.add(this.indexTokens,
									concept);
							this.FCIETargetRuleName = isent.getDerivedFrom()
									.getNamedSentence().getName();
							System.out.println("Derived from: "
									+ isent.getDerivedFrom().getNamedSentence()
											.getName());
							return;
						}
					}
				}
			}
		}
	}

	public void removeAllRelationSentences() {
		if (this.isInterpreted()) {
			this.getSemanticInterpretation().removeAllRelationSentences();
		}
	}

	public String getTuffyString() {
		return "Annotation" + this.getNumericID();
	}

	public boolean isSentenceLength() {
		int tsize = this.getTokens().size();
		int ssize = this.getSentenceAnnotation().getSentence().getTokens()
				.size();
		boolean samesize = tsize == ssize - 1;
		if (samesize) {
			int x = 1;
		}
		return samesize;
	}

	public MoonstoneRuleInterface getMoonstoneRuleInterface() {
		if (this.getGrammar() != null) {
			return this.getGrammar().getGrammarModule()
					.getMoonstoneRuleInterface();
		}
		return null;
	}

	// // 8/2/2016
	// public void gatherNestedTargetAnnotations() {
	// if (this.nestedTargetAnnotations == null) {
	// if (this.containsTargetConcept()) {
	// this.nestedTargetAnnotations = VUtils.listify(this);
	// }
	// if (this.hasChildren()) {
	// for (Annotation child : this
	// .getTextuallySortedSourceAnnotations()) {
	// this.nestedTargetAnnotations = VUtils.append(
	// this.nestedTargetAnnotations,
	// child.nestedTargetAnnotations);
	// }
	// }
	// }
	// }

	// public Vector<Annotation> getNestedTargetAnnotations() {
	// return this.nestedTargetAnnotations;
	// }
	//
	// public int getNumberOfNestedTargetAnnotations() {
	// return (this.nestedTargetAnnotations == null ? 0
	// : this.nestedTargetAnnotations.size());
	// }

	public String getFCIETargetRuleName() {
		return FCIETargetRuleName;
	}

	// 11/11/2016 TRYING TO FIX LEAK
	public static void gcAll(Vector<Annotation> annotations) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				annotation.gc();
			}
		}
	}

	public void gc() {
		if (!this.garbageCollected) {
			if (this.textuallySortedSourceAnnotations != null) {
				for (Annotation child : this.textuallySortedSourceAnnotations) {
					child.gc();
				}
			}
			this.setParents(null);
			this.parentAnnotations = null;
			this.knowledgeBase = null;
			sentenceAnnotation = null;
			rule = null;
			string = null;
			text = null;
			lexicallySortedSourceAnnotations = null;
			textuallySortedSourceAnnotations = null;
			value = null;
			indexTokens = null;
			semanticInterpretation = null;
			flags = null;
			tokens = null;
			coveredWords = null;
			signature = null;
			userObject = null;
			nestedTargetAnnotations = null;
			topmostNestedTargetAnnotations = null;
			nestedTargetConcepts = null;

			// From superclasses:
			this.setSubjectSentences(null);
			this.setModifierSentences(null);
			this.setPropertySentences(null);
			this.setVariables(null);
			this.setVariableBindings(null);
			this.setProofVariableStack(null);
			this.setExpressionProofWrappers(null);
			this.setProperties(null);

			this.garbageCollected = true;
		}
	}

	public static boolean annotationContains(Annotation a1, Annotation a2) {
		return SeqUtils.simpleContains(a1.getTokenStart(), a1.getTokenEnd(),
				a2.getTokenStart(), a2.getTokenEnd());
	}

	public static boolean annotationsCoincide(Annotation a1, Annotation a2) {
		return SeqUtils.coincides(a1.getTokenStart(), a1.getTokenEnd(),
				a2.getTokenStart(), a2.getTokenEnd());
	}

	public static boolean eitherCointainsOrCoincides(Annotation a1,
			Annotation a2) {
		return annotationContains(a1, a2) || annotationContains(a2, a1)
				|| annotationsCoincide(a1, a2);
	}

	public boolean isNegated() {
		return JavaFunctions.isNegated(this);
	}

	public static boolean sameConceptSamePolarity(Annotation a1, Annotation a2) {
		if (a1.hasConcept() && a1.getConcept().equals(a2.getConcept())) {
			if (a1.isNegated()) {
				return a2.isNegated();
			}
			if (a2.isNegated()) {
				return a1.isNegated();
			}
			return true;
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////
	// 12/7/2017: IMPORTING READMISSION FUNCTIONALITY TO EXTRACT TOPMOST
	// RELEVANT
	// TARGET ANNOTATIONS FROM PARSETREE

	public Vector<Annotation> gatherNonconflictingNestedRelevantAnnotations() {
		return gatherNonconflictingNestedRelevantAnnotations(new Hashtable());
	}

	private Vector<Annotation> gatherNonconflictingNestedRelevantAnnotations(
			Hashtable chash) {
		Vector<Annotation> annotations = null;
		Readmission readmission = this.getMoonstoneRuleInterface()
				.getReadmission();
		Rule rule = this.getRule();
		DocumentGrammar dgrammar = (DocumentGrammar) this
				.getMoonstoneRuleInterface().getDocumentGrammar();

		if (this.containsTargetConcept()) {
			String cstr = this.getConcept().toString();
			StringConstant sc = (StringConstant) this.getConcept();
			Vector<String> negations = readmission.getNegatedConcepts(cstr);
			boolean priorneg = false;
			if (negations != null) {
				for (String neg : negations) {
					if (chash.get(neg) != null) {
						priorneg = true;
						break;
					}
				}
			}
			if (chash.get(cstr) == null && !priorneg) {
				boolean gateEquivalent = false;
				if (!chash.isEmpty()) {
					for (Object o : chash.keySet()) {
						if (o instanceof StringConstant) {
							StringConstant csc = (StringConstant) o;
							if (dgrammar.conceptsAreGateEquivalents(sc, csc)) {
								gateEquivalent = true;
								break;
							}

						}
					}
				}
				if (!gateEquivalent) {
					chash.put(cstr, cstr);
					chash.put(sc, sc);
					annotations = VUtils.listify(this);
				}
			}
		}

		// 1/15/2018 TEST!!! "pt does  not want to live in an apartment"
		// should not be searched for LiveAtHome.
		if (this.hasChildren() && !this.isNegated()) {
			for (Annotation child : this.getChildAnnotations()) {
				annotations = VUtils.append(annotations, child
						.gatherNonconflictingNestedRelevantAnnotations(chash));
			}
		}
		return annotations;
	}

	public static Vector<Annotation> gatherAnnotationsContainingTargetConcept(
			Vector<Annotation> annotations) {
		Vector<Annotation> targets = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.containsTargetConcept()) {
					targets = VUtils.add(targets, annotation);
				}
			}
		}
		return targets;
	}

	public long getNumericAnnotationID() {
		return numericAnnotationID;
	}

	public boolean containsRuleStatingTargetConceptDirectly() {
		if (this.getRule() != null
				&& this.getRule().isStatesTargetConceptDirectly()) {
			return true;
		}
		if (this.hasChildren()) {
			for (Annotation child : this.getChildAnnotations()) {
				if (child.containsRuleStatingTargetConceptDirectly()) {
					return true;
				}
			}
		}
		return false;
	}

}
