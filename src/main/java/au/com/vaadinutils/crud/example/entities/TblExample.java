package au.com.vaadinutils.crud.example.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import au.com.vaadinutils.crud.CrudEntity;

/**
 * The persistent class for the tblexternaldatabases database table.
 * 
 */
@Entity

@Table(name = "tblExample")
public class TblExample implements Serializable, CrudEntity
{
	private static final long serialVersionUID = 1L;

	static public final String FIND_ALL = "findAll";

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idtblExternalDatabases;

	private String host;

	
	private String JDBCDriverClass;

	private String name;

	private String password;

	@Column(name="`schema`")
	private String schema;

	private String username;

	public TblExample()
	{
	}

	public Long getIdtblExternalDatabases()
	{
		return this.idtblExternalDatabases;
	}

	public void setIdtblExternalDatabases(Long idtblExternalDatabases)
	{
		this.idtblExternalDatabases = idtblExternalDatabases;
	}

	public String getHost()
	{
		return this.host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public String getJDBCDriverClass()
	{
		return this.JDBCDriverClass;
	}

	public void setJDBCDriverClass(String JDBCDriverClass)
	{
		this.JDBCDriverClass = JDBCDriverClass;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPassword()
	{
		return this.password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getSchema()
	{
		return this.schema;
	}

	public void setSchema(String schema)
	{
		this.schema = schema;
	}

	
	public String getUsername()
	{
		return this.username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	@Override
	public Long getId()
	{
		return idtblExternalDatabases;
	}

	@Override
	public void setId(Long id)
	{
		idtblExternalDatabases = id;
		
	}

}