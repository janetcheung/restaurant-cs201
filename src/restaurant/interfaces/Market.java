package restaurant.interfaces;

import java.util.List;

import restaurant.CashierAgent;
import restaurant.CookAgent;
import restaurant.MarketAgent.Food;
import restaurant.MarketAgent.Order;

public interface Market {
	
	public abstract void msgReceivedOrder(List<CookAgent.Food> list);

	public abstract void msgOrderDone(Order o);

//	public abstract void msgRestockedFood(Food f);

	public abstract void msgHereIsPayment(String choice, double money);

//	public abstract void msgCantPay(Customer customer);
	
}