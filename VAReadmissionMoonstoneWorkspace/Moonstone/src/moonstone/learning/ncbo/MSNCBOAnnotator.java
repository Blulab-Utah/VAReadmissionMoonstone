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

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import tsl.documentanalysis.document.Document;
import tsl.utilities.JDomUtils;
import tsl.utilities.VUtils;

public class MSNCBOAnnotator {
	// public static final String annotatorUrl =
	// "http://rest.bioontology.org/obs/annotator";

	// 11/17/2014
	public static final String annotatorUrl = "http://data.bioontology.org/annotator";

	public static void extractCUIsFromNCBO(Vector<Document> documents) {
		Hashtable hash = new Hashtable();
		Vector<MSConcept> allConcepts = new Vector(0);
		int count = 0;
		if (documents != null) {
			for (Document document : documents) {
				System.out.println("\tExtracting NCBO concepts from "
						+ document.getName());
				Vector<MSConcept> concepts = getConcepts(document.getText());
				if (concepts != null) {
					for (MSConcept concept : concepts) {
						concept.addToHash(hash, allConcepts);
					}
				}
				document.reset();
			}
		}
		if (allConcepts != null) {
			for (MSConcept concept : allConcepts) {
				count += concept.addCUIStructures();
			}
		}
		System.out.println("\tDone.  New NCBO concepts=" + count);
	}

	public static Vector<MSConcept> getConcepts(String text) {
		Vector<MSConcept> concepts = null;
		try {
			HttpClient client = new HttpClient();
			PostMethod method = new PostMethod(annotatorUrl);

			method.addParameter("text", text);
			method.addParameter("format", "xml");

			method.addParameter("longest_only", "true");
			method.addParameter("mappings", "true");
			method.addParameter("exclude_numbers", "true");

			method.addParameter("asfasdf", "asdfas");

			method.addParameter("apikey",
					"3c98c4df-7b8d-4971-b540-e8aa8651a87b");

			// Execute the POST method
			int statusCode = client.executeMethod(method);

			if (statusCode != -1) {
				try {
					String xml = method.getResponseBodyAsString();
					concepts = extractConcepts(xml);
					method.releaseConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return concepts;
	}

	// public static Vector<Concept> getConcepts(String text) {
	// Vector<Concept> concepts = null;
	// try {
	// HttpClient client = new HttpClient();
	// PostMethod method = new PostMethod(annotatorUrl);
	//
	// // Configure the form parameters
	// // method.addParameter("longestOnly", "true");
	// method.addParameter("wholeWordOnly", "true");
	// method.addParameter("filterNumber", "true");
	// method.addParameter("stopWords", "");
	// method.addParameter("withDefaultStopWords", "true");
	// method.addParameter("isTopWordsCaseSensitive", "false");
	// method.addParameter("mintermSize", "3");
	// method.addParameter("scored", "true");
	// method.addParameter("withSynonyms", "true");
	// method.addParameter("ontologiesToExpand", "");
	// method.addParameter("ontologiesToKeepInResult", "1057,1404,1499");
	// // method.addParameter("ontologiesToKeepInResult", "");
	// method.addParameter("isVirtualOntologyId", "true");
	// method.addParameter("semanticTypes", "");
	// method.addParameter("levelMax", "0");
	// method.addParameter("mappingTypes", "null"); // null, Automatic
	// method.addParameter("textToAnnotate", text);
	// // method.addParameter("format", "text");
	// method.addParameter("format", "xml");
	// // Options are 'text', 'xml',
	// // 'tabDelimited'
	// method.addParameter("apikey",
	// "3c98c4df-7b8d-4971-b540-e8aa8651a87b");
	//
	// // Execute the POST method
	// int statusCode = client.executeMethod(method);
	//
	// if (statusCode != -1) {
	// try {
	// String xml = method.getResponseBodyAsString();
	// concepts = extractConcepts(xml);
	// method.releaseConnection();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return concepts;
	// }

	private static Vector<MSConcept> extractConcepts(String xml) {
		Vector<MSConcept> concepts = null;
		try {
			org.jdom.Document jdoc = new SAXBuilder()
					.build(new ByteArrayInputStream(xml.getBytes()));
			Element root = jdoc.getRootElement();
			Vector<Element> acnodes = JDomUtils.getElementsByName(root,
					"annotationCollection");
			if (acnodes != null) {
				for (Element acnode : acnodes) {
					Vector<Element> anodes = JDomUtils.getElementsByName(
							acnode, "annotation");
					if (anodes != null) {
						for (Element anode : anodes) {
							Element ascnode = JDomUtils.getElementByName(anode,
									"annotationsCollection");
							if (ascnode != null) {
								Element asnode = JDomUtils.getElementByName(ascnode,
										"annotations");
								if (asnode != null) {
									Element cnode = JDomUtils.getElementByName(asnode,
											"text");
									String text = cnode.getValue();
									cnode = JDomUtils.getElementByName(asnode,
											"matchType");
									String mtype = cnode.getValue();
									cnode = JDomUtils.getElementByName(asnode,
											"from");
									String intstr = cnode.getValue();
									int from = Integer.parseInt(intstr);
									cnode = JDomUtils.getElementByName(asnode,
											"from");
									intstr = cnode.getValue();
									int to = Integer.parseInt(intstr);
									MSConcept c = new MSConcept("cid?", "fid?",
											"oid?", null, null, null, from, to);
									concepts = VUtils.add(concepts, c);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return concepts;
	}

	// private static Vector<Concept> extractConcepts(String xml) {
	// Vector<Concept> concepts = null;
	// String str;
	// try {
	// org.jdom.Document jdoc = new SAXBuilder()
	// .build(new ByteArrayInputStream(xml.getBytes()));
	// Element root = jdoc.getRootElement();
	// Vector<Element> nodes = JDomUtils.getElementsByName(root,
	// "annotationBean");
	// if (nodes != null) {
	// for (Element node : nodes) {
	// Element cnode = JDomUtils.getElementByName(node, "concept");
	// String localConceptID = cnode.getChild("localConceptId")
	// .getText();
	// String localOntologyID = cnode.getChild("localOntologyId")
	// .getText();
	// String fullID = cnode.getChild("fullId").getText();
	// String preferredName = cnode.getChild("preferredName")
	// .getText();
	// Vector<Element> snodes = JDomUtils.getElementsByName(cnode,
	// "semanticTypeBean");
	// Vector<String> semanticTypes = null;
	// if (snodes != null) {
	// for (Element snode : snodes) {
	// semanticTypes = VUtils.add(semanticTypes, snode
	// .getChild("semanticType").getText()
	// .toLowerCase());
	// }
	// }
	// snodes = JDomUtils.getElementsByName(cnode, "synonyms");
	// Vector<String> synonyms = null;
	// if (snodes != null) {
	// for (Element snode : snodes) {
	// str = snode.getChildText("string");
	// synonyms = VUtils.add(synonyms, str);
	// }
	// }
	// cnode = JDomUtils.getElementByName(node, "context");
	// int from = Integer.parseInt(cnode.getChildText("from"));
	// int to = Integer.parseInt(cnode.getChildText("to"));
	// Concept concept = new Concept(localConceptID, fullID,
	// localOntologyID, preferredName, semanticTypes,
	// synonyms, from, to);
	// concepts = VUtils.add(concepts, concept);
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return concepts;
	// }

}
