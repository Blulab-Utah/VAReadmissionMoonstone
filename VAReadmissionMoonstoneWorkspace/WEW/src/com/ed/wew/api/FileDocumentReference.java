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

import java.io.*;

public class FileDocumentReference implements DocumentReference {
    private String directory;
    private String name;
    private static final String sep = "/";

    public FileDocumentReference(final String fullPath) {

        name = fullPath.substring(fullPath.lastIndexOf(FileDocumentReference.sep) + 1);
        directory = fullPath.substring(0, fullPath.lastIndexOf(FileDocumentReference.sep));
    }

    public FileDocumentReference(final String directory, final String fileName) {
        this.directory = directory;
        name = fileName;
    }

    @Override
    public Reader createReader() {
        try {
            return new FileReader(directory + FileDocumentReference.sep + name);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDirectory() {
        return directory;
    }

    // public void setDirectory(final String directory) {
    // this.directory = directory;
    // }

    @Override
    public String getName() {
        return name;
    }

    // public void setName(final String name) {
    // this.name = name;
    // }

}
