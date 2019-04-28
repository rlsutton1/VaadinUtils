package au.com.vaadinutils.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

public class JpaSettings
{
	static Map<String, Object> queryHints = new HashMap<>();

	public static void addQueryHint(final String hintName, final Object value)
	{
		JpaSettings.queryHints.put(hintName, value);
	}

	public static void addQueryHints(final Map<String, Object> queryHints)
	{
		JpaSettings.queryHints.putAll(queryHints);
	}

	public static void setQueryHints(final Query query)
	{
		for (Entry<String, Object> queryHint : queryHints.entrySet())
		{
			query.setHint(queryHint.getKey(), queryHint.getValue());
		}
	}
}
