import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Generation{
	
	private List<int[]> generation = new ArrayList<int[]>();
	private int pop_max = 0;
	private boolean debug = false;
	
	public Generation(){
		pop_max = Driver.pop_max;
	}
	
	public void add(int[] candidate){
		this.generation.add(candidate);
	}
	
	public void cull(){
		List<int[]> unique = new ArrayList<int[]>();
		boolean add = true;
		
		for (int[] member : this.generation) {
			for (int[] uMem : unique) {
				add = false;
				if (!Arrays.equals(member, uMem))
					add = true;
			}
			if (add)
				unique.add(member);
		}
		this.generation.clear();
		this.generation = unique;
	}
	
	public void evolve(int gen_num){
		
		// Choose two facts via roulette wheel selection:
		Random rand = new Random();
		Selection selector = new Selection(generation, rand);
		
		selector.select(-99);
		int[] a = selector.getSelection();
		
		selector.select(selector.getLastIndexChoice());
		int[] b = selector.getSelection();
		
		if (Arrays.equals(a, b))
			throw new java.lang.Error("Breeding identical parents!");
		
		// Breed them using an or-tree and the alternate search control:
		int pr_size = Driver.courses.size() + Driver.labs.size();
		OrTree<int[]> tree = new OrTree<int[]>(pr_size);
		
		int[] child = new int[pr_size];
		child = tree.breedCandidates(a, b);
		
		if (Driver.print_prs)
			System.out.println("Child #" + gen_num + "\tEval score: " + Driver.eval.getValue(child));
		
		generation.add(child);
		
		// Check that the population maximum has not been exceeded:
		if (generation.size() > pop_max) {
			// If it has been, reduce the population by taking out the worst individual:
			Collections.sort(generation, new Comparator<int[]>() {
		        public int compare(int[] sol1, int[] sol2){
		        	int e1 = Driver.eval.getValue(sol1);
		        	int e2 = Driver.eval.getValue(sol2);
		        	
		        	if (e1 == e2)
		            	return 0;
		            else if (e1 > e2)
		            	return 1;
		            else
		            	return -1;
		        }
		    });
			
			generation.remove(generation.size()-1);
		}		
	}
	
	public List<int[]> getGeneration(){
		return this.generation;
	}
	
	public void printAvg() {
		float avg = 0;
		for (int i = 0; i < generation.size(); i++)
			avg += Driver.eval.getValue(generation.get(i));
		avg = avg / generation.size();
		System.out.println(avg);
	}
	
	public void print(){
		System.out.println("\nFinal Generation:");
		for (int[] sol : this.generation)
			System.out.println(Arrays.toString(sol));
		System.out.print("\n");
	}
}
