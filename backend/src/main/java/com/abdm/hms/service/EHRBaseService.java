package com.abdm.hms.service;

import com.abdm.hms.dto.VitalSignsDto;
import com.abdm.hms.entity.Patient;
import com.abdm.hms.repository.PatientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EHRBaseService {

    private final RestTemplate restTemplate;
    private final PatientRepository patientRepository;

    @Value("${ehrbase.base-url}")
    private String ehrBaseUrl;

    // ==========================================================
    // CREATE EHR
    // ==========================================================
    public String createEhr() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Prefer", "return=representation");

        String requestBody = """
        {
          "_type":"EHR_STATUS",
          "archetype_node_id":"openEHR-EHR-EHR_STATUS.generic.v1",
          "name":{
            "value":"EHR status"
          },
          "subject":{
            "_type":"PARTY_SELF"
          },
          "is_queryable":true,
          "is_modifiable":true
        }
        """;

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                ehrBaseUrl + "/ehr",
                request,
                String.class
        );

        try {

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(response.getBody());

            String ehrId = root.path("ehr_id")
                    .path("value")
                    .asText();

            return ehrId;

        } catch (Exception e) {

            throw new RuntimeException("Failed to parse EHR response", e);
        }
    }
        // ==========================================================
    // SAVE VITALS
    // ==========================================================
    public String saveVitals(Long patientId, VitalSignsDto dto) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (patient.getEhrId() == null || patient.getEhrId().isBlank()) {
            throw new RuntimeException("Patient has no EHR");
        }

        String ehrId = patient.getEhrId();

        System.out.println("====================================");
        System.out.println("PATIENT FOUND");
        System.out.println("Patient Name : " + patient.getFullName());
        System.out.println("Patient ID   : " + patient.getPatientId());
        System.out.println("EHR ID       : " + ehrId);
        System.out.println("====================================");

        Map<String, Object> composition = new HashMap<>();

        composition.put("vital_signs2/category|code", "433");
        composition.put("vital_signs2/category|terminology", "openehr");
        composition.put("vital_signs2/category|value", "event");

        String startTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        composition.put("vital_signs2/context/start_time", startTime);

        composition.put("vital_signs2/context/setting|code", "238");
        composition.put("vital_signs2/context/setting|value", "other care");
        composition.put("vital_signs2/context/setting|terminology", "openehr");

        composition.put(
                "vital_signs2/body_temperature/any_event:0/temperature|magnitude",
                dto.getTemperature());

        composition.put(
                "vital_signs2/body_temperature/any_event:0/temperature|unit",
                "Cel");

        composition.put(
                "vital_signs2/blood_pressure/any_event:0/systolic|magnitude",
                dto.getSystolic());

        composition.put(
                "vital_signs2/blood_pressure/any_event:0/systolic|unit",
                "mm[Hg]");

        composition.put(
                "vital_signs2/blood_pressure/any_event:0/diastolic|magnitude",
                dto.getDiastolic());

        composition.put(
                "vital_signs2/blood_pressure/any_event:0/diastolic|unit",
                "mm[Hg]");

        composition.put(
                "vital_signs2/pulse_heart_beat/any_event:0/rate|magnitude",
                dto.getPulse());

        composition.put(
                "vital_signs2/pulse_heart_beat/any_event:0/rate|unit",
                "/min");

        composition.put(
                "vital_signs2/respiration/any_event:0/rate|magnitude",
                dto.getRespiration());

        composition.put(
                "vital_signs2/respiration/any_event:0/rate|unit",
                "/min");

        composition.put(
                "vital_signs2/pulse_oximetry/any_event:0/spo|numerator",
                dto.getSpo2());

        composition.put(
                "vital_signs2/pulse_oximetry/any_event:0/spo|denominator",
                100);

        composition.put(
                "vital_signs2/pulse_oximetry/any_event:0/spo|type",
                3);

        composition.put("vital_signs2/language|code", "en");
        composition.put("vital_signs2/language|terminology", "ISO_639-1");

        composition.put("vital_signs2/territory|code", "IN");
        composition.put("vital_signs2/territory|terminology", "ISO_3166-1");

        composition.put("vital_signs2/composer|name", "Doctor");

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(composition);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            headers.set("Prefer", "return=representation");

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            URI uri = UriComponentsBuilder
        .fromHttpUrl(ehrBaseUrl)
        .pathSegment("ehr", ehrId, "composition")
        .queryParam("templateId", "Vital signs")
        .queryParam("format", "FLAT")
        .encode()
        .build()
        .toUri();

            System.out.println("================================");
            System.out.println("URL:");
            System.out.println(uri);

            System.out.println("================================");
            System.out.println("BODY:");
            System.out.println(jsonBody);

            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            System.out.println("Status = " + response.getStatusCode());
            System.out.println("Location = " + response.getHeaders().getFirst("Location"));

            return response.getHeaders().getFirst("Location") != null
                    ? response.getHeaders().getFirst("Location")
                    : "Vitals stored successfully.";

        } catch (HttpStatusCodeException ex) {
            System.out.println("Status Code : " + ex.getStatusCode());
            System.out.println("Response Body : ");
            System.out.println(ex.getResponseBodyAsString());

            throw ex;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}