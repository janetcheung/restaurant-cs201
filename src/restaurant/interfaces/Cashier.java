package restaurant.interfaces;

import java.util.List;

import restaurant.CustomerAgent;
import restaurant.WaiterAgent;
import restaurant.CashierAgent.Bill;

/**
 * A sample Cashier interface built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public interface Cashier {
	/**
	 * @param total The cost according to the cashier
	 *
	 * Sent by the cashier prompting the customer's money after the customer has approached the cashier.
	 */
	public abstract void msgReceivedBillRequest(Waiter waiter, Customer customer, String choice, int table);

	/**
	 * @param total change (if any) due to the customer
	 *
	 * Sent by the cashier to end the transaction between him and the customer. total will be >= 0 .
	 */
	public abstract void msgHereIsPayment(Customer customer, double money);


	/**
	 * @param remaining_cost how much money is owed
	 * Sent by the cashier if the customer does not pay enough for the bill (in lieu of sending {@link #HereIsYourChange(double)}
	 */
	public abstract void msgCantPay(Customer customer);
	
	public abstract List<Bill> getMarketBills();
	
	public abstract double getMoney();
	public abstract void setMarketBills(List<Bill> marketBills);
}