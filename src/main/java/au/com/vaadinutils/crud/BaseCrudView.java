package au.com.vaadinutils.crud;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import au.com.vaadinutils.listener.ClickEventLogged;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

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
	protected boolean restoreDelete;

	protected TextField searchField = new TextField();
	private Button newButton = new Button("New");
	protected Button applyButton = new Button("Apply");
	private Button saveButton = new Button("Save");
	private Button cancelButton = new Button("Cancel");
	protected Class<E> entityClass;

	protected ValidatingFieldGroup<E> fieldGroup;

	private VerticalLayout mainEditPanel = new VerticalLayout();

	// private E currentEntity;

	/*
	 * Any component can be bound to an external data source. This example uses
	 * just a dummy in-memory list, but there are many more practical
	 * implementations.
	 */
	protected JPAContainer<E> container;

	/* User interface components are stored in session. */
	protected EntityList<E> entityTable;
	protected VerticalLayout rightLayout;
	private Component editor;
	protected CrudPanelPair splitPanel;
	private HorizontalLayout buttonLayout;
	private AbstractLayout advancedSearchLayout;
	private VerticalLayout searchLayout;
	private CheckBox advancedSearchButton;
	protected Set<ChildCrudListener<E>> childCrudListeners = new HashSet<ChildCrudListener<E>>();
	private CrudDisplayMode displayMode = CrudDisplayMode.HORIZONTAL;
	protected HorizontalLayout actionLayout;
	private ComboBox actionCombo;

	protected BaseCrudView()
	{

	}

	BaseCrudView(CrudDisplayMode mode)
	{
		this.displayMode = mode;
	}

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
		showNoSelectionMessage();
		entityTable.select(entityTable.firstItemId());

	}

	protected EntityList<E> getTable(JPAContainer<E> container, HeadingPropertySet<E> headings)
	{
		return new EntityTable<E>(container, headings);
	}

	/*
	 * build the button layout aned editor panel
	 */

	protected abstract Component buildEditor(ValidatingFieldGroup<E> fieldGroup2);

	private void initLayout()
	{
		this.setSizeFull();

		splitPanel = displayMode.getContainer();
		this.addComponent(splitPanel.getPanel());
		this.setExpandRatio(splitPanel.getPanel(), 1);
		this.setSizeFull();

		// Layout for the tablesaveOnRowChange
		VerticalLayout leftLayout = new VerticalLayout();

		// Start by defining the LHS which contains the table
		splitPanel.setFirstComponent(leftLayout);
		searchLayout = new VerticalLayout();
		searchLayout.setWidth("100%");
		searchField.setWidth("100%");

		// expandratio and use of setSizeFull are incompatible
		// searchLayout.setSizeFull();

		Component title = getTitle();
		if (title != null)
		{
			leftLayout.addComponent(getTitle());
		}

		leftLayout.addComponent(searchLayout);

		buildSearchBar();

		leftLayout.addComponent(entityTable);
		leftLayout.setSizeFull();

		/*
		 * On the left side, expand the size of the entity List so that it uses
		 * all the space left after from bottomLeftLayout
		 */
		leftLayout.setExpandRatio(entityTable, 1);
		entityTable.setSizeFull();

		// Now define the edit area
		rightLayout = new VerticalLayout();
		splitPanel.setSecondComponent(rightLayout);

		/* Put a little margin around the fields in the right side editor */
		Panel scroll = new Panel();
		// mainEditPanel.setDescription("BaseCrud:MainEditPanel");
		mainEditPanel.setVisible(true);
		mainEditPanel.setSizeFull();
		mainEditPanel.setId("MailEditPanel");
		scroll.setSizeFull();
		scroll.setContent(mainEditPanel);

		buildActionLayout();

		leftLayout.addComponent(actionLayout);
		rightLayout.addComponent(scroll);
		rightLayout.setExpandRatio(scroll, 1.0f);
		rightLayout.setSizeFull();
		rightLayout.setId("rightLayout");

		addSaveAndCancelButtons();

		editor = buildEditor(fieldGroup);
		Preconditions.checkNotNull(editor, "Your editor implementation returned null!, you better create an editor. "
				+ entityClass);
		mainEditPanel.addComponent(editor);

		rightLayout.setVisible(false);
	}

	protected String getTitleText()
	{
		return "Override getTitleText() to set a title.";
	}

	protected Component getTitle()
	{
		HorizontalLayout holder = new HorizontalLayout();

		Label titleLabel = new Label(getTitleText());

		titleLabel.setStyleName(Reindeer.LABEL_H1);
		holder.addComponent(titleLabel);
		holder.setComponentAlignment(titleLabel, Alignment.MIDDLE_RIGHT);
		return holder;
	}

	@SuppressWarnings("null")
	private void buildActionLayout()
	{
		actionLayout = new HorizontalLayout();
		actionLayout.setWidth("100%");
		actionLayout.setMargin(new MarginInfo(false, true, false, true));

		HorizontalLayout actionArea = new HorizontalLayout();
		actionArea.setSpacing(true);
		Label applyLabel = new Label(" Action");
		actionArea.addComponent(applyLabel);
		actionArea.setComponentAlignment(applyLabel, Alignment.MIDDLE_LEFT);

		actionCombo = new ComboBox(null);
		actionCombo.setWidth("160");
		actionCombo.setNullSelectionAllowed(false);

		actionArea.addComponent(actionCombo);

		/**
		 * Add the set of actions in.
		 */
		CrudAction<E> defaultAction = null;
		for (CrudAction<E> action : getCrudActions())
		{
			if (action.isDefault())
			{
				Preconditions.checkState(defaultAction == null, "Only one action may be marked as default: "
						+ (defaultAction != null ? defaultAction.toString() : "") + " was already the default when "
						+ action.toString() + " was found to also be default.");
				defaultAction = action;
			}
			actionCombo.addItem(action);

		}

		// Select the default action
		actionCombo.setValue(defaultAction);
		actionArea.addComponent(applyButton);

		// tweak the alignments.
		actionArea.setComponentAlignment(actionCombo, Alignment.MIDDLE_RIGHT);
		actionLayout.addComponent(actionArea);
		actionLayout.setComponentAlignment(actionArea, Alignment.MIDDLE_LEFT);

		actionLayout.addComponent(newButton);
		actionLayout.setComponentAlignment(newButton, Alignment.MIDDLE_RIGHT);

		actionLayout.setHeight("35");
	}

	/**
	 * overload this method to add customer actions, in your overloaded version
	 * you should call super.getCrudActions() to get a list with the
	 * DeleteAction pre-populated
	 */
	protected List<CrudAction<E>> getCrudActions()
	{
		List<CrudAction<E>> actions = new LinkedList<CrudAction<E>>();
		@SuppressWarnings("unchecked")
		CrudAction<E> crudAction = ((CrudAction<E>) new CrudActionDelete<E>());
		actions.add(crudAction);

		return actions;
	}

	protected void addSaveAndCancelButtons()
	{
		buttonLayout = new HorizontalLayout();
		buttonLayout.setMargin(new MarginInfo(false, true, false, true));
		buttonLayout.setWidth("100%");
		buttonLayout.addComponent(cancelButton);
		buttonLayout.addComponent(saveButton);
		buttonLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
		buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);
		buttonLayout.setHeight("35");
		rightLayout.addComponent(buttonLayout);
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
		basicSearchLayout.setComponentAlignment(clear, Alignment.MIDDLE_CENTER);

		basicSearchLayout.setSpacing(true);

		/*
		 * In the bottomLeftLayout, searchField takes all the width there is
		 * after adding addNewButton. The height of the layout is defined by the
		 * tallest component.
		 */
		basicSearchLayout.setExpandRatio(searchField, 1);

	}

	private Button createClearButton()
	{

		Button clear = new Button("X");
		clear.setStyleName(Reindeer.BUTTON_SMALL);
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
					if (!advancedSearchButton.getValue())
					{
						triggerFilter();
					}

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

	/**
	 * Call this method during buildEditor to suppress the action combo and
	 * Apply button. They are displayed by default.
	 * 
	 * @param show
	 */
	protected void showActions(boolean show)
	{
		actionLayout.setVisible(show);
		applyButton.setVisible(show);
		actionCombo.setVisible(show);
	}

	/**
	 * Used when creating a 'new' record to disable actions such as 'new' and
	 * delete until the record is saved.
	 * 
	 * @param show
	 */
	private void enableActions(boolean enabled)
	{
		applyButton.setEnabled(enabled);
		actionCombo.setEnabled(enabled);
		newButton.setEnabled(enabled);
	}

	/**
	 * Call this method during buildEditor to suppress the display of the 'New'
	 * button. The 'New' button will be displayed by default.
	 */
	protected void showNew(boolean show)
	{
		newButton.setVisible(show);
	}

	protected void showSaveCancel(boolean show)
	{
		buttonLayout.setVisible(show);
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

				newClicked();

			}

		});

		applyButton.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void clicked(ClickEvent event)
			{
				Object entityId = entityTable.getValue();
				if (entityId != null)
				{
					E entity = container.getItem(entityId).getEntity();

					@SuppressWarnings("unchecked")
					CrudAction<E> action = (CrudAction<E>) actionCombo.getValue();
					if (interceptAction(action, entity))
						action.exec(BaseCrudView.this, entity);

					// actionCombo.select(actionCombo.getNullSelectionItemId());
				}
			}
		});

		cancelButton.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void clicked(ClickEvent event)
			{
				fieldGroup.discard();
				if (newEntity != null)
				{
					if (restoreDelete)
					{
						enableActions(true);
						restoreDelete = false;
					}
					newEntity = null;

					// set the selection to the first item on the page.
					// We need to set it to null first as if the first item was
					// already selected
					// then we won't get a row change which is need to update
					// the rhs.
					// CONSIDER: On the other hand I'm concerned that we might
					// confuse people as they
					// get two row changes events.
					BaseCrudView.this.entityTable.select(null);
					BaseCrudView.this.entityTable.select(entityTable.getCurrentPageFirstItemId());

				}
				else
				{
					// Force the row to be reselected so that derived
					// classes get a rowChange when we cancel.
					// CONSIDER: is there a better way of doing this?
					// Could we not just fire an 'onCancel' event or similar?
					Long id = entityTable.getCurrent().getEntity().getId();
					BaseCrudView.this.entityTable.select(null);
					BaseCrudView.this.entityTable.select(id);

				}
				splitPanel.showFirstComponet();
				if (entityTable.getCurrent() == null)
				{
					showNoSelectionMessage();
				}

				Notification.show("Changes discarded.", "Any changes you have made to this record been discarded.",
						Type.TRAY_NOTIFICATION);
			}
		});

		saveButton.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void clicked(ClickEvent event)
			{
				save();

			}

		});

	}

	/**
	 * Override this method to intercept activation of an action.
	 * 
	 * Return true if you are happy for the action to proceed otherwise return
	 * false if you want to suppress the action.
	 * 
	 * When suppressing the action you should display a notification as to why
	 * you suppressed it.
	 * 
	 * @param action
	 * @param entity
	 * @return
	 */
	protected boolean interceptAction(CrudAction<E> action, E entity)
	{
		return true;
	}

	public void delete()
	{
		Object entityId = entityTable.getValue();
		Object previousItemId = entityTable.prevItemId(entityId);
		entityTable.removeItem(entityId);
		newEntity = null;
		// set the selection to the first item
		// on the page.
		// We need to set it to null first as if
		// the first item was already selected
		// then we won't get a row change which
		// is need to update the rhs.
		// CONSIDER: On the other hand I'm
		// concerned that we might confuse
		// developers as they
		// get two row changes events.
		BaseCrudView.this.entityTable.select(null);
		BaseCrudView.this.entityTable.select(previousItemId);
		container.commit();

		postDelete(entityId);
	}

	/**
	 * hook for implements that need to do some additional cleanup after a
	 * delete
	 * 
	 */
	protected void postDelete(Object entityId)
	{

	}

	protected void save()
	{
		boolean selected = false;
		try
		{
			commit();

			if (newEntity != null)
			{
				interceptSaveValues(newEntity);

				Object id = container.addEntity(newEntity.getEntity());
				EntityItem<E> item = container.getItem(id);

				fieldGroup.setItemDataSource(item);
				entityTable.select(item.getItemId());
				selected = true;

				newEntity = null;
				if (restoreDelete)
				{
					enableActions(true);
					restoreDelete = false;
				}
			}
			else
			{
				EntityItem<E> current = entityTable.getCurrent();
				if (current != null)
				{
					interceptSaveValues(current);
				}
			}

			for (ChildCrudListener<E> commitListener : childCrudListeners)
			{
				commitListener.committed(entityTable.getCurrent());
			}

			EntityItem<E> current = entityTable.getCurrent();
			if (current != null)
			{
				postSaveAction(current);
			}

			container.commit();
			splitPanel.showFirstComponet();
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
		catch (InvalidValueException e)
		{
			logger.error(e, e);
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
		}
		catch (CommitException e)
		{
			if (e.getCause() instanceof InvalidValueException)
			{
				Notification.show("Please fix the form errors and then try again.", Type.ERROR_MESSAGE);
			}
			else
			{
				logger.error(e, e);
				Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
			}
		}
		finally
		{
			if (newEntity != null)
			{

				if (selected && entityTable.getCurrent() != null)
				{
					container.removeItem(entityTable.getCurrent());
				}
			}
		}

	}

	protected void postSaveAction(EntityItem<E> entityItem)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * opportunity for implementing classes to modify or add data to the entity
	 * being saved.
	 * 
	 * NOTE: modify item properties, accessing the entity is unreliable
	 * 
	 * @param item
	 */
	protected void interceptSaveValues(EntityItem<E> entityItem)
	{
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
		/* Reset the filter for the Entity Container. */
		resetFilters();
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
	abstract protected Filter getContainerFilter(String filterString);

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

		boolean dirty = false;
		for (ChildCrudListener<E> commitListener : childCrudListeners)
		{
			dirty |= commitListener.isDirty();
		}

		if (fieldGroup.isModified() || newEntity != null || dirty)
		{
			ConfirmDialog
					.show(UI.getCurrent(),
							"Discard changes?",
							"You have unsaved changes for this record. Continuing will result in those changes being discarded. ",
							"Continue", "Cancel", new ConfirmDialog.Listener()
							{
								private static final long serialVersionUID = 1L;

								public void onClose(ConfirmDialog dialog)
								{
									if (dialog.isConfirmed())
									{
										/*
										 * When an entity is selected from the
										 * list, we want to show that in our
										 * editor on the right. This is nicely
										 * done by the FieldGroup that binds all
										 * the fields to the corresponding
										 * Properties in our entity at once.
										 */
										fieldGroup.discard();
										if (restoreDelete)
										{
											enableActions(true);
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

		splitPanel.showSecondComponet();
		fieldGroup.setItemDataSource(item);

		// notifiy ChildCrudView's taht we've changed row.
		for (ChildCrudListener<E> commitListener : childCrudListeners)
		{
			commitListener.selectedRowChanged(item);
		}

		if (item != null || newEntity != null)
		{
			splitPanel.setSecondComponent(rightLayout);
		}
		else
		{
			showNoSelectionMessage();
		}

		rightLayout.setVisible(item != null || newEntity != null);

	}

	private void showNoSelectionMessage()
	{
		String message = "";
		if (newButton.isVisible())
		{
			message = "Click New to create a new record.";
			if (entityTable.firstItemId() != null)
			{
				message = "Click New to create a new record or click an existing "
						+ "record to view and or edit the records details.";
			}

		}
		else
		{
			if (entityTable.firstItemId() != null)
			{
				message = "click an existing record to view and or edit the records details.";
			}
			else
			{
				message = "No records were found.";
			}

		}
		VerticalLayout pane = new VerticalLayout();
		pane.setSizeFull();
		Label label = new Label(message);
		label.setWidth("300");
		label.setContentMode(ContentMode.HTML);

		pane.addComponent(label);
		pane.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
		splitPanel.setSecondComponent(pane);
	}

	protected void commit() throws CommitException
	{
		formValidate();
		fieldGroup.commit();

		// try
		// {
		// if (!fieldGroup.isValid() || !valid())
		// {
		// Notification.show("Validation Errors",
		// "Please fix any field errors and try again.",
		// Type.WARNING_MESSAGE);
		// }
		// else
		// {
		// fieldGroup.commit();
		//
		// }
		// }
		// catch (CommitException e)
		// {
		// Notification.show("Error saving changes.",
		// "Any error occured attempting to save your changes: " +
		// e.getMessage(), Type.ERROR_MESSAGE);
		// logger.error(e, e);
		// }

	}

	/**
	 * Overload this method to provide cross-field (form level) validation.
	 * 
	 * @return
	 */
	protected void formValidate() throws InvalidValueException
	{
	}

	VerticalLayout getEmptyPanel()
	{
		VerticalLayout layout = new VerticalLayout();

		Label pleaseAdd = new Label(
				"Click the 'New' button to add a new Record or click an existing record in the adjacent table to edit it.");
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
			entity = entityTable.getCurrent().getEntity();
		}
		return entity;

	}

	/**
	 * update the container and editor with any changes from the db.
	 */
	public void updateEditorFromDb()
	{
		Preconditions.checkState(!isDirty(), "The editor is dirty, save or cancel first.");

		E entity = entityTable.getCurrent().getEntity();
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

	/**
	 * a ChildCrudView adds it's self here so it will be notified when the
	 * parent saves
	 * 
	 * @param listener
	 */
	public void addChildCrudListener(ChildCrudListener<E> listener)
	{
		childCrudListeners.add(listener);
	}

	protected void newClicked()
	{
		/*
		 * Rows in the Container data model are called Item. Here we add a new
		 * row in the beginning of the list.
		 */

		allowRowChange(new RowChangeCallback()
		{

			@Override
			public void allowRowChange()
			{
				try
				{

					resetFilters();

					newEntity = container.createEntityItem(entityClass.newInstance());
					rowChanged(newEntity);
					// Can't delete when you are adding a new record.
					// Use cancel instead.
					if (applyButton.isVisible())
					{
						restoreDelete = true;
						enableActions(false);
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

	/**
	 * for child cruds, they overload this to ensure that the minimum necessary
	 * filters are always applied.
	 */
	protected void resetFilters()
	{
		container.removeAllContainerFilters();
	}

	protected boolean isNew()
	{
		return this.newEntity != null;
	}

}
