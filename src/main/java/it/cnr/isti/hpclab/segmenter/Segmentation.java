package it.cnr.isti.hpclab.segmenter;

import java.util.Collection;
import java.util.Iterator;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

//A simple class implementing a sequence of segments
public class Segmentation implements Iterable<Segment>
{
	private final Collection<Segment> segs;

	public Segmentation(final Segment segment)
	{
		this.segs = new ObjectArrayList<Segment>();
		this.segs.add(segment);
	}

	public Segmentation(final Collection<Segment> segments)
	{
		this.segs = new ObjectArrayList<Segment>();
		this.segs.addAll(segments);
	}
	
	public int size()
	{
		return segs.size();
	}
	
	public Collection<Segment> segments()
	{
		return segs;
	}
	
	@Override
	public Iterator<Segment> iterator() 
	{
		return segs.iterator();
	}

	@Override
	public String toString() 
	{
		return "[" + segs + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((segs == null) ? 0 : segs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Segmentation other = (Segmentation) obj;
		if (segs == null) {
			if (other.segs != null)
				return false;
		} else if (!segs.equals(other.segs))
			return false;
		return true;
	}

}
