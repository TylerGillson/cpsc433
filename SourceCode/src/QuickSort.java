



public class QuickSort
{
	private int[] array;
	
	public QuickSort(int[] array)
	{
		this.array = array;
	}
	
	public int[] sort()
	{
		sortHelper(0, array.length-1);
		return array;
	}
	
	private int[] sortHelper(int low, int high)
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
		int pivot = array[high]; 
		int i = low-1;
		int temp;
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
		return i+1;
	}
	
	
}