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
package workbench.api.input.knowtator;

public class KTRelation extends KTSimpleInstance {

	KTClass relation = null;
	KTAnnotation firstArgument = null;
	KTAnnotation secondArgument = null;

	public KTRelation(KTClass relation, KTAnnotation firstArgument,
			KTAnnotation secondArgument) throws Exception {
		this.relation = relation;
		this.firstArgument = firstArgument;
		this.secondArgument = secondArgument;
		firstArgument.addRelation(this);
	}
	
	public String toString() {
		String rstr = this.relation.name;
		String fastr = firstArgument.getText();
		String sastr = secondArgument.getText();
		String str = "<" + rstr + "=" + fastr + "," + sastr + ">";
		return str;
	}
}
