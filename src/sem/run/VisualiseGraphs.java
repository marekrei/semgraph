package sem.run;

import java.util.ArrayList;

import sem.exception.GraphFormatException;
import sem.graph.Graph;
import sem.graphreader.GraphReader;
import sem.graphreader.GraphReaderType;
import sem.graphvis.GraphVisualiser;

public class VisualiseGraphs {
	
	public static void visualiseGraphs(String inputTypeLabel, String inputPath){
		try {
			GraphReader reader = GraphReaderType.getType(inputTypeLabel).makeGraphReader(inputPath);

			ArrayList<Graph> graphs = new ArrayList<Graph>();

			while(reader.hasNext()){ 
			    Graph graph = reader.next();
			    graphs.add(graph);
			}
			
			reader.close();
			
			GraphVisualiser graphVisualiser = new GraphVisualiser(false);
			graphVisualiser.displayGraphs(graphs);
			
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if(args.length != 2)
			System.out.println("Usage: VisualiseGraphs <inputtype> <inputpath>");
		else
			visualiseGraphs(args[0], args[1]);
	}

}
