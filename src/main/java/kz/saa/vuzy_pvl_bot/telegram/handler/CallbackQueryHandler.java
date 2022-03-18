package kz.saa.vuzy_pvl_bot.telegram.handler;

import kz.saa.vuzy_pvl_bot.egovapi.DataObjectsService;
import kz.saa.vuzy_pvl_bot.egovapi.Vuz;
import kz.saa.vuzy_pvl_bot.service.CompareService;
import kz.saa.vuzy_pvl_bot.service.InfoService;
import kz.saa.vuzy_pvl_bot.telegram.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class CallbackQueryHandler {
    private final InfoService infoService;
    private final CompareService compareService;
    private final DataObjectsService dataObjectsService;
    private final MessageSender messageSender;

    @Autowired
    public CallbackQueryHandler(InfoService infoService, CompareService compareService, DataObjectsService dataObjectsService, MessageSender messageSender) {
        this.infoService = infoService;
        this.compareService = compareService;
        this.dataObjectsService = dataObjectsService;
        this.messageSender = messageSender;
    }

    public BotApiMethod<?> processCallbackQuery(CallbackQuery callbackQuery) {
        final long chatId = callbackQuery.getMessage().getChatId();
        final long userId = callbackQuery.getFrom().getId();
        final long messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("btn")){
            int index = Integer.valueOf(String.valueOf(data.charAt(data.length()-1)));
            data = data.substring(0, data.length()-1);
            switch (data){
                case ("btnPrevious"):
                    return infoService.getMessageWithNextVuz(chatId, messageId, index);
                case ("btnNext"):
                    return infoService.getMessageWithPreviousVuz(chatId, messageId, index);
                case ("btnSpecList"):
                    return infoService.getMessageWithSpecList(chatId, index);
                case ("btnGeo"):
                    return infoService.getMessageWithLocation(chatId, index);
                case ("btnPhone"):
                    return infoService.getMessageWithContact(chatId, index);
                case ("btnSite"):
                    return infoService.getMessageWithContact(chatId, index);
            }
        }
        if(data.contains("generate_pdf")){ //example: "generate_pdf compare_byname 3 2"
            String[] params = data.split(" ");
            String compareMode = params[1];
            int first = Integer.parseInt(params[2]);
            int second = Integer.parseInt(params[3]);
            if(first!=0&&second!=0){
                messageSender.execute(messageSender.getAnswerCallbackQuery(chatId,
                        "waiting_for_doc", callbackQuery.getId()));
                Vuz vuz1 = dataObjectsService.getVuzy()[first-1];
                Vuz vuz2 = dataObjectsService.getVuzy()[second-1];
                String textMessage = compareService.getComparingResults(chatId, vuz1, vuz2, compareMode,
                        callbackQuery.getFrom().getFirstName(), callbackQuery.getFrom().getLastName());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText(textMessage);
                return sendMessage;
            } else{
                return messageSender.getAnswerCallbackQuery(chatId, "warning.not_selected", callbackQuery.getId());
            }
        }
        if(data.contains("compare")){
            String[] params = data.split(" "); //example: "compare_byname first 1"
            int[] selected = new int[2];
            if(params[1].equals("first")){
                selected[0] = Integer.parseInt(params[2]);
            } else if(params[1].equals("second")){
                selected[1] = Integer.parseInt(params[2]);
            }
            return compareService.getEditedMessage(chatId, messageId, data.split(" ")[0], selected);
        }
        System.out.println("process call back fall");
        return null;
    }
}
