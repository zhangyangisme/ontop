package it.unibz.inf.ontop.executor.leftjoin;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.executor.SimpleNodeCentricCompositeExecutor;
import it.unibz.inf.ontop.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.pivotalrepr.LeftJoinNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.LeftJoinOptimizationProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 *
 * TODO: include LeftToInnerJoinExecutor + removal of unnecessary right parts
 */
@Singleton
public class LeftJoinCompositeExecutor extends SimpleNodeCentricCompositeExecutor<LeftJoinNode,
        LeftJoinOptimizationProposal> implements LeftJoinExecutor {

    private final ImmutableList<SimpleNodeCentricExecutor<LeftJoinNode, LeftJoinOptimizationProposal>> executors;

    @Inject
    private LeftJoinCompositeExecutor(RedundantSelfLeftJoinExecutor selfLeftJoinExecutor,
                                      ForeignKeyLeftJoinExecutor fkExecutor) {
        ImmutableList.Builder<SimpleNodeCentricExecutor<LeftJoinNode, LeftJoinOptimizationProposal>> executorBuilder = ImmutableList.builder();

        //executorBuilder.add(new LeftJoinBooleanExpressionExecutor());
        executorBuilder.add(selfLeftJoinExecutor);
        executorBuilder.add(fkExecutor);

        executors = executorBuilder.build();
    }

    @Override
    protected Optional<LeftJoinOptimizationProposal> createNewProposalFromFocusNode(LeftJoinNode focusNode) {
        LeftJoinOptimizationProposal proposal = new LeftJoinOptimizationProposalImpl(focusNode);
        return Optional.of(proposal);
    }

    @Override
    protected ImmutableList<SimpleNodeCentricExecutor<LeftJoinNode, LeftJoinOptimizationProposal>> getExecutors() {
        return executors;
    }
}
