package gov.va.api.lighthouse.mockmpi;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hl7.v3.PRPAIN201305UV02MCCIMT000100UV01Message;
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

  @PersistenceContext private EntityManager entityManager;

  /** Persist PRPAIN201306UV02 for each data file. */
  @Transactional
  @EventListener(ApplicationStartedEvent.class)
  public void initData() {
    persistResources("classpath*:data/PRPA_IN201306UV02/profile/*.xml");
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
  @SneakyThrows
  public JAXBElement<PRPAIN201306UV02> prpa_In201305Uv02Response(
      @RequestPayload JAXBElement<PRPAIN201305UV02MCCIMT000100UV01Message> request) {
    final String ssn =
        request
            .getValue()
            .getControlActProcess()
            .getQueryByParameter()
            .getValue()
            .getParameterList()
            .getLivingSubjectId()
            .get(0)
            .getValue()
            .get(0)
            .getExtension();
    PrpaIn201306Uv02Entity responseEntity = entityManager.find(PrpaIn201306Uv02Entity.class, ssn);
    String profile = responseEntity.profile();
    return JAXBContext.newInstance(PRPAIN201306UV02.class)
        .createUnmarshaller()
        .unmarshal(new StreamSource(new StringReader(profile)), PRPAIN201306UV02.class);
  }
}
