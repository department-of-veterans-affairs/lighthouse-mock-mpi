package gov.va.api.lighthouse.mockmpi.service.endpoint;

import static gov.va.api.lighthouse.mockmpi.service.endpoint.Endpoints.csWithCode;

import java.io.Serializable;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import lombok.AllArgsConstructor;
import org.hl7.v3.ActClassControlAct;
import org.hl7.v3.CD;
import org.hl7.v3.COCTMT090100UV01AssignedPerson;
import org.hl7.v3.COCTMT090100UV01Person;
import org.hl7.v3.CommunicationFunctionType;
import org.hl7.v3.EN;
import org.hl7.v3.EntityClassDevice;
import org.hl7.v3.II;
import org.hl7.v3.MCCIMT000100UV01Agent;
import org.hl7.v3.MCCIMT000100UV01Device;
import org.hl7.v3.MCCIMT000100UV01Organization;
import org.hl7.v3.MCCIMT000100UV01Receiver;
import org.hl7.v3.MCCIMT000100UV01Sender;
import org.hl7.v3.PRPAIN201309UV02;
import org.hl7.v3.PRPAIN201309UV02QUQIMT021001UV01ControlActProcess;
import org.hl7.v3.PRPAMT201307UV02ParameterList;
import org.hl7.v3.PRPAMT201307UV02PatientIdentifier;
import org.hl7.v3.PRPAMT201307UV02QueryByParameter;
import org.hl7.v3.QUQIMT021001UV01DataEnterer;
import org.hl7.v3.ST;
import org.hl7.v3.TS;
import org.hl7.v3.XActMoodIntentEvent;

@AllArgsConstructor(staticName = "createForIcnSegment")
public class PrpaIn201309Uv02Samples {
  private String icnSegment;

  private JAXBElement<MCCIMT000100UV01Agent> asAgent() {
    return new JAXBElement<>(
        new QName("asAgent"),
        MCCIMT000100UV01Agent.class,
        MCCIMT000100UV01Agent.builder()
            .classCode(List.of("AGNT"))
            .representedOrganization(
                new JAXBElement<>(
                    new QName("representedOrganization"),
                    MCCIMT000100UV01Organization.class,
                    MCCIMT000100UV01Organization.builder()
                        .classCode("ORG")
                        .determinerCode("INSTANCE")
                        .id(
                            List.of(
                                II.iIBuilder()
                                    .root("2.16.840.1.113883.4.349")
                                    .extension("TestAgentId")
                                    .build()))
                        .build()))
            .build());
  }

  private List<QUQIMT021001UV01DataEnterer> dataEnterer() {
    return List.of(
        QUQIMT021001UV01DataEnterer.builder()
            .contextControlCode("AP")
            .typeCode(List.of("ENT"))
            .assignedPerson(
                COCTMT090100UV01AssignedPerson.builder()
                    .classCode("ASSIGNED")
                    .id(
                        List.of(
                            II.iIBuilder()
                                .root("2.16.840.1.113883.4.349")
                                .extension("TestAgentId")
                                .build()))
                    .assignedPerson(
                        new JAXBElement<>(
                            new QName("assignedPerson"),
                            COCTMT090100UV01Person.class,
                            COCTMT090100UV01Person.builder()
                                .classCode(List.of("PSN"))
                                .determinerCode("INSTANCE")
                                .name(
                                    List.of(
                                        EN.eNBuilder().content(getAssignedPersonValue()).build()))
                                .build()))
                    .build())
            .build());
  }

  private List<Serializable> getAssignedPersonValue() {
    return List.of(new JAXBElement<>(new QName("family"), String.class, EN.class, "TestUserId"));
  }

  private String getTokenFrom(String id, int index) {
    StringTokenizer tokenizer = new StringTokenizer(id, " ");
    String token = tokenizer.nextToken().trim();
    for (int i = 0; i < index; i++) {
      token = tokenizer.nextToken().trim();
    }
    return token;
  }

  private JAXBElement<PRPAMT201307UV02QueryByParameter> queryByParameter() {
    ST semanticsText = ST.sTBuilder().build();
    semanticsText.getContent().add("Patient.Id");
    return new JAXBElement<>(
        new QName("queryByParameter"),
        PRPAMT201307UV02QueryByParameter.class,
        PRPAMT201307UV02QueryByParameter.builder()
            .queryId(
                II.iIBuilder()
                    .root("1.2.840.114350.1.13.99999.4567.34")
                    .extension("MY_TST_9703")
                    .build())
            .statusCode(csWithCode("new"))
            .responsePriorityCode(csWithCode("I"))
            .parameterList(
                PRPAMT201307UV02ParameterList.builder()
                    .patientIdentifier(
                        List.of(
                            PRPAMT201307UV02PatientIdentifier.builder()
                                .value(
                                    List.of(
                                        II.iIBuilder()
                                            .root("2.16.840.1.113883.4.349")
                                            .extension(icnSegment)
                                            .build()))
                                .semanticsText(semanticsText)
                                .build()))
                    .build())
            .build());
  }

  private MCCIMT000100UV01Receiver receiver() {
    return MCCIMT000100UV01Receiver.builder()
        .typeCode(CommunicationFunctionType.RCV)
        .device(
            MCCIMT000100UV01Device.builder()
                .classCode(EntityClassDevice.DEV)
                .determinerCode("INSTANCE")
                .id(List.of(II.iIBuilder().root("2.16.840.1.113883.4.349").build()))
                .build())
        .build();
  }

  public PRPAIN201309UV02 request() {
    PRPAIN201309UV02 message =
        PRPAIN201309UV02.pRPAIN201309UV02Builder().itsVersion("XML_1.0").build();
    message.setId(requesterId());
    message.setCreationTime(TS.tSBuilder().value("now").build());
    message.setVersionCode(csWithCode("4.0"));
    message.setInteractionId(II.iIBuilder().root("2.16.840.1.113883.1.6").build());
    message.setProcessingCode(csWithCode("T"));
    message.setProcessingModeCode(csWithCode("T"));
    message.setAcceptAckCode(csWithCode("AL"));
    message.getReceiver().add(receiver());
    message.setSender(sender());
    message.setControlActProcess(
        PRPAIN201309UV02QUQIMT021001UV01ControlActProcess.builder()
            .classCode(ActClassControlAct.CACT)
            .moodCode(XActMoodIntentEvent.EVN)
            .code(
                CD.cDBuilder()
                    .code("PRPA_TE201309UV02")
                    .codeSystem("2.16.840.1.113883.1.6")
                    .build())
            .dataEnterer(dataEnterer())
            .queryByParameter(queryByParameter())
            .build());
    return message;
  }

  private II requesterId() {
    String uniqueId = "MCID-DVP_000";
    return II.iIBuilder().root("1.2.840.114350.1.13.0.1.7.1.1").extension(uniqueId).build();
  }

  private MCCIMT000100UV01Sender sender() {
    return MCCIMT000100UV01Sender.builder()
        .typeCode(CommunicationFunctionType.SND)
        .device(
            MCCIMT000100UV01Device.builder()
                .classCode(EntityClassDevice.DEV)
                .determinerCode("INSTANCE")
                .id(
                    List.of(
                        II.iIBuilder()
                            .extension("TestIntegrationProcessId")
                            .root("2.16.840.1.113883.3.42.10001.100001.12")
                            .build()))
                .asAgent(asAgent())
                .build())
        .build();
  }
}
