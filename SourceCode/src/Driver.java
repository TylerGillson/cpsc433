import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Driver {
    public static void main(String[] args) {
    	String filename = args[0];
    	Parser p = new Parser(filename);
    	p.build();
    	
    	int pr_size = p.getCourses().size() + p.getLabs().size();
    	if (!p.getPartAssign().isEmpty()){
    		int[] pr = new int[pr_size];
    		Arrays.fill(pr, -99);
    		
    		ArrayList<List<String>> courses = p.getCourses();
    		ArrayList<List<String>> labs = p.getLabs();
    		ArrayList<List<String>> courseSlots = p.getCourseSlots();
    		ArrayList<List<String>> labSlots = p.getLabSlots();
    		
    		Iterator<ArrayList<List<String>>> partAssign = p.getPartAssign().iterator(); 
    		while (partAssign.hasNext()) {
    			ArrayList<List<String>> assign = partAssign.next();
    			    			
    			int pr_idx = -99;
    			int slot_idx = -99;
    			int course_idx = courses.indexOf(assign.get(0));
    			if (course_idx != -1) {
    				pr_idx = course_idx;
    				Iterator<List<String>> slots = courseSlots.iterator();
    				while (slots.hasNext()){
    					List<String> slot = slots.next();
    					if (slot.get(0).equals(assign.get(1).get(0)) && slot.get(1).equals(assign.get(2).get(0))){
    						slot_idx = courseSlots.indexOf(slot);
    					}
    				}
    			}
    			else {
    				pr_idx = courses.size() + labs.indexOf(assign.get(0));
    				Iterator<List<String>> slots = labSlots.iterator();
    				while (slots.hasNext()){
    					List<String> slot = slots.next();
    					if (slot.get(0).equals(assign.get(1).get(0)) && slot.get(1).equals(assign.get(2).get(0))){
    						slot_idx = labSlots.indexOf(slot);
    					}
    				}
    			}
    			/*
    			System.out.println(assign);
    			System.out.println(pr_idx);
    			System.out.println(slot_idx);
    			*/
    			pr[pr_idx] = slot_idx;
    		}
    		OrTree oTree = new OrTree(pr);
    	}
    	else {
    		OrTree oTree = new OrTree(pr_size);
    	}
    }
}
