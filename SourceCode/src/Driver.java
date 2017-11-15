
public class Driver {
    public static void main(String[] args) {
    	String filename = args[0];
    	Parser p = new Parser(filename);
    	p.build();
    	
    	if (!p.getPartAssign().isEmpty()){
    		System.out.println(p.getPartAssign());
    		//p.getPartAssign().iterator(). {
    		
    		OrTree oTree = new OrTree(2);
    	}
    	else {
    		int pr_size = p.getCourses().size() + p.getLabs().size();
    		OrTree oTree = new OrTree(pr_size);
    	}
    }
}
