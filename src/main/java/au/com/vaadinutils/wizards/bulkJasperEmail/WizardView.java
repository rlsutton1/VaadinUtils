package au.com.vaadinutils.wizards.bulkJasperEmail;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import net.sf.jasperreports.engine.JRException;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.HeadingPropertySet;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

abstract public class WizardView<Parent extends CrudEntity, Child extends CrudEntity, R extends Recipient> extends VerticalLayout implements WizardProgressListener
{
	private static final long serialVersionUID = 1L;

	private Wizard wizard;

	private SelectRecipientsStep<Child> recipientStep;
	private SelectFilterStep<?, ?> filterStep;
	private ConfirmDetailsStep confirmStep;
	private ShowProgressStep<Child> send;

	private JPAContainer<Child> childContainer;

	private JPAContainer<Parent> parentContainer;

	private ArrayList<Parameter<String>> parameters;


	public WizardView(JPAContainer<Parent> parentContainer, JPAContainer<Child> childContainer)
	{
		this.parentContainer = parentContainer;
		this.childContainer = childContainer;

		this.parameters = new ArrayList<Parameter<String>>();
		this.parameters.add(new Parameter<String>("Material Due Reminder First.", "MaterialDueReminder1st"));
		this.parameters.add(new Parameter<String>("Material Due Reminder Second.", "MaterialDueReminder2nd"));
	}

	public ShowProgressStep<Child> getSend()
	{
		return send;
	}

	public SelectRecipientsStep<Child> getRecipientStep()
	{
		return recipientStep;
	}

	public void enter(ViewChangeEvent event)
	{
		recipientStep = new SelectRecipientsStep<Child>(this);
		filterStep = new SelectFilterStep<Parent, Child>(this);
		confirmStep = new ConfirmDetailsStep(this);
		send = new ShowProgressStep<Child>(this);

		// create the Wizard component and add the steps
		wizard = new Wizard();
		wizard.setUriFragmentEnabled(true);
		wizard.addListener(this);
		wizard.addStep(filterStep, "filter");
		wizard.addStep(recipientStep, "select");
		wizard.addStep(confirmStep, "confirm");
		wizard.addStep(send, "send");
		wizard.setSizeFull();
		wizard.setUriFragmentEnabled(true);

		/* Main layout */
		this.setMargin(true);
		this.setSpacing(true);
		this.addComponent(wizard);
		this.setComponentAlignment(wizard, Alignment.TOP_CENTER);
		this.setSizeFull();

	}

	@Override
	public void activeStepChanged(WizardStepActivationEvent event)
	{
		// NOOP

	}

	@Override
	public void stepSetChanged(WizardStepSetChangedEvent event)
	{
		Page.getCurrent().setTitle(event.getComponent().getCaption());

	}

	@Override
	public void wizardCompleted(WizardCompletedEvent event)
	{
		this.endWizard("Transmission Completed!");

	}

	@Override
	public void wizardCancelled(WizardCancelledEvent event)
	{
		this.endWizard("Transmission Cancelled!");

	}

	private void endWizard(String message)
	{
		wizard.setVisible(false);
		Notification.show(message);
		Page.getCurrent().setTitle(message);
		Page.getCurrent().setLocation("");
	}

	public EntityContainer<Child> getChildContainer()
	{
		return childContainer;
	}

	public EntityContainer<Parent> getParentContainer()
	{
		return parentContainer;
	}


	public List<Parameter<String>> getParameters()
	{
		return parameters;
	}

	static public class Parameter<P>
	{
		private String description;
		private P value;

		public P getValue()
		{
			return value;
		}

		Parameter(String description, P value)
		{
			this.description = description;
			this.value = value;
		}

		public String getDescription()
		{
			return description;
		}
		
		public String toString()
		{
			return this.description;
		}
	}

	public SelectFilterStep<?, ?> getFilterStep()
	{
		return filterStep;
	}

	abstract protected AbstractLayout buildFilter();
	
	abstract protected AbstractLayout buildConfirm();
	
	
	abstract protected R getRecipient(Long recipientId);

	abstract protected HeadingPropertySet<Child> getVisibleSelectColumns();

	abstract protected SingularAttribute<Parent, String> getParentDisplayProperty();


	abstract protected SingularAttribute<Child, Parent> getChildForeignAttribute();

	abstract protected String getChildDisplayProperty();

	abstract protected JasperProxy getJasperProxy() throws JRException;

	abstract protected boolean validateFilter();

}
