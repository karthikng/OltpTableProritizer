package dev.oltp.prioritizer.common;

public enum DbConstants {
  DATABASE_USERNAME("db-username"), 
  DATABASE_PSWD("db-password"), 
  DATABASE_SERVICE_NAME("db-service-name"), 
  DATABASE_PORT("db-port"), 
  DATABASE_HOST("db-host"), 
  DATABASE_SID("db-sid");

  private String value;

  private DbConstants(String val) {
    value = val;
  }

  public String value() {
    return value;
  }
}
