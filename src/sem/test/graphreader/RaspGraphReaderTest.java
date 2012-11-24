package sem.test.graphreader;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import sem.exception.GraphFormatException;
import sem.graph.Graph;
import sem.graphreader.RaspGraphReader;

public class RaspGraphReaderTest {

	private String dir = "examples/rasp/";
	private String smallFile = this.dir + "file1.rasp";
	private String largeFile = this.dir + "pnp_1000.rasp.gz";

	@Test
	public void testNodes() {
		try {
			RaspGraphReader reader = new RaspGraphReader(smallFile, false);
			reader.next();
			Graph graph = reader.next();
			
			assertTrue(graph.getNodes().size() == 11);
			
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
																				"linguistics_NN1")); // Missing the dot here because of the representation.
			for(int i = 0; i < checkNodes.size(); i++){
				String nodeLabel = graph.getNodes().get(i).getLemma() + "_" + graph.getNodes().get(i).getPos();
				assertTrue(nodeLabel.equals(checkNodes.get(i)));
			}
			
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testEdges() {
		try {
			RaspGraphReader reader = new RaspGraphReader(smallFile, false);
			RaspXmlGraphReaderTest.testEdges(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMultipleParses(){
		try{
			RaspGraphReader reader = new RaspGraphReader(smallFile, true);
			RaspXmlGraphReaderTest.testMultipleParses(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMultipleLemmas(){
		try{
			RaspGraphReader reader = new RaspGraphReader(smallFile, true);
			reader.nextSentence();
			reader.nextSentence();
			ArrayList<Graph> sentence = reader.nextSentence();

			assertTrue(sentence.get(0).getNodes().size() == 15); // Changed here
			assertTrue(sentence.get(5).getNodes().size() == 15); // And here
			
			assertTrue(sentence.get(0).getNodes().get(3).getLemma().equals("processing"));
			assertTrue(sentence.get(0).getNodes().get(3).getPos().equals("NN1"));
			assertTrue(sentence.get(5).getNodes().get(3).getLemma().equals("process"));
			assertTrue(sentence.get(5).getNodes().get(3).getPos().equals("VVG"));
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testReadLarge(){
		try{
			RaspGraphReader reader = new RaspGraphReader(largeFile, true);
			RaspXmlGraphReaderTest.testReadLarge(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testReadDir(){
		try{
			RaspGraphReader reader = new RaspGraphReader(this.dir, true);
			RaspXmlGraphReaderTest.testReadDir(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
}
