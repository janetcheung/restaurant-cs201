package restaurant.interfaces;

import restaurant.CustomerAgent;
import restaurant.WaiterAgent;
import restaurant.CashierAgent.Bill;

/**
 * A sample Waiter interface built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public interface Waiter {
	/**
	 * @param total The cost according to the cashier
	 *
	 * Sent by the cashier prompting the customer's money after the customer has approached the cashier.
	 */
	public void msgBillPlease(Customer customer);
	/**
	 * @param total change (if any) due to the customer
	 *
	 * Sent by the cashier to end the transaction between him and the customer. total will be >= 0 .
	 */
	public void msgBillReady(Bill b);

	/**
	 * @param remaining_cost how much money is owed
	 * Sent by the cashier if the customer does not pay enough for the bill (in lieu of sending {@link #HereIsYourChange(double)}
	 */
//	public void msgCantPay(Customer customer);
	
}