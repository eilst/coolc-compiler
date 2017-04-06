package coolc.compiler.visitors;

import java.util.HashMap;
import java.util.Map;

import coolc.compiler.ErrorManager;
import coolc.compiler.autogen.analysis.DepthFirstAdapter;
import coolc.compiler.autogen.node.AAttributeFeature;
import coolc.compiler.autogen.node.AClassDecl;
import coolc.compiler.autogen.node.AFormal;
import coolc.compiler.autogen.node.ALetDecl;
import coolc.compiler.autogen.node.ALetExpr;
import coolc.compiler.autogen.node.AMethodFeature;
import coolc.compiler.autogen.node.AObjectExpr;
import coolc.compiler.autogen.node.Node;
import coolc.compiler.autogen.node.PFormal;
import coolc.compiler.util.Environment;

public class IdentifierValidator extends DepthFirstAdapter {
	private Environment e;
	private Map<Node, String> types;
	
	public IdentifierValidator() {
		e = new Environment();
		types = new HashMap<Node, String>();
	}
	
	@Override
	public void inAClassDecl(AClassDecl node) {
		e.newLevel();		
	}
	
	@Override
	public void outAClassDecl(AClassDecl node) {
		e.forgetLevel();
	}
	
	@Override
	public void inAAttributeFeature(AAttributeFeature node) {
		// TODO validate typoe of node.getExpr() vs node.getTypeId()
		e.put(node.getObjectId().getText(), node.getTypeId().getText());
	}
	
	@Override
	public void inAMethodFeature(AMethodFeature node) {
		e.newLevel();
		for(PFormal formal : node.getFormal()) {
			AFormal f = (AFormal)formal;
			e.put(f.getObjectId().getText(), f.getTypeId().getText()); 
		}
	}
	
	@Override
	public void outAMethodFeature(AMethodFeature node) {
		e.forgetLevel();
	}
	
	@Override
	public void inALetExpr(ALetExpr node) {
		e.newLevel();
	}
	
	@Override
	public void outALetExpr(ALetExpr node) {
		e.forgetLevel();
	}
	
	@Override
	public void inALetDecl(ALetDecl node) {
		// TODO validate type of node.getExpr() vs node.getTypeId()
		e.put(node.getObjectId().getText(), node.getTypeId().getText());		
	}
	
	@Override
	public void inAObjectExpr(AObjectExpr node) {
		String type = e.get(node.getObjectId().getText());
		if (type == null) {
			ErrorManager.getInstance().semanticError("Coolc.semant.undeclIdentifier");
		} 
			
		node.setType(type);
		types.put(node, type);
	}
	
}
