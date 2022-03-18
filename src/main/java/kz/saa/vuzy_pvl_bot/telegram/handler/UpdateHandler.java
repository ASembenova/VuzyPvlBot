package kz.saa.vuzy_pvl_bot.telegram.handler;

import kz.saa.vuzy_pvl_bot.egovapi.EgovApiConnection;
import kz.saa.vuzy_pvl_bot.service.LocaleMessageService;
import kz.saa.vuzy_pvl_bot.telegram.BotState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateHandler {
    public final EgovApiConnection egovConnection;
    public final CallbackQueryHandler callbackQueryHandler;
    public final MessageHandler messageHandler;
    private final LocaleMessageService localeMessageService;
    @Autowired
    public UpdateHandler(EgovApiConnection egovConnection, CallbackQueryHandler callbackQueryHandler, MessageHandler messageHandler, LocaleMessageService localeMessageService) {
        this.egovConnection = egovConnection;
        this.callbackQueryHandler = callbackQueryHandler;
        this.messageHandler = messageHandler;
        this.localeMessageService = localeMessageService;
    }

    public BotApiMethod<?> handleUpdate(Update update){
        if(update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return callbackQueryHandler.processCallbackQuery(callbackQuery);
        } else if (update.hasMessage()){
            Message message = update.getMessage();
            return handleInputMessage(message);
        }
        return null;
    }

    private BotApiMethod<?> handleInputMessage(Message message) {
        BotState botState = null;
        String inputMessage = message.getText();
        if (inputMessage.equals("/start")){
            botState = BotState.START;
        } else if(inputMessage.equals(localeMessageService.getMessage("help", message.getChatId()))){
            botState = BotState.HELP;
        }
        return messageHandler.handle(message, botState);
    }
}
