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
			/*
			System.out.println("name: "+name);
			System.out.println("course_slots: "+course_slots);
			System.out.println("lab_slots: "+lab_slots);
			System.out.println("courses: "+courses);
			System.out.println("labs: "+labs);
			System.out.println("not_compatible: "+not_compatible);
			System.out.println("unwanted: "+unwanted);
			System.out.println("preferences: "+preferences);
			System.out.println("pair: "+pair);
			System.out.println("part_assign: "+part_assign);
			*/
		}
		catch(Exception e) {
		    e.printStackTrace();
			System.out.println("Unable to open file '" + fileName + "'");                
		}
	}
	
	private void buildList(ArrayList<List<String>> list, String[] ls, String split_flag) { 
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
	
	private void build2DList(ArrayList<ArrayList<List<String>>> list, String[] ls) { 
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
	
	private void info_extractor(String str) {
		String[] ls = str.split(":\n");
		
		switch (ls[0].toLowerCase().trim()) {
			case "name":
				this.name = ls[1];
				break;
			case "course slots":
				buildList(this.course_slots,ls,",");
				break;
			case "lab slots":
				buildList(this.lab_slots,ls,",");
				break;
			case "courses":
				buildList(this.courses,ls," ");
				break;
			case "labs":
				buildList(this.labs,ls," ");
				break;
			case "not compatible":
				build2DList(this.not_compatible,ls);
				break;
			case "unwanted":
				build2DList(this.unwanted,ls);
				break;
			case "preferences":
				build2DList(this.preferences,ls);
				break;
			case "pair":
				build2DList(this.pair,ls);
				break;
			case "partial assignments":
				build2DList(this.part_assign,ls);
				break;
			default:
				System.out.println("Cannot assign " + ls[0]);	
		}	
	}	
}
