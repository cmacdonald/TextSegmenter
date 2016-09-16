package it.cnr.isti.hpclab.query;

import java.util.Arrays;

/** 
 * A node representing the logical and of the underlying queries.
 */
public class And extends Composite 
{
	public And( final Query... query ) 
	{
		super( query );
	}
	
	@Override
	public String toString() 
	{
		return super.toString( "AND(", ")", ", " );
	}

	@Override
	public boolean equals( final Object o ) 
	{
		if ( ! ( o instanceof And ) ) 
			return false;
		return Arrays.equals( query, ((And)o).query );
	}
	
	@Override
	public int hashCode() 
	{
		return Arrays.hashCode( query ) ^ getClass().hashCode();
	}	
}
