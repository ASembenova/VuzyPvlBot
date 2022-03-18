package kz.saa.vuzy_pvl_bot.telegram;

import com.vdurmont.emoji.EmojiParser;
import kz.saa.vuzy_pvl_bot.service.LocaleMessageService;
import kz.saa.vuzy_pvl_bot.service.flying.PDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageSender {
    @Autowired
    @Lazy
    private AbsSender bot;
    private final PDFService pdfService;
    private final LocaleMessageService localeMessageService;

    public MessageSender(PDFService pdfService, LocaleMessageService localeMessageService) {
        this.pdfService = pdfService;
        this.localeMessageService = localeMessageService;
    }

    public SendMessage createMessageWithKeyboard(long chatId, String messageTag,
                                                 ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(localeMessageService.getMessage(messageTag, chatId));
        if (replyKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }
        return sendMessage;
    }

    public SendMessage createMessageWithKeyboard(long chatId, String messageTag,
                                                 ReplyKeyboardMarkup replyKeyboardMarkup,
                                                 boolean containsEmoji) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if(containsEmoji){
            sendMessage.setText(EmojiParser.parseToUnicode(localeMessageService.getMessage(messageTag, chatId)));
        }
        if (replyKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }
        return sendMessage;
    }

    public ReplyKeyboardMarkup getMenuKeyboard(List<String> namesOfButtons) {
        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        for (String name: namesOfButtons) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(name));
            keyboard.add(row);
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public SendMessage createMessageWithInlineKeyboard(long chatId,
                                                       String messageTag, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(localeMessageService.getMessage(messageTag, chatId));
        sendMessage.setParseMode("html");
        if (inlineKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        return sendMessage;
    }


    public void sendPdf(long chatId, File file){
        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(new InputFile(file));
        sendDocument.setChatId(String.valueOf(chatId));
        try {
            bot.execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
    public List<String> getButtonList(List<String> tags, long chatId){
        List<String> list = new ArrayList<>();
        for(String tag:tags){
            list.add(localeMessageService.getMessage(tag, chatId));
        }
        return list;
    }

    public AnswerCallbackQuery getAnswerCallbackQuery(long chatId, String messageTag, String callbackQueryId){
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setText(localeMessageService.getMessage(messageTag, chatId));
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        return answerCallbackQuery;
    }

    public void execute(BotApiMethod botApiMethod){
        try {
            this.bot.execute(botApiMethod);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
