package ru.hse.client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

public class Client {

    public static void main(String[] args) throws MalformedURLException {
        final URL url = new URL("http://localhost:8080/mvc-example-1.0" +
                "/getbase64ndvi?year=2017&month=09");

        try (
                final InputStream in = url.openStream();
                final InputStreamReader reader = new InputStreamReader(in);//, decoder);
//                final FileOutputStream fis = new FileOutputStream("test.png");
        ) {
            BufferedImage img = ImageIO.read(in);
            ImageIO.write(img, "png", new File("saved2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

//        final String test = sb.toString();
    }

    static void getBse64() throws MalformedURLException {
        final URL url = new URL("http://localhost:8080/mvc-example-1.0" +
                "/getbase64ndvi?year=2017&month=09");

        try (
                final InputStream in = url.openStream();
                final InputStreamReader reader = new InputStreamReader(in);//, decoder);
//                final FileOutputStream fis = new FileOutputStream("test.png");
        ) {
            char[] bytes = new char[4096];
           reader.read(bytes);
            byte[] decodedBytes = Base64.getDecoder()
                    .decode(new String(bytes).getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
