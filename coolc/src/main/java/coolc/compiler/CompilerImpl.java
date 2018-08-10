package coolc.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PushbackReader;
import java.util.Set;

import coolc.compiler.autogen.lexer.LexerException;
import coolc.compiler.autogen.node.Start;
import coolc.compiler.autogen.parser.Parser;
import coolc.compiler.autogen.parser.ParserException;
import coolc.compiler.exceptions.SemanticException;
import coolc.compiler.util.Error;
import coolc.compiler.visitors.ASTPrinter;
import coolc.compiler.visitors.ASTPrinterTypes;

public class CompilerImpl implements Compiler {
	public static String file = "src/test/resources/test.cool";
	//public static String outFile = "src/test/resources/main.cool.s";
	
	private CoolcLexer lexer;
	private Parser parser;
	private SemanticFacade semantic;
	private CodegenFacade codegen;
	
	public CoolcLexer getLexer() {
		return lexer;
	}
	
	/*
	 * This main method is just for convenience, remember the test methods (from the Compiler
	 * interface) are the ones called from the tests.
	 */
	public static void main(String [] args) throws LexerException, IOException, ParserException {
		CompilerImpl compiler = new CompilerImpl();
		Start start = null;
		
		if (args.length > 0) {
			file = args[0];
		}
		
//		Instantiate YOUR concrete classes here!
//		If semantic and codegen are null, you will only get NullPointerException
//		Example:
		compiler.setup(new ExampleSemanticChecker(), new ARMCodegen());		
		
		try {
			start = compiler.lexAndParse(new File(file), System.err);
		} catch (ParserException e) {
			System.err.format("\"%s\", Syntax error at or near [%s]\nLast good token was [%s]\n%s\n",
					file, e.getToken().getText(), compiler.getLexer().getLastToken().getText(),
					e.getMessage());
			System.err.format("\nCompilation halted due to parse errors.\n");			
			System.exit(-1);
		}

		try {
			compiler.semanticCheck(start, System.err);
		} catch (SemanticException e) {
			System.err.format("\nCompilation halted due to semantic errors.\n");			
			System.exit(-1);
		}
		
		// If no errors, print the AST WITH TYPES!!!
		//System.out.println("AASSTT");
		start.apply(new ASTPrinterTypes(System.out));
		
		// When generating code, uncomment this:
//		PrintStream out = new PrintStream(new FileOutputStream(outFile));
//		compiler.genCode(start, out);
	//	compiler.genCode(start, System.out);
		
	}
	
	@Override
	public Start lexAndParse(File file, PrintStream out) throws LexerException, IOException, ParserException {
		lexer = new CoolcLexer(new PushbackReader(new FileReader(file)), out);
		parser = new Parser(lexer);
		lexer.setParser(parser);
		
		Start start = parser.parse();
		return start;
	}
	
	public void semanticCheck(Start start, PrintStream out) throws SemanticException {
		// Here it is assumed semantic was set elsewhere
		semantic.setup(start, out);
		semantic.check();
	}
	
	public void genCode(Start start, PrintStream out) {
		// Here it is assumed codegen was set elsewhere
		codegen.setup(start, out);
		codegen.gen();
	}

	@Override
	public void setup(SemanticFacade s, CodegenFacade c) {
		semantic = s;
		codegen = c;		
	}

	@Override
	public Set<Error> getErrors() {		
		return semantic.getErrors();
	}

}
