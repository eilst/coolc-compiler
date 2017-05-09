package coolc.compiler;


import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import org.jboss.util.graph.Graph;
import coolc.compiler.autogen.node.AAttributeFeature;
import coolc.compiler.autogen.node.AClassDecl;
import coolc.compiler.autogen.node.AFormal;
import coolc.compiler.autogen.node.AMethodFeature;
import coolc.compiler.autogen.node.ANoExpr;
import coolc.compiler.autogen.node.PFeature;
import coolc.compiler.autogen.node.PFormal;
import coolc.compiler.autogen.node.TObjectId;
import coolc.compiler.autogen.node.TTypeId;
import coolc.compiler.util.Attributes;
import coolc.compiler.util.Formals;
import coolc.compiler.util.Methods;
import coolc.compiler.util.Symbol;
import coolc.compiler.util.SymbolTableBuilder;

public class TableC {
	
	private static TableC instance;
	TreeMap<String,AClassDecl> table;
	Hashtable<String,LinkedList<Methods>> methodTable;
	Hashtable<String,Methods> methodTableC;
	Hashtable<String,Attributes> attributeTable;
	Hashtable<String,LinkedList<Symbol>> symbolTable;
	LinkedList<Symbol> symbolF;
	Graph graph;
	SymbolTableBuilder sTable;
	
	private TableC(){
		table = new TreeMap();
		methodTable = new Hashtable<String,LinkedList<Methods>>();
		methodTableC = new Hashtable<String,Methods>();
		attributeTable = new Hashtable<String,Attributes>();
		symbolTable = new Hashtable<String,LinkedList<Symbol>>();
		symbolF = new LinkedList<Symbol>();
		graph = new Graph();
		sTable = new SymbolTableBuilder();
		
	}
	
	public SymbolTableBuilder getsTable() {
		return sTable;
	}

	public void setsTable(SymbolTableBuilder sTable) {
		this.sTable = sTable;
	}

	public static TableC getInstance() {
		if (instance == null) {
			instance = new TableC();
		}
		return instance;
	}
	
	public void reset() {
		table = new TreeMap();
		methodTable = new Hashtable<String,LinkedList<Methods>>();
		methodTableC = new Hashtable<String,Methods>();
		attributeTable = new Hashtable<String,Attributes>();
		symbolTable = new Hashtable<String,LinkedList<Symbol>>();
		symbolF = new LinkedList<Symbol>();
		graph = new Graph();
		sTable = new SymbolTableBuilder();
	}
	
	

	public Hashtable<String, Methods> getMethodTableC() {
		return methodTableC;
	}

	public void setMethodTableC(Hashtable<String, Methods> methodTableC) {
		this.methodTableC = methodTableC;
	}

	public Hashtable<String, Attributes> getAttributeTable() {
		return attributeTable;
	}

	public void setAttributeTable(Hashtable<String, Attributes> attributeTable) {
		this.attributeTable = attributeTable;
	}

	public Hashtable<String, LinkedList<Symbol>> getSymbolTable() {
		return symbolTable;
	}

	public void setTableC(Hashtable<String, LinkedList<Symbol>> TableC) {
		this.symbolTable = TableC;
	}

	public TreeMap<String, AClassDecl> getTable() {
		return table;
	}

	public void setTable(TreeMap<String, AClassDecl> table) {
		this.table = table;
	}
	
	
	
	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public void installBasic(){
		LinkedList<PFeature> featList;
		LinkedList<PFormal> formalList;
		PFeature pf;
		 
		featList = new LinkedList<PFeature>();
		 
		
	
		 
		AClassDecl ObjectClass = new AClassDecl(
		new TTypeId("Object"),
		new TTypeId("No_class"),
		featList
		);
		
		featList = new LinkedList<PFeature>();
		 
		
		 
		AClassDecl IOClass = new AClassDecl(
		new TTypeId("IO"),
		new TTypeId("Object"),
		featList
		);
		//  no methods and only a single attribute, the
		// or integer.
		featList = new LinkedList<PFeature>();
		pf = new AAttributeFeature(
		new TObjectId("val"),
		new TTypeId("prim_slot"),
		new ANoExpr()
		);
		featList.add(pf);
		 
		AClassDecl IntClass = new AClassDecl(
		new TTypeId("Int"),
		new TTypeId("Object"),
		featList
		);
		 
		featList = new LinkedList<PFeature>();
		pf = new AAttributeFeature(
		new TObjectId("val"),
		new TTypeId("prim_slot"),
		new ANoExpr()
		);
		featList.add(pf);
		 
		AClassDecl BoolClass = new AClassDecl(
		new TTypeId("Bool"),
		new TTypeId("Object"),
		featList
		);
		 
		// The class Str has a number of slots and operations:
		// val the length of the string
		// str_field the string itself
		// length() : Int returns length of the string
		// concat(arg: Str) : Str performs string concatenation
		// substr(arg: Int, arg2: Int): Str substring selection
		featList = new LinkedList<PFeature>();
		pf = new AAttributeFeature(
		new TObjectId("val"),
		new TTypeId("Int"),
		new ANoExpr()
		);
		featList.add(pf);
		 
		pf = new AAttributeFeature(
		new TObjectId("str_field"),
		new TTypeId("prim_slot"),
		new ANoExpr()
		);
		featList.add(pf);
		 
		
		 
		
		 
		 
		AClassDecl StringClass = new AClassDecl(
		new TTypeId("String"),
		new TTypeId("Object"),
		featList
		);
		
		table.put("Object", ObjectClass);
		table.put("IO", IOClass);
		table.put("Int", IntClass);
		table.put("Bool", BoolClass);
		table.put("String", StringClass);
		LinkedList<Formals> formals = new LinkedList<Formals>();
		LinkedList<Methods> infos = new LinkedList<Methods>();
		infos.add(new Methods("Int","String",formals));
		methodTable.put("length", infos);
		
		
	
		
		
	}
	
	public void addClassToTable(AClassDecl node){
    	LinkedList<PFeature> featList,auxList;
		LinkedList<PFormal> formalList,auxFList;
		LinkedList<Formals> finfo;
		PFeature pf;
		
		//featList = node.getFeature();
		featList = new LinkedList<PFeature>();
		auxList = node.getFeature();
		Iterator it1 = auxList.iterator();
		while(it1.hasNext()){
			PFeature p = (PFeature) it1.next();
			if(p instanceof AMethodFeature){
				finfo = new LinkedList<Formals>();
				AMethodFeature mf = (AMethodFeature)p;
				auxFList = mf.getFormal();			
				LinkedList<PFormal> formals = new LinkedList<PFormal>();
				for(int i = 0;i<auxFList.size();i++){
					PFormal pformal = auxFList.get(i);
					finfo.add(new Formals(((AFormal)pformal).getObjectId().getText(),((AFormal)pformal).getTypeId().getText()));
					if(symbolTable.containsKey(((AFormal)pformal).getObjectId().getText())){
						symbolTable.get(((AFormal)pformal).getObjectId().getText()).add(new Symbol(((AFormal)pformal).getTypeId().getText(),"method",mf.getInClass()));
					}else{
						LinkedList<Symbol> symbols = new LinkedList<Symbol>();
						symbols.add(new Symbol(((AFormal)pformal).getTypeId().getText(),"method",mf.getInClass()));
						symbolTable.put(((AFormal)pformal).getObjectId().getText(),symbols);
					}
				}
				//System.out.println(mf.getObjectId().getText()+"."+mf.getInClass());
				if(methodTable.containsKey(mf.getObjectId().getText())){
					LinkedList<Methods> info = methodTable.get(mf.getObjectId().getText());
					info.add(new Methods(mf.getTypeId().getText(),mf.getInClass(),finfo));
				}else{
					LinkedList<Methods> info = new LinkedList<Methods>();
					info.add(new Methods(mf.getTypeId().getText(),mf.getInClass(),finfo));
					methodTable.put(mf.getObjectId().getText(), info);
					
				}
				
				methodTableC.put(node.getName().getText()+"."+mf.getObjectId().getText(),new Methods(mf.getTypeId().getText(),mf.getInClass(),finfo));
				
				featList.add(new AMethodFeature(
						new TObjectId(mf.getObjectId().getText()),
						formals,
						new TTypeId(mf.getTypeId().getText()),
						new ANoExpr()
						));
			}
			
			if(p instanceof AAttributeFeature){
				AAttributeFeature mf = (AAttributeFeature) p;
				pf = new AAttributeFeature(
						new TObjectId(mf.getObjectId().getText()),
						new TTypeId(mf.getTypeId().getText()),
						new ANoExpr()
						);
						featList.add(pf);
				attributeTable.put(mf.getObjectId().getText(), new Attributes(mf.getTypeId().getText(),mf.getInClass()));
				
				if(symbolTable.containsKey(mf.getObjectId().getText())){
					symbolTable.get(mf.getObjectId().getText()).add(new Symbol(mf.getTypeId().getText()
							,"method",mf.getInClass()));
				}else{
					LinkedList<Symbol> symbols = new LinkedList<Symbol>();
					symbols.add(new Symbol(mf.getTypeId().getText()
							,"method",mf.getInClass()));
					symbolTable.put(mf.getObjectId().getText(),symbols);
				}
			}
				
		}
		
		TTypeId inh;
		if(node.getInherits() != null){
			inh = new TTypeId(node.getInherits().getText());
		}else{
			inh = new TTypeId("Object");
		}
		
		AClassDecl newClass = new AClassDecl(
				new TTypeId(node.getType()),
				inh,
				featList
				);
		
		table.put(node.getType(), newClass);
    }

	public void resetInstance() {
		instance = new TableC();
		
	}
	
}
