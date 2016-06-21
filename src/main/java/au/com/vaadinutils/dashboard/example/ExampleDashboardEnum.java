package au.com.vaadinutils.dashboard.example;

import au.com.vaadinutils.dashboard.PortalAdderIfc;
import au.com.vaadinutils.dashboard.PortalEnumIfc;

public enum ExampleDashboardEnum implements PortalEnumIfc
{

	EXAMPLE
	{
		@Override
		public PortalAdderIfc instancePortalAdder(String guid)
		{
			return new ExamplePortal(guid);
		}
	};

	@Override
	abstract public PortalAdderIfc instancePortalAdder(String guid);

}
