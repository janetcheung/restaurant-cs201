package restaurant.interfaces;

/**
 * A sample Customer interface built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public interface Customer {
	/**
	 * @param total The cost according to the cashier
	 *
	 * Sent by the cashier prompting the customer's money after the customer has approached the cashier.
	 */
	public abstract void msgHereIsBill(double bill);

	/**
	 * @param total change (if any) due to the customer
	 *
	 * Sent by the cashier to end the transaction between him and the customer. total will be >= 0 .
	 */
	public void msgHereIsChange(double change);

	/**
	 *  @param Allows the customer to leave
	 *  
	 *  Sent by the cashier to allow a cheapskate customer to leave the restaurant
	 */
	public abstract void msgYouCanGo();

	public abstract String getName();

	public abstract double getBill();
	
	public abstract double getMoney();
	
	public abstract double getChange();
	
	public abstract boolean isCheapskate();
	
	public abstract void setCheapskate(boolean b);
	
	public abstract void gotPoor();
	
	public abstract void gotRich();

	/**
	 * @param remaining_cost how much money is owed
	 * Sent by the cashier if the customer does not pay enough for the bill (in lieu of sending {@link #HereIsYourChange(double)}
	 */
//	public abstract void YouOweUs(double remaining_cost);

}