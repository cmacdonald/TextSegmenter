package it.cnr.isti.hpclab.segmenter;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.util.Pair;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Ordering;

import it.unimi.dsi.fastutil.objects.Object2FloatAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class UnigramWordSegmenter 
{
	/** 
	 * The total number of words in the corpus. A subset of this corpus is found in 
	 * <tt>/unigrams.txt</tt> and <tt>/bigrams.txt</tt>.
	 */
	protected static final float TOTAL = 1024908267229.0F;

	/**
	 * Utility static method to load and parse unigram and bigram counts.
	 * 
	 * @param filename  a text file's name composed by tab-separated string and float lines.
	 *  
	 * @return a map from strings to floats
	 */
	protected static Object2FloatMap<String> parseFile(final String filename)
	{
		try {
			System.err.print("Loading " + filename + "... ");
			InputStream input = UnigramWordSegmenter.class.getResourceAsStream(filename);
		
			List<String> lines = IOUtils.readLines(input, Charset.defaultCharset());
		
			Object2FloatMap<String> map = new Object2FloatAVLTreeMap<String>();
		
			for (String line: lines) {
				String tok[] = line.split("\t");
				map.put(tok[0], Float.parseFloat(tok[1]));
			}
			System.err.println("Done!");
			return map;
		} catch (Exception x) {
			// Should never happen
			return null;
		}
	}

	protected static final String unigramsResource = "/unigrams.txt";		
	protected static final int DEFAULT_CACHE_SIZE = 1024;
	
	protected static final Object2FloatMap<String> unigram_counts = parseFile(unigramsResource);
	
	/**
	 * Iterable class to cycle through all possible two-string splits of a given string.
	 * A maximum length for the first string of the split must be provided (default is 24).
	 */
	protected static class Splitter implements Iterable<Pair<String,String>>
	{
		private final static int DEFAULT_LENGTH_FIRST_STRING = 24;
		
		private final String text;
		private final int limit;
		
		public Splitter(final String text)
		{
			this(text, DEFAULT_LENGTH_FIRST_STRING);
		}
		
		public Splitter(final String text, final int limit)
		{
			this.text = text;
			this.limit = limit;
		}
		
		@Override
		public Iterator<Pair<String,String>> iterator() 
		{
			return new Iterator<Pair<String,String>>() 
			{
				int pos = 1;
				
				@Override
				public boolean hasNext() 
				{
					return (pos < Math.min(text.length(), limit) + 1);
				}

				@Override
				public Pair<String,String> next() 
				{
					Pair<String, String> res = new Pair<String, String>(text.substring(0, pos), text.substring(pos));
					pos++;
					return res;
				}
			};
		}	
	}
		
	/**
	 * Utility static method to return a score for a given word drawn from the unigram probabilities.
	 * Includes a try to penalize (but not nullify) the score of words not in the language model derived from the unigram file.
	 * 
	 * @param word the word to score
	 * 
	 * @return the probability of the word according to the unigram language model.
	 * 		   If word is null or empty, 1.0F is returned.
	 */
	protected static float score(final String word)
	{
		if (word == null || word.isEmpty()) {
			return 1.0F;
		} else if (unigram_counts.containsKey(word)) {
			// Probability of the given word
            return unigram_counts.getFloat(word) / TOTAL;
		} else {
            // Penalize words not found in the unigrams according to their length, a crucial heuristic.
            return (float) (10.0 / (TOTAL * Math.pow(10, word.length())));
		}
	}
	
	/**
	 * Utility static method to lowercase a string and remove all non-alphanumeric characters.
	 * 
	 * @param input the text to modify
	 * 
	 * @return the input text without non-alphanumeric characters and lowercased.
	 */
	public static String clean(String input)
	{
		return input.toLowerCase().replaceAll("[^a-z0-9]", "");
	}
	
	private static final LoadingCache<String, List<String>> memoizationCache = CacheBuilder.newBuilder()
			.maximumSize(DEFAULT_CACHE_SIZE)
	        .build(new CacheLoader<String, List<String>>() {
	        	@Override
	            public List<String> load(String input) 
	        	{
	                return search(input);
	            }
	        });
	
	/**
	 * Main static method to divide a phrase without spaces back into its constituent parts.
	 * 
	 * @param input the string to segment (all non-alphanumeric characters are ignored)
	 * 
	 * @return a list of constituent word according to the unigram language model
	 */
	public static List<String> segment(final String input)
	{
		// return search(clean(input));
		try {
			return memoizationCache.get(clean(input));
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static List<String> search(String input)
	{
		if (input == null || input.isEmpty())
			return new ObjectArrayList<String>();
				
		if (unigram_counts.containsKey(input)) {
			List<String> res = new ObjectArrayList<String>();
			res.add(input);
			return res;
		}
		
		List<List<String>> candidates = new ObjectArrayList<List<String>>(); 
		for (Pair<String, String> p: new Splitter(input)) {
			// candidates.add(combine(p.getFirst(), search(p.getSecond())));
			try {
				candidates.add(combine(p.getFirst(), memoizationCache.get(p.getSecond())));
			} catch (ExecutionException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		Ordering<List<String>> o = new Ordering<List<String>>() {
			@Override
			public int compare(List<String> left, List<String> right)
			{
				double left_prob = 0.0f, right_prob = 0.0f;
				for (String word: left)
					left_prob += Math.log10(score(word));
				for (String word: right)
					right_prob += Math.log10(score(word)); 

				return Double.compare(left_prob, right_prob);
			}
		};
		
		return o.max(candidates);
	}

	protected static List<String> combine(final String word, final List<String> rem)
	{
		List<String> list = new ObjectArrayList<String>();
		list.add(word);
		list.addAll(rem);
		return list;
	}
	
	public static void main(String args[])
	{	
		String test1 = "aroseisaroseisarose";
		String test2 = "whitechineserestaurant";
		String test3 = "newyorkrestaurant";
		String test4 = "sitdown";
		String test5 = "WhenintheCourseofhumaneventsitbecomesnecessaryforonepeople";
		String test6 = "WhenintheCourseofhumaneventsitbecomesnecessaryforonepeopletodissolvethepoliticalbandswhichhaveconnectedthemwithanother.";
		
		System.err.println(segment(test1));
		System.err.println(segment(test2));
		System.err.println(segment(test3));
		System.err.println(segment(test4));
		System.err.println(segment(test5));
		System.err.println(segment(test6));		
	}
}
