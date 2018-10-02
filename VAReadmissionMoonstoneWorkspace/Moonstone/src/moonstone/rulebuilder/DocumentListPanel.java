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
package moonstone.rulebuilder;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tsl.documentanalysis.document.Document;

public class DocumentListPanel extends JPanel implements ListSelectionListener {

	Vector<Document> documents = null;
	DefaultListModel listModel = null;
	JList jlist = null;
	MoonstoneRuleInterface msri = null;
	JFrame frame = null;

	public DocumentListPanel(MoonstoneRuleInterface msri,
			Vector<Document> documents) {
		this.msri = msri;
		listModel = new DefaultListModel();
		this.documents = documents;
		this.jlist = new JList(listModel);
		for (Document d : documents) {
			String dname = d.getName();
			listModel.addElement(dname);
		}
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlist.setSelectedIndex(0);
		jlist.addListSelectionListener(this);
		JScrollPane jsp = new JScrollPane(jlist);
		
		Dimension d = new Dimension(400, 200);
		jsp.setMinimumSize(d);
		jsp.setPreferredSize(d);
		jsp.setVisible(true);
		
		this.add(jsp);
		
		this.frame = new JFrame();
		this.frame.setContentPane(this);
		this.frame.pack();
		this.frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
	}
	
	public void valueChanged(ListSelectionEvent e) {
		int index = jlist.getSelectedIndex();
		Document d = this.documents.elementAt(index);
		msri.setDisplayedDocument(d);
		this.frame.dispose();
	}

	public JFrame getFrame() {
		return frame;
	}

}
