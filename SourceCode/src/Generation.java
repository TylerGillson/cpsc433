import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Generation{
	
	private List<int[]> generation = new ArrayList<int[]>();
	private int pop_max = 0;
	
	public Generation(){
		pop_max = Driver.pop_max;
	}
	
	public void add(int[] candidate){
		this.generation.add(candidate);
	}
	
	public void evolve(int gen_num){
		
		// Choose two facts via roulette wheel selection:
		Random rand = new Random();
		Selection selector = new Selection(generation, rand);
		
		selector.select(-99);
		int[] a = selector.getSelection();
		
		selector.select(selector.getLastIndexChoice());
		int[] b = selector.getSelection();
		
		// Breed them using an or-tree and the alternate search control:
		int pr_size = Driver.courses.size() + Driver.labs.size();
		OrTree<int[]> tree = new OrTree<int[]>(pr_size);
		
		int[] child = new int[pr_size];
		child = tree.breedCandidates(a, b);
		
		if (Driver.print_prs)
			System.out.println("Child #" + gen_num + "\tEval score: " + Driver.eval.getValue(child, false));
		
		generation.add(child);
		
		// Check that the population maximum has not been exceeded:
		if (generation.size() > pop_max) {
			
			// If it has been, reduce the population by taking out the worst individual:
			Collections.sort(generation, new Comparator<int[]>() {
		        public int compare(int[] sol1, int[] sol2){
		        	int e1 = Driver.eval.getValue(sol1, false);
		        	int e2 = Driver.eval.getValue(sol2, false);
		        	
		        	if (e1 == e2)
		            	return 0;
		            else if (e1 < e2)
		            	return 1;
		            else
		            	return -1;
		        }
		    });
			
			// Cull the generation:
			for (int i = 0; i < Driver.cull_num; i++)
				generation.remove(i);
		}		
	}
	
	public List<int[]> getGeneration() {
		return this.generation;
	}
	
	public void printData(boolean initialFlag) {
		String output = "\t\tAvg: " + getAvg() + "\t\tMin: " + getMin() + "\t\tMax: " + getMax() + "\n";
		if (initialFlag)
			output = "\t" + output;
		System.out.print(output);
	}
	
	public String getAvg() {
		float avg = 0;
		for (int i = 0; i < generation.size(); i++)
			avg += Driver.eval.getValue(generation.get(i), false);
		avg = avg / generation.size();
		return String.valueOf(avg);
	}
	
	public String getMin() {
		int min = Driver.eval.getValue(generation.get(0), false);
		for (int i = 1; i < generation.size(); i++)
			if (min > Driver.eval.getValue(generation.get(i), false))
				min =  Driver.eval.getValue(generation.get(i), false);
		return String.valueOf(min);
	}
	
	public String getMax() {
		int max = Driver.eval.getValue(generation.get(0), false);
		for (int i = 1; i < generation.size(); i++)
			if (max < Driver.eval.getValue(generation.get(i), false))
				max =  Driver.eval.getValue(generation.get(i), false);
		return String.valueOf(max);
	}
	
	public void print(){
		System.out.println("\nFinal Generation:");
		for (int[] sol : this.generation)
			System.out.println(Arrays.toString(sol));
		System.out.print("\n");
	}
}
