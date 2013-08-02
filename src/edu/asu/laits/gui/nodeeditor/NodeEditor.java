/**
 * (c) 2013, Arizona Board of Regents for and on behalf of Arizona State
 * University. This file is part of LAITS.
 *
 * LAITS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * LAITS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LAITS. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.asu.laits.gui.nodeeditor;

import edu.asu.laits.editor.ApplicationContext;
import edu.asu.laits.editor.GraphEditorPane;
import edu.asu.laits.gui.BlockingToolTip;
import edu.asu.laits.model.HelpBubble;
import edu.asu.laits.model.PersistenceManager;
import edu.asu.laits.model.TaskSolution;
import edu.asu.laits.model.Vertex;
import java.awt.Color;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.log4j.Logger;

/**
 *
 * @author ramayantiwari
 */
public class NodeEditor extends javax.swing.JDialog {

    private DescriptionPanelView dPanel;
    private PlanPanelView pPanel;
    private InputsPanelView iPanel;
    private CalculationsPanelView cPanel;
    //Tab Pane Indexes
    private final int DESCRIPTION = 0;
    private final int PLAN = 1;
    private final int INPUTS = 2;
    private final int CALCULATIONS = 3;
    private boolean extraTabEvent;
    private int selectedTab;
    private GraphEditorPane graphPane;
    private Vertex currentVertex;
    /**
     * Logger
     */
    private static Logger logs = Logger.getLogger("DevLogs");
    private static Logger activityLogs = Logger.getLogger("ActivityLogs");
    /**
     * Creates new form NodeEditor2
     */
    public NodeEditor(GraphEditorPane editorPane, Vertex selected) {
        super(editorPane.getMainFrame(), false);
        graphPane = editorPane;
        currentVertex = selected;
        initComponents();
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(2, 0, -1, 0));
        setTabListener();
        initNodeEditor();
        if(ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")){
            addHelpBalloon(ApplicationContext.getFirstNextNode(), "onLoad", getTabName(selectedTab));
        }       
    }

    private void initNodeEditor() {
        logs.debug("Initializing NodeEditor");
        activityLogs.debug("NodeEditor opened for Node '" + currentVertex.getName() + "'");
        displayEnterButton();
        initTabs();
        setTitle(getNodeEditorTitle());
        setEditorMessage("", true);
        prepareNodeEditorDisplay();
        if (ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")) {
            buttonCancel.setEnabled(false);
        }

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeNodeEditor();
            }
        });
        
    }
    
    private String getNodeEditorTitle() {
        String title = "Node Editor - ";
        if (currentVertex.getName().equals("")) {
            title += "New Node";
        } else {
            title += currentVertex.getName();
        }

        return title;
    }

    private void prepareNodeEditorDisplay() {
        logs.debug("Preparing Node Editor Display");
        setLocationRelativeTo(null);
        pack();

        setVisible(true);
        setResizable(false);

        if (ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")) {
            if (!currentVertex.getPlanStatus().equals(Vertex.PlanStatus.CORRECT)
                    && !currentVertex.getPlanStatus().equals(Vertex.PlanStatus.GAVEUP)) {
                tabPane.setEnabledAt(INPUTS, false);
                tabPane.setForegroundAt(INPUTS, Color.GRAY);
            }
            if (!currentVertex.getInputsStatus().equals(Vertex.InputsStatus.CORRECT)
                    && !currentVertex.getInputsStatus().equals(Vertex.InputsStatus.GAVEUP)) {
                tabPane.setEnabledAt(CALCULATIONS, false);
                tabPane.setForegroundAt(CALCULATIONS, Color.GRAY);
            }
        }
        
    }

    public void initTabs() {
        logs.debug("Initializing NodeEditor Tabs - Start");

        dPanel = new DescriptionPanelView(this);
        pPanel = new PlanPanelView(this);

        iPanel = new InputsPanelView(this);
        cPanel = new CalculationsPanelView(this);

        activityLogs.debug("Vertex Details before opening node editor ");
        activityLogs.debug(dPanel.printDescriptionPanelDetails());
        //activityLogs.debug(pPanel.printPlanPanel());
        activityLogs.debug(iPanel.printInputsPanel());
        activityLogs.debug(cPanel.printCalculationPanel());

        descriptionPanel.setLayout(new java.awt.GridLayout(1, 1));
        descriptionPanel.add(dPanel);

        planPanel.setLayout(new java.awt.GridLayout(1, 1));
        planPanel.add(pPanel);

        inputsPanel.setLayout(new java.awt.GridLayout(1, 1));
        inputsPanel.add(iPanel);

        calculationPanel.setLayout(new java.awt.GridLayout(1, 1));
        calculationPanel.add(cPanel);

        setSelectedPanel();

        logs.debug("Initializing NodeEditor Tabs - End");
    }

    private void setSelectedPanel() {

        if (!currentVertex.getInputsStatus().equals(Vertex.InputsStatus.UNDEFINED)
                && !currentVertex.getInputsStatus().equals(Vertex.InputsStatus.INCORRECT)) {
            logs.debug("setting calc panel as current");
            activityLogs.debug("Node Editor is opend with Calculations Tab for Node: " + currentVertex.getName());
            selectedTab = CALCULATIONS;
            tabPane.setSelectedIndex(CALCULATIONS);
        } else if (!currentVertex.getPlanStatus().equals(Vertex.PlanStatus.UNDEFINED)
                && !currentVertex.getPlanStatus().equals(Vertex.PlanStatus.INCORRECT)) {
            logs.debug("Setting Inputs Panel as Current");
            activityLogs.debug("Node Editor is opend with Inputs Tab for Node: " + currentVertex.getName());
            selectedTab = INPUTS;
            tabPane.setSelectedIndex(INPUTS);
        } else if (!currentVertex.getDescriptionStatus().equals(Vertex.DescriptionStatus.UNDEFINED)
                && !currentVertex.getDescriptionStatus().equals(Vertex.DescriptionStatus.INCORRECT)) {
            System.out.println("Setting Plan as current");
            logs.debug("Setting Plan Panel as Current");
            activityLogs.debug("Node Editor is opend with Plan Tab for Node: " + currentVertex.getName());
            selectedTab = PLAN;
            tabPane.setSelectedIndex(PLAN);
        } else {
            logs.debug("Setting Desc Panel as Current");
            activityLogs.debug("Node Editor is opend with Description Tab for Node: " + currentVertex.getName());
            selectedTab = DESCRIPTION;
            tabPane.setSelectedIndex(DESCRIPTION);
        }


        setCheckGiveupButtons();

    }

    private void setTabListener() {
        logs.debug("Setting Tab Listener");

        tabPane.addChangeListener(new ChangeListener() {
            // Set the Tab of Node Editor according to the finished Tabs
            public void stateChanged(ChangeEvent e) {

                // If clicking on same Tab - Do nothing
                if (tabPane.getSelectedIndex() == selectedTab) {
                    extraTabEvent = false;
                    return;
                }
                activityLogs.debug("User Trying to Change Tab to " + getTabName(tabPane.getSelectedIndex())
                        + " Previous Tab was " + getTabName(selectedTab));

                if (extraTabEvent) {
                    logs.debug("Exiting because of extraTabEvent");
                    extraTabEvent = false;
                    return;
                }

                if (ApplicationContext.getAppMode().equals("AUTHOR")) {
                    processEditorInput();
                } else {
                    if (!isCurrentPanelChecked() && ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")) {
                        activityLogs.debug("User tried switching Tab without using Check or Giveup ");
                        setEditorMessage("Please use Check or Giveup buttons before proceeding.", true);
                        tabPane.setSelectedIndex(selectedTab);
                        return;
                    }
                }

                switch (tabPane.getSelectedIndex()) {
                case DESCRIPTION:
                    activityLogs.debug("User Is in the Description Tab ");
                    setEditorMessage("", true);
                    selectedTab = DESCRIPTION;
                    if (currentVertex.getDescriptionStatus().equals(Vertex.DescriptionStatus.CORRECT)
                            || currentVertex.getDescriptionStatus().equals(Vertex.DescriptionStatus.GAVEUP)) {
                        dPanel.setEditableTree(false);
                    }
                    break;

                case PLAN:
                    if (pPanel.isViewEnabled()) {
                        activityLogs.debug("User Is in the Plan Tab ");
                        setEditorMessage("", true);
                        selectedTab = PLAN;
                        if (currentVertex.getPlanStatus().equals(Vertex.PlanStatus.CORRECT)
                                || currentVertex.getPlanStatus().equals(Vertex.PlanStatus.GAVEUP)) {
                            pPanel.setEditableRadio(false);

                        }

                        addHelpBalloon(currentVertex.getName(), "onLoad", "Plan");  //not working
                    } else {
                        extraTabEvent = true;
                        tabPane.setSelectedIndex(selectedTab);
                        return;
                    }
                    break;
                    
                case INPUTS:
                    if (iPanel.isViewEnabled()) {
                        activityLogs.debug("User Is in the Inputs Tab ");
                        setEditorMessage("", true);
                        selectedTab = INPUTS;
                        if (currentVertex.getInputsStatus().equals(Vertex.InputsStatus.CORRECT)
                                || currentVertex.getInputsStatus().equals(Vertex.InputsStatus.GAVEUP)) {
                            iPanel.setEditableInputs(false);
                        }
                        addHelpBalloon(currentVertex.getName(), "onLoad", "INPUTS");
                    } else {
                        extraTabEvent = true;
                        tabPane.setSelectedIndex(selectedTab);
                        return;
                    }
                    break;
                    
                case CALCULATIONS:
                    if (cPanel.isViewEnabled()) {
                        activityLogs.debug("User Is in the Calculations Tab ");
                        setEditorMessage("", true);
                        selectedTab = CALCULATIONS;
                        if (currentVertex.getCalculationsStatus().equals(Vertex.CalculationsStatus.CORRECT)
                                || currentVertex.getCalculationsStatus().equals(Vertex.CalculationsStatus.GAVEUP)) {
                            cPanel.setEditableCalculations(false);
                        }
                        addHelpBalloon(currentVertex.getName(), "onLoad", "CALCULATIONS");
                    } else {
                        extraTabEvent = true;
                        tabPane.setSelectedIndex(selectedTab);
                        return;
                    }
                    break;
                }
                setCheckGiveupButtons();
                logs.debug("Tab Stage Changed Action - Ends");
            }
        });
        logs.debug("Setting Tab Listener -Ends");
    }

    private boolean isCurrentPanelChecked() {
        if (selectedTab == CALCULATIONS
                && currentVertex.getCalculationsStatus().equals(Vertex.CalculationsStatus.UNDEFINED)) {
            return false;
        } else if (selectedTab == INPUTS
                && currentVertex.getInputsStatus().equals(Vertex.InputsStatus.UNDEFINED)) {
            return false;
        } else if (selectedTab == PLAN
                && currentVertex.getPlanStatus().equals(Vertex.PlanStatus.UNDEFINED)) {
            return false;
        } else if (selectedTab == DESCRIPTION
                && currentVertex.getDescriptionStatus().equals(Vertex.DescriptionStatus.UNDEFINED)) {
            return false;
        }
        return true;
    }

    private boolean processEditorInput() {
        switch(selectedTab){
        case DESCRIPTION:
            if (dPanel.processDescriptionPanel()) {
                logs.debug("Saving Description Panel");
                getInputsPanel().updateNodeDescription();
                currentVertex.setDescriptionStatus(Vertex.DescriptionStatus.CORRECT);
                editorMsgLabel.setText("");
            } else {
                extraTabEvent = true;
                currentVertex.setDescriptionStatus(Vertex.DescriptionStatus.INCORRECT);
                tabPane.setSelectedIndex(DESCRIPTION);
                return false;
            }
            break;
        case PLAN:

            if (pPanel.processPlanPanel()) {
                logs.debug("Saving PLAN Panel");
                currentVertex.setPlanStatus(Vertex.PlanStatus.CORRECT);
                editorMsgLabel.setText("");
            } else {
                extraTabEvent = true;
                currentVertex.setPlanStatus(Vertex.PlanStatus.INCORRECT);
                tabPane.setSelectedIndex(PLAN);
                return false;
            }
            break;
        case INPUTS:

            if (iPanel.processInputsPanel()) {
                logs.debug("Saving INPUTS Panel");
                editorMsgLabel.setText("");
                currentVertex.setInputsStatus(Vertex.InputsStatus.CORRECT);
                cPanel.initPanel();
            } else {
                extraTabEvent = true;
                tabPane.setSelectedIndex(INPUTS);
                currentVertex.setInputsStatus(Vertex.InputsStatus.INCORRECT);
                return false;
            }
            break;
        case CALCULATIONS:

            if (cPanel.processCalculationsPanel()) {
                logs.debug("Saving CALCULATIONS Panel");
                editorMsgLabel.setText("");
                currentVertex.setCalculationsStatus(Vertex.CalculationsStatus.CORRECT);
            } else {
                extraTabEvent = true;
                tabPane.setSelectedIndex(CALCULATIONS);
                currentVertex.setCalculationsStatus(Vertex.CalculationsStatus.INCORRECT);
                return false;
            }
        }

        return true;
    }

    /**
     * Method responsible for Enabling and Disabling Check/Giveup buttons based
     * on the use case
     */
    private void setCheckGiveupButtons() {
        logs.debug("Setting Check and Giveup Button for Tab " + getTabName(selectedTab));

        if ((ApplicationContext.getAppMode().equalsIgnoreCase("STUDENT") || 
                ApplicationContext.getAppMode().equalsIgnoreCase("COACHED"))) {
            logs.debug("Enabling Check and Giveup");
            this.checkButton.setEnabled(true);
            this.giveUpButton.setEnabled(true);

            String taskPhase = ApplicationContext.getCorrectSolution().getPhase();

            // Disable Giveup in Challege tasks
            if (taskPhase.equalsIgnoreCase("Challenge")) {
                this.giveUpButton.setEnabled(false);
            }

            switch(selectedTab){
            case DESCRIPTION:
               if (currentVertex.getDescriptionStatus().equals(Vertex.DescriptionStatus.GAVEUP)) {
                   giveUpButton.setEnabled(false);
                   checkButton.setEnabled(false);
               }
               break;
            case PLAN:
                if (currentVertex.getPlanStatus().equals(Vertex.PlanStatus.GAVEUP)) {
                    giveUpButton.setEnabled(false);
                    checkButton.setEnabled(false);
                }
                break;
            case INPUTS:
                if (currentVertex.getInputsStatus().equals(Vertex.InputsStatus.GAVEUP)) {
                    giveUpButton.setEnabled(false);
                    checkButton.setEnabled(false);
                }
                break;
            case CALCULATIONS:
                if (currentVertex.getCalculationsStatus().equals(Vertex.CalculationsStatus.GAVEUP)) {
                    giveUpButton.setEnabled(false);
                    checkButton.setEnabled(false);
                }
                break;
            }

        } else {
            logs.debug("Disabling Check and Giveup");
            this.checkButton.setEnabled(false);
            this.giveUpButton.setEnabled(false);
        }
    }

    public void setEditorMessage(String msg, boolean err) {
        editorMsgLabel.setText(msg);
        if (err) {
            editorMsgLabel.setForeground(Color.RED);
        } else {
            editorMsgLabel.setForeground(Color.BLUE);
        }
        editorMsgLabel.setVisible(true);
    }

    private void checkDescriptionPanel(TaskSolution correctSolution) {
        // Save Description Panel Information in the Vertex Object
        if (!dPanel.processDescriptionPanel()) {
            return;
        }

        if (correctSolution.checkNodeName(dPanel.getNodeName())) {
            currentVertex.setDescriptionStatus(Vertex.DescriptionStatus.CORRECT);
            //graphPane.getMainFrame().getMainMenu().getModelMenu().addDeleteNodeMenu();
            setEditorMessage("", false);
            dPanel.setTextFieldBackground(Color.GREEN);
            activityLogs.debug("User entered correct description");
            dPanel.setEditableTree(false);
            tabPane.setEnabledAt(PLAN, true);
            tabPane.setForegroundAt(PLAN, Color.BLACK);
        } else {
            currentVertex.setDescriptionStatus(Vertex.DescriptionStatus.INCORRECT);
            dPanel.setTextFieldBackground(Color.RED);
            setEditorMessage("That quantity is not used in the correct model. Please select another description.", true);
            activityLogs.debug("User entered incorrect description");
        }

        setTitle(currentVertex.getName());
        validate();
        repaint();
    }

    private void checkDescriptionPanelCoached(TaskSolution correctSolution) {
        // Save Description Panel Information in the Vertex Object
        if (!dPanel.processDescriptionPanel()) {
            return;
        }
        int solutionCheck = correctSolution.checkNodeNameOrdered(dPanel.getNodeName());
        if (solutionCheck == 1) {
            currentVertex.setDescriptionStatus(Vertex.DescriptionStatus.CORRECT);
            //graphPane.getMainFrame().getMainMenu().getModelMenu().addDeleteNodeMenu();
            setEditorMessage("", false);
            dPanel.setTextFieldBackground(Color.GREEN);
            activityLogs.debug("User entered correct description");
            dPanel.setEditableTree(false);
            //ApplicationContext.nextCurrentOrder();
            ApplicationContext.removeNextNodes(currentVertex.getName());
            ApplicationContext.setNextNodes(currentVertex.getName());
            tabPane.setEnabledAt(PLAN, true);
            tabPane.setForegroundAt(PLAN, Color.BLACK);
            addHelpBalloon(currentVertex.getName(), "descCheckDemo", "DESCRIPTION");
        } else if (solutionCheck == 2) {
            dPanel.setTextFieldBackground(Color.CYAN);
            setEditorMessage("That quantity used in this model, but now is not the right time to define it. Please select another description.", true);
            activityLogs.debug("User entered description out of order");
            addHelpBalloon(ApplicationContext.getFirstNextNode(), "onLoad", "DESCRIPTION");
        } else {
            currentVertex.setDescriptionStatus(Vertex.DescriptionStatus.INCORRECT);
            dPanel.setTextFieldBackground(Color.RED);
            setEditorMessage("That quantity is not used in the correct model. Please select another description.", true);
            activityLogs.debug("User entered incorrect description");

        }


        setTitle(currentVertex.getName());
        validate();
        repaint();
    }

    private void checkPlanPanel(TaskSolution correctSolution) {
        logs.debug("Checking Plan Panel");
        if (correctSolution.checkNodePlan(dPanel.getNodeName(), pPanel.getSelectedPlan())) {
            pPanel.setSelectedPlanBackground(Color.GREEN);
            setEditorMessage("", false);
            currentVertex.setPlanStatus(Vertex.PlanStatus.CORRECT);
            activityLogs.debug("User entered correct Plan");
            iPanel.updateNodeDescription();
            pPanel.setEditableRadio(false);
            tabPane.setEnabledAt(INPUTS, true);
            tabPane.setForegroundAt(INPUTS, Color.BLACK);
            if (ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")) {
                addHelpBalloon(currentVertex.getName(), "descCheckDemo", "PLAN");
            }
        } else {
            pPanel.setSelectedPlanBackground(Color.RED);
            setEditorMessage("You have selected incorrect Plan for this Node.", true);
            activityLogs.debug("User entered incorrect Plan");
            currentVertex.setPlanStatus(Vertex.PlanStatus.INCORRECT);
        }
        // Save Selected Plan to the Vertex Object
        pPanel.processPlanPanel();
        activityLogs.debug("User checked plan panel with node plan as " + pPanel.getSelectedPlan());

    }

    private void checkInputsPanel(TaskSolution correctSolution) {
        iPanel.processInputsPanel();
        iPanel.setInputsTypeBackground(new Color(240, 240, 240));
        iPanel.setInputValuesBackground(new Color(240, 240, 240));

        int result = -1;

        if (iPanel.getValueButtonSelected()) {
            result = correctSolution.checkNodeInputs(dPanel.getNodeName(), null);
        } else if (iPanel.getInputsButtonSelected()) {
            result = correctSolution.checkNodeInputs(dPanel.getNodeName(), iPanel.getSelectedInputsList());
        }

        if (result == 0 || result == 3) {
            if (result == 0) {
                iPanel.setInputsTypeBackground(Color.GREEN);
            } else {
                iPanel.setInputsTypeBackground(Color.GREEN);
                iPanel.setInputValuesBackground(Color.GREEN);
            }

            setEditorMessage("", false);
            activityLogs.debug("User entered correct Inputs");
            currentVertex.setInputsStatus(Vertex.InputsStatus.CORRECT);
            iPanel.setEditableInputs(false);
            tabPane.setEnabledAt(CALCULATIONS, true);
            tabPane.setForegroundAt(CALCULATIONS, Color.BLACK);
            if (ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")) {
                addHelpBalloon(currentVertex.getName(), "descCheckDemo", "INPUTS");
            }
        } else {
            if (result == 1 || result == -1) {
                iPanel.setInputsTypeBackground(Color.RED);
            } else {
                iPanel.setInputsTypeBackground(Color.GREEN);
                iPanel.setInputValuesBackground(Color.RED);
            }

            activityLogs.debug("User entered incorrect Inputs");
            if (iPanel.getSelectedInputsList().isEmpty() & iPanel.getInputsButtonSelected()) {
                setEditorMessage("No inputs are created or selected.", true);
            } else {
                setEditorMessage("Your Inputs are Incorrect.", true);
            }
            tabPane.setEnabledAt(CALCULATIONS, true);
            tabPane.setForegroundAt(CALCULATIONS, Color.BLACK);
            currentVertex.setInputsStatus(Vertex.InputsStatus.INCORRECT);
        }
        activityLogs.debug("User checked Inputs Panel with Type: " + currentVertex.getVertexType());
        cPanel.initPanel();
    }

    private void checkCalculationsPanel(TaskSolution correctSolution) {
        // Check Parsing Errors and Set Student's Equation in Vertex
        cPanel.processCalculationsPanel();
        cPanel.setCheckedBackground(new Color(240, 240, 240));
        // Check for fixed value
        if (correctSolution.checkNodeCalculations(currentVertex)) {
            cPanel.setCheckedBackground(Color.GREEN);
            setEditorMessage("", false);
            activityLogs.debug("User entered correct Calculations.");
            currentVertex.setCalculationsStatus(Vertex.CalculationsStatus.CORRECT);
            cPanel.setEditableCalculations(false);
            buttonCancel.setEnabled(true);
            if (ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")) {
                addHelpBalloon(currentVertex.getName(), "descCheckDemo", "CALCULATIONS");
            }
        } else {
            cPanel.setCheckedBackground(Color.RED);
            setEditorMessage("You Calculations are Inorrect.", true);
            activityLogs.debug("User entered incorrect Calculations.");
            currentVertex.setCalculationsStatus(Vertex.CalculationsStatus.INCORRECT);
        }

        activityLogs.debug("User checked calculations panel with Nodetype: " + currentVertex.getVertexType()
                + " Initial Value : " + currentVertex.getInitialValue() + " Calculations as " + currentVertex.getEquation());
    }

    private void processAuthorModeOKAction() {
        logs.debug("Processing Author Mode OK Button Action");
        if (dPanel.processDescriptionPanel()) {
            activityLogs.debug(dPanel.printDescriptionPanelDetails());
            if (pPanel.processPlanPanel()) {
                activityLogs.debug(pPanel.printPlanPanel());
                if (iPanel.processInputsPanel()) {
                    logs.debug("Setting Inputs completely");
                    activityLogs.debug(iPanel.printInputsPanel());
                    currentVertex.setInputsStatus(Vertex.InputsStatus.CORRECT);
                    if (cPanel.processCalculationsPanel()) {
                        currentVertex.setCalculationsStatus(Vertex.CalculationsStatus.CORRECT);
                    } else {
                        currentVertex.setCalculationsStatus(Vertex.CalculationsStatus.INCORRECT);
                    }
                    activityLogs.debug(cPanel.printCalculationPanel());
                } else {
                    logs.debug("Setting input incompletely");
                    currentVertex.setInputsStatus(Vertex.InputsStatus.INCORRECT);
                }
                graphPane.repaint();
            }
            this.dispose();
        }
    }

    private void processTutorModeOKAction() {
        logs.debug("Processing Tutor Mode OK Button Action");
        if (!isCheckGiveupButtonUsed()) {
            activityLogs.debug("User pressed OK without using Check or Giveup button");
            return;
        }
        activityLogs.debug("Closing NodeEditor because of OK action.");
        this.dispose();
    }

    private boolean isCheckGiveupButtonUsed() {
        logs.debug("Verifying if Check or Giveup button was used");

        if (tabPane.getSelectedIndex() == DESCRIPTION
                && currentVertex.getDescriptionStatus().equals(Vertex.DescriptionStatus.UNDEFINED)) {
            showUndefinedTabErr();
            return false;
        } else if (tabPane.getSelectedIndex() == PLAN
                && currentVertex.getPlanStatus().equals(Vertex.PlanStatus.UNDEFINED)) {
            showUndefinedTabErr();
            return false;
        } else if (tabPane.getSelectedIndex() == INPUTS
                && currentVertex.getInputsStatus().equals(Vertex.InputsStatus.UNDEFINED)) {
            showUndefinedTabErr();
            return false;
        } else if (tabPane.getSelectedIndex() == CALCULATIONS
                && currentVertex.getCalculationsStatus().equals(Vertex.CalculationsStatus.UNDEFINED)) {
            showUndefinedTabErr();
            return false;
        }

        return true;
    }

    private void showUndefinedTabErr() {
        this.editorMsgLabel.setText("Please use Check or Giveup buttons before exiting");
    }

    public Vertex getCurrentVertex() {
        return currentVertex;
    }

    public GraphEditorPane getGraphPane() {
        return graphPane;
    }

    public CalculationsPanelView getCalculationsPanel() {
        return cPanel;
    }

    public DescriptionPanelView getDescriptionPanel() {
        return dPanel;
    }

    public InputsPanelView getInputsPanel() {
        return iPanel;
    }

    private void refreshGraphPane() {
        graphPane.getMainFrame().validate();
        graphPane.getMainFrame().repaint();
    }

    /**
     * Make necessary clean up and save graph session when NodeEditor closes
     */
    private void closeNodeEditor() {

        activityLogs.debug("User pressed Close button for Node " + currentVertex.getName());
        // Delete this vertex if its not defined and user hits Cancel
        if (currentVertex.getDescriptionStatus().equals(Vertex.DescriptionStatus.UNDEFINED)
                || currentVertex.getDescriptionStatus().equals(Vertex.DescriptionStatus.INCORRECT)) {
            graphPane.removeSelected();
        }

        activityLogs.debug("Closing NodeEditor because of Close action.");
        if (!ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")) {
            graphPane.getMainFrame().getModelToolBar().enableDeleteNodeButton();
            graphPane.getMainFrame().getMainMenu().getModelMenu().enableDeleteNodeMenu();
        }

        // Save Student's session to server
        PersistenceManager.saveSession();

        graphPane.getMainFrame().addHelpBalloon(currentVertex.getName(), "nodeEditorClose");
        this.dispose();
    }

    private void displayEnterButton() {
        if (ApplicationContext.getAppMode().equalsIgnoreCase("STUDENT") || 
                ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")) {
            buttonOK.hide();
        } else {
            buttonOK.show();
        }
    }

    public void addHelpBalloon(String name, String timing, String panel) {
        logs.debug("Adding Help Bubble for "+panel);
        if (ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")) {
            System.out.println("addhelpballoon passing in " + name);
            List<HelpBubble> bubbles = ApplicationContext.getHelp(name, panel, timing);
            if (!bubbles.isEmpty()) {
                for (HelpBubble bubble : bubbles) {

                    if (panel.equalsIgnoreCase("description")) {
                        new BlockingToolTip(this, bubble, getLabel("dPanel", bubble.getAttachedTo()));
                    } else if (panel.equalsIgnoreCase("plan")) {
                        System.out.println("Trying to add help in Plan. Msg: " + bubble.getMessage() + "  " + bubble.getAttachedTo());
                        System.out.println("comp: " + pPanel.getLabel(bubble.getAttachedTo()));
                        new BlockingToolTip(this, bubble, getLabel("pPanel", bubble.getAttachedTo()));
                    } else if (panel.equalsIgnoreCase("inputs")) {
                        new BlockingToolTip(this, bubble, getLabel("iPanel", bubble.getAttachedTo()));
                    } else if (panel.equalsIgnoreCase("calculations")) {
                        new BlockingToolTip(this, bubble, getLabel("cPanel", bubble.getAttachedTo()));
                    }

                }
            }
        }
    }

    public JComponent getLabel(String panel, String attachedTo) {
        JComponent rPanel = null;
        if (panel.equalsIgnoreCase("dPanel")) {
            rPanel = dPanel.getLabel(attachedTo);
        } else if (panel.equalsIgnoreCase("pPanel")) {
            rPanel = pPanel.getLabel(attachedTo);
        } else if (panel.equalsIgnoreCase("iPanel")) {
            rPanel = iPanel.getLabel(attachedTo);
        } else if (panel.equalsIgnoreCase("cPanel")) {
            rPanel = cPanel.getLabel(attachedTo);
        }
        if (rPanel == null) {
            rPanel = getLabel(attachedTo);
        }
        if (rPanel == null) {
            return null;
        } else {
            return rPanel;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLayeredPane1 = new javax.swing.JLayeredPane();
        jRadioButton1 = new javax.swing.JRadioButton();
        tabPane = new javax.swing.JTabbedPane();
        descriptionPanel = new javax.swing.JPanel();
        planPanel = new javax.swing.JPanel();
        inputsPanel = new javax.swing.JPanel();
        calculationPanel = new javax.swing.JPanel();
        checkButton = new javax.swing.JButton();
        giveUpButton = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        editorMsgLabel = new javax.swing.JLabel();
        bottomSpacer = new javax.swing.JLabel();
        tabPanel = new javax.swing.JLabel();

        jRadioButton1.setText("jRadioButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        tabPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tabPane.setMinimumSize(new java.awt.Dimension(500, 500));
        tabPane.setOpaque(true);
        tabPane.setPreferredSize(new java.awt.Dimension(500, 400));
        tabPane.setRequestFocusEnabled(false);
        tabPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabPaneMouseClicked(evt);
            }
        });
        tabPane.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                tabPaneMouseDragged(evt);
            }
        });

        descriptionPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        descriptionPanel.setFocusable(false);
        descriptionPanel.setPreferredSize(new java.awt.Dimension(506, 615));
        descriptionPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        tabPane.addTab("Description", descriptionPanel);

        org.jdesktop.layout.GroupLayout planPanelLayout = new org.jdesktop.layout.GroupLayout(planPanel);
        planPanel.setLayout(planPanelLayout);
        planPanelLayout.setHorizontalGroup(
            planPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 601, Short.MAX_VALUE)
        );
        planPanelLayout.setVerticalGroup(
            planPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 562, Short.MAX_VALUE)
        );

        tabPane.addTab("Plan", planPanel);

        org.jdesktop.layout.GroupLayout inputsPanelLayout = new org.jdesktop.layout.GroupLayout(inputsPanel);
        inputsPanel.setLayout(inputsPanelLayout);
        inputsPanelLayout.setHorizontalGroup(
            inputsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 601, Short.MAX_VALUE)
        );
        inputsPanelLayout.setVerticalGroup(
            inputsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 562, Short.MAX_VALUE)
        );

        tabPane.addTab("Inputs", inputsPanel);

        org.jdesktop.layout.GroupLayout calculationPanelLayout = new org.jdesktop.layout.GroupLayout(calculationPanel);
        calculationPanel.setLayout(calculationPanelLayout);
        calculationPanelLayout.setHorizontalGroup(
            calculationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 601, Short.MAX_VALUE)
        );
        calculationPanelLayout.setVerticalGroup(
            calculationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 562, Short.MAX_VALUE)
        );

        tabPane.addTab("Calculations", calculationPanel);

        checkButton.setText("Check");
        checkButton.setEnabled(false);
        checkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkButtonActionPerformed(evt);
            }
        });

        giveUpButton.setText("Demo");
        giveUpButton.setActionCommand("Give Up");
        giveUpButton.setEnabled(false);
        giveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                giveUpButtonActionPerformed(evt);
            }
        });

        buttonCancel.setText("Close");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOK.setText("Enter");
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        editorMsgLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        editorMsgLabel.setForeground(new java.awt.Color(255, 0, 0));
        editorMsgLabel.setText("jLabel1");

        tabPanel.setForeground(new java.awt.Color(238, 238, 238));
        tabPanel.setText("Node Editor");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(302, 302, 302)
                .add(tabPanel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(32, 32, 32)
                        .add(editorMsgLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 601, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(14, 14, 14))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(35, 35, 35)
                            .add(checkButton)
                            .add(18, 18, 18)
                            .add(giveUpButton)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(buttonOK)
                            .add(18, 18, 18)
                            .add(buttonCancel))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .addContainerGap()
                            .add(tabPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 622, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(10, 27, Short.MAX_VALUE)
                    .add(bottomSpacer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 596, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(tabPanel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tabPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(editorMsgLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(checkButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(giveUpButton))
                    .add(buttonCancel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(buttonOK))
                .addContainerGap())
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(0, 649, Short.MAX_VALUE)
                    .add(bottomSpacer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 39, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void checkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkButtonActionPerformed
        // Action for Check Button
        logs.debug("Handling Check Action");
        TaskSolution correctSolution = ApplicationContext.getCorrectSolution();

        switch(tabPane.getSelectedIndex()) {
        case DESCRIPTION:
            activityLogs.debug("Check button pressed for Description Panel");
            if (ApplicationContext.getAppMode().equalsIgnoreCase("COACHED")) {
                checkDescriptionPanelCoached(correctSolution);
            } else {

                checkDescriptionPanel(correctSolution);
            }
            break;
            
        case PLAN:
            activityLogs.debug("Check button pressed for Plan Panel");
            checkPlanPanel(correctSolution);
            break;
            
        case INPUTS:
            activityLogs.debug("Check button pressed for Inputs Panel");

            checkInputsPanel(correctSolution);
            break;
            
        case CALCULATIONS:
            activityLogs.debug("Check button pressed for Calculations Panel");
            checkCalculationsPanel(correctSolution);
        }

        // Refreshing Graph
        refreshGraphPane();
    }//GEN-LAST:event_checkButtonActionPerformed

    // This is string name of tab used in problem xml to
    // specify help bubbles and used in logging
    private String getTabName(int id) {
        switch(id) {
        case DESCRIPTION:
            return "DESCRIPTION";
        case PLAN:
            return "PLAN";
        case INPUTS:
            return "INPUTS";
        case CALCULATIONS:
            return "CALCULATIONS";
        } 
        throw new IllegalArgumentException("invalid tab number "+id);
    }

    private void giveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_giveUpButtonActionPerformed
        // Action for Giveup Button
        editorMsgLabel.setText("");
        switch(tabPane.getSelectedIndex()) {
        case DESCRIPTION:
            activityLogs.debug("Giveup button pressed for Description Panel");
            List<HelpBubble> bubbles = ApplicationContext.getHelp(ApplicationContext.getFirstNextNode(), "DESCRIPTION", "descFilled");
            if (!bubbles.isEmpty()) {
                for (HelpBubble bubble : bubbles) {
                    bubble.setDisplayed(true);
                }
            }
            dPanel.giveUpDescriptionPanel();
            dPanel.processDescriptionPanel();
            currentVertex.setDescriptionStatus(Vertex.DescriptionStatus.GAVEUP);
            setTitle(currentVertex.getName());
            //graphPane.getMainFrame().getMainMenu().getModelMenu().addDeleteNodeMenu();
            validate();
            repaint();
            currentVertex.setDescriptionStatus(Vertex.DescriptionStatus.GAVEUP);
            dPanel.setEditableTree(false);
            tabPane.setEnabledAt(PLAN, true);
            tabPane.setForegroundAt(PLAN, Color.BLACK);
            addHelpBalloon(currentVertex.getName(), "descCheckDemo", "DESCRIPTION");
            break;
            
        case PLAN:
            activityLogs.debug("Giveup button pressed for Plan Panel");
            pPanel.giveUpPlanPanel();
            pPanel.processPlanPanel();
            currentVertex.setPlanStatus(Vertex.PlanStatus.GAVEUP);
            iPanel.updateNodeDescription();
            pPanel.setEditableRadio(false);
            tabPane.setEnabledAt(INPUTS, true);
            tabPane.setForegroundAt(INPUTS, Color.BLACK);
            break;
            
        case INPUTS:
            activityLogs.debug("Giveup button pressed for Inputs Panel");

            iPanel.setInputsTypeBackground(new Color(240, 240, 240));
            iPanel.setInputValuesBackground(new Color(240, 240, 240));
            if (iPanel.giveUpInputsPanel()) {
                iPanel.processInputsPanel();
                currentVertex.setInputsStatus(Vertex.InputsStatus.GAVEUP);
            } else {
                currentVertex.setInputsStatus(Vertex.InputsStatus.INCORRECT);
            }
            cPanel.initPanel();
            iPanel.setEditableInputs(false);
            tabPane.setEnabledAt(CALCULATIONS, true);
            tabPane.setForegroundAt(CALCULATIONS, Color.BLACK);
            break;
            
        case CALCULATIONS:
            activityLogs.debug("Giveup button pressed for Calculations Panel");
            cPanel.setCheckedBackground(new Color(240, 240, 240));
            if (cPanel.giveUpCalculationsPanel()) {
                cPanel.processCalculationsPanel();
                currentVertex.setCalculationsStatus(Vertex.CalculationsStatus.GAVEUP);
                cPanel.setEditableCalculations(false);
                buttonCancel.setEnabled(true);
            } else {
                currentVertex.setCalculationsStatus(Vertex.CalculationsStatus.INCORRECT);
            }
            break;
            
        }
        checkButton.setEnabled(false);
        giveUpButton.setEnabled(false);
        refreshGraphPane();
    }//GEN-LAST:event_giveUpButtonActionPerformed
    public JComponent getLabel(String label) {

        Map<String, JComponent> map = new HashMap<String, JComponent>();
        map.put("tabPane", tabPane);
        map.put("checkButton", checkButton);
        map.put("giveUpButton", giveUpButton);
        map.put("buttonCancel", buttonCancel);
        map.put("editorMsgLabel", editorMsgLabel);
        map.put("tabPanel", tabPanel);
        if (map.containsKey(label)) {
            return map.get(label);
        } else {
            return null;
        }
    }

    public void refreshInputs() {
        iPanel.refreshInputs();
    }

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        closeNodeEditor();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        activityLogs.debug("User pressed Enter button for Node '" + currentVertex.getName() + "'");
        /*
         * if (ApplicationContext.getAppMode().equalsIgnoreCase("STUDENT")) {
         * processTutorModeOKAction(); } else
         */ {
            processAuthorModeOKAction();
        }

    }//GEN-LAST:event_buttonOKActionPerformed

    private void tabPaneMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabPaneMouseDragged
    }//GEN-LAST:event_tabPaneMouseDragged

    private void tabPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabPaneMouseClicked
    }//GEN-LAST:event_tabPaneMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bottomSpacer;
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOK;
    private javax.swing.JPanel calculationPanel;
    private javax.swing.JButton checkButton;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JLabel editorMsgLabel;
    private javax.swing.JButton giveUpButton;
    private javax.swing.JPanel inputsPanel;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JPanel planPanel;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JLabel tabPanel;
    // End of variables declaration//GEN-END:variables
}
