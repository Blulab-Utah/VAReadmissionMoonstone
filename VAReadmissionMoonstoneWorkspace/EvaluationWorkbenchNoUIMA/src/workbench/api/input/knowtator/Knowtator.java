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

import workbench.api.Analysis;

public class Knowtator {

	public static KnowtatorIO createKnowtatorIO(Analysis analysis) {
		KnowtatorIO kio = new KnowtatorIO(analysis, null, null);
		return kio;
	}

	public static void readSchema(KnowtatorIO kio, String str, boolean isXML)
			throws Exception {
		kio.extractSchema(str, isXML);
	}

	public static void readAnnotationFile(KnowtatorIO kio, String docname, String fstr,
			boolean isXML) throws Exception {
		if (isXML) {
			kio.setXMLFormat(KnowtatorIO.SHARPXMLFormat);
			kio.extractAnnotationsFromXMLString(docname, fstr);
			kio.resolveReferences();
			kio.clearSimpleInstances();
		} else {
			kio.extractAnnotationsFromPinsFileString(fstr);
		}
	}

	public static void postProcess(KnowtatorIO kio, Analysis analysis)
			throws Exception {
		kio.createWorkbenchAnnotations(analysis);
		
		// I don't use Document objects, so no need to attach to ACs?
//		analysis.attachDocumentsToKnowtatorAnnotationCollections(analysis
//				.getPrimaryAnnotator());
//		analysis.attachDocumentsToKnowtatorAnnotationCollections(analysis
//				.getSecondaryAnnotator());
	}
	
}
