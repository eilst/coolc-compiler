package coolc.compiler.util;
import java.util.Hashtable;
import java.util.Stack;

public class SymbolTableBuilder {
	
    private Stack<Hashtable> tbl;
    private static SymbolTableBuilder instance;
    
    public SymbolTableBuilder() {
    	tbl = new Stack();
    }
    
    public void enterScope() { tbl.push(new Hashtable());}

    public void exitScope() {
	if (tbl.empty()) { 
		System.out.println("No scope");
	}
	tbl.pop();
    }

    public static SymbolTableBuilder getInstance(){
		if(instance == null){
			instance = new SymbolTableBuilder();
		}
		return instance;
	}
    
    public void addId(AbstractSymbol id, Object info) {
	if (tbl.empty()) { 
		System.out.println("No scope");
	}
	((Hashtable)tbl.peek()).put(id, info);
    }
    
    public void addId(String id, Object info) {
    	if (tbl.empty()) { 
    		System.out.println("No scope");
    	}
    	((Hashtable)tbl.peek()).put(id, info);
        }

    public Object lookup(AbstractSymbol sym) {
	if (tbl.empty()) { 
		System.out.println("No scope");	
	}
	for (int i = tbl.size() - 1; i >= 0; i--) {
	    Object info = ((Hashtable)tbl.elementAt(i)).get(sym);
	    if (info != null) return info;
	}
	return null;
    }
    
    public Object lookup(String sym) {
    	if (tbl.empty()) { 
    		System.out.println("No scope");	
    	}
    	for (int i = tbl.size() - 1; i >= 0; i--) {
    	    Object info = ((Hashtable)tbl.elementAt(i)).get(sym);
    	    if (info != null) return info;
    	}
    	return null;
        }

    public Object probe(AbstractSymbol sym) {
	if (tbl.empty()) { 
		System.out.println("No scope");
	}
	return ((Hashtable)tbl.peek()).get(sym);
    }
    
    public String toString() {
	String res = "";
	for (int i = tbl.size() - 1, j = 0; i >= 0; i--, j++) {
	    res += "Scope " + j + ": " + tbl.elementAt(i) + "\n";
	}
	return res;
    }
}