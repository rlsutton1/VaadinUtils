package au.com.vaadinutils.dashboard;

import java.util.Collection;

import org.vaadin.alump.gridstack.GridStackLayoutNoJQuery;
import org.vaadin.alump.gridstack.GridStackMoveEvent;
import org.vaadin.alump.gridstack.GridStackMoveEvent.GridStackMoveListener;

public class DashBoardNoJQuery extends GridStackLayoutNoJQuery
{
	// Logger logger = LogManager.getLogger();

	private static final long serialVersionUID = 1L;

	public DashBoardNoJQuery()
	{
		super(8);

		setVerticalMargin(12);
		setMinWidth(150);

		// See styles.scss of this demo project how to handle columns sizes on
		// CSS size
		addStyleName("eight-column-grid-stack");

		// One cell height is set to 80 pixels
		setCellHeight(40);

		setSizeFull();

		addGridStackMoveListener(new GridStackMoveListener()
		{

			@Override
			public void onGridStackMove(Collection<GridStackMoveEvent> events)
			{
				for (GridStackMoveEvent event : events)
				{
					((Portal) event.getMovedChild()).savePosition(event);
				}

			}

		});

	}

}
