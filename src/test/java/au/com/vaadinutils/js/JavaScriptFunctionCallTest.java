package au.com.vaadinutils.js;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JavaScriptFunctionCallTest
{

	@Test
	public void testNoArgs()
	{
		JavaScriptFunctionCall func = new JavaScriptFunctionCall("fred");
		assertTrue(func.getCall().equals("fred();"));
	}

	@Test
	public void testNumberArg()
	{
		JavaScriptFunctionCall func = new JavaScriptFunctionCall("fred", new Integer(1));
		assertTrue(func.getCall().equals("fred(1);"));
	}

	@Test
	public void testNumber2Arg()
	{
		JavaScriptFunctionCall func = new JavaScriptFunctionCall("fred", new Integer(1), new Integer(2));
		assertTrue(func.getCall().equals("fred(1,2);"));
	}

	@Test
	public void testNumberAndBooleanArg()
	{
		JavaScriptFunctionCall func = new JavaScriptFunctionCall("fred", new Integer(1), new Integer(2), true);
		assertTrue(func.getCall().equals("fred(1,2,true);"));
	}

	@Test
	public void testNumberAndBooleanFalseArg()
	{
		JavaScriptFunctionCall func = new JavaScriptFunctionCall("fred", 1, 2, false);
		assertTrue(func.getCall().equals("fred(1,2,false);"));
	}

	@Test
	public void testNumberAndBooleanFalsePlusStringArg()
	{
		JavaScriptFunctionCall func = new JavaScriptFunctionCall("fred", 1, 2, false, "test Str'ing ");
		String test = "fred(1,2,false,'test Str\\'ing ');";
		assertTrue(func.getCall().equals(test));
	}

}
