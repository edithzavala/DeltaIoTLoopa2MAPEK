package logic.loop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.loopa.analyzer.IAnalyzer;
import org.loopa.autonomicmanager.AMElementAdpaptationType;
import org.loopa.autonomicmanager.IAutonomicManager;
import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.comm.message.PolicyConfigMessageBody;
import org.loopa.executer.IExecuter;
import org.loopa.generic.element.ILoopAElement;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.knowledgebase.IKnowledgeBase;
import org.loopa.monitor.IMonitor;
import org.loopa.planner.IPlanner;
import org.loopa.policy.IPolicy;
import org.loopa.recipient.IRecipient;
import org.loopa.recipient.Recipient;

public class DeltaIoTSimpleAutonomicManager implements IAutonomicManager {
  private final String id;

  private IPolicy policy;
  private Map<String, ILoopAElement> elementsById;

  private IMonitor m;
  private IAnalyzer a;
  private IPlanner p;
  private IExecuter e;
  private IKnowledgeBase kb;

  public DeltaIoTSimpleAutonomicManager(String amId, IPolicy amP, IMonitor m, IAnalyzer a, IPlanner p,
      IExecuter e, IKnowledgeBase kb) {
    super();
    this.id = amId;
    this.policy = amP;

    this.m = m;
    this.a = a;
    this.p = p;
    this.e = e;
    this.kb = kb;

    elementsById = new HashMap<>();
    elementsById.put(this.m.getElementId(), this.m);
    elementsById.put(this.a.getElementId(), this.a);
    elementsById.put(this.p.getElementId(), this.p);
    elementsById.put(this.e.getElementId(), this.e);
    elementsById.put(this.kb.getElementId(), this.kb);
  }

  @Override
  public void construct() {
    this.m.construct();
    this.a.construct();
    this.p.construct();
    this.e.construct();
    this.kb.construct();

    this.m.addElementRecipient(new Recipient(this.a.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.a.getElementId())), this.a));
    this.m.addElementRecipient(new Recipient(this.kb.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.kb.getElementId())), this.kb));

    this.a.addElementRecipient(new Recipient(this.p.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.p.getElementId())), this.p));
    this.a.addElementRecipient(new Recipient(this.kb.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.kb.getElementId())), this.kb));

    this.p.addElementRecipient(new Recipient(this.e.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.e.getElementId())), this.e));
    this.p.addElementRecipient(new Recipient(this.kb.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.kb.getElementId())), this.kb));

    this.e.addElementRecipient(new Recipient(this.kb.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.kb.getElementId())), this.kb));

    this.kb.addElementRecipient(new Recipient(this.m.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.m.getElementId())), this.m));
    this.kb.addElementRecipient(new Recipient(this.a.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.a.getElementId())), this.a));
    this.kb.addElementRecipient(new Recipient(this.p.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.p.getElementId())), this.p));
    this.kb.addElementRecipient(new Recipient(this.e.getElementId(),
        Arrays.asList(this.policy.getPolicyContent().get(this.e.getElementId())), this.e));

  }

  @Override
  public void addME(String meId, IRecipient me) {
    this.e.addElementRecipient(me);
    this.m.addElementRecipient(me);
//    sentPoliciesOfNewME(meId, this.m);
//    sentPoliciesOfNewME(meId, this.a);
//    sentPoliciesOfNewME(meId, this.p);
//    sentPoliciesOfNewME(meId, this.e);
//    sentPoliciesOfNewME(meId, this.kb);
  }

//  private void sentPoliciesOfNewME(String meId, ILoopAElement element) {
//    Map<String, String> policyBody = new HashMap<>();
//    policyBody.put("meId", meId);
//    PolicyConfigMessageBody messageContent =
//        new PolicyConfigMessageBody(element.getFunctionalLogic().getComponentId(), policyBody);
//    IMessage mssgAdapt = new Message(this.id, element.getFunctionalLogic().getComponentId(),
//        Integer.parseInt(element.getElementPolicy().getPolicyContent()
//            .get(LoopAElementMessageCode.MSSGINAL.toString())),
//        MessageType.REQUEST.toString(), messageContent.getMessageBody());
//    element.getReceiver().doOperation(mssgAdapt);
//  }

  @Override
  public void AdaptLoopElement(String adapter, String elementId, AMElementAdpaptationType adaptType,
      Map<String, String> adaptation) {
    Map<AMElementAdpaptationType, ILoopAElementComponent> componentByTypeOfAdaptation =
        new HashMap<AMElementAdpaptationType, ILoopAElementComponent>() {
          {
            put(AMElementAdpaptationType.RECEIVER, elementsById.get(elementId).getReceiver());
            put(AMElementAdpaptationType.LOGICSELECTOR,
                elementsById.get(elementId).getLogicSelector());
            put(AMElementAdpaptationType.FLOGIC, elementsById.get(elementId).getFunctionalLogic());
            put(AMElementAdpaptationType.ALOGIC, elementsById.get(elementId).getAdaptationLogic());
            put(AMElementAdpaptationType.MESSAGECOMPOSER,
                elementsById.get(elementId).getMessageComposer());
            put(AMElementAdpaptationType.SENDER, elementsById.get(elementId).getSender());
            put(AMElementAdpaptationType.KNOWLEDGEMANAGER,
                elementsById.get(elementId).getKnowledge());
          }
        };

    PolicyConfigMessageBody messageContent = new PolicyConfigMessageBody(
        componentByTypeOfAdaptation.get(adaptType).getComponentId(), adaptation);

    IMessage mssgAdapt = new Message(adapter, elementsById.get(elementId).getElementId(),
        Integer.parseInt(elementsById.get(elementId).getElementPolicy().getPolicyContent()
            .get(LoopAElementMessageCode.MSSGINAL.toString())),
        MessageType.REQUEST.toString(), messageContent.getMessageBody());

    elementsById.get(elementId).getReceiver().doOperation(mssgAdapt);

  }

  public IMonitor getMonitor() {
    return m;
  }

  public void setMonitor(IMonitor m) {
    this.m = m;
  }

  public IAnalyzer getAanalyzer() {
    return a;
  }

  public void setAnalyzer(IAnalyzer a) {
    this.a = a;
  }

  public IPlanner getPlanner() {
    return p;
  }

  public void setPlanner(IPlanner p) {
    this.p = p;
  }

  public IExecuter getExecuter() {
    return e;
  }

  public void setExecuter(IExecuter e) {
    this.e = e;
  }

  public IKnowledgeBase getKb() {
    return kb;
  }

  public void setKb(IKnowledgeBase kb) {
    this.kb = kb;
  }

  @Override
  public String getAutonomicManagerId() {
    return this.id;
  }
}
