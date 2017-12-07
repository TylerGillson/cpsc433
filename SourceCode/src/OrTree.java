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
	
	public List<OrTree<T>> options;
	
	// Constructor for beginning with a partial solution:
	public OrTree(int[] pr){
		this.data = pr.clone();
		this.children = new LinkedList<OrTree<T>>();
	}
	
	// "Empty" Constructor:
	public OrTree(int length){
		this.data = new int[length];
		Arrays.fill(this.data, -99);
		this.children = new LinkedList<OrTree<T>>();
	}
	
	public OrTree<T> addChild(int[] child){
		OrTree<T> childNode = new OrTree<T>(child);
		childNode.parent = this;
		this.children.add(childNode);
		return childNode;
	}
	
	public int[] getData(){
		return this.data;
	}
	
	/**
	 * Find all possible slots, s, applicable to the section located at pr[idx].
	 * Then, for each slot, create a child node representing assign(section) = s.
	 * @param idx - An integer index into a pr instance. Corresponds to a course/lab section.
	 */
	public void altern(int section_idx){
		
		// Determine is the section is a course or a lab:
		boolean isCourse = (section_idx < Driver.courses.size()) ? true : false;
		
		// Determine possible slot index values:
		int slot_indices = isCourse ? Driver.course_slots.size() : Driver.lab_slots.size();
		
		// Create successor nodes:
		for (int i=0; i<slot_indices; i++){
			int[] new_candidate = this.data.clone();
 			new_candidate[section_idx] = i;
			
			// If successor is viable, add it to the current node's children:
			if (Driver.constr.evaluate(new_candidate) == true) { 
				this.addChild(new_candidate);
			}
		}
	}
	
	/**
	 * Perform an or-tree-based search to generate a candidate solution.
	 * @return sol - An integer array which is a pr-solved instance.
	 */
	public int[] buildCandidate2(ArrayList<Integer> mostTightlyBound) {
		
		for (int i = 0; i < mostTightlyBound.size(); i++) {
			int expand_idx = mostTightlyBound.get(i);
			
			if (this.data[expand_idx] != -99)
				continue;
			
			altern(expand_idx);
			
			if (this.children.size() == 0) {
				this.data = Driver.pr;
				return buildCandidate2(mostTightlyBound);
			}
			
			Random rand = new Random();
			int randIdx = rand.nextInt(this.children.size());
			
			this.data = this.children.get(randIdx).data;
			this.children.clear();
		}
		
		return this.data;
	}
	
	public int[] buildCandidate(ArrayList<Integer> mostTightlyBound, int mtbIndex, List<OrTree<T>> leafHeap){
		
		// Return a solution when one is found:
		if (pr_finished(this.data))
			return this.data;
		else {
			// Determine index of element of pr that will be expanded by altern.
			// We expand the most tightly bound section.
			int expand_idx = mostTightlyBound.get(mtbIndex);	
			
			// Avoid over-writing values designed by partial assignments:
			if (this.data[expand_idx] != -99){
				mtbIndex++;
				return this.buildCandidate(mostTightlyBound, mtbIndex, leafHeap);
			}
			
			// Generate successor nodes for said section:
			altern(expand_idx);
			
			//System.out.println(leafHeap.size() + " " + this.children.size());
			
			// Remove the current node from leafHeap, as it has been expanded:
			leafHeap.remove(this);
			mtbIndex++;
			
			// Add the successor nodes to leafHeap:
			for (OrTree<T> c: this.children) {
				if (leafHeap.contains(c) == false)
					leafHeap.add(c);
			}
			
			Random rand = new Random();
			int randIndex = 0;
			int[] solution = new int[this.data.length];
			
			// If there are children, continue searching:
			if (this.children.size() > 0) {
				
				// Choose a random successor node to expand: 
				randIndex = rand.nextInt(this.children.size());
				OrTree<T> child = this.children.get(randIndex);
				
				// Recursively expand successor nodes until completion:
				solution = child.buildCandidate(mostTightlyBound, mtbIndex, leafHeap);
				
				if (solution == null) {
					this.children.remove(child);
					leafHeap.remove(child);
					
					if (leafHeap.isEmpty())
						return null;
					
					// Randomly select from leafHeap and expand:
					randIndex = rand.nextInt(leafHeap.size());
					OrTree<T> fromheap = leafHeap.get(randIndex);
					solution = fromheap.buildCandidate(mostTightlyBound, 0, leafHeap);
					return solution;
				}
				else
					return solution;
			} 
			// If all children were dead-ends, consult the leapHeap:
			else if (!leafHeap.isEmpty()) {
				
				// Randomly select from leafHeap and expand:
				randIndex = rand.nextInt(leafHeap.size());		
				OrTree<T> fromheap = leafHeap.get(randIndex);
				solution = fromheap.buildCandidate(mostTightlyBound, 0, leafHeap);
				return solution;
			}
			// If the leafHeap is empty and there are no children, there is no solution.
			else
				return null;
		}
	}
	
	/**
	 * Perform an or-tree-based search to breed two candidate solutions.
	 * @param par1 - Parent 1.
	 * @param par2 - Parent 2.
	 * @return sol - An integer array which is a new, hybrid pr-solved instance.
	 */
	public int[] breedCandidates(int[] par1, int[] par2){
		int len = par1.length;
		
		// Initialize child
		int[] child = Driver.pr.clone();
		
		// Iterate over each index of the parent candidates and execute breeding logic:
		for (int i=0; i<len; i++){
			
			// Skip indices that are governed by partial assignments:
			if (child[i] != -99)
				continue;
			
			// If parents agree, attempt to preserve mutual genetics:
			if (par1[i] == par2[i]) {
				child[i] = par1[i];
				
				// Check validity of preservation:
				if (Driver.constr.evaluate(child))
					continue;
			}
			
			// Otherwise, execute breeding logic:
			child = breed(child, par1, par2, i);
		}
		
		// Return the new, hybridized pr-instance:
		return child;
	}
	
	public int[] breed(int[] child, int[] par1, int[] par2, int i) {
		
		// Assess the viability of selecting each parent's assignment:
		child[i] = par1[i];
		boolean pick_par1 = Driver.constr.evaluate(child);
		
		child[i] = par2[i];
		boolean pick_par2 = Driver.constr.evaluate(child);
		
		// If only par1 is viable, choose par1's assignment:
		if (pick_par1 && !pick_par2) {
			child[i] = par1[i];
			return child;
		}
		// If only par2 is viable, choose par2's assignment:
		else if (!pick_par1 && pick_par2) {
			child[i] = par2[i];
			return child;
		}
		// Otherwise, perform altern and randomly select from among viable options:
		else {
			this.data = child;
			this.children.clear();
			altern(i);
			
			if (this.children.size() > 0) {
				// Make the assignment and continue iterating:
				Random rn = new Random();
				int select = rn.nextInt(this.children.size());
				child = this.children.get(select).data;
				return child;
			}
			return child;
		}
	}
	
	/**
	 * Check an OrTree instance to determine if its data is in state: pr-solved.
	 * @param t 		- An OrTree instance.
	 * @return finished - A Boolean indicating whether a solution has been found.
	 */
	public boolean pr_finished(int[] data){
		// Iterate over the tree's data array to check for unassigned indices:
		for (int i=0; i<data.length; i++){
			if (data[i] == -99)
				return false;
		}
		return true;
	}
	
	public int getDepth(){
		if (this.parent == null)
			return 0;
		else 
			return this.parent.getDepth() + 1;
		
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

