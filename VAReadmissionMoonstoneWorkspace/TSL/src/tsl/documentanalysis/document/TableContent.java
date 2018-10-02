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
package tsl.documentanalysis.document;

import java.util.Vector;
import tsl.utilities.VUtils;

public class TableContent extends HeaderContent {

	private Vector<AttributeValuePair> attributeValuePairs = null;

	public TableContent(Header header) {
		super(header);
	}
	
	public void readContent() {
		Document doc = this.header.getDocument();
		int tindex = this.header.getTextStartTokenIndex();
		doc.setTokenIndex(tindex);
		AttributeValuePair av = null;
		while (!this.header.atTextEnd()) {
			av = AttributeValuePair.getAttributeValuePair(this.header);
			if (av != null) {
				attributeValuePairs = VUtils.add(attributeValuePairs, av);
				tindex = doc.getTokenIndex();
			} else {
				doc.setTokenIndex(tindex++);
			}
		}
	}

	public Vector<AttributeValuePair> getAttributeValuePairs() {
		return attributeValuePairs;
	}


}
