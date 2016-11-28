package it.cnr.isti.hpclab.segmenter;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.util.Pair;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class WikiQuerySegmentation 
{
	protected static ObjectSet<String> parseFile(final String filename)
	{
		try {
			System.err.print("Loading Wikipedia Titles... ");
			GZIPInputStream input = new GZIPInputStream(WikiQuerySegmentation.class.getResourceAsStream(filename));

		
			List<String> lines = IOUtils.readLines(input, Charset.defaultCharset());
		
			ObjectSet<String> map = new ObjectAVLTreeSet<String>();
		
			for (String line: lines) {
				map.add(line);
			}
			System.err.println("Done!");
			return map;
		} catch (Exception x) {
			// Should never happen
			return null;
		}
	}

	protected static final String titlesResource  = "/clean.filtered.wiki.titles.txt.gz";
	
	protected static final ObjectSet<String> wiki_titles  = parseFile(titlesResource);
	
	protected static final int DEFAULT_CACHE_SIZE = 1024;
	
	private static final LoadingCache<String, Pair<Segment, Integer>> memoizationCache = CacheBuilder.newBuilder()
			.maximumSize(DEFAULT_CACHE_SIZE)
	        .build(new CacheLoader<String, Pair<Segment, Integer>>() {
	        	@Override
	            public Pair<Segment, Integer> load(String input) 
	        	{
	                return best_bigram(input);
	            }
	        });

	private static Pair<Segment, Integer> best_bigram(String segment)
	{
		Collection<Segment> bigrams = TextSegmentation.bigrams(segment);
		Segment best_bigram = null;
		int best_count = 0;
		for (Segment bigram: bigrams) {
			if (wiki_titles.contains(bigram.toString())) {
				if (NaiveQuerySegmentation.bigram_counts.containsKey(bigram.toString())) {
					int count = NaiveQuerySegmentation.bigram_counts.getInt(bigram.toString());
					if (count > best_count) {
						best_count = count;
						best_bigram = bigram;
					}
				}
			}
		}
		return new Pair<Segment, Integer>(best_bigram, best_count);
	}
	
	private static int weight(Segment segment)
	{
		String segment_string = segment.toString();
		if (wiki_titles.contains(segment_string)) {
			// return segment.size() + best_bigram(segment_string).getSecond();
			try {
				return segment.size() + memoizationCache.get(segment_string).getSecond();
			} catch (ExecutionException e) {
				e.printStackTrace();
				return 0;
			}
		} else {
			if (segment.size() > 3) {
				return -1;
			} else if (segment.size() == 3) {
				if (NaiveQuerySegmentation.trigram_counts.containsKey(segment_string)) {
					return NaiveQuerySegmentation.trigram_counts.getInt(segment_string);
				} else {
					return -1;
				}
			} else if (segment.size() == 2) {
				if (NaiveQuerySegmentation.bigram_counts.containsKey(segment_string)) {
					return NaiveQuerySegmentation.bigram_counts.getInt(segment_string);
				} else {
					return -1;
				}
			} else /* if (words == 1) */ {
				return 0;
			}
		}
	}
	
	public static Segmentation segment(String text)
	{
		return segment(text, 5);
	}
	
	public static Segmentation segment(String text, int ngram_limit)
	{
		Segmentation best_segmentation = null;
		float best_score = -1.0f;
		for (Segmentation curr_segmentation: TextSegmentation.create(text, ngram_limit)) {
			float curr_score = 0.0f;
			for (Segment segment: curr_segmentation) {
				curr_score += segment.size() * weight(segment);
			}
			if (curr_score > best_score) {
				best_segmentation = curr_segmentation;
				best_score = curr_score;
			}
		}
		return best_segmentation;
	}
	
	public static void main(String[] args)
	{
		String test1 = "toronto blue jays";
		String test2 = "new york city blue jeans";
		String test3 = "new york city";
		
		System.err.println(segment(test1, 3));
		System.err.println(segment(test2, 3));
		System.err.println(segment(test3, 3));

	}
}
