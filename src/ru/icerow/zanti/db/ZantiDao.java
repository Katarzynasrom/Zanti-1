package ru.icerow.zanti.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import ru.icerow.zanti.Document;

/**
 *
 * @author Artyom
 */
public class ZantiDao {
    private String dbName;
    private final Properties dbProperties;
    private Connection dbConnection;
    private PreparedStatement stmtGetListDocuments;
    private PreparedStatement stmtGetDocument;
    private PreparedStatement stmtAddDocument;
    private boolean isConnected;
    
    // <editor-fold defaultstate="collapsed" desc="Prepared Statements">
    private static final String strCreateDocumentsTable =
            "create table APP.DOCUMENTS (" +
            "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "    AUTHOR      VARCHAR(255), " +
            "    NAME        VARCHAR(255), " +
            "    DESCRIPTION LONG VARCHAR" +
            ")";

    private static final String strGetListDocuments =
            "SELECT ID, NAME, AUTHOR, DESCRIPTION FROM APP.DOCUMENTS "  +
            "ORDER BY NAME ASC";

    private static final String strAddDocument =
            "INSERT INTO APP.DOCUMENTS " +
            "   (NAME, AUTHOR, DESCRIPTION) " +
            "VALUES (?, ?, ?)";
    // </editor-fold>
    
    public ZantiDao() {
        dbProperties = loadDBProperties();
        dbName = dbProperties.getProperty("dbName");

        setDBSystemDir();
        String driverName = dbProperties.getProperty("derby.driver"); 
        loadDatabaseDriver(driverName);
        if(!dbExists()) {
            createDatabase();
        }
    }

    private void setDBSystemDir() {
        // decide on the db system directory
        String userHomeDir = System.getProperty("user.home", ".");
        String systemDir = userHomeDir + "/.zanti";
        System.setProperty("derby.system.home", systemDir);
        
        // create the db system directory if it's not yet existing
        File fileSystemDir = new File(systemDir);
        if (!fileSystemDir.exists()) {
            fileSystemDir.mkdir();
        }
    }

    private Properties loadDBProperties() {
        InputStream dbPropInputStream;
        dbPropInputStream = ZantiDao.class.getResourceAsStream("Configuration.properties");
        Properties dbProperties = new Properties();
        try {
            dbProperties.load(dbPropInputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return dbProperties;
    }

    private void loadDatabaseDriver(String driverName) {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    private boolean dbExists() {
        boolean bExists = false;
        String dbLocation = getDatabaseLocation();
        File dbFileDir = new File(dbLocation);
        if (dbFileDir.exists()) {
            bExists = true;
        }
        return bExists;
    }

    private String getDatabaseLocation() {
        String dbLocation = System.getProperty("derby.system.home") + "/" + dbName;
        return dbLocation;
    }

    private boolean createDatabase() {
        boolean bCreated = false;
        Connection dbConnection;
        
        String dbUrl = getDatabaseUrl();
        dbProperties.put("create", "true");
        
        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            bCreated = createTables(dbConnection);
        } catch (SQLException ex) {
        }
        dbProperties.remove("create");
        return bCreated;
    }

    private String getDatabaseUrl() {
        String dbUrl = dbProperties.getProperty("derby.url") + dbName;
        return dbUrl;
    }

    private boolean createTables(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(strCreateDocumentsTable);
            bCreatedTables = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return bCreatedTables;
    }

    public boolean connect() {
        String dbUrl = getDatabaseUrl();
        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            stmtAddDocument = dbConnection.prepareStatement(strAddDocument, Statement.RETURN_GENERATED_KEYS);
            
            isConnected = dbConnection != null;
        } catch (SQLException ex) {
            isConnected = false;
        }
        return isConnected;
    }
    
    public void disconnect() {
        if(isConnected) {
            String dbUrl = getDatabaseUrl();
            dbProperties.put("shutdown", "true");
            try {
                DriverManager.getConnection(dbUrl, dbProperties);
            } catch (SQLException ex) {
            }
            isConnected = false;
        }
    }
    
    ////
    // Data related methods below
    ////
    
    public int addDocument(Document document) {
        int id = -1;
        try {
            stmtAddDocument.clearParameters();
            stmtAddDocument.setString(1, document.getName());
            stmtAddDocument.setString(2, document.getAuthor());
            stmtAddDocument.setString(3, document.getDescription());
            stmtAddDocument.executeUpdate();
            ResultSet results = stmtAddDocument.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return id;
    }
    
    public List<Document> getListEntries() {
        List<Document> listEntries = new ArrayList<>();
        Statement queryStatement;
        ResultSet results;
        
        try {
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(strGetListDocuments);
            while(results.next()) {
                int id = results.getInt(1);
                String name = results.getString(2);
                String author = results.getString(3);
                String description = results.getString(4);
                
                Document entry = new Document(id, name, author, description);
                listEntries.add(entry);
            }
            
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            
        }
        
        return listEntries;
    }
    
    

}
