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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import tsl.documentanalysis.document.Document;
import workbench.api.Analysis;
import workbench.api.AnnotatorType;
import workbench.api.OutcomeResult;
import workbench.api.annotation.Annotation;
import workbench.api.constraint.ConstraintMatch;
import workbench.api.typesystem.Type;
import workbench.arr.Colors;
import annotation.AnnotationCollection;

public class AnnotationPanel extends JPanel implements TreeSelectionListener,
		MouseMotionListener, MouseListener {
	Analysis analysis = null;
	Type annotationType = null;
	ATree tree = null;
	DefaultTreeModel treeModel = null;
	AnnotationTable table = null;
	AnnotationTableModel tableModel = null;

	AnnotationCollection annotationCollection = null;
	ATreeMutableTreeNode rootNode = null;
	ATreeMutableTreeNode selectedNode = null;
	Hashtable<Object, ATreeMutableTreeNode> userObjectNodeHash = new Hashtable();
	Vector<Annotation> allMatchedAnnotations = null;

	Hashtable<String, Annotation> attributeTypeHash = new Hashtable();
	TreePath lastTreePath = null;
	public static boolean isMouseDown = false;

	public static AnnotationPanel AnnotationJTree = null;

	public AnnotationPanel(Analysis analysis) {
		this.analysis = analysis;

		this.tableModel = new AnnotationTableModel(this);
		this.table = new AnnotationTable(this.tableModel);
		this.table.addMouseMotionListener(this);

		initializeColumns();

		add(new JScrollPane(this.table));

		// Before switch to table.
		// rootNode = new ATreeMutableTreeNode(this, "Documents");
		// treeModel = new ATreeDefaultTreeModel(rootNode);
		// tree = new ATree(treeModel);
		// tree.setEditable(true);
		// tree.getSelectionModel().setSelectionMode(
		// TreeSelectionModel.SINGLE_TREE_SELECTION);
		// tree.setShowsRootHandles(true);
		// tree.addTreeSelectionListener(this);
		// tree.addMouseMotionListener(this);

		// add(tree);

	}

	void initializeColumns() {
		for (int i = 0; i < this.table.getColumnCount(); i++) {
			TableColumn column = this.table.getColumnModel().getColumn(i);
			int width = this.tableModel.getPreferredColumnWidth(i);
			column.setPreferredWidth(width);
		}
	}

	public AnnotationTable getTable() {
		return table;
	}

	public AnnotationTableModel getTableModel() {
		return this.tableModel;
	}

	public void updateTable(String value, OutcomeResult result,
			AnnotatorType atype) throws Exception {
		ConstraintMatch cm = this.analysis.getSelectedConstraintMatch();
		this.allMatchedAnnotations = cm.getAllMatchedDocumentAnnotations(value,
				result, atype);
	}

	// 9/28/2014
	void doExternalSelection() throws Exception {
		Annotation selected = this.analysis.getSelectedAnnotation();
		if (selected != null && this.allMatchedAnnotations != null) {
			int row = this.allMatchedAnnotations.indexOf(selected);
			if (row >= 0) {
				this.table.selectedRow = row;
				this.table.selectedColumn = 0;
				this.analysis.getWorkbenchGUI().fireAllDataUpdates();
			}
		}
	}

	// //////////////////////////////
	// 9/28/2014
	private static AnnotationTable annotationTable = null;
	private static Font smallFont = new Font("Serif", Font.PLAIN, 12);
	private static boolean processingMouseEvent = false;
	private static boolean processingMouseMoved = false;
	private static int lastMouseRow = -1;
	private static int lastMouseColumn = -1;

	public class AnnotationTable extends JTable implements
			ListSelectionListener {

		AnnotationPanel annotationPanel = null;
		int selectedRow = -1;
		int selectedColumn = -1;
		Vector<Annotation> annotations = null;

		AnnotationTable(AnnotationTableModel model) {
			super(model);
			annotationTable = this;
			annotationPanel = model.annotationPanel;
			this.setShowGrid(true);
			this.setGridColor(Color.GRAY);
			this.setRowHeight(15);
			this.setFont(smallFont);
			// setToolTipText("");
		}

		public void processMouseEvent(MouseEvent e) {
			processingMouseEvent = true;
			super.processMouseEvent(e);
			processingMouseEvent = false;
		}

		public int getRowHeight(int row) {
			return 15;
		}

		public Component prepareRenderer(TableCellRenderer renderer,
				int rowIndex, int colIndex) {
			Component c = super.prepareRenderer(renderer, rowIndex, colIndex);
			Color color = getColor(rowIndex, colIndex);
			c.setBackground(color);
			return c;
		}

		public TableCellEditor getCellEditor() {
			return super.getCellEditor();
		}

		Color getColor(int row, int col) {
			if (row == annotationTable.selectedRow) {
				return Colors.darkBlueGray;
			}
			return Color.white;
		}

		// Before 9/28/2014
		// Color getColor(int row, int col) {
		// boolean selectedCell = (row == annotationTable.selectedRow && col ==
		// annotationTable.selectedColumn);
		// if (selectedCell) {
		// return Colors.darkBlueGray;
		// }
		// if (row == annotationTable.getSelectedRow()) {
		// return Colors.lightBlueGray;
		// }
		// if (col == annotationTable.selectedColumn) {
		// return Color.LIGHT_GRAY;
		// }
		// return Color.white;
		// }

		void doSelection(int row, int col) throws Exception {
			WBGUI wbgui = this.annotationPanel.analysis.getWorkbenchGUI();
			selectedRow = row;
			selectedColumn = col;
			Analysis analysis = this.annotationPanel.analysis;
			Annotation annotation = this.annotationPanel.allMatchedAnnotations
					.elementAt(row);
			analysis.setSelectedAnnotation(annotation);
			analysis.setSelectedDocument(annotation.getDocument());
			analysis.getWorkbenchGUI().setFiringAllDataUpdates(false);
			analysis.getWorkbenchGUI().fireAllDataUpdates();
		}

	}

	public class AnnotationTableModel extends AbstractTableModel {
		AnnotationPanel annotationPanel = null;

		AnnotationTableModel(AnnotationPanel apanel) {
			this.annotationPanel = apanel;
			this.fireTableRowsInserted(0, getRowCount());
		}

		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			if (this.annotationPanel.allMatchedAnnotations != null) {
				return this.annotationPanel.allMatchedAnnotations.size();
			}
			return 0;
		}

		public String getColumnName(int col) {
			Analysis analysis = this.annotationPanel.analysis;
			switch (col) {
			case 0:
				return "Document";
			case 1:
				return "Text";
			case 2:
				return "Classification";
			case 3:
				return "Spans";
			case 4:
				return "Annotator";
			}
			return ".";
		}

		public Object getValueAt(int row, int col) {
			if (this.annotationPanel.allMatchedAnnotations == null) {
				return "*";
			}
			Annotation annotation = this.annotationPanel.allMatchedAnnotations
					.elementAt(row);
			switch (col) {
			case 0:
				return annotation.getDocument().getName();
			case 1:
				return annotation.getText();
			case 2:
				return annotation.getClassificationValue();
			case 3:
				return annotation.getSpans();
			case 4:
				return annotation.getKtAnnotation().getAnnotatorName();
			}
			return "*";
		}

		public Class getColumnClass(int col) {
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		private void setColumnWidth(int col, int width) {
			TableColumn column = annotationTable.getColumnModel()
					.getColumn(col);
			column.setMaxWidth(width);
			column.setMinWidth(width);
			column.setWidth(width);
			column.setPreferredWidth(width);
			annotationTable.sizeColumnsToFit(-1);
		}

		private int getPreferredColumnWidth(int col) {
			switch (col) {
			case 0:
				return 100;
			case 1:
				return 300;
			case 2:
				return 150;
			case 3:
				return 200;
			}
			return 100;
		}

		public void setPreferredColumnWidth(int col) {
			int width = getPreferredColumnWidth(col);
			setColumnWidth(col, width);
		}

		DefaultComboBoxModel getSelectedComboBoxModel() {
			return null;
		}
	}

	// JTree code
	private Object lastSelectedItem = null;
	private static boolean DoingUserSelection = false;

	public void updateTree(String value, OutcomeResult result,
			AnnotatorType atype) throws Exception {
		Annotation firstAnnotation = null;
		this.rootNode.removeAllChildren();
		this.treeModel.reload();
		ConstraintMatch cm = this.analysis.getSelectedConstraintMatch();
		for (Document document : this.analysis.getAllDocuments()) {
			String key = ConstraintMatch.getDocumentMatchedPairKey(
					document.getName(), value, result);
			Vector<Annotation> annotations = cm.getMatchedDocumentAnnotations(
					document.getName(), value, result, atype);
			if (annotations != null) {
				if (firstAnnotation == null) {
					firstAnnotation = annotations.firstElement();
				}
				Collections.sort(annotations, new Annotation.PositionSorter());
				ATreeMutableTreeNode dnode = new ATreeMutableTreeNode(this,
						document);
				treeModel.insertNodeInto(dnode, rootNode,
						rootNode.getChildCount());
				for (Annotation annotation : annotations) {
					ATreeMutableTreeNode anode = new ATreeMutableTreeNode(this,
							annotation);
					treeModel.insertNodeInto(anode, dnode,
							dnode.getChildCount());
				}
			}
		}
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		if (firstAnnotation != null) {
			this.analysis.setSelectedAnnotation(firstAnnotation);
			this.analysis.setSelectedDocument(firstAnnotation.getDocument());
			this.analysis.getWorkbenchGUI().fireAllDataUpdates();
		}
	}

	public void valueChanged(TreeSelectionEvent e) {
		ATreeMutableTreeNode node = (ATreeMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (node != null) {
			this.selectedNode = node;
			Object o = node.getUserObject();
		}
	}

	void processValueSelection(Object o) throws Exception {
		DoingUserSelection = false;
		if (!DoingUserSelection && o != null
				&& (lastSelectedItem == null || !lastSelectedItem.equals(o))) {
			lastSelectedItem = o;
			DoingUserSelection = true;
			if (o instanceof Annotation) {
				Annotation annotation = (Annotation) o;
				this.analysis.setSelectedAnnotation(annotation);
				this.analysis.setSelectedDocument(annotation.getDocument());
				this.analysis.getWorkbenchGUI().fireAllDataUpdates();
			}
			DoingUserSelection = false;
		}
	}

	class ATreeMutableTreeNode extends DefaultMutableTreeNode {
		public static final long serialVersionUID = 0;

		ATreeMutableTreeNode(AnnotationPanel tree, Object o) {
			super(o);
		}

		public String toString() {
			Object o = this.getUserObject();
			String rv = "*";
			if (o instanceof Document) {
				Document d = (Document) o;
				rv = d.getName();
			} else if (o instanceof Annotation) {
				Annotation a = (Annotation) o;
				rv = "\"" + a.getText() + "\"<" + a.getType().getName() + ">["
						+ a.getStart() + "-" + a.getEnd()
						+ "]                  ";
			}
			return rv;
		}
	}

	class ATreeDefaultTreeModel extends DefaultTreeModel {
		ATreeDefaultTreeModel(ATreeMutableTreeNode node) {
			super(node);
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			ATreeMutableTreeNode pathnode = (ATreeMutableTreeNode) path
					.getLastPathComponent();
		}

	}

	class ATree extends JTree {
		public static final long serialVersionUID = 0;

		ATree(DefaultTreeModel model) {
			super(model);
		}

		public String getToolTipText(MouseEvent e) {
			Point p = e.getPoint();
			TreePath path = tree.getClosestPathForLocation(p.x, p.y);
			ATreeMutableTreeNode node = (ATreeMutableTreeNode) path
					.getLastPathComponent();
			return "";
		}

	}

	class ATreeModelListener implements TreeModelListener {

		public void treeNodesChanged(TreeModelEvent e) {
			ATreeMutableTreeNode node = (ATreeMutableTreeNode) (e.getTreePath()
					.getLastPathComponent());
			try {
				int index = e.getChildIndices()[0];
				node = (ATreeMutableTreeNode) (node.getChildAt(index));
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
				Point p = new Point(e.getX(), e.getY());
				int col = this.table.columnAtPoint(p);
				int row = this.table.rowAtPoint(p);
				int numrows = this.table.getModel().getRowCount();
				boolean flag = true;
				if (col != lastMouseColumn || row != lastMouseRow
						|| numrows == 1) {
					lastMouseRow = row;
					lastMouseColumn = col;
					this.table.doSelection(row, col);
				}
				isMouseDown = false;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void mouseDragged(MouseEvent e) {
		int x = 1;
	}

	public String toString() {
		return "<AnnotationJTree:  Level=" + this.annotationType + ">";
	}

	public DefaultTreeModel getTreeModel() {
		return this.treeModel;
	}

}
