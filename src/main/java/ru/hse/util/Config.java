package ru.hse.util;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Config {

    private Map<String, String> datesAndFiles;

    public Config() {
        this.datesAndFiles = new HashMap<>();
        String path = getClass().getClassLoader().getResource("01.tiff").getPath().replace("01.tiff", "");//"/Users/victoria/IdeaProjects/mvcexample/src/main/resources/";
        datesAndFiles.put("01", path + "01.tiff");
        datesAndFiles.put("02", path + "02.TIFF");
        datesAndFiles.put("03", path + "03.TIFF");
        datesAndFiles.put("04", path + "04.TIFF");
        datesAndFiles.put("05", path + "05.TIFF");
        datesAndFiles.put("06", path + "06.TIFF");
        datesAndFiles.put("07", path + "07.TIFF");
        datesAndFiles.put("08", path + "08.TIFF");
        datesAndFiles.put("09", path + "09.TIFF");
        datesAndFiles.put("10", path + "10.TIFF");
        datesAndFiles.put("11", path + "11.TIFF");
    }

    public Map<String, String> getDatesAndFiles() {
        return datesAndFiles;
    }

//    public LatLon getCoordinates(String str) {
//        String[] coords = str.split("_");
//        return LatLon.fromDegrees(Double.parseDouble(coords[0]), Double.parseDouble(coords[1])); // lat, lon
//    }


    public double[] getDoubleCoordinates(String str) {
        String[] coords = str.split("_");
        return new double[]{Double.parseDouble(coords[0]), Double.parseDouble(coords[1])}; // lat, lon
    }

    public Integer[] getTileNumberForCoords(String coords, int numOfTiles) {
        double[] dooubleCoords = getDoubleCoordinates(coords);

        double tileWidth = 360.0 / numOfTiles;
        double tileHeight = 360.0 / numOfTiles;

        int tileStartX =  (int) ((dooubleCoords[0] + 180) / tileWidth);
//        int tileStartX = dooubleCoords[0] > 0 ? (int) (dooubleCoords[0] / tileWidth) + numOfTiles / 2
//                : (int) (Math.abs(dooubleCoords[0]) / tileWidth);
        int tileStartY = (int) ((dooubleCoords[1] + 180) / tileHeight);
//        int tileStartY = dooubleCoords[1] >= 0 ? (int) (dooubleCoords[1] / tileHeight) + numOfTiles / 2
//                : (int) (Math.abs(dooubleCoords[1]) / tileHeight);

        return new Integer[]{tileStartX, tileStartY}; // TODO: доделать
    }

}
