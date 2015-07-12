package bridgempp.data;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Version;

@Entity
public class User
{
	@Id
	@Column(name = "IDENTIFIER", nullable = false, length = 50)
	private String identifier;

	@Column(name = "USER_NAME", nullable = false, length = 50)
	private String name;
	
	@Column(name = "PERMISSIONS", nullable = false)
	private int permissions;
	
	@ManyToMany(mappedBy = "users")
	private Collection<Endpoint> endpoints;
	
	@Version
	@Column(name = "LAST_UPDATED_TIME")
	private Date lastUpdatedTime;
	
	User(String identifier)
	{
		this.identifier = identifier;
		this.name = "";
		this.permissions = 0;
	}
	
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the permissions
	 */
	public int getPermissions()
	{
		return permissions;
	}

	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(int permissions)
	{
		this.permissions = permissions;
	}
	
	public String toString()
	{
		return (name.length() == 0)?identifier:name;
	}

	public boolean hasAlias()
	{
		return name.length() > 0;
	}
}