import java.util.*;

public class Constr
{
	private boolean valid = true;
	private int [] currentAssign;
	private Course [] sectionList;
	private Slot [] slotList;
	private int [] slotMax;
	private int pr_size;
	private int all_slots_size;
	
	public Constr ()
	{
		pr_size = Driver.courses.size() + Driver.labs.size();
		all_slots_size = Driver.course_slots.size() + Driver.lab_slots.size();
		makeCoursesAndSlots();
	}
	
	public void makeCoursesAndSlots(){
		Course [] all_sections = new Course[pr_size]; 
		Slot [] all_slots = new Slot[all_slots_size];
		
		Driver.courses.forEach(c -> {
    		int idx = Driver.courses.indexOf(c);
			Course section = new Course("course", idx);
			all_sections[idx] = section;});
		
		Driver.labs.forEach(l -> {
    		int idx = Driver.labs.indexOf(l);
			Course section = new Course("lab", idx);
			all_sections[idx] = section;});
		
		Driver.course_slots.forEach(cs -> {
    		int idx = Driver.course_slots.indexOf(cs);
			Slot course_slot = new Slot("course", idx);
			all_slots[idx] = course_slot;});
		
		Driver.lab_slots.forEach(ls -> {
    		int idx = Driver.lab_slots.indexOf(ls);
			Slot lab_slot = new Slot("lab", idx);
			all_slots[idx] = lab_slot;});
		
		sectionList = all_sections;
		slotList = all_slots;
		slotMax = new int[all_slots.length];
	}
	
	//Potentially combine into one or two bigger functions?
	//What's the most efficient?
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
			if (sectionList[i].getEvening() == true && slotList[currentAssign[i]].getEvening() == false)
				valid = false;
			else if (sectionList[i].getEvening() == false && slotList[currentAssign[i]].getEvening() == true)
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
			if (slotList[i].getMax() < slotMax[i]){
				valid = false;
				break;
			}
		}
	}	
	
	//For each course gets the list of unwanted slots and checks through all of them 
	//to see if current slot is unwanted
	public void unwanted()
	{
		for (int i = 0; i < currentAssign.length; i++)
		{
			if (!valid)
				break;
			
			for (int j = 0; j < sectionList[i].getUnwanted().size(); j++)
			{
				if (sectionList[i].getUnwanted().get(j).equals(slotList[currentAssign[i]].getName())){
					valid = false;
					break;
				}
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
			ArrayList<List<String>> currentIncompatible = new ArrayList<List<String>>();
			ArrayList<Course> currentCourses = new ArrayList<Course>();
			
			for (int j = 0; j < currentAssign.length; j++)
			{
				if (currentAssign[j] == i)
				{
					currentIncompatible.addAll(sectionList[j].getIncompatible());
					currentCourses.add(sectionList[j]);
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