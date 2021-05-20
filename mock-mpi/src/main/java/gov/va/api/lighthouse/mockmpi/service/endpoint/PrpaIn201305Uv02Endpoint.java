package gov.va.api.lighthouse.mockmpi.service.endpoint;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@AllArgsConstructor(onConstructor = @__({@Autowired}))
@Endpoint
public class PrpaIn201305Uv02Endpoint {
  private static final String NAMESPACE_URI = "http://vaww.oed.oit.va.gov";

  private static final String EDIPI_ROOT = "2.16.840.1.113883.3.42.10001.100001.12";

  private static final String ICN_ROOT = "2.16.840.1.113883.4.349";

  @PersistenceContext private EntityManager entityManager;

  /** Return the identifier present (ssn, edipi, or icn) in the request. */
  private String getIdentifier(JAXBElement<PRPAIN201305UV02> request) {
    String ssn = getSsn(request);
    if (ssn != null && !ssn.isBlank()) {
      return ssn;
    }
    String icn = getRequestId(request, ICN_ROOT);
    if (icn != null && !icn.isBlank()) {
      return icn;
    }
    String edipi = getRequestId(request, EDIPI_ROOT);
    if (edipi != null && !edipi.isBlank()) {
      return edipi;
    }
    return null;
  }

  /** Return ID present in request that matches the passed in root value. */
  private String getRequestId(JAXBElement<PRPAIN201305UV02> request, String rootValue) {
    String root =
        Optional.ofNullable(request)
            .map(req -> req.getValue())
            .map(value -> value.getControlActProcess())
            .map(controlActProcess -> controlActProcess.getQueryByParameter())
            .map(queryByParameter -> queryByParameter.getValue())
            .map(value -> value.getParameterList())
            .map(parameterList -> parameterList.getId())
            .map(id -> id.getRoot())
            .orElse(null);
    if (root != null && root.equals(rootValue)) {
      return Optional.ofNullable(request)
          .map(req -> req.getValue())
          .map(value -> value.getControlActProcess())
          .map(controlActProcess -> controlActProcess.getQueryByParameter())
          .map(queryByParameter -> queryByParameter.getValue())
          .map(value -> value.getParameterList())
          .map(parameterList -> parameterList.getId())
          .map(id -> id.getExtension())
          .orElse(null);
    }
    return null;
  }

  @SneakyThrows
  private JAXBElement<PRPAIN201306UV02> getResponse(String identifier) {
    PrpaIn201306Uv02Entity responseEntity =
        entityManager.find(PrpaIn201306Uv02Entity.class, identifier);
    if (responseEntity == null) {
      return null;
    }
    return JAXBContext.newInstance(PRPAIN201306UV02.class)
        .createUnmarshaller()
        .unmarshal(
            new StreamSource(new StringReader(responseEntity.profile())), PRPAIN201306UV02.class);
  }

  private String getSsn(JAXBElement<PRPAIN201305UV02> request) {
    String ssn =
        Optional.ofNullable(request)
            .map(req -> req.getValue())
            .map(value -> value.getControlActProcess())
            .map(controlActProcess -> controlActProcess.getQueryByParameter())
            .map(queryByParameter -> queryByParameter.getValue())
            .map(value -> value.getParameterList())
            .map(parameterList -> parameterList.getLivingSubjectId())
            .filter(livingSubjectIdList -> !livingSubjectIdList.isEmpty())
            .map(livingSubjectIdList -> livingSubjectIdList.get(0))
            .map(livingSubjectId -> livingSubjectId.getValue())
            .filter(valueList -> !valueList.isEmpty())
            .map(valueList -> valueList.get(0))
            .map(ii -> ii.getExtension())
            .orElse(null);
    return ssn;
  }

  /** Persist PRPAIN201306UV02 for each data file. */
  @Transactional
  @EventListener(ApplicationStartedEvent.class)
  public void initData() {
    persistResources("classpath*:data/PRPA_IN201306UV02/profile/*.xml");
    persistResources("classpath*:data/PRPA_IN201306UV02/profile_edipi/*.xml");
    persistResources("classpath*:data/PRPA_IN201306UV02/profile_icn/*.xml");
  }

  /** Persist resources in classpath to entityManager. */
  @SneakyThrows
  public void persistResources(String classpath) {
    Resource[] resources = new PathMatchingResourcePatternResolver().getResources(classpath);
    for (Resource resource : resources) {
      String filename = resource.getFilename();
      checkState(filename != null);
      String ssnOrIcn = filename.substring(0, filename.indexOf("."));
      try (InputStream inputStream = resource.getInputStream();
          BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
        String xml = reader.lines().collect(joining("\n"));
        entityManager.persist(
            PrpaIn201306Uv02Entity.builder().ssnOrIcn(ssnOrIcn).profile(xml).build());
      }
    }
  }

  /** Get MPI PRPAIN201306UV02 Response. */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "PRPA_IN201305UV02")
  @ResponsePayload
  public JAXBElement<PRPAIN201306UV02> prpa_In201305Uv02Response(
      @RequestPayload JAXBElement<PRPAIN201305UV02> request) {
    String identifier = getIdentifier(request);
    if (identifier == null) {
      return getResponse("invalid_request");
    }
    JAXBElement<PRPAIN201306UV02> response = getResponse(identifier);
    if (response != null) {
      return response;
    }
    return getResponse("not_found");
  }
}
