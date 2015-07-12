package bridgempp.statistics;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity(name = "STATISTIC_STORE")
public class StatisticStore {

	@Id
	@Column(name = "STATISTICS_ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long statisticsID;
	
	@Version
	@Column(name = "VERSION", nullable = false)
	private long statisticsVersion;
	
	public int messageCount = 0;
	public long lastStartup = 0;
	public long uptime = 0;
	public long installDate = 0;

}
