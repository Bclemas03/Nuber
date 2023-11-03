package nuber.students;

public class Passenger extends Person
{
	String region = null;
	public Passenger(String name, int maxSleep) {
		super(name, maxSleep);
	}

	public void addRegion(String r){
		region = r;
	}

	public int getTravelTime()
	{
		return (int)(Math.random() * maxSleep);
	}

}
