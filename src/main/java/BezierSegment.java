import java.awt.geom.*;

/**
 * Class: BezierSegment
 * Holds data relating to a segment of a bezier curve object. Shape is controlled by 4 control points.
 * - point0 holds the starting coordinates of the segment
 * - point3 holds the ending coordinates of the segment
 * - point1, point2 hold the coordinates of the two control points.
 * Based on the Bezier Curve Tool by:
 * @Author Berin Martini
 * @Version 2012-01-18
 * @see https://imagej.nih.gov/ij/plugins/bezier-tool/index.html
 */
public class BezierSegment {
    private OptionsStore optionStore;
    private BezierSegment next = null;
    private BezierSegment previous = null;
    public BezierControlPoint point0;
    public BezierControlPoint point1;
    public BezierControlPoint point2;
    public BezierControlPoint point3;
    
    /**
     * Constructor for BezierSegment
     * @param optionStore Options Store
     * @param x0 Starting X coordinate
     * @param y0 Starting Y coordinate
     * @param x1 Control point 1 X coordinate
     * @param y1 Control point 1 X coordinate
     * @param x2 Control point 2 Y coordinate
     * @param y2 Control point 2 Y coordinate
     * @param x3 Ending coordinate X coordinate
     * @param y3 Ending coordinate Y coordinate
     */
    BezierSegment(OptionsStore optionStore, double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
        this.optionStore = optionStore;
        point0 = new BezierControlPoint(this, optionStore, BezierPointType.START_POINT, x0, y0); // Starting coordinates
        point1 = new BezierControlPoint(this, optionStore, BezierPointType.CONTROL_1, x1, y1); // Control point 1
        point2 = new BezierControlPoint(this, optionStore, BezierPointType.CONTROL_2, x2, y2); // Control point 1
        point3 = new BezierControlPoint(this, optionStore, BezierPointType.END_POINT, x3, y3); // Ending coordinates
    }
    
    /**
     * Gets the next Bezier curve segment
     * @return BezierSegment | null
     */
    public BezierSegment next() {
        return next;
    }

    /**
     * Sets the next segment
     * @param newBezier
     */
    public void setNext(BezierSegment newBezier) {
        next = newBezier;
    }
    
    /**
     * Gets the previous Bezier curve segment
     * @return BezierSegment | null
     */
    public BezierSegment previous() {
        return previous;
    }
    
    /**
     * Sets the previous segment
     * @param bezierSegment
     */
    public void setPrevious(BezierSegment bezierSegment) {
        previous = bezierSegment;
    }

    /**
     * Inserts a new segment after this current segment.
     * @param bezierSegment
     */
    public void insertAsNext(BezierSegment bezierSegment) {
        bezierSegment.setNext(next);
        bezierSegment.setPrevious(this);
        if (next != null) {
            next.setPrevious(bezierSegment);
        }
        next = bezierSegment;
    }
    
    /**
     * Inserts a new segment before this current segment.
     * @param bezierSegment
     */
    public void insertAsPrevious(BezierSegment bezierSegment) {
        bezierSegment.setNext(this);
        bezierSegment.setPrevious(previous);
        if (previous != null) {
            previous.setNext(bezierSegment);
        }
        previous = bezierSegment;
    }
    
    /**
     * Moves a given Bezier point type to the X,Y coordinate
     * @param pointType
     * @param newX
     * @param newY
     */
    public void movePoint(BezierPointType pointType, double newX, double newY) {
        if (pointType == BezierPointType.START_POINT) {
            double newP1X = point1.x - point0.x + newX;
            double newP1Y = point1.y - point0.y + newY;
            point1.movePoint(newP1X, newP1Y);
        } else if (pointType == BezierPointType.CONTROL_1) {
            if (previous != null) {
                previous.point2.movePoint((point0.x + point0.x - point1.x), (point0.y + point0.y - point1.y));
            }
        } else if (pointType == BezierPointType.CONTROL_2) {
            if (next != null) {
                next.point1.movePoint((point3.x + point3.x - point2.x), (point3.y + point3.y - point2.y));
            }
        } else if (pointType == BezierPointType.END_POINT) {
            double newP2X = point2.x - point3.x + newX;
            double newP2Y = point2.y - point3.y + newY;
            point2.movePoint(newP2X, newP2Y);
            if (next != null) {
                next.point0.setPoint(newX, newY);
            }
        }
    }
    
    /**
     * Returns an array of control point coordinates in the form double[i][x,y]. Order of the points is: START_POINT,
     * CONTROL_1, CONTROL_2, END_POINT
     * @return double[][]
     */
    public double[][] getPointCoordinates() {
        double[][] coordinates = {{point0.x, point0.y}, {point1.x, point1.y}, {point3.x, point3.y}, {point2.x, point2.y}};
        return coordinates;
    }

    /**
     * Returns true/false if the coordinate given is inside or near the point.
     * We check 2*pointWidth from the center of the control point.
     * @param testX
     * @param testY
     * @return boolean
     */
    public boolean isInsideControlPoint(double testX, double testY) {
        if(insideControlPoint(testX, testY) != null)
            return true;
        return false;
    }
    
    /**
     * Returns a bezier curve path for the segment using the Path2D.Double class and methods
     * @see https://docs.oracle.com/javase/7/docs/api/java/awt/geom/Path2D.Double.html
     * @return Path2D.Double
     */
    public Path2D.Double getCurvePath() {
        Path2D.Double curvePath = new Path2D.Double();
        curvePath.moveTo(point0.x, point0.y);
        curvePath.curveTo(point1.x, point1.y, point2.x, point2.y, point3.x, point3.y);
        return curvePath;
    }

    /**
     * Calculates and returns the X coordinate for a value of t, where t is 0.0-1.0
     * @param t
     * @return double
     */
    public double getXCoordinate(double t)
    {
        // Idiot checks...
        if(t< 0.0) t = 0.0;
        if(t > 1.0) t = 1.0;

        // The formula for a 4-point bezier curve: 
        // y = a*(1−t)^3 + b*t*3*(1−t)^2 + c*3*(1−t)*t^2 + d*t^3
        // where t = [0.0-1.0]
        // a = point0.x
        // b = point1.x
        // c = point2.x
        // d = point2.x

        return Math.pow(1.0-t, 3)*point0.x + 3*Math.pow(1.0-t, 2)*t*point1.x + 3*(1.0-t)*Math.pow(t,2)*point2.x + Math.pow(t,3)*point3.x;
    }

    /**
     * Calculates and returns the Y coordinate for a value of t, where t is 0.0-1.0
     * @param t
     * @return double
     */
    public double getYCoordinate(double t)
    {
        // Idiot checks...
        if(t< 0.0) t = 0.0;
        if(t > 1.0) t = 1.0;

        // The formula for a 4-point bezier curve: 
        // y = a*(1−t)^3 + b*t*3*(1−t)^2 + c*3*(1−t)*t^2 + d*t^3
        // where t = [0.0-1.0]
        // a = point0.y
        // b = point1.y
        // c = point2.y
        // d = point2.y
        
        return Math.pow(1.0-t, 3)*point0.y + 3*Math.pow(1.0-t, 2)*t*point1.y + 3*(1.0-t)*Math.pow(t,2)*point2.y + Math.pow(t,3)*point3.y;
    }

    /**
     * Returns a double[x,y] array of coordinates defining a bezier curve at point t.
     * @return double[]
     */
    public double[] getCurveCoordinates(double t)
    {
        double[] coords = new double[2];

        coords[0] = getXCoordinate(t);
        coords[1] = getYCoordinate(t);

        return coords;
    }

    /**
     * Returns a double[i][x,y] array of coordinates defining a bezier curve from the 4 control points
     * where number of points along the curve is defined by the optionStore.getProbeNumber() value.
     * @return double[][]
     */
    public double[][] getCurveCoordinates()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[][] coordinates = new double[probeNumber][2];

        double t = 1.0/probeNumber;
        for(int i = 0; i < probeNumber; i++) {
            if(i > 0)
                t = ((1.0-(1.0/probeNumber))/probeNumber)*i;

            // Add to the array
            coordinates[i] = getCurveCoordinates(t);
        }

        return coordinates;
    }

    /**
     * Returns a double[] of t values used in the calculations
     * @return
     */
    double[] getCurveTValues()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[] tValues = new double[probeNumber];
        
        double t = 1.0/probeNumber;
        for(int i = 0; i < probeNumber; i++) {
            if(i > 0)
                t = ((1.0-(1.0/probeNumber))/probeNumber)*i;

            // Add to the array
            tValues[i] = t;
        }

        return tValues;
    }

    /**
     * Returns the x coordinates for first derivative at position t
     * @return double
     */
    public double getFirstDeriativeXCoordinate(double t)
    {
        return  3 * Math.pow(t, 2) * (point3.x + 3 * (point1.x - point2.x) - point0.x) +
        6 * t * (point0.x - 2 * point1.x + point2.x) +
        3 *(point1.x - point0.x);
    }

    /**
     * Returns the y coordinates for first derivative at position t
     * @return double
     */
    public double getFirstDeriativeYCoordinate(double t)
    {
        return  3 * Math.pow(t, 2) * (point3.y + 3 * (point1.y - point2.y) - point0.y) +
        6 * t * (point0.y - (2 * point1.y) + point2.y) +
        3 *(point1.y - point0.y);
    }

    /**
     * Returns the x and y coordinates for first derivative at position t
     * @return double[]
     */
    public double[] getFirstDeriative(double t)
    {
        double[] coords = new double[2];

        coords[0] = getFirstDeriativeXCoordinate(t);
        coords[1] = getFirstDeriativeYCoordinate(t);

        return coords;
    }

    /**
     * Returns the x and y coordinates for curve tangent at position t
     * This is the same as the first degree derivation but normalized
     * @return double[]
     */
    public double[] getTangent(double t)
    {
        double[] tangent = getFirstDeriative(t);

        // Normalize these so they have a length of 1.0
        double d = Math.sqrt(Math.pow(tangent[0], 2) + Math.pow(tangent[1], 2));
        tangent[0] = tangent[0] / d;
        tangent[1] = tangent[1] / d;

        return tangent;
    }

    /**
     * Returns a list of tangents coordinates for probe points t along curve
     * @return double[][]
     */
    public double[][] getCurveTangents()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[][] coordinates = new double[probeNumber][2];

        double t = 1.0/probeNumber;
        for(int i = 0; i < probeNumber; i++) {
            if(i > 0)
                t = ((1.0-(1.0/probeNumber))/probeNumber)*i;

            // Add to the array
            coordinates[i] = getTangent(t);
        }

        return coordinates;
    }

    /**
     * Returns the normal to the curve at point t
     * @param t
     * @return
     */
    public double[] getNormal(double t)
    {
        double[] coords = new double[2];

        double[] tangent = getTangent(t);
        coords[0] = tangent[0] * 0 - tangent[1] * 1;
        coords[1] = tangent[0] * 1 + tangent[1] * 0;

        return coords;
    }

    /**
     * Returns a list of normal coordinates for probe points t along curve
     * @return double[][]
     */
    public double[][] getCurveNormals()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[][] coordinates = new double[probeNumber][2];

        double t = 1.0/probeNumber;
        for(int i = 0; i < probeNumber; i++) {
            if(i > 0)
                t = ((1.0-(1.0/probeNumber))/probeNumber)*i;

            // Add to the array
            coordinates[i] = getNormal(t);
        }

        return coordinates;
    }

    /**
     * Returns the x coordinates for second derivative at position t
     * @return double
     */
    public double getSecondDeriativeXCoordinate(double t)
    {
        return  6 * t * (point3.x + 3 * (point1.x - point2.x) - point0.x) +
        6 * (point0.x - (2 * point1.x) + point2.x);
    }

    /**
     * Returns the y coordinates for second derivative at position t
     * @return double
     */
    public double getSecondDeriativeYCoordinate(double t)
    {
        return  6 * t * (point3.y + 3 * (point1.y - point2.y) - point0.y) +
        6 * (point0.y - (2 * point1.y) + point2.y);
    }

    /**
     * Returns the x,y coordinates for second derivative at position t
     * @return double[]
     */
    public double[] getSecondDeriative(double t)
    {
        double[] coords = new double[2];

        coords[0] = getSecondDeriativeXCoordinate(t);
        coords[1] = getSecondDeriativeYCoordinate(t);

        return coords;
    }

    /**
     * Returns kappa (curvature) value for point t on the bezier curve segment
     * @see https://pomax.github.io/bezierinfo/#curvature
     * @param t
     * @return double
     */
    public double getCurveKappa(double t)
    {

        double[] d = getFirstDeriative(t);
        double[] dd = getSecondDeriative(t);
        double numerator = d[0]* dd[1] - dd[0] * d[1];
        double denominator = Math.pow(d[0]*d[0] + d[1]*d[1], 3/2);

        if(denominator == 0)
            return Double.NaN;

        return numerator / denominator;
    }

    /**
     * Returns a list of kappa values one for each probe point
     * @return double[]
     */
    public double[] getCurveKappas()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[] kappas = new double[probeNumber];

        double t = 1.0/probeNumber;
        for(int i = 0; i < probeNumber; i++) {
            if(i > 0)
                t = ((1.0-(1.0/probeNumber))/probeNumber)*i;

            // Add to the array
            kappas[i] = getCurveKappa(t);
        }

        return kappas;
    }


    /**
     * Returns the radius of a circle that fits the curvature at point t on the curve
     * @param t
     * @return
     */
    public double getRadiusOfFittedCircle(double t)
    {
        return 1 / getCurveKappa(t);
    }

    /**
     * Returns a list of radii values for circle fitting one for each probe point
     * @return double[]
     */
    public double[] getRadiiOfFittedCircle()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[] radii = new double[probeNumber];

        double t = 1.0/probeNumber;
        for(int i = 0; i < probeNumber; i++) {
            if(i > 0)
                t = ((1.0-(1.0/probeNumber))/probeNumber)*i;

            // Add to the array
            radii[i] = getRadiusOfFittedCircle(t);
        }

        return radii;
    }
    
    /**
     * Returns a control point if the coordinate given is inside or near the point.
     * We check 2*pointWidth from the center of the control point.
     * @param testX
     * @param testY
     * @return BezierControlPoint
     */
    public BezierControlPoint insideControlPoint(double testX, double testY) {
        // The order that the points are tested in are very important
        // if they get changed than the behaviour of other parts of 
        // the program will get affected. 
        if (point1.contains(testX, testY)) {
            return point1;
        } else if (point0.contains(testX, testY)) {
            return point0;
        } else if (point2.contains(testX, testY)) {
            return point2;
        } else if (point3.contains(testX, testY)) {
            return point3;
        }
        return null;
    }
}
