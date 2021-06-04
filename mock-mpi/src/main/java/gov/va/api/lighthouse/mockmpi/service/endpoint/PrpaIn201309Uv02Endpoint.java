package gov.va.api.lighthouse.mockmpi.service.endpoint;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import gov.va.api.lighthouse.mockmpi.service.config.PatientVistaSiteDetails;
import gov.va.api.lighthouse.mpi.PatientIdentifierSegment;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import lombok.AllArgsConstructor;
import org.hl7.v3.II;
import org.hl7.v3.PRPAIN201309UV02;
import org.hl7.v3.PRPAIN201309UV02QUQIMT021001UV01ControlActProcess;
import org.hl7.v3.PRPAIN201310UV02;
import org.hl7.v3.PRPAMT201307UV02ParameterList;
import org.hl7.v3.PRPAMT201307UV02PatientIdentifier;
import org.hl7.v3.PRPAMT201307UV02QueryByParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/** 1309 request mock endpoint. */
@Endpoint
@AllArgsConstructor(onConstructor_ = @Autowired)
public class PrpaIn201309Uv02Endpoint {
  private static final String NAMESPACE_URI = "http://vaww.oed.oit.va.gov";

  private final PatientVistaSiteDetails siteDetails;

  private Optional<String> extractIcnFrom1309Request(JAXBElement<PRPAIN201309UV02> requestBody) {
    // We only expect one icn at a time
    return Optional.ofNullable(requestBody)
        .map(JAXBElement::getValue)
        .map(PRPAIN201309UV02::getControlActProcess)
        .map(PRPAIN201309UV02QUQIMT021001UV01ControlActProcess::getQueryByParameter)
        .map(JAXBElement::getValue)
        .map(PRPAMT201307UV02QueryByParameter::getParameterList)
        .map(PRPAMT201307UV02ParameterList::getPatientIdentifier)
        .stream()
        .flatMap(Collection::stream)
        .findFirst()
        .map(PRPAMT201307UV02PatientIdentifier::getValue)
        .stream()
        .flatMap(Collection::stream)
        .map(II::getExtension)
        .filter(Objects::nonNull)
        .map(PatientIdentifierSegment::parse)
        .filter(pis -> "200M".equals(pis.assigningLocation()) && "NI".equals(pis.identifierType()))
        .map(PatientIdentifierSegment::identifier)
        .findFirst();
  }

  private List<PatientIdentifierSegment> mapIcnToIdentifiers(String icn) {
    // <id extension="1011537977V693883^NI^200M^USVHA^P" root="2.16.840.1.113883.4.349"/>
    // <id extension="16264^PI^358^USVHA^A" root="2.16.840.1.113883.4.349"/>
    // The identifier value here is set to `000000` because mock-mpi does not actually know a
    // patient's DFN at a specific site. This value could be updated eventually if desired.
    var identifiers =
        siteDetails.getOrDefault(icn).sites().stream()
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
    return identifiers;
  }

  /** Entrypoint to MockMPI 1309 request. */
  @ResponsePayload
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "PRPA_IN201309UV02")
  public JAXBElement<PRPAIN201310UV02> prpain201309Uv02Request(
      @RequestPayload JAXBElement<PRPAIN201309UV02> requestBody) {
    var maybeIcn = extractIcnFrom1309Request(requestBody);
    // Map sites to a PatientIdentifierSegment

    List<PatientIdentifierSegment> identifiers = emptyList();
    if (maybeIcn.isPresent()) {
      identifiers = mapIcnToIdentifiers(maybeIcn.get());
    }
    return new JAXBElement<>(
        new QName("PRPA_IN201310UV02"),
        PRPAIN201310UV02.class,
        PrpaIn201309Uv02ResponseGenerator.forIdentifiers(identifiers).response());
  }
}
