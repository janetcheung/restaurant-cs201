package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.HostAgent;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**  
 * Subpanel of restaurantPanel.
 * This holds the scroll panes for the customers and, later, for waiters
 */
public class ListPanel extends JPanel implements ActionListener {

	final static int gridLayoutX = 3;
	final static int gridLayoutY = 3;
	final static int listGridLayoutX = 5;
	final static int listGridLayoutY = 1;
	final static int panelSize = 20;

	public JScrollPane pane =
			new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private JPanel view = new JPanel();
	
	//Add Customer Panel
	private List<JButton> custList = new ArrayList<JButton>();
	private JButton addCustomerButton = new JButton("Add");
	private JTextField addPersonB = new JTextField();
	private JCheckBox checkBox = new JCheckBox("Hungry?");
	private JPanel addCustomerPanel = new JPanel();

	//Add Waiter Panel
	private List<JButton> waiterList = new ArrayList<JButton>();
	private JButton addWaiterButton = new JButton("Add");
	private JTextField addWaiterB = new JTextField();
	private JPanel addWaiterPanel = new JPanel();
		
	private RestaurantPanel restPanel;
	private String type;

	public class custPair {
		JCheckBox box;
		JPanel panel;
		JLabel label;
		CustomerAgent customer;

		public custPair(JPanel p, JLabel l, JCheckBox b){
			panel = p;
			label = l;
			box = b;
		}
		public JPanel getPanel(){
			return panel;
		}
		
		public JLabel getLabel(){
			return label;
		}
		
		public JCheckBox getBox(){
			return box;
		}
		
		public CustomerAgent getCustomer(){
			return customer;
		}
		
	}
	private ArrayList<custPair> customerPair = new ArrayList<custPair>();
	/**
	 * Constructor for ListPanel.  Sets up all the gui
	 *
	 * @param rp   reference to the restaurant panel
	 * @param type indicates if this is for customers or waiters
	 */
	
	public ArrayList<custPair> getCustomerPair(){
		return customerPair;
	}
	
	public ListPanel(RestaurantPanel rp, String type) {
		restPanel = rp;
		this.type = type;

		setLayout(new GridLayout(gridLayoutX,gridLayoutY));
		
		add(new JLabel("<html><pre> <u>" + type + "</u><br></pre></html>"));

		if (type == "Customers"){
		//Add Customer Panel setup
			addCustomerPanel.setLayout(new GridLayout(gridLayoutX,gridLayoutY));
			add(addCustomerPanel);

			addPersonB.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					checkBox.setEnabled(true);
				}

				public void removeUpdate(DocumentEvent e) {
					checkBox.setEnabled(false);
				}

				public void insertUpdate(DocumentEvent e){
					checkBox.setEnabled(true);
				}
			});

			addCustomerPanel.add(addPersonB); 
		
			addCustomerButton.addActionListener(this);
			addCustomerPanel.add(addCustomerButton);

			checkBox.addActionListener(this);
			checkBox.setEnabled(false);
			addCustomerPanel.add(checkBox);

			view.setLayout(new GridLayout(listGridLayoutX, listGridLayoutY));
			pane.setViewportView(view);
			add(pane);
		}
		//Add Waiter Panel setup
		else if (type == "Waiters"){
			checkBox.setText("Break?");
			addWaiterPanel.setLayout(new GridLayout(gridLayoutX,gridLayoutY));
			add(addWaiterPanel);

			addWaiterPanel.add(addWaiterB); 

			addWaiterButton.addActionListener(this);
			addWaiterPanel.add(addWaiterButton);
			
			checkBox.addActionListener(this);
			checkBox.setVisible(false);
			addWaiterPanel.add(checkBox);
			
			view.setLayout(new GridLayout(listGridLayoutX, listGridLayoutY));
			pane.setViewportView(view);
			add(pane);
		}
	}

	/**
	 * Method from the ActionListener interface.
	 * Handles the event of the add button being pressed
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == addCustomerButton) {
			// Chapter 2.19 describes showInputDialog()
			//addPerson(JOptionPane.showInputDialog("Please enter a name:"));
			
			if (checkBox.isSelected()){
				addHungryPerson(addPersonB.getText());
			}
			else 
				addPerson(addPersonB.getText());
			addPersonB.setText(null);
			checkBox.setSelected(false);
			checkBox.setEnabled(false);
		}
		
		else if (e.getSource() == addWaiterButton) {
			addWaiter(addWaiterB.getText());
			addWaiterB.setText(null);
		}
		
		else {
			// Isn't the second for loop more beautiful?
			/*for (int i = 0; i < list.size(); i++) {
                JButton temp = list.get(i);*/
			for(JButton cl: custList){
				if(e.getSource() == cl){
					restPanel.showInfo(type, cl.getText());
				}
			}
			for (JButton wl: waiterList){
				if (e.getSource() == wl){
					restPanel.showInfo(type, wl.getText());
				}
			}
			for(custPair temp: customerPair){
				if(e.getSource() == temp.box){
					temp.customer.getGui().setHungry();
					temp.box.setEnabled(false);
				}
			}
		}
	}
	/**
	 * If the add button is pressed, this function creates
	 * a spot for it in the scroll pane, and tells the restaurant panel
	 * to add a new person.
	 *
	 * @param name name of new person
	 */
	public void addWaiter(String name) {
		if (name != null) {
			JPanel panel = new JPanel();
			JButton button = new JButton(name);
			JLabel label = new JLabel (name);
			button.setBackground(Color.white);

			Dimension paneSize = pane.getSize();

			button.addActionListener(this);

			panel.setName(name);
			panel.add(label);

			waiterList.add(button);
			view.add(button);

			restPanel.addPerson(type, name);//puts customer on list
			restPanel.showInfo(type, name);//puts hungry button on panel

			validate();
		}
	}
	public void addPerson(String name ) {
		if (name != null) {
			JPanel panel = new JPanel();
			JButton button = new JButton(name);
			JLabel label = new JLabel (name);
			JCheckBox checkBox = new JCheckBox("Hungry?");
			button.setBackground(Color.white);

			Dimension paneSize = pane.getSize();
			Dimension buttonSize = new Dimension(paneSize.width - 20,
									(int) (paneSize.height / 7));
			button.setPreferredSize(buttonSize);
			button.setMinimumSize(buttonSize);
			button.setMaximumSize(buttonSize);
			button.addActionListener(this);

			checkBox.addActionListener(this);

			panel.setName(name);
			panel.add(label);
			panel.add(checkBox);

//			list.add(panel);
			custList.add(button);
			view.add(button);

			custPair tempCust = new custPair(panel, label, checkBox);

			tempCust.customer = restPanel.addPanelPerson(type, name);//puts customer on list
			restPanel.showInfo(type, name);//puts hungry button on panel

			customerPair.add(tempCust);

			validate();
		}
	}
	public void addHungryPerson(String name ) {
		if (name != null) {
			JPanel panel = new JPanel();
			            JButton button = new JButton(name);
			JLabel label = new JLabel (name);
			JCheckBox checkBox = new JCheckBox("Hungry?");
			            button.setBackground(Color.white);

			Dimension paneSize = pane.getSize();
			            Dimension buttonSize = new Dimension(paneSize.width - 20,
			                    (int) (paneSize.height / 7));
			            button.setPreferredSize(buttonSize);
			            button.setMinimumSize(buttonSize);
			            button.setMaximumSize(buttonSize);
			            button.addActionListener(this);

			checkBox.addActionListener(this);
			checkBox.setSelected(true);
			checkBox.setEnabled(false);
			
			panel.setName(name);
			panel.add(label);
//			panel.add(checkBox);
//			list.add(panel);
//			view.add(panel);
			custList.add(button);
			view.add(button);
			custPair tempCust = new custPair(panel, label, checkBox);

			tempCust.customer = restPanel.addPanelPerson(type, name);//puts customer on list
			tempCust.customer.getGui().setHungry();
			restPanel.showInfo(type, name);//puts hungry button on panel

			customerPair.add(tempCust);

			validate();
		}
	}
}
