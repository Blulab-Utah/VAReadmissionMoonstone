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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import tsl.expression.form.sentence.Sentence;
import tsl.inference.backwardchaining.BackwardChainingInferenceEngine;
import tsl.inference.backwardchaining.Query;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.HUtils;

public class KnowledgeBasePanel extends JPanel implements ActionListener {
	protected KnowledgeBase kb = null;
	protected JButton relationSentenceButton = null;
	protected JButton logicSentenceButton = null;
	protected Hashtable<String, Sentence> sentenceHash = new Hashtable();
	protected Sentence currentSentence = null;
	protected JTextPane queryResultsPane = null;
	protected LogicalSentencePanel logicalSentencePanel = null;
	protected RelationSentencePanel relationSentencePanel = null;
	protected JFrame frame = null;

	public KnowledgeBasePanel(KnowledgeBase kb, boolean isVisible) {
		this.kb = kb;
		this.relationSentenceButton = new JButton("RelationSentence");
		this.add(this.relationSentenceButton);
		this.relationSentenceButton.addActionListener(this);
		this.logicSentenceButton = new JButton("LogicSentence");
		this.add(this.logicSentenceButton);
		this.logicSentenceButton.addActionListener(this);
		JButton qbutton = new JButton("Query");
		this.add(qbutton);
		qbutton.addActionListener(this);
		this.queryResultsPane = new JTextPane();
		this.add(this.queryResultsPane);
		this.frame = new JFrame("Panel");
		this.frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setOpaque(true);
		this.frame.setContentPane(this);
		this.frame.pack();
		this.frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("RelationSentence".equals(cmd)) {
			this.relationSentencePanel = new RelationSentencePanel(this);
			displayPanel(this.relationSentencePanel, "RelationSentence");
		} else if ("LogicSentence".equals(cmd)) {
			this.logicalSentencePanel = new LogicalSentencePanel(this);
			displayPanel(this.logicalSentencePanel, "LogicalSentence");
		} else if ("Query".equals(cmd)) {
			doQuery();
		}
	}

	public void doQuery() {
		if (this.currentSentence != null) {
			this.kb.initializeForm(this.currentSentence);
			Vector results = Query
					.doQuery(kb, this.currentSentence, null, null, true);
			String str = Query.extractResultsString(this.currentSentence,
					results);
			this.queryResultsPane.setText(str);
		}
	}

	public static void displayPanel(JPanel panel, String name) {
		JFrame frame = new JFrame("Panel");
		frame.setTitle(name);
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		panel.setOpaque(true);
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);
	}

	public void addSentence(Sentence s) {
		if (s != null) {
			this.currentSentence = s;
			if (s.getName() != null) {
				this.sentenceHash.put(s.getName(), s);
				if (this.logicalSentencePanel != null) {
					this.logicalSentencePanel.addSentence(s);
				}
			}
		}
	}

	public void deleteSentence(Sentence s) {
		if (s != null) {
			this.sentenceHash.remove(s.getName());
			this.currentSentence = null;
			if (this.logicalSentencePanel != null) {
				this.logicalSentencePanel.deleteSentence(s);
			}
		}
	}

	public Vector<Sentence> getNamedSentences() {
		return HUtils.getElements(this.sentenceHash);
	}

}
