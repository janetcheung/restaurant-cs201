package restaurant;

import restaurant.gui.CustomerGui;
import restaurant.gui.RestaurantGui;
import restaurant.interfaces.Customer;
import agent.Agent;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;

/**
 * Restaurant customer agent.
 */
public class CustomerAgent extends Agent implements Customer{
	private String name;
	private int hungerLevel = 5;        // determines length of meal
	Timer timer = new Timer();
	private CustomerGui customerGui;
	
	private String choice;
	private Menu menu = null;
	private double bill;
	private double money = 20;
	private double change;
	private int num;
	private boolean cheapskate = false;

	// agent correspondents
	private HostAgent host;
	private CashierAgent cashier;
	private WaiterAgent waiter;

	//    private boolean isHungry = false; //hack for gui
	public enum AgentState
	{DoingNothing, WaitingInRestaurant, BeingSeated, Seated, calledWaiter, ordered, Eating, DoneEating, NeedBill, 
		AtCashier, Paying, WaitingForChange, Leaving};
	private AgentState state = AgentState.DoingNothing;//The start state

	public enum AgentEvent 
	{none, gotHungry, followWaiter, seated, decidedToLeave, decidedChoice, ordering, waitingForFood, needToReorder, 
		gotFood, doneEating, waitingForBill, gotBill, paying, waitingForChange, gotChange, doneLeaving};
	AgentEvent event = AgentEvent.none;


	/**
	 * Constructor for CustomerAgent class
	 *
	 * @param name name of the customer
	 * @param gui  reference to the customergui so the customer can send it messages
	 */
	public CustomerAgent(String name){
		super();
		this.name = name;
	}

	/**
	 * hack to establish connection to Host agent.
	 */
	public void setHost(HostAgent host) {
		this.host = host;
	}

	public void setWaiter(WaiterAgent waiter) {
		this.waiter = waiter;
	}
	
	public void setCashier(CashierAgent cashier){
		this.cashier = cashier;
	}
	public String getCustomerName() {
		return name;
	}
	// Messages
	public void msgHereIsYourNumber(int num){
		this.num = num;
//		System.err.println("Customer Number: " + num);
		customerGui.setQueuePosition(num);
//		System.err.println("XDestination after getting number: "+ customerGui.getXDestination());

		stateChanged();
	}
	
	public void msgFollowWaiter(WaiterAgent waiter, Menu menu){
		this.waiter = waiter;
		this.menu = menu;
		print("Received msgFollowWaiter");
		event = AgentEvent.followWaiter;
		stateChanged();
	}
	
	public void msgDecidedChoice(){
		event = AgentEvent.decidedChoice;
		stateChanged();
	}
	
	public void msgWhatDoYouWant(){
		print("Received msgWhatDoYouWant");
		event = AgentEvent.ordering;
		stateChanged();
	}
	
	
	public void msgPleaseReorder(Menu menu){
		print ("Received msgPleaseReorder");
//		print ("" + state);
		this.menu = menu;
		
		Random random = new Random();
		int decision = random.nextInt(10);
		if (decision % 5 == 0){
			event = AgentEvent.decidedToLeave;
//			System.err.println("Choosing to leave");
			print("Choosing to leave");
		}
		else
			event = AgentEvent.needToReorder;
		stateChanged();
	}
	
	public void msgHereIsFood(String choice){
		print("Received msgHereIsFood");
		event = AgentEvent.gotFood;
		customerGui.gotFood();
		stateChanged();
	}
	
	public void msgHereIsBill(double bill){
		print("Received msgHereIsBill");
		this.bill = bill;
		event = AgentEvent.gotBill;
		stateChanged();
	}

	public void msgHereIsChange(double change){
		print("Received msgHereIsChange");
		this.change = change;
		event = AgentEvent.gotChange;
		stateChanged();
	}
	public void msgAnimationFinishedGoToSeat() {
		//from animation
		event = AgentEvent.seated;
		stateChanged();
	}
	public void msgAnimationFinishedLeaveRestaurant() {
		//from animation
		event = AgentEvent.doneLeaving;
		stateChanged();
	}

	public void msgAnimationFinishedGoToCashier(){
//		System.err.println("Customer finished going to cashier");
		event = AgentEvent.paying;
		stateChanged();
	}
	/** Non Normative scenarios
	 * 
	 */
	public void msgRestaurantFull(){
		print("Received msgRestaurantFull");
		//Using a random decision to decide whether or not to leave
		Random random = new Random();
		int decision = random.nextInt(4);
		
		if (decision % 2 == 0){
			event = AgentEvent.decidedToLeave;
			print("Choosing to leave");
		}
		
		else print("Choosing to wait");
		stateChanged();
	}
	
	public void msgYouCanGo(){
		print("Received msgYouCanGo");
		event = AgentEvent.decidedToLeave;
		print("Allowed to leave now");
		stateChanged();
	}
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		//	CustomerAgent is a finite state machine

		if (state == AgentState.DoingNothing && event == AgentEvent.gotHungry ){
			state = AgentState.WaitingInRestaurant;
			goToRestaurant();
			return true;
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.followWaiter ){
			state = AgentState.BeingSeated;
			SitDown();
			return true;
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.decidedToLeave){
			state = AgentState.Leaving;
			leaveRestaurant();
			return true;
		}
		if (state == AgentState.BeingSeated && event == AgentEvent.seated){
			state = AgentState.Seated;
			decideChoice();
			return true;
		}
		
		if (state == AgentState.Seated && event == AgentEvent.decidedToLeave){
			state = AgentState.Leaving;
			leaveTable();
		}
		if (state == AgentState.Seated && event == AgentEvent.decidedChoice){
			state = AgentState.calledWaiter;
			callWaiter();
			return true;
		}

		if (state == AgentState.calledWaiter && event == AgentEvent.ordering){
			state = AgentState.ordered;
			orderFood();
			return true;
		}
		
		if (state == AgentState.ordered && event == AgentEvent.waitingForFood){
			state = AgentState.WaitingInRestaurant;
			//waitForFood();
			return true;
		}
		
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.needToReorder){
			state = AgentState.Seated;
			decideChoice();
			return true;
		}
		
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.decidedToLeave){
			state = AgentState.Leaving;
			leaveTable();
			return true;
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.gotFood){
			state = AgentState.Eating;
			EatFood();
			return true;
		}
		if (state == AgentState.Eating && event == AgentEvent.doneEating){
			state = AgentState.NeedBill;
			askForBill();
			return true;
		}
		
		if (state == AgentState.NeedBill && event == AgentEvent.gotBill){
			state = AgentState.AtCashier;
			goToCashier();
			return true;
		}
		
		if (state == AgentState.AtCashier && event == AgentEvent.paying){
			state = AgentState.Paying;
			payBill();
			return true;
		}
		
		if (state == AgentState.Paying && event == AgentEvent.waitingForChange){
			state = AgentState.WaitingForChange;
			//no action
			return true;
		}
		
		if (state == AgentState.Paying && event == AgentEvent.decidedToLeave){
			state = AgentState.Leaving;
			leaveTable();
			return true;
		}
		
		if (state == AgentState.WaitingForChange && event == AgentEvent.gotChange){
			state = AgentState.Leaving;
			leaveTable();
			return true;
		}
		
		if (state == AgentState.Leaving && event == AgentEvent.doneLeaving){
			state = AgentState.DoingNothing;
			//no action
			return true;
		}
		return false;
	}

	// Actions

	private void goToRestaurant() {
		Do("Going to restaurant");
//		System.err.println("XPos: " + customerGui.getXPos());
//		System.err.println("YPos: " + customerGui.getYPos());
//		System.err.println("XDestination: " + customerGui.getXDestination());
//		System.err.println("YDestination: " + customerGui.getYDestination());
		host.msgIWantFood(this);//send our instance, so he can respond to us
		//waiter.msgIWantFood(this);
	}

	private void SitDown() {
		Do("Being seated. Going to table");
		customerGui.DoFollowWaiter(waiter.getGui().getXDestination()-20, waiter.getGui().getYDestination()+20);
	}
	
	private void decideChoice() {
		Do("Received menu.  Deciding on choice");
		Random random = new Random();
		int randomChoice;
		
		//for a customer who can afford everything
		if (menu.menuItems.get(0).price < money){
			randomChoice = random.nextInt(menu.menuItems.size());
			choice = menu.menuItems.get(randomChoice).getMenuItem();
			customerGui.setFood(choice);
		
			timer.schedule(new TimerTask() {
				public void run() {
					print("Choice is " + choice);
					msgDecidedChoice();
				}
			},
			3000);
		}
		/*for a customer who can't afford anything
		 *decide whether to leave or IOU
		*/
		else if (menu.menuItems.get(2).price > money){
			randomChoice = random.nextInt(4);
//			System.err.println(randomChoice);
			
			//leave
			if (randomChoice %2 == 1 || event == AgentEvent.needToReorder){
				print("Cannot afford anything.");
				event = AgentEvent.decidedToLeave;
				stateChanged();
			} 
			else if (randomChoice %2 == 0){
				//IOU
				print("Will give IOU to cashier");
				cheapskate = true;
				randomChoice = random.nextInt(menu.menuItems.size());
				choice = menu.menuItems.get(randomChoice).getMenuItem();
				customerGui.setFood(choice);
			
				timer.schedule(new TimerTask() {
					public void run() {
						print("Choice is " + choice);
						msgDecidedChoice();
					}
				},
				3000);
			}
		}
	}
	
	private void callWaiter() {
		Do("Decided on choice.  Calling waiter");
		waiter.msgReadyToOrder(this);
		customerGui.DoCallWaiter();
	}
	
	private void orderFood() {
		Do("Waiter arrived.  Ordering food");
		waiter.msgHereIsMyChoice(choice, this);
		customerGui.waitingForFood();
		event = AgentEvent.waitingForFood;
	}

	private void EatFood() {
		Do("Eating Food");
		timer.schedule(new TimerTask() {
			public void run() {
				print("Done eating my " + choice);
				event = AgentEvent.doneEating;
				//isHungry = false;
				stateChanged();
			}
		},
		5000);//getHungerLevel() * 1000);//how long to wait before running task
	}

	private void askForBill(){
		waiter.msgBillPlease(this);
		customerGui.waitingForBill();
		event = AgentEvent.waitingForBill;
		stateChanged();
	}
	
	private void goToCashier(){
		Do("Going to cashier.");
		customerGui.DoGoToCashier();
		stateChanged();
	}
	
	private void payBill(){
		if (bill <= money){
			cashier.msgHereIsPayment(this, money);
			event = AgentEvent.waitingForChange;
		}
		else if (bill > money)
			cashier.msgCantPay(this);
		
	}
	private void leaveTable() {
		Do("Leaving.");
		waiter.msgDoneAndLeaving(this);
		customerGui.DoExitRestaurant();
	}
	
	private void leaveRestaurant(){
		Do("Leaving Restaurant.");
		customerGui.DoExitRestaurant();
		host.msgLeavingRestaurant(this);
	}

	// Accessors, etc.

	public String getName() {
		return name;
	}
	
	public int getHungerLevel() {
		return hungerLevel;
	}

	public void setHungerLevel(int hungerLevel) {
		this.hungerLevel = hungerLevel;
		//could be a state change. Maybe you don't
		//need to eat until hunger lever is > 5?
	}


	public void gotHungry() {//from animation
		print("I'm hungry");
		event = AgentEvent.gotHungry;
		stateChanged();
	}
	
	public void gotPoor() {
		print("I'm poor");
		money = 5;
		stateChanged();
	}
	
	public void gotRich(){
		print("I'm rich!");
		money = 50;
		stateChanged();
	}
	
	public String toString() {
		return "customer " + getName();
	}

	public void setGui(CustomerGui g) {
		customerGui = g;
	}

	public CustomerGui getGui() {
		return customerGui;
	}
	public void setMoney(int money){
		this.money = money;
	}
	
	public double getBill(){
		return bill;
	}
	
	public double getChange(){
		return change;
	}
	
	@Override
	public void setCheapskate(boolean cheapskate){
		this.cheapskate = cheapskate;
	}
	
	public boolean isCheapskate(){
		return cheapskate;
	}

	@Override
	public double getMoney() {
		return money;
	}

}

