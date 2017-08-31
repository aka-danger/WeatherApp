package Weather;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to send a GET request.
 * /

/**
 * @author Brendon Clark
 * @version 1.0
 */
public class WebServiceClient {

    private static final String USER_AGENT = "Mozilla/5.0";

    //<editor-fold defaultstate="collapsed" desc="GET Request">
    /**
     * HTTP GET Request
     * @param http address of the database
     * @param params query strings
     * @return JSON
     */
    public static String get(String http ,List<Pair<String,String>> params){
        String result = null;
        try{
            URL url = buildURL(http, (ArrayList<Pair<String, String>>)params);
            if (url== null) return null; //escape
            System.out.println(url.toString());

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            result = response.toString();
        }catch(Exception e){
        }
        //print result
        return  result ;//response.toString();//System.out.println(response.toString());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="URL creation">
    /**
     * Will take in a http address and attach some query strings to it.
     * doesn't need to be super complex.
     * @param httpAddress that the database is using
     * @param params the query string values for the web service
     * @return final URL to call the web service
     */
    public static URL buildURL(String httpAddress,ArrayList<Pair<String,String>> params) throws Exception{
        if(httpAddress == null) throw new Exception("No http address found.");
        StringBuilder http = new StringBuilder(httpAddress);
        if(params != null){
            http.append("?");
            for(Pair<String,String> item : params){
                http.append(item.first)
                        .append("=")
                        .append(item.second)
                        .append("&");
            }
            //We need to remove the last character of the string ("&")
            String result = http.toString();
            return new URL(result.substring(0,result.length()-1));
        }else return null;
    }
    //</editor-fold>
}

