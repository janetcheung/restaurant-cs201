package restaurant.gui;

import java.awt.*;

import javax.swing.JPanel;

public interface Gui {

    public void updatePosition();
    public void draw(Graphics2D g);
    public boolean isPresent();
	public void paintGui(JPanel j, Graphics2D g);

}
