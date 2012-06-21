package sem.examples;

import sem.graph.Graph;
import sem.graphreader.GraphFormatException;
import sem.graphreader.RaspXmlGraphReader;
import sem.graphwriter.TikzDependencyGraphWriter;

public class ConvertGraphToTex {
	public static void main(String[] args) {
		try {
			RaspXmlGraphReader reader = new RaspXmlGraphReader("examples/raspxml/file2.xml", RaspXmlGraphReader.NODES_TOKENS, false, false);
			TikzDependencyGraphWriter writer = new TikzDependencyGraphWriter(TikzDependencyGraphWriter.THEME_SIMPLE, "examples/tikzdependency/file2.tex");
			while(reader.hasNext()){
				Graph graph = reader.next();
				writer.write(graph);
			}
			reader.close();
			writer.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
}
