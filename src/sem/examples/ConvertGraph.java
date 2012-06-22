package sem.examples;

import java.util.ArrayList;

import sem.graph.Graph;
import sem.graphreader.GraphFormatException;
import sem.graphreader.GraphReader;
import sem.graphreader.RaspXmlGraphReader;
import sem.graphwriter.GraphWriter;
import sem.graphwriter.TSVGraphWriter;

/**
 * Example class.
 * Reads in the data in RASP XML format and writes it in TSV format.
 */

public class ConvertGraph {
	public static void main(String[] args) {
		try {
			GraphReader reader = new RaspXmlGraphReader("examples/raspxml/pnp_1000.xml.gz", RaspXmlGraphReader.NODES_TOKENS, true, true);
			GraphWriter writer = new TSVGraphWriter("examples/tsv/pnp_1000.tsv");
				
			while(reader.hasNext()){
				ArrayList<Graph> sentence = reader.nextSentence();
				writer.write(sentence);
			}
			reader.close();
			writer.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}

}
