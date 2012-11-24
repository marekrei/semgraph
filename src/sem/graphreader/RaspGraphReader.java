package sem.graphreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sem.exception.GraphFormatException;
import sem.graph.Edge;
import sem.graph.Graph;
import sem.graph.Node;
import sem.util.FileReader;
import sem.util.Tools;
/**
 * Graph reader for the default RASP format.
 * 
 * <p>The RASP toolkit: <a href="http://ilexir.co.uk/2011/open-source-rasp-release/">http://ilexir.co.uk/2011/open-source-rasp-release/</a>
 * 
 * <p>The small files were parsed with:
 * <code>./rasp.sh -m -p'-ogi -n10'</code>
 * 
 * <p>The large file was parsed with:
 * <code>./rasp.sh -m -p'-og'</code>
 *
 */
public class RaspGraphReader implements GraphReader{
	private boolean getAllParses;
	private FileReader reader;
	
	public static List<String> grsWithSubtype = Collections.unmodifiableList(new ArrayList<String>(Arrays.asList("dependent", "mod", "ncmod", "xmod", "cmod", "arg_mod", "arg", "xcomp", "ccomp", "ta")));
	public static List<String> grsWithInitialGr = Collections.unmodifiableList(new ArrayList<String>(Arrays.asList("subj", "ncsubj", "xsubj", "csubj")));
	
	private String ellipLemma = "ellip";
	
	private static Pattern labelPattern = Pattern.compile("^([^\\+:_]+)(\\+([a-zA-Z]*))?(:([0-9]+))?(_([a-zA-Z0-9]+))?$");
	
	ArrayList<Graph> nextSentence;
	int nextGraphPointer;
	
	public RaspGraphReader(String inputPath, boolean getAllParses) throws GraphFormatException{
		this.getAllParses = getAllParses;
		this.reader = new FileReader(inputPath, "\n");
		this.nextGraphPointer = 0;
		this.nextSentence = null;
		this.next();
	}
	
	/**
	 * Parses a string that represents a token/lemma.
	 * It is designed to handle formats: lemma+suffix:id_POS, lemma+suffix:id, lemma.
	 * For example: algorithm+s:6_NOUN, algorithm+s:6_NOUN, algorithms
	 * 
	 * Returns a LinkedHashMap with four fields: lemma, suffix, index, pos
	 * @param label
	 * @return
	 * @throws GraphFormatException 
	 */
	public static LinkedHashMap<String,String> parseLabel(String label) throws GraphFormatException{
		if(label == null)
			throw new RuntimeException("Input label cannot be null");
		
		LinkedHashMap<String,String> fields = new LinkedHashMap<String,String>();
		
		Matcher matcher = labelPattern.matcher(label);
		if(matcher.matches()){
			fields.put("lemma", matcher.group(1));
			fields.put("suffix", matcher.group(3));
			fields.put("index", matcher.group(5));
			fields.put("pos", matcher.group(7));
		}
		else{
			fields.put("lemma", label);
			fields.put("suffix", null);
			fields.put("index", null);
			fields.put("pos", null);
		}
		return fields;
	}
	
	/**
	 * Parses a line that represents a GR and return a LinkedHashMap with the arguments.
	 * The hashmap has five keys: type, subtype, head, dependent, initialgr.
	 * @param line
	 * @return
	 * @throws GraphFormatException 
	 */
	public static LinkedHashMap<String,String> parseGr(String line, boolean simpleEdgeFormat) throws GraphFormatException{
		LinkedHashMap<String,String> arguments = new LinkedHashMap<String,String>();
		arguments.put("type", null);
		arguments.put("sybtype", null);
		arguments.put("head", null);
		arguments.put("dependent", null);
		arguments.put("initialgr", null);
		
		String originalLine = line;
		
		// Check input for null
		if(line == null)
			throw new RuntimeException("Input line cannot be null");
		
		// Remove brackets
		if(line.length() >= 2 && line.charAt(0) == '(' && line.charAt(line.length()-1) == ')')
			line = line.substring(1, line.length()-1);
		
		// Split by whitespace
		ArrayList<String> lineParts = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
		
		// If the arguments are surrounded by bars (e.g. |ncmod|), remove them
		for(int i = 0; i < lineParts.size(); i++){
			String linePart = lineParts.get(i);
			if(linePart.length() >= 2 && linePart.charAt(0) == '|' && linePart.charAt(linePart.length()-1) == '|'){
				lineParts.add(i, linePart.substring(1, linePart.length()-1));
				lineParts.remove(i+1);
			}
			// If the type (first argument) doesn't have bars, don't modify the others either.
			else if(i == 0)
				break;
		}

		String type = lineParts.get(0);
		arguments.put("type", type);
		
		// Simple edge format
		if(simpleEdgeFormat){
			// Passive still has to have only one argument in simple Edge format
			// Other types need exactly two.
			if(type.equals("passive")){
				if(lineParts.size() == 2){
					arguments.put("head", lineParts.get(1));
				}
				else
					throw new GraphFormatException("GR has wrong number of arguments", originalLine);
			}
			else {
				if(lineParts.size() == 3){
					arguments.put("head", lineParts.get(1));
					arguments.put("dependent", lineParts.get(2));
				}
				else
					throw new GraphFormatException("GR has wrong number of arguments", originalLine);
			}
		}
		// Full edge format
		else {
			if(type.equals("passive")){
				if(lineParts.size() == 2){
					arguments.put("head", lineParts.get(1));
				}
				else
					throw new GraphFormatException("GR has wrong number of arguments", originalLine);
			}
			else if(grsWithSubtype.contains(type)){
				if(grsWithInitialGr.contains(type))
					throw new RuntimeException("Illegal state");
				if(lineParts.size() == 4){
					arguments.put("subtype", lineParts.get(1));
					arguments.put("head", lineParts.get(2));
					arguments.put("dependent", lineParts.get(3));
				}
				else
					throw new GraphFormatException("GR has wrong number of arguments", originalLine);
			}
			else if(grsWithInitialGr.contains(type)){
				if(grsWithSubtype.contains(type))
					throw new RuntimeException("Illegal state");
				if(lineParts.size() == 4){
					arguments.put("head", lineParts.get(1));
					arguments.put("dependent", lineParts.get(2));
					arguments.put("initialgr", lineParts.get(3));
				}
				else
					throw new GraphFormatException("GR has wrong number of arguments", originalLine);
			}
			else{
				if(lineParts.size() == 3){
					arguments.put("head", lineParts.get(1));
					arguments.put("dependent", lineParts.get(2));
				}
				else
					throw new GraphFormatException("GR has wrong number of arguments", originalLine);
			}
		}
		
		return arguments;
	}
	
	private ArrayList<Graph> readSentence() throws GraphFormatException{
		ArrayList<Graph> graphs = null;
		
		String line, section = "", metaData = "";
		Graph graph = null;
		Node headNode = null, depNode = null;
		
		while (reader.hasNext()) {
			line = reader.next().trim();

			if(line.trim().length() == 0){
				if(graphs == null)
					continue;
				else
					break;
			}
			
			// If we have reached here, it is not an empty line any more.
			if(graphs == null)
				graphs = new ArrayList<Graph>();
			
			if(!line.startsWith("("))
				section = line;
			if(line.startsWith("gr-list: ")){
				graph = new Graph();
				graphs.add(graph);
			}
			
			// If it's a GR
			if(line.startsWith("(") && section.startsWith("gr-list: ") && graph != null){ 
				LinkedHashMap<String,String> grInfo = parseGr(line, false);
				
				headNode = null;
				depNode = null;
				
				//Resolving head node
				LinkedHashMap<String,String> headInfo = parseLabel(grInfo.get("head"));
				if(headInfo.get("index") != null && headInfo.get("index").length() > 0){
					int headId = Tools.getInt(headInfo.get("index"), -1)-1;
					if(headId < 0)
						throw new GraphFormatException("Head ID is smaller than 1.", line);
					
					// adding the right amount of placeholders to the list.
					while(graph.getNodes().size() <= headId)
						graph.getNodes().add(null);
					
					if(graph.getNodes().get(headId) != null){
						headNode = graph.getNodes().get(headId);
					}
					else{
						headNode = new Node(headInfo.get("lemma"), headInfo.get("pos"));
						graph.getNodes().remove(headId);
						graph.getNodes().add(headId, headNode);
					}
				}
				else if(headInfo.get("lemma").equals(ellipLemma)){
					headNode = Graph.ellip.clone();
				}
				else {
					headNode = new Node(headInfo.get("lemma"), headInfo.get("pos"));
				}
				
				//Resolving dep node
				if(grInfo.get("type").equals("passive")){
					depNode = Graph.nil.clone();
				}
				else{
					LinkedHashMap<String,String> depInfo = parseLabel(grInfo.get("dependent"));
					if(depInfo.get("index") != null && depInfo.get("index").length() > 0){
						int depId = Tools.getInt(depInfo.get("index"), -1)-1;
						if(depId < 0)
							throw new GraphFormatException("Head ID is smaller than 1.", line);
						
						// adding the right amount of placeholders to the list.
						while(graph.getNodes().size() <= depId)
							graph.getNodes().add(null);
						
						if(graph.getNodes().get(depId) != null){
							depNode = graph.getNodes().get(depId);
						}
						else{
							depNode = new Node(depInfo.get("lemma"), depInfo.get("pos"));
							graph.getNodes().remove(depId);
							graph.getNodes().add(depId, depNode);
						}
					}
					else {
						depNode = new Node(headInfo.get("lemma"), headInfo.get("pos"));
					}
				}
				
				if(headNode == null || depNode == null)
					throw new GraphFormatException("Head or dep could not be resolved to nodes.", line);
				
				graph.addEdge(grInfo.get("type"), headNode, depNode);
			}
			else {
				metaData += line + "\n";
			}
		}

		// Post-processing
		if(graphs != null){
			for(Graph g : graphs){
				// Removing placeholders
				Iterator<Node> iterator = g.getNodes().iterator();
				while(iterator.hasNext()){
					if(iterator.next() == null)
						iterator.remove();
				}
				
				// Adding remaining nodes (null, ellip) to the nodelist
				for(Edge edge : g.getEdges()){
					if(!g.getNodes().contains(edge.getHead()))
						g.addNode(edge.getHead());
					if(!g.getNodes().contains(edge.getDep()))
						g.addNode(edge.getDep());
				}
				
				// Adding metadata
				g.putMetadata("text", metaData.trim());
			}
		}
		
		if(graphs != null && graphs.size() == 0)
			graphs.add(new Graph());
		
		return graphs;
	}

	/**
	 * Check whether there are more graphs available.
	 * @return	True if there are more graphs available.
	 */
	@Override
	public boolean hasNext() {
		if(this.nextSentence == null)
			return false;
		return true;
	}

	/**
	 * Get the next graph from the corpus.
	 * @return	The next graph.
	 * @throws GraphFormatException 
	 */
	@Override
	public Graph next() throws GraphFormatException {
		Graph graph = null;
		if(this.nextSentence != null){
			graph = this.nextSentence.get(nextGraphPointer);
			nextGraphPointer++;
		}
		if(this.nextSentence == null || !getAllParses || nextGraphPointer >= this.nextSentence.size()){
			this.nextSentence = readSentence();
			nextGraphPointer = 0;
		}
		return graph;
	}

	/**
	 * Read a sentence from the corpus. 
	 * This returns a list of graphs if getAllParses is set to true.
	 * @return	List of graphs
	 * @throws GraphFormatException 
	 */
	@Override
	public ArrayList<Graph> nextSentence() throws GraphFormatException {
		ArrayList<Graph> tempSentence;
		if(getAllParses)
			tempSentence = this.nextSentence;
		else{
			tempSentence = new ArrayList<Graph>();
			tempSentence.add(this.nextSentence.get(0));
		}
		this.nextSentence = readSentence();
		return tempSentence;
	}

	/**
	 * Reset the whole reading process to the beginning.
	 * @throws GraphFormatException 
	 */
	@Override
	public void reset() throws GraphFormatException {
		if(reader != null)
			reader.reset();
		this.nextSentence = null;
		this.nextGraphPointer = 0;
		this.next();
	}

	/**
	 * Close the reader.
	 */
	public void close(){
		this.reader.close();
		this.nextSentence = null;
		this.nextGraphPointer = 0;
		this.nextSentence = null;
	}

	public static void main(String[] args){
		try {
			RaspGraphReader rgr = new RaspGraphReader("examples/rasp/file1.rasp", true);
			while(rgr.hasNext()){
				rgr.next().print();
			}
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
}
