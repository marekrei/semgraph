package sem.graphreader;

import java.util.ArrayList;
import java.util.Iterator;

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
	
	public ParsevalGraphReader(String inputPath) throws GraphFormatException{
		this.reader = new FileReader(inputPath, "\n");
		this.nextGraph = null;
		this.next();
	}
	
	private Graph readNextGraph() throws GraphFormatException{
		String line, metaData = "", type;
		Graph graph = null;
		String[] attributes;
		ArrayList<String> headInfo, depInfo;
		Node head, dep;
		int headId, depId;
		
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
				if(line.length() >= 2 && line.charAt(0) == '(' && line.charAt(line.length()-1) == ')')
					line = line.substring(1, line.length()-1);
				attributes = line.split("\\s+");
				type = attributes[0];
				
				if(RaspGraphReader.grsWithSubtype.contains(type) && attributes.length > 3){
					attributes[1] = attributes[2];
					attributes[2] = attributes[3];
				}
				
				// Resolving the head
				head = null;
				if(attributes[1].equals(ellipLemma)){
					head = Graph.ellip.clone();
				}
				else{
					headInfo = RaspGraphReader.parseLabel(attributes[1]);
					if(headInfo.get(2) == null){
						head = new Node(headInfo.get(0), headInfo.get(3));
					}
					else {
						headId = Tools.getInt(headInfo.get(2), -1)-1;
						if(headId < 0)
							throw new GraphFormatException("Head ID is negative.", line);
						while(graph.getNodes().size() <= headId)
							graph.getNodes().add(null);
						if(graph.getNodes().get(headId) == null){
							graph.getNodes().remove(headId);
							head = new Node(headInfo.get(0), headInfo.get(3));
							graph.getNodes().add(headId, head);
						}
						else
							head = graph.getNodes().get(headId);
					}
				}
				
				// Resolving the dependent
				dep = null;
				if(attributes.length < 3){
					dep = Graph.nil.clone();
				}
				else {
					depInfo = RaspGraphReader.parseLabel(attributes[2]);
					if(depInfo.get(2) == null){
						dep = new Node(depInfo.get(0), depInfo.get(3));
					}
					else {
						depId = Tools.getInt(depInfo.get(2), -1)-1;
						if(depId < 0)
							throw new GraphFormatException("Dependent ID is negative.", line);
						while(graph.getNodes().size() <= depId)
							graph.getNodes().add(null);
						if(graph.getNodes().get(depId) == null){
							graph.getNodes().remove(depId);
							dep = new Node(depInfo.get(0), depInfo.get(3));
							graph.getNodes().add(depId, dep);
						}
						else
							dep = graph.getNodes().get(depId);
					}
				}

				if(head == null || dep == null)
					throw new GraphFormatException("Unable to head or dependent.", line);
				
				graph.addEdge(type, head, dep);
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
			graph.putMetadata("data", metaData.trim());
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
			ParsevalGraphReader reader = new ParsevalGraphReader("examples/parseval/file1.parseval");
			
			while(reader.hasNext()){
				reader.next().print();
			}
		} catch (GraphFormatException e) {
			e.printLine();
			e.printStackTrace();
		}
	}
}
