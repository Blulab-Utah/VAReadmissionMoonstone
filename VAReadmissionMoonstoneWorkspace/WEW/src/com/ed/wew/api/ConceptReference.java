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

/**
 * A reference to Concept object. Will be used as a key in Maps.
 * 
 * 
 * Later might be extended to connect to Concept object from Knowledge Author.
 * 
 * @author Andrey Santrosyan
 * 
 */
public interface ConceptReference extends Serializable {
    public String getCUI();
    public String getName();

}
