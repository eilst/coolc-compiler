package coolc.compiler.util;


public class Formals {
	
	String name;
	String type;
	
	public Formals(String name,String type) {
		super();
		this.type = type;
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
