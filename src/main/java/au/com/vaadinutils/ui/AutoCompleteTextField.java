package au.com.vaadinutils.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickListener;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.TextField;

public class AutoCompleteTextField<E> extends TextField
{

	private static final long serialVersionUID = 1L;

	private AutoCompeleteQueryListener<E> listener;

	Map<E, String> options = new LinkedHashMap<>();

	private AutoCompleteOptionSelected<E> optionListener;

	private ContextMenu contextMenu;

	/**
	 * <pre>
	 * {@code
	 * sample usage
	 * 
	 * 	AutoCompleteTextFieldV2<PostCode> suburb = new AutoCompleteTextFieldV2<>();
	 * 
	 * suburb.setQueryListener(new AutoCompeleteQueryListener<PostCode>()
	 * {
	 * 
	 * 	    @Override
	 * 	    public void handleQuery(AutoCompleteTextFieldV2<PostCode> field,String queryText)
	 * 	    {
	 * 		    field.addOption(new PostCode(3241),"Title");
	 * 	    }
	 * 	});
	 * 
	 * 	suburb.setOptionSelectionListener(new AutoCompleteOptionSelected<PostCode>()
	 * 	{
	 * 	    
	 * 	    @Override
	 * 	    public void optionSelected(AutoCompleteTextFieldV2<PostCode> field, PostCode option)
	 * 	    {
	 * 		field.setValue(option.getSuburb());
	 * 	    }
	 * 	});
	 * }
	 * </pre>
	 */

	public AutoCompleteTextField()
	{

		contextMenu = new ContextMenu();
		contextMenu.setAsContextMenuOf(this);
		contextMenu.setOpenAutomatically(false);

		setTextChangeEventMode(TextChangeEventMode.LAZY);
		setImmediate(true);
		addTextChangeListener(new TextChangeListener()
		{
			private static final long serialVersionUID = 1L;

			public void textChange(final TextChangeEvent event)
			{
				options.clear();
				if (listener != null)
				{
					listener.handleQuery(AutoCompleteTextField.this, event.getText());
				}
				showOptionMenu();
			}

		});

	}

	private void showOptionMenu()
	{

		contextMenu.removeAllItems();
		contextMenu.open(this);

		for (final Entry<E, String> option : options.entrySet())
		{
			ContextMenuItem menuItem = contextMenu.addItem(option.getValue());
			menuItem.addItemClickListener(new ContextMenuItemClickListener()
			{

				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event)
				{
					optionListener.optionSelected(AutoCompleteTextField.this, option.getKey());
				}
			});
		}

	}

	public void setOptionSelectionListener(AutoCompleteOptionSelected<E> listener)
	{
		this.optionListener = listener;
	}

	public void removeOptionSelectionListener()
	{
		optionListener = null;
	}

	public void setQueryListener(AutoCompeleteQueryListener<E> listener)
	{
		this.listener = listener;
	}

	public void removeQueryListener()
	{
		listener = null;
	}

	public void addOption(E option, String optionLabel)
	{
		options.put(option, optionLabel);
	}
}
