package kz.saa.vuzy_pvl_bot.controller;

import kz.saa.vuzy_pvl_bot.service.flying.PDFService;
import kz.saa.vuzy_pvl_bot.telegram.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class WebhookController {

    private final TelegramBot telegramBot;

    @Autowired
    private PDFService pdfService;

    public WebhookController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }


    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }

    @GetMapping("/zzz")
    public void downloadPdf(HttpServletResponse response){
        try {
            Path file = Paths.get(pdfService.generatePDF().getAbsolutePath());
            if(Files.exists(file)){
                response.setContentType("application/pdf");
                response.addHeader("Content-Disposition", "attachment; filename"+ file.getFileName());
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
