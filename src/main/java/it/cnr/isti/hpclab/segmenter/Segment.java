package it.cnr.isti.hpclab.segmenter;

import java.util.Collection;
import java.util.Iterator;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

// A simple class implementing a sequence of word
public class Segment implements Iterable<String>
{

 public static String strJoin(Collection<String> aArr, String sSep) {
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

	private final Collection<String> words;

	public Segment(final String text)
	{
		this.words = new ObjectArrayList<String>();
		
		if (text != null && text.length() != 0)
			for (String s: text.split("\\s+")) 
				words.add(s);
	}
	
	public int size()
	{
		return words.size();
	}
	
	@Override
	public Iterator<String> iterator() 
	{
		return words.iterator();
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((words == null) ? 0 : words.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Segment other = (Segment) obj;
		if (words == null) {
			if (other.words != null)
				return false;
		} else if (!words.equals(other.words))
			return false;
		return true;
	}

	@Override
	public String toString() 
	{
		return strJoin(words, " "); // String.join(" ", words);
	}

}
