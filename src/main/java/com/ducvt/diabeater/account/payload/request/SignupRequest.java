package com.ducvt.diabeater.account.payload.request;

import java.util.Date;
import java.util.Set;

import javax.validation.constraints.*;

public class SignupRequest {

  @NotBlank
  @Size(min = 3, max = 20)
  private String username;

  @Size(max = 50)
  @Email
  private String email;

  private Set<String> role;

//  @NotBlank
  @Size(min = 6, max = 40)
  private String password;

  private String fullName;

  private String gender;

  private Integer age;

  private String description;

  private String diseaseType;

  private Date diseaseStart;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Set<String> getRole() {
    return this.role;
  }

  public void setRole(Set<String> role) {
    this.role = role;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDiseaseType() {
    return diseaseType;
  }

  public void setDiseaseType(String diseaseType) {
    this.diseaseType = diseaseType;
  }

  public Date getDiseaseStart() {
    return diseaseStart;
  }

  public void setDiseaseStart(Date diseaseStart) {
    this.diseaseStart = diseaseStart;
  }
}
