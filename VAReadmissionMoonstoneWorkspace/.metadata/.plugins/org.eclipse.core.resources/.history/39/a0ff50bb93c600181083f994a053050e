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
package tsl.startup;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Properties;

import tsl.error.TSLException;

public class StartupParameters {

	private Properties properties = null;
	private String rootDirectory = null;
	private String ruleDirectory = null;
	private String resourceDirectory = null;
	private String KBDirectory = null;
	private String regExpPatternDirectory = null;
	private static String RuleDirectoryName = "GrammarRules";
	private static String ResourceDirectoryName = "resources";
	private static String RegExprDirectoryName = "RegExp";
	private static String KBDirectoryName = "OnyxKBFiles";

	public StartupParameters(String propertyFileName) throws TSLException {
		readProperties(propertyFileName);
	}

	// private void readProperties() throws TSLException {
	// readProperties(PropertyFileName);
	// }

	private void readProperties(String propertyFileName) throws TSLException {
		this.properties = new Properties();
		File file = new File(propertyFileName);
		if (!file.exists()) {
			file = null;
		}
		URL url = this.getClass().getResource("/" + propertyFileName);
		if (file == null) {
			if (url != null) {
				file = new File(url.getFile());
			} else {
				file = new File("./" + propertyFileName);
			}
		}
		if (!file.exists()) {
			throw new TSLException("Parameter file not found");
		}
		this.rootDirectory = file.getParentFile().getAbsolutePath();
		try {
			this.properties.load(new FileReader(file));
		} catch (Exception e) {
			throw new TSLException("Unable to load parameter file");
		}
		this.ruleDirectory = this.rootDirectory + File.separatorChar
				+ RuleDirectoryName;
		this.resourceDirectory = this.rootDirectory + File.separatorChar
				+ ResourceDirectoryName;
		this.KBDirectory = this.rootDirectory + File.separatorChar
				+ KBDirectoryName;
		this.regExpPatternDirectory = this.rootDirectory + File.separatorChar
				+ RegExprDirectoryName;
		if (!(new File(this.ruleDirectory).isDirectory())
				|| !(new File(this.resourceDirectory).isDirectory())
				|| !(new File(this.KBDirectory).isDirectory())
				|| !(new File(this.regExpPatternDirectory).isDirectory())) {
			throw new TSLException("One of the main directories does not exist");
		}
		if (!(new File(this.ruleDirectory).isDirectory())) {
			throw new TSLException("Rule directory does not exist");
		}
		if (!(new File(this.resourceDirectory).isDirectory())) {
			throw new TSLException("Resource directory does not exist");
		}
		if (!(new File(this.KBDirectory).isDirectory())) {
			throw new TSLException("KB directory does not exist");
		}
		if (!(new File(this.regExpPatternDirectory).isDirectory())) {
			throw new TSLException("RegExp directory does not exist");
		}
	}

	public Properties getProperties() {
		return this.properties;
	}

	public String getPropertyValue(String property) {
		return this.properties.getProperty(property);
	}

	public void setPropertyValue(String property, Object value) {
		this.properties.put(property, value);
	}

	public boolean isPropertyTrue(String property) {
		Object value = this.properties.getProperty(property);
		if (value != null && "true".equals(value.toString().toLowerCase())) {
			return true;
		}
		return false;
	}

	public String getFileName(String fname) {
		String fstr = null;
		fstr = this.rootDirectory + File.separatorChar + fname;
		if (new File(fstr).exists()) {
			return fstr;
		}
		return null;
	}

	public String getFileName(String dname, String value) {
		String fstr = null;
		fstr = dname + File.separatorChar + value;
		if (new File(fstr).exists()) {
			return fstr;
		}
		String property = this.getPropertyValue(value);
		if (property != null && !property.equals(value)) {
			return getFileName(dname, property);
		}
		return null;
	}

	public String getResourceFileName(String value) {
		return getFileName(this.resourceDirectory, value);
	}

	public String getRuleFileName(String value) {
		return getFileName(this.ruleDirectory, value);
	}

	public String getRootFileName(String value) {
		return getFileName(this.rootDirectory, value);
	}

	public String getRootDirectory() {
		return rootDirectory;
	}

	public String getRuleDirectory() {
		return ruleDirectory;
	}

	public String getResourceDirectory() {
		return resourceDirectory;
	}

	public String getKBDirectory() {
		return KBDirectory;
	}

	public String getRegExpPatternDirectory() {
		return regExpPatternDirectory;
	}

}
