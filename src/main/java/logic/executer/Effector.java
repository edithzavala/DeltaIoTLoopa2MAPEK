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

package logic.executer;

import java.io.IOException;
import java.util.Arrays;

import org.loopa.comm.message.IMessage;
import org.loopa.element.functionallogic.enactor.IFunctionalLogicEnactor;
import org.loopa.executer.effector.IEffector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import application.Application;
import logic.MonitorL1DeltaIoTClient;
import logic.analyzer.AnalyzerFleManager;
import model.AdaptationPlan;
import model.AdaptationType;
import model.DeltaIoTRecepient;

public class Effector implements IEffector {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	private final MonitorL1DeltaIoTClient mdcli = new MonitorL1DeltaIoTClient();

	@Override
	public void effect(IMessage t) {
//		LOGGER.info("Receive message - code:" + t.getMessageCode() + " type:" + t.getMessageCode() + " from:"
//				+ t.getMessageFrom() + " to:" + t.getMessageTo() + " body:" + t.getMessageBody().toString());
		ObjectMapper mapper = new ObjectMapper();
		try {
			AdaptationPlan ap = mapper.readValue(t.getMessageBody().get("content"), AdaptationPlan.class);
			ap.getMonitorsToAdapt().forEach((type, config) -> {
				if (type.equals(AdaptationType.UP)) {
					// TODO next version: create application.json, Dockerfile and docker-compose.yml
					// using the config indictaed in message t
					ProcessBuilder processBuilder = new ProcessBuilder();
					if (ap.getMonitorsToAdapt().size() == 1) {
						processBuilder.command("bash", "-c", "./monitors_up.sh");//all motes
					}else {
						processBuilder.command("bash", "-c", "./monitors_up2.sh");//less motes
					}
					Process process;
					try {
						process = processBuilder.start();
						// TODO next version: this notification should go through KB
						if (ap.getMonitorsToAdapt().size() == 1) {
							Application.loop.getAMLoop().getMonitor().removeElementRecipient("iotmonitorl1m2");
						}
						DeltaIoTRecepient rec = new DeltaIoTRecepient();
						rec.setId(config.getId());
						rec.setTypeOfData(Arrays.asList("STATUS", "ADAPT"));
						rec.setRecipient("http://localhost:8097/");
						Application.loop.getAMLoop().getMonitor().addElementRecipient(rec);
						((AnalyzerFleManager) ((IFunctionalLogicEnactor) Application.loop.getAMLoop().getAanalyzer()
								.getFunctionalLogic().getMessageManager()).getManager()).addMonitor(config);
						// LOGGER.info(output.toString());
						LOGGER.info("Adapted");
					} catch (IOException e) {// )| InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					mdcli.adaptMonitor((String)Application.loop.getAMLoop().getExecuter().getElementRecipient(t.getMessageTo()).getRecipient(),t);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
