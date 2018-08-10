package coolc.compiler.visitors;


import java.util.Iterator;
import java.util.LinkedList;
import coolc.compiler.ErrorManager;
import org.jboss.util.graph.*;
import coolc.compiler.autogen.analysis.DepthFirstAdapter;
import coolc.compiler.autogen.node.AAssignExpr;
import coolc.compiler.autogen.node.AAtExpr;
import coolc.compiler.autogen.node.AAttributeFeature;
import coolc.compiler.autogen.node.ABranch;
import coolc.compiler.autogen.node.ACallExpr;
import coolc.compiler.autogen.node.ACaseExpr;
import coolc.compiler.autogen.node.AClassDecl;
import coolc.compiler.autogen.node.ADivExpr;
import coolc.compiler.autogen.node.AEqExpr;
import coolc.compiler.autogen.node.AFormal;
import coolc.compiler.autogen.node.AIfExpr;
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
import coolc.compiler.autogen.node.ANotExpr;
import coolc.compiler.autogen.node.APlusExpr;
import coolc.compiler.autogen.node.AWhileExpr;
import coolc.compiler.autogen.node.PBranch;
import coolc.compiler.autogen.node.PExpr;
import coolc.compiler.autogen.node.PFeature;
import coolc.compiler.autogen.node.PFormal;
import coolc.compiler.autogen.node.PLetDecl;
import coolc.compiler.autogen.node.TTypeId;
import coolc.compiler.exceptions.SemanticException;
import coolc.compiler.util.AbstractSymbol;
import coolc.compiler.TableC;
import coolc.compiler.util.Error;
import coolc.compiler.util.Symbol;

public class Simbols extends DepthFirstAdapter{
		
		public Graph grafo;
		TableC tableC;
		ErrorManager errorManager;
		Vertex<Integer> vertex;
		int nivelActual;
		   
		public Simbols(){
			tableC = TableC.getInstance();
			tableC.reset();
			tableC.installBasic();
			errorManager = ErrorManager.getInstance();
			grafo = tableC.getGraph();
			
			Vertex<Integer> objectVertex = new Vertex<Integer>("Object");
			objectVertex.setData(0);
			grafo.addVertex(objectVertex);
			grafo.setRootVertex(objectVertex);
			
			Vertex<Integer> ioVertex = new Vertex<Integer>("IO");
			ioVertex.setData(1);
			grafo.addVertex(ioVertex);
			grafo.addEdge(objectVertex,ioVertex, 1);
			
			Vertex<Integer> BoolVertex = new Vertex<Integer>("Bool");
			BoolVertex.setData(1);
			grafo.addVertex(BoolVertex);
			grafo.addEdge(objectVertex,BoolVertex, 1);
			
			Vertex<Integer> StringVertex = new Vertex<Integer>("String");
			StringVertex.setData(1);
			grafo.addVertex(StringVertex);
			grafo.addEdge(objectVertex,StringVertex, 1);
			
			Vertex<Integer> IntVertex = new Vertex<Integer>("Int");
			IntVertex.setData(1);
			grafo.addVertex(IntVertex);
			grafo.addEdge(objectVertex,IntVertex, 1);
		}
		
		
		@Override
		public void inAClassDecl(AClassDecl node)
		{
			node.setType(node.getName().getText());
			if(node.getInherits() != null){
				if(node.getInherits().getText().equals("SELF_TYPE")){
					errorManager.getErrors().add(Error.INHERIT_SELF_TYPE);
					return;
				}
				if(		node.getInherits().getText().equals("Int")  || 
						node.getInherits().getText().equals("String")  || 
						node.getInherits().getText().equals("Bool")){
					errorManager.getErrors().add(Error.INHERIT_BASIC);
				}
				node.getInherits().setType(node.getInherits().getText());
			}else{
				node.setInherits(new TTypeId("Object"));
			}
			
			Iterator<PFeature> i = node.getFeature().iterator();
			while(i.hasNext()){
				PFeature f = i.next();
				f.setInClass(node.getName().getText());
				if(f instanceof AMethodFeature){
					PExpr e = ((AMethodFeature) f).getExpr();
					setInClass(e,node.getName().getText());
					f.setType(((AMethodFeature)f).getTypeId().getText());
				}
				if(f instanceof AAttributeFeature){
					if(((AAttributeFeature) f).getObjectId().getText().equals("self")){
						errorManager.getErrors().add(Error.SELF_ATTR);
					}
					PExpr e = ((AAttributeFeature) f).getExpr();
					if(e != null){
						setInClass(e,node.getName().getText());
					}
					f.setType(((AAttributeFeature)f).getTypeId().getText());
				}
			}
			if(tableC.getTable().containsKey(node.getName().getText())){
				errorManager.getErrors().add(Error.REDEFINED);
			}
			tableC.addClassToTable(node);
			

		}
		
		@Override
		public void outAClassDecl(AClassDecl node)
		{
			if(		node.getName().getText().equals("SELF_TYPE")    ||
					node.getName().getText().equals("Object")    ||
					node.getName().getText().equals("Int")       || 
					node.getName().getText().equals("String")  	 ||
					node.getName().getText().equals("IO")  	 ||
					node.getName().getText().equals("Bool") ){
				errorManager.getErrors().add(Error.REDEF_BASIC);
				
			}		
			
			Vertex<Integer> v = new Vertex<Integer>(node.getName().getText());
			grafo.addVertex(v);
			
			if(node.getInherits() != null){		
				if(node.getInherits().getText().equals("IO")){
					Vertex aux = grafo.findVertexByName("IO");
					Vertex aux2 = grafo.findVertexByName(node.getName().getText());
					grafo.addEdge(aux, aux2, 1);
				}else{
					Vertex inh = new Vertex(node.getInherits().getText());
					grafo.addVertex(inh);
					Vertex aux = grafo.findVertexByName(node.getInherits().getText());
					Vertex aux2 = grafo.findVertexByName(node.getName().getText());
					grafo.addEdge(aux, aux2, 1);
				}
				
			}else{
				Vertex aux = grafo.findVertexByName("Object");
				Vertex aux2 = grafo.findVertexByName(node.getName().getText());
				grafo.addEdge(aux, aux2, 1);
			}
		}
		
		@Override
		public void inABranch(ABranch node)
	    {
			
			if(tableC.getSymbolTable().containsKey(node.getObjectId().getText())){
				tableC.getSymbolTable().get(node.getObjectId().getText()).add(new Symbol(node.getTypeId().getText()
						,"branch",node.getInClass()));
			}else{
				LinkedList<Symbol> symbols = new LinkedList<Symbol>();
				symbols.add(new Symbol(node.getTypeId().getText()
						,"branch",node.getInClass()));
				tableC.getSymbolTable().put(node.getObjectId().getText(),symbols);
			}
	    }
	    
	    @Override
	    public void inALetExpr(ALetExpr node){
	    	Iterator i = node.getLetDecl().iterator();
	    	while(i.hasNext()){
	    		ALetDecl letDecl = (ALetDecl) i.next();
	    		if(letDecl.getObjectId().getText().equals("self")){
	    			errorManager.getErrors().add(Error.SELF_IN_LET);
	    		}
	    		if(tableC.getSymbolTable().containsKey(letDecl.getObjectId().getText())){
					tableC.getSymbolTable().get(letDecl.getObjectId().getText()).add(new Symbol(letDecl.getTypeId().getText()
							,"let",node.getInClass()));
				}else{
					LinkedList<Symbol> symbols = new LinkedList<Symbol>();
					symbols.add(new Symbol(letDecl.getTypeId().getText()
							,"let",node.getInClass()));
					tableC.getSymbolTable().put(letDecl.getObjectId().getText(),symbols);
				}
	    	}
	    }
	    
	    
	    @Override
	    public void inAMethodFeature(AMethodFeature node){
	    	
	    	LinkedList<PFormal> formals = node.getFormal();
	    	for(int i = 0;i<formals.size();i++){
				AFormal b1 = (AFormal)formals.get(i);
				for(int j = 0;j<formals.size();j++){
					if(i != j){
						AFormal b2 = (AFormal)formals.get(j);
						if(b1.getObjectId().getText().equals(b2.getObjectId().getText())){
							errorManager.getErrors().add(Error.FORMAL_REDEFINITION);
						}
					}
				}
			}
	    	
	    	if(node.getTypeId().getText().equals("SELF_TYPE") && node.getExpr() instanceof ANewExpr){
	    		ANewExpr expr = (ANewExpr) node.getExpr();
	    		if(!expr.getTypeId().getText().equals("SELF_TYPE")){
	    			errorManager.getErrors().add(Error.BAD_INFERRED);
	    		}
	    	}
	    	
	    	
	    }
	    
	    public void setInClass(PExpr expr, String node){
	    	expr.setInClass(node);
	    	
	    	if(expr instanceof AAssignExpr){
	    		setInClass(((AAssignExpr)expr).getExpr(),node);
	    	}
	    	
	    	if(expr instanceof AAtExpr){
	    		setInClass(((AAtExpr)expr).getExpr(),node);
	    		Iterator<PExpr> i = ((AAtExpr)expr).getList().iterator();
				while(i.hasNext()){
					PExpr aux = i.next();
					setInClass(aux,node);
				}
	    	}
	    	
	    	if(expr instanceof ACallExpr){
	    		Iterator<PExpr> i = ((ACallExpr)expr).getExpr().iterator();
				while(i.hasNext()){
					PExpr aux = i.next();
					setInClass(aux,node);
				}
	    	}
	    	
	    	if(expr instanceof ACaseExpr){
	    		setInClass(((ACaseExpr)expr).getTest(),node);
	    		Iterator<PBranch> i = ((ACaseExpr)expr).getBranch().iterator();
				while(i.hasNext()){
					PBranch aux = i.next();
					setInClass(aux,node);
				}
	    	}
	    	
	    	if(expr instanceof ADivExpr){
	    		setInClass(((ADivExpr)expr).getL(),node);
	    		setInClass(((ADivExpr)expr).getR(),node);
	    	}
	    	
	    	if(expr instanceof AEqExpr){
	    		setInClass(((AEqExpr)expr).getL(),node);
	    		setInClass(((AEqExpr)expr).getR(),node);
	    	}
	    	
	    	if(expr instanceof AIfExpr){
	    		setInClass(((AIfExpr)expr).getTest(),node);
	    		setInClass(((AIfExpr)expr).getTrue(),node);
	    		setInClass(((AIfExpr)expr).getFalse(),node);
	    	}
	    	
	    	if(expr instanceof AIsvoidExpr){
	    		setInClass(((AIsvoidExpr)expr).getExpr(),node);
	    	}
	    	
	    	if(expr instanceof ALeExpr){
	    		setInClass(((ALeExpr)expr).getL(),node);
	    		setInClass(((ALeExpr)expr).getR(),node);
	    	}
	    	
	    	
	    	if(expr instanceof AListExpr){
				Iterator<PExpr> i2 = ((AListExpr)expr).getExpr().iterator();
				while(i2.hasNext()){
					PExpr aux = i2.next();
					setInClass(aux,node);
				}
			}
	    	
	    	if(expr instanceof ALtExpr){
	    		setInClass(((ALtExpr)expr).getL(),node);
	    		setInClass(((ALtExpr)expr).getR(),node);
	    	}
	    	
	    	if(expr instanceof AMinusExpr){
	    		setInClass(((AMinusExpr)expr).getL(),node);
	    		setInClass(((AMinusExpr)expr).getR(),node);
	    	}
	    	
	    	if(expr instanceof AMultExpr){
	    		setInClass(((AMultExpr)expr).getL(),node);
	    		setInClass(((AMultExpr)expr).getR(),node);
	    	}
	    	
	    	if(expr instanceof ANegExpr){
	    		setInClass(((ANegExpr)expr).getExpr(),node);
	    	}
	    	
	    	if(expr instanceof ANotExpr){
	    		setInClass(((ANotExpr)expr).getExpr(),node);
	    	}
	    	
	    	if(expr instanceof APlusExpr){
	    		setInClass(((APlusExpr)expr).getL(),node);
	    		setInClass(((APlusExpr)expr).getR(),node);
	    	}    	
	    	
			if(expr instanceof AWhileExpr){
				setInClass(((AWhileExpr)expr).getTest(),node);
	    		setInClass(((AWhileExpr)expr).getLoop(),node);
			}
			
			if(expr instanceof ALetExpr){
				setInClass(((ALetExpr)expr).getExpr(), node);
				Iterator<PLetDecl> i2 = ((ALetExpr)expr).getLetDecl().iterator();
				while(i2.hasNext()){
					PLetDecl aux = i2.next();
					setInClass(aux,node);
				}
			}
			
			
	    }
	    
	    
	    public void setInClass(PBranch expr, String node){
	    	expr.setInClass(node);
	    	if(expr instanceof ABranch){
	    		setInClass(((ABranch)expr).getExpr(), node);
	    	}
	    }
	    
	    public void setInClass(PLetDecl expr, String node){
	    	expr.setInClass(node);
	    	if(expr instanceof ALetDecl){
	    		if(((ALetDecl)expr).getExpr() != null){
	    			setInClass(((ALetDecl)expr).getExpr(),node);
	    		}
	    	}
	    }

}
