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
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipient;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduledDateParameter;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailSender;
import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;
import au.com.vaadinutils.layout.TopVerticalLayout;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class JasperReportEmailWindow extends Window
{
	private static final long serialVersionUID = 1L;
	private EmailTargetLayout emailTargetLayout;
	private TextField subject;

	public JasperReportEmailWindow(final JasperReportProperties props, final Collection<ReportParameter<?>> params)
	{
		JasperReportProperties temp = props;
		for (ReportParameter<?> p : params)
		{
			if (p instanceof ReportChooser)
			{
				ReportChooser chooser = (ReportChooser) p;
				temp = chooser.getReportProperties(props);
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
		final JasperReportProperties reportProperties = temp;

		TopVerticalLayout outerLayout= new TopVerticalLayout();
		outerLayout.setSizeFull();
		
		FormLayout layout = new FormLayout();
		layout.setSizeFull();

		emailTargetLayout = new EmailTargetLayout();

		emailTargetLayout.add(null);

		subject = new TextField("Subject");
		layout.addComponent(subject);

		Button send = new Button("Send");
		ClickListener t = null;
		send.addClickListener(new ClickListener()
		{

			@Override
			public void buttonClick(ClickEvent event)
			{
				ReportEmailScheduleEntity schedule = sendEmail(params, reportProperties);

				JasperReportEmailWindow.this.close();
			}
		});

		layout.addComponent(send);

		this.setWidth("400");
		this.setHeight("200");
		layout.setMargin(true);
		layout.setSpacing(true);
	//	layout.setHeight("100");
		

		setCaption("Email " + props.getReportTitle());

		outerLayout.addComponent(layout);
		outerLayout.addComponent(emailTargetLayout);
		
		this.setContent(outerLayout);
		setModal(true);
		// center();
		UI.getCurrent().addWindow(this);

	}

	private ReportEmailScheduleEntity sendEmail(Collection<ReportParameter<?>> params,
			JasperReportProperties reportProperties)
	{
		ReportEmailScheduleEntity schedule = new ReportEmailScheduleEntity();
		schedule.setTitle(reportProperties.getReportTitle());
		schedule.setReportFilename(reportProperties.getReportFileName());
		schedule.setMessage(reportProperties.getReportTitle() + " report is attached");
		schedule.setSubject(subject.getValue());
		schedule.setReportClass(reportProperties.getReportClass());
		schedule.setScheduleMode(ScheduleMode.ONE_TIME);
		schedule.setOneTimeRunTime(new Date());
		schedule.setEnabled(true);

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
					String[] names = param.getParameterNames().toArray(new String[] {});

					// add non date fields
					ReportEmailParameterEntity rparam = new ReportEmailParameterEntity();
					rparam.setName(names[0]);
					rparam.setValue(param.getValue(names[0]).toString());
					rparams.add(rparam);
					entityManager.persist(rparam);
				}
				else
				{
					// add date fields
					ReportEmailScheduledDateParameter rparam = new ReportEmailScheduledDateParameter();
					String[] names = param.getParameterNames().toArray(new String[] {});
					rparam.setStartName(names[0]);
					rparam.setStartDate( param.getStartDate());
					rparam.setEndName(names[1]);
					rparam.setEndDate( param.getEndDate());

					rparam.setType(param.getDateParameterType());
					rparam.setOffsetType(DateParameterOffsetType.CONSTANT);
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

		List<ReportEmailRecipient> recips = new LinkedList<ReportEmailRecipient>();
		for (EmailTargetLine target : emailTargetLayout.getTargets())
		{
			ReportEmailRecipient recipient = new ReportEmailRecipient();
			recipient.setEmail((String) target.targetAddress.getValue());
			recipient.setOwner(schedule);
			entityManager.persist(recipient);
			recips.add(recipient);
		}
		schedule.setRecipients(recips);

		entityManager.persist(schedule);

		entityManager.flush();

		reportEmailSender = entityManager.merge(reportEmailSender);
		return schedule;
	}
}
