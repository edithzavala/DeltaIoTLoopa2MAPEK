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
package logic.loop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.loopa.analyzer.Analyzer;
import org.loopa.analyzer.IAnalyzer;
import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.comm.message.PolicyConfigMessageBody;
import org.loopa.element.functionallogic.enactor.IFunctionalLogicEnactor;
import org.loopa.element.functionallogic.enactor.analyzer.AnalyzerFunctionalLogicEnactor;
import org.loopa.element.functionallogic.enactor.executer.ExecuterFunctionalLogicEnactor;
import org.loopa.element.functionallogic.enactor.knowledgebase.KnowledgeBaseFuncionalLogicEnactor;
import org.loopa.element.functionallogic.enactor.monitor.MonitorFunctionalLogicEnactor;
import org.loopa.element.functionallogic.enactor.planner.PlannerFunctionalLogicEnactor;
import org.loopa.executer.Executer;
import org.loopa.executer.IExecuter;
import org.loopa.knowledgebase.IKnowledgeBase;
import org.loopa.knowledgebase.KnowledgeBase;
import org.loopa.monitor.IMonitor;
import org.loopa.monitor.Monitor;
import org.loopa.planner.IPlanner;
import org.loopa.planner.Planner;
import org.loopa.policy.Policy;
import org.loopa.recipient.IRecipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import logic.MAPMessageSender;
import logic.analyzer.AnalyzerFleManager;
import logic.executer.ExecuterFleManager;
import logic.executer.ExecuterMessageSender;
import logic.knowledgebase.KnowledgeBaseFleManager;
import logic.monitor.MonitorFleManager;
import logic.monitor.MonitorMessageSender;
import logic.planner.PlannerFleManager;
import model.AMConfiguration;
import model.MonitorFleConfiguration;
import model.MonitorsConfig;

public class SimpleLoopManager {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	private DeltaIoTSimpleAutonomicManager loop;

	public SimpleLoopManager(String configFile) {
		this.loop = null;
		AMConfiguration config;
		ObjectMapper mapper = new ObjectMapper();
		//String filePath = "/tmp/config/" + configFile;
		String filePath = "config/" + configFile;
		try {
			String data = new String(Files.readAllBytes(Paths.get(filePath)));
			config = mapper.readValue(data, AMConfiguration.class);
			// Create elements
			LOGGER.info("LOOP | Create MAPE-K elements");
			IMonitor m = new Monitor(config.getElementsId().get("monitorId"),
					new Policy(config.getElementsId().get("monitorId"), config.getElementsMessagesCodes()),
					new MonitorFunctionalLogicEnactor(new MonitorFleManager()), new MonitorMessageSender());

			IAnalyzer a = new Analyzer(config.getElementsId().get("analyzerId"),
					new Policy(config.getElementsId().get("analyzerId"), config.getElementsMessagesCodes()),
					new AnalyzerFunctionalLogicEnactor(new AnalyzerFleManager()), new MAPMessageSender());

			IPlanner p = new Planner(config.getElementsId().get("plannerId"),
					new Policy(config.getElementsId().get("plannerId"), config.getElementsMessagesCodes()),
					new PlannerFunctionalLogicEnactor(new PlannerFleManager()), new MAPMessageSender());

			IExecuter e = new Executer(config.getElementsId().get("executerId"),
					new Policy(config.getElementsId().get("executerId"), config.getElementsMessagesCodes()),
					new ExecuterFunctionalLogicEnactor(new ExecuterFleManager()), new ExecuterMessageSender());

			IKnowledgeBase kb = new KnowledgeBase(config.getElementsId().get("kbId"),
					new Policy(config.getElementsId().get("kbId"), config.getElementsMessagesCodes()),
					new KnowledgeBaseFuncionalLogicEnactor(new KnowledgeBaseFleManager()), new MAPMessageSender());
			// Create loop
			this.loop = new DeltaIoTSimpleAutonomicManager(config.getAutonomicManagerId(),
					new Policy(config.getAutonomicManagerId(), config.getElementsMessageBodyTypes()), m, a, p, e, kb);
			// Construct loop
			LOGGER.info("LOOP | Construct loop (connect elements and elements' components)");
			this.loop.construct();
			// Add logic policy
			LOGGER.info("LOOP | Set logic policy");
			Map<String, String> policyContent = new HashMap<>();
			MonitorFleConfiguration mConfig = new MonitorFleConfiguration();
			mConfig.setMonFreq(config.getMonFreq());
			mConfig.setMonitorsl1(config.getMonitorsl1());
			policyContent.put("config", mapper.writeValueAsString(mConfig));
			PolicyConfigMessageBody messageContentFL = new PolicyConfigMessageBody(
					m.getFunctionalLogic().getComponentId(), policyContent);
			IMessage mssgAdaptFL = new Message(config.getElementsId().get("monitorId"),
					m.getReceiver().getComponentId(), 2, MessageType.REQUEST.toString(),
					messageContentFL.getMessageBody());
			m.getReceiver().doOperation(mssgAdaptFL);
			/****************************************************/
			Map<String, String> policiesContent = new HashMap<>();
			MonitorsConfig apeConfig = new MonitorsConfig();
			apeConfig.setMonitorsl1(config.getMonitorsl1());
			policiesContent.put("config", mapper.writeValueAsString(apeConfig));
			PolicyConfigMessageBody messageContentFLA = new PolicyConfigMessageBody(
					a.getFunctionalLogic().getComponentId(), policiesContent);
			PolicyConfigMessageBody messageContentFLP = new PolicyConfigMessageBody(
					p.getFunctionalLogic().getComponentId(), policiesContent);
			IMessage mssgAdaptFLA = new Message(config.getElementsId().get("analyzerId"),
					a.getReceiver().getComponentId(), 2, MessageType.REQUEST.toString(),
					messageContentFLA.getMessageBody());
			IMessage mssgAdaptFLP = new Message(config.getElementsId().get("plannerId"),
					p.getReceiver().getComponentId(), 2, MessageType.REQUEST.toString(),
					messageContentFLP.getMessageBody());
			a.getReceiver().doOperation(mssgAdaptFLA);
			p.getReceiver().doOperation(mssgAdaptFLP);
			// Add loop recipients (MEs)
			Thread.sleep(2000);
			LOGGER.info("LOOP | Add loop recipients and their policies (MEs - monitors L1)");
			for (IRecipient recipient : config.getRecipients()) {
				this.loop.addME(recipient.getrecipientId(), recipient);
			}
			//Start monitoring
			Thread.sleep(1000);
			LOGGER.info("LOOP | Start monitoring");
			((MonitorFleManager) ((IFunctionalLogicEnactor) m.getFunctionalLogic().getMessageManager()).getManager())
					.start();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public DeltaIoTSimpleAutonomicManager getAMLoop() {
		return loop;
	}
}
