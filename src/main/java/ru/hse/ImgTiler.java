package ru.hse;

import au.gov.ga.worldwind.tiler.gdal.GDALException;
import com.sun.imageio.plugins.png.PNGImageReader;
import com.sun.imageio.plugins.png.PNGImageReaderSpi;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import com.sun.org.apache.bcel.internal.generic.LALOAD;
import gov.nasa.worldwind.data.TiledElevationProducer;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwind.geom.LatLon;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.coverage.processing.operation.Resample;
import org.geotools.factory.Hints;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.processing.Operation;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import ru.hse.util.ImageTiler;
import ru.hse.util.Processor;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.media.jai.*;
import java.awt.*;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.*;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class ImgTiler {
    private static CoverageProcessor processor;

    public static void main(String[] args) throws IOException, GDALException {
        final CoverageProcessor proc = new CoverageProcessor(null);
        for (Operation o : proc.getOperations()) {
            System.out.println(o.getName());
            System.out.println(o.getDescription());
            System.out.println();
        }
        ImageTiler imageTiler = new ImageTiler();
        imageTiler.setInputFile(new File(ImgTiler.class.getClassLoader()
                .getResource("RenderData.tiff").getPath()));
        imageTiler.setNumberOfHorizontalTiles(10);
        imageTiler.setNumberOfVerticalTiles(10);
        imageTiler.setOutputDirectory(new File(ImgTiler.class.getClassLoader()
                .getResource("RenderData.tiff").getPath().replace("RenderData.tiff", "")));
//        imageTiler.setTileScale(10.0);
        imageTiler.tile();

        checkMosaic(imageTiler);

//        try {
//            new Processor().getImage(LatLon.fromDegrees(45.75, 75.65),
//                    LatLon.fromDegrees(35.53, 65.76),
//                    ImgTiler.class.getClassLoader().getResource("RenderData.tiff").getPath());
//            processor = CoverageProcessor.getInstance(null);
//            testJAI();
//
//
////            resizeUsingJavaAlgo("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.png",
////                    100, 100);
//        } catch (IOException | TransformException | FactoryException e) {
//            e.printStackTrace();
//        }
//        TiledImageProducer producer = new TiledImageProducer();
////        producer.acceptsDataSource(new FileInputStream(""));
//        TiledElevationProducer elevationProducer = new TiledElevationProducer();
//        Tiler.tileImages(GDALUtil.open(
//                new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.png")),
//        true, true, new Sector(0, 30, 0, 30),
//                new LatLon(0,0),
//                0, 100, 0, "png", false);

    }

    private static void checkMosaic(ImageTiler imageTiler) throws IOException {
        AbstractGridFormat format = GridFormatFinder.findFormat(new File(
                "/Users/victoria/IdeaProjects/mvcexample/target/classes/2_4.tiff"));
        Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        GridCoverage2DReader gridReader = format.getReader(
                new File("/Users/victoria/IdeaProjects/mvcexample/target/classes/2_4.tiff"),
                hints);
        GridCoverage2D gridCoverage1 = gridReader.read(null);
        gridReader = format.getReader(
                new File("/Users/victoria/IdeaProjects/mvcexample/target/classes/3_4.tiff"),
                hints);
        GridCoverage2D gridCoverage2 = gridReader.read(null);

        GridCoverage2D mosaic = imageTiler.doMosaic(Arrays.asList(gridCoverage1, gridCoverage2));

        File result = new File("/Users/victoria/IdeaProjects/mvcexample/target/classes/mosaic.tiff");
        format.getWriter(result).write(mosaic, null);
    }


    static void getTiles(String filepath, RenderedImage image) throws IOException {
        String path = "/Users/victoria/IdeaProjects/mvcexample/src/main/resources/";
        TiledImage tiledImage = new TiledImage(image, image.getWidth(), image.getHeight());
        TiledImage subImage = tiledImage.getSubImage(0, 0, tiledImage.getWidth() / 4, tiledImage.getHeight() / 4);
        RenderedOp saveTiled = JAI.create("filestore", tiledImage, path + "tiled.png", "png");
        RenderedOp saveSubTiled = JAI.create("filestore", tiledImage, path + "subtiled.png", "png");

        ImageReader reader = new TIFFImageReader(new TIFFImageReaderSpi());
        reader.setInput(ImageIO.createImageInputStream(new File(filepath)));
//        Raster tile = image.getTile(image.getMinTileX(), image.getMinTileY());
//        ColorModel colorModel = image.getColorModel();
//        BufferedImage bufferedImage = new BufferedImage(colorModel, tile.createCompatibleWritableRaster(),
//                colorModel.isAlphaPremultiplied(), null);
        int cols = (int) Math.ceil(reader.getWidth(0) / (double) reader.getTileWidth(0));
        int rows = (int) Math.ceil(reader.getHeight(0) / (double) reader.getTileHeight(0));

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                BufferedImage tile = reader.readTile(0, col, row);
                ImageIO.write(tile, "png", new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/tile" + col + row + 1 + ".png"));
            }
        }
    }

    static void testJAI() throws IOException, FactoryException {
        File world = new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/RenderData.tiff"); // TODO change absolute path

        RenderedImage image = ImageIO.read(world);
        getTiles(world.getAbsolutePath(), image);
        double ratio = (double) image.getHeight() / (double) image.getWidth();

        final CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326", true);
        Envelope2D envelope = new Envelope2D(wgs84, -180, -90, 360, 180);
        GridCoverage2D gcFullWorld = new GridCoverageFactory().create("world", image, envelope);

        GridCoverage scale = Operations.DEFAULT.scale(gcFullWorld, 5, 5, 0, 0,
                Interpolation.getInstance(Interpolation.INTERP_BICUBIC));

//        GridCoverage2D scaleGC2D = (GridCoverage2D)Operations.DEFAULT.scale(gcFullWorld, 10, 10, 10, 10,
//                Interpolation.getInstance(Interpolation.INTERP_BICUBIC));


//        // crop, we cannot reproject it fully to the google projection
//        final Envelope2D cropEnvelope = new Envelope2D(wgs84, /*-180, -80,*/0, 0, 30, 15); //360, 160);
//        GridCoverage2D gcCropWorld = (GridCoverage2D) Operations.DEFAULT.crop(gcFullWorld, cropEnvelope);
//
//        // resample
//        Hints.putSystemDefault(Hints.RESAMPLE_TOLERANCE, 0d);
//        GridCoverage2D gcResampled = (GridCoverage2D) Operations.DEFAULT.resample(gcCropWorld, wgs84,// CRS.decode("EPSG:3857"),
//                null, Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
//
//        File resampled = new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/resampled.png");
//        File cropped = new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/cropped.png");
//
//        ImageIO.write(gcResampled.getRenderedImage(), "png", resampled);
//        ImageIO.write(gcCropWorld.getRenderedImage(), "png", cropped);
        ImageIO.write(scale.getRenderedImage(), "png", new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/scale.png"));
//        ImageIO.write(scaleGC2D.getRenderedImage(), "png", new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/scaleGC2D.png"));
//        affine(gcFullWorld, Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
//        resizeUsingJavaAlgo("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/cropped.png",
//                image.getWidth(), image.getHeight());

    }

    public void testFlipTranslated() throws Exception {
        // build a translated image
        SampleModel sm = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, 256, 256, 3);
        ColorModel cm = PlanarImage.createColorModel(sm);
        TiledImage ti = new TiledImage(-10, -10, 5, 5, 0, 0, sm, cm);
        Graphics2D g = ti.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(-10, -10, 5, 5);
        g.dispose();

        // build a coverage around it
        CoordinateReferenceSystem wgs84LatLon = CRS.decode("EPSG:4326");
        final GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null);
        GridCoverage2D coverage = factory.create("translated", ti, new Envelope2D(wgs84LatLon, 3, 5, 6, 8));

        // verify we're good
        int[] pixel = new int[3];
        coverage.evaluate((DirectPosition) new DirectPosition2D(4, 6), pixel);

        // now reproject flipping the axis
        CoordinateReferenceSystem wgs84LonLat = CRS.decode("EPSG:4326", true);
        GridGeometry gg = new GridGeometry2D(new GridEnvelope2D(-10, -10, 5, 5), (Envelope) new Envelope2D(wgs84LonLat, 5, 3, 8, 6));
        GridCoverage2D flipped = (GridCoverage2D) Operations.DEFAULT.resample(coverage, wgs84LonLat,
                gg, Interpolation.getInstance(Interpolation.INTERP_NEAREST));

        // before the fix the pixel would have been black
        flipped.evaluate((DirectPosition) new DirectPosition2D(6, 4), pixel);
    }

    static void test() {
        Resample resample = new Resample();

    }

    private void doTranslation(GridCoverage2D grid) throws NoninvertibleTransformException {
        final int transX = -253;
        final int transY = -456;
        final double scaleX = 0.04;
        final double scaleY = -0.04;
        final ParameterBlock block = new ParameterBlock().
                addSource(grid.getRenderedImage()).
                add((float) transX).
                add((float) transY);
        RenderedImage image = JAI.create("Translate", block);

        /*
         * Create a grid coverage from the translated image but with the same envelope.
         * Consequently, the 'gridToCoordinateSystem' should be translated by the same
         * amount, with the opposite sign.
         */
        AffineTransform expected = null;//getAffineTransform(grid);
        expected = new AffineTransform(expected); // Get a mutable instance.
        final GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null);
        grid = factory.create("Translated", image, grid.getEnvelope(), grid.getSampleDimensions(), new GridCoverage2D[]{grid}, grid.getProperties());
        expected.translate(-transX, -transY);

        /*
         * Apply the "Resample" operation with a specific 'gridToCoordinateSystem' transform.
         * The envelope is left unchanged. The "Resample" operation should compute automatically
         * new image bounds.
         */
        final AffineTransform at = AffineTransform.getScaleInstance(scaleX, scaleY);
        final MathTransform tr = ProjectiveTransform.create(at);
        //account for the half pixel correction between the two spaces since we are talking raster here but the resample will talk model!
        final MathTransform correctedTransform = PixelTranslation.translate(tr, PixelInCell.CELL_CORNER, PixelInCell.CELL_CENTER);
        final GridGeometry2D geometry = new GridGeometry2D(null, correctedTransform, null);
        final GridCoverage2D newGrid = (GridCoverage2D) Operations.DEFAULT.resample(grid, grid.getCoordinateReferenceSystem(), geometry, null);
        image = newGrid.getRenderedImage();
        expected.preConcatenate(at.createInverse());
        final Point point = new Point(transX, transY);
    }

    private static void affine(final GridCoverage2D coverage, final Interpolation interp) throws IOException, FactoryException {
        // Caching initial properties.
        final RenderedImage originalImage = coverage.getRenderedImage();
        final int w = originalImage.getWidth();
        final int h = originalImage.getHeight();

        // Getting parameters for doing a scale.
        final ParameterValueGroup param = processor.getOperation("Affine").getParameters();
        param.parameter("Source").setValue(coverage);
//        param.parameter("transform").setValue(new AffineTransform(0.5, 0.0, 0.0, 0.5, 0.0, 0.0));
        param.parameter("transform").setValue(new AffineTransform(7.0, 0.0, 0.0, 7.0, 5.0, 5.0));
        param.parameter("Interpolation").setValue(interp);

        // Doing a first scale.
        GridCoverage2D scaled = (GridCoverage2D) processor.doOperation(param);
        RenderedImage scaledImage = scaled.getRenderedImage();
        ImageIO.write(scaledImage, "png", new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/scaled.png"));

        Operation resample = processor.getOperation("resample");
        ParameterValueGroup parameters = resample.getParameters();
        param.parameter("Source").setValue(coverage);
//        param.parameter("InterpolationType").setValue("");
//        param.parameter("CoordinateReferenceSystem").setValue(CRS.decode("EPSG:4326"));
//        param.parameter("BackgroundValues").setValue(new double[]{});

        scaled = (GridCoverage2D) processor.doOperation(param);
        RenderedImage scaledImage2 = scaled.getRenderedImage();
        ImageIO.write(scaledImage2, "png", new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/scaled2.png"));


    }

    public static void resizeUsingJavaAlgo(String source, int width, int height) throws IOException {
        BufferedImage sourceImage = ImageIO.read(new FileInputStream(source));
        double ratio = (double) sourceImage.getWidth() / sourceImage.getHeight();
        if (width < 1) {
            width = (int) (height * ratio + 0.4);
        } else if (height < 1) {
            height = (int) (width / ratio + 0.4);
        }

        Image scaled = sourceImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bufferedScaled = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedScaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(scaled, 0, 0, width, height, null);

        ImageIO.write(bufferedScaled, "png", new File("/Users/victoria/IdeaProjects/mvcexample/src/main/resources/bufferedScaled.png"));
    }
}
