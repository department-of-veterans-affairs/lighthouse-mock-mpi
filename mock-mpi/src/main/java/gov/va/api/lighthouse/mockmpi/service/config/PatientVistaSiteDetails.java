package gov.va.api.lighthouse.mockmpi.service.config;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

/** Patient Vista Site Details. */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PatientVistaSiteDetails {
  @Delegate private Map<String, SiteDetails> vistaSiteDetails;

  public SiteDetails getOrDefault(String icn) {
    return vistaSiteDetails().getOrDefault(icn, vistaSiteDetails().get("default"));
  }

  /** Site details constructor. */
  @Data
  @Builder
  @AllArgsConstructor(staticName = "of")
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
