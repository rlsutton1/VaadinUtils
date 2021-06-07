package au.com.vaadinutils.js;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;

import au.com.vaadinutils.dao.JpaEntityHelper;
import au.com.vaadinutils.errorHandling.ErrorWindow;
import elemental.json.JsonArray;

public class JSCallWithReturnValue
{
	// Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private static final int EXPECTED_RESPONSE_TIME_MS = 1500;

	// this is a particularly large value as this time may include considerable
	// server side processing time
	protected static final int RESPONSE_TIMEOUT_MS = 15000;
	private String hookName;
	private String jsToExecute;
	private String errorHookName;

	Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private Exception trace;

	// setting the pool size to 1, we will hopefully never execute any events
	private final static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1, new ThreadFactory()
	{
		@Override
		public Thread newThread(Runnable r)
		{
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setName(JSCallWithReturnValue.class.getSimpleName());
			t.setDaemon(true);
			return t;
		}
	});

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
	 *            JSCallWithReturnValue(JavaScriptFunctionCall("myMethod",true,1)).callBoolean(callback);
	 * 
	 */
	public JSCallWithReturnValue(final JavaScriptFunctionCall call)
	{
		this(call.getCall());
	}

	public void callBoolean(final JavaScriptCallback<Boolean> callback)
	{
		call(new JavaScriptCallback<JsonArray>()
		{
			boolean done = false;

			@SuppressWarnings({ "unused", "null" })
			@Override
			public void callback(JsonArray arguments)
			{
				if (!done)
				{
					done = true;

					// beware arguments.getBoolean(...) can return null or any
					// type for that matter
					Boolean result = arguments.getBoolean(0);
					if (result == null)
					{
						logger.warn("Method returned null, changing to false :\n" + jsToExecute);
						result = false;
					}
					callback.callback(result);
				}
				else
				{
					logger.warn("This appears to have been a duplicate callback, ignoring it!");
				}
			}
		});

	}

	public void callString(final JavaScriptCallback<String> callback)
	{
		call(new JavaScriptCallback<JsonArray>()
		{
			boolean done = false;

			@Override
			public void callback(JsonArray arguments)
			{
				if (!done)
				{
					done = true;
					if (arguments.length() > 0)
					{
						callback.callback(arguments.getString(0));
					}
					else
					{
						callback.callback(null);
					}
				}
				else
				{
					logger.warn("This appears to have been a duplicate callback, ignoring it!");
				}
			}
		});

	}

	public void callVoid(final JavaScriptCallback<Void> callback)
	{
		call(new JavaScriptCallback<JsonArray>()
		{
			boolean done = false;

			@Override
			public void callback(JsonArray value)
			{
				if (!done)
				{
					done = true;
					callback.callback(null);
				}
				else
				{
					logger.warn("This appears to have been a duplicate callback, ignoring it!");
				}
			}
		});
	}

	void call(final JavaScriptCallback<JsonArray> callback)
	{

		final Stopwatch timer = Stopwatch.createStarted();
		final ScheduledFuture<?> future = createTimeoutHook();

		JavaScript.getCurrent().addFunction(hookName, new JavaScriptFunction()
		{

			private static final long serialVersionUID = 1L;
			boolean done = false;

			@Override
			public void call(JsonArray arguments)
			{
				try
				{
					if (timer.elapsed(TimeUnit.MILLISECONDS) > EXPECTED_RESPONSE_TIME_MS)
					{
						logger.warn(jsToExecute + "\n\nResponded after {}ms", timer.elapsed(TimeUnit.MILLISECONDS));
					}
					logger.debug("Handling response for " + hookName);
					if (!done)
					{
						done = true;
						callback.callback(arguments);
					}
					else
					{
						logger.warn("This appears to have been a duplicate callback, ignoring it!");
					}

				}
				catch (Exception e)
				{
					logger.error(e, e);
					logger.error(trace, trace);
				}
				finally
				{
					future.cancel(false);
					removeHooks(hookName, errorHookName);
				}
			}
		});

		final String wrappedJs = wrapJSInTryCatch(jsToExecute);
		setupErrorHook(future);
		JavaScript.getCurrent().execute(wrappedJs);

	}

	void callBlind(final JavaScriptCallback<Void> javaScriptCallback)
	{
		final Stopwatch timer = Stopwatch.createStarted();
		final ScheduledFuture<?> future = createTimeoutHook();

		JavaScript.getCurrent().addFunction(hookName, new JavaScriptFunction()
		{
			boolean done = false;

			private static final long serialVersionUID = 1L;

			@Override
			public void call(JsonArray arguments)
			{
				logger.debug("Handling response for " + hookName);
				if (!done)
				{
					done = true;
					javaScriptCallback.callback(null);
				}
				else
				{
					logger.warn("This appears to have been a duplicate callback, ignoring it!");
				}
				future.cancel(false);
				removeHooks(hookName, errorHookName);
				if (timer.elapsed(TimeUnit.MILLISECONDS) > EXPECTED_RESPONSE_TIME_MS)
				{
					logger.warn(jsToExecute + "\n\nResponded after {}ms", timer.elapsed(TimeUnit.MILLISECONDS));
				}

			}
		});
		setupErrorHook(future);
		JavaScript.getCurrent().execute(wrapJSInTryCatchBlind(jsToExecute));

	}

	void removeHooks(final String hook1, final String hook2)
	{
		final JavaScript js = JavaScript.getCurrent();
		js.removeFunction(hook1);
		js.removeFunction(hook2);

	}

	private void setupErrorHook(final ScheduledFuture<?> future)
	{
		trace = new JavaScriptException("Java Script Invoked From Here, JS:" + jsToExecute);

		JavaScript.getCurrent().addFunction(errorHookName, new JavaScriptFunction()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void call(JsonArray arguments)
			{
				try
				{
					String value = arguments.getString(0);
					logger.error(jsToExecute + " -> resulted in the error: " + value, trace);
					Exception ex = new JavaScriptException(trace.getMessage() + " , JS Cause: " + value, trace);
					ErrorWindow.showErrorWindow(ex);
				}
				catch (Exception e)
				{
					ErrorWindow.showErrorWindow(trace);
				}
				finally
				{
					future.cancel(false);
					JavaScript.getCurrent().removeFunction(hookName);
					JavaScript.getCurrent().removeFunction(errorHookName);

				}

			}
		});

	}

	ScheduledFuture<?> createTimeoutHook()
	{
		final Date requestedAt = new Date();
		Runnable runner = new Runnable()
		{

			@Override
			public void run()
			{
				logger.error(
						jsToExecute + " -> Timeout " + RESPONSE_TIMEOUT_MS + " requested at " + requestedAt + "ms");

			}
		};
		ScheduledFuture<?> future = pool.schedule(runner, RESPONSE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		return future;
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

				+ "{debugger;console.error(err);" + errorHookName + "(err.message+' '+err.stack);};";
		logger.debug(wrapped);

		return wrapped;
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

				+ "{console.error(err);" + errorHookName + "(err.message+' '+err.stack);};";

		// logger.error(wrapped);
		return wrapped;
	}
}
