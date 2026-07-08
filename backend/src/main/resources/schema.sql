CREATE DATABASE IF NOT EXISTS abdm_hms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE abdm_hms;

CREATE TABLE IF NOT EXISTS patients (
  patient_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  gender VARCHAR(30) NOT NULL,
  date_of_birth DATE NOT NULL,
  phone_number VARCHAR(20) NOT NULL,
  address VARCHAR(255) NOT NULL,
  abha_number VARCHAR(30) UNIQUE
);

CREATE TABLE IF NOT EXISTS doctors (
  doctor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  specialization VARCHAR(120) NOT NULL,
  department VARCHAR(120) NOT NULL,
  phone_number VARCHAR(20) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS appointments (
  appointment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  appointment_date DATE NOT NULL,
  appointment_time TIME NOT NULL,
  status VARCHAR(40) NOT NULL,
  CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
  CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

CREATE TABLE IF NOT EXISTS prescriptions (
  prescription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  medicine_name VARCHAR(160) NOT NULL,
  dosage VARCHAR(120) NOT NULL,
  instructions VARCHAR(500),
  prescribed_date DATE NOT NULL,
  CONSTRAINT fk_prescriptions_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
  CONSTRAINT fk_prescriptions_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

CREATE TABLE IF NOT EXISTS observations (
  observation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  blood_pressure VARCHAR(30) NOT NULL,
  temperature DECIMAL(5,2) NOT NULL,
  heart_rate INT NOT NULL,
  weight DECIMAL(6,2) NOT NULL,
  height DECIMAL(6,2) NOT NULL,
  recorded_date DATE NOT NULL,
  CONSTRAINT fk_observations_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);
