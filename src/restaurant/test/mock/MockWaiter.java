package restaurant.test.mock;

import restaurant.CashierAgent.Bill;
import restaurant.CashierAgent.billState;
import restaurant.Menu;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Waiter;

public class MockWaiter extends Mock implements Waiter {
	
	public MockWaiter(String name) {
		super(name);
	}

	public Cashier cashier;

	@Override
	public void msgBillPlease(Customer customer) {
	}

	@Override
	public void msgBillReady(Bill b) {
		// TODO Auto-generated method stub
//		System.err.println("waiter received msgBillReady from cashier");
//		System.err.println("b: " +b);
//		System.err.println("b.getCustomer: " + b.getCustomer());
//		System.err.println("b.getTotal:" +b.getTotal());
		log.add(new LoggedEvent("Received msgBillReady from cashier."));
		b.setState(billState.needPayment);
		b.getCustomer().msgHereIsBill(b.getTotal());
	}
	
}

