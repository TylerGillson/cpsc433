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
	public int[] buildCandidate(ArrayList<Integer> mostTightlyBound, int mtbIndex, List<OrTree<T>> leafHeap, Random rand){
		
		System.out.println("LEAF HEAP SIZE: " + leafHeap.size());
		
		// Return a solution when one is found:
		if (pr_finished(this.data))
			return this.data;
		else {
			// Determine index of element of pr that will be expanded by altern:
			int expand_idx = mostTightlyBound.get(mtbIndex);	
			
			// Avoid over-writing values designed by partial assignments:
			if (this.data[expand_idx] != -99){
				mtbIndex++;	
				return this.buildCandidate(mostTightlyBound, mtbIndex, leafHeap, rand);
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
				int randIndex = rand.nextInt(this.children.size());
				OrTree<T> child = this.children.get(randIndex);
				
				// Recursively expand successor nodes until completion:
				return child.buildCandidate(mostTightlyBound, mtbIndex, leafHeap, rand);
			} 
			// If all children were dead-ends, consult the leapHeap:
			else if (!leafHeap.isEmpty()) {
				
				// Randomly select a node from leafHeap to expand: 
				int randIndex = rand.nextInt(leafHeap.size());
				OrTree<T> fromHeap = leafHeap.get(randIndex);
				
				return fromHeap.buildCandidate(mostTightlyBound, 0, leafHeap, rand);
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
	public int[] breedCandidates(int[] child, int idx, int[] par1, int[] par2, List<OrTree<T>> leafHeap, Random rand){
		
		// Return the child once it is complete:
		if (idx == child.length)
			return child;
		
		// Skip indices that are governed by partial assignments:
		if (child[idx] != -99) {
			idx++;
			return breedCandidates(child, idx, par1, par2, leafHeap, rand);
		}
			
		// If parents agree, attempt to preserve mutual genetics:
		if (par1[idx] == par2[idx]) {
			child[idx] = par1[idx];
				
			// Check validity of preservation:
			if (Driver.constr.evaluate(child)) {
				idx++;
				return breedCandidates(child, idx, par1, par2, leafHeap, rand);
			}
		}
			
		// Otherwise, execute breeding logic:
		child = breed(child, idx, par1, par2, leafHeap, rand);
		idx++;
		return breedCandidates(child, idx, par1, par2, leafHeap, rand);
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
	public int[] breed(int[] child, int idx, int[] par1, int[] par2, List<OrTree<T>> leafHeap, Random rand) {
		
		// Assess the viability of selecting each parent's assignment:
		child[idx] = par1[idx];
		boolean pick_par1 = Driver.constr.evaluate(child);
		
		child[idx] = par2[idx];
		boolean pick_par2 = Driver.constr.evaluate(child);
		
		// If only par1 is viable, choose par1's assignment:
		if (pick_par1 && !pick_par2) {
			child[idx] = par1[idx];
			idx++;
			return breedCandidates(child, idx, par1, par2, leafHeap, rand);
		}
		// If only par2 is viable, choose par2's assignment:
		else if (!pick_par1 && pick_par2) {
			child[idx] = par2[idx];
			idx++;
			return breedCandidates(child, idx, par1, par2, leafHeap, rand);
		}
		// Otherwise, perform altern and randomly select from among viable options:
		else {
			this.children.clear();
			altern(idx);
			leafHeap.remove(this);
			
			for (OrTree<T> c : this.children)
				leafHeap.add(c);
			
			// If there are children, continue searching:
			if (this.children.size() > 0) {
				
				// Choose a random successor node to expand: 
				int randIndex = rand.nextInt(this.children.size());
				OrTree<T> c = this.children.get(randIndex);
				
				child = c.breedCandidates(child, idx, par1, par2, leafHeap, rand);
				
				if (Driver.constr.evaluate(child) == false) {
					this.children.remove(c);
					leafHeap.remove(c);
					
					randIndex = rand.nextInt(leafHeap.size());
					OrTree<T> fromHeap = leafHeap.get(randIndex);
					return breedCandidates(fromHeap.data, 0, par1, par2, leafHeap, rand);
				}
				else
					return breedCandidates(child, idx, par1, par2, leafHeap, rand);
			} 
			// If all children were dead-ends, consult the leapHeap:
			else { //(!leafHeap.isEmpty()) {
				
				int randIndex = rand.nextInt(leafHeap.size());
				OrTree<T> fromHeap = leafHeap.get(randIndex);
				return breedCandidates(fromHeap.data, 0, par1, par2, leafHeap, rand);
			}
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

