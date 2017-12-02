import java.util.*;

public class Course
{
	private List<String> name;
	private boolean evening = false;
	private ArrayList<List<String>> unwanted = new ArrayList<List<String>>();
	private ArrayList<List<String>> incompatible = new ArrayList<List<String>>();
	private int constrVal = 0;
	private ArrayList<List<String>> labList;
	
	// Still need to deal with adding labs of courses to incompatible, 500 level courses, etc.
	public Course(String type, int idx)
	{
		// Set name:
		name = type.equals("course") ? Driver.courses.get(idx) : Driver.labs.get(idx);
		
		// Set evening:
		if (name.get(3).equals("LEC 09"))
			evening = true;
		
		// Populate not_compatible:
		for (int i = 0; i < Driver.not_compatible.size(); i++)
		{
			if (Driver.not_compatible.get(i).get(0).equals(name)){
				incompatible.add(Driver.not_compatible.get(i).get(1));
				break;
			}
			if (Driver.not_compatible.get(i).get(1).equals(name))
				incompatible.add(Driver.not_compatible.get(i).get(0));			
		}
		
		// Populate unwanted:
		for (int i = 0; i < Driver.unwanted.size(); i++)
		{
			if (Driver.unwanted.get(i).get(0).equals(name))
				unwanted.add(Driver.unwanted.get(i).get(1));	
		}
		
		// Calculate constrVal:
		constrVal = unwanted.size() + incompatible.size();
		
		//build lab list
		for (int i = 0; i < Driver.labs.size(); i++) {
			List<String> labName =Driver.labs.get(i);
			if (labName.containsAll(name))
				labList.add(labName);
		}
	}
	
	public List<String> getName()
	{
		return name;
	}
	
	public ArrayList<List<String>> getLabList(){
		return labList;
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
	
	public String toString(){
		return "Name: " + name + " Evening: " + String.valueOf(evening) + " constrVal: " + String.valueOf(constrVal);
	}
}
