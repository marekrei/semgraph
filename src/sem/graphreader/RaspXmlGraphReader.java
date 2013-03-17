package sem.graphreader;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import sem.exception.GraphFormatException;
import sem.graph.Edge;
import sem.graph.Graph;
import sem.graph.Node;
import sem.util.Tools;
import sem.util.XmlReader;

/**
 * Graph reader for the RASP XML format.
 * 
 * <p>RASP toolkit: <a href="http://ilexir.co.uk/2011/open-source-rasp-release/">http://ilexir.co.uk/2011/open-source-rasp-release/</a>
 * 
 * <p>It supports multiple tags, multiple parses, and reads weighted GR info into the metadata field.
 * It can also directly read the file in a GZIP format.
 * 
 * <p>The small example files were parsed using the following command:
 * <code>./rasp.sh -m -p'-mgi -pr -n10' < input.txt > output.xml</code>
 * 
 * <p>The big example file was parsed with this command and then gzipped, to save time and space:
 * <code>./rasp.sh -m -p'-mg -pr' < input.txt > output.xml</code>
 * 
 * <p>There are two cases where the output from RASP does not exactly correspond to a graph. 
 * First, the passive property for verbs is represented as an edge where the verb is the head but the dependent does not exist.
 * This is solved by adding a new 'null' node to the graph that acts as a dependent for that edge.
 * Second, the head of a relation can be marked as an ellipsis, which doesn't correspond to any lemma in the sentence. 
 * In such a case, a new 'ellip' node is added to the graph that acts as the head.
 * If these nodes/edges are not needed, they should be removed in post-processing.
 * 
 * <p>There are also two possible ways of selecting nodes for the node list of each graph:
 * <ul>
 * <li>NODES_ALL - Include all lemmas that RASP outputs for a given sentence. If the option of multiple POS tags is activated, this can result in multiple nodes that correspond to a single token.
 * <li>NODES_TOKENS - If a lemma is used in the edges of a graph, that's the one we include. Otherwise, if a token does not have any lemmas participating in the edges, add the first lemma of this token to the list of nodes. This should result in matching numbers of nodes and tokens.
 * </ul>
 * If the multiple tags option is not activated in RASP, both of these modes should give the same output.
 */
public class RaspXmlGraphReader implements GraphReader{
	private boolean getAllParses;
	private boolean getMetaData;
	private XmlReader xmlReader;
	private ArrayList<Graph> nextSentence;
	private int nextGraphPointer;
	private int sentenceCount;
	private String ellipLemma = "ellip";
	private ArrayList<String> domPath;
	private int nodeSelectionMode;
	private String part;
	
	/**
	 * The nodes list will contain all the lemmas given by RASP, including the cases where two lemmas correspond to the same token.
	 */
	public static final int NODES_ALL = 0;
	
	/**
	 * The system tries to construct the most likely set of lemmas, given the sentence and the graph. A node is included if it is used in one of the edges. Also, if a word is not used in the edges at all, its first lemma is included. This way the number of tokens in the sentence and the number of nodes in the graph should match.
	 */
	public static final int NODES_TOKENS = 1;
	
	/**
	 * Create a new reader for RASP XML.
	 * @param inputPath		Path to the file or directory.
	 * @param nodeSelectionMode		Set the way that nodes are added to the list of nodes in the graph. NODES_ALL includes all lemmas that RASP outputs. NODES_TOKENS includes one lemma for each token in the sentence.
	 * @param getAllParses	Whether to include alternative parses for each sentence (if available).
	 * @param getMetaData	Whether to read metadata (sentence id and weighted grs).
	 * @throws GraphFormatException 
	 */
	public RaspXmlGraphReader(String inputPath, int nodeSelectionMode, boolean getAllParses, boolean getMetaData) throws GraphFormatException{
		this.getAllParses = getAllParses;
		this.getMetaData = getMetaData;
		this.nextSentence = null;
		this.nextGraphPointer = 0;
		this.xmlReader = new XmlReader(inputPath);
		this.domPath = new ArrayList<String>();
		this.nodeSelectionMode = nodeSelectionMode;
		this.part = null;
		this.reset();
	}
	
	/**
	 * Select the list of nodes to be added to the graph, based on the specified node selection mode.
	 * We want every word to have a lemma represented in the graph. 
	 * Also have to remember that we want to add clones of the lemma nodes, so that different Graph object do not share the same Node objects. This allows for independent editing of the graphs.
	 * @param nodeSelectionMode
	 * @param lemmas	
	 * @param wordIds
	 * @param nodeMap
	 * @return
	 */
	private ArrayList<Node> selectNodes(int nodeSelectionMode, LinkedHashMap<Integer,Node> lemmas, LinkedHashMap<Node,Integer> wordIds, LinkedHashMap<Node, Node> nodeMap){
		ArrayList<Node> selectedNodes = new ArrayList<Node>();
		
		if(nodeSelectionMode == RaspXmlGraphReader.NODES_ALL){
			for(Node lemma : lemmas.values()){
				if(nodeMap != null && nodeMap.containsKey(lemma))
					selectedNodes.add(nodeMap.get(lemma));
				else
					selectedNodes.add(lemma.clone());
			}
		}
		else if(nodeSelectionMode == RaspXmlGraphReader.NODES_TOKENS){
			LinkedHashMap<Integer,ArrayList<Node>> wordMap = new LinkedHashMap<Integer,ArrayList<Node>>();
			int maxWordNum = -1;
			for(Entry<Node,Integer> e : wordIds.entrySet()){
				if(!wordMap.containsKey(e.getValue()))
					wordMap.put(e.getValue(), new ArrayList<Node>());
				wordMap.get(e.getValue()).add(e.getKey());
				if(e.getValue() > maxWordNum)
					maxWordNum = e.getValue();
			}
			
			for(int i = 0; i <= maxWordNum; i++){
				if(!wordMap.containsKey(i))
					continue;
				Node chosenNode = null;
				for(Node node : wordMap.get(i)){
					if(nodeMap != null && nodeMap.containsKey(node)){
						if(chosenNode == null)
							chosenNode = nodeMap.get(node);
						else
							throw new RuntimeException("GRs are pointing to multiple lemmas of the same word.");
					}
				}
				if(chosenNode == null)
					chosenNode = wordMap.get(i).get(0);
				selectedNodes.add(chosenNode);
			}
		}
		// Now we add all nodes that are in the graph but were not in the list of lemmas (ellip and nil).
		if(nodeMap != null){
			for(Node node : nodeMap.values()){
				if(!selectedNodes.contains(node))
					selectedNodes.add(node);
			}
		}
		return selectedNodes;
	}
	/*
	private ArrayList<Node> selectNodes(LinkedHashMap<Integer,Node> lemmas, HashMap<Node,Integer> wordIds, HashMap<Node, Node> nodeMap){
		ArrayList<Node> selectedNodes = new ArrayList<Node>();
		
		int first, index = 0;
		boolean foundMatch;
		while(index < lemmas.size()){
			first = index;
			foundMatch = false;
			do{
				if(nodeMap != null && nodeMap.containsKey(lemmas.get(index))){
					if(foundMatch == false){
						foundMatch = true;
						selectedNodes.add(nodeMap.get(lemmas.get(index)));
					}
					else 
						throw new RuntimeException("GRs are pointing to multiple lemmas of the same word.");
				}
				index++;
			} while(index < lemmas.size() && wordIds.get(lemmas.get(first)).equals(wordIds.get(lemmas.get(index))));

			if(!foundMatch){
				selectedNodes.add(lemmas.get(first).clone());
			}
		}

		// Now we add all nodes that are in the graph but were not in the list of lemmas (ellip and nil).
		if(nodeMap != null){
			for(Node node : nodeMap.values()){
				if(!selectedNodes.contains(node))
					selectedNodes.add(node);
			}
		}
		
		return selectedNodes;
	}
	*/
	/**
	 * Read the next sentence of graphs from the corpus.
	 * If the sentence contains no valid graphs, return a list with one graph containing no edges.
	 * If there are no more sentences, return null.
	 * @return	The list of graphs corresponding to the next sentence.
	 * @throws GraphFormatException 
	 */
	private ArrayList<Graph> readSentence() throws GraphFormatException{
		ArrayList<Graph> graphs = null;
		
		
		LinkedHashMap<Integer,Node> lemmas = new LinkedHashMap<Integer,Node>();
		LinkedHashMap<Node,Integer> wordIds = new LinkedHashMap<Node,Integer>();
		ArrayList<Edge> tempEdges = new ArrayList<Edge>();
		
		int lemmaId, wordId, grHeadId, grDepId;
		String tag, lemma, pos, lemmaNum, wordNum, grType, grHead, grDep, grWeight, grWeightsPart = "", grWeights = "";
		Node headNode, depNode;
		boolean xparse = false;
		
		while (xmlReader.hasNext()) {
			xmlReader.next();
			
			if(xmlReader.isStartElement()){
				tag = xmlReader.getLocalName();
				domPath.add(tag);
				
				if(tag.equals("sentence")){
					graphs = new ArrayList<Graph>();
					sentenceCount++;
					grWeightsPart = "";
					grWeights = "";
				}
				
				else if(tag.equals("lemma") && domPath.contains("lemma-list")){
					lemma = xmlReader.getAttributeValue(null, "lem");
					pos = xmlReader.getAttributeValue(null, "pos");
					lemmaNum = xmlReader.getAttributeValue(null, "num");
					wordNum = xmlReader.getAttributeValue(null, "wnum");
					
					if(lemma == null || pos == null || lemmaNum == null || wordNum == null)
						throw new RuntimeException("One of the lemma attributes is null. This is not allowed.");
					
					lemmaId = Tools.getInt(lemmaNum, -1)-1;
					wordId = Tools.getInt(wordNum, -1)-1;
					
					if(lemmaId < 0)
						throw new GraphFormatException("Forbidden value for lemma number.", lemmaNum);
					if(wordId < 0)
						throw new GraphFormatException("Forbidden value for word number. ", wordNum);
					
					if(lemmas.containsKey(lemmaId))
						throw new GraphFormatException("Duplicate index values for lemmas.", "" + lemma + ":" + lemmaNum + "_" + pos);
					Node node = new Node(lemma, pos);
					lemmas.put(new Integer(lemmaId), node);
					wordIds.put(node, new Integer(wordId));
				}
				
				else if(tag.equals("gr-list")){
					tempEdges.clear();
				}
				
				else if(tag.equals("gr")){
					grType = xmlReader.getAttributeValue(null, "type");
					grHead = xmlReader.getAttributeValue(null, "head");
					grDep = xmlReader.getAttributeValue(null, "dep");
					grHeadId = Tools.getInt(grHead, -1)-1;
					grDepId = Tools.getInt(grDep, -1)-1;
					
					// Resolving the head node
					headNode = null;
					if(grHead.equals(ellipLemma)){
						headNode = Graph.ellip.clone();
					}
					else if(grHeadId < 0){
						if(grHead.equals(";")) //This is a known RASP output bug. Skipping this gr.
							continue;
						else
							throw new GraphFormatException("Forbidden value for head in GR.", grHead);
					}
					else if(!lemmas.containsKey(grHeadId)){
						throw new GraphFormatException("GR head id is not present in the lemmas.", "" + grHeadId);
					}
					else
						headNode = lemmas.get(grHeadId);
					
					// Resolving the dep node
					depNode = null;
					if(grDep == null){
						depNode = Graph.nil.clone();
					}
					else if(grDepId < 0){
						// These are needed exceptions. They are bugs in RASP output. These GRs will be skipped.
						if(grDep.equals("to") || grDep.equals("0")) 
							continue;
						else
							throw new GraphFormatException("Forbidden value for dep in GR.", grDep);
					}
					else if(!lemmas.containsKey(grDepId)){
						throw new GraphFormatException("GR dep id is not present in the lemmas.", "" + grDepId);
					}
					else
						depNode = lemmas.get(grDepId);
					
					if(headNode == null || depNode == null)
						throw new GraphFormatException("Head or dep is null in readSentence()");
					
					if(domPath.contains("gr-list")){
						tempEdges.add(new Edge(grType, headNode, depNode));
					}
					else if(domPath.contains("weighted") || domPath.contains("ewg-weighted")){
						grWeightsPart = grType + "\t" + headNode.getLabel() + "\t" + depNode.getLabel();
					}
				}
				
				else if(tag.equals("gr-weight") && (domPath.contains("weighted") || domPath.contains("ewg-weighted"))){
					if(grWeightsPart.length() > 0){
						grWeight = xmlReader.getAttributeValue(null, "weight");
						grWeights += grWeightsPart + "\t" + grWeight + "\n";
						grWeightsPart = "";
					}
				}
				else if(tag.equals("part")){
					part = xmlReader.getAttributeValue(null, "n");
				}
				
				else if(tag.equals("xparse"))
					xparse = true;
				
			}
			else if(xmlReader.isEndElement()){
				tag = xmlReader.getLocalName();
				domPath.remove(domPath.size()-1);
				
				if(tag.equals("gr-list") || tag.equals("xparse")){
					Graph graph = new Graph();
					graphs.add(graph);
					// Adding edges to the graph
					// Making clones of all the nodes so that they can be operated on independently.
					LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node>();
					for(Edge edge : tempEdges){
						if(!nodeMap.containsKey(edge.getHead()))
							nodeMap.put(edge.getHead(), edge.getHead().clone());
						if(!nodeMap.containsKey(edge.getDep()))
							nodeMap.put(edge.getDep(), edge.getDep().clone());
						graph.addEdge(edge.getLabel(), nodeMap.get(edge.getHead()), nodeMap.get(edge.getDep()));
					}
					
					graph.getNodes().addAll(selectNodes(this.nodeSelectionMode, lemmas, wordIds, nodeMap));
					tempEdges.clear();
				}
				
				else if(tag.equals("sentence")){
					break;
				}
			}
		}
		
		if(this.getMetaData && graphs != null){
			for(Graph g : graphs){
				g.putMetadata("sentenceId", ""+sentenceCount);
				g.putMetadata("weightedGrs", grWeights.trim());
				g.putMetadata("xparse", xparse?"true":"false");
				g.putMetadata("part", part);
			}
		}

		if(graphs != null && graphs.size() == 0){
			Graph graph = new Graph();
			graphs.add(graph);

			graph.getNodes().addAll(selectNodes(this.nodeSelectionMode, lemmas, wordIds, null));
		}
		
		return graphs;
	}

	/**
	 * Get the next graph from the corpus.
	 * @return	The next graph.
	 * @throws GraphFormatException 
	 */
	@Override
	public Graph next() throws GraphFormatException {
		Graph graph = null;
		if(this.nextSentence != null){
			graph = this.nextSentence.get(nextGraphPointer);
			nextGraphPointer++;
		}
		if(this.nextSentence == null || !getAllParses || nextGraphPointer >= this.nextSentence.size()){
			this.nextSentence = readSentence();
			nextGraphPointer = 0;
		}
		return graph;
	}

	/**
	 * Check whether there are more graphs available.
	 * @return	True if there are more graphs available.
	 */
	@Override
	public boolean hasNext() {
		if(this.nextSentence == null)
			return false;
		return true;
	}

	/**
	 * Reset the whole reading process to the beginning.
	 * @throws GraphFormatException 
	 */
	@Override
	public void reset() throws GraphFormatException {
		if(xmlReader != null)
			xmlReader.reset();
		this.nextSentence = null;
		this.nextGraphPointer = 0;
		this.sentenceCount = 0;
		this.domPath.clear();
		this.next();
	}
	
	/**
	 * Close the reader.
	 */
	public void close(){
		this.xmlReader.close();
		this.nextSentence = null;
		this.sentenceCount = 0;
		this.nextGraphPointer = 0;
	}
	
	/**
	 * Read a sentence from the corpus. 
	 * This returns a list of graphs if getAllParses is set to true.
	 * @return	List of graphs
	 * @throws GraphFormatException 
	 */
	public ArrayList<Graph> nextSentence() throws GraphFormatException{
		ArrayList<Graph> tempSentence;
		if(getAllParses)
			tempSentence = this.nextSentence;
		else{
			tempSentence = new ArrayList<Graph>();
			tempSentence.add(this.nextSentence.get(0));
		}
		this.nextSentence = readSentence();
		return tempSentence;
	}
}
