package sem.examples;

import java.util.ArrayList;

import sem.exception.GraphFormatException;
import sem.graph.Graph;
import sem.graphreader.GraphReader;
import sem.graphreader.RaspXmlGraphReader;
import sem.graphvis.GraphVisualiser;

/**
 * Example class.
 * Shows how to read graphs from a file and display them in the visualiser.
 *
 */
public class VisualiseGraphs {
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
