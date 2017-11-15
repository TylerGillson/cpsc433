import java.util.Arrays;

public class OrTree {
	private int[] pr;
	
	public OrTree(int length) {
		this.pr = new int[length];
		System.out.println(Arrays.toString(this.pr));
	}
	
	public OrTree(int[] pr) {
		this.pr = pr;
	}

}
