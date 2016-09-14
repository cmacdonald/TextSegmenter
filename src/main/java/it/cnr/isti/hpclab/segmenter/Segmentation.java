package it.cnr.isti.hpclab.segmenter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.math3.util.Pair;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Segmentation 
{
	protected static final int DEFAULT_CACHE_SIZE = 1024;
	
	private static final LoadingCache<String, List<List<String>>> memoizationCache = CacheBuilder.newBuilder()
			.maximumSize(DEFAULT_CACHE_SIZE)
	        .build(new CacheLoader<String, List<List<String>>>() {
	        	@Override
	            public List<List<String>> load(String input) 
	        	{
	                return _search(input);
	            }
	        });

	protected static class WordSplitter implements Iterable<Pair<String,String>>
	{	
		private final String[] words;
				
		public WordSplitter(final String text)
		{
			this.words = text.split("\\s+");
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
					return (pos < 1 + words.length);
				}

				@Override
				public Pair<String,String> next() 
				{
					
					Pair<String, String> res = new Pair<String, String>(
							String.join(" ", Arrays.copyOfRange(words, 0, pos)),
							String.join(" ", Arrays.copyOfRange(words, pos, words.length))
					);
					
					pos++;
					
					return res;
				}
			};
		}	
	}
	
	private static Set<List<String>> search(String input)
	{
		// return unique(_search(input));
		try {
			return unique(memoizationCache.get(input));
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private static List<List<String>> _search(String input)
	{		
		if (input == null || input.isEmpty())
			return singleton(null);
			
		if (input.length() == 1)
			return singleton(input);
		 
		List<List<String>> candidates = new ObjectArrayList<List<String>>();
		for (Pair<String, String> p: new WordSplitter(input)) {
			// candidates.addAll(combine(singleton(p.getFirst()), _search(p.getSecond())));
			try {
				candidates.addAll(combine(singleton(p.getFirst()), memoizationCache.get(p.getSecond())));
			} catch (ExecutionException e) {
				e.printStackTrace();
				return null;
			}

			candidates.addAll(singleton(input));
		}		
		return candidates;
	}
	
	
	
	public static Set<List<String>> unique(List<List<String>> ll)
	{
		Set<List<String>> set = new ObjectAVLTreeSet<List<String>>();
		for (List<String> l: ll)
			set.add(l);
		return set;
	}
	
	public static List<List<String>> singleton(String input)
	{
		List<String> l = new ObjectArrayList<String>();
		if (input != null && !input.isEmpty())
			l.add(input);
		List<List<String>> sl = new ObjectArrayList<List<String>>();
		sl.add(l);
		return sl;
	}
	
	protected static List<List<String>> combine(final List<List<String>> rem1, final List<List<String>> rem2)
	{
		List<List<String>> candidates = new ObjectArrayList<List<String>>();
		for (List<String> l1: rem1)
			for (List<String> l2: rem2) {
				List<String> tmp = new ObjectArrayList<String>();
				tmp.addAll(l1);
				tmp.addAll(l2);
				candidates.add(new ObjectArrayList<String>(tmp));
			}
		return candidates;
	}
	
	public static void main(String args[])
	{
		String test = "1 1 1 1 1 1 1 1 1 1 1 1";
		Set<List<String>> res = search(test);
		for (List<String> l: res)
			print(l);
	}
	
	private static void print(final List<String> l)
	{
		for (String s: l)
			System.err.print("(" + s + ") ");
		System.err.println();
	}
}
