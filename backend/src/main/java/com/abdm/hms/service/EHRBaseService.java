package com.abdm.hms.service;

import com.abdm.hms.dto.DiagnosisDto;
import com.abdm.hms.dto.DischargeSummaryDto;
import com.abdm.hms.dto.LabReportDto;
import com.abdm.hms.dto.PrescriptionDto;
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
import org.springframework.util.StringUtils;

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

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("EHRbase returned an invalid response");
            }

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(response.getBody());

            String ehrId = root.path("ehr_id")
                    .path("value")
                    .asText();

            if (!StringUtils.hasText(ehrId)) {
                throw new IllegalStateException("EHRbase did not return an EHR ID");
            }

            return ehrId;

        } catch (Exception e) {

            throw new RuntimeException("Failed to parse EHR response", e);
        }
    }
        // ==========================================================
    // SAVE VITALS
    // ==========================================================
    public String saveDiagnosis(Long patientId, DiagnosisDto dto) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!StringUtils.hasText(patient.getEhrId())) {
            throw new RuntimeException("Patient has no EHR");
        }

        String ehrId = patient.getEhrId();
        Map<String, Object> composition = new HashMap<>();

        composition.put("diagnosis/category|code", "433");
        composition.put("diagnosis/category|terminology", "openehr");
        composition.put("diagnosis/category|value", "event");

        String startTime = LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        composition.put("diagnosis/context/start_time", startTime);
        composition.put("diagnosis/context/setting|code", "238");
        composition.put("diagnosis/context/setting|value", "other care");
        composition.put("diagnosis/context/setting|terminology", "openehr");
        composition.put("diagnosis/diagnosis_name|value", dto.getDiagnosisName());
        composition.put("diagnosis/diagnosis_code|value", dto.getDiagnosisCode());
        composition.put("diagnosis/description|value", dto.getDescription());
        composition.put("diagnosis/severity|value", dto.getSeverity());
        composition.put("diagnosis/doctor_name|value", dto.getDoctorName());
        composition.put("diagnosis/diagnosis_date|value", dto.getDiagnosisDate().toString());
        composition.put("diagnosis/notes|value", dto.getNotes());
        composition.put("diagnosis/language|code", "en");
        composition.put("diagnosis/language|terminology", "ISO_639-1");
        composition.put("diagnosis/territory|code", "IN");
        composition.put("diagnosis/territory|terminology", "ISO_3166-1");
        composition.put("diagnosis/composer|name", dto.getDoctorName() != null ? dto.getDoctorName() : "Doctor");

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
                    .queryParam("templateId", "Diagnosis")
                    .queryParam("format", "FLAT")
                    .encode()
                    .build()
                    .toUri();

            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response.getHeaders().getFirst("Location") != null
                    ? response.getHeaders().getFirst("Location")
                    : "Diagnosis stored successfully.";

        } catch (HttpStatusCodeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String savePrescription(Long patientId, PrescriptionDto dto) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!StringUtils.hasText(patient.getEhrId())) {
            throw new RuntimeException("Patient has no EHR");
        }

        String ehrId = patient.getEhrId();
        Map<String, Object> composition = new HashMap<>();

        composition.put("eprescription/category|code", "433");
        composition.put("eprescription/category|terminology", "openehr");
        composition.put("eprescription/category|value", "event");

        String startTime = LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        composition.put("eprescription/context/start_time", startTime);
        composition.put("eprescription/context/setting|code", "238");
        composition.put("eprescription/context/setting|value", "other care");
        composition.put("eprescription/context/setting|terminology", "openehr");
        composition.put("eprescription/medicine_name|value", dto.getMedicineName());
        composition.put("eprescription/dosage|value", dto.getDosage());
        composition.put("eprescription/instructions|value", dto.getInstructions());
        composition.put("eprescription/prescribed_date|value", dto.getPrescribedDate().toString());
        composition.put("eprescription/language|code", "en");
        composition.put("eprescription/language|terminology", "ISO_639-1");
        composition.put("eprescription/territory|code", "IN");
        composition.put("eprescription/territory|terminology", "ISO_3166-1");
        composition.put("eprescription/composer|name", dto.getDoctorName() != null ? dto.getDoctorName() : "Doctor");

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
                    .queryParam("templateId", "ePrescription")
                    .queryParam("format", "FLAT")
                    .encode()
                    .build()
                    .toUri();

            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response.getHeaders().getFirst("Location") != null
                    ? response.getHeaders().getFirst("Location")
                    : "Prescription stored successfully.";

        } catch (HttpStatusCodeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String saveLabReport(Long patientId, LabReportDto dto) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!StringUtils.hasText(patient.getEhrId())) {
            throw new RuntimeException("Patient has no EHR");
        }

        String ehrId = patient.getEhrId();
        Map<String, Object> composition = new HashMap<>();

        composition.put("lab_reports/category|code", "433");
        composition.put("lab_reports/category|terminology", "openehr");
        composition.put("lab_reports/category|value", "event");

        String startTime = LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        composition.put("lab_reports/context/start_time", startTime);
        composition.put("lab_reports/context/setting|code", "238");
        composition.put("lab_reports/context/setting|value", "other care");
        composition.put("lab_reports/context/setting|terminology", "openehr");
        composition.put("lab_reports/test_name|value", dto.getTestName());
        composition.put("lab_reports/test_code|value", dto.getTestCode());
        composition.put("lab_reports/result|value", dto.getResult());
        composition.put("lab_reports/unit|value", dto.getUnit());
        composition.put("lab_reports/reference_range|value", dto.getReferenceRange());
        composition.put("lab_reports/status|value", dto.getStatus());
        composition.put("lab_reports/report_date|value", dto.getReportDate().toString());
        composition.put("lab_reports/remarks|value", dto.getRemarks());
        composition.put("lab_reports/language|code", "en");
        composition.put("lab_reports/language|terminology", "ISO_639-1");
        composition.put("lab_reports/territory|code", "IN");
        composition.put("lab_reports/territory|terminology", "ISO_3166-1");
        composition.put("lab_reports/composer|name", "Lab Technician");

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
                    .queryParam("templateId", "LabReports")
                    .queryParam("format", "FLAT")
                    .encode()
                    .build()
                    .toUri();

            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response.getHeaders().getFirst("Location") != null
                    ? response.getHeaders().getFirst("Location")
                    : "Lab report stored successfully.";

        } catch (HttpStatusCodeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String saveDischargeSummary(Long patientId, DischargeSummaryDto dto) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!StringUtils.hasText(patient.getEhrId())) {
            throw new RuntimeException("Patient has no EHR");
        }

        String ehrId = patient.getEhrId();
        Map<String, Object> composition = new HashMap<>();

        composition.put("discharge_summary/category|code", "433");
        composition.put("discharge_summary/category|terminology", "openehr");
        composition.put("discharge_summary/category|value", "event");

        String startTime = LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        composition.put("discharge_summary/context/start_time", startTime);
        composition.put("discharge_summary/context/setting|code", "238");
        composition.put("discharge_summary/context/setting|value", "other care");
        composition.put("discharge_summary/context/setting|terminology", "openehr");
        composition.put("discharge_summary/final_diagnosis|value", dto.getFinalDiagnosis());
        composition.put("discharge_summary/clinical_summary|value", dto.getClinicalSummary());
        composition.put("discharge_summary/treatment|value", dto.getTreatment());
        composition.put("discharge_summary/advice|value", dto.getAdvice());
        composition.put("discharge_summary/discharge_date|value", dto.getDischargeDate().toString());
        composition.put("discharge_summary/follow_up_date|value", dto.getFollowUpDate().toString());
        composition.put("discharge_summary/language|code", "en");
        composition.put("discharge_summary/language|terminology", "ISO_639-1");
        composition.put("discharge_summary/territory|code", "IN");
        composition.put("discharge_summary/territory|terminology", "ISO_3166-1");
        composition.put("discharge_summary/composer|name", "Doctor");

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
                    .queryParam("templateId", "Discharge Summary")
                    .queryParam("format", "FLAT")
                    .encode()
                    .build()
                    .toUri();

            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response.getHeaders().getFirst("Location") != null
                    ? response.getHeaders().getFirst("Location")
                    : "Discharge summary stored successfully.";

        } catch (HttpStatusCodeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getPatientHistory(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!StringUtils.hasText(patient.getEhrId())) {
            throw new RuntimeException("Patient has no EHR");
        }

        String aql = """
                SELECT
                    e/ehr_id/value AS ehr_id,
                    c/uid/value AS composition_uid,
                    c/name/value AS composition_name,
                    c/archetype_details/template_id/value AS template_id,
                    c/context/start_time/value AS start_time
                FROM EHR e
                CONTAINS COMPOSITION c
                WHERE e/ehr_id/value = $ehr_id
                ORDER BY c/context/start_time/value DESC
                """;

        Map<String, Object> payload = new HashMap<>();
        payload.put("q", aql);
        payload.put("ehr_id", patient.getEhrId());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            URI uri = UriComponentsBuilder
                    .fromHttpUrl(ehrBaseUrl)
                    .pathSegment("query")
                    .build()
                    .toUri();

            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("EHRbase query failed");
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.getBody(), Map.class);
        } catch (HttpStatusCodeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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