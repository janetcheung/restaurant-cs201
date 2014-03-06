package restaurant.gui;

import restaurant.CookAgent;
import restaurant.CustomerAgent;
import restaurant.MarketAgent;
import restaurant.WaiterAgent;
import restaurant.gui.WaiterGui.waiterState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import agent.Agent;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
/**
 * Main GUI class.
 * Contains the main frame and subsequent panels
 */

public class RestaurantGui extends JFrame implements ActionListener {
  
	JFrame animationFrame = new JFrame("Restaurant Animation");
	AnimationPanel animationPanel = new AnimationPanel();
	JPanel contentPane = new JPanel();
    private RestaurantPanel restPanel = new RestaurantPanel(this);
    
    private ImageIcon host;
    private JPanel restAndInfoPanel = new JPanel();
    private JPanel scenarioPanel;
    
    private List<JButton>scenarioButtons = new ArrayList<JButton>();
    private JButton pauseButton = new JButton("Pause");
    private boolean isPaused = false;
    private JPanel infoPanel;
    private JLabel infoLabel; //part of infoPanel
    private JCheckBox stateCB;//part of infoLabel
    private JCheckBox poorCB;//part of infoPanel
    private JCheckBox richCB;//part of infoPanel
    private JButton stateB;
    private Object currentPerson;/* Holds the agent that the info is about.
    								Seems like a hack */
//    private int xTable;
//    private int yTable;
//    int NTABLES = 1;

    /**
     * Constructor for RestaurantGui class.
     * Sets up all the gui components.
     */
    
    public RestaurantGui() {
        int WINDOWX = 1000;
        int WINDOWY = 700;

    	setBounds(50, 50, WINDOWX, WINDOWY);
    	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(1, 0, 0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane);
	
        splitPane.setRightComponent(animationPanel);
        
        // Now, setup the info panel
        restAndInfoPanel.setLayout(new BoxLayout(restAndInfoPanel,BoxLayout.Y_AXIS));
        
        infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));

        stateCB = new JCheckBox();
        stateCB.setVisible(false);
        stateCB.addActionListener(this);
        
        stateB = new JButton();
        stateB.setVisible(false);
        stateB.addActionListener(this);
        
        poorCB = new JCheckBox();
        poorCB.setVisible(false);
        poorCB.addActionListener(this);
        
        richCB = new JCheckBox();
        richCB.setVisible(false);
        richCB.addActionListener(this);
        
        infoLabel = new JLabel(); 
        infoLabel.setText("<html><pre><i>Click Add to make customers</i></pre></html>");
        infoPanel.add(infoLabel);
        infoPanel.add(stateCB);
        infoPanel.add(stateB);
        infoPanel.add(poorCB);
        infoPanel.add(richCB);
        
        //set up Scenario panel
        scenarioPanel = new JPanel();
        scenarioPanel.setBorder(BorderFactory.createTitledBorder("Scenarios"));
        scenarioPanel.setLayout(new GridLayout(3,3,3,3));
        addScenarios("Deplete Cook Stock (reorder from 1 market)");
        addScenarios("Deplete Cook Stock (reorder from 2 markets)");
       
        for (JButton jb : scenarioButtons){
        	jb.setVisible(true);
        	scenarioPanel.add(jb);
        }
        
        pauseButton.addActionListener(this);
        
        restAndInfoPanel.add(restPanel);
        restAndInfoPanel.add(infoPanel);
        restAndInfoPanel.add(scenarioPanel);
        restAndInfoPanel.add(pauseButton);
        splitPane.setLeftComponent(restAndInfoPanel);
        
        
    }
   
    public void addScenarios(String s){
    	JButton button = new JButton(s);
    	button.addActionListener(this);
    	scenarioButtons.add(button);
    	
    }
    /**
     * updateInfoPanel() takes the given customer (or, for v3, Host) object and
     * changes the information panel to hold that person's info.
     *
     * @param person customer (or waiter) object
     */
    public void updateInfoPanel(Object person) {
        currentPerson = person;

        if (person instanceof CustomerAgent) {
            CustomerAgent customer = (CustomerAgent) person;
            stateCB.setVisible(true);
            stateB.setVisible(false);
            poorCB.setVisible(true);
            richCB.setVisible(true);

            stateCB.setText("Hungry?");
            if(!customer.getGui().isHungry()){
            	stateCB.isEnabled();
            }
            
            stateCB.setSelected(customer.getGui().isHungry());
           	stateCB.setEnabled(!customer.getGui().isHungry());
            infoLabel.setText(
               "<html><pre>     Name: " + customer.getName() + " </pre></html>");  
            
            poorCB.setText("Make poor");
            if(!customer.getGui().isPoor()){
            	poorCB.isEnabled();
            }
            
            poorCB.setSelected(customer.getGui().isPoor());
            poorCB.setEnabled(!customer.getGui().isPoor());
            
            richCB.setText("Make rich");
            if(!customer.getGui().isPoor()){
            	richCB.isEnabled();
            }
            richCB.setSelected(customer.getGui().isPoor());
            richCB.setEnabled(!customer.getGui().isPoor());
        }
        
        else if (person instanceof WaiterAgent) {
        	WaiterAgent waiter = (WaiterAgent) person;
        	stateB.setVisible(true);
        	stateCB.setVisible(false);
        	poorCB.setVisible(false);
        	richCB.setVisible(false);
        	
        	if (!waiter.getGui().onBreak()){
            	stateB.setText("Break?");
            	stateB.isEnabled();
        	}
        	stateB.setSelected(waiter.getGui().onBreak());
        	stateB.setEnabled(!waiter.getGui().onBreak());
        	
        	infoLabel.setText(
               "<html><pre>     Name: " + waiter.getName() + " </pre></html>");
        }
        infoPanel.validate();
    }
    	
    
    /**
     * Action listener method that reacts to the checkbox being clicked;
     * If it's the customer's checkbox, it will make him hungry
     * For v3, it will propose a break for the waiter.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == stateCB) {
            if (currentPerson instanceof CustomerAgent) {
                CustomerAgent c = (CustomerAgent) currentPerson;
                c.getGui().setHungry();
                stateCB.setEnabled(false);
            }
        }
        else if (e.getSource() == poorCB){
        	if (currentPerson instanceof CustomerAgent){
        		CustomerAgent c = (CustomerAgent) currentPerson;
        		c.getGui().setPoor();
        		poorCB.setEnabled(false);
        		richCB.setEnabled(false);
        	}
        }
        else if (e.getSource() == richCB){
        	if (currentPerson instanceof CustomerAgent){
        		CustomerAgent c = (CustomerAgent) currentPerson;
        		c.getGui().setRich();
        		richCB.setEnabled(false);
        		poorCB.setEnabled(false);
        	}
        }
        else if (e.getSource() == stateB) {
            if (currentPerson instanceof WaiterAgent){
            	WaiterAgent w = (WaiterAgent) currentPerson;

            	if (stateB.getText() == "Break?"){
            		//if waiter is requesting break, stateButton is greyed out
            		if (w.getGui().getState() == waiterState.working){
            			w.getGui().requestBreak();
            			stateB.setEnabled(false);
            		}
            	}
            }
        }
        else if (e.getSource() == scenarioButtons.get(0)){
        	if (currentPerson instanceof WaiterAgent){
        		WaiterAgent w = (WaiterAgent) currentPerson;

        		w.tellCookDepleteOptions();
        	}
        	else System.out.println("Please select a waiter");
        }
        else if (e.getSource() == scenarioButtons.get(1)){
        	if (currentPerson instanceof WaiterAgent){
        		WaiterAgent w = (WaiterAgent) currentPerson;
        		w.tellCookDepleteOption("Steak");
        		w.tellCookDepleteMarkets();
        	}
        	else System.out.println("Please select a waiter");
        }
        else if (e.getSource() == pauseButton) {
        	if (isPaused){
        		isPaused = false;
        		pauseButton.setText("Pause");
        		restPanel.restart();
        	}
        	else if (!isPaused){
        		isPaused = true;
        		pauseButton.setText("Restart");
        		restPanel.pause();
        		
        	}
        	
        }
    }
    /**
     * Message sent from a customer gui to enable that customer's
     * "I'm hungry" checkbox.
     *
     * @param c reference to the customer
     */
    public void setCustomerEnabled(CustomerAgent c) {
        if (currentPerson instanceof CustomerAgent) {
            CustomerAgent cust = (CustomerAgent) currentPerson;
            
            for(ListPanel.custPair temp: restPanel.getCustomerPanel().getCustomerPair()){
            	if(temp.getCustomer().equals(c)){
            		temp.getBox().setEnabled(true);
            		temp.getBox().setSelected(false);
            		stateCB.setEnabled(true);
            		stateCB.setSelected(false);
            		poorCB.setEnabled(true);
            		poorCB.setSelected(false);
            		break;
            	}
            	updateInfoPanel(temp);
            }
            if (c.equals(cust)) {
                stateCB.setEnabled(true);
                stateCB.setSelected(false);
                poorCB.setEnabled(true);
                poorCB.setSelected(false);
                updateInfoPanel(cust);
            }
            
        }
    }
    
    public void setWaiterEnabled(WaiterAgent w) {
        if (currentPerson instanceof WaiterAgent) {
            WaiterAgent waiter = (WaiterAgent) currentPerson;
            if (w.equals(waiter)) {
                stateB.setEnabled(true);
                stateB.setSelected(false);
            	updateInfoPanel(w);
            }
            
        }
    }
    /**
     * Main routine to get gui started
     */
    public static void main(String[] args) {
        RestaurantGui gui = new RestaurantGui();
        gui.setTitle("JanutterButter Restaurant");
        gui.setVisible(true);
        gui.setResizable(false);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

