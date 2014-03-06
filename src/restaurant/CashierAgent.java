package restaurant;

import agent.Agent;
import restaurant.CustomerAgent.AgentEvent;
import restaurant.gui.CookGui;
import restaurant.gui.HostGui;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Waiter;
import restaurant.interfaces.Market;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Cook Agent
 */

public class CashierAgent extends Agent implements Cashier {
	
	private String name;
	private double money;
	private int nMarkets = 3;
	private List<Bill> bills = Collections.synchronizedList(new ArrayList<Bill>());
	private List<Bill> marketBills = Collections.synchronizedList(new ArrayList<Bill>());
	private List<MarketAgent> markets = Collections.synchronizedList(new ArrayList<MarketAgent>(nMarkets));
	private List<OwingCustomer> owingCustomers = Collections.synchronizedList(new ArrayList<OwingCustomer>());
	private HashMap<String, Double> prices = new HashMap<String, Double>();;
	private Timer timer = new Timer();
	public enum billState {pending, ready, needPayment, needChange, madeIOU, done};

	public CashierAgent(String name) {
		super();

		this.name = name;		
		
		prices.put("Steak", 15.99);
		prices.put("Chicken",10.99);
		prices.put("Salad", 5.99);
		prices.put("Pizza", 8.99);
		
		money = 1000;
		
	}

	public String getName() {
		return name;
	}
	
	public void addMarket(MarketAgent m) {
		markets.add(m);
	}

	// Messages

	public void msgReceivedBillRequest(Waiter waiter, Customer customer, String choice, int table) {
		print("Received msgReceivedBillRequest");
		bills.add(new Bill(waiter, customer, choice, table));
		stateChanged();
	}
	
	public void msgHereIsPayment(Customer customer, double money){
		print("Received msgHereIsPayment");
		synchronized(getBills()){
			for (Bill b : getBills()){
				if (b.customer == customer) {
					b.payment = money;
					b.setState(billState.needChange);
//					System.err.println ("Customer needs change: "+ b.state);
					stateChanged();

				}
			}
		}
	}
	
	public void msgReceivedMarketBill(Market market, String choice, double total){
		print("Received msgReceivedMarketBill");
		Bill b = new Bill(null, null, choice, 0);
		b.setState(billState.needPayment);
		b.market = market;
		b.total = total;
		marketBills.add(b);
	
		stateChanged();
	}
	
	/**Non normative scenarios
	 * @param  */
	public void msgCantPay(Customer customer){
		print("Received msgCantPay");
		owingCustomers.add(new OwingCustomer(customer, customer.getBill()));
//		System.err.println("OwingCustomer: " + customer.getName() + " owes " + customer.getBill());

		synchronized(getBills()){
			for (Bill b: getBills()){
				if (b.customer == customer){
					b.setState(billState.madeIOU);
				}
			}
		}
		stateChanged();
	}
	
	public void msgPartialOrderBill(Market market, String choice, double total){
		print("Received msgPartialOrderBill");
		Bill b = new Bill(null, null, choice, 0);
		b.setState(billState.needPayment);
		b.market = market;
		b.total = total;
		marketBills.add(b);
	
		stateChanged();
	}
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {

	synchronized(getBills()){
		for (Bill b: getBills()){
			if (b.state == billState.done){
				removeBill(b);
				return true;
			}
		}
	}	
	synchronized(getBills()){
		for (Bill b: getBills()){
//			System.err.println("Bill status: " + b.state);
			if (b.state == billState.pending){
				updateTotal(b);
				return true;
			}
		}
	}
		
	synchronized(getBills()){
		for (Bill b: getBills()){
			if (b.state == billState.ready){
				giveBill(b);
				return true;
			}
		}
	}
		
	synchronized(getBills()){
		for (Bill b: getBills()){
			if (b.state == billState.needPayment){
				//giveBill(b);
				return true;
			}
		}
	}
	synchronized(getBills()){
		for (Bill b: getBills()){
			if (b.state == billState.madeIOU){
				dealtWithBill(b);
				return true;
			}
		}
	}
	synchronized(getBills()){
		for (Bill b: getBills()){
			if (b.state == billState.needChange){
//				System.err.println("Cashier in scheduler: giving change");
				giveChange(b);
				return true;
			}
		}
	}
	
	synchronized(getMarketBills()){
		for (Bill b: getMarketBills()){
			if (b.state == billState.needPayment){
				givePayment(b);
				return true;
			}
		}
	}
	
	synchronized(getMarketBills()){
		for (Bill b: getMarketBills()){
			if (b.state == billState.done){
				removeMarketBill(b);
				return true;
			}
		}
	}
		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions
	
	public void updateTotal(Bill bill){
		//check if it is customer owes anything, then update the bill
		OwingCustomer cust = null;
//		System.err.println("In updateTotal");
		bill.total = prices.get(bill.choice);
		
	synchronized(owingCustomers){
		for (OwingCustomer o: owingCustomers){
			if (o.customer.equals(bill.customer)){
//				System.err.println("Added bill to total");
				cust = new OwingCustomer(o.customer, o.total);
				bill.total = o.total + bill.total;
//				System.err.println("bill total : " + bill.total);
				print(bill.customer.getName() + " bill total: " + bill.total);
			}
		}
	}
		if (cust != null)
			owingCustomers.remove(cust);
		bill.setState(billState.ready);
		stateChanged();
	}
	
	public void giveBill(Bill b){
		b.waiter.msgBillReady(b);
		b.setState(billState.needPayment);
		stateChanged();
	}
	
		
	public void giveChange(Bill b){
		double change = b.payment - b.total;
		money -= change;
		
		b.change = change;
		b.customer.msgHereIsChange(change);
//		getBills().remove(b);
		b.state = billState.done;
		stateChanged();
	}
	
	public void dealtWithBill(Bill b){
		b.customer.msgYouCanGo();
//		getBills().remove(b);
		b.state = billState.done;
		stateChanged();
	}
	
	public void removeBill(Bill b){
		getBills().remove(b);
		stateChanged();
	}
	
	public void givePayment(Bill b){
		b.market.msgHereIsPayment(b.choice, b.total);
//		System.err.println("b.choice: "+b.choice);
//		System.err.println("b.total: " + b.total);
		money -= b.total;

		b.state = billState.done;
		stateChanged();
	}
	
	public void removeMarketBill(Bill b){
		getMarketBills().remove(b);
		stateChanged();
	}
	
	
	//utilities

	public double getMoney(){
		return money;
	}
	
	public void setMoney(double m){
		this.money = m;
	}
	public List<Bill> getBills() {
		return bills;
	}

	public void setBills(List<Bill> bills) {
		this.bills = bills;
	}

	public List<Bill> getMarketBills(){
		return marketBills;
	}
	
	public void setMarketBills(List<Bill> marketBills){
		this.marketBills = marketBills;
	}
	public class Bill {
		Waiter waiter;
		Customer customer;
		Market market;
		String choice;
		int tableNumber;
		double total;
		double payment;
		double change;
		private billState state;
	
		public Bill(Waiter waiter, Customer customer, String choice, int tableNumber){
			this.waiter = waiter;
			this.customer = customer;
			this.choice = choice;
			this.tableNumber = tableNumber;
			this.setState(billState.pending);
			
			market = null;
			total = 0;
			payment = 0;
			change = 0;
		}
		
		public double getTotal(){
			return total;
		}

		public double getPayment() {
			return payment;
		}
		
		public double getChange() {
			return change;
		}
		public billState getState() {
			return state;
		}

		public void setState(billState state) {
			this.state = state;
		}
		
		public Customer getCustomer(){
			return customer;
		}
	}	
	
	private class OwingCustomer{
		Customer customer;
		double total;
		
		OwingCustomer(Customer customer, double d){
			this.customer = customer;
			this.total = d;
		}
	}

}

