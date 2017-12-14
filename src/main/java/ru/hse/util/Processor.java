package ru.hse.util;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.ImageTiler;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.BoundingBox;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.ImgTiler;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@Service
public class Processor {
    @Autowired
    ru.hse.util.ImageTiler imageTiler;

    public RenderedImage getImage(LatLon tr, LatLon bl, String pathToImage) throws FactoryException, TransformException, IOException {
        // TODO: crop or tile

        GeneralEnvelope transform = CRS.transform(new Envelope2D(CRS.decode("EPSG:32637"), bl.latitude.degrees, tr.longitude.degrees,
                        bl.latitude.degrees - tr.latitude.degrees,
                        tr.longitude.degrees - bl.longitude.degrees), // TODO: переделать в массивы double
                CRS.decode("EPSG:4326"));

        final GeoTiffFormat format = new GeoTiffFormat();

        // getting a reader
        GridCoverageReader reader = format.getReader(new File(pathToImage));
        GridCoverage2D source = (GridCoverage2D) reader.read(null);
        final CoverageProcessor processor = new CoverageProcessor();
        final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
        param.parameter("Source").setValue(source);
        param.parameter("Envelope").setValue(transform);

        GridCoverage2D coverage2D = (GridCoverage2D) processor.doOperation(param);
        ImageIO.write(coverage2D.getRenderedImage(), "png",
                new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/crop.png"));

        ImageTiler imageTiler = new ImageTiler();
        imageTiler.tileImage(ImageIO.read(new File(pathToImage)), Sector.boundingSector(tr, bl), null);

        return null;
    }

    public RenderedImage getImage(double[] tr, double[] bl, String pathToImage, int numOfTiles) throws FactoryException, TransformException, IOException {
        // TODO: crop or tile

        GeneralEnvelope transform = CRS.transform(new Envelope2D(CRS.decode("EPSG:32637"), bl[0], tr[1],
                        bl[0] - tr[0],
                        tr[1] - bl[1]), // TODO: переделать в массивы double
                CRS.decode("EPSG:4326"));

        final GeoTiffFormat format = new GeoTiffFormat();

        // getting a reader
        GridCoverageReader reader = format.getReader(new File(pathToImage));
        GridCoverage2D source = (GridCoverage2D) reader.read(null);
        final CoverageProcessor processor = new CoverageProcessor();
        final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
        param.parameter("Source").setValue(source);
        param.parameter("Envelope").setValue(transform);

        GridCoverage2D coverage2D = (GridCoverage2D) processor.doOperation(param);
        ImageIO.write(coverage2D.getRenderedImage(), "png",
                new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/crop.png"));

        final CoverageProcessor proc = new CoverageProcessor(null);

        imageTiler.setInputFile(new File(ImgTiler.class.getClassLoader()
                .getResource("RenderData.tiff").getPath()));
        imageTiler.setNumberOfHorizontalTiles(numOfTiles);
        imageTiler.setNumberOfVerticalTiles(numOfTiles);
        imageTiler.setOutputDirectory(new File(ImgTiler.class.getClassLoader()
                .getResource("RenderData.tiff").getPath().replace("RenderData.tiff", "")));
//        imageTiler.setTileScale(10.0);
        imageTiler.tile();
//        imageTiler.tileImage(ImageIO.read(new File(pathToImage)), Sector.boundingSector(tr, bl), null);

        return null;
    }

    public void bandTiles(String[] pathToTiles) throws IOException {
        ArrayList<GridCoverage2D> coverages = new ArrayList<>();
        final GeoTiffFormat format = new GeoTiffFormat();

        for (String pathToTile : pathToTiles) {
            GridCoverageReader reader = format.getReader(new File(pathToTile));
            GridCoverage2D source = (GridCoverage2D) reader.read(null);
            coverages.add(source);
        }
        imageTiler.setTileScale(10.0);
        GridCoverage2D result = imageTiler.doMosaic(coverages);
        saveToFile(result);
    }

    private void saveToFile(GridCoverage2D image) throws IOException {
        ImageIO.write(image.getRenderedImage(), "tiff",
                new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/result.tiff"));
    }


}
