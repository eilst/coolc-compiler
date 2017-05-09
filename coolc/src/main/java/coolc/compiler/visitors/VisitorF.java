package coolc.compiler.visitors;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;

import coolc.compiler.ErrorManager;
import coolc.compiler.TableC;
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
import coolc.compiler.autogen.node.ALetDecl;
import coolc.compiler.autogen.node.ALetExpr;
import coolc.compiler.autogen.node.AListExpr;
import coolc.compiler.autogen.node.AMethodFeature;
import coolc.compiler.autogen.node.AMinusExpr;
import coolc.compiler.autogen.node.AMultExpr;
import coolc.compiler.autogen.node.ANegExpr;
import coolc.compiler.autogen.node.ANewExpr;
import coolc.compiler.autogen.node.AObjectExpr;
import coolc.compiler.autogen.node.APlusExpr;
import coolc.compiler.autogen.node.AWhileExpr;
import coolc.compiler.autogen.node.PBranch;
import coolc.compiler.autogen.node.PExpr;
import coolc.compiler.autogen.node.PFeature;
import coolc.compiler.autogen.node.PFormal;
import coolc.compiler.autogen.node.PLetDecl;
import coolc.compiler.util.Error;
import coolc.compiler.util.Formals;
import coolc.compiler.util.Methods;
import coolc.compiler.util.Symbol;

public class VisitorF extends DepthFirstAdapter{
		
		TableC tablec;
		ErrorManager errorManager;
		int currentLevel;
		Graph graph;
		   
		public VisitorF(){
			tablec = tablec.getInstance();
			errorManager = ErrorManager.getInstance();
			graph = tablec.getGraph();

		}
		
		@Override
		public void inAClassDecl(AClassDecl node)
		{
			
			tablec.getsTable().enterScope();
			currentLevel = 1;
			
	    	AClassDecl decl = tablec.getTable().get(node.getName().getText());
	    	while(!decl.getInherits().getText().equals("Object") && !decl.getInherits().getText().equals("IO")){
	    		decl = tablec.getTable().get(decl.getInherits().getText());
	    		if(decl == null){
	    			errorManager.getErrors().add(Error.CANNOT_INHERIT);
	    			return;
	    		}
	    		LinkedList<PFeature> fts = decl.getFeature();
				Iterator it = fts.iterator();
		    	while(it.hasNext()){
		    		PFeature feat = (PFeature) it.next();
		    		if(feat instanceof AAttributeFeature){
		    			tablec.getsTable().addId(((AAttributeFeature) feat).getObjectId().getText(),
		    					new Symbol(((AAttributeFeature) feat).getTypeId().getText(),"method",feat.getInClass()));
		    		}
		    	}
	    	}
	    	
	    	LinkedList<PFeature> feats = node.getFeature();
			Iterator i = feats.iterator();
	    	while(i.hasNext()){
	    		PFeature feat = (PFeature) i.next();
	    		if(feat instanceof AAttributeFeature){
	    			if(tablec.getsTable().lookup(((AAttributeFeature) feat).getObjectId().getText()) != null){
	    				errorManager.getErrors().add(Error.ATTR_INHERITED);
	    			}else{
	    				tablec.getsTable().addId(((AAttributeFeature) feat).getObjectId().getText(),
	    					new Symbol(((AAttributeFeature) feat).getTypeId().getText(),"method",feat.getInClass()));
	    			}
	    		}
	    	}
	    }
		
		@Override
		public void outAClassDecl(AClassDecl node)
		{
			
			tablec.getsTable().exitScope();
			currentLevel = 1;
		}
		
		@Override
	    public void inAMethodFeature(AMethodFeature node){
	    	
			tablec.getsTable().addId(node.getObjectId().getText(),
					new Symbol(node.getTypeId().getText(),"method",node.getInClass()));
		
			
	    	currentLevel++;
	    	tablec.getsTable().enterScope();
	    	
	    	if(node.getFormal().size() != 0){
	    		LinkedList<PFormal> formals = node.getFormal();
	    		Iterator i = formals.iterator();
	    		while(i.hasNext()){
	    			PFormal formal = (PFormal) i.next();
	    			tablec.getsTable().addId(((AFormal)formal).getObjectId().getText(),
	    					new Symbol(((AFormal)formal).getTypeId().getText(),"formal",node.getInClass()));
	    		}
	    	}
	    	
	    	checkOverriding(node);
	    }
		

		@Override
	    public void outAMethodFeature(AMethodFeature node){
	    	
	    	currentLevel--;
	    	tablec.getsTable().exitScope();
	    	
	    	String type = node.getTypeId().getText();
	    	if(type.equals("SELF_TYPE")){
	    		type = node.getInClass();
	    	}
	    	if(!tablec.getTable().containsKey(type)){
	    		errorManager.getErrors().add(Error.TYPE_NOT_FOUND);
	    	}
	    	
	    	LinkedList<PFormal> formals = node.getFormal();
	    	Iterator iter = formals.iterator();
	    	while(iter.hasNext()){
	    		AFormal formal = (AFormal)iter.next();
	    		if(formal.getObjectId().getText().equals("self")){
	    			errorManager.getErrors().add(Error.SELF_FORMAL);
	    		}
	    		if(formal.getTypeId().getText().equals("SELF_TYPE")){
	    			errorManager.getErrors().add(Error.SELF_TYPE_FORMAL);
	    			errorManager.getErrors().add(Error.UNDECL_IDENTIFIER);
	    		}
	    	}
	    	
	    	if(node.getExpr() instanceof AIfExpr){
	    		AIfExpr ifExpr = (AIfExpr) node.getExpr();
	    		String trueType = ifExpr.getTrue().getType();
	    		String falseType = ifExpr.getFalse().getType();
	    		if(trueType.contains("SELF_TYPE")){
	    			trueType = trueType.substring(13);
	    		}
	    		if(falseType.contains("SELF_TYPE")){
	    			falseType = falseType.substring(13);
	    		}
	    		if(invalidType(node.getTypeId().getText(),trueType)){
	    			errorManager.getErrors().add(Error.BAD_INFERRED);
	    		}
	    		if(invalidType(node.getTypeId().getText(),falseType)){
	    			errorManager.getErrors().add(Error.BAD_INFERRED);
	    		}
	    	}
	    
	    }
		
		@Override
		public void inACaseExpr(ACaseExpr node)
	    {
			currentLevel++;
	    	tablec.getsTable().enterScope();
	    }
		
		@Override
		public void inABranch(ABranch node)
	    {
			tablec.getsTable().addId(node.getObjectId().getText(),
					new Symbol(node.getTypeId().getText(),"branch",node.getInClass()));
	    }
	    
	    @Override
	    public void inALetExpr(ALetExpr node){
	    	
	    	currentLevel++;
	    	tablec.getsTable().enterScope();
	    	
	    	if(node.getLetDecl().size() != 0){
	    		LinkedList<PLetDecl> decls = node.getLetDecl();
	    		Iterator i = decls.iterator();
	    		while(i.hasNext()){
	    			PLetDecl decl = (PLetDecl) i.next();
	    			tablec.getsTable().addId(((ALetDecl)decl).getObjectId().getText(),
	    					new Symbol(((ALetDecl)decl).getTypeId().getText(),"let",node.getInClass()));
	    		}
	    	}
	    }
	    
	    @Override
	    public void outALetExpr(ALetExpr node){
	    	
	    	currentLevel--;
	    	tablec.getsTable().exitScope();
	    	if(node.getExpr().getType().equals("SELF_TYPE")){
	    		node.setType(node.getExpr().getType()+" of "+node.getInClass());
	    	}else{
	    		node.setType(node.getExpr().getType());
	    	}

	    }
	    
	    @Override
	    public void outAObjectExpr(AObjectExpr node)
	    {
	    	if(node.getObjectId().getText().equals("self")){
	    		node.setType("SELF_TYPE of "+node.getInClass());
	    	}else{
	    		
	    		
	    		Symbol n = (Symbol) tablec.getsTable().lookup(node.getObjectId().getText());
	    		if(n != null){
	    			if(n.getType().equals("SELF_TYPE")){
	    				node.setType(n.getType()+" of "+node.getInClass());
	    			}else{
	    				node.setType(n.getType());	
	    			}
	    		}else{
	    			errorManager.getErrors().add(Error.UNDECL_IDENTIFIER);
	    		}
	        }
	    	
	    	
	    }
		
		@Override
		public void outACaseExpr(ACaseExpr node){
			
			currentLevel--;
	    	tablec.getsTable().exitScope();
	    	
			LinkedList<String> classes = new LinkedList<String>();
			LinkedList<PBranch> branches = node.getBranch();
			
			if(branches.size() == 1){
				ABranch b = (ABranch) branches.get(0);
				node.setType(b.getExpr().getType());
			}else{
				
				Iterator it = branches.iterator();
				while(it.hasNext()){
					ABranch b = (ABranch)it.next();
					classes.add(b.getExpr().getType());
				}
				
				Iterator its = classes.iterator();
				boolean iguales = true;
				String saux = (String)its.next();
				while(its.hasNext()){
					String saux2 = (String)its.next();
					if(!saux.equals(saux2)){
						iguales = false;
					}
				}
				
				if(iguales){
					node.setType(classes.getFirst());
				}else{
				
					Iterator it2 = classes.iterator();
					boolean same = true;
					String aux = (String) it2.next();
					Vertex<Integer> v = graph.findVertexByName(aux);
					Integer minLevel = v.getData();
					String clase = v.getName();

					while(it2.hasNext()){
						String s = (String) it2.next();
						v = graph.findVertexByName(s);
						if(v.getData() < minLevel){
							minLevel = v.getData();
							clase = v.getName();
							same = false;	
						}
						if(v.getData() != minLevel){
							same = false;
						}
					}
					
					if(same){
						Integer a1 = (Integer) graph.findVertexByName(clase).getData();
						String s = node.getTest().getType();
						if(s.contains("SELF_TYPE")){
							s = s.substring(13);
						}
						Integer a2 = (Integer) graph.findVertexByName(s).getData();
						if( a1 <= a2 ){
							node.setType(clase);
						}else{
							node.setType(node.getTest().getType());
						}
					}else{
						node.setType(clase);
					}
				}
				
				for(int i = 0;i<branches.size();i++){
					ABranch b1 = (ABranch)branches.get(i);
					for(int j = 0;j<branches.size();j++){
						if(i != j){
							ABranch b2 = (ABranch)branches.get(j);
							if(b1.getTypeId().getText().equals(b2.getTypeId().getText())){
								errorManager.getErrors().add(Error.DUPLICATE_BRANCH);
							}
						}
					}
				}
				
			}
			
		}
		
		@Override
		public void outAIfExpr(AIfExpr node){
			
			String derecha = node.getTrue().getType();
			String izquierda = node.getFalse().getType();
			Vertex<Integer> vd;
			Vertex<Integer> vi;
			
			if(derecha.equals(izquierda)){
				node.setType(derecha);
			}else{
				
				if(derecha.contains("SELF_TYPE")){
					derecha = derecha.substring(13);
				}
				
				if(izquierda.contains("SELF_TYPE")){
					izquierda = izquierda.substring(13);
				}

				vd = graph.findVertexByName(derecha);
				vi = graph.findVertexByName(izquierda);
				
				if(vd.getData() == 0){
					node.setType(vd.getName());
					return;
				}
				if(vi.getData() == 0){
					node.setType(vi.getName());
					return;
				}
				
				while(vd.getData() != 0 && vi.getData() != 0){
					int aDer = vd.getData();
					int aIzq = vd.getData();
					
					if(aDer != aIzq){
						if(aDer < aIzq){
							vd = vd.getIncomingEdge(0).getFrom();
						}else{
							vi = vi.getIncomingEdge(0).getFrom();
						}
						if(vd.getName().equals(vi.getName())){
							node.setType(vd.getName());
							return;
						}
					}else{
						vd = vd.getIncomingEdge(0).getFrom();
						vi = vi.getIncomingEdge(0).getFrom();
						if(vd.getName().equals(vi.getName())){
							node.setType(vd.getName());
							return;
						}
					}
				}
			}		
		}
		
		@Override
	    public void outANewExpr(ANewExpr node)
	    {
	    	if(node.getTypeId().getText().equals("SELF_TYPE")){
	    		node.setType(node.getTypeId().getText()+" of "+node.getInClass());
	    	}else{
	    		node.setType(node.getTypeId().getText());
	    	}
	    	
	    }
	    
	    @Override
	    public void outAAssignExpr(AAssignExpr node)
	    {
	        node.setType(node.getExpr().getType());
	        
	        Vertex v = graph.findVertexByName(node.getExpr().getType());
	        String tyype = node.getObjectId().getText();
	        if(tyype.contains("self")){
	        	errorManager.getErrors().add(Error.ASSIGN_SELF);
	        	errorManager.getErrors().add(Error.BAD_INFERRED);
	        	return;
	        }
	        
	        Symbol s = (Symbol) tablec.getsTable().lookup(tyype);      
	        Vertex v2 = graph.findVertexByName(s.getType());
	        
	        LinkedList<String> types = new LinkedList<String>(); 
	        List<Edge> edges;
	        
	        if(!s.getType().equals(node.getExpr().getType())){
	        	edges =  v.getOutgoingEdges();
		        if(edges.size() > 0){
			        Vertex aux = v.getOutgoingEdge(0).getTo();
			        while(aux != null){
			        	types.add(aux.getName());
			        	edges =  aux.getOutgoingEdges();
			        	if(edges.size() > 0){
			        		aux = aux.getOutgoingEdge(0).getTo();
			        	}else{
			        		aux = null;
			        	}
			        }
		        }
		        
		        boolean error = true;
		        boolean end = false;
		        Iterator i = types.iterator();
		        while(i.hasNext() && !end){
		        	String str = (String) i.next();
		        	if(node.getExpr().getType().equals(str)){
		        		error = false;
		        		end = false;
		        	}
		        }
		        
		        if(error){
		        	errorManager.getErrors().add(Error.BAD_ASSIGNMENT);
		        }
	        }
	    }
	    
	    @Override
	    public void outAListExpr(AListExpr node)
	    {
	        node.setType(node.getExpr().getLast().getType());
	    }
	    
	    @Override
	    public void outAAtExpr(AAtExpr node)
	    {

	    	if(node.getTypeId() == null){
		    	String callerType = node.getExpr().getType();
		    	Methods minfo = tablec.getMethodTableC().get(callerType+"."+node.getObjectId().getText());
		    	if(minfo != null){
		    		LinkedList<Formals> formals = minfo.getFormals();
		    		LinkedList<PExpr> exprs = node.getList();
		    		if(formals.size() != exprs.size()){
		    		}else{
		    			for(int i = 0;i<formals.size();i++){
		    				if(!formals.get(i).getType().equals(exprs.get(i).getType())){
		    					String typeA = formals.get(i).getType();
		    					String typeB = exprs.get(i).getType();
		    					if(typeA.contains("SELF_TYPE")){
		    						String[] parts = typeA.split(" ");
		    						typeA = parts[parts.length-1];
		    					}
		    					if(typeB.contains("SELF_TYPE")){
		    						String[] parts = typeB.split(" ");
		    						typeB = parts[parts.length-1];
		    					}
		    					if(invalidType(typeA,typeB)){
		    						errorManager.getErrors().add(Error.FORMALS_FAILED_LONG);
		    					}
		    				}else{
		    					
		    				}
		    			}
		    		}
		    	}else{
		    		errorManager.getErrors().add(Error.DISPATCH_UNDEFINED);
		    		
		    	}
	    	}else{
	    		Methods minfo = tablec.getMethodTableC().get(node.getTypeId().getText()+"."+node.getObjectId().getText());
	    		if(minfo == null){
	    			
	    		}else{
	    			String callType = node.getExpr().getType();
	    			if(callType.contains("SELF_TYPE")){
	    				callType = callType.substring(13);
	    			}
	    			if(invalidType(node.getTypeId().getText(),callType)){
	    				errorManager.getErrors().add(Error.STATIC_FAIL_TYPE);
	    				errorManager.getErrors().add(Error.BAD_INFERRED);
	    			}
	    		}
		    	
	    	}
	    	
	    	if(node.getObjectId().getText().equals("copy")){
	    		node.setType(node.getExpr().getType());
	    		return;
	    	}
	    	if(node.getObjectId().getText().equals("length")){
	    		node.setType("Int");
	    		return;
	    	}
	    	
	    	LinkedList<Methods> info = tablec.getMethodTable().get(node.getObjectId().getText());
	    	
	    	Methods mInfo = info.get(0);
	        for(int i = 0;i<info.size();i++){
	        	mInfo = info.get(i);
	        	if(mInfo.getClassType().equals(node.getExpr().getType())){
	        		i = info.size();
	        	}
	        }
	        
	        String type;
	        if(mInfo.getReturnType().equals("SELF_TYPE")){
	        	type = mInfo.getClassType();
	        	node.setType(type);
	        	return;
	        }

	        node.setType(mInfo.getReturnType());

	    }
	    
	    @Override
	    public void outACallExpr(ACallExpr node){
	    	
	    	String type = tablec.getMethodTable().get(node.getObjectId().getText()).get(0).getReturnType();
	    	if(type.equals("SELF_TYPE")){
	    		type = type.concat(" of "+node.getInClass());
	    		node.setType(type);
	    		return;
	    	}
	    	node.setType(type);
	    	
	    	Methods minfo = tablec.getMethodTableC().get(node.getInClass()+"."+node.getObjectId().getText());
	    	LinkedList<Formals> formals = minfo.getFormals();
	    	if(formals.size() != node.getExpr().size()){
	    	
	    	}else{
	    		for(int i = 0;i<formals.size();i++){
	    			String formalType = formals.get(i).getType();
	    			if(formalType.contains("SELF_TYPE")){
	    				formalType = formalType.substring(13);
	    			}
	    			String expType = node.getExpr().get(i).getType();
	    			if(expType.contains("SELF_TYPE")){
	    				expType = expType.substring(13);
	    			}
	    			if(!formalType.equals(expType)){
	    				errorManager.getErrors().add(Error.FORMALS_FAILED_LONG);
	    				
	    			}
	    		}
	    	}
	    }
	    
	    @Override
		public void outANegExpr(ANegExpr node){
			node.setType(node.getExpr().getType());
		}
	    
	    @Override
	    public void outAMinusExpr(AMinusExpr node){
	    	if(!node.getL().getType().equals("Int") || !node.getR().getType().equals("Int")){
	    		errorManager.getErrors().add(Error.NOT_INT_PARAMS);
	    	}
	    }
	    
	    @Override
	    public void outAPlusExpr(APlusExpr node){
	    	if(!node.getL().getType().equals("Int") || !node.getR().getType().equals("Int")){
	    		errorManager.getErrors().add(Error.NOT_INT_PARAMS);
	    	}
	    }
	    
	    @Override
	    public void outAMultExpr(AMultExpr node){
	    	if(!node.getL().getType().equals("Int") || !node.getR().getType().equals("Int")){
	    		errorManager.getErrors().add(Error.NOT_INT_PARAMS);
	    	}
	    }
	    
	    @Override
	    public void outADivExpr(ADivExpr node){
	    	if(!node.getL().getType().equals("Int") || !node.getR().getType().equals("Int")){
	    		errorManager.getErrors().add(Error.NOT_INT_PARAMS);
	    	}
	    }
	    
	    @Override
	    public void outAEqExpr(AEqExpr node){
	    	if(node.getL().getType().equals("String") || node.getR().getType().equals("String")){
	    		if(!node.getL().getType().equals(node.getR().getType())){
		    		errorManager.getErrors().add(Error.BASIC_COMPARE);
		    	}
	    	}
	    	if(node.getL().getType().equals("Int")|| node.getR().getType().equals("Int")){
	    		if(!node.getL().getType().equals(node.getR().getType())){
		    		errorManager.getErrors().add(Error.BASIC_COMPARE);
		    	}
	    	}
	    	if(node.getL().getType().equals("Bool")|| node.getR().getType().equals("Bool")){
	    		if(!node.getL().getType().equals(node.getR().getType())){
		    		errorManager.getErrors().add(Error.BASIC_COMPARE);
		    	}
	    	}
	    }
	    
	    @Override
	    public void outAWhileExpr(AWhileExpr node){
	    	if(!node.getTest().getType().equals("Bool")){
	    		errorManager.getErrors().add(Error.BAD_LOOP);
	    	}
	    }
	    
	    public boolean invalidType(String typeA, String typeB){

	    	if(typeA.equals(typeB)){
	        	return false;
	        }
	    	
	    	Vertex v = graph.findVertexByName(typeA);
	        Vertex v2 = graph.findVertexByName(typeB);
	        
	        LinkedList<String> types = new LinkedList<String>(); 
	        if(v.getOutgoingEdges() == null){
	        	return true;
	        }
	        
	        List<Edge> edges =  v.getOutgoingEdges();
		        if(edges.size() > 0){
			        Vertex aux = v.getOutgoingEdge(0).getTo();
			        while(aux != null){
			        	types.add(aux.getName());
			        	edges =  aux.getOutgoingEdges();
			        	if(edges.size() > 0){
			        		aux = aux.getOutgoingEdge(0).getTo();
			        	}else{
			        		aux = null;
			        	}
			        }
		        }
		        
		        boolean error = true;
		        boolean end = false;
		        Iterator i = types.iterator();
		        while(i.hasNext() && !end){
		        	String str = (String) i.next();
		        	if(typeB.equals(str)){
		        		error = false;
		        		end = false;
		        	}
		        }
		        return error;
	        
	    }

	    public void checkOverriding(AMethodFeature node) {
			String classType = node.getInClass();
			Vertex v = graph.findVertexByName(classType);
			Vertex parent = v.getIncomingEdge(0).getFrom();
			boolean flag = false;
			while(!parent.getName().equals("Object") && !flag){
				if(tablec.getMethodTableC().containsKey(parent.getName()+"."+node.getObjectId().getText())){
					Methods parentMethod = tablec.getMethodTableC().get(parent.getName()+"."+node.getObjectId().getText());
					LinkedList<Formals> parentFormals = parentMethod.getFormals();
					LinkedList<PFormal> currentFormals = node.getFormal();
					if(parentFormals.size() != currentFormals.size()){
						errorManager.getErrors().add(Error.DIFF_N_FORMALS);
					}else{
						for(int i = 0;i<parentFormals.size();i++){
							Formals parentFormal = parentFormals.get(i);
							AFormal currentFormal = (AFormal) currentFormals.get(i);
							if(!parentFormal.getType().equals(currentFormal.getTypeId().getText())){
								errorManager.getErrors().add(Error.BAD_REDEFINITION);
							}
						}
					}
				}
				if(parent.getIncomingEdgeCount() > 0){
					parent = parent.getIncomingEdge(0).getFrom();
				}else{
					flag = true;
				}
			}
		}
}
