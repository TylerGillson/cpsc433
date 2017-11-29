import java.util.*;

public class Course
{
	private final int EVE_VAL = 100;
	private String name;
	private boolean evening = false;
	private ArrayList<List<String>> unwanted = new ArrayList<List<String>>();
	private ArrayList<List<String>> incompatible = new ArrayList<List<String>>();
	private int constrVal = 0;
	
	//Still need to deal with adding labs of courses to incompatible, 500 level courses, etc.
	public Course(String type, int idx)
	{
		name = type.equals("course") ? Driver.courses.get(idx).toString() : Driver.labs.get(idx).toString();
				
		if (name.substring(0,5).equals("LEC 09"))
			evening = true;
		
		//Think about only needing to add to list if first element since we merge lists when checking? Problem with constraint number
		for (int i = 0; i < Driver.not_compatible.size(); i++)
		{
			if (Driver.not_compatible.get(i).get(0).equals(name))
				incompatible.add(Driver.not_compatible.get(i).get(1));
			if (Driver.not_compatible.get(i).get(1).equals(name))
				incompatible.add(Driver.not_compatible.get(i).get(0));			
		}
		
		for (int i = 0; i < Driver.unwanted.size(); i++)
		{
			if (Driver.unwanted.get(i).get(0).equals(name))
				unwanted.add(Driver.unwanted.get(i).get(1));	
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
	
	public ArrayList<List<String>> getUnwanted()
	{
		return unwanted;
	}

	public ArrayList<List<String>> getIncompatible()
	{
		return incompatible;
	}	
	
	public int numOfConstraints()
	{
		return constrVal;
	}
}