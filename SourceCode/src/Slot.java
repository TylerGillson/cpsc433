import java.util.*;

public class Slot
{
	private String name;
	private boolean evening = false;
	private int max;
	
	public Slot (String slotName, int slotMax)
	{
		name = slotName;
		max = slotMax;
		//Need to dertemine if evening slot
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean getEvening()
	{
		return evening;
	}

	public int getMax()
	{
		return max;
	}
}