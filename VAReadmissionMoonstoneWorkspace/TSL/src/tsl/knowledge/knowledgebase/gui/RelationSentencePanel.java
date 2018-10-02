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
package tsl.knowledge.knowledgebase.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tsl.expression.form.sentence.NotSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

public class RelationSentencePanel extends JPanel implements ItemListener,
		ActionListener {

	private KnowledgeBasePanel kbp = null;
	private KnowledgeBase kb = null;
	private Sentence sentence = null;
	private boolean isNegated = false;
	private JCheckBox negationButton = null;
	private JComboBox relationComboBox = null;
	private RelationConstant selectedRelation = null;
	private Vector<Term> selectedArguments = null;
	private Vector<ArgumentComboBox> argumentComboBoxes = null;
	private Vector<Variable> argumentVariables = null;
	private JTextField sentenceText = null;
	private JTextField sentenceNameText = null;
	private int relationArity = 4;
	private static Vector<String> vnames = VUtils.arrayToVector(new String[] {
			"?x", "?y", "?z", "?w" });

	public RelationSentencePanel(KnowledgeBasePanel kbp) {
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		this.kbp = kbp;
		this.kb = kbp.kb;
		negationButton = new JCheckBox("Negated");
		negationButton.addItemListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		this.add(negationButton, c);

		Vector<RelationConstant> rcs = kb.getAllRelations();
		this.selectedRelation = rcs.firstElement();
		relationComboBox = new RelationComboBox(this, rcs);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		this.add(new JLabel("Relation:"), c);
		c.gridx = 1;
		c.gridy = 1;
		this.add(relationComboBox, c);
		relationComboBox.addItemListener(this);

		int index = 0;
		int xindex = 0;
		c.gridx = xindex++;
		c.gridy = 2;
		this.add(new JLabel("Arguments:"), c);
		for (String vname : vnames) {
			Variable var = new Variable(vname);
			this.argumentVariables = VUtils.add(this.argumentVariables, var);
			Vector<Variable> vars = new Vector(0);
			vars.add(var);
			ArgumentComboBox acb = new ArgumentComboBox(this, vars, index++);
			acb.setEditable(true);
			this.argumentComboBoxes = VUtils.add(this.argumentComboBoxes, acb);
			c.gridx = xindex++;
			this.add(acb, c);
			acb.addItemListener(this);
		}
		this.selectedArguments = new Vector(this.argumentVariables);
		this.sentenceText = new JTextField(50);
		c.gridx = 0;
		c.gridy = 3;
		this.add(new JLabel("Sentence:"), c);
		c.gridx = 1;
		c.gridy = 3;
		this.add(this.sentenceText, c);
		this.sentenceNameText = new JTextField(10);
		c.gridx = 0;
		c.gridy = 4;
		this.add(new JLabel("SentenceName:"), c);
		c.gridx = 1;
		this.add(this.sentenceNameText, c);

		c.gridx = 0;
		c.gridy = 5;
		JButton sb = new JButton("Store");
		sb.addActionListener(this);
		this.add(sb, c);

		this.getSentence();
	}

	private Sentence getSentence() {
		if (this.selectedRelation != null) {
			Vector<Term> args = new Vector(this.selectedArguments.subList(0,
					this.selectedRelation.getArity()));
			args.insertElementAt(this.selectedRelation, 0);
			RelationSentence rs = RelationSentence.createRelationSentence(
					(KnowledgeBase) null, args);
			if (this.isNegated) {
				this.sentence = new NotSentence(rs);
			} else {
				this.sentence = rs;
			}
		}
		if (this.sentence != null) {
			this.sentenceText.setText(this.sentence.toString());
			String sname = this.sentenceNameText.getText();
			if (sname != null && sname.length() > 0
					&& Character.isLetter(sname.charAt(0))) {
				this.sentence.setName(sname);
			} else {
				this.sentence.setName(this.sentence.toString());
			}
		}
		this.adjustArgumentComboBoxVisibility();
		this.sentenceNameText.setText(this.sentence.getName());
		return this.sentence;
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if (negationButton.equals(source)) {
			this.isNegated = !this.isNegated;
			getSentence();
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (e.getSource().equals(this.sentenceNameText)) {
			String str = this.sentenceNameText.getText();

		} else if ("Store".equals(cmd)) {
			getSentence();
			this.kbp.addSentence(this.sentence);
		}
	}

	private void adjustArgumentComboBoxVisibility() {
		for (int i = 0; i < 4; i++) {
			boolean isvisible = (i < this.relationArity ? true : false);
			ArgumentComboBox acb = this.argumentComboBoxes.elementAt(i);
			acb.setVisible(isvisible);
		}
	}

	class RelationComboBox extends JComboBox {
		RelationSentencePanel rspanel = null;

		public RelationComboBox(RelationSentencePanel rspanel,
				Vector<RelationConstant> rcs) {
			super(rcs);
			this.rspanel = rspanel;
			setSelectedItem(0);
			this.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					comboBoxActionPerformed(e);
				}
			});
		}

		public String toString() {
			Object o = getSelectedItem();
			return o.toString();
		}

		protected void comboBoxActionPerformed(ActionEvent e) {
			if ("comboBoxChanged".equals(e.getActionCommand())) {
				this.rspanel.selectedRelation = (RelationConstant) this
						.getSelectedItem();
				this.rspanel.relationArity = this.rspanel.selectedRelation
						.getArity();
				this.rspanel.updateArgumentLists();
				this.rspanel.getSentence();
			}
		}
	}

	private void updateArgumentLists() {
//		if (this.selectedRelation != null) {
//			for (int i = 0; i < this.selectedRelation.getArity(); i++) {
//				Vector<Term> v = new Vector(0);
//				Variable var = this.argumentVariables.elementAt(i);
//				v.add(var);
//				Vector<Term> args = this.kb.getRelationSentenceArguments(
//						this.selectedRelation.getName(), i);
//				v = VUtils.append(v, args);
//				// if (v.size() > 20) {
//				// v = new Vector(v.subList(0, 20));
//				// }
//				ArgumentComboBox acb = this.argumentComboBoxes.elementAt(i);
//				acb.removeAllItems();
//				for (Term t : v) {
//					acb.addItem(t);
//				}
//			}
//		}
	}

	class ArgumentComboBox extends JComboBox {
		RelationSentencePanel rspanel = null;
		private int index = -1;

		public ArgumentComboBox(RelationSentencePanel rspanel, Vector args,
				int index) {
			super(args);
			this.rspanel = rspanel;
			this.index = index;
			setSelectedItem(0);
			this.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					comboBoxActionPerformed(e);
				}
			});
		}

		public String toString() {
			Object o = getSelectedItem();
			return o.toString();
		}

		protected void comboBoxActionPerformed(ActionEvent e) {
			if ("comboBoxChanged".equals(e.getActionCommand())) {
				Object o = this.getSelectedItem();
				Term term = null;
				if (o instanceof String) {
					String str = (String) o;
					term = new ObjectConstant(str);
				} else if (o instanceof Term) {
					term = (Term) o;
				}
				if (term != null) {
					this.rspanel.selectedArguments.setElementAt(term,
							this.index);
					this.rspanel.getSentence();
				}
			}
		}
	}

}
