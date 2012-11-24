package sem.examples;

import sem.graph.Edge;
import sem.graph.Graph;
import sem.graph.Node;

/**
 * Example class.
 * Shows how to construct a graph with nodes and edges.
 *
 */
public class ConstructGraphs {
	public static void main(String[] args) {
		Graph graph = new Graph();
		
		Node node1 = new Node("submarine", "NN");
		Node node2 = new Node("yellow", "JJ");
		Edge edge1 = new Edge("mod", node1, node2);
		graph.addNode(node1);
		graph.addNode(node2);
		graph.addEdge(edge1);
		
		Node node3 = graph.addNode("big", "JJ");
		graph.addEdge("mod", node1, node3);
		
	}

}
