package sem.test.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sem.util.Tools;

public class ToolsTest {
	
	String dir = "semtests/";
	String file = dir + "semtest-tools.txt";
	
	public static void initTestDir(String dir){
		File directory = new File(dir);
		if(directory.exists()){
			for(File f : directory.listFiles())
				f.delete();
		}
		else
			directory.mkdir();
	}
	
	public static void removeTestDir(String dir){
		File directory = new File(dir);
		if(directory.exists()){
			for(File f : directory.listFiles())
				f.delete();
			directory.delete();
		}
	}

	@Before
	public void setUp() throws Exception {
		ToolsTest.initTestDir(dir);
	}

	@After
	public void tearDown() throws Exception {
		ToolsTest.removeTestDir(dir);
	}
	
	@Test
	public void testRunCommand() {
		File f = new File(file);
		if(f.exists())
			f.delete();
	
		Tools.runCommand("touch " + file);
		assertTrue(f.exists());
		
		Tools.runCommand("rm " + file);
		assertTrue(!f.exists());
	}

	@Test
	public void testIsInt() {
		assertTrue(Tools.isInt(null) == false);
		assertTrue(Tools.isInt("") == false);
		assertTrue(Tools.isInt("hello") == false);
		assertTrue(Tools.isInt("6") == true);
		assertTrue(Tools.isInt("19") == true);
		assertTrue(Tools.isInt(".19") == false);
		assertTrue(Tools.isInt("asdasd8") == false);
	}

	@Test
	public void testIsBrack() {
		assertTrue(Tools.isBrack("(") == true);
		assertTrue(Tools.isBrack(")") == true);
		assertTrue(Tools.isBrack("{") == true);
		assertTrue(Tools.isBrack("}") == true);
		assertTrue(Tools.isBrack("<") == true);
		assertTrue(Tools.isBrack(">") == true);
		assertTrue(Tools.isBrack("[") == true);
		assertTrue(Tools.isBrack("]") == true);
		
		assertTrue(Tools.isBrack(null) == false);
		assertTrue(Tools.isBrack("") == false);
		assertTrue(Tools.isBrack("asd") == false);
		assertTrue(Tools.isBrack("9") == false);
		assertTrue(Tools.isBrack("(8)") == false);
		assertTrue(Tools.isBrack("a)") == false);
		
	}

	@Test
	public void testIsEndBrack() {
		assertTrue(Tools.isEndBrack("(") == false);
		assertTrue(Tools.isEndBrack(")") == true);
		assertTrue(Tools.isEndBrack("{") == false);
		assertTrue(Tools.isEndBrack("}") == true);
		assertTrue(Tools.isEndBrack("<") == false);
		assertTrue(Tools.isEndBrack(">") == true);
		assertTrue(Tools.isEndBrack("[") == false);
		assertTrue(Tools.isEndBrack("]") == true);
		
		assertTrue(Tools.isEndBrack(null) == false);
		assertTrue(Tools.isEndBrack("") == false);
		assertTrue(Tools.isEndBrack("asd") == false);
		assertTrue(Tools.isEndBrack("9") == false);
		assertTrue(Tools.isEndBrack("(8)") == false);
		assertTrue(Tools.isEndBrack("a)") == false);
	}

	@Test
	public void testIsStartBrack() {
		assertTrue(Tools.isStartBrack("(") == true);
		assertTrue(Tools.isStartBrack(")") == false);
		assertTrue(Tools.isStartBrack("{") == true);
		assertTrue(Tools.isStartBrack("}") == false);
		assertTrue(Tools.isStartBrack("<") == true);
		assertTrue(Tools.isStartBrack(">") == false);
		assertTrue(Tools.isStartBrack("[") == true);
		assertTrue(Tools.isStartBrack("]") == false);
		
		assertTrue(Tools.isStartBrack(null) == false);
		assertTrue(Tools.isStartBrack("") == false);
		assertTrue(Tools.isStartBrack("asd") == false);
		assertTrue(Tools.isStartBrack("9") == false);
		assertTrue(Tools.isStartBrack("(8)") == false);
		assertTrue(Tools.isStartBrack("a)") == false);
	}

	@Test
	public void testGaussianPdfDouble() {
		assertTrue(Math.abs(Tools.gaussianPdf(0) - 0.3989422804014327) < 0.0000001);
		assertTrue(Math.abs(Tools.gaussianPdf(1) - 0.24197072451914337) < 0.0000001);
		assertTrue(Math.abs(Tools.gaussianPdf(-1) - 0.24197072451914337) < 0.0000001);
	}

	@Test
	public void testGaussianPdfDoubleDoubleDouble() {
		assertTrue(Math.abs(Tools.gaussianPdf(10, 10, 3) - 0.1329807601338109) < 0.0000001);
		assertTrue(Math.abs(Tools.gaussianPdf(9, 10, 3) - 0.12579440923099774) < 0.0000001);
	}

	@Test
	public void testIsFirstLetterCapitalized() {
		assertTrue(Tools.isFirstLetterCapitalized(null) == false);
		assertTrue(Tools.isFirstLetterCapitalized("") == false);
		assertTrue(Tools.isFirstLetterCapitalized("test") == false);
		assertTrue(Tools.isFirstLetterCapitalized("Test") == true);
		assertTrue(Tools.isFirstLetterCapitalized("TEST") == true);
	}

	@Test
	public void testGetInt() {
		assertTrue(Tools.getInt(null, -1) == -1);
		assertTrue(Tools.getInt("", -1) == -1);
		assertTrue(Tools.getInt("hello", -1) == -1);
		assertTrue(Tools.getInt("7h6", -1) == -1);
		assertTrue(Tools.getInt("70", -1) == 70);
		assertTrue(Tools.getInt("-5", -1) == -5);
		assertTrue(Tools.getInt("0", -1) == 0);
	}

	@Test
	public void testGetDouble() {
		assertTrue(Tools.getDouble(null, 0.0) == 0.0);
		assertTrue(Tools.getDouble("", 0.0) == 0.0);
		assertTrue(Tools.getDouble("asd", 0.0) == 0.0);
		assertTrue(Tools.getDouble("76", 0.0) == 76.0);
		assertTrue(Tools.getDouble("6g5", 0.0) == 0.0);
		assertTrue(Tools.getDouble("0.54", 0.0) == 0.54);
		assertTrue(Tools.getDouble("-2.2", 0.0) == -2.2);
	}

	@Test
	public void testGetFileList() {
		File f = new File(file);
		if(f.exists())
			f.delete();
		
		
		ArrayList<String> list1 = Tools.getFileList(dir);
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<String> list2 = Tools.getFileList(dir);
		
		assertTrue(list2.size() - list1.size() == 1);
		f.delete();
	}

	@Test
	public void testSave() {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("key1", "val1");
		map.put("key2", "val2");
		Tools.save(map, file);
		
		map = (HashMap<String,String>)Tools.load(file);
		assertTrue(map.size() == 2);
		assertTrue(map.get("key1").equals("val1"));
		assertTrue(map.get("key2").equals("val2"));
	}

	@Test
	public void testPearsonAndSpearman() {
		HashMap<Integer,Double> map1 = new HashMap<Integer,Double>();
    	HashMap<Integer,Double> map2 = new HashMap<Integer,Double>();
    	
    	map1.put(1, 106.0);
    	map1.put(2, 86.0);
    	map1.put(3, 100.0);
    	map1.put(4, 101.0);
    	map1.put(5, 99.0);
    	map1.put(6, 103.0);
    	map1.put(7, 97.0);
    	map1.put(8, 113.0);
    	map1.put(9, 112.0);
    	map1.put(10, 110.0);
    	
    	map2.put(1, 7.0);
    	map2.put(2, 0.0);
    	map2.put(3, 27.0);
    	map2.put(4, 50.0);
    	map2.put(5, 28.0);
    	map2.put(6, 29.0);
    	map2.put(7, 20.0);
    	map2.put(8, 12.0);
    	map2.put(9, 6.0);
    	map2.put(10, 17.0);
    	
    	map1.put(11, 103.0);
    	map1.put(12, 103.0);
    	map1.put(13, 86.0);
    	map1.put(14, 112.0);
    	map1.put(15, 112.0);
    	map1.put(16, 112.0);
    	
    	map2.put(11, 7.0);
    	map2.put(12, 29.0);
    	map2.put(13, 16.0);
    	map2.put(14, 7.0);
    	map2.put(15, 16.0);
    	map2.put(16, 2.0);
    	
    	assertTrue(Math.abs(Tools.pearson(map1, map2) - -0.156351184949153) < 0.00000001);
    	assertTrue(Math.abs(Tools.spearman(map1, map2) - -0.31964874409991) < 0.00000001);
	}

	@Test
	public void testSort() {
		LinkedHashMap<String,Double> map = new LinkedHashMap<String,Double>();
		map.put("5", 5.0);
		map.put("2", 2.0);
		map.put("4", 4.0);
		map.put("1", 1.0);
		map.put("3", 3.0);
		
		// sorting ascending
		int count = 0;
		for(Entry<String,Double> e : Tools.sort(map, false).entrySet()){
			assertTrue(e.getKey().equals("" + (count+1)));
			assertTrue(e.getValue() == (count+1));
			count++;
		}
		assertTrue(count == 5);
		
		// sorting descending
		count = 5;
		for(Entry<String,Double> e : Tools.sort(map, true).entrySet()){
			assertTrue(e.getKey().equals("" + (count)));
			assertTrue(e.getValue() == (count));
			count--;
		}
		assertTrue(count == 0);
		
		// the original map should be the same
		assertTrue(map.size() == 5);
		String string = "";
		for(Entry<String,Double> e : map.entrySet())
			string += e.getKey();
		assertTrue(string.equals("52413"));
	}
}
