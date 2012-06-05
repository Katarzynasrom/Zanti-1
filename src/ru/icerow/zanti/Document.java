package ru.icerow.zanti;

/**
 *
 * @author Artyom
 */
public class Document {
    private int id;
    private String name;
    private String author;
    private String description;
    
    public Document(String name, String author, String description) {
        this(-1, name, author, description);
    }
    
    public Document(int id, String name, String author, String description) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.description = description;
    }

    public int getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getAuthor() {
        return this.author;
    }
    
    public String getDescription() {
        return this.description;
    }
}
