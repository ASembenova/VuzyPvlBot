package kz.saa.vuzy_pvl_bot.service;

import kz.saa.vuzy_pvl_bot.egovapi.DataObjectsService;
import kz.saa.vuzy_pvl_bot.egovapi.Vuz;
import kz.saa.vuzy_pvl_bot.model.Degree;
import kz.saa.vuzy_pvl_bot.model.Speciality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SpecialityService {

    @Autowired
    private final SplitterService splitterService;

    @Autowired
    private final DataObjectsService dataObjectsService;

    @Autowired
    private final LocaleMessageService localeMessageService;

    public SpecialityService(SplitterService splitterService, DataObjectsService dataObjectsService, LocaleMessageService localeMessageService) {
        this.splitterService = splitterService;
        this.dataObjectsService = dataObjectsService;
        this.localeMessageService = localeMessageService;
    }

    public String searchByKeyword(String keyword, long chatId){
        Vuz[] vuzy = dataObjectsService.getVuzy();
        int counter = 0;
        StringBuilder result = new StringBuilder();
        for (Vuz v:vuzy) {
            if(localeMessageService.getLocaleTag(chatId)=="kz"){
                result.append(splitterService.splitFullname(v.name1));
            } else{
                result.append(splitterService.splitFullname(v.name2));
            }
            result.append("\n");
            StringBuilder temp = new StringBuilder();
            List<String> list = getSimpleListByRegex(v, chatId);
            for (String spec:list) {
                if (spec.toLowerCase().contains(keyword.toLowerCase())){
                    temp.append(spec);
                    temp.append("\n");
                    counter++;
                }
            }
            if (temp.toString().isEmpty()){
                temp.append(localeMessageService.getMessage("not_found", chatId)+"\n");
            }
            result.append(temp.toString());
            result.append("\n");
        }
        result.append(localeMessageService.getMessage("total", chatId));
        result.append(": "+counter);
        return result.toString();
    }

    public List<String> getSimpleListByRegex(Vuz vuz, long chatId){
        List<String> list = new ArrayList<>();
        String data;
        if(localeMessageService.getLocaleTag(chatId)=="kz"){
            data = vuz.name6;
        } else{
            data = vuz.name7;
        }
        data = data.replaceAll(", 6", ",\n6");
        data = data.replaceAll(",6", ",\n6");
        data = data.replaceAll(",  6", ",\n6");
        data = data.replaceAll(", 5", ",\n5");
        data = data.replaceAll(", 7", ",\n7");
        data = data.replaceAll(", 8", ",\n8");
        String regex = "([5-8])([BDMВМ])(\\d{5,6})(.*)([,]?)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        while (matcher.find()) {
            String found = data.substring(matcher.start(), matcher.end());
            if ((found.charAt(found.length()-1))==','){
                found = found.substring(0, found.length()-1).trim();
            }
            list.add(found);
        }
        return list;
    }

    public Set<Speciality> getSpecialitySet(Vuz vuz, Comparator<Speciality> comparator){
        Set<Speciality> set = new TreeSet<>(comparator);
        String data = vuz.name7;
        data = data.replaceAll(", 6", ",\n6");
        data = data.replaceAll(",6", ",\n6");
        data = data.replaceAll(",  6", ",\n6");
        data = data.replaceAll(", 5", ",\n5");
        data = data.replaceAll(", 7", ",\n7");
        data = data.replaceAll(", 8", ",\n8");
        String regex = "([5-8])([BDMВМ])(\\d{5,6})([\\s-]+)([^\\n]*)([,]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        while (matcher.find()) {
            Speciality spec = new Speciality();
            spec.setVuzFullname(splitterService.splitFullname(vuz.name2));
            spec.setCode(matcher.group(1)+matcher.group(2)+matcher.group(3));
            //spec.setCode(matcher.group(3));
            String name = matcher.group(5).trim();
            if(name.charAt(name.length()-1)==','){
                name = name.substring(0, name.length()-1);
            }
            spec.setName(name);
            switch (matcher.group(2)){
                case "B":
                case "В":
                    spec.setDegree(Degree.B);
                    break;
                case "M":
                case "М":
                    spec.setDegree(Degree.M);
                    break;
                case "D":
                    spec.setDegree(Degree.D);
                    break;
                /*default:
                    spec.setDegree(Degree.B);*/
            }
            set.add(spec);
        }

        return set;

    }

    public List<Set<Speciality>> getIntersectionByName(Vuz v1, Vuz v2){
        //Comparator<Speciality> byName = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCodeSize).thenComparing(Speciality::getCodeFirstSymbol).thenComparing(Speciality::getName);
        Comparator<Speciality> byName = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCodeSize).thenComparing(Speciality::getCodeFirstSymbol).thenComparing(Speciality::getMeaningNumber).thenComparing(Speciality::getName);
        return getIntersection(v1, v2, byName);
    }

    public List<Set<Speciality>> getIntersectionByCode(Vuz v1, Vuz v2){
        //Comparator<Speciality> byCode = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCodeSize).thenComparing(Speciality::getCodeFirstSymbol).thenComparing(Speciality::getCode);
        //Comparator<Speciality> byCode = Comparator.comparing(Speciality::getCode);
        Comparator<Speciality> byCode = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCode);
        //Comparator<Speciality> byCode = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCodeSize).thenComparing(Speciality::getCodeFirstSymbol).thenComparing(Speciality::getCode);

        return getIntersection(v1, v2, byCode);
    }

    public List<Set<Speciality>> getIntersectionByNameAndCode(Vuz v1, Vuz v2){
        //Comparator<Speciality> byNameAndCode = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCodeSize).thenComparing(Speciality::getCodeFirstSymbol).thenComparing(Speciality::getName).thenComparing(Speciality::getCode);
        Comparator<Speciality> byNameAndCode = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCode).thenComparing(Speciality::getName);
        return getIntersection(v1, v2, byNameAndCode);
    }

    public List<Set<Speciality>> getDifferenceByName(Vuz v1, Vuz v2){
        //Comparator<Speciality> byName = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCodeSize).thenComparing(Speciality::getCodeFirstSymbol).thenComparing(Speciality::getName);
        Comparator<Speciality> byName = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCodeSize).thenComparing(Speciality::getCodeFirstSymbol).thenComparing(Speciality::getMeaningNumber).thenComparing(Speciality::getName);
        return getDifference(v1, v2, byName);
    }

    public List<Set<Speciality>> getDifferenceByCode(Vuz v1, Vuz v2){
        Comparator<Speciality> byCode = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCode);
        //Comparator<Speciality> byCode = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCodeSize).thenComparing(Speciality::getCodeFirstSymbol).thenComparing(Speciality::getCode);
        //Comparator<Speciality> byCode = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCodeSize).thenComparing(Speciality::getCodeFirstSymbol).thenComparing(Speciality::getCode);
        return getDifference(v1, v2, byCode);
    }

    public List<Set<Speciality>> getDifferenceByNameAndCode(Vuz v1, Vuz v2){
        //Comparator<Speciality> byNameAndCode = Comparator.comparing(Speciality::getName).thenComparing(Speciality::getCode);
        Comparator<Speciality> byNameAndCode = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCode).thenComparing(Speciality::getName);
        //Comparator<Speciality> byNameAndCode = Comparator.comparing(Speciality::getDegree).thenComparing(Speciality::getCodeSize).thenComparing(Speciality::getCodeFirstSymbol).thenComparing(Speciality::getName).thenComparing(Speciality::getCode);

        return getDifference(v1, v2, byNameAndCode);
    }

    public List<Set<Speciality>> getIntersection(Vuz v1, Vuz v2, Comparator<Speciality> comparator){
        Set<Speciality> set1;
        set1 = getSpecialitySet(v1, comparator);
        Set<Speciality> set2;
        set2 = getSpecialitySet(v2, comparator);
        Set<Speciality> setCommon1 = new TreeSet<>(comparator);
        setCommon1.addAll(set1);
        setCommon1.retainAll(set2);
        Set<Speciality> setCommon2 = new TreeSet<>(comparator);
        setCommon2.addAll(set2);
        setCommon2.retainAll(set1);
        List<Set<Speciality>> commons = new ArrayList<>();
        commons.add(setCommon1);
        commons.add(setCommon2);
        return commons;
    }

    public List<Set<Speciality>> getDifference(Vuz v1, Vuz v2, Comparator<Speciality> comparator){
        Set<Speciality> set1 = getSpecialitySet(v1,comparator);
        set1.removeAll(getIntersection(v1, v2, comparator).get(0));
        Set<Speciality> set2 = getSpecialitySet(v2,comparator);
        set2.removeAll(getIntersection(v1, v2, comparator).get(1));
        List<Set<Speciality>> difference = new ArrayList<>();
        difference.add(set1);
        difference.add(set2);
        return difference;
    }



}
