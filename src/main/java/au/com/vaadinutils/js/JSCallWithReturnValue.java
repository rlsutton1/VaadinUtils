package au.com.vaadinutils.js;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;

import au.com.vaadinutils.dao.JpaEntityHelper;
import au.com.vaadinutils.errorHandling.ErrorWindow;
import elemental.json.JsonArray;

public class JSCallWithReturnValue
{
	// Logger logger = LogManager.getLogger();

	private String hookName;
	private String jsToExecute;
	private String errorHookName;

	Logger logger = LogManager.getLogger();
	private Exception trace;

	/**
	 * 
	 * @param jsToExecute
	 *            - the actual JS you want to execute and get a return value for
	 *            like... new Date(); <br>
	 *            <br>
	 *            JavaScriptCallback <Boolean> callback = new JavaScriptCallback
	 *            <Boolean>()<br>
	 *            {<br>
	 * 
	 *            public void callback(Boolean value)<br>
	 *            { <br>
	 *            // do something here with value<br>
	 *            }<br>
	 *            };<br>
	 * 
	 *            new
	 *            JSCallWithReturnValue("myMethod(true)").callBoolean(callback);
	 * 
	 */
	public JSCallWithReturnValue(final String jsToExecute)
	{
		this.hookName = "callback" + JpaEntityHelper.getGuid().replace("-", "_");
		this.errorHookName = "error" + JpaEntityHelper.getGuid().replace("-", "_");
		this.jsToExecute = jsToExecute;

	}

	public void callBoolean(final JavaScriptCallback<Boolean> callback)
	{

		JavaScript.getCurrent().addFunction(hookName, new JavaScriptFunction()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void call(JsonArray arguments)
			{
				try
				{
					callback.callback(arguments.getBoolean(0));
					JavaScript.getCurrent().removeFunction(hookName);
					logger.warn("Responded");
				}
				catch (Exception e)
				{
					logger.error(e.getMessage());
					logger.error(trace, trace);
				}
			}
		});

		final String wrappedJs = wrapJSInTryCatch(jsToExecute);
		setupErrorHook();
		JavaScript.getCurrent().execute(wrappedJs);
	}

	public void callVoid(final JavaScriptCallback<Void> callback)
	{

		JavaScript.getCurrent().addFunction(hookName, new JavaScriptFunction()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void call(JsonArray arguments)
			{
				callback.callback(null);
				JavaScript.getCurrent().removeFunction(hookName);
				JavaScript.getCurrent().removeFunction(errorHookName);
				logger.warn("Responded");

			}
		});
		setupErrorHook();
		JavaScript.getCurrent().execute(wrapJSInTryCatch(jsToExecute));
	}

	private void setupErrorHook()
	{
		trace = new JavaScriptException("Java Script Invoked From Here, JS:" + jsToExecute);

		final JavaScriptCallback<String> callback = new JavaScriptCallback<String>()
		{

			@Override
			public void callback(String value)
			{

				JavaScript.getCurrent().removeFunction(hookName);
				JavaScript.getCurrent().removeFunction(errorHookName);
				logger.error(jsToExecute + " -> resulted in the error: " + value, trace);
				Exception ex = new JavaScriptException(trace.getMessage() + " , JS Cause: " + value, trace);
				ErrorWindow.showErrorWindow(ex);

			}
		};

		JavaScript.getCurrent().addFunction(errorHookName, new JavaScriptFunction()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void call(JsonArray arguments)
			{
				callback.callback(arguments.getString(0));
				JavaScript.getCurrent().removeFunction(hookName);
				JavaScript.getCurrent().removeFunction(errorHookName);

			}
		});

	}

	private String wrapJSInTryCatch(String js)
	{
		js = js.trim();
		if (js.endsWith(";"))
		{
			js = js.substring(0, js.length() - 1);
		}

		final String wrapped = "try{" + hookName + "(" + js + ");}"

				+ "catch(err)"

				+ "{console.error(err);" + errorHookName + "(err.message);};";
		logger.error(wrapped);

		return wrapped;
	}

	void callBlind(final JavaScriptCallback<Void> javaScriptCallback)
	{
		JavaScript.getCurrent().addFunction(hookName, new JavaScriptFunction()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void call(JsonArray arguments)
			{
				javaScriptCallback.callback(null);
				JavaScript.getCurrent().removeFunction(hookName);
				JavaScript.getCurrent().removeFunction(errorHookName);
				logger.warn("Responded");

			}
		});
		setupErrorHook();
		JavaScript.getCurrent().execute(wrapJSInTryCatchBlind(jsToExecute));

	}

	private String wrapJSInTryCatchBlind(String js)
	{

		js = js.trim();
		if (js.endsWith(";"))
		{
			js = js.substring(0, js.length() - 1);
		}

		final String wrapped = "try{" + js + ";" + hookName + "();}"

				+ "catch(err)"

				+ "{console.error(err);" + errorHookName + "(err.message);};";

		// logger.error(wrapped);
		return wrapped;
	}
}
