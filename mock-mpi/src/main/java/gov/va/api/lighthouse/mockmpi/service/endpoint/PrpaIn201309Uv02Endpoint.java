package gov.va.api.lighthouse.mockmpi.service.endpoint;

import static gov.va.api.lighthouse.mockmpi.service.endpoint.Endpoints.csWithCode;
import static java.util.stream.Collectors.toList;

import gov.va.api.lighthouse.mockmpi.service.config.VistaSiteConfig;
import gov.va.api.lighthouse.mpi.PatientIdentifierSegment;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import lombok.AllArgsConstructor;
import org.hl7.v3.ActClassControlAct;
import org.hl7.v3.CD;
import org.hl7.v3.COCTMT090003UV01AssignedEntity;
import org.hl7.v3.CommunicationFunctionType;
import org.hl7.v3.EntityClassDevice;
import org.hl7.v3.II;
import org.hl7.v3.MCCIMT000300UV01Acknowledgement;
import org.hl7.v3.MCCIMT000300UV01Device;
import org.hl7.v3.MCCIMT000300UV01Receiver;
import org.hl7.v3.MCCIMT000300UV01Sender;
import org.hl7.v3.MCCIMT000300UV01TargetMessage;
import org.hl7.v3.MFMIMT700711UV01Custodian;
import org.hl7.v3.MFMIMT700711UV01QueryAck;
import org.hl7.v3.PN;
import org.hl7.v3.PRPAIN201309UV02;
import org.hl7.v3.PRPAIN201309UV02QUQIMT021001UV01ControlActProcess;
import org.hl7.v3.PRPAIN201310UV02;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01ControlActProcess;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01RegistrationEvent;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01Subject1;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01Subject2;
import org.hl7.v3.PRPAMT201304UV02Patient;
import org.hl7.v3.PRPAMT201304UV02Person;
import org.hl7.v3.PRPAMT201307UV02ParameterList;
import org.hl7.v3.PRPAMT201307UV02PatientIdentifier;
import org.hl7.v3.PRPAMT201307UV02QueryByParameter;
import org.hl7.v3.ParticipationTargetSubject;
import org.hl7.v3.TS;
import org.hl7.v3.XActMoodIntentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@AllArgsConstructor(onConstructor_ = @Autowired)
public class PrpaIn201309Uv02Endpoint {
  private static final String NAMESPACE_URI = "http://vaww.oed.oit.va.gov";

  private final VistaSiteConfig vistaSiteConfig;

  private PRPAMT201304UV02Patient patient(
      List<PatientIdentifierSegment> patientIdentifierSegments) {
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

  /** Entrypoint to MockMPI 1309 request. */
  @ResponsePayload
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "PRPA_IN201309UV02")
  public JAXBElement<PRPAIN201310UV02> prpain201309UV02Request(
      @RequestPayload JAXBElement<PRPAIN201309UV02> requestBody) {
    // We only expect one icn at a time
    String icn =
        Optional.ofNullable(requestBody)
            .map(JAXBElement::getValue)
            .map(PRPAIN201309UV02::getControlActProcess)
            .map(PRPAIN201309UV02QUQIMT021001UV01ControlActProcess::getQueryByParameter)
            .map(JAXBElement::getValue)
            .map(PRPAMT201307UV02QueryByParameter::getParameterList)
            .map(PRPAMT201307UV02ParameterList::getPatientIdentifier)
            .stream()
            .flatMap(Collection::stream)
            .map(PRPAMT201307UV02PatientIdentifier::getValue)
            .flatMap(Collection::stream)
            .map(II::getExtension)
            .map(PatientIdentifierSegment::parse)
            .map(PatientIdentifierSegment::identifier)
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("Request did not contain an icn value."));
    // Map sites to a PatientIdentifierSegment
    // <id extension="1011537977V693883^NI^200M^USVHA^P" root="2.16.840.1.113883.4.349"/>
    // <id extension="16264^PI^358^USVHA^A" root="2.16.840.1.113883.4.349"/>
    // ToDo realistic identifier value?
    var identifiers =
        vistaSiteConfig.getOrDefault(icn).sites().stream()
            .map(
                site ->
                    PatientIdentifierSegment.builder()
                        .identifier("00000")
                        .identifierType("PI")
                        .assigningLocation(site)
                        .assigningAuthority("USVHA")
                        .identifierStatus("A")
                        .build())
            .collect(toList());
    // Add the icn to the identifier list
    identifiers.add(
        PatientIdentifierSegment.builder()
            .identifier(icn)
            .identifierType("NI")
            .assigningLocation("200M")
            .assigningAuthority("USVHA")
            .identifierStatus("A")
            .build());
    return new JAXBElement<>(
        new QName("PRPA_IN201310UV02"), PRPAIN201310UV02.class, response(patient(identifiers)));
  }

  private PRPAIN201310UV02 response(PRPAMT201304UV02Patient identifiers) {
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
    response
        .getReceiver()
        .add(
            MCCIMT000300UV01Receiver.builder()
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
                .build());
    response.setSender(
        MCCIMT000300UV01Sender.builder()
            .typeCode(CommunicationFunctionType.SND)
            .device(
                MCCIMT000300UV01Device.builder()
                    .determinerCode("INSTANCE")
                    .classCode(EntityClassDevice.DEV)
                    .id(
                        List.of(
                            II.iIBuilder()
                                .extension("200M")
                                .root("2.16.840.1.113883.4.349")
                                .build()))
                    .build())
            .build());
    response
        .getAcknowledgement()
        .add(
            MCCIMT000300UV01Acknowledgement.builder()
                .typeCode(csWithCode("AA"))
                .targetMessage(
                    MCCIMT000300UV01TargetMessage.builder()
                        .id(
                            II.iIBuilder()
                                .extension("MCID-MMPI_" + UUID.randomUUID())
                                .root("1.2.840.114350.1.13.0.1.7.1.1")
                                .build())
                        .build())
                .build());
    var nullFlavor = II.iIBuilder().build();
    nullFlavor.getNullFlavor().add("NA");
    response.setControlActProcess(
        PRPAIN201310UV02MFMIMT700711UV01ControlActProcess.builder()
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
                                        .patient(identifiers)
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
            .build());
    return response;
  }
}
