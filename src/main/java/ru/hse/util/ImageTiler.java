package ru.hse.util;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.coverage.processing.operation.Mosaic;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.resources.Arguments;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple tiling of a coverage based simply on the number vertical/horizontal tiles desired and
 * subdividing the geographic envelope. Uses coverage processing operations.
 */
@Component
public class ImageTiler {

    private final int NUM_HORIZONTAL_TILES = 16;
    private final int NUM_VERTICAL_TILES = 8;

    private Integer numberOfHorizontalTiles = NUM_HORIZONTAL_TILES;
    private Integer numberOfVerticalTiles = NUM_VERTICAL_TILES;
    private Double tileScale;
    private File inputFile;
    private File outputDirectory;


    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public Integer getNumberOfHorizontalTiles() {
        return numberOfHorizontalTiles;
    }

    public void setNumberOfHorizontalTiles(Integer numberOfHorizontalTiles) {
        this.numberOfHorizontalTiles = numberOfHorizontalTiles;
    }

    public Integer getNumberOfVerticalTiles() {
        return numberOfVerticalTiles;
    }

    public void setNumberOfVerticalTiles(Integer numberOfVerticalTiles) {
        this.numberOfVerticalTiles = numberOfVerticalTiles;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Double getTileScale() {
        return tileScale;
    }

    public void setTileScale(Double tileScale) {
        this.tileScale = tileScale;
    }

     public void tile() throws IOException {
//        AbstractGridFormat format = GridFormatFinder.findFormat(this.getInputFile());
         final GeoTiffFormat format = new GeoTiffFormat();
         String fileExtension = this.getFileExtension(this.getInputFile());

        //working around a bug/quirk in geotiff loading via format.getReader which doesn't set this
        //correctly
        Hints hints = null;
        if (format instanceof GeoTiffFormat) {
            hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,Boolean.TRUE);
        }

//        GridCoverage2DReader gridReader = format.getReader(
//                this.getInputFile(),
//                hints);
         GridCoverage2DReader gridReader = format.getReader(inputFile);
        GridCoverage2D gridCoverage = gridReader.read(null);
        Envelope2D coverageEnvelope = gridCoverage.getEnvelope2D();
        double coverageMinX = coverageEnvelope.getBounds().getMinX();
        double coverageMaxX = coverageEnvelope.getBounds().getMaxX();
        double coverageMinY = coverageEnvelope.getBounds().getMinY();
        double coverageMaxY = coverageEnvelope.getBounds().getMaxY();

        int htc = this.getNumberOfHorizontalTiles() != null
                ? this.getNumberOfHorizontalTiles() : NUM_HORIZONTAL_TILES;
        int vtc = this.getNumberOfVerticalTiles() != null
                ? this.getNumberOfVerticalTiles() : NUM_VERTICAL_TILES;

        double geographicTileWidth = (coverageMaxX - coverageMinX) / (double)htc;
        double geographicTileHeight = (coverageMaxY - coverageMinY) / (double)vtc;

        CoordinateReferenceSystem targetCRS = gridCoverage.getCoordinateReferenceSystem();

        //make sure to create our output directory if it doesn't already exist
        File tileDirectory = this.getOutputDirectory();
        if (!tileDirectory.exists()) {
            tileDirectory.mkdirs();
        }

        //iterate over our tile counts
        for (int i = 0; i < htc; i++) {
            for (int j = 0; j < vtc; j++) {

                System.out.println("Processing tile at indices i: " + i + " and j: " + j);
                //create the envelope of the tile
                Envelope envelope = getTileEnvelope(coverageMinX, coverageMinY, geographicTileWidth,
                        geographicTileHeight, targetCRS, i, j);

                GridCoverage2D finalCoverage = cropCoverage(gridCoverage, envelope);

                if (this.getTileScale() != null) {
                    finalCoverage = scaleCoverage(finalCoverage);
                }

                //use the AbstractGridFormat's writer to write out the tile
                File tileFile = new File(tileDirectory, i + "_" + j + "." + fileExtension);
                format.getWriter(tileFile).write(finalCoverage, null);
            }
        }

    }

    private Envelope getTileEnvelope(double coverageMinX, double coverageMinY,
                                     double geographicTileWidth, double geographicTileHeight,
                                     CoordinateReferenceSystem targetCRS, int horizontalIndex, int verticalIndex) {

        double envelopeStartX = (horizontalIndex * geographicTileWidth) + coverageMinX;
        double envelopeEndX = envelopeStartX + geographicTileWidth;
        double envelopeStartY = (verticalIndex * geographicTileHeight) + coverageMinY;
        double envelopeEndY = envelopeStartY + geographicTileHeight;

        return new ReferencedEnvelope(
                envelopeStartX, envelopeEndX, envelopeStartY, envelopeEndY, targetCRS);
    }

    private GridCoverage2D cropCoverage(GridCoverage2D gridCoverage, Envelope envelope) {
        CoverageProcessor processor =  CoverageProcessor.getInstance();

        //An example of manually creating the operation and parameters we want
        final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
        param.parameter("Source").setValue(gridCoverage);
        param.parameter("Envelope").setValue(envelope);

        return (GridCoverage2D) processor.doOperation(param);
    }

    public GridCoverage2D doMosaic(List<GridCoverage2D> sources) throws IOException {
        CoverageProcessor processor = CoverageProcessor.getInstance(GeoTools
                .getDefaultHints());
        ParameterValueGroup param = processor.getOperation("Mosaic").getParameters();

        // Creation of a List of the input Sources
//        List<GridCoverage2D> sources = new ArrayList<GridCoverage2D>(2);
//        sources.add(gridCoverage1);
//        sources.add(gridCoverage2);
        // Setting of the sources
        param.parameter("Sources").setValue(sources);
        int TILE_SIZE = 10;

        ImageLayout il = new ImageLayout();
        Hints hints = new Hints();
        hints.put(JAI.KEY_IMAGE_LAYOUT, il);
//        il.setTileHeight(TILE_SIZE);
//        il.setTileWidth(TILE_SIZE);

        // Mosaic operation
        GridCoverage2D mosaic = (GridCoverage2D) processor.doOperation(param, hints);

        if (tileScale != null && tileScale != 0)
            mosaic = scaleCoverage(mosaic);
        return mosaic;
//        Mosaic op = new Mosaic();
//        ParameterValueGroup parameters = op.getParameters();
//        parameters.parameter("Source").setValue(gridCoverage1);
//        parameters.parameter("Source").setValue(gridCoverage2);
//        return (GridCoverage2D) op.doOperation(parameters, new Hints(Hints.KEY_INTERPOLATION, Hints.VALUE_INTERPOLATION_BICUBIC));
    }

    private GridCoverage2D scaleCoverage(GridCoverage2D coverage) {
        Operations ops = new Operations(null);
        coverage = (GridCoverage2D) ops.scale(
                coverage, this.getTileScale(), this.getTileScale(), 0, 0);
//        coverage = (GridCoverage2D) ops.resample(
//                coverage, coverage.getEnvelope(), Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
        return coverage;
    }
}