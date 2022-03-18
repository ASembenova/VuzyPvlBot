package kz.saa.vuzy_pvl_bot.telegram.handler;

import kz.saa.vuzy_pvl_bot.service.CompareService;
import kz.saa.vuzy_pvl_bot.service.InfoService;
import kz.saa.vuzy_pvl_bot.service.LocaleMessageService;
import kz.saa.vuzy_pvl_bot.service.MenuService;
import kz.saa.vuzy_pvl_bot.telegram.BotState;
import kz.saa.vuzy_pvl_bot.telegram.BotStateCash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MessageHandler {
    private final BotStateCash botStateCash;
    private final MenuService menuService;
    private final LocaleMessageService localeMessageService;
    private final InfoService infoService;
    private final CompareService compareService;
    @Autowired
    public MessageHandler(BotStateCash botStateCash, MenuService menuService, LocaleMessageService localeMessageService, InfoService infoService, CompareService compareService) {
        this.botStateCash = botStateCash;
        this.menuService = menuService;
        this.localeMessageService = localeMessageService;
        this.infoService = infoService;
        this.compareService = compareService;
    }

    public BotApiMethod<?> handle(Message message, BotState newBotState) {
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();
        String messageText = message.getText();
        BotState oldBotState = botStateCash.getBotState(userId);

        if(newBotState==null){
            if(messageText.equals(localeMessageService.getMessage("back", chatId))
            || ((oldBotState==BotState.SELECT_LANG) && (!messageText.equals(
                    localeMessageService.getMessage("change_lang", chatId))))){
                botStateCash.popBotState(chatId);
                newBotState = botStateCash.getBotState(chatId);
            }else{
                newBotState = nextStep(oldBotState, message.getText(), chatId);
                if(!oldBotState.equals(newBotState)){
                    botStateCash.saveBotState(userId, newBotState);
                }
            }
        } else {
            botStateCash.saveBotState(userId, newBotState);
        }

        System.out.println(botStateCash.getStack(chatId));



        String nameOfNewBotState = newBotState.name();
        switch (nameOfNewBotState) {
            case "START":
                return menuService.getWelcomeMessage(chatId);
            case "MAIN_MENU":
                return menuService.getMainMenuMessage(message.getText(), chatId);
            case "SELECT_ALL":
                return menuService.getMenuSelectAllMessage(chatId);
            case "SELECT_ONE":
                if (message.getText().equals(localeMessageService.getMessage("show_one", chatId))) {
                    return menuService.getMenuSelectOneMessage(chatId);
                } else{
                    return infoService.getOneVuzInfo(chatId, message.getText());
                }
            case "SEARCH":
                return menuService.getMessageWithSearchInstruction(chatId);
            case "TYPE_SEARCH_QUERY":
                return menuService.getSearchResults(message.getText(), chatId);
            case "COMPARE":
                return menuService.getCompareMessageInfo(chatId);
            case "COMPARE_BYNAME":
            case "COMPARE_BYCODE":
            case "COMPARE_BYNAME_AND_CODE":
                return compareService.getCompareMessage(chatId, nameOfNewBotState.toLowerCase());
            case "HELP":
                return menuService.getHelpMessage(chatId);
            case "SELECT_LANG":
                return menuService.getSelectLangMessage(chatId);
            default:
                return menuService.getWelcomeMessage(chatId);
        }
    }

    private BotState nextStep(BotState oldBotState, String messageText, long chatId) {
        String nameOfBotState = oldBotState.name();
        BotState newBotState = null;
        switch (nameOfBotState) {
            case "START":
                newBotState = BotState.MAIN_MENU;
                break;
            case "MAIN_MENU":
                if (messageText.equals(localeMessageService.getMessage("show_all", chatId))) {
                    newBotState = BotState.SELECT_ALL;
                } else if (messageText.equals(localeMessageService.getMessage("show_one", chatId))) {
                    newBotState = BotState.SELECT_ONE;
                } else if (messageText.equals(localeMessageService.getMessage("change_lang", chatId))) {
                    newBotState = BotState.SELECT_LANG;
                } else {
                    newBotState = BotState.MAIN_MENU;
                }
                break;
            case "SELECT_ALL":
                if (messageText.equals(localeMessageService.getMessage("search", chatId))) {
                    newBotState = BotState.SEARCH;
                } else if(messageText.equals(localeMessageService.getMessage("show_all.compare_spec", chatId))){
                    newBotState = BotState.COMPARE;
                }
                else {
                    newBotState = BotState.SELECT_ALL;
                }
                break;
            case "COMPARE":
            case "COMPARE_BYNAME":
            case "COMPARE_BYCODE":
            case "COMPARE_BYNAME_AND_CODE":
                if (messageText.equals(localeMessageService.getMessage("compare_byname", chatId))){
                    newBotState = BotState.COMPARE_BYNAME;
                } else if(messageText.equals(localeMessageService.getMessage("compare_bycode", chatId))){
                    newBotState = BotState.COMPARE_BYCODE;
                } else if(messageText.equals(localeMessageService.getMessage("compare_byname_and_code", chatId))){
                    newBotState = BotState.COMPARE_BYNAME_AND_CODE;
                }
                break;
            case "SEARCH":
                newBotState = BotState.TYPE_SEARCH_QUERY;
                break;
            case "TYPE_SEARCH_QUERY":
                newBotState = BotState.TYPE_SEARCH_QUERY;
                break;
            case "SELECT_LANG":
                if(!messageText.equals(localeMessageService.getMessage("change_lang", chatId))){
                    botStateCash.popBotState(chatId);
                    newBotState = botStateCash.getBotState(chatId);
                }else{
                    newBotState = BotState.SELECT_LANG;
                }
                break;
            default:
                newBotState = oldBotState;
        }
        return newBotState;
    }


    /*private BotState previousStep(BotState oldBotState, String messageText, long chatId) {
        String nameOfBotState = oldBotState.name();
        BotState newBotState;
        switch (nameOfBotState) {
            case "SELECT_ALL":
            case "SELECT_ONE":
            case "THIS_ONE_SELECTED":
                newBotState = BotState.MAIN_MENU;
                break;
            case "TYPE_SEARCH_QUERY":
            case "SEARCH":
                newBotState = BotState.SELECT_ALL;
                break;
            case "COMPARE_BYNAME":
            case "COMPARE_BYCODE":
            case "COMPARE_BYNAME_AND_CODE":
                newBotState = BotState.COMPARE;
                break;
            case "MAIN_MENU":
            default:
                newBotState = BotState.START;
        }
        return newBotState;
    }*/
}
