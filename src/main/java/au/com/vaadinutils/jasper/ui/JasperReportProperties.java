package au.com.vaadinutils.jasper.ui;

import javax.persistence.EntityManager;

import au.com.vaadinutils.jasper.JasperSettings;

public class JasperReportProperties
{
	private String reportFileName;

	private EntityManager em;

	private JasperSettings settings;
	private JasperReportDataProvider dataProvider;
	String reportTitle;

	public JasperReportProperties(String title, String fileName, JasperReportDataProvider dataProvider, EntityManager em,
			JasperSettings settings)
	{
		this.reportTitle = title;
		this.reportFileName = fileName;
		this.dataProvider = dataProvider;
		this.em = em;
		this.settings = settings;
	}

	/**
	 * @return the reportFileName
	 */
	public String getReportFileName()
	{
		return reportFileName;
	}

	/**
	 * @return the em
	 */
	public EntityManager getEm()
	{
		return em;
	}

	/**
	 * @return the settings
	 */
	public JasperSettings getSettings()
	{
		return settings;
	}

	/**
	 * @return the dataProvider
	 */
	public JasperReportDataProvider getDataProvider()
	{
		return dataProvider;
	}

	/**
	 * @return the reportTitle
	 */
	public String getReportTitle()
	{
		return reportTitle;
	}

	

}
