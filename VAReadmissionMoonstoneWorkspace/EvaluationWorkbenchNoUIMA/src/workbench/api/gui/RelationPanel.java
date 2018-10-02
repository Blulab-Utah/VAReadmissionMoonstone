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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.CellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import workbench.api.Analysis;
import workbench.api.annotation.Annotation;
import workbench.api.typesystem.Relation;

public class RelationPanel extends JPanel implements MouseMotionListener,
		MouseListener, ActionListener {

	Analysis analysis = null;
	RelationTableModel model = null;
	RelationTable relationTable = null;
	JScrollPane scrollPane = null;
	JLabel label = null;
	static Font smallFont = new Font("Serif", Font.PLAIN, 12);
	static Color tempColor = new Color(0xffffff);
	static boolean processingMouseEvent = false;
	static boolean processingMouseMoved = false;
	static int lastMouseRow = -1;
	static int lastMouseColumn = -1;

	public RelationPanel(Analysis analysis) {
		this.analysis = analysis;
		model = new RelationTableModel();
		relationTable = new RelationTable();
		initializeColumns();
		relationTable
				.setPreferredScrollableViewportSize(new Dimension(800, 400));
		// relationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		relationTable.addMouseMotionListener(this);
		relationTable.addMouseListener(this);
		scrollPane = new JScrollPane(relationTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.label = new JLabel("RELATIONS", null, JLabel.CENTER);
		// add(this.label);
		add(scrollPane);
		this.setOpaque(true);
	}

	public void actionPerformed(ActionEvent e) {

	}

	void resetLabel() {
		Annotation annotation = analysis.getSelectedAnnotation();
		if (annotation != null && analysis.getSelectedAnnotator() != null) {
			String annotator = analysis.getSelectedAnnotator().getNames()
					.toString().toUpperCase();
			if (annotator != null
					&& annotation.getClassificationValue() != null) {
				Object o = annotation.getClassificationValue();
				if (o instanceof String) {
					String str = (String) o;
					if (str.length() > 20) {
						str = str.substring(0, 20) + "...";
					}
					str = "Relations for: \"" + str + "\"";
					label.setText(str);
				}
			}
		}
	}

	void initializeColumns() {
		for (int i = 0; i < relationTable.getColumnCount(); i++) {
			TableColumn column = relationTable.getColumnModel().getColumn(i);
			int width = (i == 0 ? 100 : 400);
			column.setPreferredWidth(width);
		}
		model.fireTableDataChanged();
	}

	public class RelationTable extends JTable implements ListSelectionListener {

		String selectedState = null;

		int selectedRow = -1;

		/** The selected column. */
		int selectedColumn = -1;

		RelationTable() {
			super(model);
			relationTable = this;
			this.setShowGrid(true);
			this.setGridColor(Color.GRAY);
			this.setRowHeight(15);
			this.setFont(smallFont);
			setToolTipText("");
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
			boolean selectedCell = (row == relationTable.selectedRow && col == relationTable.selectedColumn);
			// if (col > 0 && selectedCell) {
			// return Color.DARK_GRAY;
			// }
			// if (arrTool.getAnalysis().getSelectedClassification() != null
			// && arrTool
			// .getAnalysis()
			// .getSelectedClassification()
			// .equals(arrTool.getAnalysis()
			// .getClassificationByRow(row))) {
			// return Color.LIGHT_GRAY;
			// }
			//
			// if (row == relationTable.selectedRow) {
			// return Color.DARK_GRAY;
			// }
			// if (col > 0 && col == relationTable.selectedColumn) {
			// return Color.LIGHT_GRAY;
			// }
			return Color.white;
		}

		void doSelection(int row, int col) throws Exception {
			selectedRow = row;
			selectedColumn = col;
			Annotation relatum = model.getRelatum(row);
			if (relatum != null) {
				// Deactivated...
				// if (analysis.getSelectedLevel() != relatum.getType()) {
				// analysis.setSelectedClassification(relatum
				// .getClassification());
				// analysis.setSelectedLevel((Annotation) relatum.getType());
				// }
				analysis.setSelectedAnnotation(relatum);
			}

		}

	};

	public class RelationTableModel extends AbstractTableModel {

		RelationTableModel() {
			this.fireTableRowsInserted(0, getRowCount());
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			resetLabel();
			Annotation annotation = analysis.getSelectedAnnotation();
			if (annotation != null && annotation.getRelations() != null) {
				return annotation.getRelations().size();
			}
			return 0;
		}

		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Relation";
			case 1:
				return "Relatum";
			}
			return ".";
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) {
				String relation = getRelation(row);
				return relation;
			}
			Annotation relatum = getRelatum(row);
			return relatum;
		}

		public String getRelation() {
			return (relationTable.selectedRow >= 0 ? getRelation(relationTable.selectedRow)
					: null);
		}

		public String getRelation(int row) {
			if (analysis.getSelectedAnnotation() != null) {
				Annotation annotation = analysis.getSelectedAnnotation();
				Vector<Relation> v = annotation.getRelations();
				if (v != null && row >= 0 && row < v.size()) {
					Relation r = v.elementAt(row);
					return r.getRelation();
				}
			}
			return null;
		}

		public Annotation getRelatum(int row) {
			if (analysis.getSelectedAnnotation() != null) {
				Annotation annotation = analysis.getSelectedAnnotation();
				Vector<Relation> v = annotation.getRelations();
				if (v != null && row >= 0 && row < v.size()) {
					Relation ro = v.elementAt(row);
					return ro.getModifier();
				}
			}
			return null;
		}

		public Class getColumnClass(int col) {
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		private void setColumnWidth(int col, int width) {
			TableColumn column = relationTable.getColumnModel().getColumn(col);
			column.setMaxWidth(width);
			column.setMinWidth(width);
			column.setWidth(width);
			column.setPreferredWidth(width);
			relationTable.sizeColumnsToFit(-1);
		}

		private int getPreferredColumnWidth(int col) {
			return (col == 0 ? 160 : 250);
		}

		public void setPreferredColumnWidth(int col) {
			int width = getPreferredColumnWidth(col);
			setColumnWidth(col, width);
		}

		DefaultComboBoxModel getSelectedComboBoxModel() {
			return null;
		}
	}

	public void mouseMoved(MouseEvent e) {
		if (e.isControlDown()) {
			processingMouseMoved = true;
			Point p = new Point(e.getX(), e.getY());
			int col = relationTable.columnAtPoint(p);
			int row = relationTable.rowAtPoint(p);
			if (col != lastMouseColumn || row != lastMouseRow) {
				lastMouseRow = row;
				lastMouseColumn = col;
				CellEditor ce = relationTable.getCellEditor();
				if (ce != null) {
					ce.stopCellEditing();
				}
				relationTable.selectedRow = row;
				relationTable.selectedColumn = col;
				Annotation relatum = model.getRelatum(row);
				try {
					if (relatum != null) {
						WBGUI arrTool = analysis.getWorkbenchGUI();
						analysis.setSelectedAnnotation(relatum);
						arrTool.getPrimaryDocumentPanel().highlightSentences();
						arrTool.getSecondaryDocumentPanel()
								.highlightSentences();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				model.fireTableDataChanged();
			}
			processingMouseMoved = false;
		}
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mouseClicked(MouseEvent e) {
		// try {
		// if (relationTable.selectedRow >= 0
		// && relationTable.selectedColumn >= 0) {
		// relationTable.doSelection(relationTable.selectedRow,
		// relationTable.selectedColumn);
		// }
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }
	}

	public RelationTableModel getModel() {
		return model;
	}

}
