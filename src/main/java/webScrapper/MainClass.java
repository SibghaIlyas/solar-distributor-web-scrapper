package webScrapper;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainClass {
    public static void main(String[] arg) throws IOException, InterruptedException, GeneralSecurityException, ParseException {
        WebScrapper webScrapper = new WebScrapper();
        webScrapper.login();
        webScrapper.SolarDistributorWebScrapper();


    }
}
