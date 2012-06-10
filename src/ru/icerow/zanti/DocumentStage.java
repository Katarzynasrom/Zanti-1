package ru.icerow.zanti;

/**
 *
 * @author Artyom
 */
public class DocumentStage {
    private int id;
    private String name;
    
    public DocumentStage(String name) {
        this(-1, name);
    }

    public DocumentStage(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
}
