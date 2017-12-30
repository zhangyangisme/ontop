package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * All its children are expected to project its projected variables
 */
public interface UnionNode extends ExplicitVariableProjectionNode, NaryOperatorNode {

    @Override
    UnionNode clone();

    @Override
    UnionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;

    @Override
    SubstitutionResults<UnionNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) ;

    @Override
    SubstitutionResults<UnionNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query);

    /**
     * Returns true if its has, as a child, a construction node defining the variable.
     *
     * To be called on already lifted tree.
     */
    boolean hasAChildWithLiftableDefinition(Variable variable, ImmutableList<IQTree> children);
}
