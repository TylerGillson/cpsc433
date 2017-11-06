import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;



public class SlotMap {
    private HashMap<String, int[] > hmap = new HashMap<String, int[]>();
    private static SlotMap instance;

    public SlotMap() {
    	instance = new SlotMap();
    }
    
    public SlotMap getInstances() {

    	return instance;	
    }
    public void add(String day, String time, int max, int min) {
    	int[] maxmin = new int [2];
    	maxmin[0]=max;
    	maxmin[1]=min;
    	
    	String key = day+'-'+time;
    	
    	hmap.put(key,maxmin);
    }
    
    public int getMin(String day, String time) {
    	int[] maxmin;
      	String key = day+'-'+time;
    	maxmin = hmap.get(key);
    	return maxmin[1];
    }
    
    public int getMax(String day, String time) {
    	int[] maxmin;
      	String key = day+'-'+time;	
    	maxmin = hmap.get(key);
    	return maxmin[0];
    	
    }
    
    public void remove(String day, String time) {
      	String key = day+'-'+time;	
    	hmap.remove(key);
    }

}
