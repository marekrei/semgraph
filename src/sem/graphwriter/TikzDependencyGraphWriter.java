package sem.graphwriter;

import java.util.Arrays;
import java.util.List;

import sem.graph.Edge;
import sem.graph.Graph;
import sem.util.FileWriter;

/*
 * Writes the graphs in LaTeX format.
 * 
 * <p>It uses the tikz-dependency, so make sure you have it installed when trying to convert the .tex into .pdf:
 * <a href="http://sourceforge.net/projects/tikz-dependency/">http://sourceforge.net/projects/tikz-dependency/</a>
 * 
 * <p>Sometimes the edges don't get positioned nicely, leading to overlapping edge labels. So take a look at the tikz-dependency documentation or the wiki example if you want to manually adjust the properties of the graph.
 * <a href="http://en.wikibooks.org/wiki/LaTeX/Linguistics#Dependency_Trees_using_TikZ-dependency">http://en.wikibooks.org/wiki/LaTeX/Linguistics#Dependency_Trees_using_TikZ-dependency</a>
 * 
 * <p>You can also specify the theme of the graph in the constructor. Try different values 0-6.
 */
public class TikzDependencyGraphWriter implements GraphWriter{
	private List<String> themes = Arrays.asList("default", "simple", "night", "brazil", "grassy", "iron", "copper");
	public static int THEME_DEFAULT = 0;
	public static int THEME_SIMPLE = 1;
	public static int THEME_NIGHT = 2;
	public static int THEME_BRAZIL = 3;
	public static int THEME_GRASSY = 4;
	public static int THEME_IRON = 5;
	public static int THEME_COPPER = 6;
	
	
	private FileWriter fileWriter;
	private int theme;
	private int counter;
	
	public TikzDependencyGraphWriter(int theme, String file){
		if(theme >= 0 && theme <= 6)
			this.theme = theme;
		else
			this.theme = 0;
		this.counter = 0;
		this.open(file);
	}
	
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
		this.fileWriter.writeln("\\begin{dependency}[theme = " + this.themes.get(this.theme) + "]");
		this.fileWriter.writeln("\\begin{deptext}[column sep=1em]");
		this.fileWriter.writeln(lemmaLine + "\\\\");
		this.fileWriter.writeln(posLine + "\\\\");
		this.fileWriter.writeln("\\end{deptext}");
		
		for(Edge edge : graph.getEdges()){
			this.fileWriter.writeln("\\depedge{" + (graph.getNodes().indexOf(edge.getHead())+1) + "}{" + (graph.getNodes().indexOf(edge.getDep())+1) + "}{" + escapeLatex(edge.getLabel()) + "}"
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
