package sem.graphreader;

import sem.exception.GraphFormatException;

/**
 * Contains a list of GraphReader types and creates a new GraphReader based on the String label.
 *
 */
public enum GraphReaderType {
	RASP("rasp"),
	RASP_XML("raspxml"),
	CNC("cnc"),
	PARSEVAL("parseval"),
	TSV("tsv")
	;
	
	private final String label;
	
	private GraphReaderType(String label){
		this.label = label.toLowerCase();
	}
	
	public String getLabel(){
		return this.label;
	}
	
	public static GraphReaderType getType(String label){
		for(GraphReaderType graphReaderType : GraphReaderType.values())
			if(graphReaderType.getLabel().equalsIgnoreCase(label))
				return graphReaderType;
		return null;
	}
	
	public GraphReader makeGraphReader(String inputPath) throws GraphFormatException{
		switch(this){
		case RASP:
			return new RaspGraphReader(inputPath, true);
		case RASP_XML:
			return new RaspXmlGraphReader(inputPath, RaspXmlGraphReader.NODES_TOKENS, true, false);
		case CNC:
			return new CnCGraphReader(inputPath);
		case PARSEVAL:
			return new ParsevalGraphReader(inputPath, false, false);
		case TSV:
			return new TSVGraphReader(inputPath, true);
		default:
			throw new RuntimeException("No graphreader defined for corpus type: " + (this != null?this.getLabel():this));
		}
	}
}
