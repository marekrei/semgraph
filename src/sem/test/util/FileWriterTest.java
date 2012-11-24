package sem.test.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sem.util.FileWriter;

public class FileWriterTest {
	
	String dir = "semtests/";
	String file = dir + "semtest-filewriter.txt";

	@Before
	public void setUp() throws Exception {
		File d = new File(dir);
		if(!d.exists())
			d.mkdir();
	}
	
	@After
	public void tearDown() throws Exception {
		File f;
		if((f = new File(file)).exists())
			f.delete();
	}

	@Test
	public void test() {
		FileWriter fw = new FileWriter(file);
		fw.writeln("This is line 1");
		fw.write("This is ");
		fw.write("line 2");
		fw.write("\n");
		fw.writeln("This is line 3");
		fw.flush();
		fw.close();
		
		try{
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int count = 1;
			
			while ((strLine = br.readLine()) != null)   {
				assertTrue(strLine.equals("This is line " + count));
				count++;
			}
			assertTrue(count == 4);
			
			in.close();
		}catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

}
