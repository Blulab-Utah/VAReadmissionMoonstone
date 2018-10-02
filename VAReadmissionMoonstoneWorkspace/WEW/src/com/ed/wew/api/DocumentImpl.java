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
package com.ed.wew.api;

import java.io.Reader;
import java.util.Comparator;

@Deprecated
public class DocumentImpl implements DocumentReference {
    private Reader reader;
    private String name;
    private String text;

    // Lee
    public DocumentImpl() {}

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Reader createReader() {
        return reader;
    }

    public void setReader(final Reader reader) {
        this.reader = reader;
    }
    
    public static class DocumentImplSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			DocumentImpl a1 = (DocumentImpl) o1;
			DocumentImpl a2 = (DocumentImpl) o2;
			return a1.getName().compareTo(a2.getName());
		}
	}

}
