import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Generation{
	private List<int[]> generation = new ArrayList<int[]>();
	private int pop_max = 0;
	private boolean debug = false;
	
	public Generation(){
		pop_max = Driver.pop_max; //for now get the population max everytime. Eventually this can just be called in the constructor
	
	}
	
	public void add(int[] candidate){
		this.generation.add(candidate);
	}
	
	public void evolve(){
		
		if (debug) System.out.println("I am evolving!");


		//check to seethat out generation is not Empty
		if(generation.isEmpty())
		{
			throw new IllegalStateException();
		}

		//We can evolve our population



		//First we choose two facts wtih F_Select
		Random rand = new Random();
		Selection selector = new Selection(generation, rand);
		int[] a = selector.select();
		int[] b = selector.select(selector.getLastIndexChoice());
		
		
		//We now have the two facts that we can use with the or-tree, and use the alt-search Control
		OrTree tree = new OrTree(2); //I am unsure what the length represents
		int[] child = tree.breedCandidates(a, b);

		generation.add(child);
		if (generation.size() < pop_max){
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
		System.out.println("Printing Generation:  ");
		for (int[] sol : this.generation){
			System.out.println(Arrays.toString(sol));
		}
	}
}
