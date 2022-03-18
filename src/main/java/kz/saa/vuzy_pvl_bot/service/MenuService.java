package kz.saa.vuzy_pvl_bot.service;

import com.vdurmont.emoji.EmojiParser;
import kz.saa.vuzy_pvl_bot.egovapi.DataObjectsService;
import kz.saa.vuzy_pvl_bot.telegram.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MenuService {
    private final LocaleMessageService localeMessageService;
    private final DataObjectsService dataObjectsService;
    private final SpecialityService specialityService;
    private final MessageSender messageSender;
    private final CompareService compareService;
    @Autowired
    public MenuService(LocaleMessageService localeMessageService, DataObjectsService dataObjectsService, SpecialityService specialityService, MessageSender messageSender, CompareService compareService) {
        this.localeMessageService = localeMessageService;
        this.dataObjectsService = dataObjectsService;
        this.specialityService = specialityService;
        this.messageSender = messageSender;
        this.compareService = compareService;
    }

    public SendMessage getWelcomeMessage(long chatId) {
        ArrayList<String> namesOfButtons = new ArrayList<>();
        namesOfButtons.add(EmojiParser.parseToUnicode("Казакша :kz:"));
        namesOfButtons.add(EmojiParser.parseToUnicode("Русский :ru:"));
        final ReplyKeyboardMarkup replyKeyboardMarkup = messageSender.getMenuKeyboard(namesOfButtons);
        return messageSender.createMessageWithKeyboard(chatId, "welcome", replyKeyboardMarkup);
    }

    public BotApiMethod<?> getMainMenuMessage(String message, long chatId) {
        if(message.contains("\uD83C\uDDF0\uD83C\uDDFF")){
            localeMessageService.changeLang("kz", chatId);
        } else if (message.contains("\uD83C\uDDF7\uD83C\uDDFA")){
            localeMessageService.changeLang("ru", chatId);
        }
        List<String> namesOfButtons = messageSender.getButtonList(Arrays.asList("show_all", "show_one", "change_lang", "help", "back"), chatId);
        final ReplyKeyboardMarkup replyKeyboardMarkup = messageSender.getMenuKeyboard(namesOfButtons);
        return messageSender.createMessageWithKeyboard(chatId, "main_menu", replyKeyboardMarkup);
    }

    public BotApiMethod<?> getMenuSelectAllMessage(long chatId) {
        ArrayList<String> keys = new ArrayList<>();
        keys.add("show_all.all_param");
        keys.add("show_all.compare_number");
        keys.add("show_all.compare_spec");
        keys.add("search");
        keys.add("back");
        keys.add("help");
        List<String> namesOfButtons = messageSender.getButtonList(keys, chatId);
        final ReplyKeyboardMarkup replyKeyboardMarkup = messageSender.getMenuKeyboard(namesOfButtons);
        return messageSender.createMessageWithKeyboard(chatId, "show_all.text", replyKeyboardMarkup);
    }

    public BotApiMethod<?> getMenuSelectOneMessage(long chatId) {
        ArrayList<String> namesOfButtons = new ArrayList<>();
        namesOfButtons.addAll(dataObjectsService.vuzList(chatId));
        namesOfButtons.addAll(messageSender.getButtonList(Arrays.asList("help", "back"), chatId));
        final ReplyKeyboardMarkup replyKeyboardMarkup = messageSender.getMenuKeyboard(namesOfButtons);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return messageSender.createMessageWithKeyboard(chatId, "show_one.text", replyKeyboardMarkup);
    }

    public SendMessage getMessageWithSearchInstruction(long chatId) {
        ArrayList<String> namesOfButtons = new ArrayList<>();
        namesOfButtons.addAll(messageSender.getButtonList(Arrays.asList("back"), chatId));
        final ReplyKeyboardMarkup replyKeyboardMarkup = messageSender.getMenuKeyboard(namesOfButtons);
        return messageSender.createMessageWithKeyboard(chatId, "search.instr", replyKeyboardMarkup);
    }

    public SendMessage getSearchResults(String keyword, long chatId) {
        String textMessage = specialityService.searchByKeyword(keyword, chatId);
        return SendMessage.builder().text(textMessage).chatId(String.valueOf(chatId)).build();
    }

    public SendMessage getCompareMessageInfo(long chatId) {
        ArrayList<String> namesOfButtons = new ArrayList<>();
        namesOfButtons.addAll(messageSender.getButtonList(
                Arrays.asList("compare_byname", "compare_bycode", "compare_byname_and_code", "back"), chatId));
        final ReplyKeyboardMarkup replyKeyboardMarkup = messageSender.getMenuKeyboard(namesOfButtons);
        return messageSender.createMessageWithKeyboard(chatId, "compare_instr", replyKeyboardMarkup);
    }

    public SendMessage getHelpMessage(long chatId) {
        ArrayList<String> namesOfButtons = new ArrayList<>();
        namesOfButtons.addAll(messageSender.getButtonList(Arrays.asList("back"), chatId));
        final ReplyKeyboardMarkup replyKeyboardMarkup = messageSender.getMenuKeyboard(namesOfButtons);
        return messageSender.createMessageWithKeyboard(chatId, "help.message", replyKeyboardMarkup);
    }

    public BotApiMethod<?> getSelectLangMessage(long chatId) {
        ArrayList<String> namesOfButtons = new ArrayList<>();
        namesOfButtons.add(EmojiParser.parseToUnicode("Казакша :kz:"));
        namesOfButtons.add(EmojiParser.parseToUnicode("Русский :ru:"));
        final ReplyKeyboardMarkup replyKeyboardMarkup = messageSender.getMenuKeyboard(namesOfButtons);
        return messageSender.createMessageWithKeyboard(chatId, "select_lang.message", replyKeyboardMarkup);
    }
}
