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
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import moonstone.annotation.Annotation;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.variable.Variable;
import tsl.inference.backwardchaining.BackwardChainingInferenceEngine;
import tsl.inference.backwardchaining.BackwardChainingInferenceEngineNEW;
import tsl.inference.backwardchaining.Query;
import tsl.inference.forwardchaining.ForwardChainingInferenceEngine;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class MoonstoneQueryPanel extends JPanel implements ActionListener,
		MouseListener, MouseMotionListener, KeyListener, ItemListener {

	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private KnowledgeEngine knowledgeEngine = null;
	private JCheckBox queryDebugCheckBox = null;
	private JCheckBox breakAtFirstProofCheckBox = null;
	private JTextArea queryTextField = null;
	private JTextPane queryAnswerTextPane = null;
	private JTextPane queryKBTextPane = null;
	private JFrame queryJFrame = null;
	private JButton addAllAnnotationsButton = null;
	private JButton addCorpusSentencesButton = null;
	private JButton forwardChainingExpansionButton = null;
	private JButton originalBackwardChainingQueryButton = null;
	private JButton newBackwardChainingQueryButton = null;
	private KnowledgeBase knowledgeBase = null;
	private ForwardChainingInferenceEngine forwardChainingInferenceEngine = null;

	public MoonstoneQueryPanel(MoonstoneRuleInterface mri) {
		super(new GridBagLayout());
		this.moonstoneRuleInterface = mri;
		this.knowledgeEngine = mri.getKnowledgeEngine();
		boolean debug = mri.getKnowledgeEngine().getStartupParameters()
				.isPropertyTrue("doQueryDebug");
		
		this.knowledgeBase = this.knowledgeEngine.getCurrentKnowledgeBase();
		this.forwardChainingInferenceEngine = new ForwardChainingInferenceEngine(
				debug);

		this.forwardChainingInferenceEngine.storeRules(this.knowledgeBase
				.getAllImplicationSentences());
		GridBagConstraints c = new GridBagConstraints();

		JPanel panel = new JPanel();

		addAllAnnotationsButton = new JButton("AddAllAnnotations");
		addAllAnnotationsButton.addActionListener(this);
		panel.add(addAllAnnotationsButton);

		addCorpusSentencesButton = new JButton("AddCorpusSentences");
		addCorpusSentencesButton.addActionListener(this);
		panel.add(addCorpusSentencesButton);

		forwardChainingExpansionButton = new JButton("ExpandFCIE");
		forwardChainingExpansionButton.addActionListener(this);
		panel.add(forwardChainingExpansionButton);
		originalBackwardChainingQueryButton = new JButton("QueryOriginal");
		originalBackwardChainingQueryButton.addActionListener(this);
		panel.add(originalBackwardChainingQueryButton);
		newBackwardChainingQueryButton = new JButton("QueryNew");
		newBackwardChainingQueryButton.addActionListener(this);
		panel.add(newBackwardChainingQueryButton);
		JLabel label = new JLabel("ToggleDebug:");
		panel.add(label);
		this.queryDebugCheckBox = new JCheckBox();
		this.queryDebugCheckBox.addItemListener(this);
		panel.add(this.queryDebugCheckBox);
		label = new JLabel("BreakAtFirstProof:");
		panel.add(label);
		this.breakAtFirstProofCheckBox = new JCheckBox();
		this.breakAtFirstProofCheckBox.addItemListener(this);
		panel.add(this.breakAtFirstProofCheckBox);
		c.gridx = 0;
		c.gridy = 0;
		this.add(panel, c);

		panel = new JPanel();
		label = new JLabel("Query:");
		panel.add(label);
		this.queryTextField = new JTextArea(4, 60);
		panel.add(this.queryTextField);
		c.gridx = 0;
		c.gridy = 1;
		this.add(panel, c);

		panel = new JPanel(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel("Answers:"), c);
		this.queryAnswerTextPane = new JTextPane();
		JScrollPane jsp = new JScrollPane(this.queryAnswerTextPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension d = new Dimension(400, 400);
		jsp.setMinimumSize(d);
		jsp.setPreferredSize(d);
		jsp.setVisible(true);
		c.gridx = 0;
		c.gridy = 1;
		panel.add(jsp, c);
		c.gridx = 1;
		c.gridy = 0;
		panel.add(new JLabel("KnowledgeBase:"), c);
		this.queryKBTextPane = new JTextPane();
		jsp = new JScrollPane(this.queryKBTextPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		d = new Dimension(400, 400);
		jsp.setMinimumSize(d);
		jsp.setPreferredSize(d);
		jsp.setVisible(true);
		c.gridx = 1;
		c.gridy = 1;
		panel.add(jsp, c);

		c.gridx = 0;
		c.gridy = 2;
		this.add(panel, c);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source.equals(this.addAllAnnotationsButton)) {
			populateKBUsingAnnotations(this.moonstoneRuleInterface
					.getDisplayedAnnotations());
		} else if (source.equals(this.addCorpusSentencesButton)) {
			this.populateKBUsingSentences(this.moonstoneRuleInterface
					.getCorpusTSLSentences());
		} else if (source.equals(this.forwardChainingExpansionButton)) {
			this.doForwardChaining();
		} else if (source.equals(this.originalBackwardChainingQueryButton)) {
			doQueryOriginal();
		} else if (source.equals(this.newBackwardChainingQueryButton)) {
			doQueryNew();
		}
	}

	private void doQueryOriginal() {
		this.queryAnswerTextPane.setText("No Answers");
		String qstr = this.queryTextField.getText();
		if (this.knowledgeBase != null && qstr != null && qstr.length() > 6) {
			this.knowledgeBase
					.setInferenceEngine(new BackwardChainingInferenceEngine(
							this.knowledgeBase));
			doQuery(qstr);
		}
	}

	private void doQueryNew() {
		this.queryAnswerTextPane.setText("No Answers");
		String qstr = this.queryTextField.getText();
		if (qstr != null && qstr.length() > 6) {
			this.knowledgeBase
					.setInferenceEngine(new BackwardChainingInferenceEngineNEW(
							this.knowledgeBase));
			doQuery(qstr);
		}
	}

	private void addAllAnnotations() {

	}

	private void doForwardChaining() {
		try {
			this.knowledgeEngine.pushKnowledgeBase(this.knowledgeBase);

			long start = 1;
			while (start > 0) {
				Vector<RelationSentence> rsents = this.knowledgeBase
						.getAllRelationSentences();
				start = System.currentTimeMillis();
				this.forwardChainingInferenceEngine.clearInferenceStructures();
				Vector<Sentence> newsents = this.forwardChainingInferenceEngine
						.getAllInferredSentences(rsents);
				long end = System.currentTimeMillis();
				int nscount = this.forwardChainingInferenceEngine
						.getNewSentenceCount();
				System.out.println("Milliseconds=" + (end - start));
				if (newsents != null) {
					for (Sentence ns : newsents) {
						try {
							System.out.println("\t" + ns);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				start = 0;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			this.knowledgeEngine.popKnowledgeBase();
		}
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if (source.equals(this.queryDebugCheckBox)) {
			boolean debug = (e.getStateChange() == ItemEvent.DESELECTED ? false
					: true);
			KnowledgeEngine.setDoQueryDebug(debug);
		} else if (source.equals(this.breakAtFirstProofCheckBox)) {
			boolean breakAtFirst = (e.getStateChange() == ItemEvent.DESELECTED ? false
					: true);
			KnowledgeEngine.setBreakAtFirstProof(breakAtFirst);
		}
	}

	private void doQuery(String qstr) {
		try {
			KnowledgeBase kb = this.knowledgeBase;
			if (kb != null) {
				this.knowledgeEngine.pushKnowledgeBase(kb);
				Sentence qs = Sentence.createSentence(qstr);
				kb.initializeForm(qs);
				long starttime = System.currentTimeMillis();
				Vector results = null;
				results = Query.doQuery(kb, qs, null, null, true);
				long endtime = System.currentTimeMillis();
				double milliseconds = (endtime - starttime);
				double seconds = milliseconds / 1000f;
				int pc = kb.getInferenceEngine().getProofCount();
				double lips = pc / seconds;
				System.out.println("Milliseconds=" + (int) milliseconds
						+ ",SentenceInferences=" + pc + ",LIPS=" + (int) lips);
				if (results != null) {
					StringBuffer sb = new StringBuffer();
					sb.append("TRUE\n");
					if (results.size() > 0) {
						for (Object o : results) {
							Vector rv = (Vector) o;
							String str = "";
							if (rv.size() != qs.getVariableCount()) {
								// System.out.println("INVALID RESULTS ROW:" +
								// rv);
								continue;
							}
							for (int i = 0; i < qs.getVariableCount(); i++) {
								Variable var = qs.getVariable(i);
								Object value = rv.elementAt(i);
								if (i > 0) {
									str += "  ";
								}
								str += var.getName() + "=" + value;
								if (i < qs.getVariableCount() - 1) {
									str += "\n";
								}
							}
							sb.append(str + "\n");
						}
					}
					this.queryAnswerTextPane.setText(sb.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.knowledgeEngine.popKnowledgeBase();
		}
	}

	public void populateKBUsingAnnotations(Vector<Annotation> annotations) {
		this.queryKBTextPane.setText("");
		this.knowledgeBase = this.moonstoneRuleInterface.getControl()
				.populateQueryKBUsingAnnotations(annotations);
		if (this.knowledgeBase != null) {
			Vector<RelationSentence> sentences = this.knowledgeBase
					.getAllRelationSentences();
			StringBuffer sb = new StringBuffer();
			if (sentences != null) {
				for (RelationSentence rs : sentences) {
					sb.append(rs + "\n");
				}
			}
			this.queryKBTextPane.setText(sb.toString());
		}
	}

	public void populateKBUsingSentences(
			Vector<tsl.expression.term.relation.RelationSentence> sentences) {
		this.queryKBTextPane.setText("");
		this.knowledgeBase = this.moonstoneRuleInterface.getControl()
				.populateQueryKBUsingSentences(sentences);
		if (this.knowledgeBase != null && sentences != null) {
			StringBuffer sb = new StringBuffer();
			for (RelationSentence rs : sentences) {
				sb.append(rs + "\n");
			}
			this.queryKBTextPane.setText(sb.toString());
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		try {
			if (e.isControlDown()) {
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public JFrame getFrame() {
		return queryJFrame;
	}

	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public ForwardChainingInferenceEngine getForwardChainingInferenceEngine() {
		return forwardChainingInferenceEngine;
	}

}
