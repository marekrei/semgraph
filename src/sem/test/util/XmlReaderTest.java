package sem.test.util;

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.stream.XMLStreamConstants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sem.util.FileWriter;
import sem.util.XmlReader;

public class XmlReaderTest {
	
	private String dir = "semtests/";
	private String file1 = dir + "test-xmlreader1.txt";
	private String file2 = dir + "test-xmlreader2.txt";
	private String text1 = "<root><tag1 attr1=\"value1\">contents1</tag1><tag2>contents2</tag2></root>";
	private String text2 = "<root><tag3>contents3</tag3><tag4>contents4</tag4></root>";
	

	@Before
	public void setUp() throws Exception {
		File d = new File(dir);
		if(!d.exists())
			d.mkdir();
		
		FileWriter fw = new FileWriter(file1);
		fw.writeln(text1);
		fw.close();
		
		fw = new FileWriter(file2);
		fw.writeln(text2);
		fw.close();		
		
	}

	@After
	public void tearDown() throws Exception {
		(new File(file1)).delete();
		(new File(file2)).delete();
		(new File(dir)).delete();
	}

	@Test
	public void testFileRead() {		
		XmlReader xmlReader = new XmlReader(file1);
		assertTrue(xmlReader.hasNext());
		
		String line = "";
		int count = 0;
		int eventCode;
		while(xmlReader.hasNext()){
			eventCode = xmlReader.next();
			if(eventCode == XMLStreamConstants.START_ELEMENT){
				line += "<" + xmlReader.getLocalName();
				for(int i = 0; i < xmlReader.getAttributeCount(); i++){
					line += " " + xmlReader.getAttributeLocalName(i) + "=\"" + xmlReader.getAttributeValue(i) + "\"";
				}
				line += ">";
			}
			else if(eventCode == XMLStreamConstants.END_ELEMENT)
				line += "</" + xmlReader.getLocalName() + ">";
			else if(eventCode == XMLStreamConstants.CHARACTERS)
				line += xmlReader.getText();
			count++;
		}

		assertTrue(count == 9);
		assertTrue(line.equals(text1));
		
		xmlReader.reset();
		count = 0;
		line = "";
		
		while(xmlReader.hasNext()){
			eventCode = xmlReader.next();
			if(eventCode == XMLStreamConstants.START_ELEMENT){
				line += "<" + xmlReader.getLocalName();
				for(int i = 0; i < xmlReader.getAttributeCount(); i++){
					line += " " + xmlReader.getAttributeLocalName(i) + "=\"" + xmlReader.getAttributeValue(i) + "\"";
				}
				line += ">";
			}
			else if(eventCode == XMLStreamConstants.END_ELEMENT)
				line += "</" + xmlReader.getLocalName() + ">";
			else if(eventCode == XMLStreamConstants.CHARACTERS)
				line += xmlReader.getText();
			count++;
		}

		assertTrue(count == 9);
		assertTrue(line.equals(text1));
		
		xmlReader.close();
	}
	
	@Test
	public void testDirRead() {		
		XmlReader xmlReader = new XmlReader(dir);
		assertTrue(xmlReader.hasNext());
		
		String line = "";
		int count = 0;
		int eventCode;
		while(xmlReader.hasNext()){
			eventCode = xmlReader.next();
			if(eventCode == XMLStreamConstants.START_ELEMENT){
				line += "<" + xmlReader.getLocalName();
				for(int i = 0; i < xmlReader.getAttributeCount(); i++){
					line += " " + xmlReader.getAttributeLocalName(i) + "=\"" + xmlReader.getAttributeValue(i) + "\"";
				}
				line += ">";
			}
			else if(eventCode == XMLStreamConstants.END_ELEMENT)
				line += "</" + xmlReader.getLocalName() + ">";
			else if(eventCode == XMLStreamConstants.CHARACTERS)
				line += xmlReader.getText();
			count++;
		}

		assertTrue(count == 18);
		assertTrue(line.equals(text1+text2));
		
		xmlReader.reset();
		count = 0;
		line = "";
		while(xmlReader.hasNext()){
			eventCode = xmlReader.next();
			if(eventCode == XMLStreamConstants.START_ELEMENT){
				line += "<" + xmlReader.getLocalName();
				for(int i = 0; i < xmlReader.getAttributeCount(); i++){
					line += " " + xmlReader.getAttributeLocalName(i) + "=\"" + xmlReader.getAttributeValue(i) + "\"";
				}
				line += ">";
			}
			else if(eventCode == XMLStreamConstants.END_ELEMENT)
				line += "</" + xmlReader.getLocalName() + ">";
			else if(eventCode == XMLStreamConstants.CHARACTERS)
				line += xmlReader.getText();
			count++;
		}
		
		assertTrue(count == 18);
		assertTrue(line.equals(text1+text2));
		
		xmlReader.close();
	}

}
