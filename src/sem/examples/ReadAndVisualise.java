package sem.examples;

import java.util.ArrayList;

import sem.graph.Edge;
import sem.graph.Graph;
import sem.graph.Node;
import sem.graphreader.GraphFormatException;
import sem.graphreader.GraphReader;
import sem.graphreader.RaspXmlGraphReader;
import sem.graphvis.GraphVisualiser;

public class ReadAndVisualise {
	public static void main(String[] agrs){
		try {
			// Open the reader
			GraphReader reader = new RaspXmlGraphReader("examples/raspxml/file1.xml", RaspXmlGraphReader.NODES_TOKENS, false, false);
			
			// Create a list for storing the graphs
			ArrayList<Graph> graphs = new ArrayList<Graph>();
			
			// Iterate over graphs
			while(reader.hasNext()){ 
			    Graph graph = reader.next();
			    graphs.add(graph);
			    
			    // Iterate over nodes
			    for(Node node : graph.getNodes()) 
			        System.out.println("NODE: " + node.getLemma() + " " + node.getPos());
			    
			    // Iterate over edges
			    for(Edge edge : graph.getEdges()) 
			        System.out.println("EDGE: " + edge.getLabel() + " " + edge.getHead().getLemma() + " " + edge.getDep().getLemma());
			   
			    System.out.println();
			}
			
			// Close the reader
			reader.close();
			
			// Run the visualiser
			GraphVisualiser graphVisualiser = new GraphVisualiser(false);
			graphVisualiser.displayGraphs(graphs);
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
}
