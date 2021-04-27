package gov.va.api.lighthouse.mockmpi.service.config;

import java.util.List;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {
  private static final String URL = "/psim_webservice/IdMWebService";

  @Override
  public void addInterceptors(List<EndpointInterceptor> interceptors) {
    interceptors.add(securityInterceptor());
  }

  /** Set up WSDL endpoint. */
  @Bean(name = "idmWebService")
  public SimpleWsdl11Definition defaultWsdl11Definition() {
    SimpleWsdl11Definition test =
        new SimpleWsdl11Definition(new ClassPathResource("META-INF/wsdl/IdMHL7v3.WSDL"));
    return test;
  }

  /** Set up xsd endpoint. */
  @Bean(name = "IdMHL7v3")
  public XsdSchema defaultXsdSchema() {
    return new SimpleXsdSchema(new ClassPathResource("META-INF/wsdl/IdMHL7v3.xsd"));
  }

  /** Set up servlet. */
  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, URL + "/*");
  }

  /** Security interceptor. */
  @Bean
  public Wss4jSecurityInterceptor securityInterceptor() {
    Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
    securityInterceptor.setValidationActions("");
    return securityInterceptor;
  }
}
