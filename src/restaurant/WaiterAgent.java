package restaurant;

import agent.Agent;
import restaurant.CashierAgent.Bill;
//import restaurant.MarketAgent.orderState;
import restaurant.gui.WaiterGui;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Waiter;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Waiter Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class WaiterAgent extends Agent implements Waiter{
	static final int NTABLES = 4;
	public List<MyCustomer> myCustomers = Collections.synchronizedList(new ArrayList<MyCustomer>());
	public Collection<Table> tables;
	private Menu menu;
	Timer timer = new Timer();
	
	private CookAgent cook = null;
	private HostAgent host = null;
	private CashierAgent cashier = null;
	
	private String name;
	private Semaphore atTable = new Semaphore(0,true);
	private Semaphore atFront = new Semaphore(0,true);
	private Semaphore atCook = new Semaphore(0,true);
	private Semaphore atCashier = new Semaphore(0,true);
	private Semaphore atStandby = new Semaphore(0,true);
	private Semaphore atCustomer = new Semaphore(0,true);
	
	private boolean allowedBreak = false;
	private boolean onBreak = false;
	public enum customerState{	waitingForSeat, beingSeated, seated, readyToOrder, ordering, waitingForFood, orderSent, 
								mustReorder, orderReady, eating, readyForBill, waitingForBill, billReady, paying, waitingForChange, 
								receivedChange, NO_ACTION, done}
	public enum orderState {pending, cooking, done, delivered, outOfOrder}

	public WaiterGui waiterGui = null;

	public WaiterAgent(String name) {
		super();

		this.name = name;
		this.menu = new Menu();
		
		// make some tables
		tables = new ArrayList<Table>(NTABLES);
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix));//how you add to a collections
		}
	}

	public String getMaitreDName() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setCook(CookAgent cook){
		this.cook = cook;
	}

	public void setHost(HostAgent host){
		this.host = host;
	}
	
	public void setCashier(CashierAgent cashier){
		this.cashier = cashier;
	}
	
	public Collection getTables() {
		return tables;
	}
	// Messages

	public String getWaiterName() {
		return name;
	}
	
	public int getNumberOfTables(){
		return NTABLES;
	}
	
	public boolean allowedBreak() {
		return allowedBreak;
	}

	// Messages
	
	public void msgSeatCustomer(CustomerAgent customer, int table){
		/*Add customer c to empty table
		 * set customer's waiter to this guy
		 */
		print("Received msgSeatCustomer");
		MyCustomer mc = new MyCustomer(customer, table); 
		
		mc.state = customerState.waitingForSeat; 
//		System.err.println("Adding customer");
		myCustomers.add(mc);
//		System.err.println("Added customer "  + mc.c.getCustomerName() + " state " + mc.state.toString());

		stateChanged();
//		return;
	}
	
	public void msgReadyToOrder(Customer customer){
		/*change customer state to readyToOrder
		 * 
		 */
		print("received msgReadyToOrder from " + customer.getName());
//		synchronized(myCustomers){
			for (MyCustomer mc : myCustomers) {
				if (mc.c.getName().equals(customer.getName())){
					mc.state = customerState.readyToOrder;
					stateChanged();
					return;
				}
			}
//		}
	}
	
	public void msgHereIsMyChoice(String choice, Customer customer){
		/* change customer choice to choice
		 * waitingForOrder.release();
		 */
		print("Received msgHereIsMyChoice");
//		synchronized(myCustomers){
			for (MyCustomer mc : myCustomers) {
				if (mc.c == customer) {
					mc.choice = choice;
					mc.order = new Order(choice);
					mc.state = customerState.waitingForFood;
					stateChanged();
				return;
				}
			}
//		}
		
	}
	
	public void msgOutOfOrder(String choice, int tableNumber){
		/*if there exists a pending order
		 * then order is now out
		 */
		print("Received msgOutOfOrder");
//		synchronized(myCustomers){
			for (MyCustomer mc : myCustomers) {
				if (mc.table == tableNumber){
					if (mc.choice == choice){
						if(mc.order.state == orderState.pending){
							mc.order.state = orderState.outOfOrder;
							mc.state = customerState.mustReorder;
							stateChanged();
							return;
						}
					}
				}
			}
//		}
	}
	public void msgOrderDone(String choice, int tableNumber){
		/*if there exists a pending order
		 * 		then order is now done;
		 */
		print("Received msgOrderDone");
//		synchronized(myCustomers){
			for (MyCustomer mc : myCustomers) {
				if (mc.table == tableNumber){
					if (mc.choice == choice){
						if (mc.order.state == orderState.pending){
							mc.order.state = orderState.done;
							mc.state = customerState.orderReady;
							stateChanged();
							return;
						}
					}
				}
			}
//		}
	}
	
	public void msgBillPlease(Customer customer){
		print ("Received msgBillPlease");
//		synchronized(myCustomers){
			for (MyCustomer mc: myCustomers){
				if(mc.c == customer){
					mc.state = customerState.readyForBill;
					stateChanged();
					return;
				}
			}
//		}
	}
	public void msgBillReady(Bill b){
		print ("Received msgBillReady for " + b.tableNumber + "for " + b.customer);
//		synchronized(myCustomers){
			for (MyCustomer mc: myCustomers){
//				System.err.println("checking customer " + mc.c.getCustomerName() + " at " + mc.table);
				if (mc.table == b.tableNumber){
					mc.bill = b;
					mc.state = customerState.billReady;
//					System.err.println("matched. state changed");
					stateChanged();
					return;
				}
			}
//		}
	}

	public void msgDoneAndLeaving(Customer customer){
		/*		if a customer is leaving,
		 * 			change the customer's state to done
		 * 
		 */
//		System.err.println("Received msgLeavingTable");
		for (Table table : tables) {
			if (table.getOccupant() == customer) {
				print(customer + " leaving " + table);
				table.setUnoccupied();
			}
		}
		
//		synchronized(myCustomers){
			for (MyCustomer mc : myCustomers) {
				if(mc.c == customer) {
					mc.state = customerState.done;
					stateChanged();
					return;
				}
			}	
//		}
	}
		
	public void msgLeavingTable(Customer customer) {
//		synchronized(tables){
//		System.err.println("Received msgLeavingTable");
			for (Table table : tables) {
				if (table.getOccupant() == customer) {
					print(customer + " leaving " + table);
					table.setUnoccupied();
				}
			}
//		}
//		synchronized(myCustomers){
			for (MyCustomer mc : myCustomers){
				if(mc.c == customer){
					mc.state = customerState.done;
					return;
				}
			}
//		}
		stateChanged();
	}
	
	public void msgYesBreak(){
		allowedBreak = true;
		waiterGui.breakAllowed();
		print ("Break is allowed.  Finishing up");
		print("allowedBreak: " + allowedBreak + " myCustomers size: " + myCustomers.size());
		stateChanged();
		//return;
	}
	
	public void msgNoBreak(){
		allowedBreak = false;
		waiterGui.breakNotAllowed();
		print ("Break is not allowed.  Continuing to work");
		stateChanged();
		//return;
	}
	public void msgAtCustomer() {
//		System.err.println("At Customer");
		atCustomer.release();
		stateChanged();
		
	}
	public void msgAtFront(){
//		print("Received msgAtFront()");
		atFront.release();
		stateChanged();
		//return;
	}
	
	public void msgAtTable() {//from animation
//		System.err.println("msgAtTable() called");
		atTable.release();// = true;
		stateChanged();
		//return;
	}

	public void msgAtCook() {
		atCook.release();
		stateChanged();
		//return;
	}
	
	public void msgAtCashier() {
//		System.err.println("at Cashier");
		atCashier.release();
		stateChanged();
		//return;
	}
	
	public void msgAtStandby(){
		atStandby.release();
		stateChanged();
		//return;
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
	try{
//		System.err.println("myCustomers.isEmpty: " + myCustomers.isEmpty());
//		System.err.println("onBreak: " + onBreak);
		if (onBreak ){
//			System.out.println("In scheduler: returning from break");
			returnFromBreak();
			return true;
		}
		
		if (myCustomers.isEmpty() && allowedBreak){
//			System.out.println("In scheduler: going on break");
			goOnBreak();
			return true;
		}
				
		if (!myCustomers.isEmpty() && !onBreak){
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers){
			if (mc.state == customerState.orderReady){
				deliverOrder(mc);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){	
		for (MyCustomer mc : myCustomers){
			if (mc.state == customerState.billReady){
//				System.err.println("deliverBill in scheduler");
				deliverBill(mc);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){	
		for (MyCustomer mc : myCustomers){
			if (mc.state == customerState.waitingForSeat){
//				System.err.println("ComeGetCustomer");
//				System.out.println("comeGetCustomer");
				comeGetCustomer(mc, mc.table);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers) {
			if (mc.state == customerState.beingSeated){
				seatCustomer(mc, mc.table);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers) {
			if (mc.state == customerState.seated && !mc.leftTable){
				mc.leftTable = true;
				leaveTable(mc);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers){
//			print(mc.state.toString());
			if (mc.state == customerState.readyToOrder){
				takeOrder(mc);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers){
//			print(mc.state.toString());
			if (mc.state == customerState.ordering){
//				print("Customer is now ordering");
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers){
			if(mc.state == customerState.orderSent){
				//Do Nothing for now
				waitForAction(mc);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){	
		for (MyCustomer mc : myCustomers) {
			if (mc.state == customerState.mustReorder){
				askToReorder(mc);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers){
			if (mc.state == customerState.waitingForFood){
				giveOrderToCook(mc);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers) {
			if (mc.state == customerState.eating){
				leaveTable(mc);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers){
			if (mc.state == customerState.readyForBill){
				getBill(mc);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers){
			if (mc.state == customerState.waitingForBill){
				//do nothing
				waitForAction(mc);
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers){
			if (mc.state == customerState.paying){
				//do nothing
				return true;
			}
		}
//	}
//	synchronized(myCustomers){
		for (MyCustomer mc : myCustomers) {
//			print("in scheduler for mc.state = done");
			if (mc.state == customerState.done){
				notifyHost(mc);
				return true;
			}
		}
//	}
		if (myCustomers.isEmpty())
			returnToFront();
		}
	}catch(ConcurrentModificationException e){
		return false;
	}
		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	public void requestBreak(){
		print("Requesting break");
		host.msgRequestingBreak(this);
	}
	

	public void goOnBreak(){
		waiterGui.DoGoToFront();
		try {
			atFront.acquire();
		} catch(InterruptedException e){
			e.printStackTrace();
		}
		waiterGui.goOnBreak();
//		state = waiterState.onBreak;
		onBreak = true;
//		System.err.println("Break status: " + state);
//		System.out.println("goOnBreak: " + onBreak);
		//stateChanged();
	}
	

	public void returnFromBreak(){
		onBreak = false;
		allowedBreak = false;
		timer.schedule(new TimerTask() {
			public void run() {
//				System.err.println("Return from break");
				
				waiterGui.returnFromBreak();
				notifyHostBack();
//				state = waiterState.backFromBreak;
				//stateChanged();
			}
		},5000);		
	}
	
	public void notifyHostBack(){
		waiterGui.DoLeaveCustomer();
		host.msgImBack(this);
//		state = waiterState.working;

		try {
			atStandby.acquire();
		}catch (InterruptedException e){
			e.printStackTrace();
		}
//
//		System.err.println("Waiterstate: " + state);
//		System.out.println("WaiterState: " + state);
		//stateChanged();
	}
	
	private void comeGetCustomer(MyCustomer customer, int table){
//		System.err.println("Getting Customer");
		waiterGui.DoGoToCustomer(customer.c);
//		if (waiterGui.getXPos() != waiterGui.getXHomePos()){
		try {
			atCustomer.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		}
		
		customer.state = customerState.beingSeated;
//		System.err.println("Customer State: " + customer.state);
		stateChanged();
	}
	
	private void seatCustomer(MyCustomer customer, int table) {
		DoSeatCustomer(customer.c, table);

		customer.c.msgFollowWaiter(this, menu);
		
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		synchronized(tables){
			for (Table t: tables){
				if (t.tableNumber == table){
					t.setOccupant(customer.c);
				}
			}
//		}
		customer.state = customerState.seated;
		stateChanged();
	}
	
	private void takeOrder(MyCustomer mc){
		/*DoGoToTable(customer);
		 * msgWhatDoYouWant();
		 * customer.state = askedToOrder;
		 * waitingForOrder.acquire();
		 */
		DoGoTakeOrder(mc);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mc.c.msgWhatDoYouWant();
		mc.state = customerState.NO_ACTION;
		stateChanged();
	}
	
	private void giveOrderToCook(MyCustomer mc){
		DoGiveOrderToCook();
		try {
			atCook.acquire();
		}catch (InterruptedException e){
			e.printStackTrace();
		}
		cook.msgReceivedOrder(this, mc.choice, mc.table);
		mc.state = customerState.NO_ACTION;
		stateChanged();
	}
	
	private void askToReorder(MyCustomer mc){
		/*Remove menuItem from the menu
		 * before passing in the updated menu to the customer
		 */
		
		print("Going to table" + mc.table);
		waiterGui.DoGoToTable(mc.table);
		try {
			atTable.acquire();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		
		menu.menuItems.remove(mc.choice);
		mc.c.msgPleaseReorder(menu);
		mc.state = customerState.seated;

//		for (int i = 0; i < menu.nMenuItems ; i++)
//			System.err.println("Menu Items: " + menu.menuItems.get(i).getMenuItem());
		stateChanged();
	}
	private void deliverOrder(MyCustomer mc){
//		DoDeliverOrder(mc);
		waiterGui.DoGoToCook();
		try{
			atCook.acquire();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		cook.msgGotFood(mc.choice, mc.table);
		waiterGui.DoDeliverOrderToTable(mc.table);
		try{
			atTable.acquire();
		}catch (InterruptedException e){
			e.printStackTrace();
		}
		mc.c.msgHereIsFood(mc.choice);
		mc.state = customerState.eating;
		stateChanged();
	}

	private void leaveTable(MyCustomer mc){
		waiterGui.DoLeaveCustomer();
		stateChanged();
	}
	
	private void getBill(MyCustomer mc){
		DoGetBill();
		try {
			atCashier.acquire();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		cashier.msgReceivedBillRequest(this, mc.c, mc.choice, mc.table);
		mc.state = customerState.NO_ACTION;
		stateChanged();
	}
	
	private void deliverBill(MyCustomer mc){
//		DoDeliverBill(mc);
		waiterGui.DoGoToCashier();
		try {
			atCashier.acquire();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		waiterGui.DoGoToTable(mc.table);
//		System.err.println("in deliverBill");
		try{
			atTable.acquire();
		}catch (InterruptedException e){
			e.printStackTrace();
		}
		mc.c.msgHereIsBill(mc.bill.total);
		mc.state = customerState.paying;
		stateChanged();
	}

	private void notifyHost(MyCustomer mc){
//		System.out.println(mc.c + " is leaving");
		
		host.msgLeavingTable(mc.c);
		myCustomers.remove(mc);
		waiterGui.DoLeaveCustomer();
		try {
			atFront.acquire();
		} catch(InterruptedException e){
			e.printStackTrace();
		}
		stateChanged();
	}
	
	private void returnToFront(){
		waiterGui.DoGoToFront();
		stateChanged();
	}

	// The animation DoXYZ() routines
	private void DoSeatCustomer(CustomerAgent customer, int table) {
		//Notice how we print "customer" directly. It's toString method will do it.
		//Same with "table"
		waiterGui.DoGoToCustomer(customer);
		try {
			atCustomer.acquire();
		} catch(InterruptedException e){
			e.printStackTrace();
		}
		print("Seating " + customer + " at " + table);
		waiterGui.DoBringToTable(customer, table); 

	}

	private void DoGoTakeOrder(MyCustomer mc){
		/*Go to customer's table
		 * Essentially the same function as DoSeatCustomer
		 */
		print("Going to table" + mc.table);
		waiterGui.DoGoToTable(mc.table);
	}
	
	private void DoGiveOrderToCook(){
//		print("Going to cook" );
		waiterGui.DoGoToCook();
	}
//	
//	private void DoDeliverOrder(MyCustomer mc){
//		waiterGui.DoDeliverOrderToTable(mc.table);
//	}
//	
	private void DoGetBill() {
		waiterGui.DoGoToCashier();
		stateChanged();
	}

	private void waitForAction(MyCustomer mc){
		mc.state = customerState.NO_ACTION;
	}
	/**Non normative scenarios*/
	
	public void tellCookDepleteOptions(){
		cook.msgDepleteOptions();
		stateChanged();
	}
	
	public void tellCookDepleteOption(String s){
		cook.msgDepleteOption(s);
		stateChanged();
	}
	
	public void tellCookDepleteMarkets(){
		cook.msgDepleteMarkets();
		stateChanged();
	}
	//utilities

	public void setGui(WaiterGui gui) {
		waiterGui = gui;
	}

	public WaiterGui getGui() {
		return waiterGui;
	}

	private class Table {
		Customer occupiedBy;
		int tableNumber;

		Table(int tableNumber) {
			this.tableNumber = tableNumber;
		}

		void setOccupant(Customer cust) {
			occupiedBy = cust;
		}

		void setUnoccupied() {
			occupiedBy = null;
		}

		Customer getOccupant() {
			return occupiedBy;
		}

		boolean isOccupied() {
			return occupiedBy != null;
		}

		public String toString() {
			return "table " + tableNumber;
		}
	}
	
	private class MyCustomer {
		CustomerAgent c;
		customerState state;
		int table;
		String choice;
		Order order;
		Bill bill;
		boolean leftTable;
		
		
		MyCustomer (CustomerAgent c, int table){
			this.c = c;
			this.table = table;
			order = null;
			bill = null;
			state = customerState.waitingForSeat;
			leftTable = false;
		}

		public void setOrder(Order order){
			this.order = order;
		}
		
		public void setBill(Bill bill){
			this.bill = bill;
		}
	}
	private class Order {
		String name;
		orderState state;

		Order (String name){
			this.name = name;
			this.state = orderState.pending;
		}
	}
	
}

