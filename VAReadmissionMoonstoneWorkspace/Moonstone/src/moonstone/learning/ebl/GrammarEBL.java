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
package moonstone.learning.ebl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import moonstone.annotation.Annotation;
import moonstone.grammar.Grammar;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.document.Document;
import tsl.expression.Expression;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.relation.PatternRelationSentence;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.inference.forwardchaining.ForwardChainingInferenceEngine;
import tsl.startup.StartupParameters;
import tsl.tsllisp.Sexp;
import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.TimeUtils;
import tsl.utilities.VUtils;
import utility.UnixFormat;

// 2/4/2016 NOTE:  I just flipped the user-selected concept from a string to a StringConstant,
// and added a flag on StringConstant indicating whether it is complex.  I've made lots of 
// changes, not yet tested...

public class GrammarEBL {

	private ForwardChainingInferenceEngine forwardChainingInferenceEngine = null;
	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private Vector<Annotation> sentenceAnnotations = null;
	private Vector<File> documentFiles = null;
	private String currentDocumentName = null;
	private int annotationIndex = 0;
	private int lastProcessedAnnotationIndex = 0;
	private int documentIndex = 0;
	private int newRuleID = 0;
	private boolean quitting = false;
	private Hashtable<String, String> acceptedStringHash = new Hashtable();
	private boolean requiresUserValidation = true;
	private String trainingCorpusDirectory = null;
	private String sentenceGrammarRootDirectory = null;
	private String corpusRuleStorageDirectory = null;
	private Vector<Rule> newEBLGrammarRules = null;
	private Hashtable<String, String> relationConceptHash = new Hashtable();
	private String defaultRuleExtractionFile = null;
	private String defaultEBLRuleDirectory = null;
	private boolean analyzingCorpusFiles = false;
	private int terminalPatternTokenType = 0;
	private Vector<String> targetConceptList = null;

	public static int UseTypeTerminalPatternTokens = 1;
	public static int UseConceptTerminalPatternTokens = 2;
	public static int UseWordTerminalPatternTokens = 3;

	private static String BaseRuleName = "AUTO";
	public static String EBLGrammarAcceptedStringFile = "EBLGrammarAcceptedStringFile";
	public static String EBLRequireUserValidation = "EBLRequireUserValidation";

	public static String EBLSentenceGrammarRootDirectory = "EBLSentenceGrammarRootDirectory";
	public static String EBLCorpusRuleStorageDirectory = "EBLCorpusRuleStorageDirectory";
	private static String EBLCorpusTrainingDirectory = "EBLCorpusTrainingDirectory";
	private static String EBLDefaultRuleFile = "EBLDefaultRuleFile";
	private static String EBLRuleDirectory = "EBLRuleDirectory";
	private static String GrammarEBLConceptFileName = "GrammarEBLConceptFile";
	public static String InferredTargetRelationName = "inferred-target";

	public GrammarEBL(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
		StartupParameters sp = msri.getKnowledgeEngine().getStartupParameters();
		this.forwardChainingInferenceEngine = this.moonstoneRuleInterface
				.getMoonstoneQueryPanel().getForwardChainingInferenceEngine();
		Vector<ImplicationSentence> isents = msri.getKnowledgeEngine()
				.getCurrentKnowledgeBase().getAllImplicationSentences();
		this.forwardChainingInferenceEngine.storeRules(isents);
		this.sentenceGrammarRootDirectory = sp
				.getPropertyValue(EBLSentenceGrammarRootDirectory);
		this.trainingCorpusDirectory = sp
				.getPropertyValue(EBLCorpusTrainingDirectory);
		this.corpusRuleStorageDirectory = sp
				.getPropertyValue(EBLCorpusRuleStorageDirectory);
		this.defaultRuleExtractionFile = sp
				.getPropertyValue(EBLDefaultRuleFile);
		this.defaultEBLRuleDirectory = sp.getPropertyValue(EBLRuleDirectory);
		this.readAcceptedStrings();
		this.requiresUserValidation = msri.getKnowledgeEngine()
				.getStartupParameters()
				.isPropertyTrue(EBLRequireUserValidation);
		this.populateTargetConceptList();
	}

	public void getDocumentFilesFromTrainingDirectory() {
		String fpath = this.moonstoneRuleInterface.getKnowledgeEngine()
				.getStartupParameters().getRootDirectory()
				+ File.separatorChar + this.trainingCorpusDirectory;
		Vector<File> files = FUtils.readFilesFromDirectory(fpath);
		this.documentFiles = null;
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".txt")) {
					this.documentFiles = VUtils.add(this.documentFiles, file);
				}
			}
		}
	}

	public void analyzeCorpusFiles() {
		this.analyzingCorpusFiles = true;
		while (!this.quitting) {
			this.processNextCorpusAnnotation();
		}
		this.storeEBLGrammarRules();
		this.clear();
		this.documentFiles = null;
		System.out.println("FINISHED");
	}

	private void readAcceptedStrings() {
		String fname = this.moonstoneRuleInterface.getKnowledgeEngine()
				.getStartupParameters()
				.getPropertyValue(EBLGrammarAcceptedStringFile);
		if (fname != null) {
			String fpath = this.moonstoneRuleInterface
					.getResourceDirectoryName() + File.separatorChar + fname;
			String fstr = FUtils.readFile(fpath);
			if (fstr != null) {
				String[] strs = fstr.split("::");
				for (int i = 0; i < strs.length; i++) {
					String str = strs[i];
					this.acceptedStringHash.put(str, str);
				}
			}
		}
	}

	private void processNextCorpusAnnotation() {
		if (this.documentFiles != null
				&& this.documentIndex < this.documentFiles.size()
				&& (this.sentenceAnnotations == null || this.annotationIndex >= this.sentenceAnnotations
						.size() - 1)) {
			this.newEBLGrammarRules = null;
			this.annotationIndex = this.lastProcessedAnnotationIndex = 0;
			File file = this.documentFiles.elementAt(this.documentIndex++);
			this.currentDocumentName = file.getName();
			System.out.println("\nGrammarEBL: Processing \"" + file.getName()
					+ "\"...");
			String text = FUtils.readFile(file);
			try {
				text = UnixFormat.convertToUnixFormat(text);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			Document doc = new Document(this.currentDocumentName, text);
			this.sentenceAnnotations = this.moonstoneRuleInterface
					.applyNarrativeGrammarToText(doc, true, false, true);
		}
		if (this.sentenceAnnotations != null
				&& this.annotationIndex < this.sentenceAnnotations.size()) {

			Annotation annotation = this.sentenceAnnotations
					.elementAt(this.annotationIndex++);
			if (annotation.isInterpreted()
					&& this.acceptedStringHash.get(annotation.getText()) == null) {
				System.out.print(".");
				if (annotation.isInterpreted()
						&& !annotation.containsTargetConcept()
						&& this.acceptedStringHash.get(annotation.getText()) == null) {
					Vector<RelationSentence> tisents = this
							.gatherInferredTargetSentences(annotation);
					if (tisents != null) {
						RelationSentence firstrs = tisents.firstElement();
						for (int i = 0; i < firstrs.getArity(); i++) {
							Object t = firstrs.getTerm(i);
							String cname = t.getClass().getName();
							int x = 1;
						}
						this.processAnnotationWithInferredStatements(
								annotation, tisents);
					}
				}
			}
		} else if (this.documentIndex >= this.documentFiles.size()) {
			this.quitting = true;
		}
	}

	private Vector<RelationSentence> gatherInferredTargetSentences(
			Annotation annotation) {
		Vector<RelationSentence> tisents = null;
		Vector<RelationSentence> rsents = annotation
				.getRelationSentences(false);
		if (rsents != null) {
			Vector<RelationSentence> isents = (Vector<RelationSentence>) this.forwardChainingInferenceEngine
					.getAllInferredRelationSentences(rsents);
			tisents = this.gatherInferredTargetSentences(isents);
		}
		return tisents;
	}

	public void createBagOfConceptsRuleFromMoonstoneAnnotation() {
		Annotation annotation = this.moonstoneRuleInterface
				.getSelectedAnnotation();
		Grammar grammar = this.moonstoneRuleInterface.getSentenceGrammar();
		if (annotation != null && annotation.hasConcept()) {
			Rule rule = annotation.getRule();
			Vector<String> tokens = new Vector(0);
			this.gatherAllAnnotationConcepts(annotation, tokens);
			if (tokens != null && tokens.size() >= 2) {
				Vector<Vector<String>> embedded = VUtils.listifyVector(tokens);
				Rule newrule = new Rule();
				Vector<Vector> patternLists = newrule
						.extractEmbeddedPatternConstants(embedded);
				newrule.setPatternLists(patternLists);
				StringConstant concept = this
						.getConceptStringFromTargetList(annotation.getConcept());
				if (concept == null) {
					return;
				}
				String dateString = TimeUtils.getDateTimeString();
				Document doc = annotation.getDocument();
				String newruleid = "BOC-" + rule.getRuleID() + "-"
						+ this.newRuleID++ + "-" + dateString;
				newrule.setRuleID(newruleid);
				newrule.setExampleSnippet(StrUtils
						.removeNonAlphaDigitSpaceCharacters(annotation
								.getText()));
				if (concept != null) {
					// Object concept =
					// Constant.extractConstant(newrule.getKnowledgeBase(),
					// conceptString);
					newrule.setResultConcept(concept);
				}
				if (annotation.getType() != null) {
					newrule.setType(annotation.getType());
				}
				newrule.setOrdered(true);
				newrule.setSpecialized(true);
				newrule.setWindow(rule.getWindow());
				newrule.setBagOfConceptsRule(true);
				newrule.addPropertyToRemove("directionality");
				Sexp sexp = newrule.toSexp(false);
				newrule.setSexp(sexp);

				boolean allowanyway = true;

				if (!allowanyway && grammar.rulePatternExists(newrule)) {
					this.moonstoneRuleInterface
							.displayMessageDialog("Rule pattern already exists");
				} else {
					grammar.pushRulePatternHash(newrule);
					this.newEBLGrammarRules = VUtils.add(
							this.newEBLGrammarRules, newrule);
					this.storeEBLGrammarRules();
				}
			}
		}
	}

	public void gatherAllAnnotationConcepts(Annotation annotation,
			Vector<String> tokens) {
		if (annotation != null) {
			if (this.annotationIsSingletonToTerminal(annotation)) {
				String token = null;
				if (annotation.hasConcept()) {
					token = annotation.getConcept().toString();
				} else if (annotation.hasMacro()) {
					token = annotation.getMacro();
				}
				tokens = VUtils.addIfNot(tokens, token);
			} else if (annotation.hasChildren()) {
				for (Annotation child : annotation.getChildAnnotations()) {
					gatherAllAnnotationConcepts(child, tokens);
				}
			}
		}
	}

	public void extractDomainSpecificRulesFromSelectedAnnotation() {
		Annotation annotation = this.moonstoneRuleInterface
				.getSelectedAnnotation();
		StringConstant concept = null;
		if (annotation != null) {
			concept = this.getConceptStringFromTargetList(annotation
					.getConcept());
			if (concept == null) {
				return;
			}
			this.analyzingCorpusFiles = false;
			Vector<RelationSentence> isents = this
					.gatherInferredTargetSentences(annotation);
			this.extractRulesFromAnnotation(annotation, isents, concept);
			this.storeEBLGrammarRules();
		} else {
			this.moonstoneRuleInterface
					.displayMessageDialog("No annotation selected...");
		}
	}

	public void processAnnotationWithInferredStatements(Annotation annotation,
			Vector<RelationSentence> tisents) {
		Vector<Annotation> displayed = VUtils.listify(annotation);
		this.moonstoneRuleInterface.getControl().getSentenceGrammar()
				.setDisplayedAnnotations(displayed);
		if (this.requiresUserValidation) {
			this.lastProcessedAnnotationIndex = this.annotationIndex - 1;
			this.moonstoneRuleInterface.repopulateJTree(true);
			this.moonstoneRuleInterface.setTextToAnalyze(annotation.getText());
			String tistr = "Snippet=\"" + annotation.getText() + "\"\n";

			tistr += "Sentence=\""
					+ annotation.getSentenceAnnotation().getSentence()
							.getText() + "\"\n";
			Object[] options = { "Accept (Concepts)", "Reject",
					"Negative Example", "Bad String", "Backup", "Cancel" };
			int answer = JOptionPane.showOptionDialog(new JFrame(), tistr,
					null, JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

			switch (answer) {
			case 0:
				this.extractRulesFromAnnotation(annotation, tisents);
				break;
			case 1:
				break;
			case 2:
				break;
			case 3:
				this.acceptedStringHash.put(annotation.getText(),
						annotation.getText());
				this.storeAcceptedStrings();
				break;
			case 4:
				this.backupAnnotation();
				break;
			case 5:
				this.quitting = true;
				break;
			default:
				break;
			}
		} else {
			this.extractRulesFromAnnotation(annotation, tisents);
		}
	}

	public void extractRulesFromAnnotation(Annotation annotation,
			Vector<RelationSentence> isents) {
		extractRulesFromAnnotation(annotation, isents, null);
	}

	public void extractRulesFromAnnotation(Annotation annotation,
			Vector<RelationSentence> isents, StringConstant concept) {
		if (annotation.getRule() != null) {
			if (annotation.getRule().isSpecialized()) {
				int answer = JOptionPane.showConfirmDialog(new JFrame(), "Source rule is specialized.  Continue?");
				if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.NO_OPTION) {
					return;
				}
			}
			// if (this.moonstoneRuleInterface.getLpcfg() != null) {
			// this.moonstoneRuleInterface.getLpcfg().processMeaningCounts(annotation);
			// }

			if (this.analyzingCorpusFiles) {
				this.acceptedStringHash.put(annotation.getText(),
						annotation.getText());
			}
			Vector<Rule> newrules = generateSnapshotRules(annotation, isents,
					null, true, concept);
			this.newEBLGrammarRules = VUtils.append(this.newEBLGrammarRules,
					newrules);
		}
	}

	public void extractSpecializedRuleSpecialization() {
		Annotation annotation = this.moonstoneRuleInterface
				.getSelectedAnnotation();
		if (annotation != null && annotation.getRule() != null
				&& !annotation.getRule().isSpecialized()) {
			this.extractDomainSpecificRulesFromSelectedAnnotation();
			if (this.newEBLGrammarRules != null) {
				Rule sourceRule = this.newEBLGrammarRules.firstElement();
				StringConstant concept = this
						.getConceptStringFromTargetList(annotation.getConcept()
								.toString());
				if (concept == null) {
					return;
				}
				String dateString = TimeUtils.getDateTimeString();
				Document doc = annotation.getDocument();
				Rule newrule = sourceRule.clone();
				newrule.setWindow(sourceRule.getWindow());
				newrule.setPropertyPredicates(null);
				newrule.setActions(null);
				newrule.setSemanticRelations(null);
				newrule.setLocalVariables(null);
				newrule.setSpecialized(true);
				String newruleid = BaseRuleName + "-"
						+ annotation.getRule().getRuleID() + "-"
						+ this.newRuleID++ + "-" + dateString;
				newrule.setRuleID(newruleid);
				newrule.setSourceID(sourceRule.getRuleID());
				newrule.setExampleSnippet(StrUtils
						.removeNonAlphaDigitSpaceCharacters(annotation
								.getText()));
				if (concept != null) {
					newrule.setResultConcept(concept);
				}
				if (annotation.getType() != null) {
					newrule.setType(annotation.getType());
				}
				newrule.addPropertyToRemove("directionality");
				Vector<Vector> patternLists = VUtils.listify(VUtils
						.listify(sourceRule.getRulenamePatternToken()));
				newrule.setPatternLists(patternLists);
				Sexp sexp = newrule.toSexp(false);
				newrule.setSexp(sexp);
				this.newEBLGrammarRules = VUtils.add(this.newEBLGrammarRules,
						newrule);
			}
		}
	}

	private Vector<Rule> generateSnapshotRules(Annotation annotation,
			Vector<RelationSentence> isents, String dateString, boolean attop,
			StringConstant concept) {
		if (annotation.getRule() == null || annotation.getRule().isTerminal()) {
			return null;
		}
		Grammar sgrammar = this.moonstoneRuleInterface.getSentenceGrammar();
		if (dateString == null) {
			dateString = TimeUtils.getDateTimeString();
		}
		Document doc = annotation.getDocument();
		Rule newrule = annotation.getRule().clone();
		newrule.setSpecialized(true);
		if (!attop) {
			newrule.setIntermediate(true);
		} else {
			newrule.addPropertyToRemove("directionality");
		}
		String newruleid = BaseRuleName + "-"
				+ annotation.getRule().getRuleID() + "-" + this.newRuleID++
				+ "-" + dateString;
		newrule.setRuleID(newruleid);
		newrule.setSourceID(annotation.getRule().getRuleID());
		newrule.setExampleSnippet(StrUtils
				.removeNonAlphaDigitSpaceCharacters(annotation.getText()));

		if (concept == null && isents != null) {
			for (RelationSentence isent : isents) {
				RelationConstant rc = isent.getRelation();
				if (InferredTargetRelationName.equals(rc.getName())
						&& isent.getArity() == 2
						&& isent.getModifier() instanceof StringConstant) {
					concept = (StringConstant) isent.getModifier();
					break;
				}
			}
		}
		if (concept != null) {
			newrule.setResultConcept(concept);
		}

		Vector<Vector> patternLists = null;
		Vector<Rule> newrules = null;

		if (annotation.getType() != null) {
			newrule.setType(annotation.getType());
		}

		for (Annotation child : annotation
				.getLexicallySortedSourceAnnotations()) {
			String ctoken = null;
			if (child.getRule() != null) {
				if (annotationIsSingletonToTerminal(child)
						|| child.getRule().isBagOfConceptsRule()) {
					ctoken = child.getRule().getRulenamePatternToken();
				} else {
					Vector<Rule> ncr = generateSnapshotRules(child, null,
							dateString, false, null);
					if (ncr != null) {
						Rule firstcrule = ncr.firstElement();
						Object key = firstcrule.getPatternLists();
						Vector<Rule> oldcrules = sgrammar
								.getRulePatternHash(key);
						if (oldcrules != null) {
							firstcrule = oldcrules.firstElement();
						}
						ctoken = firstcrule.getRulenamePatternToken();
						newrules = VUtils.append(newrules, ncr);
					} else {
						ctoken = child.getRule().getRulenamePatternToken();
					}
				}
			} else {
				ctoken = this.getAnnotationPatternToken(child);
			}
			patternLists = VUtils.add(patternLists, VUtils.listify(ctoken));
		}
		newrule.setPatternLists(patternLists);
		if (isents != null) {
			Hashtable rhash = new Hashtable();
			for (RelationSentence is : isents) {
				if (this.isTargetRelation(is.getRelation())) {
					newrule.setContainsTargetConcept(true);
					Vector terms = new Vector(0);
					terms.add(is.getRelation());
					for (int i = 0; i < is.getTermCount(); i++) {
						Object o = is.getTerm(i);
						if (o instanceof Annotation) {
							o = annotation.getParsetreePathName((Annotation) o);
						}
						o = Term.wrapTerm(o);
						terms.add(o);
					}
					PatternRelationSentence prs = new PatternRelationSentence(
							terms);
					if (rhash.get(prs.getRelation()) == null) {
						rhash.put(prs.getRelation(), prs);
						newrule.addSemanticRelation(prs);
					}
				}
			}
		}
		Sexp sexp = newrule.toSexp();
		newrule.setSexp(sexp);

		newrules = VUtils.append(VUtils.listify(newrule), newrules);

		// System.out.println(sexp.toNewlinedString());

		return newrules;
	}

	private String getAnnotationPatternToken(Annotation annotation) {
		String token = annotation.getText();
		if (this.terminalPatternTokenType == GrammarEBL.UseTypeTerminalPatternTokens) {
			if (annotation.getType() != null) {
				token = annotation.getType().getFormalName();
			}
		} else if (this.terminalPatternTokenType == GrammarEBL.UseConceptTerminalPatternTokens) {
			if (annotation.getCui() != null) {
				token = annotation.getCui().toUpperCase();
			} else if (annotation.getConcept() != null) {
				token = annotation.getConcept().toString();
			}
		}
		return token;
	}

	private Vector<RelationSentence> gatherInferredTargetSentences(
			Vector<RelationSentence> isents) {
		Vector<RelationSentence> targets = null;
		if (isents != null) {
			for (RelationSentence isent : isents) {
				if (isTargetRelation(isent.getRelation())) {
					targets = VUtils.add(targets, isent);
				}
			}
		}
		return targets;
	}

	private boolean isTargetRelation(RelationConstant rc) {
		return this.relationConceptHash.get(rc.getName()) != null;
	}

	// private String getTargetConcept(Vector<RelationSentence> tisents) {
	// if (tisents != null) {
	// for (RelationSentence rs : tisents) {
	// String cname = this.relationConceptHash.get(rs.getRelation().getName());
	// if (cname != null) {
	// return cname;
	// }
	// }
	// }
	// return null;
	// }

	private boolean annotationIsSingletonToTerminal(Annotation annotation) {
		Rule rule = annotation.getRule();
		if (rule != null && rule.isSingleton()) {
			if (rule.isSingletonWordInput()) {
				return true;
			}
			if (annotation.isNumber()) {
				return true;
			}
			if (annotation.getSingleChild() != null) {
				return annotationIsSingletonToTerminal(annotation
						.getSingleChild());
			}
		}
		return false;
	}

	private void backupAnnotation() {
		int last = this.lastProcessedAnnotationIndex;
		if (last > 0) {
			last--;
		}
		this.annotationIndex = last;
	}

	private void storeAcceptedStrings() {
		if (this.analyzingCorpusFiles) {

			if (1 == 1) {
				return;
			}

			String fname = this.moonstoneRuleInterface.getKnowledgeEngine()
					.getStartupParameters()
					.getPropertyValue(EBLGrammarAcceptedStringFile);
			if (fname != null) {
				String fpath = this.moonstoneRuleInterface
						.getResourceDirectoryName()
						+ File.separatorChar
						+ fname;
				StringBuffer sb = new StringBuffer();
				for (Enumeration<String> e = this.acceptedStringHash.keys(); e
						.hasMoreElements();) {
					String str = e.nextElement();
					sb.append(str + "::");
				}
				FUtils.writeFile(fpath, sb.toString());
			}
		}
	}

	public void clear() {
		this.documentIndex = 0;
		this.annotationIndex = 0;
		this.currentDocumentName = null;
		this.quitting = false;
		this.newEBLGrammarRules = null;
		this.moonstoneRuleInterface.resetTitle();
	}

	public void storeEBLGrammarRules() {
		this.moonstoneRuleInterface.resetTitle();
		MoonstoneRuleInterface msri = this.moonstoneRuleInterface;
		StartupParameters sp = msri.getStartupParameters();
		String dateString = TimeUtils.getDateTimeString();
		dateString = StrUtils.replaceNonAlphaNumericCharactersWithDelim(
				dateString, '_');
		int x = 1;
		if (this.newEBLGrammarRules != null) {
			String fullname = sp.getRuleDirectory() + File.separatorChar
					+ msri.getSelectedGrammarDirectoryName();
			if (this.currentDocumentName == null) {
				// Before 10/31/2016
				// fullname += File.separatorChar +
				// this.defaultRuleExtractionFile;
				fullname += File.separatorChar + "sentence-grammar"
						+ File.separatorChar + "EBL" + File.separatorChar
						+ "EBLRules" + File.separatorChar 
						+ "EBLRule_" + dateString;
			} else {
				fullname += File.separatorChar
						+ this.corpusRuleStorageDirectory + File.separatorChar
						+ this.currentDocumentName;
			}
			FUtils.findOrCreateDirectory(fullname, true);
			Grammar.storeRules(this.newEBLGrammarRules, fullname, "wordrule");
			// if (this.moonstoneRuleInterface.getLpcfg() != null) {
			// this.moonstoneRuleInterface.getLpcfg().storeMeaningCounts();
			// }
			// 10/31/2016
			this.newEBLGrammarRules = null;
			this.moonstoneRuleInterface.reloadRules();
		} else {
			this.moonstoneRuleInterface
					.displayMessageDialog("No rules to store");
		}
	}

	private StringConstant getConceptStringFromTargetList(Object token) {
		StringConstant selected = null;
		Vector<StringConstant> displayable = null;
		Vector<StringConstant> allConcepts = this.moonstoneRuleInterface
				.getKnowledgeEngine().getCurrentOntology()
				.getAllStringConstants();
		if (allConcepts != null) {
			for (StringConstant cs : allConcepts) {
				if (!cs.isComplex()) {
					displayable = VUtils.add(displayable, cs);
				} else {
					int x = 1;
				}
			}
		}
		if (displayable != null) {
			Collections.sort(displayable, new Expression.NameSorter());
			Object[] clist = VUtils.vectorToArray(displayable);
			selected = (StringConstant) JOptionPane.showInputDialog(
					new JFrame(), "Select Concept:", "Customized Dialog",
					JOptionPane.PLAIN_MESSAGE, null, clist, clist[0]);
		}
		return selected;
	}

	private void populateTargetConceptList() {
		this.targetConceptList = this.moonstoneRuleInterface.getReadmission()
				.getAllMoonstoneTargetConcepts();
		String fname = this.moonstoneRuleInterface.getStartupParameters()
				.getPropertyValue(GrammarEBLConceptFileName);
		if (fname != null) {
			String fullname = this.moonstoneRuleInterface
					.getResourceDirectoryName() + File.separatorChar + fname;
			File f = new File(fullname);
			try {
				if (f.exists()) {
					BufferedReader in = new BufferedReader(new FileReader(f));
					String line = null;
					Vector<String> strs = null;
					while ((line = in.readLine()) != null) {
						if (line.length() > 0) {
							strs = VUtils.add(strs, line.trim());
						}
					}
					this.targetConceptList = VUtils.appendNew(
							this.targetConceptList, strs);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Vector<Rule> getEBLGrammarRules() {
		return this.newEBLGrammarRules;
	}

	public void assignInferredTargetConcepts(Vector<Annotation> annotations) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				assignInferredTargetConcept(annotation);
			}
		}
	}

	// 7/6/2016
	public void assignInferredTargetConcept(Annotation annotation) {
		if (!annotation.containsTargetConcept() && annotation.getType() != null
				&& annotation.getRule() != null
				&& annotation.getRule().isComplexConcept()) {
			Object oldconcept = annotation.getConcept();
			Vector<RelationSentence> isents = this
					.gatherInferredTargetSentences(annotation);
			if (isents != null) {
				for (RelationSentence isent : isents) {
					String cname = this.relationConceptHash.get(isent
							.getRelation().getName());
					if (cname != null) {
						StringConstant sc = StringConstant
								.createStringConstant(cname,
										annotation.getType(), false);
						annotation.setConcept(sc);
						System.out.println("AssignInferredTargetConcept: "
								+ annotation.getText() + ",OLD=" + oldconcept
								+ ",NEW=" + sc);
						return;
					}
				}
			}
		}
	}

	// 7/5/2016
	public void runCorpusAnalysisThread() {
		try {
			Thread t = new Thread(new GrammarEBLCorpusAnalysis());
			t.start();
			while (t.isAlive()) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
