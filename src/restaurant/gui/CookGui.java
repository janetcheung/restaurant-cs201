package restaurant.gui;


import restaurant.CustomerAgent;
import restaurant.HostAgent;
import restaurant.WaiterAgent;
import restaurant.CookAgent;
import restaurant.CookAgent.Food;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import java.util.*;
import java.util.List;

public class CookGui implements Gui {

    private CookAgent agent = null;
    private ImageIcon cook = new ImageIcon ("src/img/cook.png");
     
    RestaurantGui gui;
    private int xPos = 200, yPos = 430;//default cook position
    private int defaultXPos = 200, defaultYPos = 430;//default start position
    private int xDestination = 200, yDestination = 430;
    private int speed = 1;
    
    private List<Dimension> tablePositions = new ArrayList<Dimension>();
    private HashMap<Integer,Dimension> tableMap = new HashMap<Integer,Dimension>();
    private List<Grill> grills = new ArrayList<Grill>();
    private List<Order> orders = new ArrayList<Order>();
    
    public int xTable = 100;
    public int yTable = 250;
    private int xGrill = 200;
    private int yGrill = 470;
    private int xFridge = 190;
    private int yFridge = 430;
    private int xPlating = 200;
    private int yPlating = 420;

	static final int platingTableWidth = 100;
    static final int platingTableHeight = 20;
    
    private enum Command {noCommand, GoToFridge, GoToGrill, GoToPlating, GoToHome};
	private Command command=Command.noCommand;
        
    public CookGui(CookAgent agent, RestaurantGui gui) {
        this.agent = agent;
        this.gui = gui;

        grills.add(new Grill("Steak", false, xGrill, yGrill));
        grills.add(new Grill("Chicken", false, xGrill + grills.size()*25, yGrill));
        grills.add(new Grill("Salad", false, xGrill + grills.size()*25, yGrill));
        grills.add(new Grill("Pizza", false, xGrill + grills.size()*25, yGrill));
                
    }
    
    public void setTableDestination(int table){
    	Dimension d = tableMap.get(table);
    	xTable = (int) d.getWidth();
    	yTable = (int) d.getHeight();
    }
    
    public void setTableDestination(int x, int y){
    	xTable = x;
    	yTable = y;
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
			if (command==Command.GoToFridge){
				agent.msgAtFridge();
			}
			else if (command == Command.GoToGrill){
				agent.msgAtGrill();
			}
			else if (command == Command.GoToPlating){
				agent.msgAtPlating();
			}
			else if (command == Command.GoToHome){
				agent.msgAtHome();
			}
		command=Command.noCommand;
        }
    }
    
    public void goToGrill(String choice){
    	command = Command.GoToGrill;
    	
//    	System.err.println("Choice: " + choice);
    	for (Grill g: grills){
    		if (g.name == choice){
    			xDestination = g.xPos;
    			yDestination = g.yPos - 5;
    		}
//    		else System.err.println("not found");
    	}
    }

    public void doCollectIngredients(){
    	
    	xDestination = xFridge;
    	yDestination = yFridge;
    	
    	command = Command.GoToFridge;
    }
    public void doCooking(String choice){
    	for (Grill g: grills){
    		if (g.name == choice){
    			g.on = true;
    		}
    	}
    }
    
    public void doGoHome(){
    	xDestination = defaultXPos;
    	yDestination = defaultYPos;
    	
    	command = Command.GoToHome;
    }
    
    public void doneCooking(String choice){
    	for (Grill g: grills){
    		if (g.name == choice){
    			g.on = false;
    		}
    	}
    }
    public void remove(String choice ){
    	orders.remove(choice);
    }
    public void doPlating(String choice){
    	orders.add (new Order(choice, false, xPlating+(orders.size()*20), yPlating-30));
    	
    	xDestination = xPlating;
    	yDestination = yPlating;
    	
    	command = Command.GoToPlating;
    	
    }
    public void draw(Graphics2D g) {
    	g.setColor(Color.BLACK);
        g.fillRect(defaultXPos, defaultYPos, 20, 20);
        
    }
    
    public boolean isPresent() {
        return true;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setDelivered(String s){
    	for (Order o: orders){
    		if (o.name == s){
    			o.delivered = true;
    			return;
    		}
    	}
    }
    public void setSpeed(int s){
		speed = s;
	}
	
	public int getSpeed(){
		return speed;
	}
	public void paintGui(JPanel j, Graphics2D g) {
		cook.paintIcon(j, g, xPos, yPos);

		for (Grill grill : grills){
			if (!grill.on)
				grill.grillOff.paintIcon(j, g, grill.xPos, grill.yPos);
			else if (grill.on)
				grill.grillOn.paintIcon(j, g, grill.xPos, grill.yPos);
		}
		
		for (Order order : orders){
			if (!order.delivered)
				order.food.paintIcon(j, g, order.xPos, order.yPos);
			
		}
		
	}
	

	private class Grill{
		String name;
		ImageIcon grillOn = new ImageIcon ("src/img/grillOn.png");
		ImageIcon grillOff = new ImageIcon("src/img/grillOff.png");
		int xPos;
		int yPos;
		boolean on;
		
		Grill(String name, boolean on, int x, int y){
			this.name = name;
			this.on = on;
			this.xPos = x;
			this.yPos = y;

		}
		
		public void setGrillOn(boolean b){
			on = b;
		}
	}
	
	private class Order{
		String name;
		boolean delivered;
		int xPos;
		int yPos;
		private ImageIcon food;
	    
		
		Order(String name, boolean delivered, int x, int y){
			this.name = name;
			this.delivered = delivered;
			this.xPos = x;
			this.yPos = y;
			
			this.food = new ImageIcon("src/img/" + name + ".png");
			
		}
		
	}
}