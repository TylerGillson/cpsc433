import java.util.*;

public class Course
{
	private final int EVE_VAL = 100;
	private String name;
	private boolean evening = false;
	private ArrayList<String> unwanted = new ArrayList<String>();
	private ArrayList<String> incompatible = new ArrayList<String>();
	private int constrVal = 0;
	
	//Still need to deal with adding labs of courses to incompatible, 500 level courses, etc.
	public Course(String courseName, ArrayList<List<String>> totalUnwanted, ArrayList<List<String>> totalIncompatible)
	{
		name = courseName;
		if (courseName.substring(0,5).equals("LEC 9"))
			evening = true;
		
		//Think about only needeing to add to list if first element since we merge lists when checking? Problem with constraint number
		for (int i = 0; i < totalIncompatible.size(); i++)
		{
			if (totalIncompatible.get(i).get(0).equals(name))
				incompatible.add(totalIncompatible.get(i).get(1));
			if (totalIncompatible.get(i).get(1).equals(name))
				incompatible.add(totalIncompatible.get(i).get(0));			
		}
		
		for (int i = 0; i < totalUnwanted.size(); i++)
		{
			if (totalUnwanted.get(i).get(0).equals(name))
				unwanted.add(totalUnwanted.get(i).get(1));	
		}
		
		constrVal = unwanted.size() + incompatible.size() + (evening ? EVE_VAL : 0);
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean getEvening()
	{
		return evening;
	}
	
	public ArrayList<String> getUnwanted()
	{
		return unwanted;
	}

	public ArrayList<String> getIncompatible()
	{
		return incompatible;
	}	
	
	public int numOfConstraints()
	{
		return constrVal;
	}
}