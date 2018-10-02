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
package workbench.api.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.startup.StartupParameters;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;
import workbench.api.Analysis;
import workbench.api.constraint.ConstraintPacket;

public class ConstraintPanel extends JPanel implements ActionListener,
		ListSelectionListener {

	private Analysis analysis = null;
	private Map<String, ConstraintPacket> constraintMap = new HashMap();
	private KnowledgeBase knowledgeBase = null;
	private JList constraintJList = null;
	private Vector<ConstraintPacket> constraintPackets = null;
	private ConstraintPacket selectedConstraintPacket = null;
	private JTextField queryNamePane = null;
	private JTextPane queryExpressionPane = null;
	private JButton processNewQueryButton = null;
	private static String ConstraintFileParameter = "ConstraintFile";

	public static String[][] constraintDefinitions = {
			{ "AnnotationHasClassification",
					"(\"annotationHasClassification\" ?annotation ?value)" },
			{ "MatchingPairHasSameType",
					"(\"annotationPairHasSameType\" ?annotation1 ?annotation2)" },
			{
					"PrimaryHasClassificationAndSecondaryDoesNot",
					"(and (\"annotationHasClassification\" ?annotation1 ?classname) "
							+ "(not (\"annotationHasClassification\" ?annotation2 ?classname)))" },
			{
					"SameClassificationButPrimaryIsAbsentAndSecondaryIsNot",
					"(and (\"annotationHasClassification\" ?annotation ?value) "
							+ " (\"annotationHasAttributeValue\" ?annotation \"directionality\" \"negated\"))" },

	};

	public ConstraintPanel(Analysis analysis) throws Exception {
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		this.analysis = analysis;
		this.knowledgeBase = this.analysis.getKnownledgeEngine()
				.getCurrentKnowledgeBase();
		initialize();
		JScrollPane jsp = null;
		if (this.constraintPackets != null) {
			this.selectedConstraintPacket = this.constraintPackets
					.firstElement();
			this.constraintJList = new JList(this.getAllConstraintNames());
			this.constraintJList
					.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			this.constraintJList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			this.constraintJList.setVisibleRowCount(-1);
			this.constraintJList.addListSelectionListener(this);
			jsp = new JScrollPane(this.constraintJList,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jsp.setPreferredSize(new Dimension(250, 250));
			this.add(jsp, c);
		}

		c.gridy++;
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Query Name:");
		panel.add(label);
		this.queryNamePane = new JTextField(20);
		panel.add(this.queryNamePane);
		this.add(panel, c);

		c.gridy++;
		this.queryExpressionPane = new JTextPane();
		this.queryExpressionPane.setEditable(true);
		this.queryExpressionPane.setCaretPosition(0);
		this.queryExpressionPane.setFocusTraversalKeysEnabled(false);
		Dimension d = new Dimension(600, 200);
		this.queryExpressionPane.setPreferredSize(d);
		this.queryExpressionPane.setMinimumSize(d);
		jsp = new JScrollPane(this.queryExpressionPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setPreferredSize(new Dimension(250, 250));
		this.add(jsp, c);

		c.gridy++;
		panel = new JPanel();
		this.processNewQueryButton = new JButton("StoreQuery");
		this.processNewQueryButton.addActionListener(this);
		panel.add(this.processNewQueryButton);
		this.add(panel, c);
	}

	private void initialize() throws Exception {
		StartupParameters sp = KnowledgeEngine.getCurrentKnowledgeEngine()
				.getStartupParameters();
		String fpath = sp.getResourceFileName(ConstraintFileParameter);
		if (fpath != null) {
			TLisp tlisp = TLisp.getTLisp();
			Sexp sexp = (Sexp) tlisp.loadFile(fpath);
			for (Enumeration<Sexp> e = sexp.elements(); e.hasMoreElements();) {
				Sexp sv = e.nextElement();
				String name = (String) TLUtils.convertToJObject(sv.getFirst());
				Sexp s = (Sexp) sv.getSecond();
				Vector v = TLUtils.convertSexpToJVector(s);
				String cstr = s.toString();
				ConstraintPacket cp = createConstraintPacket(name, v, cstr);
				if (cp != null) {
					this.constraintMap.put(cp.getName(), cp);
					this.constraintPackets = VUtils.add(this.constraintPackets,
							cp);
				}
			}
		}
	}

	public ConstraintPacket createConstraintPacket(String name, Vector v,
			String cstr) throws Exception {
		ConstraintPacket cp = this.getConstraintPacket(name);
		if (cp == null) {
			cp = ConstraintPacket.createConstraintPacket(this.knowledgeBase,
					name, v, cstr, this.analysis);
			if (cp != null) {
				this.constraintMap.put(name, cp);
			}
		}
		return cp;
	}

	private ConstraintPacket getConstraintPacket(String name) {
		return this.constraintMap.get(name);
	}

	public Vector<String> getAllConstraintNames() {
		return HUtils.getKeys(this.constraintMap);
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			String cname = (String) this.constraintJList.getSelectedValue();
			ConstraintPacket cp = this.getConstraintPacket(cname);
			this.analysis.setSelectedConstraintPacket(cp);
			this.analysis.updateStatistics();
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String cmd = e.getActionCommand();
		if (source.equals(this.processNewQueryButton)) {
			System.out.println("Hit Done button.");
		}
	}

	public ConstraintPacket getSelectedConstraintPacket() {
		return selectedConstraintPacket;
	}

}
