package kz.saa.vuzy_pvl_bot.service;

import com.vdurmont.emoji.EmojiParser;
import kz.saa.vuzy_pvl_bot.egovapi.DataObjectsService;
import kz.saa.vuzy_pvl_bot.egovapi.Vuz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendContact;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVenue;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class InfoService {
    private final DataObjectsService dataObjectsService;
    private final LocaleMessageService localeMessageService;
    private final SplitterService splitterService;
    private final SpecialityService specialityService;
    @Autowired
    public InfoService(DataObjectsService dataObjectsService, LocaleMessageService localeMessageService, SplitterService splitterService, SpecialityService specialityService) {
        this.dataObjectsService = dataObjectsService;
        this.localeMessageService = localeMessageService;
        this.splitterService = splitterService;
        this.specialityService = specialityService;
    }

    public SendMessage getOneVuzInfo(long chatId, String vuzName){
        int index = dataObjectsService.getIndexByVuzName(vuzName, chatId);
        String textMessage = dataObjectsService.oneVuzInfo(index, chatId);
        final InlineKeyboardMarkup inlineKeyboardMarkup = getInlineMessageButtons(index, chatId);
        return createMessageWithKeyboard(chatId, EmojiParser.parseToUnicode(textMessage), inlineKeyboardMarkup);
    }

    public SendMessage getOneVuzInfo(long chatId, int index){
        String textMessage = dataObjectsService.oneVuzInfo(index, chatId);
        final InlineKeyboardMarkup inlineKeyboardMarkup = getInlineMessageButtons(index, chatId);
        return createMessageWithKeyboard(chatId, EmojiParser.parseToUnicode(textMessage), inlineKeyboardMarkup);
    }

    private InlineKeyboardMarkup getInlineMessageButtons(int index, long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton buttonPrevious = new InlineKeyboardButton();
        buttonPrevious.setText(EmojiParser.parseToUnicode(localeMessageService.getMessage("previous", chatId)));
        InlineKeyboardButton buttonNext = new InlineKeyboardButton();
        buttonNext.setText(EmojiParser.parseToUnicode(localeMessageService.getMessage("next", chatId)));
        InlineKeyboardButton buttonSpecList = new InlineKeyboardButton();
        buttonSpecList.setText(EmojiParser.parseToUnicode(localeMessageService.getMessage("spec_list", chatId)));
        InlineKeyboardButton buttonGeo = new InlineKeyboardButton();
        buttonGeo.setText(EmojiParser.parseToUnicode(localeMessageService.getMessage("show_on_map", chatId)));
        InlineKeyboardButton buttonPhone = new InlineKeyboardButton();
        buttonPhone.setText(EmojiParser.parseToUnicode(localeMessageService.getMessage("call", chatId)));
        InlineKeyboardButton buttonSite = new InlineKeyboardButton();
        buttonSite.setText(EmojiParser.parseToUnicode(localeMessageService.getMessage("visit_site", chatId)));
        buttonSite.setUrl(dataObjectsService.getSiteUrl(index));
        buttonPrevious.setCallbackData("btnPrevious"+index);
        buttonNext.setCallbackData("btnNext"+index);
        buttonSpecList.setCallbackData("btnSpecList"+index);
        buttonGeo.setCallbackData("btnGeo"+index);
        buttonPhone.setCallbackData("btnPhone"+index);
        buttonSite.setCallbackData("btnSite"+index);
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow4 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow5 = new ArrayList<>();
        keyboardButtonsRow1.add(buttonPrevious);
        keyboardButtonsRow1.add(buttonNext);
        keyboardButtonsRow2.add(buttonSpecList);
        keyboardButtonsRow3.add(buttonGeo);
        keyboardButtonsRow4.add(buttonPhone);
        keyboardButtonsRow5.add(buttonSite);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        rowList.add(keyboardButtonsRow3);
        rowList.add(keyboardButtonsRow4);
        rowList.add(keyboardButtonsRow5);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public SendVenue getMessageWithLocation(long chatId, int index){
        Vuz vuz = dataObjectsService.getVuzy()[index];
        String locationStr = vuz.name18;
        String address;
        String title;
        if (localeMessageService.getLocaleTag(chatId).equals("kz")){
            address = vuz.name9 + ", " + vuz.name16;
            title = vuz.name1;
        } else {
            address = vuz.name10 +", " + vuz.name17;
            title = vuz.name2;
        }
        double[] location = splitterService.splitGeo(locationStr);
        double latitude = location[0];
        double longtitude = location[1];
        return new SendVenue(String.valueOf(chatId),latitude, longtitude,title, address); //return new SendLocation(String.valueOf(chatId), latitude, longtitude);
    }

    public EditMessageText getMessageWithNextVuz(long chatId, long messageId, int index){
        int newIndex = index+1;
        if(newIndex>=dataObjectsService.getVuzy().length){
            newIndex=0;
        }
        return getEditedMessageAboutVuz(chatId, messageId, newIndex);
    }

    public EditMessageText getMessageWithPreviousVuz(long chatId, long messageId, int index){
        int newIndex = index-1;
        if(newIndex<0){
            newIndex = dataObjectsService.getVuzy().length-1;
        }
        return getEditedMessageAboutVuz(chatId, messageId, newIndex);
    }

    public EditMessageText getEditedMessageAboutVuz(long chatId, long messageId, int newIndex){
        EditMessageText newMessage = new EditMessageText();
        newMessage.setMessageId((int) messageId);
        newMessage.setText(EmojiParser.parseToUnicode(dataObjectsService.oneVuzInfo(newIndex, chatId)));
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setReplyMarkup((InlineKeyboardMarkup) getOneVuzInfo(chatId, newIndex).getReplyMarkup());
        newMessage.setParseMode("html");
        return newMessage;
    }

    public SendMessage getMessageWithSpecList(long chatId, int index) {
        Vuz vuz = dataObjectsService.getVuzy()[index];
        int count = specialityService.getSimpleListByRegex(vuz, chatId).size();
        String data;
        String vuzName;
        if (localeMessageService.getLocaleTag(chatId) == "kz") {
            data = vuz.name6;
            vuzName = splitterService.splitFullname(vuz.name1);
        } else {
            data = vuz.name7;
            vuzName = splitterService.splitFullname(vuz.name2);
        }
        data = data.replaceAll(", 6", ",\n6");
        data = data.replaceAll(",6", ",\n6");
        data = data.replaceAll(",  6", ",\n6");
        data = data.replaceAll(", 5", ",\n5");
        data = data.replaceAll(", 7", ",\n7");
        data = data.replaceAll(", 8", ",\n8");
        StringBuilder result = new StringBuilder();
        result.append(vuzName);
        result.append("\n");
        result.append(EmojiParser.parseToUnicode(localeMessageService.getMessage("spec_list", chatId)));
        result.append("\n");
        result.append(data);
        result.append("\n");
        result.append(localeMessageService.getMessage("total", chatId));
        result.append(": "+count);
        final SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(result.toString());
        sendMessage.setParseMode("html");
        return sendMessage;
    }

    public SendContact getMessageWithContact(long chatId, int index) {
        String phone = splitterService.splitPhone(dataObjectsService.getVuzy()[index].name11);
        String name = splitterService.splitFullname(dataObjectsService.getVuzy()[index].name2);
        return new SendContact(String.valueOf(chatId),phone,name);
    }


    private SendMessage createMessageWithKeyboard(final long chatId, String textMessage,
                                                  final InlineKeyboardMarkup inlineKeyboardMarkup) {
        final SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textMessage);
        sendMessage.setParseMode("html");
        if (inlineKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        return sendMessage;
    }

}
