package au.com.vaadinutils.crud;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import au.com.vaadinutils.listener.ClickEventLogged;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
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

public abstract class BaseCrudView<E extends CrudEntity> extends VerticalLayout implements RowChangeListener<E>,
		Selected<E>
{

	private static Logger logger = Logger.getLogger(BaseCrudView.class);
	private static final long serialVersionUID = 1L;

	protected EntityItem<E> newEntity = null;
	/**
	 * When we enter inNew mode we need to hide the delete button. When we exit
	 * inNew mode thsi var is used to determine if we need to restore the delete
	 * button. i.e. if it wasn't visible before 'new' we shouldn't make it
	 * visible now.
	 */
	private boolean restoreDelete;

	private TextField searchField = new TextField();
	private Button newButton = new Button("New");
	private Button deleteButton = new Button("Delete");
	private Button saveButton = new Button("Save");
	private Button cancelButton = new Button("Cancel");
	private Class<E> entityClass;

	public ValidatingFieldGroup<E> fieldGroup;

	private VerticalLayout mainEditPanel = new VerticalLayout();

	// private E currentEntity;

	/*
	 * Any component can be bound to an external data source. This example uses
	 * just a dummy in-memory list, but there are many more practical
	 * implementations.
	 */
	private JPAContainer<E> container;

	/* User interface components are stored in session. */
	private EntityList<E> entityTable;
	private VerticalLayout rightLayout;
	private AbstractLayout editor;
	private HorizontalSplitPanel splitPanel;
	private HorizontalLayout buttonLayout;
	private AbstractLayout advancedSearchLayout;
	private VerticalLayout searchLayout;
	private CheckBox advancedSearchButton;

	protected void init(Class<E> entityClass, JPAContainer<E> container, HeadingPropertySet<E> headings)
	{
		this.entityClass = entityClass;
		this.container = container;
		try
		{
			container.setBuffered(true);
			container.setFireContainerItemSetChangeEvents(true);
			container.setAutoCommit(true);


		}
		catch (Exception e)
		{
			logger.error(" ******* when constructing a jpaContainer for use with the BaseCrudView use JPAContainerFactory.makeBatchable ****** ");
			logger.error(e, e);
			throw new RuntimeException(e);
		}
		fieldGroup = new ValidatingFieldGroup<E>(container, entityClass);
		fieldGroup.setBuffered(true);

		entityTable = getTable(container, headings);
		entityTable.setRowChangeListener(this);
		entityTable.setSortEnabled(true);

		initLayout();
		entityTable.init();
		initSearch();
		initButtons();
		this.setVisible(true);
		entityTable.select(entityTable.firstItemId());

	}

	protected EntityList<E> getTable(JPAContainer<E> container, HeadingPropertySet<E> headings)
	{
		return new EntityTable<E>(container, headings);
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
		searchLayout.setWidth("100%");
		searchField.setWidth("100%");
		leftLayout.addComponent(searchLayout);

		buildSearchBar();

		leftLayout.addComponent(entityTable);
		leftLayout.setSizeFull();

		/*
		 * On the left side, expand the size of the contactList so that it uses
		 * all the space left after from bottomLeftLayout
		 */
		leftLayout.setExpandRatio(entityTable, 1);
		entityTable.setSizeFull();

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
		// mainEditPanel.setDescription("BaseCrud:MainEditPanel");
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

	private void buildSearchBar()
	{
		HorizontalLayout basicSearchLayout = new HorizontalLayout();
		basicSearchLayout.setSizeFull();
		basicSearchLayout.setSpacing(true);
		searchLayout.addComponent(basicSearchLayout);

		AbstractLayout advancedSearch = buildAdvancedSearch();
		if (advancedSearch != null)
		{
			basicSearchLayout.addComponent(advancedSearchButton);
		}

		basicSearchLayout.addComponent(searchField);
		basicSearchLayout.setExpandRatio(searchField, 1.0f);

		Button clear = createClearButton();
		basicSearchLayout.addComponent(clear);

		basicSearchLayout.addComponent(newButton);
		basicSearchLayout.setSpacing(true);

		/*
		 * In the bottomLeftLayout, searchField takes all the width there is
		 * after adding addNewContactButton. The height of the layout is defined
		 * by the tallest component.
		 */
		basicSearchLayout.setExpandRatio(searchField, 1);

	}

	private Button createClearButton()
	{
		Button clear = new Button("Clear Filter");
		clear.setImmediate(true);
		clear.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void clicked(ClickEvent event)
			{
				searchField.setValue("");
				clearAdvancedFilters();
				triggerFilter();

			}

		});
		return clear;
	}

	private AbstractLayout buildAdvancedSearch()
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

			searchLayout.addComponent(advancedSearchLayout);
			advancedSearchLayout.setVisible(false);
		}
		return advancedSearchLayout;
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

	protected void showSaveCancel(boolean show)
	{

		saveButton.setVisible(show);
		cancelButton.setVisible(show);
	}

	protected void setSplitPosition(float pos)
	{
		splitPanel.setSplitPosition(pos);
	}

	private void initButtons()
	{
		newButton.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			public void clicked(ClickEvent event)
			{

				/*
				 * Rows in the Container data model are called Item. Here we add
				 * a new row in the beginning of the list.
				 */

				allowRowChange(new RowChangeCallback()
				{

					@Override
					public void allowRowChange()
					{
						try
						{
							container.removeAllContainerFilters();

							newEntity = container.createEntityItem(entityClass.newInstance());
							rowChanged(newEntity);
							// Can't delete when you are adding a new record.
							// Use cancel instead.
							if (deleteButton.isVisible())
							{
								restoreDelete = true;
								showDelete(false);
							}

							rightLayout.setVisible(true);
						}
						catch (ConstraintViolationException e)
						{
							FormHelper.showConstraintViolation(e);
						}
						catch (InstantiationException e)
						{
							logger.error(e, e);
							throw new RuntimeException(e);
						}
						catch (IllegalAccessException e)
						{
							logger.error(e, e);
							throw new RuntimeException(e);
						}
					}
				});

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
										Object previousItemId = entityTable.prevItemId(contactId);
										entityTable.removeItem(contactId);
										newEntity = null;
										// set the selection to the first item
										// on the page.
										// We need to set it to null first as if
										// the first item was already selected
										// then we won't get a row change which
										// is need to update the rhs.
										// CONSIDER: On the other hand I'm
										// concerned that we might confuse
										// people as they
										// get to row changes events.
										BaseCrudView.this.entityTable.select(null);
										BaseCrudView.this.entityTable.select(previousItemId);
										container.commit();
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
				if (newEntity != null)
				{
					if (restoreDelete)
					{
						showDelete(true);
						restoreDelete = false;
					}
					// set the selection to the first item on the page.
					// We need to set it to null first as if the first item was
					// already selected
					// then we won't get a row change which is need to update
					// the rhs.
					// CONSIDER: On the other hand I'm concerned that we might
					// confuse people as they
					// get to row changes events.
					BaseCrudView.this.entityTable.select(null);
					BaseCrudView.this.entityTable.select(entityTable.getCurrentPageFirstItemId());
				}

				newEntity = null;
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

	public void save()
	{
//		Object id = null;
		try
		{
			commit();

			if (newEntity != null)
			{
				interceptSaveValues(newEntity.getEntity());

				Object id =  container.addEntity(newEntity.getEntity());
				EntityItem<E> item = container.getItem(id);
				//container.commit();
				
				

				fieldGroup.setItemDataSource(item);
				entityTable.select(item.getItemId());
				// If we leave the save button active, clicking it again
				// duplicates the record
	//			rightLayout.setVisible(false);
			}
			else
			{
				E current = entityTable.getCurrent();
				if (current != null)
				{
					interceptSaveValues(current);
					container.commit();
				}
			}

			if (newEntity != null)
			{
				newEntity = null;
				if (restoreDelete)
				{
					showDelete(true);
					restoreDelete = false;
				}
			}

			Notification.show("Changes Saved", "Any changes you have made have been saved.", Type.TRAY_NOTIFICATION);

		}
		catch (PersistenceException e)
		{
			logger.error(e, e);
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
		}
		catch (ConstraintViolationException e)
		{
			logger.error(e, e);
			FormHelper.showConstraintViolation(e);
		}
		finally
		{
			if (newEntity != null)
			{
				container.removeItem(entityTable.getCurrent());
			}
		}

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
				if (advancedSearchLayout != null && advancedSearchButton.getValue())
				{
					filter = getAdvancedContainerFilter(filter);
				}
				applyFilter(filter);
			}

		});

		searchField.focus();
	}

	/**
	 * call this method to cause filters to be applied
	 */
	protected void triggerFilter()
	{
		Filter filter = getContainerFilter(searchField.getValue());
		if (advancedSearchButton != null && advancedSearchButton.getValue())
		{
			filter = getAdvancedContainerFilter(filter);
		}
		applyFilter(filter);
	}

	public void applyFilter(final Filter filter)
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

	/**
	 * called when the advancedFilters layout should clear it's values
	 */
	protected void clearAdvancedFilters()
	{

	}

	@Override
	/** Called when the currently selected row in the 
	 *  table part of this view has changed.
	 *  We use this to update the editor's current item.
	 */
	public void allowRowChange(final RowChangeCallback callback)
	{

		if (fieldGroup.isModified() || newEntity != null)
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
										if (restoreDelete)
										{
											showDelete(true);
											restoreDelete = false;
										}

										newEntity = null;

										callback.allowRowChange();

									}
									else
									{
										// User did not confirm so don't allow
										// the change.

									}
								}
							});
		}
		else
		{
			callback.allowRowChange();
		}

	}

	@Override
	/** Called when the currently selected row in the 
	 *  table part of this view has changed.
	 *  We use this to update the editor's current item.
	 *  
	 *  @item the item that is now selected. This may be null if selection has been lost.
	 */
	public void rowChanged(EntityItem<E> item)
	{

		// The contact is null if the row is de-selected
		if (item != null)
		{

			fieldGroup.setItemDataSource(item);

			// removed as this doesn't make any sense particularly since the
			// property id is hard coded to'name'.
			// Preconditions.checkState(fieldGroup.getItemDataSource().getItemPropertyIds().contains("name"),
			// "valid listFieldNames are " +
			// fieldGroup.getItemDataSource().getItemPropertyIds().toString());

		}
		else
		{
			fieldGroup.setItemDataSource(null);
		}
		rightLayout.setVisible(item != null || newEntity != null);

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
		E entity = null;
		if (newEntity != null)
			entity = newEntity.getEntity();
		if (entity == null)
		{
			entity = entityTable.getCurrent();
		}
		return entity;

	}

	/**
	 * update the container and editor with any changes from the db.
	 */
	public void updateEditorFromDb()
	{
		Preconditions.checkState(!isDirty(), "The editor is dirty, save or cancel first.");

		E entity = entityTable.getCurrent();
		container.refresh();
		entityTable.select(null);
		entityTable.select(entity.getId());

	}

	/**
	 * check if the editor has changes
	 * 
	 * @return
	 */
	public boolean isDirty()
	{
		return fieldGroup.isModified() || newEntity != null;
	}

}
