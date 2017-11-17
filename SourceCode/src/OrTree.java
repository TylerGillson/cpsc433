import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// Credit: https://github.com/gt4dev/yet-another-tree-structure/blob/master/java/src/com/tree/TreeNode.java

public class OrTree<T>{
	
	public int[] data;
	public OrTree<T> parent;
	public List<OrTree<T>> children;
	
	// Recursion Control Globals:
	public static Boolean quit;
	public static int[] sol;
	
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
	 * Then, for each slot, create a child node representing assign(section) = s
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
	 * @return int[] sol - An integer array which is a pr-solved instance.
	 */
	public int[] buildCandidate(){
		// Index of element of pr that will be expanded by altern.
		int expand_idx;
		
		//	Array format: {num_constr,index}
		int[] max_constr = {0,0};
		
		// Determine most tightly bound section within pr:
		for (int i=0; i<this.data.length; i++){
			// Only consider unassigned sections
			if (this.data[i] == -99){
				int cur_constr = num_constr(i);
				if (cur_constr > max_constr[0]){
					max_constr[0] = cur_constr;
					max_constr[1] = i;
				}
			}
		}
		// We expand the most tightly bound section.
		expand_idx = max_constr[1];	
		
		// Generate successor nodes for said section:
		altern(expand_idx);
		
		// Recursively expand successor nodes until completion:
		quit = false;
		while (quit == false){
			if (!pr_finished(this)){
				for (OrTree<T> child : this.children){
					if (!pr_finished(child))
						child.buildCandidate();
					else
						quit = true;
				}
			}
		}
		
		// Return a solution from the completed OrTree:
		quit = false;
		return getSolution();
	}
	
	/**
	 * Check an OrTree instance to determine if its data is in state: pr-solved.
	 * @param t 		- An OrTree instance.
	 * @return finished - A Boolean indicating whether a solution has been found.
	 */
	public boolean pr_finished(OrTree<T> t){
		boolean finished = true;
		// Iterate over the tree's data array to check for unassigned indices:
		for (int i=0; i<t.data.length; i++){
			if (t.data[i] == -99)
				finished = false;
		}
		return finished;
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
	 * @return int[] sol - An integer array which is a pr-solved instance.
	 */
	public int[] getSolution(){		
		while (quit == false){
			for (OrTree<T> child : this.children){
				// If the child is finished, return its data as the solution:
				if (pr_finished(child)){
					sol = child.data;
					// Set global flag to kill other recursive calls:
					quit = true;
					break;
				}
				// Otherwise, generate a new set of recursive calls: 
				else
					child.getSolution();
			}	
		}
		return sol;
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

