package au.com.vaadinutils.dashboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.entity.BaseCrudEntity;

/**
 * The persistent class for the tblportal database table.
 * 
 */
@Entity
@Table(name = "tblportal")
public class Tblportal extends BaseCrudEntity
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6749641927189770291L;

	@JoinColumn(name = "layoutId")
	private Tblportallayout portalLayout;

	@OneToMany(mappedBy = "portal")
	private Set<Tblportalconfig> configs = new HashSet<>();

	private String type;

	public Tblportal()
	{
	}

	@JoinColumn(name = "portalDataId")
	TblPortalData data;

	public String getType()
	{
		return this.type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public Set<Tblportalconfig> getConfigs()
	{
		return Collections.unmodifiableSet(configs);
	}

	public void addConfig(Tblportalconfig config)
	{
		config.setPortal(this);
		configs.add(config);
	}

	public void removeConfig(Tblportalconfig config)
	{
		configs.remove(config);

	}

	public Map<String, Tblportalconfig> createConfigMap()
	{
		Map<String, Tblportalconfig> configMap = new HashMap<>();
		for (Tblportalconfig config : getConfigs())
		{
			configMap.put(config.getKey(), config);
			addConfig(config);
			EntityManagerProvider.persist(config);
		}
		return configMap;
	}

	public void setConfigValue(String key, String value)
	{
		Tblportalconfig config = createConfigMap().get(key);
		if (config == null)
		{
			config = new Tblportalconfig();
			config.setKey(key);
			addConfig(config);
			EntityManagerProvider.persist(config);
		}
		config.setValue(value);

	}

	public String getConfigValue(String key, String defaultValue)
	{
		Tblportalconfig config = createConfigMap().get(key);
		if (config == null)
		{
			return defaultValue;
		}
		return config.getValue();

	}

	public void setPortalLayout(Tblportallayout portalLayout)
	{
		this.portalLayout = portalLayout;
	}

	public Tblportallayout getPortalLayout()
	{
		return portalLayout;
	}

	public TblPortalData getData()
	{
		return data;

	}

}