import java.util.*;

public class Course
{
	private final int EVE_VAL = 100;
	private List<String> name;
	private boolean evening = false;
	private ArrayList<ArrayList<List<String>>> unwanted = new ArrayList<ArrayList<List<String>>>();
	private ArrayList<List<String>> incompatible = new ArrayList<List<String>>();
	private ArrayList<List<String>> labList = new ArrayList<List<String>>();
	private int index;
	private int constrVal = 0;
	
	public Course(String type, int idx)
	{
		// Set name and index:
		index = type.equals("course") ? idx : idx + Driver.courses.size();
		name = type.equals("course") ? Driver.courses.get(idx) : Driver.labs.get(idx);
		
		// Set evening:
		if (name.get(3).length() > 0 && name.get(3).charAt(0) == '9')
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
				unwanted.add(Driver.unwanted.get(i));	
		}
		
		// Build lab list:
		for (int i = 0; i < Driver.labs.size(); i++) {
			List<String> labName = Driver.labs.get(i);
			if (labName.containsAll(name))
				labList.add(labName);
		}
		
		// Calculate constrVal:
		constrVal = unwanted.size() + incompatible.size() + labList.size() + (evening ? EVE_VAL : 0);
	}
	
	public int getIndex()
	{
		return index;
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
	
	public ArrayList<ArrayList<List<String>>> getUnwanted()
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
