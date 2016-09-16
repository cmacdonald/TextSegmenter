package it.cnr.isti.hpclab.expander;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.text.ParseException;

import org.apache.commons.math3.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Parser for wordnet prolog format
 * <p>
 * See http://wordnet.princeton.edu/man/prologdb.5WN.html for a description of the format.
 */

// TODO: There is a bug, since same word as noun and adjective is not managed, there is some unexpected deletion... (e.g., welfare)
public class WordnetExpander 
{
	// First object is a word->synset_id map
	// Second object is a synset_id->synonyms' list list
	public static Pair<Object2IntMap<String>, ObjectList<ObjectList<String>>> parseFile(final String filename) throws IOException, ParseException 
	{
		System.err.print("Loading " + filename + "... ");
		InputStream input = WordnetExpander.class.getResourceAsStream(filename);
	
		Object2IntMap<String> map = new Object2IntAVLTreeMap<String>();
		ObjectList<ObjectList<String>> list = new ObjectArrayList<ObjectList<String>>();
	
		// List<String> lines = IOUtils.readLines(input, Charset.defaultCharset());
		final InputStreamReader reader = new InputStreamReader(input, Charset.defaultCharset());
		LineNumberReader br = new LineNumberReader(reader);
		
		try {
			String line = null;
			String lastSynSetID = "";
			ObjectList<String> synset = new ObjectArrayList<String>();
		      
			while ((line = br.readLine()) != null) {
				String synSetID = line.substring(2, 11);

		        if (!synSetID.equals(lastSynSetID)) {
		        	addInternal(synset, map, list);
		        	synset = new ObjectArrayList<String>();
		        }
		        
		        int start = line.indexOf('\'') + 1;
		        int end = line.lastIndexOf('\'');
		        
		        String text = line.substring(start, end).replace("''", "'");
		        
		        synset.add(text.toLowerCase());		        
		        lastSynSetID = synSetID;
			}
		      
			// final synset in the file
			addInternal(synset, map, list);
			
			System.err.println("Done!");
			return new Pair<Object2IntMap<String>, ObjectList<ObjectList<String>>>(map, list);
		} catch (IllegalArgumentException e) {
			ParseException ex = new ParseException("Invalid synonym rule at line " + br.getLineNumber(), 0);
			ex.initCause(e);
			throw ex;
		} finally {
			br.close();
		}
	}
	
	private static void addInternal(final ObjectList<String> synset, Object2IntMap<String> map, ObjectList<ObjectList<String>> list)
	{
	    if (synset.size() <= 1) {
	        return; // nothing to do
	    }
	    
	    list.add(synset);
	    int pos = list.size() - 1;
	    for (String syn: synset) {
	    	map.put(syn, pos);
	    }
	}
	
	protected static final String wordnetResource = "/wn_s.pl";		
	
	protected static Object2IntMap<String> wordnet_map = null;
	protected static ObjectList<ObjectList<String>> wordnet_synsets = null;
	
	static {
		try {
			Pair<Object2IntMap<String>, ObjectList<ObjectList<String>>> pair = parseFile(wordnetResource);
			wordnet_map = pair.getFirst();
			wordnet_synsets = pair.getSecond();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static ObjectList<String> getSynonims(final String word)
	{
		return wordnet_synsets.get(wordnet_map.getInt(word.toLowerCase()));
	}

	public static void main(String args[])
	{
		String test1 = "greek";
		String test2 = "welfare";
		String test3 = "Agreeable";
		
		System.err.println(WordnetExpander.getSynonims(test1));
		System.err.println(WordnetExpander.getSynonims(test2));
		System.err.println(WordnetExpander.getSynonims(test3));
	}
}
