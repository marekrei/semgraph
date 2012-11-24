package sem.run;

import java.util.ArrayList;

import sem.exception.GraphFormatException;
import sem.graph.Graph;
import sem.graphreader.GraphReader;
import sem.graphreader.GraphReaderType;
import sem.graphwriter.GraphWriter;
import sem.graphwriter.GraphWriterType;

/**
 * Converts graphs between compatible formats
 *
 */
public class ConvertGraphs {
	public static void convertGraphs(String inputTypeLabel, String inputPath, String outputTypeLabel, String outputPath){
		try {
			GraphReader reader = GraphReaderType.getType(inputTypeLabel).makeGraphReader(inputPath);
			GraphWriter writer = GraphWriterType.getType(outputTypeLabel).makeGraphWriter(outputPath);

			while(reader.hasNext()){
				ArrayList<Graph> sentence = reader.nextSentence();
				writer.write(sentence);
			}
			
			reader.close();
			writer.close();
		} catch (GraphFormatException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 4)
			System.out.println("Usage: ConvertGraphs <inputtype> <inputpath> <outputtype> <outputpath>");
		else
			convertGraphs(args[0], args[1], args[2], args[3]);
	}

}
