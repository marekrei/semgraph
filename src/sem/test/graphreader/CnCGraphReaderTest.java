package sem.test.graphreader;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import sem.exception.GraphFormatException;
import sem.graph.Graph;
import sem.graphreader.CnCGraphReader;

public class CnCGraphReaderTest {
	
	private String dir = "examples/cnc/";
	private String smallFile = this.dir + "file1_tok.cnc";
	private String largeFile = this.dir + "pnp_1000_tok.cnc.gz";

	@Test
	public void testNodes() {
		try {
			CnCGraphReader reader = new CnCGraphReader(smallFile);
			reader.next();
			Graph graph = reader.next();
			
			assertTrue(graph.getNodes().size() == 12);
			
			ArrayList<String> checkNodes = new ArrayList<String>(Arrays.asList("natural_JJ", 
																				"language_NN", 
																				"processing_NN", 
																				"be_VBZ", 
																				"a_DT", 
																				"field_NN", 
																				"of_IN", 
																				"computer_NN", 
																				"science_NN", 
																				"and_CC", 
																				"linguistics_NNS",
																				"._."));
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
			CnCGraphReader reader = new CnCGraphReader(smallFile);
			
			// Let's take the second graph, to be sure that there is nothing carried over from the first one.
			reader.next();
			Graph graph = reader.next();
			
			assertTrue(graph.getEdges().size() == 11);
			
			ArrayList<String> checkEdges = new ArrayList<String>(Arrays.asList("ncmod	processing_NN	language_NN",
																				"ncmod	processing_NN	natural_JJ",
																				"det	field_NN	a_DT",
																				"ncmod	science_NN	computer_NN",
																				"conj	and_CC	linguistics_NNS",
																				"conj	and_CC	science_NN",
																				"dobj	of_IN	linguistics_NNS",
																				"dobj	of_IN	science_NN",
																				"ncmod	field_NN	of_IN",
																				"xcomp	be_VBZ	field_NN",
																				"ncsubj	be_VBZ	processing_NN"));
			
			for(int i = 0; i < checkEdges.size(); i++){
				String headLabel = graph.getEdges().get(i).getHead().getLemma() + "_" + graph.getEdges().get(i).getHead().getPos();
				String depLabel = graph.getEdges().get(i).getDep().getLemma() + "_" + graph.getEdges().get(i).getDep().getPos();
				String edgeLabel = graph.getEdges().get(i).getLabel() + "\t" + headLabel + "\t" + depLabel;
				if(!edgeLabel.equals(checkEdges.get(i)))
					System.out.println(edgeLabel + " " + checkEdges.get(i));
				assertTrue(edgeLabel.equals(checkEdges.get(i)));
			}
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReadLarge(){
		try{
			CnCGraphReader reader = new CnCGraphReader(largeFile);
			int graphCount = 0;
			while(reader.hasNext()){
				reader.next();
				graphCount++;
			}
			
			assertTrue(graphCount == 451);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	

}
