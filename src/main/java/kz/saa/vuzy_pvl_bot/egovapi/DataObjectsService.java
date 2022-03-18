package kz.saa.vuzy_pvl_bot.egovapi;

import kz.saa.vuzy_pvl_bot.service.LocaleMessageService;
import kz.saa.vuzy_pvl_bot.service.SplitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataObjectsService {
    private Vuz[] vuzy;
    private Metadata metadata;
    private final LocaleMessageService localeMessageService;
    private final SplitterService splitterService;
    @Autowired
    public DataObjectsService(EgovApiConnection egovApiConnection, LocaleMessageService localeMessageService, SplitterService splitterService){
        this.localeMessageService = localeMessageService;
        vuzy = egovApiConnection.createVuzObjects();
        metadata = egovApiConnection.createMetadataObject();
        this.splitterService = splitterService;
    }

    public Vuz[] getVuzy() {
        return vuzy;
    }

    public Metadata getMetadata(){
        return metadata;
    }

    public int getIndexByVuzName(String vuzname, long chatId){
        int index = 0;
        String[] vuzFullname = new String[vuzy.length];
        if(localeMessageService.getLocaleTag(chatId)=="ru"){
            for (int i = 0; i < vuzy.length; i++) {
                vuzFullname[i] = vuzy[i].name2;
            }
        } else if(localeMessageService.getLocaleTag(chatId)=="kz"){
            for (int i = 0; i < vuzy.length; i++) {
                vuzFullname[i] = vuzy[i].name1;
            }
        }
        for (int i = 0; i < vuzy.length; i++) {
            if(vuzFullname[i].contains(vuzname)){
                index = i;
                break;
            }
        }
        return index;
    }
    
    public String oneVuzInfo(int index, long chatId){
        if(localeMessageService.getLocaleTag(chatId)=="kz"){
            return oneVuzInfoKz(index);
        } else {
            return oneVuzInfoRu(index);
        }
    }

    private String oneVuzInfoRu(int index) {
        StringBuilder message = new StringBuilder();
        message.append("<b>");
        message.append(metadata.fields.name2.labelRu); //name rus
        message.append("</b>:\n");
        message.append(vuzy[index].name2);
        message.append("\n\n");
        message.append(metadata.fields.name5.labelRu); //direction
        message.append(":\n");
        message.append(vuzy[index].name5);
        message.append("\n\n");
        message.append(metadata.fields.name3.labelRu); //fio ruk
        message.append(" :male_office_worker: :\n");
        message.append(vuzy[index].name3);
        message.append("\n\n");
        message.append(metadata.fields.name8.labelRu); //number
        message.append(" :busts_in_silhouette: :\n");
        message.append(vuzy[index].name8);
        message.append("\n\n");
        message.append(metadata.fields.name15.labelRu);//web-site
        message.append(" :globe_with_meridians: :\n");
        message.append(vuzy[index].name15);
        message.append("\n\n");
        message.append(metadata.fields.name17.labelRu); //address
        message.append(" :office: :\n");
        message.append(vuzy[index].name10);
        message.append(", ");
        message.append(vuzy[index].name17);
        message.append("\n\n");
        message.append(metadata.fields.name11.labelRu); //phone
        message.append(" :telephone_receiver::\n");
        message.append(vuzy[index].name11);
        message.append("\n\n");
        message.append(metadata.fields.name12.labelRu); //email
        message.append(" :e-mail: :\n");
        message.append(vuzy[index].name12);
        message.append("\n\n");
        message.append(metadata.fields.name14.labelRu); //mode
        message.append(" :hourglass_flowing_sand: :\n");
        message.append(vuzy[index].name14);
        message.append("\n\n");
        String textMessage = message.toString();
        return textMessage;
    }

    private String oneVuzInfoKz(int index) {
        StringBuilder message = new StringBuilder();
        message.append("<b>");
        message.append(metadata.fields.name1.labelKk); //name rus
        message.append("</b>:\n");
        message.append(vuzy[index].name1);
        message.append("\n\n");
        message.append(metadata.fields.name4.labelKk); //direction
        message.append(":\n");
        message.append(vuzy[index].name4);
        message.append("\n\n");
        message.append(metadata.fields.name3.labelKk); //fio ruk
        message.append(" :male_office_worker: :\n");
        message.append(vuzy[index].name3);
        message.append("\n\n");
        message.append(metadata.fields.name8.labelKk); //number
        message.append(" :busts_in_silhouette: :\n");
        message.append(vuzy[index].name8);
        message.append("\n\n");
        message.append(metadata.fields.name15.labelKk);//web-site
        message.append(":globe_with_meridians::\n");
        message.append(vuzy[index].name15);
        message.append("\n\n");
        message.append(metadata.fields.name16.labelKk); //address
        message.append(":office::\n");
        message.append(vuzy[index].name9);
        message.append(", ");
        message.append(vuzy[index].name16);
        message.append("\n\n");
        message.append(metadata.fields.name11.labelKk); //phone
        message.append(" :telephone_receiver::\n");
        message.append(vuzy[index].name11);
        message.append("\n\n");
        message.append(metadata.fields.name12.labelKk); //email
        message.append(":e-mail::\n");
        message.append(vuzy[index].name12);
        message.append("\n\n");
        message.append(metadata.fields.name13.labelKk); //mode
        message.append(":hourglass_flowing_sand::\n");
        message.append(vuzy[index].name13);
        message.append("\n\n");
        String textMessage = message.toString();
        return textMessage;
    }

    public List<String> vuzList(long chatId){
        List<String> list = new ArrayList<>();
        if(localeMessageService.getLocaleTag(chatId)=="kz"){
            for (Vuz v:vuzy) {
                list.add(splitterService.splitFullname(v.name1));
            }
        } else {
            for (Vuz v:vuzy) {
                list.add(splitterService.splitFullname(v.name2));
            }
        }
        return list;
    }

    public String getSiteUrl(int index){
        return vuzy[index].name15;
    }



}
