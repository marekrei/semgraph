package sem.graphreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public static ArrayList<String> parseLabel(String label){
		if(label.length() >= 2 && label.charAt(0) == '|' && label.charAt(label.length()-1) == '|')
			label = label.substring(1, label.length()-1);
		
		Matcher matcher = labelPattern.matcher(label);
		if(matcher.matches()){
			return new ArrayList<String>(Arrays.asList(matcher.group(1), matcher.group(3), matcher.group(5), matcher.group(7)));
		}
		
		return new ArrayList<String>(Arrays.asList(label, null, null, null));
	}
	
	private ArrayList<Graph> readSentence() throws GraphFormatException{
		ArrayList<Graph> graphs = null;
		
		String line, section = "";
		ArrayList<String> attributes;
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
				if(line.charAt(0) == '(' && line.charAt(line.length()-1) == ')' && line.length() >= 2)
					line = line.substring(1, line.length()-1);
				attributes = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
				
				String type = attributes.get(0);
				if(type.charAt(0) == '|' && type.charAt(type.length()-1) == '|' && type.length() >= 2)
					type = type.substring(1, type.length()-1);
				
				if(attributes.size() >= 4 && grsWithSubtype.contains(type))
					attributes.remove(1);
				
				headNode = null;
				depNode = null;
				
				//Resolving head node
				ArrayList<String> headInfo = parseLabel(attributes.get(1));
				if(headInfo.get(2) != null && headInfo.get(2).length() > 0){
					int headId = Tools.getInt(headInfo.get(2), -1)-1;
					if(headId < 0)
						throw new GraphFormatException("Head ID is smaller than 1.", line);
					
					// adding the right amount of placeholders to the list.
					while(graph.getNodes().size() <= headId)
						graph.getNodes().add(null);
					
					if(graph.getNodes().get(headId) != null){
						headNode = graph.getNodes().get(headId);
					}
					else{
						headNode = new Node(headInfo.get(0), headInfo.get(3));
						graph.getNodes().remove(headId);
						graph.getNodes().add(headId, headNode);
					}
				}
				else if(headInfo.get(0).equals(ellipLemma)){
					headNode = Graph.ellip.clone();
				}
				else {
					headNode = new Node(headInfo.get(0), headInfo.get(3));
				}
				
				//Resolving dep node
				if(type.equals("passive")){
					depNode = Graph.nil.clone();
				}
				else{
					ArrayList<String> depInfo = parseLabel(attributes.get(2));
					if(depInfo.get(2) != null && depInfo.get(2).length() > 0){
						int depId = Tools.getInt(depInfo.get(2), -1)-1;
						if(depId < 0)
							throw new GraphFormatException("Head ID is smaller than 1.", line);
						
						// adding the right amount of placeholders to the list.
						while(graph.getNodes().size() <= depId)
							graph.getNodes().add(null);
						
						if(graph.getNodes().get(depId) != null){
							depNode = graph.getNodes().get(depId);
						}
						else{
							depNode = new Node(depInfo.get(0), depInfo.get(3));
							graph.getNodes().remove(depId);
							graph.getNodes().add(depId, depNode);
						}
					}
					else {
						depNode = new Node(headInfo.get(0), headInfo.get(3));
					}
				}
				
				if(headNode == null || depNode == null)
					throw new GraphFormatException("Head or dep could not be resolved to nodes.", line);
				
				graph.addEdge(type, headNode, depNode);
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
