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
			checkCourseLabConflicts();
		}
			
		if (valid == true)
			if (debugToggle) System.out.println("Check course-lab conflict passed");
		
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
	public void incompatible()
	{
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
			
			
			
			//if they are assigned,get their type
			String leftType = "LEC";
			if (left.contains("TUT") || left.contains("LAB")) 
				leftType ="TUT";

			//if they are assigned,get their type
			String rightType = "LEC";
			if (right.contains("TUT") || right.contains("LAB")) 
				rightType = "TUT";
		
			//if LEC/LEC
			if (leftType.equals("LEC") && rightType.equals("LEC"))
				valid = LLCheck(leftAssign,rightAssign);
			//if TUT/TUT
			else if (leftType.equals("TUT") && rightType.equals("TUT"))
				valid = TTCheck(leftAssign,rightAssign);
			else {
				if (leftType.equals("LEC"))
					valid = LTCheck(leftAssign,rightAssign);
				else 
					valid = LTCheck(rightAssign,leftAssign);
			}
			if (valid == false)
				return;	
		}
		valid = true;
	}
	
	
	private boolean LLCheck(int lec1TimeSlot, int lec2TimeSlot) {
		List<String> ts1Info = Driver.course_slots.get(lec1TimeSlot); 
		List<String> ts2Info = Driver.course_slots.get(lec2TimeSlot); 
		//if same day same time return false
		if (ts1Info.get(0).equals(ts2Info.get(0)) && ts1Info.get(1).equals(ts2Info.get(1)))
			return false;		
		return true;
	}
	
	private boolean TTCheck(int tut1TimeSlot, int tut2TimeSlot) {
		List<String> ts1Info = Driver.lab_slots.get(tut1TimeSlot); 
		List<String> ts2Info = Driver.lab_slots.get(tut2TimeSlot); 
		//if same day same time return false
		if (ts1Info.get(0).equals(ts2Info.get(0)) && ts1Info.get(1).equals(ts2Info.get(1)))
			return false;		
		return true;
	}
	
	private boolean LTCheck(int lecTimeSlot, int tutTimeSlot) {
		//get the time slot info 
		List<String> ts1Info = Driver.course_slots.get(lecTimeSlot); 
		List<String> ts2Info = Driver.lab_slots.get(tutTimeSlot); 
		//init lec day time and tut day time
		String lecDay = ts1Info.get(0);
		String tutDay = ts2Info.get(0);
		
		String lecTime = ts1Info.get(1);
		String tutTime = ts2Info.get(1);
		
		//init some useful items
		DateTimeFormatter DTF = DateTimeFormatter.ofPattern("H:mm");
		
		//both on monday compare time
		if (lecDay.equals("MO") && tutDay.equals("MO")) {
			if (lecTime.equals(tutTime)) 
				return false;	
		}

		//both on tuesday compare overlaps
		else if (lecDay.equals("TU") && tutDay.equals("TU")) {
			LocalTime lecStartTime = LocalTime.parse(lecTime, DTF);
			LocalTime lecEndTime = lecStartTime.plusMinutes(90);
			
			LocalTime tutStartTime = LocalTime.parse(tutTime, DTF);
			LocalTime tutEndTime = tutStartTime.plusMinutes(60);
			//if lec start in duration of tut
			if (lecStartTime.isAfter(tutStartTime) && lecStartTime.isBefore(tutEndTime))
				return false;
			//if tut start in duration of lec
			else if (tutStartTime.isAfter(lecStartTime) && tutStartTime.isBefore(lecEndTime))
				return false;
		}
		//if lab is FR and lec is on monday
		else if (lecDay.equals("MO") && tutDay.equals("FR")) {
			LocalTime lecStartTime=  LocalTime.parse(lecTime, DTF);
			LocalTime lecEndTime = lecStartTime.plusMinutes(60);
			
			LocalTime tutStartTime = LocalTime.parse(tutTime,DTF);
			LocalTime tutEndTime = tutStartTime.plusMinutes(120);
			//if lec start in duration of tut
			if (lecStartTime.isAfter(tutStartTime) && lecStartTime.isBefore(tutEndTime))
				return false;
			//if tut start in duration of lec
			else if(tutStartTime.isAfter(lecStartTime) && tutStartTime.isBefore(lecEndTime))
				return false;
		}
		
		//on diff days
		return true;
	}
	
	public void checkCourseLabConflicts() {
		int num_courses = Driver.courses.size();
		
		for (int i = 0; i < num_courses; i++) {
			ArrayList<List<String>> labs = sectionList[i].getLabList();
			
			if (currentAssign[i] == -99)
				continue;
			
			for (int j = 0; j < labs.size(); j++) {
				int labIndex = Driver.labs.indexOf(labs.get(j));
				int labSlotIndex = currentAssign[labIndex + num_courses];
				
				if (labSlotIndex == -99)
					continue;
				
				if (!LTCheck(currentAssign[i], labSlotIndex)) {
					valid = false;
					break;
				}
			}	
		}
	}
	
}
