/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.icerow.zanti;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author Artyom
 */
public class HtmlReport extends Report {

    @Override
    protected void writeHeader() {
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String header = "Документы содержащиеся в системе на " + dateFormat.format(calendar.getTime());
            out.write("<html>\r\n");
            out.write("<head>\r\n");
            out.write("<title>" + header + "</title>\r\n");
            out.write("</head>\r\n");
            out.write("<body>\r\n");
            out.write("<h1>" + header + "</h1>\r\n");
            out.write("<ul>\r\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void writeEntry(Document document) {
        try {
            out.write("<li>" + document.getId() + ". " + document.getName() + "\r\n");
            out.write("<ul>\r\n");
            out.write("<li>Автор: " + document.getAuthor() + "</li>\r\n");
            out.write("<li>Описание:\r\n" + document.getDescription() + "</li>\r\n");
            out.write("</ul>\r\n");
            out.write("</li>\r\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void writeSeparator() {}

    @Override
    protected void writeFooter() {
        try {
            out.write("</ul>\r\n");
            out.write("</body>\r\n");
            out.write("</html>\r\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getDescription() {
        return "HTML отчёт";
    }

    @Override
    protected String getExtension() {
        return "html";
    }
    
}
