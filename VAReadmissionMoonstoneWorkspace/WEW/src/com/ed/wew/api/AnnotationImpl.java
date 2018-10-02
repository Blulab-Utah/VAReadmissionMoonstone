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

public class AnnotationImpl implements Annotation {
    private String text;
    private String semanticType;
    private String classification;
    private int start;
    private int end;
    private DocumentReference documentReference;
    private ConceptReference conceptReference;
    private AnnotatorReference annotatorReference;

    // Lee
    public AnnotationImpl() {

    }

    public AnnotationImpl(final workbench.api.annotation.Annotation wba, final DocumentReference dr, final ConceptReference cr, final AnnotatorReference ar) {
        text = wba.getText();
        semanticType = wba.getType().getName();
        Object concept = wba.getClassificationValue();
        if (concept instanceof String) {
        	classification = (String) concept;
        }
        start = wba.getStart();
        end = wba.getEnd();
        documentReference = dr;
        conceptReference = cr;
        annotatorReference = ar;
        conceptReference = new ConceptImpl(classification, classification);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ed.wew.api.Annotation#getText()
     */
    @Override
    public String getText() {
        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ed.wew.api.Annotation#getSemanticType()
     */
    @Override
    public String getSemanticType() {
        return semanticType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ed.wew.api.Annotation#getClassification()
     */
    @Override
    public String getClassification() {
        return classification;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ed.wew.api.Annotation#getStart()
     */
    @Override
    public int getStart() {
        return start;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ed.wew.api.Annotation#getEnd()
     */
    @Override
    public int getEnd() {
        return end;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ed.wew.api.Annotation#getDocumentReference()
     */
    @Override
    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ed.wew.api.Annotation#getConceptReference()
     */
    @Override
    public ConceptReference getConceptReference() {
        return conceptReference;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ed.wew.api.Annotation#getAnnotatorReference()
     */
    @Override
    public AnnotatorReference getAnnotatorReference() {
        return annotatorReference;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public void setSemanticType(final String semanticType) {
        this.semanticType = semanticType;
    }

    public void setClassification(final String classification) {
        this.classification = classification;
    }

    public void setStart(final int start) {
        this.start = start;
    }

    public void setEnd(final int end) {
        this.end = end;
    }

    public void setDocumentReference(final DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    public void setConceptReference(final ConceptReference conceptReference) {
        this.conceptReference = conceptReference;
    }

    public void setAnnotatorReference(final AnnotatorReference annotatorReference) {
        this.annotatorReference = annotatorReference;
    }

    // Lee
    @Override
    public String toString() {
        // Text=\"" + this.getText() + ",
        String str =
                        "(AnnotationImpl: Type=" + this.getSemanticType() + ",Classification=" + this.getClassification() + ",Start=" + this.getStart()
                                        + ",End=" + this.getEnd() + ")";
        return str;

    }

}
