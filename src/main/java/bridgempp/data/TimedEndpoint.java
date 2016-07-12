package bridgempp.data;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;

import bridgempp.ShadowManager;
import bridgempp.service.BridgeService;

@Entity(name = "TIMEDENDPOINT")
public class TimedEndpoint extends Endpoint {

	@Column(name = "CREATED", nullable = false)
	Date creationDate;

	@Column(name = "EXPIRE", nullable = false)
	Date expireDate;

	public TimedEndpoint(BridgeService service, String identifier, long expire,
			TemporalUnit timeUnit) {
		super(service, identifier);
		creationDate = Date.from(Instant.now());
		expireDate = Date.from(creationDate.toInstant().plus(expire, timeUnit));
	}

	public void checkExpired() {
		if (Date.from(Instant.now()).after(expireDate)) {
			ShadowManager.log(Level.INFO, "Endpoint " + this.toString()
					+ " has expired, removing...");
			DataManager.removeState(this);
		}
	}

}
