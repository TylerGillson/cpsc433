import java.io.*;
import java.util.*;


public class Parser{
	private String fileName;
	private String line = null;
	private String filecontents;
	private String name;
	private ArrayList<List<String>> course_slots = new ArrayList<List<String>>();
	private ArrayList<List<String>> lab_slots = new ArrayList<List<String>>();
	private ArrayList<List<String>> courses = new ArrayList<List<String>>();
	private ArrayList<List<String>> labs = new ArrayList<List<String>>();
	private ArrayList<ArrayList<List<String>>> not_compatible = new ArrayList<ArrayList<List<String>>>();
	private ArrayList<ArrayList<List<String>>> unwanted = new ArrayList<ArrayList<List<String>>>();
	private ArrayList<ArrayList<List<String>>> preferences = new ArrayList<ArrayList<List<String>>>();
	private ArrayList<ArrayList<List<String>>> pair = new ArrayList<ArrayList<List<String>>>();
	private ArrayList<ArrayList<List<String>>> part_assign = new ArrayList<ArrayList<List<String>>>();
	
	public String getName() {
		return name;
	}
	public ArrayList<List<String>> getCourseSlots() {
		return course_slots;
	}
	public ArrayList<List<String>> getLabSlots() {
		return lab_slots;
	}
	public ArrayList<List<String>> getCourses() {
		return courses;
	}
	public ArrayList<List<String>> getLabs() {
		return labs;
	}
	public ArrayList<ArrayList<List<String>>> getNotCompatible() {
		return not_compatible;
	}
	public ArrayList<ArrayList<List<String>>> getUnwanted() {
		return unwanted;
	}
	public ArrayList<ArrayList<List<String>>> getPreferences() {
		return preferences;
	}
	public ArrayList<ArrayList<List<String>>> getPair() {
		return pair;
	}
	public ArrayList<ArrayList<List<String>>> getPartAssign() {
		return part_assign;
	}
	
	public Parser() {}
	public Parser(String name) {
		fileName = name;	
	}
	
	public void build() {
		try {
		    // FileReader reads text files
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
		
			while((line = bufferedReader.readLine()) != null) {
				if (filecontents == null)
					filecontents = line+"\n";
				else
					filecontents += line+"\n";
			}   
			// Always close files.
			bufferedReader.close();     
			
			String cut[] = filecontents.split("\n\n");
			for (String s: cut) {
				info_extractor(s);
			}

		}
		catch(Exception e) {
		    e.printStackTrace();
			System.out.println("Unable to open file '" + fileName + "'");                
		}
	}
	
	/**
	 * Parses each string in an array of strings, creating a list of lists of data.
	 * @param list 			- Data structure to be filled.
	 * @param ls   			- Array of strings to be parsed.
	 * @param split_flag	- Character sequence to split by.
	 */
	private void buildList1(ArrayList<List<String>> list, String[] ls, String split_flag) { 
		if (ls.length > 1){
			for (String l : ls[1].split("\n")){
				l = l.replaceAll(" +", " ");
				List<String> vals = new ArrayList<String>();
				
				for (String item : l.split(split_flag)){
					item = item.replaceAll(" +", "");
					vals.add(item);
				}
				list.add(vals);
			}
		}
	}
	
	/**
	 * Parses each string in an array of strings, creating a list of lists of lists of data.
	 * @param list - Data structure to be filled.
	 * @param ls   - Array of strings to be parsed.
	 */
	private void buildList2(ArrayList<ArrayList<List<String>>> list, String[] ls) { 
		if (ls.length > 1){
			for (String l : ls[1].split("\n")){
				l = l.trim().replaceAll(" +", " ");
				ArrayList<List<String>> vals = new ArrayList<List<String>>();
				
				for (String item : l.split(",")){
					item = item.trim().replaceAll(" +", " ");
					List<String> val = new ArrayList<String>();
					
					for (String elem : item.split(" "))
						val.add(elem);
					
					vals.add(val);
				}
				list.add(vals);
			}
		}
	}
	
	/**
	 * Identifies input sub-sections and handles them individually.
	 * @param str	- A string containing raw input.
	 */
	private void info_extractor(String str) {
		String[] ls = str.split(":\n");
		
		switch (ls[0].toLowerCase().trim()) {
			case "name":
				this.name = ls[1];
				break;
			case "course slots":
				buildList1(this.course_slots,ls,",");
				break;
			case "lab slots":
				buildList1(this.lab_slots,ls,",");
				break;
			case "courses":
				buildList1(this.courses,ls," ");
				break;
			case "labs":
				buildList1(this.labs,ls," ");
				break;
			case "not compatible":
				buildList2(this.not_compatible,ls);
				break;
			case "unwanted":
				buildList2(this.unwanted,ls);
				break;
			case "preferences":
				buildList2(this.preferences,ls);
				break;
			case "pair":
				buildList2(this.pair,ls);
				break;
			case "partial assignments":
				buildList2(this.part_assign,ls);
				break;
			default:
				System.out.println("No input data for: " + ls[0].substring(0, ls[0].length()-1));	
		}	
	}	
}
