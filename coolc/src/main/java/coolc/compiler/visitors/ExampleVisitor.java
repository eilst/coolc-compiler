package coolc.compiler.visitors;

import coolc.compiler.ErrorManager;
import coolc.compiler.autogen.analysis.DepthFirstAdapter;
import coolc.compiler.autogen.node.AClassDecl;
import coolc.compiler.autogen.node.AProgram;

public class ExampleVisitor extends DepthFirstAdapter {
	private boolean hasMain = false;
	
	/*
	 * outAProgram is the last node visited, so we check here if there was no main method
	 */
	@Override
	public void outAProgram(AProgram node) {
		if (!hasMain) {
			ErrorManager.getInstance().semanticError("Coolc.semant.noMain");
		}
	}
	
	/*
	 * When visiting every Class, we check if it is called Main.
	 */
	@Override
	public void inAClassDecl(AClassDecl node) {
		if (node.getName().getText().equals("Main")) {
			hasMain = true;
		}
	}
	
}
