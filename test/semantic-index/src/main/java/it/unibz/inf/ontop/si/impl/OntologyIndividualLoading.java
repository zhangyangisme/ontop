package it.unibz.inf.ontop.si.impl;


import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.owlapi.utils.OWLAPIABoxIterator;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.si.impl.SILoadingTools.RepositoryInit;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Properties;

import static it.unibz.inf.ontop.si.impl.SILoadingTools.*;


public class OntologyIndividualLoading {

    private static final Logger LOG = LoggerFactory.getLogger(OntologyIndividualLoading.class);

    /**
     * High-level method
     */
    public static OntopSemanticIndexLoader loadOntologyIndividuals(OWLOntology owlOntology, Properties properties)
            throws SemanticIndexException {

        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
        AtomFactory atomFactory = defaultConfiguration.getAtomFactory();
        TermFactory termFactory = defaultConfiguration.getTermFactory();
        TypeFactory typeFactory = defaultConfiguration.getTypeFactory();
        OWLAPITranslatorOWL2QL owlapiTranslator = defaultConfiguration.getInjector().getInstance(OWLAPITranslatorOWL2QL.class);

        RepositoryInit init = createRepository(owlOntology, atomFactory, termFactory, owlapiTranslator, typeFactory);

        try {
            /*
            Loads the data
             */
            OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(init.abox
                    .orElseThrow(() -> new IllegalStateException("An ontology closure was expected")),
                    init.reasoner, owlapiTranslator);

            int count = init.dataRepository.insertData(init.localConnection, aBoxIter, 5000, 500);
            LOG.debug("Inserted {} triples from the ontology.", count);

            /*
            Creates the configuration and the loader object
             */
            OntopSQLOWLAPIConfiguration configuration = createConfiguration(init.dataRepository, owlOntology, init.jdbcUrl, properties);
            return new OntopSemanticIndexLoaderImpl(configuration, init.localConnection);
        }
        catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }
}
