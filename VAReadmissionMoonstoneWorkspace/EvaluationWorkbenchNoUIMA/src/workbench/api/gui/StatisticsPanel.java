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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import tsl.documentanalysis.document.Sentence;
import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.TimeUtils;
import tsl.utilities.VUtils;
import workbench.api.Analysis;
import workbench.api.AnnotatorType;
import workbench.api.Constants;
import workbench.api.OutcomeResult;
import workbench.api.WorkbenchAPIObject;
import workbench.api.annotation.Annotation;
import workbench.api.constraint.ConstraintMatch;
import workbench.arr.Colors;
import workbench.arr.EvaluationWorkbench;

/**
 * The Class StatisticsPane.
 */
public class StatisticsPanel extends JPanel implements MouseMotionListener,
		MouseListener, ActionListener {

	private Analysis analysis = null;
	private AccuracyJTableModel model = null;
	private AccuracyJTable accuracyTable = null;

	private static Font smallFont = new Font("Serif", Font.PLAIN, 12);
	private int lastSelectedMouseRow = -1;
	private int lastSelectedMouseColumn = -1;
	private static Color selectedCellColor = Colors.lightTan;
	private static Color selectedRowColor = Colors.darkBlueGray;
	private static Color selectedColumnColor = Colors.darkBlueGray;
	private static Color unselectedColor = Color.white;

	private static Vector<String> SpanOutcomeMeasureColumnLabels = VUtils
			.arrayToVector(new String[] { "Concept", "TP", "FP", "TN", "FN",
					"Acc", "PPV", "Sen", "NPV", "Spec", "ScottsPi", "CohenK",
					"Fmeas" });

	private static Vector<String> ClassOnlyOutcomeMeasureColumnLabels = VUtils
			.arrayToVector(new String[] { "Accuracy", "Correct", "Incorrect",
					"Total" });

	public static String SelectedErrorColumn = null;

	public StatisticsPanel(Analysis analysis) {
		this.analysis = analysis;
		model = new AccuracyJTableModel(this);
		accuracyTable = new AccuracyJTable(this);
		initializeColumns();
		accuracyTable
				.setPreferredScrollableViewportSize(Constants.StatisticsPanelDimension);
		accuracyTable.setMinimumSize(Constants.StatisticsPanelDimension);
		accuracyTable.setPreferredSize(Constants.StatisticsPanelDimension);
		accuracyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		accuracyTable.addMouseMotionListener(this);
		accuracyTable.addMouseListener(this);
		add(new JScrollPane(accuracyTable));
		this.setOpaque(true);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
	}

	void initializeColumns() {
		for (int i = 0; i < accuracyTable.getColumnCount(); i++) {
			TableColumn column = accuracyTable.getColumnModel().getColumn(i);
			int width = this.model.getPreferredColumnWidth(i);
			column.setPreferredWidth(width);
		}
	}

	public String getColumnLabel(int col) {
		String label = SpanOutcomeMeasureColumnLabels.elementAt(col);
		return label;
	}

	public class AccuracyJTable extends JTable implements ListSelectionListener {
		StatisticsPanel cpane = null;
		Vector accuracy = null;
		private int selectedRow = -1;
		private int selectedColumn = -1;

		AccuracyJTable(StatisticsPanel cpane) {
			super(model);
			this.cpane = cpane;
			accuracyTable = this;
			this.setShowGrid(true);
			this.setGridColor(Color.GRAY);
			this.setRowHeight(15);
			this.setFont(smallFont);
			setToolTipText("");
		}

		public String getToolTipText(MouseEvent e) {
			Point p = e.getPoint();
			int row = rowAtPoint(p);
			int col = columnAtPoint(p);
			String text = null;
			return "*";
		}

		void populate() {
			model.fireTableRowsInserted(0, getRowCount());
			model.fireTableDataChanged();
		}

		public void doSelection(int row, int col) throws Exception {
			int classRow = row - 1;
			int resultCol = col - 1;
			ConstraintMatch cm = this.cpane.getConstraintMatch();
			if (classRow >= 0 && resultCol >= 0 && cm != null
					&& cm.getAlternativeValues() != null) {
				showCell(row, col);
				if (resultCol < OutcomeResult.values().length) {
					OutcomeResult or = OutcomeResult.values()[col - 1];
					String value = cm.getAlternativeValues()
							.elementAt(classRow);
					// Determine how to show one or the other or both in
					// annotation
					// panel.

					AnnotatorType atype = AnnotatorType.primary;

					if ("FN".equals(or.toString())) {
						int x = 1;
					}
					if ("FP".equals(or.toString())) {
						atype = AnnotatorType.secondary;
					}

					this.cpane.analysis.getWorkbenchGUI().getAnnotationPanel()
							.updateTable(value, or, atype);

					// Before 9/28/2014
					// this.cpane.analysis.getWorkbenchGUI().getAnnotationPanel()
					// .updateTree(value, or, atype);
					this.cpane.analysis.getWorkbenchGUI().fireAllDataUpdates();
				}
			}
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
			boolean isSelectedRow = false;
			boolean isSelectedCol = false;
			isSelectedRow = (row == lastSelectedMouseRow);
			isSelectedCol = (col == lastSelectedMouseColumn);
			if (col > 0) {
				if (isSelectedRow && isSelectedCol) {
					return selectedCellColor;
				}
				if (isSelectedCol) {
					return selectedColumnColor;
				}
			}
			if (isSelectedRow) {
				return selectedRowColor;
			}
			return Color.white;
		}

		public void showCell(int row, int column) {
			Rectangle rect = getCellRect(row, column, true);
			scrollRectToVisible(rect);
		}

		public void setSelectedRow(int row) {
			this.selectedRow = row;
		}

		public int getSelectedRow() {
			return this.selectedRow;
		}

		public void setSelectedColumn(int col) {
			this.selectedColumn = col;
		}

		public int getSelectedColumn() {
			return this.selectedColumn;
		}

	};

	public class AccuracyJTableModel extends AbstractTableModel {
		private StatisticsPanel statisticsPane = null;

		AccuracyJTableModel(StatisticsPanel spane) {
			this.statisticsPane = spane;
			this.fireTableRowsInserted(0, getRowCount());
		}

		public int getColumnCount() {
			return SpanOutcomeMeasureColumnLabels.size();
		}

		public int getRowCount() {
			if (this.statisticsPane.getConstraintMatch() != null
					&& this.statisticsPane.getConstraintMatch()
							.getAlternativeValues() != null) {
				int rc = this.statisticsPane.getConstraintMatch()
						.getAlternativeValues().size() + 1;
				return rc;
			}
			return 1;
		}

		public String getColumnName(int col) {
			String name = getColumnLabel(col);
			return name;
		}

		String getFirstColumnName(int row) {
			if (row < 0) {
				return "SUMMARY";
			}
			if (this.statisticsPane.getConstraintMatch() != null) {
				return this.statisticsPane.getConstraintMatch()
						.getFirstColumnNameAll(row);
			}
			return "*";
		}

		public Object getValueAt(int row, int col) {
			row = row - 1;
			ConstraintMatch cm = this.statisticsPane.getConstraintMatch();
			if (cm == null) {
				return "*";
			}
			if (col == 0) {
				return getFirstColumnName(row);
			}
			if (row == -1) { // Summary row
				switch (col) {
				case 1:
					return cm.getCumulativeTruePositiveCount();
				case 2:
					return cm.getCumulativeFalsePositiveCount();
				case 3:
					return cm.getCumulativeTrueNegativeCount();
				case 4:
					return cm.getCumulativeFalseNegativeCount();
				case 5:
					return getAccuracy(row);
				case 6:
					return getPPV(row);
				case 7:
					return getSensitivity(row);
				case 8:
					return getNPV(row);
				case 9:
					return getSpecificity(row);
				case 10:
					return getScottsPi(row);
				case 11:
					return getCohensKappa(row);
				case 12:
					return getFmeasure(row);
				}
			} else {
				switch (col) {
				case 1:
					return cm.getTruePositiveCount(row);
				case 2:
					return cm.getFalsePositiveCount(row);
				case 3:
					return cm.getTrueNegativeCount(row);
				case 4:
					return cm.getFalseNegativeCount(row);
				case 5:
					return getAccuracy(row);
				case 6:
					return getPPV(row);
				case 7:
					return getSensitivity(row);
				case 8:
					return getNPV(row);
				case 9:
					return getSpecificity(row);
				case 10:
					return getScottsPi(row);
				case 11:
					return getCohensKappa(row);
				case 12:
					return getFmeasure(row);
				}
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
			TableColumn column = accuracyTable.getColumnModel().getColumn(col);
			column.setMaxWidth(width);
			column.setMinWidth(width);
			column.setWidth(width);
			column.setPreferredWidth(width);
			accuracyTable.sizeColumnsToFit(-1);
		}

		private int getPreferredColumnWidth(int col) {
			if (col == 0) {
				return 250;
			}
			return 40;
		}

		public void setPreferredColumnWidth(int col) {
			int width = getPreferredColumnWidth(col);
			setColumnWidth(col, width);
		}
	}

	// 9/23/2014
	private boolean externallyChangingSelectedPosition = false;

	public void externallyChangeSelectedPosition(int row) throws Exception {
		if (row >= 0 && row != getSelectedRow()
				&& !externallyChangingSelectedPosition) {
			externallyChangingSelectedPosition = true;
			setSelectedRow(row);
			setSelectedColumn(0);
			lastSelectedMouseRow = getSelectedRow();
			lastSelectedMouseColumn = getSelectedColumn();
			accuracyTable.doSelection(row, 0);
			externallyChangingSelectedPosition = false;
		}
	}

	public void changeSelectedPosition(int row, int col) throws Exception {
		setSelectedRow(row);
		setSelectedColumn(col);
		lastSelectedMouseRow = getSelectedRow();
		lastSelectedMouseColumn = getSelectedColumn();
		accuracyTable.doSelection(row, col);
	}

	public void mouseMoved(MouseEvent e) {
		if (e.isControlDown() || e.isMetaDown()
				|| EvaluationWorkbench.isMouseControlKeyInteraction()) {
			try {
				Point p = new Point(e.getX(), e.getY());
				int col = accuracyTable.columnAtPoint(p);
				int row = accuracyTable.rowAtPoint(p);
				if (col != lastSelectedMouseColumn
						|| row != lastSelectedMouseRow) {
					lastSelectedMouseRow = row;
					lastSelectedMouseColumn = col;
					changeSelectedPosition(row, col);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
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

	// Before 1/8/2013
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			EvaluationWorkbench.toggleIsMouseControlKeyInteraction();
		}
	}

	public void setSelectedRow(int row) {
		this.accuracyTable.setSelectedRow(row);
	}

	public int getSelectedRow() {
		return this.accuracyTable.getSelectedRow();
	}

	public void setSelectedColumn(int col) {
		this.accuracyTable.setSelectedColumn(col);
	}

	public int getSelectedColumn() {
		return this.accuracyTable.getSelectedColumn();
	}

	public int getLastSelectedMouseRow() {
		return lastSelectedMouseRow;
	}

	public void setLastSelectedMouseRow(int row) {
		this.lastSelectedMouseRow = row;
	}

	public int getLastSelectedMouseColumn() {
		return lastSelectedMouseColumn;
	}

	public void setLastSelectedMouseColumn(int col) {
		this.lastSelectedMouseColumn = col;
	}

	public String getAccuracy(int row) {
		ConstraintMatch cm = this.getConstraintMatch();
		float tp = cm.getTruePositiveCount(row);
		float fp = cm.getFalsePositiveCount(row);
		float tn = cm.getTrueNegativeCount(row);
		float fn = cm.getFalseNegativeCount(row);
		float num = tp + tn;
		float den = tp + fp + tn + fn;
		return getResultString(num, den);
	}

	public String getPPV(int row) {
		ConstraintMatch cm = this.getConstraintMatch();
		float tp = cm.getTruePositiveCount(row);
		float fp = cm.getFalsePositiveCount(row);
		float num = tp;
		float den = tp + fp;
		return getResultString(num, den);
	}

	public String getSensitivity(int row) {
		ConstraintMatch cm = this.getConstraintMatch();
		float tp = cm.getTruePositiveCount(row);
		float fn = cm.getFalseNegativeCount(row);
		float num = tp;
		float den = tp + fn;
		return getResultString(num, den);
	}

	public String getNPV(int row) {
		ConstraintMatch cm = this.getConstraintMatch();
		float tn = cm.getTrueNegativeCount(row);
		float fn = cm.getFalseNegativeCount(row);
		float num = tn;
		float den = tn + fn;
		return getResultString(num, den);
	}

	public String getSpecificity(int row) {
		ConstraintMatch cm = this.getConstraintMatch();
		float fp = cm.getFalsePositiveCount(row);
		float tn = cm.getTrueNegativeCount(row);
		float num = tn;
		float den = tn + fp;
		return getResultString(num, den);
	}

	public String getCohensKappa(int row) {
		ConstraintMatch cm = this.getConstraintMatch();
		float tp = cm.getTruePositiveCount(row);
		float tn = cm.getTrueNegativeCount(row);
		float fp = cm.getFalsePositiveCount(row);
		float fn = cm.getFalseNegativeCount(row);

		/* PFR kappa 4-26-2012 */
		/* get Agreement observed, and by chance, Ao - Ae / 1 - Ae */
		float totalobs = tp + tn + fp + fn;

		/* get marginal sums on contingency table */
		/*
		 * eg primary 1 yes no prim 2 yes tp fp no fn tn
		 */
		float marg4p2_pos = (tp + fp); /*
										 * across row if primary 1 is
										 * cols=ground truth
										 */
		float marg4p1_pos = (tp + fn); /* across cols */
		float marg4p2_neg = (tn + fn);
		float marg4p1_neg = (tn + fp);

		float E_tp = 0;
		float E_tn = 0;
		float Ae = 0;
		float Ao = 0;
		if (totalobs > 0) {
			/*
			 * essiantly, take Prob(primary 1 said true)*Prob(primary2 said
			 * true), etc... where Prob is calcuated for each rater
			 */
			E_tp = (marg4p1_pos / totalobs) * (marg4p2_pos / totalobs); /*
																		 * expected
																		 * tp
																		 * matches
																		 */
			E_tn = (marg4p1_neg / totalobs) * (marg4p2_neg / totalobs); /*
																		 * expect
																		 * tn
																		 * matches
																		 */

			Ao = (tp + tn) / totalobs; /* agreements or matches frequency */
			Ae = (E_tp + E_tn); /* expect chance agreements */
		}

		float num = Ao - Ae;
		float den = 1 - Ae;
		return getResultString(num, den);
	}

	public String getScottsPiFormula(int row) {
		String formula = "Scott's Pi = *";
		return formula;
	}

	/* PFR added this for Scotts PI calculation, 4-26 */
	public String getScottsPi(int row) {
		ConstraintMatch cm = this.getConstraintMatch();
		float tp = cm.getTruePositiveCount(row);
		float tn = cm.getTrueNegativeCount(row);
		float fp = cm.getFalsePositiveCount(row);
		float fn = cm.getFalseNegativeCount(row);

		/* PFR kappa 4-26-2012 */
		/* get Agreement observed, and by chance, Ao - Ae / 1 - Ae */
		float totalobs = tp + tn + fp + fn;

		/*
		 * eg primary 1 yes no prim 2 yes tp fp no fn tn
		 */
		float marg4p2_pos = (tp + fp); /*
										 * across row if primary 1 is
										 * cols=ground truth
										 */
		float marg4p1_pos = (tp + fn); /* across cols */
		float marg4p2_neg = (tn + fn);
		float marg4p1_neg = (tn + fp);

		float E_tp = 0;
		float E_tn = 0;
		float Ae = 0;
		float Ao = 0;
		if (totalobs > 0) {
			/*
			 * essentially, estimate Prob (primary said true) as pooled across
			 * raters, etcc..
			 */
			E_tp = (marg4p1_pos + marg4p2_pos) / (2 * totalobs); /*
																 * expected tp
																 * matches
																 */
			E_tn = (marg4p1_neg + marg4p2_neg) / (2 * totalobs); /*
																 * expect tn
																 * matches
																 */

			Ao = (tp + tn) / totalobs; /* agreements or matches frequency */
			Ae = (E_tp + E_tn); /* expect chance agreements */
		}

		float num = Ao - Ae;
		float den = 1 - Ae;
		return getResultString(num, den);
	}

	public String getFmeasure(int row) {
		ConstraintMatch cm = this.getConstraintMatch();
		float tp = cm.getTruePositiveCount(row);
		float tn = cm.getTrueNegativeCount(row);
		float fp = cm.getFalsePositiveCount(row);
		float fn = cm.getFalseNegativeCount(row);

		/* F=(1+B^2)*recall*precision/ B^2*precision + recall */
		float Bwt = 1;
		/*
		 * B weight of recall vs precsn, B>1,or<1, means weight recall, orprecn,
		 * more
		 */

		float recall = 0;
		float den = tp + fn;
		if (den > 0) {
			recall = tp / den;
		}
		/* else let recall be 0 */

		float precision = 0;
		den = tp + fp;
		if (den > 0) {
			precision = tp / den;
		}
		/* else let precsn be 0 */

		float num = (1 + Bwt * Bwt) * recall * precision;
		den = (Bwt * Bwt * precision + recall);

		return getResultString(num, den);
	}

	String getResultString(float numerator, float denominator) {
		float result = 0;
		String resultString = "*";
		if (denominator > 0) {
			result = numerator / denominator;
			resultString = String.valueOf(result);
			if (resultString.length() > 4) {
				resultString = resultString.substring(0, 4);
			}
		}
		return resultString;
	}

	public ConstraintMatch getConstraintMatch() {
		return this.analysis.getSelectedConstraintMatch();
	}

	public AccuracyJTableModel getModel() {
		return model;
	}

	public void printUnadjudicatedMatchCountsToFile() {
		ConstraintMatch cm = this.analysis.getSelectedConstraintMatch();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cm.getAlternativeValues().size(); i++) {
			String cstr = cm.getAlternativeValues().elementAt(i);
			sb.append(cstr + ",");
			sb.append(cm.getTruePositiveCount(i) + ",");
			sb.append(cm.getFalsePositiveCount(i) + ",");
			sb.append(cm.getTrueNegativeCount(i) + ",");
			sb.append(cm.getFalseNegativeCount(i) + "\n");
		}
		String fname = "C:\\Users\\VHASLCChrisL1\\Desktop\\READMISSION\\UnadjudicatedMatchFile.txt";
		FUtils.writeFile(fname, sb.toString());
	}

	public void printStatisticsToTerminal() {
		ConstraintMatch cm = this.analysis.getSelectedConstraintMatch();
		StringBuffer sb = new StringBuffer();
		sb.append("CUMULATIVE:@");
		sb.append("TP=" + cm.getCumulativeTruePositiveCount() + "@");
		sb.append("FP=" + cm.getCumulativeFalsePositiveCount() + "@");
		sb.append("FN=" + cm.getCumulativeFalseNegativeCount() + "@");
		sb.append("ACC=" + this.getResultString((float) cm.getAccuracy(-1), 1f)
				+ "@");
		sb.append("SEN="
				+ this.getResultString((float) cm.getSensitivity(-1), 1f) + "@");
		sb.append("F=" + this.getResultString((float) cm.getFmeasure(-1), 1f)
				+ "@");
		sb.append("PPV=" + this.getResultString((float) cm.getPPV(-1), 1f)
				+ "\n");
		for (int i = 0; i < cm.getAlternativeValues().size(); i++) {
			String cstr = cm.getAlternativeValues().elementAt(i);
			sb.append("Variable=" + cstr + "@");
			sb.append("TP=" + cm.getTruePositiveCount(i) + "@");
			sb.append("FP=" + cm.getFalsePositiveCount(i) + "@");
			sb.append("FN=" + cm.getFalseNegativeCount(i) + "@");
			sb.append("ACC="
					+ this.getResultString((float) cm.getAccuracy(i), 1f) + "@");
			sb.append("SEN="
					+ this.getResultString((float) cm.getSensitivity(i), 1f)
					+ "@");
			sb.append("F="
					+ this.getResultString((float) cm.getFmeasure(i), 1f) + "@");
			sb.append("PPV=" + this.getResultString((float) cm.getPPV(i), 1f)
					+ "\n");
			Vector<Annotation> fps = Annotation.getUniqueAnnotations(cm
					.getFalsePositives(cstr));
			if (fps != null) {
				for (Annotation fp : fps) {
					String snippet = fp.getText();
					String doctext = fp.getDocument().getText();
					Sentence sentence = fp.getDocument().getSentence(
							fp.getStart());
					String context = "*";
					if (sentence != null) {
						context = sentence.getText();
					}
					context = StrUtils.removeChar(context, '\n');
					if (context.length() > 100) {
						context = context.substring(0, 100) + "...";
					}
					sb.append("...@...@" + snippet + "@" + context
							+ "@...@...@...@...\n");
				}
			}
		}
		FUtils.writeFile(
				"C:\\Users\\VHASLCChrisL1\\Desktop\\ReadmissionMoonstone\\Moonstone\\ReadmissionFPResults",
				sb.toString());
	}

	public void writeStatisticsXMLToFile() {
		String resourcedir = this.analysis.getKnownledgeEngine()
				.getStartupParameters().getResourceDirectory();
		String dname = this.analysis.getKnownledgeEngine()
				.getStartupParameters()
				.getPropertyValue("WorkbenchStatisticsDirectory");
		if (resourcedir != null && dname != null
				&& this.analysis.getSelectedConstraintMatch() != null) {
			String dpath = resourcedir + File.separatorChar + dname;
			FUtils.findOrCreateDirectory(dpath, false);
			String datestr = TimeUtils.getDateTimeString();
			// String oname = this.analysis.getSelectedConstraintMatch()
			// .getWorkbenchAPIObject().getName();
			String cname = this.analysis.getSelectedConstraintPacket()
					.getName();
			String xml = this.generateXML(datestr, cname);
			datestr = StrUtils.replaceNonAlphaNumericCharactersWithDelim(
					datestr, '_');
			String fullpath = dpath + File.separatorChar + cname + "_"
					+ datestr;
			FUtils.writeFile(fullpath, xml, true);
		}
	}

	public String generateXML(String datestr, String conname) {
		StringBuffer sb = new StringBuffer();
		sb.append("<EvaluationWorkbenchStatistics date=\"" + datestr
				+ "\" constraint=\"" + conname + "\">\n");
		for (int row = 0; row < this.model.getRowCount(); row++) {
			sb.append("  <EvaluationWorkbenchRow row=" + row + ">\n");
			for (int col = 0; col < this.model.getColumnCount(); col++) {
				String cname = this.model.getColumnName(col);
				String cstr = this.model.getValueAt(row, col).toString();
				if (col == 0) {
					int index = cstr.indexOf(':');
					if (index > 0) {
						cstr = cstr.substring(index + 1);
					}
				}
				sb.append("    <WorkbenchColumn column=\"" + cname
						+ "\" value=\"" + cstr + "\"/>\n");
			}
			sb.append("  </EvaluationWorkbenchRow>\n");
		}
		sb.append("</EvaluationWorkbenchStatistics>");
		return sb.toString();
	}
}
