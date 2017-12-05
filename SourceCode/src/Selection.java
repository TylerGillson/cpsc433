import java.lang.Math;
import java.util.Random;
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
		this.generation = generation;// = facts.toArray(new int[facts.size()][facts.get(0).length]);
		evalArray = new float[generationSize];
		indexArray = new float[generationSize];

		//Set up the array contents
		for (int i = 0; i<generationSize; i++)
			indexArray[i] = i;
		
		int sum = 0;
		int tempValue;
		for (int i = 0; i< generationSize; i++)
		{
			tempValue = Driver.eval.getValue(generation.get(i));
			evalArray[i] = tempValue;
			sum += tempValue;
		}
		
		//Next we need to normalize each of the eval weights of the facts
		
		// Avoid division by 0 edge case:
		if (sum == 0)
			sum += 1;
		
		for (int i = 0; i < generationSize; i++)
			evalArray[i] = evalArray[i] / sum;

		QuickSortWithIndex sorter = new QuickSortWithIndex(evalArray);
		//We now need to sort the now normalized factEvals
		float[][] sorted = sorter.sort();
		evalArray = sorted[0];
		indexArray=  sorted[1];
	}

	public int getLastIndexChoice()
	{
		return lastChoice;
	}
	
	public int[] getSelection(){
		return selection;
	}
	
	/**
	* Method performs f_select based on the population that the object was instantiated with
	* @param int ignoreFactIndex, the fact that is to not be used.
	* @return int[] some random fact based on the eval value of the roulette selection
	*/
	public void select(int ignoreFactIndex)
	{
		// Generate a random float within the range of existing eval values:
		float min = 99;
		float max = -99;
		for (int i=0; i<evalArray.length; i++){
			if (i == lastChoice)
				continue;
			if (evalArray[i] < min)
				min = evalArray[i];
			if (evalArray[i] > max)
				max = evalArray[i];
		}
		float randFloat = min + rand.nextFloat() * (max - min);
		
		// Perform roulette selection:
		for (int i = 0; i < evalArray.length; i++) {
			
			if (i == ignoreFactIndex)
				continue;
			if (evalArray[i] >= randFloat) {	
				//System.out.println("lastChoice = " + (int) indexArray[i] + " " + evalArray[i]);
				lastChoice = i;
				selection = generation.get(i);
				break;
			}
		}
	}

	public static int[][] sortFromEvals(int[][] facts)
	{

		//First we find the total eval value of each of the facts
		int sum = 0;
		if (facts.length == 0) throw new IllegalStateException();
		float[] factEvals = new float[facts.length];
		float[] courseIndexArray = new float[facts[0].length];

		for (int i = 0; i<facts[0].length; i++)
		{
			courseIndexArray[i] = i;
		}
		//To prevent multiple array lookups we use tempValue to store the eval value
		int tempValue;
		for (int i = 0; i< facts.length; i++)
		{
			tempValue = Driver.eval.getValue(facts[i]);
			factEvals[i] = tempValue;
			sum += tempValue;
		}
		//Next we need to normalize each of the eval weights of the facts
		for (int i = 0; i< facts.length; i++)
		{
			factEvals[i] = factEvals[i] /sum;
		}

		QuickSortWithIndex sorter = new QuickSortWithIndex(factEvals);
		//We now need to sort the now normalized factEvals
		float[][] temp = sorter.sort();
		factEvals = temp[0];
		courseIndexArray=  temp[1];


		int[][] sortedFacts = new int[courseIndexArray.length][facts[0].length];
		for (int i = 0; i < courseIndexArray.length; i++)
		{
			sortedFacts[i] = facts[Math.round(courseIndexArray[i])];

		}
		return sortedFacts;
	}
}
