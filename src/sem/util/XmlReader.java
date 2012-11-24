package sem.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Class for simplifying the reading of XML documents.
 * It can take as input a single file or a directory of files, either plain text or GZIP-ed.
 *
 */
public class XmlReader implements XMLStreamReader{
	private ArrayList<File> inputFiles;
	private File currentInputFile;
	private XMLStreamReader xmlStreamReader;
	private InputStream fileInputStream;
	
	/**
	 * Constructs a new XmlReader.
	 * @param inputPath Path to file or directory.
	 */
	public XmlReader(String inputPath){
		this.inputFiles = new ArrayList<File>();
		this.currentInputFile = null;
		this.xmlStreamReader = null;
		
		File input = new File(inputPath);
		if(!input.exists()){
			System.err.println("Error: Input path for XmlReader is invalid: " + inputPath);
			System.exit(1);
		}
		
		this.inputFiles.addAll(FileReader.listFileRec(input));

		this.reset();
	}
	
	/**
	 * Reset the XmlReader
	 */
	public void reset() {
		try {
			this.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.currentInputFile = null;
		this.xmlStreamReader = null;
		this.fileInputStream = null;
	}
	
	public void openNextInputFile() {

		int currentIndex = -1;
		if(this.currentInputFile != null)
			currentIndex = inputFiles.indexOf(currentInputFile);

		if(currentIndex + 1 >= 0 && currentIndex + 1 < inputFiles.size())
			currentInputFile = inputFiles.get(currentIndex + 1);
		else 
			return;

		try {
			this.close();
			
			if(currentInputFile.getName().endsWith(".gz"))
				this.fileInputStream = new GZIPInputStream(new FileInputStream(currentInputFile));
			else
				this.fileInputStream = new FileInputStream(currentInputFile);

			this.xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(this.fileInputStream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	@Override
	public boolean hasNext(){
		try{
			if(this.xmlStreamReader == null || !this.xmlStreamReader.hasNext())
				this.openNextInputFile();
			if(this.xmlStreamReader == null)
				return false;
			return this.xmlStreamReader.hasNext();
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int next() {
		try{
			return this.xmlStreamReader.next();
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}


	@Override
	public void close(){
		try {
			if(this.xmlStreamReader != null)
				this.xmlStreamReader.close();
			if(this.fileInputStream != null)
				this.fileInputStream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getAttributeCount() {
		return this.xmlStreamReader.getAttributeCount();
	}

	@Override
	public String getAttributeLocalName(int index) {
		return this.xmlStreamReader.getAttributeLocalName(index);
	}

	@Override
	public QName getAttributeName(int index) {
		return this.xmlStreamReader.getAttributeName(index);
	}

	@Override
	public String getAttributeNamespace(int index) {
		return this.xmlStreamReader.getAttributeNamespace(index);
	}

	@Override
	public String getAttributePrefix(int index) {
		return this.xmlStreamReader.getAttributePrefix(index);
	}

	@Override
	public String getAttributeType(int index) {
		return this.xmlStreamReader.getAttributeType(index);
	}

	@Override
	public String getAttributeValue(int index) {
		return this.xmlStreamReader.getAttributeValue(index);
	}

	@Override
	public String getAttributeValue(String namespaceURI, String localName) {
		return this.xmlStreamReader.getAttributeValue(namespaceURI, localName);
	}

	@Override
	public String getCharacterEncodingScheme() {
		return this.xmlStreamReader.getCharacterEncodingScheme();
	}

	@Override
	public String getElementText() throws XMLStreamException {
		return this.xmlStreamReader.getElementText();
	}

	@Override
	public String getEncoding() {
		return this.xmlStreamReader.getEncoding();
	}

	@Override
	public int getEventType() {
		return this.xmlStreamReader.getEventType();
	}

	@Override
	public String getLocalName() {
		return this.xmlStreamReader.getLocalName();
	}

	@Override
	public Location getLocation() {
		return this.xmlStreamReader.getLocation();
	}

	@Override
	public QName getName() {
		return this.xmlStreamReader.getName();
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return this.xmlStreamReader.getNamespaceContext();
	}

	@Override
	public int getNamespaceCount() {
		return this.xmlStreamReader.getNamespaceCount();
	}

	@Override
	public String getNamespacePrefix(int index) {
		return this.xmlStreamReader.getNamespacePrefix(index);
	}

	@Override
	public String getNamespaceURI() {
		return this.xmlStreamReader.getNamespaceURI();
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return this.xmlStreamReader.getNamespaceURI(prefix);
	}

	@Override
	public String getNamespaceURI(int index) {
		return this.xmlStreamReader.getNamespaceURI(index);
	}

	@Override
	public String getPIData() {
		return this.xmlStreamReader.getPIData();
	}

	@Override
	public String getPITarget() {
		return this.xmlStreamReader.getPITarget();
	}

	@Override
	public String getPrefix() {
		return this.xmlStreamReader.getPrefix();
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return this.xmlStreamReader.getProperty(name);
	}

	@Override
	public String getText() {
		return this.xmlStreamReader.getText();
	}

	@Override
	public char[] getTextCharacters() {
		return this.xmlStreamReader.getTextCharacters();
	}

	@Override
	public int getTextCharacters(int sourceStart, char[] target,
			int targetStart, int length) throws XMLStreamException {
		return this.xmlStreamReader.getTextCharacters(sourceStart, target, targetStart, length);
	}

	@Override
	public int getTextLength() {
		return this.xmlStreamReader.getTextLength();
	}

	@Override
	public int getTextStart() {
		return this.xmlStreamReader.getTextStart();
	}

	@Override
	public String getVersion() {
		return this.xmlStreamReader.getVersion();
	}

	@Override
	public boolean hasName() {
		return this.xmlStreamReader.hasName();
	}

	@Override
	public boolean hasText() {
		return this.xmlStreamReader.hasText();
	}

	@Override
	public boolean isAttributeSpecified(int index) {
		return this.xmlStreamReader.isAttributeSpecified(index);
	}

	@Override
	public boolean isCharacters() {
		return this.xmlStreamReader.isCharacters();
	}

	@Override
	public boolean isEndElement() {
		return this.xmlStreamReader.isEndElement();
	}

	@Override
	public boolean isStandalone() {
		return this.xmlStreamReader.isStandalone();
	}

	@Override
	public boolean isStartElement() {
		return this.xmlStreamReader.isStartElement();
	}

	@Override
	public boolean isWhiteSpace() {
		return this.xmlStreamReader.isWhiteSpace();
	}

	@Override
	public int nextTag() throws XMLStreamException {
		return this.xmlStreamReader.nextTag();
	}

	@Override
	public void require(int type, String namespaceURI, String localName)
			throws XMLStreamException {
		this.xmlStreamReader.require(type, namespaceURI, localName);
		
	}

	@Override
	public boolean standaloneSet() {
		return this.xmlStreamReader.standaloneSet();
	}
}
