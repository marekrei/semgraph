package sem.examples;

import sem.graph.Graph;
import sem.graphreader.CnCGraphReader;
import sem.graphreader.GraphFormatException;
import sem.graphreader.GraphReader;
import sem.graphreader.ParsevalGraphReader;
import sem.graphreader.RaspGraphReader;
import sem.graphreader.RaspXmlGraphReader;
import sem.graphreader.TSVGraphReader;
import sem.graphwriter.GraphWriter;
import sem.graphwriter.TikzDependencyGraphWriter;

public class ConvertGraphToTex {
	public static void main(String[] args) {
		try {
			GraphReader reader = null;
			GraphWriter writer = null;
			
			if(args.length == 0){
				reader = new RaspXmlGraphReader("examples/raspxml/file2.xml", RaspXmlGraphReader.NODES_TOKENS, false, false);
				writer = new TikzDependencyGraphWriter("examples/tikzdependency/file2.tex", true, false, true);
			}
			else if(args.length == 3){
				if(args[0].equalsIgnoreCase("rasp"))
					reader = new RaspGraphReader(args[1], true);
				else if(args[0].equalsIgnoreCase("raspxml"))
					reader = new RaspXmlGraphReader(args[1], RaspXmlGraphReader.NODES_TOKENS, true, false);
				else if(args[0].equalsIgnoreCase("cnc"))
					reader = new CnCGraphReader(args[1]);
				else if(args[0].equalsIgnoreCase("parseval"))
					reader = new ParsevalGraphReader(args[1]);
				else if(args[0].equalsIgnoreCase("tsv"))
					reader = new TSVGraphReader(args[1], true);
				
				writer = new TikzDependencyGraphWriter(args[2], true, false, true);
			}
		
			if(reader == null || writer == null){
				System.out.println("Usage: ConvertGraphToTex <inputtype> <inputpath> <outputpath>");
				System.exit(1);
			}
			
			
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
