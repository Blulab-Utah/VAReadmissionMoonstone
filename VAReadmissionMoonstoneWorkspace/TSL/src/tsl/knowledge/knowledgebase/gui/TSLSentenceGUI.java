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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import tsl.expression.form.sentence.AndSentence;
import tsl.expression.form.sentence.ComplexSentence;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.form.sentence.NotSentence;
import tsl.expression.form.sentence.OrSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.form.sentence.rule.RuleSentence;
import tsl.expression.term.Term;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.variable.Variable;
import tsl.inference.backwardchaining.BackwardChainingInferenceEngine;
import tsl.inference.backwardchaining.Query;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.VUtils;

public class TSLSentenceGUI extends JPanel implements TreeSelectionListener,
		ItemListener, ActionListener, MouseMotionListener {
	KnowledgeBase workingKB = null;
	KnowledgeBase relationKB = null;
	TSLJTree tree = null;
	TSLJTreeDefaultTreeModel model = null;
	TSLJTreeMutableTreeNode rootNode = null;
	Sentence rootSentence = null;
	Sentence deletedSentence = null;
	TSLJTreeMutableTreeNode selectedNode = null;
	JTextPane sentenceText = null;
	TreePath lastTreePath = null;
	RelationConstant selectedRelation = null;
	int relationArity = 0;
	Vector<Variable> argumentVariables = null;
	Vector<ArgumentComboBox> argumentComboBoxes = null;
	Hashtable<RelationConstant, Vector<Vector<Term>>> relationArgumentHash = new Hashtable();
	Vector selectedArguments = null;
	// Vector<Vector> argumentVectors = null;
	JTextPane answerPane = null;
	RelationComboBox relationComboBox = null;
	private static boolean isMouseDown = false;
	private static Vector<String> vnames = VUtils.arrayToVector(new String[] {
			"?x", "?y", "?z", "?w" });
	private static int sentenceIDIndex = -1;

	private static String[][] menuInfo = {
			{ null, "createSentence", "Create Sentence" },
			{ null, "sentenceOperations", "Sentence Operations" },
			{ null, "query", "Do Query" }, };

	private static Object[][] menuItemInfo = {
			{ "createSentence", "addFromText", "Add Sentence from Text",
					new Integer(KeyEvent.VK_T),
					new Integer(InputEvent.CTRL_DOWN_MASK) },
			{ "createSentence", "addFromArguments",
					"Add Sentence from Arguments", new Integer(KeyEvent.VK_G),
					new Integer(InputEvent.CTRL_DOWN_MASK) },
			{ "createSentence", "addAndSentence", "Add \'And\' Sentence",
					new Integer(KeyEvent.VK_A),
					new Integer(InputEvent.CTRL_DOWN_MASK) },
			{ "createSentence", "addOrSentence", "Add \'Or\' Sentence",
					new Integer(KeyEvent.VK_O),
					new Integer(InputEvent.CTRL_DOWN_MASK) },
			{ "createSentence", "addNotSentence", "Add \'Not\' Sentence",
					new Integer(KeyEvent.VK_N),
					new Integer(InputEvent.CTRL_DOWN_MASK) },
			{ "createSentence", "addImplicationSentence",
					"Add \'Implication\' Sentence", new Integer(KeyEvent.VK_I),
					new Integer(InputEvent.CTRL_DOWN_MASK) },
			{ "createSentence", "addRule", "Add Rule",
					new Integer(KeyEvent.VK_R),
					new Integer(InputEvent.CTRL_DOWN_MASK) },
			{ "sentenceOperations", "deleteSentence", "Delete Sentence",
					new Integer(KeyEvent.VK_X),
					new Integer(InputEvent.ALT_DOWN_MASK) },
			{ "sentenceOperations", "replaceSentence", "Replace Sentence",
					new Integer(KeyEvent.VK_V),
					new Integer(InputEvent.ALT_DOWN_MASK) },
			{ "sentenceOperations", "extractRelation",
					"Extract Relation from Text", new Integer(KeyEvent.VK_R),
					new Integer(InputEvent.ALT_DOWN_MASK) },
			{ "sentenceOperations", "validateSentence", "Validate Sentence",
					new Integer(KeyEvent.VK_V),
					new Integer(InputEvent.META_DOWN_MASK) },
			{ "sentenceOperations", "extractRelation",
					"Extract Relation from Text", new Integer(KeyEvent.VK_R),
					new Integer(InputEvent.ALT_DOWN_MASK) },
			{ "sentenceOperations", "nameRule", "Name Rule",
					new Integer(KeyEvent.VK_R),
					new Integer(InputEvent.META_DOWN_MASK) },
			{ "query", "doQuery", "Do Query", new Integer(KeyEvent.VK_Q),
					new Integer(InputEvent.CTRL_DOWN_MASK) }, };

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					TSLSentenceGUI tg = new TSLSentenceGUI(null);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});
	}

	public TSLSentenceGUI(KnowledgeBase relationKB) {
		super(new GridBagLayout());

		Dimension tsldim = new Dimension(1000, 600);
		this.setPreferredSize(tsldim);
		this.setMinimumSize(tsldim);

		this.workingKB = new KnowledgeBase();
		this.relationKB = (relationKB != null ? relationKB : this.workingKB);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		JPanel panel = new JPanel();

		panel = new JPanel();
		Vector<RelationConstant> rcs = this.relationKB.getAllRelations();
		if (rcs != null) {
			Collections.sort(rcs, new Term.NameSorter());
			this.selectedRelation = rcs.firstElement();
			this.relationComboBox = new RelationComboBox(this, rcs);
			panel.add(new JLabel("Relation:"));
			panel.add(relationComboBox);
			relationComboBox.addItemListener(this);
		}

		int index = 0;
		panel.add(new JLabel("Arguments:"));
		for (String vname : vnames) {
			Variable var = new Variable(vname);
			this.argumentVariables = VUtils.add(this.argumentVariables, var);
			Vector<Variable> vars = VUtils.listify(var);
			ArgumentComboBox acb = new ArgumentComboBox(this, vars, index++);
			acb.setEditable(true);
			this.argumentComboBoxes = VUtils.add(this.argumentComboBoxes, acb);
			panel.add(acb);
			acb.addItemListener(this);
		}
		this.selectedArguments = new Vector(this.argumentVariables);
		this.add(panel, c);
		c.gridy++;

		panel = new JPanel();
		JLabel label = new JLabel("Sentence:");
		Dimension d = new Dimension(600, 200);
		this.sentenceText = new JTextPane();
		this.sentenceText.setMinimumSize(d);
		this.sentenceText.setPreferredSize(d);
		panel.add(label);
		panel.add(this.sentenceText);
		this.add(panel, c);

		c.gridy++;
		rootNode = new TSLJTreeMutableTreeNode(this, null);
		model = new TSLJTreeDefaultTreeModel(rootNode);
		tree = new TSLJTree(model);
		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(this);
		tree.addMouseMotionListener(this);
		Dimension spaneldim = new Dimension(1200, 300);
		JScrollPane jsp = new JScrollPane(tree,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setMinimumSize(spaneldim);
		jsp.setPreferredSize(spaneldim);
		add(jsp, c);

		c.gridy++;
		// panel = new JPanel();
		// Vector<RelationConstant> rcs = this.relationKB.getAllRelations();
		// if (rcs != null) {
		// Collections.sort(rcs, new Term.NameSorter());
		// this.selectedRelation = rcs.firstElement();
		// this.relationComboBox = new RelationComboBox(this, rcs);
		// panel.add(new JLabel("Relation:"));
		// panel.add(relationComboBox);
		// relationComboBox.addItemListener(this);
		// }
		//
		// c.gridy++;
		// int index = 0;
		// panel.add(new JLabel("Arguments:"));
		// for (String vname : vnames) {
		// Variable var = new Variable(vname);
		// this.argumentVariables = VUtils.add(this.argumentVariables, var);
		// Vector<Variable> vars = VUtils.listify(var);
		// ArgumentComboBox acb = new ArgumentComboBox(this, vars, index++);
		// acb.setEditable(true);
		// this.argumentComboBoxes = VUtils.add(this.argumentComboBoxes, acb);
		// panel.add(acb);
		// acb.addItemListener(this);
		// }
		// this.selectedArguments = new Vector(this.argumentVariables);
		// this.add(panel, c);

		initializeRelationArgumentHash(rcs);

		JFrame frame = new JFrame();
		frame.setJMenuBar(createMenuBar(menuInfo, menuItemInfo, this, this));
		frame.setContentPane(this);
		frame.pack();
		frame.setVisible(true);
	}

	public void repopulateJTree() {
		repopulateJTree(this.rootSentence);
	}

	public void repopulateJTree(Sentence sentence) {
		sentenceIDIndex = 0;
		this.workingKB.updateSentenceVariables(sentence);
		this.rootSentence = sentence;
		this.rootNode = populateJTree(null, sentence);
		this.selectedNode = this.rootNode;
		this.model.setRoot(this.rootNode);
		this.model.reload();
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	public TSLJTreeMutableTreeNode populateJTree(TSLJTreeMutableTreeNode node,
			Sentence sentence) {
		if (sentence != null) {
			sentence.setKnowledgeBase(this.relationKB);
			String sid = "S" + sentenceIDIndex++;
			sentence.setStringID(sid);
		}
		TSLJTreeMutableTreeNode cnode = new TSLJTreeMutableTreeNode(this,
				sentence);
		if (node != null) {
			model.insertNodeInto(cnode, node, node.getChildCount());
			sentence.setContainedBy(node.sentence);
		}
		if (sentence instanceof AndSentence) {
			AndSentence asent = (AndSentence) sentence;
			if (asent.getSentences() != null) {
				for (Sentence csent : asent.getSentences()) {
					populateJTree(cnode, csent);
				}
			}
		} else if (sentence instanceof OrSentence) {
			OrSentence osent = (OrSentence) sentence;
			if (osent.getSentences() != null) {
				for (Sentence csent : osent.getSentences()) {
					populateJTree(cnode, csent);
				}
			}
		} else if (sentence instanceof NotSentence) {
			NotSentence nsent = (NotSentence) sentence;
			if (nsent.getSentence() != null) {
				populateJTree(cnode, nsent.getSentence());
			}
		} else if (sentence instanceof ImplicationSentence) {
			ImplicationSentence isent = (ImplicationSentence) sentence;
			if (isent.getAntecedent() != null) {
				populateJTree(cnode, isent.getAntecedent());
			}
			if (isent.getConsequent() != null) {
				populateJTree(cnode, isent.getConsequent());
			}
		}
		return cnode;
	}

	public String getRootSentenceString() {
		return getSentenceString(this.rootNode, 0);
	}

	public String getSentenceString(TSLJTreeMutableTreeNode node, int depth) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < depth; i++) {
			sb.append("  ");
		}
		if (node.sentence != null) {
			if (node.sentence instanceof ComplexSentence) {
				ComplexSentence csent = (ComplexSentence) node.sentence;
				sb.append(csent instanceof AndSentence ? "(and \n" : "(or \n");
				for (int i = 0; i < node.getChildCount(); i++) {
					TSLJTreeMutableTreeNode cnode = (TSLJTreeMutableTreeNode) node
							.getChildAt(i);
					String cstr = getSentenceString(cnode, depth + 1);
					sb.append(cstr);
				}
			} else {
				sb.append(node.sentence.toString());
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void valueChanged(TreeSelectionEvent e) {
		TSLJTreeMutableTreeNode node = (TSLJTreeMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (node != null) {
			this.selectedNode = node;
		}
	}

	public void itemStateChanged(ItemEvent e) {
		// Object source = e.getItemSelectable();
		// if (this.addSentenceFromTextButton.equals(source)) {
		// String sstr = this.sentenceText.getText();
		// TSLJTreeMutableTreeNode node = (this.selectedNode != null ?
		// this.selectedNode
		// : this.rootNode);
		// addSentence(node, extractSentenceFromText(sstr));
		// }
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		TSLJTreeMutableTreeNode node = (this.selectedNode != null ? this.selectedNode
				: this.rootNode);
		if ("addFromText".equals(cmd)) {
			String sstr = this.sentenceText.getText();
			addSentence(node, extractSentenceFromText(sstr));
		} else if ("addFromArguments".equals(cmd)) {
			addSentence(node, extractSentenceFromArguments());
		} else if ("addAndSentence".equals(cmd)) {
			addSentence(node, extractSentenceFromText("(and )"));
		} else if ("addOrSentence".equals(cmd)) {
			addSentence(node, extractSentenceFromText("(or )"));
		} else if ("addNotSentence".equals(cmd)) {
			addSentence(node, extractSentenceFromText("(not )"));
		} else if ("addImplicationSentence".equals(cmd)) {
			addSentence(node, extractSentenceFromText("(-> )"));
		} else if ("addRule".equals(cmd)) {
			addSentence(node, extractSentenceFromText("(defrule )"));
		} else if ("deleteSentence".equals(cmd)) {
			if (this.selectedNode != null && this.selectedNode.sentence != null) {
				deleteSentence(this.selectedNode.sentence);
			}
		} else if ("replaceSentence".equals(cmd)) {
			String sstr = this.sentenceText.getText();
			replaceSentence(node, extractSentenceFromText(sstr));
		} else if ("extractRelation".equals(cmd)) {
			extractRelationFromText();
		} else if ("doQuery".equals(cmd)) {
			doQuery();
		} else if ("validateSentence".equals(cmd)) {
			validateRootSentence();
		} else if ("nameRule".equals(cmd)) {
			nameRule();
		}
	}

	public void nameRule() {
		if (this.rootSentence == null
				|| !(this.rootSentence instanceof RuleSentence)) {
			JOptionPane.showMessageDialog(new JFrame(),
					"Root sentence must be of type RuleSentence");
			return;
		}
		String str = JOptionPane.showInputDialog(new JFrame(), "Rule Name:");
		if (str != null) {
			RuleSentence rs = (RuleSentence) this.rootSentence;
			rs.setName(str);
			repopulateJTree();
		}
	}

	public boolean isSentenceValid(Sentence sentence) {
		return sentence != null && sentence.validate() == null;
	}

	public void validateRootSentence() {
		String msg = null;
		if (this.rootSentence != null) {
			msg = this.rootSentence.validate();
		} else {
			msg = "Sentence not defined.";
		}
		if (msg == null) {
			msg = "No Errors.";
		}
		JOptionPane.showMessageDialog(new JFrame(), msg);
	}

	public Vector doQuery() {
		Vector<Vector> results = null;
		try {
			String rstr = "No Results";
			if (this.rootSentence instanceof Sentence) {
				results = Query.doQuery(this.relationKB, this.rootSentence,
						null, null, true);
				if (results != null && this.answerPane != null) {
					StringBuffer sb = new StringBuffer();
					for (Vector result : results) {
						sb.append(result.toString());
						sb.append("\n");
					}
					rstr = sb.toString();
					if (rstr != null) {
						if (rstr.length() > 100) {
							rstr = rstr.substring(0, 100);
							this.answerPane.setText(rstr);
						}
					}
				}
			}
		} catch (Exception e) {
			String msg = e.getMessage();
			JOptionPane.showMessageDialog(new JFrame(), msg);
			e.printStackTrace();
		}
		return results;
	}

	public void extractRelationFromText() {
		try {
			String text = this.sentenceText.getText();
			if (text != null && text.charAt(0) == '(') {
				RelationSentence rs = (RelationSentence) Sentence
						.createSentence(text);
				this.updateRelationArgumentHash(rs);
				RelationConstant rc = rs.getRelation();
				this.selectedRelation = rc;
				this.updateRelationList();
				this.selectRelation();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Sentence extractSentenceFromArguments() {
		if (this.selectedRelation != null && this.selectedArguments != null) {
			Vector<Term> args = new Vector(this.selectedArguments.subList(0,
					this.selectedRelation.getArity()));
			args.insertElementAt(this.selectedRelation, 0);
			RelationSentence rs = RelationSentence.createRelationSentence(
					(KnowledgeBase) null, args);
			rs.setKnowledgeBase(this.relationKB);
			return rs;
		}
		return null;
	}

	public Sentence extractSentenceFromText(String sstr) {
		Sentence sentence = null;
		try {
			if (sstr != null && sstr.length() > 1) {
				TLisp tlisp = TLisp.getTLisp();
				if (sstr.charAt(0) != '\'') {
					sstr = "'" + sstr;
				}
				Sexp sexp = (Sexp) tlisp.evalString(sstr);
				Vector v = TLUtils.convertSexpToJVector(sexp);
				sentence = Sentence.createSentence(v);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sentence;
	}

	public void addSentence(TSLJTreeMutableTreeNode node, Sentence sentence) {
		if (sentence != null) {
			if (this.selectedNode != null) {
				Sentence ssent = this.selectedNode.sentence;
				if (ssent instanceof AndSentence) {
					((AndSentence) ssent).addSentence(sentence);
					repopulateJTree();
				} else if (ssent instanceof OrSentence) {
					((OrSentence) ssent).addSentence(sentence);
					repopulateJTree();
				} else if (ssent instanceof NotSentence) {
					((NotSentence) ssent).setSentence(sentence);
					repopulateJTree();
				} else if (ssent instanceof RuleSentence) {
					RuleSentence rsent = (RuleSentence) ssent;
					if (rsent.getAntecedent() == null) {
						rsent.setAntecedent(sentence);
						repopulateJTree();
					} else if (rsent.getConsequent() == null
							&& sentence instanceof RelationSentence) {
						RelationSentence rs = (RelationSentence) sentence;
						rsent.setConsequent(rs);
						repopulateJTree();
					}
				} else if (ssent instanceof ImplicationSentence) {
					ImplicationSentence isent = (ImplicationSentence) ssent;
					if (isent.getAntecedent() == null) {
						isent.setAntecedent(sentence);
						repopulateJTree();
					} else if (isent.getConsequent() == null
							&& sentence instanceof RelationSentence) {
						RelationSentence rs = (RelationSentence) sentence;
						isent.setConsequent(rs);
						repopulateJTree();
					}
				} else if (this.selectedNode == this.rootNode) {
					repopulateJTree(sentence);
				}
			}
		}
	}

	public void replaceSentence(TSLJTreeMutableTreeNode node, Sentence sentence) {
		if (sentence != null) {
			if (this.selectedNode != null) {
				Sentence ssent = this.selectedNode.sentence;
				Sentence cbs = (Sentence) ssent.getContainedBy();
				int index = -1;
				Vector<Sentence> sentences = null;
				if (cbs instanceof AndSentence) {
					sentences = ((AndSentence) cbs).getSentences();
					index = sentences.indexOf(ssent);
					sentences.remove(index);
					sentences.insertElementAt(sentence, index);
					repopulateJTree();
				} else if (cbs instanceof OrSentence) {
					sentences = ((OrSentence) cbs).getSentences();
					index = sentences.indexOf(ssent);
					sentences.remove(index);
					sentences.insertElementAt(sentence, index);
					repopulateJTree();
				} else if (cbs instanceof NotSentence) {
					NotSentence nsent = (NotSentence) cbs;
					nsent.setSentence(sentence);
					repopulateJTree();
				} else if (this.selectedNode == this.rootNode) {
					repopulateJTree(sentence);
				}
			}
		}
	}

	public void deleteSentence(Sentence sentence) {
		Sentence cbs = (Sentence) sentence.getContainedBy();
		this.deletedSentence = sentence;
		if (cbs instanceof AndSentence) {
			AndSentence as = (AndSentence) cbs;
			if (as.getSentences().size() > 1) {
				as.getSentences().remove(sentence);
			} else {
				deleteSentence(as);
			}
		} else if (cbs instanceof OrSentence) {
			OrSentence os = (OrSentence) cbs;
			if (os.getSentences().size() > 1) {
				os.getSentences().remove(sentence);
			} else {
				deleteSentence(os);
			}
		} else if (cbs instanceof NotSentence) {
			deleteSentence(cbs);
		} else if (cbs instanceof ImplicationSentence) {
			deleteSentence(cbs);
		} else if (cbs == null) {
			this.rootSentence = null;
		}
		repopulateJTree();
	}

	private Object lastSelectedItem = null;
	private static boolean DoingUserSelection = false;

	void processValueSelection(Object o) throws Exception {
		DoingUserSelection = false;
		if (!DoingUserSelection && o != null
				&& (lastSelectedItem == null || !lastSelectedItem.equals(o))) {
			lastSelectedItem = o;
			DoingUserSelection = true;
			DoingUserSelection = false;
		}
	}

	class TSLJTreeMutableTreeNode extends DefaultMutableTreeNode {
		TSLSentenceGUI panel = null;
		Sentence sentence = null;

		TSLJTreeMutableTreeNode(TSLSentenceGUI panel, Sentence sentence) {
			super(sentence);
			this.panel = panel;
			this.sentence = sentence;
		}

		public String toString() {
			// String str =
			// "**************************************************";
			String str = "*";
			if (this.sentence != null) {
				str = this.sentence.getStringID() + " "
						+ this.sentence.toShortString();
			}
			return str;
		}
	}

	class TSLJTreeDefaultTreeModel extends DefaultTreeModel {
		TSLJTreeDefaultTreeModel(TSLJTreeMutableTreeNode node) {
			super(node);
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			try {
				TSLJTreeMutableTreeNode node = (TSLJTreeMutableTreeNode) path
						.getLastPathComponent();

				// 3/13/2014: TRYING TO ADD THE SENTENCE FROM THE NODE ITSELF
				String str = (String) newValue;
				Sentence s = Sentence.createSentence(str);
				TSLJTreeMutableTreeNode parent = (TSLJTreeMutableTreeNode) node
						.getParent();
				if (parent != null && parent.sentence != null
						&& parent.sentence.isComplex()) {
					ComplexSentence cs = (ComplexSentence) parent.sentence;
					Vector<Sentence> csents = cs.getSentences();
					VUtils.replaceWith(csents, node.sentence, s);
				}
				node.sentence = s;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class TSLJTree extends JTree {
		public static final long serialVersionUID = 0;

		TSLJTree(DefaultTreeModel model) {
			super(model);
		}

	}

	class TSLJTreeModelListener implements TreeModelListener {

		public void treeNodesChanged(TreeModelEvent e) {
			TSLJTreeMutableTreeNode node = (TSLJTreeMutableTreeNode) (e
					.getTreePath().getLastPathComponent());
			try {
				int index = e.getChildIndices()[0];
				node = (TSLJTreeMutableTreeNode) (node.getChildAt(index));
			} catch (NullPointerException exc) {
			}
		}

		public void treeNodesInserted(TreeModelEvent e) {
		}

		public void treeNodesRemoved(TreeModelEvent e) {
		}

		public void treeStructureChanged(TreeModelEvent e) {
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
				isMouseDown = true;
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path != null && path != this.lastTreePath) {
					this.lastTreePath = path;
					if (tree.isPathSelected(path)) {
						tree.removeSelectionPath(path);
					} else {
						Object lastElement = path.getLastPathComponent();
						this.selectedNode = (TSLJTreeMutableTreeNode) lastElement;
						tree.addSelectionPath(path);
						processValueSelection(this.selectedNode.getUserObject());
					}
				}
				isMouseDown = false;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void mouseDragged(MouseEvent e) {
	}

	class RelationComboBox extends JComboBox {
		TSLSentenceGUI panel = null;

		public RelationComboBox(TSLSentenceGUI panel,
				Vector<RelationConstant> rcs) {
			super(rcs);
			AutoCompleteDecorator.decorate(this);
			this.setEditable(true);
			this.panel = panel;
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
				if (this.getItemCount() > 0) {
					this.panel.selectedRelation = (RelationConstant) this
							.getSelectedItem();
					this.panel.relationArity = this.panel.selectedRelation
							.getArity();
					this.panel.selectRelation();
				}
			}
		}
	}

	private void adjustArgumentComboBoxVisibility() {
		for (int i = 0; i < 4; i++) {
			boolean isvisible = (i < this.relationArity ? true : false);
			ArgumentComboBox acb = this.argumentComboBoxes.elementAt(i);
			acb.setVisible(isvisible);
		}
	}

	private void updateSentenceText(String str) {
		if (str == null) {
			str = "";
		}
		this.sentenceText.setText(str);
	}

	private void updateSentenceTextFromArguments() {
		Sentence s = extractSentenceFromArguments();
		if (s != null) {
			String sstr = s.toLisp();
			this.updateSentenceText(sstr);
		}
	}

	private void updateArgumentLists() {
		if (this.selectedRelation != null) {
			Vector<Vector<Term>> argList = this.relationArgumentHash
					.get(this.selectedRelation);
			if (argList != null) {
				for (int i = 0; i < this.selectedRelation.getArity(); i++) {
					Vector<Term> args = argList.elementAt(i);
					ArgumentComboBox acb = this.argumentComboBoxes.elementAt(i);
					acb.removeAllItems();
					for (Term t : args) {
						acb.addItem(t);
					}
				}
			}
		}
	}

	private void updateRelationList() {
		this.relationComboBox.removeAllItems();
		for (Enumeration<RelationConstant> e = this.relationArgumentHash.keys(); e
				.hasMoreElements();) {
			RelationConstant rc = e.nextElement();
			this.relationComboBox.addItem(rc);
		}
	}

	class ArgumentComboBox extends JComboBox {
		TSLSentenceGUI panel = null;
		private int index = -1;

		public ArgumentComboBox(TSLSentenceGUI panel, Vector args, int index) {
			super(args);
			this.panel = panel;
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
					this.panel.selectedArguments.setElementAt(term, this.index);
					this.panel.updateSentenceTextFromArguments();
				}
			}
		}
	}

	public Sentence getTSLSentence() {
		if (this.isSentenceValid(this.rootSentence)) {
			return rootSentence;
		}
		return null;
	}

	public Sentence getRootSentence() {
		return rootSentence;
	}

	public void setRootSentence(Sentence rootSentence) {
		this.rootSentence = rootSentence;
	}

	public static JMenuBar createMenuBar(String[][] menuinfo,
			Object[][] menuiteminfo, ActionListener listener,
			JComponent component) {
		Hashtable menuhash = new Hashtable();
		JMenuBar menubar = new JMenuBar();

		for (int i = 0; i < menuinfo.length; i++) {
			String[] array = (String[]) menuinfo[i];
			String parentname = array[0];
			String menuname = array[1];
			String displayname = array[2];
			JMenu menu = new JMenu(displayname);
			menuhash.put(menuname, menu);
			if (parentname != null) {
				JMenu parent = (JMenu) menuhash.get(parentname);
				parent.add(menu);
			} else {
				menubar.add(menu);
			}
		}

		for (int i = 0; i < menuiteminfo.length; i++) {
			Object[] array = (Object[]) menuiteminfo[i];
			String menuname = (String) array[0];
			String actionname = (String) array[1];
			String displayname = (String) array[2];
			int key = -1;
			int modifier = -1;
			if (array.length > 3) {
				Integer k = (Integer) array[3];
				key = k.intValue();
				Integer m = (Integer) array[4];
				modifier = m.intValue();
			}
			JMenu menu = (JMenu) menuhash.get(menuname);
			JMenuItem menuitem = new JMenuItem(displayname);
			menuitem.setActionCommand(actionname);
			menuitem.addActionListener(listener);
			if (modifier > 0) {
				KeyStroke ks = KeyStroke.getKeyStroke(key, modifier);
				menuitem.setAccelerator(ks);
			}
			menu.add(menuitem);
		}
		return menubar;
	}

	public static boolean isMouseDown() {
		return isMouseDown;
	}

	private void selectRelation() {
		if (this.selectedRelation != null) {
			this.relationComboBox.setSelectedItem(this.selectedRelation);
			this.updateArgumentLists();
			this.adjustArgumentComboBoxVisibility();
			this.updateSentenceTextFromArguments();
		}
	}

	private void initializeRelationArgumentHash(Vector<RelationConstant> rcs) {
		if (rcs != null) {
			for (RelationConstant rc : rcs) {
				Vector<Vector<Term>> argList = null;
				for (int i = 0; i < rc.getArity(); i++) {
					// Vector<Term> relargs = this.relationKB
					// .getUniqueRelationSentenceArguments(rc.getName(), i);
					// if (relargs != null) {
					// Collections.sort(relargs, new Term.LabelSorter());
					// Term argvar = this.argumentVariables.elementAt(i);
					// Vector newargs = VUtils.listify(argvar);
					// newargs = VUtils.append(newargs, relargs);
					// argList = VUtils.add(argList, newargs);
					// }
				}
				if (argList != null) {
					this.relationArgumentHash.put(rc, argList);
				}
			}
		}
	}

	private void updateRelationArgumentHash(RelationSentence rs) {
		RelationConstant rc = rs.getRelation();
		Vector<Vector<Term>> argList = this.relationArgumentHash.get(rc);
		if (argList == null) {
			for (int i = 0; i < rc.getArity(); i++) {
				Term term = (Term) rs.getTerm(i);
				Variable var = this.argumentVariables.elementAt(i);
				Vector<Term> args = VUtils.listify(var);
				args = VUtils.addIfNot(args, term);
				argList = VUtils.add(argList, args);
			}
			this.relationArgumentHash.put(rc, argList);
		}
	}

}
