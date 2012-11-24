package sem.graphvis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Schema;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.force.SpringForce;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import sem.exception.GraphFormatException;
import sem.graph.Edge;
import sem.graph.Graph;
import sem.graph.Node;
import sem.graphreader.CnCGraphReader;
import sem.graphreader.GraphReader;
import sem.graphreader.ParsevalGraphReader;
import sem.graphreader.RaspGraphReader;
import sem.graphreader.RaspXmlGraphReader;
import sem.graphreader.TSVGraphReader;
import sem.graphwriter.GraphWriter;
import sem.graphwriter.TSVGraphWriter;
import sem.util.Tools;

/**
 * An application for visualising the graphs. Take a look at sem.examples.VisualiseGraphs for sample code on how to run it.
 * 
 * <p>Navigate through sentences using Z and X, iterate over alternative parses (if available) using A and S.
 * 
 * <p>An experimental feature for editing the graphs is also included (has to be enabled when initialising the visualiser).
 * 
 * <p>The following commands are supported:
 * <ul>
 * <li><code>next / n</code> - next sentence
 * <li><code>prev / p</code> - previous sentence
 * <li><code>goto &lt;i&gt;</code> - jump to &lt;i&gt;th sentence (e.g., goto 100)
 * <li><code>add &lt;t&gt; &lt;i&gt; &lt;j&gt;</code> - add an edge labeled &lt;t&gt; from the &lt;i&gt;th node to the &lt;j&gt;th node
 * <li><code>del &lt;i&gt; &lt;j&gt;</code> - delete the edge from the &lt;i&gt;th node to the &lt;j&gt;th node
 * <li><code>save &lt;file&gt;</code> - save the graph in TSV format into &lt;file&gt;
 * </ul>
 */
public class GraphVisualiser {
	private boolean enableEditing;
	private ArrayList<ArrayList<Graph>> sentences;
	private int indexS, indexG; // sentence and graph pointer
	
	private Visualization visualization;
	public JTextField commandArea = null;
	private JEditorPane textArea;
	
	private prefuse.data.Graph prefuseGraph = null;
	
	/**
	 * Constructor.
	 * @param enableEditing	Set to true if you want to enable graph editing.
	 */
	public GraphVisualiser(boolean enableEditing){
		this.enableEditing = enableEditing;
	}
	
	/**
	 * Create the main user interface.
	 */
	private void createWindow(){
		// Creating the frame
		JFrame frame = new JFrame("GraphVisualiser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.WHITE);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
       
        // Creating the graph visualization area
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Graph", createGraphPanel());
        frame.add(tabbedPane);
        
        // Creating the text area
        textArea = new JEditorPane("text/html", "");
        textArea.setEditable(false);
        frame.add(textArea);
        
        // Creating the command area
		commandArea = new JTextField();
		commandArea.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		    	  if(parseCommand(commandArea.getText()))
		    		  commandArea.setText("");
		      }
		});
		if(this.enableEditing)
        	frame.add(commandArea);
        
        // Creating the menu bar
        JMenu menuItem = new JMenu("Navigate");
        menuItem.add(new GraphVisualiserMenuAction("Previous graph","A", this));
        menuItem.add(new GraphVisualiserMenuAction("Next graph","S", this));
        menuItem.add(new GraphVisualiserMenuAction("Previous sentence","Z", this));
        menuItem.add(new GraphVisualiserMenuAction("Next sentence","X", this));
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menuItem);
        frame.setJMenuBar(menuBar);
        
        // Showing the window
        frame.pack();
        frame.setVisible(true);
	}
	
	/**
	 * Set up graph-specific components of the UI.
	 * @return
	 */
	private JPanel createGraphPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);
		visualization = new Visualization();
		
		int[] palette = new int[] {
				 ColorLib.rgb(171,255,154)//, ColorLib.rgb(190,190,255)
		};
		
		// map nominal data values to colors using our provided palette
		DataColorAction fill = new DataColorAction("graph.nodes", "label", Constants.NOMINAL, VisualItem.FILLCOLOR ,palette);
		 
		//DataColorAction fill = new DataColorAction("graph.nodes", "label", Constants.NOMINAL, VisualItem.FILLCOLOR);
		//fill.add(VisualItem.FIXED, ColorLib.rgb(255,100,100));
		//fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));
		
		ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));
		ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));
		ColorAction arrows = new ColorAction("graph.edges", VisualItem.FILLCOLOR, ColorLib.gray(200));
	 	ActionList color = new ActionList();
		color.add(fill);
		color.add(text);
		color.add(edges);
		color.add(arrows);
		ActionList layout = new ActionList(Activity.INFINITY);
		
		ForceDirectedLayout fdl = new ForceDirectedLayout("graph",true);
		fdl.getForceSimulator().addForce(new SpringForce(2E-6f, 2000f));//2E-6f, 100f
		layout.add(fdl);
		layout.add(new RepaintAction());
		//layout.add(fill);
		layout.add(new GraphVisualiserEdgeLayout("edgeDeco"));
		layout.add(new RepaintAction());
		visualization.putAction("color", color);
		visualization.putAction("layout", layout);
		 
		panel.add(createDisplay(visualization), BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * Create the display.
	 * @param vis
	 * @return
	 */
	private Display createDisplay(Visualization vis)
	{
		Display d = new Display(vis);
        d.setSize(720, 500); // set display size
        d.addControlListener(new FocusControl(1));
        // drag individual items around
        d.addControlListener(new DragControl());
        // pan with left-click drag on background
        d.addControlListener(new PanControl()); 
        d.addControlListener(new WheelZoomControl());
        // zoom with right-click drag
        d.addControlListener(new ZoomControl());
        d.addControlListener(new ZoomToFitControl());        
        d.setForeground(Color.GRAY);
        d.setBackground(Color.WHITE);
        return d;
	}
	
	/**
	 * Analyse the user input and perform the appropriate action.
	 * @param command	User command.
	 * @return	True if a command was executed, false otherwise.
	 */
	private boolean parseCommand(String command){
		command = command.trim();
		if(command.equals("next") || command.equals("n")){
			this.nextSentence();
			return true;
		}
		else if(command.equals("prev") || command.equals("p")){
			this.prevSentence();
			return true;
		}
		else if(command.startsWith("goto ")){
			String[] attributes = command.split("\\s+");
			if(attributes.length == 2){
				int sentenceId = Tools.getInt(attributes[1], -1);
				if(sentenceId >= 0 && sentenceId < this.sentences.size()){
					this.indexS = sentenceId;
					this.indexG = 0;
					this.resetGraph();
					return true;
				}
			}
		}
		else if(command.startsWith("add ")){
			String[] attrbutes = command.split("\\s+");
			if(attrbutes.length == 4){
				int headId = Tools.getInt(attrbutes[2], -1);
				int depId = Tools.getInt(attrbutes[3], -1);
				if(headId >= 0 && depId >= 0 && headId < this.getCurrentGraph().getNodes().size() && depId < this.getCurrentGraph().getNodes().size()){
					this.getCurrentGraph().addEdge(attrbutes[1], this.getCurrentGraph().getNodes().get(headId), this.getCurrentGraph().getNodes().get(depId));
					int edgeId = prefuseGraph.addEdge(headId, depId);
		    		prefuseGraph.getEdge(edgeId).setString("label", attrbutes[1]);
		    		visualization.run("color");
		    		return true;
				}
			}
		}
		else if(command.startsWith("del ")){
			String[] commandBits = command.split("\\s+");
			if(commandBits.length == 3){
				int headId = Tools.getInt(commandBits[1], -1);
				int depId = Tools.getInt(commandBits[2], -1);
				if(headId >= 0 && depId >= 0 && headId < this.getCurrentGraph().getNodes().size() && depId < this.getCurrentGraph().getNodes().size()){
					Node headNode = this.getCurrentGraph().getNodes().get(headId);
					Node depNode = this.getCurrentGraph().getNodes().get(depId);
					ArrayList<Edge> removeEdges = new ArrayList<Edge>();
					for(Edge edge : this.getCurrentGraph().getEdges()){
						if(edge.getHead() == headNode && edge.getDep() == depNode){
							removeEdges.add(edge);
						}
					}
					this.getCurrentGraph().getEdges().removeAll(removeEdges);
					
					int edgeId = this.prefuseGraph.getEdge(headId, depId);
					this.prefuseGraph.removeEdge(edgeId);
					visualization.run("color");
					return true;
				}
			}
		}
		else if(command.startsWith("save ")){
			String[] commandBits = command.split("\\s+");
			if(commandBits.length == 2){
				GraphWriter writer = new TSVGraphWriter(commandBits[1]);
				for(ArrayList<Graph> sentence : this.sentences)
					writer.write(sentence);
				writer.close();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Show the next sentence.
	 */
	public void nextSentence(){
		indexS++;
		if(indexS >= sentences.size())
			indexS = 0;
		indexG = 0;
		this.resetGraph();
	}
	
	/**
	 * Show the previous sentence.
	 */
	public void prevSentence(){
		indexS--;
		if(indexS < 0)
				indexS = sentences.size()-1;
		indexG = 0;
		this.resetGraph();
	}
	
	/**
	 * Show the next graph.
	 */
	public void nextGraph(){
		indexG++;
		if(indexG >= sentences.get(indexS).size())
			indexG = 0;
		this.resetGraph();
	}
	
	/**
	 * Show the previous graph.
	 */
	public void prevGraph(){
		indexG--;
		if(indexG < 0)
			indexG = sentences.get(indexS).size()-1;
		this.resetGraph();
	}

	/**
	 * Get the currently active graph.
	 * @return	The current graph.
	 */
	public Graph getCurrentGraph(){
		return this.sentences.get(indexS).get(indexG);
	}
	
	/**
	 * Convert a normal graph into a prefuse graph.
	 * @param graph	Input graph.
	 * @return	Prefuse graph.
	 */
	private prefuse.data.Graph createPrefuseGraph(Graph graph)
	{
		prefuse.data.Graph prefuseGraph = new prefuse.data.Graph(true);
		prefuseGraph.getNodeTable().addColumn("label", String.class);
		prefuseGraph.getEdgeTable().addColumn("label", String.class);
		
    	for(Node node : graph.getNodes()){
    		prefuse.data.Node prefuseNode = prefuseGraph.addNode();
    		prefuseNode.setString("label", node.getLabel()+":"+graph.getNodes().indexOf(node));
    	}
    	
    	for(Edge edge : graph.getEdges()){
    		int edgeId = prefuseGraph.addEdge(this.getCurrentGraph().getNodes().indexOf(edge.getHead()), this.getCurrentGraph().getNodes().indexOf(edge.getDep()));
    		prefuseGraph.getEdge(edgeId).setString("label", edge.getLabel());
    	}
    	return prefuseGraph;
	}
	
	/**
	 * Get the text representation of the sentence (shown below the graph).
	 * @param graph
	 * @return
	 */
	private String getText(Graph graph){
		String text = "";
    	/*if(graph.hasMetadata()){
    		for(String metadata : graph.getMetadata().values()){
	    		for(String l : metadata.trim().split("\n"))
	    			text +=l+"<br>";
    		}
    		if(text.length() > 4)
    			text = text.substring(0, text.length()-4);
    	}
    	else */
    	{
    		for(Node node : graph.getNodes()){
    			text += node.getLemma() + " ";
    		}
    		if(text.length() > 0)
    			text = text.substring(0, text.length()-1);
    	}
    	
    	if(text.trim().length() > 0)
    		text ="<html>" + indexS + "." + indexG + " : " +  text.trim() + "</html>";
    	else
    		text = "<html>" + indexS + "." + indexG + "</html>";
    	
    	return text;
	}

	/**
	 * Reset and redraw the graph. Should be run after loading a different graph.
	 */
	public void resetGraph(){
		String text = getText(this.getCurrentGraph());
    	this.textArea.setText(text);
    	
    	prefuseGraph = this.createPrefuseGraph(this.getCurrentGraph());

		// add the graph to the visualization as the data group "graph"
		// nodes and edges are accessible as "graph.nodes" and "graph.edges"
		visualization.removeGroup("edgeDeco");
		visualization.removeGroup("graph");
		visualization.add("graph", prefuseGraph);
		//vis.setInteractive("graph.edges", null, false);

		LabelRenderer r = new LabelRenderer("label");
		
		r.setRoundedCorner(8, 8); // round the corners
		EdgeRenderer er = new EdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_FORWARD);
		DefaultRendererFactory drf = new DefaultRendererFactory(r, er);
		drf.add(new InGroupPredicate("edgeDeco"), new LabelRenderer("label"));
		visualization.setRendererFactory(drf);

		//LabelRenderer r2 = new LabelRenderer("name");
		//vis_tree.setRendererFactory(new DefaultRendererFactory(r2));
        
        LabelRenderer m_nodeRenderer = new LabelRenderer("label");
        m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
        m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
        m_nodeRenderer.setRoundedCorner(8,8);
        
        //EdgeRenderer m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);
        //DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer, m_edgeRenderer);
        //rf.add(new InGroupPredicate("tree.edges"), m_edgeRenderer);

        Schema EDGE_DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();
    	EDGE_DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false); //noninteractive 
    	EDGE_DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.rgba(100,100,100,200)); 
    	EDGE_DECORATOR_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma",11)); // and not too big

    	visualization.addDecorators("edgeDeco", "graph.edges", EDGE_DECORATOR_SCHEMA);
    	
    	visualization.run("draw");
    	visualization.run("color");
    	visualization.run("layout");
        
        //vis_tree.run("layout");
        //viz_tree.run("layout");
        //vis_tree.run("repaint");
        
       /* 
        vis_tree.run("fullPaint");
        vis_tree.run("repaint");
        vis_tree.run("layout");
        //vis_tree.run("draw");
        vis_tree.run("color");
        //vis_tree.run("filter");
        //vis_tree.run("fullPaint");
       */
	}
	
	/**
	 * Display a single graph.
	 * @param graph
	 */
	public void displayGraph(Graph graph){
		if(graph == null)
			throw new RuntimeException("No input");
		ArrayList<Graph> graphs = new ArrayList<Graph>();
		graphs.add(graph);
		this.displayGraphs(graphs);
	}
	
	/**
	 * Display a list of graphs. 
	 * @param graphs
	 */
	public void displayGraphs(ArrayList<Graph> graphs){
		if(graphs == null || graphs.size() == 0)
			throw new RuntimeException("No input");
		ArrayList<ArrayList<Graph>> sentences = new ArrayList<ArrayList<Graph>>();
		sentences.add(graphs);
		this.displaySentences(sentences);
	}
	
	/**
	 * Display a list of sentences (a list of lists of graphs). Useful for iterating over different sentences with alternative parses.
	 * @param sentences
	 */
	public void displaySentences(ArrayList<ArrayList<Graph>> sentences){
		if(sentences == null || sentences.size() == 0)
			throw new RuntimeException("No input");
		this.sentences = sentences;
		this.createWindow();
		this.resetGraph();
	}
	
	/**
	 * This is a redundant function. Do not use. For reference only.
	 */
	public static void main(String[] args){
		try {
			GraphReader reader = null;
			if(args.length == 2){
				if(args[0].equalsIgnoreCase("rasp"))
					reader = new RaspGraphReader(args[1], true);
				else if(args[0].equalsIgnoreCase("raspxml"))
					reader = new RaspXmlGraphReader(args[1], RaspXmlGraphReader.NODES_TOKENS, true, false);
				else if(args[0].equalsIgnoreCase("cnc"))
					reader = new CnCGraphReader(args[1]);
				else if(args[0].equalsIgnoreCase("parseval"))
					reader = new ParsevalGraphReader(args[1], false, false);
				else if(args[0].equalsIgnoreCase("tsv"))
					reader = new TSVGraphReader(args[1], true);
			}
			if(reader == null){
				System.out.println("Usage: GraphVisualiser <inputtype> <inputpath>");
				System.exit(1);
			}
			
			ArrayList<ArrayList<Graph>> sentences = new ArrayList<ArrayList<Graph>>();
			while(reader.hasNext())
				sentences.add(reader.nextSentence());
			reader.close();
			GraphVisualiser visualiser = new GraphVisualiser(false);
			visualiser.displaySentences(sentences);
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
}
