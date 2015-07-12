package bridgempp.services.whatsapp;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import bridgempp.BridgeService;
import bridgempp.data.ServiceConfiguration;

@Entity(name = "WHATSAPP_SERVICE_CONFIGURATION")
@DiscriminatorValue("WHATSAPP_SERVICE")
public class WhatsappServiceConfiguration extends ServiceConfiguration
{
	@Column(name = "Phone_Number", nullable = false, length = 50)
	private String phone;
	
	@Column(name = "Password", nullable = false, length = 50)
	private String password;
	

	@Override
	protected BridgeService instantiateService()
	{
		WhatsappService service = new WhatsappService();
		service.configure(phone, password);
		this.service = service;
		return service;
	}

}
