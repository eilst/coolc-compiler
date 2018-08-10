package coolc.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import au.com.bytecode.opencsv.CSVReader;
import coolc.compiler.autogen.lexer.LexerException;
import coolc.compiler.autogen.node.Start;
import coolc.compiler.autogen.parser.ParserException;
import coolc.compiler.exceptions.SemanticException;
import coolc.compiler.visitors.ASTPrinterTypes;
import coolc.compiler.util.Error;

public class SemanticTest {
	public final String outputPath = "target/outputSemantic/";
	public final String inputPath = "src/test/resources/semantic/input/";
	public final String refPath = "src/test/resources/semantic/reference/";
	public final String cases = "src/test/resources/semantic/";
	
	// The compiler class just assembles behavior
	Compiler compiler;
	// semanticFacade concrete class
	Class<?> klass;
	// The instance (recreated every test)
	SemanticFacade semanticFacade;
	
	/**
	 * Read CSV with information about test cases
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Iterator<Object[]> readCases(String file) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(cases + file), ';');
		ArrayList<Object[]> list = new ArrayList<Object[]>();

		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			list.add( new Object[] { nextLine[0], nextLine[2] } );
		}
		
		reader.close();
		return list.iterator();
	}

	/**
	 * Successful cases.
	 * 
	 * @return
	 * @throws IOException
	 */
	@DataProvider(name = "filesProviderOk")
	public Iterator<Object[]> readCasesOk() throws IOException {
		return readCases("cases.ok");
	}

	/**
	 * Beginning every test (set of test methods)
	 * @param context
	 * @throws ClassNotFoundException
	 */
	@BeforeTest
	public void getParameter(ITestContext context) throws ClassNotFoundException {
		klass = Class.forName(context.getCurrentXmlTest().getParameter("semanticFacade"));
		
		compiler = new CompilerImpl();
	}
	
	@BeforeTest
	public void createOutput() {
		File output = new File(outputPath);
		output.mkdirs();
	}

	
	/**
	 * Before running every method, the semanticFacade gets a new instance.
	 * 
	 * BEWARE, IF YOU USE SINGLETONS, RESET THE STATE HERE.
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@BeforeMethod
	public void getFacade() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> ctor = klass.getConstructor();
		semanticFacade = (SemanticFacade)ctor.newInstance();
		
		ErrorManager.getInstance().reset();
		// If you use singletons, reset here!!!!
		// Example:
		// ClassTable.getInstance().resetInstance();
	}

	/**
	 * Test for success cases.
	 * 
	 * Configure the compiler with the facade and call the semantic analysis.
	 * 
	 * @param file
	 * @param testName
	 * @throws SemanticException
	 * @throws IOException
	 * @throws LexerException
	 * @throws ParserException
	 */
	@Test(dataProvider = "filesProviderOk")
	public void testOk(String file, String testName) throws SemanticException, IOException, LexerException, ParserException {
		PrintStream out = new PrintStream(new FileOutputStream(outputPath + file + ".out"));

		compiler.setup(semanticFacade, null);
		Start start = compiler.lexAndParse(new File(inputPath + file), out);
		if(start != null){
			compiler.semanticCheck(start, out);
		}
		
		
		start.apply(new ASTPrinterTypes(out));
		out.close();
		
		Iterator<String> refLines = FileUtils.readLines(new File(refPath + file + ".out")).iterator();
		Iterator<String> outLines = FileUtils.readLines(new File(outputPath + file + ".out")).iterator();
		
		while(true) {				
			if (!refLines.hasNext()) break;
			String r = refLines.next();
			String o = outLines.next();
			assert r.compareTo(o) == 0 : String.format("%s -> %s, reference=[%s], output=[%s]", file, testName, r, o);
		}
	}

	/**
	 * The error cases get checked with a SET of errors.
	 * @param file
	 * @param setTest
	 * @throws LexerException
	 * @throws IOException
	 * @throws ParserException
	 * @throws SemanticException
	 */
	public void genericBad(String file, Set<Error> setTest) throws LexerException, IOException, ParserException, SemanticException {
		Set<Error> setResult = null;
		
		PrintStream out = new PrintStream(new FileOutputStream(outputPath + file + ".out"));
		compiler.setup(semanticFacade, null);
		Start start = compiler.lexAndParse(new File(inputPath + file), out);
		
		try {
			compiler.semanticCheck(start, out);
		} catch (SemanticException e) {
			setResult = compiler.getErrors();
			assert setResult.containsAll(setTest);
			return;
		}			
		assert false;
	}
	
	/*************************************************
	 * Error cases
	 *************************************************/
	
	
	@Test
	public void testBad1() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("anattributenamedself.cool", EnumSet.of(Error.SELF_ATTR));
	}
	@Test
	public void testBad2() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("assignnoconform.cool", EnumSet.of(Error.BAD_ASSIGNMENT));
	}
	@Test
	public void testBad3() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("attrbadinit.cool", EnumSet.of(Error.UNDECL_IDENTIFIER));
	}
	@Test
	public void testBad4() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("attroverride.cool", EnumSet.of(Error.ATTR_INHERITED));
	}
	@Test
	public void testBad5() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("badargs1.cool", EnumSet.of(Error.FORMALS_FAILED_LONG));
	}
	@Test
	public void testBad6() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("badarith.cool", EnumSet.of(Error.NOT_INT_PARAMS));
	}
	@Test
	public void testBad7() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("baddispatch.cool", EnumSet.of(Error.DISPATCH_UNDEFINED));
	}
	@Test
	public void testBad8() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("badequalitytest.cool", EnumSet.of(Error.BASIC_COMPARE));
	}
	@Test
	public void testBad9() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("badequalitytest2.cool", EnumSet.of(Error.BASIC_COMPARE));
	}
	@Test
	public void testBad10() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("badmethodcallsitself.cool", EnumSet.of(Error.FORMALS_FAILED_LONG));
	}
	@Test
	public void testBad11() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("badredefineint.cool", EnumSet.of(Error.REDEF_BASIC));
	}
	@Test
	public void testBad12() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("badstaticdispatch.cool", EnumSet.of(Error.STATIC_FAIL_TYPE));
	}
	@Test
	public void testBad13() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("badwhilebody.cool", EnumSet.of(Error.DISPATCH_UNDEFINED));
	}
	@Test
	public void testBad14() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("badwhilecond.cool", EnumSet.of(Error.BAD_LOOP));
	}
	@Test
	public void testBad15() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("caseidenticalbranch.cool", EnumSet.of(Error.DUPLICATE_BRANCH));
	}
	@Test
	public void testBad16() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("dupformals.cool", EnumSet.of(Error.FORMAL_REDEFINITION));
	}
	@Test
	public void testBad17() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("inheritsbool.cool", EnumSet.of(Error.INHERIT_BASIC));
	}
	@Test
	public void testBad18() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("inheritsselftype.cool", EnumSet.of(Error.INHERIT_SELF_TYPE, Error.NO_MAIN));
	}
	@Test
	public void testBad19() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("inheritsstring.cool", EnumSet.of(Error.INHERIT_BASIC));
	}
	@Test
	public void testBad20() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("letself.cool", EnumSet.of(Error.SELF_IN_LET));
	}
	@Test
	public void testBad21() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("lubtest.cool", EnumSet.of(Error.BAD_INFERRED));
	}
	@Test
	public void testBad22() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("missingclass.cool", EnumSet.of(Error.CANNOT_INHERIT));
	}
	@Test
	public void testBad23() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("nomain.cool", EnumSet.of(Error.NO_MAIN));
	}
	@Test
	public void testBad24() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("outofscope.cool", EnumSet.of(Error.UNDECL_IDENTIFIER));
	}
	@Test
	public void testBad25() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("overridingmethod4.cool", EnumSet.of(Error.BAD_REDEFINITION));
	}
	@Test
	public void testBad26() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("signaturechange.cool", EnumSet.of(Error.DIFF_N_FORMALS));
	}
	@Test
	public void testBad27() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("redefinedclass.cool", EnumSet.of(Error.REDEFINED));
	}
	@Test
	public void testBad28() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("redefinedobject.cool", EnumSet.of(Error.REDEF_BASIC));
	}
	@Test
	public void testBad29() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("returntypenoexist.cool", EnumSet.of(Error.TYPE_NOT_FOUND));
	}
	@Test
	public void testBad30() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("self-assignment.cool", EnumSet.of(Error.ASSIGN_SELF, Error.BAD_INFERRED));
	}
	@Test
	public void testBad31() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("selfinformalparameter.cool", EnumSet.of(Error.SELF_FORMAL));
	}
	@Test
	public void testBad32() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("selftypebadreturn.cool", EnumSet.of(Error.BAD_INFERRED));
	}
	@Test
	public void testBad33() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("selftypeparameterposition.cool", EnumSet.of(Error.SELF_TYPE_FORMAL, Error.UNDECL_IDENTIFIER));
	}
	@Test
	public void testBad34() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("selftyperedeclared.cool", EnumSet.of(Error.REDEF_BASIC));
	}
	@Test
	public void testBad35() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("trickyatdispatch2.cool", EnumSet.of(Error.STATIC_FAIL_TYPE, Error.BAD_INFERRED));
	}
	@Test
	public void testBad36() throws LexerException, IOException, ParserException, SemanticException {
		genericBad("anattributenamedself.cool", EnumSet.of(Error.SELF_ATTR));
	}

}