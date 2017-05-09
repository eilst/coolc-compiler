package coolc.compiler;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import coolc.compiler.TableC;
import coolc.compiler.util.Error;
import coolc.compiler.visitors.VisitorF;
import coolc.compiler.visitors.Simbols;
import coolc.compiler.autogen.node.Start;
import coolc.compiler.exceptions.SemanticException;
import coolc.compiler.visitors.ExampleVisitor;
import coolc.compiler.visitors.IdentifierValidator;
import coolc.compiler.ErrorManager;

public class ExampleSemanticChecker implements SemanticFacade {

	private Start start;
	private PrintStream out;
	Set<Error> errors;
	ErrorManager errorManager;
	TableC tableC;

	@Override
	public void setup(Start start, PrintStream out) {
		this.start = start;
		this.out = out;
		errors = new HashSet<>();
		errorManager = ErrorManager.getInstance();
		errorManager.reset();
		tableC = TableC.getInstance();
		tableC.reset();
	}
	

	@Override
	public void check() throws SemanticException {
		// Set the errors to print on whatever PrintStream we are working on
		

		// Here instantiate and call whatever visitors you need
		//start.apply(new ExampleVisitor());
		//start.apply(new IdentifierValidator());
				//1st
				Simbols sv = new Simbols();
				start.apply(sv);
				
				if(tableC.getTable().get("Main") == null){
					errorManager.getErrors().add(Error.NO_MAIN);
				}
				if(sv.grafo.findCycles().length > 0){
					errorManager.getErrors().add(Error.CYCLE);
					throw new SemanticException();
				}
				
				if(errorManager.getErrors().size() > 0){
					throw new SemanticException();
				}
				
				
				//2ND
				ExampleVisitor hv = new ExampleVisitor();
				start.apply(hv);
				
				if(errorManager.getErrors().size() > 0){
				throw new SemanticException();
				}
		
				
				//3RD
				VisitorF scv = new VisitorF();
				start.apply(scv);
				if(errorManager.getErrors().size() > 0){
					throw new SemanticException();
				}
				
		
	}

	@Override
	public Set<Error> getErrors() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
				return errorManager.getErrors();
	}

}
