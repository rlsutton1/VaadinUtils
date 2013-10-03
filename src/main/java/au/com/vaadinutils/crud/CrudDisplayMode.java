package au.com.vaadinutils.crud;


public enum CrudDisplayMode
{
	VERTICAL
	{
		@Override
		public CrudPanelPair getContainer()
		{

			return new CrudPanelSplitPairVertical();
		}
	},
	HORIZONTAL
	{
		@Override
		public CrudPanelPair getContainer()
		{
			return new CrudPanelSplitPairHorizontal();
		}
	},
	HIDDING
	{
		@Override
		public CrudPanelPair getContainer()
		{
			return new CrudPanelSplitPairHidding();
		}
	};

	abstract public CrudPanelPair getContainer();

}
