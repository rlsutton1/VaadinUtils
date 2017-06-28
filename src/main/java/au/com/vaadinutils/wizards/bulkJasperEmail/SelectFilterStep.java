package au.com.vaadinutils.wizards.bulkJasperEmail;

import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.ui.Component;

import au.com.vaadinutils.crud.CrudEntity;

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
