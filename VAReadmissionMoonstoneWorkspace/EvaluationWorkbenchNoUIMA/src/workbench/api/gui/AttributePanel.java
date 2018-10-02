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
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import workbench.api.Constants;
import workbench.api.annotation.Annotation;
import workbench.api.annotation.Annotator;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Type;
import workbench.arr.Colors;

public class AttributePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

	private Analysis analysis = null;
	private JLabel label = null;
	private AttributeTableModel model = null;
	private AttributeTable attributeTable = null;
	private JScrollPane scrollPane = null;
	private JButton FPtoTPButton = null;
	private static Font smallFont = new Font("Serif", Font.PLAIN, 12);
	private static boolean processingMouseEvent = false;
	private static boolean processingMouseMoved = false;
	private static int lastMouseRow = -1;
	private static int lastMouseColumn = -1;
	private static boolean addingNewAttribute = false;

	public AttributePanel(Analysis analysis) {
		super(new BorderLayout());
		this.analysis = analysis;
		this.FPtoTPButton = new JButton("FPtoTP");
		this.FPtoTPButton.addActionListener(this);
		this.add(this.FPtoTPButton);
		model = new AttributeTableModel(this);
		attributeTable = new AttributeTable(this);
		initializeColumns();
		attributeTable.setPreferredScrollableViewportSize(Constants.AttributeTableScrollPaneDimension);
		attributeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		attributeTable.addMouseMotionListener(this);
		attributeTable.addMouseListener(this);
		scrollPane = new JScrollPane(attributeTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.label = new JLabel("ATTRIBUTES", null, JLabel.CENTER);
		add(this.label, BorderLayout.PAGE_START);
		add(scrollPane, BorderLayout.PAGE_END);
		this.setOpaque(true);
	}

	void resetLabel() {
		Annotation annotation = this.analysis.getSelectedAnnotation();
		if (annotation != null && annotation.getClassificationValue() != null) {
			Object o = annotation.getClassificationValue();
			if (o instanceof String) {
				String str = (String) o;
				if (str.length() > 40) {
					str = str.substring(0, 40) + "...";
				}
				str = "Attributes for: \"" + str + "\"";
				label.setText(str);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if ("FPtoTP".equals(command)) {
			this.analysis.recordAnnotationFPtoTPText();
		}
	}

	void initializeColumns() {
		model.fireTableStructureChanged();
		for (int i = 0; i < attributeTable.getColumnCount(); i++) {
			TableColumn column = attributeTable.getColumnModel().getColumn(i);
			if (i == 0) {
				AttributeJComboBox cb = new AttributeJComboBox(this);
				cb.setEditable(true);
				DefaultCellEditor dce = new AttributeTableCellEditor(cb);
				column.setCellEditor(dce);
			} else {
				ValueJComboBox cb = new ValueJComboBox(this);
				cb.setEditable(true);
				DefaultCellEditor dce = new ValueTableCellEditor(cb);
				column.setCellEditor(dce);
			}
			int width = (i == 0 ? 150 : 120);
			column.setPreferredWidth(width);
		}
		model.fireTableDataChanged();
	}

	public void toggleColumn() throws Exception {
		int currentColumn = 0;
		Annotator annotator = analysis.getSelectedAnnotator();
		if (annotator.isPrimary()) {
			currentColumn = 1;
		} else {
			currentColumn = 2;
		}
		this.attributeTable.selectedColumn = currentColumn;
		this.model.fireTableDataChanged();
	}

	public class AttributeTable extends JTable implements ListSelectionListener {

		AttributePanel attributePanel = null;
		ValueJComboBox selectedComboBox = null;
		String selectedState = null;
		int selectedRow = -1;
		int selectedColumn = -1;

		AttributeTable(AttributePanel apanel) {
			super(model);
			this.attributePanel = apanel;
			attributeTable = this;
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

		public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int colIndex) {
			Component c = super.prepareRenderer(renderer, rowIndex, colIndex);
			Color color = getColor(rowIndex, colIndex);
			c.setBackground(color);
			return c;
		}

		public TableCellEditor getCellEditor() {
			return super.getCellEditor();
		}

		Color getColor(int row, int col) {
			if (row == lastMouseRow && col == lastMouseColumn) {
				return Colors.darkBlueGray;
			}
			if (row == lastMouseRow) {
				return Colors.lightBlueGray;
			}
			return Color.white;
		}

		void doSelection(int row, int col) throws Exception {
			WBGUI wbgui = this.attributePanel.analysis.getWorkbenchGUI();
			selectedRow = row;
			selectedColumn = col;
			wbgui.fireAllDataUpdates();
		}
	}

	public class AttributeTableModel extends AbstractTableModel {
		AttributePanel attributePanel = null;

		AttributeTableModel(AttributePanel apanel) {
			this.attributePanel = apanel;
			this.fireTableRowsInserted(0, getRowCount());
		}

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			resetLabel();
			Annotation annotation = this.attributePanel.analysis.getSelectedAnnotation();
			int numRows = 1;
			if (annotation != null) {
				Type type = annotation.getType();
				numRows = type.getNumberOfAttributes();
			}
			if (addingNewAttribute && annotation != null) {
				numRows += 1;
			}
			return Math.max(1, numRows);
		}

		public String getColumnName(int col) {
			Analysis analysis = this.attributePanel.analysis;
			switch (col) {
			case 0:
				return "Attribute";
			case 1:
				return analysis.getPrimaryAnnotator().getNames().toString();
			case 2:
				return analysis.getSecondaryAnnotator().getNames().toString();
			}
			return ".";
		}

		public Object getValueAt(int row, int col) {
			Analysis analysis = this.attributePanel.analysis;
			Annotation annotation = analysis.getSelectedAnnotation();
			if (annotation == null || analysis.getSelectedAnnotationEvent() == null) {
				return null;
			}
			Annotation primary = annotation.getMatchingPrimaryAnnotation();
			Annotation secondary = annotation.getMatchingSecondaryAnnotation();
			Attribute attribute = getAttribute(row);
			if (col == 0) {
				if (attribute != null) {
					return attribute.getName();
				}
			}
			if (col == 1) {
				if (primary != null) {
					Object value = primary.getAttributeValue(attribute);
					return value;
				}
			}
			if (col == 2) {
				if (secondary != null) {
					Object value = secondary.getAttributeValue(attribute);
					return value;
				}
			}
			return null;
		}

		public Attribute getAttribute() {
			return (attributeTable.selectedRow >= 0 ? getAttribute(attributeTable.selectedRow) : null);
		}

		public Attribute getAttribute(int row) {
			Annotation annotation = this.attributePanel.analysis.getSelectedAnnotation();
			if (annotation == null || row < 0) {
				return null;
			}
			Type type = annotation.getType();
			Vector<Attribute> attributes = type.getAttributes();
			if (attributes != null && row < attributes.size()) {
				Attribute attr = attributes.elementAt(row);
				return attr;
			}
			return null;
		}

		public Class getColumnClass(int col) {
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			Annotation annotation = this.attributePanel.analysis.getSelectedAnnotation();
			boolean editable = false;
			try {
				if (annotation != null) {
					Type type = annotation.getType();
					int numattr = type.getNumberOfAttributes();
					if ((col > 0 && row < numattr) || (col == 0 && row == numattr)) {
						editable = true;
					}
					if (processingMouseEvent) {
						attributeTable.doSelection(row, col);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return editable;
		}

		private void setColumnWidth(int col, int width) {
			TableColumn column = attributeTable.getColumnModel().getColumn(col);
			column.setMaxWidth(width);
			column.setMinWidth(width);
			column.setWidth(width);
			column.setPreferredWidth(width);
			attributeTable.sizeColumnsToFit(-1);
		}

		private int getPreferredColumnWidth(int col) {
			return (col == 0 ? 250 : 160);
		}

		public void setPreferredColumnWidth(int col) {
			int width = getPreferredColumnWidth(col);
			setColumnWidth(col, width);
		}

		DefaultComboBoxModel getSelectedComboBoxModel() {
			return null;
		}
	}

	class ValueJComboBox extends JComboBox {

		AttributePanel attributePanel = null;

		public ValueJComboBox(AttributePanel apanel) {
			this.attributePanel = apanel;
			setPreferredSize(new Dimension(150, 20));
			setEditable(true);
			this.setFont(smallFont);
			if (attributeTable.selectedRow >= 0) {
				Attribute attr = model.getAttribute(attributeTable.selectedRow);
				if (attr != null) {
					String aname = attr.getName();
					Annotation annotation = apanel.analysis.getSelectedAnnotation();
					if (annotation != null) {
						Type type = annotation.getType();
						Attribute attribute = type.getAttribute(aname);
						if (attribute != null) {
							Vector values = attribute.getValues();
							if (values != null) {
								for (Object value : values) {
									addItem(value);
								}
							}
						}
					}
				}
				Object selection = (String) attributeTable.getValueAt(attributeTable.selectedRow,
						attributeTable.selectedColumn);
				if (selection != null) {
					setSelectedItem(selection);
				}
			}
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
			String command = e.getActionCommand();
			if ("comboBoxChanged".equals(command) || "comboBoxEdited".equals(command)) {
				ValueJComboBox cb = (ValueJComboBox) e.getSource();
				if (attributeTable.selectedRow >= 0) {
					String vstr = (String) cb.getSelectedItem();
					Annotation annotation = this.attributePanel.analysis.getSelectedAnnotation();
					Attribute attr = model.getAttribute();
					String aname = attr.getName();
					Type type = annotation.getType();
					Attribute attribute = type.getAttribute(aname);
					annotation.putAttributeValue(attribute, vstr);
					model.fireTableDataChanged();
				}
			}
		}
	}

	public class ValueTableCellEditor extends DefaultCellEditor {
		public static final long serialVersionUID = 0;
		ValueJComboBox cb = null;

		public ValueTableCellEditor(ValueJComboBox cb) {
			super(cb);
			this.cb = cb;
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
			try {
				if (col > 0) {
					attributeTable.doSelection(row, col);
					this.cb = new ValueJComboBox(attributeTable.attributePanel);
					Annotation annotation = attributeTable.attributePanel.analysis.getSelectedAnnotation();
					if (annotation != null) {
						Attribute attribute = model.getAttribute();
						Object av = annotation.getAttributeValue(attribute);
						if (av != null) {
							this.cb.setSelectedItem(av);
						}
					}
					return this.cb;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	class AttributeJComboBox extends JComboBox {
		private AttributePanel attributePanel = null;

		public AttributeJComboBox(AttributePanel apanel) {
			this.attributePanel = apanel;
			setPreferredSize(new Dimension(150, 20));
			setEditable(true);
			this.setFont(smallFont);
			Annotation annotation = apanel.analysis.getSelectedAnnotation();
			if (attributeTable.selectedColumn == 0 && annotation != null && annotation.getType() != null) {
				Type type = annotation.getType();
				Vector<String> attributes = type.getAttributeStrings();
				if (attributes != null) {
					for (String astr : attributes) {
						addItem(astr);
					}
				}
			}
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
			String command = e.getActionCommand();
			if ("comboBoxChanged".equals(command) || "comboBoxEdited".equals(command)) {
				AttributeJComboBox cb = (AttributeJComboBox) e.getSource();
				String aname = (String) cb.getSelectedItem();
				Annotation annotation = cb.attributePanel.analysis.getSelectedAnnotation();
				Type type = annotation.getType();
				Attribute attribute = type.getAttribute(aname);
				String value = null;
				if (attribute == null) {
					type.addAttribute(aname);
				}
				attribute = type.getAttribute(aname);
				if (attribute != null) {
					if (attribute.getValues() != null) {
						value = (String) attribute.getValues().firstElement();
					} else {
						value = "EMPTY";
					}
					// if (annotation1 != null) {
					// annotation1.setAttribute(aname, value);
					// }
					// if (annotation2 != null) {
					// annotation2.setAttribute(aname, value);
					// }
					// arrTool.fireAllTableDataChanged();
				}

			}
		}
	}

	public class AttributeTableCellEditor extends DefaultCellEditor {
		public static final long serialVersionUID = 0;
		AttributeJComboBox cb = null;

		public AttributeTableCellEditor(AttributeJComboBox cb) {
			super(cb);
			this.cb = cb;
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
			Annotation annotation = cb.attributePanel.analysis.getSelectedAnnotation();
			Vector<Attribute> attributes = annotation.getAllAttributes();
			if (col == 0 && annotation != null && attributes != null && row == attributes.size()) {
				this.cb = new AttributeJComboBox(cb.attributePanel);
			}
			return this.cb;
		}
	}

	public void mouseMoved(MouseEvent e) {
		try {
			if (e.isControlDown()
			// || EvaluationWorkbench.isMouseControlKeyInteraction()
			) {
				addingNewAttribute = false;
				processingMouseMoved = true;
				Point p = new Point(e.getX(), e.getY());
				int col = attributeTable.columnAtPoint(p);
				int row = attributeTable.rowAtPoint(p);
				if (col != lastMouseColumn || row != lastMouseRow) {
					lastMouseRow = row;
					lastMouseColumn = col;
					CellEditor ce = attributeTable.getCellEditor();
					if (ce != null) {
						ce.stopCellEditing();
					}
					attributeTable.doSelection(row, col);
				}
				processingMouseMoved = false;
			} else if (e.isMetaDown()) {
				addingNewAttribute = true;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * ******* Mouse operations ******.
	 * 
	 * @param e
	 *            the MouseEvent
	 */
	public void mousePressed(MouseEvent e) {
		// try {
		// if (attributeTable.selectedColumn == 0 && e.isMetaDown()) {
		// addingNewAttribute = true;
		// arrTool.fireAllTableDataChanged();
		// } else if (addingNewAttribute) {
		// addingNewAttribute = false;
		// arrTool.fireAllTableDataChanged();
		// }
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }
	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mouseClicked(MouseEvent e) {
	}

	public AttributeTable getAttributeTable() {
		return attributeTable;
	}

	public AttributeTableModel getModel() {
		return model;
	}

}