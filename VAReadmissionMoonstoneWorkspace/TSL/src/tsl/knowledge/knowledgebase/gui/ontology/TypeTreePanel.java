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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;

public class TypeTreePanel extends JPanel implements TreeSelectionListener,
		ActionListener, MouseMotionListener {
	private OntologyModeler ontologyModeler = null;
	private Ontology ontology = null;
	private TypeJTree tree = null;
	private TypeTreeModel model = null;
	private TypeTreeNode rootNode = null;
	private TypeConstant rootType = null;
	private TypeTreeNode selectedNode = null;
	private TreePath lastTreePath = null;
	private static boolean isMouseDown = false;

	public TypeTreePanel(OntologyModeler om) {
		this.ontologyModeler = om;
		this.ontology = om.getOntology();
		Dimension minimumSize = new Dimension(800, 600);
		rootNode = new TypeTreeNode(this, null);
		model = new TypeTreeModel(rootNode);
		tree = new TypeJTree(this);
		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(this);
		tree.addMouseMotionListener(this);
		populateJTree();
		JScrollPane jsp = new JScrollPane(tree);
		this.add(jsp);
	}

	public void actionPerformed(ActionEvent e) {

	}

	public void valueChanged(TreeSelectionEvent e) {

	}

	public void populateJTree() {
		this.rootNode = new TypeTreeNode(this, "*************");
		if (this.ontology.getAllTypeConstants() != null) {
			for (TypeConstant type : this.ontology.getAllTypeConstants()) {
				if (type.getParents() == null) {
					populateJTree(this.rootNode, type);
				}
			}
		}
		this.selectedNode = this.rootNode;
		this.model.setRoot(this.rootNode);
		this.model.reload();
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	public void populateJTree(TypeTreeNode node, TypeConstant type) {
		TypeTreeNode cnode = new TypeTreeNode(this, type);
		model.insertNodeInto(cnode, node, node.getChildCount());
		if (type.getChildren() != null) {
			for (Term ctype : type.getChildren()) {
				populateJTree(cnode, (TypeConstant) ctype);
			}
		}
	}

	class TypeTreeNode extends DefaultMutableTreeNode {
		TypeTreePanel panel = null;
		TypeConstant type = null;

		TypeTreeNode(TypeTreePanel panel, Object o) {
			super(o);
			this.panel = panel;
			if (o instanceof TypeConstant) {
				this.type = (TypeConstant) o;
			}
		}

		public String toString() {
			if (this.getUserObject() != null) {
				return this.getUserObject().toString();
			}
			return "*";
		}
	}

	class TypeTreeModel extends DefaultTreeModel {
		TypeTreeModel(TypeTreeNode node) {
			super(node);
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			TypeTreeNode pathnode = (TypeTreeNode) path.getLastPathComponent();
		}
	}

	class TypeJTree extends JTree {

		TypeJTree(TypeTreePanel panel) {
			super(panel.model);
		}

	}

	class TypeJTreeModelListener implements TreeModelListener {

		public void treeNodesChanged(TreeModelEvent e) {
			TypeTreeNode node = (TypeTreeNode) (e.getTreePath()
					.getLastPathComponent());
			try {
				int index = e.getChildIndices()[0];
				node = (TypeTreeNode) (node.getChildAt(index));
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
						this.selectedNode = (TypeTreeNode) lastElement;
						tree.addSelectionPath(path);
						// processValueSelection(this.selectedNode.getUserObject());
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

	class TypeComboBox extends JComboBox {
		TypeTreePanel panel = null;

		public TypeComboBox(TypeTreePanel panel, Vector<RelationConstant> rcs) {
			super(rcs);
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

			}
		}
	}

}
