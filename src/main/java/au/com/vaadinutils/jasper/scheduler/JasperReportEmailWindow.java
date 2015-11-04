package au.com.vaadinutils.jasper.scheduler;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;

import org.vaadin.dialogs.ConfirmDialog;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.parameter.ReportChooser;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterOffsetType;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailParameterEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipient;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipientVisibility;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduledDateParameter;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailSender;
import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class JasperReportEmailWindow extends Window
{
	private static final long serialVersionUID = 1L;
	private EmailTargetLayout emailTargetLayout;
	private TextField subject;
	private TextArea message;

	public JasperReportEmailWindow(final JasperReportProperties props, final Collection<ReportParameter<?>> params)
	{
		

		try
		{
			new InternetAddress(props.getUserEmailAddress());

		}
		catch (Exception e)
		{
			Notification.show("Your email address (" + props.getUserEmailAddress()
					+ ") is invalid, go to accounts and fix your email address.", Type.ERROR_MESSAGE);
			return;
		}

		
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

		// TopVerticalLayout outerLayout = new TopVerticalLayout();
		// outerLayout.setSizeFull();

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();

		emailTargetLayout = new EmailTargetLayout();
		emailTargetLayout.add(null);
		layout.addComponent(emailTargetLayout);

		subject = new TextField("Subject");
		subject.setWidth("100%");
		layout.addComponent(subject);
		subject.setValue(reportProperties.getReportTitle() + " report is attached");

		message = new TextArea("Message");
		message.setValue(reportProperties.getReportTitle() + " report is attached");
		message.setSizeFull();
		message.setMaxLength(1023);
		layout.addComponent(message);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSizeFull();
		buttonLayout.setHeight("10");

		Button send = new Button("Send");
		send.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				ReportEmailScheduleEntity schedule = sendEmail(params, reportProperties);

				if (schedule != null)
				{
					JasperReportEmailWindow.this.close();
					JasperReportScheudulerService.SELF.reschedule();
					Notification.show("Your email has been scheduled for immediate delivery", Type.WARNING_MESSAGE);
				}

			}
		});

		buttonLayout.addComponent(send);
		buttonLayout.setHeight("40");

		Button closer = new Button("Close");
		closer.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				ConfirmDialog.show(UI.getCurrent(), "No email will be sent if you close this window",
						new ConfirmDialog.Listener()
						{

							private static final long serialVersionUID = 1L;

							@Override
							public void onClose(ConfirmDialog arg0)
							{
								if (arg0.isConfirmed())
								{
									JasperReportEmailWindow.this.close();
								}

							}

						});

			}
		});

		buttonLayout.addComponent(closer);
		buttonLayout.setComponentAlignment(closer, Alignment.MIDDLE_RIGHT);
		layout.addComponent(buttonLayout);

		this.setWidth("80%");
		this.setHeight("80%");
		layout.setMargin(true);
		layout.setSpacing(true);
		
		setCaption("Email " + props.getReportTitle());

		layout.setExpandRatio(message, 1);

		this.setClosable(false);
		this.setContent(layout);
		setModal(true);
		// center();
		UI.getCurrent().addWindow(this);

	}

	private ReportEmailScheduleEntity sendEmail(Collection<ReportParameter<?>> params,
			JasperReportProperties reportProperties)
	{
		String errorMessage = "";
		boolean hasValidTargets = false;
		for (EmailTargetLine target : emailTargetLayout.getTargets())
		{
			if (target.targetAddress.getValue() != null)
			{
				if (!target.targetAddress.isValid())
				{
					errorMessage = target.targetAddress.getValue() + " is not a valid email address";
					Notification.show(errorMessage, Type.ERROR_MESSAGE);
					return null;
				}
				hasValidTargets = true;
			}
			
		}
		if (!hasValidTargets)
		{
			Notification.show("Set at least one Recipient.",Type.ERROR_MESSAGE);
			return null;
		}

		ReportEmailScheduleEntity schedule = new ReportEmailScheduleEntity();
		schedule.setTitle(reportProperties.getReportTitle());
		schedule.setReportFilename(reportProperties.getReportFileName());
		schedule.setReportTemplateIdentifier(reportProperties.getReportIdentifier());
		schedule.setMessage(message.getValue());
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
					rparam.setValue(param.getValue(names[0]).toString(), param.getDisplayValue(names[0]));
					rparams.add(rparam);
					entityManager.persist(rparam);
				}
				else
				{
					// add date fields
					ReportEmailScheduledDateParameter rparam = new ReportEmailScheduledDateParameter();
					String[] names = param.getParameterNames().toArray(new String[] {});
					rparam.setStartName(names[0]);
					rparam.setStartDate(param.getStartDate());
					rparam.setEndName(names[1]);
					rparam.setEndDate(param.getEndDate());

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
			recipient.setVisibility((ReportEmailRecipientVisibility) target.targetTypeCombo.getValue());
			entityManager.persist(recipient);
			recips.add(recipient);
		}
		schedule.setRecipients(recips);
		schedule.setNextScheduledRunTime(schedule.getScheduleMode().getNextRuntime(schedule, new Date()));
		schedule.setOutputFormat(OutputFormat.PDF);

		entityManager.persist(schedule);

		entityManager.flush();

		reportEmailSender = entityManager.merge(reportEmailSender);
		return schedule;
	}
}
