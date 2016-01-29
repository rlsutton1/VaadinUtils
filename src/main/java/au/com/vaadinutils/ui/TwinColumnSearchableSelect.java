package au.com.vaadinutils.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.util.DefaultQueryModifierDelegate;
import com.vaadin.data.Buffered;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;

public class TwinColumnSearchableSelect<C extends CrudEntity> extends CustomField<Collection<C>>
{

	private static final long serialVersionUID = -4316521010865902678L;
	private Logger logger = LogManager.getLogger();
	private SingularAttribute<C, ?> listField;
	private String itemLabel;

	protected Collection<C> sourceValue;
	protected Table selectedCols;
	protected BeanContainer<Long, C> beans;
	protected JPAContainer<C> availableContainer;
	protected SearchableSelectableEntityTable<C> available;
	private SingularAttribute<C, Long> beanIdField;
	protected Button addButton = new Button("<");
	protected Button removeButton = new Button(">");
	protected Button removeAllButton = new Button(">>");
	protected Button addAllButton = new Button("<<");
	protected Filter baselineFilter;
	protected HorizontalLayout mainLayout;
	protected ValueChangeListener<C> listener;
	protected Button addNewButton = new Button(FontAwesome.PLUS);
	private CreateNewCallback<C> createNewCallback;
	@SuppressWarnings("rawtypes")
	private Class<? extends Collection> valueClass;
	private boolean isAscending;
	private boolean showAddRemoveAll;
	protected String availableColumnHeader;

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

	public TwinColumnSearchableSelect(final String fieldName, final SingularAttribute<C, ?> listField, final String itemLabel)
	{
		this(fieldName, listField, itemLabel, true);
	}

	public TwinColumnSearchableSelect(final String fieldName, final SingularAttribute<C, ?> listField, String itemLabel, final boolean isAscending)
	{
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
		selectedCols = new Table();

		selectedCols.setContainerDataSource(createBeanContainer());
		if (!selectedCols.getContainerPropertyIds().contains(itemLabel))
		{
			logger.error("you need to define a getter for the field {} in {}, valid fields are {}", itemLabel,
					listField.getDeclaringType().getJavaType(),
					Arrays.toString(selectedCols.getContainerPropertyIds().toArray()));
		}

		selectedCols.setVisibleColumns(itemLabel);
		selectedCols.setColumnHeaders(fieldName);

		selectedCols.setSizeFull();
		selectedCols.setHeight("200");
		selectedCols.setSelectable(true);
		selectedCols.setMultiSelect(true);
		// setting value of header here so that subclasses can
		// modify header if needed
		setAvailableColumnHeader(assignAvailableHeaderValue());
		createAvailableTable();

		addSelectedColumnTooltip();
		addNewButton.setVisible(false);
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
		selectedCols.setHeight(height);
		available.setHeight(height);
		mainLayout.setHeight(height);
	}

	@Override
	protected Component initContent()
	{
		mainLayout.setSizeFull();

		mainLayout.addComponent(selectedCols);

		mainLayout.addComponent(buildButtons());

		mainLayout.addComponent(available);
		mainLayout.setExpandRatio(available, 1);
		mainLayout.setExpandRatio(selectedCols, 1);

		return mainLayout;
	}

	public void setSelectedColumnGenerator(ColumnGenerator generatedColumn)
	{
		selectedCols.addGeneratedColumn(itemLabel, generatedColumn);
	}

	private void createAvailableTable()
	{

		available = new SearchableSelectableEntityTable<C>(this.getClass().getSimpleName())
		{

			/**
			*
			*/
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
				Filter filter = null;
				if (filterString != null && filterString.length() > 0)
				{
					filter = new SimpleStringFilter(listField.getName(), filterString, true, false);
				}
				if (baselineFilter != null)
				{
					if (filter != null)
					{
						filter = new And(baselineFilter, filter);
					}
					else
					{
						filter = baselineFilter;
					}
				}
				return filter;
			}

			@Override
			protected String getTitle()
			{
				return "";
			}
		};
		available.disableSelectable();

		available.setSizeFull();
		available.setHeight("200");
	}

	private Component buildButtons()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setWidth("50");
		layout.setHeight("100");

		removeButton.addClickListener(removeClickListener());
		removeButton.setHeight("50");

		removeAllButton.addClickListener(removeAllClickListener());
		removeAllButton.setHeight("50");

		addButton.addClickListener(addClickListener());

		addAllButton.addClickListener(addAllClickListener());

		addNewButton.addClickListener(addNewClickListener());

		layout.addComponent(removeButton);
		layout.addComponent(addButton);
		layout.addComponent(removeAllButton);
		layout.addComponent(addAllButton);
		layout.addComponent(addNewButton);

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
		selectedCols.setReadOnly(b);
		super.setReadOnly(b);

		// hide the add/remove and available list
		addButton.setVisible(!b);
		removeButton.setVisible(!b);
		if (showAddRemoveAll)
		{
			addAllButton.setVisible(!b);
			removeAllButton.setVisible(!b);
		}
		available.setVisible(!b);
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
		availableContainer.removeAllContainerFilters();
		availableContainer.addContainerFilter(filter);

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
		return selectedCols;
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
		selectedCols.refreshRowCache();
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
		this.selectedCols.setColumnHeaders(header);
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
				beans.removeAllItems();
				List<Long> ids = new LinkedList<>();
				ids.addAll((Collection<? extends Long>) available.getContainer().getItemIds());

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
				ids.addAll((Collection<Long>) available.getSelectedItems());
				if (ids.size() > 0)
				{
					for (Long id : ids)
					{
						if (id != null)
						{
							if (isPreAddActionRequired())
							{
								handlePreAddAction(id);
							}
							else
							{
								handleAddAction(id);
							}

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
					ids.addAll((Collection<? extends Long>) selectedCols.getValue());

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
					beans.removeAllItems();
					if (listener != null)
					{
						listener.valueChanged(getFieldValue());
					}
				}
				catch (Exception e)
				{
					logger.error(e, e);
				}
			}
		};
	}
}
