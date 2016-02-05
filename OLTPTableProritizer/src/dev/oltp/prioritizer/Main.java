package dev.oltp.prioritizer;


import dev.oltp.prioritizer.bean.TableDetail;
import dev.oltp.prioritizer.common.CommonConstants;
import dev.oltp.prioritizer.common.DbConstants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class Main {

  private static Properties programProperties = new Properties();

  private static String dbUrl;
  
  private static void init() throws Exception {
    programProperties.load(ClassLoader
        .getSystemResourceAsStream(CommonConstants.PROGRAM_DETAILS_FILES.value()));

    String serviceName = programProperties.getProperty(DbConstants.DATABASE_SERVICE_NAME.value());
    String port = programProperties.getProperty(DbConstants.DATABASE_PORT.value());
    String host = programProperties.getProperty(DbConstants.DATABASE_HOST.value());
    if  (serviceName != null && !serviceName.isEmpty()) {
      dbUrl = "jdbc:oracle:thin:@" + host + ":" + port + "/" + serviceName;
    } else {
      String sid = programProperties.getProperty(DbConstants.DATABASE_SID.value());
      dbUrl = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
    }

  }

  private Connection getConnection() throws SQLException, ClassNotFoundException {
    Class.forName("oracle.jdbc.driver.OracleDriver");
    
    return DriverManager.getConnection(dbUrl,
        programProperties.getProperty(DbConstants.DATABASE_USERNAME.value()),
        programProperties.getProperty(DbConstants.DATABASE_PSWD.value()));
  }

  private List<TableDetail> identifyRelationRecursively(Connection connection, 
      List<TableDetail> tableDetails, int level) throws SQLException {
    List<TableDetail> tempList = new ArrayList<>();
    
    String query = "select table_name, r_constraint_name, constraint_type "
        + "from all_constraints "
        + "where owner = 'PAL' "
        + "and table_name in (select table_name from all_cons_columns where constraint_name = ?)";
    
    for (TableDetail detail : tableDetails) {
      
      if (detail.getRefConstraintName() == null) {
        continue;
      }
      
      try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
        preparedStatement.setString(1, detail.getRefConstraintName());
        ResultSet resultSet = preparedStatement.executeQuery();
        
        while (resultSet.next()) {
          String tableName = resultSet.getString("table_name");
          String refConstraintName = resultSet.getString("r_constraint_name");
          
          //Eliminating the cyclic dependency within the table
          if (detail.getTableName().equals(tableName)) {
            continue;
          }
          
          TableDetail tableDetail = new TableDetail();
          tableDetail.setTableName(tableName);
          
          String constraintType = resultSet.getString("constraint_type");
          if (constraintType.equals("R")) {
            tableDetail.setRefConstraintName(refConstraintName);
          } else {
            tableDetail.setRefConstraintName(null);
          }
          tableDetail.setPriority(level);
          tableDetail.setChain(detail.getChain() + "-> " + tableName);
        
          tempList.add(tableDetail);
        }
      }
    }
    
    System.out.println("List size at level " + level + " is : " + tempList.size());
    if (tempList.size() > 0) {
      tempList.addAll(identifyRelationRecursively(connection, tempList, ++level));
    }
    return tempList;
  }

  private void rearrangePriority(List<TableDetail> tableDetails) {
    Map<String, TableDetail> tempMap = new  HashMap<>();

    for (TableDetail tableDetail : tableDetails) {
      if (tempMap.containsKey(tableDetail.getTableName())) {
        if (tempMap.get(tableDetail.getTableName()).getPriority() < tableDetail.getPriority()) {
          tempMap.put(tableDetail.getTableName(), tableDetail);
        }
      } else {
        tempMap.put(tableDetail.getTableName(), tableDetail);
      }
    }
    
    System.out.println(tempMap.values());
  }
  
  private void getTabledetails(Connection connection) throws SQLException {
    int level = 1;
    List<TableDetail> tableDetails = new ArrayList<>();

    String query = "select table_name, r_constraint_name "
        + "from all_constraints "
        + "where constraint_type = 'R' "
        + "and owner = 'PAL'";

    try (PreparedStatement statement = connection.prepareStatement(query); 
        ResultSet rSet = statement.executeQuery()) {

      while (rSet.next()) {

        String tableName = rSet.getString("table_name");
        String refConstraintName = rSet.getString("r_constraint_name");

        TableDetail tableDetail = new TableDetail();
        tableDetail.setTableName(tableName);
        tableDetail.setRefConstraintName(refConstraintName);
        tableDetail.setPriority(level);
        tableDetail.setChain(tableName);

        tableDetails.add(tableDetail);
      }
    }

    System.out.println("List size at level " + level + " is : " + tableDetails.size());
    tableDetails.addAll(identifyRelationRecursively(connection, tableDetails, ++level));
    
    rearrangePriority(tableDetails);
  }

  private static void close() {
    programProperties.clear();
  }

  /**
   * Main method
   * 
   * @param args - args
   * @throws Exception - throws Exception
   */
  public static void main(String[] args) throws Exception {
    init();

    Main main = new Main();
    Connection connection  = main.getConnection();
    main.getTabledetails(connection);

    close();
  }
}
