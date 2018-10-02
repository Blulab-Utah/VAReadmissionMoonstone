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

public class ConceptImpl implements ConceptReference {

    private String cui = null;
    private String name = null;

    // Lee
    public ConceptImpl(final String name, final String cui) {
        this.name = name;
        this.cui = cui;
    }

    @Override
    public String getCUI() {
        return cui;
    }

    public void setCui(final String cui) {
        this.cui = cui;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String str = "(Concept: Name=" + name + ",CUI=" + cui + ")";
        return str;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        String aThis = name;
        String aThat = ((ConceptReference)obj).getName();
        return aThis == null ? aThat == null : aThis.equals(aThat);
    }

}
