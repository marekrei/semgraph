package sem.test.graph;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import sem.graph.Edge;
import sem.graph.Graph;
import sem.graph.Node;

/**
 * Collection of tests for the graphs, nodes and edges.
 *
 */
public class GraphTest {
	
	/**
	 * Creating a new graph.
	 */
	@Test
	public void testGraph(){
		Graph graph = new Graph();
		assertTrue(graph != null);
	}
	
	/**
	 * Creating a new node and adding it to the graph.
	 */
	@Test
	public void testAddNode() {
		Node n1 = new Node("Lemma1", "POS1");
		Graph graph = new Graph();
		assertTrue(graph.getNodes().size() == 0);
		
		graph.addNode(n1);
		assertTrue(graph.getNodes().size() == 1);
		
		assertTrue(graph.getNodes().get(0).getLemma().equals("Lemma1"));
		assertTrue(graph.getNodes().get(0).getPos().equals("POS1"));
		assertTrue(graph.getNodes().get(0).getLabel().equals("Lemma1_POS1"));
	}

	/**
	 * Creating a new node directly from the lemma and POS, and adding it to the graph.
	 */
	@Test
	public void testAddNodeString() {
		Graph graph = new Graph();
		assertTrue(graph.getNodes().size() == 0);
		
		graph.addNode("Lemma1", "POS1");
		assertTrue(graph.getNodes().size() == 1);
		
		assertTrue(graph.getNodes().get(0).getLemma().equals("Lemma1"));
		assertTrue(graph.getNodes().get(0).getPos().equals("POS1"));
		assertTrue(graph.getNodes().get(0).getLabel().equals("Lemma1_POS1"));
	}

	/**
	 * Adding a new edge.
	 */
	@Test
	public void testAddEdge() {
		Graph graph = new Graph();
		Node n1 = new Node("Label1", "POS1");
		Node n2 = new Node("Label2", "POS2");
		Edge edge = new Edge("rel1", n1, n2);
		
		assertTrue(graph.getEdges().size() == 0);
		graph.addEdge(edge);
		assertTrue(graph.getEdges().size() == 1);
		
		assertTrue(graph.getEdges().get(0).getHead().getLemma().equals("Label1"));
		assertTrue(graph.getEdges().get(0).getHead().getPos().equals("POS1"));
		assertTrue(graph.getEdges().get(0).getHead().getLabel().equals("Label1_POS1"));
		
		assertTrue(graph.getEdges().get(0).getDep().getLemma().equals("Label2"));
		assertTrue(graph.getEdges().get(0).getDep().getPos().equals("POS2"));
		assertTrue(graph.getEdges().get(0).getDep().getLabel().equals("Label2_POS2"));
		
		assertTrue(graph.getEdges().get(0).getLabel().equals("rel1"));
	}

	/**
	 * Adding a new edge from string label.
	 */
	@Test
	public void testAddEdgeStringNodeNode() {
		Graph graph = new Graph();
		Node n1 = new Node("Label1", "POS1");
		Node n2 = new Node("Label2", "POS2");
		
		assertTrue(graph.getEdges().size() == 0);
		graph.addEdge("rel1", n1, n2);
		assertTrue(graph.getEdges().size() == 1);
		
		assertTrue(graph.getEdges().get(0).getHead().getLemma().equals("Label1"));
		assertTrue(graph.getEdges().get(0).getHead().getPos().equals("POS1"));
		assertTrue(graph.getEdges().get(0).getHead().getLabel().equals("Label1_POS1"));
		
		assertTrue(graph.getEdges().get(0).getDep().getLemma().equals("Label2"));
		assertTrue(graph.getEdges().get(0).getDep().getPos().equals("POS2"));
		assertTrue(graph.getEdges().get(0).getDep().getLabel().equals("Label2_POS2"));
		
		assertTrue(graph.getEdges().get(0).getLabel().equals("rel1"));
	}

	/**
	 * Getting the list of nodes.
	 */
	@Test
	public void testGetNodes() {
		Graph graph = new Graph();
		assertTrue(graph.getNodes().size() == 0);
		
		Node n1 = new Node("Lemma1", "POS1");
		Node n2 = new Node("Lemma2", "POS2");
		graph.addNode(n1);
		assertTrue(graph.getNodes().size() == 1);
		graph.addNode(n2);
		assertTrue(graph.getNodes().size() == 2);
		
		for(int i = 0; i < 2; i++){
			assertTrue(graph.getNodes().get(i).getLemma().equals("Lemma" + (i+1)));
			assertTrue(graph.getNodes().get(i).getPos().equals("POS" + (i+1)));
			assertTrue(graph.getNodes().get(i).getLabel().equals("Lemma" + (i+1) + "_" + "POS" + (i+1)));
		}
	}

	/**
	 * Getting the list of edges.
	 */
	@Test
	public void testGetEdges() {
		Graph graph = new Graph();
		assertTrue(graph.getEdges().size() == 0);
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.add(new Node("Lemma1", "POS1"));
		nodes.add(new Node("Lemma2", "POS2"));
		nodes.add(new Node("Lemma3", "POS3"));
		nodes.add(new Node("Lemma4", "POS4"));
		
		for(int i = 1; i < 4; i++){
			graph.addEdge("rel" + i, nodes.get(i-1), nodes.get(i));
		}
		
		assertTrue(graph.getEdges().size() == 3);
		for(int i = 1; i < 4; i++){
			assertTrue(graph.getEdges().get(i-1).getLabel().equals("rel"+i));
			assertTrue(graph.getEdges().get(i-1).getHead().getLabel().equals("Lemma"+i+"_POS"+i));
			assertTrue(graph.getEdges().get(i-1).getDep().getLabel().equals("Lemma"+(i+1)+"_POS"+(i+1)));
		}
	}

	/**
	 * Adding metadata.
	 */
	@Test
	public void testPutData() {
		Graph graph = new Graph();
		assertTrue(graph.getMetadata("key1") == null);
		graph.putMetadata("key1", "value1");
		graph.putMetadata("key2", "value2");
		assertTrue(graph.getMetadata("key1").equals("value1"));
		assertTrue(graph.getMetadata("key2").equals("value2"));
	}

	/**
	 * Cloning the graph.
	 */
	@Test
	public void testClone() {
		Graph graph = new Graph();
		Node n1 = new Node("Lemma1", "POS1");
		Node n2 = new Node("Lemma2", "POS2");
		Node n3 = new Node("Lemma3", "POS3");
		
		Edge e1 = new Edge("rel1", n1, n2);
		Edge e2 = new Edge("rel2", n2, n3);
		
		graph.addNode(n1);
		graph.addNode(n2);
		graph.addNode(n3);
		
		graph.addEdge(e1);
		graph.addEdge(e2);
		
		graph.putMetadata("key1", "value1");
		graph.putMetadata("key2", "value2");
		
		Graph clone = graph.clone();
		
		assertTrue(clone.getNodes().size() == 3);
		assertTrue(clone.getEdges().size() == 2);
		
		for(int i = 1; i <= 3; i++){
			assertTrue(clone.getNodes().get(i-1).getLabel().equals("Lemma" + i + "_POS" + i));
		}
		
		for(int i = 1; i <= 2; i++){
			assertTrue(clone.getEdges().get(i-1).getLabel().equals("rel"+i));
			assertTrue(clone.getEdges().get(i-1).getHead().getLabel().equals("Lemma"+i+"_POS"+i));
			assertTrue(clone.getEdges().get(i-1).getDep().getLabel().equals("Lemma"+(i+1)+"_POS"+(i+1)));
		}
		
		assertTrue(clone.getMetadata("key1").equals("value1"));
		assertTrue(clone.getMetadata("key2").equals("value2"));
		
		assertTrue(graph.toString().equals(clone.toString()));
		
		clone.getNodes().get(0).setLemma("Newlemma1");
		assertTrue(!graph.toString().equals(clone.toString()));
		
		clone = graph.clone();
		clone.addNode("Lemma4", "POS4");
		assertTrue(!graph.toString().equals(clone.toString()));
		
		clone = graph.clone();
		clone.getEdges().get(0).setLabel("Newlabel1");
		assertTrue(!graph.toString().equals(clone.toString()));
		
		clone = graph.clone();
		clone.addEdge("rel3", n3, n2);
		assertTrue(!graph.toString().equals(clone.toString()));
		
		clone = graph.clone();
		clone.getEdges().remove(0);
		assertTrue(!graph.toString().equals(clone.toString()));
		
		clone = graph.clone();
		clone.getNodes().remove(0);
		assertTrue(!graph.toString().equals(clone.toString()));
		
		clone = graph.clone();
		clone.putMetadata("key1", "newvalue1");
		assertTrue(graph.getMetadata("key1").equals("value1"));
		assertTrue(clone.getMetadata("key1").equals("newvalue1"));
		
	}

}
