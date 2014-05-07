package au.com.vaadinutils.jasper.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.FormHelper;
import au.com.vaadinutils.crud.HeadingPropertySet;
import au.com.vaadinutils.crud.ValidatingFieldGroup;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.jasper.filter.ExpanderComponent;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterOffsetType;
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
import au.com.vaadinutils.validator.EmailValidator;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/** A start view for navigating to the main view */
public class JasperReportScheduleLayout extends BaseCrudView<ReportEmailScheduleEntity>
{

	private final VerticalLayout recipientListHolder = new VerticalLayout();

	Logger logger = LogManager.getLogger();

	private VerticalLayout paramForm;

	private ReportFilterUIBuilder builder;

	private JasperReportProperties reportProperties;

	public JasperReportScheduleLayout()
	{
		JPAContainer<ReportEmailScheduleEntity> container = makeJPAContainer();

		HeadingPropertySet<ReportEmailScheduleEntity> headings = new HeadingPropertySet.Builder<ReportEmailScheduleEntity>()
				
				.addColumn("Report", ReportEmailScheduleEntity_.reportTitle)
				.addColumn("Subject", ReportEmailScheduleEntity_.subject)
				.addColumn("Owner", ReportEmailScheduleEntity_.sender)
				.addColumn("Enabled",ReportEmailScheduleEntity_.enabled)
				.addColumn("Last Run",ReportEmailScheduleEntity_.lastRuntime).build();

		init(ReportEmailScheduleEntity.class, container, headings);
		this.disallowNew(true);
	}

	public JasperReportScheduleLayout(Long id)
	{
		JPAContainer<ReportEmailScheduleEntity> container = makeJPAContainer();

		HeadingPropertySet<ReportEmailScheduleEntity> headings = new HeadingPropertySet.Builder<ReportEmailScheduleEntity>()
				.addColumn("Message", ReportEmailScheduleEntity_.message)
				.addColumn("Report", ReportEmailScheduleEntity_.reportFileName)
				.addColumn("Subject", ReportEmailScheduleEntity_.subject)
				.addColumn("Owner", ReportEmailScheduleEntity_.sender).build();

		init(ReportEmailScheduleEntity.class, container, headings);
		this.disallowNew(true);
		entityTable.select(id);
	}

	public JPAContainer<ReportEmailScheduleEntity> makeJPAContainer()
	{
		return getGenericDao(ReportEmailScheduleEntity.class).createVaadinContainer();

	}

	private <E> JpaBaseDao<E, Long> getGenericDao(Class<E> class1)
	{
		return new JpaBaseDao<E, Long>(class1);

	}

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	public TabSheet buildEditor(ValidatingFieldGroup<ReportEmailScheduleEntity> validatingFieldGroup)
	{
		TopVerticalLayout wrapper = new TopVerticalLayout();
		wrapper.setSizeFull();

		TabSheet tabsheet = new TabSheet();
		Tab emailTab = tabsheet.addTab(wrapper, "Email");

		FormLayout main = new FormLayout();
		main.setMargin(true);
		main.setSizeFull();

		recipientListHolder.setSizeFull();
		recipientListHolder.setSpacing(true);
		recipientListHolder.setMargin(new MarginInfo(true, true, false, true));

		main.addComponent(recipientListHolder);

		FormHelper<ReportEmailScheduleEntity> helper = new FormHelper<ReportEmailScheduleEntity>(main, fieldGroup);

		// MultiColumnFormLayout<ReportEmailScheduleEntity> layout = new
		// MultiColumnFormLayout<ReportEmailScheduleEntity>(
		// 1, validatingFieldGroup);
		// layout.setColumnFieldWidth(0, 250);

		wrapper.addComponent(main);
		// main.addComponent(wrapper);

		helper.bindTextField("Report", ReportEmailScheduleEntity_.reportTitle).setReadOnly(true);

		helper.bindEntityField("From", ReportEmailScheduleEntity_.sender, ReportEmailSender.class,
				ReportEmailSender_.username);

		helper.bindTextField("Subject", ReportEmailScheduleEntity_.subject);

		helper.bindTextAreaField("Message", ReportEmailScheduleEntity_.message.getName(), 5);

		helper.bindTextAreaField("Report Log", ReportEmailScheduleEntity_.reportLog.getName(), 10).setReadOnly(true);
		// layout.bindTextField("Class",
		// ReportEmailScheduleEntity_.JasperReportPropertiesClassName).setReadOnly(true);

		// layout.bindTextField("Report file name",
		// ReportEmailScheduleEntity_.reportFileName).setReadOnly(true);

		TopVerticalLayout scheduleWrapper = new TopVerticalLayout();
		scheduleWrapper.setSizeFull();
		tabsheet.addTab(scheduleWrapper, "Schedule");
		FormLayout scheduleForm = new FormLayout();
		scheduleForm.setSizeFull();
		scheduleWrapper.addComponent(scheduleForm);

		buildScheduleTab(validatingFieldGroup, scheduleForm, helper);

		TopVerticalLayout paramWrapper = new TopVerticalLayout();
		paramWrapper.setSizeFull();
		tabsheet.addTab(paramWrapper, "Params");
		paramForm = new VerticalLayout();
		paramForm.setSizeFull();
		paramWrapper.addComponent(paramForm);

		// TopVerticalLayout dateParamWrapper = new TopVerticalLayout();
		// dateParamWrapper.setSizeFull();
		// tabsheet.addTab(dateParamWrapper, "Dates Params");
		// dateParamForm = new VerticalLayout();
		// dateParamForm.setSizeFull();
		// dateParamWrapper.addComponent(dateParamForm);

		// layout.bindTextField("Days of Month",
		// ReportEmailScheduleEntity_.scheduledDaysOfMonth);
		// layout.bindTextField("Days of Week",
		// ReportEmailScheduleEntity_.scheduledDaysOfWeek);

		return tabsheet;

	}

	private void buildScheduleTab(ValidatingFieldGroup<ReportEmailScheduleEntity> validatingFieldGroup,
			FormLayout main, FormHelper<ReportEmailScheduleEntity> helper)
	{

		helper.bindBooleanField(main, validatingFieldGroup, "Enabled", ReportEmailScheduleEntity_.enabled);

		
		helper.bindDateField(main, validatingFieldGroup, "Last Run Time", ReportEmailScheduleEntity_.lastRuntime,
				"yyyy/MM/dd HH:mm", Resolution.MINUTE).setReadOnly(true);

		ComboBox modeCombo = helper.bindEnumField(main, validatingFieldGroup, "Mode",
				ReportEmailScheduleEntity_.scheduleMode, ScheduleMode.class);

		final DateField oneTime = helper.bindDateField(main, validatingFieldGroup, "Scheduled at",
				ReportEmailScheduleEntity_.oneTimeRunDateTime, "yyyy/MM/dd HH:mm", Resolution.MINUTE);
		// final DateField timeOfDay = helper.bindDateField("Schedule Time",
		// ReportEmailScheduleEntity_.timeOfDayToRun,
		// "yyyy/MM/dd HH:mm", Resolution.MINUTE);

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
				switch (mode)
				{
				case ONE_TIME:
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
	/** Called when the currently selected row in the 
	 *  table part of this view has changed.
	 *  We use this to update the editor's current item.
	 *  
	 *  @item the item that is now selected. This may be null if selection has been lost.
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
		private DateField dateField;
		private TimePicker timePicker;

		public EntityParamUpdater(String name, ComboBox offsetType, DateField dateField, TimePicker timePicker)
		{
			this.name = name;
			this.offsetType = offsetType;
			this.dateField = dateField;
			this.timePicker = timePicker;
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
				final ComboBox offsetType = new ComboBox(dateParam.getLabel(), offsetTypes);
				offsetType.setImmediate(true);
				offsetType.setNullSelectionAllowed(false);

				final DateField dateField = new DateField("", dateParam.getDate());
				dateField.setResolution(Resolution.DAY);
				dateField.setDateFormat("yyyy/MM/dd");
				final TimePicker timePicker = new TimePicker("");
				timePicker.setValues(dateParam.getDate());
				timePicker.setVisible(dateParam.getType() == DateParameterType.DATE_TIME);

				offsetType.addValueChangeListener(new ValueChangeListener()
				{

					private static final long serialVersionUID = 7081417825842355432L;

					@Override
					public void valueChange(ValueChangeEvent event)
					{
						DateParameterOffsetType offsetTypeValue = (DateParameterOffsetType) event.getProperty()
								.getValue();
						dateField.setVisible(offsetTypeValue == DateParameterOffsetType.CONSTANT);

					}
				});
				offsetType.setValue(dateParam.getOffsetType());
				dateLayout.addComponent(offsetType);
				dateLayout.addComponent(dateField);
				dateLayout.addComponent(timePicker);
				paramForm.addComponent(dateLayout);

				updaters.add(new EntityParamUpdater(dateParam.getName(), offsetType, dateField, timePicker));

			}
		}

	}

	private void paramsHandleRowChange(EntityItem<ReportEmailScheduleEntity> item)
	{

		try
		{
			builder = null;
			reportProperties = null;
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
					if (schedParam.getName().equalsIgnoreCase(builtParam.getParameterName()))
					{
						builtParam.setValueAsString(schedParam.getValue());
					}
				}

			}

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error(e, e);
		}

	}

	private void emailRecipientsHandleRowChange(EntityItem<ReportEmailScheduleEntity> item)
	{
		recipientListHolder.removeAllComponents();
		int ctr = 0;
		lines.clear();

		if (item != null)
		{
			ReportEmailScheduleEntity entity = item.getEntity();
			if (entity != null)
			{
				for (ReportEmailRecipient recip : entity.getRecipients())
				{
					ctr++;
					lines.add(insertTargetLine(lines.size(), recip));
				}
				if (ctr == 0)
				{
					lines.add(insertTargetLine(lines.size(), null));
				}
			}
		}
	}

	@Override
	protected void interceptSaveValues(EntityItem<ReportEmailScheduleEntity> entityItem)
	{
		ReportEmailScheduleEntity entity = entityItem.getEntity();
		List<ReportEmailRecipient> recips = entity.getRecipients();
		for (TargetLine line : lines)
		{
			// check if the recipient exists
			String email = (String) line.targetAddress.getValue();
			// String email = (String) item.getItemProperty("id").getValue();
			boolean found = false;
			for (ReportEmailRecipient recip : recips)
			{
				if (recip.getEmail() != null && recip.getEmail().equalsIgnoreCase(email))
				{
					found = true;
					break;
				}
			}
			// if not then add them
			if (!found)
			{
				ReportEmailRecipient reportEmailRecipient = new ReportEmailRecipient();
				reportEmailRecipient.setEmail(email);
				recips.add(reportEmailRecipient);
				reportEmailRecipient.setOwner(entity);
			}
		}

	}

	/**
	 * called after a record has been committed to the database
	 */
	protected void postSaveAction(ReportEmailScheduleEntity entityItem)
	{
		List<ReportEmailRecipient> toRemove = new LinkedList<ReportEmailRecipient>();
		for (ReportEmailRecipient recip : entityItem.getRecipients())
		{
			boolean found = false;
			for (TargetLine line : lines)
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
		for (ReportEmailRecipient recip : toRemove)
		{
			recip.setOwner(null);
		}

		saveChangesToReportParameters(entityItem);
		for (EntityParamUpdater updater : updaters)
		{
			for (ScheduledDateParameter dateParam : entityItem.getDateParameters())
			{
				if (dateParam.getName().equalsIgnoreCase(updater.name))
				{
					// merge date & time
					Date date = updater.dateField.getValue();
					Date time = (Date) updater.timePicker.getValue();

					Calendar dateCal = Calendar.getInstance();
					dateCal.setTime(date);
					Calendar timeCal = Calendar.getInstance();
					timeCal.setTime(time);
					dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
					dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
					dateCal.set(Calendar.SECOND, 0);
					dateCal.set(Calendar.MILLISECOND, 0);

					dateParam.setDate(dateCal.getTime());
					dateParam.setOffsetType((DateParameterOffsetType) updater.offsetType.getValue());

					break;
				}
			}
		}

	}

	private void saveChangesToReportParameters(ReportEmailScheduleEntity entityItem)
	{
		for (ReportParameter<?> bParam : builder.getReportParameters())
		{
			for (ReportEmailParameter eParam : entityItem.getReportParameters())
			{
				if (bParam.getParameterName().equalsIgnoreCase(eParam.getName()))
				{
					eParam.setValue((String) bParam.getValue());
					break;
				}
			}
		}
	}

	class TargetLine
	{
		ComboBox targetTypeCombo;
		ComboBox targetAddress;
		Button actionButton;
		public int row;
	}

	List<TargetLine> lines = new LinkedList<TargetLine>();

	private TargetLine insertTargetLine(final int row, ReportEmailRecipient recip)
	{

		final HorizontalLayout recipientHolder = new HorizontalLayout();
		recipientHolder.setSizeFull();
		recipientHolder.setSpacing(true);

		final List<ReportEmailRecipientVisibility> targetTypes = new LinkedList<ReportEmailRecipientVisibility>();
		for (ReportEmailRecipientVisibility rerv : ReportEmailRecipientVisibility.values())
		{
			targetTypes.add(rerv);
		}

		final TargetLine line = new TargetLine();
		line.row = row;

		line.targetTypeCombo = new ComboBox(null, targetTypes);
		line.targetTypeCombo.setWidth("60");
		line.targetTypeCombo.select(targetTypes.get(0));

		line.targetAddress = new ComboBox(null);
		line.targetAddress.setImmediate(true);
		line.targetAddress.setTextInputAllowed(true);
		line.targetAddress.setInputPrompt("Enter Contact Name or email address");
		line.targetAddress.setWidth("100%");
		line.targetAddress.addValidator(new EmailValidator("Please enter a valid email address."));

		line.targetAddress.setContainerDataSource(getValidEmailContacts());
		line.targetAddress.setItemCaptionPropertyId("namedemail");
		line.targetAddress.setNewItemsAllowed(true);
		if (recip != null && recip.getEmail() != null)
		{
			line.targetAddress.setValue(recip.getEmail());
		}

		line.targetAddress.setNewItemHandler(new NewItemHandler()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void addNewItem(final String newItemCaption)
			{
				final IndexedContainer container = (IndexedContainer) line.targetAddress.getContainerDataSource();

				final Item item = addItem(container, "", newItemCaption);
				if (item != null)
				{
					line.targetAddress.addItem(item.getItemProperty("id").getValue());
					line.targetAddress.setValue(item.getItemProperty("id").getValue());
				}
			}
		});

		if (recip != null)
		{

		}

		if (row == 0)
		{
			line.actionButton = new Button("+");
			line.actionButton.setDescription("Click to add another email address line.");
			line.actionButton.setStyleName(Reindeer.BUTTON_SMALL);
			line.actionButton.addClickListener(new ClickListener()
			{

				private static final long serialVersionUID = 6505218353927273720L;

				@Override
				public void buttonClick(ClickEvent event)
				{
					lines.add(insertTargetLine(lines.size(), null));
				}
			});
		}
		else
		{
			line.actionButton = new Button("-");
			line.actionButton.setDescription("Click to remove this email address line.");
			line.actionButton.setStyleName(Reindeer.BUTTON_SMALL);
			line.actionButton.addClickListener(new ClickListener()
			{

				private static final long serialVersionUID = 3104323607502279386L;

				@Override
				public void buttonClick(ClickEvent event)
				{
					recipientListHolder.removeComponent(recipientHolder);
					lines.remove(line);

				}
			});
		}

		recipientHolder.addComponent(line.targetTypeCombo);
		recipientHolder.addComponent(line.targetAddress);
		recipientHolder.addComponent(line.actionButton);
		recipientHolder.setExpandRatio(line.targetAddress, 1);

		recipientListHolder.addComponent(recipientHolder);

		return line;
	}

	private IndexedContainer getValidEmailContacts()
	{
		final IndexedContainer container = new IndexedContainer();

		JpaBaseDao<ReportEmailRecipient, Long> reportEmailRecipient = getGenericDao(ReportEmailRecipient.class);

		container.addContainerProperty("id", String.class, null);
		container.addContainerProperty("email", String.class, null);
		container.addContainerProperty("namedemail", String.class, null);

		for (final ReportEmailRecipient contact : reportEmailRecipient.findAll())
		{
			if (contact.getEmail() != null)
			{
				addItem(container, null, contact.getEmail());
			}
		}
		return container;
	}

	@SuppressWarnings("unchecked")
	private Item addItem(final IndexedContainer container, final String named, String email)
	{
		// When we are editing an email (as second time) we can end up with
		// double brackets so we strip them off here.
		if (email.startsWith("<"))
		{
			email = email.substring(1);
		}
		if (email.endsWith(">"))
		{
			email = email.substring(0, email.length() - 1);
		}

		final Item item = container.addItem(email);
		if (item != null)
		{
			item.getItemProperty("id").setValue(email);
			item.getItemProperty("email").setValue(email);
			String namedEmail;
			if (named != null && named.trim().length() > 0)
			{
				namedEmail = named + " <" + email + ">";
			}
			else
			{
				namedEmail = "<" + email + ">";
			}
			item.getItemProperty("namedemail").setValue(namedEmail);
		}
		return item;
	}

	@Override
	protected Filter getContainerFilter(String filterText, boolean advancedSearchActive)
	{
		Filter filter = null;
		String[] searchFields = new String[] { ReportEmailScheduleEntity_.subject.getName() };
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

	@Override
	protected String getTitleText()
	{
		return "Report email schedule";
	}

}
