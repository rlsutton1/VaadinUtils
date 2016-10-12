package au.com.vaadinutils.js;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public class JavaScriptFunctionCall
{
	List<String> safeArgs = new LinkedList<>();
	private String functionName;

	/**
	 * creates a string suitable for calling a javascript function, where the
	 * parameters have been correctly escaped
	 * 
	 * example usage
	 * 
	 * JavaScriptFunctionCall func = new JavaScriptFunctionCall("fred", 1, 2,
	 * false, "test Str'ing ");<br>
	 * 
	 * func.getCall();
	 * 
	 * @param function
	 *            name of the java script function
	 * @param args
	 *            arguments to pass to the function
	 */
	JavaScriptFunctionCall(String function, Object... args)
	{
		Preconditions.checkArgument(StringUtils.isNotEmpty(function));

		functionName = function;
		for (Object arg : args)
		{
			if (arg instanceof Number)
			{
				safeArgs.add(arg.toString());
			}
			else if (arg instanceof Boolean)
			{
				safeArgs.add(arg.toString());
			}
			else
			{
				safeArgs.add("'" + StringEscapeUtils.escapeEcmaScript(arg.toString()) + "'");
			}

		}
	}

	String getCall()
	{
		String call = functionName + "(";

		for (String arg : safeArgs)
		{
			call += arg + ",";
		}
		if (!safeArgs.isEmpty())
		{
			call = call.substring(0, call.length() - 1);
		}
		return call + ");";
	}

}
