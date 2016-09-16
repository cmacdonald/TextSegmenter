package it.cnr.isti.hpclab.query;

import java.util.Arrays;

/** 
 * A node representing the logical or of the underlying queries.
 */

public class Or extends Composite 
{
	public Or( final Query... query ) 
	{
		super( query );
	}

	@Override
	public String toString() 
	{
		return super.toString( "OR(", ")", ", " );
	}

	@Override
	public boolean equals( final Object o ) 
	{
		if ( ! ( o instanceof Or ) ) 
			return false;
		return Arrays.equals( query, ((Or)o).query );
	}
	
	@Override
	public int hashCode() 
	{
		return Arrays.hashCode( query ) ^ getClass().hashCode();
	}
}
