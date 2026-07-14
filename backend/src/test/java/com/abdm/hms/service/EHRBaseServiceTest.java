package com.abdm.hms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.abdm.hms.entity.Patient;
import com.abdm.hms.repository.PatientRepository;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EHRBaseServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private EHRBaseService ehrBaseService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ehrBaseService, "ehrBaseUrl", "http://localhost:8080/ehrbase");
    }

    @Test
    void getPatientHistoryReturnsRowsWhenPatientHasEhr() {
        Patient patient = Patient.builder()
                .patientId(1L)
                .ehrId("ehr-123")
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"rows\":[[\"history\"]],\"columns\":[]}", HttpStatus.OK));

        Map<String, Object> history = ehrBaseService.getPatientHistory(1L);

        assertThat(history).containsKey("rows");
        assertThat(history.get("rows")).isNotNull();
    }
}
