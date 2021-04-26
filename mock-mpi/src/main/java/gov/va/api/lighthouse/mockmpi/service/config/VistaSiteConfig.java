package gov.va.api.lighthouse.mockmpi.service.config;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Slf4j
@Configuration
public class VistaSiteConfig {
  @Delegate private Map<String, SiteDetails> vistaSiteDetails;

  public SiteDetails getOrDefault(String icn) {
    return vistaSiteDetails().getOrDefault(icn, vistaSiteDetails().get("default"));
  }

  @Bean
  @SneakyThrows
  VistaSiteConfig load(@Value("${vista-site.configuration}") String vistaSiteProperties) {
    Map<String, SiteDetails> details;
    try (FileInputStream is = new FileInputStream(vistaSiteProperties)) {
      log.info("VistaSiteConfig: {}", vistaSiteProperties);
      details =
          JacksonConfig.createMapper()
              .readValue(is, new TypeReference<Map<String, SiteDetails>>() {});
    } catch (FileNotFoundException e) {
      // If the given file cannot be found, load an empty config
      details = Map.of("default", SiteDetails.builder().build());
    }
    if (!details.containsKey("default")) {
      throw new IllegalArgumentException("vista-sites.json must contain a default site value.");
    }
    vistaSiteDetails = details;
    return this;
  }

  @Data
  @Builder
  public static class SiteDetails {
    List<String> sites;

    /** Lazy Initializer. */
    public List<String> sites() {
      if (sites == null) {
        sites = List.of();
      }
      return sites;
    }
  }
}
