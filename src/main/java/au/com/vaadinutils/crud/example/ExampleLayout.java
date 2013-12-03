package au.com.vaadinutils.crud.example;

import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.HeadingPropertySet;
import au.com.vaadinutils.crud.MultiColumnFormLayout;
import au.com.vaadinutils.crud.ValidatingFieldGroup;
import au.com.vaadinutils.crud.example.entities.TblExample;
import au.com.vaadinutils.dao.BatchingPerRequestEntityProvider;
import au.com.vaadinutils.layout.TopVerticalLayout;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.VerticalLayout;

/** A start view for navigating to the main view */
public class ExampleLayout extends BaseCrudView<TblExample>
{

	public ExampleLayout()
	{
		JPAContainer<TblExample> container = makeJPAContainer();

		HeadingPropertySet<TblExample> headings = new HeadingPropertySet.Builder<TblExample>()
				.addColumn("Name", "name").addColumn("Host", "host").addColumn("Type", "type").build();

		init(TblExample.class, container, headings);
	}

	public JPAContainer<TblExample> makeJPAContainer()
	{

		JPAContainer<TblExample> container = new JPAContainer<TblExample>(TblExample.class);
		container.setEntityProvider(new BatchingPerRequestEntityProvider<TblExample>(TblExample.class));
		return container;

	}

	private static final long serialVersionUID = 1L;

	public AbstractLayout buildEditor(ValidatingFieldGroup<TblExample> validatingFieldGroup)
	{

		VerticalLayout main = new VerticalLayout();

		MultiColumnFormLayout<TblExample> layout = new MultiColumnFormLayout<TblExample>(1, validatingFieldGroup);
		layout.setColumnFieldWidth(0, 250);

		TopVerticalLayout wrapper = new TopVerticalLayout();
		wrapper.addComponent(layout);
		main.addComponent(wrapper);

		layout.bindTextField("Name", "name");
		layout.bindTextField("Host", "host");
		layout.bindTextField("Schema", "schema");
		layout.bindTextField("Username", "username");
		layout.bindPasswordField("Password", "password");

		return main;

	}


	@Override
	protected String getTitleText()
	{
		return "Example";
	}

	@Override
	protected Filter getContainerFilter(String filterText, boolean advancedSearchActive)
	{
		Filter filter = null;
		String[] searchFields = new String[] { "name" };
		for (String property : searchFields)
		{
			if (filter == null)
			{
				filter = new SimpleStringFilter(property, filterText, true, false);
			}
			filter = new Or(new SimpleStringFilter(property, filterText, true, false), filter);
		}

		return filter;
	}

}
