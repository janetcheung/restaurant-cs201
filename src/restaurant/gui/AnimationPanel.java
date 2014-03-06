package restaurant.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

public class AnimationPanel extends JPanel implements ActionListener {

    private final int WINDOWX = 700;
    private final int WINDOWY = 700;
    static int xTable = 200;
    static int yTable = 250;
    static int xPlatingTable = 200;
    static int yPlatingTable = 410;
    static int platingTableWidth = 100;
    static int platingTableHeight = 20;
    static int xFridge = 150;
    static int yFridge = 420;
    static int tableWidth = 50;
    static int tableHeight = 50;
    static final int NTABLES = 4;
//	private static final Object  = null;
    
    private ImageIcon background = new ImageIcon("src/img/floor.jpg");
    private ImageIcon cashier = new ImageIcon("src/img/cashier.png");
    private ImageIcon fridge = new ImageIcon("src/img/fridge.png");
    
    private Dimension bufferSize;

    private List<Gui> guis = new ArrayList<Gui>();

    public AnimationPanel() {
    	setSize(WINDOWX, WINDOWY);
        setVisible(true);
         
        bufferSize = this.getSize();
        
    	Timer timer = new Timer(20, this );
    	timer.start();
    }
    
	public void actionPerformed(ActionEvent e) {
		repaint();  //Will have paintComponent called
	}

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        //Clear the screen by painting a rectangle the size of the frame
        g2.setColor(getBackground());
        g2.fillRect(0, 0, WINDOWX, WINDOWY );
        
        background.paintIcon(this, g2, 0, 0);
        cashier.paintIcon(this, g2, 20, 150);
        fridge.paintIcon(this, g2, xPlatingTable- 50, 400);

        g2.setColor(Color.ORANGE);
        for(int i=0; i < NTABLES; i++)
        	g2.fillRect(xTable+100*i, yTable, tableWidth, tableHeight);
        
        g2.setColor(Color.GRAY);
        g2.fillRect(xPlatingTable,yPlatingTable, platingTableWidth, platingTableHeight);

        for(Gui gui : guis) {
            if (gui.isPresent()) {
                gui.updatePosition();
//                gui.draw(g2);
                gui.paintGui(this, g2);
            }
        }
        
    }

    public void addGui(CustomerGui gui) {
        guis.add(gui);
    }

    public void addGui(HostGui gui) {
        guis.add(gui);
    }

	public void addGui(WaiterGui gui) {
		guis.add(gui);
	}
	
	public void addGui(CookGui gui) {
		guis.add(gui);
	}
}
