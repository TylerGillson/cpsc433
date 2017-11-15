import java.util.ArrayList;
import java.util.List;

public class Generation{
	private List<int[]> generation = new ArrayList<int[]>();
	
	public Generation(){
	}
	
	public void add(int[] candidate){
		this.generation.add(candidate);
	}
	
	public void evolve(){
		
	}
	
	public List<int[]> getGeneration(){
		return this.generation;
	}
}
