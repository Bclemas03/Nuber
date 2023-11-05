package nuber.students;

import java.util.concurrent.ThreadLocalRandom;

public class Driver extends Person {

	private Passenger currentPassenger;
	public Driver(String driverName, int maxSleep)
	{
		super(driverName, maxSleep);
	}

	//Driver methods don't require synchronization, as no same Driver object is visible to more than one thread simultaneously
	/**
	 * Stores the provided passenger as the driver's current passenger and then
	 * sleeps the thread for between 0-maxDelay milliseconds.
	 * 
	 * @param newPassenger Passenger to collect
	 * @throws InterruptedException
	 */
	public void pickUpPassenger(Passenger newPassenger)
	{
		//driveToDestination(newPassenger);
		try{
			currentPassenger = newPassenger;
			//Sleep thread for a time between 0 and maxSleep inclusive (both 0 and the max value are options but no higher or lower)
			Thread.sleep(ThreadLocalRandom.current().nextInt(-1, maxSleep + 1));
		}
		catch (Exception e){
			System.out.println(e);
		}
	}

	/**
	 * Sleeps the thread for the amount of time returned by the current 
	 * passenger's getTravelTime() function
	 * 
	 * @throws InterruptedException
	 */
	public void driveToDestination() {
		try{
			Thread.sleep(this.currentPassenger.getTravelTime());
		}
		catch (Exception e){
			System.out.println(e);
		}
	}
	
}
