package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.NodeTransformationProposal;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.QueryNodeVisitor;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class DistinctNodeImpl extends QueryModifierNodeImpl implements DistinctNode {

    private static final String DISTINCT_NODE_STR = "DISTINCT";

    @Inject
    private DistinctNodeImpl(IntermediateQueryFactory iqFactory) {
        super(iqFactory);
    }

    @Override
    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitution(descendingSubstitution, constraint));
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree child) {
        return transformer.transformDistinct(tree, this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public DistinctNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return node instanceof DistinctNode;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return queryNode instanceof DistinctNode;
    }

    @Override
    public String toString() {
        return DISTINCT_NODE_STR;
    }

    @Override
    public DistinctNode clone() {
        return iqFactory.createDistinctNode();
    }
}
