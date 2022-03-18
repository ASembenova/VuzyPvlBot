package kz.saa.vuzy_pvl_bot.telegram;

import kz.saa.vuzy_pvl_bot.telegram.handler.UpdateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;


public class TelegramBot extends SpringWebhookBot {

    private String botPath;
    private String botUsername;
    private String botToken;

    @Autowired
    public final UpdateHandler updateHandler;


    public TelegramBot(DefaultBotOptions options, SetWebhook setWebhook, UpdateHandler updateHandler) {
        super(options, setWebhook);
        this.updateHandler = updateHandler;
    }

    public TelegramBot(SetWebhook setWebhook, UpdateHandler updateHandler) {
        super(setWebhook);
        this.updateHandler = updateHandler;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        BotApiMethod<?> botApiMethod = updateHandler.handleUpdate(update);
        if (botApiMethod!=null && botApiMethod.getClass() == SendMessage.class) {
            SendMessage sendMessage = (SendMessage) botApiMethod;
            int size = sendMessage.getText().length();
            if (size > 4096) {
                String text = sendMessage.getText();
                String chatId = sendMessage.getChatId();
                for (int i = 0; i < size; i += 4096) {
                    SendMessage part = new SendMessage();
                    if (i < size - 4096) {
                        part.setText(text.substring(i, i + 4096));
                    } else {
                        part.setText(text.substring(i, size - 1));
                    }
                    part.setChatId(chatId);
                    try {
                        this.execute(part);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return botApiMethod;
            }
        }
        if(botApiMethod!=null){
            return botApiMethod;
        }
        return null;
    }

    public void setBotPath(String botPath) {
        this.botPath = botPath;
    }

    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }


    @Override
    public String getBotPath() {
        return botPath;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    /*public void downloadPDF(){
        try {
            Path file = Paths.get(pdfService.generatePDF().getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/



    /*private BotApiMethod<?> sendDiagram(long chatId){
        try {
            HtmlImageGenerator htmlImageGenerator = new HtmlImageGenerator();
            String userdir = System.getProperty("user.dir");
            File file = new File(userdir + "/src/main/resources/templates/", "diagram.html");
            List<String> list = Files.readLines(file, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            for (String s:list) {
                stringBuilder.append(s);
            }
            String html = stringBuilder.toString();
            htmlImageGenerator.loadHtml(html);
            File image = new File(userdir + "/src/main/resources/pdf-resources/", "asel.png");
            image.createNewFile();
            htmlImageGenerator.getBufferedImage();
            htmlImageGenerator.saveAsImage(image);
        }catch (IOException e){
            e.printStackTrace();

        }

        return null;
    }*/

}

