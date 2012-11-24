package sem.graphreader;

import java.util.ArrayList;

import sem.exception.GraphFormatException;
import sem.graph.Graph;
import sem.util.FileReader;
import sem.util.Tools;

/**
 * Graph reader for the C&C parser format.
 * 
 * <p>
 * For more info: <a href="http://svn.ask.it.usyd.edu.au/trac/candc">http://svn.ask.it.usyd.edu.au/trac/candc</a>
 * </p>
 */
public class CnCGraphReader implements GraphReader{
	private FileReader reader;
	private Graph nextGraph;
	
	public CnCGraphReader(String inputPath) throws GraphFormatException{
		this.reader = new FileReader(inputPath, "\n");
		this.nextGraph = null;
		this.next();
	}
	
	private int findLemmaId(String label) throws GraphFormatException{
		if(label.length() >= 2 && label.charAt(0) == '|' && label.charAt(label.length()-1) == '|')
			label = label.substring(1, label.length()-1);
		int index = label.lastIndexOf('_');
		if(index == -1 || index + 1 == label.length())
			throw new GraphFormatException("Lemma ID not found.", label);
		int id = Tools.getInt(label.substring(index+1), -1);
		if(id < 0)
			throw new GraphFormatException("Lemma ID not found.", label);
		return id;
	}
	
	private Graph readNextGraph() throws GraphFormatException{
		String line;
		ArrayList<String> buffer = new ArrayList<String>();
		String[] tokens, attributes;
		Graph graph = null;
		int headId, depId;
		
		while (reader.hasNext()) {
			line = reader.next().trim();
			if(line.trim().length() == 0){
				if(graph == null)
					continue;
				else
					break;
			}
			else if(line.startsWith("#"))
				continue;
			else if(line.startsWith("<c>")){
				graph = new Graph();
				
				// Adding nodes
				tokens = line.split("\\s+");
				for(int i = 1; i < tokens.length; i++){
					attributes = tokens[i].split("\\|");
					if(attributes.length != 6)
						throw new GraphFormatException("Illegal number of attributes for a token.", tokens[i]);
					graph.addNode(attributes[1], attributes[2]);
				}
				
				// Adding edges
				for(String grLine : buffer){
					if(grLine.length() >= 2 && grLine.charAt(0) == '(' && grLine.charAt(grLine.length()-1) == ')')
						grLine = grLine.substring(1, grLine.length()-1);
					attributes = grLine.split("\\s+");
					String type = attributes[0];
					
					headId = -1;
					depId = -1;
					if(RaspGraphReader.grsWithSubtype.contains(type)){
						headId = findLemmaId(attributes[2]);
						depId = findLemmaId(attributes[3]);
					}
					else {
						headId = findLemmaId(attributes[1]);
						depId = findLemmaId(attributes[2]);
					}
					
					if(headId < 0 || depId < 0)
						throw new GraphFormatException("Unable to resolve head and dependent.", line);
					
					if(headId >= graph.getNodes().size() || depId >= graph.getNodes().size())
						throw new GraphFormatException("The head or dependent does not exist in the nodes.", line);
					
					graph.addEdge(type, graph.getNodes().get(headId), graph.getNodes().get(depId));
				}
				buffer.clear();
			}
			else{
				buffer.add(line);
			}
		}
		
		return graph;
	}

	/**
	 * Check whether there is another graph available.
	 * @return True if there is at least one more graph, false otherwise.
	 */
	@Override
	public boolean hasNext() {
		if(this.nextGraph == null)
			return false;
		return true;
	}

	/**
	 * Get the next graph.
	 * @return	The next graph.
	 */
	@Override
	public Graph next() throws GraphFormatException {
		Graph graph = this.nextGraph;
		this.nextGraph = readNextGraph();
		return graph;
	}

	/**
	 * Get the next sentence.
	 * @return	The next sentence.
	 */
	@Override
	public ArrayList<Graph> nextSentence() throws GraphFormatException {
		if(!this.hasNext())
			return null;
		ArrayList<Graph> sentence = new ArrayList<Graph>();
		sentence.add(this.next());
		return sentence;
	}

	/**
	 * Reset the reader.
	 */
	@Override
	public void reset() throws GraphFormatException {
		if(this.reader != null)
			this.reader.reset();
		this.nextGraph = null;
		this.next();
	}

	/**
	 * Close the reader.
	 */
	@Override
	public void close() {
		if(this.reader != null)
			this.reader.close();
		this.nextGraph = null;
	}
	
	public static void main(String[] args){
		try {
			CnCGraphReader cncr = new CnCGraphReader("examples/candc/file1_tok.candc");
			
			while(cncr.hasNext()){
				cncr.next().print();
			}
		} catch (GraphFormatException e) {
			e.printLine();
			e.printStackTrace();
		}
	}
	
}
