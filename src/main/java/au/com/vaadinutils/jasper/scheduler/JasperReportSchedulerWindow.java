package au.com.vaadinutils.jasper.scheduler;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.jasper.parameter.ReportChooser;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterOffsetType;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailParameterEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduledDateParameter;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailSender;
import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class JasperReportSchedulerWindow extends Window
{
	private static final long serialVersionUID = 1L;

	public JasperReportSchedulerWindow(JasperReportProperties props, Collection<ReportParameter<?>> params)
	{
		JasperReportProperties reportProperties = props;
		for (ReportParameter<?> p : params)
		{
			if (p instanceof ReportChooser)
			{
				ReportChooser chooser = (ReportChooser) p;
				reportProperties = chooser.getReportProperties(reportProperties);
			}
			else
			{
				if (!p.validate())
				{
					Notification.show("Invalid parameter " + p.getLabel());
					return;
				}
			}
		}

		ReportEmailScheduleEntity schedule = new ReportEmailScheduleEntity();
		schedule.setTitle(reportProperties.getReportTitle());
		schedule.setReportFilename(reportProperties.getReportFileName());
		schedule.setMessage(reportProperties.getReportTitle() + " report is attached");
		schedule.setSubject(reportProperties.getReportTitle());
		schedule.setReportClass(reportProperties.getReportClass());
		schedule.setScheduleMode(ScheduleMode.ONE_TIME);
		schedule.setOneTimeRunTime(new Date());
		schedule.setEnabled(false);

		EntityManager entityManager = EntityManagerProvider.getEntityManager();

		List<ReportEmailParameterEntity> rparams = new LinkedList<ReportEmailParameterEntity>();
		List<ReportEmailScheduledDateParameter> dparams = new LinkedList<ReportEmailScheduledDateParameter>();
		for (ReportParameter<?> param : params)
		{
			// omit report choosers, as they would complicate and confuse
			if (!(param instanceof ReportChooser))
			{
				if (!param.isDateField())
				{
					// add non data fields
					ReportEmailParameterEntity rparam = new ReportEmailParameterEntity();
					rparam.setName(param.getParameterName());
					rparam.setValue(param.getValue().toString());
					rparams.add(rparam);
					entityManager.persist(rparam);
				}
				else
				{
					// add date fields
					ReportEmailScheduledDateParameter rparam = new ReportEmailScheduledDateParameter();
					rparam.setName(param.getParameterName());
					rparam.setDate(param.getDate());
					rparam.setType(param.getDateParameterType());
					rparam.setOffsetType(DateParameterOffsetType.DAY_OF_SCHEDULE);
					rparam.setLabel(param.getLabel());
					dparams.add(rparam);
					entityManager.persist(rparam);

				}
			}
		}
		schedule.setParameters(rparams);

		schedule.setDateParameters(dparams);

		ReportEmailSender reportEmailSender = new ReportEmailSender();
		reportEmailSender.setUserName(reportProperties.getUsername());
		reportEmailSender.setEmailAddress(reportProperties.getUserEmailAddress());
		schedule.setSender(reportEmailSender);

		entityManager.persist(reportEmailSender);
		entityManager.persist(schedule);

		entityManager.flush();

		reportEmailSender = entityManager.merge(reportEmailSender);
		this.setWidth("90%");
		this.setHeight("80%");

		this.setContent(new JasperReportScheduleLayout(schedule.getId()));
		setModal(true);
		// center();
		UI.getCurrent().addWindow(this);

	}
}
