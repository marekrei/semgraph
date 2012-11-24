package sem.graphwriter;

import java.util.List;

import sem.graph.Graph;

/**
 * The general interface for a graph writer.
 *
 */
public interface GraphWriter {
	public void open(String file);
	public void write(Graph graph);
	public void write(List<Graph> sentence);
	public void close();
}
