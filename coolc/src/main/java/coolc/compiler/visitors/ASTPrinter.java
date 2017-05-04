package coolc.compiler.visitors;

import java.io.PrintStream;
import java.util.Stack;

import coolc.compiler.autogen.analysis.ReversedDepthFirstAdapter;
import coolc.compiler.autogen.node.EOF;
import coolc.compiler.autogen.node.Node;
import coolc.compiler.autogen.node.Start;
import coolc.compiler.autogen.node.Token;


/**
 * Text display of the AST, with (optionally) color output.
 * 
 * To print the AST we do a reverse depth first traversal. We do this because it
 * is easier to know which element is the last child in any node. This makes it
 * easier to do nice indenting.
 * 
 * @author Roger Keays <rogerk@ieee.org> 7/9/2001
 * @author Abdul <jcordoba@itesm.mx> 02/02/2012 - Simplified for output to a custom PrintStream *no color*
 */
public class ASTPrinter extends ReversedDepthFirstAdapter {

	// variables. We use a stack to push on indent tokens...
	protected String indent = "";
	protected String output = "";
	private boolean last = false;
	protected Stack<String> indentchar = new Stack<String>();
	private PrintStream out;
	
	public ASTPrinter(PrintStream out) {
		this.out = out;
	}

	/*
	 * The last node we visit. It prints out the entire text that we have built.
	 */
	public void outStart(Start node) {
		out.format("\n  >%s\n", output.substring(3, output.length()));
	}

	/*
	 * As we visit each non-terminal node push on the indent we need for this
	 * node. The next node we visit will always be the last child of this node.
	 */
	public void defaultIn(Node node) {
		if (last)
			indentchar.push("`");
		else
			indentchar.push("|");

		indent = indent + "   ";
		last = true;
	}

	/*
	 * As we leave a non-terminal node, we pull off the indent character and
	 * prepend this nodes line to the output text.
	 */
	public void defaultOut(Node node) {
		// replace the current indent with the one from the stack
		indent = indent.substring(0, indent.length() - 3);
		indent = indent.substring(0, indent.length() - 1)
				+ (String) indentchar.pop();

		// prepend this line to the output.
		output = indent
				+ "- "
				+ node.getClass()
						.getName()
						.substring(
								node.getClass().getName().lastIndexOf('.') + 1)
				+ "\n" + output;

		// replace any ` with a |
		indent = indent.substring(0, indent.length() - 1) + '|';
	}

	/*
	 * When we visit a terminals we just print it out. We always set last to
	 * false after this because the next node we visit will never be the last
	 * sibling.
	 */
	public void defaultCase(Node node) {
		// last sibling has a ` instead of a |
		if (last)
			indent = indent.substring(0, indent.length() - 1) + '`';

		// prepend this line to the output
		output = indent + "- " + ((Token) node).getText() + "\n" + output;

		// replace any ` with a |
		indent = indent.substring(0, indent.length() - 1) + '|';
		last = false;
	}

	public void caseEOF(EOF node) {
		last = false;
	}

}
