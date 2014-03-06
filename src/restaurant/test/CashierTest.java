package restaurant.test;

import restaurant.CashierAgent;
import restaurant.CashierAgent.Bill;
import restaurant.CashierAgent.billState;
import restaurant.test.mock.LoggedEvent;
import restaurant.test.mock.MockCustomer;
import restaurant.test.mock.MockMarket;
import restaurant.test.mock.MockWaiter;

import junit.framework.*;

/**
 * 
 * This class is a JUnit test class to unit test the CashierAgent's basic interaction
 * with waiters, customers, and the host.
 * It is provided as an example to students in CS201 for their unit testing lab.
 *
 * @author Monroe Ekilah
 */
public class CashierTest extends TestCase
{
	//these are instantiated for each test separately via the setUp() method.
	CashierAgent cashier;
	MockWaiter waiter;
	MockWaiter waiter2;
	MockCustomer customer;
	MockCustomer customer2;
	MockMarket market1;
	MockMarket market2;
	
	String choice;
	
	
	/**
	 * This method is run before each test. You can use it to instantiate the class variables
	 * for your agent and mocks, etc.
	 */
	public void setUp() throws Exception{
		super.setUp();		
		cashier = new CashierAgent("cashier");		
		customer = new MockCustomer("mockcustomer");
		waiter = new MockWaiter("mockwaiter");
		market1 = new MockMarket("mockmarket1");
		market2 = new MockMarket("mockmarket2");
	}	
	/**
	 * Test 1: Waiter, Customer, Cashier
	 */
	public void testOneNormalCustomerScenario()
	{
		//setUp() runs first before this test!
		System.out.println("--Begin Test 1--");

		customer.cashier = cashier;	
		
		//check preconditions
		assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
				+ waiter.log.toString(), 0, waiter.log.size());

		assertEquals("MockCustomer should have an empty event log before the Cashier's scheduler is called. Instead, the MockCustomer's event log reads: "
				+ customer.log.toString(), 0, customer.log.size());

		assertEquals("Cashier should have 0 bills in it. It doesn't.",cashier.getBills().size(), 0);		
		
		//step 1 of the test
		cashier.msgReceivedBillRequest(waiter, customer, "Steak", 3);//send the message from a waiter
		Bill bill = cashier.getBills().get(0);
		
		//check postconditions for step 1 and preconditions for step 2
		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getBills().size(), 1);
		
		assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be paid for.", cashier.pickAndExecuteAnAction());
		
		assertEquals(
				"MockWaiter should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
		assertEquals(
				"MockCustomer should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockCustomer's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
		assertTrue("CashierBill should contain a bill of price = $15.99. It contains something else instead: $" 
				+ cashier.getBills().get(0).getTotal(), cashier.getBills().get(0).getTotal() == 15.99);
		
		assertTrue("CashierBill should contain a bill with the right customer in it. It doesn't.", 
					cashier.getBills().get(0).getCustomer() == customer);
		
		//step 2 of the test
		waiter.msgBillReady(bill);
		
		//check postconditions for step 2 / preconditions for step 3
		
		assertTrue("MockCustomer should have logged an event for receiving \"HereIsYourBill\" with the correct balance, but his last event logged reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received HereIsYourBill from waiter. Total = 15.99"));
		
		assertTrue("CashierBill should contain a bill with state == needChange. It doesn't.",
				cashier.getBills().get(0).getState() == billState.needChange);

		
		//step 3
		//NOTE: I called the scheduler in the assertTrue statement below (to succinctly check the return value at the same time)
//		customer.msgHereIsBill(bill.getTotal());

		assertTrue("Cashier's scheduler should have returned true , but didn't.", 
					cashier.pickAndExecuteAnAction());
		
		//check postconditions for step 3 / preconditions for step 4
		assertTrue("CashierBill should contain a bill with state == done. It doesn't.",
				cashier.getBills().get(0).getState() == billState.done);

		assertTrue("CashierBill should contain changeDue == 4.01. It contains something else instead: $" 
				+ cashier.getBills().get(0).getChange(), cashier.getBills().get(0).getChange() == 4.01);
		
		assertTrue("MockCustomer should have logged an event for receiving \"HereIsYourChange\" with the correct change, but his last event logged reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received HereIsYourChange from cashier. Change = 4.01"));

		//step 4
		assertTrue("Cashier's scheduler should have returned true (needs to react to remove bill), but didn't.", 
					cashier.pickAndExecuteAnAction());

		//check postconditions for step 4		
		assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
		
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
		
		System.out.println("--Concluded Test 1--");
	
	}//end one normal customer scenario
	
	/**
	 * Test 2: 1 market
	 */
	public void testTwoNormalMarketScenario(){
		System.out.println("--Begin Test 2--");
	//check preconditions
			assertEquals("Cashier should have 0 bills in it. It doesn't.",cashier.getMarketBills().size(), 0);		
			assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket1's event log reads: "
							+ market1.log.toString(), 0, market1.log.size());
			
			//step 1 of the test
			cashier.msgReceivedMarketBill(market1, "Steak", 5*5.00); //send this message from market1
			Bill marketBill = cashier.getMarketBills().get(0);
			
			//check postconditions for step 1 and preconditions for step 2
			assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMaket1's event log reads: "
							+ market1.log.toString(), 0, market1.log.size());
			
			assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getMarketBills().size(), 1);

			assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
					cashier.getMarketBills().get(0).getState() == billState.needPayment);
			
			assertTrue("MarketBill should contain a bill of price = $25.00. It contains something else instead: $" 
					+ cashier.getMarketBills().get(0).getTotal(), cashier.getMarketBills().get(0).getTotal() == 25.00);
						
			//step 2 of the test	
			assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be paid for.", cashier.pickAndExecuteAnAction());

			//check postconditions for step 2 / preconditions for step 3
			
			assertTrue("Cashier should have money = $975.00.  It contains something else: $"
					+ cashier.getMoney(), cashier.getMoney() == 975.00);
			
			assertTrue("MockMarket should have logged an event for receiving \"HereIsPayment\" with the correct change, but his last event logged reads instead: " 
					+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received HereIsPayment from cashier. Total = 25.0"));

			assertTrue("MarketBill should contain a bill with state == done. It doesn't.",
					cashier.getMarketBills().get(0).getState() == billState.done);
			
			//step 3
			//NOTE: I called the scheduler in the assertTrue statement below (to succintly check the return value at the same time)
			
			assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be removed.", cashier.pickAndExecuteAnAction());

			assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
			
			//step 4
			assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
					cashier.pickAndExecuteAnAction());
			
			System.out.println("--Concluded Test 2--");
		
		}//end one normal market scenario
	/**
	 * Test 3: 2 markets
	 */
	public void testThreeNonNormalMarketScenario(){
		
		System.out.println("--Begin Test 3--");
		//check preconditions
		assertEquals("Cashier should have 0 bills in it. It doesn't.",cashier.getMarketBills().size(), 0);		
		
		assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket1's event log reads: "
						+ market1.log.toString(), 0, market1.log.size());
		
		assertEquals("MockMarket2 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket2's event log reads: "
				+ market2.log.toString(), 0, market2.log.size());

		//step 1
		cashier.msgPartialOrderBill(market1, "Steak", 3*5.00); //send this message from market1
		cashier.msgPartialOrderBill(market2, "Steak", 2*5.00); //send this message from market2
		
		Bill marketBill1 = cashier.getMarketBills().get(0);
		Bill marketBill2 = cashier.getMarketBills().get(1);
		
		//check postconditions for step 1 and preconditions for step 2
		
		assertEquals("Cashier should have 2 bills in it. It doesn't.", cashier.getMarketBills().size(), 2);
		
		assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket1's event log reads: "
				+ market1.log.toString(), 0, market1.log.size());

		assertEquals("MockMarket2 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket2's event log reads: "
				+ market2.log.toString(), 0, market2.log.size());

		assertTrue("MarketBill1 should contain a bill with state == needPayment. It doesn't.",
				cashier.getMarketBills().get(0).getState() == billState.needPayment);
		
		assertTrue("MarketBill2 should contain a bill with state == needPayment. It doesn't.", 
				cashier.getMarketBills().get(1).getState() == billState.needPayment);
		
		assertTrue("MarketBill1 should contain a bill of price = $15.00. It contains something else instead: $" 
				+ cashier.getMarketBills().get(0).getTotal(), cashier.getMarketBills().get(0).getTotal() == 15.00);
		
		assertTrue("MarketBill1 should contain a bill of price = $10.00. It contains something else instead: $" 
				+ cashier.getMarketBills().get(1).getTotal(), cashier.getMarketBills().get(1).getTotal() == 10.00);
				
		//step 2 of the test
		assertTrue("Cashier's scheduler should have returned true.  The bills are ready to be paid for.", cashier.pickAndExecuteAnAction());

		//check postconditions for step 2 / preconditions for step 3
		assertEquals("Cashier should have 2 bills in it. It doesn't.", cashier.getMarketBills().size(), 2);

		assertTrue("MarketBill1 should contain a bill with state == done. It doesn't.",
				cashier.getMarketBills().get(0).getState() == billState.done);
		
		assertTrue("MarketBill2 should contain a bill with state == needPayment.  It doesn't.",
				cashier.getMarketBills().get(1).getState() == billState.needPayment);

		assertTrue("Cashier should have money = $985.00.  It contains something else: $"
				+ cashier.getMoney(), cashier.getMoney() == 985.00);
				
		assertTrue("MockMarket should have logged an event for receiving \"HereIsPayment\" with the correct change, but his last event logged reads instead: " 
				+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received HereIsPayment from cashier. Total = 15.0"));

		//step 3 of the test
		assertTrue("Cashier's scheduler should have returned true.  Bill2 is ready to be paid for.", cashier.pickAndExecuteAnAction());

		//step 3 postconditions
		assertEquals("Cashier should have 2 bills in it. It doesn't.", cashier.getMarketBills().size(), 2);
		
		assertTrue("MarketBill1 should contain a bill with state == done. It doesn't.",
				cashier.getMarketBills().get(0).getState() == billState.done);
		
		assertTrue("MarketBill2 should contain a bill with state == done.  It doesn't.",
				cashier.getMarketBills().get(1).getState() == billState.done);	

		assertTrue("Cashier should have money = $975.00.  It contains something else: $"
				+ cashier.getMoney(), cashier.getMoney() == 975.00);
				
		assertTrue("MockMarket should have logged an event for receiving \"HereIsPayment\" with the correct change, but his last event logged reads instead: " 
				+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received HereIsPayment from cashier. Total = 15.0"));

		assertTrue("MockMarket should have logged an event for receiving \"HereIsPayment\" with the correct change, but his last event logged reads instead: " 
				+ market2.log.getLastLoggedEvent().toString(), market2.log.containsString("Received HereIsPayment from cashier. Total = 10.0"));

		//step 4 of the test
		assertTrue("Cashier's scheduler should have returned true.  The bills are ready to be removed.", cashier.pickAndExecuteAnAction());
		//Step 4 post conditions
		assertEquals("Cashier should have removed the bills. It didn't.", cashier.getBills().size(), 0);
				
		//step 5 of the test
		assertTrue("Cashier's scheduler should have returned true since it ran through the scheduler, but didn't.", 
				cashier.pickAndExecuteAnAction());
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
				
		System.out.println("--Concluded Test 3--");
		
	}
	/**
	 * Test 4: 1 customer, 1 waiter, 1 market
	 */
	public void testFourNormalScenario(){
		customer.cashier = cashier;	
		
		System.out.println("--Begin Test 4--");
		//check preconditions
				assertEquals("Cashier should have 0 bills in it. It doesn't.",cashier.getBills().size(), 0);
				assertEquals("Cashier should have 0 marketBills in it.  It doesn't.", cashier.getMarketBills().size(), 0);
				assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
				assertEquals("MockCustomer should have an empty event log before the Cashier's scheduler is called. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
				
				assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket1's event log reads: "
						+ market1.log.toString(), 0, market1.log.size());
		
				//step 1 of the test
				cashier.msgReceivedBillRequest(waiter, customer, "Steak", 3);//send the message from a waiter
				Bill bill = cashier.getBills().get(0);
				
				cashier.msgReceivedMarketBill(market1, "Steak", 5*5.00); //send this message from market1
				Bill marketBill = cashier.getMarketBills().get(0);
				
				//check postconditions for step 1 and preconditions for step 2
				assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
								+ waiter.log.toString(), 0, waiter.log.size());
				
				assertEquals("MockCustomer should have an empty event log before the Cashier's scheduler is called.  Instead, the MockCustomer's event log reads: " 
								+ customer.log.toString(), 0, customer.log.size());
				
				assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called.  Instead, the MockMarket1's event log reads: " 
						+ market1.log.toString(), 0, market1.log.size());
		
				assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMaket1's event log reads: "
						+ market1.log.toString(), 0, market1.log.size());
		
				assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getMarketBills().size(), 1);

				assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
						cashier.getMarketBills().get(0).getState() == billState.needPayment);
		
				assertTrue("MarketBill should contain a bill of price = $25.00. It contains something else instead: $" 
						+ cashier.getMarketBills().get(0).getTotal(), cashier.getMarketBills().get(0).getTotal() == 25.00);
		
				assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getBills().size(), 1);
				
				assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be paid for.", cashier.pickAndExecuteAnAction());
				
				//step 2 of the test
				waiter.msgBillReady(bill);
				
				//check postconditions for step 2 / preconditions for step 3
				
				assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getMarketBills().size(), 1);

				assertTrue("CashierBill should contain a bill with state == needChange. It doesn't.",
						cashier.getBills().get(0).getState() == billState.needChange);

				assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
						cashier.getMarketBills().get(0).getState() == billState.needPayment);
				
				assertTrue("CashierBill should contain a bill of price = $15.99. It contains something else instead: $" 
						+ cashier.getBills().get(0).getTotal(), cashier.getBills().get(0).getTotal() == 15.99);
				
				assertTrue("MarketBill should contain a bill of price = $25.00. It contains something else instead: $" 
						+ cashier.getMarketBills().get(0).getTotal(), cashier.getMarketBills().get(0).getTotal() == 25.00);
				
				assertTrue("CashierBill should contain a bill with the right customer in it. It doesn't.", 
							cashier.getBills().get(0).getCustomer() == customer);
				
				assertTrue("MockCustomer should have logged an event for receiving \"HereIsYourBill\" with the correct balance, but his last event logged reads instead: " 
						+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received HereIsYourBill from waiter. Total = 15.99"));
				
				assertTrue("CashierBill should contain a bill with state == needChange. It doesn't.",
						cashier.getBills().get(0).getState() == billState.needChange);

								
				//step 3
				//NOTE: I called the scheduler in the assertTrue statement below (to succintly check the return value at the same time)
				assertTrue("Cashier's scheduler should have returned true (needs to react to needPayments), but didn't.", 
							cashier.pickAndExecuteAnAction());
				//Step 3 post conditions
				assertTrue("CashierBill should contain a bill with state == done. It doesn't.",
						cashier.getBills().get(0).getState() == billState.done);

				assertTrue("CashierBill should contain changeDue == 4.01. It contains something else instead: $" 
						+ cashier.getBills().get(0).getChange(), cashier.getBills().get(0).getChange() == 4.01);
				
				assertTrue("MockCustomer should have logged an event for receiving \"HereIsYourChange\" with the correct change, but his last event logged reads instead: " 
						+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received HereIsYourChange from cashier. Change = 4.01"));
				
				assertTrue("Cashier should have money = $995.99.  It contains something else: $"
						+ cashier.getMoney(), cashier.getMoney() == 995.99);
				
				//step 4
				assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be removed.", cashier.pickAndExecuteAnAction());
				
				//step 4 postconditions
				assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
				
				assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
						cashier.getMarketBills().get(0).getState() == billState.needPayment);
				
				//step 5
				assertTrue("Cashier's scheduler should have returned true (needs to react to marketBill), but didn't.", 
							cashier.pickAndExecuteAnAction());
				//step 5 post conditions
				assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
				
				assertEquals("Cashier should have 1 market bill. It didn't.", cashier.getMarketBills().size(), 1);
				
				//Money= +15.99 - 20 - 25
				assertTrue("Cashier should have money = $970.99.  It contains something else: $"
						+ cashier.getMoney(), cashier.getMoney() == 970.99);
				
				assertTrue("MockMarket should have logged an event for receiving \"HereIsPayment\" with the correct change, but his last event logged reads instead: " 
						+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received HereIsPayment from cashier. Total = 25.0"));

				assertTrue("MarketBill should contain a bill with state == done. It doesn't.",
						cashier.getMarketBills().get(0).getState() == billState.done);
				
				//step 6
				assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be removed.", cashier.pickAndExecuteAnAction());
				//step 6 post conditions
				assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
				
				assertEquals("Cashier should have 0 market bill. It didn't.", cashier.getMarketBills().size(), 0);

				//step 7
				assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
						cashier.pickAndExecuteAnAction());
				
				System.out.println("--Concluded Test 4--");
	}
	
	/**
	 * Test 5: 1 poor customer, 1 waiter, 1 market
	 */
	public void testFiveNormalScenario(){
		customer.cashier = cashier;
		System.out.println("--Begin Test 5--");
		//check preconditions
				assertEquals("Cashier should have 0 bills in it. It doesn't.",cashier.getBills().size(), 0);
				assertEquals("Cashier should have 0 marketBills in it.  It doesn't.", cashier.getMarketBills().size(), 0);
				assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
				assertEquals("MockCustomer should have an empty event log before the Cashier's scheduler is called. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
				
				assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket1's event log reads: "
						+ market1.log.toString(), 0, market1.log.size());
		
		//step 1 of the test
		cashier.msgReceivedBillRequest(waiter, customer, "Steak", 3);//send the message from a waiter
		Bill bill = cashier.getBills().get(0);
		
		cashier.msgReceivedMarketBill(market1, "Steak", 5*5.00); //send this message from market1
		Bill marketBill = cashier.getMarketBills().get(0);
		
		customer.gotPoor();
		customer.setCheapskate(true);
		//check postconditions for step 1 and preconditions for step 2
		assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
		assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called.  Instead, the MockMarket1's event log reads: " 
				+ market1.log.toString(), 0, market1.log.size());

		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getBills().size(), 1);
		
		assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be paid for.", cashier.pickAndExecuteAnAction());
		
		assertEquals(
				"MockWaiter should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
		assertTrue("MockCustomer should have logged an event for receiving \"gotPoor\" with the correct money, but his last event logged reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("customer got poor"));

		assertTrue("customer should have money = $5.00.  It contains something else: $" 
				+ customer.getMoney(), customer.getMoney() == 5.00);
		
		//step 2 of the test
		waiter.msgBillReady(bill);
		//check postconditions for step 2 / preconditions for step 3  
		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getMarketBills().size(), 1);

		assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
				cashier.getMarketBills().get(0).getState() == billState.needPayment);

		assertTrue("MarketBill should contain a bill of price = $25.00. It contains something else instead: $" 
				+ cashier.getMarketBills().get(0).getTotal(), cashier.getMarketBills().get(0).getTotal() == 25.00);

		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getBills().size(), 1);
		
		//step 3
		assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be paid for.", cashier.pickAndExecuteAnAction());
		//step 3 postconditions
		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getMarketBills().size(), 1);
		
		assertTrue("MockCustomer should have logged an event for receiving \"YouCanGo\" after not paying, but his last event logged reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("received msgYouCanGo"));

		assertTrue("CashierBill should contain a bill with state == done. It doesn't.",
				cashier.getBills().get(0).getState() == billState.done);

		assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
				cashier.getMarketBills().get(0).getState() == billState.needPayment);

		assertTrue("CashierBill should contain a bill of price = $15.99. It contains something else instead: $" 
				+ cashier.getBills().get(0).getTotal(), cashier.getBills().get(0).getTotal() == 15.99);
		
		assertTrue("MarketBill should contain a bill of price = $25.00. It contains something else instead: $" 
				+ cashier.getMarketBills().get(0).getTotal(), cashier.getMarketBills().get(0).getTotal() == 25.00);
		
		assertTrue("CashierBill should contain a bill with the right customer in it. It doesn't.", 
					cashier.getBills().get(0).getCustomer() == customer);

						
		//step 4
		assertTrue("Cashier's scheduler should have returned true (needs to react to needPayments), but didn't.", 
					cashier.pickAndExecuteAnAction());
	
		assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
		
		assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
				cashier.getMarketBills().get(0).getState() == billState.needPayment);
		
		//step 5
		assertTrue("Cashier's scheduler should have returned true (needs to react to marketBill), but didn't.", 
					cashier.pickAndExecuteAnAction());
		//step 5 post conditions
		assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
		
		assertEquals("Cashier should have 1 market bill. It didn't.", cashier.getMarketBills().size(), 1);
		
		assertTrue("Cashier should have money = $975.00.  It contains something else: $"
				+ cashier.getMoney(), cashier.getMoney() == 975.00);
		
		assertTrue("MockMarket should have logged an event for receiving \"HereIsPayment\" with the correct change, but his last event logged reads instead: " 
				+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received HereIsPayment from cashier. Total = 25.0"));

		assertTrue("MarketBill should contain a bill with state == done. It doesn't.",
				cashier.getMarketBills().get(0).getState() == billState.done);
		
		//step 6
		assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be removed.", cashier.pickAndExecuteAnAction());
		//step 6 post conditions
		assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
		
		assertEquals("Cashier should have 0 market bill. It didn't.", cashier.getMarketBills().size(), 0);

		//step 7
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
		
		System.out.println("--Concluded Test 5--");
	}
	
	/**
	 * Test 6: 1 rich customer, 1 waiter, 1 market
	 */
	public void testSixNonNormalScenario(){
		customer.cashier = cashier;	
		
		System.out.println("--Begin Test 6--");
		//check preconditions
				assertEquals("Cashier should have 0 bills in it. It doesn't.",cashier.getBills().size(), 0);
				assertEquals("Cashier should have 0 marketBills in it.  It doesn't.", cashier.getMarketBills().size(), 0);
				assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
				assertEquals("MockCustomer should have an empty event log before the Cashier's scheduler is called. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
				
				assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket1's event log reads: "
						+ market1.log.toString(), 0, market1.log.size());
		
				//step 1 of the test
				cashier.msgReceivedBillRequest(waiter, customer, "Steak", 3);//send the message from a waiter
				Bill bill = cashier.getBills().get(0);
				
				cashier.msgReceivedMarketBill(market1, "Steak", 5*5.00); //send this message from market1
				Bill marketBill = cashier.getMarketBills().get(0);
				
				//check postconditions for step 1 and preconditions for step 2
				assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
								+ waiter.log.toString(), 0, waiter.log.size());
				
				assertEquals("MockCustomer should have an empty event log before the Cashier's scheduler is called.  Instead, the MockCustomer's event log reads: " 
								+ customer.log.toString(), 0, customer.log.size());
				
				assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called.  Instead, the MockMarket1's event log reads: " 
						+ market1.log.toString(), 0, market1.log.size());
		
				assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMaket1's event log reads: "
						+ market1.log.toString(), 0, market1.log.size());
		
				assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getMarketBills().size(), 1);

				assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
						cashier.getMarketBills().get(0).getState() == billState.needPayment);
		
				assertTrue("MarketBill should contain a bill of price = $25.00. It contains something else instead: $" 
						+ cashier.getMarketBills().get(0).getTotal(), cashier.getMarketBills().get(0).getTotal() == 25.00);
		
				assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getBills().size(), 1);
				
				assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be paid for.", cashier.pickAndExecuteAnAction());
				
				//step 2 of the test
				customer.gotRich();
				waiter.msgBillReady(bill);
				
				//check postconditions for step 2 / preconditions for step 3
				
				assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getMarketBills().size(), 1);

				assertTrue("CashierBill should contain a bill with state == needChange. It doesn't.",
						cashier.getBills().get(0).getState() == billState.needChange);

				assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
						cashier.getMarketBills().get(0).getState() == billState.needPayment);
				
				assertTrue("CashierBill should contain a bill of price = $15.99. It contains something else instead: $" 
						+ cashier.getBills().get(0).getTotal(), cashier.getBills().get(0).getTotal() == 15.99);
				
				assertTrue("MarketBill should contain a bill of price = $25.00. It contains something else instead: $" 
						+ cashier.getMarketBills().get(0).getTotal(), cashier.getMarketBills().get(0).getTotal() == 25.00);
				
				assertTrue("CashierBill should contain a bill with the right customer in it. It doesn't.", 
							cashier.getBills().get(0).getCustomer() == customer);
				
				assertTrue("MockCustomer should have logged an event for receiving \"HereIsYourBill\" with the correct balance, but his last event logged reads instead: " 
						+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received HereIsYourBill from waiter. Total = 15.99"));
				
				assertTrue("CashierBill should contain a bill with state == needChange. It doesn't.",
						cashier.getBills().get(0).getState() == billState.needChange);

								
				//step 3
				//NOTE: I called the scheduler in the assertTrue statement below (to succintly check the return value at the same time)
				assertTrue("Cashier's scheduler should have returned true (needs to react to needPayments), but didn't.", 
							cashier.pickAndExecuteAnAction());
				//Step 3 post conditions
				assertTrue("CashierBill should contain a bill with state == done. It doesn't.",
						cashier.getBills().get(0).getState() == billState.done);
				
				assertTrue("CashierBill should contain changeDue == 34.01. It contains something else instead: $" 
						+ cashier.getBills().get(0).getChange(), cashier.getBills().get(0).getChange() == 34.01);
				
				assertTrue("MockCustomer should have logged an event for receiving \"HereIsYourChange\" with the correct change, but his last event logged reads instead: " 
						+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received HereIsYourChange from cashier. Change = 34.01"));
		
				assertTrue("Cashier should have money = $965.99.  It contains something else: $"
						+ cashier.getMoney(), cashier.getMoney() == 965.99);

				//step 4
				assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be removed.", cashier.pickAndExecuteAnAction());
				//step 4 postconditions
				assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
				
				assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
						cashier.getMarketBills().get(0).getState() == billState.needPayment);
				
				//step 5
				assertTrue("Cashier's scheduler should have returned true (needs to react to marketBill), but didn't.", 
							cashier.pickAndExecuteAnAction());
				//step 5 post conditions
				assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
				
				assertEquals("Cashier should have 1 market bill. It didn't.", cashier.getMarketBills().size(), 1);

				assertTrue("Cashier should have money = $940.99.  It contains something else: $"
						+ cashier.getMoney(), cashier.getMoney() == 940.99);
				
				assertTrue("MockMarket should have logged an event for receiving \"HereIsPayment\" with the correct change, but his last event logged reads instead: " 
						+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received HereIsPayment from cashier. Total = 25.0"));

				assertTrue("MarketBill should contain a bill with state == done. It doesn't.",
						cashier.getMarketBills().get(0).getState() == billState.done);
				
				//step 6
				assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be removed.", cashier.pickAndExecuteAnAction());
				//step 6 post conditions
				assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
				
				assertEquals("Cashier should have 0 market bill. It didn't.", cashier.getMarketBills().size(), 0);

				//step 7
				assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
						cashier.pickAndExecuteAnAction());
				
				System.out.println("--Concluded Test 6--");
	}
	
	/**
	 * Test 7: 1 customer, 1 waiter, 2 markets
	 */
	
	public void testSevenNonNormalScenario(){
		customer.cashier = cashier;
		System.out.println("--Begin Test 7--");
		
		//check preconditions
		assertEquals("Cashier should have 0 bills in it. It doesn't.",cashier.getBills().size(), 0);
		assertEquals("Cashier should have 0 marketBills in it.  It doesn't.", cashier.getMarketBills().size(), 0);
		assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
				+ waiter.log.toString(), 0, waiter.log.size());

		assertEquals("MockCustomer should have an empty event log before the Cashier's scheduler is called. Instead, the MockCustomer's event log reads: "
				+ customer.log.toString(), 0, customer.log.size());
		
		assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket1's event log reads: "
				+ market1.log.toString(), 0, market1.log.size());
		
		assertEquals("MockMarket2 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket2's event log reads: "
				+ market2.log.toString(), 0, market2.log.size());
		
		//step 1 of the test
		cashier.msgReceivedBillRequest(waiter, customer, "Steak", 3);//send the message from a waiter
		Bill bill = cashier.getBills().get(0);
		
		cashier.msgPartialOrderBill(market1, "Steak", 3*5.00); //send this message from market1
		cashier.msgPartialOrderBill(market2, "Steak", 2*5.00); //send this message from market2
		
		Bill marketBill1 = cashier.getMarketBills().get(0);
		Bill marketBill2 = cashier.getMarketBills().get(1);
		
		//check postconditions for step 1 and preconditions for step 2
		
		assertEquals("Cashier should have 2 bills in it. It doesn't.", cashier.getMarketBills().size(), 2);
		
		assertEquals("MockMarket1 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket1's event log reads: "
				+ market1.log.toString(), 0, market1.log.size());

		assertEquals("MockMarket2 should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket2's event log reads: "
				+ market2.log.toString(), 0, market2.log.size());

		assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
				cashier.getMarketBills().get(0).getState() == billState.needPayment);
		
		assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
				cashier.getMarketBills().get(1).getState() == billState.needPayment);

		assertTrue("MarketBill should contain a bill of price = $15.00. It contains something else instead: $" 
				+ cashier.getMarketBills().get(0).getTotal(), cashier.getMarketBills().get(0).getTotal() == 15.00);

		assertTrue("MarketBill should contain a bill of price = $10.00. It contains something else instead: $" 
				+ cashier.getMarketBills().get(1).getTotal(), cashier.getMarketBills().get(1).getTotal() == 10.00);

		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.getBills().size(), 1);
		
		assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be paid for.", cashier.pickAndExecuteAnAction());
		
		//step 2 of the test
		waiter.msgBillReady(bill);
		
		//check postconditions for step 2 / preconditions for step 3
		
		assertEquals("Cashier should have 2 bills in it. It doesn't.", cashier.getMarketBills().size(), 2);

		assertTrue("CashierBill should contain a bill with state == needChange. It doesn't.",
				cashier.getBills().get(0).getState() == billState.needChange);

		assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
				cashier.getMarketBills().get(0).getState() == billState.needPayment);
		
		assertTrue("CashierBill should contain a bill of price = $15.99. It contains something else instead: $" 
				+ cashier.getBills().get(0).getTotal(), cashier.getBills().get(0).getTotal() == 15.99);
		
		assertTrue("MarketBill1 should contain a bill of price = $15.00. It contains something else instead: $" 
				+ cashier.getMarketBills().get(0).getTotal(), cashier.getMarketBills().get(0).getTotal() == 15.00);

		assertTrue("MarketBill2 should contain a bill of price = $10.00. It contains something else instead: $" 
				+ cashier.getMarketBills().get(1).getTotal(), cashier.getMarketBills().get(1).getTotal() == 10.00);
		
		assertTrue("CashierBill should contain a bill with the right customer in it. It doesn't.", 
					cashier.getBills().get(0).getCustomer() == customer);
		
		assertTrue("MockCustomer should have logged an event for receiving \"HereIsYourBill\" with the correct balance, but his last event logged reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received HereIsYourBill from waiter. Total = 15.99"));
		
		assertTrue("CashierBill should contain a bill with state == needChange. It doesn't.",
				cashier.getBills().get(0).getState() == billState.needChange);
								
		//step 3
		//NOTE: I called the scheduler in the assertTrue statement below (to succintly check the return value at the same time)
		assertTrue("Cashier's scheduler should have returned true (needs to react to needPayments), but didn't.", 
					cashier.pickAndExecuteAnAction());
		//Step 3 post conditions
		assertTrue("CashierBill should contain a bill with state == done. It doesn't.",
				cashier.getBills().get(0).getState() == billState.done);

		assertTrue("CashierBill should contain changeDue == 4.01. It contains something else instead: $" 
				+ cashier.getBills().get(0).getChange(), cashier.getBills().get(0).getChange() == 4.01);
		
		assertTrue("MockCustomer should have logged an event for receiving \"HereIsYourChange\" with the correct change, but his last event logged reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received HereIsYourChange from cashier. Change = 4.01"));
		
		assertTrue("Cashier should have money = $995.99.  It contains something else: $"
				+ cashier.getMoney(), cashier.getMoney() == 995.99);
		
		//step 4
		assertTrue("Cashier's scheduler should have returned true.  The bill is ready to be removed.", cashier.pickAndExecuteAnAction());
		
		//step 4 postconditions
		assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
		
		assertTrue("MarketBill should contain a bill with state == needPayment. It doesn't.",
				cashier.getMarketBills().get(0).getState() == billState.needPayment);
		
		//step 5
		assertTrue("Cashier's scheduler should have returned true (needs to react to marketBill), but didn't.", 
					cashier.pickAndExecuteAnAction());
		//step 5 post conditions
		assertEquals("Cashier should have removed the bill. It didn't.", cashier.getBills().size(), 0);
		
		assertEquals("Cashier should have 2 market bills. It didn't.", cashier.getMarketBills().size(), 2);
		
		//Money= +15.99 - 20 - 15
		assertTrue("Cashier should have money = $980.99.  It contains something else: $"
				+ cashier.getMoney(), cashier.getMoney() == 980.99);
		
		assertTrue("MockMarket should have logged an event for receiving \"HereIsPayment\" with the correct change, but his last event logged reads instead: " 
				+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received HereIsPayment from cashier. Total = 15.0"));

		//step 6 of the test
		assertTrue("Cashier's scheduler should have returned true.  The bills are ready to be removed.", cashier.pickAndExecuteAnAction());
		//Step 6 post conditions
		assertEquals("Cashier should have removed the bills. It didn't.", cashier.getBills().size(), 0);
		
		assertTrue("Cashier should have money = $970.99.  It contains something else: $"
				+ cashier.getMoney(), cashier.getMoney() == 970.99);
			
		assertTrue("MockMarket should have logged an event for receiving \"HereIsPayment\" with the correct change, but his last event logged reads instead: " 
				+ market2.log.getLastLoggedEvent().toString(), market2.log.containsString("Received HereIsPayment from cashier. Total = 10.0"));

	
		//step 7 of the test
		assertTrue("Cashier's scheduler should have returned true since it runs through the scheduler for customer, but didn't.", 
				cashier.pickAndExecuteAnAction());
		
		assertTrue("Cashier's scheduler should have returned true since it runs through the scheduler for market1, but didn't.", 
				cashier.pickAndExecuteAnAction());
		
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
		System.out.println("--Concluded Test 7--");
	}
	
	
	
	/**
	 * Test 8: Cashier not enough money, 1 market
	 */
}
