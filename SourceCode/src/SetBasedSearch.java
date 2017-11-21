import java.lang.Math;
import java.util.Random;

public class SetBasedSearch
{
	private int population = 0;
	private final int populationMax;

	//*************************************README*********************************
	/*
	This is WIP! There are bugs in this, and names and methods are bound.
	This is just my current version, just so you can see what I am doing roughly
	
	
	*/
	
	
	public SetBasedSearch(int populationMax)
	{
		this.populationMax = populationMax;
		
	}
	
	public int[][] main_control(int[] partAssign, int[][] input)
	{
		int[] individual;
		int[][] newInput = new int[input.length+1][input[0].length];
		while ( population < populationMax)
		{
			
			if (elementInArray (input, partAssign))
			{
				individual = fakeOrTreeResult(partAssign.length); //We call our or tree here, using s0 = partAssign
				
			}
			else
			{
				individual = fakeOrTreeResult(partAssign.length); //We call our or tree here, using s0 defined in the or tree.
			}
	
			//There is a better way to do this!
			
			for(int i = 0; i < input.length; i++)
			{
				newInput[i] = input[i];
				
			}
			newInput[input.length] = individual;
			population +=1;
			}
		return newInput;
		
	}
	
	private int[] fakeOrTreeResult(int length)
	{
		Random rand = new Random();
		int[] fact = new int[length];
		for (int i = 0; i<length ; i++){
			fact[i] = rand.nextInt(100);
		}
		return fact;
	}
	
	private static boolean elementInArray(int[][] array, int[] element)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == element) 
				return true;
		}
		
		return false;
	}
	
	
}