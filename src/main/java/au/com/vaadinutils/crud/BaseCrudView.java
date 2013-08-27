package au.com.vaadinutils.crud;

import javax.validation.ConstraintViolationException;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import au.com.vaadinutils.listener.ClickListenerLogged;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
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
	private String[] headings;
	private MultiColumnFormLayout<E> editor;

	protected void init( Class<E> entityClass, JPAContainer<E> container, String[] headings)
	{
		this.entityClass = entityClass;
		this.container = container;
		this.headings = headings;
		fieldGroup = new ValidatingFieldGroup<E>(container,entityClass);
		fieldGroup.setBuffered(true);

		entityTable = new EntityTable(container, headings);
		entityTable.setRowChangeListener(this);

		initLayout();
		entityTable.init();
			initSearch();
		initButtons();
		this.setVisible(true);
	}

	/*
	 * build the button layout aned editor panel
	 */

	protected abstract MultiColumnFormLayout<E> buildEditor(ValidatingFieldGroup<E> fieldGroup2);

	private void initLayout()
	{
		this.setSizeFull();

		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		this.addComponent(splitPanel);
		this.setExpandRatio(splitPanel, 1);

		// Layout for the table
		VerticalLayout leftLayout = new VerticalLayout();

		// Start by defining the LHS which contains the table
		splitPanel.addComponent(leftLayout);
		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		leftLayout.addComponent(bottomLeftLayout);
		bottomLeftLayout.addComponent(searchField);
		bottomLeftLayout.addComponent(newButton);
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
		bottomLeftLayout.setWidth("100%");
		searchField.setWidth("100%");
		bottomLeftLayout.setExpandRatio(searchField, 1);

		// Now define the edit area
		rightLayout = new VerticalLayout();
		splitPanel.addComponent(rightLayout);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");
		buttonLayout.addComponent(saveButton);
		buttonLayout.addComponent(cancelButton);
		buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);
		buttonLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_LEFT);
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
				try
				{
					commit();

					if (inNew)
					{
						Long id = (Long) container.addEntity(BaseCrudView.this.currentEntity);
						BaseCrudView.this.entityTable.select(id);
						inNew = false;
					}
				}
				catch (ConstraintViolationException e)
				{
					FormHelper.showConstraintViolation(e);
				}

			}
		});

	}

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

				/* Reset the filter for the contactContainer. */
				container.removeAllContainerFilters();
				container.addContainerFilter(getContainerFilter());
			}

		});
	}

	private Filter getContainerFilter()
	{
		return null;
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
			Preconditions.checkState(fieldGroup.getItemDataSource().getItemPropertyIds().contains("name"),
					"valid listFieldNames are " + fieldGroup.getItemDataSource().getItemPropertyIds().toString());

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

				Notification
						.show("Changes Saved", "Any changes you have made have been saved.", Type.TRAY_NOTIFICATION);
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