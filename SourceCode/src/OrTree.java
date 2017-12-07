import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class OrTree<T>{
	
	public int[] data;
	public OrTree<T> parent;
	public List<OrTree<T>> children;
	
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
	 * @param idx	- An integer index into a pr instance. Corresponds to a course/lab section.
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
	 * Attempt to build a candidate solution for the problem instance.
	 * @param mostTightlyBound 	- Array indicating which order to process the indices in.
	 * @param mtbIndex			- The current index of mostTightlyBound to process.
	 * @param leafHeap			- A list of valid leaves to expand.
	 * @return					- A completed candidate, or null if one does not exist.
	 */
	public int[] buildCandidate(ArrayList<Integer> mostTightlyBound, int mtbIndex, List<OrTree<T>> leafHeap){
		
		// Return a solution when one is found:
		if (pr_finished(this.data))
			return this.data;
		else {
			// Determine index of element of pr that will be expanded by altern:
			int expand_idx = mostTightlyBound.get(mtbIndex);	
			
			// Avoid over-writing values designed by partial assignments:
			if (this.data[expand_idx] != -99){
				mtbIndex++;
				return this.buildCandidate(mostTightlyBound, mtbIndex, leafHeap);
			}
			
			// Generate successor nodes for current section:
			altern(expand_idx);
			
			// Remove the current node from leafHeap, as it has been expanded:
			leafHeap.remove(this);
			mtbIndex++;
			
			// Add the successor nodes to leafHeap:
			for (OrTree<T> c: this.children) {
				if (leafHeap.contains(c) == false)
					leafHeap.add(c);
			}
			
			// If there are children, continue searching:
			if (this.children.size() > 0) {
				
				// Choose a random successor node to expand: 
				Random rand = new Random();
				int randIndex = rand.nextInt(this.children.size());
				OrTree<T> child = this.children.get(randIndex);
				
				// Recursively expand successor nodes until completion:
				int [] solution = child.buildCandidate(mostTightlyBound, mtbIndex, leafHeap);
				
				if (solution == null) {
					this.children.remove(child);
					leafHeap.remove(child);
					
					if (leafHeap.isEmpty())
						return null;
					
					solution = pickNewNode(mostTightlyBound, leafHeap);
					return solution;
				}
				else
					return solution;
			} 
			// If all children were dead-ends, consult the leapHeap:
			else if (!leafHeap.isEmpty()) {
				
				int[] solution = pickNewNode(mostTightlyBound, leafHeap);
				return solution;
			}
			// If the leafHeap is empty and there are no children, there is no solution.
			else
				return null;
		}
	}
	
	/**
	 * When the OrTree based search hits a dead-end, this method chooses a new node to expand.
	 * @param mostTightlyBound
	 * @param leafHeap
	 * @return solution
	 */
	public int[] pickNewNode(ArrayList<Integer> mostTightlyBound, List<OrTree<T>> leafHeap) {
		
		// Randomly select a node from leafHeap to expand:
		Random rand = new Random(); 
		int randIndex = rand.nextInt(leafHeap.size());
		OrTree<T> fromheap = leafHeap.get(randIndex);
		int[] solution = fromheap.buildCandidate(mostTightlyBound, 0, leafHeap);
		return solution;
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
	
	/**
	 * Executes breeding logic at a specific index.
	 * Used in the alternate search control for the GA.
	 * @param child
	 * @param par1
	 * @param par2
	 * @param i
	 * @return child
	 */
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
}

