package gov.va.api.lighthouse.mockmpi.service.endpoint;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.lighthouse.mockmpi.Application;
import gov.va.oit.oed.vaww.ObjectFactory;
import javax.xml.bind.JAXBElement;
import org.hl7.v3.CS;
import org.hl7.v3.II;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201305UV02QUQIMT021001UV01ControlActProcess;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectId;
import org.hl7.v3.PRPAMT201306UV02ParameterList;
import org.hl7.v3.PRPAMT201306UV02QueryByParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {Application.class},
    properties = {"patient.vista-site.details=src/test/resources/vista-sites.json"})
public class PrpaIn201305Uv02EndpointTest {
  @Autowired PrpaIn201305Uv02Endpoint prpaIn201305Uv02Endpoint;

  private PRPAIN201306UV02 getResponse(JAXBElement<PRPAIN201305UV02> request) {
    return prpaIn201305Uv02Endpoint.prpa_In201305Uv02Response(request).getValue();
  }

  JAXBElement<PRPAIN201305UV02> invalidRequest() {
    PRPAIN201305UV02 prpain201305UV02 = new PRPAIN201305UV02();
    prpain201305UV02.setControlActProcess(
        PRPAIN201305UV02QUQIMT021001UV01ControlActProcess.builder()
            .queryByParameter(
                new org.hl7.v3.ObjectFactory()
                    .createPRPAIN201305UV02QUQIMT021001UV01ControlActProcessQueryByParameter(
                        PRPAMT201306UV02QueryByParameter.builder()
                            .parameterList(PRPAMT201306UV02ParameterList.builder().build())
                            .build()))
            .build());
    return new ObjectFactory().createPRPAIN201305UV02(prpain201305UV02);
  }

  @Test
  public void invalidRequestTest() {
    assertThat(
            getResponse(invalidRequest())
                .getControlActProcess()
                .getQueryAck()
                .getQueryResponseCode())
        .isEqualTo(responseCode("AE"));
  }

  @Test
  public void notFoundTest() {
    assertThat(
            getResponse(validSsnRequest("123456789"))
                .getControlActProcess()
                .getQueryAck()
                .getQueryResponseCode())
        .isEqualTo(responseCode("NF"));
  }

  private CS responseCode(String code) {
    CS responseCode = new CS();
    responseCode.setCode(code);
    return responseCode;
  }

  JAXBElement<PRPAIN201305UV02> validIcnRequest(String icn) {
    PRPAIN201305UV02 prpain201305UV02 = new PRPAIN201305UV02();
    prpain201305UV02.setControlActProcess(
        PRPAIN201305UV02QUQIMT021001UV01ControlActProcess.builder()
            .queryByParameter(
                new org.hl7.v3.ObjectFactory()
                    .createPRPAIN201305UV02QUQIMT021001UV01ControlActProcessQueryByParameter(
                        PRPAMT201306UV02QueryByParameter.builder()
                            .parameterList(
                                PRPAMT201306UV02ParameterList.builder()
                                    .id(
                                        II.iIBuilder()
                                            .root("2.16.840.1.113883.4.349")
                                            .extension(icn)
                                            .build())
                                    .build())
                            .build()))
            .build());
    return new ObjectFactory().createPRPAIN201305UV02(prpain201305UV02);
  }

  @Test
  public void validIcnTest() {
    assertThat(
            getResponse(validIcnRequest("1008691040V020761"))
                .getControlActProcess()
                .getSubject()
                .get(0)
                .getRegistrationEvent()
                .getSubject1()
                .getPatient()
                .getId()
                .get(0)
                .getExtension())
        .contains("1008691040V020761");
  }

  JAXBElement<PRPAIN201305UV02> validSsnRequest(String ssn) {
    PRPAIN201305UV02 prpain201305UV02 = new PRPAIN201305UV02();
    prpain201305UV02.setControlActProcess(
        PRPAIN201305UV02QUQIMT021001UV01ControlActProcess.builder()
            .queryByParameter(
                new org.hl7.v3.ObjectFactory()
                    .createPRPAIN201305UV02QUQIMT021001UV01ControlActProcessQueryByParameter(
                        PRPAMT201306UV02QueryByParameter.builder()
                            .parameterList(
                                PRPAMT201306UV02ParameterList.builder()
                                    .livingSubjectId(
                                        singletonList(
                                            PRPAMT201306UV02LivingSubjectId.builder()
                                                .value(
                                                    singletonList(
                                                        II.iIBuilder().extension(ssn).build()))
                                                .build()))
                                    .build())
                            .build()))
            .build());
    return new ObjectFactory().createPRPAIN201305UV02(prpain201305UV02);
  }

  @Test
  public void validSsnTest() {
    assertThat(
            getResponse(validSsnRequest("796130115"))
                .getControlActProcess()
                .getSubject()
                .get(0)
                .getRegistrationEvent()
                .getSubject1()
                .getPatient()
                .getPatientPerson()
                .getValue()
                .getAsOtherIDs()
                .get(0)
                .getId()
                .get(0)
                .getExtension())
        .isEqualTo("796130115");
  }
}
