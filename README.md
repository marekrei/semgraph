SemGraph
=========

Author:		Marek Rei (marek@marekrei.com)

Version:	0.3

Updated:	2012-11-24

Homepage:	<http://www.marekrei.com/projects/semgraph/>

Download:	<https://github.com/marekrei/semgraph>

Documentation:	<http://www.marekrei.com/doc/semgraph/0.3/>

About
-----

SemGraph is a Java library for reading, writing and visualising graphs in different formats, mostly meant for dependency graphs of sentences.
After parsing a large corpus of text, this can be used to conveniently iterate over sentences to collect features, build vector space models or analyse the parser output.
It is designed so that the underlying parser can be easily changed without affecting the rest of the implementation.

The library includes a visualiser which creates a dynamic view of the graphs. An experimental feature can be enabled to also edit the graphs (e.g., correcting parses) using that interface.

![](http://www.marekrei.com/img/semgraph_graphvisualiser_screenshot.png "The graph visualiser")

![](http://www.marekrei.com/img/semgraph_latex3.png "LaTeX representation")

Supported formats
-----------------
Currently, the following input formats are supported:

* rasp - The default output from the RASP parser <http://ilexir.co.uk/2011/open-source-rasp-release/>
* raspxml - The xml output format from the RASP parser.
* cnc - The default output format from the C&C parser. <http://svn.ask.it.usyd.edu.au/trac/candc/>
* parseval - One of the outputs of the RASP parser and the format used by the Depbank/GR dataset.
* tsv - A simplified tab-separated format for representing graphs and sentences.

Also, the graphs can be written to an output file and the following formats are available:

* tsv - A simple tab-separated format. This format is supported for both reading and writing.
* tikzdependency - Produces a LaTeX representation of the graphs using the tikz-dependency library.

Please see the files in the examples directory for a better idea of the different formats. For example, here is a dependency graph in the rasp format:

	(|Natural| |language| |processing| |is| |a| |field| |of| |computer| |science| |and| |linguistics| |.|) 1 ;
	gr-list: 1
	(|ncsubj| |be+s:4_VBZ| |processing:3_NN1| _)
	(|xcomp| _ |be+s:4_VBZ| |field:6_NN1|)
	(|det| |field:6_NN1| |a:5_AT1|)
	(|iobj| |field:6_NN1| |of:7_IO|)
	(|dobj| |of:7_IO| |and:10_CC|)
	(|ncmod| _ |and:10_CC| |computer:8_NN1|)
	(|conj| |and:10_CC| |science:9_NN1|)
	(|conj| |and:10_CC| |linguistics:11_NN1|)
	(|ncmod| _ |processing:3_NN1| |Natural:1_JJ|)
	(|ncmod| _ |processing:3_NN1| |language:2_NN1|)


Usage
-----

Download the jar file and include it in your class path. The readers for different file formats are in the sem.graphreader package. They can take as input a single file or a whole directory. The files can be plain text or gzipped. The visualiser is in sem.graphvis.GraphVisualiser.
Take a look at the classes in the sem.examples package for an idea of how to use the readers and the visualiser.

Here is some example code for reading in graphs from the input file, printing out information about their nodes and edges, and running the visualiser.


	try {
		// Open the reader
		GraphReader reader = new RaspXmlGraphReader("examples/raspxml/file1.xml", RaspXmlGraphReader.NODES_TOKENS, false, false);
		
		// Create a list for storing the graphs
		ArrayList<Graph> graphs = new ArrayList<Graph>();
		
		// Iterate over graphs
		while(reader.hasNext()){ 
			Graph graph = reader.next();
			graphs.add(graph);

			// Iterate over nodes
			for(Node node : graph.getNodes()) 
				System.out.println("NODE: " + node.getLemma() + " " + node.getPos());

			// Iterate over edges
			for(Edge edge : graph.getEdges()) 
				System.out.println("EDGE: " + edge.getLabel() + " " + edge.getHead().getLemma() + " " + edge.getDep().getLemma());

			System.out.println();
		}
		
		// Close the reader
		reader.close();
		
		// Run the visualiser
		GraphVisualiser graphVisualiser = new GraphVisualiser(false);
		graphVisualiser.displayGraphs(graphs);
	} catch (GraphFormatException e) {
		e.printStackTrace();
	}



You can run the  GraphVisualiser directly from the jar file:

	VisualiseGraphs <inputtype> <inputpath>

For example:

	java -cp semgraph.jar:lib/prefuse.jar sem.run.VisualiseGraphs raspxml examples/raspxml/file1.xml


You can also convert some graphs to LaTeX format using the jar:

	ConvertGraphs <inputtype> <inputpath> <outputtype> <outputpath>

For example:

	java -cp semgraph.jar sem.run.ConvertGraphs raspxml examples/raspxml/file1.xml tikzdependency example.tex


Dependencies
------------

The Prefuse library needs to be included in your Java class path for the visualisation: <http://prefuse.org/>

The JUnit library needs to be included for the unit tests: <http://junit.sourceforge.net/>

Tikz-dependency library is needed to convert the LaTeX representation to pdf: <http://sourceforge.net/projects/tikz-dependency/>

Tikz is needed to run tikz-dependency: <http://www.texample.net/tikz/>

You might have tikz already installed on your system. But if you get weird errors, try updating it. The tikz-dependency documentation describes how to install both of the libraries.

Changes
-------

**0.3**

* Added GraphReaderType and GraphWriterType to handle different formats
* Several small code modifications

**0.2**

* Added support for writing graphs in LaTeX, using the tikz-dependency library.
* Added the option to specify how nodes are created in the RaspXmlGraphReader (either based on lemmas or tokens).

**0.1**

* Initial release 


License
-------

This software is distributed under the GNU Affero General Public License version 3. It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. The authors are not responsible for how it performs (or doesn't). See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

If you wish to use this software under a different license, feel free to contact me.
