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

package logic.monitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageBody;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.element.functionallogic.enactor.monitor.IMonitorFleManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.policy.IPolicy;
import org.loopa.policy.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.MonitorFleConfiguration;;

public class MonitorFleManager implements IMonitorFleManager {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	private IPolicy allPolicy = new Policy(this.getClass().getName(), new HashMap<String, String>());
	private final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);
	private ScheduledFuture<?> futureTask;
	private MonitorFleConfiguration policy;
	private ILoopAElementComponent owner;

	@Override
	public void processLogicData(Map<String, String> data) {
		LOGGER.info("Send data to analysis");
		sendDataToAnalysis(data.get("data"));
	}

	private void sendDataToAnalysis(String data) {
		LoopAElementMessageBody messageContent = new LoopAElementMessageBody("ANALYZE", data);
		messageContent.getMessageBody().put("contentType", "monitoringData");
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
				this.policy = mapper.readValue(config.get("config"), MonitorFleConfiguration.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.allPolicy.update(new Policy(this.allPolicy.getPolicyOwner(), config));
	}

	private Runnable getMonitoringTask() {
		Runnable monitoringTask = new Runnable() {
			@Override
			public void run() {
				LoopAElementMessageBody messageContent = new LoopAElementMessageBody("STATUS", null);
				String code = getComponent().getElement().getElementPolicy().getPolicyContent()
						.get(LoopAElementMessageCode.MSSGOUTFL.toString());
				IMessage t = new Message(owner.getComponentId(), allPolicy.getPolicyContent().get(code),
						Integer.parseInt(code), MessageType.REQUEST.toString(), messageContent.getMessageBody());
				((ILoopAElementComponent) owner.getComponentRecipient(t.getMessageTo()).getRecipient()).doOperation(t);
			}
		};
		return monitoringTask;
	}

	public void start() {
		this.futureTask = SCHEDULER.scheduleAtFixedRate(getMonitoringTask(), 0, this.policy.getMonFreq(),
				TimeUnit.MILLISECONDS);
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
