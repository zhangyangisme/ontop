package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import fj.P;
import fj.P2;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.LeftJoinOptimizationProposalImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;
import static junit.framework.TestCase.assertTrue;

/**
 * TODO: explain
 */

public class SelfLeftJoinTest {

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 3);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 3);
    private final static AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("table3", 3);
    private final static AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 3);
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable Y = DATA_FACTORY.getVariable("Y");
    private final static Variable Z = DATA_FACTORY.getVariable("z");
    private final static Constant TWO = DATA_FACTORY.getConstantLiteral("2");

    private final static Variable M = DATA_FACTORY.getVariable("m");
    private final static Variable M1 = DATA_FACTORY.getVariable("m1");
    private final static Variable N = DATA_FACTORY.getVariable("n");
    private final static Variable N1 = DATA_FACTORY.getVariable("n1");
    private final static Variable N2 = DATA_FACTORY.getVariable("n2");
    private final static Variable O = DATA_FACTORY.getVariable("o");
    private final static Variable O1 = DATA_FACTORY.getVariable("o1");
    private final static Variable O2 = DATA_FACTORY.getVariable("o2");

    private final static ImmutableExpression EXPRESSION1 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, M, N);
    private final static ImmutableExpression EXPRESSION2 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, N, M);

    private final MetadataForQueryOptimization metadata;

    public SelfLeftJoinTest() {
        metadata = initMetadata();
    }

    private static MetadataForQueryOptimization initMetadata() {
        ImmutableMultimap.Builder<AtomPredicate, ImmutableList<Integer>> uniqueKeyBuilder = ImmutableMultimap.builder();

        /**
         * Table 1: non-composite unique constraint and regular field
         */
        uniqueKeyBuilder.put(TABLE1_PREDICATE, ImmutableList.of(1));

        /**
         * Table 2: non-composite unique constraint and regular field
         */
        uniqueKeyBuilder.put(TABLE2_PREDICATE, ImmutableList.of(2));
        /**
         * Table 3: composite unique constraint over the first TWO columns
         */
        uniqueKeyBuilder.put(TABLE3_PREDICATE, ImmutableList.of(1, 2));

        return new MetadataForQueryOptimizationImpl(uniqueKeyBuilder.build(), new UriTemplateMatcher());
    }

    /**
     *  TODO: explain
     */
    @Test
    public void testSelfJoinElimination1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(
                //Optional.of(DATA_FACTORY.getImmutableExpression(
                //ExpressionOperation.EQ, M, M)));
                Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        expectedQueryBuilder.addChild(constructionNode1, dataNode5);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testSelfJoinElimination2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(
                //Optional.of(DATA_FACTORY.getImmutableExpression(
                //ExpressionOperation.EQ, M, M)));
                Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M1, N, O));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(constructionNode1, leftJoinNode1);

        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode6 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M1, N, O));
        expectedQueryBuilder.addChild(leftJoinNode1, dataNode5, LEFT);
        expectedQueryBuilder.addChild(leftJoinNode1, dataNode6, RIGHT);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testLeftJoinElimination1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(
                //Optional.of(DATA_FACTORY.getImmutableExpression(
                //ExpressionOperation.EQ, M, M)));
                Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N2, O2));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);
        queryBuilder.addChild(leftJoinNode, dataNode3, LEFT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N2, O2));
        expectedQueryBuilder.addChild(constructionNode1, dataNode5);
        expectedQueryBuilder.addChild(constructionNode1, dataNode6);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }


}
