package coolc.compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import coolc.compiler.autogen.lexer.LexerException;
import coolc.compiler.autogen.node.Start;
import coolc.compiler.autogen.parser.ParserException;
import coolc.compiler.exceptions.SemanticException;
import coolc.compiler.util.Error;


public interface Compiler {
	public Start lexAndParse(File file, PrintStream output) throws LexerException, IOException, ParserException;
	public void semanticCheck(Start node, PrintStream output) throws SemanticException;
	public void genCode(Start node, PrintStream output);
	public void setup(SemanticFacade s, CodegenFacade c);
	public Set<Error> getErrors();
}
