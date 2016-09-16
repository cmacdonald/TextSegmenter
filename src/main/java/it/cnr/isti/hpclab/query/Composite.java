package it.cnr.isti.hpclab.query;

/** 
 * A abstract composite node containing an array of component queries.
 */

public abstract class Composite implements Query 
{
	/** The component queries. Although public, this field should not be changed after creation. */
	public final Query query[];
	
	public Composite( final Query... query ) 
	{
		this.query = query;
	}
	
	/** 
	 * Returns a copy of the vector of the component queries (the queries themselves are not copied). 
	 * 
	 * @return a copy of the vector of the component queries.
	 */
	public Query[] components() 
	{
		return query.clone();
	}

	/** 
	 * Returns a string representation of this node, given a start string, and end string and a separator.
	 * Instantiating subclasses can easily write their {@link Object#toString()}
	 * methods by supplying these three strings and calling this method.
	 * 
	 * @param start the string to be used at the start of the string representation.
	 * @param end the string to be used at the end of the string representation.
	 * @param sep the separator between component queries.
	 * @return a string representation for this composite query node.
	 */
	
	protected String toString( final String start, final String end, final String sep ) 
	{
		final StringBuffer s = new StringBuffer();

		s.append( start );
		for (int i = 0; i < query.length; i++ ) { 
			if ( i != 0 )
				s.append( sep );
			s.append( query[ i ] );
		}
		s.append( end );
		
		return s.toString();
	}
}
