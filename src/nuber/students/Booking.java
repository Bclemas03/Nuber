package nuber.students;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Booking represents the overall "job" for a passenger getting to their destination.
 * 
 * It begins with a passenger, and when the booking is commenced by the region 
 * responsible for it, an available driver is allocated from dispatch. If no driver is 
 * available, the booking must wait until one is. When the passenger arrives at the destination,
 * a BookingResult object is provided with the overall information for the booking.
 * 
 * The Booking must track how long it takes, from the instant it is created, to when the 
 * passenger arrives at their destination. This should be done using Date class' getTime().
 * 
 * Booking's should have a globally unique, sequential ID, allocated on their creation. 
 * This should be multi-thread friendly, allowing bookings to be created from different threads.
 * 
 * @author james
 *
 */
public class Booking implements Callable<BookingResult>{

	public Driver driver;	
	private NuberDispatch dispatch;
	private Passenger passenger;
	private int id;
	private static int numOfBookings = 0;
	/**
	 * Creates a new booking for a given Nuber dispatch and passenger, noting that no
	 * driver is provided as it will depend on whether one is available when the region 
	 * can begin processing this booking.
	 * 
	 * @param dispatch
	 * @param passenger
	 */
	public Booking(NuberDispatch dispatch, Passenger passenger)
	{
		this.dispatch = dispatch;
		this.passenger = passenger;
		this.id = ++numOfBookings;
	}
	
	/**
	 * At some point, the Nuber Region responsible for the booking can start it (has free spot),
	 * and calls the Booking.call() function, which:
	 * 1.	Asks Dispatch for an available driver
	 * 2.	If no driver is currently available, the booking must wait until one is available. 
	 * 3.	Once it has a driver, it must call the Driver.pickUpPassenger() function, with the 
	 * 			thread pausing whilst as function is called.
	 * 4.	It must then call the Driver.driveToDestination() function, with the thread pausing 
	 * 			whilst as function is called.
	 * 5.	Once at the destination, the time is recorded, so we know the total trip duration. 
	 * 6.	The driver, now free, is added back into Dispatch�s list of available drivers. 
	 * 7.	The call() function the returns a BookingResult object, passing in the appropriate 
	 * 			information required in the BookingResult constructor.
	 *
	 * @return A BookingResult containing the final information about the booking 
	 */
	public BookingResult call() {
		dispatch.logEvent(null, " Is awaiting a driver...");
		NuberRegion region = this.dispatch.regionDict.get(this.passenger.region);
		while (dispatch.idleDrivers.size() <= 0){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.driver = dispatch.getDriver();
		region.waitingJobs.remove(this);
		dispatch.logEvent(this, " Is has been assigned driver " + this.driver.name);
		

		dispatch.logEvent(this, " Is being picked up");
		driver.pickUpPassenger(passenger);
		Date startTime = new Date();

		dispatch.logEvent(this, " Is being dropped off");
		driver.driveToDestination();
		Date endTime = new Date();
		
		long travelTime = endTime.getTime() - startTime.getTime();
		dispatch.logEvent(this, " trip took " + travelTime + " milliseconds");

		dispatch.addDriver(driver);
		region.simultaneousJobs.remove(this);
		return new BookingResult(id, passenger, driver, travelTime);
	}
	
	/***
	 * Should return the:
	 * - booking ID, 
	 * - followed by a colon, 
	 * - followed by the driver's name (if the driver is null, it should show the word "null")
	 * - followed by a colon, 
	 * - followed by the passenger's name (if the passenger is null, it should show the word "null")
	 * 
	 * @return The compiled string
	 */
	@Override
	public String toString()
	{
		try{
			return id + ": " + driver.name + ": " + passenger.name;
		}
		catch(Exception e){
			System.out.println(e);
			return "";
		}
		
	}

}
