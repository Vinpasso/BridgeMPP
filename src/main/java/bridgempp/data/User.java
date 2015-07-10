package bridgempp.data;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class User
{
	@Id
	@Column(name = "USER_ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long UserID;
	
	@Column(name = "USER_NAME", nullable = false, length = 50)
	private String name;
	
	@Column(name = "PERMISSIONS", nullable = false)
	private int permissions;
	
	@ManyToMany(mappedBy = "users")
	private Collection<Endpoint> endpoints;
	
	
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

}
