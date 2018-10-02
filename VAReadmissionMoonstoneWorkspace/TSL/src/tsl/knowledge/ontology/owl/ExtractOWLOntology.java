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
package tsl.knowledge.ontology.owl;

//import java.io.File;
//import java.io.FileReader;
//import java.util.Collection;
//import java.util.Iterator;
//
////import org.semanticweb.owlapi.apibinding.OWLManager;
////import org.semanticweb.owlapi.model.IRI;
////import org.semanticweb.owlapi.model.OWLDataFactory;
////import org.semanticweb.owlapi.model.OWLOntology;
////import org.semanticweb.owlapi.model.OWLOntologyCreationException;
////import org.semanticweb.owlapi.model.OWLOntologyManager;
//
//import edu.stanford.smi.protege.exception.OntologyLoadException;
//import edu.stanford.smi.protegex.owl.model.OWLComplementClass;
//import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
//import edu.stanford.smi.protegex.owl.model.OWLIndividual;
//import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
//import edu.stanford.smi.protegex.owl.model.OWLMinCardinality;
//import edu.stanford.smi.protegex.owl.model.OWLModel;
//import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
//import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
//import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
//import edu.stanford.smi.protegex.owl.model.RDFIndividual;
//import edu.stanford.smi.protegex.owl.model.RDFProperty;
//import edu.stanford.smi.protegex.owl.model.RDFResource;
//import edu.stanford.smi.protegex.owl.model.RDFSClass;
//import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
//import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
//import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
//import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFProperty;
//import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSDatatype;
//import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSNamedClass;
//
//import tsl.expression.term.relation.RelationConstant;
//import tsl.expression.term.type.TypeConstant;
//import tsl.knowledge.engine.KnowledgeEngine;
//import tsl.knowledge.ontology.Ontology;
//import tsl.knowledge.ontology.TypeRelationSentence;
//
//import edu.stanford.smi.protegex.owl.ProtegeOWL;
//
public class ExtractOWLOntology {
//
//	public static void main(String[] args) {
//		try {
//			// OWLOntologyManager manager =
//			// OWLManager.createOWLOntologyManager();
//			// IRI iri = IRI
//			// .create("http://blulab.chpc.utah.edu/ontologies/SchemaOntology.owl");
//			// OWLOntology ontology = manager
//			// .loadOntologyFromOntologyDocument(iri);
//			//
//			// OWLDataFactory factory = manager.getOWLDataFactory();
//
//			KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine();
//			Ontology tslOntology = new Ontology("owl");
//			ke.setCurrentOntology(tslOntology);
//
//			extractOWLOntology(
//					"/Users/leechristensen/Desktop/MelissaOntologies/RDF/SchemaOntology.rdf",
//					tslOntology);
//
//			// extractOWLOntology(
//			// "/Users/leechristensen/Desktop/MelissaOntologies/RDF/ModifierOntology.rdf",
//			// tslOntology);
//
//			extractOWLOntology(
//					"/Users/leechristensen/Desktop/MelissaOntologies/RDF/PneumoniaOntology.rdf",
//					tslOntology);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void extractOWLOntology(String filename, Ontology ontology) {
//		try {
//			File file = new File(filename);
//			FileReader reader = new FileReader(file);
//			OWLModel model = ProtegeOWL.createJenaOWLModelFromReader(reader);
//			RDFSClass root = (RDFSClass) model.getRootCls();
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
//
//				if ("dyspnea".equals(shortName)) {
//					int x = 1;
//					x = x;
//				}
//
//				TypeConstant type = TypeConstant.createTypeConstant(shortName,
//						fullName);
//				type.setOntology(ontology);
//				// types = VUtils.add(types, type);
//				Collection c = sc.getOwnSlots();
//				if (c != null) {
//					DefaultRDFProperty umlsrp = null;
//					for (Iterator i = c.iterator(); i.hasNext();) {
//						o = i.next();
//						if (o instanceof DefaultRDFProperty) {
//							DefaultRDFProperty p = (DefaultRDFProperty) o;
//							if (p.getName().contains("blulab")) {
//								
//								System.out.println("Type=" + type
//										+ ",Property="
//										+ p.getName());
//								
//								if (type.getName().contains("consolidation") && p.getName().contains("altLabel")) {
//									int x = 1;
//									x = x;
//								}
//								
//								Collection values = (Collection) sc
//										.getPropertyValues(p);
//								
//								if (values != null && !values.isEmpty()) {
//									for (Iterator pi = values.iterator(); pi
//											.hasNext();) {
//										Object value = pi.next();
//										String vname = value.toString();
//										TypeConstant tc = TypeConstant
//												.findByName(vname);
//										if (tc != null && tc != type) {
//											tc.addParent(type);
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			Collection props = model.getRDFProperties();
//			for (Iterator it = props.iterator(); it.hasNext();) {
//				RDFProperty rp = (RDFProperty) it.next();
//				String rfn = rp.getName();
//				String rsn = extractShortName(rfn);
//
//				if (!rfn.contains("blulab")) {
//					continue;
//				}
//				RelationConstant rc = RelationConstant
//						.createRelationConstant(rsn);
//				for (Iterator i = rp.getSuperproperties(false).iterator(); i
//						.hasNext();) {
//					RDFProperty parent = (RDFProperty) i.next();
//					String prfn = rp.getName();
//					String prsn = extractShortName(rfn);
//					RelationConstant prc = RelationConstant
//							.createRelationConstant(prsn);
//					rc.addParent(prc);
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
//				if (range instanceof RDFSDatatype) {
//					RDFSDatatype dt = (RDFSDatatype) range;
//					RDFSNamedClass t = (RDFSNamedClass) dt.getRDFType();
//					int x = 1;
//					x = x;
//				}
//				if (modifier == null) {
//					int x = 1;
//					x = x;
//				}
//				if (subject != null) {
//					if (modifier != null) {
//						TypeRelationSentence trs = new TypeRelationSentence(rc,
//								subject, modifier);
//						ontology.addSentence(trs);
//
//					}
//				}
//			}
//			ontology.setupTypeConnectionsWithExpandedRelations();
//			// Before 11/22/2013
//			// ontology.setupTypeConnectionsWithExpandedRelations(types);
//			// String lstr = ontology.toLispString();
//			// System.out.println(lstr);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static void doTest() {
//		try {
//			OWLModel model = ProtegeOWL.createJenaOWLModel();
//
//			OWLNamedClass personClass = model.createOWLNamedClass("Person");
//
//			OWLDatatypeProperty ageProperty = model
//					.createOWLDatatypeProperty("age");
//			ageProperty.setRange(model.getXSDint());
//			ageProperty.setDomain(personClass);
//
//			OWLObjectProperty childrenProperty = model
//					.createOWLObjectProperty("children");
//			childrenProperty.setRange(personClass);
//			childrenProperty.setDomain(personClass);
//
//			RDFIndividual darwin = personClass.createRDFIndividual("Darwin");
//			darwin.setPropertyValue(ageProperty, new Integer(0));
//
//			RDFIndividual holgi = personClass.createRDFIndividual("Holger");
//			holgi.setPropertyValue(childrenProperty, darwin);
//			holgi.setPropertyValue(ageProperty, new Integer(33));
//
//			OWLNamedClass brotherClass = model.createOWLNamedClass("Brother");
//			brotherClass.addSuperclass(personClass);
//			brotherClass.removeSuperclass(model.getOWLThingClass());
//
//			OWLIndividual individual = brotherClass.createOWLIndividual("Hans");
//
//			OWLNamedClass sisterClass = model.createOWLNamedSubclass("Sister",
//					personClass);
//
//			OWLDatatypeProperty property = model
//					.createOWLDatatypeProperty("name");
//			property.setRange(model.getXSDstring());
//
//			RDFSDatatype xsdDate = model.getRDFSDatatypeByName("xsd:date");
//			OWLDatatypeProperty dateProperty = model.createOWLDatatypeProperty(
//					"dateProperty", xsdDate);
//			RDFSLiteral dateLiteral = model.createRDFSLiteral("1971-07-06",
//					xsdDate);
//			individual.setPropertyValue(dateProperty, dateLiteral);
//			RDFSLiteral myDate = (RDFSLiteral) individual
//					.getPropertyValue(dateProperty);
//
//			OWLNamedClass animalClass = model.createOWLNamedClass("Animal");
//
//			// So the range of the children class property becomes the union of
//			// >=1 classes.
//			childrenProperty.addUnionDomainClass(personClass);
//			childrenProperty.addUnionDomainClass(animalClass);
//
//			OWLObjectProperty sonsProperty = model
//					.createOWLObjectProperty("sons");
//			sonsProperty.addSuperproperty(childrenProperty);
//			Collection ud = sonsProperty.getUnionDomain(true);
//
//			boolean t1 = ud.contains(personClass);
//			boolean t2 = ud.contains(animalClass);
//
//			OWLMinCardinality minCardinality = model.createOWLMinCardinality(
//					childrenProperty, 1);
//			personClass.addSuperclass(minCardinality);
//
//			OWLNamedClass manClass = model.createOWLNamedSubclass("Man",
//					personClass);
//			OWLNamedClass womanClass = model.createOWLNamedSubclass("Woman",
//					personClass);
//
//			// Create expression (PersonClass & !(Man | Woman))
//			OWLUnionClass unionClass = model.createOWLUnionClass();
//			unionClass.addOperand(manClass);
//			unionClass.addOperand(womanClass);
//			OWLComplementClass complementClass = model
//					.createOWLComplementClass(unionClass);
//			OWLIntersectionClass intersectionClass = model
//					.createOWLIntersectionClass();
//			intersectionClass.addOperand(personClass);
//			intersectionClass.addOperand(complementClass);
//
//			OWLNamedClass kidClass = model.createOWLNamedClass("Kid");
//			kidClass.addSuperclass(intersectionClass);
//
//			// OWLNamedClass kidClass = (OWLNamedClass)
//			// model.createRDFSClassFromExpression("Person & !(Man | Woman)");
//			String parsable = intersectionClass.getParsableExpression();
//			System.out.println("Expression: " + parsable);
//
//			RDFSClass c = model.createRDFSClassFromExpression("!(" + parsable
//					+ ")");
//			System.out.println("New expression: " + c.getParsableExpression());
//
//			printClassTree(personClass, "");
//
//		} catch (OntologyLoadException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static void printClassTree(RDFSClass cls, String indentation) {
//		System.out.println(indentation + cls.getName());
//		for (Iterator it = cls.getSubclasses(false).iterator(); it.hasNext();) {
//			RDFSClass subclass = (RDFSClass) it.next();
//			printClassTree(subclass, indentation + "    ");
//		}
//	}
//
//	private static String extractShortName(String fullName) {
//		int index = -1;
//		String shortName = fullName;
//		if ((index = fullName.indexOf("#")) > 0) {
//			shortName = fullName.substring(index + 1);
//		}
//		return shortName;
//	}

}