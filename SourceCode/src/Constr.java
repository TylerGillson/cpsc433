import java.util.*;

public class Constr
{
	private boolean valid = true;
	private int [] currentAssign;
	private Course [] courseList;
	private Slot [] slotList;
	private int [] slotMax;
	
	public Constr (Course [] courses, Slot [] slots)
	{
		courseList = courses;
		slotList = slots;
		slotMax = new int[slots.length];
	}
	
	//Potentially combine into one or two bigger functions?
	//What's the most effiecient?
	public boolean evaluate (int [] assign)
	{
		currentAssign = assign;
		
		if (valid == true)
			evening();
		
		if (valid == true)
			max();
		
		if (valid == true)
			unwanted();

		if (valid == true)
			incompatible();		
		
		return valid;
	}
	
	//Optimize so stops looping when invalid
	//Checks each course to see if it is an evening course
	//If so checks if it's in evening slot
	public void evening()
	{
		for (int i = 0; i < currentAssign.length; i++)
		{
			if (courseList[i].getEvening() == true && slotList[currentAssign[i]].getEvening() == false)
				valid = false;
		}			
	}

	//Optimize so stops looping when invalid
	//First goes through all courses and adds to count of slot when that slot is used
	//Then compares slot counts versus slot max
	public void max()
	{
		for (int i = 0; i < currentAssign.length; i++)
		{
			slotMax[currentAssign[i]]++;
		}
		
		for (int i = 0; i < slotList.length; i++)
		{
			if (slotList[i].getMax() < slotMax[i])
				valid = false;
		}
	}	
	
	//For each course gets the list of unwanted slots and checks through all of them 
	//to see if current slot is unwanted
	public void unwanted()
	{
		for (int i = 0; i < currentAssign.length; i++)
		{
			for (int j = 0; j < courseList[i].getUnwanted().size(); j++)
			{
				if (courseList[i].getUnwanted().get(j).equals(slotList[currentAssign[i]].getName()))
					valid = false;
			}
		}
	}
	
	//For each slot gets courses in that slot and their incompatible courses.
	//Merges together incompatibles and checks against courses.
	//If a course appears in both lists then it isn't valid
	public void incompatible()
	{
		for (int i = 0; i < slotList.length; i++)
		{
			ArrayList<String> currentIncompatible = new ArrayList<String>();
			ArrayList<Course> currentCourses = new ArrayList<Course>();
			
			for (int j = 0; j < currentAssign.length; j++)
			{
				if (currentAssign[j] == i)
				{
					currentIncompatible.addAll(courseList[j].getIncompatible());
					currentCourses.add(courseList[j]);
				}
			}
			
			for (int m = 0; m < currentIncompatible.size(); m++)
			{
				for (int n = 0; n < currentCourses.size(); n++)
				{
					if (currentIncompatible.get(m).equals(currentCourses.get(n)))
						valid = false;
				}
			}			
		}
	}	
}