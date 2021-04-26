package gov.va.api.lighthouse.mockmpi.service.endpoint;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.lighthouse.mockmpi.service.config.PatientVistaSiteDetails;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.hl7.v3.II;
import org.hl7.v3.PRPAIN201309UV02;
import org.hl7.v3.PRPAIN201310UV02;
import org.hl7.v3.PRPAIN201310UV02MCCIMT000300UV01Message;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01ControlActProcess;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01RegistrationEvent;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01Subject1;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01Subject2;
import org.hl7.v3.PRPAMT201304UV02Patient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PrpaIn201309Uv02EndpointTest {
  static Stream<Arguments> prpain201309UV02Request() {
    return Stream.of(
        arguments(
            "1010101010V666666",
            new String[] {"1010101010V666666^NI^200M^USVHA^A", "00000^PI^123^USVHA^A"}),
        arguments(
            "1111111111V111111",
            new String[] {"1111111111V111111^NI^200M^USVHA^A", "00000^PI^456^USVHA^A"}),
        arguments(
            "2222222222V222222",
            new String[] {
              "2222222222V222222^NI^200M^USVHA^A", "00000^PI^456^USVHA^A", "00000^PI^789^USVHA^A"
            }));
  }

  private PrpaIn201309Uv02Endpoint endpoint() {
    return new PrpaIn201309Uv02Endpoint(
        PatientVistaSiteDetails.of(
            Map.of(
                "default",
                PatientVistaSiteDetails.SiteDetails.of(List.of("123")),
                "1111111111V111111",
                PatientVistaSiteDetails.SiteDetails.of(List.of("456")),
                "2222222222V222222",
                PatientVistaSiteDetails.SiteDetails.of(List.of("456", "789")))));
  }

  private List<String> extractIcnFrom1310Response(JAXBElement<PRPAIN201310UV02> response) {
    return Optional.ofNullable(response)
        .map(JAXBElement::getValue)
        .map(PRPAIN201310UV02MCCIMT000300UV01Message::getControlActProcess)
        .map(PRPAIN201310UV02MFMIMT700711UV01ControlActProcess::getSubject)
        .map(o -> o.get(0))
        .map(PRPAIN201310UV02MFMIMT700711UV01Subject1::getRegistrationEvent)
        .map(PRPAIN201310UV02MFMIMT700711UV01RegistrationEvent::getSubject1)
        .map(PRPAIN201310UV02MFMIMT700711UV01Subject2::getPatient)
        .map(PRPAMT201304UV02Patient::getId)
        .stream()
        .flatMap(Collection::stream)
        .map(II::getExtension)
        .collect(toList());
  }

  @ParameterizedTest
  @MethodSource
  void prpain201309UV02Request(String icn, String[] vistaSites) {
    var request =
        new JAXBElement<>(
            new QName("PRPA_IN201309UV02"),
            PRPAIN201309UV02.class,
            PrpaIn201309Uv02Samples.createForIcn(icn).request());
    var actual = endpoint().prpain201309UV02Request(request);
    assertThat(extractIcnFrom1310Response(actual)).containsExactlyInAnyOrder(vistaSites);
  }
}
