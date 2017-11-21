public class QuickSortWithIndex 
{
	private float[] array; 
	private float[] indexArray;
	
	/**
	* Creates an QuickSortWithIndex object to sort an array. This is intended to sort normalized eveal values.
	* @param float[] array, the array to be sorted
	*/
	public QuickSortWithIndex(float[] array)
	{
		this.array = array;
		this.indexArray = new float[array.length];
	}
	
	/**
	* Method uses quick sort to sort the array.
	* @return float[][] arrayPair where arrayPair[0] = sorted array, and arrayPair[1] = indexArray, the original index of each array value.
	*/
	public float[][] sort()
	{
		sortHelper(0, array.length-1);
		
		float[][] arrayPair = new float[2][array.length];
		arrayPair[0] = array;
		arrayPair[1] = indexArray;
		return arrayPair;
	}
	
	private float[] sortHelper(int low, int high)
	{
		if (low < high)
		{
			
			int partIndex = partition(low, high);
			
			array = sortHelper(low, partIndex-1);
			array = sortHelper(partIndex +1, high);
		}
		
		return array;
		
	}
	
	
	private int partition(int low, int high){
		float pivot = array[high]; 
		int i = low-1;
		float temp;
		for(int j = low; j<= high -1; j++)
		{
			if (array[j] <= pivot)
			{
				i++;
				 temp = array[i];
				array[i] = array[j];
				array[j] = temp;
			}
		}
		temp = array[i+1];
		array[i+1] = array[high];
		array[high] = temp;
		
		temp = indexArray[i+1];
		indexArray[i+1] = indexArray[high];
		indexArray[high] = temp;
		return i+1;
	}


}