package sem.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import sem.test.graph.*;
import sem.test.tokeniser.*;
import sem.test.graphreader.*;


@RunWith(Suite.class)
@SuiteClasses({ GraphTest.class, 
				TokeniserTest.class,
				RaspXmlGraphReaderTest.class,
				RaspGraphReaderTest.class,
				ParsevalGraphReaderTest.class,
				CnCGraphReaderTest.class,
				TSVGraphReaderTest.class
				})

public class AllTests {

}
