SemGraph
=========

Author:		Marek Rei (marek@marekrei.com)

Version:	0.1

Updated:	2012-06-11

Homepage:	<http://www.marekrei.com/projects/semgraph/>

Download:	<https://github.com/marekrei/semgraph>

Documentation:	<http://www.marekrei.com/doc/semgraph/>

About
-----

SemGraph is a library for reading, writing and visualising graphs, mostly meant for dependency graphs of sentences.
After parsing a large corpus of text, this can be used to conveniently iterate over sentences to collect features, build vector space models or analyse the parser output.
It is designed so that the underlying parser can be easily changed without affecting the rest of the implementation.
The visualiser creates a dynamic view of the graphs. An experimental feature can be enabled to also edit the graphs using the visualiser (e.g., correcting parses).


![The graph visualiser](http://www.marekrei.com/img/graphvisualiser_screenshot.png)


Supported formats
-----------------

Currently, the following input formats are supported:

* rasp - The default output from the RASP parser <http://ilexir.co.uk/2011/open-source-rasp-release/>
* raspxml - The xml output format from the RASP parser.
* cnc - The default output format from the C&C parser. <http://svn.ask.it.usyd.edu.au/trac/candc/>
* parseval - One of the outputs of the RASP parser and the format used by the Depbank/GR dataset.
* tsv - A simplified tab-separated format for representing graphs and sentences.

Please see the files in the examples directory for a better idea of the different formats.

There is also a class for writing in the tsv format. This allows the user to load the graphs, edit them, save, and load again.



Usage
-----

Download the jar file and include it in your class path. The readers for different file formats are in the sem.graphreader package. They can take as input a single file or a whole directory. The files can be plain text or gzipped. The visualiser is in sem.graphvis.GraphVisualiser.
Take a look at the classes in the sem.examples package for an idea of how to use the readers and the visualiser.

The Prefuse library needs to be included for the visualisation: <http://prefuse.org/>

The JUnit library needs to be included for the unit tests: <http://junit.sourceforge.net/>


License
-------

This software is distributed under the GNU Affero General Public License version 3. It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. The authors are not responsible for how it performs (or doesn't). See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
