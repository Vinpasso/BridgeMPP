package bridgempp.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity(name = "ACCESS_TOKEN")
public class AccessKey {
    @Id
    @Column(name = "TOKEN", nullable = false, length = 100)
    private String key;
    @Column(name = "PERMISSIONS", nullable = false)
    private int permissions;
    @Column(name = "USE_ONCE", nullable = false)
    private boolean useOnce;

    
    /**
     * JPA Constructor
     */
    AccessKey()
    {
    	
    }

    public AccessKey(String key, int permissions, boolean useOnce) {
        this.key = key;
        this.permissions = permissions;
        this.useOnce = useOnce;
    }

    @Override
    public String toString() {
        return "AccessKey: Permissions " + permissions + " useOnce " + useOnce + " Key " + key;
    }

    /**
     * @return the permissions
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * @return the useOnce
     */
    public boolean isUseOnce() {
        return useOnce;
    }

    /**
     * @return the Key
     */
    public String getKey() {
        return key;
    }

}
