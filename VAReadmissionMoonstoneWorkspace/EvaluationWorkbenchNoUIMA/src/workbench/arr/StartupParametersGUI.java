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
package workbench.arr;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;
import workbench.api.gui.WBGUI;

public class StartupParametersGUI extends JPanel implements ActionListener {
	JFrame frame = null;
	public EvaluationWorkbench arrTool = null;
	Container pane = null;
	public StartupParameters startupParameters = null;
	JTextField workbenchLocationText = null;
	JTextField firstAnnotatorText = null;
	JTextField secondAnnotatorText = null;
	JComboBox firstAnnotatorTypeCB = null;
	JComboBox secondAnnotatorTypeCB = null;
	JTextField textCorpusDirectoryText = null;
	JButton textCorpusDirectorySelectButton = null;
	JTextField firstInputAnnotationDirectoryText = null;
	JButton firstInputAnnotationDirectorySelectButton = null;
	JTextField secondInputAnnotationDirectoryText = null;
	JButton secondInputAnnotationDirectorySelectButton = null;
	JTextField firstPINSFileText = null;
	JButton firstPINSFileSelectButton = null;
	JTextField secondPINSFileText = null;
	JButton secondPINSFileSelectButton = null;
	JTextField annotationOutputDirectoryText = null;
	JButton annotationOutputDirectorySelectButton = null;
	JTextField knowtatorSchemaFileText = null;
	JButton knowtatorSchemaFileSelectButton = null;
	JTextField validateFileDirectoryText = null;
	JButton validateFileDirectorySelectButton = null;
	JComboBox visualClassificationComboBox = null;
	JTextField defaultClassificationLabelText = null;
	public static StartupParametersGUI SPGUI = null;
	public static WBGUI WorkbenchGUI = null;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				try {
					SPGUI = new StartupParametersGUI(true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(new JFrame(),
							StrUtils.getStackTrace(e));
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});
	}

	public static EvaluationWorkbench startupEvaluationWorkbench(boolean visible) {
		try {
			SPGUI = new StartupParametersGUI(visible);
			SPGUI.arrTool.initializeFromStartupParameterGUI();
			return SPGUI.arrTool;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public StartupParametersGUI(boolean visible) {
		try {
			this.frame = new JFrame();
			this.arrTool = new EvaluationWorkbench();
			this.startupParameters = new StartupParameters(arrTool, true);
			addComponents();
			assignFieldsFromStartupParameters();
			setTitle();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setOpaque(true);
			frame.setContentPane(this);
			frame.pack();
			frame.setVisible(visible);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addComponents() {
		JPanel panel = null;

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		panel = new JPanel();
		JLabel label = new JLabel("Evaluation Workbench Location:");
		this.workbenchLocationText = new JTextField(50);
		this.workbenchLocationText.addActionListener(this);
		panel.add(label);
		panel.add(this.workbenchLocationText);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("First annotator name:");
		firstAnnotatorText = new JTextField(10);
		firstAnnotatorText.addActionListener(this);
		panel.add(label);
		panel.add(firstAnnotatorText);
		label = new JLabel("Second annotator name:");
		secondAnnotatorText = new JTextField(10);
		secondAnnotatorText.addActionListener(this);
		panel.add(label);
		panel.add(secondAnnotatorText);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("First annotator type:");
		firstAnnotatorTypeCB = new JComboBox(StartupParameters.AnnotatorTypes);
		firstAnnotatorTypeCB.addActionListener(this);
		panel.add(label);
		panel.add(firstAnnotatorTypeCB);
		label = new JLabel("Second annotator type:");
		secondAnnotatorTypeCB = new JComboBox(StartupParameters.AnnotatorTypes);
		secondAnnotatorTypeCB.addActionListener(this);
		panel.add(label);
		panel.add(secondAnnotatorTypeCB);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("Document Corpus Directory:");
		this.textCorpusDirectoryText = new JTextField(80);
		this.textCorpusDirectoryText.addActionListener(this);
		this.textCorpusDirectorySelectButton = new JButton("Select");
		this.textCorpusDirectorySelectButton.addActionListener(this);
		panel.add(label);
		panel.add(this.textCorpusDirectoryText);
		panel.add(this.textCorpusDirectorySelectButton);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("First Annotation Directory:");
		this.firstInputAnnotationDirectoryText = new JTextField(80);
		this.firstInputAnnotationDirectoryText.addActionListener(this);
		this.firstInputAnnotationDirectorySelectButton = new JButton("Select");
		firstInputAnnotationDirectorySelectButton.addActionListener(this);
		panel.add(label);
		panel.add(this.firstInputAnnotationDirectoryText);
		panel.add(firstInputAnnotationDirectorySelectButton);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("Second Annotation Directory:");
		this.secondInputAnnotationDirectoryText = new JTextField(80);
		this.secondInputAnnotationDirectoryText.addActionListener(this);
		this.secondInputAnnotationDirectorySelectButton = new JButton("Select");
		this.secondInputAnnotationDirectorySelectButton.addActionListener(this);
		panel.add(label);
		panel.add(this.secondInputAnnotationDirectoryText);
		panel.add(this.secondInputAnnotationDirectorySelectButton);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("First Knowtator PINS File:");
		this.firstPINSFileText = new JTextField(80);
		this.firstPINSFileText.addActionListener(this);
		this.firstPINSFileSelectButton = new JButton("Select");
		this.firstPINSFileSelectButton.addActionListener(this);
		panel.add(label);
		panel.add(this.firstPINSFileText);
		panel.add(this.firstPINSFileSelectButton);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("Second Knowtator PINS File:");
		this.secondPINSFileText = new JTextField(80);
		this.secondPINSFileText.addActionListener(this);
		this.secondPINSFileSelectButton = new JButton("Select");
		this.secondPINSFileSelectButton.addActionListener(this);
		panel.add(label);
		panel.add(this.secondPINSFileText);
		panel.add(this.secondPINSFileSelectButton);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("Annotation Output Directory:");
		this.annotationOutputDirectoryText = new JTextField(80);
		this.annotationOutputDirectoryText.addActionListener(this);
		this.annotationOutputDirectorySelectButton = new JButton("Select");
		this.annotationOutputDirectorySelectButton.addActionListener(this);
		panel.add(label);
		panel.add(this.annotationOutputDirectoryText);
		panel.add(this.annotationOutputDirectorySelectButton);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("Knowtator Schema File:");
		this.knowtatorSchemaFileText = new JTextField(80);
		this.knowtatorSchemaFileText.addActionListener(this);
		this.knowtatorSchemaFileSelectButton = new JButton("Select");
		this.knowtatorSchemaFileSelectButton.addActionListener(this);
		panel.add(label);
		panel.add(this.knowtatorSchemaFileText);
		panel.add(this.knowtatorSchemaFileSelectButton);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("Adjudication Directory:");
		this.validateFileDirectoryText = new JTextField(80);
		this.validateFileDirectoryText.addActionListener(this);
		this.validateFileDirectorySelectButton = new JButton("Select");
		this.validateFileDirectorySelectButton.addActionListener(this);
		panel.add(label);
		panel.add(this.validateFileDirectoryText);
		panel.add(this.validateFileDirectorySelectButton);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		label = new JLabel("Classification Labels:");
		this.defaultClassificationLabelText = new JTextField(80);
		panel.add(label);
		panel.add(this.defaultClassificationLabelText);
		this.add(panel, c);

		c.gridy++;
		panel = new JPanel();
		JButton button = new JButton("Exit");
		button.addActionListener(this);
		panel.add(button);
		button = new JButton("Read");
		button.addActionListener(this);
		panel.add(button);
		button = new JButton("Save");
		button.addActionListener(this);
		panel.add(button);
		button = new JButton("Initialize Workbench");
		button.addActionListener(this);
		panel.add(button);

		this.add(panel, c);
	}

	public void assignFieldsFromStartupParameters() throws Exception {
		this.workbenchLocationText
				.setText(this.startupParameters.workbenchDirectory);
		firstAnnotatorText.setText(this.startupParameters.firstAnnotatorName);
		secondAnnotatorText.setText(this.startupParameters.secondAnnotatorName);
		if (this.startupParameters.inputTypeFirstAnnotator != null) {
			firstAnnotatorTypeCB
					.setSelectedItem(this.startupParameters.inputTypeFirstAnnotator);
		}
		if (this.startupParameters.inputTypeSecondAnnotator != null) {
			secondAnnotatorTypeCB
					.setSelectedItem(this.startupParameters.inputTypeSecondAnnotator);
		}
		this.textCorpusDirectoryText
				.setText(this.startupParameters.textInputDirectory);
		this.firstInputAnnotationDirectoryText
				.setText(this.startupParameters.annotationInputDirectoryFirstAnnotator);
		this.secondInputAnnotationDirectoryText
				.setText(this.startupParameters.annotationInputDirectorySecondAnnotator);
		this.firstPINSFileText
				.setText(this.startupParameters.knowtatorPinsFileFirstAnnotator);
		this.secondPINSFileText
				.setText(this.startupParameters.knowtatorPinsFileSecondAnnotator);
		this.annotationOutputDirectoryText
				.setText(this.startupParameters.annotationOutputDirectory);
		this.knowtatorSchemaFileText
				.setText(this.startupParameters.knowtatorSchemaFile);
		this.validateFileDirectoryText
				.setText(this.startupParameters.validationFile);
		if (this.arrTool.getTypeSystem() != null) {
			String cstr = this.arrTool.getTypeSystem()
					.getDelimitedDefaultClassificationPropertyString();
			if (cstr != null) {
				this.defaultClassificationLabelText.setText(cstr);
			}
		}
		this.defaultClassificationLabelText
				.setText(this.startupParameters.defaultClassificationPropertyNames);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			String cmd = e.getActionCommand();
			Object source = e.getSource();
			String bdir = this.startupParameters.getWorkbenchDirectory();
			String text = null;
			if (source instanceof JTextField) {
				text = ((JTextField) source).getText();
			}
			if (source instanceof JComboBox) {
				JComboBox cb = (JComboBox) source;
				String type = (String) cb.getSelectedItem();
				if (cb.equals(firstAnnotatorTypeCB)) {
					this.startupParameters.inputTypeFirstAnnotator = type;
				} else if (cb.equals(secondAnnotatorTypeCB)) {
					this.startupParameters.inputTypeSecondAnnotator = type;
				}
			} else if (source.equals(this.firstAnnotatorText)) {
				this.startupParameters.firstAnnotatorName = text;
			} else if (source.equals(this.secondAnnotatorText)) {
				this.startupParameters.secondAnnotatorName = text;
			} else if (source.equals(this.textCorpusDirectoryText)) {
				this.startupParameters.textInputDirectory = this.textCorpusDirectoryText
						.getText();
			} else if (source.equals(this.textCorpusDirectorySelectButton)) {
				File dir = FUtils.chooseDirectory(
						this.startupParameters.textInputDirectory, bdir);
				if (dir != null) {
					this.startupParameters.textInputDirectory = dir
							.getAbsolutePath();
					this.textCorpusDirectoryText
							.setText(this.startupParameters.textInputDirectory);
				}
			} else if (source.equals(this.firstInputAnnotationDirectoryText)) {
				this.startupParameters.annotationInputDirectoryFirstAnnotator = this.firstInputAnnotationDirectoryText
						.getText();
			} else if (source
					.equals(this.firstInputAnnotationDirectorySelectButton)) {
				File dir = FUtils
						.chooseDirectory(
								this.startupParameters.annotationInputDirectoryFirstAnnotator,
								bdir);
				if (dir != null) {
					this.startupParameters.annotationInputDirectoryFirstAnnotator = dir
							.getAbsolutePath();
					this.firstInputAnnotationDirectoryText
							.setText(this.startupParameters.annotationInputDirectoryFirstAnnotator);
				}
			} else if (source.equals(this.secondInputAnnotationDirectoryText)) {
				this.startupParameters.annotationInputDirectorySecondAnnotator = this.secondInputAnnotationDirectoryText
						.getText();
			} else if (source
					.equals(this.secondInputAnnotationDirectorySelectButton)) {
				File dir = FUtils
						.chooseDirectory(
								this.startupParameters.annotationInputDirectorySecondAnnotator,
								bdir);
				if (dir != null) {
					this.startupParameters.annotationInputDirectorySecondAnnotator = dir
							.getAbsolutePath();
					this.secondInputAnnotationDirectoryText
							.setText(this.startupParameters.annotationInputDirectorySecondAnnotator);
				}
			} else if (source.equals(this.firstPINSFileText)) {
				this.startupParameters.knowtatorPinsFileFirstAnnotator = this.firstPINSFileText
						.getText();
			} else if (source.equals(this.firstPINSFileSelectButton)) {
				File file = FUtils.chooseFile(
						this.startupParameters.knowtatorPinsFileFirstAnnotator,
						bdir, "Choose PINS file:");
				if (file != null) {
					this.startupParameters.knowtatorPinsFileFirstAnnotator = file
							.getAbsolutePath();
					this.firstPINSFileText
							.setText(this.startupParameters.knowtatorPinsFileFirstAnnotator);
				}
			} else if (source.equals(this.secondPINSFileText)) {
				this.startupParameters.knowtatorPinsFileSecondAnnotator = this.secondPINSFileText
						.getText();
			} else if (source.equals(this.secondPINSFileSelectButton)) {
				File file = FUtils
						.chooseFile(
								this.startupParameters.knowtatorPinsFileSecondAnnotator,
								bdir, "Choose PINS file:");
				if (file != null) {
					this.startupParameters.knowtatorPinsFileSecondAnnotator = file
							.getAbsolutePath();
					this.secondPINSFileText
							.setText(this.startupParameters.knowtatorPinsFileSecondAnnotator);
				}
			} else if (source.equals(this.annotationOutputDirectoryText)) {
				this.startupParameters.annotationOutputDirectory = this.annotationOutputDirectoryText
						.getText();
			} else if (source
					.equals(this.annotationOutputDirectorySelectButton)) {
				File file = FUtils.chooseDirectory(
						this.startupParameters.annotationOutputDirectory, bdir);
				if (file != null) {
					this.startupParameters.annotationOutputDirectory = file
							.getAbsolutePath();
					this.annotationOutputDirectoryText
							.setText(this.startupParameters.annotationOutputDirectory);
				}
			} else if (source.equals(this.knowtatorSchemaFileText)) {
				this.startupParameters.knowtatorSchemaFile = this.knowtatorSchemaFileText
						.getText();
			} else if (source.equals(this.knowtatorSchemaFileSelectButton)) {
				File file = FUtils.chooseFile(
						this.startupParameters.knowtatorSchemaFile, bdir,
						"Choose Knowtator Schema File:");
				if (file != null) {
					this.startupParameters.knowtatorSchemaFile = file
							.getAbsolutePath();
					this.knowtatorSchemaFileText
							.setText(this.startupParameters.knowtatorSchemaFile);
				}
			} else if (source.equals(this.validateFileDirectoryText)) {
				this.startupParameters.validationFile = this.validateFileDirectoryText
						.getText();
			} else if (source.equals(validateFileDirectorySelectButton)) {
				File file = FUtils.chooseDirectory(
						this.startupParameters.validationFile, bdir);
				if (file != null) {
					this.startupParameters.validationFile = file
							.getAbsolutePath();
					this.validateFileDirectoryText
							.setText(this.startupParameters.validationFile);
				}
			} else if (source.equals(this.defaultClassificationLabelText)) {
				String str = this.defaultClassificationLabelText.getText();
				this.startupParameters.defaultClassificationPropertyNames = str;
				Vector<String> v = StrUtils.stringList(str, ',');
				if (v != null && v.size() > 0) {
					arrTool.typeSystem.setDefaultClassificationProperties(v);
				}
			} else if ("Exit".equals(cmd)) {
				System.exit(0);
			} else if ("Read".equals(cmd)) {
				File file = FUtils.chooseFile(
						this.startupParameters.workbenchDirectory,
						"Parameter File");
				if (file != null) {
					this.startupParameters.startupPropertiesFilename = file
							.getName();
					this.startupParameters.readParameters();
					this.assignFieldsFromStartupParameters();
					this.setTitle();
				}
			} else if ("Save".equals(cmd)) {
				this.startupParameters.storeParameterProperties();
				this.startupParameters.writeParametersToStartupFile();
			} else if ("Initialize Workbench".equals(cmd)) {
				String parameterErrors = this.startupParameters.getErrors();
				if (parameterErrors != null) {
					JOptionPane.showMessageDialog(this.frame, parameterErrors);
				} else {
					this.startupParameters.storeParameterProperties();
					this.arrTool.initializeFromStartupParameterGUI();

//					if (WorkbenchGUI == null) {
//						WorkbenchGUI = new WBGUI(null, this.arrTool);
//					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	void setTitle() {
		String title = "Evaluation Workbench:  Parameter File = \""
				+ this.startupParameters.startupPropertiesFilename + "\"";
		this.frame.setTitle(title);
	}

	public JFrame getFrame() {
		return frame;
	}

}
