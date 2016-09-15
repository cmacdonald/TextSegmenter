package it.cnr.isti.hpclab.segmenter;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;

import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class NaiveQuerySegmentation 
{
	/**
	 * Utility static method to load and parse unigram and bigram counts.
	 * 
	 * @param filename  a text file's name composed by tab-separated string and integer lines.
	 *  
	 * @return a map from strings to integers
	 */
	protected static Object2IntMap<String> parseFile(final String filename)
	{
		try {
			InputStream input = NaiveQuerySegmentation.class.getResourceAsStream(filename);
		
			List<String> lines = IOUtils.readLines(input, Charset.defaultCharset());
		
			Object2IntMap<String> map = new Object2IntAVLTreeMap<String>();
		
			for (String line: lines) {
				String tok[] = line.split("\t");
				map.put(tok[0], Integer.parseInt(tok[1]));
			}
			return map;
		} catch (Exception x) {
			// Should never happen
			return null;
		}
	}

	protected static final String bigramsResource  = "/msn.clean.frequent.bigrams.txt";
	protected static final String trigramsResource = "/msn.clean.frequent.trigrams.txt";
	
	protected static final Object2IntMap<String> bigram_counts  = parseFile(bigramsResource);
	protected static final Object2IntMap<String> trigram_counts = parseFile(trigramsResource);
		
	public static Segmentation segment(String text)
	{
		return segment(text, true, 5);
	}
	
	public static Segmentation segment(String text, boolean normalization, int ngram_limit)
	{
		int tri_norm = normalization ? 27 : 1;
		int bi_norm  = normalization ? 4 : 1;
		
		Segmentation best_segmentation = null;
		float best_score = 0.0f;
		for (Segmentation curr_segmentation: TextSegmentation.create(text, ngram_limit)) {
			float curr_score = 0.0f;
			for (Segment segment: curr_segmentation) {
				if (segment.size() > 3) {
					curr_score = Float.NEGATIVE_INFINITY;
					break;
				} else if (segment.size() == 3) {
					if (trigram_counts.containsKey(segment.toString())) {
						int freq = trigram_counts.getInt(segment.toString());
						curr_score += tri_norm * freq;
					} else {
						curr_score += -1.0f;
					}
				} else if (segment.size() == 2) {
					if (bigram_counts.containsKey(segment.toString())) {
						int freq = bigram_counts.getInt(segment.toString());
						curr_score += bi_norm * freq;
					} else {
						curr_score += -1.0f;
					}
				} else /* if (words == 1) */ {
					// curr_score += 0;
				}
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
		
		System.err.println(segment(test1,true, 3));
		System.err.println(segment(test2,false,3));
		System.err.println(segment(test3,true, 3));
	}
}
