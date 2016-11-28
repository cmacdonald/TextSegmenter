package it.cnr.isti.hpclab.segmenter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.math3.util.Pair;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

// This class implements a static method segment(text) that takes as input a string of text.
// It outputs all possible segmentations of the given text, breaking it into words at whitespaces.
// A segment is a contiguous subsequence of words in text.
// A segmentation is a sequence of disjoint segments in text, whose concatenation is equal to text.
// For a text with k word, there are 2^{k-1} valid segmentations.

public class TextSegmentation 
{
	protected static final int DEFAULT_CACHE_SIZE = 1024;
	
	private final static int DEFAULT_NGRAM_LIMIT = 5;
	
	private static int limit = DEFAULT_NGRAM_LIMIT;
	
	private static final LoadingCache<String, Collection<Segmentation>> memoizationCache = CacheBuilder.newBuilder()
			.maximumSize(DEFAULT_CACHE_SIZE)
	        .build(new CacheLoader<String, Collection<Segmentation>>() {
	        	@Override
	            public Collection<Segmentation> load(String input) 
	        	{
	                return _search(input);
	            }
	        });

	protected static class WordSplitter implements Iterable<Pair<String,String>>
	{	
		
		private final String[] words;
		private final int limit;
				
		public WordSplitter(final String text)
		{
			this(text, DEFAULT_NGRAM_LIMIT);
		}
		
		public WordSplitter(final String text, int limit)
		{
			this.words = text.split("\\s+");
			this.limit = limit;
		}
		
		@Override
		public Iterator<Pair<String,String>> iterator() 
		{
			return new Iterator<Pair<String,String>>() 
			{
				int pos = 1;

				@Override public void remove() { throw new UnsupportedOperationException(); }
				
				@Override
				public boolean hasNext() 
				{
					// return (pos < 1 + words.length);
					return (pos < Math.min(words.length, limit) + 1);
				}

				@Override
				public Pair<String,String> next() 
				{
					
					Pair<String, String> res = new Pair<String, String>(
							strJoin(Arrays.copyOfRange(words, 0, pos), " "), //String.join(" ", Arrays.copyOfRange(words, 0, pos)),
							strJoin(Arrays.copyOfRange(words, pos, words.length), " ") //String.join(" ", Arrays.copyOfRange(words, pos, words.length))
					);
					
					pos++;
					
					return res;
				}
			};
		}	
	}
	
	public static Collection<Segment> bigrams(String input)
	{
		Collection<Segment> big = new ObjectArrayList<Segment>();
		String[] words = input.split("\\s+");
		if (words.length > 1) {
			for (int i = 0; i < words.length -1; ++i)
				big.add(new Segment(words[i] + ' ' + words[i+1]));
		}
		return big;
	}
	
	public static Collection<Segmentation> create(String input)
	{
		return create(input, DEFAULT_NGRAM_LIMIT);
	}
	
	public static Collection<Segmentation> create(String input, int limit)
	{
		TextSegmentation.limit = limit;
		// return unique(_search(input));
		
		try {
			return unique(memoizationCache.get(input));
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Collection<Segmentation> _search(String input)
	{		
		if (input == null || input.isEmpty())
			return singleton(input);
			
		Collection<Segmentation> candidates = new ObjectArrayList<Segmentation>();
		for (Pair<String, String> p: new WordSplitter(input, TextSegmentation.limit)) {
			if (p.getSecond() != null && p.getSecond().length() != 0)
				// candidates.addAll(combine(singleton(p.getFirst()), _search(p.getSecond())));
				try {
					candidates.addAll(combine(singleton(p.getFirst()), memoizationCache.get(p.getSecond())));
				} catch (ExecutionException e) {
					e.printStackTrace();
					return null;
				}
				
			if (input.split("\\s+").length <= TextSegmentation.limit)
				candidates.addAll(singleton(input));
		}		
		return candidates;
	}
	
	private static Collection<Segmentation> unique(Collection<Segmentation> ll)
	{
		List<Segmentation> set = new ObjectArrayList<Segmentation>();
		for (Segmentation l: ll)
			if (!set.contains(l))
			set.add(l);
		return set;
	}
	
	private static Collection<Segmentation> singleton(String input)
	{
		Collection<Segmentation> res = new ObjectArrayList<Segmentation>();
		
		res.add(new Segmentation(new Segment(input)));
		
		return res;
	}
	
	private static Collection<Segmentation> combine(final Collection<Segmentation> rem1, final Collection<Segmentation> rem2)
	{
		Collection<Segmentation> candidates = new ObjectArrayList<Segmentation>();
		for (Segmentation l1: rem1)
			for (Segmentation l2: rem2) {
				List<Segment> tmp = new ObjectArrayList<Segment>();
				tmp.addAll(l1.segments());
				tmp.addAll(l2.segments());
				candidates.add(new Segmentation(tmp));
			}
		return candidates;
	}
	
	public static void main(String args[])
	{
		String test = "1 1 1 1";
		// String test = "1 2 3 4 5 6 7 8 9 0";
		
		Collection<Segmentation> res = TextSegmentation.create(test,3);
		for (Segmentation l: res)
			print(l);
		
	}
	
	private static void print(final Segmentation l)
	{
		for (Segment s: l)
			System.err.print("(" + s + ") ");
		System.err.println();
	}

	public static String strJoin(List<String> aArr, String sSep) {
		return strJoin(aArr.toArray(new String[aArr.size()]), sSep);
	}

	public static String strJoin(String[] aArr, String sSep) {
    StringBuilder sbStr = new StringBuilder();
    for (int i = 0, il = aArr.length; i < il; i++) {
        if (i > 0)
            sbStr.append(sSep);
        sbStr.append(aArr[i]);
    }
    return sbStr.toString();
}
}
