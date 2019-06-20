package logic;

import java.util.HashMap;
import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.generic.element.ILoopAElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.AdaptationPlan;
import model.AdaptationType;
import model.MonitorStatus;

public class MonitorL1DeltaIoTClient {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

	public void getMonitorStatus(String url, ILoopAElement receiverResponse, IMessage mssg) {
		RestTemplate restTemplate = new RestTemplate();
		long startTime = System.currentTimeMillis();
		long responseTime = 0;
		int state = 404;
		try {
			LOGGER.info("Request monitor status");
			ResponseEntity<String> response = restTemplate
					.getForEntity(url + mssg.getMessageBody().get("type").split("_")[0].toLowerCase(), String.class);
			responseTime = System.currentTimeMillis() - startTime;
			LOGGER.info("Receive monitor status");
			state = response.getStatusCodeValue();
		} catch (Exception e) {
			//e.printStackTrace(); Don't show connection refused
		}
		Map<String, String> messageBody = new HashMap<>();
		MonitorStatus ms = new MonitorStatus();
		ms.setMonitorId(mssg.getMessageTo());
		ms.setResponseTime(responseTime);
		ms.setState(state);
		ObjectMapper mapper = new ObjectMapper();
		try {
			messageBody.put("data", mapper.writeValueAsString(ms));
			IMessage monDataMssg = new Message("STATUS", receiverResponse.getElementId(),
					Integer.parseInt(receiverResponse.getElementPolicy().getPolicyContent()
							.get(LoopAElementMessageCode.MSSGINFL.toString())),
					MessageType.RESPONSE.toString(), messageBody);
			receiverResponse.getReceiver().doOperation(monDataMssg);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public void adaptMonitor(String url, IMessage t) {
//		LOGGER.info("Send message - code:" + t.getMessageCode() + " type:" + t.getMessageCode() + " from:"
//				+ t.getMessageFrom() + " to:" + t.getMessageTo() + " body:" + t.getMessageBody().toString()
//				+ " *** to url" + url);
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();
		try {
			AdaptationPlan ap = mapper.readValue(t.getMessageBody().get("content"), AdaptationPlan.class);
			LOGGER.info("Request monitor adaptation");
			ResponseEntity<String> response = restTemplate.postForEntity(
					url + t.getMessageBody().get("type").split("_")[0].toLowerCase(),
					mapper.writeValueAsString(ap.getMonitorsToAdapt().get(AdaptationType.CONFIGURE)), String.class);
			LOGGER.info("Receive response (" + response.getStatusCode()+")");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
