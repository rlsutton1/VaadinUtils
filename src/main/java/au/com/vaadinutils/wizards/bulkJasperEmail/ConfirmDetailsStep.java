package au.com.vaadinutils.wizards.bulkJasperEmail;

import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.ui.Component;

public class ConfirmDetailsStep implements WizardStep
{

	private WizardView<?, ?, ?> wizardView;

	public ConfirmDetailsStep(WizardView<?, ?, ?> wizardView)
	{
		this.wizardView = wizardView;
	}

	@Override
	public String getCaption()
	{
		return "Confim Details";
	}

	@Override
	public Component getContent()
	{

		return wizardView.buildConfirm();
	}

	@Override
	public boolean onAdvance()
	{
		return true;
	}

	@Override
	public boolean onBack()
	{
		return true;
	}

}
