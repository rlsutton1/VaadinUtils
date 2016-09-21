package au.com.vaadinutils.dao;

import javax.persistence.metamodel.Attribute;

public class JoinOnBuilder<K, V>
{
	enum JoinOnType
	{
		EQUAL, IN;
	}

	private Attribute<K, V> attribute;
	private Object value;
	private JoinOnType type;

	public JoinOnBuilder(final Attribute<K, V> attribute, final Object value, final JoinOnType type)
	{
		this.attribute = attribute;
		this.value = value;
		this.type = type;
	}

	public Attribute<K, V> getAttribute()
	{
		return attribute;
	}

	public Object getValue()
	{
		return value;
	}

	public JoinOnType getType()
	{
		return type;
	}
}