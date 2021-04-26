package gov.va.api.lighthouse.mockmpi.service.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class PatientVistaSiteDetailsTest {

    private PatientVistaSiteDetails details() {
    return PatientVistaSiteDetails.of(
        Map.of(
            "default",
            PatientVistaSiteDetails.SiteDetails.builder().sites(List.of("def")).build(),
                "david",
                PatientVistaSiteDetails.SiteDetails.builder().sites(List.of("rose")).build()
                ));
    }

    static Stream<Arguments> getOrDefault() {
        return Stream.of(
                arguments("default", "def"),
                arguments("david", "rose"),
                arguments("ew", "def")
        );
    }

    @ParameterizedTest
    @MethodSource
    void getOrDefault(String icn, String expected) {
        assertThat(details().getOrDefault(icn).sites()).containsExactly(expected);
    }

}
