package gov.va.api.lighthouse.mockmpi.service.config;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.experimental.Delegate;

@Value
@AllArgsConstructor(staticName = "of")
public class PatientVistaSiteDetails {
  @Delegate private Map<String, SiteDetails> vistaSiteDetails;

  public SiteDetails getOrDefault(String icn) {
    return vistaSiteDetails().getOrDefault(icn, vistaSiteDetails().get("default"));
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
