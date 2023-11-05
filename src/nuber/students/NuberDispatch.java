package nuber.students;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * The core Dispatch class that instantiates and manages everything for Nuber
 * 
 * @author james
 *
 */
public class NuberDispatch {

	
	//Array of idleDrivers for adding drivers into
	public LinkedList<Driver> idleDrivers;

	//dict of regions stored by string
	public HashMap<String, NuberRegion> regionDict;
	//regionInfoMap 
	private HashMap<String, Integer> regionInfo;
	//The maximum number of idle drivers that can be awaiting a booking 
	private final int MAX_DRIVERS;
	//boolean for the logging or nonlogging of events, default false.
	private boolean logEvents;
	
	
	/**
	 * Creates a new dispatch objects and instantiates the required regions and any other objects required.
	 * It should be able to handle a variable number of regions based on the HashMap provided.
	 * 
	 * @param regionInfo Map of region names and the max simultaneous bookings they can handle
	 * @param logEvents Whether logEvent should print out events passed to it
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents)
	{
		this.regionInfo = regionInfo;
		this.regionDict = new HashMap<String, NuberRegion>();
		for (String region :  regionInfo.keySet()){
			regionDict.putIfAbsent(region, new NuberRegion(this, region, regionInfo.get(region)));
		}


		this.logEvents = logEvents;
		//Max_Drivers is inialised as 999
		this.MAX_DRIVERS = 999;
		//idleDrivers is inialised as a Driver Array length of Max_Drivers
		this.idleDrivers = new LinkedList<Driver>(){};
	}
	
	/**
	 * Adds drivers to a queue of idle driver.
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @param The driver to add to the queue.
	 * @return Returns true if driver was added to the queue
	 */
	public boolean addDriver(Driver newDriver)
	{
		if (idleDrivers.size() <= MAX_DRIVERS){
			if (idleDrivers.contains(newDriver)) {
				return false;
			}
			else {
				idleDrivers.add(newDriver);
				return true;
			}
		}
		else {
			return false;
		}
	}
	
	/**
	 * Gets a driver from the front of the queue
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @return A driver that has been removed from the queue
	 */
	public synchronized Driver getDriver()
	{
		if (idleDrivers.size() > 0){
			Driver driver = idleDrivers.getFirst();
			idleDrivers.removeFirst();
			return driver;
		}
		else{
			return null;
		}
	}

	/**
	 * Prints out the string
	 * 	    booking + ": " + message
	 * to the standard output only if the logEvents variable passed into the constructor was true
	 * 
	 * @param booking The booking that's responsible for the event occurring
	 * @param message The message to show
	 */
	public void logEvent(Booking booking, String message) {
		
		if (!logEvents) return;
		
		System.out.println(booking + ": " + message);
		
	}

	/**
	 * Books a given passenger into a given Nuber region.
	 * 
	 * Once a passenger is booked, the getBookingsAwaitingDriver() should be returning one higher.
	 * 
	 * If the region has been asked to shutdown, the booking should be rejected, and null returned.
	 * 
	 * @param passenger The passenger to book
	 * @param region The region to book them into
	 * @return returns a Future<BookingResult> object
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
		try{
			passenger.addRegion(region);
			NuberRegion NRegion = regionDict.get(region);
			if (!NRegion.isShutdown){
				return NRegion.bookPassenger(passenger);
			}
		}
		catch(Exception e){
			System.out.println("Invalid region selected for booking, add region first, or use valid region.\nError: ");
			System.out.print(e);
			return null;
		}
		System.out.println("can't book, " + passenger.name + " because " + region + " is shutdown");
		return null;
	}

	/**
	 * Gets the number of non-completed bookings that are awaiting a driver from dispatch
	 * 
	 * Once a driver is given to a booking, the value in this counter should be reduced by one
	 * 
	 * @return Number of bookings awaiting driver, across ALL regions
	 */
	public int getBookingsAwaitingDriver()
	{
		int numWaiting = 0;
		for (NuberRegion region : regionDict.values()){
			numWaiting += region.waitingJobs.size();
		}
		return numWaiting;
	}
	
	/**
	 * Tells all regions to finish existing bookings already allocated, and stop accepting new bookings
	 */
	public void shutdown() {
		for (NuberRegion region : regionDict.values()){
			region.shutdown();
		}
	}

}
