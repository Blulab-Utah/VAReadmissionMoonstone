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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import workbench.api.Analysis;
import workbench.api.WorkbenchAPIObject;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Type;

public class TypePanel extends JPanel implements TreeSelectionListener,
		MouseMotionListener {

	private Analysis analysis = null;
	private WorkbenchAPIObject rootTypeObject = null;
	private DefaultTreeModel treeModel = null;
	private TypeObjectJTree tree = null;
	private TypeObjectMutableTreeNode rootNode = null;
	private TypeObjectMutableTreeNode selectedNode = null;
	private TreePath lastTreePath = null;
	private static boolean userInteraction = false;

	public TypePanel(Analysis analysis) {
		this.analysis = analysis;
		rootTypeObject = analysis.getTypeSystem().getRootType();
		createJTree();
	}

	void createJTree() {
		rootNode = new TypeObjectMutableTreeNode(rootTypeObject);
		treeModel = new TypeObjectDefaultTreeModel(rootNode);
		wrapChildNodes(rootNode);
		treeModel.addTreeModelListener(new TypeObjectTreeModelListener());
		tree = new TypeObjectJTree(treeModel);
		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(this);
		tree.addMouseMotionListener(this);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		JScrollPane jsp = new JScrollPane(tree,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		jsp.setMinimumSize(new Dimension(100, 100));
		JScrollPane scrollPane = new JScrollPane(jsp);
		this.removeAll();
		add(scrollPane);
	}

	private void wrapChildNodes(TypeObjectMutableTreeNode pnode) {
		WorkbenchAPIObject o = (WorkbenchAPIObject) pnode.getUserObject();
		if (o instanceof Type) {
			Type type = (Type) o;
			if (type.getChildren() != null) {
				for (Type child : type.getChildren()) {
					TypeObjectMutableTreeNode cnode = new TypeObjectMutableTreeNode(
							child);
					treeModel.insertNodeInto(cnode, pnode,
							pnode.getChildCount());
					wrapChildNodes(cnode);
				}
			}
			if (type.getAttributes() != null) {
				for (Attribute attr : type.getAttributes()) {
					TypeObjectMutableTreeNode cnode = new TypeObjectMutableTreeNode(
							attr);
					treeModel.insertNodeInto(cnode, pnode,
							pnode.getChildCount());
				}
			}
		}
	}

	void processValueSelection(Object o) throws Exception {
		this.analysis.setUserSelectedWorkbenchAPIObject((WorkbenchAPIObject) o);
		this.analysis.getWorkbenchGUI().fireAllDataUpdates();
	}

	public TypeObjectMutableTreeNode getSelectedNode() {
		return this.selectedNode;
	}

	public void valueChanged(TreeSelectionEvent e) {
		userInteraction = true;
		TypeObjectMutableTreeNode node = (TypeObjectMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (node != null) {
			WorkbenchAPIObject o = (WorkbenchAPIObject) node.getUserObject();
			this.analysis.setUserSelectedWorkbenchAPIObject(o);
			this.analysis.updateStatistics();
			this.analysis.getWorkbenchGUI().fireAllDataUpdates();
		}
		userInteraction = false;
	}

	public static boolean withUserInteraction() {
		return userInteraction;
	}

	private class TypeObjectMutableTreeNode extends DefaultMutableTreeNode {

		TypeObjectMutableTreeNode(Object o) {
			super(o);
		}

		public String toString() {
			return this.getUserObject().toString();
		}
	}

	private class TypeObjectDefaultTreeModel extends DefaultTreeModel {

		TypeObjectDefaultTreeModel(TypeObjectMutableTreeNode node) {
			super(node);
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			TypeObjectMutableTreeNode pathnode = (TypeObjectMutableTreeNode) path
					.getLastPathComponent();
			pathnode = pathnode;
		}
	}

	class TypeObjectJTree extends JTree {

		public static final long serialVersionUID = 0;

		TypeObjectJTree(DefaultTreeModel model) {
			super(model);
		}

		public String getToolTipText(MouseEvent e) {
			Point p = e.getPoint();
			TreePath path = tree.getClosestPathForLocation(p.x, p.y);
			TypeObjectMutableTreeNode node = (TypeObjectMutableTreeNode) path
					.getLastPathComponent();
			return "";
		}

	}

	class TypeObjectTreeModelListener implements TreeModelListener {

		public void treeNodesChanged(TreeModelEvent e) {
			TypeObjectMutableTreeNode node = (TypeObjectMutableTreeNode) (e
					.getTreePath().getLastPathComponent());
			try {
				int index = e.getChildIndices()[0];
				node = (TypeObjectMutableTreeNode) (node.getChildAt(index));
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

	public TypeObjectJTree getTree() {
		return tree;
	}

	public void setTree(TypeObjectJTree tree) {
		this.tree = tree;
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

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		try {
			if (e.isControlDown()) {
				userInteraction = true;
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path != null && path != this.lastTreePath) {
					this.lastTreePath = path;
					if (tree.isPathSelected(path)) {
						tree.removeSelectionPath(path);
					} else {
						Object lastElement = path.getLastPathComponent();
						this.selectedNode = (TypeObjectMutableTreeNode) lastElement;
						tree.addSelectionPath(path);
						processValueSelection(this.selectedNode.getUserObject());
					}
				}
				userInteraction = false;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
