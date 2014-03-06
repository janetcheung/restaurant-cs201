package restaurant;

import agent.Agent;
import restaurant.CustomerAgent.AgentEvent;
import restaurant.gui.CookGui;
import restaurant.gui.HostGui;
import restaurant.interfaces.Market;
import restaurant.interfaces.Cashier;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Market Agent
 */

public class MarketAgent extends Agent implements Market {
	
	private String name;
	private int marketNum;
	private List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());
	private HashMap<String, Food> inventory;
	private HashMap<String, Double> prices;
	private Timer timer = new Timer();

	private enum orderState {pending, packaging, done, needPayment, madeIOU, paid, outOfOrder};
	private enum foodState {normal, low, waitingForDelivery, waitingForRedelivery}
//	private enum billState {needPayment, madeIOU, done}

//	public CookGui cookGui = null;

	private CookAgent cook = null;
	private CashierAgent cashier = null;
	
	public MarketAgent(String name, int marketNum) {
		super();

		this.name = name;
		this.marketNum = marketNum;
		
		inventory = new HashMap<String, Food>();
		inventory.put("Steak", new Food("Steak", 10, 300*5));
		inventory.put("Chicken", new Food("Chicken", 10, 400*5));
		inventory.put("Salad", new Food("Salad", 10, 200*5));
		inventory.put("Pizza", new Food("Pizza", 10, 500*5));
		
		prices = new HashMap<String, Double>();
		prices.put("Steak", 5.00);
		prices.put("Chicken", 3.00);
		prices.put("Salad", 2.00);
		prices.put("Pizza", 5.00);
		
	}

	public String getName() {
		return name;
	}

	public void setCook(CookAgent cook){
		this.cook = cook;
	}
	
	public void setCashier(CashierAgent cashier){
		this.cashier = cashier;
	}

	// Messages

	public void msgReceivedOrder(List<CookAgent.Food> list) {
		print("Received msgReceivedOrder");
//		System.err.println("Market received order");
//		orders.add(new Order(choice));
		synchronized(list){
			for (CookAgent.Food f: list)
				orders.add(new Order(f.name, f.reorderQuantity));
		}
		stateChanged();
	}
	
	public void msgOrderDone(Order o) {
//		System.err.println("Market order done");
		print("Received msgOrderDone");
		o.state = orderState.done;
		stateChanged();
	}
	
	public void msgRestockedFood(Food f){
		f.state = foodState.normal;
		stateChanged();
	}

	public void msgDepleteStock(){
		synchronized(inventory.keySet()){
			for (String s: inventory.keySet()){
				inventory.get(s).quantity = 3;
			}
		}
		stateChanged();
	}
	public void msgHereIsPayment(String choice, double money){
		print("Received msgHereIsPayment");
		for (Order o: orders){
			if (o.choice == choice){
				o.state = orderState.paid;
				stateChanged();
				return;
			}
		}
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		/* If there exists o in orders s.t. order.state=done
		 * 		plateIt(o);
		 * else if there exists o in order s.t. order.state=pending
		 * 		cookIt(o);
		 */
	synchronized(orders){
		for (Order o: orders){
			if (o.state == orderState.needPayment){
				//do nothing
				return true;
			}
		}
	}
	
	synchronized(orders){
		for(Order o: orders){
			if (o.state == orderState.paid){
				removeIt(o);
				return true;
			}
		}
	}
	synchronized(orders){
		for (Order o: orders){
			if (o.state == orderState.done){
				shipIt(o);
				return true;
			}
		}
	}
	synchronized(orders){
		for (Order o: orders){
			if (o.state == orderState.pending){
				packageIt(o);
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
	
	public void removeIt(Order o){
		orders.remove(o);
		stateChanged();
	}
	
	public void packageIt(Order o){
		/*DoCooking(o);
		 * o.state = cooking;
		 * 
		 */
		if (inventory.get(o.choice).quantity == 0){
			print("Out of this option");
//			o.waiter.msgOutOfOrder(o.choice, o.tableNumber);
			cook.msgDontHaveFood(o.choice,this,0);
			o.state = orderState.outOfOrder;
			orders.remove(o);
		}
		else if (inventory.get(o.choice).quantity < o.reorderQuantity){
				inventory.get(o.choice).state = foodState.low;
				inventory.get(o.choice).inStock = false;
				
				o.total = inventory.get(o.choice).quantity * prices.get(o.choice);
//				System.err.println("Market cannot completely fulfill " + o.choice);
				DoPackaging(o);
		}
		else {
			o.state = orderState.packaging;
//			inventory.put(o.choice, inventory.get(o.choice)-1);
			inventory.get(o.choice).quantity = inventory.get(o.choice).quantity - o.reorderQuantity;
			print(o.choice +" left: " + inventory.get(o.choice).quantity);
			o.total = o.reorderQuantity * prices.get(o.choice);
			DoPackaging(o);
		}
		
		
		stateChanged();
	}
	
	public void shipIt(Order o){

//		o.waiter.msgOrderDone(o.choice, o.tableNumber);
		print("shipping order to cook");
		DoShipping(o);
//		orders.remove(o);
		stateChanged();
	}

	//DoXYZ() routines
	public void DoPackaging(final Order o){
		//get order's cooking time
//		print("packing order "+ o.choice);
		
		timer.schedule(new TimerTask() {
			public void run() {
				o.state = orderState.done;
				stateChanged();
			}
		},inventory.get(o.choice).shippingTime);
	}
	
	public void DoShipping(Order o){ 
		/* Tell cook this market cannot completely fulfill order
		 * and deliver what was available
		 */
//		System.err.println("in DoShipping");
		if (inventory.get(o.choice).state != foodState.low){
			cook.msgDontHaveFood(o.choice, this, inventory.get(o.choice).quantity);
			cashier.msgReceivedMarketBill(this, o.choice, o.total);

		}
		else {
//			System.err.println("o.choice: " + o.choice);
//			System.err.println("o.total: " + o.total);
			cook.msgRestockedFood(o.choice, o.reorderQuantity);
			cashier.msgPartialOrderBill(this, o.choice, o.total);
		}
		o.state = orderState.needPayment;
		stateChanged();
	}
	//utilities
	public void setStock(String choice, int quantity){
		inventory.get(choice).quantity = quantity;
	}
	
	public class Order {
		String choice;
		int reorderQuantity;
		double total;
		orderState state;
	
		Order(String choice, int reorderQuantity){
			this.choice = choice;
			this.reorderQuantity = reorderQuantity;
			state = orderState.pending;
		}
		
	}
	
	public class Food {
		String name;
		foodState state;
		int quantity;
		int shippingTime;
		int lowQuantity;
		int reorderQuantity;
		boolean inStock;
		
		Food(String name, int quantity, int shippingTime){
			this.name = name;
			this.quantity = quantity;
			this.shippingTime = shippingTime;
			
			lowQuantity = 2;
			reorderQuantity = 5;
			inStock = true;
			state = foodState.normal;
		}
		
		public void setInStock(boolean inStock){
			this.inStock = inStock;
		}
	}
	
	
	
}

