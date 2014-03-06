package restaurant.gui;


import restaurant.CustomerAgent;
import restaurant.HostAgent;

import java.awt.*;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class HostGui implements Gui {

    private HostAgent agent = null;
    private ImageIcon host = new ImageIcon("src/img/host.png");
    
    private int xPos = -20, yPos = -20;//default host position
    private int xDestination = -20, yDestination = -20;//default start position
    private int speed=3;
    public static final int xTable = 200;
    public static final int yTable = 250;

    public HostGui(HostAgent agent) {
        this.agent = agent;
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

        if (xPos == xDestination && yPos == yDestination
        		& (xDestination == xTable + 20) & (yDestination == yTable - 20)) {
           agent.msgAtTable();
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        g.fillRect(xPos, yPos, 20, 20);
    }

    public boolean isPresent() {
        return true;
    }

    public void DoBringToTable(CustomerAgent customer) {
        xDestination = xTable + 20;
        yDestination = yTable - 20;
    }

    public void DoLeaveCustomer() {
        xDestination = -20;
        yDestination = -20;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setSpeed(int s){
		speed = s;
	}
	
	public int getSpeed(){
		return speed;
	}
	public void paintGui(JPanel j, Graphics2D g) {
		host.paintIcon(j, g, 30, 30);
	}
    
}

