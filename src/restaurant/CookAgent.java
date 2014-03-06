package restaurant;

import agent.Agent;
import restaurant.CustomerAgent.AgentEvent;
import restaurant.gui.CookGui;
import restaurant.gui.HostGui;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Cook Agent
 */

public class CookAgent extends Agent {
	
	private String name;
	private int nMarkets = 3;
	
	private List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());
	private List<Food> reorders = Collections.synchronizedList(new ArrayList<Food>());
	private List<MarketAgent> markets = Collections.synchronizedList(new ArrayList<MarketAgent>(nMarkets));
;
	private HashMap<String, Food> inventory;
	private Timer timer = new Timer();
	private boolean marketDepleted = false;
	private enum orderState {pending, cooking, done, waitingForDelivery, finished, outOfOrder};
	private enum foodState {normal, low, out, waitingForDelivery, waitingForRedelivery}
	
	private Semaphore atGrill = new Semaphore(0, true);
	private Semaphore atFridge = new Semaphore(0,true);
	private Semaphore atPlating = new Semaphore(0,true);
	private Semaphore atHome = new Semaphore(0,true);

	public CookGui cookGui = null;

	public CookAgent(String name) {
		super();

		this.name = name;
		
		inventory = new HashMap<String, Food>();
		inventory.put("Steak", new Food("Steak", 5, 3000));
		inventory.put("Chicken", new Food("Chicken", 5, 4000));
		inventory.put("Salad", new Food("Salad", 5, 2000));
		inventory.put("Pizza", new Food("Pizza", 5, 5000));
		
	}

	public String getName() {
		return name;
	}

	public void addMarket(MarketAgent m) {
		markets.add(m);
	}
	// Messages

	public void msgReceivedOrder(WaiterAgent waiter, String choice, int table) {
		print("Received msgReceivedOrder");
		orders.add(new Order(waiter,choice, table));
		stateChanged();
	}
	
	public void msgOrderDone(Order o) {
		o.state = orderState.done;
		stateChanged();
	}
	
	public void msgRestockedFood(String choice, int n){
//		System.err.println("Received msgRestockedFood for " + choice);
		print("Received msgRestockedFood for " + choice);
		synchronized(reorders){
			for (Food f: reorders){
				if (f.name == choice){
					f.state = foodState.normal;
				}
			}
		}
		inventory.get(choice).quantity = inventory.get(choice).quantity + n;
//		System.err.println(choice + " inventory: " + inventory.get(choice).quantity);
		stateChanged();
	}
	
	public void msgGotFood(String s, int table){
		synchronized (orders){
			for (Order o: orders){
				if (o.choice == s){
					if (o.tableNumber == table){
						o.state = orderState.finished;
						cookGui.setDelivered(o.choice);
					}
				}
			}
		}
	}
	
	public void msgDontHaveFood(String choice, MarketAgent m, int n){
//		System.err.println("Received msgDontHaveFood for " + choice);
		print("Received msgDontHaveFood for " + choice + " from " + m.getName());
		synchronized(reorders){
			for (Food f: reorders){
				if (f.name == choice) {
//				System.err.println("In msgDontHaveFood loop");
					if (f.lowQuantity < f.quantity+n){
						f.state = foodState.normal;
//						System.err.println("Food state is normal");
					}
					else{
//						System.err.println("Need to choose different market");
						f.reorderQuantity= n - f.reorderQuantity;
						f.availableMarkets.remove(m.getName());
						reorders.remove(f);
						f.state = foodState.waitingForRedelivery;
					}	
					stateChanged();
				}
			}
		}
	}
	/**Non normative scenarios*/
	public void msgDepleteOptions(){
		print("Depleting options");
		synchronized(inventory.keySet()){
			for (String s : inventory.keySet()){
				inventory.get(s).quantity = 0;
				inventory.get(s).state = foodState.out;
			}
		}
		stateChanged();
	}
	
	public void msgDepleteOption(String s){
		print("Depleting " + s);
		inventory.get(s).quantity = 0;
		inventory.get(s).state = foodState.out;
		
		stateChanged();
	}
	
	public void msgDepleteMarkets(){
		print("Depleting markets");
		marketDepleted = true;
		stateChanged();
	}
	
	public void msgAtGrill(){
		atGrill.release();
	}
	
	public void msgAtFridge(){
		atFridge.release();
	}
	
	public void msgAtPlating(){
		atPlating.release();
	}
	
	public void msgAtHome(){
		atHome.release();
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
		
	if (marketDepleted){
		depleteMarkets();
		return true;
	}
	try {
	synchronized(inventory.keySet()){	
		for (String s: inventory.keySet()){
			if (inventory.get(s).state == foodState.out){
//				System.err.println(inventory.get(s).name + " state is out");
				reorderFood(inventory.get(s));
				return true;
			}
		}
	}
	synchronized(inventory.keySet()){
		for (String s: inventory.keySet()){
//			System.err.println("in scheduler");
			if (inventory.get(s).state == foodState.low){
//				System.err.println(inventory.get(s).name + " state is low");
				print(inventory.get(s).name + " level is low");
				reorderFood(inventory.get(s));
				return true;
			}
		}
	}
	
	synchronized(orders){
		for (Order o: orders){
			if (o.state == orderState.finished){
				removeIt(o);
			}
		}
	}
	
	synchronized(orders){
		for (Order o: orders){
			if (o.state == orderState.done){
				plateIt(o);
				return true;
			}
		}
	}
	
	synchronized(orders){
		for (Order o: orders){
			if (o.state == orderState.pending){
				cookIt(o);
				return true;
			}
		}
	}
	
	} catch (ConcurrentModificationException e){
		return false;
	}
	synchronized(reorders){
		for (Food f: reorders){
			if (f.state == foodState.normal){
				removeFoodFromReorders(f);
				return true;
			}
		}
	}
	synchronized(reorders){
		for (Food f: reorders){
			if (f.state == foodState.waitingForDelivery){
				//do nothing;
				return true;
			}
		}
	}
	synchronized(reorders){
		for (Food f: reorders){
			if (f.state == foodState.waitingForRedelivery){
//				System.err.println("Choosing another market");
				chooseAnotherMarket(f);
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
	
	public void cookIt(Order o){
		/*DoCooking(o);
		 * o.state = cooking;
		 * 
		 */
		if (inventory.get(o.choice).quantity == 0){
			print("Out of this option");
			o.waiter.msgOutOfOrder(o.choice, o.tableNumber);
			o.state = orderState.outOfOrder;
			orders.remove(o);
		}
		else {
			DoCollectIngredients();
			o.state = orderState.cooking;
//			inventory.put(o.choice, inventory.get(o.choice)-1);
			inventory.get(o.choice).quantity--;
			print(o.choice +" left: " + inventory.get(o.choice).quantity);
			DoCooking(o);
		}
		
		if (inventory.get(o.choice).quantity <= inventory.get(o.choice).lowQuantity){
			if (inventory.get(o.choice).state != foodState.waitingForDelivery  ||
					inventory.get(o.choice).state != foodState.waitingForRedelivery){
				inventory.get(o.choice).state = foodState.low;
			}
		}
		stateChanged();
	}
	
	public void plateIt(Order o){
		print("Plating order " + o.choice);
		cookGui.doPlating(o.choice);
		try{
			atPlating.acquire();
		}catch (InterruptedException e){
			e.printStackTrace();
		}		
//		orders.remove(o);
		o.state = orderState.waitingForDelivery;
		o.waiter.msgOrderDone(o.choice, o.tableNumber);
		stateChanged();
	}

	public void removeIt(Order o){
//		System.err.println("Remove imgicon");
		cookGui.setDelivered(o.choice);
		orders.remove(o);
		stateChanged();
	}
	public void reorderFood(Food f){
		//Choose a random market	
		Random random = new Random();
		int randomMarket = random.nextInt(markets.size());
		
//		for (String am: f.availableMarkets){
//			if (markets.get(randomMarket).getName() == am){
				reorders.add(new Food(f.name,f.reorderQuantity, f.cookingTime*5));
//				System.err.println("Reordering " + f.name);
				print("Reordering " + f.name);
				markets.get(randomMarket).msgReceivedOrder(reorders);
//				System.err.println("Cook sent in reorder");
				f.state = foodState.waitingForDelivery;
				stateChanged();
//			}
//		}
	}
	
	public void chooseAnotherMarket(Food f){
		Random random = new Random();
		int randomMarket = random.nextInt(f.availableMarkets.size());
		for (String am : f.availableMarkets){
			if (am == markets.get(randomMarket).getName())
				reorders.add(new Food(f.name, f.reorderQuantity, f.cookingTime*5));
				markets.get(randomMarket).msgReceivedOrder(reorders);
				f.state = foodState.waitingForDelivery;
				stateChanged();
		}
	}
	
	public void removeFoodFromReorders(Food f){
		reorders.remove(f);
		stateChanged();
	}
	//DoXYZ() routines
	public void DoCooking(final Order o){
		//get order's cooking time
		print("cooking order "+ o.choice);
		cookGui.goToGrill(o.choice);
		
		try{
			atGrill.acquire();
		}catch (InterruptedException e){
			e.printStackTrace();
		}

		cookGui.doCooking(o.choice);
		timer.schedule(new TimerTask() {
			public void run() {
				o.state = orderState.done;
//				System.err.println("o.state: " + o.state);
				cookGui.doneCooking(o.choice);
				stateChanged();
			}
		},inventory.get(o.choice).cookingTime);

	}
	
	public void DoPlating(Order o){ 
		//nothing right now
		print("Plating order " + o.choice);
		cookGui.doPlating(o.choice);
		try{
			atPlating.acquire();
		}catch (InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public void DoCollectIngredients(){
		print("Collecting ingredients ");
		
		cookGui.doCollectIngredients();
		
		try{
			atFridge.acquire();
		}catch (InterruptedException e){
			e.printStackTrace();
		}
	}
	//utilities

	public void setGui(CookGui gui) {
		cookGui = gui;
	}

	public CookGui getGui() {
		return cookGui;
	}
	
	public void depleteMarkets(){
		for (MarketAgent m: markets)
			m.msgDepleteStock();
		
		marketDepleted = false;
		stateChanged();
	}
	public int getStock(String choice){
		return inventory.get(choice).quantity;
	}
	public void setStock(String choice, int amount){
		inventory.get(choice).quantity = amount;
	}
	
	private class Order {
		WaiterAgent waiter;
		String choice;
		int tableNumber;
		orderState state;
	
		Order(WaiterAgent waiter, String choice, int tableNumber){
			this.waiter = waiter;
			this.choice = choice;
			this.tableNumber = tableNumber;
			state = orderState.pending;
		}
	
		//if two customers have same order for the same waiter (PROBLEM)
	}
	
	public class Food {
		String name;
		foodState state;
		List<String> availableMarkets = new ArrayList<String>(nMarkets);
		int quantity;
		int cookingTime;
		int lowQuantity;
		int reorderQuantity;
		
		Food(String name, int quantity, int cookingTime){
			this.name = name;
			this.quantity = quantity;
			this.cookingTime = cookingTime;
			 
			lowQuantity = 2;
			reorderQuantity = 5;
			state = foodState.normal;
			
			for (int i = 0; i< nMarkets; i++){
				availableMarkets.add("Market " +i);
			}
		}
		
		public void setFoodState(foodState state){
			this.state = state;
		}
	}
	
	
	
}

