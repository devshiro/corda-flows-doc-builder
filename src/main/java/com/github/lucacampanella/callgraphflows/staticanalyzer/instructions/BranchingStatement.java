package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.Utils.Utils;
import com.github.lucacampanella.callgraphflows.graphics.components.GBaseGraphicComponent;
import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.components.GConditionalBranch;
import com.github.lucacampanella.callgraphflows.graphics.components.GInstruction;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzer;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.template.TemplateMatcher;

import java.util.Optional;

/**
 * Father class for all statements in which a branching can happen, like {@link For}, {@link ForEach}, {@link IfElse}
 * etc...
 */
public abstract class BranchingStatement implements StatementWithCompanionInterface {

    protected Branch branchTrue = new Branch();
    protected Branch branchFalse = new Branch();
    protected String conditionDescription;
    protected int conditionLineNumber = -1;
    StatementInterface blockingStatementInCondition;
    GConditionalBranch graphElem = new GConditionalBranch();
    GInstruction conditionInstruction = null;
    Branch internalMethodInvocations = new Branch();



    protected BranchingStatement(){}

    public boolean hasBlockingStatementInCondition() {
        return blockingStatementInCondition != null;
    }

    public StatementInterface getBlockingStatementInCondition() {
        return blockingStatementInCondition;
    }

    public Branch getBranchTrue() {
        return branchTrue;
    }

    public Branch getBranchFalse() {
        return branchFalse;
    }

    @Override
    public GBaseGraphicComponent getGraphElem() {
        return toBePainted() ? graphElem : null;
    }

    @Override
    public String toString() {
        return "BranchingStatement (body omitted): " + getStringRepresentation();
    }

    public boolean acceptCompanion(StatementWithCompanionInterface companion) {
//        return needsCompanion() && blockingStatementInCondition.acceptCompanion(companion);
        //todo
        return true;
    }

    @Override
    public void createGraphLink(StatementWithCompanionInterface companion) {
        //todo
//        blockingStatementInCondition.createGraphLink(companion);
    }

    @Override
    public boolean needsCompanion() {
        return hasBlockingStatementInCondition();
    }

    public String getStringRepresentation() {
        return conditionDescription;
    }

    @Override
    public boolean isBranchingStatement() {
        return true;
    }

    protected GInstruction getConditionInstruction() {
        return hasBlockingStatementInCondition() ? (GInstruction) getBlockingStatementInCondition().getGraphElem() :
                conditionInstruction;
    }

    @Override
    public Optional<String> getTargetSessionName() {
        if(hasBlockingStatementInCondition()) {
            return blockingStatementInCondition.getTargetSessionName();
        }
        return Optional.empty();
    }

    protected void buildGraphElem() {
        GConditionalBranch elem = graphElem;

        elem.setEnteringArrowText(getConditionInstruction());
        getBranchTrue().getStatements().forEach(stmt -> elem.addComponent(stmt.getGraphElem()));
    }

    protected void initiateBlockingStatementAndConditionInstruction(CtExpression condition,
                                                                           CtStatement statement,
                                                                           AnalyzerWithModel analyzer) {
        //automatically initiated to null if the condition doesn't contain a matching blocking statement that
        //communicates with the other flow
        this.blockingStatementInCondition =
                MatcherHelper.instantiateStatementIfQueryableMatches(condition, statement, analyzer);
        //todo: what if there are two blocking statements in the loop condition?

        formatDescription(statement);
        conditionDescription = Utils.removePackageDescriptionIfWanted(conditionDescription);

        if(this.hasBlockingStatementInCondition()) {
            this.conditionInstruction =
                    (GInstruction) this.getBlockingStatementInCondition().getGraphElem();
            this.conditionInstruction.setText(conditionDescription);
        }
        else {
            this.conditionInstruction =
                    new GInstruction(this.conditionLineNumber, this.conditionDescription);
            this.conditionInstruction.setTextColor(GBaseTextComponent.LESS_IMPORTANT_TEXT_COLOR);
        }

        this.internalMethodInvocations.add(
                StaticAnalyzer.getAllRelevantMethodInvocations(condition, analyzer));
    }

    protected abstract String formatDescription(CtStatement statement);

    @Override
    public boolean isRelevantForAnalysis() {
        if(hasBlockingStatementInCondition() && blockingStatementInCondition.isRelevantForAnalysis()) {
            return true;
        }
        return internalMethodInvocations.isRelevant() || getBranchTrue().isRelevant() || getBranchFalse().isRelevant();
    }

    @Override
    public boolean toBePainted() {
        if(hasBlockingStatementInCondition() && blockingStatementInCondition.toBePainted()) {
            return true;
        }
        return getBranchTrue().toBePainted() || getBranchFalse().toBePainted();
    }

    @Override
    public Branch getInternalMethodInvocations() {
        return internalMethodInvocations;
    }

    /**
     * @return true if there is an initiateFlow call, but doesn't look into subFlows
     */
    @Override
    public Optional<InitiateFlow> getInitiateFlowStatementAtThisLevel() {
        Optional<InitiateFlow>
                res = getInternalMethodInvocations().getInitiateFlowStatementAtThisLevel();
        if(res.isPresent()) {
            return res;
        }
        res = getBranchTrue().getInitiateFlowStatementAtThisLevel();
        if(res.isPresent()) {
            return res;
        }
        res = getBranchFalse().getInitiateFlowStatementAtThisLevel();
        return res;
    }

    @Override
    public Branch flattenInternalMethodInvocations() {
        Branch res = new Branch();
        getInternalMethodInvocations().forEach(stmt -> res.add(stmt.flattenInternalMethodInvocations()));
        res.add(this);
        return res;
    }
}