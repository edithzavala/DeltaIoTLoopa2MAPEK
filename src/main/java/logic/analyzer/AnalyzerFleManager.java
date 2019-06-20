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

package logic.analyzer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageBody;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.element.functionallogic.enactor.analyzer.IAnalyzerFleManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.policy.IPolicy;
import org.loopa.policy.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.MonitorConfiguration;
import model.MonitorStatus;
import model.MonitorSymptom;
import model.MonitorsConfig;
import model.Symptom;

public class AnalyzerFleManager implements IAnalyzerFleManager {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	private IPolicy allPolicy = new Policy(this.getClass().getName(), new HashMap<String, String>());
	private Map<String, Integer> monitorSymptoms = new HashMap<>();
	private Map<String, Integer> monitorMinSymptoms = new HashMap<>();
	private MonitorsConfig policy;
	private ILoopAElementComponent owner;

	@Override
	public void processLogicData(Map<String, String> data) {
		LOGGER.info("Receive data");
		ObjectMapper mapper = new ObjectMapper();
		try {
			MonitorStatus ms = mapper.readValue(data.get("content"), MonitorStatus.class);
			if (ms.getResponseTime() > 450) {
				LOGGER.info("Monitor " + ms.getMonitorId() + " slow(" + this.monitorSymptoms.get(ms.getMonitorId())
				+ ")");
				if (this.monitorSymptoms.get(ms.getMonitorId()) == this.monitorMinSymptoms.get(ms.getMonitorId())) {
					MonitorSymptom msym = new MonitorSymptom();
					msym.setMonitorId(ms.getMonitorId());
					msym.setSymptom(Symptom.SLOW);
					this.monitorSymptoms.put(ms.getMonitorId(), 0);
					sendDataToPlanner(mapper.writeValueAsString(msym));
				} else {
					this.monitorSymptoms.put(ms.getMonitorId(), this.monitorSymptoms.get(ms.getMonitorId()) + 1);
				}
			} else if (ms.getState() == 404) {
				LOGGER.info("Monitor " + ms.getMonitorId() + " down(" + this.monitorSymptoms.get(ms.getMonitorId())
				+ ")");
				if (this.monitorSymptoms.get(ms.getMonitorId()) == this.monitorMinSymptoms.get(ms.getMonitorId())) {
					MonitorSymptom msym = new MonitorSymptom();
					msym.setMonitorId(ms.getMonitorId());
					msym.setSymptom(Symptom.DOWN);
					this.monitorSymptoms.put(ms.getMonitorId(), 0);
					sendDataToPlanner(mapper.writeValueAsString(msym));
				} else {
					this.monitorSymptoms.put(ms.getMonitorId(), this.monitorSymptoms.get(ms.getMonitorId()) + 1);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addMonitor(MonitorConfiguration mc) {
		this.monitorMinSymptoms.put(mc.getId(), mc.getMinSymptoms());
		this.monitorSymptoms.put(mc.getId(), 1);
	}
	
	private void sendDataToPlanner(String monSymp) {
		LoopAElementMessageBody messageContent = new LoopAElementMessageBody("PLAN", monSymp);
		messageContent.getMessageBody().put("contentType", "analysisData");
		String code = this.getComponent().getElement().getElementPolicy().getPolicyContent()
				.get(LoopAElementMessageCode.MSSGOUTFL.toString());
		IMessage mssg = new Message(this.owner.getComponentId(), this.allPolicy.getPolicyContent().get(code),
				Integer.parseInt(code), MessageType.REQUEST.toString(), messageContent.getMessageBody());
		((ILoopAElementComponent) this.owner.getComponentRecipient(mssg.getMessageTo()).getRecipient())
				.doOperation(mssg);
	}

	@Override
	public ILoopAElementComponent getComponent() {
		return this.owner;
	}

	@Override
	public void setComponent(ILoopAElementComponent c) {
		this.owner = c;

	}

	@Override
	public void setConfiguration(Map<String, String> config) {
		if (config.containsKey("config")) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				this.policy = mapper.readValue(config.get("config"), MonitorsConfig.class);
				for (MonitorConfiguration m : this.policy.getMonitorsl1()) {
					this.monitorSymptoms.put(m.getId(), 1);
					this.monitorMinSymptoms.put(m.getId(), m.getMinSymptoms());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.allPolicy.update(new Policy(this.allPolicy.getPolicyOwner(), config));
	}
}
