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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import tsl.expression.form.sentence.Sentence;

public class TSLGUI extends JPanel {
	private JTextArea textArea = null;
	private Sentence sentence = null;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new TSLGUI();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});
	}

	public TSLGUI() {
		Dimension dim = new Dimension(1200, 600);
		this.textArea = new JTextArea();
		this.textArea.setMinimumSize(dim);
		this.textArea.setPreferredSize(dim);
		dim = new Dimension(600, 200);
		JScrollPane jsp = new JScrollPane(this.textArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setPreferredSize(dim);
		jsp.setMinimumSize(dim);
		this.add(jsp);
		
		JFrame frame = new JFrame();
		frame.setContentPane(this);
		frame.setTitle("TSL Sentence Editor");
		frame.pack();
		frame.setVisible(true);
	}

	public Sentence getSentence() {
		Sentence sentence = null;
		try {
			String text = this.textArea.getText();
			this.sentence = Sentence.createSentence(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.sentence;
	}
	
	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
		String tstr = sentence.toNewlinedString();
		this.textArea.setText(tstr);
	}
	

}
