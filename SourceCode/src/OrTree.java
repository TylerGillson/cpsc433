import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// Credit: https://github.com/gt4dev/yet-another-tree-structure/blob/master/java/src/com/tree/TreeNode.java

public class OrTree<T>{
	
	public int[] data;
	public OrTree<T> parent;
	public List<OrTree<T>> children;
	public boolean finish;
	// Recursion Control Globals:
//		public static Boolean quit = false;
//		public static int[] sol;

	
	// Constructor for beginning with a partial solution:
	public OrTree(int[] pr){
		this.data = pr;
		this.children = new LinkedList<OrTree<T>>();
	}
	
	// "Empty" Constructor:
	public OrTree(int length){
		this.data = new int[length];
		this.children = new LinkedList<OrTree<T>>();
	}
	
	public OrTree<T> addChild(int[] child){
		OrTree<T> childNode = new OrTree<T>(child);
		childNode.parent = this;
		this.children.add(childNode);
		return childNode;
	}
	
	/**
	 * Determine how tightly bound a particular section is. (within a pr instance).
	 * @param section_idx - An integer index into a pr instance. Corresponds to a course/lab section.
	 * @return
	 */
	public int num_constr(int section_idx){
		Random rn = new Random();
		return rn.nextInt(10) + 1;
	}
	
	public boolean constr(int[] candidate){
		return true;
	}
	
	/**
	 * Find all possible slots, s, applicable to the section located at pr[idx].
	 * Then, for each slot, create a child node representing assign(section) = s.
	 * @param idx - An integer index into a pr instance. Corresponds to a course/lab section.
	 */
	public void altern(int section_idx){
		// Determine is the section is a course or a lab:
		boolean isCourse;
		if (section_idx < Driver.courses.size())
			isCourse = true;
		else
			isCourse = false;
		
		// Determine possible slot index values:
		int slot_indices = isCourse ? Driver.course_slots.size() : Driver.lab_slots.size();
		
		// Create successor nodes:
		for (int i=0; i<slot_indices; i++){
			int[] new_candidate = this.data.clone();
			new_candidate[section_idx] = i;
			
			// If successor is viable, add it to the current node's children:
			if (constr(new_candidate))
				this.addChild(new_candidate);
		}
	}
	
	/**
	 * Perform an or-tree-based search to generate a candidate solution.
	 * @return sol - An integer array which is a pr-solved instance.
	 */
	public int[] buildCandidate(){
		// Index of element of pr that will be expanded by altern.
		int expand_idx;
		
		//	Array format: {num_constr,index}
		int max_constr0 = 0;
		int max_constr1 = 0;
		// Determine most tightly bound section within pr:
		for (int i=0; i<this.data.length; i++){
			// Only consider unassigned sections
			if (this.data[i] == -99){
				int cur_constr = num_constr(i);
				if (cur_constr > max_constr0){
					max_constr0 = cur_constr;
					max_constr1 = i;
				}
			}
		}
		// We expand the most tightly bound section.
		expand_idx = max_constr1;	
	
		// Generate successor nodes for said section:
		altern(expand_idx);
		
		// Recursively expand successor nodes until completion:
		if (!pr_finished(this)){
			for (OrTree<T> child : this.children){
				if (!pr_finished(child))
					child.buildCandidate();
				else
					break;
			}
		}

		
		// Return a solution from the completed OrTree:

		return getSolution();
	}
	
	/**
	 * Perform an or-tree-based search to breed two candidate solutions.
	 * @param par1 - Parent 1.
	 * @param par2 - Parent 2.
	 * @return sol - An integer array which is a new, hybrid pr-solved instance.
	 */
	public int[] breedCandidates(int[] par1, int[] par2){
		int len = par1.length;
		int[] child = new int[len];
		boolean pick_par1;
		boolean pick_par2;
		
		// Iterate over each index of the parent candidates and execute breeding logic:
		for (int i=0; i<len; i++){
			// If parents agree, preserve mutual genetics:
			if (par1[i] == par2[i])
				child[i] = par1[i];
			else {
				// Assess the viability of selecting each parent's assignment:
				child[i] = par1[i];
				pick_par1 = constr(child);
				child[i] = par2[i];
				pick_par2 = constr(child);
				
				// If only par1 is viable, choose par1's assignment:
				if (pick_par1 && !pick_par2)
					child[i] = par1[i];
				// If only par2 is viable, choose par2's assignment:
				else if (!pick_par1 && pick_par2)
					child[i] = par2[i];
				// Otherwise, perform altern and randomly select from among viable options:
				else {
					altern(i);
					ArrayList<int[]> options = new ArrayList<int[]>();
					for (OrTree<T> c : this.children){
						if (constr(c.data))
							options.add(c.data);
					}
					Random rn = new Random();
					int select = rn.nextInt(options.size());
					child = options.get(select);
				}
			}
		}
		
		// Return the new, hybridized pr-instance:
		return child;
	}
	
	/**
	 * Check an OrTree instance to determine if its data is in state: pr-solved.
	 * @param t 		- An OrTree instance.
	 * @return finished - A Boolean indicating whether a solution has been found.
	 */
	public boolean pr_finished(OrTree<T> t){
		// Iterate over the tree's data array to check for unassigned indices:
		for (int i=0; i<t.data.length; i++){
			if (t.data[i] == -99)
				return false;
		}
		return true;
	}
	
	public int getDepth(){
		if (this.parent == null)
			return 0;
		else {
			return this.parent.getDepth() + 1;
		}
	}
	
	/**
	 * Recover a solution from a completed OrTree.
	 * @return sol - An integer array which is a pr-solved instance.
	 */
	public int[] getSolution(){		
//		while (quit == false){
			for (OrTree<T> child : this.children){
				// If the child is finished, return its data as the solution:
				if (pr_finished(child)){
					return child.data;
					// Set global flag to kill other recursive calls:
//					quit = true;
				
				}
				// Otherwise, generate a new set of recursive calls: 
				else {
					return child.getSolution();
				}
			}	
//		}
		return null;
	}
	
	/**
	 * Print an OrTree to the console.
	 * @param is_root - Boolean to avoid duplicate print statements.
	 */
	public void printTree(Boolean is_root){
		if (is_root)
			System.out.println("depth: " + this.getDepth() + " data: " + Arrays.toString(this.data));
		
		for (OrTree<T> child : this.children){
			int depth = child.getDepth();
			System.out.println("depth: " + depth + " data: " + Arrays.toString(child.data));
			child.printTree(false);
		}
	}
}

