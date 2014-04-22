package au.com.vaadinutils.jasper;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.Executor;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

public class CustomAsynchronousFillHandle extends AsynchronousFillHandle
{

	protected CustomAsynchronousFillHandle(JasperReportsContext jasperReportsContext, JasperReport jasperReport,
			Map<String, Object> parameters, JRDataSource dataSource, Connection conn) throws JRException
	{
		super(jasperReportsContext, jasperReport, parameters, dataSource, conn);
	}

	public CustomAsynchronousFillHandle(JasperReportsContext jasperReportsContext, JasperReport jasperReport,
			Map<String, Object> parameters, Connection conn) throws JRException
	{
		super(jasperReportsContext, jasperReport, parameters, null, conn);
	}

	volatile private JasperReportProperties dataProvider;
	volatile public OutputFormat outputFormat;

	public void setDataProvider(JasperReportProperties dataProvider, OutputFormat outputFormat)
	{
		this.dataProvider = dataProvider;
		this.outputFormat = outputFormat;

	}

	/**
	 * Returns an executor that creates a new thread to perform the report
	 * execution.
	 */
	@Override
	protected Executor getReportExecutor()
	{
		return new ThreadExecutor();
	}

	protected class ThreadExecutor implements Executor
	{
		public void execute(Runnable command)
		{

			Thread fillThread = new Thread(new RunnableWrapper(command), "report name goes here");

			fillThread.start();
		}
	}

	class RunnableWrapper implements Runnable
	{
		private Runnable command;

		RunnableWrapper(Runnable command)
		{
			this.command = command;
		}

		@Override
		public void run()
		{
			dataProvider.prepareForOutputFormat(outputFormat);
			command.run();

		}

	}

	/**
	 * @see #createHandle(JasperReportsContext, JasperReport, Map, Connection)
	 */
	public static CustomAsynchronousFillHandle createCustomHandle(JasperReport jasperReport,
			Map<String, Object> parameters, Connection conn) throws JRException
	{
		return createCustomHandle(DefaultJasperReportsContext.getInstance(), jasperReport, parameters, conn);
	}

	/**
	 * Creates an asychronous filling handle.
	 * 
	 * @param jasperReportsContext
	 *            the context
	 * @param jasperReport
	 *            the report
	 * @param parameters
	 *            the parameter map
	 * @param conn
	 *            the connection
	 * @return the handle
	 * @throws JRException
	 */
	public static CustomAsynchronousFillHandle createCustomHandle(JasperReportsContext jasperReportsContext,
			JasperReport jasperReport, Map<String, Object> parameters, Connection conn) throws JRException
	{
		return new CustomAsynchronousFillHandle(jasperReportsContext, jasperReport, parameters, conn);
	}

}
