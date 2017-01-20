package coolc.compiler;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PushbackReader;

import coolc.compiler.autogen.lexer.LexerException;
import coolc.compiler.autogen.node.EOF;
import coolc.compiler.autogen.node.Token;
import coolc.compiler.autogen.parser.Parser;
import coolc.compiler.util.Util;


/* No mover nada en esta clase excepto el nombre del archivo de prueba */
public class Main {
	public static String file = "src/test/resources/test.cool";
	
	private CoolLexer lexer;

	
	public CoolLexer getLexer() {
		return lexer;
	}

	public static void main(String [] args) throws LexerException, IOException {
		Main m = new Main();
		
		if (args.length > 0) {
			file = args[0];
		}
		
		try {
			m.lexerCheck(file, System.err);
		} catch (Exception e) {

			System.err.format("Compilation halted due to parse errors.\n");
			
			System.exit(-1);
		}
	}
	
	public void lexerCheck(String file, PrintStream out) throws LexerException, IOException {
		CoolLexer lexer = new CoolLexer(new PushbackReader(new FileReader(file)), out);
		Parser p = new Parser(lexer);
		lexer.setParser(p);
		
		 new CoolLexer(new PushbackReader(new FileReader(file)), out);
		
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

	

}
