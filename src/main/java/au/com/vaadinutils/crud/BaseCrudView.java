package au.com.vaadinutils.crud;

import javax.validation.ConstraintViolationException;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import au.com.vaadinutils.listener.ClickListenerLogged;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.PropertySetChangeEvent;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public abstract class BaseCrudView<E> extends VerticalLayout implements RowChangeListener<E>, Selected<E>
{

	private static Logger logger = Logger.getLogger(BaseCrudView.class);
	private static final long serialVersionUID = 1L;

	private boolean inNew = false;

	private TextField searchField = new TextField();
	private Button newButton = new Button("New");
	private Button deleteButton = new Button("Delete");
	private Button saveButton = new Button("Save");
	private Button cancelButton = new Button("Cancel");
	private Class<E> entityClass;

	public ValidatingFieldGroup<E> fieldGroup;

	private VerticalLayout mainEditPanel = new VerticalLayout();

	private E currentEntity;

	/*
	 * Any component can be bound to an external data source. This example uses
	 * just a dummy in-memory list, but there are many more practical
	 * implementations.
	 */
	private JPAContainer<E> container;

	/* User interface components are stored in session. */
	private EntityTable<E> entityTable;
	private VerticalLayout rightLayout;
	private AbstractLayout editor;
	private HorizontalSplitPanel splitPanel;
	private HorizontalLayout buttonLayout;
	private AbstractLayout advancedSearchLayout;
	private VerticalLayout searchLayout;
	private HorizontalLayout basicSearchLayout;
	private CheckBox advancedSearchButton;

	protected void init(Class<E> entityClass, JPAContainer<E> container, HeadingPropertySet<E> headings)
	{
		this.entityClass = entityClass;
		this.container = container;
		fieldGroup = new ValidatingFieldGroup<E>(container, entityClass);
		fieldGroup.setBuffered(true);

		entityTable = new EntityTable<E>(container, headings.getColumns());
		entityTable.setRowChangeListener(this);
		entityTable.setSortEnabled(true);
		
	

		initLayout();
		entityTable.init();
		initSearch();
		initButtons();
		this.setVisible(true);
		entityTable.select(entityTable.firstItemId());

	}

	/*
	 * build the button layout aned editor panel
	 */

	protected abstract AbstractLayout buildEditor(ValidatingFieldGroup<E> fieldGroup2);

	private void initLayout()
	{
		this.setSizeFull();

		splitPanel = new HorizontalSplitPanel();
		this.addComponent(splitPanel);
		this.setExpandRatio(splitPanel, 1);

		// Layout for the table
		VerticalLayout leftLayout = new VerticalLayout();

		// Start by defining the LHS which contains the table
		splitPanel.addComponent(leftLayout);
		searchLayout = new VerticalLayout();

		basicSearchLayout = new HorizontalLayout();
		basicSearchLayout.setSizeFull();
		searchLayout.addComponent(basicSearchLayout);

		leftLayout.addComponent(searchLayout);
		buildAdvancedSearch();
		basicSearchLayout.addComponent(searchField);
		basicSearchLayout.setExpandRatio(searchField, 1.0f);

		basicSearchLayout.addComponent(newButton);
		basicSearchLayout.setSpacing(true);
		leftLayout.addComponent(entityTable);
		leftLayout.setSizeFull();

		/*
		 * On the left side, expand the size of the contactList so that it uses
		 * all the space left after from bottomLeftLayout
		 */
		leftLayout.setExpandRatio(entityTable, 1);
		entityTable.setSizeFull();

		/*
		 * In the bottomLeftLayout, searchField takes all the width there is
		 * after adding addNewContactButton. The height of the layout is defined
		 * by the tallest component.
		 */
		searchLayout.setWidth("100%");
		searchField.setWidth("100%");
		basicSearchLayout.setExpandRatio(searchField, 1);

		// Now define the edit area
		rightLayout = new VerticalLayout();
		splitPanel.addComponent(rightLayout);

		buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");
		buttonLayout.addComponent(cancelButton);
		buttonLayout.addComponent(saveButton);
		buttonLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
		buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);
		buttonLayout.setHeight("40");

		/* Put a little margin around the fields in the right side editor */
		Panel scroll = new Panel();
		mainEditPanel.setMargin(true);
		mainEditPanel.setVisible(true);
		mainEditPanel.setSizeFull();
		scroll.setSizeFull();
		scroll.setContent(mainEditPanel);

		// Delete button
		HorizontalLayout deleteLayout = new HorizontalLayout();
		deleteLayout.setWidth("100%");
		deleteLayout.addComponent(deleteButton);
		deleteLayout.setComponentAlignment(deleteButton, Alignment.MIDDLE_RIGHT);
		deleteLayout.setHeight("30");

		rightLayout.addComponent(deleteLayout);
		rightLayout.addComponent(scroll);
		rightLayout.setExpandRatio(scroll, 1.0f);
		rightLayout.setSizeFull();
		rightLayout.setId("rightLayout");

		editor = buildEditor(fieldGroup);
		mainEditPanel.addComponent(editor);
		rightLayout.addComponent(buttonLayout);

		rightLayout.setVisible(false);
	}

	private void buildAdvancedSearch()
	{
		advancedSearchLayout = getAdvancedSearchLayout();
		if (advancedSearchLayout != null)
		{
			advancedSearchButton = new CheckBox("Advanced");

			advancedSearchButton.setImmediate(true);
			advancedSearchButton.addValueChangeListener(new ValueChangeListener()
			{

				/**
				 * 
				 */
				private static final long serialVersionUID = -4396098902592906470L;

				@Override
				public void valueChange(ValueChangeEvent arg0)
				{
					advancedSearchLayout.setVisible(advancedSearchButton.getValue());

					triggerFilter();

				}
			});

			basicSearchLayout.addComponent(advancedSearchButton);
			searchLayout.addComponent(advancedSearchLayout);
			advancedSearchLayout.setVisible(false);
		}
	}

	protected AbstractLayout getAdvancedSearchLayout()
	{
		// TODO Auto-generated method stub
		return null;
	}

	protected void showDelete(boolean show)
	{

		deleteButton.setVisible(show);
	}

	protected void showNew(boolean show)
	{

		newButton.setVisible(show);
	}

	protected void setSplitPosition(float pos)
	{
		splitPanel.setSplitPosition(pos);
	}

	private void initButtons()
	{
		newButton.addClickListener(new ClickListenerLogged()
		{
			private static final long serialVersionUID = 1L;

			public void clicked(ClickEvent event)
			{
				try
				{
					/*
					 * Rows in the Container data model are called Item. Here we
					 * add a new row in the beginning of the list.
					 */
					if (allowRowChange())
					{
						container.removeAllContainerFilters();
						inNew = true;
						EntityItem<E> entityItem = container.createEntityItem(entityClass.newInstance());
						rowChanged(entityItem);

						rightLayout.setVisible(true);
					}
				}
				catch (ConstraintViolationException e)
				{
					FormHelper.showConstraintViolation(e);
				}
				catch (InstantiationException e)
				{
					throw new RuntimeException(e);
				}
				catch (IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}
			}

		});

		deleteButton.addClickListener(new ClickListener()
		{
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event)
			{
				Object contactId = entityTable.getValue();
				if (contactId != null)
				{
					E contact = container.getItem(contactId).getEntity();
					ConfirmDialog.show(UI.getCurrent(), "Delete Contact",
							"Are you sure you want to delete " + contact.toString(), "Delete", "Cancel",
							new ConfirmDialog.Listener()
							{
								private static final long serialVersionUID = 1L;

								@Override
								public void onClose(ConfirmDialog dialog)
								{
									if (dialog.isConfirmed())
									{
										Object contactId = entityTable.getValue();
										entityTable.removeItem(contactId);
										BaseCrudView.this.currentEntity = null;
										inNew = false;
									}
								}
							});
				}
			}
		});

		cancelButton.addClickListener(new ClickListener()
		{
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event)
			{
				fieldGroup.discard();
				if (inNew)
				{
					BaseCrudView.this.entityTable.select(null);
				}

				inNew = false;
				Notification.show("Changes discarded.", "Any changes you have made to this contact been discarded.",
						Type.TRAY_NOTIFICATION);
			}
		});

		saveButton.addClickListener(new ClickListener()
		{
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event)
			{
				save();

			}

		});

	}

	public E save()
	{
		try
		{
			commit();

			interceptSaveValues(BaseCrudView.this.currentEntity);

			if (inNew)
			{
				Long id = (Long) container.addEntity(BaseCrudView.this.currentEntity);
				BaseCrudView.this.entityTable.select(id);
				inNew = false;

			}

			// TODO: flush before announcing we've saved

			Notification.show("Changes Saved", "Any changes you have made have been saved.", Type.TRAY_NOTIFICATION);

		}
		catch (ConstraintViolationException e)
		{
			FormHelper.showConstraintViolation(e);
		}
		return BaseCrudView.this.currentEntity;
	}

	/**
	 * opportunity for implementing classes to modify or add data to the entity
	 * being saved
	 * 
	 * @param currentEntity2
	 */
	abstract protected void interceptSaveValues(E entity);

	private void initSearch()
	{

		/*
		 * We want to show a subtle prompt in the search field. We could also
		 * set a caption that would be shown above the field or description to
		 * be shown in a tooltip.
		 */
		searchField.setInputPrompt("Search");

		/*
		 * Granularity for sending events over the wire can be controlled. By
		 * default simple changes like writing a text in TextField are sent to
		 * server with the next Ajax call. You can set your component to be
		 * immediate to send the changes to server immediately after focus
		 * leaves the field. Here we choose to send the text over the wire as
		 * soon as user stops writing for a moment.
		 */
		searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		searchField.setImmediate(true);

		/*
		 * When the event happens, we handle it in the anonymous inner class.
		 * You may choose to use separate controllers (in MVC) or presenters (in
		 * MVP) instead. In the end, the preferred application architecture is
		 * up to you.
		 */
		searchField.addTextChangeListener(new TextChangeListener()
		{
			/**
                         *
                         */
			private static final long serialVersionUID = 1L;

			public void textChange(final TextChangeEvent event)
			{
				Filter filter = getContainerFilter(event.getText());
				if (advancedSearchButton.getValue())
				{
					filter = getAdvancedContainerFilter(filter);
				}
				applyFilter(filter);
			}

		});
	}

	/**
	 * call this method to cause filters to be applied
	 */
	protected void triggerFilter()
	{
		Filter filter = getContainerFilter(searchField.getValue());
		if (advancedSearchButton.getValue())
		{
			filter = getAdvancedContainerFilter(filter);
		}
		applyFilter(filter);
	}

	private void applyFilter(final Filter filter)
	{
		/* Reset the filter for the contactContainer. */
		container.removeAllContainerFilters();
		container.addContainerFilter(filter);
		container.discard();
		
		entityTable.select(entityTable.firstItemId());
		
	}

	protected String getSearchFieldText()
	{
		return searchField.getValue();
	}

	/**
	 * create a filter for the text supplied, the text is as entered in the text
	 * search bar.
	 * 
	 * @param string
	 * @return
	 */
	abstract protected Filter getContainerFilter(String string);

	/**
	 * to initiate advanced filtering call triggerFilter();
	 * 
	 * this method is only invoked when the advanced filter is visible. It is
	 * called, allowing the advanced filter to be added to the simple filter.
	 * 
	 * @param string
	 * @return
	 */
	protected Filter getAdvancedContainerFilter(Filter filter)
	{
		return filter;
	}

	@Override
	/** Called when the currently selected row in the 
	 *  table part of this view has changed.
	 *  We use this to update the editor's current item.
	 */
	public boolean allowRowChange()
	{

		if (fieldGroup.isModified() || inNew)
		{
			ConfirmDialog
					.show(UI.getCurrent(),
							"Discard changes?",
							"You have unsaved changes for this Contact. Continuing will result in those changes being discarded. ",
							"Continue", "Cancel", new ConfirmDialog.Listener()
							{
								private static final long serialVersionUID = 1L;

								public void onClose(ConfirmDialog dialog)
								{
									if (dialog.isConfirmed())
									{
										/*
										 * When a contact is selected from the
										 * list, we want to show that in our
										 * editor on the right. This is nicely
										 * done by the FieldGroup that binds all
										 * the fields to the corresponding
										 * Properties in our contact at once.
										 */
										fieldGroup.discard();
										inNew = false;

									}
									else
									{
										// User did not confirm so don't allow
										// the change.

									}
								}
							});
		}

		return true;

	}

	@Override
	/** Called when the currently selected row in the 
	 *  table part of this view has changed.
	 *  We use this to update the editor's current item.
	 */
	public void rowChanged(EntityItem<E> item)
	{

		// The contact is null if the row is de-selected
		if (item != null)
		{

			this.currentEntity = item.getEntity();
			fieldGroup.setItemDataSource(item);

			// removed as this doesn't make any sense particularly since the
			// property id is hard coded to'name'.
			// Preconditions.checkState(fieldGroup.getItemDataSource().getItemPropertyIds().contains("name"),
			// "valid listFieldNames are " +
			// fieldGroup.getItemDataSource().getItemPropertyIds().toString());

		}
		else
		{
			this.currentEntity = null;
			fieldGroup.setItemDataSource(null);
		}

		rightLayout.setVisible(this.currentEntity != null);

	}

	protected void commit()
	{
		try
		{
			if (!fieldGroup.isValid())
			{
				Notification.show("Validation Errors", "Please fix any field errors and try again.",
						Type.WARNING_MESSAGE);
			}
			else
			{
				fieldGroup.commit();

			}
		}
		catch (CommitException e)
		{
			Notification.show("Error saving changes.",
					"Any error occured attempting to save your changes: " + e.getMessage(), Type.ERROR_MESSAGE);
			logger.error(e, e);
		}

	}

	VerticalLayout getEmptyPanel()
	{
		VerticalLayout layout = new VerticalLayout();

		Label pleaseAdd = new Label(
				"Click the 'New' button to add new Contact or click an existing contact in the adjacent table to edit it.");
		layout.addComponent(pleaseAdd);
		layout.setComponentAlignment(pleaseAdd, Alignment.MIDDLE_CENTER);
		layout.setSizeFull();
		return layout;
	}

	@Override
	public E getCurrent()
	{
		return currentEntity;

	}

}
