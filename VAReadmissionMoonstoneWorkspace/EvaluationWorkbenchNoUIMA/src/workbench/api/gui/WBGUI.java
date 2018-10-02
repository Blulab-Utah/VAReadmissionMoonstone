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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import tsl.knowledge.engine.KnowledgeEngine;
import workbench.api.Analysis;
import workbench.api.annotation.Annotation;
import workbench.arr.EvaluationWorkbench;

public class WBGUI extends JPanel implements ActionListener {

	private Analysis analysis = null;
	private EvaluationWorkbench arrtool = null;
	private JFrame frame = null;
	private StatisticsPanel statisticsPanel = null;
	private JTabbedPane annotationTypeTabbedPane = null;
	private AnnotationPanel annotationPanel = null;
	private ConstraintPanel constraintPanel = null;
	private TypePanel typePanel = null;
	private RelationPanel relationPanel = null;
	private DocumentPanel primaryDocumentPanel = null;
	private DocumentPanel secondaryDocumentPanel = null;
	private AttributePanel attributePanel = null;
	public static WBGUI WorkbenchGUI = null;

	protected static String[][] menuInfo = { { null, "operations", "Operations" }, };

	protected static Object[][] menuItemInfo = {
			{ "operations", "printStatistics", "Print Statistics" },
			{ "operations", "writeStatisticsToFile", "Write Statistics to File" },
			{ "operations", "writeMentionsToFile", "Write Mentions to File" },
			{ "operations", "writeMentionsToFileMatchHidden",
					"Write Mentions to File (Match Hidden)" },
					// 5/7/2018:  For writing unadjusted count file used by 
					// MoonstoneEHostFinalAdjudication class methods.
			{ "operations", "writeUnadjudicatedVariableMatchCounts",
					"Write Unadjudicated Variable Match Counts" }, };

	public WBGUI(Analysis analysis, EvaluationWorkbench wb, boolean visible)
			throws Exception {
		WorkbenchGUI = this;
		if (analysis == null) {
			analysis = new Analysis(this, wb);
		}
		if (wb != null) {
			this.arrtool = wb;
			// this.properties = wb.getStartupParameters().getProperties();
		}
		this.analysis = analysis;
		analysis.setWorkbenchGUI(this);

		if (KnowledgeEngine.getCurrentKnowledgeEngine().getStartupParameters()
				.isPropertyTrue("CreateWorkbenchGUI")) {
			this.initializeGraphics(visible);
		}
	}

	public void initializeGraphics(boolean visible) throws Exception {
		Dimension d = null;

		d = new Dimension(1200, 800);
		this.setPreferredSize(d);
		this.setMinimumSize(d);

		this.statisticsPanel = new StatisticsPanel(this.analysis);
		JScrollPane statjsp = new JScrollPane(this.statisticsPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// d = new Dimension(500, 400);
		d = new Dimension(500, 300);
		statjsp.setMinimumSize(d);
		statjsp.setPreferredSize(d);

		this.annotationTypeTabbedPane = new JTabbedPane();
		// d = new Dimension(1000, 400);
		d = new Dimension(1000, 300);
		JScrollPane jsp = new JScrollPane(this.annotationTypeTabbedPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setPreferredSize(d);
		jsp.setMinimumSize(d);

		this.annotationPanel = new AnnotationPanel(this.analysis);
		d = new Dimension(600, 300);
		this.annotationPanel.setMinimumSize(d);

		JScrollPane annjsp = new JScrollPane(this.annotationPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		d = new Dimension(400, 400);
		jsp.setMinimumSize(d);
		jsp.setPreferredSize(d);

		this.attributePanel = new AttributePanel(this.analysis);
		d = new Dimension(400, 200);
		this.attributePanel.setMinimumSize(d);

		JScrollPane attrjsp = new JScrollPane(this.attributePanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setMinimumSize(d);
		jsp.setPreferredSize(d);

		JTabbedPane attrtpane = new JTabbedPane();
		attrtpane.addTab("Attributes", attrjsp);

		JSplitPane arp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, annjsp,
				attrtpane);

		this.annotationTypeTabbedPane.addTab("Annotations", arp);

		d = new Dimension(400, 400);
		this.constraintPanel = new ConstraintPanel(this.analysis);
		JScrollPane conjsp = new JScrollPane(this.constraintPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		d = new Dimension(400, 400);
		conjsp.setMinimumSize(d);
		conjsp.setPreferredSize(d);
		this.annotationTypeTabbedPane.addTab("Queries", conjsp);

		this.typePanel = new TypePanel(this.analysis);
		JScrollPane typejsp = new JScrollPane(this.typePanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		typejsp.setMinimumSize(d);
		typejsp.setPreferredSize(d);
		this.annotationTypeTabbedPane.addTab("Types", typejsp);

		this.relationPanel = new RelationPanel(this.analysis);
		JScrollPane reljsp = new JScrollPane(this.relationPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		d = new Dimension(900, 400);
		reljsp.setMinimumSize(d);
		reljsp.setPreferredSize(d);
		this.annotationTypeTabbedPane.addTab("Relations", reljsp);

		d = new Dimension(900, 400);
		JSplitPane statsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				statjsp, this.annotationTypeTabbedPane);
		statsp.setMinimumSize(d);
		statsp.setPreferredSize(d);

		this.primaryDocumentPanel = new DocumentPanel(analysis,
				analysis.getPrimaryAnnotator());
		this.secondaryDocumentPanel = new DocumentPanel(analysis,
				analysis.getSecondaryAnnotator());
		JSplitPane dsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				this.primaryDocumentPanel, this.secondaryDocumentPanel);

		d = new Dimension(1000, 4000);

		dsp.setMinimumSize(d);
		dsp.setPreferredSize(d);

		JScrollPane docjsp = new JScrollPane(dsp,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		d = new Dimension(1000, 500);
		docjsp.setMinimumSize(d);
		docjsp.setPreferredSize(d);

		JSplitPane wbsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statsp,
				docjsp);
		d = new Dimension(1200, 1000);
		wbsp.setMinimumSize(d);
		wbsp.setPreferredSize(d);
		this.add(wbsp);

		this.frame = new JFrame();
		this.frame
				.setJMenuBar(createMenuBar(menuInfo, menuItemInfo, this, this));
		this.frame.setContentPane(this);
		this.frame.pack();
		this.frame.setVisible(visible);
	}

	private void setTitle() {
		String str = "";
		if (this.analysis.getSelectedDocument() != null) {
			str += "Document=" + this.analysis.getSelectedDocument().getName()
					+ ":";
		}
		if (this.analysis.getSelectedConstraintPacket() != null) {
			str += "MatchCriterion="
					+ this.analysis.getSelectedConstraintPacket().getName();
		}
		this.frame.setTitle(str);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String cmd = e.getActionCommand();

		if ("printStatistics".equals(cmd)) {
			this.statisticsPanel.printStatisticsToTerminal();
		} else if ("writeStatisticsToFile".equals(cmd)) {
			this.statisticsPanel.writeStatisticsXMLToFile();
		} else if ("writeMentionsToFile".equals(cmd)) {
			// this.getAnalysis().getSelectedConstraintMatch()
			// .writeSpreadsheetFile();
		} else if ("writeMentionsToFileMatchHidden".equals(cmd)) {
			this.getAnalysis().getSelectedConstraintMatch()
					.writeSpreadsheetFileMatchHidden();
		}
	}

	private static JMenuBar createMenuBar(String[][] menuinfo,
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

	private boolean firingAllDataUpdates = false;

	public void fireAllDataUpdates() {
		if (!firingAllDataUpdates) {
			try {
				firingAllDataUpdates = true;
				this.statisticsPanel.getModel().fireTableDataChanged();
				this.primaryDocumentPanel.highlightSentences();
				this.secondaryDocumentPanel.highlightSentences();
				this.primaryDocumentPanel.setCaretPosition(this.analysis
						.getSelectedAnnotation());
				this.secondaryDocumentPanel.setCaretPosition(this.analysis
						.getSelectedAnnotation());
				this.annotationPanel.getTableModel().fireTableDataChanged();
				this.annotationPanel.doExternalSelection();
				this.attributePanel.getModel().fireTableDataChanged();
				this.relationPanel.getModel().fireTableDataChanged();
				this.setTitle();
				firingAllDataUpdates = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Analysis getAnalysis() {
		return analysis;
	}

	public StatisticsPanel getStatisticsPanel() {
		return statisticsPanel;
	}

	public AnnotationPanel getAnnotationPanel() {
		return annotationPanel;
	}

	public DocumentPanel getPrimaryDocumentPanel() {
		return primaryDocumentPanel;
	}

	public DocumentPanel getSecondaryDocumentPanel() {
		return secondaryDocumentPanel;
	}

	public ConstraintPanel getConstraintPanel() {
		return constraintPanel;
	}

	// public Properties getProperties() {
	// return properties;
	// }

	public EvaluationWorkbench getEvaluationWorkbench() {
		return this.arrtool;
	}

	public TypePanel getTypePanel() {
		return typePanel;
	}

	public RelationPanel getRelationPanel() {
		return relationPanel;
	}

	public AttributePanel getAttributePanel() {
		return attributePanel;
	}

	public boolean isFiringAllDataUpdates() {
		return firingAllDataUpdates;
	}

	public void setFiringAllDataUpdates(boolean firingAllDataUpdates) {
		this.firingAllDataUpdates = firingAllDataUpdates;
	}

}
