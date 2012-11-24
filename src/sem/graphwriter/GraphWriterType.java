package sem.graphwriter;

import sem.exception.GraphFormatException;

/**
 * Contains a list of GraphWriter types and creates a new GraphWriter based on the String label.
 *
 */
public enum GraphWriterType {
	TIKZDEPENDENCY("tikzdependency"),
	TSV("tsv")
	;
	
	private final String label;
	
	private GraphWriterType(String label){
		this.label = label.toLowerCase();
	}
	
	public String getLabel(){
		return this.label;
	}
	
	public static GraphWriterType getType(String label){
		for(GraphWriterType graphWriterType : GraphWriterType.values())
			if(graphWriterType.getLabel().equalsIgnoreCase(label))
				return graphWriterType;
		return null;
	}
	
	public GraphWriter makeGraphWriter(String outputPath) throws GraphFormatException{
		switch(this){
		case TIKZDEPENDENCY:
			return new TikzDependencyGraphWriter(outputPath, true, false, true);
		case TSV:
			return new TSVGraphWriter(outputPath);
		default:
			throw new RuntimeException("No graphwriter defined for corpus type: " + (this != null?this.getLabel():this));
		}
	}
}
