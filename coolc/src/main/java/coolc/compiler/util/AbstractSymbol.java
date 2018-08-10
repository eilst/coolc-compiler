package coolc.compiler.util;

import coolc.compiler.util.AbstractSymbol;

public class AbstractSymbol {
	
    protected String str;
    protected int index;
    
    public AbstractSymbol(String str, int len, int index) {
	this.str = str.length() == len ? str : str.substring(0, len);
	this.index = index;
    }

    public boolean equalString(String str, int len) {
	String other = str.length() == len ? str : str.substring(0, len);
	return this.str.equals(other);
    }

    public boolean equalsIndex(int index) {
	return this.index == index;
    }

    public boolean equals(Object another) {
	return (another instanceof AbstractSymbol) && 
	    ((AbstractSymbol)another).index == this.index;
    }

    public String getString() {	return str; }
    public String toString()  { return str;}

}
