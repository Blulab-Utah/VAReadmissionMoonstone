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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;

import tsl.dbaccess.mysql.MySQL;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.jlisp.JLUtils;
import tsl.jlisp.JLisp;
import tsl.jlisp.JLispException;
import tsl.jlisp.Sexp;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.ontology.Ontology;
import tsl.knowledge.ontology.TypeRelationSentence;
import tsl.utilities.VUtils;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class UMLSOntology extends Ontology {

	public UMLSOntology() {
		super("umls");
	}

	public static UMLSOntology createFromLisp(String ontologyString)
			throws JLispException {
		KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine();
		UMLSOntology ontology = ke.getUMLSOntology();
		ke.setCurrentOntology(ontology);
		JLisp jLisp = JLisp.getJLisp();
		Sexp sexp = (Sexp) jLisp.evalString(ontologyString);
		Vector elements = JLUtils.convertSexpToJVector(sexp);
		// Vector predicates = VUtils.rest(elements);
		// ontology.addTypesAndRelations(predicates);
		ke.resetCurrentOntology();
		return ontology;
	}
	
	public void addTypesAndRelations(Vector predicates) {
		if (predicates != null) {
			for (Enumeration e = predicates.elements(); e.hasMoreElements();) {
				Vector v = (Vector) e.nextElement();
				UMLSTypeConstant subject = null;
				UMLSTypeConstant modifier = null;
				String relname = (String) v.elementAt(0);
				String sname = (String) v.elementAt(1);
				String mname = (String) v.elementAt(2);
				if (!"domain".equals(relname)) {
					subject = (UMLSTypeConstant) UMLSTypeConstant
							.createUMLSTypeConstant(sname);
					RelationConstant rc = RelationConstant
							.createRelationConstant(relname);
					modifier = (UMLSTypeConstant) UMLSTypeConstant
							.createUMLSTypeConstant(mname);
					if ("isa".equals(rc.getName())) {
						subject.addParent(modifier);
					} else {
						TypeRelationSentence trs = new TypeRelationSentence(rc,
								subject, modifier);
						addSentence(trs);
					}
				}
			}
			setupTypeConnectionsWithExpandedRelations();
		}
	}
	
	public void readOntologySQL() {
		try {
			System.out.print("Loading UMLS Semantic Network ...");
			Connection connection = MySQL.getMySQL().getUMLSConnection();
			if (connection != null) {
				String sql = "select sty_rl1, rl, sty_rl2 from srstr";
				PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) connection
						.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				for (boolean validrow = rs.first(); validrow; validrow = rs
						.next()) {
					String st1 = rs.getString(1);
					String relstr = rs.getString(2);
					String st2 = rs.getString(3);
					if (st1 != null && relstr != null && st2 != null) {
						st1 = st1.toLowerCase();
						relstr = relstr.toLowerCase();
						st2 = st2.toLowerCase();
						UMLSTypeInfo ti1 = UMLSTypeInfo.findByUI(st1);
						UMLSTypeInfo ti2 = UMLSTypeInfo.findByUI(relstr);
						UMLSTypeInfo ti3 = UMLSTypeInfo.findByUI(st2);
						if (!ti1.isRelation() && !ti1.equals(ti3)) {
							RelationConstant rc = (RelationConstant) ti2
									.getTerm();
							TypeConstant subject = (TypeConstant) ti1.getTerm();
							TypeConstant modifier = (TypeConstant) ti3
									.getTerm();
							TypeRelationSentence trs = new TypeRelationSentence(
									rc, subject, modifier);
							if ("isa".equals(trs.getRelation().getName())) {
								subject.addParent(modifier);
							}
						}
					}
				}
				ps.close();
				// Before 11/22/2013.  Again, types are added to the namespace and don't
				// need to be gathered here.
//				Vector types = new Vector(0);
//				UMLSStructuresShort uss = UMLSStructuresShort.currentUMLSStructs;
//				if (uss.getAllCUIStructures() != null) {
//					for (CUIStructureShort cp : UMLSStructuresShort
//							.getUMLSStructures().getAllCUIStructures()) {
//						if (cp.getTypeInfo() != null
//								&& cp.typeInfo.getTerm() != null) {
//							types = VUtils.addIfNot(types,
//									cp.typeInfo.getTerm());
//						}
//					}
//				}
				setupTypeConnectionsWithExpandedRelations();
				System.out.println("Done");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
