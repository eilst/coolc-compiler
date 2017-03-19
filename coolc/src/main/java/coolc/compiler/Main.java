package coolc.compiler;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PushbackReader;

import coolc.compiler.CoolcLexer;
import coolc.compiler.autogen.node.Start;
import coolc.compiler.autogen.parser.ParserException;
import coolc.compiler.visitors.ASTPrinter;
import coolc.compiler.autogen.lexer.LexerException;
import coolc.compiler.autogen.node.EOF;
import coolc.compiler.autogen.node.Token;
import coolc.compiler.autogen.parser.Parser;
import coolc.compiler.util.Util;


/* Do not move anything below except the name of the test file */
public class Main {
	public static String file = "src/test/resources/test.cool";
	
	private CoolcLexer lexer;
	private Parser parser;

	
	public CoolcLexer getLexer() {
		return lexer;
	}

	public static void main(String [] args) throws LexerException, IOException {
		Main m = new Main();
		
		if (args.length > 0) {
			file = args[0];
		}
		
		try {
			m.parseCheck(file, System.err);
		} catch (Exception e) {

			System.err.format("Compilation halted due to parse errors.\n");
			
			System.exit(-1);
		}
	}
	
	public void lexerCheck(String file, PrintStream out) throws LexerException, IOException {
		CoolcLexer lexer = new CoolcLexer(new PushbackReader(new FileReader(file)), out);
		Parser p = new Parser(lexer);
		lexer.setParser(p);
		
		
		out.format("#name %s\n", file);
		while(true) {
			Token token = lexer.next();
			
			if (token instanceof EOF) {
				break;
			}			
			
			out.format("#%d %s \"%s\"\n", 
					token.getLine(),
					token.getClass().getSimpleName(),
					Util.escapeString(token.getText()));		
		}

	}
	
	public void parseCheck(String file, PrintStream out) throws LexerException, IOException, ParserException {
		lexer = new CoolcLexer(new PushbackReader(new FileReader(file)), out);
		parser = new Parser(lexer);
		lexer.setParser(parser);
		
		Start start = parser.parse();		

		start.apply(new ASTPrinter(out));		
	}
	

}
