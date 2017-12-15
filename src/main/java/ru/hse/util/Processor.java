package ru.hse.util;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@Service
public class Processor {

    private ru.hse.util.ImageTiler imageTiler = new ImageTiler();

//    public RenderedImage getImage(LatLon tr, LatLon bl, String pathToImage) throws FactoryException, TransformException, IOException {
//        // TODO: crop or tile
//
//        GeneralEnvelope transform = CRS.transform(new Envelope2D(CRS.decode("EPSG:32637"), bl.latitude.degrees, tr.longitude.degrees,
//                        bl.latitude.degrees - tr.latitude.degrees,
//                        tr.longitude.degrees - bl.longitude.degrees), // TODO: переделать в массивы double
//                CRS.decode("EPSG:4326"));
//
//        final GeoTiffFormat format = new GeoTiffFormat();
//
//        // getting a reader
//        GridCoverageReader reader = format.getReader(new File(pathToImage));
//        GridCoverage2D source = (GridCoverage2D) reader.read(null);
//        final CoverageProcessor processor = new CoverageProcessor();
//        final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
//        param.parameter("Source").setValue(source);
//        param.parameter("Envelope").setValue(transform);
//
//        GridCoverage2D coverage2D = (GridCoverage2D) processor.doOperation(param);
//        ImageIO.write(coverage2D.getRenderedImage(), "png",
//                new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/crop.png"));
//
//        ImageTiler imageTiler = new ImageTiler();
//        imageTiler.tileImage(ImageIO.read(new File(pathToImage)), Sector.boundingSector(tr, bl), null);
//
//        return null;
//    }

    public void loadTiles(String pathToImage, int tilesCount) throws IOException {
        final GeoTiffFormat format = new GeoTiffFormat();
        imageTiler.setInputFile(new File(pathToImage));
//        imageTiler.setInputFile(new File(ImgTiler.class.getClassLoader()
//                .getResource("RenderData.TIFF").getPath()));
        imageTiler.setNumberOfHorizontalTiles(tilesCount);
        imageTiler.setNumberOfVerticalTiles(tilesCount);
        imageTiler.setOutputDirectory(new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/tiles"/*ImgTiler.class.getClassLoader()
                .getResource("RenderData.TIFF").getPath().replace("RenderData.TIFF", "")*/));
//        imageTiler.setTileScale(10.0);
        imageTiler.tile();
    }

    public GridCoverage2D getImage(String pathToImage, int numOfTiles) throws FactoryException, TransformException, IOException {
        // TODO: crop or tile

//        GeneralEnvelope transform = CRS.transform(new Envelope2D(CRS.decode("EPSG:32637"), bl[0], tr[1],
//                        bl[0] - tr[0],
//                        tr[1] - bl[1]), // TODO: переделать в массивы double
//                CRS.decode("EPSG:4326"));

        final GeoTiffFormat format = new GeoTiffFormat();
        return format.getReader(new File(pathToImage)).read(null);

        // getting a reader
//        GridCoverageReader reader = format.getReader(new File(pathToImage));
//        GridCoverage2D source = (GridCoverage2D) reader.read(null);
//        final CoverageProcessor processor = new CoverageProcessor();
//        final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
//        param.parameter("Source").setValue(source);
//        param.parameter("Envelope").setValue(transform);
//
//        GridCoverage2D coverage2D = (GridCoverage2D) processor.doOperation(param);
//        ImageIO.write(coverage2D.getRenderedImage(), "png",
//                new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/crop.png"));

//        final CoverageProcessor proc = new CoverageProcessor(null);
//
//        imageTiler.setInputFile(new File(pathToImage));
////        imageTiler.setInputFile(new File(ImgTiler.class.getClassLoader()
////                .getResource("RenderData.TIFF").getPath()));
//        imageTiler.setNumberOfHorizontalTiles(numOfTiles);
//        imageTiler.setNumberOfVerticalTiles(numOfTiles);
//        imageTiler.setOutputDirectory(new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/tiles"/*ImgTiler.class.getClassLoader()
//                .getResource("RenderData.TIFF").getPath().replace("RenderData.TIFF", "")*/));
////        imageTiler.setTileScale(10.0);
//        imageTiler.tile();
////        imageTiler.tileImage(ImageIO.read(new File(pathToImage)), Sector.boundingSector(tr, bl), null);

//        return null;
    }

    public GridCoverage2D getMosaic(Config config, String tr, String bl, int TILES_NUM, String resultFileName) throws IOException {
        Integer[] trNums = config.getTileNumberForCoords(tr, TILES_NUM);
        Integer[] blNums = config.getTileNumberForCoords(bl, TILES_NUM);
        String pathToDir = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/tiles/";
        ArrayList<String> pathToTiles = new ArrayList<>();
//                    pathToDir + blNums[0] + "_" + blNums[1] + ".tiff",
//                    pathToDir + trNums[0] + "_" + trNums[1] + ".tiff"
//            };

        for (int i=0 ; i <= trNums[0] -  blNums[0]; i++) {
            for (int j=0 ; j <= blNums[1] - trNums[1]; j++)
                pathToTiles.add(pathToDir + String.valueOf(blNums[0]+ i) + "_" +
                        String.valueOf(blNums[1] + j) + ".tiff");
//                pathToTiles.add(pathToDir + String.valueOf(blNums[0] + i) + "_" +
//                        String.valueOf(blNums[1]) + ".tiff");
        }


        return bandTiles(pathToTiles, resultFileName);
    }

    public GridCoverage2D bandTiles(ArrayList<String> pathToTiles, String resultFileName) throws IOException {
        ArrayList<GridCoverage2D> coverages = new ArrayList<>();
        final GeoTiffFormat format = new GeoTiffFormat();

        for (String pathToTile : pathToTiles) {
            GridCoverageReader reader = format.getReader(new File(pathToTile));
            GridCoverage2D source = (GridCoverage2D) reader.read(null);
            coverages.add(source);
        }
//        imageTiler.setTileScale(10.0);
        GridCoverage2D result = imageTiler.doMosaic(coverages);
        saveToFile(format, result, resultFileName);
        return result;

    }

    private void saveToFile(GeoTiffFormat format, GridCoverage2D image, String resultFileName) throws IOException {
        ImageIO.write(image.getRenderedImage(), "TIFF",
        new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/" + resultFileName));
//        format.getWriter(new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/"
//                + resultFileName)).write(image, null);
    }


}
