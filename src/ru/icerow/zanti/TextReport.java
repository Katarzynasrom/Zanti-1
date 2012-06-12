package ru.icerow.zanti;

import java.io.IOException;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 *
 * @author Artyom
 */
public class TextReport extends Report {
    
    @Override
    protected void writeHeader() {
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            out.write("Документы содержащиеся в системе на " + dateFormat.format(calendar.getTime()) + "\r\n\r\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void writeEntry(Document document) {
        try {
            out.write(document.getId() + ". " + document.getName() + "\r\n");
            out.write("Автор: " + document.getAuthor() + "\r\n");
            out.write("Описание:\r\n" + document.getDescription() + "\r\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void writeSeparator() {
        try {
            out.write("\r\n-=-=-=-=-=-=-=-=-=-=-=-=-\r\n\r\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void writeFooter() {}

    @Override
    protected String getDescription() {
        return "Текстовый отчёт";
    }

    @Override
    protected String getExtension() {
        return "txt";
    }
}
