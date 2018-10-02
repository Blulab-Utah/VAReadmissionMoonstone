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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import tsl.expression.form.sentence.AndSentence;
import tsl.expression.form.sentence.NotSentence;
import tsl.expression.form.sentence.OrSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

public class LogicalSentencePanel extends JPanel implements ItemListener,
		ActionListener, MouseListener {
	private KnowledgeBasePanel kbp = null;
	private KnowledgeBase kb = null;
	private Vector<Sentence> sentences = null;
	private Sentence currentSentence = null;
	private boolean isNegated = false;
	private JCheckBox negationButton = null;
	private JRadioButton andButton = null;
	private JRadioButton orButton = null;
	private JComboBox sentenceComboBox = null;
	private Vector<Sentence> allSentences = null;
	private JList sentenceList = null;
	private JTextField sentenceNameText = null;
	private JTextField sentenceText = null;
	private DefaultListModel sentenceListModel = null;
	private static boolean UpdatingComboBox = false;

	public LogicalSentencePanel(KnowledgeBasePanel kbp) {
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
		JPanel lpanel = new JPanel(new GridLayout(1, 0));
		andButton = new JRadioButton();
		andButton.setSelected(true);
		orButton = new JRadioButton();
		orButton.setSelected(false);
		lpanel.add(new JLabel("And"));
		lpanel.add(andButton);
		lpanel.add(new JLabel("Or"));
		lpanel.add(orButton);
		c.gridx = 0;
		c.gridy = 1;
		this.add(lpanel, c);

		c.gridx = 1;
		c.gridy = 2;
		sentenceComboBox = new JComboBox();
		this.add(sentenceComboBox, c);
		sentenceComboBox.addItemListener(this);
		sentenceComboBox.addActionListener(this);

		c.gridx = 0;
		c.gridy = 3;
		this.add(new JLabel("Sentences:"), c);
		this.sentenceListModel = new DefaultListModel();
		this.sentenceList = new JList(this.sentenceListModel);
		this.sentenceList.addMouseListener(this);
		this.sentenceList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		c.gridx = 1;
		this.add(this.sentenceList, c);

		c.gridx = 0;
		c.gridy = 4;
		this.sentenceNameText = new JTextField(10);
		this.add(new JLabel("SentenceName:"), c);
		c.gridx = 1;
		this.add(this.sentenceNameText, c);
		this.sentenceNameText.addActionListener(this);

		c.gridx = 0;
		c.gridy = 5;
		this.sentenceText = new JTextField(60);
		this.add(new JLabel("Sentence:"), c);
		c.gridx = 1;
		c.gridwidth = 6;
		this.add(this.sentenceText, c);
		c.gridwidth = 1;

		c.gridx = 0;
		c.gridy = 6;
		JButton dsb = new JButton("DeleteSentence");
		this.add(dsb, c);
		dsb.addActionListener(this);

		c.gridx = 1;
		JButton sb = new JButton("Store");
		sb.addActionListener(this);
		this.add(sb, c);
	}
	
	public void deleteSentence(Sentence s) {
		this.sentenceListModel.removeElement(s);
		this.sentences = VUtils.remove(this.sentences, s);
		this.allSentences = VUtils.remove(this.allSentences, s);
		updateSentenceComboBox();
	}
	
	public void addSentence(Sentence s) {
		this.allSentences = VUtils.addIfNot(this.allSentences, s);
		this.updateSentenceComboBox();
	}

	public void updateSentenceComboBox() {
		UpdatingComboBox = true;
		this.sentenceComboBox.removeAllItems();
		if (this.allSentences != null) {
			for (Sentence s : this.allSentences) {
				this.sentenceComboBox.addItem(s);
			}
		}
		UpdatingComboBox = false;
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if (source.equals(negationButton)) {
			this.isNegated = !this.isNegated;
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		System.out.println("CMD=" + cmd);
		if ("comboBoxChanged".equals(cmd) && !UpdatingComboBox) {
			Sentence s = (Sentence) this.sentenceComboBox.getSelectedItem();
			if (!(this.sentences != null && this.sentences.contains(s))) {
				this.sentences = VUtils.add(this.sentences, s);
				this.sentenceListModel.addElement(s);
			}
		} else if ("DeleteSentence".equals(cmd)) {
			Object[] v = this.sentenceList.getSelectedValues();
			if (v != null && v.length > 0) {
				for (int i = 0; i < v.length; i++) {
					Sentence s = (Sentence) v[i];
					deleteSentence(s);
				}
			}
		} else if ("Store".equals(cmd)) {
			createLogicalSentence();
			if (this.currentSentence != null) {
				this.kbp.addSentence(this.currentSentence);
			}
		}
	}

	private Sentence createLogicalSentence() {
		if (this.sentences != null && this.sentences.size() > 0) {
			boolean isOr = this.orButton.isSelected();
			boolean isNegated = this.negationButton.isSelected();
			Sentence newsent = null;
			Vector<Sentence> sentences = new Vector(this.sentences);
			if (isOr) {
				OrSentence orsent = new OrSentence();
				orsent.setSentences(sentences);
				newsent = orsent;
			} else {
				AndSentence andsent = new AndSentence();
				andsent.setSentences(sentences);
				newsent = andsent;
			}
			if (isNegated) {
				newsent = new NotSentence(newsent);
			}
			String sname = this.sentenceNameText.getText();
			if (!(sname != null && sname.length() > 1 && Character
					.isLetter(sname.charAt(0)))) {
				sname = newsent.toString();
			}
			newsent.setName(sname);
			this.currentSentence = newsent;
			this.sentenceText.setText(newsent.toString());
		}
		return this.currentSentence;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {

		}
	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}
}
