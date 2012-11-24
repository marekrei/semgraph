package sem.test.graphreader;

import org.junit.Test;

import sem.exception.GraphFormatException;
import sem.graphreader.TSVGraphReader;

public class TSVGraphReaderTest {

	private String dir = "examples/tsv/";
	private String smallFile = this.dir + "file1.tsv";
	private String largeFile = this.dir + "pnp_1000.tsv.gz";

	@Test
	public void testNodes() {
		try {
			TSVGraphReader reader = new TSVGraphReader(smallFile, false);
			RaspXmlGraphReaderTest.testNodes(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEdges() {
		try {
			TSVGraphReader reader = new TSVGraphReader(smallFile, false);
			RaspXmlGraphReaderTest.testEdges(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMultipleParses(){
		try{
			TSVGraphReader reader = new TSVGraphReader(smallFile, true);
			RaspXmlGraphReaderTest.testMultipleParses(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMultipleLemmas(){
		try{
			TSVGraphReader reader = new TSVGraphReader(smallFile, true);
			RaspXmlGraphReaderTest.testMultipleLemmas(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testReadLarge(){
		try{
			TSVGraphReader reader = new TSVGraphReader(largeFile, true);
			RaspXmlGraphReaderTest.testReadLarge(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testReadDir(){
		try{
			TSVGraphReader reader = new TSVGraphReader(this.dir, true);
			RaspXmlGraphReaderTest.testReadDir(reader);
			reader.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
}
