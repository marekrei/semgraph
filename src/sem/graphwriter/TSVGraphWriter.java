package sem.graphwriter;

import java.util.List;

import sem.graph.Graph;
import sem.util.FileWriter;

/**
 * Graph writer for the TSV format.
 *
 */
public class TSVGraphWriter implements GraphWriter{
	private FileWriter fileWriter;
	
	public TSVGraphWriter(String file){
		this.open(file);
	}
	
	/**
	 * Open the writer (can be done from the constructor).
	 */
	@Override
	public void open(String file) {
		if(this.fileWriter != null)
			this.fileWriter.close();
		this.fileWriter = new FileWriter(file);
	}

	/**
	 * Close the writer.
	 */
	@Override
	public void close() {
		if(this.fileWriter != null)
			this.fileWriter.close();
	}

	/**
	 * Write a graph (as the only graph in a sentence).
	 */
	public void write(Graph graph) {
		fileWriter.writeln("<s>");
		
		writeGraph(graph);
		
		fileWriter.writeln("</s>");
	}
	
	/**
	 * Write a sentence (list of graphs).
	 */
	public void write(List<Graph> sentence){
		fileWriter.writeln("<s>");
		for(Graph g : sentence)
			writeGraph(g);
		fileWriter.writeln("</s>");
	}
	
	private void writeGraph(Graph graph){
		fileWriter.writeln("<g>");
		fileWriter.writeln("<lem>");
		for(int i = 0; i < graph.getNodes().size(); i++)
			fileWriter.writeln("" + (i+1) + "\t" + graph.getNodes().get(i).getLemma() + "\t" + graph.getNodes().get(i).getPos());
		fileWriter.writeln("</lem>");
		
		fileWriter.writeln("<gr>");
		for(int i = 0; i < graph.getEdges().size(); i++)
			fileWriter.writeln("" + graph.getEdges().get(i).getLabel() + "\t" + (graph.getNodes().indexOf(graph.getEdges().get(i).getHead())+1) + "\t" + (graph.getNodes().indexOf(graph.getEdges().get(i).getDep())+1));
		fileWriter.writeln("</gr>");
		fileWriter.writeln("</g>");
	}
	
	/**
	 * Reset the writer.
	 */
	public void reset(){
		this.close();
		this.fileWriter.reset();
	}
}
