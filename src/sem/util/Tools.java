package sem.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Collection of small useful functions.
 *
 */
public class Tools {	
	
	/**
	 * Run a system command.
	 * @param command
	 */
	public static void runCommand(String command)
	{
		String[] cmd = {"/bin/sh", "-c", command};
		Runtime runtime = Runtime.getRuntime();
		try {
			Process p = runtime.exec(cmd);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Check whether the string is an integer.
	 * @param string
	 * @return
	 */
	public static boolean isInt(String string)
	{
		try{
			Integer.parseInt(string);
			return true;
		}catch(Exception e)
		{
			return false;
		}
	}
	
	/**
	 * Check whether the string is a bracket.
	 * @param string
	 * @return
	 */
	public static boolean isBrack(String string)
	{
		if(isEndBrack(string) || isStartBrack(string))
			return true;
		return false;
	}
	
	/**
	 * Check whether the string is an ending bracket.
	 * @param string
	 * @return
	 */
	public static boolean isEndBrack(String string)
	{
		if(string == null)
			return false;
		if(string.equals(")") || string.equals("]") || string.equals("}") || string.equals(">"))
			return true;
		return false;
	}
	
	/**
	 * Check whether the string is a starting bracket.
	 * @param string
	 * @return
	 */
	public static boolean isStartBrack(String string)
	{
		if(string == null)
			return false;
		if(string.equals("(") || string.equals("[") || string.equals("{") || string.equals("<"))
			return true;
		return false;
	}
	
	/**
	 * Calculate the value based on the Gaussian probability density function.
	 * @param x
	 * @return
	 */
    public static double gaussianPdf(double x) {
        return Math.exp(-1*x*x / 2) / Math.sqrt(2 * Math.PI);
    }

	/**
	 * Calculate the value based on the Gaussian probability density function.
	 * @param x
	 * @return
	 */
    public static double gaussianPdf(double x, double mean, double stddev) {
        return gaussianPdf((x - mean) / stddev) / stddev;
    }
    
    public static boolean isFirstLetterCapitalized(String string)
    {
    	if(string == null)
    		return false;
    	if(string.length() < 1)
    		return false;
    	if(Character.isUpperCase(string.charAt(0)))
    		return true;
    	return false;
    }
    
    /**
     * Get the integer from text. If not an integer, return the default value.
     * @param text
     * @param def
     * @return
     */
    public static int getInt(String text, int def)
    {
    	try{
    		return Integer.parseInt(text);
    	} catch (Exception e)
    	{
    		return def;
    	}
    }
    
    /**
     * Get the double from text. If not a double, return the default value.
     * @param text
     * @param def
     * @return
     */
    public static double getDouble(String text, double def)
    {
    	try{
    		return Double.parseDouble(text);
    	} catch (Exception e)
    	{
    		return def;
    	}
    }
    
    /**
     * Get the list of files in a directory.
     * @param dir
     * @return
     */
    public static ArrayList<String> getFileList(String dir)
    {
    	ArrayList<String> fileList = new ArrayList<String>();
    	for(String file : (new File(dir)).list())
    		fileList.add(dir+file);
    	Collections.sort(fileList);
    	return fileList;
    }
    
    /**
     * Serialise the object into a file.
     * @param obj
     * @param file
     */
    public static void save(Object obj, String file)
    {
    	FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(file);
			out = new ObjectOutputStream(fos);
			out.writeObject(obj);
			out.close();
		} catch (IOException ex) {
			System.out.println("Error in util.Tools.save()");
			ex.printStackTrace();
			System.exit(1);
		}
    }
    
    /**
     * Load the serialised object from a file.
     * @param file
     * @return
     */
    public static Object load(String file)
    {
    	Object obj = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(file);
			in = new ObjectInputStream(fis);
			obj = in.readObject();
			in.close();
		} catch (Exception ex) {
			System.out.println("Error in util.Tools.load()");
			ex.printStackTrace();
			System.exit(1);
		}
		return obj;
    }
    
    /**
     * Calculate Pearson's correlation coefficient.
     * @param v1
     * @param v2
     * @return
     */
    public static <T> double pearson(HashMap<T,Double> v1, HashMap<T,Double> v2){
    	double mean1 = 0.0, mean2 = 0.0;
    	if(v1.size() != v2.size()){
    		System.out.println("Error1: Incompatible vectors in Tools.pearson()");
    		System.exit(1);
    	}
    	
    	for(Entry<T,Double> e : v1.entrySet())
    		mean1 += e.getValue();
    	mean1 /= v1.size();
    	for(Entry<T,Double> e : v2.entrySet())
    		mean2 += e.getValue();
    	mean2 /= v2.size();
    	
    	double a = 0, b = 0, c = 0;
    	Double val2;
    	
    	for(Entry<T,Double> e : v1.entrySet()){
    		val2 = v2.get(e.getKey());
    		
    		if(val2 == null){
    			System.out.println("Error2: Incompatible vectors in Tools.pearson()");
        		System.exit(1);
    		}
    		a += (e.getValue() - mean1)*(val2 - mean2);
    		b += (e.getValue() - mean1)*(e.getValue() - mean1);
    		c += (val2 - mean2)*(val2 - mean2);
    	}
    	
    	return a / (Math.sqrt(b) * Math.sqrt(c));
    }
    
    /**
     * Sort a hashmap based on the values (descenging or ascending).
     * @param v
     * @param desc
     * @return
     */
    public static <T> LinkedHashMap<T,Double> sort(HashMap<T,Double> v, boolean desc)
	{
		class EntryComparator implements Comparator<Entry<T,Double>>{
			public int compare(Entry<T,Double> arg1, Entry<T,Double> arg2) {
				if(arg1.getValue() < arg2.getValue())
					return -1;
				else if(arg1.getValue() > arg2.getValue())
					return 1;
				return 0;
			}
		}

		ArrayList<Entry<T,Double>> list = new ArrayList<Entry<T,Double>>(v.entrySet());
		if(desc)
			Collections.sort(list, Collections.reverseOrder(new EntryComparator()));
		else
			Collections.sort(list, new EntryComparator());
		LinkedHashMap<T,Double> newv = new LinkedHashMap<T,Double>();
		for(Entry<T,Double> e : list)
			newv.put(e.getKey(), e.getValue());
		return newv;
	}
    
    public static <T> HashMap<T,Double> _convert_to_ranks(HashMap<T,Double> v){
    	LinkedHashMap<T,Double> sorted = sort(v, true);
    	ArrayList<Entry<T,Double>> sorted_array = new ArrayList<Entry<T,Double>>(sorted.entrySet());
    	HashMap<T,Double> ranks = new HashMap<T,Double>();
    	
    	int start = 0, end = 0;
    	while(start < sorted_array.size()){
    		end = start;
    		do{
    			end++;
    		}while(end < sorted_array.size() && sorted_array.get(start).getValue().doubleValue() == sorted_array.get(end).getValue().doubleValue());
    		
    		for(int k = start; k < end; k++){
    			ranks.put(sorted_array.get(k).getKey(), (double)(start+1+end)/(double)2.0);
    			//System.out.println(sorted_array.get(k).getKey() + " " + sorted_array.get(k).getValue() + " " + ranks.get(sorted_array.get(k).getKey()));
    		}
    		start = end;
    	}
    	//System.out.println();
    	return ranks;
    }
    
    /**
     * Calculate Spearman's correlation coefficient.
     * @param v1
     * @param v2
     * @return
     */
    public static <T> double spearman(HashMap<T,Double> v1, HashMap<T,Double> v2){
    	HashMap<T,Double> rv1 = _convert_to_ranks(v1);
    	HashMap<T,Double> rv2 = _convert_to_ranks(v2);
    	return pearson(rv1, rv2);
    }
    
    /**
     * Join a string using the delimiter.
     * @param pieces
     * @param delimiter
     * @return
     */
    public static String join(String[] pieces, String delimiter){
    	if(pieces.length == 0)
    		return "";
    	if(pieces.length == 1)
    		return pieces[0];
    	String result = "";
    	for(int i = 0; i < pieces.length; i++){
    		result += pieces[i];
    		if(i+1 < pieces.length)
    			result += delimiter;
    	}
    	return result;
    }
    
    /**
     * Join a string using the delimiter.
     * @param pieces
     * @param delimiter
     * @return
     */
    public static String join(List<String> pieces, String delimiter){
    	String result = "";
    	for(int i = 0; i < pieces.size(); i++){
    		result += pieces.get(1);
    		if(i+1 < pieces.size())
    			result += delimiter;
    	}
    	return result;
    }
    
    private static void testCorrelation(){
    	
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
    	
    	System.out.println("Pearson's: " + pearson(map1, map2));
    	System.out.println("Spearman's: " + spearman(map1, map2));
    	
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
    	
    	System.out.println("Pearson's: " + pearson(map1, map2));
    	System.out.println("Spearman's: " + spearman(map1, map2));
    	
    	for(Entry<Integer,Double> e : map1.entrySet()){
    		System.out.println(e.getValue() + "\t" + map2.get(e.getKey()));
    	}
    }
    
    
    
    public static void main(String[] args){
    	testCorrelation();
    }
}
