package au.com.vaadinutils.menu;

public class WindowSizerNull implements WindowSizer
{

	@Override
	public int width()
	{
		throw new RuntimeException("Not implemented");
	}

	@Override
	public int height()
	{
		throw new RuntimeException("Not implemented");

	}
	// Logger logger = LogManager.getLogger();
}
