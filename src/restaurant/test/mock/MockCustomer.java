package restaurant.test.mock;


import restaurant.Menu;
import restaurant.interfaces.Waiter;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;

/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public class MockCustomer extends Mock implements Customer {

	/**
	 * Reference to the Cashier under test that can be set by the unit test.
	 */
	public Cashier cashier;
	private double bill;
	private double money = 20;
	private double change;
	private boolean cheapskate;

	public MockCustomer(String name) {
		super(name);

	}
	@Override
	public void msgHereIsBill(double total) {
		log.add(new LoggedEvent("Received HereIsYourBill from waiter. Total = "+ total));
	
		if(this.isCheapskate()){
			//test the non-normative scenario where the customer has no money if they are labeled a "cheapskate"
			cashier.msgCantPay(this);
		}
//		else if (this.getMoney() <= 5) {
//			//test the non-normative scenario where the customer has no money and is not a "cheapskate"
//		}
		else if (this.getMoney() >= 16){
			//test the non-normative scenario where the customer overpays if their name contains the string "rich"
			cashier.msgHereIsPayment(this, this.getMoney());
		}
//		else {
//			//test the normative scenario
//			cashier.msgHereIsPayment(this, total);
//		}
		else
		cashier.msgHereIsPayment(this, total);

	}

	@Override
	public void msgHereIsChange(double total) {
		log.add(new LoggedEvent("Received HereIsYourChange from cashier. Change = "+ total));
//		System.err.println("Customer received msgHereIsYourChange from cashier.  Change = " + total);
	}

//	@Override
//	public void YouOweUs(double remaining_cost) {
//		log.add(new LoggedEvent("Received YouOweUs from cashier. Debt = "+ remaining_cost));
//	}
	
	@Override
	public void msgYouCanGo(){
//		System.err.println("Customer can go");
		log.add(new LoggedEvent("received msgYouCanGo"));
	}
	@Override
	public double getBill() {
		return bill;
	}
	@Override
	public double getMoney() {
		return money;
	}
	
	@Override
	public double getChange() {
		return change;
	}
	@Override
	public boolean isCheapskate() {
		return cheapskate;
	}
	
	@Override
	public void setCheapskate(boolean b){
		cheapskate = b;
	}
	@Override
	public void gotPoor() {
		money = 5;
		log.add(new LoggedEvent("customer got poor"));

	}
	
	@Override
	public void gotRich() {
		money = 50;
		log.add(new LoggedEvent("customer got rich"));
	}
	
	
}
