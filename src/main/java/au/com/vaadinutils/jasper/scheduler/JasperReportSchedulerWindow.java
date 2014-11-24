package au.com.vaadinutils.jasper.scheduler;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import au.com.vaadinutils.help.HelpSplitPanel;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterOffsetType;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailParameterEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity_;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduledDateParameter;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailSender;
import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class JasperReportSchedulerWindow extends Window
{
	private static final long serialVersionUID = 1L;

	public JasperReportSchedulerWindow(final JasperReportProperties reportProperties,
			final Collection<ReportParameter<?>> params)
	{

		
		try
		{
			new InternetAddress(reportProperties.getUserEmailAddress());

		}
		catch (Exception e)
		{
			Notification.show("Your email address (" + reportProperties.getUserEmailAddress()
					+ ") is invalid, go to accounts and fix your email address.", Type.ERROR_MESSAGE);
			return;
		}

		this.setWidth("90%");
		this.setHeight("98%");

		HelpSplitPanel wrapper = new HelpSplitPanel(new JasperReportScheduleLayout(new ScheduleCreater()
		{

			@Override
			public ReportEmailScheduleEntity create()
			{
				ReportEmailScheduleEntity schedule = new ReportEmailScheduleEntity();
				schedule.setTitle(reportProperties.getReportTitle());
				schedule.setReportFilename(reportProperties.getReportFileName());
				schedule.setMessage(reportProperties.getReportTitle() + " report is attached");
				schedule.setSubject(reportProperties.getReportTitle());
				schedule.setReportClass(reportProperties.getReportClass());
				schedule.setScheduleMode(ScheduleMode.ONE_TIME);
				schedule.setOneTimeRunTime(new Date());
				schedule.setEnabled(false);
				schedule.setReportTemplateIdentifier(reportProperties.getReportIdentifier());

				List<ReportEmailParameterEntity> rparams = new LinkedList<ReportEmailParameterEntity>();
				List<ReportEmailScheduledDateParameter> dparams = new LinkedList<ReportEmailScheduledDateParameter>();
				for (ReportParameter<?> param : params)
				{
					if (param.isDateField())
					{
						// add date fields
						ReportEmailScheduledDateParameter rparam = new ReportEmailScheduledDateParameter();

						String[] names = param.getParameterNames().toArray(new String[] {});
						rparam.setStartName(names[0]);
						rparam.setStartDate(param.getStartDate());
						rparam.setEndName(names[1]);
						rparam.setEndDate(param.getEndDate());

						rparam.setType(param.getDateParameterType());
						rparam.setOffsetType(DateParameterOffsetType.TODAY);
						rparam.setLabel(param.getLabel());
						dparams.add(rparam);

					}

				}
				schedule.setParameters(rparams);

				schedule.setDateParameters(dparams);

				ReportEmailSender reportEmailSender = new ReportEmailSender();
				reportEmailSender.setUserName(reportProperties.getUsername());

				try
				{
					new InternetAddress(reportProperties.getUserEmailAddress());

				}
				catch (Exception e)
				{
					Notification.show("Your email address (" + reportProperties.getUserEmailAddress()
							+ ") is invalid, go to accounts and fix your email address.", Type.ERROR_MESSAGE);
					return null;
				}

				reportEmailSender.setEmailAddress(reportProperties.getUserEmailAddress());
				schedule.setSender(reportEmailSender);

				return schedule;
			}

			@Override
			public void addContainerFilter(JPAContainer<ReportEmailScheduleEntity> container)
			{
				Filter filter = new Compare.Equal(ReportEmailScheduleEntity_.reportIdentifier.getName(),
						reportProperties.getReportIdentifier().toString());
				container.addContainerFilter(filter);

			}
		}));

		this.setContent(wrapper);
		setModal(true);
		// center();
		UI.getCurrent().addWindow(this);

	}

}
