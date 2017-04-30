package coolc.compiler;

import java.io.PrintStream;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import coolc.compiler.autogen.analysis.DepthFirstAdapter;
import coolc.compiler.autogen.node.AClassDecl;
import coolc.compiler.autogen.node.AIntExpr;
import coolc.compiler.autogen.node.AMethodFeature;
import coolc.compiler.autogen.node.APlusExpr;
import coolc.compiler.autogen.node.Start;

public class ARMCodegen extends DepthFirstAdapter implements CodegenFacade {

	private PrintStream out;
	private Start start;
	STGroup group;
	ST st;
	
	StringBuffer sb;

	@Override
	public void setup(Start start, PrintStream out) {
		this.start = start;
		this.out = out;
		sb = new StringBuffer();
		
	}

	@Override
	public void gen() {
		group = new STGroupFile("src/main/resources/cool/compiler/templates/arm.stg");
		st = group.getInstanceOf("base");

//		*** Global Declarations
//		*** Constants
//	    1. String literals
//        1.1 Get list of literals (each must have a unique index)
//        1.2 Get list of integer constants (of string sizes')
//        1.3 Replace in the template:
//            - tag 
//            - object size: [tag, size, ptr to dispTab, ptr to int, (len(contenido)+1)%4] = ?
//                (+1 because the 0 at the end of strings)
//            - index of the ptr of int (with the size)
//            - value (in the string)
//    2. Integer literals
//        2.1 Literals neccessary for the strings' sizes
//        2.2 + constants in source code
//        2.3 Replace in the template:
//            - tag 
//            - object size: [tag, size, ptr to dispTab and content] = 4 words
//            - value
//		** Tables
//	    1. class_nameTab: table for the name of the classes in string
//        1.1 The objects were already declared above
//        1.2 The tag of each class is used for the offset from class_nameTab
//    2. class_objTab: prototipes and constructors for each object
//        2.1 Indexed by tag: 2*tag -> protObj, 2*tag+1 -> init
//    3. dispTab fo reach class
//        3.1 Listing of the methods for each class considering inheritance
//		*** protObjs
//			Attributes, also consider inherited ones.
//
//		*** Global declarations of the text segment
//		*** Constructors (init) for each class
//		*** Methods
		
		// Note this is like instance some type of inner class to hold the values
		st.addAggr("strings.{idx,tag,size,sizeIdx,value}", 1, 5, 8, 0, "String 1");
		st.addAggr("strings.{idx,tag,size,sizeIdx,value}", 2, 5, 11, 0, "Hello World");
		st.addAggr("strings.{idx,tag,size,sizeIdx,value}", 3, 5, 16, 0, "Cool compiler");
		
		st.addAggr("ints.{idx,tag,value}", 1, 3, 15);
		st.addAggr("ints.{idx,tag,value}", 1, 3, 28);
		
		
		start.apply(this);
		st.add("text", lastResult);
		String result = st.render();
		
		out.print(result);
	}
	
	String lastResult;
	
	@Override
	public void inAIntExpr(AIntExpr node) {
		ST st;
		st = group.getInstanceOf("intExpr");
		// TODO: Here you need to put some way that the constant know
		// its name in assembly, for example maybe "34" is int_const3
		// st.add("e", node.getIntConst().codeRef());
		// or
		// st.add("e", node.getIntConst().codeRef());
		// whatever, for the example I will let it fixed
		st.add("e", "int_const27");
		
		lastResult = st.render();
	}
	
	private AClassDecl klass;
	@Override
	public void inAClassDecl(AClassDecl node) {
		klass = node;
	}
	
	
	@Override
	public void inAMethodFeature(AMethodFeature node) {
		st.add("text", klass.getName().getText() + "." + node.getObjectId().getText() + ":\n");
	}
	
	@Override
	public void outAMethodFeature(AMethodFeature node) {
		//st.add("text", sb.append(lastResult));
	}
	
	@Override
	public void caseAPlusExpr(APlusExpr node) {
		ST st;
		st = group.getInstanceOf("addExpr");
		node.getL().apply(this);
		st.add("left", lastResult);
		node.getR().apply(this);
		st.add("right", lastResult);
		lastResult = st.render();
	}

}
