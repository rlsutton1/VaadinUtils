package au.com.vaadinutils.help;

import java.util.concurrent.ExecutionException;

public interface HelpIndex
{

	String lookupHelpIndex(Enum<?> helpId) throws ExecutionException;

	String getPageUrl();

	String getIndexPageUrl();

}
