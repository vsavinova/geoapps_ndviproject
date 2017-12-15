package ru.hse;

import gov.nasa.worldwind.geom.LatLon;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
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
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;

//@RequestMapping(value = "/say")
@Controller
public class HelloController {
    @Autowired
    private Config config = new Config();

    int TILES_NUM = 10;

//    @Autowired
//    private Processor processor = new Processor();

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String printHello(ModelMap model) {
        model.addAttribute("message", "Hello Spring MVC Framework!");
        return "hello";
    }

    @RequestMapping(value = "/getndvi",
            method = RequestMethod.GET)
//            produces = "image/png")
//    produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public String getNDVI(
                          @RequestParam("month") String month,
                          @RequestParam("tr") String tr, // "70.75_35.55"
                          @RequestParam("bl") String bl
                          /*ModelMap model*/) throws IOException {
//        model.addAttribute("month", month);
//        model.addAttribute("year", year);
//        return "ndvi";
//        InputStream in = HelloController.class.getResourceAsStream("/resources/RenderData.png");

//        LatLon topR = config.getCoordinates(tr);
//        LatLon bottomL = config.getCoordinates(bl);
//        Config config= new Config();
        String response = "{message: %s, image: %s}";
        config.getDoubleCoordinates(tr);
        config.getDoubleCoordinates(bl);
        String pathToFile = config.getDatesAndFiles().get(month );

        //TODO: delete for not debug
//        pathToFile = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.TIFF";

        try {
//            RenderedImage returnImage = processor.getImage(topR, bottomL, pathToFile);


//        InputStream in = new FileInputStream(pathToFile);
//        InputStream in = new FileInputStream("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.png");
//        return IOUtils.toByteArray(in);
        GeoTiffFormat geoTiffFormat = new GeoTiffFormat();
        GeoTiffReader reader = geoTiffFormat.getReader(new File(pathToFile));
        GridCoverage2D img = reader.read(null);//ImageIO.read(in);

        // Create a byte array output stream.
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        // Write to output stream
//        ImageIO.write(img, "tiff", bao);
        geoTiffFormat.getWriter(bao).write(img,null);

        response = String.format(response, "ok", bao.toByteArray());
        } catch (Exception e) {
            response = String.format(response, "error", "");
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "/getTile",
            method = RequestMethod.GET)
    @ResponseBody
    public String getTile(@RequestParam("count") String tilesNum,
                          @RequestParam("tr") String tr, // "70.75_35.55"
                          @RequestParam("bl") String bl) {
        String response = "{message: %s, image: %s}";
        String pathToFile = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/tiles/0_0.TIFF";//enderData.TIFF";

        try {
            Processor processor = new Processor();
//            Config config= new Config();

            double[] doubleCoordinatesTr = config.getDoubleCoordinates(tr);
            double[] doubleCoordinatesBl = config.getDoubleCoordinates(bl);
            Integer[] trNums = config.getTileNumberForCoords(tr, TILES_NUM);
            Integer[] blNums = config.getTileNumberForCoords(bl, TILES_NUM);
            GridCoverage2D image;
            String pathToDir = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/tiles/";
            ArrayList<String> pathToTiles = new ArrayList<>();
//                    pathToDir + blNums[0] + "_" + blNums[1] + ".tiff",
//                    pathToDir + trNums[0] + "_" + trNums[1] + ".tiff"
//            };



            if (Math.abs(trNums[0] - blNums[0] + blNums[1] - trNums[1]) > 0) {
                for (int i = 0; i <= Math.abs(trNums[0] - blNums[0]); i++) {
                    for (int j = 0; j <= Math.abs(blNums[1] - trNums[1]); j++)
                        pathToTiles.add(pathToDir + String.valueOf(blNums[0] + i) + "_" +
                                String.valueOf(blNums[1] + j) + ".tiff");
//                pathToTiles.add(pathToDir + String.valueOf(blNums[0] + i) + "_" +
//                        String.valueOf(blNums[1]) + ".tiff");
                }
//                String resultFileName = "mosresult.tiff";
                image = processor.bandTiles(pathToTiles, "result.tiff");

//                image = /*processor.getMosaic(config, tr, bl, TILES_NUM, resultFileName);
//                pathToFile = pathToFile.replace(pathToFile.substring(pathToFile.lastIndexOf("/")),
//                        "/" + resultFileName);*/
            } else
                image = processor.getImage(pathToFile,
                        Integer.valueOf(tilesNum));

            // Create a byte array output stream.
//            ByteArrayOutputStream bao = new ByteArrayOutputStream();

            GeoTiffFormat geoTiffFormat = new GeoTiffFormat();
//            String resultPath = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/moss.TIFF";
            String resultFileName = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/FINAL.TIFF";
            File resultTile = new File(resultFileName);
            geoTiffFormat.getWriter(new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/FINAL.TIFF"))
                    .write(image, null);

//            geoTiffFormat.getWriter(resultTile)
//                    .write(image, null);
            // Write to output stream
//            geoTiffFormat.getWriter(bao).write(image, null);

//            try /*(ByteArrayOutputStream byteArrayOutputStream = bao = Files.readAllBytes(resultTile.toPath()))*/ {
                byte[] bytes = Files.readAllBytes(resultTile.toPath());
//                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
//                        resultFileName));
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                int bytes = 0;


                response = String.format(response,"ok", new String(Base64.getEncoder().encode(bytes)));
//                baos.close();
//                bis.close();

            } catch (Exception e) {
                response = String.format(response, "error", "");
                e.printStackTrace();//e.getMessage();
            }

            return response;
        }


    @RequestMapping(value = "/createTiles",
            method = RequestMethod.GET)
    @ResponseBody
    public String createTiles(@RequestParam("count") String tilesNum) {
        String response = "success";
        String pathToImage = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.TIFF";
        try {
            Processor processor = new Processor();

            processor.loadTiles(pathToImage,
                    Integer.valueOf(tilesNum));
            TILES_NUM = Integer.valueOf(tilesNum);
        } catch (Exception e) {
            response = e.getMessage();
        }

        return response;
    }



    @RequestMapping(value = "/mosaic",
            method = RequestMethod.GET)
    @ResponseBody
    public String mosaic(@RequestParam("tr") String tr, // "70.75_35.55"
                         @RequestParam("bl") String bl) {
        String response = "{message:%s, image:$s";
        String pathToFile = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.TIFF";
        try {
            Processor processor = new Processor();
            Integer[] trNums = config.getTileNumberForCoords(tr, TILES_NUM);
            Integer[] blNums = config.getTileNumberForCoords(bl, TILES_NUM);
            String pathToDir = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/tiles/";
            ArrayList<String> pathToTiles = new ArrayList<>();
//                    pathToDir + blNums[0] + "_" + blNums[1] + ".tiff",
//                    pathToDir + trNums[0] + "_" + trNums[1] + ".tiff"
//            };

            for (int i = 0; i <= trNums[0] - blNums[0]; i++) {
                for (int j = 0; j <= blNums[1] - trNums[1]; j++)
                    pathToTiles.add(pathToDir + String.valueOf(blNums[0] + i) + "_" +
                            String.valueOf(blNums[1] + j) + ".tiff");
//                pathToTiles.add(pathToDir + String.valueOf(blNums[0] + i) + "_" +
//                        String.valueOf(blNums[1]) + ".tiff");
            }


            GridCoverage2D img = processor.bandTiles(pathToTiles, "result.tiff");
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            GeoTiffFormat geoTiffFormat = new GeoTiffFormat();
//            geoTiffFormat.getWriter(new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/moss.TIFF"))
//                    .write(img, null);
            // Write to output stream
            geoTiffFormat.getWriter(bao).write(img, null);

            response = String.format(response, "ok", new String(Base64.getEncoder().encode(bao.toByteArray())));
        } catch (Exception e) {
            response = String.format(response, "error", "");
                    e.printStackTrace();
        }

        return response;
    }



    @RequestMapping(value = "/getTls",
            method = RequestMethod.GET)
    @ResponseBody
    public String getTls(@RequestParam("tr") String tr, // "70.75_35.55"
                         @RequestParam("bl") String bl) {
        String response = "{message: %s, image: %s}";
        String pathToFile = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.TIFF";
        try {
            Processor processor = new Processor();
            Integer[] trNums = config.getTileNumberForCoords(tr, TILES_NUM);
            Integer[] blNums = config.getTileNumberForCoords(bl, TILES_NUM);
            String pathToDir = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/tiles/";
            Integer[] tileNumTr = config.getTileNumberForCoords(tr, TILES_NUM);
            Integer[] tileNumBl = config.getTileNumberForCoords(bl, TILES_NUM);
            if (Math.abs(tileNumTr[0] - tileNumBl[0] + tileNumBl[1] - tileNumTr[1]) > 0) {

                ArrayList<String> pathToTiles = new ArrayList<>();
//                    pathToDir + blNums[0] + "_" + blNums[1] + ".tiff",
//                    pathToDir + trNums[0] + "_" + trNums[1] + ".tiff"
//            };

                for (int i = 0; i <= Math.abs(trNums[0] - blNums[0]); i++) {
                    for (int j = 0; j <= Math.abs(blNums[1] - trNums[1]); j++)
                        pathToTiles.add(pathToDir + String.valueOf(blNums[0] + i) + "_" +
                                String.valueOf(blNums[1] + j) + ".tiff");
//                pathToTiles.add(pathToDir + String.valueOf(blNums[0] + i) + "_" +
//                        String.valueOf(blNums[1]) + ".tiff");
                }


                GridCoverage2D image = processor.bandTiles(pathToTiles, "result.tiff");
                ByteArrayOutputStream bao = new ByteArrayOutputStream();

                GeoTiffFormat geoTiffFormat = new GeoTiffFormat();
                geoTiffFormat.getWriter(new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/mosas.TIFF"))
                        .write(image, null);
                // Write to output stream
                geoTiffFormat.getWriter(bao).write(image, null);
                response = String.format(response, new String(Base64.getEncoder().encode(bao.toByteArray())));
            }
        } catch (Exception e) {
            response = String.format(response, "error", "");
            e.printStackTrace();//e.getMessage();
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