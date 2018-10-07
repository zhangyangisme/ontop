package it.unibz.inf.ontop.spec;

/*
 * #%L
 * ontop-temporal
 * %%
 * Copyright (C) 2009 - 2018 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.serializer.TurtleWriter;

import java.util.ArrayList;
import java.util.List;

public class TemporalTargetQueryRenderer {

    /**
     * Transforms the given <code>OBDAQuery</code> into a string. The method requires
     * a prefix manager to shorten full IRI name.
     */
    public static String encode(ImmutableList<ImmutableFunctionalTerm> body, PrefixManager prefixManager) {

        TurtleWriter turtleWriter = new TurtleWriter();
        for (Function atom : body) {
            String subject, predicate, object;
            String originalString = atom.getFunctionSymbol().toString();
            if (isUnary(atom)) {
                Term subjectTerm = atom.getTerm(0);
                subject = getDisplayName(subjectTerm, prefixManager);
                predicate = "a";
                object = getAbbreviatedName(originalString, prefixManager, false);
                if (originalString.equals(object)) {
                    object = "<" + object + ">";
                }
            } else if (originalString.equals("triple")) {
                Term subjectTerm = atom.getTerm(0);
                subject = getDisplayName(subjectTerm, prefixManager);
                Term predicateTerm = atom.getTerm(1);
                predicate = getDisplayName(predicateTerm, prefixManager);
                Term objectTerm = atom.getTerm(2);
                object = getDisplayName(objectTerm, prefixManager);
            } else {
                Term subjectTerm = atom.getTerm(0);
                subject = getDisplayName(subjectTerm, prefixManager);
                predicate = getAbbreviatedName(originalString, prefixManager, false);
                if (originalString.equals(predicate)) {
                    predicate = "<" + predicate + ">";
                }
                Term objectTerm = atom.getTerm(1);
                object = getDisplayName(objectTerm, prefixManager);
            }
            turtleWriter.put(subject, predicate, object);
        }
        return turtleWriter.print();
    }

    /**
     * Checks if the atom is unary or not.
     */
    private static boolean isUnary(Function atom) {
        return atom.getArity() == 5;
    }

    /**
     * Prints the short form of the predicate (by omitting the complete URI and
     * replacing it by a prefix name).
     */
    private static String getAbbreviatedName(String uri, PrefixManager pm, boolean insideQuotes) {
        return pm.getShortForm(uri, insideQuotes);
    }

    private static String appendTerms(Term term) {
        if (term instanceof Constant) {
            String st = ((Constant) term).getValue();
            if (st.contains("{")) {
                st = st.replace("{", "\\{");
                st = st.replace("}", "\\}");
            }
            return st;
        } else {
            return "{" + ((Variable) term).getName() + "}";
        }
    }

    //Appends nested concats
    private static void getNestedConcats(StringBuilder stb, Term term1, Term term2) {
        if (term1 instanceof Function) {
            Function f = (Function) term1;
            getNestedConcats(stb, f.getTerms().get(0), f.getTerms().get(1));
        } else {
            stb.append(appendTerms(term1));
        }
        if (term2 instanceof Function) {
            Function f = (Function) term2;
            getNestedConcats(stb, f.getTerms().get(0), f.getTerms().get(1));
        } else {
            stb.append(appendTerms(term2));
        }
    }

    /**
     * Prints the text representation of different terms.
     */
    private static String getDisplayName(Term term, PrefixManager prefixManager) {
        StringBuilder sb = new StringBuilder();
        if (term instanceof Function) {
            Function function = (Function) term;
            Predicate functionSymbol = function.getFunctionSymbol();
            String fname = getAbbreviatedName(functionSymbol.toString(), prefixManager, false);
            if (function.isDataTypeFunction()) {
                // Language tag case
                if (functionSymbol.getName().equals(RDF.LANGSTRING.getIRIString())) {
                    // with the language tag
                    Term var = function.getTerms().get(0);
                    Term lang = function.getTerms().get(1);
                    sb.append(getDisplayName(var, prefixManager));
                    sb.append("@");
                    if (lang instanceof ValueConstant) {
                        // Don't pass this to getDisplayName() because
                        // language constant is not written as @"lang-tag"
                        sb.append(((ValueConstant) lang).getValue());
                    } else {
                        sb.append(getDisplayName(lang, prefixManager));
                    }
                } else { // for the other data types
                    Term var = function.getTerms().get(0);
                    sb.append(getDisplayName(var, prefixManager));
                    sb.append("^^");
                    sb.append(fname);
                }
            } else if (functionSymbol instanceof URITemplatePredicate) {
                Term firstTerm = function.getTerms().get(0);

                if (firstTerm instanceof Variable) {
                    sb.append("<{");
                    sb.append(((Variable) firstTerm).getName());
                    sb.append("}>");
                } else {
                    String template = ((ValueConstant) firstTerm).getValue();

                    // Utilize the String.format() method so we replaced placeholders '{}' with '%s'
                    String templateFormat = template.replace("{}", "%s");
                    List<String> varNames = new ArrayList<>();
                    for (Term innerTerm : function.getTerms()) {
                        if (innerTerm instanceof Variable) {
                            varNames.add(getDisplayName(innerTerm, prefixManager));
                        }
                    }
                    String originalUri = String.format(templateFormat, varNames.toArray());
                    if (originalUri.equals(IriConstants.RDF_TYPE)) {
                        sb.append("a");
                    } else {
                        String shortenUri = getAbbreviatedName(originalUri, prefixManager, false); // shorten the URI if possible
                        if (!shortenUri.equals(originalUri)) {
                            sb.append(shortenUri);
                        } else {
                            // If the URI can't be shorten then use the full URI within brackets
                            sb.append("<");
                            sb.append(originalUri);
                            sb.append(">");
                        }
                    }
                }
            } else if (functionSymbol == ExpressionOperation.CONCAT) { //Concat
                List<Term> terms = function.getTerms();
                sb.append("\"");
                getNestedConcats(sb, terms.get(0), terms.get(1));
                sb.append("\"");
            } else { // for any ordinary function symbol
                sb.append(fname);
                sb.append("(");
                boolean separator = false;
                for (Term innerTerm : function.getTerms()) {
                    if (separator) {
                        sb.append(", ");
                    }
                    sb.append(getDisplayName(innerTerm, prefixManager));
                    separator = true;
                }
                sb.append(")");
            }
        } else if (term instanceof Variable) {
            sb.append("{");
            sb.append(((Variable) term).getName());
            sb.append("}");
        } else if (term instanceof URIConstant) {
            String originalUri = term.toString();

            String shortenUri = getAbbreviatedName(originalUri, prefixManager, false); // shorten the URI if possible
            if (!shortenUri.equals(originalUri)) {
                sb.append(shortenUri);
            } else {
                // If the URI can't be shorten then use the full URI within brackets
                sb.append("<");
                sb.append(originalUri);
                sb.append(">");
            }

        } else if (term instanceof ValueConstant) {
            sb.append("\"");
            sb.append(((ValueConstant) term).getValue());
            sb.append("\"");
        } else if (term instanceof BNode) {
            sb.append(((BNode) term).getName());
        }
        return sb.toString();
    }
}