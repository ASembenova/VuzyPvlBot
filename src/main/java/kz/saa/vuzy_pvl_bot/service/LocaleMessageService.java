package kz.saa.vuzy_pvl_bot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class LocaleMessageService {
    private final Map<Long, Locale> localeMap = new HashMap<>();
    private Locale defaultLocale;
    private MessageSource messageSource;

    public LocaleMessageService(@Value("${localeTag}") String localeTag, MessageSource messageSource) {
        this.messageSource = messageSource;
        this.defaultLocale = Locale.forLanguageTag(localeTag);
    }

    public void changeLang(String localeTag, long chatId){
        localeMap.put(chatId, Locale.forLanguageTag(localeTag));
    }

    public String getMessage(String message, long chatId) {
        if(!localeMap.containsKey(chatId)){
            localeMap.put(chatId, defaultLocale);
        }
        Locale locale = localeMap.get(chatId);
        return messageSource.getMessage(message, null, locale);
    }

    public String getLocaleTag(long chatId){
        if(!localeMap.containsKey(chatId)){
            localeMap.put(chatId, defaultLocale);
        }
        return localeMap.get(chatId).getLanguage();
    }


    public boolean isEmpty(){
        return localeMap.isEmpty();
    }

    public boolean containsUser(long chatId){
        return localeMap.containsKey(chatId);
    }


}
