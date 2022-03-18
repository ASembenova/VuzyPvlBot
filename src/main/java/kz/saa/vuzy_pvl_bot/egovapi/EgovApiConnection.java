package kz.saa.vuzy_pvl_bot.egovapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Component
@PropertySource("classpath:application.properties")
public class EgovApiConnection {
    @Value("https://data.egov.kz/api/v4/zhogary_oku_oryndarynyn_tizbes/v5?apiKey=${apikey}")
    private String url;

    public String getUrl() {
        return url;
    }

    public String getResponseByEgovAPI(URL url){
        String result = "";
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            result = response.toString();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(result);
        return result;
    }

    public Vuz[] createVuzObjects(){
        Vuz[] vuzy = null;
        try {
            String response = getResponseByEgovAPI(new URL(getUrl()));
            Gson gson = new GsonBuilder().create();
            vuzy = gson.fromJson(response, Vuz[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vuzy;
    }

    public Metadata createMetadataObject(){
        Metadata metadata = null;
        try {
            URL url = new URL("https://data.egov.kz/meta/zhogary_oku_oryndarynyn_tizbes/v5?pretty");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            Gson gson = new GsonBuilder().create();
            metadata = (Metadata) gson.fromJson(response.toString(), Metadata.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return metadata;
    }


}
