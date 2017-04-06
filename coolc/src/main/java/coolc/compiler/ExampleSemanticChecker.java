package coolc.compiler;

import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Set;

import coolc.compiler.ErrorManager;
import coolc.compiler.autogen.node.Start;
import coolc.compiler.exceptions.SemanticException;
import coolc.compiler.util.Error;
import coolc.compiler.visitors.ExampleVisitor;
import coolc.compiler.visitors.IdentifierValidator;

public class ExampleSemanticChecker implements SemanticFacade {

	private Start start;
	private PrintStream out;

	@Override
	public void setup(Start start, PrintStream out) {
		this.start = start;
		this.out = out;
	}

	@Override
	public void check() throws SemanticException {
		// Set the errors to print on whatever PrintStream we are working on
		ErrorManager.getInstance().setOut(out);

		// Here instantiate and call whatever visitors you need
		start.apply(new ExampleVisitor());
		start.apply(new IdentifierValidator());
		
		
		
		// The visitors may have added erros to the set
		if (ErrorManager.getInstance().getSemanticErrors() > 0) {
			throw new SemanticException();
		}
	}

	@Override
	public Set<Error> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

}
