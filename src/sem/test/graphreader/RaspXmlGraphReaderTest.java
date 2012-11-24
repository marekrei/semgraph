package sem.test.graphreader;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import sem.exception.GraphFormatException;
import sem.graph.Edge;
import sem.graph.Graph;
import sem.graph.Node;
import sem.graphreader.GraphReader;
import sem.graphreader.RaspXmlGraphReader;

public class RaspXmlGraphReaderTest {

	private String dir = "examples/raspxml/";
	private String smallFile = this.dir + "file1.xml";
	private String largeFile = this.dir + "pnp_1000.xml.gz";
	
	public static void testNodes(GraphReader reader) throws GraphFormatException{
		// Let's take the second graph, to be sure that there is nothing carried over from the first one.
		reader.next();
		Graph graph = reader.next();
		
		assertTrue(graph.getNodes().size() == 12);
		
		ArrayList<String> checkNodes = new ArrayList<String>(Arrays.asList("Natural_JJ", 
																			"language_NN1", 
																			"processing_NN1", 
																			"be_VBZ", 
																			"a_AT1", 
																			"field_NN1", 
																			"of_IO", 
																			"computer_NN1", 
																			"science_NN1", 
																			"and_CC", 
																			"linguistics_NN1", 
																			"._."));
		for(int i = 0; i < checkNodes.size(); i++){
			String nodeLabel = graph.getNodes().get(i).getLemma() + "_" + graph.getNodes().get(i).getPos();
			assertTrue(nodeLabel.equals(checkNodes.get(i)));
		}
		
		assertTrue(graph.getEdges().size() == 10);
		
		ArrayList<String> checkEdges = new ArrayList<String>(Arrays.asList("ncsubj	be_VBZ	processing_NN1",
																			"xcomp	be_VBZ	field_NN1",
																			"det	field_NN1	a_AT1",
																			"iobj	field_NN1	of_IO",
																			"dobj	of_IO	and_CC",
																			"ncmod	and_CC	computer_NN1",
																			"conj	and_CC	science_NN1",
																			"conj	and_CC	linguistics_NN1",
																			"ncmod	processing_NN1	Natural_JJ",
																			"ncmod	processing_NN1	language_NN1"));
		
		for(int i = 0; i < checkEdges.size(); i++){
			String headLabel = graph.getEdges().get(i).getHead().getLemma() + "_" + graph.getEdges().get(i).getHead().getPos();
			String depLabel = graph.getEdges().get(i).getDep().getLemma() + "_" + graph.getEdges().get(i).getDep().getPos();
			String edgeLabel = graph.getEdges().get(i).getLabel() + "\t" + headLabel + "\t" + depLabel;
			assertTrue(edgeLabel.equals(checkEdges.get(i)));
		}
	}
	
	@Test
	public void testNodes() {
		try {
			RaspXmlGraphReader reader = new RaspXmlGraphReader(smallFile, RaspXmlGraphReader.NODES_TOKENS, false, false);
			testNodes(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	public static void testEdges(GraphReader reader) throws GraphFormatException{
		// Let's take the second graph, to be sure that there is nothing carried over from the first one.
		reader.next();
		Graph graph = reader.next();
		
		assertTrue(graph.getEdges().size() == 10);
		
		ArrayList<String> checkEdges = new ArrayList<String>(Arrays.asList("ncsubj	be_VBZ	processing_NN1",
																			"xcomp	be_VBZ	field_NN1",
																			"det	field_NN1	a_AT1",
																			"iobj	field_NN1	of_IO",
																			"dobj	of_IO	and_CC",
																			"ncmod	and_CC	computer_NN1",
																			"conj	and_CC	science_NN1",
																			"conj	and_CC	linguistics_NN1",
																			"ncmod	processing_NN1	Natural_JJ",
																			"ncmod	processing_NN1	language_NN1"));
		
		for(int i = 0; i < checkEdges.size(); i++){
			String headLabel = graph.getEdges().get(i).getHead().getLemma() + "_" + graph.getEdges().get(i).getHead().getPos();
			String depLabel = graph.getEdges().get(i).getDep().getLemma() + "_" + graph.getEdges().get(i).getDep().getPos();
			String edgeLabel = graph.getEdges().get(i).getLabel() + "\t" + headLabel + "\t" + depLabel;
			assertTrue(edgeLabel.equals(checkEdges.get(i)));
		}
	}

	@Test
	public void testEdges() {
		try {
			RaspXmlGraphReader reader = new RaspXmlGraphReader(smallFile, RaspXmlGraphReader.NODES_TOKENS, false, false);
			testEdges(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	public static void testMetadata(GraphReader reader) throws GraphFormatException{
		reader.next();
		Graph graph = reader.next();

		assertTrue(graph.getMetadata("sentenceId").equals("2"));
		
		String[] weightLines = graph.getMetadata("weightedGrs").split("\\n");
		assertTrue(weightLines.length == 17);
		
		ArrayList<String> checkWeights = new ArrayList<String>(Arrays.asList("conj	and_CC	computer_NN1	0.005961",
																				"ncmod	and_CC	computer_NN1	0.495827",
																				"ncmod	science_NN1	computer_NN1	0.498209",
																				"dobj	of_IO	computer_NN1	0.000003",
																				"dobj	of_IO	and_CC	0.964730",
																				"dobj	of_IO	science_NN1	0.035267",
																				"iobj	field_NN1	of_IO	1.0",
																				"ncmod	processing_NN1	language_NN1	1.0",
																				"conj	and_CC	science_NN1	0.964733",
																				"det	and_CC	a_AT1	0.035270",
																				"det	field_NN1	a_AT1	0.964730",
																				"conj	and_CC	linguistics_NN1	1.0",
																				"ncmod	processing_NN1	Natural_JJ	1.0",
																				"xcomp	be_VBZ	field_NN1	0.964730",
																				"conj	and_CC	field_NN1	0.035270",
																				"xcomp	be_VBZ	and_CC	0.035270",
																				"ncsubj	be_VBZ	processing_NN1	1.0"));
		
		for(int i = 0; i < checkWeights.size(); i++){
			assertTrue(weightLines[i].equals(checkWeights.get(i)));
		}
	}
	
	@Test
	public void testMetadata(){
		try {
			RaspXmlGraphReader reader = new RaspXmlGraphReader(smallFile, RaspXmlGraphReader.NODES_TOKENS, false, true);
			testMetadata(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	public static void testMultipleParses(GraphReader reader) throws GraphFormatException{
		int sentenceCount = 0, graphCount = 0, edgeCount = 0;
		HashSet<String> cache = new HashSet<String>();
		
		// First, reading by sentences.
		while(reader.hasNext()){
			ArrayList<Graph> sentence = reader.nextSentence();
			for(Graph graph : sentence){
				edgeCount += graph.getEdges().size();
				cache.add(graph.toString());
				
				for(Node node : graph.getNodes()){
					assertTrue(node.getLemma() != null && node.getLemma().length() > 0);
					assertTrue(node.getPos() != null && node.getPos().length() > 0);
				}
				
				for(Edge edge : graph.getEdges()){
					assertTrue(edge.getLabel() != null && edge.getLabel().length() > 0);
					assertTrue(edge.getHead() != null && graph.getNodes().contains(edge.getHead()));
					assertTrue(edge.getDep() != null && graph.getNodes().contains(edge.getDep()));
				}
				graphCount++;
			}
			sentenceCount++;
		}
		
		assertTrue(sentenceCount == 3);
		assertTrue(graphCount == 29);
		assertTrue(edgeCount == 453);
		assertTrue(cache.size() == 26); // Total number of unique graphs in the set
		
		
		// Then, reading by graphs.
		reader.reset();
		edgeCount = 0;
		graphCount = 0;
		HashSet<String> cache2 = new HashSet<String>();
		while(reader.hasNext()){
			Graph graph = reader.next();
			
			edgeCount += graph.getEdges().size();
			cache2.add(graph.toString());
			
			for(Node node : graph.getNodes()){
				assertTrue(node.getLemma() != null && node.getLemma().length() > 0);
				assertTrue(node.getPos() != null && node.getPos().length() > 0);
			}
			
			for(Edge edge : graph.getEdges()){
				assertTrue(edge.getLabel() != null && edge.getLabel().length() > 0);
				assertTrue(edge.getHead() != null && graph.getNodes().contains(edge.getHead()));
				assertTrue(edge.getDep() != null && graph.getNodes().contains(edge.getDep()));
			}
			graphCount++;
		}

		assertTrue(graphCount == 29);
		assertTrue(edgeCount == 453);
		assertTrue(cache2.size() == 26);
		
		for(String graphString : cache2){
			assertTrue(cache.contains(graphString));
		}
	}
	
	@Test
	public void testMultipleParses(){
		try{
			RaspXmlGraphReader reader = new RaspXmlGraphReader(smallFile, RaspXmlGraphReader.NODES_TOKENS, true, true);
			testMultipleParses(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	public static void testMultipleLemmas(GraphReader reader) throws GraphFormatException{
		reader.nextSentence();
		reader.nextSentence();
		ArrayList<Graph> sentence = reader.nextSentence();

		assertTrue(sentence.get(0).getNodes().size() == 17);
		assertTrue(sentence.get(5).getNodes().size() == 17);
		
		assertTrue(sentence.get(0).getNodes().get(3).getLemma().equals("processing"));
		assertTrue(sentence.get(0).getNodes().get(3).getPos().equals("NN1"));
		assertTrue(sentence.get(5).getNodes().get(3).getLemma().equals("process"));
		assertTrue(sentence.get(5).getNodes().get(3).getPos().equals("VVG"));
	}
	
	@Test
	public void testMultipleLemmas(){
		try{
			RaspXmlGraphReader reader = new RaspXmlGraphReader(smallFile, RaspXmlGraphReader.NODES_TOKENS, true, true);
			testMultipleLemmas(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	public static void testReadLarge(GraphReader reader) throws GraphFormatException{
		int graphCount = 0;
		while(reader.hasNext()){
			reader.next();
			graphCount++;
		}
		
		assertTrue(graphCount == 451);
	}
	
	@Test
	public void testReadLarge(){
		try{
			RaspXmlGraphReader reader = new RaspXmlGraphReader(largeFile, RaspXmlGraphReader.NODES_TOKENS, true, true);
			testReadLarge(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	public static void testReadDir(GraphReader reader) throws GraphFormatException{
		int graphCount = 0;
		while(reader.hasNext()){
			reader.next();
			graphCount++;
		}
		assertTrue(graphCount == (28 + 29 + 451));
	}
	
	@Test
	public void testReadDir(){
		try{
			RaspXmlGraphReader reader = new RaspXmlGraphReader(this.dir, RaspXmlGraphReader.NODES_TOKENS, true, true);
			testReadDir(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
}
