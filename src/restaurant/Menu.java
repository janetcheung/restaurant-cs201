package restaurant;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.*;

public class Menu { 
	public int nMenuItems = 4;
	public List <MenuItem> menuItems = Collections.synchronizedList(new ArrayList<MenuItem>());
	
	Menu () {
		menuItems.add(new MenuItem("Steak", 15.99));
		menuItems.add(new MenuItem("Chicken", 10.99));
		menuItems.add(new MenuItem("Salad", 5.99));
		menuItems.add(new MenuItem("Pizza", 8.99));
	}
	public class MenuItem{
		String food;
		double price;
		
		private MenuItem(String food, double price){
			this.food = food;
			this.price = price;
		}
		
		public String getMenuItem(){
			return food;
		}
	}
	
	
	
	public void draw(Graphics2D g){
		g.setColor(Color.RED);
		g.fillRect(0,0,1,1);
	}
		
}