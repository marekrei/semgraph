package sem.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Graph structure.
 *
 */
public class Graph implements Cloneable{
	private ArrayList<Node> nodes;
	private ArrayList<Edge> edges;
	private HashMap<String,String> metadata;
	
	/**
	 * Node for representing ellipes in the graph.
	 */
	public static Node ellip = new Node("[[ellip]]", "[[ellippos]]");
	
	/**
	 * Node for representing missing nodes. For example, passive verbs in RASP are represented as an edge without a dependent.
	 */
	public static Node nil = new Node("[[null]]", "[[nullpos]]");
	
	/**
	 * Create new graph.
	 */
	public Graph(){
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
		this.metadata = null;
	}
	
	/**
	 * Get the list of nodes in this graph.
	 * @return Nodes
	 */
	public ArrayList<Node> getNodes() {
		return this.nodes;
	}
	
	/**
	 * Get the list of edges in this graph.
	 * @return Edges
	 */
	public ArrayList<Edge> getEdges() {
		return this.edges;
	}
	
	/**
	 * Add a new node to the graph.
	 * @param n	Node to be added.
	 * @return	Node that was added.
	 */
	public Node addNode(Node n) {
		this.nodes.add(n);
		return n;
	}
	
	/**
	 * Create and add a new node.
	 * @param lemma	Lemma
	 * @param pos	POS
	 * @return	The new node that was added.
	 */
	public Node addNode(String lemma, String pos) {
		Node n = new Node(lemma, pos);
		return this.addNode(n);
	}
	
	/**
	 * Add a new edge to the graph.
	 * @param e	Edge.
	 * @return	The edge that was added.
	 */
	public Edge addEdge(Edge e) {
		this.edges.add(e);
		return e;
	}
	
	/**
	 * Create and add a new edge.
	 * @param label	Label of the edge.
	 * @param head	Head node of the edge.
	 * @param dep	Dependent node of the edge.
	 * @return		The edge that was added.
	 */
	public Edge addEdge(String label, Node head, Node dep) {
		return this.addEdge(new Edge(label, head, dep));
	}
	
	/**
	 * Add metadata to the graph. The graph contains a hashmap which the reader can use to store various information.
	 * For example: putData("orig", "This is the original sentence before parsing");
	 * The data can then be retrieved using the getMetadata() method.
	 * @param key 	Key
	 * @param value Value
	 * @see 		getMetadata
	 */
	public void putMetadata(String key, String value){
		if(this.metadata == null)
			this.metadata = new HashMap<String,String>();
		this.metadata.put(key, value);
	}
	
	/**
	 * Get metadata from the graph.
	 * @param key Key
	 * @return Value
	 * @see putMetadata
	 */
	public String getMetadata(String key){
		if(this.metadata != null)
			return this.metadata.get(key);
		return null;
	}
	
	/**
	 * Get the HashMap containing all metadata.
	 * @return	Hashmap with the metadata.
	 */
	public HashMap<String,String> getMetadata(){
		return this.metadata;
	}
	
	/**
	 * Check whether the graph has any metadata attached.
	 * @return	True if there is metadata, false otherwise.
	 */
	public boolean hasMetadata(){
		if(this.metadata == null || this.metadata.size() == 0)
			return false;
		return true;
	}
	
	/**
	 * Print the graph information into standard output.
	 */
	public void print(){
		System.out.println("------------------");
		System.out.println(this.toString());
	}
	
	/**
	 * Create a string representation of the graph.
	 */
	@Override
	public String toString(){
		String string = "";
		string += "::: Nodes: \n";
		for(Node n : this.nodes)
			string += n.toString() + "\n";
		string += "::: Edges: \n";
		for(Edge e : this.edges)
			string += e.toString() + "\n";
		return string;
	}
	
	/**
	 * Create a new independent graph with identical nodes and edges.
	 */
	public Graph clone(){
		Graph graph = new Graph();
		
		// cloning nodes
		for(Node node : this.getNodes()){
			graph.addNode(node.clone());
		}
		
		//cloning edges
		Node head = null, dep = null;
		for(Edge edge : edges){
			head = null;
			dep = null;
			if(this.nodes.contains(edge.getHead()))
				head = graph.getNodes().get(this.getNodes().indexOf(edge.getHead()));
			else {
				throw new RuntimeException("Found a node as the head of an edge that is not present in the list of nodes.");
			}
			
			if(this.nodes.contains(edge.getDep()))
				dep = graph.getNodes().get(this.getNodes().indexOf(edge.getDep()));
			else {
				throw new RuntimeException("Found a node as the dependent of an edge that is not present in the list of nodes.");
			}
			
			if(head != null && dep != null){
				graph.addEdge(new Edge(edge.getLabel(), head, dep));
			}
		}
		
		// cloning metadata
		if(this.metadata != null)
			for(Entry<String,String> e : this.metadata.entrySet()){
				graph.putMetadata(e.getKey(), e.getValue());
			}
		
		return graph;
	}
}
