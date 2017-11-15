import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class OrTree<T>{
	
	public int[] data;
	public OrTree<T> parent;
	public List<OrTree<T>> children;
	
	public OrTree(int[] pr){
		this.data = pr;
		this.children = new LinkedList<OrTree<T>>();
	}
	
	public OrTree(int length){
		this.data = new int[length];
		this.children = new LinkedList<OrTree<T>>();
	}
	
	public OrTree<T> addChild(int[] child){
		OrTree<T> childNode = new OrTree<T>(child);
		childNode.parent = this;
		this.children.add(childNode);
		return childNode;
	}
	
	public boolean isRoot(){
		return parent == null;
	}
	public boolean isLeaf(){
		return children.size() == 0;
	}
	
	public int getLevel(){
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}
	
	public String toString(){
		String output = Arrays.toString(this.data);
		
		if (!children.isEmpty()){
			Iterator<OrTree<T>> kids = children.iterator();
			while (kids.hasNext())
				output = output + Arrays.toString(kids.next().data);
		}
		return output;
	}
}

