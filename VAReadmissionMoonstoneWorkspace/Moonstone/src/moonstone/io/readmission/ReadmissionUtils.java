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
package moonstone.io.readmission;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import tsl.utilities.FUtils;
import tsl.utilities.VUtils;
import workbench.api.Analysis;
import workbench.api.annotation.Annotation;
import moonstone.rulebuilder.MoonstoneRuleInterface;

public class ReadmissionUtils {

	public static void storeAllAnnotationSnippets(MoonstoneRuleInterface msri) {
		Hashtable<String, Vector<String>> shash = new Hashtable();
		Analysis analysis = msri.getWorkbench().getAnalysis();
		for (Annotation annotation : analysis.getAllAnnotations()) {
			String str = annotation.getText();
			String c = annotation.getName();
			VUtils.pushIfNotHashVector(shash, c, str);
		}
		StringBuffer sb = new StringBuffer();
		for (String c : shash.keySet()) {
			Vector<String> v = shash.get(c);
			if (v != null) {
				sb.append("\nCLASS= " + c + "\n");
				for (String str : v) {
					sb.append("\t" + str + "\n");
				}
			}
		}
		String fpath = msri.getResourceDirectoryName() + File.separatorChar
				+ "AllAnnotationSnippets";
		FUtils.writeFile(fpath, sb.toString());
	}

}
