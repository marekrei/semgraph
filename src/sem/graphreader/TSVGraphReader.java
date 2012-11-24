package sem.graphreader;

import java.util.ArrayList;

import sem.exception.GraphFormatException;
import sem.graph.Edge;
import sem.graph.Graph;
import sem.graph.Node;
import sem.util.FileReader;
import sem.util.Tools;

/**
 * Class for reading graphs in the "column" format.
 * 
 * <p>The section separators are on individual lines, every node and edge has a line with tab-separated values.
 * Sentences are marked by &lt;s&gt;, graphs are marked by &lt;g&gt;, lemmas by &lt;lem&gt;, and edges by &lt;gr&gt;.
 * 
 * <p>Each lemma line has the following format:
 * <code>ID	LEMMA	POS</code>
 * 
 * <p>Each GR line has the following format:
 * <code>LABEL	HEAD_ID	DEP_ID</code>
 * 
 * <p>Ellipes are represented by [[ellip]], null nodes by [[null]].
 * They can be either in the lemma list or the directly in the graph. If they are in the graph, then corresponding nodes are added to the graph.
 * Take a look at the example files for better understanding of the format.
 * 
 * <p>If a sentence has no graphs for some reason, it will return a single empty graph (with no edges). This is to facilitate retrieval by sentence and by single graphs.
 *
 */
public class TSVGraphReader implements GraphReader{
	private boolean getAllParses;
	private FileReader reader;
	private ArrayList<Graph> nextSentence;
	private int nextGraphPointer = 0;

	private String ellipLemma = "[[ellip]]";
	private String nullLemma = "[[null]]";
	
	public TSVGraphReader(String inputPath, boolean getAllParses) throws GraphFormatException{
		this.getAllParses = getAllParses;
		this.reader = new FileReader(inputPath);
		this.nextGraphPointer = 0;
		this.nextSentence = null;
		this.next();
	}
	
	public void reset() throws GraphFormatException{
		this.reader.reset();
		this.nextSentence = null;
		this.nextGraphPointer = 0;
		this.next();
	}

	private ArrayList<Graph> readSentence() throws GraphFormatException{
		ArrayList<Graph> graphs = null;
		
		Graph graph = null;
		boolean inLemmas = false, inGrs = false;
		int nodeId, headId, depId, lemmaCount = 0;
		String line;
		String[] attributes;
		Node headNode, depNode;
		
		while (reader.hasNext()) {
			line = reader.next().trim();
			if(line.trim().length() == 0)
				continue;
			else if(line.equals("<s>")){
				graphs = new ArrayList<Graph>();
				continue;
			}
			else if(line.equals("</s>"))
				break;
			else if(line.equals("<g>")){
				graph = new Graph();
				lemmaCount = 0;
				graphs.add(graph);
			}
			else if(line.equals("<lem>"))
				inLemmas = true;
			else if(line.equals("</lem>"))
				inLemmas = false;
			else if(line.equals("<gr>"))
				inGrs = true;
			else if(line.equals("</gr>"))
				inGrs = false;
			else if(inLemmas == true && inGrs == false && graph != null){
				// Reading in a lemma / node
				attributes = line.split("\\t");
				if(attributes.length != 3)
					throw new GraphFormatException("Illegal number of lemma attributes in TSVGraphReader.", line);

				Node node;
				if(attributes[1].equals(ellipLemma))
					node = Graph.ellip.clone();
				else if(attributes[1].equals(nullLemma))
					node = Graph.nil.clone();
				else
					node = new Node(attributes[1], attributes[2]);
				graph.addNode(node);
	    		lemmaCount++;
	    		
				nodeId = Tools.getInt(attributes[0], -1)-1;
	    		if(graph.getNodes().indexOf(node) != nodeId)
	    			throw new GraphFormatException("Error in TSVGraphReader. Mismatch on IDs.", nodeId + " " + graph.getNodes().indexOf(node));
			}
			else if(inGrs == true && inLemmas == false && graph != null){
				// Reading in a GR / edge
				attributes = line.split("\\t");
				if(attributes.length != 3)
					throw new GraphFormatException("Error: Illegal number of GR attributes in TSVGraphReader.", line);
				
				headNode = null;
				depNode = null;
				
				// Resolving head node
				if(attributes[1].equals(ellipLemma)){
					headNode = Graph.ellip.clone();
					graph.addNode(headNode);
				}
				else if(attributes[1].equals(nullLemma)){
					headNode = Graph.nil.clone();
					graph.addNode(headNode);
				}
				else{
					headId = Tools.getInt(attributes[1], -1)-1;
					if(headId >= lemmaCount)
						throw new GraphFormatException("The head id of a GR does not exist in the lemmas.", line);
					else if(headId < 0)
						throw new GraphFormatException("GR head id is not a number or a negative number.", line);
					headNode = graph.getNodes().get(headId);
				}
				
				// Resolving dep node
				if(attributes[2].equals(nullLemma)){
					depNode = Graph.nil.clone();
					graph.addNode(depNode);
				}
				else{
					depId = Tools.getInt(attributes[2], -1)-1;
					if(depId >= lemmaCount)
						throw new GraphFormatException("The dep id of a GR does not exist in the lemmas.", line);
					else if(depId < 0)
						throw new GraphFormatException("The GR dep id is not a number or a negative number.", line);
					depNode = graph.getNodes().get(depId);
				}
				
				// Creating the edge
				if(headNode != null && depNode != null){
					Edge edge = new Edge(attributes[0], headNode, depNode);
					graph.addEdge(edge);
				}
				else
					throw new GraphFormatException("Head or dep node is null.", line);
			}
		}
		
		if(graphs != null && graphs.size() == 0)
			graphs.add(new Graph());
		
		return graphs;
	}

	/**
	 * Get the next graph from the corpus.
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
			nextGraphPointer = 0;
			try{
				this.nextSentence = readSentence();
			} catch (GraphFormatException e){
				this.nextSentence = new ArrayList<Graph>();
				this.nextSentence.add(new Graph());
				throw e;
			}
		}
		return graph;
	}
	
	/**
	 * Check whether there are more graphs available.
	 */
	@Override
	public boolean hasNext() {
		if(this.nextSentence == null)
			return false;
		return true;
	}
	
	@Override
	public void close(){
		this.reader.close();
		this.nextSentence = null;
	}
	
	/**
	 * Read a sentence from the corpus. 
	 * This returns a list of graphs. If there are alternative parses for this sentence, they will all be included in the list.
	 * @return
	 * @throws GraphFormatException 
	 */
	public ArrayList<Graph> nextSentence() throws GraphFormatException{
		ArrayList<Graph> tempSentence = new ArrayList<Graph>();
		if(getAllParses)
			tempSentence.addAll(this.nextSentence);
		else
			tempSentence.add(this.nextSentence.get(0));
		this.nextSentence = readSentence();
		return tempSentence;
	}
}
