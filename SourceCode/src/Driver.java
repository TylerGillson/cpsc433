import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Driver {
    public static Parser p;
    public static Generation generation;
	public static int pop_init = 25;
	public static int pop_max  = 25;
	public static int gen_max  = 50;
	
	public static void main(String[] args) {
    	String filename = args[0];
    	p = new Parser(filename);
    	p.build();
    	generation = new Generation();
    	
    	// Determine size of problem data structure:
    	int pr_size = p.getCourses().size() + p.getLabs().size();
    	
    	// Build the first generation of candidate solutions:
    	for (int i=0; i<pop_init; i++) {
    		// Initialize an OrTree instance based on partial assignments (or not):
        	OrTree<int[]> oTree;
        	if (!p.getPartAssign().isEmpty()){
        		int[] pr = build_pr(pr_size);
        		oTree = new OrTree<int[]>(pr);
        		System.out.println(oTree.toString());
        	}
        	else {
        		oTree = new OrTree<int[]>(pr_size);
        		System.out.println(oTree.toString());
        	}
        	// Perform an or-tree-based search to build a solution candidate:
        	int[] candidate;
        	candidate = oTree.buildCandidate();
        	generation.add(candidate);
    	}
    	
    	// Run GA for specified # of generations:
    	for (int i=0; i<gen_max; i++){
    		generation.evolve();
    	}
    
    	// Sort the final generation according to our fitness function:
    	List<int[]> lastGen = generation.getGeneration();
    	Collections.sort(lastGen, new Comparator<int[]>() {
            public int compare(int[] sol1, int[] sol2){
                if (fitness(sol1) == fitness(sol2))
                	return 0;
                else if (fitness(sol1) > fitness(sol2))
                	return 1;
                else
                	return -1;
            }
        });
    	
    	// Print final output schedule:
    	printSchedule(lastGen.get(0));
    }
    
	public static int fitness(int[] sol){
		return 0;
	}
	
	public static void printSchedule(int[] sol){
		System.out.println(Arrays.toString(sol));
	}
	
    public static int[] build_pr(int pr_size){
    	int[] pr = new int[pr_size];
		Arrays.fill(pr, -99);
    	
    	ArrayList<List<String>> courses = p.getCourses();
		ArrayList<List<String>> labs = p.getLabs();
		ArrayList<List<String>> courseSlots = p.getCourseSlots();
		ArrayList<List<String>> labSlots = p.getLabSlots();
		ArrayList<ArrayList<List<String>>> partAssign = p.getPartAssign();
		
		// Iterate over each partial assignment that was in the input file:
		Iterator<ArrayList<List<String>>> partAssigns = partAssign.iterator(); 
		while (partAssigns.hasNext()) {
			ArrayList<List<String>> assign = partAssigns.next();
			    			
			int pr_idx = -99;
			int slot_idx = -99;
			int course_idx = courses.indexOf(assign.get(0));
			// The partial assignment refers to a course...
			if (course_idx != -1) {
				pr_idx = course_idx;
				// Iterate through course slots to find the index of the correct time slot:
				Iterator<List<String>> slots = courseSlots.iterator();
				while (slots.hasNext()){
					List<String> slot = slots.next();
					if (slot.get(0).equals(assign.get(1).get(0)) && slot.get(1).equals(assign.get(2).get(0))){
						slot_idx = courseSlots.indexOf(slot);
					}
				}
			}
			// The partial assignment refers to a lab...
			else {
				pr_idx = courses.size() + labs.indexOf(assign.get(0));
				// Iterate through the lab slots to find the index of the correct time slot:
				Iterator<List<String>> slots = labSlots.iterator();
				while (slots.hasNext()){
					List<String> slot = slots.next();
					if (slot.get(0).equals(assign.get(1).get(0)) && slot.get(1).equals(assign.get(2).get(0))){
						slot_idx = labSlots.indexOf(slot);
					}
				}
			}
			// Update pr to ensure that the course/lab is assigned the specified time slot:
			pr[pr_idx] = slot_idx;
		}
		return pr;
	}
}
