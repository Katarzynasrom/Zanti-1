package ru.icerow.zanti.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.sql.*;
import java.util.*;
import ru.icerow.zanti.Document;
import ru.icerow.zanti.DocumentStage;
import ru.icerow.zanti.DocumentStageContent;

/**
 *
 * @author Artyom
 */
public class ZantiDao {
    private String dbName;
    private final Properties dbProperties;
    private Connection dbConnection;
    private PreparedStatement stmtGetDocument;
    private PreparedStatement stmtAddDocument;
    private PreparedStatement stmtEditDocument;
    private PreparedStatement stmtAddDocumentStage;
    private PreparedStatement stmtAddDocumentStageContent;
    private PreparedStatement stmtGetDocumentStageContent;
    private PreparedStatement stmtAddDocumentProgress;
    private PreparedStatement stmtGetDocumentProgress;
    private PreparedStatement stmtDelDocumentProgress;
    private boolean isConnected;
    
    // <editor-fold defaultstate="collapsed" desc="Prepared Statements">
    // Tables
    private static final String strCreateDocumentsTable =
            "create table APP.DOCUMENTS (" +
            "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "    AUTHOR      VARCHAR(255), " +
            "    NAME        VARCHAR(255), " +
            "    DESCRIPTION LONG VARCHAR" +
            ")";

    private static final String strCreateDocumentStagesTable =
            "create table APP.DOCUMENTSTAGES (" +
            "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "    NAME        VARCHAR(255)" +
            ")";

    private static final String strCreateDocumentStageContentTable =
            "create table APP.DOCUMENTSTAGECONTENT (" +
            "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "    NAME        VARCHAR(255), " +
            "    STAGE_ID    INTEGER NOT NULL" +
            ")";
    
    private static final String strCreateDocumentProgressTable =
            "create table APP.DOCUMENTPROGRESS (" +
            "    ID                 INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "    DOCUMENT_ID        INTEGER NOT NULL, " +
            "    STAGECONTENT_ID    INTEGER NOT NULL" +
            ")";
    // Functions
    private static final String strGetListDocuments =
            "SELECT ID, NAME, AUTHOR, DESCRIPTION FROM APP.DOCUMENTS "  +
            "ORDER BY ID ASC";

    private static final String strGetDocument =
            "SELECT ID, NAME, AUTHOR, DESCRIPTION FROM APP.DOCUMENTS "  +
            "WHERE ID = ? " +
            "ORDER BY ID ASC";

    private static final String strEditDocument =
            "UPDATE APP.DOCUMENTS " +
            "SET NAME = ?, " +
            "    AUTHOR = ?, " +
            "    DESCRIPTION = ? " +
            "WHERE ID = ?";

    private static final String strAddDocument =
            "INSERT INTO APP.DOCUMENTS " +
            "   (NAME, AUTHOR, DESCRIPTION) " +
            "VALUES (?, ?, ?)";

    private static final String strAddDocumentStage =
            "INSERT INTO APP.DOCUMENTSTAGES " +
            "   (NAME) " +
            "VALUES (?)";

    private static final String strGetDocumentStages =
            "SELECT ID, NAME FROM APP.DOCUMENTSTAGES "  +
            "ORDER BY ID ASC";

    private static final String strAddDocumentStageContent =
            "INSERT INTO APP.DOCUMENTSTAGECONTENT " +
            "   (NAME, STAGE_ID) " +
            "VALUES (?, ?)";

    private static final String strGetDocumentStageContent =
            "SELECT ID, NAME FROM APP.DOCUMENTSTAGECONTENT "  +
            "WHERE STAGE_ID = ? " +
            "ORDER BY ID ASC";

    private static final String strGetAllDocumentStageContent =
            "SELECT ID, NAME, STAGE_ID FROM APP.DOCUMENTSTAGECONTENT "  +
            "ORDER BY ID ASC";

    private static final String strAddDocumentProgress =
            "INSERT INTO APP.DOCUMENTPROGRESS " +
            "   (DOCUMENT_ID, STAGECONTENT_ID) " +
            "VALUES (?, ?)";

    private static final String strGetDocumentProgress =
            "SELECT ID, STAGECONTENT_ID FROM APP.DOCUMENTPROGRESS "  +
            "WHERE DOCUMENT_ID = ? " +
            "ORDER BY ID ASC";

    private static final String strDelDocumentProgress =
            "DELETE FROM APP.DOCUMENTPROGRESS " +
            "WHERE DOCUMENT_ID = ? AND STAGECONTENT_ID = ?";
    // </editor-fold>
    
    public ZantiDao() {
        dbProperties = loadDBProperties();
        dbName = dbProperties.getProperty("dbName");

        setDBSystemDir();
        String driverName = dbProperties.getProperty("derby.driver"); 
        loadDatabaseDriver(driverName);
        if(!dbExists()) {
            createDatabase();
            fillTableData();
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
            ex.printStackTrace();
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
            statement.execute(strCreateDocumentStagesTable);
            statement.execute(strCreateDocumentStageContentTable);
            statement.execute(strCreateDocumentProgressTable);
            bCreatedTables = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return bCreatedTables;
    }
    
    private void fillTableData() {
        // <editor-fold defaultstate="collapsed" desc="Sample Data">
        List<String> stages = Arrays.asList(
                "Разработка технического задания",
                "Выбор направлений исследования",
                "Теоретические и экспериментальные исследования",
                "Обобщение и оценка результатов исследований"
                );
        List<String>[] stagesContent = (List<String>[]) Array.newInstance(List.class, 4);
        // Stage "Разработка технического задания"
        stagesContent[0] = Arrays.asList(
                "Научное прогнозирование",
                "Анализ результатов фундаментальных и поисковых исследований",
                "Изучение патентной документации",
                "Учет требований заказчиков"
                );
        
        // Stage "Выбор направлений исследования"
        stagesContent[1] = Arrays.asList(
                "Сбор и изучение научно-технической информации",
                "Составление аналитического обзора",
                "Проведение патентных исследований",
                "Формулирование возможных направлений решения задач, поставленных в ТЗ НИР, и их сравнительная оценка",
                "Выбор и обоснование принятого направления исследований и способов решения задач",
                "Сопоставление ожидаемых показателей новой продукции после внедрения результатов НИР с существующими показателями изделий-аналогов",
                "Оценка ориентировочной экономической эффективности новой продукции",
                "Разработка общей методики проведения исследований",
                "Составление промежуточного отчета"
                );
        
        // Stage "Теоретические и экспериментальные исследования"
        stagesContent[2] = Arrays.asList(
                "Разработка рабочих гипотез, построение моделей объекта исследований, обоснование допущений",
                "Выявление необходимости проведения экспериментов для подтверждения отдельных",
                "Положений теоретических исследований или для получения конкретных значений параметров, необходимых для проведения расчетов",
                "Разработка методики экспериментальных исследований, подготовка моделей (макетов, экспериментальных образцов), а также испытательного оборудования",
                "Проведение экспериментов, обработка полученных данных",
                "Cопоставление результатов эксперимента с теоретическими исследованиями",
                "Корректировка теоретических моделей объекта",
                "Проведение при необходимости дополнительных экспериментов",
                "Проведение технико-экономических исследований",
                "Cоставление промежуточного отчета"
                );

        // Stage "Обобщение и оценка результатов исследований"
        stagesContent[3] = Arrays.asList(
                "Обобщение результатов предыдущих этапов работ",
                "Оценка полноты решения задач",
                "Разработка рекомендаций по дальнейшим исследованиям и проведению ОКР",
                "Разработка проекта ТЗ на ОКР",
                "Составление итогового отчета",
                "Приемка НИР комиссией"
                );
        // </editor-fold>
        
        this.connect();
        for (String stage : stages) {
            int stageId = this.addDocumentStage(new DocumentStage(stage));
            for (String stageContent : stagesContent[stageId-1]) {
                this.addDocumentStageContent(new DocumentStageContent(stageContent, stageId));
            }
        }
        this.disconnect();
    }

    public boolean connect() {
        String dbUrl = getDatabaseUrl();
        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            stmtAddDocument = dbConnection.prepareStatement(strAddDocument, Statement.RETURN_GENERATED_KEYS);
            stmtGetDocument = dbConnection.prepareStatement(strGetDocument);
            stmtEditDocument = dbConnection.prepareStatement(strEditDocument);
            stmtAddDocumentStage = dbConnection.prepareStatement(strAddDocumentStage, Statement.RETURN_GENERATED_KEYS);
            stmtAddDocumentStageContent = dbConnection.prepareStatement(strAddDocumentStageContent, Statement.RETURN_GENERATED_KEYS);
            stmtGetDocumentStageContent = dbConnection.prepareStatement(strGetDocumentStageContent);
            stmtAddDocumentProgress = dbConnection.prepareStatement(strAddDocumentProgress, Statement.RETURN_GENERATED_KEYS);
            stmtGetDocumentProgress = dbConnection.prepareStatement(strGetDocumentProgress);
            stmtDelDocumentProgress = dbConnection.prepareStatement(strDelDocumentProgress);
            
            isConnected = dbConnection != null;
        } catch (SQLException ex) {
            isConnected = false;
        }
        return isConnected;
    }
    
    public void disconnect() {
        if(isConnected) {
            String dbUrl = getDatabaseUrl();
            try {
                DriverManager.getConnection(dbUrl, dbProperties);
            } catch (SQLException ex) {
                ex.printStackTrace();
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
    
    public Document getDocument(int id) {
        Document document = null;
        try {
            stmtGetDocument.clearParameters();
            stmtGetDocument.setInt(1, id);
            ResultSet result = stmtGetDocument.executeQuery();
            if (result.next()) {
                String name = result.getString("NAME");
                String author = result.getString("AUTHOR");
                String description = result.getString("DESCRIPTION");
                document = new Document(id, name, author, description);
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return document;
    }

    public boolean editDocument(Document document) {
        boolean edited = false;
        try {
            stmtEditDocument.clearParameters();
            
            stmtEditDocument.setString(1, document.getName());
            stmtEditDocument.setString(2, document.getAuthor());
            stmtEditDocument.setString(3, document.getDescription());
            stmtEditDocument.setInt(4, document.getId());
            stmtEditDocument.executeUpdate();
            edited = true;
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return edited;
    }
    
    public int addDocumentStage(DocumentStage stage) {
        int id = -1;
        try {
            stmtAddDocumentStage.clearParameters();
            stmtAddDocumentStage.setString(1, stage.getName());
            stmtAddDocumentStage.executeUpdate();
            ResultSet results = stmtAddDocumentStage.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return id;
    }
    
    public List<DocumentStage> getStages() {
        List<DocumentStage> stages = new ArrayList<>();
        Statement queryStatement;
        ResultSet results;
        
        try {
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(strGetDocumentStages);
            while(results.next()) {
                int id = results.getInt(1);
                String name = results.getString(2);
                stages.add(new DocumentStage(id, name));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return stages;
    }

    public int addDocumentStageContent(DocumentStageContent stageContent) {
        int id = -1;
        try {
            stmtAddDocumentStageContent.clearParameters();
            stmtAddDocumentStageContent.setString(1, stageContent.getName());
            stmtAddDocumentStageContent.setInt(2, stageContent.getStageId());
            stmtAddDocumentStageContent.executeUpdate();
            ResultSet results = stmtAddDocumentStageContent.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return id;
    }

    public List<DocumentStageContent> getStageContent(int stageId) {
        List<DocumentStageContent> stageContent = new ArrayList<>();
        Document document = null;
        try {
            stmtGetDocumentStageContent.clearParameters();
            stmtGetDocumentStageContent.setInt(1, stageId);
            ResultSet result = stmtGetDocumentStageContent.executeQuery();
            while(result.next()) {
                int id = result.getInt(1);
                String name = result.getString(2);
                stageContent.add(new DocumentStageContent(id, name, stageId));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return stageContent;
    }

    public List<DocumentStageContent> getAllStageContent() {
        List<DocumentStageContent> stageContent = new ArrayList<>();
        Statement queryStatement;
        ResultSet results;
        
        try {
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(strGetAllDocumentStageContent);
            while(results.next()) {
                int id = results.getInt(1);
                String name = results.getString(2);
                int stageId = results.getInt(3);
                stageContent.add(new DocumentStageContent(id, name, stageId));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return stageContent;
    }

    public void addDocumentProgress(int documentId, Set<Integer> stageContentIds) {
        try {
            for (int contentId : stageContentIds) {
                stmtAddDocumentProgress.clearParameters();
                stmtAddDocumentProgress.setInt(1, documentId);
                stmtAddDocumentProgress.setInt(2, contentId);
                stmtAddDocumentProgress.executeUpdate();
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    public Set<Integer> getDocumentProgress(int documentId) {
        Set<Integer> stageContentIds = new HashSet<>();
        try {
            stmtGetDocumentProgress.clearParameters();
            stmtGetDocumentProgress.setInt(1, documentId);
            ResultSet results = stmtGetDocumentProgress.executeQuery();
            while(results.next()) {
                stageContentIds.add(results.getInt(2));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return stageContentIds;
    }

    public void delDocumentProgress(int documentId, Set<Integer> stageContentIds) {
        try {
            for (int contentId : stageContentIds) {
                stmtDelDocumentProgress.clearParameters();
                stmtDelDocumentProgress.setInt(1, documentId);
                stmtDelDocumentProgress.setInt(2, contentId);
                stmtDelDocumentProgress.executeUpdate();
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
