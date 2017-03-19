package coolc.compiler.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Error {
	REDEF_BASIC("redefBasic"),
	INHERIT_BASIC("inheritBasic"),
	INHERIT_SELF_TYPE("inheritSelfType"),
	INHERIT_SELF("inheritSelf"),
	REDEFINED("redefined"),
	CANNOT_INHERIT("cannotInherit"),
	CYCLE("cycle"),
	UNDEFINED("undefined"),
	REMOVE_SCOPE("removeScope"),
	REPEAT_ID("repeatId"),
	NO_SCOPE("noScope"),
	SELF_ATTR("selfAttr"),
	TYPE_NOT_FOUND("typeNotFound"),
	ATTR_REDEFINITION("attrRedefinition"),
	ATTR_INHERITED("attrInherited"),
	SELF_METHOD("selfMethod"),
	METHOD_REDEFINITION("methodRedefinition"),
	BAD_REDEFINITION("badRedefinition"),
	DIFF_N_FORMALS("diffNFormals"),
	FORMAL_REDEFINITION("formalRedefinition"),
	SELF_FORMAL("selfFormal"),
	SELF_TYPE_FORMAL("selfTypeFormal"),
	SELF_IN_LET("selfInLet"),
	DUPLICATE_BRANCH("duplicateBranch"),
	UNDECL_IDENTIFIER("undeclIdentifier"),
	BAD_INFERRED("badInferred"),
	ASSIGN_SELF("assignSelf"),
	BAD_ASSIGNMENT("badAssignment"),
	FORMALS_FAILED_LONG("formalsFailedLong"),
	FORMALS_FAILED("formalsFailed"),
	BOOL_PARAM("boolParam"),
	NOT_INT_PARAMS("notIntParams"),
	BASIC_COMPARE("basicCompare"),
	STATIC_FAIL_TYPE("staticFailType"),
	DISPATCH_UNDEFINED("dispatchUndefined"),
	BAD_PREDICATE("badPredicate"),
	BAD_LOOP("badLoop"),
	BAD_LET_INIT("badLetInit"),
	NO_MAIN("noMain");
	
	private final String msg;
	
	Error(String msg) {
		this.msg = msg;
	}
	
    private static final Map<String, Error> lookup = new HashMap<String, Error>();

	static {
	    for(Error s : EnumSet.allOf(Error.class))
	         lookup.put("Coolc.semant." + s.getMsg(), s);
	}

	public String getMsg() { return msg; }
	
    public static Error get(String msg) { 
         return lookup.get(msg); 
    }
	
	
}
