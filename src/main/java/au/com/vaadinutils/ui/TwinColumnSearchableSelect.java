package au.com.vaadinutils.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.HeadingPropertySet;
import au.com.vaadinutils.crud.SearchableSelectableEntityTable;
import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.dao.NullFilter;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.util.DefaultQueryModifierDelegate;
import com.vaadin.data.Buffered;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Not;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;

/**
 * @deprecated Replaced by {@link TwinColumnSelect}
 */
@Deprecated
public class TwinColumnSearchableSelect<C extends CrudEntity> extends CustomField<Collection<C>>
{

	private static final long serialVersionUID = -4316521010865902678L;
	private Logger logger = LogManager.getLogger();

	private SingularAttribute<C, ?> listField;
	protected Collection<C> sourceValue;
	@SuppressWarnings("rawtypes")
	private Class<? extends Collection> valueClass;
	protected SearchableSelectableEntityTable<C> availableTable;
	protected JPAContainer<C> availableContainer;
	protected Table selectedTable;
	protected BeanContainer<Long, C> beans;
	private SingularAttribute<C, Long> beanIdField;

	protected HorizontalLayout mainLayout;
	protected Button addNewButton = new Button(FontAwesome.PLUS);
	protected Button addButton = new Button(">");
	protected Button removeButton = new Button("<");
	protected Button removeAllButton = new Button("<<");
	protected Button addAllButton = new Button(">>");

	protected Filter baselineFilter;
	protected Filter selectedFilter;

	protected String availableColumnHeader;
	private String itemLabel;

	protected ValueChangeListener<C> listener;
	private CreateNewCallback<C> createNewCallback;

	private boolean isAscending;
	private boolean showAddRemoveAll;
	protected boolean isRemoveAllClicked = false;

	private static final float BUTTON_LAYOUT_WIDTH = 50;
	private static final float BUTTON_WIDTH = 45;

	/**
	 * Unfortunately TwinColumnSelect wont work with large sets, it isn't
	 * searchable and it doesn't lazy load, it also isn't sortable.
	 *
	 * Hopefully I'll address all of these issues here.
	 */

	public TwinColumnSearchableSelect(String fieldName, SingularAttribute<C, ?> listField)
	{
		this(fieldName, listField, null, true);
	}

	public TwinColumnSearchableSelect(String fieldName, SingularAttribute<C, ?> listField, boolean isAscending)
	{
		this(fieldName, listField, null, isAscending);
	}

	public TwinColumnSearchableSelect(final String fieldName, final SingularAttribute<C, ?> listField,
			final String itemLabel)
	{
		this(fieldName, listField, itemLabel, true);
	}

	public TwinColumnSearchableSelect(final String fieldName, final SingularAttribute<C, ?> listField,
			String itemLabel, final boolean isAscending)
	{
		this.setCaption(fieldName);
		mainLayout = new HorizontalLayout();

		this.isAscending = isAscending;
		this.listField = listField;
		beans = new BeanContainer<Long, C>(listField.getDeclaringType().getJavaType());
		Metamodel metaModel = EntityManagerProvider.getEntityManager().getMetamodel();
		EntityType<C> type = metaModel.entity(listField.getDeclaringType().getJavaType());
		beanIdField = type.getDeclaredId(Long.class);
		availableContainer = JpaBaseDao.getGenericDao(listField.getDeclaringType().getJavaType())
				.createVaadinContainer();
		availableContainer.sort(new Object[]
		{ listField.getName() }, new boolean[]
		{ isAscending });

		if (itemLabel == null)
		{
			itemLabel = listField.getName();
		}
		this.itemLabel = itemLabel;
		selectedTable = new Table();

		selectedTable.setContainerDataSource(createBeanContainer());
		if (!selectedTable.getContainerPropertyIds().contains(itemLabel))
		{
			logger.error("you need to define a getter for the field {} in {}, valid fields are {}", itemLabel,
					listField.getDeclaringType().getJavaType(),
					Arrays.toString(selectedTable.getContainerPropertyIds().toArray()));
		}

		selectedTable.addItemClickListener(new ItemClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void itemClick(ItemClickEvent event)
			{
				if (event.isDoubleClick())
					removeButton.click();
			}
		});

		selectedTable.setVisibleColumns(itemLabel);
		selectedTable.setColumnHeaders("Selected");

		selectedTable.setWidth(200, Unit.PIXELS);
		selectedTable.setHeight(300, Unit.PIXELS);
		selectedTable.setSelectable(true);
		selectedTable.setMultiSelect(true);
		// setting value of header here so that subclasses can
		// modify header if needed
		setAvailableColumnHeader(assignAvailableHeaderValue());
		createAvailableTable();

		addSelectedColumnTooltip();
		addNewButton.setVisible(false);
		refreshSelected();
	}

	public void allowAddNew(CreateNewCallback<C> createNewCallback)
	{
		addNewButton.setVisible(true);
		this.createNewCallback = createNewCallback;
	}

	public interface ValueChangeListener<C>
	{
		void valueChanged(Collection<C> value);
	}

	@Override
	@Deprecated
	public void addValueChangeListener(Property.ValueChangeListener listener)
	{

	}

	public void addValueChangeListener(ValueChangeListener<C> listener)
	{

		this.listener = listener;

	}

	@Override
	public void setHeight(String height)
	{
		super.setHeight(height);
		selectedTable.setHeight(height);
		availableTable.setHeight(height);
		mainLayout.setHeight(height);
	}

	@Override
	protected Component initContent()
	{
		mainLayout.addComponent(availableTable);
		mainLayout.addComponent(buildButtons());
		mainLayout.addComponent(selectedTable);
		mainLayout.setExpandRatio(availableTable, 1);
		mainLayout.setExpandRatio(selectedTable, 1);

		return mainLayout;
	}

	public void setSelectedColumnGenerator(ColumnGenerator generatedColumn)
	{
		selectedTable.addGeneratedColumn(itemLabel, generatedColumn);
	}

	private void createAvailableTable()
	{
		availableTable = new SearchableSelectableEntityTable<C>(this.getClass().getSimpleName())
		{
			private static final long serialVersionUID = 1L;

			@Override
			public HeadingPropertySet<C> getHeadingPropertySet()
			{
				return new HeadingPropertySet.Builder<C>().addColumn(availableColumnHeader, itemLabel).build();
			}

			@Override
			public Filterable getContainer()
			{
				return availableContainer;
			}

			@Override
			protected Filter getContainerFilter(String filterString, boolean advancedSearchActive)
			{
				Filter searchFilter = null;

				if (filterString != null && filterString.length() > 0)
					searchFilter = getSearchFilter(filterString);

				return NullFilter.and(baselineFilter, selectedFilter, searchFilter);
			}

			@Override
			protected String getTitle()
			{
				return "";
			}
		};

		availableTable.addItemClickListener(new ItemClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void itemClick(ItemClickEvent event)
			{
				if (event.isDoubleClick())
					addButton.click();
			}
		});

		availableTable.disableSelectable();
		availableTable.setWidth(200, Unit.PIXELS);
		availableTable.setHeight(300, Unit.PIXELS);
	}

	private Component buildButtons()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth(BUTTON_LAYOUT_WIDTH, Unit.PIXELS);

		removeButton.setWidth(BUTTON_WIDTH, Unit.PIXELS);
		removeAllButton.setWidth(BUTTON_WIDTH, Unit.PIXELS);
		addButton.setWidth(BUTTON_WIDTH, Unit.PIXELS);
		addAllButton.setWidth(BUTTON_WIDTH, Unit.PIXELS);
		addNewButton.setWidth(BUTTON_WIDTH, Unit.PIXELS);

		removeButton.addClickListener(removeClickListener());
		removeAllButton.addClickListener(removeAllClickListener());
		addButton.addClickListener(addClickListener());
		addAllButton.addClickListener(addAllClickListener());
		addNewButton.addClickListener(addNewClickListener());

		layout.addComponent(addButton);
		layout.addComponent(removeButton);
		layout.addComponent(addAllButton);
		layout.addComponent(removeAllButton);
		layout.addComponent(addNewButton);

		layout.setComponentAlignment(removeButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(addButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(removeAllButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(addAllButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(addNewButton, Alignment.MIDDLE_CENTER);

		return layout;

	}

	private BeanContainer<Long, C> createBeanContainer()
	{
		beans.setBeanIdProperty(beanIdField.getName());
		return beans;

	}

	@SuppressWarnings("unchecked")
	@Override
	public void commit() throws Buffered.SourceException, InvalidValueException
	{
		super.commit();
		Collection<C> tmp = (Collection<C>) getConvertedValue();

		// avoid possible npe
		if (sourceValue == null)
		{
			sourceValue = tmp;
		}

		// add missing
		for (C c : tmp)
		{
			if (!sourceValue.contains(c))
			{
				sourceValue.add(c);
			}
		}

		// remove unneeded
		Set<C> toRemove = new HashSet<>();
		for (C c : sourceValue)
		{
			if (!tmp.contains(c))
			{
				toRemove.add(c);
			}
		}
		sourceValue.removeAll(toRemove);

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isModified()
	{
		Collection<C> convertedValue = (Collection<C>) getConvertedValue();
		Preconditions.checkNotNull(convertedValue,
				"If you look at getConvertedValue, you'll see convertedValue can never be null");

		if ((sourceValue == null || sourceValue.size() == 0) && (convertedValue.size() > 0))
			return true;
		if ((sourceValue == null || sourceValue.size() == 0) && (convertedValue.size() == 0))
			return false;
		boolean equal = convertedValue.containsAll(sourceValue) && sourceValue.containsAll(convertedValue);
		return !equal;
	}

	@Override
	public void setReadOnly(boolean b)
	{
		selectedTable.setReadOnly(b);
		super.setReadOnly(b);

		// hide the add/remove and available list
		setAddButtonVisibility(!b);
		removeButton.setVisible(!b);
		if (showAddRemoveAll)
		{
			addAllButton.setVisible(!b);
			removeAllButton.setVisible(!b);
		}
		availableTable.setVisible(!b);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setInternalValue(Collection<C> newValue)
	{
		if (newValue != null)
		{
			valueClass = newValue.getClass();
		}
		super.setInternalValue(newValue);

		beans.removeAllItems();
		if (newValue != null)
		{
			beans.addAll(newValue);
		}
		sourceValue = (Collection<C>) getConvertedValue();
		beans.sort(new Object[]
		{ listField.getName() }, new boolean[]
		{ isAscending });

		refreshSelected();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<C> getValue()
	{
		return (Collection<C>) getConvertedValue();
	}

	@SuppressWarnings("unchecked")
	public Collection<C> getFieldValue()
	{
		return (Collection<C>) getConvertedValue();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<C> getInternalValue()
	{
		return (Collection<C>) getConvertedValue();
	}

	@Override
	public Object getConvertedValue()
	{
		Collection<C> selected;
		if (valueClass != null && List.class.isAssignableFrom(valueClass))
		{
			selected = new LinkedList<>();
		}
		else
		{
			selected = new HashSet<>();
		}

		// Just to be clear that this method will NEVER return null
		Preconditions.checkNotNull(selected, "If you look at getConvertedValue, you'll see this can never be null");

		if (beans != null)
		{
			for (Long id : beans.getItemIds())
			{
				selected.add(beans.getItem(id).getBean());
			}
		}

		return selected;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Collection<C>> getType()
	{
		// Had to remove this as maven can't compile it.
		// return (Class<? extends Collection<C>>) a.getClass();

		return (Class<Collection<C>>) (Class<?>) Collection.class;
	}

	public void setFilter(Filter filter)
	{
		baselineFilter = filter;
		availableContainer.setFireContainerItemSetChangeEvents(false);
		availableContainer.removeAllContainerFilters();
		availableContainer.setFireContainerItemSetChangeEvents(true);
		availableContainer.addContainerFilter(filter);
	}

	private void refreshSelected()
	{
		final List<Long> selectedIds = beans.getItemIds();
		if (selectedIds.size() == 1)
		{
			selectedFilter = new Not(new Compare.Equal(beanIdField.getName(), selectedIds.get(0)));
			availableTable.triggerFilter();
			return;
		}

		final Vector<Filter> filters = new Vector<>();
		for (Long id : selectedIds)
		{
			filters.add(new Compare.Equal(beanIdField.getName(), id));
		}
		selectedFilter = new Not(new Or(filters.toArray(new Filter[filters.size()])));
		availableTable.triggerFilter();
	}

	public void setFilterDelegate(DefaultQueryModifierDelegate defaultQueryModifierDelegate)
	{
		availableContainer.setQueryModifierDelegate(defaultQueryModifierDelegate);

	}

	/**
	 * defaults to true (visible)
	 *
	 * @param show
	 */
	public void showAddAndRemoveAllButtons(boolean show)
	{
		showAddRemoveAll = false;
		addAllButton.setVisible(show);
		removeAllButton.setVisible(show);
	}

	public Collection<C> getSourceValue()
	{
		return sourceValue;
	}

	public boolean isRemoveAllowed()
	{
		return true;
	}

	protected Table getSelectedCols()
	{
		return selectedTable;
	}

	public void handleRemoveValidation()
	{

	}

	protected void handleAddAction(Long id)
	{
		JpaBaseDao<C, Long> dao = JpaBaseDao.getGenericDao(listField.getDeclaringType().getJavaType());
		C cust = dao.findById(id);
		if (cust != null)
		{
			beans.addBean(cust);
			if (listener != null)
			{
				listener.valueChanged(getFieldValue());
			}
		}
		refreshSelected();
	}

	public boolean isPreAddActionRequired()
	{
		return false;
	}

	public void handlePreAddAction(Long id)
	{

	}

	protected void addSelectedColumnTooltip()
	{

	}

	public void refreshSelectedColumn()
	{
		selectedTable.refreshRowCache();
	}

	protected void postRemoveAction()
	{

	}

	protected void postAddAction()
	{

	}

	protected JPAContainer<C> getAvailableContainer()
	{

		return availableContainer;
	}

	public void refreshAvailableContainer()
	{
		availableContainer.refresh();
	}

	protected void resetAvailableContainer(JPAContainer<C> newContainer)
	{
		this.availableContainer = newContainer;
	}

	public void resetSelected()
	{
		beans.removeAllItems();
		if (listener != null)
		{
			listener.valueChanged(getFieldValue());
		}
	}

	public void setSelectedColumnHeader(String header)
	{
		this.selectedTable.setColumnHeaders(header);
	}

	public String getAvailableColumnHeader()
	{
		return availableColumnHeader;
	}

	public void setAvailableColumnHeader(String availableColumnHeader)
	{
		this.availableColumnHeader = availableColumnHeader;
	}

	protected String assignAvailableHeaderValue()
	{
		return "Available";
	}

	protected ClickListener addAllClickListener()
	{
		return new ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event)
			{
				List<Long> ids = new LinkedList<>();
				ids.addAll((Collection<? extends Long>) availableTable.getContainer().getItemIds());

				for (Long id : ids)
				{
					JpaBaseDao<C, Long> dao = JpaBaseDao.getGenericDao(listField.getDeclaringType().getJavaType());
					C cust = dao.findById(id);
					if (cust != null)
					{
						beans.addBean(cust);
						if (listener != null)
						{
							listener.valueChanged(getFieldValue());
						}
					}
				}
				refreshSelected();
			}
		};
	}

	protected ClickListener addClickListener()
	{
		return new ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event)
			{
				List<Long> ids = new LinkedList<>();
				ids.addAll((Collection<Long>) availableTable.getSelectedItems());
				if (ids.size() > 0)
				{
					for (Long id : ids)
					{
						if (id != null)
						{
							if (isPreAddActionRequired())
								handlePreAddAction(id);
							else
								handleAddAction(id);

							postAddAction();
						}
					}
				}

				beans.sort(new Object[]
				{ listField.getName() }, new boolean[]
				{ isAscending });
			}
		};
	}

	protected ClickListener addNewClickListener()
	{
		return new ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				createNewCallback.createNew(new RefreshCallback()
				{
					@Override
					public void refresh()
					{
						availableContainer.refresh();
					}
				});
			}
		};
	}

	protected ClickListener removeClickListener()
	{
		return new ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event)
			{
				try
				{
					final List<Long> ids = new LinkedList<>();
					ids.addAll((Collection<? extends Long>) selectedTable.getValue());

					for (Long id : ids)
					{
						if (isRemoveAllowed())
						{
							beans.removeItem(id);
							if (listener != null)
							{
								listener.valueChanged(getFieldValue());
							}

							postRemoveAction();
							refreshSelected();
						}
						else
						{
							handleRemoveValidation();
						}
					}

				}
				catch (Exception e)
				{
					logger.error(e, e);
				}

			}
		};
	}

	protected ClickListener removeAllClickListener()
	{
		return new ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				try
				{
					isRemoveAllClicked = true;
					if (isRemoveAllowed())
					{
						beans.removeAllItems();
						if (listener != null)
						{
							listener.valueChanged(getFieldValue());
						}

						refreshSelected();
					}
					else
					{
						handleRemoveValidation();
					}
					isRemoveAllClicked = false;
				}
				catch (Exception e)
				{
					logger.error(e, e);
				}
			}
		};
	}

	protected void setAddButtonVisibility(boolean visible)
	{
		addButton.setVisible(visible);
	}

	public void setSizeFull()
	{
		super.setSizeFull();
		mainLayout.setSizeFull();
		selectedTable.setSizeFull();
		availableTable.setSizeFull();
	}

	protected Filter getSearchFilter(final String filterString)
	{
		return new SimpleStringFilter(listField.getName(), filterString, true, false);
	}

	@Override
	public void setWidth(float width, Unit unit)
	{
		super.setWidth(width, unit);

		if (mainLayout != null && selectedTable != null && availableTable != null)
		{
			mainLayout.setWidth(width, unit);

			selectedTable.setWidth(((width - 5) / 2) - (BUTTON_LAYOUT_WIDTH / 2), unit);
			availableTable.setWidth(((width - 5) / 2) - (BUTTON_LAYOUT_WIDTH / 2), unit);
		}

	}
}
