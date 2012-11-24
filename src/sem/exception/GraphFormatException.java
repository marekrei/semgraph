package sem.exception;

/**
 * Exception for unexpected errors when reading in the graph.
 *
 */
public class GraphFormatException extends Exception{
	private String message;
	private String line;
	
	public GraphFormatException(String message) {
		super(message);
		this.message = message;
		this.line = null;
    }
	
	public GraphFormatException(String message, String line) {
        super(message + " : " + line);
        this.message = message;
        this.line = line;
    }
	
	public String getLine(){
		return this.line;
	}
	
	public void printLine(){
		System.out.println(this.getLine());
	}

}
