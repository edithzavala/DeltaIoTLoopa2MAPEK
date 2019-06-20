/*******************************************************************************
 * Copyright (c) 2019 Universitat Politécnica de Catalunya (UPC)
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, ITHOUT WARRANTIES 
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 * 
 * Contributors: Edith Zavala
 ******************************************************************************/

package logic.planner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageBody;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.element.functionallogic.enactor.planner.IPlannerFleManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.policy.IPolicy;
import org.loopa.policy.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.AdaptationPlan;
import model.AdaptationType;
import model.MonitorConfiguration;
import model.MonitorSymptom;
import model.MonitorsConfig;
import model.Symptom;

public class PlannerFleManager implements IPlannerFleManager {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	private IPolicy allPolicy = new Policy(this.getClass().getName(), new HashMap<String, String>());
	private Map<String, MonitorConfiguration> activeMonitors = new HashMap<>();
	private MonitorsConfig policy;
	private ILoopAElementComponent owner;

	@Override
	public void processLogicData(Map<String, String> data) {
		LOGGER.info("Receive data");
		ObjectMapper mapper = new ObjectMapper();
		try {
			MonitorSymptom msym = mapper.readValue(data.get("content"), MonitorSymptom.class);
			MonitorConfiguration newMonitor = new MonitorConfiguration();
			newMonitor.setId("iotmonitorl1mn");
			newMonitor.setMinSymptoms(this.activeMonitors.get(msym.getMonitorId()).getMinSymptoms() + 3);
			newMonitor.setMonFreq(this.activeMonitors.get(msym.getMonitorId()).getMonFreq());
			AdaptationPlan ap = new AdaptationPlan();
			Map<AdaptationType, MonitorConfiguration> monitorsToAdapt = new HashMap<>();
			if (msym.getSymptom().equals(Symptom.DOWN)) {
				newMonitor.setMotes(this.activeMonitors.get(msym.getMonitorId()).getMotes());
				this.activeMonitors.remove(msym.getMonitorId());
			} else if (msym.getSymptom().equals(Symptom.SLOW)) {
				newMonitor.setMotes(this.activeMonitors.get(msym.getMonitorId()).getMotes().subList(0,
						this.activeMonitors.get(msym.getMonitorId()).getMotes().size() / 2));
				this.activeMonitors.get(msym.getMonitorId())
						.setMotes(this.activeMonitors.get(msym.getMonitorId()).getMotes().subList(
								(this.activeMonitors.get(msym.getMonitorId()).getMotes().size() / 2),
								this.activeMonitors.get(msym.getMonitorId()).getMotes().size()));
				monitorsToAdapt.put(AdaptationType.CONFIGURE, this.activeMonitors.get(msym.getMonitorId()));
			}
			monitorsToAdapt.put(AdaptationType.UP, newMonitor);
			this.activeMonitors.put(newMonitor.getId(), newMonitor);
			ap.setMonitorsToAdapt(monitorsToAdapt);
			sendPlanToExecute(mapper.writeValueAsString(ap));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendPlanToExecute(String plan) {
		LoopAElementMessageBody messageContent = new LoopAElementMessageBody("EXECUTE", plan);
		messageContent.getMessageBody().put("contentType", "adaptationPlan");
		String code = this.getComponent().getElement().getElementPolicy().getPolicyContent()
				.get(LoopAElementMessageCode.MSSGOUTFL.toString());
		IMessage mssg = new Message(this.owner.getComponentId(), this.allPolicy.getPolicyContent().get(code),
				Integer.parseInt(code), MessageType.REQUEST.toString(), messageContent.getMessageBody());
		((ILoopAElementComponent) this.owner.getComponentRecipient(mssg.getMessageTo()).getRecipient())
				.doOperation(mssg);
	}

	@Override
	public void setConfiguration(Map<String, String> config) {
		if (config.containsKey("config")) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				this.policy = mapper.readValue(config.get("config"), MonitorsConfig.class);
				for (MonitorConfiguration m : this.policy.getMonitorsl1()) {
					this.activeMonitors.put(m.getId(), m);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.allPolicy.update(new Policy(this.allPolicy.getPolicyOwner(), config));
	}

	@Override
	public ILoopAElementComponent getComponent() {
		return this.owner;
	}

	@Override
	public void setComponent(ILoopAElementComponent c) {
		this.owner = c;

	}
}
