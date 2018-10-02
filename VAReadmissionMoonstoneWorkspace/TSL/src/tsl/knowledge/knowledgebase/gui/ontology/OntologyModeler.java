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
package tsl.knowledge.knowledgebase.gui.ontology;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.ontology.Ontology;
import tsl.utilities.StrUtils;

public class OntologyModeler extends JPanel {
	private KnowledgeEngine knowledgeEngine = null;
	private Ontology ontology = null;
	private TypeTreePanel typeTreePanel = null;
	private TypeDescriptionPanel typeDescriptionPanel = null;
	private boolean painted = false;
	
	public OntologyModeler(KnowledgeEngine ke) {
		this.knowledgeEngine = ke;
		this.ontology = ke.getOntology();
		initializeLayout();
	}
	
	private void initializeLayout() {
		Dimension minimumSize = new Dimension(800, 600);
		this.typeTreePanel = new TypeTreePanel(this);
		this.typeDescriptionPanel = new TypeDescriptionPanel(this);
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				this.typeTreePanel, this.typeDescriptionPanel) {
			public void paint(Graphics g) {
				super.paint(g);
				if (!painted) {
					painted = true;
					this.setDividerLocation(0.50);
				}
			}
		};
		sp.setMinimumSize(minimumSize);
		this.setMinimumSize(minimumSize);
		this.setPreferredSize(minimumSize);
//		this.typeTreePanel.setMinimumSize(minimumSize);
//		this.typeDescriptionPanel.setMinimumSize(minimumSize);
		this.add(sp);
	}

	public KnowledgeEngine getKnowledgeEngine() {
		return knowledgeEngine;
	}

	public void setKnowledgeEngine(KnowledgeEngine knowledgeEngine) {
		this.knowledgeEngine = knowledgeEngine;
	}

	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(Ontology ontology) {
		this.ontology = ontology;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				try {
					Dimension minimumSize = new Dimension(800, 400);
					Ontology ontology = new Ontology("OWL");
					KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine();
					ke.setOntology(ontology);
//					extractOWLOntology("/Users/leechristensen/Desktop/EvaluationWorkbenchFolder/resources/NLPSchema-1.txt", ontology);
					OntologyModeler om = new OntologyModeler(ke);
					JFrame frame = new JFrame("TSLOntologyModeler");
					frame.setPreferredSize(minimumSize);
					frame.setContentPane(om);
					frame.pack();
					frame.setVisible(true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(new JFrame(),
							StrUtils.getStackTrace(e));
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});
	}

}
