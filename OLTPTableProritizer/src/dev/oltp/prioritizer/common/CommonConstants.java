package dev.oltp.prioritizer.common;

public enum CommonConstants {
  PROGRAM_DETAILS_FILES("resource/app.properties");

  private String value;

  CommonConstants(String val) {
    value = val;
  }

  public String value() {
    return value;
  }
}
