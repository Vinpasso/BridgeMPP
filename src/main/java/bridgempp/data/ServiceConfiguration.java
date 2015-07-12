package bridgempp.data;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class ServiceConfiguration
{
	@Id
	@Column(name = "IDENTIFIER", nullable = false, length = 50)
	private String serviceIdentifier;
	
	@OneToMany(mappedBy="")
	//TODO: MappedBy
	private Collection<Endpoint> endpoints;
	
}
