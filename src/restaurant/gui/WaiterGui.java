package restaurant.gui;


import restaurant.CustomerAgent;
import restaurant.WaiterAgent;
//import restaurant.gui.CustomerGui.Command;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class WaiterGui implements Gui {

    private WaiterAgent agent = null;
    private ImageIcon waiter = new ImageIcon("src/img/waiter.png");
    private String name;
    private int xPos = 30, yPos = 200;
    private int xDestination = 30, yDestination = 200;
    private int xDefaultPos = 30, yDefaultPos = 200;
    private int xWaitPos = 30, yWaitPos = 200;
    private boolean breakAllowed = false;
    private boolean onBreak = false;
    
    private enum Command {noCommand, GoToHome, GoToTable, GoToWaitingCustomer, GoToCashier, GoToCook, GoToBreak};
	private Command command=Command.noCommand;

    RestaurantGui gui;
    public enum waiterState {working, onBreak, waitingForBreak};
    private waiterState state;
    
    private ArrayList<Dimension> tablePositions = new ArrayList<Dimension>();
    private HashMap<Integer,Dimension> tableMap = new HashMap<Integer,Dimension>();
    
    public static final int xTable1 = 100;
    public static final int yTable1 = 250; 
    public static int xTable = 100;
    public static int yTable = 250;
    public static int xCookPos = 180;
    public static int yCookPos = 380;
    public static int xCashierPos = 20;
    public static int yCashierPos = 150;
    private int speed = 5; 
    boolean ran = true;

    public WaiterGui(WaiterAgent agent) {
        this.agent = agent;
      for (int i = 1; i <= agent.getTables().size(); i++){    
    	
		tablePositions.add(new Dimension(xTable1 + 100*i,yTable1));
		tableMap.put(i,tablePositions.get(i-1));
      }
    }

    public WaiterGui(WaiterAgent w, RestaurantGui gui) {
    	agent = w;
        name = agent.getWaiterName();
        state = waiterState.working;
        this.gui = gui;

    	  for (int i = 1; i <= agent.getTables().size(); i++){    
    	    	
    			tablePositions.add(new Dimension(xTable1 + 100*i,yTable1));
    			tableMap.put(i,tablePositions.get(i-1));
    		}
    }

	public void updatePosition() {
        
		if (xPos < xDestination){
            xPos+=speed;
			ran = false;
		}
        else if (xPos > xDestination){
            xPos-=speed;
            ran = false;
        }
        if (yPos < yDestination){
            yPos+=speed;
            ran = false;
        }
        else if (yPos > yDestination){
            yPos-=speed;
            ran = false;
        }
        
        if (xPos == xDestination && yPos == yDestination) {
			if (command==Command.GoToTable) 
				agent.msgAtTable();
			else if (command == Command.GoToCashier)
				agent.msgAtCashier();
			else if (command == Command.GoToCook)
				agent.msgAtCook();
			else if (command == Command.GoToHome)
				agent.msgAtStandby();
			else if (command == Command.GoToWaitingCustomer)
				agent.msgAtCustomer();
			else if (command==Command.GoToBreak) {
				agent.msgAtStandby(); //change position later
			}
			command=Command.noCommand;
		}
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.fillRect(xPos, yPos, 20, 20);
        if (name!=null)
        	g.drawString(name, xPos+5, yPos-5);
        
    }

    public boolean isPresent() {
        return true;
    }
    public void setSpeed(int s){
		speed = s;
	}
	
	public int getSpeed(){
		return speed;
	}
    public void goOnBreak() {
    	state = waiterState.onBreak;
    	onBreak = true;
    }
    public void requestBreak(){
    	agent.requestBreak();
    	state = waiterState.waitingForBreak;
    	onBreak = false;
    }

    public void returnFromBreak(){
    	state = waiterState.working;
    	onBreak = false;
    	gui.setWaiterEnabled(agent);
    }
    
    public waiterState getState(){
    	return state;
    }
    
    public void breakAllowed(){
    	breakAllowed = true;
    }
    
    public void breakNotAllowed(){
    	breakAllowed = false;
    	state = waiterState.working;
    }
    
    public boolean canBreak(){
    	return breakAllowed;
    }
    
    public boolean onBreak(){
    	return onBreak;
    }
    public void setTableDestination(int table){
    	Dimension d = tableMap.get(table);
    	xTable = (int) d.getWidth();
    	yTable = (int) d.getHeight();
    }
    
    public void DoGoToFront(){
    	xDestination = xDefaultPos;
    	yDestination = yDefaultPos;
    	
    	command = Command.GoToHome;
    }
    
    public void DoBringToTable(CustomerAgent customer, int tableNumber) {
    	setTableDestination(tableNumber);

    	xDestination = xTable + 20;
        yDestination = yTable - 20;
        
        command = Command.GoToTable;
    }

    public void DoGoToTable(int tableNumber) {
//    	System.err.println("in gui DoGoToTable()");
    	setTableDestination(tableNumber); 
    	xDestination = xTable + 20;
    	yDestination = yTable - 20;
    	
    	command = Command.GoToTable;
    }
    
    public void DoDeliverOrderToTable(int tableNumber){
    	setTableDestination(tableNumber);
    	xDestination = xTable + 20;
    	yDestination = yTable - 20;
    	//add more to place food down
    	
    	command = Command.GoToTable;
    }
    
    public void DoGoToCustomer(CustomerAgent customer){
    	xDestination = customer.getGui().getXPos();
    	yDestination = customer.getGui().getYPos() + 20;
    	
    	command = Command.GoToWaitingCustomer;
    }
    
    public void DoLeaveCustomer() {
        xDestination = xWaitPos;
        yDestination = yWaitPos;
        
        command = Command.GoToHome;
    }
    
    public void DoGoToHome() {
    	xDestination = xWaitPos;
    	yDestination = yWaitPos;
    	
    	command = Command.GoToHome;
    }

    public void DoGoToCook() {
    	xDestination = xCookPos;
    	yDestination = yCookPos;
    	
    	command = Command.GoToCook;
    }
    
    public void DoGoToCashier() {
    	xDestination = xCashierPos;
    	yDestination = yCashierPos;
    	
    	command = Command.GoToCashier;
    }
    
    public void setHomePos(int y){
    	yWaitPos = y;
    	yDefaultPos = y;
    	yPos = y;
    	yDestination = y;
    }
    
    public int getXHomePos(){
    	return xDefaultPos;
    }
    public int getYHomePos(){
    	return yDefaultPos;
    }
    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
    
    public int getXDestination(){
    	return xDestination;
    }
    
    public int getYDestination(){
    	return yDestination;
    }

	public void paintGui(JPanel j, Graphics2D g) {
		waiter.paintIcon(j, g, xPos, yPos);
	}

}

