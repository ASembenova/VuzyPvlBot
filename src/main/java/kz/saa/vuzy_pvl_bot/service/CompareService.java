package kz.saa.vuzy_pvl_bot.service;

import com.vdurmont.emoji.EmojiParser;
import kz.saa.vuzy_pvl_bot.egovapi.DataObjectsService;
import kz.saa.vuzy_pvl_bot.egovapi.Vuz;
import kz.saa.vuzy_pvl_bot.model.Speciality;
import kz.saa.vuzy_pvl_bot.service.flying.PDFService;
import kz.saa.vuzy_pvl_bot.telegram.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class CompareService {

    private final SpecialityService specialityService;
    private final PDFService pdfService;
    private final MessageSender messageSender;
    private final LocaleMessageService localeMessageService;
    private final DataObjectsService dataObjectsService;
    private final SplitterService splitterService;
    SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private Map <Long, int[]> selectedButtons;
    @Autowired
    public CompareService(SpecialityService specialityService, PDFService pdfService, MessageSender messageSender, LocaleMessageService localeMessageService, DataObjectsService dataObjectsService, SplitterService splitterService) {
        this.specialityService = specialityService;
        this.pdfService = pdfService;
        this.messageSender = messageSender;
        this.localeMessageService = localeMessageService;
        this.dataObjectsService = dataObjectsService;
        this.splitterService = splitterService;
        selectedButtons = new HashMap<>();
    }


    public SendMessage getCompareMessage(long chatId, String compareMode) {
        int[] selected = new int[2];
        InlineKeyboardMarkup inlineKeyboardMarkup = getInlineMessageButtons(chatId, compareMode, selected);
        return messageSender.createMessageWithInlineKeyboard(chatId, compareMode+".instr", inlineKeyboardMarkup);
    }


    public EditMessageText getEditedMessage(long chatId, long messageId, String compareMode, int[] selected){
        EditMessageText newMessage = new EditMessageText();
        newMessage.setMessageId((int) messageId);
        newMessage.setText(localeMessageService.getMessage(compareMode+".instr", chatId));
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setReplyMarkup(getInlineMessageButtons(chatId, compareMode, selected));
        newMessage.setParseMode("html");
        return newMessage;
    }

    private InlineKeyboardMarkup getInlineMessageButtons(long chatId, String compareMode, int[] selected) {
        if(selected[0]==0 && selected[1]==0){
            selectedButtons.put(chatId, selected);
        }
        int first = selectedButtons.get(chatId)[0];
        int second = selectedButtons.get(chatId)[1];
        if(selected[0]==0){
            second = selected[1];
        }
        if (selected[1]==0){
            first = selected[0];
        }
        selectedButtons.put(chatId, new int[]{first, second});
        List<String> vuzList = dataObjectsService.vuzList(chatId);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (int i=0; i<vuzList.size(); i++) {
            String vuz = vuzList.get(i);
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            if(first==i+1){
                button1.setText(vuz+"\u2714");
            } else{
                button1.setText(vuz);
            }
            button1.setCallbackData(compareMode+ " first "+(i+1));
            InlineKeyboardButton button2 = new InlineKeyboardButton();
            if(second==i+1){
                button2.setText(vuz+"\u2714");
            } else{
                button2.setText(vuz);
            }
            button2.setCallbackData(compareMode + " second "+(i+1));
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            keyboardButtonsRow.add(button1);
            keyboardButtonsRow.add(button2);
            rowList.add(keyboardButtonsRow);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(EmojiParser.parseToUnicode(localeMessageService.getMessage("generate_pdf", chatId)));
        button.setCallbackData("generate_pdf"+" "+compareMode+" "+first+" "+second);
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }


    public String getComparingResults(long chatId, Vuz v1, Vuz v2, String compareMode, String firstname, String lastname){
        Set<Speciality> different1; Set<Speciality> different2; Set<Speciality> common1; Set<Speciality> common2;
        switch (compareMode){
            case "compare_byname":
                different1 = specialityService.getDifferenceByName(v1,v2).get(0);
                different2 = specialityService.getDifferenceByName(v1,v2).get(1);
                common1 = specialityService.getIntersectionByName(v1,v2).get(0);
                common2 = specialityService.getIntersectionByName(v1,v2).get(1);
                break;
            case "compare_bycode":
                different1 = specialityService.getDifferenceByCode(v1,v2).get(0);
                different2 = specialityService.getDifferenceByCode(v1,v2).get(1);
                common1 = specialityService.getIntersectionByCode(v1,v2).get(0);
                common2 = specialityService.getIntersectionByCode(v1,v2).get(1);
                break;
            default:
                different1 = specialityService.getDifferenceByNameAndCode(v1,v2).get(0);
                different2 = specialityService.getDifferenceByNameAndCode(v1,v2).get(1);
                common1 = specialityService.getIntersectionByNameAndCode(v1,v2).get(0);
                common2 = specialityService.getIntersectionByNameAndCode(v1,v2).get(1);
                break;
        }
        List<Speciality> diff1List = new ArrayList<>(); diff1List.addAll(different1);
        List<Speciality> diff2List = new ArrayList<>(); diff2List.addAll(different2);
        List<Speciality> common1List = new ArrayList<>(); common1List.addAll(common1);
        List<Speciality> common2List = new ArrayList<>(); common2List.addAll(common2);
        int[] sizes = {different1.size(), common1.size(), common2.size(), different2.size()};
        int maxSize = Arrays.stream(sizes).max().getAsInt();
        Speciality[][] array = new Speciality[maxSize][4];
        for (int i = 0; i < array.length; i++) {
            if(i<sizes[0]){
                array[i][0] = diff1List.get(i);
            }
            if(i<sizes[1]){
                array[i][1] = common1List.get(i);
            }
            if(i<sizes[2]){
                array[i][2] = common2List.get(i);
            }
            if(i<sizes[3]){
                array[i][3] = diff2List.get(i);
            }
        }
        Map<String, Object> variables = new HashMap<>();
        String name1; String name2;
        if (localeMessageService.getLocaleTag(chatId).equals("kz")){
            name1 = splitterService.splitFullname(v1.name1);
            name2 = splitterService.splitFullname(v2.name1);
        } else {
            name1 = splitterService.splitFullname(v1.name2);
            name2 = splitterService.splitFullname(v2.name2);
        }
        variables.put("counter", String.format("%06d", pdfService.getCounter()));
        variables.put("compare_param", localeMessageService.getMessage(compareMode+".param", chatId));
        variables.put("vuz1_name", name1);
        variables.put("vuz2_name", name2);
        variables.put("firstname", firstname);
        variables.put("lastname", lastname);
        variables.put("vuz1_total", (sizes[0]+sizes[1]));
        variables.put("vuz2_total", (sizes[2]+sizes[3]));
        variables.put("common_total", sizes[1]);

        variables.put("array", array);

        variables.put("datetime", formater.format(new Date(System.currentTimeMillis())));
        try {
            File file = pdfService.generatePDF(variables, "compare_"+localeMessageService.getLocaleTag(chatId));
            messageSender.sendPdf(chatId, file);
            return "успех";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "неудача";
    }


}
