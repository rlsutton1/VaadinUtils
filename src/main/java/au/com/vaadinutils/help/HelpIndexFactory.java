package au.com.vaadinutils.help;

import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;

public class HelpIndexFactory
{

	final private static AtomicReference<HelpIndex> index = new AtomicReference<HelpIndex>();

	public static void registerHelpIndex(HelpIndex index)
	{
		HelpIndexFactory.index.set(index);
	}

	public static HelpIndex getHelpIndex()
	{
		Preconditions.checkNotNull(index, "Call registerHelpIndex first");
		return index.get();
	}
	// Logger logger = LogManager.getLogger();
}
