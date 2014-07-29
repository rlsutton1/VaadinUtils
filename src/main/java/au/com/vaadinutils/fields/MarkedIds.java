package au.com.vaadinutils.fields;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class MarkedIds
{
	final Set<Object> markedIds = new TreeSet<Object>();
	private boolean trackingSelected = true;

	private Set<SelectionListener> selectionListeners = new HashSet<SelectionListener>();
	private int containerSize;

	public void addSelectionListener(SelectionListener selectionListener)
	{
		selectionListeners.add(selectionListener);
	}

	private void updateSelectionListeners()
	{
		int count = markedIds.size();
		if (!trackingSelected)
		{
			count = containerSize - count;
		}
		for (SelectionListener listener : selectionListeners)
		{

			listener.selectedItems(count);
		}
	}

	public void clear(boolean b, int containerSize)
	{
		markedIds.clear();
		trackingSelected = b;
		this.containerSize = containerSize;
		updateSelectionListeners();
		

	}

	public void addAll(Collection<Long> value)
	{
		markedIds.addAll(value);
		updateSelectionListeners();

	}

	public void add(Object itemId)
	{
		markedIds.add(itemId);
		updateSelectionListeners();

	}

	public void remove(Object itemId)
	{
		markedIds.remove(itemId);
		updateSelectionListeners();

	}

	public boolean contains(Object itemId)
	{
		return markedIds.contains(itemId);
	}

	public void removeAll(Collection<Long> ids)
	{
		markedIds.removeAll(ids);
		updateSelectionListeners();

	}

	public Collection<?> getIds()
	{
		return markedIds;
	}

	// Logger logger = LogManager.getLogger();
	
	public boolean isTrackingSelected()
	{
		return trackingSelected;
	}
}
