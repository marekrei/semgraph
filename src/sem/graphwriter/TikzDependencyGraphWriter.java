package sem.graphwriter;

import java.util.HashMap;
import java.util.List;

import sem.graph.Edge;
import sem.graph.Graph;
import sem.util.FileWriter;

/*
 * Writes the graphs in LaTeX format.
 * 
 * <p>It uses the tikz-dependency, so make sure you have it installed when trying to convert the .tex into .pdf. You need to have a sufficiently up-to-date installation of tikz as well. The tikz-dependency documentation includes steps for installing them both.
 * <p>Sometimes the edges don't get positioned nicely, leading to overlapping edge labels. So take a look at the tikz-dependency documentation or the wiki example if you want to manually adjust the properties of the graph.
 * <p>If you specify the option in the constructor, an alternative algorithm is used that tries to do custom positioning of edges. The results will vary depending on the graph. However, it only makes a difference when using the segmented edges; it has no effect on arc edges in the tikz-dependency library.
 * 
 * <p>Tikz : <a href="http://www.texample.net/tikz/">http://www.texample.net/tikz/</a>
 * <p>Tikz-dependency : <a href="http://sourceforge.net/projects/tikz-dependency/">http://sourceforge.net/projects/tikz-dependency/</a>
 * <p>Wikibooks: <a href="http://en.wikibooks.org/wiki/LaTeX/Linguistics#Dependency_Trees_using_TikZ-dependency">http://en.wikibooks.org/wiki/LaTeX/Linguistics#Dependency_Trees_using_TikZ-dependency</a>
 */
public class TikzDependencyGraphWriter implements GraphWriter{
	
	private FileWriter fileWriter;
	private int counter;
	private boolean edgeSegmented;
	private boolean edgeBubble;
	private boolean repositionEdges;
	
	/**
	 * Create a new GraphWriter for the tikz-dependency format.
	 * @param file	Output file path.
	 * @param edgeSegmented	Use segmented edges (as opposed to arc edges).
	 * @param edgeBubble	Use bubbles around edge labels.
	 * @param repositionEdges	Reposition the edges using a different algorithm.
	 */
	public TikzDependencyGraphWriter(String file, boolean edgeSegmented, boolean edgeBubble, boolean repositionEdges){
		this.counter = 0;
		this.open(file);
		this.edgeSegmented = edgeSegmented;
		this.edgeBubble = edgeBubble;
		this.repositionEdges = repositionEdges;
	}
	
	/**
	 * Escape special characters and make the String suitable for use in LaTeX.
	 * @param input	Input String.
	 * @return	Output String.
	 */
	public static String escapeLatex(String input){
		String output = "";
		for(int i = 0; i < input.length(); i++){
			char c = input.charAt(i);
			switch(c){
				case '#':
					output += "\\#";
					break;
				case '$':
					output += "\\$";
					break;
				case '%':
					output += "\\%";
					break;
				case '&':
					output += "\\&";
					break;
				case '\\':
					output += "textbackslash()";
					break;
				case '^':
					output += "\textasciicircum{}";
					break;
				case '_':
					output += "\\_";
					break;
				case '{':
					output += "\\{";
					break;
				case '}':
					output += "\\}";
					break;
				case '~':
					output += "\textasciitilde{}";
					break;
				case '[':
					output += "{[}";
					break;
				case ']':
					output += "{]}";
					break;
				default:
					output += c;
			}
		}
		return output;
	}
	
	/** 
	 * A custom algorithm for positioning the edge heights.
	 * 
	 * The default algorithm sets the edge heights only based on the distance of the two words. Here, we keep track of the number of edges over each word and set the heights so that they don't overlap. 
	 * @param graph	Input graph.
	 * @return	HashMap of custom edge heights.
	 */
	private HashMap<Edge,Double> calculateEdgeHeights(Graph graph){
		double constant = 3.0;
		
		HashMap<Edge,Double> edgeHeights = new HashMap<Edge,Double>();
		if(graph.getNodes().size() <= 1)
			return edgeHeights;
		
		double[] gapHeights = new double[graph.getNodes().size()-1];
		for(int i = 0; i < gapHeights.length; i++)
			gapHeights[i] = 0;
		
		
		for(int gap = 0; gap < graph.getNodes().size(); gap++){
			for(int i = 0; i < graph.getNodes().size()-gap; i++){
				int j = i + gap;
				for(Edge edge : graph.getEdges()){
					if((edge.getHead() == graph.getNodes().get(i) && edge.getDep() == graph.getNodes().get(j))
							|| (edge.getHead() == graph.getNodes().get(j) && edge.getDep() == graph.getNodes().get(i))){
						double maxGapHeight = 0;
						for(int k = i; k < i+gap; k++){
							if(gapHeights[k] > maxGapHeight)
								maxGapHeight = gapHeights[k];
						}
						double newHeight = maxGapHeight+1.0;
						double diff = Math.abs(j-i);
						
						edgeHeights.put(edge, constant * newHeight / diff);
						for(int k = i; k < i+gap; k++){
							gapHeights[k] = newHeight;
						}
					}
				}
			}
		}

		return edgeHeights;
	}
	
	/**
	 * Open the writer (can be done from the constructor).
	 */
	@Override
	public void open(String file) {
		if(this.fileWriter != null)
			this.fileWriter.close();
		this.fileWriter = new FileWriter(file);
		this.fileWriter.writeln("% This document was automatically created by TikzDependencyGraphWriter in the SemGraph library.");
		this.fileWriter.writeln("% You need to have the tikz and tikz-dependency packages installed in order to compile this into a pdf (e.g. using pdflatex).");
		this.fileWriter.writeln("\\documentclass{article}");
		this.fileWriter.writeln("\\usepackage{tikz}");
		this.fileWriter.writeln("\\usepackage{tikz-dependency}");
		this.fileWriter.writeln("\\usepackage[graphics,tightpage,active]{preview}");
		this.fileWriter.writeln("\\PreviewEnvironment{dependency}");
		this.fileWriter.writeln("\\begin{document}");
	}

	/**
	 * Close the writer.
	 */
	@Override
	public void close() {
		if(this.fileWriter != null){
			this.fileWriter.writeln("\\end{document}");
			this.fileWriter.close();
		}
	}

	@Override
	public void write(Graph graph) {
		
		String lemmaLine = "", posLine = "";
		for(int i = 0; i < graph.getNodes().size(); i++){
			lemmaLine += escapeLatex(graph.getNodes().get(i).getLemma()) + " ";
			posLine += escapeLatex(graph.getNodes().get(i).getPos()) + " ";
			if(i + 1 < graph.getNodes().size()){
				lemmaLine += "\\& ";
				posLine += "\\& ";
			}
		}
		
		this.counter++;
		this.fileWriter.writeln("% ---------------- GRAPH " + this.counter);
		this.fileWriter.writeln("\\begin{dependency}[" + (this.edgeSegmented?"segmented edge":"arc edge") + (this.edgeBubble?"":", text only label") + ", label style={scale=1.3" + (this.edgeBubble?"":", above") + "}]");
		this.fileWriter.writeln("\\begin{deptext}[column sep=1em]");
		this.fileWriter.writeln(lemmaLine + "\\\\");
		this.fileWriter.writeln(posLine + "\\\\");
		this.fileWriter.writeln("\\end{deptext}");
		
		HashMap<Edge,Double> edgeHeights = null; 
		if(this.repositionEdges)
			edgeHeights = calculateEdgeHeights(graph);
		for(Edge edge : graph.getEdges()){
			this.fileWriter.write("\\depedge");
			if(edgeHeights != null)
				this.fileWriter.write("[edge unit distance=" + edgeHeights.get(edge)+ "ex]");
			this.fileWriter.writeln("{" + (graph.getNodes().indexOf(edge.getHead())+1) + "}{" + (graph.getNodes().indexOf(edge.getDep())+1) + "}{" + escapeLatex(edge.getLabel()) + "}"
					+ " % (" + edge.getLabel() + " " + edge.getHead().getLabel() + " " + edge.getDep().getLabel() + ")");
		}
		
		this.fileWriter.writeln("\\end{dependency}\n");
	}

	@Override
	public void write(List<Graph> sentence) {
		for(Graph graph : sentence)
			this.write(graph);
	}

}
