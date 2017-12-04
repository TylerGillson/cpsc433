import java.util.ArrayList;
import java.util.Arrays;
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
	
	public void evolve(){
		
		if (debug) System.out.println("I am evolving!");

		// check to see that the generation is not Empty
		if (generation.isEmpty())
			throw new IllegalStateException();

		//We can evolve our population

		//First we choose two facts with F_Select:
		Random rand = new Random();
		Selection selector = new Selection(generation, rand);
		
		selector.select(-99);
		int[] a = selector.getSelection();
		
		selector.select(selector.getLastIndexChoice());
		int[] b = selector.getSelection();
		
		//We now have the two facts that we can use with the or-tree, and use the alt-search Control
		int pr_size = Driver.courses.size() + Driver.labs.size();
		OrTree<int[]> tree = new OrTree<int[]>(pr_size);
		
		int[] child = new int[pr_size];
		child = tree.breedCandidates(a, b);
				
		generation.add(child);
		if (generation.size() <= pop_max){
			if (debug) {
				System.out.println("The child created is : ");
				for (int i = 0; i < child.length -1; i++){
					System.out.print(child[i] + ", ");
				}
				System.out.print(child[child.length-1] + "\n");
				System.out.println("_______________________");

			}
			return; //We are done increasing this generation
		}
		else{
			//We need to reduce the population by taking out the worst individual.
			if(debug) System.out.println("Reducing size!");

			int[][] sortedFacts = Selection.sortFromEvals((int[][])generation.toArray(new int[generation.size()][generation.get(0).length]));
			generation.remove(sortedFacts[sortedFacts.length -1]);
		}		
	}
	
	public List<int[]> getGeneration(){
		return this.generation;
	}
	
	public void print(){
		System.out.println("Final Generation:");
		for (int[] sol : this.generation)
			System.out.println(Arrays.toString(sol));
		System.out.print("\n");
	}
}
