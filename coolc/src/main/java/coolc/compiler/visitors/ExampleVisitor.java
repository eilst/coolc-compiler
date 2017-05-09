package coolc.compiler.visitors;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import coolc.compiler.ErrorManager;
import org.jboss.util.graph.*;
import coolc.compiler.autogen.analysis.DepthFirstAdapter;
import coolc.compiler.autogen.node.AAssignExpr;
import coolc.compiler.autogen.node.AAtExpr;
import coolc.compiler.autogen.node.AAttributeFeature;
import coolc.compiler.autogen.node.ABoolExpr;
import coolc.compiler.autogen.node.ABranch;
import coolc.compiler.autogen.node.ACallExpr;
import coolc.compiler.autogen.node.ACaseExpr;
import coolc.compiler.autogen.node.AClassDecl;
import coolc.compiler.autogen.node.ADivExpr;
import coolc.compiler.autogen.node.AEqExpr;
import coolc.compiler.autogen.node.AFormal;
import coolc.compiler.autogen.node.AIfExpr;
import coolc.compiler.autogen.node.AIntExpr;
import coolc.compiler.autogen.node.AIsvoidExpr;
import coolc.compiler.autogen.node.ALeExpr;
import coolc.compiler.autogen.node.ALetDecl;
import coolc.compiler.autogen.node.ALetExpr;
import coolc.compiler.autogen.node.AListExpr;
import coolc.compiler.autogen.node.ALtExpr;
import coolc.compiler.autogen.node.AMethodFeature;
import coolc.compiler.autogen.node.AMinusExpr;
import coolc.compiler.autogen.node.AMultExpr;
import coolc.compiler.autogen.node.ANegExpr;
import coolc.compiler.autogen.node.ANewExpr;
import coolc.compiler.autogen.node.ANoExpr;
import coolc.compiler.autogen.node.ANotExpr;
import coolc.compiler.autogen.node.AObjectExpr;
import coolc.compiler.autogen.node.APlusExpr;
import coolc.compiler.autogen.node.AStrExpr;
import coolc.compiler.autogen.node.AWhileExpr;
import coolc.compiler.autogen.node.PBranch;
import coolc.compiler.autogen.node.PExpr;
import coolc.compiler.autogen.node.PFeature;
import coolc.compiler.autogen.node.PFormal;
import coolc.compiler.autogen.node.PLetDecl;
import coolc.compiler.autogen.node.TObjectId;
import coolc.compiler.autogen.node.TTypeId;
import coolc.compiler.exceptions.SemanticException;
import coolc.compiler.util.Attributes;
import coolc.compiler.TableC;
import coolc.compiler.util.Error;
import coolc.compiler.util.Formals;
import coolc.compiler.util.Methods;
import coolc.compiler.util.Symbol;

public class ExampleVisitor extends DepthFirstAdapter{
	
	public Graph graph;
	TableC classTable;
	ErrorManager errorManager;
	Set<Error> errors;
	   
	public ExampleVisitor(){
		classTable = TableC.getInstance();
		errorManager = ErrorManager.getInstance();
		graph = classTable.getGraph();
		this.errors = errors;
		levelTable();
		checkMain();
		Vertex root = graph.findVertexByName("Object");
		graph.setRootVertex(root);
		completeTable(root);
	}
	
	
	@Override
	public void inAClassDecl(AClassDecl node)
	{
		node.setType(node.getName().getText());
		Iterator<PFeature> i = node.getFeature().iterator();
		while(i.hasNext()){
			PFeature f = i.next();
			f.setInClass(node.getName().getText());
			if(f instanceof AMethodFeature){
				PExpr e = ((AMethodFeature) f).getExpr();
				f.setType(((AMethodFeature)f).getTypeId().getText());
			}
			if(f instanceof AAttributeFeature){
				PExpr e = ((AAttributeFeature) f).getExpr();
				if(e != null){
				}
				f.setType(((AAttributeFeature)f).getTypeId().getText());
			}
		}
		
	}
	
	@Override
	public void outAClassDecl(AClassDecl node)
	{
		if(node.getName().getText().equals("Object")    ||
				node.getName().getText().equals("Int")  || 
				node.getName().getText().equals("Str")  || 
				node.getName().getText().equals("Bool") || 
				node.getName().getText().equals("IO")){
			errorManager.semanticError("redefBasic", Error.REDEF_BASIC);
		}		
		
		if(node.getInherits() != null){			

		}else{

		}
	}
	
	@Override
	public void inABranch(ABranch node)
    {		
    }
	
	@Override	
	public void outAEqExpr(AEqExpr node){
		node.setType("Bool");
	}
	
	@Override
    public void outABoolExpr(ABoolExpr node)
    {
    	node.setType("Bool");
    }
	
	@Override
	public void outALtExpr(ALtExpr node){
		node.setType("Bool");
	}
	
	@Override
    public void outAStrExpr(AStrExpr node)
    {
		node.setType("String");
	}
	
	@Override
    public void outAIntExpr(AIntExpr node)
    {
    	node.setType("Int");
    }
	
    @Override
    public void outAObjectExpr(AObjectExpr node)
    {
    	if(node.getObjectId().getText().equals("self")){
    		node.setType("SELF_TYPE of "+node.getInClass());
    	}else{
    		LinkedList<Symbol> sList = classTable.getSymbolTable().get(node.getObjectId().getText());
    		Symbol s = sList.get(0);
    		for(int i = 0;i<sList.size();i++){
    			s = sList.get(i);
            	if(s.getClassType().equals(node.getInClass())){
            		i = sList.size();
            	}
            }
    			if(s.getType().equals("SELF_TYPE")){
        			node.setType(s.getType()+" of "+node.getInClass());
        		}else{
        			node.setType(s.getType());
        		}
        }
    	
    }
    
    @Override
    public void outAIsvoidExpr(AIsvoidExpr node){
    	node.setType("Bool");
    }
    
    @Override
    public void inALetExpr(ALetExpr node){
    }
    
    @Override
    public void outAMinusExpr(AMinusExpr node){
    	node.setType("Int");
    }
    
    @Override
    public void outAPlusExpr(APlusExpr node){
    	node.setType("Int");
    }
    
    @Override
    public void outAMultExpr(AMultExpr node){
    	node.setType("Int");
    }
    
    @Override
    public void outADivExpr(ADivExpr node){
    	node.setType("Int");
    }

    @Override
    public void outAWhileExpr(AWhileExpr node)
    {
        node.setType("Object");
    }
    
    @Override
    public void outANotExpr(ANotExpr node){
    	node.setType("Bool");
    }
    
    @Override
    public void outALeExpr(ALeExpr node){
    	node.setType("Bool");
    }
    
    public void levelTable(){
    	Vertex<Integer> root = graph.findVertexByName("Object");
    	Integer level = 0;
    	setLevel(root, 0);
    }
    
    public void setLevel(Vertex<Integer> v,Integer level){
    	v.setData(level);
    	Iterator it = v.getOutgoingEdges().iterator();
    	while(it.hasNext()){
    		Edge e = (Edge)it.next();
    		setLevel(e.getTo(), level+1);
    	}
    }
    
    public void checkMain(){
    	if(classTable.getTable().get("Main") == null){
    		errorManager.getErrors().add(Error.NO_MAIN);
    	}
    }
    
    public void completeTable(Vertex vertex){
    	Vertex root = vertex;
    	if(root.getOutgoingEdgeCount() > 0){
	    	List<Edge> redges = root.getOutgoingEdges();
	    	Iterator it = redges.iterator();
	    	while(it.hasNext()){
	    		Edge e = (Edge) it.next();
	    		Vertex v = e.getTo();
	    		String type = v.getName();
	    		
	    		AClassDecl parent = classTable.getTable().get(root.getName());
	    		AClassDecl child = classTable.getTable().get(v.getName());

	    		
	    		LinkedList<PFeature> featList;
	    		LinkedList<PFormal> formalList;
	    		LinkedList<Formals> finfo;
	    		Methods info;
	    		
	    		featList = parent.getFeature();
	    		Iterator it1 = featList.iterator();
	    		while(it1.hasNext()){
	    			PFeature p = (PFeature) it1.next();
	    			if(p instanceof AMethodFeature){
	    				if(child != null){
	    					child.getFeature().add(p);
	    				}
	    				AMethodFeature mf = (AMethodFeature)p;
	    				finfo = new LinkedList<Formals>();
	    				formalList = mf.getFormal();	
	    				Iterator it2 = formalList.iterator();
	    				while(it2.hasNext()){
	    					AFormal formal = (AFormal) it2.next();
	    					finfo.add(new Formals(formal.getObjectId().getText(),formal.getTypeId().getText()));
	    				}
	    				classTable.getMethodTableC().put(type+"."+mf.getObjectId().getText(),new Methods(mf.getTypeId().getText(),type,finfo));
	    			}
	    		}
	    		if(v.getOutgoingEdgeCount() > 0){
	    			completeTable(v);
	    		}
	    	}
    	}
    }
	

}
