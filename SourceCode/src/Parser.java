import java.io.*;
import java.util.*;


public class Parser{
	private String fileName = "input.txt";
	private String line = null;
	private String filecontents;
	private String[] name;
	private String[] course_slots;
	private String[] lab_slots;
	private String[] courses;
	private String[] labs;
	private String[] not_compatible;
	private String[] Unwanted;
	private String[] preferences;
	private String[] pair;
	private String[] partial_assignments;
	
	
	
	public Parser() {}
	public Parser(String name) {
		fileName = name;	
	}
	
	
	public void build() {
		try {
		    // FileReader reads text files
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
		
			//
			while((line = bufferedReader.readLine()) != null) {
				if (filecontents == null)
					filecontents = line+"\n";
				else
					filecontents += line+"\n";
			}   
			
			// Always close files.
			bufferedReader.close();     
			
			String cut[] = filecontents.split("\n\n");
			for (String e: cut) {
				info_extractor(e);
			}
			System.out.println(Arrays.toString(name));
			System.out.println(Arrays.toString(course_slots));
			System.out.println(Arrays.toString(lab_slots));
			System.out.println(Arrays.toString(courses));
			System.out.println(Arrays.toString(labs));


		}
		catch(Exception e) {
		    System.out.println("Unable to open file '" + fileName + "'");                
		}
	}
	
	private void info_extractor(String str) {
		String[] l = str.split(":\n");
		switch (l[0].toLowerCase().trim()) {
			case "name":
				this.name = l[1].split("\n");
				break;
			case "course slots":
				this.course_slots=l[1].split("\n");
				break;
			case "lab slots":
				this.lab_slots=l[1].split("\n");
				break;
			case "courses":
				this.courses=l[1].split("\n");
				break;
			case "labs":
				this.labs=l[1].split("\n");
				break;
			case "not compatible":
				this.not_compatible=l[1].split("\n");
				break;
			case "unwanted":
				this.Unwanted=l[1].split("\n");
				break;
			case "preferences":
				this.preferences=l[1].split("\n");
				break;
			case "pair":
				this.pair=l[1].split("\n");
				break;
			case "partial assignments":
				this.partial_assignments=l[1].split("\n");
				break;
			default:
				System.out.println("cannot assigns " + l[0].toLowerCase().trim());
				
		}


		
	}
	
	
}
