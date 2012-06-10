package ru.icerow.zanti;

/**
 *
 * @author Artyom
 */
public class DocumentStageContent {
    private int id;
    private String name;
    private int stageId;
    
    public DocumentStageContent(String name, int stageId) {
        this(-1, name, stageId);
    }

    public DocumentStageContent(int id, String name, int stageId) {
        this.id = id;
        this.name = name;
        this.stageId = stageId;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }

    public int getStageId() {
        return this.stageId;
    }
}
