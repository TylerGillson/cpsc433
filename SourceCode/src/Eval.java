import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//this code is trying to optimize for speed
public class Eval {
	private int pen_coursemin = 1;
	private int pen_labmin = 1;
	private int not_paired = 1;
	private int pen_section = 1;
	private int W_minfilled = 1;
	private int W_pref = 1;
	private int W_pair = 1;
	private int W_secdiff = 1;
	private HashMap<List<List<String>>,Integer> pref = new HashMap<List<List<String>>,Integer>();
	private HashMap<List<String>,Integer> course_slots = new HashMap<List<String>,Integer>();
	private HashMap<List<String>,Integer> lab_slots = new HashMap<List<String>,Integer>();

	public Eval(String configFile) {
		//load config file
		try {
		    // FileReader reads text files
			FileReader fileReader = new FileReader(configFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while((line = bufferedReader.readLine()) != null) {
				line = line.replaceAll("\\s", "");
				String[] setting = line.split("=");
				if (setting[0].equals("minfilledWeight")) {
					this.W_minfilled = Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("prefWeight")) {
					this.W_pref = Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("pairWeight")) {
					this.W_pair= Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("secdiffWeight")){
					this.W_secdiff= Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("courseminPenalty")) {
					this.pen_coursemin = Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("labminPenalty")) {
					this.pen_labmin= Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("notpairedPenalty")) {
					this.not_paired= Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("sectionPenalty")) {
					this.pen_section = Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("pop_init")) {
					Driver.pop_init = Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("pop_max")) {
					Driver.pop_max = Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("cull_num")) {
					Driver.cull_num = Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("gen_max")) {
					Driver.gen_max = Integer.parseInt(setting[1]); 
				}
				else if (setting[0].equals("print_data")) {
					Driver.print_data = Boolean.parseBoolean(setting[1]); 
				}
				else if (setting[0].equals("print_prs")) {
					Driver.print_prs = Boolean.parseBoolean(setting[1]); 
				}
				else;			
			}   

			// Always close files.
			bufferedReader.close();     
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to load config file");                
		}
		
		//set up hashmap for preference to perform faster search, O(1) vs O(n)
		for (ArrayList<List<String>> Info : Driver.preferences) {
			List<List<String>> key = new ArrayList<List<String>>(3);
			key.add(Info.get(0));
			key.add(Info.get(1));
			key.add(Info.get(2));
			int value = Integer.parseInt(Info.get(3).get(0));
			pref.put(key, value);
		}
		
		for (List<String> Slot : Driver.course_slots) {
			List<String> key = new ArrayList<String>(2);
			key.add(Slot.get(0));
			key.add(Slot.get(1));
			int value = 0;
			course_slots.put(key, value);
		}
		
		for (List<String> Slot : Driver.lab_slots) {
			List<String> key = new ArrayList<String>(2);
			key.add(Slot.get(0));
			key.add(Slot.get(1));
			int value = 0;
			lab_slots.put(key, value);
		}
	}
	
	public int getValue(int[] assign, boolean show_data) {
		int[] cAssign = Arrays.copyOfRange(assign, 0, Driver.courses.size());
		int[] lAssign = Arrays.copyOfRange(assign, Driver.courses.size(), assign.length);

		int minValue     = W_minfilled * E_minfilled(cAssign,lAssign);
		int prefValue    = W_pref      * E_pref(cAssign,lAssign);
		int pairValue    = W_pair      * E_pair(cAssign,lAssign);
		int secdiffValue = W_secdiff   * E_secdiff(cAssign,lAssign);
		
		if (show_data) System.out.println("Min: "+minValue+" Pref: "+prefValue+" Pair: "+pairValue+" SecDiff: "+secdiffValue);
		
		return minValue+prefValue+pairValue+secdiffValue;
	}
	
	//eval minfilled
	private int E_minfilled(int[] courseAssign, int[] labAssign) {
		// for each min not meet +1
		int result = 0; 
		int[] CassignCount = new int[Driver.course_slots.size()];
		int[] LassignCount = new int[Driver.lab_slots.size()];
		
		//count the total of assign of each slot
		for (int i = 0; i < courseAssign.length; i++) {
			if (courseAssign[i] != -99)
				CassignCount[courseAssign[i]] += 1;
		}
		for (int i = 0; i < labAssign.length;i++) {
			if (labAssign[i] != -99)
				LassignCount[labAssign[i]] += 1;
		}
	
		for (int i = 0; i < Driver.course_slots.size(); i ++) {
			List<String> slotInfo= Driver.course_slots.get(i);
			String x = slotInfo.get(3);
			int minValue = Integer.parseInt(x);

			if (CassignCount[i] < minValue)
				result += this.pen_coursemin;
		}
			
		for (int i = 0; i< Driver.lab_slots.size(); i ++) {
			List<String> slotInfo=(List<String>) Driver.lab_slots.get(i);
			String x = slotInfo.get(3);
			int minValue = Integer.parseInt(x);
			
			if (LassignCount[i] < minValue)
				result += this.pen_labmin;
		}
		
		return result;
	}
	
	private int E_pref(int[] courseAssign, int[] labAssign) {
		int result = 0;
		
		for (ArrayList<List<String>> item : Driver.preferences) {
			//get the classname and its pref value
			List<String> classname = item.get(2);
			int pref_value = Integer.parseInt(item.get(3).get(0));
			
			List<String> day = new ArrayList<String>();
			List<String> time = new ArrayList<String>();
			
			List<String> slot_check = new ArrayList<String>();
			
			//if this is a lab get from labAssign
			if (classname.contains("TUT") || classname.contains("LAB")) {
				slot_check.add(item.get(0).get(0));
				slot_check.add(item.get(1).get(0));
				
				// Skip if the preference time slot does not exist:
				if (!lab_slots.containsKey(slot_check))
					continue;
				
				int labAssignIndex = Driver.labs.indexOf(classname);
				if (labAssignIndex < 0) {
					//System.out.println(item.toString());
					continue;
				}
				int slotIndex = labAssign[labAssignIndex];
				List<String> slotInfo = Driver.lab_slots.get(slotIndex);
				day.add(slotInfo.get(0));
				time.add(slotInfo.get(1));
			}
			//if this is a course get from courseAssign
			else {
				slot_check.add(item.get(0).get(0));
				slot_check.add(item.get(1).get(0));
				
				// Skip if the preference time slot does not exist:
				if (!course_slots.containsKey(slot_check))
					continue;
				
				int courseAssignIndex = Driver.courses.indexOf(classname);
				if (courseAssignIndex < 0) {
					//System.out.println(item.toString());
					continue;
				}
				int slotIndex = courseAssign[courseAssignIndex];
				List<String> slotInfo = Driver.course_slots.get(slotIndex);
				day.add(slotInfo.get(0));
				time.add(slotInfo.get(1));
			}
			
			// Check if the preference is met by the assignment:
			if (!(item.get(0).equals(day) && item.get(1).equals(time))) {
				result += pref_value;
			}
				
		}
		
		return result;
	}
	
	private int E_pair(int[] courseAssign, int[] labAssign) {
		int result = 0;
		//method 3: convert the pair input into slots assigned to theses classes, then check for matching
		for (int i = 0; i < Driver.pair.size(); i++) {
			List<List<String>> pairX = Driver.pair.get(i);
			List<String> pairX1 = pairX.get(0);
			List<String> pairX2 = pairX.get(1);
			
			//find the time for left 
			List<String> X1Time = new ArrayList<>(); 
			int indexX1 = Driver.courses.indexOf(pairX1);
			if (indexX1 == -1) {
				indexX1 = Driver.labs.indexOf(pairX1);
				int slotIndex = labAssign[indexX1];
				
				if (slotIndex == -99)
					continue;
				
				List<String> x = Driver.lab_slots.get(slotIndex);
				X1Time.add(x.get(0));
				X1Time.add(x.get(1));

			}
			else {
				int slotIndex = courseAssign[indexX1];
				
				if (slotIndex == -99)
					continue;
				
				List<String> x =Driver.course_slots.get(slotIndex);
				X1Time.add(x.get(0));
				X1Time.add(x.get(1));
			}
					
			//find the time for right
			List<String> X2Time = new ArrayList<>(); 
			int indexX2 = Driver.courses.indexOf(pairX2);
			if (indexX2 == -1) {
				indexX2 = Driver.labs.indexOf(pairX2);
				int slotIndex = labAssign[indexX2];
				
				if (slotIndex == -99)
					continue;
				
				List<String> x =Driver.lab_slots.get(slotIndex);
				X2Time.add(x.get(0));
				X2Time.add(x.get(1));

			}
			else {
				int slotIndex = courseAssign[indexX2];
				
				if (slotIndex == -99)
					continue;
				
				List<String> x =Driver.course_slots.get(slotIndex);
				X2Time.add(x.get(0));
				X2Time.add(x.get(1));
			}
			
			if (!X1Time.get(0).equals(X2Time.get(0)) || !X1Time.get(1).equals(X2Time.get(1)))
				result += this.not_paired;
		}
		return result;
	}
	
	private int E_secdiff(int[] cAssign, int[]lAssign) {
		int result = 0;
		//setup record to reduce the runtime to O(kn)
		HashMap<List<String>,List<List<String>>> courseIndex = new HashMap<List<String>,List<List<String>>>();

		//setting up record of course-index for secdiff, to avoid nested loop 
		for (int i = 0 ; i < Driver.courses.size(); i++) {
			List<String> key = new ArrayList<>();
			
			//get the name of course
			List<String> coursename = Driver.courses.get(i);
			key.add(coursename.get(0));
			key.add(coursename.get(1));
			
			//get its corresponding value in the map
			List<List<String>> value = new ArrayList<>();
			if (courseIndex.containsKey(key))
				value = courseIndex.get(key);
			
			if (cAssign[i] == -99)
				continue;
			
			List<String> slotinfo = Driver.course_slots.get(cAssign[i]);	
			List<String> slotTime = new ArrayList<>();
			slotTime.add(slotinfo.get(0));
			slotTime.add(slotinfo.get(1));

			value.add(slotTime);
			courseIndex.put(key,value);
		}

		for (List<String> key : courseIndex.keySet()) {

			List<List<String>> value = courseIndex.get(key);
			if (value.size() > 1) {	
				HashMap<List<String>,Integer> Slot_AssignValue = new HashMap<>();
				
				for (List<String> e : value) {
					if (Slot_AssignValue.containsKey(e) == false)
						Slot_AssignValue.put(e, 1);
					else {
						int counts = Slot_AssignValue.get(e);
						counts++;
						Slot_AssignValue.put(e, counts);
					}		
				}
				
				for (List<String> k : Slot_AssignValue.keySet()) {
					if (Slot_AssignValue.get(k) > 1)
						result += (Slot_AssignValue.get(k)-1) * this.pen_section;
				}
			}
		}
		return result;
	}
}
