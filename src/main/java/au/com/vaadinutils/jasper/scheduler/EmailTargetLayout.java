package au.com.vaadinutils.jasper.scheduler;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipient;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipientVisibility;
import au.com.vaadinutils.validator.EmailValidator;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class EmailTargetLayout extends VerticalLayout
{
	private static final long serialVersionUID = -6108970593368681878L;

	Logger logger = LogManager.getLogger();
	List<EmailTargetLine> lines = new LinkedList<EmailTargetLine>();

	private final static int lineHeight = 30;

	EmailTargetLayout()
	{
		setSizeFull();
		setHeight("" + lineHeight);
		setSpacing(true);

	}

	private EmailTargetLine insertTargetLine(final int row, ReportEmailRecipient recip)
	{

		final HorizontalLayout recipientHolder = new HorizontalLayout();
		recipientHolder.setSizeFull();
		recipientHolder.setSpacing(true);
		recipientHolder.setHeight("30");

		final List<ReportEmailRecipientVisibility> targetTypes = new LinkedList<ReportEmailRecipientVisibility>();
		for (ReportEmailRecipientVisibility rerv : ReportEmailRecipientVisibility.values())
		{
			targetTypes.add(rerv);
		}

		final EmailTargetLine line = new EmailTargetLine();
		line.row = row;

		line.targetTypeCombo = new ComboBox(null, targetTypes);
		line.targetTypeCombo.setWidth("80");
		line.targetTypeCombo.select(targetTypes.get(0));

		line.targetAddress = new ComboBox(null);
		line.targetAddress.setImmediate(true);
		line.targetAddress.setTextInputAllowed(true);
		line.targetAddress.setInputPrompt("Enter Contact Name or email address");
		line.targetAddress.setWidth("100%");
		line.targetAddress.addValidator(new EmailValidator("Please enter a valid email address."));

		getValidEmailContacts(line.targetAddress);
		line.targetAddress.setItemCaptionPropertyId("namedemail");
		line.targetAddress.setNewItemsAllowed(true);

		if (recip != null && recip.getEmail() != null)
		{
			line.targetAddress.setValue(recip.getEmail());
			line.targetTypeCombo.setValue(recip.getVisibility());
		}

		line.targetAddress.setNewItemHandler(new NewItemHandler()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void addNewItem(final String newItemCaption)
			{
				final IndexedContainer container = (IndexedContainer) line.targetAddress.getContainerDataSource();

				final Item item = addItem(container, "", newItemCaption);
				if (item != null)
				{
					line.targetAddress.addItem(item.getItemProperty("id").getValue());
					line.targetAddress.setValue(item.getItemProperty("id").getValue());
				}
				setHeight(calculateHeight());
			}
		});

		if (recip != null)
		{

		}

		if (row == 0)
		{
			line.actionButton = new Button("+");
			line.actionButton.setDescription("Click to add another email address line.");
			line.actionButton.setStyleName(Reindeer.BUTTON_SMALL);
			line.actionButton.addClickListener(new ClickListener()
			{

				private static final long serialVersionUID = 6505218353927273720L;

				@Override
				public void buttonClick(ClickEvent event)
				{
					lines.add(insertTargetLine(lines.size(), null));
					setHeight(calculateHeight());
				}
			});
		}
		else
		{
			line.actionButton = new Button("-");
			line.actionButton.setDescription("Click to remove this email address line.");
			line.actionButton.setStyleName(Reindeer.BUTTON_SMALL);
			line.actionButton.addClickListener(new ClickListener()
			{

				private static final long serialVersionUID = 3104323607502279386L;

				@Override
				public void buttonClick(ClickEvent event)
				{
					removeComponent(recipientHolder);
					lines.remove(line);
					setHeight(calculateHeight());

				}
			});
		}

		recipientHolder.addComponent(line.targetTypeCombo);
		recipientHolder.addComponent(line.targetAddress);
		recipientHolder.addComponent(line.actionButton);
		recipientHolder.setExpandRatio(line.targetAddress, 1);

		addComponent(recipientHolder);

		return line;
	}

	@SuppressWarnings("unchecked")
	private void getValidEmailContacts(ComboBox targetAddress)
	{

		JpaBaseDao<ReportEmailRecipient, Long> reportEmailRecipient = JpaBaseDao.getGenericDao(ReportEmailRecipient.class);

		targetAddress.addContainerProperty("id", String.class, null);
		targetAddress.addContainerProperty("email", String.class, null);
		targetAddress.addContainerProperty("namedemail", String.class, null);

		for (final ReportEmailRecipient contact : reportEmailRecipient.findAll())
		{
			if (contact.getEmail() != null)
			{
				Item item = targetAddress.addItem(contact.getEmail());
				if (item != null)
				{
					item.getItemProperty("email").setValue(contact.getEmail());
					item.getItemProperty("id").setValue(contact.getEmail());
					item.getItemProperty("namedemail").setValue(contact.getEmail());
				}

			}
		}

	}

	@SuppressWarnings("unchecked")
	private Item addItem(final IndexedContainer container, final String named, String email)
	{
		// When we are editing an email (as second time) we can end up with
		// double brackets so we strip them off here.
		if (email.startsWith("<"))
		{
			email = email.substring(1);
		}
		if (email.endsWith(">"))
		{
			email = email.substring(0, email.length() - 1);
		}
		Item item = container.getItem(email);
		if (item == null)
		{
			item = container.addItem(email);
		}
		if (item != null)
		{
			item.getItemProperty("id").setValue(email);
			item.getItemProperty("email").setValue(email);
			String namedEmail;
			if (named != null && named.trim().length() > 0)
			{
				namedEmail = named + " <" + email + ">";
			}
			else
			{
				namedEmail = "<" + email + ">";
			}
			item.getItemProperty("namedemail").setValue(namedEmail);
		}
		else
		{
			logger.error("Failed to find or create the recipient");
		}
		return item;
	}


	public List<EmailTargetLine> getTargets()
	{
		return lines;
	}

	public void add(ReportEmailRecipient target)
	{
		lines.add(insertTargetLine(lines.size(), target));
		setHeight(calculateHeight());
	}

	private String calculateHeight()
	{
		return "" + ((lineHeight * lines.size()));
	}

	public void clear()
	{
		lines.clear();
		removeAllComponents();

	}

}
