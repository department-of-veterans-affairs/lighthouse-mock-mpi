package gov.va.api.lighthouse.mockmpi.service.config;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.io.FileInputStream;
import java.util.Map;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Slf4j
@Configuration
public class VistaSiteConfig {

  @Bean
  @SneakyThrows
  PatientVistaSiteDetails load(@Value("${vista-site.configuration}") String vistaSiteProperties) {
    Map<String, PatientVistaSiteDetails.SiteDetails> details;
    try (FileInputStream is = new FileInputStream(vistaSiteProperties)) {
      log.info("VistaSiteConfig: {}", vistaSiteProperties);
      details =
          JacksonConfig.createMapper()
              .readValue(
                  is, new TypeReference<Map<String, PatientVistaSiteDetails.SiteDetails>>() {});
    }
    if (!details.containsKey("default")) {
      throw new IllegalArgumentException("vista-sites.json must contain a default site value.");
    }
    return PatientVistaSiteDetails.of(details);
  }
}
