import java.lang.Math;
import java.util.Random;
import java.util.Arrays;
import java.util.List;

public class Selection
{
	public static boolean debug = false;
	private List<int[]> generation;
	public float[] evalArray;
	private float[] indexArray;
	private Random rand;
	private int lastChoice = -99;
	private int[] selection;

	/**
	* Constructor takes in a population (the array of facts), and a Random object.
	* @param int[][] facts, Random rand, where facts is the population and random is an existing Random instance.
	*/
	public Selection(List<int[]> generation, Random rand)
	{
		this.rand = rand;
		int generationSize = generation.size();
		this.generation = generation;
		evalArray = new float[generationSize];
		indexArray = new float[generationSize];

		// Set up the index array:
		for (int i = 0; i < generationSize; i++)
			indexArray[i] = i;
		
		// Set up the eval array and calculate the generation's total eval-value:
		int sum = 0;
		int tempValue;
		for (int i = 0; i< generationSize; i++) {
			tempValue = Driver.eval.getValue(generation.get(i), false);
			evalArray[i] = tempValue;
			sum += tempValue;
		}
		
		// Avoid division by 0 edge case:
		if (sum == 0) sum += 1;
		
		// Normalize each of the eval values:
		for (int i = 0; i < generationSize; i++)
			evalArray[i] = evalArray[i] / sum;

		// Sort the normalized factEvals:
		QuickSortWithIndex sorter = new QuickSortWithIndex(evalArray);
		float[][] sorted = sorter.sort();
		evalArray  = sorted[0];
		indexArray = sorted[1];
	}

	public int getLastIndexChoice() {
		return lastChoice;
	}
	
	public int[] getSelection() {
		return selection;
	}
	
	/**
	* Method performs roulette wheel selection based on the population that the object was instantiated with.
	* @param int ignoreFactIndex, the fact that is to not be used.
	* @return int[] a semi-random fact
	*/
	public void select(int ignoreFactIndex)
	{
		// Generate a random float within the range of existing eval values:
		float min = 99;
		float max = -99;
		for (int i = 0; i < evalArray.length; i++){
			if (i == lastChoice)
				continue;
			if (evalArray[i] < min)
				min = evalArray[i];
			if (evalArray[i] > max)
				max = evalArray[i];
		}
		float randFloat = min + rand.nextFloat() * (max - min);
		
		// Perform roulette wheel selection:
		for (int i = 0; i < evalArray.length; i++) {
			if (i == ignoreFactIndex)
				continue;
			if (evalArray[i] >= randFloat) {	
				lastChoice = i;
				selection = generation.get(i);
				break;
			}
		}
	}
}
