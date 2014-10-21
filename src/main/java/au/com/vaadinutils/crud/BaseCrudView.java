package au.com.vaadinutils.crud;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.vaadin.dialogs.ConfirmDialog;

import au.com.vaadinutils.crud.events.CrudEventDistributer;
import au.com.vaadinutils.crud.events.CrudEventType;
import au.com.vaadinutils.crud.security.SecurityManagerFactoryProxy;
import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.listener.ClickEventLogged;
import au.com.vaadinutils.menu.Menu;
import au.com.vaadinutils.menu.Menus;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainer.ProviderChangedEvent;
import com.vaadin.data.Buffered;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIsTarget;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.ValoTheme;

public abstract class BaseCrudView<E extends CrudEntity> extends VerticalLayout implements RowChangeListener<E>,
		Selected<E>, DirtyListener
{

	private static transient Logger logger = LogManager.getLogger(BaseCrudView.class);
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
	protected Button newButton = new Button("New");
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
	protected Set<ChildCrudListener<E>> childCrudListeners = new HashSet<ChildCrudListener<E>>();
	private CrudDisplayMode displayMode = CrudDisplayMode.HORIZONTAL;
	protected HorizontalLayout actionLayout;
	protected ComboBox actionCombo;
	private boolean disallowEditing = false;
	private boolean hideSaveCancelLayout = false;
	private boolean disallowNew = false;
	@SuppressWarnings("unused")
	private boolean disallowDelete = false;
	private Label actionLabel;
	private boolean noEditor;
	private boolean advancedSearchOn = false;
	private Button advancedSearchCheckbox;

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
			// container.setAutoCommit(true);

		}
		catch (Exception e)
		{
			logger.error(" ******* when constructing a jpaContainer for use with the BaseCrudView use JPAContainerFactory.makeBatchable ****** ");
			logger.error(e, e);
			throw new RuntimeException(e);
		}
		fieldGroup = new ValidatingFieldGroup<E>(container, entityClass);
		fieldGroup.setBuffered(true);

		// disable this, as the disabling of the save/cancel button is buggy
		// fieldGroup.setDirtyListener(this);

		entityTable = getTable(container, headings);
		entityTable.setRowChangeListener(this);
		entityTable.setSortEnabled(true);
		entityTable.setColumnCollapsingAllowed(true);

		initLayout();

		try
		{
			entityTable.init();
		}
		catch (Exception e)
		{
			handleConstraintViolationException(e);

		}

		initSearch();
		initButtons();
		this.setVisible(true);
		showNoSelectionMessage();
		entityTable.select(entityTable.firstItemId());

		// do the security check after all the other setup, so extending classes
		// don't throw npe's due to
		// uninitialised components
		if (!getSecurityManager().canUserView())
		{
			this.setSizeFull();
			Label sorryMessage = new Label("Sorry, you do not have permission to access " + getTitleText());
			sorryMessage.setStyleName(ValoTheme.LABEL_H1);
			this.removeAllComponents();
			this.addComponent(sorryMessage);
			return;
		}

		if (!getSecurityManager().canUserDelete())
		{
			// disable delete as the user doesn't have permission to delete
			disallowDelete(true);
		}

		if (!getSecurityManager().canUserEdit())
		{
			// disable save as the user doesn't have permission to edit
			buttonLayout.removeComponent(saveButton);
			saveButton.setVisible(false);
		}

		if (!getSecurityManager().canUserCreate())
		{
			disallowNew(true);
		}
		resetFilters();

	}

	/**
	 * allows the user to sort the items in the list via drag and drop
	 * 
	 * @param ordinalField
	 */
	public void enableDragAndDropOrdering(final SingularAttribute<E, Long> ordinalField)
	{
		container.sort(new Object[] { ordinalField.getName() }, new boolean[] { true });

		this.entityTable.setDragMode(TableDragMode.ROW);
		this.entityTable.setDropHandler(new DropHandler()
		{
			private static final long serialVersionUID = -6024948983201516170L;

			public AcceptCriterion getAcceptCriterion()
			{
				return SourceIsTarget.get();
			}

			@SuppressWarnings("unchecked")
			@Override
			public void drop(DragAndDropEvent event)
			{

				if (isDirty())
				{
					Notification.show("You must save first");
					return;
				}
				Object draggedItemId = event.getTransferable().getData("itemId");
				Object targetId = ((AbstractSelectTargetDetails) event.getTargetDetails()).getItemIdOver();

				EntityItem<E> dragged = container.getItem(draggedItemId);
				EntityItem<E> target = container.getItem(targetId);

				if (target != null)
				{
					EntityItemProperty targetOrdinalProp = target.getItemProperty(ordinalField.getName());
					Long targetOrdinal = (Long) targetOrdinalProp.getValue();
				}
				EntityItemProperty draggedOrdinalProp = dragged.getItemProperty(ordinalField.getName());
				Long draggedOrdinal = (Long) draggedOrdinalProp.getValue();

				boolean added = false;
				Long ctr = 1l;

				for (Object id : container.getItemIds())
				{
					if (id.equals(targetId))
					{
						draggedOrdinalProp.setValue(ctr++);
						added = true;

					}
					if (!id.equals(draggedItemId))
					{
						container.getItem(id).getItemProperty(ordinalField.getName()).setValue(ctr++);
					}
				}
				if (!added)
				{
					draggedOrdinalProp.setValue(ctr++);

				}

				container.commit();
				container.refresh();
				container.sort(new Object[] { ordinalField.getName() }, new boolean[] { true });

				// cause this crud to save, or if its a child cause the parent
				// to save.
				try
				{
					invokeTopLevelCrudSave();
				}
				catch (Exception e)
				{
					Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
					handleConstraintViolationException(e);
				}

			}
		});

	}

	/**
	 * the child crud variant of this method calls parent.save();
	 */
	protected void invokeTopLevelCrudSave()
	{
		save();
	}

	/**
	 * if you need to provide a security manager, call
	 * SecurityManagerFactoryProxy.setFactory(...) at application initialisation
	 * time
	 * 
	 * @return
	 * @throws ExecutionException
	 */
	public CrudSecurityManager getSecurityManager()
	{
		return SecurityManagerFactoryProxy.getSecurityManager(this.getClass());
	}

	public void addGeneratedColumn(Object id, ColumnGenerator generator)
	{
		entityTable.addGeneratedColumn(id, generator);
	}

	protected EntityList<E> getTable(JPAContainer<E> container, HeadingPropertySet<E> headings)
	{
		return new EntityTable<E>(container, headings);
	}

	/*
	 * build the button layout and editor panel
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

		if (!noEditor)
		{
			mainEditPanel.setVisible(true);
			mainEditPanel.setSizeFull();
			mainEditPanel.setId("MailEditPanel");
			scroll.setSizeFull();
			scroll.setContent(mainEditPanel);
			rightLayout.addComponent(scroll);
			rightLayout.setExpandRatio(scroll, 1.0f);
			rightLayout.setSizeFull();
			rightLayout.setId("rightLayout");

			editor = buildEditor(fieldGroup);
			Preconditions.checkNotNull(
					editor,
					"Your editor implementation returned null!, you better create an editor. "
							+ entityClass.getSimpleName());
			mainEditPanel.addComponent(editor);

		}
		else
		{
			this.setSplitPosition(100);
			splitPanel.setLocked();
		}
		buildActionLayout();

		leftLayout.addComponent(actionLayout);

		addSaveAndCancelButtons();

		rightLayout.setVisible(false);
	}

	/**
	 * call this method before init if you intend not to provide an editor
	 */
	public void noEditor()
	{
		noEditor = true;

	}

	/**
	 * get the title for the page from the menu annotation, override this menu
	 * to provide a custom page title
	 * 
	 * @return
	 */
	protected String getTitleText()
	{

		Annotation annotation = this.getClass().getAnnotation(Menu.class);
		if (annotation instanceof Menu)
		{
			return ((Menu) annotation).display();
		}
		annotation = this.getClass().getAnnotation(Menus.class);
		if (annotation instanceof Menus)
		{
			return ((Menus) annotation).menus()[0].display();
		}

		return "Override getTitleText() to set a custom title.";
	}

	protected Component getTitle()
	{
		HorizontalLayout holder = new HorizontalLayout();

		Label titleLabel = new Label(getTitleText());

		titleLabel.addStyleName(ValoTheme.LABEL_H2);
		titleLabel.addStyleName(ValoTheme.LABEL_BOLD);

		holder.addComponent(titleLabel);
		holder.setComponentAlignment(titleLabel, Alignment.MIDDLE_RIGHT);
		return holder;
	}

	private void buildActionLayout()
	{
		actionLayout = new HorizontalLayout();
		actionLayout.setWidth("100%");
		actionLayout.setMargin(new MarginInfo(false, true, false, false));

		actionLabel = new Label("&nbsp;Action");
		actionLabel.setContentMode(ContentMode.HTML);
		actionLabel.setWidth("50");
		CssLayout group = new CssLayout();
		group.addStyleName("v-component-group");
		actionLayout.addComponent(group);
		group.addComponent(actionLabel);

		actionCombo = new ComboBox(null);
		actionCombo.setWidth("160");
		actionCombo.setNullSelectionAllowed(false);
		actionCombo.setTextInputAllowed(false);

		group.addComponent(actionCombo);

		addCrudActions();
		group.addComponent(applyButton);

		newButton.setCaption(getNewButtonLabel());
		newButton.setId("CrudNewButton");
		actionLayout.addComponent(newButton);

		// tweak the alignments.
		actionLayout.setComponentAlignment(group, Alignment.MIDDLE_LEFT);
		actionLayout.setComponentAlignment(newButton, Alignment.MIDDLE_RIGHT);
		actionLayout.setExpandRatio(group, 1.0f);

		actionLayout.setHeight("35");
	}

	protected String getNewButtonLabel()
	{
		return "New";
	}

	@SuppressWarnings("null")
	private void addCrudActions()
	{
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
	}

	/**
	 * overload this method to add custom actions, in your overloaded version
	 * you should call super.getCrudActions() to get a list with the
	 * DeleteAction pre-populated
	 */
	protected List<CrudAction<E>> getCrudActions()
	{
		List<CrudAction<E>> actions = new LinkedList<CrudAction<E>>();
		CrudAction<E> crudAction = new CrudActionDelete<E>();
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
		saveButton.setId("CrudSaveButton");
		buttonLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
		buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);
		buttonLayout.setHeight("35");
		rightLayout.addComponent(buttonLayout);
	}

	private void buildSearchBar()
	{

		CssLayout group = new CssLayout();
		group.setSizeFull();

		group.addStyleName("v-component-group");

		searchLayout.addComponent(group);

		AbstractLayout advancedSearch = buildAdvancedSearch();
		if (advancedSearch != null)
		{
			group.addComponent(advancedSearchCheckbox);
		}

		Button clear = createClearButton();

		group.addComponent(clear);

		// searchField.setWidth("80%");
		searchField.setId("CrudSearchField");
		group.addComponent(searchField);

	}

	private Button createClearButton()
	{

		Button clear = new Button("X");
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

			advancedSearchCheckbox = new Button("Advanced");
			advancedSearchOn = false;

			advancedSearchCheckbox.setImmediate(true);
			advancedSearchCheckbox.addClickListener(new ClickListener()
			{

				private static final long serialVersionUID = 7777043506655571664L;

				@Override
				public void buttonClick(ClickEvent event)
				{
					advancedSearchOn = !advancedSearchOn;
					advancedSearchLayout.setVisible(advancedSearchOn);
					if (!advancedSearchOn)
					{
						triggerFilter();
					}
					if (!advancedSearchOn)
					{
						advancedSearchCheckbox.removeStyleName(ValoTheme.BUTTON_FRIENDLY);
					}
					else
					{
						advancedSearchCheckbox.setStyleName(ValoTheme.BUTTON_FRIENDLY);
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
		return null;
	}

	/**
	 * Used when creating a 'new' record to disable actions such as 'new' and
	 * delete until the record is saved.
	 * 
	 * @param show
	 */
	protected void activateEditMode(boolean activate)
	{
		actionCombo.setEnabled(!activate);
		applyButton.setEnabled(!activate);

		boolean showNew = !activate;
		if (disallowNew)
			showNew = false;
		newButton.setEnabled(showNew);
	}

	/**
	 * A child class can call this method to stop a user from being able to edit
	 * a record. When called the Save/Cancel buttons are disabled from the
	 * screen.
	 * 
	 * If you also set hideSaveCancelLayout to true then the save/cancel buttons
	 * will be completely removed from the layout.
	 * 
	 * By default editing is allowed.
	 * 
	 * @param disallow
	 */
	protected void disallowEdit(boolean disallow, boolean hideSaveCancelLayout)
	{
		this.disallowEditing = disallow;
		this.hideSaveCancelLayout = hideSaveCancelLayout;
		showSaveCancel(!disallow);
		if (this.hideSaveCancelLayout)
			buttonLayout.setVisible(false);
	}

	/**
	 * A child class can call this method to stop a user from being able to add
	 * new records.
	 * 
	 * When called the 'New' button is removed from the UI.
	 * 
	 * By default adding new records is allowed.
	 * 
	 * @param disallow
	 */
	protected void disallowNew(boolean disallow)
	{
		this.disallowNew = disallow;
		showNew(!disallow);
	}

	protected boolean isDisallowNew()
	{
		return this.disallowNew;
	}

	/**
	 * A child class can call this method to stop a user from being able to
	 * delete a record. When called the delete action is removed from the action
	 * combo. If the delete is the only action then the action combo and apply
	 * button will also be removed.
	 * 
	 * By default deleting is allowed.
	 * 
	 * @param disallow
	 */
	protected void disallowDelete(boolean disallow)
	{
		this.disallowDelete = disallow;

		if (disallow || !getSecurityManager().canUserDelete())
		{
			// find and remove the delete action
			for (Object id : this.actionCombo.getItemIds())
			{
				if (id instanceof CrudActionDelete)
				{
					this.actionCombo.removeItem(id);
					break;
				}
			}
			if (this.actionCombo.size() == 0)
			{
				this.actionCombo.setVisible(false);
				this.applyButton.setVisible(false);
				this.actionLabel.setVisible(false);
			}
		}
		else
		{
			this.actionCombo.removeAllItems();
			addCrudActions();
			this.actionCombo.setVisible(true);
			this.applyButton.setVisible(true);
			this.actionLabel.setVisible(true);
		}

	}

	/**
	 * Internal method to show hide the new button when editing.
	 * 
	 * If the user has called disallowNew then the new button will never be
	 * displayed.
	 */
	private void showNew(boolean show)
	{
		if (disallowNew)
			show = false;

		newButton.setVisible(show);
	}

	/**
	 * Internal method to show/hide the save and cancel buttons when a user
	 * enters/exit editing. If disallowEditing has been called by a derived
	 * class then the save/cancel buttons will never be displayed.
	 * 
	 * @param show
	 */
	private void showSaveCancel(boolean show)
	{
		if (disallowEditing)
			show = false;

		saveButton.setVisible(show);
		cancelButton.setVisible(show);
	}

	/**
	 * Hides the Action layout which contains the 'New' button and 'Action'
	 * combo.
	 * 
	 * Hiding the action layout effectively stops the user from creating new
	 * records or applying any action such as deleting a record.
	 * 
	 * Hiding the action layout provides more room for the list of records.
	 * 
	 * @param show
	 */
	protected void showActionLayout(boolean show)
	{
		this.actionLayout.setVisible(show);
	}

	public void setSplitPosition(float pos)
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
					EntityItem<E> entity = container.getItem(entityId);

					@SuppressWarnings("unchecked")
					CrudAction<E> action = (CrudAction<E>) actionCombo.getValue();
					if (interceptAction(action, entity))
						action.exec(BaseCrudView.this, entity);
					container.commit();
					container.refreshItem(entity.getItemId());
					// actionCombo.select(actionCombo.getNullSelectionItemId());
				}
				else
					Notification.show("Please select record first.");
			}
		});

		cancelButton.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void clicked(ClickEvent event)
			{
				cancelClicked();
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
		saveButton.setStyleName(Reindeer.BUTTON_DEFAULT);
		saveButton.setDisableOnClick(true);

	}

	protected void cancelClicked()
	{
		fieldGroup.discard();
		for (ChildCrudListener<E> child : childCrudListeners)
		{
			child.discard();
		}

		if (newEntity != null)
		{
			if (restoreDelete)
			{
				activateEditMode(false);
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
	protected boolean interceptAction(CrudAction<E> action, EntityItem<E> entity)
	{
		return true;
	}

	public void delete()
	{
		E deltedEntity = entityTable.getCurrent().getEntity();
		Object entityId = entityTable.getValue();
		Object previousItemId = entityTable.prevItemId(entityId);
		entityTable.removeItem(entityId);
		newEntity = null;

		preDelete(deltedEntity);
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
		if (previousItemId != null)
		{
			BaseCrudView.this.entityTable.select(previousItemId);
		}
		else
		{
			entityTable.select(entityTable.firstItemId());
		}
		container.commit();

		EntityManagerProvider.getEntityManager().flush();

		postDelete(deltedEntity);

		CrudEventDistributer.publishEvent(this, CrudEventType.DELETE, deltedEntity);

	}

	/**
	 * hook for implementations that need to do some additional cleanup before a
	 * delete.
	 * 
	 */
	protected void preDelete(E entity)
	{

	}

	/**
	 * hook for implementations that need to do some additional cleanup after a
	 * delete.
	 * 
	 */
	protected void postDelete(E entity)
	{

	}

	public void save()
	{
		boolean selected = false;
		try
		{
			commitFieldGroup();
			CrudEventType eventType = CrudEventType.EDIT;
			if (newEntity != null)
			{
				eventType = CrudEventType.CREATE;
				interceptSaveValues(newEntity);

				Object id = container.addEntity(newEntity.getEntity());
				EntityItem<E> item = container.getItem(id);

				fieldGroup.setItemDataSource(item);
				selected = true;

				newEntity = null;
				if (restoreDelete)
				{
					activateEditMode(false);
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

			// commit the row to the database, and retrieve the possibly new
			// entity
			E newEntity = commitContainerAndGetEntityFromDB();
			if (newEntity == null)
			{
				throw new RuntimeException(
						"An error occurred, unable to retrieve updated record. Failed to save changes");
			}

			for (ChildCrudListener<E> commitListener : childCrudListeners)
			{
				// only commit dirty children, saves time for a crud with lots
				// of children
				if (commitListener.isDirty() || !(commitListener instanceof ChildCrudView))
				{
					commitListener.committed(newEntity);
				}
			}
			EntityManagerProvider.getEntityManager().flush();

			// children may have been added to the parent, evict the parent from
			// the JPA cache so it will get updated
			EntityManagerProvider.getEntityManager().getEntityManagerFactory().getCache()
					.evict(entityClass, newEntity.getId());

			postSaveAction(newEntity);
			CrudEventDistributer.publishEvent(this, eventType, newEntity);

			// select has been moved to here because when it happens earlier,
			// child cruds are caused to discard their data before saving it for
			// a new record

			container.refreshItem(newEntity.getId());
			entityTable.select(newEntity.getId());

			splitPanel.showFirstComponet();
			Notification.show("Changes Saved", "Any changes you have made have been saved.", Type.TRAY_NOTIFICATION);

		}

		catch (Exception e)
		{
			if (e.getCause() instanceof PersistenceException)
			{
				handlePersistenceException(e);
			}
			else if (e instanceof InvalidValueException)
			{
				InvalidValueException m = (InvalidValueException) e;

				Notification.show("Please fix the form errors and then try again.\n\n " + m.getMessage(),
						Type.ERROR_MESSAGE);
			}
			else if (e.getCause() instanceof InvalidValueException)
			{
				InvalidValueException m = (InvalidValueException) e.getCause();
				Notification.show("Please fix the form errors and then try again.\n\n " + m.getMessage(),
						Type.ERROR_MESSAGE);
			}
			else
			{
				handleConstraintViolationException(e);
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
			saveButton.setEnabled(true);
		}

	}

	static private void handlePersistenceException(Exception e)
	{
		if (e.getCause() instanceof PersistenceException)
		{
			String tmp = e.getMessage();
			PersistenceException pex = (PersistenceException) e.getCause();
			if (pex.getCause() instanceof DatabaseException)
			{
				DatabaseException dex = (DatabaseException) pex.getCause();
				tmp = dex.getMessage();
				if (tmp.indexOf("Query being") > 0)
				{
					// strip of the query
					tmp = tmp.substring(0, tmp.indexOf("Query being"));

					if (tmp.contains("MySQL"))
					{
						tmp = tmp.substring(tmp.indexOf("MySQL") + 5);
					}
				}
			}
			logger.error(e, e);
			throw new RuntimeException(tmp);
		}
	}

	/**
	 * logs the initial error and calls the recusive version of it'self. always
	 * throws a runtime exception
	 * 
	 * @param e
	 */
	public static void handleConstraintViolationException(Throwable e)
	{
		if (e instanceof RuntimeException && e.getCause() instanceof Buffered.SourceException)
		{
			SourceException ex = (Buffered.SourceException) e.getCause();
			if (ex.getCause() instanceof PersistenceException)
			{
				handlePersistenceException(ex);
			}

		}
		logger.error(e, e);
		handleConstraintViolationException(e, 5);
		throw new RuntimeException(e);
	}

	/**
	 * digs down looking for a useful exception, it will throw a runtime
	 * exception if it finds an useful exception
	 * 
	 * @param e
	 * @param nestLimit
	 */
	private static void handleConstraintViolationException(Throwable e, int nestLimit)
	{
		if (nestLimit > 0 && e != null)
		{
			nestLimit--;
			if (e instanceof DescriptorException)
			{
				DescriptorException desc = (DescriptorException) e;
				Notification.show(desc.getMessage(), Type.ERROR_MESSAGE);

				throw new RuntimeException(desc.getMessage());
			}
			if (e instanceof ConstraintViolationException)
			{
				String groupedViolationMessage = e.getClass().getSimpleName() + " ";
				for (ConstraintViolation<?> violation : ((ConstraintViolationException) e).getConstraintViolations())
				{
					logger.error(violation.getLeafBean().getClass().getCanonicalName() + " " + violation.getLeafBean());
					String violationMessage = violation.getLeafBean().getClass().getSimpleName() + " "
							+ violation.getPropertyPath() + " " + violation.getMessage() + ", the value was "
							+ violation.getInvalidValue();
					logger.error(violationMessage);
					groupedViolationMessage += violationMessage + "\n";
				}
				Notification.show(groupedViolationMessage, Type.ERROR_MESSAGE);
				throw new RuntimeException(groupedViolationMessage);
			}

			handleConstraintViolationException(e.getCause(), nestLimit);

		}
	}

	/**
	 * commits the container and retrieves the new recordid
	 * 
	 * we have to hook the ItemSetChangeListener to be able to get the database
	 * id of a new record.
	 */
	private E commitContainerAndGetEntityFromDB()
	{
		// don't really need an AtomicReference, just using it as a mutable
		// final variable to be used in the callback
		final AtomicReference<E> newEntity = new AtomicReference<E>();

		// call back to collect the id of the new record when the container
		// fires the ItemSetChangeEvent
		ItemSetChangeListener tmp = new ItemSetChangeListener()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 9132090066374531277L;

			@Override
			public void containerItemSetChange(ItemSetChangeEvent event)
			{
				if (event instanceof ProviderChangedEvent)
				{
					@SuppressWarnings("rawtypes")
					ProviderChangedEvent pce = (ProviderChangedEvent) event;
					@SuppressWarnings("unchecked")
					Collection<E> affectedEntities = pce.getChangeEvent().getAffectedEntities();

					if (affectedEntities.size() > 0)
					{
						@SuppressWarnings("unchecked")
						E id = (E) affectedEntities.toArray()[0];
						newEntity.set(id);

					}
				}
			}
		};

		try
		{
			// add the listener
			container.addItemSetChangeListener(tmp);
			// call commit
			container.commit();
			newEntity.set(EntityManagerProvider.getEntityManager().merge(newEntity.get()));
		}
		catch (Exception e)
		{
			handleConstraintViolationException(e);
		}
		finally
		{
			// detach the listener
			container.removeItemSetChangeListener(tmp);
		}

		// return the entity
		return newEntity.get();
	}

	/**
	 * called after a record has been committed to the database
	 */
	protected void postSaveAction(E entityItem)
	{

	}

	/**
	 * opportunity for implementing classes to modify or add data to the entity
	 * being saved.
	 * 
	 * NOTE: modify the item properties not the entity as accessing the entity
	 * is unreliable
	 * 
	 * @param item
	 * @throws Exception
	 */
	protected void interceptSaveValues(EntityItem<E> entityItem) throws Exception
	{
	}

	private void initSearch()
	{
		searchField.setInputPrompt("Search");
		searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		searchField.setImmediate(true);
		searchField.addTextChangeListener(new TextChangeListener()
		{
			private static final long serialVersionUID = 1L;

			public void textChange(final TextChangeEvent event)
			{
				try
				{
					// If advanced search is active then it should be
					// responsible
					// for triggering the filter.
					if (!advancedSearchOn)
					{
						String filterString = event.getText();
						triggerFilter(filterString);
					}
				}
				catch (Exception e)
				{
					logger.error(e, e);
					Notification.show(e.getClass().getSimpleName()+" "+e.getMessage(),Type.ERROR_MESSAGE);
				}
			}
		});

		searchField.focus();
	}

	/**
	 * call this method to cause filters to be applied
	 */
	protected void triggerFilter()
	{
		triggerFilter(searchField.getValue().trim());
	}

	private void triggerFilter(String searchText)
	{
		boolean advancedSearchActive = advancedSearchOn;
		if (advancedSearchActive || searchText.length() > 0)
		{
			Filter filter = getContainerFilter(searchText, advancedSearchActive);
			applyFilter(filter);
		}
		else
		{
			resetFilters();
		}

	}

	public void applyFilter(final Filter filter)
	{
		try
		{
			/* Reset the filter for the Entity Container. */
			resetFilters();
			container.addContainerFilter(filter);
			container.discard();

			entityTable.select(entityTable.firstItemId());
		}
		catch (Exception e)
		{
			handleConstraintViolationException(e);
		}

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
	abstract protected Filter getContainerFilter(String filterString, boolean advancedSearchActive);

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

										for (ChildCrudListener<E> child : childCrudListeners)
										{
											child.discard();
										}

										if (restoreDelete)
										{
											activateEditMode(false);
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
			try
			{
				callback.allowRowChange();
			}
			catch (Exception e)
			{
				handleConstraintViolationException(e);

			}
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
			commitListener.selectedParentRowChanged(item);
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

	protected void showNoSelectionMessage()
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

	protected void commitFieldGroup() throws CommitException
	{
		formValidate();
		selectFirstErrorFieldAndShowTab();
		fieldGroup.commit();
		for (ChildCrudListener<E> child : childCrudListeners)
		{
			child.validateFieldz();
		}

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
			EntityItem<E> entityItem = entityTable.getCurrent();
			if (entityItem != null)
			{
				entity = entityItem.getEntity();
			}
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

					searchField.setValue("");
					resetFilters();

					createNewEntity();

					rowChanged(newEntity);
					// Can't delete when you are adding a new record.
					// Use cancel instead.
					if (applyButton.isVisible())
					{
						restoreDelete = true;
						activateEditMode(true);
					}

					rightLayout.setVisible(true);

					selectFirstFieldAndShowTab();

					postNew(newEntity);
				}
				catch (Exception e)
				{
					handleConstraintViolationException(e);
				}
			}

		});
	}

	protected void selectFirstFieldAndShowTab()
	{
		for (Field<?> field : fieldGroup.getFields())
		{
			Component childField = field;

			for (int i = 0; i < 10; i++)
			{
				Component parentField = childField.getParent();
				if (parentField instanceof TabSheet)
				{
					((TabSheet) parentField).setSelectedTab(childField);
					break;
				}
				childField = parentField;
			}
			field.focus();
			break;
		}
	}

	protected void selectFirstErrorFieldAndShowTab()
	{
		for (Field<?> field : fieldGroup.getFields())
		{

			try
			{
				field.validate();
			}
			catch (Exception e)
			{
				Component childField = field;

				for (int i = 0; i < 10; i++)
				{
					Component parentField = childField.getParent();
					if (parentField instanceof TabSheet)
					{
						((TabSheet) parentField).setSelectedTab(childField);
						break;
					}
					childField = parentField;
				}
				break;
			}

		}
	}

	/**
	 * you might want to implement this method in a child crud that needs to
	 * load some sort of list when a new entity is created based on the parent
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected void createNewEntity() throws InstantiationException, IllegalAccessException
	{
		newEntity = container.createEntityItem(preNew());
	}

	/**
	 * override this method if you have child entities, you can use this
	 * opportunity to do some dirty hacking to populate fields
	 * 
	 * @param newEntity
	 */

	protected void postNew(EntityItem<E> newEntity)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Override this method if you need to initialise the entity when a new
	 * record is created.
	 * 
	 * @param newEntity
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	protected E preNew() throws InstantiationException, IllegalAccessException
	{
		return entityClass.newInstance();
	}

	/**
	 * after making changes via JPA, to get the crud to see the changes call
	 * this method if needed.
	 * 
	 * beware it is a costly opperation.
	 * 
	 * commit the transaction and flush to db, then refresh the container
	 */
	public void reloadDataFromDB()
	{
		EntityManagerProvider.getEntityManager().getTransaction().commit();
		EntityManagerProvider.getEntityManager().getTransaction().begin();
		EntityManagerProvider.getEntityManager().flush();
		container.refresh();
	}

	/**
	 * for child cruds, they overload this to ensure that the minimum necessary
	 * filters are always applied.
	 */
	protected void resetFilters()
	{
		try
		{
			container.removeAllContainerFilters();
			((EntityTable<E>) this.entityTable).refreshRowCache();
		}
		catch (Exception e)
		{
			handleConstraintViolationException(e);

		}
	}

	protected boolean isNew()
	{
		return this.newEntity != null;
	}

	public JPAContainer<E> getContainer()
	{
		return container;

	}

	// disabled as the save/cancel enable/disable is buggy
	@Override
	public void fieldGroupIsDirty(boolean b)
	{
		// saveButton.setEnabled(b);
		// cancelButton.setEnabled(b);
	}

	Set<ChildCrudListener<E>> getChildCrudListeners()
	{
		return Collections.unmodifiableSet(childCrudListeners);
	}

	protected DeleteVetoResponseData canDelete(E entity)
	{
		return new DeleteVetoResponseData(true);
	}

	protected Button getSaveButton()
	{
		return saveButton;
	}
}
