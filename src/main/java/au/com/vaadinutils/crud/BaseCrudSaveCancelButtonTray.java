package au.com.vaadinutils.crud;

import com.google.common.base.Preconditions;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class BaseCrudSaveCancelButtonTray extends HorizontalLayout
{

	private static final long serialVersionUID = 1L;
	private boolean disallowEdit;
	private boolean disallowNew;

	private Button saveButton = new Button("Save");
	private Button cancelButton = new Button("Cancel");

	public BaseCrudSaveCancelButtonTray(boolean disallowEdit, boolean disallowNew, final ButtonListener listener)
	{
		this.disallowEdit = disallowEdit;
		this.disallowNew = disallowNew;

		if (disallowEdit && disallowNew)
		{
			// hide the buttons completely
			setHeight("0");
			return;
		}

		setMargin(new MarginInfo(false, true, false, true));
		setSizeFull();
		setWidth("100%");
		addComponent(cancelButton);
		
		addComponent(saveButton);
		saveButton.setId("CrudSaveButton");
		setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
		setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);
		setHeight("35");

		saveButton.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				listener.saveClicked();

			}
		});
		saveButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
		saveButton.setDisableOnClick(true);

		cancelButton.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				listener.cancelClicked();

			}
		});

		setDefaultState();

	}

	public void startNewPhase()
	{
		Preconditions.checkArgument(!disallowNew, "New is not allowed!");
		saveButton.setEnabled(true);
		cancelButton.setEnabled(true);
	}

	public void setDefaultState()
	{
		saveButton.setEnabled(!disallowEdit);
		cancelButton.setEnabled(!disallowEdit);

	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

}
