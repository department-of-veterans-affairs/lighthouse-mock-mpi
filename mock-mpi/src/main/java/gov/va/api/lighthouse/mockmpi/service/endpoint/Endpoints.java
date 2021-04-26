package gov.va.api.lighthouse.mockmpi.service.endpoint;

import org.hl7.v3.CS;

public class Endpoints {

  /** Build an HL7 CS object with only a code. */
  public static CS csWithCode(String code) {
    var cs = CS.cSBuilder().build();
    cs.setCode(code);
    return cs;
  }
}
