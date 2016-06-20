package au.com.vaadinutils.js;

public class JavaScriptException extends Exception
{

	private static final long serialVersionUID = 1L;

	public JavaScriptException(String string, Exception trace)
	{
		super(string, trace);
	}

	public JavaScriptException(String string)
	{
		super(string);
	}
}
