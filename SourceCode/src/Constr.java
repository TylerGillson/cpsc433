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
	
	/**
	* Method checks to see if two classes have any time overlap using the index of an a slot in currentAssign
	* @param assignIndex1, assignIndex2 are the indexes of the classes in currentAssign
	* @return true if there is no conflict, false if there is a time conflict
	*/
	public  boolean checkCourseTimeConflict(int assignIndex1, int assignIndex2)
	{
		
		if (currentAssign == null) throw new NullPointerException();
		List<String> time1 = getTimeSlot(assignIndex1);
		List<String> time2 = getTimeSlot(assignIndex2);
		
		//Check to see if they are on the same day
		if(time1.get(0).compareTo(time2.get(0)) != 0){
			return true;
		} 
		
		
		float classDuration;
		if (time1.get(0).compareTo("MO") ==0){
			classDuration = 100; // 1 hour classes
		}
		else
			classDuration = 115f; // 1 hour 15 classes
		
		//We now need to compute the end start time as an integer for each class
		
		float time1start;
		
		char digit1 = time1.get(1).charAt(0);
		char digit2 = time1.get(1).charAt(1);
		
		if (digit2 == ':')
		{
			time1start = (digit1 - 48)* 100;
		}
		else{
			
			time1start = (digit1 -48)*1000 + (digit2 -48)*100;
			
		}
		
		//Do the same for the other time
		float time2start;
		
		digit1 = time2.get(1).charAt(0);
		digit2 = time2.get(1).charAt(1);
		
		if (digit2 == ':')
		{
			time2start = (digit1 - 48)* 100;
		}
		else{
			
			time2start = (digit1 -48)*1000 + (digit2 -48)*100;
		}
		
		//Use this to find the end times
		float time1end = time1start + classDuration;
		float time2end = time2start + classDuration;
		
		//Check for conflicts
		if (time1start == time2start)
			return false;
		
		//Check to see if they end before the other starts
		if(time1end <= time2start)
			return true;
		
		if(time2end <= time1start)
			return true;
		
		//Check to see if one starts in the middle of the other
		if((time1start < time2start) && (time2start < time1end))
			return false;
		
		if((time2start < time1start)&& (time1start < time2end))
			return false;
		
		//Check to see if one ends in the middle of the other
		if((time1start < time2end) && (time2end < time1end))
			return false;
		
		if((time2start < time1end) && (time1end < time2end))
			return false;
		
		
		return true;
	}
	
	
	/**
	* Method returns the time slot based on desired assignment index
	* @param int index, the index of course assign 
	* @return List<String> the time slot
	*/
	private List<String> getTimeSlot(int index)
	{
		int firstLab = Driver.courses.size();
		
		if (index < firstLab){
			return Driver.course_slots.get(currentAssign[index]);
			
		}
		else{

			return Driver.lab_slots.get(currentAssign[index]);
		}
			
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
	
	
	/**
	* Method totals the number of incampatibilies and unwanted preferences
	* It will then sort so that the first returned index is of the class that has the most incampatibilies and unwanted preferences
	* Note that an unwanted preference is worth 0.01% of an incompatible weight. This acts as a tie breaker when sorting.
	* This will only cause issues if there is a class that has over 10000 unwanted preference, and so is deemed legal for now
	* @param Parser parse, the parser used to read the file
	* @return float[] indexes, the sorted array of class indexes. The first value is the most constrained. This can be safely cast to an int without any issues.
	*/
	public static float[] getTightestBoundClass(Parser parse){
		
		
		if (parse == null) throw new NullPointerException();
		
		try{
			
		float incompatibleWeight = 1;
		float unwantedWeight = 0.0001f;
			
		ArrayList<ArrayList<List<String>>> not_compatible = parse.getNotCompatible();
		ArrayList<ArrayList<List<String>>> unwanted = parse.getUnwanted();
		
		
		//First we go throught the array and find out which class has the most restraints numerically
		float[] restraintCount = new float[parse.getCourses().size() + parse.getLabs().size()];
		
		
		//The coursemap hashmap is essentially a lookup table to find out which index correspons with what class
		HashMap courseMap = new HashMap();
		
		//We start by adding in all the courses to it
		for (int i = 0; i < parse.getCourses().size(); i++){
			courseMap.put(parse.getCourses().get(i), i);
		}
		
		//Then add all the labs
		for (int i = parse.getCourses().size(); i <parse.getCourses().size() + parse.getLabs().size(); i++){
			
			courseMap.put(parse.getLabs().get(i - parse.getCourses().size()), i);
		}
		
		//We are now ready to go through all of the incompatible classes, and find the most mentioned class
		for(int i = 0; i < not_compatible.size(); i++){
			
			for(int j = 0 ; j < not_compatible.get(i).size(); j++)
			{
				//When a class is incremented, use the hashmap to find its index, and incriment its counter
				restraintCount[(int)courseMap.get(not_compatible.get(i).get(j))]+=incompatibleWeight;  
			}	
			
		}
		
	
		
		//We then go back through and include the soft preferences for unwanted, and use it as a half interval.
		//System.out.println("Unwanted is: " + unwanted);
		for(int i = 0; i < unwanted.size(); i++){
			//System.out.println("OUTER");
			
			//When a class is incremented, use the hashmap to find its index, and incriment its counter
			restraintCount[(int)courseMap.get(unwanted.get(i).get(0))]+=unwantedWeight;  
		
			
		}
			
		
		

		//Use quick sort to sort by lowest mentioned. We then flip this.
		QuickSortWithIndex sorter = new QuickSortWithIndex(restraintCount);
		float[] sortedIndexes = sorter.sort()[1];

		float temp;
		for (int i = 0; i < sortedIndexes.length/2; i++){
			temp = sortedIndexes[i];
			sortedIndexes[i] = sortedIndexes[sortedIndexes.length - i -1];
			sortedIndexes[sortedIndexes.length - i -1] = temp;
		}
		
		//Return the flipped array
		return sortedIndexes;
		
		
		}
		catch(Exception e){
		e.getStackTrace();
		System.out.println(e);
			throw new IllegalStateException();
		}
	
		
		
	}
}
