package au.com.vaadinutils.wizards.bulkJasperEmail;

import org.vaadin.teemu.wizards.WizardStep;

import au.com.vaadinutils.crud.CrudEntity;

import com.vaadin.ui.Component;

public class SelectFilterStep<Parent extends CrudEntity, Child extends CrudEntity> implements WizardStep
{
	private WizardView<?, ?, ?> wizardView;

	public SelectFilterStep(WizardView<?, ?, ?> wizardView)
	{
		this.wizardView = wizardView;

	}

	@Override
	public String getCaption()
	{
		return "Select entity";
	}

	@Override
	public Component getContent()
	{

		return wizardView.buildFilter();
	}

	@Override
	public boolean onAdvance()
	{

		return wizardView.validateFilter();
	}

	@Override
	public boolean onBack()
	{
		return false;
	}
}
