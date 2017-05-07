package coolc.compiler.visitors;

import coolc.compiler.ErrorManager;
import coolc.compiler.autogen.analysis.DepthFirstAdapter;
import coolc.compiler.autogen.node.AClassDecl;
import coolc.compiler.autogen.node.AProgram;
import coolc.compiler.autogen.node.TTypeId;
import coolc.compiler.util.Error;


public class ExampleVisitor extends DepthFirstAdapter {

	
	boolean hasMain = false;
	boolean flag1 = true;
	boolean flag2 = true;

	
	@Override
	public void inAClassDecl(AClassDecl node){
		if(node.getName().getText().equals("Main")){
			hasMain = true;
		}
		if(node.getInherits() == null){
			node.setInherits(new TTypeId("Object"));
		}
		if(node.getInherits().getText().equals("Bool") ||
				node.getInherits().getText().equals("String")){
			flag1 = false;
			ErrorManager.getInstance().getErrors().add(Error.INHERIT_BASIC);
		}
		if(node.getInherits().getText().equals("SELF_TYPE")){
			flag1 = false;
			flag2 = true;
			ErrorManager.getInstance().getErrors().add(Error.INHERIT_SELF_TYPE);
			if(node.getName().getText().equals("Main")){
				hasMain = false;
			}
		}
		
		if(node.getName().getText().equals("Int") ||
				node.getName().getText().equals("SELF_TYPE") ||
				node.getName().getText().equals("String") ||
				node.getName().getText().equals("Object") ||
				node.getName().getText().equals("Bool")){
			 flag1 = false;
			ErrorManager.getInstance().getErrors().add(Error.REDEF_BASIC);
		}
	}
	/*
	 * outAProgram is the last node visited, so we check here if there was no main method
	 */
	@Override
	public void outAProgram(AProgram node) {
		
		if(!hasMain){
    		ErrorManager.getInstance().getErrors().add(Error.NO_MAIN);
    	}
    	if(!flag2){
    		ErrorManager.getInstance().getErrors().add(Error.SELF_ATTR);	
    	}
	}
	
	/*
	 * When visiting every Class, we check if it is called Main.
	 */

	
}
