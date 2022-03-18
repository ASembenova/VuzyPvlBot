package kz.saa.vuzy_pvl_bot.service.flying;

import com.lowagie.text.pdf.BaseFont;
import kz.saa.vuzy_pvl_bot.egovapi.DataObjectsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

@Service
public class PDFService {

    @Autowired
    private DataObjectsService dataObjectsService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    private static final String EXTERNAL_FILE_PATH = "/pdf-resources/";

    private int counter = 1;


    public File generatePDF() throws Exception {
        Context context = getContext();
        String html = loadAndFillTemplate(context);
        return renderPDF(html);
    }

    public File generatePDF(Map<String, Object> variables, String templateName) throws Exception {
        Context context = createContext(variables);
        String html = templateEngine.process(templateName, context);
        return renderPDF(html);
    }

    public File renderPDF(String html) throws Exception{
        //File file = File.createTempFile("students", ".pdf");
        File file = File.createTempFile("test", ".pdf");
        OutputStream outputStream = new FileOutputStream(file);
        ITextRenderer renderer = new ITextRenderer(20f * 4f / 3f, 20);
        ITextFontResolver resolver = renderer.getFontResolver();
        resolver.addFont("/pdf-resources/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        //resolver.addFont("/pdf-resources/helvetica.otf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        renderer.setDocumentFromString(html, EXTERNAL_FILE_PATH);
        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.close();
        file.deleteOnExit();
        counter=counter+1;
        return file;
    }

    public Context getContext(){
        Context context = new Context();
        //context.setVariable("vuzy", dataObjectsService.getVuzy());
        //context.setVariable("metadata", dataObjectsService.getMetadata());
        context.setVariable("header", "Header");
        return context;
    }

    public Context createContext(Map<String, Object> variables){
        Context context = new Context();
        for(Map.Entry<String,Object> entry: variables.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }
        return context;
    }

    public int getCounter() {
        return counter;
    }

    public String loadAndFillTemplate(Context context){
        return templateEngine.process("all_params_ru", context);
    }



}
