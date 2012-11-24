package sem.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import sem.test.graph.GraphTest;
import sem.test.graphreader.CnCGraphReaderTest;
import sem.test.graphreader.ParsevalGraphReaderTest;
import sem.test.graphreader.RaspGraphReaderTest;
import sem.test.graphreader.RaspXmlGraphReaderTest;
import sem.test.graphreader.TSVGraphReaderTest;
import sem.test.tokeniser.TokeniserTest;
import sem.test.util.FileReaderTest;
import sem.test.util.FileWriterTest;
import sem.test.util.ToolsTest;
import sem.test.util.XmlReaderTest;


@RunWith(Suite.class)
@SuiteClasses({ GraphTest.class, 
				TokeniserTest.class,
				RaspXmlGraphReaderTest.class,
				RaspGraphReaderTest.class,
				ParsevalGraphReaderTest.class,
				CnCGraphReaderTest.class,
				TSVGraphReaderTest.class,
				FileReaderTest.class,
				FileWriterTest.class,
				ToolsTest.class,
				XmlReaderTest.class
				})

public class SemGraphTests {

}
