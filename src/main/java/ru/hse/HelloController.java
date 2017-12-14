package ru.hse;

import gov.nasa.worldwind.geom.LatLon;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.hse.util.Config;
import ru.hse.util.Processor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

//@RequestMapping(value = "/say")
@Controller
public class HelloController {
    @Autowired
    private Config config;

    @Autowired
    private Processor processor;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String printHello(ModelMap model) {
        model.addAttribute("message", "Hello Spring MVC Framework!");
        return "hello";
    }

    @RequestMapping(value = "/getndvi",
            method = RequestMethod.GET,
            produces = "image/png")
//    produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] getNDVI(@RequestParam("year") String year,
                          @RequestParam("month") String month,
                          @RequestParam("tr") String tr, // "70.75_35.55"
                          @RequestParam("bl") String bl
                          /*ModelMap model*/) throws IOException {
//        model.addAttribute("month", month);
//        model.addAttribute("year", year);
//        return "ndvi";
//        InputStream in = HelloController.class.getResourceAsStream("/resources/RenderData.png");

        LatLon topR = config.getCoordinates(tr);
        LatLon bottomL = config.getCoordinates(bl);

        config.getDoubleCoordinates(tr);
        config.getDoubleCoordinates(bl);
        String pathToFile = config.getDatesAndFiles().get(month + year);

        //TODO: delete for not debug
        pathToFile = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.TIFF";

        try {
            RenderedImage returnImage = processor.getImage(topR, bottomL, pathToFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        InputStream in = new FileInputStream(pathToFile);
//        InputStream in = new FileInputStream("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.png");
//        return IOUtils.toByteArray(in);
        BufferedImage img = ImageIO.read(in);

        // Create a byte array output stream.
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        // Write to output stream
        ImageIO.write(img, "tiff", bao);

        return bao.toByteArray();
    }

    @RequestMapping(value = "/getTiles",
            method = RequestMethod.GET)
    @ResponseBody
    public String getTiles(@RequestParam("count") String tilesNum,
                           @RequestParam("tr") String tr, // "70.75_35.55"
                           @RequestParam("bl") String bl){
        String response = "success";
        String pathToFile = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.TIFF";
        try {
            processor.getImage(config.getDoubleCoordinates(tr), config.getDoubleCoordinates(bl), pathToFile,
                    Integer.valueOf(tilesNum));
        } catch (Exception e) {
            response = e.getMessage();
        }

        return response;
    }

    @RequestMapping(value = "/mosaic",
            method = RequestMethod.GET)
    @ResponseBody
    public String mosaic(@RequestParam("tr") String tr, // "70.75_35.55"
                           @RequestParam("bl") String bl){
        String response = "success";
        String pathToFile = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.TIFF";
        try {
            processor.bandTiles(new String[]{});
        } catch (Exception e) {
            response = e.getMessage();
        }

        return response;
    }



 @RequestMapping(value = "/getbase64ndvi",
            method = RequestMethod.GET,
            produces = "image/png")
//    produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] getBase64NDVI(@RequestParam("year") String year,
                          @RequestParam("month") String month/*,
                          ModelMap model*/) throws IOException {

        InputStream in = new FileInputStream("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.png");
        BufferedImage img = ImageIO.read(in);

        // Create a byte array output stream.
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        // Write to output stream
        ImageIO.write(img, "tiff", bao);

     return Base64.getEncoder().encode(bao.toByteArray());
//     return bao.toByteArray();
    }


}