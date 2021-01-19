package SleepingBarber;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SleepingBarberReentrantLock extends Thread 
{

 /* Assigned No of Chairs in the barbershop to 3. */
 public static final int Chairs = 3;
 
 /* we create the integer numberOfFreeSeats where customers can either sit on a free seat or leave the barbershop if there are no seats available */
 public static int freeSeats = Chairs;
 
 /* We create Customer pool which will be waiting for their hair cut.*/
 public static CustomerPool customers = new CustomerPool(Chairs);
 
 /* We create a ReentrantLock for barber with a condition to wait if the barber is not available*/
 public static Lock barber = new ReentrantLock();
 public static Condition barberAvailable = barber.newCondition();
 
 /*We create a ReentrantLock for chairs so that we can increment the counter safely*/
 public static Lock accessSeats = new ReentrantLock();

 class Barber extends Thread 
 {
	 int id;
	 
	 public Barber(int b) 
	 {
		 id=b;
	 }

	 public void run()
	 {
		 while (true)
		 { // runs in an infinite loop
			
			 try
			 {
				 customers.acquireCustomer(); // tries to acquire a customer - if
				 // none is available he goes to
				 // sleep
				 accessSeats.lock(); // at this time he has been awaken ->
				 // want to modify the number of
				 // available seats
				
				 freeSeats++; // one chair gets free
				 barber.lock();
				 try 
				 {
					 barberAvailable.signal(); // the barber is ready to cut
				 } 
				 finally
				 {
					 barber.unlock(); 
				 }
				 accessSeats.unlock(); 
				 this.cutHair(); // cutting...
				 
			 } 
			 catch (InterruptedException ex)
			 {
			 }
		 }
  }

  /* this method will simulate cutting hair */

  public void cutHair() 
  {
	  System.out.println("Barber " + this.id + " is serving the customer");
	  //System.out.println("The barber is cutting hair");
	  try 
	  {
		  sleep(6000);
	  } 
	  catch (InterruptedException ex) 
	  {
	  }
  }
 }
 
 /* Created a Customer Thread */
 class Customer extends Thread 
 {
	 int id;
	 boolean notCut = true;

	 public Customer(int i)
	 {
		 id = i;
	 }

	 public void run()
	 {
		 while (notCut) // keep checking until the customer is not cut
		 { 
			 accessSeats.lock(); // tries to get access to the chairs
			 if (freeSeats > 0)  // check if there are any free seats
			 { 
				 System.out.println("Customer " + this.id + " just sat down for the haircut");
				 freeSeats--; // sitting down on a chair
				
				 customers.releaseCustomer(); // notify the barber that there is a customer
				
				 accessSeats.unlock(); // don't need to lock the chairs
				 barber.lock();
				 try 
				 {
					 barberAvailable.await();  // now it's this customers turn and we have to wait if the barber is busy
					 
				 } 
				 catch (InterruptedException e) 
				 {
				 } 
				 finally 
				 {
					 barber.unlock(); 
				 }
				 notCut = false;
				 System.out.println(freeSeats + " seats available in waiting room ");
				 this.get_haircut(); // barber is cutting the hair
			 } 
			 else 
			 { // there are no free seats
				 System.out.println("There are no free seats. Customer " + this.id + " has left the barbershop.");
				 accessSeats.unlock(); // release the lock on the seats
				 notCut = false; // the customer will leave since there
			 }
		 }
	 }

	 public void get_haircut() 
	 {
		 System.out.println("Customer " + this.id + " is done with the hair cut");
		 System.out.println("------------------------------------------------------");
		 try 
		 {
			 sleep(5050);
		 } 
		 catch (InterruptedException ex) 
		 {
		 }
	 }

 }



 public static void main(String args[]) 
 {

  SleepingBarberReentrantLock barberShop = new SleepingBarberReentrantLock(); 
  barberShop.start();
 }

 public void run() 
 {
  
  for(int b =1;b<3;b++)
  {
	  Barber barber = new Barber(b);
	  barber.start(); 
	  try
	   {
	    sleep(2000);
	   } 
	   catch (InterruptedException ex)
	   {
	   }
  }


  for (int i = 1; i < 21; i++) {
   Customer aCustomer = new Customer(i);
   aCustomer.start();
   try
   {
    sleep(2000);
   } 
   catch (InterruptedException ex)
   {
   }
  }
 }
}

class CustomerPool 
{

    private final Lock lock = new ReentrantLock();
    private final Condition poolAvailable = lock.newCondition();
    private int num_customers;
    private final int max_num_customers;


    public CustomerPool(int num_customer_pools) 
    {
            this.max_num_customers = num_customer_pools;
            this.num_customers = 0;
    }

    public void acquireCustomer() throws InterruptedException 
    {
        lock.lock();
        try {
            while (num_customers <= 0)
                poolAvailable.await();
            --num_customers;
        } 
        finally
        {
            lock.unlock();
        }
    }

    public void releaseCustomer() 
    {
        lock.lock();
        try 
        {
            
            if(num_customers >= max_num_customers)      
                return;
            ++num_customers;
            poolAvailable.signal();
        } 
        finally 
        {
            lock.unlock();
        }
    }
    
    public int getNumOfCustomers() 
    {
    	return num_customers;
    }
}