package coolc.compiler.util;

public class Symbol {
	
	String type;
	String scopeType;
	String classType;
	
	public Symbol(String type, String scopeType, String classType) {
		this.type = type;
		this.scopeType = scopeType;
		this.classType = classType;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getScopeType() {
		return scopeType;
	}
	public void setScopeType(String scopeType) {
		this.scopeType = scopeType;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = classType;
	}
	
	
	
}