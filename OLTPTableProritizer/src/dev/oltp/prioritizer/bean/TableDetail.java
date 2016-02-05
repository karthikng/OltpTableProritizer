package dev.oltp.prioritizer.bean;

public class TableDetail {
  private String tableName;
  
  private String refConstraintName;
  
  private int priority;
  
  private String chain;
  
  public String getTableName() {
    return tableName;
  }
  
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
  
  public String getRefConstraintName() {
    return refConstraintName;
  }
  
  public void setRefConstraintName(String refConstraintName) {
    this.refConstraintName = refConstraintName;
  }
  
  public int getPriority() {
    return priority;
  }
  
  public void setPriority(int priority) {
    this.priority = priority;
  }
  
  public String getChain() {
    return chain;
  }
  
  public void setChain(String chain) {
    this.chain = chain;
  }
  
  @Override
  public String toString() {
    return "[TableName : " + tableName + ", priority : " + priority + "] Chain : " + chain + "\n";
  }
}
