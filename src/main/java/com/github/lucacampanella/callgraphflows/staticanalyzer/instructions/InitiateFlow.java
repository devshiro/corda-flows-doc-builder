package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.Utils.Utils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzer;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;

import java.awt.*;
import java.util.Optional;

public class InitiateFlow extends InstructionStatement {

    public InitiateFlow(CtStatement statement) {
        super(statement);
    }
    public InitiateFlow() {
        super();
    }

    private static final Color BACKGROUND_COLOR = new Color(155, 193, 255); //Blueish
//    protected Color getBackgroundColor() {
//        return BACKGROUND_COLOR;
//    }

    public static InitiateFlow fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        InitiateFlow initiateFlow = new InitiateFlow();
        initiateFlow.line = statement.getPosition().getLine();
        initiateFlow.internalMethodInvocations.add(StaticAnalyzer.getAllRelevantMethodInvocations(statement, analyzer));

        if(statement instanceof CtLocalVariable) {
            initiateFlow.targetSessionName = Optional.ofNullable(((CtLocalVariable) statement).getReference().getSimpleName());
        }
        else if(statement instanceof CtAssignment) {
            initiateFlow.targetSessionName = Optional.ofNullable(((CtAssignment) statement).getAssigned().toString());
        }

        initiateFlow.buildGraphElem();
        return initiateFlow;
    }

    public boolean modifiesSession() {
        return true;
    }

    /**
     * @return true, being itself an InitiateFlow call
     */
    public Optional<InitiateFlow> getInitiateFlowStatementAtThisLevel() {
        return Optional.of(this);
    }

    @Override
    protected void buildGraphElem() {
        super.buildGraphElem();
        graphElem.setDrawBox(true);
    }

    @Override
    public String getStringDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("initiateFlow(");
        if(targetSessionName.isPresent()) {
            sb.append(Utils.removePackageDescriptionIfWanted(targetSessionName.get()));
        }
        else {
            sb.append("session");
        }
        sb.append(")");

        return sb.toString();
    }
}