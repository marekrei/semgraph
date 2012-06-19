package sem.test.util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sem.util.FileReader;
import sem.util.FileWriter;

public class FileReaderTest {
	
	private String dir = "semtests/";
	private String file1 = dir + "semtest-filereader1.txt";
	private String file2 = dir + "semtest-filereader2.txt";
	private String file3 = dir + "semtest-filereader3.txt";

	@Before
	public void setUp() throws Exception {
		File d = new File(dir);
		if(!d.exists())
			d.mkdir();
		
		FileWriter fw = new FileWriter(file1);
		fw.writeln("This is line 1");
		fw.writeln("This is line 2");
		fw.writeln("This is line 3");
		fw.close();
		
		fw = new FileWriter(file2);
		fw.writeln("This is line 4");
		fw.writeln("This is line 5");
		fw.writeln("This is line 6");
		fw.close();
		
		fw = new FileWriter(file3);
		fw.writeln("This is line 7");
		fw.writeln("This is line 8");
		fw.writeln("This is line 9");
		fw.close();
		
	}

	@After
	public void tearDown() throws Exception {
		(new File(file1)).delete();
		(new File(file2)).delete();
		(new File(file3)).delete();
		(new File(dir)).delete();
	}

	

	
	@Test
	public void testFileRead() {
		FileReader fr = new FileReader(file2);
		
		String line;
		int count = 4;
		while(fr.hasNext()){
			line = fr.next();
			assertTrue(line.equals("This is line " + count));
			count++;
		}
		assertTrue(count == 7);
		
		fr.reset();
		count = 4;
		while(fr.hasNext()){
			line = fr.next();
			assertTrue(line.equals("This is line " + count));
			count++;
		}
		assertTrue(count == 7);
		fr.close();
		
	}
	

	@Test
	public void testDirRead() {
		FileReader fr = new FileReader(dir);
		
		String line;
		int count = 1;
		while(fr.hasNext()){
			line = fr.next();
			assertTrue(line.equals("This is line " + count));
			count++;
		}
		assertTrue(count == 10);
		
		fr.reset();
		count = 1;
		while(fr.hasNext()){
			line = fr.next();
			assertTrue(line.equals("This is line " + count));
			count++;
		}
		assertTrue(count == 10);
		fr.close();
		
	}
}
