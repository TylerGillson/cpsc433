import java.util.Random;
import java.lang.Math;

public class Ext{

	public static int[][] cross_Or_mut(int[][] facts, Random rand)
	{
		Selection selector = new Selection(facts, rand);
		int[] a = selector.select();
		int lastChosen = selector.getLastIndexChoice();
		int[] b = selector.select(lastChosen);
		
		if (a == b) throw new IllegalStateException();
		//We now have our individuals that can be used for the or tree.
		int[][] chosen = new int[][] {a, b};
		return chosen;
	}
	
	
	public static int[][] reduce(int[][] facts)
	{
		//Using quick sort we dont want to remove the first individual as it is the best, so we remove the last one instead
		int[][] sortedFacts = Selection.sortFromEvals(facts);
		int[][] reducedFacts = new int[sortedFacts.length -1][sortedFacts[0].length];
		for (int i = 0; i < reducedFacts .length; i++)
		{	
			reducedFacts[i] = sortedFacts[i];
		}
		return reducedFacts;
	}



}
