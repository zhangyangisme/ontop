package it.unibz.krdb.obda.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi3
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

import it.unibz.krdb.obda.ontology.ABoxAssertion;
import it.unibz.krdb.obda.owlapi3.OWLAPI3ABoxIterator;
import junit.framework.TestCase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

public class OWLAPI2ABoxIteratorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testABoxAssertionIterator() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		Iterator<OWLAxiom> owliterator = ontology.getAxioms().iterator();
		Iterator<ABoxAssertion> aboxit = new OWLAPI3ABoxIterator(owliterator);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}

	public void testABoxAssertionIterable() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		Iterator<ABoxAssertion> aboxit = new OWLAPI3ABoxIterator(ontology.getAxioms());
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testABoxAssertionEmptyIterable() throws Exception {

		Iterator<ABoxAssertion> aboxit = new OWLAPI3ABoxIterator(new HashSet<OWLAxiom>());
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 0);
	}




	
	public void testABoxAssertionOntology() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		Iterator<ABoxAssertion> aboxit = new OWLAPI3ABoxIterator(ontology);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testABoxAssertionEmptyOntology() throws Exception {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.createOntology();

		Iterator<ABoxAssertion> aboxit = new OWLAPI3ABoxIterator(ontology);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 0);
	}
	
	public void testABoxAssertionOntologies() throws Exception {
		String owlfile1 = "src/test/resources/test/ontologies/translation/onto1.owl";
		String owlfile2 = "src/test/resources/test/ontologies/translation/onto2.owl";
		String owlfile3 = "src/test/resources/test/ontologies/translation/onto3.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		manager.loadOntologyFromOntologyDocument((new File(owlfile1)));
		manager.loadOntologyFromOntologyDocument((new File(owlfile2)));
		manager.loadOntologyFromOntologyDocument((new File(owlfile3)));

		Iterator<ABoxAssertion> aboxit = new OWLAPI3ABoxIterator(manager.getOntologies());
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testABoxAssertionEmptyOntologySet() throws Exception {

		Iterator<ABoxAssertion> aboxit = new OWLAPI3ABoxIterator(new HashSet<OWLOntology>());
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 0);
	}


}
