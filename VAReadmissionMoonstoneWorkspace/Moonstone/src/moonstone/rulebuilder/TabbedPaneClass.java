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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import moonstone.annotation.AnnotationIntegrationMaps;

public class TabbedPaneClass extends JTabbedPane {

	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	JPanel rulePanel = null;
	JPanel queryPanel = null;

	public TabbedPaneClass(MoonstoneRuleInterface mri) {
		this.moonstoneRuleInterface = mri;
		this.createRulePanel();
		this.addTab("Rule", this.rulePanel);
		this.queryPanel = mri.moonstoneQueryPanel = new MoonstoneQueryPanel(mri);
		this.addTab("Query", this.queryPanel);
	}
	
	private void createRulePanel() {
		MoonstoneRuleInterface mri = this.moonstoneRuleInterface;
		this.rulePanel = new JPanel();
		this.rulePanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Rule Type:");
		mri.ruleTypeCB = new JComboBox(MoonstoneRuleInterface.RuleTypes);
		mri.ruleTypeCB.setSelectedItem("wordrule");
		mri.ruleType = (String) mri.ruleTypeCB.getSelectedItem();
		mri.ruleTypeCB.addActionListener(mri);
		mri.ruleTypeCB.addItemListener(mri);
		panel.add(label);
		panel.add(mri.ruleTypeCB);
		this.rulePanel.add(panel, c);
		c.gridy++;

		panel = new JPanel();
		label = new JLabel("Definition:");
		Dimension d = new Dimension(600, 200);
		mri.ruleDefinitionTextPane = new JTextPane();
		mri.ruleDefinitionTextPane.setMinimumSize(d);
		mri.ruleDefinitionTextPane.setPreferredSize(d);
		mri.ruleDefinitionTextPane.setEditable(true);
		mri.ruleDefinitionTextPane.setVisible(true);
		mri.ruleDefinitionTextPane.addKeyListener(mri);
		JScrollPane jsp = new JScrollPane(mri.ruleDefinitionTextPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setMinimumSize(d);
		jsp.setPreferredSize(d);
		jsp.setVisible(true);
		panel.add(label);
		panel.add(jsp);
		this.rulePanel.add(panel, c);
		c.gridy++;

		panel = new JPanel();
		label = new JLabel("File:");
		mri.ruleFileNameTextField = new JTextField(12);
		mri.ruleFileNameTextField.addActionListener(mri);
		panel.add(label);
		panel.add(mri.ruleFileNameTextField);

		mri.ruleFileNameButton = new JButton("Choose");
		mri.ruleFileNameButton.addActionListener(mri);
		panel.add(mri.ruleFileNameButton);

		label = new JLabel("Concepts:");
		mri.ruleConceptCB = new JComboBox(
				AnnotationIntegrationMaps.getConcepts());
		mri.ruleConceptCB.addActionListener(mri);
		panel.add(label);
		panel.add(mri.ruleConceptCB);
		this.rulePanel.add(panel, c);
		c.gridy++;

		panel = new JPanel();
		label = new JLabel("SearchToken:");
		mri.ruleTokenTextField = new JTextField(12);
		mri.ruleTokenTextField.addActionListener(mri);
		panel.add(label);
		panel.add(mri.ruleTokenTextField);

		label = new JLabel("SearchName:");
		mri.ruleNameTextField = new JTextField(12);
		mri.ruleNameTextField.addActionListener(mri);
		panel.add(label);
		panel.add(mri.ruleNameTextField);
		label = new JLabel("IDs:");
		mri.ruleIDCB = new JComboBox() {
			public void actionPerformed(ActionEvent e) {
				MoonstoneRuleInterface mri = MoonstoneRuleInterface.RuleEditor;
				mri.ruleID = (String) mri.ruleIDCB.getSelectedItem();
			}
		};
		mri.ruleIDCB.addActionListener(mri);
		mri.ruleIDCB.addItemListener(mri);
		panel.add(label);
		panel.add(mri.ruleIDCB);
		this.rulePanel.add(panel, c);
	}
	
	// Before 9/6/2015
//	private void createRulePanel() {
//		MoonstoneRuleInterface mri = this.moonstoneRuleInterface;
//		this.rulePanel = new JPanel();
//		this.rulePanel.setLayout(new GridBagLayout());
//		
//		GridBagConstraints c = new GridBagConstraints();
//		c.gridx = 0;
//		c.gridy = 0;
//		
//		JPanel panel = new JPanel();
//		JLabel label = new JLabel("Rule Type:");
//		mri.ruleTypeCB = new JComboBox(MoonstoneRuleInterface.RuleTypes);
//		mri.ruleTypeCB.setSelectedItem("wordrule");
//		mri.ruleType = (String) mri.ruleTypeCB.getSelectedItem();
//		mri.ruleTypeCB.addActionListener(mri);
//		mri.ruleTypeCB.addItemListener(mri);
//		panel.add(label);
//		panel.add(mri.ruleTypeCB);
//		this.rulePanel.add(panel, c);
//		c.gridy++;
//
//		panel = new JPanel();
//		label = new JLabel("Definition:");
//		Dimension d = new Dimension(600, 200);
//		mri.ruleDefinitionTextPane = new JTextPane();
//		mri.ruleDefinitionTextPane.setMinimumSize(d);
//		mri.ruleDefinitionTextPane.setPreferredSize(d);
//		mri.ruleDefinitionTextPane.setEditable(true);
//		mri.ruleDefinitionTextPane.setVisible(true);
//		mri.ruleDefinitionTextPane.addKeyListener(mri);
//		JScrollPane jsp = new JScrollPane(mri.ruleDefinitionTextPane,
//				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
//				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		jsp.setMinimumSize(d);
//		jsp.setPreferredSize(d);
//		jsp.setVisible(true);
//		panel.add(label);
//		panel.add(jsp);
//		this.rulePanel.add(panel, c);
//		c.gridy++;
//
//		panel = new JPanel();
//		label = new JLabel("File:");
//		mri.ruleFileNameTextField = new JTextField(12);
//		mri.ruleFileNameTextField.addActionListener(mri);
//		panel.add(label);
//		panel.add(mri.ruleFileNameTextField);
//
//		mri.ruleFileNameButton = new JButton("Choose");
//		mri.ruleFileNameButton.addActionListener(mri);
//		panel.add(mri.ruleFileNameButton);
//
//		label = new JLabel("Concepts:");
//		mri.ruleConceptCB = new JComboBox(
//				AnnotationIntegrationMaps.getConcepts());
//		mri.ruleConceptCB.addActionListener(mri);
//		panel.add(label);
//		panel.add(mri.ruleConceptCB);
//		this.rulePanel.add(panel, c);
//		c.gridy++;
//
//		panel = new JPanel();
//		label = new JLabel("SearchToken:");
//		mri.ruleTokenTextField = new JTextField(12);
//		mri.ruleTokenTextField.addActionListener(mri);
//		panel.add(label);
//		panel.add(mri.ruleTokenTextField);
//
//		label = new JLabel("SearchName:");
//		mri.ruleNameTextField = new JTextField(12);
//		mri.ruleNameTextField.addActionListener(mri);
//		panel.add(label);
//		panel.add(mri.ruleNameTextField);
//		label = new JLabel("IDs:");
//		mri.ruleIDCB = new JComboBox() {
//			public void actionPerformed(ActionEvent e) {
//				MoonstoneRuleInterface mri = MoonstoneRuleInterface.RuleEditor;
//				mri.ruleID = (String) mri.ruleIDCB.getSelectedItem();
//			}
//		};
//		mri.ruleIDCB.addActionListener(mri);
//		mri.ruleIDCB.addItemListener(mri);
//		panel.add(label);
//		panel.add(mri.ruleIDCB);
//		this.rulePanel.add(panel, c);
//
//	}

}
