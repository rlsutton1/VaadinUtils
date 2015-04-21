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
	private SingularAttribute<C, ?> listField;
	private String itemLabel;

	private Collection<C> sourceValue;
	private Table selectedCols;
	Logger logger = LogManager.getLogger();
	private BeanContainer<Long, C> beans;
	private JPAContainer<C> availableContainer;
	private SearchableSelectableEntityTable<C> available;
	private SingularAttribute<C, Long> beanIdField;
	private Button addButton = new Button("<");
	private Button removeButton = new Button(">");
	private Button removeAllButton = new Button(">>");
	private Button addAllButton = new Button("<<");
	private Filter baselineFilter;
	private HorizontalLayout mainLayout;
	private ValueChangeListener<C> listener;
	private Button addNewButton = new Button(FontAwesome.PLUS);
	private CreateNewCallback<C> createNewCallback;

	/**
	 * Unfortunately TwinColumnSelect wont work with large sets, it isn't
	 * searchable and it doesn't lazy load, it also isn't sortable.
	 * 
	 * Hopefully I'll address all of these issues here.
	 */

	public TwinColumnSearchableSelect(String fieldName, SingularAttribute<C, ?> listField)
	{
		this(fieldName, listField, null);
	}
	
	public TwinColumnSearchableSelect(String fieldName, SingularAttribute<C, ?> listField, String itemLabel)
	{
		beans = new BeanContainer<Long, C>(listField.getDeclaringType().getJavaType());

		mainLayout = new HorizontalLayout();

		this.listField = listField;
		Metamodel metaModel = EntityManagerProvider.getEntityManager().getMetamodel();
		EntityType<C> type = metaModel.entity(listField.getDeclaringType().getJavaType());
		beanIdField = type.getDeclaredId(Long.class);
		availableContainer = JpaBaseDao.getGenericDao(listField.getDeclaringType().getJavaType())
				.createVaadinContainer();
		availableContainer.sort(new Object[]
		{ listField.getName() }, new boolean[]
		{ true });

		if (itemLabel == null)
		{
			itemLabel = listField.getName();
		}
		this.itemLabel = itemLabel;
		selectedCols = new Table();
		selectedCols.setContainerDataSource(createBeanContainer());
		if (!selectedCols.getContainerPropertyIds().contains(itemLabel))
		{
			logger.error("you need to define a getter for the field {} in {}, valid fields are {}",
					itemLabel, listField.getDeclaringType().getJavaType(),
					Arrays.toString(selectedCols.getContainerPropertyIds().toArray()));
		}
		selectedCols.setVisibleColumns(itemLabel);
		selectedCols.setColumnHeaders(fieldName);
		selectedCols.setSizeFull();
		selectedCols.setHeight("200");
		selectedCols.setSelectable(true);
		createAvailableTable();

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
		selectedCols.addGeneratedColumn(listField.getName(), generatedColumn);
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
				return new HeadingPropertySet.Builder<C>().addColumn("Available", itemLabel).build();
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

		removeButton.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				try
				{
					Long id = (Long) selectedCols.getValue();
					beans.removeItem(id);
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
		});
		removeButton.setHeight("50");

		removeAllButton.addClickListener(new ClickListener()
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
		});
		removeAllButton.setHeight("50");

		addButton.addClickListener(new ClickListener()
		{

			/**
	     * 
	     */
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event)
			{
				List<Long> ids = new LinkedList<>();
				ids.addAll((Collection<? extends Long>) available.getSelectedItems());
				if (ids.size() > 0)
				{
					Long id = ids.get(0);
					if (id != null)
					{
						JpaBaseDao<C, Long> dao = (JpaBaseDao<C, Long>) JpaBaseDao.getGenericDao(listField
								.getDeclaringType().getJavaType());
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
			}
		});

		addAllButton.addClickListener(new ClickListener()
		{

			/**
	     * 
	     */
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
					JpaBaseDao<C, Long> dao = (JpaBaseDao<C, Long>) JpaBaseDao.getGenericDao(listField
							.getDeclaringType().getJavaType());
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
		});

		addNewButton.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 173977618488084577L;

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
		});

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
		if ((sourceValue == null || sourceValue.size() == 0) && (convertedValue != null && convertedValue.size() > 0))
			return true;
		if ((sourceValue == null || sourceValue.size() == 0) && (convertedValue == null || convertedValue.size() == 0))
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
		available.setVisible(!b);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setInternalValue(Collection<C> newValue)
	{
		super.setInternalValue(newValue);

		beans.removeAllItems();
		if (newValue != null)
		{
			beans.addAll(newValue);
		}
		sourceValue = (Collection<C>) getConvertedValue();
	}

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

	@SuppressWarnings("unchecked")
	public Collection<C> getInternalValue()
	{
		return (Collection<C>) getConvertedValue();
	}

	public Object getConvertedValue()
	{

		Collection<C> selected = new HashSet<>();
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
	public Class<? extends Collection<C>> getType()
	{
		return (Class<? extends Collection<C>>) Collection.class;
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
		addAllButton.setVisible(show);
		removeAllButton.setVisible(show);
	}

}
