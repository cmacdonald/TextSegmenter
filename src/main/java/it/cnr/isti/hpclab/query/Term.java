package it.cnr.isti.hpclab.query;

/** 
 * A node representing a single term.
 */

public class Term implements Query 
{
	/** The term represented by this node*/
	public final String term;
	

	public Term( final String term ) 
	{
		this.term = term;
	}
	
	@Override
	public String toString() 
	{
		return term.toString();
	}

	@Override
	public boolean equals( final Object o ) 
	{
		if ( ! ( o instanceof Term) ) 
			return false;
		final Term t = ((Term)o);
		if ( ( term != null ) != ( t.term != null ) ) 
			return false;
		return term != null && term.toString().equals( ((Term)o).term.toString() );
	}
	
	@Override
	public int hashCode() 
	{
		return term == null ? 0 : term.hashCode() ^ getClass().hashCode();
	}
}
