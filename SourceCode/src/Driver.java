import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Driver {
    public static Parser p;
    public static Generation generation;
	
    // Search constraints:
    public static int pop_init = 10;
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
	public static int[] pr;
	
	// Assign Checking Objects:
	public static Constr constr;
	public static Eval eval;
	
	/**
	 * Parse an input file containing scheduling information.
	 * Generate and output an optimized schedule based on the information in the input file.
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile;
		String filename;
    	
		// Deal with command line args:
		try {
			configFile = args[0];
			filename = args[1];

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
	    	
	    	// Initialize eval:
	    	eval = new Eval(configFile);
		}
		catch(Exception e) {
			System.out.println("Invalid argument: try \t java Driver [configFile] [inputFile]");
			System.exit(0);
		}
		
		// Check for Tue 11:00 - 12:30 Course Slot:
		checkForTU11();
		
    	// Check for / deal with CPSC 313 & CPSC 413:
    	manage313413("313");	
    	manage313413("413");
    	
    	// Initialize Constr AFTER dealing w/ CPSC 313/413:
    	constr = new Constr();
    	
    	// Initialize the first generation of candidate solutions:
    	initGeneration0();
    	System.out.println();
    	
    	// Run GA for specified # of generations:
    	for (int i=0; i<gen_max; i++){
    		generation.evolve();
    	}
    
    	// Sort the final generation according to our fitness function and select the optimal solution:
    	int[] solution = sortLastGen();
    	    	
    	// Print final generation:
    	generation.print();
    	System.out.print("\n");
    	
    	// Print final solution + output schedule:
    	System.out.println("Final Solution:" + "\n" + Arrays.toString(solution) + "\n");
    	printSchedule(solution);
	}
    	
	/**
	 * "Output Converter" to print a textual schedule based on a pr instance.
	 * @param sol - The completed pr instance to be converted.
	 */
	public static void printSchedule(int[] sol){
		String output = "Eval-value: " + String.valueOf(eval.getValue(sol)) + "\n";
		String line;
		String day;
		String time;
		
		// Iterate through sol's entries and re-construct section, day, and time info:
		for (int i=0; i<sol.length; i++){

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
		
		// Instantiate pr
		pr = new int[pr_size];
		Arrays.fill(pr, -99);
		
		// Initialize an OrTree instance based on partial assignments (or not):
		OrTree<int[]> oTree;
		if (!part_assign.isEmpty()){
			// build_pr initializes the global variable, pr, according to partial assignments.
			build_pr(pr_size);
			oTree = new OrTree<int[]>(pr);
		}
		else {
			oTree = new OrTree<int[]>(pr_size);
		}
		
		// Build the first generation of candidate solutions:
    	for (int i=0; i<pop_init; i++) {	
        	int[] candidate = new int[pr_size];
        	OrTree<int[]> t = new OrTree<int[]>(oTree.getData());
        	
        	// Perform an or-tree-based search to build a solution candidate:
        	ArrayList<Integer> mostTightlyBound = getMTBCopy();
        	candidate = t.buildCandidate(mostTightlyBound);
        	
        	System.out.println("CANDIDATE " + i + ": " + Arrays.toString(candidate));
        	generation.add(candidate);
    	}
	}
	
	public static ArrayList<Integer> getMTBCopy(){
		int[] mTB = constr.getMostTightlyBoundIndices().clone();
		ArrayList<Integer> mostTightlyBound = new ArrayList<Integer>(); 
		for (int idx : mTB)
			mostTightlyBound.add(idx);
		return mostTightlyBound;
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
	        	int e1 = eval.getValue(sol1);
	        	int e2 = eval.getValue(sol2);
	        	
	        	if (e1 == e2)
	            	return 0;
	            else if (e1 > e2)
	            	return 1;
	            else
	            	return -1;
	        }
	    });
		int[] sol = lastGen.get(0); 
		return sol;
	}
	
	public static void checkTU18Exists(){
		boolean tu18Exists = false;
		for (int i=0; i < lab_slots.size(); i++){
			List<String> ls = lab_slots.get(i);
			if (ls.get(0).equals("TU") && ls.get(1).equals("18:00")){
				tu18Exists = true;
				return;
			}
		}
		if (!tu18Exists)
			throw new java.lang.Error("CPSC 313/413 was included in Courses, but the correct Lab Slot does not exist!");
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
		
		// Add each CPSC 313/413-related course to sections:
		courses.forEach(c -> {
			if (c.get(0).equals("CPSC") && c.get(1).equals(course))
				sections.add(c);});
		
		// If there are CPSC-related courses:
		if (!sections.isEmpty()){
			
			// Do this only once:
			if (course.equals("313"))
				checkTU18Exists();
			
			// Add a partial assignment for CPSC 813/913:
			ArrayList<List<String>> lab_part_assign = new ArrayList<List<String>>();
			String num = (course.equals("313")) ? "813" : "913";
			List<String> cL = Arrays.asList("CPSC", num, "", "");
			lab_part_assign.add(cL);
			cL = new ArrayList<String>();
			cL.add("TU");
			lab_part_assign.add(cL);
			cL = new ArrayList<String>();
			cL.add("18:00");
			lab_part_assign.add(cL);
			part_assign.add(lab_part_assign);
			
			// Add CPSC 813/913 to labs:
			if (course.equals("313")){
				List<String> cpsc813 = Arrays.asList("CPSC", "813", "", ""); 
				labs.add(cpsc813);
			} else if (course.equals("413")){
				List<String> cpsc913 = Arrays.asList("CPSC", "913", "", ""); 
				labs.add(cpsc913);
			}
						
			// Add unwanted(a,s) statements for members of sections:
			sections.forEach(s -> {
				addUnwanted(s, "MO", "18:00");
				addUnwanted(s, "TU", "17:00");
				addUnwanted(s, "TU", "18:30");
			});
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
	 * If the input file contains the following invalid course slot: Tue 11:00 - 12:30,
	 * It is deleted from course_slots.
	 */
	public static void checkForTU11(){
		int delete = -99;
		Iterator<List<String>> courseSlots = course_slots.iterator(); 
		while (courseSlots.hasNext()) {
			List<String> cs = courseSlots.next();
			if (cs.get(0).equals("TU") && cs.get(1).equals("11:00"))
				delete = course_slots.indexOf(cs);
		}
		if (delete != -99)
			course_slots.remove(delete);
	}
	
	/**
	 * Generate pr instances according to presence of partial assignments, or not.
	 * @param pr_size
	 * @return pr - Problem instance for or-tree-based search.
	 */
    public static void build_pr(int pr_size){
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
				if (slot_idx == -99)
					throw new java.lang.Error("The partial assignment for this course indicated an invalid slot!");
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
				if (slot_idx == -99)
					throw new java.lang.Error("The partial assignment for this lab indicated an invalid slot!");
			}
			// The partial assignment refers to a course/lab not in the input.
			else {
				throw new java.lang.Error("This course/lab was not in the input!");
			}
				
			// Update pr to ensure that the course/lab is assigned the specified time slot:
			pr[pr_idx] = slot_idx;
		}
	}
}
