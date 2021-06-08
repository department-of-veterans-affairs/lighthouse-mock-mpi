package gov.va.api.lighthouse.mockmpi.service.config;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.io.FileInputStream;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Vista Site Configuration class. */
@Data
@Slf4j
@Configuration
public class VistaSiteConfig {

  @Bean
  @SneakyThrows
  PatientVistaSiteDetails load(@Value("${patient.vista-site.details}") String vistaSiteProperties) {
    PatientVistaSiteDetails details;
    try (FileInputStream is = new FileInputStream(vistaSiteProperties)) {
      log.info("VistaSiteConfig: {}", vistaSiteProperties);
      details = JacksonConfig.createMapper().readValue(is, PatientVistaSiteDetails.class);
    }
    if (!details.containsKey("default")) {
      throw new IllegalArgumentException(
          "patient.vista-site.details json file must contain a default site value.");
    }
    return details;
  }
}
