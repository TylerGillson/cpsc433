import java.lang.Math;
import java.util.Random;

public class Selection 
{
	public static boolean debug = false;
	private int[][] factArray;
	private float[] evalArray;
	private float[] indexArray;
	private Random rand;
	private int lastChoice;
	
	/**
	* Constructor takes in a population (the array of facts), and a Random object.
	* @param int[][] facts, Random rand, where facts is the population and random is an existing Random instance.
	*/
	public Selection(int[][] facts, Random rand)
	{
		this.rand = rand;
		factArray = facts;
		evalArray = new float[factArray[0].length];
		indexArray = new float[factArray[0].length];
		
		//Set up the array contents
		for (int i = 0; i<factArray[0].length; i++)
		{
			indexArray[i] = i;
		}
		int sum = 0;
		int tempValue; 
		for (int i = 0; i< factArray.length; i++)
		{
			tempValue = tempEval(factArray[i]);
			evalArray[i] = tempValue;
			sum += tempValue;
		}
		//Next we need to normalize each of the eval weights of the facts 
		for (int i = 0; i< factArray.length; i++)
		{
			evalArray[i] = evalArray[i] /sum;
		}
		
		QuickSortWithIndex sorter = new QuickSortWithIndex(evalArray);
		//We now need to sort the now normalized factEvals
		float[][] temp = sorter.sort();
		evalArray = temp[0];
		indexArray=  temp[1];
	}
	
	public int getLastIndexChoice()
	{
		try{
			lastChoice +=-0;		
		}catch(Exception e)
		{
			throw new IllegalStateException();
		}
	
		return lastChoice;
	
	}
	/**
	* Method performs f_select based on the population that the object was instantiated with
	* @param int ignoreFactIndex, the fact that is to not be used. 
	* @return int[] some random fact based on the eval evalue of the roulette selection
	*/
	public int[] select(int ignoreFactIndex)
	{
		float randFloat = rand.nextFloat();  //We now have our random index. 
		for (int i = 0; i < evalArray.length; i++)
		{
			if ( i != ignoreFactIndex)
			{
				if (evalArray[Math.round(indexArray[i])] >= randFloat)
				{
					lastChoice = Math.round(indexArray[i]);
					return factArray[Math.round(indexArray[i])];
				}	
			}			
		}
		
		lastChoice = Math.round(indexArray[0]);
		return factArray[Math.round(indexArray[0])];
	}
	
	
	
	/**
	* Method performs f_select based on the population that the object was instantiated with 
	* @return int[] some random fact based on the eval evalue of the roulette selection
	*/
	public int[] select()
	{
		float randFloat = rand.nextFloat();  //We now have our random index. 
		for (int i = 0; i < evalArray.length; i++)
		{
			
			if (evalArray[Math.round(indexArray[i])] >= randFloat)
			{
				lastChoice = Math.round(indexArray[i]);
				return factArray[Math.round(indexArray[i])];
			}	
						
		}
		lastChoice = Math.round(indexArray[0]);
		return factArray[Math.round(indexArray[0])];
	}
	
	
	
	
	/**
	* This method performs f_select without an object being instantiated.
	* This should only be used if this will be called once with a set of facts. Otherwise an object should be instantiated
	* @param int[][] facts, a 2d array, where the first array is the array of assignments
	* @return int[] selectedFact, the fact chosen from the population via roulette selection
	*/
	public static int[] select_singleuse(int [][] facts)
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
			tempValue = tempEval(facts[i]);
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
		
		if(debug){
			System.out.println("The normalized eval values is as follows: ");
			for (int i = 0; i <factEvals.length; i++)
			{
				System.out.println(factEvals[Math.round(courseIndexArray[i])] + " at index " + Math.round(courseIndexArray[i]));
			}
		}
		
		Random rand = new Random();
		float randFloat = rand.nextFloat();  //We now have our random index. 
		for (int i = 0; i < factEvals.length; i++)
		{
			if (factEvals[Math.round(courseIndexArray[i])] >= randFloat)
			{
				return facts[Math.round(courseIndexArray[i])];
			}
			
		}
		
		return facts[Math.round(courseIndexArray[0])];

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
			tempValue = tempEval(facts[i]);
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
	
	//A temporary eval function
	public static int tempEval(int[] assign)
	{
		int temp;
		int val = 0;
		//This is just an arbitrary method 
		for (int i = 0; i< assign.length; i++)
		{
			temp = assign[i];
			if (temp%2 == 0) val += 1;
			if (temp% 3 == 0) val += 3;
		}
		//This method prefers assignments that do not contain numbers divisible by two or three
		return val;
	}



}