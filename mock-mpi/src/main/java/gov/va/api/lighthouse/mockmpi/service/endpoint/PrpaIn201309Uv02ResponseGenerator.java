package gov.va.api.lighthouse.mockmpi.service.endpoint;

import static gov.va.api.lighthouse.mockmpi.service.endpoint.Endpoints.csWithCode;
import static java.util.stream.Collectors.toList;

import gov.va.api.lighthouse.mpi.PatientIdentifierSegment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hl7.v3.ActClassControlAct;
import org.hl7.v3.CD;
import org.hl7.v3.CE;
import org.hl7.v3.COCTMT090003UV01AssignedEntity;
import org.hl7.v3.CommunicationFunctionType;
import org.hl7.v3.ED;
import org.hl7.v3.EntityClassDevice;
import org.hl7.v3.II;
import org.hl7.v3.MCCIMT000300UV01Acknowledgement;
import org.hl7.v3.MCCIMT000300UV01AcknowledgementDetail;
import org.hl7.v3.MCCIMT000300UV01Device;
import org.hl7.v3.MCCIMT000300UV01Receiver;
import org.hl7.v3.MCCIMT000300UV01Sender;
import org.hl7.v3.MCCIMT000300UV01TargetMessage;
import org.hl7.v3.MFMIMT700711UV01Custodian;
import org.hl7.v3.MFMIMT700711UV01QueryAck;
import org.hl7.v3.PN;
import org.hl7.v3.PRPAIN201310UV02;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01ControlActProcess;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01RegistrationEvent;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01Subject1;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01Subject2;
import org.hl7.v3.PRPAMT201304UV02Patient;
import org.hl7.v3.PRPAMT201304UV02Person;
import org.hl7.v3.ParticipationTargetSubject;
import org.hl7.v3.TS;
import org.hl7.v3.XActMoodIntentEvent;

/** 1309 response generator. */
@Value
@AllArgsConstructor(staticName = "forIdentifiers")
public class PrpaIn201309Uv02ResponseGenerator {
  private final List<PatientIdentifierSegment> patientIdentifierSegments;

  private MCCIMT000300UV01Acknowledgement acknowledgementFailure() {
    var code = CE.cEBuilder().build();
    code.setCodeSystem("2.16.840.1.113883.5.1100");
    code.setCode("INTERR");
    code.setDisplayName("Internal System Error");
    var text = ED.eDBuilder().build();
    text.getContent().add("Could not extract ICN from parameters.");
    return MCCIMT000300UV01Acknowledgement.builder()
        .typeCode(csWithCode("AE"))
        .targetMessage(
            MCCIMT000300UV01TargetMessage.builder()
                .id(
                    II.iIBuilder()
                        .extension("MCID-MMPI_" + UUID.randomUUID())
                        .root("1.2.840.114350.1.13.0.1.7.1.1")
                        .build())
                .build())
        .acknowledgementDetail(
            List.of(MCCIMT000300UV01AcknowledgementDetail.builder().code(code).text(text).build()))
        .build();
  }

  private MCCIMT000300UV01Acknowledgement acknowledgementSuccess() {
    return MCCIMT000300UV01Acknowledgement.builder()
        .typeCode(csWithCode("AA"))
        .targetMessage(
            MCCIMT000300UV01TargetMessage.builder()
                .id(
                    II.iIBuilder()
                        .extension("MCID-MMPI_" + UUID.randomUUID())
                        .root("1.2.840.114350.1.13.0.1.7.1.1")
                        .build())
                .build())
        .build();
  }

  private PRPAIN201310UV02MFMIMT700711UV01ControlActProcess controlActProcessFailure() {
    return PRPAIN201310UV02MFMIMT700711UV01ControlActProcess.builder()
        .classCode(ActClassControlAct.CACT)
        .moodCode(XActMoodIntentEvent.EVN)
        .code(CD.cDBuilder().code("PRPA_TE201310UV02").build())
        .queryAck(
            MFMIMT700711UV01QueryAck.builder()
                .queryId(
                    II.iIBuilder()
                        .extension("MY_TST_9703")
                        .root("1.2.840.114350.1.13.99999.4567.34")
                        .build())
                .queryResponseCode(csWithCode("AE"))
                .build())
        .build();
  }

  private PRPAIN201310UV02MFMIMT700711UV01ControlActProcess controlActProcessSuccess() {
    var nullFlavor = II.iIBuilder().build();
    nullFlavor.getNullFlavor().add("NA");
    return PRPAIN201310UV02MFMIMT700711UV01ControlActProcess.builder()
        .classCode(ActClassControlAct.CACT)
        .moodCode(XActMoodIntentEvent.EVN)
        .code(CD.cDBuilder().code("PRPA_TE201310UV02").build())
        .subject(
            List.of(
                PRPAIN201310UV02MFMIMT700711UV01Subject1.builder()
                    .typeCode(List.of("SUBJ"))
                    .registrationEvent(
                        PRPAIN201310UV02MFMIMT700711UV01RegistrationEvent.builder()
                            .id(List.of(nullFlavor))
                            .classCode(List.of("REG"))
                            .moodCode(List.of("EVN"))
                            .statusCode(csWithCode("active"))
                            .subject1(
                                PRPAIN201310UV02MFMIMT700711UV01Subject2.builder()
                                    .typeCode(ParticipationTargetSubject.SBJ)
                                    .patient(patient())
                                    .build())
                            .custodian(
                                MFMIMT700711UV01Custodian.builder()
                                    .typeCode(List.of("CST"))
                                    .assignedEntity(
                                        COCTMT090003UV01AssignedEntity.builder()
                                            .classCode("ASSIGNED")
                                            .id(
                                                List.of(
                                                    II.iIBuilder()
                                                        .root("2.16.840.1.113883.4.349")
                                                        .build()))
                                            .build())
                                    .build())
                            .build())
                    .build()))
        .queryAck(
            MFMIMT700711UV01QueryAck.builder()
                .queryId(
                    II.iIBuilder()
                        .extension("MY_TST_9703")
                        .root("1.2.840.114350.1.13.99999.4567.34")
                        .build())
                .queryResponseCode(csWithCode("OK"))
                .build())
        .build();
  }

  private PRPAMT201304UV02Patient patient() {
    List<II> ids =
        patientIdentifierSegments.stream()
            .map(
                pis ->
                    II.iIBuilder()
                        .extension(pis.toIdentifierString())
                        .root("2.16.840.1.113883.4.349")
                        .build())
            .collect(toList());
    var nullFlavor = PN.pNBuilder().build();
    nullFlavor.getNullFlavor().add("NA");
    return PRPAMT201304UV02Patient.builder()
        .id(ids)
        .statusCode(csWithCode("active"))
        .patientPerson(
            new JAXBElement<>(
                new QName("patientPerson"),
                PRPAMT201304UV02Person.class,
                PRPAMT201304UV02Person.builder()
                    .determinerCode("INSTANCE")
                    .classCode(List.of("PSN"))
                    .name(List.of(nullFlavor))
                    .build()))
        .build();
  }

  private MCCIMT000300UV01Receiver receiver() {
    return MCCIMT000300UV01Receiver.builder()
        .typeCode(CommunicationFunctionType.RCV)
        .device(
            MCCIMT000300UV01Device.builder()
                .determinerCode("INSTANCE")
                .classCode(EntityClassDevice.DEV)
                .id(
                    List.of(
                        II.iIBuilder()
                            .extension("200MMPIE")
                            .root("2.16.840.1.113883.3.42.10001.100001.12")
                            .build()))
                .build())
        .build();
  }

  /** Build a PRPAIN201310UV02 response object using the provided patient identifier values. */
  public PRPAIN201310UV02 response() {
    var response = PRPAIN201310UV02.pRPAIN201310UV02Builder().itsVersion("XML_1.0").build();
    response.setId(
        II.iIBuilder()
            .extension("WSDOC0000000000000000000000000")
            .root("2.16.840.1.113883.4.349")
            .build());
    response.setCreationTime(TS.tSBuilder().value(Instant.now().toString()).build());
    response.setVersionCode(csWithCode("3.5"));
    response.setInteractionId(
        II.iIBuilder().extension("PRPA_IN201310UV02").root("2.16.840.1.113883.1.6").build());
    response.setProcessingCode(csWithCode("T"));
    response.setProcessingModeCode(csWithCode("T"));
    response.setAcceptAckCode(csWithCode("NE"));
    response.getReceiver().add(receiver());
    response.setSender(sender());
    if (patientIdentifierSegments().isEmpty()) {
      // Failure
      response.getAcknowledgement().add(acknowledgementFailure());
      response.setControlActProcess(controlActProcessFailure());
    } else {
      // Success
      response.getAcknowledgement().add(acknowledgementSuccess());
      response.setControlActProcess(controlActProcessSuccess());
    }
    return response;
  }

  private MCCIMT000300UV01Sender sender() {
    return MCCIMT000300UV01Sender.builder()
        .typeCode(CommunicationFunctionType.SND)
        .device(
            MCCIMT000300UV01Device.builder()
                .determinerCode("INSTANCE")
                .classCode(EntityClassDevice.DEV)
                .id(
                    List.of(
                        II.iIBuilder().extension("200M").root("2.16.840.1.113883.4.349").build()))
                .build())
        .build();
  }
}
