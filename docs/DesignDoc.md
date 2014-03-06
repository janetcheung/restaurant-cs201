# Design Document

### Host Agent
####Host Data
* `List <CustomerAgent> waitingCustomers;`
* `Collection <Table> tables;`
* `Collection <MyWaiter> waiters;`
* `class MyWaiter { WaiterAgent waiter, waiterState state;}`
* `enum waiterState {working, requestingBreak, onBreak;}`

####Host Messages
* **tableFree(Table table)** {

	t.isOccupied = false;
	}
* **IWantFood(CustomerAgent c)** {

	waitingCustomers.add(c);
	}

* **LeavingTable(CustomerAgent c)**{

	if there exists a table in tables s.t. table.occupant == c {
	
		table.setUnoccupied();}
	
	}
* **RequestingBreak(WaiterAgent w)** {
	
	if there exists mw in waiters s.t. mw.waiter == w {
	
		mw.state = requestingBreak;}
	
	}
* **ImBack(WaiterAgent w)** {

	if there exists mw in waiters s.t. mw.waiter == w {
	
		mw.state = working;}
		
	}

####Host Scheduler
* if there exists mw in waiters s.t. mw.state ==requestingBreak {

		signOffBreak(mw);}
		
	}
	
* if waitingCustomers.size > tables.size {

		notifyCustomer(waitingCustomers.get(waitingCustomers.size() - 1));}
		
	}
	
* if (!waitingCustomers.isEmpty()) {

		if(!waiters.isEmpty()){
		
			while (waiters.get(nextWaiter).state == onBreak)
			
				chooseNextWaiter();
			
			if there exists a table in tables {
				
				if (!table.isOccupied()){
				
					seatCustomer(waiters.get(nextWaiter), table);
				}
			
			}
	
		}
	
	}

####Host Actions
* **seatCustomer(MyWaiter mw, ustomerAgent c, Table table)** {
	
	table.setOccupant(c);

	mw.seatCustomer(c, table);

	waitingCustomers.remove(c); }
	
* **chooseNextWaiter()**{
	
		nextWaiter = ++nextWaiter % waiters.size();
	}

* **signOffBreak(MyWaiter mw)**{

		if (waiters.size() <= 1) {
		
			mw.waiter.msgNoBreak();
			
			mw.state = working;}
		
		else {
		
			mw.waiter.msgYesBreak();
			
			mw.state = onBreak;}

* **notifyCustomer(CustomerAgent c){

		c.msgRestaurantFull();
	}
	
### Customer Agent
####Customer Data
* `String name;`
* `int hungerLevel;`
* `String choice; `
* `Timer timer;`
* `Menu menu;`
* `WaiterAgent waiter;`
* `HostAgent host;`
* `Menu menu;`
* `double bill;`
* `double money;`
* `enum AgentState { DoingNothing, WaitingInRestaurant, BeingSeated, Seated, 
					CalledWaiter, Ordered, Eating, DoneEating, NeedBill, AtCashier,
					Paying, WaitingForChange, Leaving}`
* `enum AgentEvent { none, gotHungry, followWaiter, seated, decidedToLeave, decidedChoice, 
					ordering, waitingForFood, needToReorder, gotFood, doneEating, 
					waitingForBill, gotBill, paying, waitingForChange, gotChange, doneLeaving}`

####Customer Messages
* **FollowWaiter(WaiterAgent, Menu menu)** {

	this.waiter = waiter;

	this.menu = menu;
	
	event = followWaiter;}
* **DecidedChoice()** {
	
	event = decidedChoice;}
* **whatDoYouWant()** {

	state = ordering;}
* **PleaseReorder(Menu menu)**{
	
		this.menu = menu;
		
		if (decision % randomInt == 0 )
			
				event = decidedToLeave;
				
		else event = needToReorder;}
* **HereIsFood(String choice)** {

		event = gotFood;
		
		customerGui.gotFood();}
* **HereIsBill(double bill)**{

		this.bill = bill;
		
		event = gotBill;}
* **HereIsChange(double change)** {

		event = gotChange;}
* **AnimationFinishedGoToSeat()**{

		event = seated;}
* **AnimationFinishedLeaveRestaurant()**{

		event = doneLeaving;}
* **AnimationFinishedGoToCashier()**{

		event = paying;}

* **RestaurantFull()**{
		
		if (decision % randomInt == 0){
		
			event = decidedToLeave;}
			
	}
* **YouCanGo()**{

	event = decidedToLeave;}
####Customer Scheduler
* if (state == DoingNothing && event == gotHungry){

		then {	
		
			state = WaitingInRestaurant;
			
			goToRestaurant();
			}
		
		}
* if (state == WaitingInRestaurant && event == followWaiter){
		
		then {
			
			state = BeingSeated;
			
			sitDown();
			
			}
		
		}
* if (state == WaitingInRestaurant && event == decidedToLeave){

		then {
		
			state = Leaving;
			
			leaveRestaurant();
			
			}
		
		}
* if (state == BeingSeated && event == seated) {
		
		then {
			
			state = Seated;
			
			decideChoice();
			
			}
		
		}
* if (state == Seated && event == decidedToLeave) {

		then {
		
			state = Leaving;
			
			leaveTable();
		
			}
		
		}
* if (state == Seated && event == decidedChoice) {
		
		then {
		
			state = calledWaiter;
			
			callWaiter();
			
			}
		
		}
* if (state == calledWaiter && event == ordering){
		
		then {
			
			state = ordered;
			
			orderFood();
			
			}
		
		}
* if (state == ordered && event == waitingForFood){

		then {
		
			state = WaitingInRestaurant;
			
			}
			
		}
* if (state == WaitingInRestaurant && event == decidedToLeave){

		then {
		
			state = Leaving;
			
			leaveTable();
			
			}
		
		}
* if (state == WaitingInRestaurant && event == gotFood){
		
		then {
			
			state = Eating;
			
			eatFood();
			
			}
		
		}
* if (state == Eating && event == doneEating) {
		
		then {
			
			state = NeedBill;
			
			askForBill();
			
			}
* if (state == NeedBill && event == gotBill) {

		then { 
			
			state = atCashier;
			
			goToCashier();
			
			}
			
		}
* if (state == atCashier && event == paying) {

		then {
		
			state = Paying;
			
			payBill();
			
			}
		
		}
* if (state == Paying && event == waitingForChange) {

		then {
		
			do nothing
			
			}
		
		}
* if (state == Paying && event == decidedToLeave) {

		then {
		
			state = Leaving;
			
			leaveTable();
			
			}
			
		}
* if (state == WaitingForChange && event == gotChange) {

		then {
		
			state = Leaving;
			
			leaveTable();
			
			}
			
		}
* if (state == Leaving && event == doneLeaving)}
		
		then {
			
			state = DoingNothing;
			
			}

####Actions
* **goToRestaurant()**{

	host.msgWantFood(this); }
* **sitDown()**{

	custGui.DoFollowWaiter(waiterGui.xPosition, waiterGui.yPosition);}
* **decideChoice()**{
	
	if (mostExpensiveItem < money){
	
		randomChoice = random.nextInt(menu.menuItems.size);
		
		choice = menu.menuItems.get(randomChoice).getMenuItem();
		
		customerGui.setFood(choice);
		
		msgDecidedChoice();
	}
	
	else if (cheapestItem > money){
	
		randomChoice = random.nextInt();
		
		if (randomChoice %2 == 1 || event == needToReorder){
		
			event = decidedToLeave;
		}
		
		else if (randomChoice %2 == 0 ) {
		
			randomChoice = random.nextInt(menu.menuItems.size);
		
			choice = menu.menuItems.get(randomChoice).getMenuItem();
			
			customerGui.setFood(choice);
		
			msgDecidedChoice();
		}
	
	}
	
}
* **callWaiter()**{ 

	waiter.msgReadyToOrder(this); 

	custGui.DoCallWaiter();}
* **orderFood()** {

	waiter.msgHereIsMyChoice(choice, this);
	
	custGui.waitingForFood();
	
	event = waitingForFood;}
* **eatFood()** {
	
	DoEatFood();
	
	event = doneEating;}
* **askForBill()**{

	waiter.msgBillPlease(this);
	
	customerGui.waitingForBill();
	
	event = waitingForBill;}
* **goToCashier()** {

	customerGui.DoGoToCashier();}
* **payBill()**{

	if (bill <= money){
	
		cashier.msgHereIsPayment(this, money);
		
		event = waitingForChange;}
		
	else if (bill > money){
	
		cashier.msgCantPay(this);}
		
	}
* **leaveRestaurant(){

	customerGui.DoExitRestuarant();
	
	host.msgLeavingRestaurant(this);}
* **leaveTable()** {
	
	waiter.msgDoneAndLeaving(this);
	
	custGui.DoExitRestaurant();}
* **gotHungry()** {

		event = gotHungry;}
* **gotPoor()**{
		
		money = 5;}
* **gotRich()**{

		money = 50;}
### Cook Agent
####Cook Data
* `List <Order> orders;`
* `class Order { WaiterAgent w; String choice; int tableNum; state s;}`
* `List <Food> reorders;`
* `class Food { String name, foodState state, List<String> availableMarkets, int quantity,
				int cookingTime, int lowQuantity, int reorderQuantity}`
* `List <MarketAgent> markets;`
* `HashMap<String, Food> inventory;`
* `enum orderState {pending, cooking, done, finished}`
* `enum foodState {normal, low, out, waitingForDelivery, waitingForRedelivery}`
* `Timer timer;`
* `HashMap<String,int> CookingTimes();`

####Cook Messages
* **ReceivedOrder(WaiterAgent w, String choice, int tableNum)** {
	
		orders.add(new order(w,choice,tableNum));}
* **OrderDone(Order o)**{
		
		o.state = done;}
		
* **RestockedFood(String choice, int n)**{

		if there exists f in reorders s.t. f.name == choice {
		
			f.state = normal;}
		
		inventory.get(choice).quantity = quantity + n;
		
	}
* **DontHaveFood(String choice, MarketAgent m, int n)**{

		if there exists f in reorders s.t. f.name == choice {
		
			if (f.lowQuantity < f.quantity + n) {
				
				f.state = normal;}
				
			else {
			
				f.reorderQuantity = n - reorderQuantity;
				
				f.availableMarkets.remove(m.getName);
				
				reorders.remove(f);
				
				f.state = waitingForRedelivery;}
				
		}
		
	}
	
* **DepleteOptions()** {

		for all s in inventory {
		
			inventory.get(s).quantity = 0;
			
			inventory.get(s).state = out;}
			
	}
####Cook Scheduler
* if there exists String s in inventory s.t. inventory.s.state == out) {

	reorderFood(inventory.get(s));}
	
* if there exists String s in inventory s.t. inventory.s.state == low) {

	reorderFood(inventory.get(s));}
	
* if there exists o in orders such that order.state == done{
		
		then plateIt(o);}
		
* if there exists o in orders such that order.state = pending{

		then cookIt(o);}
		
* if there exists f in reorders s.t. f.state == normal) {

		removeFoodFromReorders(f);}

* if there exists f in reorders s.t. f.state == waitingForDelivery) {

		do nothing}

* if there exists f in reorders s.t. f.state == waitingForRedelivery) {

		chooseAnotherMarket(f);}
		
####Cook Actions
* **cookIt(Order o)**{
	
	if (inventory.get(o.choice).quantity == 0){
	
		o.waiter.msgOutofOrder(o.choice, o.tablenumber);
		
		o.state = outOfOrder;
		
		orders.remove(o);}
		
	else{
		cookGui.DoCooking(o);
		
		timer.start();
		
		run(){}, cookingTimes.get(o.choice));
		
		inventory.get(o.choice).quantity--;
		
		o.state = cooking;}
		
	if (inventory.get(o.choice).quantity <= inventory.get(o.choice).lowQuantity){
	
		if (inventory.get(o.choice).state != waitingForDelivery || waitingForRedlivery) {
		
			inventory.get(o.choice).state = low;
			
		}

	}
	
}
* **plateIt(Order o)**{

		o.w.msgOrderDone(o.choice, o.tableNumber);
		
		cookGui.DoPlating(o);
		
		orders.remove(o);}
		
* **reorderFood(Food f)** {

		chooseRandomMarket();
		
		reorders.add(new Food (...));
		
		markets.get(randomMarket).msgReceivedOrder(reorders);
		
		f.state = waitingForDelivery;
		
	}
* **chooseAnotherMarket(Food f)** {

		chooseRandomMarket(f.availableMarkets.size);
		
		if there exists s in f.availableMarkets s.t. s == markets.get(randomMarket).getName()) {
		
			reorders.add(new Food (...));
			
			markets.get(randomMarket).msgReceivedOrder(reorders);
			
			f.state = waitingForDelivery;}
			
	}
* **removeFoodFromReorders(Food f)** {

	reorders.remove(f);}
###Waiter Agent
####Waiter Data
* `class myCustomer {CustomerAgent c, int table, String choice; customerState s;}`
* `List <myCustomer> customers;`
* `Collection<Table> tables;`
* `CookAgent cook;`
* `HostAgent host;`
* `CashierAgent cashier;`
* `class Order { myCustomer c; orderState s; }`
* `Menu menu;`
* `Semaphore atTable = new Semaphore(0,true);`
* `Semaphore atFront = new Semaphore(0, true);`
* `Semaphore atCook = new Semaphore(0,true);`
* `Semaphore atCashier = new Semaphore(0, true);`
* `Semaphore atStandby = new Semaphore(0, true);`
* `boolean allowedBreak = false;`
* `boolean onBreak = false;`
* `enum customerState {waitingForSeat, beingSeated, seated, readyToOrder, ordering, waitingForFood,
					orderSent, orderReady, eating, readyForBill, waitingForBill, billReady, paying, waitingForChange, 
					receivedChange, No_Action, done}`
* `enum orderState {pending, cooking, done, delivered}`

####Waiter Messages
* **SeatCustomer(CustomerAgent customer, int tableNum)**{
	
	customers.add(new myCustomer(c, table waiting));
	
	c.setWaiter(this);
	}
* **ReadyToOrder(CustomerAgent customer)**{
	
	if there exists a c in customers s.t. c == customer 
	
		then c.s = readyToOrder;
		}
		
* **HereIsMyChoice(String choice, CustomerAgent customer)**{
	
	atTable.release();
	
	if there exists a c in customer s.t. c == customer 
		
		then c.choice = choice;
		
			c.order = new Order(choice);
			
			c.s = waitingForFood;
		
		}
		
* **OrderDone(String choice, int table)**{
	
	if there exists a c in customers s.t. mc.choice == choice {
		
		if (c.order.s == pending) {
		
				c.order.s = done;
				
				c.s = orderReady;
		
		}
	}
* **BillPlease(CustomerAgent customer)**{

	if there exists a c in customers s.t. mc.c == customer {
	
		mc.state == readyForBill;
		
	}

	}
	
* **BillReady(Bill b)**{

	if there exists an mc in customers s.t. mc.table == b.table){
	
			mc.bill = b;
			
			mc.state = billReady;
			
		}
	
	}
* **DoneAndLeaving(CustomerAgent customer)** {
	
	if there exists a c in customers s.t. mc.c == customer 
	
		then c.s = done;
	
	}
	
* **LeavingTable(CustomerAgent customer)** {

	if there exists a t in tables s.t. t.occupant == customer {
	
		t.setUnoccupied();
	
	}
	
	if there exists an mc in customers s.t. mc. c == customer {
	
		mc.state == done;

	}
	
}

* **YesBreak()** {

		allowedBreak = true;
		
		waiterGui.breakAllowed();
	
	}

* **NoBreak()** {

		allowedBreak = false;
		
		waiterGui.breakNotAllowed();
		
	}
	
* **
	
* **AtFront()** {

		atFront.release();
		
	}
		
* **AtTable()**{
		
		atTable.release();
	
	}
	
* **AtCook()**{

		atCook.release();
	
	}
		
####Waiter Scheduler

* if (onBreak) 

	returnFromBreak();
* if (myCustomers.isEmpty() && allowedBreak)
	
	goOnBreak();
	
* if there exists a c in customer s.t. c.s == waitingForSeat {
		
		then comeGetCustomer(c, c.table)}
* if there exists a c in customer s.t. c.s == beingSeated {

		then seatCustomer(c,c.table))}
* if there exists a c in customer s.t. c.s == readyToOrder {

		then takeOrder(c)}
* if there exists a c in customer s.t. c.s == ordering {

		then nothing happens}
* if there exists a c in customer s.t. c.s == waitingForFood {

		then giveOrderToCook(c)}
* if there exists a c in customer s.t. c.s == orderSent {

		then nothing happens }
* if there exists a c in customer s.t. c.s == orderReady {

		then deliverOrder(c)}
* if there exists a c in customer s.t. c.s == eating {

		then leaveTable(c)}
* if there exists a c in customer s.t. c.s == done {

		then notifyHost(c)}
* if there exists a c in customer s.t. c.s == billReady {

		then deliverBill(c);}
* if there exists a c in customer s.t. c.s == seated && ! c.leftTable {

		then leaveTable(c);}
* if there exists a c in customer s.t. c.s == readyForBill {

		then getBill(c);}
* if there exists a c in customer s.t. c.s == waitingForBill {

		then do nothing }
* if there exists a c in customer s.t. c.s == paying {

		notifyHost(c);}
* if customer.isEmpty() {
		
		returnToFront();}
####Waiter Actions 
* **requestBreak()**{

	host.msgRequestingBreak(this);}
	
* **goOnBreak()**{

	waiterGui.DoGoToFront();
	
	waiterGui.goOnBreak();
	
	onBreak = true;}
* **returnFromBreak()**{

	onBreak = false;
	
	allowedBreak = false;
	
	waiterGui.returnFromBreak();
	
	notifyHostBack();}
* **notifyHostBack(){

	waiterGui.DoLeaveCustomer();
	
	host.msgImBack(this);}
* **comeGetCustomer(MyCustomer customer, int table)** {
		
		waiterGui.DoGoToFront();
		
		customer.state = beingSeated; }
* **seatCustomer(MyCustomer customer, int table)** {

		DoSeatCustomer(customer.c, table);
		
		customer.c.msgFollowWaiter(this, menu);
		
		customer.state = seated;
		
		}
* **takeOrder(MyCustomer customer)** {

		DoGoTakeOrder(customer);
		
		customer.c.msgWhatDoYouWant();
		
		customer.state = No_Action;
		
		}
* **giveOrderToCook(MyCustomer customer)** {
		
		DoGiveOrderToCook();
		
		cook.msgReceivedOrder(this, customer.choice, customer.table);
		
		customer.state = No_Action;
		
		}
* ** askToReorder(MyCustomer mc)** {

		waiterGui.DoGoToTable(mc.table);
		
		menu.menuItems.remove(mc.choice);
		
		mc.c.msgPleaseReorder(menu);
		
		mc.state = seated;}
* **deliverOrder(MyCustomer customer)** {

		DoDeliverOrder(customer);
		
		customer.c.msgHereIsFood(customer.choice);
		
		customer.state = eating;
		
		}
* **leaveTable(MyCustomer customer)** {

		DoLeaveTable();
		
		}
* ** getBill(MyCustomer mc)** {

		DoGetBill();
		
		cashier.msgReceivedBillRequest(this, mc.c, mc.choice, mc.table);
		
		mc.state = No_Action;
		
		}
* **deliverBill(MyCustomer mc)** {

		waiterGui.DoGoToTable(mc.table);
		
		mc.c.msHereIsBill(mc.bill.total);
		
		mc.state = paying;
		
		}
* **notifyHost(MyCustomer customer)** {
	
		host.msgLeavingTable(customer.c);
		
		customers.remove(customer);
		
		}

* **returnToFront()** {

		waiterGui.DoGoToFront();}
		
* **tellCookDepleteOptions()**{

		cook.msgDepleteOptions();}
### Market Agent
####Market Data
* `ArrayList<Order> orders;`
* `HashMap<String, Food> inventory;`
* `Timer timer;`
* `class Food { String name, int quantity, int shippingTime, int lowQuantity, int reorderQuantity}`
* `class Order { String choice, int reorderQuantity, orderState state }`
* `enum orderState{pending, packaging, done, OutOfOrder}`
* `enum foodState{normal, low, waitingForDelivery, waitingForRedelivery}`

####Market Messages
* **ReceivedOrder(Food list)**{

		add a new order for each food in the list}
* **OrderDone(Order o)**{

		o.state = done}
* **RestockedFood(Food f)**{

		f.state = normal}

####Market Scheduler
* if there exists an o in orders s.t. o.state == done {

		shipIt(o);}
* if there exists an o in orders s.t. o.state == pending {

		packageIt(o);}

####Market Actions
* **packageIt(Order o)** {
	
		if (inventory.get(o.choice).quantity == 0){
		
			cook.msgDontHaveFood(o.choice);
			
			o.state = outOfOrder;
			
			orders.remove(o);
			
		}
		
		else if (inventory.get(o.choice).quantity < reorderQuantity){
		
			inventory.get(o.choice).state = low;
			
			DoPackaging(o);
			
		}
		
		else {
		
			o.state = packaging;
			
			inventory.get(o.choice).quantity = quantity - reorderQuantity;
			
			DoPackaging(o);
			
		}
		
* **shipIt(Order o)** {

		DoShipping(o);
		
		orders.remove(o);
	
	}
	
###Cashier Agent
####Cashier Data
* `List<Bill> bills`
* `List<OwingCustomer> owingCustomers`
* `HashMap<String, Double> prices`
* `Timer timer`
* `class Bill {WaiterAgent waiter, CustomerAgent customer, int table, String choice, 
				int table, double total, double payment, billState state}
* `class OwingCustomer {CustomerAgent customer, double total}
* `enum billState {pending, ready, needPayment, needChange, madeIOU}`

####Cashier Messages
* **ReceivedBillRequest(WaiterAgent waiter, CustomerAgent customer, String choice, int table)**{

		bills.add(new Bill(...))
	}
* **HereIsPayment(CustomerAgent customer,double money)**{

		if there exists a b in bills s.t. b.customer == customer {
			
			b.payment = money;
			b.state = needChange;
		
		}
	
	}
* **CantPay(CustomerAgent customer)**{

		owingCustomers.add(new OwingCustomer(...));
		
		if there exists a b in bills s.t. b.customer == customer {
		
			b.state = madeIOU;
			
		}

	}

####Cashier Scheduler
* if there exists a b in bills s.t. b.state == pending {

		updateTotal(b);}
* if there exists a b in bills s.t. b.state == ready {
		
		giveBill(b);}
* if there exists a b in bills s.t. b.state == needPayment {

		do nothing until bill has been fulfilled by customer}
* if there exists a b in bills s.t. b.state == needChange {

		giveChange(b);}

####Cashier Actions
* **updateTotal(Bill bill)**{

		bill.total = prices.get(bill.choice);
		
		if there exists an o in owingCustomers s.t. o.customer equals bill.customer{
		
			bill.total = o.total + bill.total;
			
			owingCustomers.remove(o);}
			
		bill.state = ready;}
* **giveBill(Bill bill)**{

		bill.waiter.msgBillReady(bill);
		
		bill.state = needPayment;}
* **giveChange(Bill bill)** {

		change = bill.total - bill.payment;
		
		bill.customer.msgHereIsChange(change);
		
		bills.remove(bill);}
* **dealtWithBill(Bill bill)** {

		bill.customer.msgYouCanGo();
		
		bills.remove(bill);}
