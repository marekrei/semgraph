package sem.examples;

import java.util.ArrayList;

import sem.graph.Graph;
import sem.graphreader.GraphFormatException;
import sem.graphreader.GraphReader;
import sem.graphreader.RaspXmlGraphReader;
import sem.graphvis.GraphVisualiser;

/**
 * Example class.
 * Shows how to run the visualiser.
 *
 */
public class VisualiseGraph {
	public static void main(String[] args){
		try {
			ArrayList<ArrayList<Graph>> sentences = new ArrayList<ArrayList<Graph>>();
			GraphReader reader = new RaspXmlGraphReader("examples/raspxml/file1.xml", RaspXmlGraphReader.NODES_TOKENS, true, true);
			while(reader.hasNext())
				sentences.add(reader.nextSentence());
			reader.close();
			GraphVisualiser graphVis = new GraphVisualiser(false); // set to true for graph editing
			graphVis.displaySentences(sentences);
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
		
	}

}
