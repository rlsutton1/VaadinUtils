package au.com.vaadinutils.wizards.bulkJasperEmail;


import java.util.Collection;

import org.vaadin.teemu.wizards.WizardStep;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.HeadingPropertySet;
import au.com.vaadinutils.crud.SelectableEntityTable;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

public class SelectRecipientsStep<C extends CrudEntity> implements WizardStep
{
	private SelectableEntityTable<C> selectableTable;
	private VerticalLayout layout;

	public SelectRecipientsStep(WizardView<?, C, ?> wizardView)
	{
		layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSizeFull();

		EntityContainer<C> childContainer = wizardView.getChildContainer();
		
		HeadingPropertySet<C> headings = wizardView.getVisibleSelectColumns();
		
		selectableTable = new SelectableEntityTable<C>(childContainer, headings,this.getClass().getSimpleName());
		selectableTable.setSizeFull();
		layout.addComponent(selectableTable);

	}

	@Override
	public String getCaption()
	{
		return "Select Recipients";
	}

	@Override
	public Component getContent()
	{
		
		
		return layout;
	}

	@Override
	public boolean onAdvance()
	{
		boolean advance = true;
		if (selectableTable.size() == 0)
		{
			advance = false;
			Notification.show("You must select at least one recipient.");
		}
		return advance;
	}

	@Override
	public boolean onBack()
	{
		return true;
	}

	
	public Collection<?> getRecipientIds()
	{
		return selectableTable.getSelectedIds();
	}

	public int getRecipientCount()
	{
		return selectableTable.getSelectedIds().size();
	}

}
