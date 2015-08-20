package au.com.vaadinutils.fields;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class OrderingListSelect<T> extends CustomComponent
{
	public enum Direction
	{
		UP, DOWN
	};

	private ListSelect listSelect;
	private Button upButton;
	private Button downButton;
	private JPAContainer<T> container;
	private List<OrderingListSelectItem> listItems = new ArrayList<>();
	private String pkProperty;
	private String captionProperty;
	private String sortProperty;
	private boolean modified = false;

	public OrderingListSelect(String caption)
	{
		final GridLayout layout = new GridLayout(2, 1);
		listSelect = new ListSelect();
		final VerticalLayout buttonLayout = new VerticalLayout();
		buttonLayout.setSpacing(true);
		layout.addComponent(listSelect, 0, 0);
		layout.addComponent(buttonLayout, 1, 0);
		layout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
		upButton = new Button("▲");
		downButton = new Button("▼");
		buttonLayout.addComponent(upButton);
		buttonLayout.addComponent(downButton);
		setButtonClickListeners();

		listSelect.setCaption(caption);

		setCompositionRoot(layout);
	}

	public OrderingListSelect()
	{
		this(null);
	}

	private void setButtonClickListeners()
	{
		upButton.addClickListener(new ClickListener()
		{

			@Override
			public void buttonClick(ClickEvent event)
			{
				if (listSelect != null && !listSelect.isEmpty())
				{
					moveItem((Long) listSelect.getValue(), Direction.UP);
				}
			}
		});

		downButton.addClickListener(new ClickListener()
		{

			@Override
			public void buttonClick(ClickEvent event)
			{
				if (listSelect != null && !listSelect.isEmpty())
				{
					moveItem((Long) listSelect.getValue(), Direction.DOWN);
				}
			}
		});
	}

	public void build()
	{
		// Populate list select and build internal list of items
		listSelect.removeAllItems();
		listItems.clear();
		for (Object itemId : container.getItemIds())
		{
			Item item = container.getItem(itemId);
			Long pk = (Long) item.getItemProperty(pkProperty).getValue();
			String caption = (String) item.getItemProperty(captionProperty).getValue();

			OrderingListSelectItem newItem = new OrderingListSelectItem(pk, caption);
			listItems.add(newItem);
			listSelect.addItem(pk);
			listSelect.setItemCaption(pk, caption);
		}

		listSelect.setWidth("300");

	}

	private void moveItem(Long selectedPk, Direction direction)
	{
		// Work out the corresponding item in our internal list
		int index = getIndex(selectedPk);

		// Remove the item from our internal list
		OrderingListSelectItem movedItem = listItems.remove(index);

		// And reposition it based on the direction given
		int newIndex = -1;
		if (direction.equals(Direction.UP))
		{
			newIndex = index == 0 ? index : --index;
		}
		else if (direction.equals(Direction.DOWN))
		{
			newIndex = index == listItems.size() ? index : ++index;
		}
		listItems.add(newIndex, movedItem);
//		movedItem.setId(newIndex);

		// Repopulate the list select based on the new order
		listSelect.removeAllItems();
		for (OrderingListSelectItem listItem : listItems)
		{
			final Long pk = listItem.getPk();
			Item item = container.getItem(pk);
			String caption = (String) item.getItemProperty(captionProperty).getValue();

			listSelect.addItem(pk);
			listSelect.setItemCaption(pk, caption);
		}

		// Reselect the existing selected item
		listSelect.setValue(selectedPk);
		setModified(true);
	}

	private int getIndex(Long pk)
	{
		int index = -1;

		for (int i = 0; i < listItems.size(); i++)
		{
			OrderingListSelectItem item = listItems.get(i);
			if (pk.equals(item.getPk()))
			{
				index = i;
				break;
			}
		}

		return index;
	}

	@SuppressWarnings("unchecked")
	public void save()
	{
		for (Object itemId : container.getItemIds())
		{
			Item item = container.getItem(itemId);
			Long pk = (Long) item.getItemProperty(pkProperty).getValue();
			Property<Long> sortOrder = item.getItemProperty(sortProperty);
			int index = getIndex(pk);
			sortOrder.setValue(new Long(index));

		}

		container.commit();
		setModified(false);
	}

	public ListSelect getListSelect()
	{
		return listSelect;
	}

	public void setContainerDataSource(JPAContainer<T> container)
	{
		this.container = container;
	}

	public JPAContainer<T> getContainerDataSource()
	{
		return container;
	}

	public void setPkProperty(String pkProperty)
	{
		this.pkProperty = pkProperty;
	}

	public String getPkProperty()
	{
		return pkProperty;
	}

	public void setCaptionProperty(String captionProperty)
	{
		this.captionProperty = captionProperty;
	}

	public String getCaptionProperty()
	{
		return captionProperty;
	}

	public void setSortProperty(String sortProperty)
	{
		this.sortProperty = sortProperty;
	}

	public String getSortProperty()
	{
		return sortProperty;
	}

	public void setNullSelectionAllowed(boolean b)
	{
		listSelect.setNullSelectionAllowed(b);
	}
	
	public void setModified(boolean modified)
	{
		this.modified = modified;
	}
	
	public boolean isModified()
	{
		return modified;
	}
}
