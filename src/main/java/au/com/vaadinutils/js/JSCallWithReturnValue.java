package au.com.vaadinutils.js;

import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;

import au.com.vaadinutils.dao.JpaEntityHelper;
import elemental.json.JsonArray;

public class JSCallWithReturnValue
{
	// Logger logger = LogManager.getLogger();

	private String hookName;
	private String jsToExecute;

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
				callback.callback(arguments.getBoolean(0));
				JavaScript.getCurrent().removeFunction(hookName);
			}
		});

		final String wrappedJs = wrapJSInTryCatch(jsToExecute, hookName);
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

			}
		});

		JavaScript.getCurrent().execute(wrapJSInTryCatch(jsToExecute, hookName));
	}

	private String wrapJSInTryCatch(String js, String hookName)
	{
		return "try{" + hookName + "(" + js + ");}"

				+ "catch(err)"

				+ "{alert(err);};";
	}
}
