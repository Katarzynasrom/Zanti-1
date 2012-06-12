package ru.icerow.zanti;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Artyom
 */
public abstract class Report {
    protected abstract void writeHeader();
    protected abstract void writeEntry(Document document);
    protected abstract void writeSeparator();
    protected abstract void writeFooter();
    protected abstract String getDescription();
    protected abstract String getExtension();
    protected String filename;
    protected Writer out;
    protected List<Document> documentList;
    
    public void generateReport(List<Document> documentList) {
        this.documentList = documentList;
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(getDescription(), getExtension());
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            filename = fileChooser.getSelectedFile().getAbsolutePath();
            if(!filename.toLowerCase().endsWith("." + getExtension())) {
               filename = filename + "." + getExtension();
            }
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                out = new OutputStreamWriter(fos, "UTF8");
                writeHeader();
                for (Document document : documentList) {
                    writeEntry(document);
                    if (documentList.indexOf(document) != documentList.size() - 1) {
                        writeSeparator();
                    }
                }
                writeFooter();
                out.close();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
