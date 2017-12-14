package ru.hse.util;

public class PolySamp {


    /** This image to be resampled */
    protected double[] original;

    /** X-dimension of image */
    protected int mx;

    /** Y-dimension of image */
    protected int my;

    /** Is this an intensive image */
    protected boolean intensive;

    /** Drizzle offset */
    protected double drizzOffset;

    /** Drizzle Area */
    protected double drizzArea;


    /** Initialize a PolySamp object with the image to be sampled.
     *
     *  @param original	The original image[m x n]
     *  @param mx	The x-dimension of the image
     *  @param my        The y-dimension of the image
     */
    public PolySamp(double[] original, int mx, int my) {
        this (original, mx, my, 1, false);
    }

    /** Initialize a PolySamp object with the image to be sampled.
     *
     *  @param original	The original image[m x n]
     *  @param mx	The x-dimension of the image
     *  @param my        The y-dimension of the image
     *  @param drizzle  The drizzle factor should range from >0 to 1 and
     *                  indicates the length of the side of the pixel
     *                  inside the original image pixel in which the
     *                  flux is assumed to be contained.
     *  @param intensive	Indicates whether the image
     *                          has a dimensionality like temperature
     *                          such that a pixel value is
     *                          independent of the size of the pixel.
     */
    public PolySamp(double[] original, int mx, int my,
                    double drizzle, boolean intensive) {

        this.original = original;
        this.mx       = mx;
        this.my       = my;
        setDrizzle(drizzle);
        setIntensive(intensive);
    }

    /** Reset the image to be sampled
     *
     *  @param original	The original image[m x n]
     *  @param mx	The x-dimension of the image
     *  @param my        The y-dimension of the image
     */
    public void setImage(double[] original, int mx, int my) {
        this.original = original;
        this.mx       = mx;
        this.my       = my;
    }

    /** Reset the image to be sampled
     *
     *  @param original	The original image[m x n]
     *  @param mx	The x-dimension of the image
     *  @param my       The y-dimension of the image
     *  @param drizzle  The drizzle factor should range from >0 to 1 and
     *                  indicates the length of the side of the pixel
     *                  inside the original image pixel in which the
     *                  flux is assumed to be contained.
     *  @param intensive	Indicates whether the image
     *                          has a dimensionality like temperature
     *                          independent of the size of the pixel.
     */
    public void setImage(double[] original, int mx, int my, double drizzle,
                         boolean intensive) {
        setImage(original, mx, my);
        setDrizzle(drizzle);
        setIntensive(intensive);
    }

    /** Set the drizzle factor for sampling
     *  @param drizzle The drizzle factor should range from >0 to 1 and
     *                 indicates the length of the side of the pixel
     *                 inside the original image pixel in which the
     *                 flux is assumed to be contained.
     */

    public void setDrizzle(double drizzle) {

        if (drizzle <= 0) {
            drizzle = .01;
        }
        if (drizzle > 1) {
            drizzle = 1;
        }

        drizzArea = drizzle*drizzle;
        drizzOffset = (1-drizzle)/2;
    }

    /** Indicate whether the image is intensive.
     *  If intensive is set to true, then the drizzle is set to 1.
     *  @param intensive	Indicates whether the image
     *                          has a dimensionality like temperature
     *                          independent of the size of the pixel.
     *.
     */
    public void setIntensive(boolean intensive) {
        this.intensive = intensive;
        if (intensive) {
            setDrizzle(1);
        }
    }


    /** Calculate the area of a convex polygon.
     * This function calculates the area of a convex polygon
     * by deconvolving the polygon into triangles and summing
     * the areas of the consituents.  The user provides the
     * coordinates of the vertices of the polygon in sequence
     * along the circumference (in either direction and starting
     * at any point).
     *
     * Only distinct vertices should be given, i.e., the
     * first vertex should not be repeated at the end of the list.
     *
     * @param	n	The number of vertices in the polygon.
     * @param   x	The x coordinates of the vertices
     * @param   y	The y coordinates of teh vertices
     * @return		The area of the polygon.
     */
    public static double convexArea(int n, double[] x, double[] y) {


        double area = 0;

        for(int i=1; i<n-1; i += 1) {

            area += triangleArea(x[0],y[0], x[i], y[i], x[i+1], y[i+1]);
        }

        return area;
    }

    /** Calculate the area of an arbitrary triangle.
     *  Use the vector formula
     *     A = 1/2 sqrt(X^2 Y^2 - (X-Y)^2)
     *  where X and Y are vectors describing two sides
     *  of the triangle.
     *
     *  @param x0	x-coordinate of first vertex
     *  @param y0       y-coordinate of first vertex
     *  @param x1       x-coordinate of second vertex
     *  @param y1       y-coordinate of second vertex
     *  @param x2       x-coordinate of third vertex
     *  @param y2       y-coordinate of third vertex
     *
     *  @return         Area of the triangle.
     */

    public static double triangleArea(double x0, double y0,
                                      double x1, double y1,
                                      double x2, double y2) {

        // Convert vertices to vectors.
        double a = x0-x1;
        double b = y0-y1;
        double e = x0-x2;
        double f = y0-y2;

        double area=  (a*a+b*b)*(e*e+f*f) - (a*e+b*f)*(a*e+b*f);
        if (area <= 0) {
            return 0; // Roundoff presumably!
        } else {
            return Math.sqrt(area)/2;
        }
    }


    /** Clip a polygon to a half-plane bounded by a vertical line.
     *  Users can flip the input axis order to clip by a horizontal line.
     *
     *  This function uses pre-allocated arrays for
     *  output so that no new objects are generated
     *  during a call.
     *
     *  @param 	n	Number of vertices in the polygon
     *  @param  x	X coordinates of the vertices
     *  @param  y	Y coordinates of the vertices
     *  @param  nx	New X coordinates
     *  @param  ny      New Y coordinates
     *  @param  val	Value at which clipping is to occur.
     *  @param  dir     Direction for which data is to be
     *                  clipped.  true-> clip below val, false->clip above val.
     *
     *  @return         The number of new vertices.
     *
     */
    public static int lineClip(int n,
                               double[] x, double[] y,
                               double[] nx, double[] ny,
                               double val, boolean dir) {

        int	nout=0;

        // Need to handle first segment specially
        // since we don't want to duplicate vertices.
        boolean last = inPlane(x[n-1], val, dir);

        for (int i=0; i < n; i += 1) {

            if (last) {

                if (inPlane(x[i], val, dir)) {
                    // Both endpoints in, just add the new point
                    nx[nout] = x[i];
                    ny[nout] = y[i];
                    nout    += 1;
                } else {
                    double ycross;
                    // Moved out of the clip region, add the point we moved out
                    if (i == 0) {
                        ycross = y[n-1] + (y[0]-y[n-1])*(val-x[n-1])/(x[0]-x[n-1]);
                    } else {
                        ycross = y[i-1] + (y[i]-y[i-1])*(val-x[i-1])/(x[i]-x[i-1]);
                    }
                    nx[nout] = val;
                    ny[nout] = ycross;
                    nout    += 1;
                    last     = false;
                }

            } else {

                if (inPlane(x[i], val, dir)) {
                    // Moved into the clip region.  Add the point
                    // we moved in, and the end point.
                    double ycross;
                    if (i == 0) {
                        ycross = y[n-1] + (y[0]-y[n-1])*(val-x[n-1])/(x[i]-x[n-1]);
                    } else {
                        ycross = y[i-1] + (y[i]-y[i-1])*(val-x[i-1])/(x[i]-x[i-1]);
                    }
                    nx[nout]  = val;
                    ny[nout] = ycross;
                    nout += 1;

                    nx[nout] = x[i];
                    ny[nout] = y[i];
                    nout += 1;
                    last     = true;

                } else {
                    // Segment entirely clipped.
                }
            }
        }
        return nout;
    }

    /**
     * Is the test value on the on the proper side of a line.
     *
     * @param test	Value to be tested
     * @param divider	Critical value
     * @param direction True if values greater than divider are 'in'
     *                  False if smaller values are 'in'.
     * @return          Is the value on the right side of the divider?
     */
    public static boolean inPlane(double test, double divider, boolean direction) {

        // Note that since we always include
        // points on the dividing line as 'in'.  Not sure
        // if this is important though...

        if (direction) {
            return test >= divider;
        } else {
            return test <= divider;
        }
    }

    // Intermediate storage used by rectClip.
    // The maximum number of vertices we will get if we start with
    // a convex quadrilateral is 12, but we use larger
    // arrays in case this routine is used is some other context.
    // If we were good we'd be checking this when we add points in
    // the clipping process.

    private double[] rcX0 = new double[100];
    private double[] rcX1 = new double[100];
    private double[] rcY0 = new double[100];
    private double[] rcY1 = new double[100];

    /** Clip a polygon by a non-rotated rectangle.
     *
     *  This uses a simplified version of the Sutherland-Hodgeman polygon
     *  clipping method.  We assume that the region to be clipped is
     *  convex.  This implies that we will not need to worry about
     *  the clipping breaking the input region into multiple
     *  disconnected areas.
     *    [Proof: Suppose the resulting region is not convex.  Then
     *     there is a line between two points in the region that
     *     crosses the boundary of the clipped region.  However the
     *     clipped boundaries are all lines from one of the two
     *     figures we are intersecting.  This would imply that
     *     this line crosses one of the boundaries in the original
     *     image.  Hence either the original polygon or the clipping
     *     region would need to be non-convex.]
     *
     *  Private arrays are used for intermediate results to minimize
     *  allocation costs.
     *
     *  @param n	Number of vertices in the polygon.
     *  @param x	X values of vertices
     *  @param y        Y values of vertices
     *  @param nx	X values of clipped polygon
     *  @param ny       Y values of clipped polygon
     *
     *  @param          minX Minimum X-value
     *  @param		minY Minimum Y-value
     *  @param          maxX MAximum X-value
     *  @param          maxY Maximum Y-value
     *
     *  @return		Number of vertices in clipped polygon.
     */
    public int rectClip(int n, double[] x, double[] y, double[] nx, double[] ny,
                        double minX, double minY, double maxX, double maxY) {

        int nCurr;

        // lineClip is called four times, once for each constraint.
        // Note the inversion of order of the arguments when
        // clipping vertically.

        nCurr = lineClip(n, x, y, rcX0, rcY0, minX, true);

        if (nCurr > 0) {
            nCurr = lineClip(nCurr, rcX0, rcY0, rcX1, rcY1, maxX, false);

            if (nCurr > 0) {
                nCurr = lineClip(nCurr, rcY1, rcX1, rcY0, rcX0, minY, true);

                if (nCurr > 0) {
                    nCurr = lineClip(nCurr, rcY0, rcX0, ny, nx, maxY, false);
                }
            }
        }

        // We don't need to worry that we might not have set the output arrays.
        // If nCurr == 0, then it doesn't matter that
        // we haven't set nx and ny.  And if it is then we've gone
        // all the way and they are already set.
        return nCurr;
    }

    /** Debugging routine that prints a list of vertices.
     *  @param n The number of vertices in the polygon
     *  @param x X coordinates
     *  @param y Y coordinates
     */
    protected static void printVert(int n, double[] x,double[] y, String label) {

        for (int i=0; i<n; i += 1) {
            System.out.println(label+"   "+x[i]+"  "+y[i]);
        }
    }

    // Intermediate storage used by polySamp
    private double psX0[] = new double[12];
    private double psY0[] = new double[12];
    private double psX1[] = new double[12];
    private double psY1[] = new double[12];

    /**
     * Perform a flux conserving resampling of one image to a
     * resampled map.  In this version we are given the
     * the pixel corners as matrices of X and Y arrays.
     * <p>
     * Resampling is done by clipping each output pixel to the box
     * specified by one of the original pixels.  The area of the
     * overlap between the pixels is computed and an appropriate fraction
     * of the flux from the original pixel is added to the output pixel.
     * <p>
     * To minimize object allocation costs private arrays are
     * used for intermediate storage.
     * <p>
     * A number of the arrays used here are linearized versions
     * of two-d arrays.  The 'real' dimensions are indicated
     * in the comments.
     *
     *  @param nx	X dimension of resampled map
     *  @param ny       Y dimension of resampled map
     *  @param x	X coordinates of corners of pixels in resampled map
     *                    [(nx+1)*(ny+1)]. These coordinates are in terms
     *                    of the pixels in the original image.
     *  @param y        Y coordinates of corners of pixels in resampled map
     *                    [(nx+1)*(ny+1)]. These coordinates are in terms
     *                    of the pixels in the original image.
     *  @return         Array of resampled pixels [nx x ny].
     */
    public double[] polySamp(int nx, int ny, double[] x, double[] y){

        // Create the array for the output image.
        double[] output = new double[nx*ny];
        double[] px = new double[4];
        double[] py = new double[4];

        // Loop over the output pixels.
        for (int j=0; j<ny; j += 1) {
            for (int i=0; i<nx; i += 1) {


                // The p's need to go around the vertices of
                // the pixel sequentially as if we were traversing
                // the pixel circumference.  Either direction is fine.
                //
                // Remember that the X and Y arrays are one larger
                // than the number of pixels in each direction.

                int p0 = i  + j*(nx+1);
                int p1 = p0 + 1;
                int p2 = p1 + (nx+1);
                int p3 = p2 - 1;


                // Intialize the arrays of pixel vertex coordinates
                px[0] = x[p0];
                px[1] = x[p1];
                px[2] = x[p2];
                px[3] = x[p3];

                py[0] = y[p0];
                py[1] = y[p1];
                py[2] = y[p2];
                py[3] = y[p3];

                output[i+nx*j] = samplePixel(px, py);

                // For intensive quantities convert from sum to weighted average.
                // We just need to divide by the total area of the output pixel.
                if (intensive) {
                    double area = convexArea(4, px, py);
                    if (area > 0) {
                        output[p0] /= area;
                    } else {
                        output[p0] = 0;
                    }
                }

            } // Y Ends of loops over output pixels.
        } // X

        // We should have filled in each map pixel.
        return output;
    }

    /**
     * Perform a flux conserving resampling of one image to a
     * resampled map.  In this version we are given the
     * the pixel corners separately for each pixel.
     * <p>
     * Resampling is done by clipping each output pixel to the box
     * specified by one of the original pixels.  The area of the
     * overlap between the pixels is computed and an appropriate fraction
     * of the flux from the original pixel is added to the output pixel.
     * <p>
     *  @param n	Number of pixels.
     *  @param x	X coordinates of corners of pixels in resampled map.
     *                  Each pixel is represented by four consecutive values
     *                  in the array [4xn].
     *  @param x	Y coordinates of corners of pixels in resampled map.
     *                  Each pixel is represented by four consecutive values
     *                  in the array [4xn].
     *  @return		An array of sampled data [n].
     */

    public double[] polySamp(int n, double[] x, double[] y) {

        double[] output = new double[n];
        double[] px = new double[4];
        double[] py = new double[4];

        for (int i=0; i<n; i += 1) {
            System.arraycopy(x, i*4, px, 0, 4);
            System.arraycopy(y, i*4, py, 0, 4);

            output[i] = samplePixel(px,py);
            if (intensive) {
                double area = convexArea(4,x,y);
                if (area > 0) {
                    output[i] /= convexArea(4,x,y);
                } else {
                    output[i] = 0;
                }
            }

        }
        return output;
    }


    /** Sample a single map pixel.
     *
     *  @param x The x values of the corners of the pixel [4]
     *  @param y The y values of the corners of the pixel [4]
     *
     *  @return  The total flux in the resampled pixel.
     */
    public double samplePixel(double[] x, double[] y) {

        // Find a bounding box for the pixel coordinates.
        double minX = x[0];
        double maxX = minX;
        double minY = y[0];
        double maxY = minY;


        for (int k=1; k<4; k += 1) {

            // Note that a value can't simultaneously
            // update the minimum and maximum...
            if (x[k] < minX) {
                minX = x[k];
            } else if (x[k] > maxX) {
                maxX = x[k];
            }
            if (y[k] < minY) {
                minY = y[k];
            } else if (y[k] > maxY) {
                maxY = y[k];
            }

        }


        // Round the extrema of the pixel coordinates to
        // integer values.
        minX = Math.floor(minX);
        maxX = Math.ceil(maxX);

        minY = Math.floor(minY);
        maxY = Math.ceil(maxY);


        // Check to see if pixel is entirely off original image.
        // If so we don't need to do anything further.
        if (maxX <= 0 || minX >= mx || maxY <= 0 || minY >= my) {
            return 0;
        }

        // Check if the resampling pixel is entirely enclosed in
        // the image pixel.  Need to check this before
        // we 'clip' our bounding box.  This check
        // should significantly increase the speed
        // for oversampling images, but it will cause
        // a tiny slowdown when the sampling pixels are as large
        // or larger than the original pixels.
        //
        // We're doing equalities with
        // double values, but they are guaranteed to be
        // integers.
        if (minX == maxX-1 && minY == maxY-1) {
            double val = original[(int)minX + ((int)minY)*mx];
            return val*convexArea(4,x,y);
        }

        // Clip the bounding box to the original image dimensions
        if (maxX >= mx) {
            maxX = mx;
        }

        if (minX < 0) {
            minX = 0;
        }

        if (maxY >= my) {
            maxY = my;
        }

        if (minY < 0) {
            minY = 0;
        }


        // Loop over the potentially overlapping pixels.
        //
        double value=0;
        double tArea=0;

        for (int n=(int)minY; n<(int)maxY; n += 1) {

            // the vmin/max values are the areas in
            // which the 'flux' of a given pixel is treated
            // as being.

            double vminY = n + drizzOffset;
            double vmaxY = n + 1 - drizzOffset;

            for (int m=(int)minX; m<(int)maxX; m += 1) {

                double vminX = m + drizzOffset;
                double vmaxX = m + 1 - drizzOffset;


                // Clip the quadrilaterel given by the coordinates
                // of the resampling pixel, to this particular
                // image pixel.
                int nv = rectClip(4, x, y, psX1, psY1,
                        vminX,vminY, vmaxX,vmaxY);

                // If there is no overlap we won't get any
                // vertices back in the clipped set.
                if (nv > 0) {

                    // Calculate the area of the clipped pixel and compare
                    // it to the area in which the flux of the original
                    // pixel is found.  The returned area should
                    // never be greater than the drizzArea.
                    double area   = convexArea(nv, psX1, psY1);
                    tArea += area;
                    double factor = area/drizzArea;

                    // Add the appropriate fraction of the original
                    // flux into the output pixel.
                    value += factor*original[m+n*mx];
                }
            }
        }

        return value;
    }



    /** Do a nearest neighbor interpolation.
     *  This method is included for comparision with
     *  the samplePixel method.
     *
     *  @param x      X coordinate of center of resampling pixel
     *  @param y      Y coordiante of center of resampling pixel
     */

    public double nnSample(double x, double y) {

        x = Math.floor(x+0.5);
        y = Math.floor(y+0.5);

        if (x < 0 || x >= mx  || y < 0 || y >= my) {
            return 0;
        } else {
            return original[(int) x + ((int) y)*mx];
        }
    }


    /** Perform a bilinear interpolation of the image.
     *  This method is used for comparison with the samplePixel
     *  method.
     *
     * @param x X value of the center of the resampling pixel.
     * @param y Y value of the center of the resampling pixel.
     */

    public double bilinSample(double x, double y) {


        // The values of the pixels are assumed to be
        // at the center of the pixel.  Thus we cannot
        // interpolate past outermost half-pixel edge of teh
        // map.

        x -= 0.5;
        y -= 0.5;

        if (x < 0 || x >= mx-1 || y < 0 || y >= my-1) {
            return 0;
        }

        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        double dx = x-ix;
        double dy = y-iy;

        double interp = (1-dx)*(1-dy)* original[ix   +  mx*iy] +
                dx  *(1-dy)* original[ix+1 +  mx*iy] +
                (1-dx)*  dy  * original[ix   +  mx*(iy+1)] +
                dx  *  dy  * original[ix+1 +  mx*(iy+1)];
        return interp;
    }
}