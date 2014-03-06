package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.HostAgent;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CustomerGui implements Gui{

	private CustomerAgent agent = null;
	private boolean isPresent = false;
	private boolean isHungry = false;
	private boolean isPoor = false;
	private boolean waitingForFood = false;
	private boolean waitingForBill = false;
	private boolean foodDelivered = false;
	
	private ImageIcon customer = new ImageIcon("src/img/customer.png");
	private ImageIcon bill = new ImageIcon("src/img/bill.png");
	private ImageIcon food;
	private String name;
	
	RestaurantGui gui;

	private enum Command {noCommand, GoToRestaurant, GoToSeat, GoToCashier, LeaveRestaurant};
	private Command command=Command.noCommand;

	private int xPos, yPos;
	private int xDestination, yDestination;
	private int speed = 5;
	public static final int xTable = 180;
	public static final int yTable = 250;
	public static final int defaultXPos = 70;
	public static final int defaultYPos = 40;
	public static int xCashierPos = 20;
    public static int yCashierPos = 150;

	public CustomerGui(CustomerAgent c, RestaurantGui gui){ //HostAgent m) {
		agent = c;
		name = agent.getCustomerName();
		xPos = 70;
		yPos = 40;
		xDestination = 70;
		yDestination = 40;
		this.gui = gui;
	}

	public void updatePosition() {
		if (xPos < xDestination)
			xPos+=speed;
		else if (xPos > xDestination)
			xPos-=speed;

		if (yPos < yDestination)
			yPos+=speed;
		else if (yPos > yDestination)
			yPos-=speed;

		if (xPos == xDestination && yPos == yDestination) {
			if (command==Command.GoToSeat) 
				agent.msgAnimationFinishedGoToSeat();
			else if (command == Command.GoToCashier)
				agent.msgAnimationFinishedGoToCashier();
			else if (command==Command.LeaveRestaurant) {
				agent.msgAnimationFinishedLeaveRestaurant();
				System.out.println("about to call gui.setCustomerEnabled(agent);");
				isHungry = false;
				isPoor = false;
				gui.setCustomerEnabled(agent);
			}
			command=Command.noCommand;
		}
	}
	
	public void setQueuePosition(int num){
		xDestination = defaultXPos + num*30;
		yDestination = defaultYPos;

	}

	public void draw(Graphics2D g) {
		g.setColor(Color.GREEN);
		g.fillRect(xPos, yPos, 20, 20);
		g.drawString(name, xPos+5, yPos-5);
		
		if (foodDelivered){
			g.setColor(Color.MAGENTA);
//			g.drawString(food, xPos+5, yPos+30);
		}
		
		else if (waitingForFood) {
			g.setColor(Color.MAGENTA);
			g.drawString(food + "?", xPos, yPos);
		}
	}
	
	public void paintGui(JPanel j, Graphics2D g) {
		customer.paintIcon(j,g, xPos, yPos);
		
		g.setColor(Color.MAGENTA);

		if (waitingForBill){
			bill.paintIcon(j, g, xPos, yPos-40);
			g.drawString("?", xPos+40, yPos-15);
		}
		
		if (waitingForFood){
			food.paintIcon(j, g, xPos, yPos-40);
			g.drawString("?", xPos+40, yPos-15);
		}
		else if (foodDelivered){
			food.paintIcon(j, g, xPos+5, yPos+5);
		}
	}

	public boolean isPresent() {
		return isPresent;
	}
	
	public void setPoor(){
		isPoor = true;
		agent.gotPoor();
	}
	
	public void setRich(){
		isPoor = false;
		agent.gotRich();
	}
	
	public boolean isPoor(){
		return isPoor;
	}
	
	public void setHungry() {
		isHungry = true;
		agent.gotHungry();
		setPresent(true);
		
		command = Command.GoToRestaurant;
	}
	public boolean isHungry() {
		return isHungry;
	}
	

	public void setPresent(boolean p) {
		isPresent = p;
	}
	
	public void setSpeed(int s){
		speed = s;
	}
	
	public int getSpeed(){
		return speed;
	}
	
	public void setHomePos(int x){
		xPos = x;
		xDestination = x;
	}
	public void setFood(String choice) {
		String path = "/restaurant_janetche/src/img/" + choice + ".png";
		food = new ImageIcon(path);
	}
	
	public void setXPos(int x){
		xPos = x;
	}
	
	public void setYPos(int y){
		yPos = y;
	}
	
	public void DoFollowWaiter(int x, int y) {
		setTableDestination(x,y);
//		System.out.println("Customer position: " + x + ", " + y);
		command = Command.GoToSeat;
	}
	public void DoCallWaiter(){
		//Nothing yet
	}

	public void DoGoToCashier() {
		xDestination = xCashierPos;
		yDestination = yCashierPos;
		
		foodDelivered = false;
		waitingForBill = false;
    	command = Command.GoToCashier;
    }
    
	public void DoExitRestaurant() {
		xDestination = -40;
		yDestination = -40;
		
//		foodDelivered = false;
		command = Command.LeaveRestaurant;
	}

	public void setTableDestination(int x, int y) {
		xDestination = x;
		yDestination = y;
	}
	
	public int getXTableDestination () {
		return xDestination;
	}
	
	public int getYTableDestination(){
		return yDestination;
	}
	
	public int getXPos(){
		return xPos;
	}
	
	public int getYPos(){
		return yPos;
	}
	
	public int getXDestination(){
		return xDestination;
	}
	
	public int getYDestination(){
		return yDestination;
	}
	public void gotFood(){
		foodDelivered = true;
		waitingForFood = false;
	}
	
	public void waitingForBill(){
		waitingForBill = true;
	}
	
	public void waitingForFood(){
		waitingForFood = true;
	}
	
}
