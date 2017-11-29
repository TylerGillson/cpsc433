
public class Slot
{
	private String name;
	private boolean evening = false;
	private int max;
	
	public Slot (String type, int idx)
	{
		name = type.equals("course") ? Driver.courses.get(idx).toString() : Driver.labs.get(idx).toString();
		max = type.equals("course") ? Integer.valueOf(Driver.course_slots.get(idx).get(2)) : Integer.valueOf(Driver.lab_slots.get(idx).get(2));
		setEvening(type, idx); 
	}
	
	public void setEvening(String type, int idx){
		String time = type.equals("course") ? Driver.course_slots.get(idx).get(1) : Driver.lab_slots.get(idx).get(1);
		int hr = Integer.valueOf(time.substring(0, time.indexOf(':')));
		evening = (hr >= 18) ? true : false;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean getEvening()
	{
		return evening;
	}

	public int getMax()
	{
		return max;
	}
}