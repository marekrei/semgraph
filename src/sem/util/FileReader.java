package sem.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;

/**
 * Comparator for sorting files accoring to their path name.
 *
 */
class FileComparator implements Comparator<File>{
	@Override
	public int compare(File arg0, File arg1) {
		return arg0.getAbsolutePath().compareTo(arg1.getAbsolutePath());
	}
}

/**
 * Class for simplifying file reading.
 * Given an input file, it will read it line by line.
 * Given a directory, it will iterate through all the lines in all the files in that directory.
 *
 */
public class FileReader{
	private ArrayList<File> inputFiles;
	private File currentInputFile;
	private BufferedReader reader;
	private String nextLine;
	private String buffer;
	private String fileAddendum;
	
	/**
	 * Recursively find all files that are contained in that directory.
	 * If given a file as input, it will return only that file.
	 * @param file Main file.
	 * @return ArrayList of files.
	 */
	public static ArrayList<File> listFileRec(File file){
		ArrayList<File> files = new ArrayList<File>();
		if(file.isFile()){
			files.add(file);
		}
		else if(file.isDirectory()) {
			for(File f : file.listFiles()){
				files.addAll(listFileRec(f));
			}
		}
		Collections.sort(files, new FileComparator());
		
		return files;
	}
	
	/**
	 * Constructs a new FileReader.
	 * Takes as input the path to a file or a directory.
	 * @param inputPath Input path.
	 */
	public FileReader(String inputPath){
		this(inputPath, null);
	}
	
	/**
	 * Constructs a new FileReader.
	 * Takes as input the path to a file or a directory.
	 * Also allows for specification of a string added to the end of each file. This can be useful if we want to separate data in different files by a newline, for example.
	 * @param inputPath Input path.
	 * @param	fileAddendum	String to be appended at the end of each file.
	 */
	public FileReader(String inputPath, String fileAddendum){
		this.inputFiles = new ArrayList<File>();
		this.currentInputFile = null;
		this.reader = null;
		this.nextLine = null;
		this.fileAddendum = fileAddendum;
		
		File input = new File(inputPath);
		if(!input.exists()){
			throw new RuntimeException("Input path for FileReader is invalid: " + inputPath);
		}
		
		this.inputFiles.addAll(listFileRec(input));

		this.reset();
	}

	/**
	 * Reset the FileReader.
	 */
	public void reset(){
		try {
			if(reader != null){
				reader.close();
				reader = null;
			}
			currentInputFile = null;
			nextLine = null;
			this.buffer = null;
			this.next();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Close the FileReader.
	 */
	public void close() {
		if(reader != null){
			try {
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Shift reading to the next input file.
	 */
	private void openNextInputFile() {
		int currentIndex = -1;
		if(currentInputFile != null)
			currentIndex = inputFiles.indexOf(currentInputFile);
		
		if(currentIndex + 1 >= 0 && currentIndex + 1 < inputFiles.size())
			currentInputFile = inputFiles.get(currentIndex + 1);
		else 
			return;

		try {
			if(reader != null)
				reader.close();
			DataInputStream in;
			if(currentInputFile.getName().endsWith(".gz"))
				in = new DataInputStream(new GZIPInputStream(new FileInputStream(currentInputFile)));
			else
				in = new DataInputStream(new FileInputStream(currentInputFile));
			this.reader = new BufferedReader(new InputStreamReader(in));
			this.buffer = this.fileAddendum;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Read the next line from input.
	 * @return Next line.
	 */
	private String readNextLine(){
		String line = null;
		try {
			if(reader == null)
				this.openNextInputFile();
			if(reader == null)
				return null;
			
			line = reader.readLine();
			if(line == null){
				if(this.buffer != null && this.buffer.length() > 0){
					int index = this.buffer.indexOf('\n');
					if(index >= 0){
						line = this.buffer.substring(0, index);
						if(index +1 < this.buffer.length())
							this.buffer = this.buffer.substring(index+1, this.buffer.length());
						else
							this.buffer = "";
					}
					else{
						line = this.buffer;
						this.buffer = "";
					}
				}
				else {
					openNextInputFile();
					line = reader.readLine();
				}
			}
			if(line == null) 
				return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return line;
	}
	
	/**
	 * Get the next line.
	 */
	public String next() {
		String prevLine = this.nextLine;
		this.nextLine = readNextLine();
		return prevLine;
	}

	/**
	 * Check whether the reader has any lines left.
	 */
	public boolean hasNext() {
		if(this.nextLine != null)
			return true;
		return false;
	}
	
	/**
	 * Example use case.
	 * @param args
	 */
	public static void main(String[] args){
		FileReader fileReader = new FileReader("examples/plaintext/", "EOF");
		int count = 0;

		while(fileReader.hasNext()){
			System.out.println(count + " : " + fileReader.next());
			if(++count > 10)
				break;
		}
	}
}
