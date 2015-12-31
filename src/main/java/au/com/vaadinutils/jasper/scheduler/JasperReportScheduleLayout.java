package au.com.vaadinutils.jasper.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.EntityTable;
import au.com.vaadinutils.crud.FormHelper;
import au.com.vaadinutils.crud.HeadingPropertySet;
import au.com.vaadinutils.crud.ValidatingFieldGroup;
import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.fields.CKEditorEmailField;
import au.com.vaadinutils.help.HelpProvider;
import au.com.vaadinutils.help.VaadinUtilsHelpEnum;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.filter.ExpanderComponent;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportChooser;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterOffsetType;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterType;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailParameterEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipient;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipientVisibility;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity_;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailSender;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailSender_;
import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;
import au.com.vaadinutils.layout.TimePicker;
import au.com.vaadinutils.layout.TopVerticalLayout;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/** A start view for navigating to the main view */
public class JasperReportScheduleLayout extends BaseCrudView<ReportEmailScheduleEntity> implements View, HelpProvider
{

	Logger logger = LogManager.getLogger();

	private VerticalLayout paramForm;

	private ReportFilterUIBuilder builder;

	private JasperReportProperties reportProperties;

	EmailTargetLayout emailTargetLayout = new EmailTargetLayout();

	private ScheduleCreater scheduleCreater;

	private ComboBox sender;

	private TextField reportTitle;

	private ComboBox outputFormat;

	public JasperReportScheduleLayout()
	{

		JPAContainer<ReportEmailScheduleEntity> container = makeJPAContainer();

		HeadingPropertySet<ReportEmailScheduleEntity> headings = new HeadingPropertySet.Builder<ReportEmailScheduleEntity>()

				.addGeneratedColumn("Status", getStatusColumnGenerator())
				.addColumn("Report", ReportEmailScheduleEntity_.reportTitle)
				.addColumn("Subject", ReportEmailScheduleEntity_.subject)
				.addColumn("Owner", ReportEmailScheduleEntity_.sender)
				.addColumn("Next Run", ReportEmailScheduleEntity_.nextScheduledTime).build();

		this.disallowNew(true);
		init(ReportEmailScheduleEntity.class, container, headings);
	}

	private ColumnGenerator getStatusColumnGenerator()
	{
		return new ColumnGenerator()
		{

			private static final long serialVersionUID = -1873561613938103218L;

			@Override
			public Object generateCell(Table source, Object itemId, Object columnId)
			{
				@SuppressWarnings("unchecked")
				EntityItem<ReportEmailScheduleEntity> item = (EntityItem<ReportEmailScheduleEntity>) source
						.getItem(itemId);
				ReportEmailScheduleEntity schedule = item.getEntity();

				final Label label = new Label("<font color='green'>Scheduled</font>");
				label.setContentMode(ContentMode.HTML);
				if (schedule.getReportLog() != null && schedule.getReportLog().length() > 0	&& !schedule.getReportLog().equals(Scheduler.REPORT_SUCCESSFULLY_RUN))
				{
					label.setValue("<font color='red'><b>Error</b></font>");
				}
				else
				{
					if (!schedule.isEnabled())
					{
						label.setValue("<font color='orange'><b>Disabled</b></font>");
					}
				}

				return label;

			}
		};
	}

	public JasperReportScheduleLayout(ScheduleCreater creater)
	{
		this.scheduleCreater = creater;
		JPAContainer<ReportEmailScheduleEntity> container = makeJPAContainer();

		HeadingPropertySet<ReportEmailScheduleEntity> headings = new HeadingPropertySet.Builder<ReportEmailScheduleEntity>()

				.addColumn("Report", ReportEmailScheduleEntity_.reportTitle)
				.addColumn("Subject", ReportEmailScheduleEntity_.subject)
				.addColumn("Owner", ReportEmailScheduleEntity_.sender)
				.addColumn("Next Run", ReportEmailScheduleEntity_.nextScheduledTime)
				.addColumn("Enabled", ReportEmailScheduleEntity_.enabled)
				.addColumn("Last Run", ReportEmailScheduleEntity_.lastRuntime).build();
		init(ReportEmailScheduleEntity.class, container, headings);
		resetFilters();
		if (container.size() > 0)
		{
			entityTable.select(container.getIdByIndex(1));
		}
		else
		{
			entityTable.select(null);
		}

	}

	@Override
	protected void resetFilters()
	{
		container.removeAllContainerFilters();
		if (scheduleCreater != null)
		{
			scheduleCreater.addContainerFilter(container);
		}
		((EntityTable<ReportEmailScheduleEntity>) this.entityTable).refreshRowCache();
	}

	protected ReportEmailScheduleEntity preNew()
	{
		return scheduleCreater.create();
	}

	@Override
	protected ReportEmailScheduleEntity preNew(ReportEmailScheduleEntity previousEntity)
			throws InstantiationException, IllegalAccessException
	{
		return scheduleCreater.create();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void postNew(EntityItem<ReportEmailScheduleEntity> newEntity)
	{
		try
		{

			EntityManagerProvider.persist(newEntity.getEntity().getSender());
			EntityManagerProvider.getEntityManager().flush();

			// set the sender
			Long newId = newEntity.getEntity().getSender().getId();

			JPAContainer<ReportEmailSender> ds = (JPAContainer<ReportEmailSender>) sender.getContainerDataSource();
			ds.refresh();
			sender.setReadOnly(false);
			sender.select(newId);
			sender.setReadOnly(true);
			outputFormat.setValue(OutputFormat.PDF);
		}
		catch (Exception e)
		{
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
			logger.error(e, e);
		}
	}

	// private Long createSender(ReportEmailScheduleEntity
	// reportEmailScheduleEntity) throws AddressException
	// {
	// ReportEmailSender senderEntity =null;
	// if (reportEmailScheduleEntity.hasSenderEmailAddress())
	// {
	// JpaBaseDao<ReportEmailSender, Long> dao =
	// JpaBaseDao.getGenericDao(ReportEmailSender.class);
	// AttributesHashMap<ReportEmailSender> attributes = new
	// AttributesHashMap<>();
	// String emailAddress =
	// reportEmailScheduleEntity.getSendersEmailAddress().toString();
	//
	// String username = reportEmailScheduleEntity.getSendersUsername();
	// attributes.safePut(ReportEmailSender_.username, username);
	// attributes.safePut(ReportEmailSender_.emailAddress, emailAddress);
	//
	// senderEntity = dao.findOneByAttributes(attributes);
	//
	// }
	// if (senderEntity == null)
	// {
	// senderEntity = new ReportEmailSender();
	// scheduleCreater.create()
	// senderEntity.setUserName(username);
	// senderEntity.setEmailAddress(emailAddress);
	// EntityManagerProvider.persist(senderEntity);
	// EntityManagerProvider.getEntityManager().flush();
	// }
	// return senderEntity.getId();
	//
	// }

	public JPAContainer<ReportEmailScheduleEntity> makeJPAContainer()
	{
		return JpaBaseDao.getGenericDao(ReportEmailScheduleEntity.class).createVaadinContainer();

	}

	private static final long serialVersionUID = 1L;

	@Override
	@SuppressWarnings("deprecation")
	public TabSheet buildEditor(ValidatingFieldGroup<ReportEmailScheduleEntity> validatingFieldGroup)
	{

		TabSheet tabsheet = new TabSheet();
		tabsheet.setSizeFull();

		VerticalLayout main = new VerticalLayout();
		main.setSpacing(true);
		main.setMargin(true);
		main.setSizeFull();
		tabsheet.addTab(main, "Email");

		main.addComponent(emailTargetLayout);

		FormHelper<ReportEmailScheduleEntity> helper = new FormHelper<ReportEmailScheduleEntity>(main, fieldGroup);

		reportTitle = helper.bindTextField("Report", ReportEmailScheduleEntity_.reportTitle);
		reportTitle.setReadOnly(true);

		sender = helper.bindEntityField("From", ReportEmailScheduleEntity_.sender, ReportEmailSender.class,
				ReportEmailSender_.username);
		sender.setReadOnly(true);

		outputFormat = helper.bindEnumField("Output format", ReportEmailScheduleEntity_.outputFormat.getName(),
				OutputFormat.class);

		outputFormat.removeItem(OutputFormat.HTML);

		helper.bindTextField("Subject", ReportEmailScheduleEntity_.subject);
		CKEditorEmailField message = helper.bindEditorField("Message", ReportEmailScheduleEntity_.message, false);

		helper.bindTextField("Report Log", ReportEmailScheduleEntity_.reportLog.getName()).setReadOnly(true);

		main.setExpandRatio(message, 1);

		TopVerticalLayout scheduleWrapper = new TopVerticalLayout();
		scheduleWrapper.setSizeFull();
		tabsheet.addTab(scheduleWrapper, "Schedule");
		FormLayout scheduleForm = new FormLayout();
		scheduleForm.setSizeFull();
		scheduleForm.setMargin(true);
		scheduleWrapper.addComponent(scheduleForm);

		buildScheduleTab(validatingFieldGroup, scheduleForm, helper);

		VerticalLayout paramWrapper = new VerticalLayout();
		paramWrapper.setSizeFull();
		tabsheet.addTab(paramWrapper, "Parameters");
		paramForm = new VerticalLayout();
		paramForm.setSizeFull();
		paramForm.setMargin(true);

		paramWrapper.addComponent(paramForm);

		return tabsheet;

	}

	private void buildScheduleTab(ValidatingFieldGroup<ReportEmailScheduleEntity> validatingFieldGroup, FormLayout main,
			FormHelper<ReportEmailScheduleEntity> helper)
	{

		helper.bindBooleanField(main, validatingFieldGroup, "Enabled", ReportEmailScheduleEntity_.enabled);

		helper.bindDateField(main, validatingFieldGroup, "Last Run Time", ReportEmailScheduleEntity_.lastRuntime,
				"yyyy/MM/dd HH:mm", Resolution.MINUTE).setReadOnly(true);
		helper.bindDateField(main, validatingFieldGroup, "Next Run Time", ReportEmailScheduleEntity_.nextScheduledTime,
				"yyyy/MM/dd HH:mm", Resolution.MINUTE).setReadOnly(true);

		ComboBox modeCombo = helper.bindEnumField(main, validatingFieldGroup, "Frequency",
				ReportEmailScheduleEntity_.scheduleMode, ScheduleMode.class);

		final DateField oneTime = helper.bindDateField(main, validatingFieldGroup, "Start time",
				ReportEmailScheduleEntity_.oneTimeRunDateTime, "yyyy/MM/dd HH:mm", Resolution.MINUTE);

		final TimePicker timeOfDay = new TimePicker("Start time");
		main.addComponent(timeOfDay);
		validatingFieldGroup.bind(timeOfDay, ReportEmailScheduleEntity_.timeOfDayToRun.getName());

		// day of week
		final DayOfWeekCheckBoxes dayLayout = new DayOfWeekCheckBoxes();
		validatingFieldGroup.bind(dayLayout, ReportEmailScheduleEntity_.scheduledDaysOfWeek.getName());

		main.addComponent(dayLayout);

		// day of month
		List<String> options = new LinkedList<String>();
		for (int i = 1; i < 32; i++)
		{
			options.add("" + i);
		}

		final ComboBox dayOfMonth = helper.bindComboBox(main, validatingFieldGroup,
				ReportEmailScheduleEntity_.scheduledDayOfMonth.getName(), "Day of month", options);
		dayOfMonth.setConverter(Integer.class);
		dayOfMonth.setNullSelectionAllowed(true);

		modeCombo.addValueChangeListener(new ValueChangeListener()
		{

			private static final long serialVersionUID = 3640488458535487174L;

			@Override
			public void valueChange(ValueChangeEvent event)
			{
				ScheduleMode mode = (ScheduleMode) event.getProperty().getValue();
				if (mode == null)
				{
					mode = ScheduleMode.ONE_TIME;
				}
				oneTime.setRequired(false);

				switch (mode)
				{
					case ONE_TIME:
						oneTime.setRequired(true);
						oneTime.setVisible(true);
						timeOfDay.setVisible(false);
						dayLayout.setVisible(false);
						dayOfMonth.setVisible(false);

						break;
					case DAY_OF_MONTH:
						oneTime.setVisible(false);
						timeOfDay.setVisible(true);
						dayLayout.setVisible(false);
						dayOfMonth.setVisible(true);
						break;
					case DAY_OF_WEEK:
						oneTime.setVisible(false);
						timeOfDay.setVisible(true);
						dayLayout.setVisible(true);
						dayLayout.setRequired(true);
						dayOfMonth.setVisible(false);
						break;
					case EVERY_DAY:
						oneTime.setVisible(false);
						timeOfDay.setVisible(true);
						dayLayout.setVisible(false);
						dayOfMonth.setVisible(false);
						break;

				}

			}
		});
	}

	@Override
	/**
	 * Called when the currently selected row in the table part of this view has
	 * changed. We use this to update the editor's current item.
	 *
	 * @item the item that is now selected. This may be null if selection has
	 *       been lost.
	 */
	public void rowChanged(EntityItem<ReportEmailScheduleEntity> item)
	{

		super.rowChanged(item);
		emailRecipientsHandleRowChange(item);
		dateParamsHandleRowChange(item);
		paramsHandleRowChange(item);

	}

	class EntityParamUpdater
	{

		private String name;
		private ComboBox offsetType;
		private DateField startDateField;
		private TimePicker startTimePicker;
		private DateField endDateField;
		private TimePicker endTimePicker;

		public EntityParamUpdater(String name, ComboBox offsetType, DateField startDateField,
				TimePicker startTimePicker, DateField endDateField, TimePicker endTimePicker)
		{
			this.name = name;
			this.offsetType = offsetType;
			this.startDateField = startDateField;
			this.startTimePicker = startTimePicker;
			this.endDateField = endDateField;
			this.endTimePicker = endTimePicker;

		}

	}

	final List<EntityParamUpdater> updaters = new LinkedList<EntityParamUpdater>();

	private void dateParamsHandleRowChange(EntityItem<ReportEmailScheduleEntity> item)
	{
		paramForm.removeAllComponents();
		paramForm.setSpacing(true);
		paramForm.setSizeFull();
		paramForm.setMargin(true);
		updaters.clear();
		if (item != null)
		{
			ReportEmailScheduleEntity entity = item.getEntity();

			List<DateParameterOffsetType> offsetTypes = new LinkedList<DateParameterOffsetType>();
			for (DateParameterOffsetType offsetType : DateParameterOffsetType.values())
			{
				offsetTypes.add(offsetType);
			}

			for (final ScheduledDateParameter dateParam : entity.getDateParameters())
			{
				HorizontalLayout dateLayout = new HorizontalLayout();
				dateLayout.setSizeFull();
				dateLayout.setSpacing(true);
				dateLayout.setHeight("55");

				final ComboBox offsetType = new ComboBox(dateParam.getLabel(), offsetTypes);
				offsetType.setImmediate(true);
				offsetType.setNullSelectionAllowed(false);
				offsetType.setWidth("140");

				final DateField startDateField = new DateField("From", dateParam.getStartDate());
				startDateField.setResolution(Resolution.DAY);
				startDateField.setDateFormat("yyyy/MM/dd");

				// pickers visability doesn't change, it's determined by the
				// parameter type which can't be changed here
				final TimePicker startTimePicker = new TimePicker("");
				startTimePicker.setValues(dateParam.getStartDate());
				startTimePicker.setVisible(dateParam.getType() == DateParameterType.DATE_TIME);

				final DateField endDateField = new DateField("To", dateParam.getEndDate());
				endDateField.setResolution(Resolution.DAY);
				endDateField.setDateFormat("yyyy/MM/dd");

				// pickers visability doesn't change, it's determined by the
				// parameter type which can't be changed here
				final TimePicker endTimePicker = new TimePicker("");
				endTimePicker.setValues(dateParam.getStartDate());
				endTimePicker.setVisible(dateParam.getType() == DateParameterType.DATE_TIME);

				offsetType.addValueChangeListener(new ValueChangeListener()
				{

					private static final long serialVersionUID = 7081417825842355432L;

					@Override
					public void valueChange(ValueChangeEvent event)
					{
						DateParameterOffsetType offsetTypeValue = (DateParameterOffsetType) event.getProperty()
								.getValue();
						startDateField.setVisible(offsetTypeValue == DateParameterOffsetType.CONSTANT);
						endDateField.setVisible(offsetTypeValue == DateParameterOffsetType.CONSTANT);

					}
				});
				offsetType.setValue(dateParam.getOffsetType());
				dateLayout.addComponent(offsetType);
				dateLayout.addComponent(startDateField);
				dateLayout.addComponent(startTimePicker);
				dateLayout.addComponent(endDateField);
				dateLayout.addComponent(endTimePicker);
				paramForm.addComponent(dateLayout);

				updaters.add(new EntityParamUpdater(dateParam.getLabel(), offsetType, startDateField, startTimePicker,
						endDateField, endTimePicker));

			}
		}

	}

	private void paramsHandleRowChange(EntityItem<ReportEmailScheduleEntity> item)
	{

		try
		{
			builder = null;
			reportProperties = null;
			if (item != null)
			{
				ReportEmailScheduleEntity entity = item.getEntity();
				reportProperties = entity.getJasperReportPropertiesClass().newInstance();
				builder = reportProperties.getFilterBuilder();
				List<ExpanderComponent> paramComponents = builder.buildLayout(true);
				for (ExpanderComponent componet : paramComponents)
				{
					paramForm.addComponent(componet.getComponent());
					if (componet.shouldExpand())
					{
						paramForm.setExpandRatio(componet.getComponent(), 1);
					}
				}
				for (ReportParameter<?> builtParam : builder.getReportParameters())
				{
					for (ReportEmailParameter schedParam : entity.getReportParameters())
					{
						for (String parameterName : builtParam.getParameterNames())
						{
							if (schedParam.getName().equalsIgnoreCase(parameterName))
							{
								builtParam.setValueAsString(schedParam.getValue(), parameterName);
								break;
							}
						}
					}

				}
			}
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}

	}

	private void emailRecipientsHandleRowChange(EntityItem<ReportEmailScheduleEntity> item)
	{
		int ctr = 0;
		emailTargetLayout.clear();

		if (item != null)
		{
			ReportEmailScheduleEntity entity = item.getEntity();
			if (entity != null)
			{
				for (ReportEmailRecipient recip : entity.getRecipients())
				{
					ctr++;
					emailTargetLayout.add(recip);
				}
				if (ctr == 0)
				{
					emailTargetLayout.add(null);
				}
			}
		}
	}

	@Override
	protected void interceptSaveValues(EntityItem<ReportEmailScheduleEntity> entityItem)
	{
		ReportEmailScheduleEntity entity = entityItem.getEntity();
		entity.setNextScheduledRunTime(entity.getScheduleMode().getNextRuntime(entity, new Date()));

		List<ReportEmailRecipient> recips = entity.getRecipients();
		Set<ReportEmailRecipient> matchedRecips = new HashSet<ReportEmailRecipient>();
		for (EmailTargetLine line : emailTargetLayout.getTargets())
		{
			// check if the recipient exists
			String email = (String) line.targetAddress.getValue();
			if (email != null && email.length() > 0)
			{
				// String email = (String)
				// item.getItemProperty("id").getValue();
				boolean found = false;
				for (ReportEmailRecipient recip : recips)
				{
					if (recip.getEmail() != null && recip.getEmail().equalsIgnoreCase(email))
					{
						found = true;
						recip.setVisibility((ReportEmailRecipientVisibility) line.targetTypeCombo.getValue());
						matchedRecips.add(recip);
						break;
					}
				}
				// if not then add them
				if (!found)
				{
					ReportEmailRecipient reportEmailRecipient = new ReportEmailRecipient();
					reportEmailRecipient.setEmail(email);
					reportEmailRecipient
							.setVisibility((ReportEmailRecipientVisibility) line.targetTypeCombo.getValue());

					recips.add(reportEmailRecipient);
					matchedRecips.add(reportEmailRecipient);
				}
			}
		}
		recips.clear();
		recips.addAll(matchedRecips);
		if (recips.size() == 0)
		{
			throw new InvalidValueException("Select at least one Recipient");
		}

		saveChangesToReportParameters(entity);

	}

	/**
	 * called after a record has been committed to the database
	 */
	@Override
	protected void postSaveAction(ReportEmailScheduleEntity entityItem)
	{
		removeDeletedRecipients(entityItem);

		saveChangesToReportParameters(entityItem);
		for (EntityParamUpdater updater : updaters)
		{
			for (ScheduledDateParameter dateParam : entityItem.getDateParameters())
			{
				if (dateParam.getLabel().equalsIgnoreCase(updater.name))
				{
					// merge date & time
					Date startDate = updater.startDateField.getValue();
					Date startTime = updater.startTimePicker.getValue();

					Calendar startDateCal = Calendar.getInstance();
					startDateCal.setTime(startDate);
					Calendar startTimeCal = Calendar.getInstance();
					startTimeCal.setTime(startTime);
					startDateCal.set(Calendar.HOUR_OF_DAY, startTimeCal.get(Calendar.HOUR_OF_DAY));
					startDateCal.set(Calendar.MINUTE, startTimeCal.get(Calendar.MINUTE));
					startDateCal.set(Calendar.SECOND, 0);
					startDateCal.set(Calendar.MILLISECOND, 0);

					dateParam.setStartDate(startDateCal.getTime());

					// merge date & time
					Date endDate = updater.endDateField.getValue();
					Date endTime = updater.endTimePicker.getValue();

					Calendar endDateCal = Calendar.getInstance();
					endDateCal.setTime(endDate);
					Calendar endTimeCal = Calendar.getInstance();
					endTimeCal.setTime(endTime);
					endDateCal.set(Calendar.HOUR_OF_DAY, startTimeCal.get(Calendar.HOUR_OF_DAY));
					endDateCal.set(Calendar.MINUTE, startTimeCal.get(Calendar.MINUTE));
					endDateCal.set(Calendar.SECOND, 0);
					endDateCal.set(Calendar.MILLISECOND, 0);

					dateParam.setEndDate(endDateCal.getTime());

					dateParam.setOffsetType((DateParameterOffsetType) updater.offsetType.getValue());

					break;
				}
			}
		}

		EntityManagerProvider.merge(entityItem);

	}

	private void removeDeletedRecipients(ReportEmailScheduleEntity entityItem)
	{
		List<ReportEmailRecipient> toRemove = new LinkedList<ReportEmailRecipient>();
		for (ReportEmailRecipient recip : entityItem.getRecipients())
		{
			boolean found = false;
			for (EmailTargetLine line : emailTargetLayout.getTargets())
			{
				String email = (String) line.targetAddress.getValue();

				if (email != null && email.equalsIgnoreCase(recip.getEmail()))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				toRemove.add(recip);
			}
		}

		entityItem.getRecipients().removeAll(toRemove);

	}

	private void saveChangesToReportParameters(ReportEmailScheduleEntity entityItem)
	{
		for (ReportParameter<?> bParam : builder.getReportParameters())
		{
			boolean set = false;
			for (ReportEmailParameter eParam : entityItem.getReportParameters())
			{
				for (String parameterName : bParam.getParameterNames())
				{
					if (parameterName.equalsIgnoreCase(eParam.getName()))
					{
						eParam.setValue((String) bParam.getValue(parameterName), bParam.getDisplayValue(parameterName));
						set = true;

						changedReportProperties(entityItem, bParam);

						break;
					}
				}
			}
			if (!set && !bParam.isDateField())
			{
				// add missing parameter

				ReportEmailParameterEntity reportEmailParameterEntity = new ReportEmailParameterEntity();
				reportEmailParameterEntity.setLabel(bParam.getLabel());

				String[] names = bParam.getParameterNames().toArray(new String[]
				{});
				reportEmailParameterEntity.setName(names[0]);
				reportEmailParameterEntity.setValue((String) bParam.getValue(names[0]),
						bParam.getDisplayValue(names[0]));

				changedReportProperties(entityItem, bParam);

				EntityManagerProvider.getEntityManager().persist(reportEmailParameterEntity);
				entityItem.addReportParameter(reportEmailParameterEntity);
			}
		}
	}

	private void changedReportProperties(ReportEmailScheduleEntity entityItem, ReportParameter<?> bParam)

	{
		try
		{
			if (bParam instanceof ReportChooser)
			{
				ReportChooser chooser = (ReportChooser) bParam;
				JasperReportProperties props = entityItem.getJasperReportPropertiesClass().newInstance();
				JasperReportProperties newProps = chooser.getReportProperties(props);

				entityItem.setReportFilename(newProps.getReportFileName());
				entityItem.setTitle(newProps.getReportTitle());
				reportTitle.setReadOnly(false);
				reportTitle.setValue(newProps.getReportTitle());
				reportTitle.setReadOnly(true);
			}
		}
		catch (Exception e)
		{
			logger.error(e, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Filter getContainerFilter(String filterText, boolean advancedSearchActive)
	{
		Filter filter = null;
		String[] searchFields = new String[]
		{ ReportEmailScheduleEntity_.subject.getName() };
		for (String property : searchFields)
		{
			if (filter == null)
			{
				filter = new SimpleStringFilter(property, filterText, true, false);
			}
			filter = new Or(new SimpleStringFilter(property, filterText, true, false), filter);
		}

		return filter;
	}

	/**
	 * Overload this method to provide cross-field (form level) validation.
	 *
	 * @return
	 */
	@Override
	protected void formValidate() throws InvalidValueException
	{
		if (builder != null)
		{
			for (ReportParameter<?> builtParam : builder.getReportParameters())
			{
				if (!builtParam.validate())

				{
					throw new InvalidValueException(builtParam.getLabel() + " is invalid");
				}
			}
		}
	}

	@Override
	public void enter(ViewChangeEvent event)
	{

	}

	@Override
	public String getTitleText()
	{
		return "Report Scheduler";
	}

	@Override
	public Enum<?> getHelpId()
	{
		return VaadinUtilsHelpEnum.VAADIN_UTILS_HELP_REPORT_SCHEDULER;
	}

}
