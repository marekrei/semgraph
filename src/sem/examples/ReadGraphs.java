package sem.examples;

import sem.exception.GraphFormatException;
import sem.graph.Edge;
import sem.graph.Graph;
import sem.graph.Node;
import sem.graphreader.RaspXmlGraphReader;

/**
 * Example class.
 * Shows how to iterate over graphs and print out some information.
 */
public class ReadGraphs {

	public static void main(String[] args) {
		try {
			RaspXmlGraphReader reader = new RaspXmlGraphReader("examples/raspxml/file1.xml", RaspXmlGraphReader.NODES_TOKENS, false, false);
			while(reader.hasNext()){ 
				// Get the next graph
				Graph graph = reader.next();
				// Iterate over nodes
				for(Node node : graph.getNodes()) 
					System.out.println("NODE: " + node.getLemma() + " " + node.getPos());
				// Iterate over edges
				for(Edge edge : graph.getEdges()) 
					System.out.println("EDGE: " + edge.getLabel() + " " + edge.getHead().getLemma() + " " + edge.getDep().getLemma());
				System.out.println();
			}
			reader.close();
		} catch (GraphFormatException e) {
			e.printLine(); // The error usually contains the line that was being parsed when the error occurred.
			e.printStackTrace();
		}
		
	}
	
}
