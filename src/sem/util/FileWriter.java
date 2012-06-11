package sem.util;

import java.io.BufferedWriter;
import java.io.IOException;

public class FileWriter {
	private BufferedWriter writer;
	private String filename;
	
	public FileWriter(String filename){
		this.filename = filename;
		this.init();
	}
	
	private void init(){
		try{
			java.io.FileWriter fstream = new java.io.FileWriter(this.filename);
		    writer = new BufferedWriter(fstream);
		}catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void write(String string){
		try {
			writer.write(string);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void writeln(String string){
		write(string + "\n");
	}
	
	public void close(){
		try {
			if(this.writer != null)
				writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void write(String text, String file){
		FileWriter fw = new FileWriter(file);
		fw.write(text);
		fw.close();
	}
	
	public void flush(){
		try {
			if(this.writer != null)
				this.writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void reset(){
		this.close();
		this.init();
	}
}
