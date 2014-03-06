package restaurant;

import agent.Agent;
import restaurant.gui.HostGui;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class HostAgent extends Agent {
	private String name;
	private int nWaiters = 0;
	private int nextWaiter;
	private int numTablesOccupied = 0;
	static final int NTABLES = 4;

	public List<CustomerAgent> waitingCustomers= Collections.synchronizedList(new ArrayList<CustomerAgent>());
	public List<MyWaiter> waiters= Collections.synchronizedList(new ArrayList<MyWaiter>(nWaiters));
	public Collection<Table> tables;

	private Semaphore atTable = new Semaphore(0,true);
	public enum customerState{needNumber, queued}
	public enum waiterState {working, requestingBreak, onBreak}
	
	public HostGui hostGui = null;

	public HostAgent(String name) {
		super();

		this.name = name;
		nextWaiter = 0;
		// make some tables
		tables = new ArrayList<Table>(NTABLES);
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix));//how you add to a collections
		}
		
//		waiters = new ArrayList<MyWaiter>(nWaiters);
	}

	public String getMaitreDName() {
		return name;
	}

	public String getName() {
		return name;
	}
	
	public int getNWaiters(){
		return nWaiters;
	}

	public void setNWaiters(int nWaiters){
		this.nWaiters = nWaiters;
	}
	
	public List getWaiters(){
		return waiters;
	}
	public List getWaitingCustomers() {
		return waitingCustomers;
	}

	public Collection getTables() {
		return tables;
	}
	
	public void addWaiter(WaiterAgent w){
		waiters.add(new MyWaiter(w));
		stateChanged();
	}
	// Messages

	public void msgIWantFood(CustomerAgent cust) {
//		print("someone's hungry!");
		waitingCustomers.add(cust);
		stateChanged();
	}

	public void msgLeavingTable(CustomerAgent cust) {
		
//		synchronized(tables){
			for (Table table : tables) {
				if (table.getOccupant() == cust) {
					print(cust + " leaving " + table);
					table.setUnoccupied();
					print(table + " is free");
					numTablesOccupied--;
				}
			}
//		}
		stateChanged();
	}
	
	public void msgImBack(WaiterAgent w){
		print("Received msgImBack");
		synchronized(waiters){
			for (MyWaiter mw: waiters){
				if (mw.waiter == w){
					mw.state = waiterState.working;
				}
			}
		}
		stateChanged();
	}
	
	public void msgRequestingBreak(WaiterAgent w){
		print("Received msgRequestingBreak");
		synchronized(waiters){
			for (MyWaiter mw: waiters){
				if (mw.waiter == w){
					mw.state = waiterState.requestingBreak;
				}
			}
		}
		stateChanged();
	}
	public void msgAtTable() {//from animation
		//print("msgAtTable() called");
		atTable.release();// = true;
		stateChanged();
	}

	/**Non normative scenarios*/
	public void msgLeavingRestaurant(CustomerAgent customer){
		synchronized(waitingCustomers){
			for (CustomerAgent c: waitingCustomers){
				if (c.equals(customer)){
					waitingCustomers.remove(c);
					stateChanged();
				}
			}
		}
	}
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		/* Think of this next rule as:
            Does there exist a table and customer,
            so that table is unoccupied and customer is waiting.
            If so seat him at the table.
		 */
	synchronized(waiters){
		for (MyWaiter mw: waiters){
			if (mw.state == waiterState.requestingBreak){
				signOffBreak(mw);
				return true;
			}
		}
	}

		if (waitingCustomers.size() > tables.size()){
//			if (numTablesOccupied == tables.size()){
//				if (waitingCustomers.get(0).state == customerState.waiting)
				notifyCustomer(waitingCustomers.get(waitingCustomers.size()-1));
				return true;
//			}
		}
		
		
//	synchronized(tables){
		if(!waitingCustomers.isEmpty()){
			giveCustomerNumber(waitingCustomers.get(waitingCustomers.size() - 1));
			if(!waiters.isEmpty()){
				//Choosing next waiter
					while (waiters.get(nextWaiter).state == waiterState.onBreak)
						chooseNextWaiter();	
					for(Table table : tables){
						if(!table.isOccupied()){
//							for(MyWaiter mw: waiters){
//								if(mw.state == waiterState.working){
							seatCustomer(waiters.get(nextWaiter), waitingCustomers.get(0),table);
							return true;
						}
//					}
				}
			}
//		}
			else print ("No waiter at the moment");		
//		}
			
		
	}
		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	private void chooseNextWaiter(){
		nextWaiter = ++nextWaiter % waiters.size();
	}
	
	private void seatCustomer(MyWaiter mw, CustomerAgent customer, Table table) {

		print(mw.waiter.getName() + ", seat " + customer + " at " + table);
		table.setOccupant(customer);
		numTablesOccupied++;

		mw.waiter.msgSeatCustomer(customer, table.tableNumber);
		
		waitingCustomers.remove(customer);
		chooseNextWaiter();
		stateChanged();
	}

	private void signOffBreak(MyWaiter mw){
		int numWorkingWaiters = 0;
		for(MyWaiter w: waiters){ numWorkingWaiters = (w.state != waiterState.onBreak) ? ++numWorkingWaiters : --numWorkingWaiters;}
		if (waiters.size() <= 1 || numWorkingWaiters < 1){
			print("No break allowed");
			mw.waiter.msgNoBreak();
			mw.state = waiterState.working;
		}
		else {
			print("Break is allowed.");
			mw.waiter.msgYesBreak();
			mw.state = waiterState.onBreak;
		}
		//stateChanged();
	}
	
	private void notifyCustomer(CustomerAgent c){
		c.msgRestaurantFull();
		//stateChanged();
	}
	
	private void giveCustomerNumber(CustomerAgent customer){
		for (int i = 0 ; i < waitingCustomers.size(); i++){
			if (waitingCustomers.get(i).equals(customer))
				customer.msgHereIsYourNumber(i);
		}
	}

	//utilities

	public void setGui(HostGui gui) {
		hostGui = gui;
	}

	public HostGui getGui() {
		return hostGui;
	}

	private class Table {
		CustomerAgent occupiedBy;
		int tableNumber;

		Table(int tableNumber) {
			this.tableNumber = tableNumber;
		}

		void setOccupant(CustomerAgent cust) {
			occupiedBy = cust;
		}

		void setUnoccupied() {
			occupiedBy = null;
		}

		CustomerAgent getOccupant() {
			return occupiedBy;
		}

		boolean isOccupied() {
			return occupiedBy != null;
		}

		public String toString() {
			return "table " + tableNumber;
		}
	}
	
	private class MyWaiter {
		WaiterAgent waiter;
		waiterState state;
		
		MyWaiter(WaiterAgent waiter){
			this.waiter = waiter;
			state = waiterState.working;
		}
	}
}

