package restaurant.test.mock;


import java.util.List;

import restaurant.CookAgent.Food;
import restaurant.MarketAgent.Order;
import restaurant.Menu;
import restaurant.interfaces.Market;
import restaurant.interfaces.Cashier;


public class MockMarket extends Mock implements Market {

	/**
	 * Reference to the Cashier under test that can be set by the unit test.
	 */
	public Cashier cashier;
	private double bill;
	private double money;
	private double change;

	public MockMarket(String name) {
		super(name);

	}

	@Override
	public void msgReceivedOrder(List<Food> list) {
		log.add(new LoggedEvent("Received ReceivedOrder from Cook."));

	}

	@Override
	public void msgOrderDone(Order o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsPayment(String choice, double money) {
		log.add(new LoggedEvent("Received HereIsPayment from cashier. Total = "+ money));
		

	}
	
	
}
