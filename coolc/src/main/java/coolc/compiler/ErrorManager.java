package coolc.compiler;

import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Set;

import coolc.compiler.util.Errors;
import coolc.compiler.util.Messages;

public class ErrorManager {
	private static ErrorManager instance;
	private int semantic;
	private PrintStream out;
	private Set<Errors> errors;
	
	public Set<Errors> getErrors() {
		return errors;
	}

	public void setErrors(Set<Errors> errors) {
		this.errors = errors;
	}

	private ErrorManager() {
	}
	
	public int getSemantic() {
		return semantic;
	}
	
	public void setOut(PrintStream _out) {
		out = _out;
	}
	
	public static ErrorManager getInstance() {
		if (instance == null) {
			instance = new ErrorManager();
			instance.setErrors(EnumSet.noneOf(Errors.class));	
		}
		
		return instance;
	}
	
	public void semantic(String msg, Object ... args) {
		semantic++;
		if (out != null) {			
			errors.add(Errors.get(msg));
			out.format(Messages.getString(msg), args);
		}
	}
	
	public void fatal(String msg, Object ... args) {
		if (out != null) {
			out.format(Messages.getString(msg), args);
		}
		System.exit(1);
	}
	
	public void reset() {
		semantic = 0;
		errors = EnumSet.noneOf(Errors.class);	
	}
}
