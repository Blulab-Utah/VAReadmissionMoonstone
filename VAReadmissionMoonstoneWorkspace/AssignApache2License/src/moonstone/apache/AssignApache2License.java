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
package moonstone.apache;

import java.io.File;
import java.util.Hashtable;

import tsl.utilities.FUtils;

public class AssignApache2License {
	
	// 8/6/2018
	public static void main(String[] strs) {
		String ldir = "/Users/leechristensen/Desktop/MoonstoneDemoVersions/MoonstoneDemo/resources/Apache2License.txt";
		String sdir = "/Users/leechristensen/Desktop/READMISSION/VAReadmissionMoonstone_9_25_2018/VAReadmissionMoonstoneWorkspace";
		addApache2LicenseToSourceFiles(ldir, sdir);
	}

	public static void addApache2LicenseToSourceFiles(String licensefilestr, String sourcedirstr) {
		Hashtable<String, String> fhash = new Hashtable<String, String>();
		if (sourcedirstr != null && licensefilestr != null) {
			String license = FUtils.readFile(licensefilestr);
			File sourcedir = new File(sourcedirstr);
			if (sourcedir.exists() && sourcedir.isDirectory()) {
				File[] files = sourcedir.listFiles();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					String fpath = file.getAbsolutePath();
					if (file.isFile() && fpath.endsWith(".java")) {
						String text = FUtils.readFile(file);
						String alltext = "/*\n" + license + "*/\n" + text;
						fhash.put(fpath, alltext);
					} else if (file.isDirectory()) {
						addApache2LicenseToSourceFiles(licensefilestr, fpath);
					}
				}
			}
		}
		for (String fpath : fhash.keySet()) {
			String ftext = fhash.get(fpath);
			FUtils.writeFile(fpath, ftext);
		}
	}

}
