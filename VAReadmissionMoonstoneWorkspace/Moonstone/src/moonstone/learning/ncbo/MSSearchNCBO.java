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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tsl.documentanalysis.tokenizer.Token;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class MSSearchNCBO {

	static String inputSearchParameters = "?ontologyids=1057,1404,1499"
			+ "&isexactmatch=1&includeproperties=0";

//	public static void testCasesAgainstNCBO(Onyx onyx) {
//		Access.getAccess();
//		Hashtable<String, Vector<Vector<BNState>>> conceptStateHash = new Hashtable();
//		for (BNCaseGroup group : BNCaseGroup.allCaseGroups) {
//			if (group.getDocument() != null) {
//				group.getDocument().analyzeSentences();
//			}
//			for (BNCase bncase : group.getCases()) {
//				if (bncase.getType().getDomain().isPulmonaryDomain()
//						&& !bncase.getSummaryState().isNull()
//						&& (bncase.getType().isCondition() || bncase.getType()
//								.isLocation())) {
//					BNTree tree = getTree(group.getBnTrees(), bncase);
//					Vector<BNState> states = null;
//					if (tree != null) {
//						states = tree.getAllNonStubInputStates();
//						states = getRelevantStates(states);
//					} else {
//						states = bncase.getNonStubWords();
//					}
//					if (states != null) {
//						Vector<BNState> v = new Vector(states);
//						Collections.sort(v, new BNState.NameSorter());
//						String concept = bncase.getSummaryState().getName();
//						VUtils.pushIfNotHashVector(conceptStateHash, concept, v);
//					}
//				}
//			}
//		}
//		float matchedConceptCount = 0f;
//		float matchedInputCount = 0f;
//		float matchedConceptOrInputCount = 0f;
//		float matchedHeadCount = 0f;
//		float skippedPermutationCount = 0f;
//		Vector<String> concepts = HUtils.getKeys(conceptStateHash);
//		Collections.sort(concepts);
//		System.out.println("PerConcept Analysis:");
//		System.out
//				.println("Concept,ConceptNameMatch,InputWordMatch,HeadWordMatch");
//		for (String concept : concepts) {
//			boolean conceptNameFound = false;
//			boolean inputWordsFound = false;
//			boolean inputHeadFound = false;
//			boolean skippedTooManyPermutations = false;
//			Vector<Vector> perms = null;
//			String cname = new String(concept.toLowerCase());
//			if (cname.charAt(0) == '*') {
//				cname = cname.substring(1);
//			}
//			Vector<Concept> c = null;
//			c = mapWordsToConcepts(cname);
//			if (c != null) {
//				conceptNameFound = true;
//			}
//			if (!conceptNameFound) {
//				cname = cname.replace(" ns", " nonspecific");
//				cname = cname.replace(" nos", " nonspecific");
//				c = mapWordsToConcepts(cname);
//				if (c != null) {
//					conceptNameFound = true;
//				}
//			}
//			Vector<Vector<BNState>> statev = conceptStateHash.get(concept);
//			for (Vector<BNState> sv : statev) {
//				Vector<String> words = VUtils.gatherFields(sv, "name");
//				Vector<Vector> p = SetUtils.permutations(words);
//				perms = VUtils.append(perms, p);
//			}
//			Collections.sort(perms, new VUtils.InverseLengthSorter());
//			int pcounter = 0;
//			for (Vector<String> p : perms) {
//				if (pcounter > 50) {
//					skippedTooManyPermutations = true;
//					break;
//				}
//				String str = StrUtils.stringListConcat(p, " ");
//				c = mapWordsToConcepts(str);
//				if (c != null) {
//					inputWordsFound = true;
//					break;
//				}
//				pcounter++;
//			}
//			for (Vector<BNState> sv : statev) {
//				BNState head = getHead(sv);
//				if (head != null) {
//					String str = head.getName();
//					c = mapWordsToConcepts(str);
//					if (c != null) {
//						inputHeadFound = true;
//						break;
//					}
//				}
//			}
//			if (conceptNameFound) {
//				matchedConceptCount++;
//			}
//			if (inputWordsFound) {
//				matchedInputCount++;
//			}
//			if (conceptNameFound || inputWordsFound) {
//				matchedConceptOrInputCount++;
//			}
//			if (inputHeadFound) {
//				matchedHeadCount++;
//			}
//			if (skippedTooManyPermutations) {
//				skippedPermutationCount++;
//			}
//			System.out.print(concept + ",");
//			System.out.print(conceptNameFound ? "C," : "*,");
//			System.out.print(inputWordsFound ? "W," : "*,");
//			System.out.println(inputHeadFound ? "H" : "*");
//		}
//		float conceptCoverage = matchedConceptCount / concepts.size();
//		float inputCoverage = matchedInputCount / concepts.size();
//		float conceptOrInputCoverage = matchedConceptOrInputCount
//				/ concepts.size();
//		float headCoverage = matchedHeadCount / concepts.size();
//
//		System.out.println("\nTOTALS:\n");
//		System.out.println("NumConcepts=" + concepts.size());
//		System.out.println("ConceptName Coverage=" + conceptCoverage);
//		System.out.println("InputWord Coverage=" + inputCoverage);
//		System.out.println("ConceptName Or InputWord Coverage="
//				+ conceptOrInputCoverage);
//		System.out.println("Head Input Word Coverage=" + headCoverage);
//	}

//	static BNState getHead(Vector<BNState> states) {
//		if (states != null) {
//			for (BNState state : states) {
//				if (state.getNode().isTopicHead()) {
//					return state;
//				}
//			}
//		}
//		return null;
//	}

//	static BNTree getTree(Vector<ClassifierTree> trees, BNCase bncase) {
//		if (trees != null) {
//			Vector<String> words = VUtils.gatherFields(bncase.getInput(),
//					"name");
//			if (words != null) {
//				Collections.sort(words);
//				for (ClassifierTree tree : trees) {
//					if (tree.getTerm() instanceof BNTemplate) {
//						BNTemplate template = (BNTemplate) tree.getTerm();
//						Vector<String> twords = VUtils.gatherFields(
//								template.getInput(), "name");
//						Collections.sort(twords);
//						if (words.equals(twords)) {
//							return (BNTree) tree;
//						}
//					}
//				}
//			}
//		}
//		return null;
//	}

//	static boolean findNCBOConcept(String concept) {
//		String cname = new String(concept.toLowerCase());
//		if (cname.charAt(0) == '*') {
//			cname = cname.substring(1);
//		}
//		String command = "virtual/rdf/" + cname;
//		Document doc = Access.getAccess().parseXMLFile(command, "?");
//		return false;
//	}
//
//	static Vector<Concept> mapWordsToConcepts(String str) {
//		Vector<Concept> concepts = null;
//		try {
//			String command = "search/" + str + inputSearchParameters;
//			Document doc = Access.getAccess().parseXMLFile(command, "&");
//			NodeList listOfSearchResults = doc
//					.getElementsByTagName("searchBean");
//			int totalSearchResults = listOfSearchResults.getLength();
//			if (totalSearchResults > 0) {
//				for (int s = 0; s < listOfSearchResults.getLength(); s++) {
//					Node firstSearchNode = listOfSearchResults.item(s);
//					if (firstSearchNode.getNodeType() == Node.ELEMENT_NODE) {
//						Element firstSearchElement = (Element) firstSearchNode;
//						NodeList conceptIdList = firstSearchElement
//								.getElementsByTagName("conceptIdShort");
//						Element conceptIdElement = (Element) conceptIdList
//								.item(0);
//						NodeList conceptIDList = conceptIdElement
//								.getChildNodes();
//						String conceptID = ((Node) conceptIDList.item(0))
//								.getNodeValue().trim();
//						NodeList preferredNameList = firstSearchElement
//								.getElementsByTagName("preferredName");
//						Element preferredNameElement = (Element) preferredNameList
//								.item(0);
//						NodeList textPreferredNameList = preferredNameElement
//								.getChildNodes();
//						String preferredName = ((Node) textPreferredNameList
//								.item(0)).getNodeValue().trim();
//						concepts = VUtils.add(concepts, new Concept(conceptID,
//								preferredName));
//					}
//				}
//			}
//		} catch (Exception err) {
//			err.printStackTrace();
//		}
//		return concepts;
//	}

//	static Vector getRelevantStates(Vector<BNState> states) {
//		Vector relevantStates = null;
//		if (states != null) {
//			for (BNState state : states) {
//				if (state.getType().getDomain().isPulmonaryDomain()
//						&& (state.getType().isCondition() || state.getType()
//								.isLocation())) {
//					relevantStates = VUtils.add(relevantStates, state);
//				}
//			}
//		}
//		return relevantStates;
//	}

//	public static Vector<Vector<String>> gatherProbableWordGroups(
//			Vector<onyx.document.document.Document> documents) {
//		float maxtfidf = 0f;
//		int maxindex = -1;
//		Hashtable<Vector<String>, Vector<String>> hash = new Hashtable();
//		for (onyx.document.document.Document document : documents) {
//			document.analyzeSentences();
//			for (onyx.document.document.Sentence sentence : document
//					.getAllSentences()) {
//				Parse parse = sentence.parse(false);
//				float[] tfidfs = new float[parse.getLength()];
//				for (int i = 0; i < parse.getLength(); i++) {
//					Token token = parse.getWordTokens().elementAt(i);
//					tfidfs[i] = document.getDocumentAccess().getWordTFIDF(
//							token.getString(), sentence.getDocument());
//					if (tfidfs[i] > maxtfidf) {
//						maxtfidf = tfidfs[i];
//						maxindex = i;
//					}
//				}
//				Vector<Phrase> covering = null;
//				if (maxindex >= 0) {
//					covering = parse.getCoveringPhrases(maxindex, false);
//				}
//				if (covering != null) {
//					for (Phrase phrase : covering) {
//						float first = 0, last = 0, previous = 0, following = 0;
//						first = tfidfs[phrase.getStart()];
//						last = tfidfs[phrase.getEnd()];
//						if (phrase.getStart() > 0) {
//							previous = tfidfs[phrase.getStart() - 1];
//						}
//						if (phrase.getEnd() < parse.getLength() - 1) {
//							following = tfidfs[phrase.getEnd() + 1];
//						}
//						if (first > previous && last > following) {
//							Vector<String> v = null;
//							for (int i = phrase.getStart(); i <= phrase
//									.getEnd(); i++) {
//								Token token = parse.getWordTokens()
//										.elementAt(i);
//								if (token.getWord() != null
//										&& (token.getWord().isNoun() || token
//												.getWord().isAdjective())) {
//									v = VUtils.addIfNot(v, token.getString()
//											.toLowerCase());
//								}
//							}
//							if (v != null) {
//								hash.put(v, v);
//							}
//						}
//					}
//				}
//				sentence.setParse(null);
//			}
//		}
//		Vector<Vector<String>> result = HUtils.getKeys(hash);
//		return result;
//	}

}
