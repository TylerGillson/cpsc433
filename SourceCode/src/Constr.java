import java.util.*;

/* HARD CONSTRAINTS
1. No more than coursemax(s) courses can be assigned to slot s
	CHECK
	
2. No more than labmax(s) labs can be assigned to slot s
	CHECK
	
3. assign(ci) has to be unequal to assign(lik) for all k and i. (a course and its labs cannot be in the same slot)
	This is handled implicitly. Courses are only ever assigned course slots and labs are only ever assigned lab slots.
	
4. not-compatible(a,b) means: assign(a) cannot equal assign(b) (where a,b in Courses + Labs)
	CHECK --- Current solution highly inefficient.
	
5. partassign: assign(a) must equal partassign(a) for all a in Courses + Labs with partassign(a) not equal to $
	CHECK
	
6. unwanted(a,s): assign(a) cannot equal s (with a in Courses + Labs and s in Slots)
	CHECK
	
7. All course sections with a section number starting LEC 9 are evening classes and have to be scheduled into evening slots (18:00 or later).
	CHECK
	
8. All 500-level course sections must be scheduled into different time slots.
	???
	
9. No courses can be scheduled at Tuesdays 11:00-12:30.
	CHECK
	
	IF CPSC 313 IN COURSES:
10. CPSC 813 must be scheduled for Tuesdays/Thursdays 18:00-19:00
11. CPSC 813 cannot overlap with any labs/tutorials, or course sections of CPSC 313
12. CPSC 813 cannot overlay with any courses that cannot overlap with CPSC 313
	
	IF CPSC 413 IN COURSES:
13. CPSC 913 must be scheduled for Tuesdays/Thursdays 18:00-19:00 
14. CPSC 913 cannot overlap with any labs/tutorials, or course sections of CPSC 413
15. CPSC 913 cannot overlay with any courses that cannot overlap with CPSC 413

16. ASSUMPTION: We can ignore the following (due to abstract slot representation):
	-- If a course (course section) is put into a slot on Mondays, it has to be put into the corresponding time slots on Wednesdays and Fridays.
	-- If a course (course section) is put into a slot on Tuesdays, it has to be put into the corresponding time slots on Thursdays.
	-- If a lab/tutorial is put into a slot on Mondays, it has to be put into the corresponding time slots on Wednesdays.
	-- If a lab/tutorial is put into a slot on Tuesdays, it has to be put into the corresponding time slots on Thursdays.
*/

public class Constr
{
	// Set once, upon instantiation:
	private Course [] sectionList;
	private Slot [] slotList;
	private int pr_size;
	private int all_slots_size;
	
	// Change every time:
	private boolean valid;
	private int [] currentAssign;
	private int [] slotHas;
	
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
		
		int num_courses = Driver.courses.size();
		
		Driver.labs.forEach(l -> {
    		int idx = Driver.labs.indexOf(l);
			Course section = new Course("lab", idx);
			all_sections[idx + num_courses] = section;});
		
		Driver.course_slots.forEach(cs -> {
    		int idx = Driver.course_slots.indexOf(cs);
			Slot course_slot = new Slot("course", idx);
			all_slots[idx] = course_slot;});
		
		int num_course_slots = Driver.course_slots.size();
		
		Driver.lab_slots.forEach(ls -> {
			int idx = Driver.lab_slots.indexOf(ls);
			Slot lab_slot = new Slot("lab", idx);
			all_slots[idx + num_course_slots] = lab_slot;});
		
		sectionList = all_sections;
		slotList = all_slots;
	}
	
	public boolean evaluate (int [] assign)
	{
		currentAssign = assign;
		valid = true;
		slotHas = new int[all_slots_size];
		
		//System.out.println("TOP: "+Arrays.toString(currentAssign));
		
		if (valid == true)
			evening();
		
		if (valid == true){
			//System.out.println("Evening passed.");
			max();
		}
		
		if (valid == true){
			//System.out.println("Max passed.");
			unwanted();
		}
			
		if (valid == true){
			//System.out.println("Unwanted passed.");
			incompatible();
		}
					
		//if (valid == true)
			//System.out.println("Incompatible passed.");
		
		return valid;
	}
	
	/**
	* Method returns the time slot based on desired assignment index
	* @param int index, the index of course assign 
	* @return List<String> the time slot
	*/
	private List<String> getTimeSlot(int index)
	{
		int firstLab = Driver.courses.size();
		
		if (index < firstLab)
			return Driver.course_slots.get(currentAssign[index]);
		else
			return Driver.lab_slots.get(currentAssign[index]);
	}
	
	// Checks each course to see if it is an evening course
	// If so checks if it's in evening slot
	public void evening()
	{
		for (int i = 0; i < currentAssign.length; i++)
		{
			if (sectionList[i].getEvening() == true && slotList[i].getEvening() == false){
				valid = false;
				break;
			}
		}			
	}

	//First goes through all courses and adds to count of slot when that slot is used
	//Then compares slot counts versus slot max
	public void max()
	{
		for (int i = 0; i < currentAssign.length; i++)
		{
			int num_courses = Driver.courses.size();
			int offset = (i < num_courses) ? 0 : Driver.course_slots.size();
			if (currentAssign[i] != -99){
				if (i < num_courses)
					slotHas[currentAssign[i]]++;
				else
					slotHas[currentAssign[i] + offset]++;
			}
				
		}
		
		//System.out.println(Arrays.toString(slotList));
		
		for (int i = 0; i < slotList.length; i++)
		{
			//System.out.println(slotList[i].getMax() + " " + slotHas[i]);
			if (slotList[i].getMax() < slotHas[i]){
				//System.out.println(Arrays.toString(slotHas));
				//System.out.println("FAILING MAX, i=" + i);
				valid = false;
				break;
			}
		}
		//System.out.println(Arrays.toString(slotHas));
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
				if (sectionList[i].getUnwanted().get(j).equals(slotList[i].getName())){
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
	
	
	/**
	* Method totals the number of incompatibilities and unwanted preferences
	* It will then sort so that the first returned index is of the class that has the most incompatibilities and unwanted preferences
	* Note that an unwanted preference is worth 0.01% of an incompatible weight. This acts as a tie breaker when sorting.
	* This will only cause issues if there is a class that has over 10000 unwanted preference, and so is deemed legal for now
	* @param Parser parse, the parser used to read the file
	* @return float[] indexes, the sorted array of class indexes. The first value is the most constrained. This can be safely cast to an int without any issues.
	*/
	public static float[] getTightestBoundClass(){
		
		try{
			float incompatibleWeight = 1;
			float unwantedWeight = 0.0001f;
				
			ArrayList<ArrayList<List<String>>> not_compatible = Driver.not_compatible;
			ArrayList<ArrayList<List<String>>> unwanted = Driver.unwanted;
			
			int num_courses = Driver.courses.size();
			int num_labs = Driver.labs.size();
			
			//First we go through the array and find out which class has the most restraints numerically
			float[] restraintCount = new float[num_courses + num_labs];
			
			//The coursemap hashmap is essentially a lookup table to find out which index corresponds with what class
			HashMap<List<String>, Integer> courseMap = new HashMap<List<String>, Integer>();
			
			//We start by adding in all the courses to it
			for (int i = 0; i < num_courses; i++){
				courseMap.put(Driver.courses.get(i), i);
			}
			
			//Then add all the labs
			for (int i = 0; i < num_labs; i++){
				courseMap.put(Driver.labs.get(i), i);
			}
			
			//We are now ready to go through all of the incompatible classes, and find the most mentioned class
			for(int i = 0; i < not_compatible.size(); i++){
				
				for(int j = 0; j < not_compatible.get(i).size(); j++)
				{
					//When a class is incremented, use the hashmap to find its index, and increment its counter
					restraintCount[(int)courseMap.get(not_compatible.get(i).get(j))]+=incompatibleWeight;  
				}	
			}
				
			//We then go back through and include the soft preferences for unwanted, and use it as a half interval.
			//System.out.println("Unwanted is: " + unwanted);
			for(int i = 0; i < unwanted.size(); i++){
				//System.out.println("OUTER");
				
				//When a class is incremented, use the hashmap to find its index, and increment its counter
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
	
	/**
	* Method checks to see if two classes have any time overlap using the index of an a slot in currentAssign
	* @param assignIndex1, assignIndex2 are the indexes of the classes in currentAssign
	* @return true if there is no conflict, false if there is a time conflict
	*/
	public boolean checkCourseTimeConflict(int assignIndex1, int assignIndex2)
	{	
		if (currentAssign == null) throw new NullPointerException();
		List<String> time1 = getTimeSlot(assignIndex1);
		List<String> time2 = getTimeSlot(assignIndex2);
		
		//Check to see if they are on the same day
		if(time1.get(0).compareTo(time2.get(0)) != 0){
			return true;
		} 
		
		float classDuration;
		if (time1.get(0).compareTo("MO") == 0){
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
}
