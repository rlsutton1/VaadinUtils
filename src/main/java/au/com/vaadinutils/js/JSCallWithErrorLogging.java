package au.com.vaadinutils.js;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JSCallWithErrorLogging
{
	// Logger logger = LogManager.getLogger();

	Logger logger = LogManager.getLogger();

	/**
	 * 
	 * @param jsToExecute
	 *            - the actual JS you want to execute alert('hello'); <br>
	 *            <br>
	 * 
	 *            new JSCallWithErrorLogging("alert('hello')");
	 * 
	 */
	public JSCallWithErrorLogging(final String jsToExecute)
	{
		new JSCallWithReturnValue(jsToExecute).callBlind(new JavaScriptCallback<Void>()
		{

			@Override
			public void callback(Void value)
			{
			}
		});
	}

	/**
	 * 
	 * @param jsToExecute
	 *            - the actual JS you want to execute alert('hello'); <br>
	 *            <br>
	 * 
	 *            new
	 *            JSCallWithErrorLogging(JavaScriptFunctionCall("alert","hello"));
	 * 
	 */
	public JSCallWithErrorLogging(final JavaScriptFunctionCall call)
	{
		this(call.getCall());
	}

}
