package au.com.vaadinutils.js;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;

import au.com.vaadinutils.dao.JpaEntityHelper;
import au.com.vaadinutils.errorHandling.ErrorWindow;
import elemental.json.JsonArray;

public class JSCallWithReturnValue
{
	// Logger logger = LogManager.getLogger();

	private static final int EXPECTED_RESPONSE_TIME_MS = 1500;

	// this is a particularly large value as this time may include considerable
	// server side processing time
	protected static final int RESPONSE_TIMEOUT_MS = 30000;
	private String hookName;
	private String jsToExecute;
	private String errorHookName;

	Logger logger = LogManager.getLogger();
	private Exception trace;

	// setting the pool size to 1, we will hopefully never execute any events
	private final static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

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
		final Stopwatch timer = Stopwatch.createStarted();
		final ScheduledFuture<?> future = createTimeoutHook();

		JavaScript.getCurrent().addFunction(hookName, new JavaScriptFunction()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void call(JsonArray arguments)
			{
				try
				{
					if (timer.elapsed(TimeUnit.MILLISECONDS) > EXPECTED_RESPONSE_TIME_MS)
					{
						logger.warn("Responded after {}ms", timer.elapsed(TimeUnit.MILLISECONDS));
					}
					logger.info("Handling response for " + hookName);
					callback.callback(arguments.getBoolean(0));

				}
				catch (Exception e)
				{
					logger.error(e, e);
					logger.error(trace, trace);
				}
				finally
				{
					future.cancel(false);
					JavaScript.getCurrent().removeFunction(hookName);
					JavaScript.getCurrent().removeFunction(errorHookName);
				}
			}
		});

		final String wrappedJs = wrapJSInTryCatch(jsToExecute);
		setupErrorHook(future);
		JavaScript.getCurrent().execute(wrappedJs);

	}

	public void callVoid(final JavaScriptCallback<Void> callback)
	{
		final Stopwatch timer = Stopwatch.createStarted();
		final ScheduledFuture<?> future = createTimeoutHook();

		JavaScript.getCurrent().addFunction(hookName, new JavaScriptFunction()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void call(JsonArray arguments)
			{
				if (timer.elapsed(TimeUnit.MILLISECONDS) > EXPECTED_RESPONSE_TIME_MS)
				{
					logger.warn("Responded after {}ms", timer.elapsed(TimeUnit.MILLISECONDS));
				}
				callback.callback(null);
				future.cancel(false);
				JavaScript.getCurrent().removeFunction(hookName);
				JavaScript.getCurrent().removeFunction(errorHookName);
			}
		});
		setupErrorHook(future);
		JavaScript.getCurrent().execute(wrapJSInTryCatch(jsToExecute));

	}

	void callBlind(final JavaScriptCallback<Void> javaScriptCallback)
	{
		final Stopwatch timer = Stopwatch.createStarted();
		final ScheduledFuture<?> future = createTimeoutHook();

		JavaScript.getCurrent().addFunction(hookName, new JavaScriptFunction()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void call(JsonArray arguments)
			{
				logger.info("Handling response for " + hookName);
				javaScriptCallback.callback(null);
				future.cancel(false);
				JavaScript.getCurrent().removeFunction(hookName);
				JavaScript.getCurrent().removeFunction(errorHookName);
				if (timer.elapsed(TimeUnit.MILLISECONDS) > EXPECTED_RESPONSE_TIME_MS)
				{
					logger.warn("Responded after {}ms", timer.elapsed(TimeUnit.MILLISECONDS));
				}

			}
		});
		setupErrorHook(future);
		JavaScript.getCurrent().execute(wrapJSInTryCatchBlind(jsToExecute));

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
				logger.error(jsToExecute + " -> Timeout " + RESPONSE_TIMEOUT_MS + " requested at " + requestedAt + "ms",
						trace);

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

				+ "{console.error(err);" + errorHookName + "(err.message);};";
		logger.info(wrapped);

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

				+ "{console.error(err);" + errorHookName + "(err.message);};";

		// logger.error(wrapped);
		return wrapped;
	}
}
