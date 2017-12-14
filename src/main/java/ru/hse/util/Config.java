package ru.hse.util;

import gov.nasa.worldwind.geom.LatLon;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Config {

    private Map<String, String> datesAndFiles;

    public Config() {
        this.datesAndFiles = new HashMap<>();
        datesAndFiles.put("0117", getClass().getClassLoader().getResource("0117.png").getPath());
        datesAndFiles.put("0217", getClass().getClassLoader().getResource("0217.png").getPath());
        datesAndFiles.put("0317", getClass().getClassLoader().getResource("0317.png").getPath());
        datesAndFiles.put("0417", getClass().getClassLoader().getResource("0417.png").getPath());
        datesAndFiles.put("0517", getClass().getClassLoader().getResource("0517.png").getPath());
        datesAndFiles.put("0617", getClass().getClassLoader().getResource("0617.png").getPath());
        datesAndFiles.put("0717", getClass().getClassLoader().getResource("0717.png").getPath());
        datesAndFiles.put("0817", getClass().getClassLoader().getResource("0817.png").getPath());
        datesAndFiles.put("0917", getClass().getClassLoader().getResource("0917.png").getPath());
        datesAndFiles.put("1017", getClass().getClassLoader().getResource("1017.png").getPath());
        datesAndFiles.put("1117", getClass().getClassLoader().getResource("1117.png").getPath());
    }

    public Map<String, String> getDatesAndFiles() {
        return datesAndFiles;
    }

    public LatLon getCoordinates(String str) {
        String[] coords = str.split("_");
        return LatLon.fromDegrees(Double.parseDouble(coords[0]), Double.parseDouble(coords[1])); // lat, lon
    }


    public double[] getDoubleCoordinates(String str) {
        String[] coords = str.split("_");
        return new double[]{Double.parseDouble(coords[0]), Double.parseDouble(coords[1])}; // lat, lon
    }

    public Integer getTileNumberForCoords(String tr, String bl){
        double[] trCoords = getDoubleCoordinates(tr);
        double[] blCoords = getDoubleCoordinates(bl);

        return 0; // TODO: доделать
    }

}
