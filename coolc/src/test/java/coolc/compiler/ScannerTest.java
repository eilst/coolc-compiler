package coolc.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import au.com.bytecode.opencsv.CSVReader;
import coolc.compiler.Main;
import coolc.compiler.autogen.lexer.LexerException;

public class ScannerTest {
	public final String outputPath = "target/outputLexer/";
	public final String inputPath = "src/test/resources/scanner/input/";
	public final String refPath = "src/test/resources/scanner/reference/";
	public final String cases = "src/test/resources/scanner/cases";
	
	@BeforeTest
	public void createOutput() {
		File output = new File(outputPath);
		output.mkdirs();
	}

	@DataProvider(name = "filesProvider")
	public Iterator<Object[]> readCases() throws IOException {
		CSVReader reader = new CSVReader(new FileReader(cases), ';');
		ArrayList<Object[]> list = new ArrayList<Object[]>();

		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			list.add( new Object[] { nextLine[0], nextLine[2] } );
		}

		return list.iterator();
	}

	@Test(dataProvider = "filesProvider")
	public void test(String file, String testName) throws LexerException, IOException {
		PrintStream out = new PrintStream(new FileOutputStream(outputPath + file + ".out"));
		new Main().lexerCheck(inputPath + file, out);
		
		File reference = new File(refPath + file + ".out");
		Iterator<String> refLines = FileUtils.readLines(reference).iterator();
		Iterator<String> outLines = FileUtils.readLines(new File(outputPath + file + ".out")).iterator();
		
		while(true) {				
			if (!refLines.hasNext()) break;
			String r = refLines.next();
			String o = outLines.next();
			assert r.compareTo(o) == 0 : String.format("%s -> %s, reference=[%s], output=[%s]", file, testName, r, o);
		}
	}

}
