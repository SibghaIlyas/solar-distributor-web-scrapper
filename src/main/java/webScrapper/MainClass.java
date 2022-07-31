package webScrapper;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainClass {

    public static void main(String[] arg) throws IOException, GeneralSecurityException, InterruptedException {
        WebScrapper webScrapper = new WebScrapper();
        webScrapper.login();
        webScrapper.SolarDistributorWebScrapper();


    }
}
