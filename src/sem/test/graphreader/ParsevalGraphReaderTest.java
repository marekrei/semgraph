package sem.test.graphreader;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import sem.exception.GraphFormatException;
import sem.graph.Graph;
import sem.graphreader.ParsevalGraphReader;

public class ParsevalGraphReaderTest {

	private String dir = "examples/parseval/";
	private String smallFile = this.dir + "file1.parseval";
	private String largeFile = this.dir + "pnp_1000.parseval.gz";

	@Test
	public void testEdges() {
		try {
			ParsevalGraphReader reader = new ParsevalGraphReader(smallFile, false, false);
			// Let's take the second graph, to be sure that there is nothing carried over from the first one.
			reader.next();
			Graph graph = reader.next();
			
			assertTrue(graph.getEdges().size() == 10);
			
			ArrayList<String> checkEdges = new ArrayList<String>(Arrays.asList("ncsubj	be	processing",
																				"xcomp	be	field",
																				"det	field	a",
																				"iobj	field	of",
																				"dobj	of	and",
																				"ncmod	and	computer",
																				"conj	and	science",
																				"conj	and	linguistics",
																				"ncmod	processing	Natural",
																				"ncmod	processing	language"));
			
			for(int i = 0; i < checkEdges.size(); i++){
				String headLabel = graph.getEdges().get(i).getHead().getLemma();
				String depLabel = graph.getEdges().get(i).getDep().getLemma();
				String edgeLabel = graph.getEdges().get(i).getLabel() + "\t" + headLabel + "\t" + depLabel;
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
			ParsevalGraphReader reader = new ParsevalGraphReader(largeFile, false, false);
			RaspXmlGraphReaderTest.testReadLarge(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReadDir(){
		try{
			ParsevalGraphReader reader = new ParsevalGraphReader(this.dir, false, false);
			int graphCount = 0;
			while(reader.hasNext()){
				reader.next();
				graphCount++;
			}
			assertTrue(graphCount == (3 + 3 + 451));
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}

}
