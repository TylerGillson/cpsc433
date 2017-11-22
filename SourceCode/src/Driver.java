import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/* HARD CONSTRAINTS
1. No more than coursemax(s) courses can be assigned to slot s
2. No more than labmax(s) labs can be assigned to slot s
3. assign(ci) has to be unequal to assign(lik) for all k and i. (a course and its labs cannot be in the same slot)
4. not-compatible(a,b) means: assign(a) cannot equal assign(b) (where a,b in Courses + Labs) 
5. partassign: assign(a) must equal partassign(a) for all a in Courses + Labs with partassign(a) not equal to $
6. unwanted(a,s): assign(a) cannot equal s (with a in Courses + Labs and s in Slots)
7. All course sections with a section number starting LEC 9 are evening classes and have to be scheduled into evening slots (18:00 or later).
8. All 500-level course sections must be scheduled into different time slots.
9. No courses can be scheduled at Tuesdays 11:00-12:30.
	
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

public class Driver {
    public static Parser p;
    public static Generation generation;
	
    // Search constraints:
    public static int pop_init = 2;
	public static int pop_max  = 25;
	public static int gen_max  = 50;
	
	// Global data structures to be filled by the parser:
	public static ArrayList<List<String>> courses;
	public static ArrayList<List<String>> labs;
	public static ArrayList<List<String>> lab_slots;
	public static ArrayList<List<String>> course_slots;
	public static ArrayList<ArrayList<List<String>>> not_compatible;
	public static ArrayList<ArrayList<List<String>>> unwanted;
	public static ArrayList<ArrayList<List<String>>> preferences;
	public static ArrayList<ArrayList<List<String>>> pair;
	public static ArrayList<ArrayList<List<String>>> part_assign;
	
	
	//random stuff
	public static Eval eval;
	
	/**
	 * Parse an input file containing scheduling information.
	 * Generate and output an optimized schedule based on the information in the input file.
	 * @param args
	 */
	public static void main(String[] args) {
		String configfile;
		String filename;
    	// Deal with command line args:
		try {
			configfile = args[0];
			filename = args[1];

			//bulid parser
	    	p = new Parser(filename);
	    	p.build();
	    	
	    	generation = new Generation();
	    	
	    	// Initialize data structures:
	    	courses = p.getCourses();
	    	labs = p.getLabs();
	    	lab_slots = p.getLabSlots();
	    	course_slots = p.getCourseSlots();
	    	not_compatible = p.getNotCompatible();
	    	unwanted = p.getUnwanted();
	    	preferences = p.getPreferences();
	    	pair = p.getPair();
	    	part_assign = p.getPartAssign();
	    	
	    	eval = new Eval(configfile);

		}
		catch(Exception e) {
			System.out.println("Invalid argument: try \t java Driver [configFile] [inputfile]");
			System.exit(0);
		}
    	// Check for / deal with CPSC 313 & CPSC 413:
    	//System.out.println("lab_slots pre: "+lab_slots);
    	manage313413("313");
    	manage313413("413");
    	//System.out.println("lab_slots post: "+lab_slots);
    	
    	// Initialize the first generation of candidate solutions:
    	initGeneration0();
    	
    	// Run GA for specified # of generations:
    	for (int i=0; i<gen_max; i++){
    		generation.evolve();
    	}
    
    	// Sort the final generation according to our fitness function and select the optimal solution:
    	int[] solution = sortLastGen();
    	    	
    	// Print final generation:
    	generation.print();
    	System.out.print("\n");
    	
    	// Print final output schedule:
    	printSchedule(solution);
    	eval.getValue(solution);
	}
    
	public static int eval(int[] sol){
		return eval.getValue(sol);
	}
	
	/**
	 * "Output Converter" to print a textual schedule based on a pr instance.
	 * @param sol - The completed pr instance to be converted.
	 */
	public static void printSchedule(int[] sol){
		String output = "Eval-value: " + String.valueOf(eval(sol)) + "\n";
		String line;
		String day;
		String time;
		
		// Iterate through sol's entries and re-construct section, day, and time info:
		for (int i=0; i<sol.length-1; i++){
			int slot_idx = sol[i];
			String section = "";
			List<String> section_elem;
			
			// The i-th sol-entry is a course:
			if (i < courses.size()){
				section_elem = courses.get(i);
				for (int j=0; j<section_elem.size(); j++){
					section = section + section_elem.get(j) + " ";
				}
				section = section.trim();
				day = course_slots.get(slot_idx).get(0);
				time = course_slots.get(slot_idx).get(1);
			}
			// The i-th sol-entry is a lab:
			else {
				section_elem = labs.get(i-courses.size());
				for (int j=0; j<section_elem.size(); j++){
					section = section + section_elem.get(j) + " ";
				}
				section = section.trim();
				day = lab_slots.get(slot_idx).get(0);
				time = lab_slots.get(slot_idx).get(1);
			}
			// Compute white space and construct a line:
			String tabs = (section_elem.size() == 4) ? "\t\t\t" : "\t\t";
			line = section + tabs + ": " + day + ", " + time;
			output = output + line + "\n";
		}
		System.out.println(output);
	}
	
	/**
	 * Initializes the first generation of candidate solutions.
	 * If partial assignments exist, create a base pr instance that satisfies them.
	 * Using either the latter instance (or an empty one) as a starting point,
	 * conduct or-tree-based searches to create pop_init candidates.
	 */
	public static void initGeneration0(){
		// Determine size of problem data structure:
		int pr_size = courses.size() + labs.size();
		
		// Initialize an OrTree instance based on partial assignments (or not):
		OrTree<int[]> oTree;
		if (!part_assign.isEmpty()){
			int[] pr = build_pr(pr_size);
			oTree = new OrTree<int[]>(pr);
		}
		else {
			oTree = new OrTree<int[]>(pr_size);
		}
		
		// Build the first generation of candidate solutions:
    	for (int i=0; i<pop_init; i++) {	
        	int[] candidate = new int[pr_size];
        	OrTree<int[]> t = new OrTree<int[]>(oTree.data);
        	// Perform an or-tree-based search to build a solution candidate:
        	candidate = t.buildCandidate();
        	generation.add(candidate);
        	
        	// TESTING:
        	//t.printTree(true);
    	}
	}
	
	/**
	 * Sort the final generation of solutions according to the eval function
	 * and return the most optimal solution.
	 * @return sol - The most optimal solution from the last generation.
	 */
	public static int[] sortLastGen(){
		List<int[]> lastGen = generation.getGeneration();
		Collections.sort(lastGen, new Comparator<int[]>() {
	        public int compare(int[] sol1, int[] sol2){
	            if (eval(sol1) == eval(sol2))
	            	return 0;
	            else if (eval(sol1) > eval(sol2))
	            	return 1;
	            else
	            	return -1;
	        }
	    });
		int[] sol = lastGen.get(0); 
		return sol;
	}
	
	/**
	 * Check for & deal with CPSC 313 / CPSC 413 as follows:
	 * 1. Add CPSC 813/913 to courses.
	 * 2. Add "TU, 18:00, 2, 1" to lab slots.
	 * 3. Generate list of all CPSC 313 labs & course sections & courses that cannot overlap w/ them.
	 * 4. Add unwanted(a,s) to unwanted for all a in above list, for: "MON, 18:00", "TU 17:00", and "TU 18:30".
	 */
	public static void manage313413(String course){
		// Begin generating list of all CPSC 313/413-related sections:
		ArrayList<List<String>> sections = new ArrayList<List<String>>();
		
		// Add each CPSC 313-related course to sections:
		courses.forEach(c -> {
			if (c.get(0).equals("CPSC") && c.get(1).equals(course))
				sections.add(c);});
		
		// If there are CPSC-related courses:
		if (!sections.isEmpty()){
			// Add CPSC 813/913 to courses:
			if (course.equals("313")){
				List<String> cpsc813 = Arrays.asList("CPSC", "813", "", ""); 
				courses.add(cpsc813);
			} else if (course.equals("413")){
				List<String> cpsc913 = Arrays.asList("CPSC", "913", "", ""); 
				courses.add(cpsc913);
			}
			
			// Add CPSC 813/913's time slot to lab_slots if it is not already there.
			// If it is, increment its lab_max and lab_min values by 1.
			boolean inLabSlots = false;
			Iterator<List<String>> labSlots = lab_slots.iterator();
			while (labSlots.hasNext()){
				List<String> slot = labSlots.next();
				if (slot.get(0).equals("TU") && slot.get(1).equals("18:00")){
					slot.set(2, String.valueOf((Integer.parseInt(slot.get(2)) + 1)));
					slot.set(3, String.valueOf((Integer.parseInt(slot.get(3)) + 1)));
					inLabSlots = true;
				}
			}
			if (inLabSlots == false){
				List<String> cpsc813913Slot = Arrays.asList("TU", "18:00", "1", "1"); 
				lab_slots.add(cpsc813913Slot);
			}
			
			// Add each CPSC 313/413-related lab to sections:
			labs.forEach(l -> {
	    		if (l.get(0).equals("CPSC") && l.get(1).equals(course))
	    			sections.add(l);});
			
			// Deal with "courses that can't overlap with CPSC 313/413"...
			// WHAT DOES THIS MEAN???
			
			// Add unwanted(a,s) statements for members of sections:
			
			//System.out.println("unwanted init: "+unwanted);
			sections.forEach(s -> {
				addUnwanted(s, "MO", "18:00");
				addUnwanted(s, "TU", "17:00");
				addUnwanted(s, "TU", "18:30");
			});
			//System.out.println("unwanted post: "+unwanted);
		}
	}
	
	/**
	 * An a new entry to the 'unwanted' data structure.
	 * @param section
	 * @param day
	 * @param time
	 */
	public static void addUnwanted(List<String> section, String day, String time){
		ArrayList<List<String>> new_unwanted = new ArrayList<List<String>>();
		new_unwanted.add(section);
		new_unwanted.add(Arrays.asList(day));
		new_unwanted.add(Arrays.asList(time));
		unwanted.add(new_unwanted);
	}
	
	/**
	 * Generate pr instances according to presence of partial assignments, or not.
	 * @param pr_size
	 * @return pr - Problem instance for or-tree-based search.
	 */
    public static int[] build_pr(int pr_size){
    	int[] pr = new int[pr_size];
		Arrays.fill(pr, -99);
    	
		// Iterate over each partial assignment that was in the input file:
		Iterator<ArrayList<List<String>>> partAssigns = part_assign.iterator(); 
		while (partAssigns.hasNext()) {
			ArrayList<List<String>> assign = partAssigns.next();
			    			
			int pr_idx = -99;
			int slot_idx = -99;
			int course_idx = courses.indexOf(assign.get(0));
			
			// The partial assignment refers to a course.
			if (course_idx != -1) {
				pr_idx = course_idx;
				
				// Iterate through course slots to find the index of the correct time slot:
				Iterator<List<String>> slots = course_slots.iterator();
				while (slots.hasNext()){
					List<String> slot = slots.next();
					if (slot.get(0).equals(assign.get(1).get(0)) && slot.get(1).equals(assign.get(2).get(0))){
						slot_idx = course_slots.indexOf(slot);
					}
				}
			}
			// The partial assignment refers to a lab.
			else if (labs.indexOf(assign.get(0)) != -1) {
				pr_idx = courses.size() + labs.indexOf(assign.get(0));
				
				// Iterate through the lab slots to find the index of the correct time slot:
				Iterator<List<String>> slots = lab_slots.iterator();
				while (slots.hasNext()){
					List<String> slot = slots.next();
					if (slot.get(0).equals(assign.get(1).get(0)) && slot.get(1).equals(assign.get(2).get(0))){
						slot_idx = lab_slots.indexOf(slot);
					}
				}
			}
			// The partial assignment refers to a course/lab not in the input.
			else
				throw new java.lang.Error("This course/lab was not in the input!");
			
			// Update pr to ensure that the course/lab is assigned the specified time slot:
			pr[pr_idx] = slot_idx;
		}
		return pr;
	}
}
