package sem.graphreader;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
 *
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
	
	/**
	 * Create a new reader for RASP XML.
	 * @param inputPath		Path to the file or directory.
	 * @param getAllParses	Whether to include alternative parses for each sentence (if available).
	 * @param getMetaData	Whether to read metadata (sentence id and weighted grs).
	 * @throws GraphFormatException 
	 */
	public RaspXmlGraphReader(String inputPath, boolean getAllParses, boolean getMetaData) throws GraphFormatException{
		this.getAllParses = getAllParses;
		this.getMetaData = getMetaData;
		this.nextSentence = null;
		this.nextGraphPointer = 0;
		this.xmlReader = new XmlReader(inputPath);
		this.domPath = new ArrayList<String>();
		this.reset();
	}
	
	/**
	 * Select the list of nodes to be added to the graph.
	 * We want every word to have a lemma represented in the graph. If a lemma is used in the GRs, that's the one we include. Otherwise, take the first on from the list of nodes.
	 * Also have to remember that we want to add clones of the lemma nodes, to allow for independent editing.
	 * @param lemmas	
	 * @param wordIds
	 * @param nodeMap
	 * @return
	 */
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
		HashMap<Node,Integer> wordIds = new HashMap<Node,Integer>();
		ArrayList<Edge> tempEdges = new ArrayList<Edge>();
		
		int lemmaId, wordId, grHeadId, grDepId;
		String tag, lemma, pos, lemmaNum, wordNum, grType, grHead, grDep, grWeight, grWeightsPart = "", grWeights = "";
		Node headNode, depNode;
		
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
						throw new GraphFormatException("Duplicate index values for lemmas.", "" + lemmaId);
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
				
			}
			else if(xmlReader.isEndElement()){
				tag = xmlReader.getLocalName();
				domPath.remove(domPath.size()-1);
				
				if(tag.equals("gr-list")){
					Graph graph = new Graph();
					graphs.add(graph);
					// Adding edges to the graph
					// Making clones of all the nodes so that they can be operated on independently.
					HashMap<Node, Node> nodeMap = new HashMap<Node, Node>();
					for(Edge edge : tempEdges){
						if(!nodeMap.containsKey(edge.getHead()))
							nodeMap.put(edge.getHead(), edge.getHead().clone());
						if(!nodeMap.containsKey(edge.getDep()))
							nodeMap.put(edge.getDep(), edge.getDep().clone());
						graph.addEdge(edge.getLabel(), nodeMap.get(edge.getHead()), nodeMap.get(edge.getDep()));
					}
					
					for(Node node : selectNodes(lemmas, wordIds, nodeMap))
						graph.addNode(node);
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
			}
		}

		if(graphs != null && graphs.size() == 0){
			Graph graph = new Graph();
			graphs.add(graph);
			for(Node node : selectNodes(lemmas, wordIds, null))
				graph.addNode(node);
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
