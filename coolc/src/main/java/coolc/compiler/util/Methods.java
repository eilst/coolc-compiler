package coolc.compiler.util;


import java.util.LinkedList;

public class Methods {
	
	String returnType;
	String classType;
	LinkedList<Formals> formals;
	
	
	
	public Methods(String returnType, String classType, LinkedList<Formals> formals) {
		super();
		this.returnType = returnType;
		this.classType = classType;
		this.formals = formals;
	}
	
	
	
	public LinkedList<Formals> getFormals() {
		return formals;
	}



	public void setFormals(LinkedList<Formals> formals) {
		this.formals = formals;
	}



	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = classType;
	}
	
	
}
