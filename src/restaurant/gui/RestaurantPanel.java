package restaurant.gui;

import restaurant.CashierAgent;
import restaurant.CookAgent;
import restaurant.CustomerAgent;
import restaurant.HostAgent;
import restaurant.MarketAgent;
import restaurant.WaiterAgent;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Panel in frame that contains all the restaurant information,
 * including host, cook, waiters, and customers.
 */
public class RestaurantPanel extends JPanel {

    //Host, cook, waiters and customers
    private HostAgent host = new HostAgent("Sarah");
    private ArrayList <WaiterAgent>waiters = new ArrayList<WaiterAgent>();
    private ArrayList <MarketAgent>markets = new ArrayList<MarketAgent>();
    private CookAgent cook = new CookAgent("Cook");
    private CashierAgent cashier = new CashierAgent("Cashier");

    private HostGui hostGui = new HostGui(host);
    private ArrayList<WaiterGui> waiterGuis = new ArrayList<WaiterGui>();
    private CookGui cookGui;
    private Vector<CustomerAgent> customers = new Vector<CustomerAgent>();

    private JPanel restLabel = new JPanel();
    private ListPanel customerPanel = new ListPanel(this, "Customers");
    private ListPanel waiterPanel = new ListPanel(this, "Waiters");
	private JButton pauseButton = new JButton("Pause");

    private JPanel group = new JPanel();
    private static final int nMarkets = 3;

    private RestaurantGui gui; //reference to main gui

    final static int gridLayoutX = 1;
    final static int gridLayoutY = 2;
    final static int gridLayoutGAP = 10;
    public RestaurantPanel(RestaurantGui gui) {
        this.gui = gui;
        
        host.setGui(hostGui);
        gui.animationPanel.addGui(hostGui);
        host.startThread();
        
        cookGui = new CookGui(cook, gui);
        cook.setGui(cookGui);
        gui.animationPanel.addGui(cookGui);
        cook.startThread();      
        
        cashier.startThread();
        
        for(int i = 0; i < nMarkets; i++){
        	MarketAgent m = new MarketAgent("Market " +i, i);
        	m.setCook(cook);
        	m.setCashier(cashier);
        	markets.add(m);
        	cook.addMarket(m);
        	cashier.addMarket(m);
        	m.startThread();
        }
        
        setLayout(new GridLayout(gridLayoutX, gridLayoutY, gridLayoutGAP, gridLayoutGAP));
        group.setLayout(new GridLayout(gridLayoutX, gridLayoutY, gridLayoutGAP, gridLayoutGAP));
        group.add(customerPanel);
        group.add(waiterPanel);
        

        initRestLabel();
        add(restLabel);
        add(group);
    }

    /**
     * Sets up the restaurant label that includes the menu,
     * and host and cook information
     */
    private void initRestLabel() {
        JLabel label = new JLabel();
        restLabel.setLayout(new BorderLayout());
        label.setText(
                "<html><h3><u>Tonight's Staff</u></h3><table><tr><td>host:</td><td>" + host.getName() + "</td></tr></table><h3><u> Menu</u></h3><table><tr><td>Steak</td><td>$15.99</td></tr><tr><td>Chicken</td><td>$10.99</td></tr><tr><td>Salad</td><td>$5.99</td></tr><tr><td>Pizza</td><td>$8.99</td></tr></table><br></html>");

        restLabel.setBorder(BorderFactory.createRaisedBevelBorder());
        restLabel.add(label, BorderLayout.CENTER);
        restLabel.add(new JLabel("               "), BorderLayout.EAST);
        restLabel.add(new JLabel("               "), BorderLayout.WEST);
    }

    /**
     * When a customer or waiter is clicked, this function calls
     * updatedInfoPanel() from the main gui so that person's information
     * will be shown
     *
     * @param type indicates whether the person is a customer or waiter
     * @param name name of person
     */
    public void showInfo(String type, String name) {

        if (type.equals("Customers")) {

            for (int i = 0; i < customers.size(); i++) {
                CustomerAgent temp = customers.get(i);
                if (temp.getName() == name)
                    gui.updateInfoPanel(temp);
            }
        }
        else if (type.equals("Waiters")){
        	for (int i = 0; i < waiters.size(); i++){
        		WaiterAgent temp = waiters.get(i);
        		if(temp.getName() == name)
        			gui.updateInfoPanel(temp);
        	}
        }
    }
    
    public void pause(){
    	host.pause();
    	cook.pause();
    	cashier.pause();
    	
    	for(WaiterAgent w: waiters)
    		w.pause();
    	for(CustomerAgent c: customers)
    		c.pause();
    	for(MarketAgent m: markets)
    		m.pause();
    	
    	System.out.println("Pausing.  Finishing scheduled actions");
    }
    
    public void restart() {
    	host.restart();
    	cook.restart();
    	cashier.pause();
    	
    	for (WaiterAgent w: waiters)
    		w.restart();
    	for (CustomerAgent c: customers)
    		c.restart();
    	for (MarketAgent m: markets)
    		m.restart();
    	
    	System.out.println("Restarted");
    }
    /**
     * Adds a customer or waiter to the appropriate list
     *
     * @param type indicates whether the person is a customer or waiter (later)
     * @param name name of person
     */
    public void addPerson(String type, String name) {

//    	if (type.equals("Customers")) {
//    		CustomerAgent c = new CustomerAgent(name);	
//    		CustomerGui g = new CustomerGui(c, gui);
//    		
//    		c.setHost(host);
//    		c.setCashier(cashier);
//    		c.setGui(g);
//    		customers.add(c);
//    		c.startThread();
////    		System.out.println("add customer");
//    	}
    	 if (type.equals("Waiters")) {
    		WaiterAgent w = new WaiterAgent(name);
    		WaiterGui wg = new WaiterGui(w, gui);
    		wg.setHomePos(wg.getYHomePos()+ (waiters.size()*30));
    		
//    		System.err.println("wYXHomePos: " + wg.getYHomePos());
//    		System.err.println("wgYPos: " + wg.getYPos());
//    		System.err.println("wgYDestination: " + wg.getYDestination());
    		
    		gui.animationPanel.addGui(wg);
    		w.setHost(host);
    		w.setCook(cook);
    		w.setCashier(cashier);
    		w.setGui(wg);
    		waiters.add(w);
    		host.addWaiter(w);
    		w.startThread();
//    		System.out.println("add waiter");
    		
    		host.setNWaiters(host.getNWaiters()+1);
    	}
    }
    
    //overloaded type of addPerson function
    public CustomerAgent addPanelPerson(String type, String name){
    	if (type.equals("Customers")) {
    		CustomerAgent c = new CustomerAgent(name);	
    		CustomerGui g = new CustomerGui(c, gui);
    		
    		g.setHomePos(g.getXPos() + (customers.size()*30));

//    		System.err.println("cXHomePos: " + g.getXPos());
//    		System.err.println("cXDestination: " + g.getXDestination());
//    		gui.animationPanel.addGui(g);// dw

    		gui.animationPanel.addGui(g);// dw
    		c.setHost(host);
    		c.setCashier(cashier);
    		c.setGui(g);
    		customers.add(c);
    		
    		c.startThread();
//    		System.out.println("add person");
    		
    		return c;
    	}
    	return null;
    }
    
    public ListPanel getCustomerPanel(){
    	return customerPanel;
    }
    
    public ListPanel getWaiterPanel(){
    	return waiterPanel;
    }
}
