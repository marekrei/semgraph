package sem.graphreader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import sem.exception.GraphFormatException;
import sem.graph.Edge;
import sem.graph.Graph;
import sem.graph.Node;
import sem.util.FileReader;
import sem.util.Tools;

/**
 * Graph reader for the Parseval format.
 * It will also work with the DepBank/GR format, but it will not correctly map lemmas together, as that representation doesn't include token numbering.
 * 
 * <p>
 * Example files were parsed using:
 * <code>
 * ./rasp.sh -m -p'-e'
 * <code>
 * </p>
 */
public class ParsevalGraphReader implements GraphReader{
	private FileReader reader;
	private Graph nextGraph;
	private String ellipLemma = "ellip";
	private boolean simpleEdgeFormat;
	private boolean simpleNodeFormat;
	
	/**
	 * GraphReader for the Parseval format.
	 * The default (full) format represents nodes as lemma+suffix:id_POS or lemma+suffix:id, e.g. algorithm+s:6_NOUN or algorithm+s:6_NOUN
	 * The simple format has each node containing only the label/lemma, e.g. algorithms. We can also enable this if we want to force all the extra information to be retained in the node label.
	 * @param inputPath
	 * @param simpleFormat 		Enable simple node format
	 * @throws GraphFormatException
	 */
	public ParsevalGraphReader(String inputPath, boolean simpleEdgeFormat, boolean simpleNodeFormat) throws GraphFormatException{
		this.reader = new FileReader(inputPath, "\n");
		this.nextGraph = null;
		this.simpleEdgeFormat = simpleEdgeFormat;
		this.simpleNodeFormat = simpleNodeFormat;
		this.next();
	}
	
	private Node createNode(String label, ArrayList<Node> nodes) throws GraphFormatException{
		
		Node node;
		int nodeId;
		if(this.simpleNodeFormat){
			node = new Node(label, "POS");
		}
		else {
			LinkedHashMap<String,String> nodeInfo = RaspGraphReader.parseLabel(label);
			if(nodeInfo.get("index") == null || nodeInfo.get("index").trim().length() == 0){
				node = new Node(nodeInfo.get("lemma"), ((nodeInfo.get("pos")==null)?"POS":nodeInfo.get("pos")));
			}
			else {
				nodeId = Tools.getInt(nodeInfo.get("index"), -1)-1;
				if(nodeId < 0)
					throw new GraphFormatException("Head ID is negative.", label);
				while(nodes.size() <= nodeId)
					nodes.add(null);
				if(nodes.get(nodeId) == null){
					nodes.remove(nodeId);
					node = new Node(nodeInfo.get("lemma"), ((nodeInfo.get("pos")==null)?"POS":nodeInfo.get("pos")));
					nodes.add(nodeId, node);
				}
				else
					node = nodes.get(nodeId);
			}
		}
		return node;
	}
	
	private Graph readNextGraph() throws GraphFormatException{
		String line, metaData = "", type;
		Graph graph = null;
		String[] attributes;
		Node head, dep;
		
		while (reader.hasNext()) {
			line = reader.next().trim();
			if(line.startsWith("%"))
				continue;
			if(line.length() == 0){
				if(graph == null)
					continue;
				else
					break;
			}
			
			if(graph == null)
				graph = new Graph();
			
			if(line.startsWith("(")){
				LinkedHashMap<String,String> grInfo = RaspGraphReader.parseGr(line, this.simpleEdgeFormat);
				
				// Resolving the head
				head = null;
				if(grInfo.get("head").equals(ellipLemma)){
					head = Graph.ellip.clone();
				}
				else{
					head = createNode(grInfo.get("head"), graph.getNodes());
				}
				
				// Resolving the dependent
				dep = null;
				if(grInfo.get("type").equals("passive"))
					dep = Graph.nil.clone();
				else if(grInfo.get("dependent") == null)
					throw new GraphFormatException("Dependent is null.", line);
				else
					dep = createNode(grInfo.get("dependent"), graph.getNodes());

				if(head == null)
					throw new GraphFormatException("Unable to resolve head.", line);
				if(dep == null)
					throw new GraphFormatException("Unable to resolve dependent.", line);
				
				graph.addEdge(grInfo.get("type"), head, dep);
			}
			else {
				metaData += line + "\n";
			}
		}
		
		// Post-processing
		if(graph != null){
			// Removing placeholders
			Iterator<Node> iterator = graph.getNodes().iterator();
			while(iterator.hasNext()){
				if(iterator.next() == null)
					iterator.remove();
			}
			
			// Adding missing nodes to the nodelist
			for(Edge edge : graph.getEdges()){
				if(!graph.getNodes().contains(edge.getHead()))
					graph.addNode(edge.getHead());
				if(!graph.getNodes().contains(edge.getDep()))
					graph.addNode(edge.getDep());
			}
			graph.putMetadata("text", metaData.trim());
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
			ParsevalGraphReader reader = new ParsevalGraphReader("examples/parseval/file1.parseval", false, true);
			
			while(reader.hasNext()){
				reader.next().print();
			}
		} catch (GraphFormatException e) {
			e.printLine();
			e.printStackTrace();
		}
	}
}
