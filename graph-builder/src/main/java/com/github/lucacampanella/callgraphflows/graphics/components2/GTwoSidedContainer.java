package com.github.lucacampanella.callgraphflows.graphics.components2;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;
import com.github.lucacampanella.callgraphflows.staticanalyzer.ClassDescriptionContainer;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.InitiateFlow;
import org.jfree.graphics2d.svg.SVGGraphics2D;

public class GTwoSidedContainer extends GBaseComponent {

    protected static final int SPACE_BETWEEN_FLOWS = 20;

    GSubFlowIndented mainSubFlow = new GSubFlowIndented();
    GSubFlowIndented counterpartySubFlow = null;
    int counterpartyStartY = -1;
    GInstruction initiateFlowInstruction = null;
    GBaseText arrowText = null;

    public GTwoSidedContainer() {
        mainSubFlow.setParent(this);
    }


    @Override
    public void draw(SVGGraphics2D g2, int x, int y) {
        if(getHeight(g2) == -1) { //we didn't compute the internal distribution
            computeHeight(g2);
        }
        mainSubFlow.draw(g2, x, y);
        if(hasCounterpartySubFlow()) {
            int counterpartyStartX = x + mainSubFlow.getWidth(g2) + SPACE_BETWEEN_FLOWS;
            counterpartySubFlow.draw(g2, counterpartyStartX,y + counterpartyStartY);
//todo arrow between the two

            int arrowBetweenFlowY = initiateFlowInstruction.getLastDrawnStartY() +
                    (initiateFlowInstruction.getHeight(g2)/2);
            int arrowBetweenFlowStartX = initiateFlowInstruction.getLastDrawnStartX() +
                    initiateFlowInstruction.getWidth(g2);
            int arrowBetweenFlowFinishX = counterpartyStartX + GSubFlowIndented.WIDTH
                    - GSubFlowIndented.INDENTATION-1;
            g2.drawLine(arrowBetweenFlowStartX, arrowBetweenFlowY, arrowBetweenFlowFinishX, arrowBetweenFlowY);
        }
        mainSubFlow.drawBrothersAndLinks(g2);
        if(hasCounterpartySubFlow()) {
            counterpartySubFlow.drawBrothersAndLinks(g2);
        }
    }

    public boolean isTwoSidedComponent() {
        return true;
    }

    @Override
    protected int computeHeight(SVGGraphics2D g2) {
        //compute dimensions with draw logic
        if(!hasCounterpartySubFlow()) {
            final ComponentWithRelativeY blockingComp = mainSubFlow.setUpDimensions(g2, null);
            if(blockingComp != null) {
                throw new IllegalStateException("Cannot have a two sided flow with only one side and " +
                        "that side has direct components that have brothers and links");
            }
            return mainSubFlow.getHeight(g2);
        }
        else {
            final ComponentWithRelativeY initiateFlowInstrWithY =
                    mainSubFlow.setUpDimensionsUntilInitiateFlow(g2, initiateFlowInstruction);
            counterpartyStartY = initiateFlowInstrWithY.getY() + initiateFlowInstruction.getHeight(g2)/2;
            ComponentWithRelativeY blockingLeftWithY = mainSubFlow.setUpDimensions(g2, initiateFlowInstrWithY);
            ComponentWithRelativeY blockingRightWithY = counterpartySubFlow.setUpDimensions(g2, null);

            GBaseSimpleComponent blockingLeft;
            GBaseSimpleComponent blockingRight;
            boolean doneLeft = false;
            while(blockingLeftWithY != null && blockingRightWithY != null) {
                blockingLeft = (GBaseSimpleComponent) blockingLeftWithY.getComp();
                blockingRight = (GBaseSimpleComponent) blockingRightWithY.getComp();
                if(blockingLeft.isBrotherWith(blockingRight)) {
                    int maxY = Math.max(blockingLeftWithY.getY(), blockingRightWithY.getY() + counterpartyStartY);
                    blockingLeftWithY.setY(maxY);
                    blockingRightWithY.setY(maxY - counterpartyStartY);
                    if(!doneLeft) {
                        blockingLeftWithY = mainSubFlow.setUpDimensions(g2, blockingLeftWithY);
                    }
                    blockingRightWithY = counterpartySubFlow.setUpDimensions(g2, blockingRightWithY);
                }
                else {
                    ComponentWithRelativeY blockingLeftWithYBackup = blockingLeftWithY;
                    blockingLeftWithY = mainSubFlow.setUpDimensions(g2, blockingLeftWithY);
                    if(blockingLeftWithY == null) {
                        blockingLeftWithY = blockingLeftWithYBackup;
                        doneLeft = true;
                        blockingRightWithY = counterpartySubFlow.setUpDimensions(g2, blockingRightWithY);
                    }
                }
            }
            //finish in case they where not aligned, strange, probably error
            while(blockingLeftWithY != null && !doneLeft) {
                blockingLeftWithY = mainSubFlow.setUpDimensions(g2, blockingLeftWithY);
            }
            while(blockingRightWithY != null) {
                blockingRightWithY = counterpartySubFlow.setUpDimensions(g2, blockingRightWithY);
            }
            return Math.max(mainSubFlow.getHeight(g2), counterpartySubFlow.getHeight(g2) + counterpartyStartY);
        }
    }

    @Override
    protected int computeWidth(SVGGraphics2D g2) {
        int res = mainSubFlow.getWidth(g2);
        if(hasCounterpartySubFlow()) {
            res += SPACE_BETWEEN_FLOWS + counterpartySubFlow.getWidth(g2);
        }
        return res;
    }

    public boolean hasCounterpartySubFlow() {
        return counterpartySubFlow != null;
    }

    public GSubFlowIndented getMainSubFlow() {
        return mainSubFlow;
    }

    public GSubFlowIndented getCounterpartySubFlow() {
        return counterpartySubFlow;
    }

    public void setMainSubFlow(GSubFlowIndented mainSubFlow) {
        this.mainSubFlow = mainSubFlow;
        mainSubFlow.setParent(this);
        recomputeDims();
    }

    public void setCounterpartySubFlow(GSubFlowIndented counterpartySubFlow, GInstruction initiateFlowInstruction) {
        this.counterpartySubFlow = counterpartySubFlow;
        this.initiateFlowInstruction = initiateFlowInstruction;
        this.counterpartySubFlow.setParent(this);
        recomputeDims();
    }

    public void setArrowText(GBaseText arrowText) {
        this.arrowText = arrowText;
    }

    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        mainSubFlow.drawBrothersAndLinks(g2);
        if(hasCounterpartySubFlow()) {
            counterpartySubFlow.drawBrothersAndLinks(g2);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("main sub flow = [");
        sb.append(mainSubFlow.toString());
        sb.append("]\n");
        if(counterpartySubFlow != null) {
            sb.append("counterpartySubFlow = [");
            sb.append(counterpartySubFlow);
            sb.append("]");
        }

        return sb.toString();
    }

    public int getCounterpartyStartY() {
        return counterpartyStartY;
    }

    public static GTwoSidedContainer fromAnalysisResult(AnalysisResult analysisResult) {
        GTwoSidedContainer twoSidedContainer = new GTwoSidedContainer();

        final ClassDescriptionContainer classDescription = analysisResult.getClassDescription();

        final GSubFlowIndented mainFlow = GSubFlowIndented.fromBranch(analysisResult.getStatements());
        StringBuilder enteringArrowTextSB = new StringBuilder(classDescription.getNameWithParent());
        for(String annotation : classDescription.getAnnotations()) {
            enteringArrowTextSB.append("\n@");
            enteringArrowTextSB.append(annotation);
        }
        mainFlow.setEnteringArrowText(new GBaseText(enteringArrowTextSB.toString()));
        twoSidedContainer.setMainSubFlow(mainFlow);

        final AnalysisResult initiatedClassResult = analysisResult.getCounterpartyClassResult();
        if(initiatedClassResult != null) {
            final InitiateFlow initiateFlow = analysisResult.getStatements().getInitiateFlowStatementAtThisLevel()
                    .orElseThrow(() -> new IllegalStateException("Analysis with initiated counterparty, " +
                            "but no initiate Flow instruction at this level"));

            final GSubFlowIndented counterpartyFlow = GSubFlowIndented.fromBranch(initiatedClassResult.getStatements());

            enteringArrowTextSB = new StringBuilder(initiatedClassResult.getClassDescription().getNameWithParent());
            enteringArrowTextSB.append("\n@InitiatedBy(");
            enteringArrowTextSB.append(classDescription.getNameWithParent());
            enteringArrowTextSB.append(")");

            counterpartyFlow.setEnteringArrowText(new GBaseText(enteringArrowTextSB.toString()));
            twoSidedContainer.setCounterpartySubFlow(counterpartyFlow,
                    (GInstruction) initiateFlow.getGraphElem());
        }

        return twoSidedContainer;
    }
}