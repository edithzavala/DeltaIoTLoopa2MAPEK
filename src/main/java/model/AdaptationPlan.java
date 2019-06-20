package model;

import java.util.Map;

public class AdaptationPlan {
	private Map<AdaptationType, MonitorConfiguration> monitorsToAdapt;

	public Map<AdaptationType, MonitorConfiguration> getMonitorsToAdapt() {
		return monitorsToAdapt;
	}

	public void setMonitorsToAdapt(Map<AdaptationType, MonitorConfiguration> monitorsToAdapt) {
		this.monitorsToAdapt = monitorsToAdapt;
	}
}
