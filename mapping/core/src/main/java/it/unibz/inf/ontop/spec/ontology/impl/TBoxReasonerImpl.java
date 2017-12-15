package it.unibz.inf.ontop.spec.ontology.impl;

/*
 * #%L
 * ontop-reformulation-core
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


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.ontology.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.google.common.collect.ImmutableSet;

/**
 * TBoxReasonerImpl
 *
 *    a DAG-based TBox reasoner
 *
 * @author Roman Kontchakov
 *
 */

public class TBoxReasonerImpl implements TBoxReasoner {

	private final ClassifiedOntologyVocabularyCategoryImpl<ObjectPropertyExpression, ObjectPropertyExpression> objectPropertyDAG;
    private final ClassifiedOntologyVocabularyCategoryImpl<DataPropertyExpression, DataPropertyExpression> dataPropertyDAG;
    private final ClassifiedOntologyVocabularyCategoryImpl<ClassExpression, OClass> classDAG;
    private final ClassifiedOntologyVocabularyCategoryImpl<DataRangeExpression, Datatype> dataRangeDAG;
    private final ClassifiedOntologyVocabularyCategoryImpl<AnnotationProperty, AnnotationProperty> annotationProperties;

    public static final class ClassifiedOntologyVocabularyCategoryImpl<T, V> implements ClassifiedOntologyVocabularyCategory<T,V> {
        private final ImmutableMap<String, V> iriIndex;
        private final EquivalencesDAG<T> dag;

        public ClassifiedOntologyVocabularyCategoryImpl(ImmutableMap<String, V> iriIndex, EquivalencesDAG<T> dag) {
            this.iriIndex = iriIndex;
            this.dag = dag;
        }

        @Override
        public Collection<V> all() {
            return iriIndex.values();
        }

        @Override
        public boolean contains(String iri) {
            return iriIndex.containsKey(iri);
        }

        @Override
        public V get(String iri) {
            V oc = iriIndex.get(iri);
            if (oc == null)
                throw new RuntimeException("NOT FOUND: " + iri);
            return oc;
        }

        @Override
        public EquivalencesDAG<T> dag() {
            return dag;
        }
    }


    /**
	 * constructs a TBox reasoner from an ontology
	 * @param onto: ontology
	 */

	public static TBoxReasoner create(Ontology onto) {

	    OntologyImpl impl = (OntologyImpl)onto;

		final DefaultDirectedGraph<ObjectPropertyExpression, DefaultEdge> objectPropertyGraph =
				getObjectPropertyGraph(onto);
		final EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG =
				EquivalencesDAGImpl.getEquivalencesDAG(objectPropertyGraph);

		final DefaultDirectedGraph<DataPropertyExpression, DefaultEdge> dataPropertyGraph =
				getDataPropertyGraph(onto);
		final EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG =
				EquivalencesDAGImpl.getEquivalencesDAG(dataPropertyGraph);

		final EquivalencesDAGImpl<ClassExpression> classDAG =
				EquivalencesDAGImpl.getEquivalencesDAG(getClassGraph(onto, objectPropertyGraph, dataPropertyGraph));

		final EquivalencesDAGImpl<DataRangeExpression> dataRangeDAG =
				EquivalencesDAGImpl.getEquivalencesDAG(getDataRangeGraph(onto, dataPropertyGraph));

		chooseObjectPropertyRepresentatives(objectPropertyDAG);
		chooseDataPropertyRepresentatives(dataPropertyDAG);
		chooseClassRepresentatives(classDAG, objectPropertyDAG, dataPropertyDAG);
		chooseDataRangeRepresentatives(dataRangeDAG, dataPropertyDAG);

		TBoxReasonerImpl r = new TBoxReasonerImpl(
		        new ClassifiedOntologyVocabularyCategoryImpl<>(impl.vocabulary.concepts, classDAG),
                new ClassifiedOntologyVocabularyCategoryImpl<>(OntologyImpl.OWL2QLDatatypes, dataRangeDAG),
                new ClassifiedOntologyVocabularyCategoryImpl<>(impl.vocabulary.objectProperties, objectPropertyDAG),
                new ClassifiedOntologyVocabularyCategoryImpl<>(impl.vocabulary.dataProperties, dataPropertyDAG),
                new ClassifiedOntologyVocabularyCategoryImpl<>(impl.vocabulary.annotationProperties, null));
//		if (equivalenceReduced) {
//			r = getEquivalenceSimplifiedReasoner(r);
//		}
		return r;
	}

	/**
	 * constructs from DAGs
	 * @param classDAG
	 * @param dataRangeDAG
	 * @param objectPropertyDAG
	 * @param objectPropertyDAG
	 */
	private TBoxReasonerImpl(ClassifiedOntologyVocabularyCategoryImpl<ClassExpression, OClass> classDAG,
                             ClassifiedOntologyVocabularyCategoryImpl<DataRangeExpression, Datatype> dataRangeDAG,
                             ClassifiedOntologyVocabularyCategoryImpl<ObjectPropertyExpression, ObjectPropertyExpression> objectPropertyDAG,
                             ClassifiedOntologyVocabularyCategoryImpl<DataPropertyExpression, DataPropertyExpression> dataPropertyDAG,
                             ClassifiedOntologyVocabularyCategoryImpl<AnnotationProperty, AnnotationProperty> annotationProperties) {
		this.objectPropertyDAG = objectPropertyDAG;
		this.dataPropertyDAG = dataPropertyDAG;
		this.classDAG = classDAG;
		this.dataRangeDAG = dataRangeDAG;
		this.annotationProperties = annotationProperties;
	}

	@Override
	public String toString() {
		return objectPropertyDAG.toString() + "\n" + dataPropertyDAG.toString() + "\n" + classDAG.toString();
	}


    @Override
    public ClassifiedOntologyVocabularyCategory<ObjectPropertyExpression, ObjectPropertyExpression> objectProperties() {
        return objectPropertyDAG;
    }

    @Override
    public ClassifiedOntologyVocabularyCategory<DataPropertyExpression, DataPropertyExpression> dataProperties() {
        return dataPropertyDAG;
    }

    /**
	 * Return the DAG of classes
	 *
	 * @return DAG
	 */


	@Override
	public ClassifiedOntologyVocabularyCategory<ClassExpression, OClass> classes() {
		return classDAG;
	}

	/**
	 * Return the DAG of datatypes and data property ranges
	 *
	 * @return DAG
	 */


	@Override
	public ClassifiedOntologyVocabularyCategory<DataRangeExpression, Datatype> dataRanges() {
		return dataRangeDAG;
	}

    @Override
    public ClassifiedOntologyVocabularyCategory<AnnotationProperty, AnnotationProperty> annotationProperties() {
        return annotationProperties;
    }

	/**
	 * Return the DAG of object properties
	 *
	 * @return DAG
	 */

	public EquivalencesDAG<ObjectPropertyExpression> getObjectPropertyDAG() {
		return objectPropertyDAG.dag();
	}

	/**
	 * Return the DAG of data properties
	 *
	 * @return DAG
	 */

	public EquivalencesDAG<DataPropertyExpression> getDataPropertyDAG() {
		return dataPropertyDAG.dag();
	}


	// INTERNAL DETAILS




	@Deprecated // test only
	public DefaultDirectedGraph<ClassExpression,DefaultEdge> getClassGraph() {
		return ((EquivalencesDAGImpl<ClassExpression>)classDAG.dag()).getGraph();
	}
	@Deprecated // test only
	public DefaultDirectedGraph<DataRangeExpression,DefaultEdge> getDataRangeGraph() {
		return ((EquivalencesDAGImpl<DataRangeExpression>)dataRangeDAG.dag()).getGraph();
	}

	@Deprecated // test only
	public DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> getObjectPropertyGraph() {
		return ((EquivalencesDAGImpl<ObjectPropertyExpression>)objectPropertyDAG.dag()).getGraph();
	}
	@Deprecated // test only
	public DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> getDataPropertyGraph() {
		return  ((EquivalencesDAGImpl<DataPropertyExpression>)dataPropertyDAG.dag()).getGraph();
	}


	@Deprecated // test only
	public int edgeSetSize() {
		return ((EquivalencesDAGImpl<ObjectPropertyExpression>)objectPropertyDAG.dag()).edgeSetSize()
                + ((EquivalencesDAGImpl<DataPropertyExpression>)dataPropertyDAG.dag()).edgeSetSize()
                + ((EquivalencesDAGImpl<ClassExpression>)classDAG.dag()).edgeSetSize();
	}

	@Deprecated // test only
	public int vertexSetSize() {
		return ((EquivalencesDAGImpl<ObjectPropertyExpression>)objectPropertyDAG.dag()).vertexSetSize()
                + ((EquivalencesDAGImpl<DataPropertyExpression>)dataPropertyDAG.dag()).vertexSetSize()
                +  ((EquivalencesDAGImpl<ClassExpression>)classDAG.dag()).vertexSetSize();
	}


	// ---------------------------------------------------------------------------------


	// lexicographical comparison of property names (a property is before its inverse)
	private static final Comparator<DataPropertyExpression> dataPropertyComparator = new Comparator<DataPropertyExpression>() {
		@Override
		public int compare(DataPropertyExpression o1, DataPropertyExpression o2) {
			int compared = o1.getName().compareTo(o2.getName());
			return compared;
		}
	};

	// lexicographical comparison of property names (a property is before its inverse)
	private static final Comparator<ObjectPropertyExpression> objectPropertyComparator = new Comparator<ObjectPropertyExpression>() {
		@Override
		public int compare(ObjectPropertyExpression o1, ObjectPropertyExpression o2) {
			int compared = o1.getName().compareTo(o2.getName());
			if (compared == 0) {
				if (o1.isInverse() == o2.isInverse())
					return 0;
				else if (o2.isInverse())
					return -1;
				else
					return 1;
			}
			return compared;
		}
	};


	private static void chooseObjectPropertyRepresentatives(EquivalencesDAGImpl<ObjectPropertyExpression> dag) {

		for (Equivalences<ObjectPropertyExpression> set : dag) {

			// skip if has already been done
			if (set.getRepresentative() != null)
				continue;

			ObjectPropertyExpression rep = Collections.min(set.getMembers(), objectPropertyComparator);
			ObjectPropertyExpression repInv = rep.getInverse();

			Equivalences<ObjectPropertyExpression> setInv = dag.getVertex(repInv);

			if (rep.isInverse()) {
				repInv = Collections.min(setInv.getMembers(), objectPropertyComparator);
				rep = repInv.getInverse();

				setInv.setIndexed();
			}
			else
				set.setIndexed();

			set.setRepresentative(rep);
			if (!set.contains(repInv)) {
				// if not symmetric
				// (each set either consists of symmetric properties
				//        or none of the properties in the set is symmetric)
				setInv.setRepresentative(repInv);
			}
		}
	}

	private static void chooseDataPropertyRepresentatives(EquivalencesDAGImpl<DataPropertyExpression> dag) {

		for (Equivalences<DataPropertyExpression> set : dag) {
			// skip if has already been done
			if (set.getRepresentative() != null)
				continue;

			DataPropertyExpression rep = Collections.min(set.getMembers(), dataPropertyComparator);

			set.setIndexed();

			set.setRepresentative(rep);
		}
	}


	// lexicographical comparison of class names
	private static final Comparator<OClass> classComparator = new Comparator<OClass>() {
		@Override
		public int compare(OClass o1, OClass o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	// lexicographical comparison of class names
	private static final Comparator<Datatype> datatypeComparator = new Comparator<Datatype>() {
		@Override
		public int compare(Datatype o1, Datatype o2) {
			return o1.getPredicate().getName().compareTo(o2.getPredicate().getName());
		}
	};


	private static void chooseClassRepresentatives(EquivalencesDAGImpl<ClassExpression> dag,
												   EquivalencesDAG<ObjectPropertyExpression> objectPropertyDAG,
												   EquivalencesDAG<DataPropertyExpression> dataPropertyDAG) {

		for (Equivalences<ClassExpression> equivalenceSet : dag) {

			ClassExpression representative = null;
			if (equivalenceSet.size() <= 1) {
				representative = equivalenceSet.iterator().next();
			}
			else {
				// find a named class as a representative
				OClass namedRepresentative = null;
				for (ClassExpression e : equivalenceSet)
					if (e instanceof OClass) {
						if (namedRepresentative == null || classComparator.compare((OClass)e, namedRepresentative) < 0)
							namedRepresentative = (OClass)e;
					}

				if (namedRepresentative == null) {
					ClassExpression first = equivalenceSet.iterator().next();
					if (first instanceof ObjectSomeValuesFrom) {
						ObjectSomeValuesFrom firstp = (ObjectSomeValuesFrom)first;
						ObjectPropertyExpression prop = firstp.getProperty();
						ObjectPropertyExpression propRep = objectPropertyDAG.getVertex(prop).getRepresentative();
						representative = propRep.getDomain();
					}
					else {
						assert (first instanceof DataSomeValuesFrom);
						DataSomeValuesFrom firstp = (DataSomeValuesFrom)first;
						DataPropertyExpression prop = firstp.getProperty();
						DataPropertyExpression propRep = dataPropertyDAG.getVertex(prop).getRepresentative();
						representative = propRep.getDomainRestriction(DatatypeImpl.rdfsLiteral);
					}
				}
				else
					representative = namedRepresentative;
			}

			equivalenceSet.setRepresentative(representative);
			if (representative instanceof OClass)
				equivalenceSet.setIndexed();
		}
	}

	private static void chooseDataRangeRepresentatives(EquivalencesDAGImpl<DataRangeExpression> dag,
													   EquivalencesDAG<DataPropertyExpression> dataPropertyDAG) {

		for (Equivalences<DataRangeExpression> equivalenceSet : dag) {

			DataRangeExpression representative = null;
			if (equivalenceSet.size() <= 1) {
				representative = equivalenceSet.iterator().next();
			}
			else {
				// find a named class as a representative
				Datatype namedRepresentative = null;
				for (DataRangeExpression e : equivalenceSet)
					if (e instanceof Datatype) {
						if (namedRepresentative == null || datatypeComparator.compare((Datatype)e, namedRepresentative) < 0)
							namedRepresentative = (Datatype)e;
					}

				if (namedRepresentative == null) {
					DataRangeExpression first = equivalenceSet.iterator().next();
					assert (first instanceof DataPropertyRangeExpression);
					DataPropertyRangeExpression firstp = (DataPropertyRangeExpression)first;
					DataPropertyExpression prop = firstp.getProperty();
					Equivalences<DataPropertyExpression> vertex = dataPropertyDAG.getVertex(prop);
					if (vertex == null){
						throw new IllegalStateException("Unknown data property: " + prop);
					}
					DataPropertyExpression propRep = vertex.getRepresentative();
					representative = propRep.getRange();
				}
				else
					representative = namedRepresentative;
			}

			equivalenceSet.setRepresentative(representative);
			if (representative instanceof OClass)
				equivalenceSet.setIndexed();
		}
	}

	/**
	 * constructs a TBoxReasoner that has a reduced number of classes and properties in each equivalent class
	 *
	 *   - each object property equivalence class contains one property (representative)
	 *     except when the representative property is equivalent to its inverse, in which
	 *     case the equivalence class contains both the property and its inverse
	 *
	 *   - each data property equivalence class contains a single property (representative)
	 *
	 *   - each class equivalence class contains the representative and all domains / ranges
	 *     of the representatives of property equivalence classes
	 *
	 *  in other words, the constructed TBoxReasoner is the restriction to the vocabulary of the representatives
	 *     all other symbols are mapped to the nodes via *Equivalences hash-maps
	 *
	 * @param reasoner
	 * @return reduced reasoner
	 */


	private static TBoxReasonerImpl getEquivalenceSimplifiedReasoner(TBoxReasoner reasoner) {

		// OBJECT PROPERTIES
		//
		SimpleDirectedGraph<Equivalences<ObjectPropertyExpression>, DefaultEdge> objectProperties
				= new SimpleDirectedGraph<>(DefaultEdge.class);

		// create vertices for properties
		for(Equivalences<ObjectPropertyExpression> node : reasoner.objectProperties().dag()) {
			ObjectPropertyExpression rep = node.getRepresentative();
			ObjectPropertyExpression repInv = rep.getInverse();

			Equivalences<ObjectPropertyExpression> reducedNode;
			if (!node.contains(repInv))
				reducedNode = new Equivalences<>(ImmutableSet.of(rep), rep, node.isIndexed());
			else
				// the object property is equivalent to its inverse
				reducedNode = new Equivalences<>(ImmutableSet.of(rep, repInv), rep, node.isIndexed());

			objectProperties.addVertex(reducedNode);
		}

		EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG = EquivalencesDAGImpl.reduce(
				(EquivalencesDAGImpl<ObjectPropertyExpression>)reasoner.objectProperties().dag(), objectProperties);

		// DATA PROPERTIES
		//
		SimpleDirectedGraph<Equivalences<DataPropertyExpression>, DefaultEdge> dataProperties
				= new SimpleDirectedGraph<>(DefaultEdge.class);

		// create vertices for properties
		for(Equivalences<DataPropertyExpression> node : reasoner.dataProperties().dag()) {
			DataPropertyExpression rep = node.getRepresentative();

			Equivalences<DataPropertyExpression> reducedNode = new Equivalences<>(ImmutableSet.of(rep), rep, node.isIndexed());
			dataProperties.addVertex(reducedNode);
		}

		EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG = EquivalencesDAGImpl.reduce(
				(EquivalencesDAGImpl<DataPropertyExpression>)reasoner.dataProperties().dag(), dataProperties);

		// CLASSES
		//
		SimpleDirectedGraph<Equivalences<ClassExpression>, DefaultEdge> classes = new SimpleDirectedGraph<>(DefaultEdge.class);

		// create vertices for classes
		for(Equivalences<ClassExpression> node : reasoner.classes().dag()) {
			ClassExpression rep = node.getRepresentative();

			ImmutableSet.Builder<ClassExpression> reduced = new ImmutableSet.Builder<>();
			for (ClassExpression equi : node) {
				if (equi.equals(rep)) {
					reduced.add(equi);
				}
				else if (equi instanceof OClass) {
					// an entry is created for a named class
					//OClass equiClass = (OClass) equi;
					//classEquivalenceMap.put(equiClass.getName(), (OClass)rep);
				}
				else if (equi instanceof ObjectSomeValuesFrom) {
					// the property of the existential is a representative of its equivalence class
					if (objectPropertyDAG.getVertex(((ObjectSomeValuesFrom) equi).getProperty()) != null)
						reduced.add(equi);
				}
				else  {
					// the property of the existential is a representative of its equivalence class
					if (dataPropertyDAG.getVertex(((DataSomeValuesFrom) equi).getProperty()) != null)
						reduced.add(equi);
				}
			}

			Equivalences<ClassExpression> reducedNode = new Equivalences<>(reduced.build(), rep, node.isIndexed());
			classes.addVertex(reducedNode);
		}

		EquivalencesDAGImpl<ClassExpression> classDAG = EquivalencesDAGImpl.reduce(
				(EquivalencesDAGImpl<ClassExpression>)reasoner.classes().dag(), classes);

		// DATA RANGES
		//
		// TODO: a proper implementation is in order here

        TBoxReasonerImpl impl = (TBoxReasonerImpl)reasoner;
		return new TBoxReasonerImpl(
		        new ClassifiedOntologyVocabularyCategoryImpl<>(impl.classDAG.iriIndex, classDAG),
                impl.dataRangeDAG,
                new ClassifiedOntologyVocabularyCategoryImpl<>(impl.objectPropertyDAG.iriIndex, objectPropertyDAG),
                new ClassifiedOntologyVocabularyCategoryImpl<>(impl.dataPropertyDAG.iriIndex, dataPropertyDAG),
                impl.annotationProperties);
	}




	/**
	 *  graph representation of object property inclusions in the ontology
	 *
	 *  adds inclusions between the inverses of R and S if
	 *         R is declared a sub-property of S in the ontology
	 *
	 * @param ontology
	 * @return the graph of the property inclusions
	 */

	private static DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> getObjectPropertyGraph(Ontology ontology) {

		DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (ObjectPropertyExpression role : ontology.getObjectProperties()) {
			if (!role.isBottom() && !role.isTop()) {
				graph.addVertex(role);
				graph.addVertex(role.getInverse());
			}
		}

		for (ObjectPropertyExpression role : ontology.getAuxiliaryObjectProperties()) {
			graph.addVertex(role);
			graph.addVertex(role.getInverse());
		}

		// property inclusions
		for (BinaryAxiom<ObjectPropertyExpression> roleIncl : ontology.getSubObjectPropertyAxioms()) {
			// adds the direct edge and the inverse (e.g., R ISA S and R- ISA S-)
			graph.addEdge(roleIncl.getSub(), roleIncl.getSuper());
			graph.addEdge(roleIncl.getSub().getInverse(), roleIncl.getSuper().getInverse());
		}

		return graph;
	}

	/**
	 *  graph representation of data property inclusions in the ontology
	 *
	 *  adds inclusions between the inverses of R and S if
	 *         R is declared a sub-property of S in the ontology
	 *
	 * @param ontology
	 * @return the graph of the property inclusions
	 */

	private static DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> getDataPropertyGraph(Ontology ontology) {

		DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (DataPropertyExpression role : ontology.getDataProperties())
			if (!role.isBottom() && !role.isTop())
				graph.addVertex(role);

		for (BinaryAxiom<DataPropertyExpression> roleIncl : ontology.getSubDataPropertyAxioms())
			graph.addEdge(roleIncl.getSub(), roleIncl.getSuper());

		return graph;
	}

	/**
	 * graph representation of the class inclusions in the ontology
	 *
	 * adds inclusions of the domain of R in the domain of S if
	 *           the provided property graph has an edge from R to S
	 *           (given the getPropertyGraph algorithm, this also
	 *           implies inclusions of the range of R in the range of S
	 *
	 * @param ontology
	 * @param objectPropertyGraph obtained by getObjectPropertyGraph
	 * @param dataPropertyGraph obtained by getDataPropertyGraph
	 * @return the graph of the concept inclusions
	 */

	private static DefaultDirectedGraph<ClassExpression,DefaultEdge> getClassGraph (Ontology ontology,
																					DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> objectPropertyGraph,
																					DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> dataPropertyGraph) {

		DefaultDirectedGraph<ClassExpression,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (OClass concept : ontology.getClasses())
			if (!concept.isBottom() && !concept.isTop())
				graph.addVertex(concept);

		// domains and ranges of roles
		for (ObjectPropertyExpression role : objectPropertyGraph.vertexSet())
			graph.addVertex(role.getDomain());

		// edges between the domains and ranges for sub-properties
		for (DefaultEdge edge : objectPropertyGraph.edgeSet()) {
			ObjectPropertyExpression child = objectPropertyGraph.getEdgeSource(edge);
			ObjectPropertyExpression parent = objectPropertyGraph.getEdgeTarget(edge);
			graph.addEdge(child.getDomain(), parent.getDomain());
		}

		// domains and ranges of roles
		for (DataPropertyExpression role : dataPropertyGraph.vertexSet())
			for (DataSomeValuesFrom dom : role.getAllDomainRestrictions())
				graph.addVertex(dom);

		// edges between the domains and ranges for sub-properties
		for (DefaultEdge edge : dataPropertyGraph.edgeSet()) {
			DataPropertyExpression child = dataPropertyGraph.getEdgeSource(edge);
			DataPropertyExpression parent = dataPropertyGraph.getEdgeTarget(edge);
			graph.addEdge(child.getDomainRestriction(DatatypeImpl.rdfsLiteral), parent.getDomainRestriction(DatatypeImpl.rdfsLiteral));
		}


		// class inclusions from the ontology
		for (BinaryAxiom<ClassExpression> clsIncl : ontology.getSubClassAxioms())
			graph.addEdge(clsIncl.getSub(), clsIncl.getSuper());

		return graph;
	}

	private static DefaultDirectedGraph<DataRangeExpression,DefaultEdge> getDataRangeGraph (Ontology ontology,
																							DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> dataPropertyGraph) {

		DefaultDirectedGraph<DataRangeExpression,DefaultEdge> dataRangeGraph
				= new  DefaultDirectedGraph<DataRangeExpression,DefaultEdge>(DefaultEdge.class);

		// ranges of roles
		for (DataPropertyExpression role : dataPropertyGraph.vertexSet())
			dataRangeGraph.addVertex(role.getRange());

		// edges between the ranges for sub-properties
		for (DefaultEdge edge : dataPropertyGraph.edgeSet()) {
			DataPropertyExpression child = dataPropertyGraph.getEdgeSource(edge);
			DataPropertyExpression parent = dataPropertyGraph.getEdgeTarget(edge);
			dataRangeGraph.addEdge(child.getRange(), parent.getRange());
		}

		// data range inclusions from the ontology
		for (BinaryAxiom<DataRangeExpression> clsIncl : ontology.getSubDataRangeAxioms()) {
			dataRangeGraph.addVertex(clsIncl.getSuper()); // Datatype is not among the vertices from the start
			dataRangeGraph.addEdge(clsIncl.getSub(), clsIncl.getSuper());
		}

		return dataRangeGraph;
	}

}