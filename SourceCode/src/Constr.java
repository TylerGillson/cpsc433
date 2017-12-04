import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/* HARD CONSTRAINTS
1. No more than coursemax(s) courses can be assigned to slot s
	CHECK
	
2. No more than labmax(s) labs can be assigned to slot s
	CHECK
	
3. assign(ci) has to be unequal to assign(lik) for all k and i. (a course and its labs cannot be in the same slot)
	???
	
4. not-compatible(a,b) means: assign(a) cannot equal assign(b) (where a,b in Courses + Labs)
	CHECK --- Current solution highly inefficient.
	
5. partassign: assign(a) must equal partassign(a) for all a in Courses + Labs with partassign(a) not equal to $
	CHECK
	
6. unwanted(a,s): assign(a) cannot equal s (with a in Courses + Labs and s in Slots)
	CHECK
	
7. All course sections with a section number starting LEC 9 are evening classes and have to be scheduled into evening slots (18:00 or later).
	CHECK
	
8. All 500-level course sections must be scheduled into different time slots.
	CHECK
	
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
	
	private boolean debugToggle = false;
	
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
		
		if (valid == true)
			evening();
		
		if (valid == true){
			if (debugToggle) System.out.println("Evening passed.");
			max();
		}
		
		if (valid == true){
			if (debugToggle) System.out.println("Max passed.");
			unwanted();
		}
			
		if (valid == true){
			if (debugToggle) System.out.println("Unwanted passed.");
			incompatible();
		}
					
		if (valid == true){
			if (debugToggle) System.out.println("Incompatible passed.");
			check500();
		}
		
		if (valid == true){
			if (debugToggle) System.out.println("Check 500-level passed.");
			
		if (valid == true){
			if (debugToggle) System.out.println("Check 500-level passed.");
			checkCourseLabConflicts();
			if(!valid) {
				Driver.printSchedule(currentAssign);	
				/*if(debugToggle) {
					Scanner sc = new Scanner(System.in);
					debug("Check to see if conflict ok?");
					sc.next();
				*/
			}	
			//checkCourseLabOverlap();
		}
		if(valid == true)
			if(debugToggle) System.out.println("Check course-lab conflict passed");
		}
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
		if (debugToggle) System.out.println(Arrays.toString(slotList));
		
		for (int i = 0; i < slotList.length; i++)
		{
			if (slotList[i].getMax() < slotHas[i]){
				if (debugToggle) System.out.println(Arrays.toString(slotHas));
				if (debugToggle) System.out.println("FAILING MAX, i=" + i);
				
				valid = false;
				break;
			}
		}
		if (debugToggle) System.out.println(Arrays.toString(slotHas));
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
	
	
	//Checks if 500 level courses are in the same slot
	//Adds slot index of 500 level courses to an ArrayList
	//Converts ArrayList to Set and compares the lengths to see if there were duplicates in the ArrayList
	public void check500()
	{
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < currentAssign.length; i++) {
			if (sectionList[i].getName().get(1).charAt(0) == '5')
				indices.add(i);
		}
		
		Set<Integer> indexSet = new HashSet<Integer>(indices);
		if (indices.size() != indexSet.size())
			valid = false;
	}
	
	//Returns an array of indices corresponding to the most tightly bound courses
	public int[] getMostTightlyBoundIndices()
	{
		// Convert sectionList from an array of courses into a list of courses:
		List<Course> sortedCourses = new ArrayList<Course>();
		for (int i=0; i < sectionList.length; i++)
			sortedCourses.add(sectionList[i]);
		
		// Sort the resulting list according to number of constraints:
		Collections.sort(sortedCourses, new Comparator<Course>() {
	        public int compare(Course c1, Course c2){
	        	int constrVal1 = c1.numOfConstraints();
	        	int constrVal2 = c2.numOfConstraints();
	        	
	        	if (constrVal1 == constrVal2)
	            	return 0;
	            else if (constrVal1 < constrVal2)
	            	return 1;
	            else
	            	return -1;
	        }
	    });
		
		// Generate an array containing the sorted indices:
		int[] sortedIndices = new int[sectionList.length];
		for (int i=0; i < sortedCourses.size(); i++)
			sortedIndices[i] = sortedCourses.get(i).getIndex();
		
		return sortedIndices;
	}
	/**
	 * use to check if the class assigned is incompatible
	 */
	/*
	public void incompatible() {
		
		//get all pair of incompatible
		for (int i = 0; i<Driver.not_compatible.size(); i++) {
			ArrayList<List<String>> badPair = Driver.not_compatible.get(i);
			
			//get the assign of left 
			List<String> left = badPair.get(0);
			int leftIndex;
			if (left.contains("TUT") || left.contains("LAB")) 
				leftIndex = Driver.labs.indexOf(left)+Driver.courses.size();
			else
				leftIndex = Driver.courses.indexOf(left);
			
			//get the assign of right
			List<String> right= badPair.get(1);
			int rightIndex;
			if (right.contains("TUT") || right.contains("LAB")) 
				rightIndex = Driver.labs.indexOf(right)+Driver.courses.size();
			else
				rightIndex = Driver.courses.indexOf(right);
			
			//use check if they have time conflict
			if (checkCourseTimeConflict(leftIndex,rightIndex) == false) {
				valid = false;
				return;
			}
			
		}
		
		
		
		
	}
	*/
	public void incompatible()
	{
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("H:mm");

		
		//get all pair of incompatible
		for (int i = 0; i<Driver.not_compatible.size(); i++) {
			ArrayList<List<String>> badPair = Driver.not_compatible.get(i);
			
			//get the assign of left 
			List<String> left = badPair.get(0);
			int leftIndex;
			if (left.contains("TUT") || left.contains("LAB")) 
				leftIndex = Driver.labs.indexOf(left)+Driver.courses.size();
			else
				leftIndex = Driver.courses.indexOf(left);
			int leftAssign = currentAssign[leftIndex];
			//if unassign go next
			if (leftAssign == -99 )
				continue;			
			//if they are assigned,get their time
			List<String> leftTime;
			String leftType = "LEC";
			if (left.contains("TUT") || left.contains("LAB")) {
				leftTime = Driver.lab_slots.get(leftAssign);
				leftType ="TUT";
			}
			else
				leftTime = Driver.course_slots.get(leftAssign);
			
			
			

			//get the assign of right
			List<String> right= badPair.get(1);
			int rightIndex;
			if (right.contains("TUT") || right.contains("LAB")) 
				rightIndex = Driver.labs.indexOf(right)+Driver.courses.size();
			else
				rightIndex = Driver.courses.indexOf(right);
			int rightAssign = currentAssign[rightIndex];
			//if unassign go next 
			if (rightAssign == -99 )
				continue;	
			//if they are assigned,get their time
			List<String> rightTime;
			String rightType = "LEC";
			if (right.contains("TUT") || right.contains("LAB")) {
				rightTime = Driver.lab_slots.get(rightAssign);
				rightType = "TUT";
			}
			else
				rightTime = Driver.course_slots.get(rightAssign);
			
			//compare the 2 times
			if(leftTime.get(0).equals("MO") && rightTime.get(0).equals("MO")) {
				//if both on Monday and start at same time
				if (leftTime.get(1).equals(rightTime.get(1))) {
					valid = false;
					return;
				}
			}
			//if one is on Tuesday other is on Monday
			else if (leftTime.get(0).equals("MO") && rightTime.get(0).equals("TU")){
				continue;
			}
			else if (leftTime.get(0).equals( "TU") && rightTime.get(0).equals("MO")){
				continue;
			}
			//if both on Tuesday
			else if (leftTime.get(0).equals("TU") && rightTime.get(0).equals("TU")){
				if (leftType.equals(rightType)) {
					if (leftTime.get(1).equals(rightTime.get(1))) {
						valid = false;
						return;
					}
				}
				else {
					//check for overlapping time LEC slot is 1h30m long
					if (leftType.equals("LEC")) {
						//get left start and end
						LocalTime leftStartTime=  LocalTime.parse(leftTime.get(1),dateTimeFormatter);
						LocalTime leftEndTime = leftStartTime.plusMinutes(90);
						
						//check if right starts within the range of left time slot
						LocalTime rightStartTime=  LocalTime.parse(rightTime.get(1),dateTimeFormatter);
						if (rightStartTime.isAfter(leftStartTime) && rightStartTime.isBefore(leftEndTime)) {
							valid = false;
							return;
						}
						
					}
					else {
						//get right start and end
						LocalTime rightStartTime=  LocalTime.parse(rightTime.get(1),dateTimeFormatter);
						LocalTime rightEndTime = rightStartTime.plusMinutes(90);
						//check if right starts within the range of left time slot
						LocalTime leftStartTime =  LocalTime.parse(leftTime.get(1),dateTimeFormatter);
						if (leftStartTime.isAfter(rightStartTime) && leftStartTime.isBefore(rightEndTime)) {
							valid = false;
							return;
						}
					}		
				}
			}

			//if left are Fridays labs
			else if(leftTime.get(0).equals("FR")) {
				//if both are friday lab and start at same time
				if (rightTime.get(0).equals("FR") && rightTime.get(1).equals(leftTime.get(1))) {
					valid = false;
					return;
				}
				//else, right is a non friday but a lec on mo
				else if(rightType.equals("LEC") && rightTime.get(0).equals("MO")) {
					LocalTime leftStartTime=  LocalTime.parse(leftTime.get(1),dateTimeFormatter);
					LocalTime leftEndTime = leftStartTime.plusMinutes(120);	
					LocalTime rightStartTime=  LocalTime.parse(rightTime.get(1),dateTimeFormatter);
					if (rightStartTime.isAfter(leftStartTime) && rightStartTime.isBefore(leftEndTime)) {
						valid = false;
						return;
					}
					
				}
			
		
			}	
			
		}
		valid= true;
	}
				
	
	/**
	* Method checks to see if two classes have any time overlap using the index of an a slot in currentAssign
	* @param assignIndex1, assignIndex2 are the indexes of the classes in currentAssign
	* @return true if there is no conflict, false if there is a time conflict
	*/
	public boolean checkCourseTimeConflict(int assignIndex1, int assignIndex2)
	{	
		
	
		debug("______________________________________");
		debug("In checkCourseTimeConflict: ");
		debug("Index1 = " + assignIndex1 + " which is " + currentAssign[assignIndex1]);
		debug("Index2 = " + assignIndex2 + " which is " + currentAssign[assignIndex2]);
		if(assignIndex1 == assignIndex2) throw new IllegalArgumentException("Indexes cannot be equal!");
		
		if((currentAssign[assignIndex1] == -99) ||(currentAssign[assignIndex2] == -99))
			return true;
	
		if (currentAssign == null) throw new NullPointerException();
		
		List<String> time1 = getTimeSlot(assignIndex1);
		List<String> time2 = getTimeSlot(assignIndex2);
		
		debug("Time1 is: " + time1);
		debug("Time2 is: " + time2);
		
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
			time1start = (digit1 - 48)* 100;
		else
			time1start = (digit1 -48)*1000 + (digit2 -48)*100;
		
		//Do the same for the other time
		float time2start;
		digit1 = time2.get(1).charAt(0);
		digit2 = time2.get(1).charAt(1);
		
		if (digit2 == ':')
			time2start = (digit1 - 48)* 100;
		else
			time2start = (digit1 -48)*1000 + (digit2 -48)*100;
		
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
	
	private void debug(String val){
		if(debugToggle) System.out.println(val);
	}
	
	public void checkCourseLabConflicts(){
		
		boolean safe = true;
		ArrayList<List<String>> mainList = (ArrayList<List<String>>)Driver.courses.clone();
		
		
		for(int i =0; i < Driver.labs.size(); i++){
			mainList.add(Driver.labs.get(i));
		}
		
		ArrayList<List<String>> labList;
		debug("The contents of sectionList is: ");
		for(int i = 0; i <sectionList.length; i++){
			debug(i + " = " + sectionList[i].getName());
			
		}
		for (int i = 0; i < sectionList.length - Driver.labs.size(); i++){
			debug("Checking course: " + sectionList[i].getName());
			
				
				labList  = sectionList[i].getLabList();
				debug("With labs: ");
				if(labList == null) debug("Lab list is null.");
				for(int k = 0; k < labList.size(); k++){
					if(labList.get(k) == null) debug("Lab = null");
					debug("Lab " + k + ": " + labList.get(k));
				}
				for (int j = 0; j < labList.size(); j++){
					debug("    Checking lab: " + labList.get(j));
					
					safe = checkCourseTimeConflict(mainList.indexOf(sectionList[i].getName()),mainList.indexOf(labList.get(j)));
					if(!safe){
						valid = false;
						debug("Failed due to : " + sectionList[i].getName() + ", " + labList.get(j) );
						
						return;
					}						
				}
				safe = checkLabLabconflict(labList, mainList);
				if(!safe){
					valid = false;
					debug("Failed due to lab v lab conflict");
					return;
				}
		}			
		
		return;
	}
	
	private boolean checkLabLabconflict(ArrayList<List<String>> labList, ArrayList<List<String>> mainList){
		if (labList.size() < 2)
			return true;
		
		if (labList.size() == 2)
			return checkCourseTimeConflict(mainList.indexOf(labList.get(0)), mainList.indexOf(labList.get(1)));

		//We know that there is at least 3 labs, or more
		//We check that none of these conflict with the first lab in the list
		boolean safe = true;
		for (int i = 1; i < labList.size(); i++){
			safe = checkCourseTimeConflict(mainList.indexOf(labList.get(0)), mainList.indexOf(labList.get(i)));
			if(!safe) return false;
		}
		labList.remove(0); //Remove the first element to reduce size
		return checkLabLabconflict(labList, mainList);
	}
}
