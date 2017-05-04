package coolc.compiler;

import java.io.PrintStream;

import coolc.compiler.autogen.node.Start;

public interface CodegenFacade {
	public void setup(Start start, PrintStream out);
	public void gen();
}
