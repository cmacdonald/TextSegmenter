package it.cnr.isti.hpclab.segmenter;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.apache.commons.math3.util.Pair;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Ordering;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BigramSegmenter extends UnigramSegmenter
{
	protected static final String bigramsResource  = "/bigrams.txt";
	protected static final int DEFAULT_CACHE_SIZE = 1024;
	
	protected static Object2FloatMap<String> bigram_counts = parseFile(bigramsResource);
	
	protected static float score(final String word, final String prev)
	{
		if (prev == null) {
			if (word == null || word.isEmpty()) {
				return 1.0f;
			} else if (unigram_counts.containsKey(word)) {
				// Probability of the given word
	            return unigram_counts.getFloat(word) / TOTAL;
			} else {
	            // Penalize words not found in the unigrams according to their length, a crucial heuristic.
	            return (float) (10.0 / (TOTAL * Math.pow(10, word.length())));
			}
		} else {
			String bigram = prev + " " + word;
			if (bigram_counts.containsKey(bigram) && unigram_counts.containsKey(prev)) {
				// Conditional probability of the word given the previous word.
				// The technical name is *stupid backoff* and it's not a probability
				// distribution but it works well in practice.
				return bigram_counts.getFloat(bigram) / TOTAL / score(prev);
			} else {
				// Fall back to using the unigram probability.
		        return score(word);
			}
		}
	}

	private static final LoadingCache<String, Pair<Float,List<String>>> memoizationCache = CacheBuilder.newBuilder()
			.maximumSize(DEFAULT_CACHE_SIZE)
	        .build(new CacheLoader<String, Pair<Float,List<String>>>() {
	        	@Override
	            public Pair<Float,List<String>> load(String input) 
	        	{
	        		String tok[] = input.split(" ");
	        		if (tok.length == 1)
	        			return search(tok[0], null);
	        		return search(tok[0], tok[1]);
	            }
	        });
	
	public static List<String> segment(final String input)
	{
		// return search(clean(input), null).getSecond();
		try {
			return memoizationCache.get(clean(input)).getSecond();
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Pair<Float,List<String>> search(String input, String prev)
	{
		if (input == null || input.isEmpty())
			return new Pair<Float,List<String>>(0.0f, new ObjectArrayList<String>());
		
		List<Pair<Float,List<String>>> candidates = new ObjectArrayList<Pair<Float,List<String>>>(); 
		for (Pair<String, String> p: new Splitter(input)) {
			// candidates.add(combine((float) Math.log10(score(p.getFirst(), prev)), p.getFirst(), search(p.getSecond(), p.getFirst())));
			try {
				candidates.add(combine((float) Math.log10(score(p.getFirst(), prev)), p.getFirst(), memoizationCache.get(p.getSecond() + " " + p.getFirst())));
			} catch (ExecutionException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		Ordering<Pair<Float,List<String>>> o = new Ordering<Pair<Float,List<String>>>() {
			@Override
			public int compare(Pair<Float,List<String>> left, Pair<Float,List<String>> right)
			{
				return Float.compare(left.getFirst(), right.getFirst());
			}
		};

		return o.max(candidates);
	}
	
	private static Pair<Float,List<String>> combine(float prob, String word, Pair<Float,List<String>> rem)
	{
		List<String> list = new ObjectArrayList<String>();
		list.add(word);
		list.addAll(rem.getSecond());
		return new Pair<Float,List<String>>(prob + rem.getFirst(), list);
	}

	public static void main(String args[])
	{	
		System.out.print ("Performing some tests: ");
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
		
		String line;
		Scanner scan = new Scanner(System.in);
	    System.out.print ("Enter some text: ");
	    while ((line = scan.nextLine()) != null) {
	    	if (line.equals("*"))
	    		break;
	    	System.out.println ("Segments: " + segment(line));
	    	System.out.print ("Enter some text: ");
	    }
	    scan.close();
	}
}
