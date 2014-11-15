package it.unibz.krdb.obda.sesame;

/*
 * #%L
 * ontop-obdalib-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class SesameStatement implements Statement {
    private static final long serialVersionUID = 3398547980791013746L;
	private Resource subject = null;
	private URI predicate = null;
	private Value object = null;
	private Resource context = null;
	private ValueFactory fact = new ValueFactoryImpl();
	
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

	public SesameStatement(Assertion assertion) {
		
		if (assertion instanceof ObjectPropertyAssertion) {
			//object or data property assertion
			ObjectPropertyAssertion ba = (ObjectPropertyAssertion) assertion;
			ObjectConstant subj = ba.getSubject();
			Predicate pred = ba.getProperty().getPredicate();
			ObjectConstant obj = ba.getObject();
			
			// convert string into respective type
			subject = getResource(subj);
			
			predicate = fact.createURI(pred.getName().toString()); // URI
			
			object = getResource(obj);
		} 
		if (assertion instanceof DataPropertyAssertion) {
			//object or data property assertion
			DataPropertyAssertion ba = (DataPropertyAssertion) assertion;
			ObjectConstant subj = ba.getSubject();
			Predicate pred = ba.getProperty().getPredicate();
			ValueConstant obj = ba.getValue();
			
			// convert string into respective type
			subject = getResource(subj);
			
			predicate = fact.createURI(pred.getName().toString()); // URI
			
			if (obj instanceof ValueConstant)
				object = getLiteral((ValueConstant)obj);		
			else 
				throw new RuntimeException("Invalid constant as object!" + obj);
		} 
		else if (assertion instanceof ClassAssertion) { 
			//class assertion
			ClassAssertion ua = (ClassAssertion) assertion;
			ObjectConstant subj = ua.getIndividual();
			String pred = OBDAVocabulary.RDF_TYPE;
			Predicate obj = ua.getConcept().getPredicate();
			
			// convert string into respective type
			subject = getResource(subj);
			
			predicate = fact.createURI(pred); // URI
		
			object = fact.createURI(obj.getName().toString());
			
		}
	}
	
	private Resource getResource(ObjectConstant obj) {
		if (obj instanceof BNode)
			return fact.createBNode(((BNode)obj).getName());
		else if (obj instanceof URIConstant)
			return fact.createURI(((URIConstant)obj).getURI());
		else 
			throw new RuntimeException("Invalid constant as subject!" + obj);		
	}
	
	public Literal getLiteral(ValueConstant literal)
	{
		if (literal.getType() == COL_TYPE.LITERAL) {                                        // special 17
			Literal value = fact.createLiteral(literal.getValue(), (URI)null);
			return value;
		}
		else if (literal.getType() == COL_TYPE.LITERAL_LANG) {
			Literal value = fact.createLiteral(literal.getValue(), literal.getLanguage());
			return value;
		}
		else if (literal.getType() == COL_TYPE.OBJECT) {
			Literal value = fact.createLiteral(literal.getValue(), dtfac.getDataTypeURI(COL_TYPE.STRING));
			return value;
		}	
		else {
			URI datatype = dtfac.getDataTypeURI(literal.getType());
			Literal value = fact.createLiteral(literal.getValue(), datatype);
			return value;
		}
	}

	public Resource getSubject() {
		return subject;
	}

	public URI getPredicate() {
		return predicate;
	}

	public Value getObject() {
		return object;
	}

	public Resource getContext() {
		// TODO Auto-generated method stub
		return context;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Statement)) return false;

        Statement that = (Statement) o;

        Resource thatContext = that.getContext();
        if (context != null ? !context.equals(thatContext) : thatContext != null) return false;
        Value thatObject = that.getObject();
        if (object != null ? !object.equals(thatObject) : thatObject != null) return false;
        URI thatPredicate = that.getPredicate();
        if (predicate != null ? !predicate.equals(thatPredicate) : thatPredicate != null) return false;
        Resource thatSubject = that.getSubject();
        if (subject != null ? !subject.equals(thatSubject) : thatSubject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int contextComponent = context != null ? context.hashCode() : 0;
        int subjectComponent = subject != null ? subject.hashCode() : 0;
        int predicateComponent = predicate != null ? predicate.hashCode() : 0;
        int objectComponent = object != null ? object.hashCode() : 0;
        return 1013 * contextComponent + 961 * subjectComponent + 31 * predicateComponent + objectComponent;
    }

	@Override
	public String toString()
	{
		return "("+subject+", "+predicate+", "+object+")";
	}
}
