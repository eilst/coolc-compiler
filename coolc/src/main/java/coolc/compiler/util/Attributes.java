package coolc.compiler.util;


public class Attributes {
	
	String type;
	String classType;
	
	public Attributes(String type, String classType) {
		this.type = type;
		this.classType = classType;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = classType;
	}
	
	
}
