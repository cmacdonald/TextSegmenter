package it.cnr.isti.hpclab.query;

/** 
 * A parse exception.
 */

public class QueryParserException extends Exception 
{
	private static final long serialVersionUID = 1L;

	public QueryParserException( Throwable cause ) 
	{
		super( cause );
	}

	public QueryParserException( String msg ) 
	{
		super( msg );
	}
}
