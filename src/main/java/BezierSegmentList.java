import java.awt.geom.*;
import ij.gui.Overlay;

/**
 * Class: BezierSegmentList
 * Based on the Bezier Curve Tool by:
 * @Author Berin Martini
 * @Version 2012-01-18
 * @see https://imagej.nih.gov/ij/plugins/bezier-tool/index.html
 */
public class BezierSegmentList {

    private OptionsStore optionStore;
    private BezierSegment bezierStart = null;
    private BezierSegment bezierEnd = null;
    private BezierSegment bezierCurrent = null;
    private BezierPointType pointType = BezierPointType.START_POINT;
    private double	x0, y0, x1, y1, x3, y3, xTmp, yTmp;
     
    /**
     * Construtor that sets the starting point width
     * @param optionStore
     */
    BezierSegmentList(OptionsStore optionStore) {
        this.optionStore = optionStore;
    }

    /**
     * Is the list of segments empty?
     * @return boolean
     */
    public boolean isEmpty() {
        return (bezierStart == null);
    }
    
    /**
     * DO we only have one control point at the moment?
     * @return boolean
     */
    public boolean onlyOneBezierControlPoint() {
        return (bezierStart == bezierEnd);
    }

    /**
     * Updates the current cursor position
     * @param x
     * @param y
     */
    public void cursorPos(double x, double y) {
        xTmp = x;
        yTmp = y;
    }
    
    /**
     * Sets a new Bezier control point at coordinates X,Y
     * @param x
     * @param y
     */
    public void setNewBezierControlPoint(double x, double y) {
        if (pointType == BezierPointType.START_POINT) {
            x0 = x;
            y0 = y;
            xTmp = x;
            yTmp = y;
            pointType = BezierPointType.CONTROL_1;
        } else if (pointType == BezierPointType.CONTROL_1) {
            x1 = x;
            y1 = y;
            pointType = BezierPointType.END_POINT;
        } else if (pointType == BezierPointType.END_POINT) {
            x3 = x;
            y3 = y;
            xTmp = x;
            yTmp = y;
            pointType = BezierPointType.CONTROL_2;
        } else if (pointType == BezierPointType.CONTROL_2) {
            bezierStart = new BezierSegment(optionStore, x0, y0, x1, y1, x, y, x3, y3);
            bezierEnd = bezierStart;
            pointType = BezierPointType.START_POINT;
        } 
            
    }
    
    /**
     * Returns the coordinates for the controls points
     * @return double[][]
     */
    public double[][] getControlPointCoordinates() {
        double[][] coordinates = {{}};
        if (isEmpty()) {
            if (pointType == BezierPointType.CONTROL_1) {
                double[][] c = {{x0, y0},{xTmp, yTmp}};
                coordinates = c;
            } else if (pointType == BezierPointType.END_POINT) {
                double[][] c = {{x0, y0},{x1, y1}};
                coordinates = c;
            } else if (pointType == BezierPointType.CONTROL_2) {
                double[][] c = {{x0, y0},{x1, y1}, {x3, y3}, {xTmp, yTmp}};
                coordinates = c;
            } 
        } else {
            coordinates = bezierStart.getPointCoordinates();
            bezierCurrent = bezierStart.next();
            while (bezierCurrent != null) {
                double[][] coor = new double[coordinates.length + 4][2];
                System.arraycopy(coordinates, 0, coor, 0, coordinates.length);
                System.arraycopy(bezierCurrent.getPointCoordinates(), 0, coor, coordinates.length, 4);
                coordinates = coor;
                bezierCurrent = bezierCurrent.next();
            }
        }
        return coordinates;
    }

    /**
     * Returns the number of segments stored in the list
     * @return int
     */
    public int getNumberSegments()
    {
        if (bezierStart == null) {
            return 0;
        }
        
        int i = 1;
        bezierCurrent = bezierStart.next();
        while (bezierCurrent != null) {
            i++;
            bezierCurrent = bezierCurrent.next();
        }

        return i;
    }

    /**
     * Returns all the coordinates for a curve using the probe number to generate the X,Y positions.
     * double[k][j][i] = k is the segment number, j is a probe point, and i is hte X/Y coordinate.
     * @return double[][][] | null
     */
    public double[][][] getCurveCoordinates()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[][] segCoordinates;
        
        if (bezierStart == null) {
            return null;
        }

        // Need to know how many segments we will be looking at to setup the double[][]
        int i = getNumberSegments();
        double[][][] coordinates = new double[i][probeNumber][2];

        int j = 0;
        // Get the coordinates of the bezierStart curve
        segCoordinates = bezierStart.getCurveCoordinates();
        for(int k = 0; k < probeNumber; k++)
        {
            coordinates[j][k][0] = segCoordinates[k][0]; // x
            coordinates[j][k][1] = segCoordinates[k][1]; // y
        }
        j++;

        // Get coordinates of all other curves and add them to the return
        bezierCurrent = bezierStart.next();
        while (bezierCurrent != null) {
            segCoordinates = bezierCurrent.getCurveCoordinates();

            for(int k = 0; k < probeNumber; k++)
            {
                coordinates[j][k][0] = segCoordinates[k][0]; // x
                coordinates[j][k][1] = segCoordinates[k][1]; // y
            }

            j++;
            bezierCurrent = bezierCurrent.next();
        }

        return coordinates;
    }

    /**
     * Returns all the tangent coordinates for a curve using the probe number to generate the X,Y positions.
     * double[k][j][i] = k is the segment number, j is a probe point, and i is the X/Y coordinate.
     * @return double[][][] | null
     */
    public double[][][] getCurveTangents()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[][] segCoordinates;
        
        if (bezierStart == null) {
            return null;
        }

        // Need to know how many segments we will be looking at to setup the double[][]
        int i = getNumberSegments();
        double[][][] coordinates = new double[i][probeNumber][2];

        int j = 0;
        // Get the coordinates of the bezierStart curve
        segCoordinates = bezierStart.getCurveTangents();
        for(int k = 0; k < probeNumber; k++)
        {
            coordinates[j][k][0] = segCoordinates[k][0]; // x
            coordinates[j][k][1] = segCoordinates[k][1]; // y
        }
        j++;

        // Get coordinates of all other curves and add them to the return
        bezierCurrent = bezierStart.next();
        while (bezierCurrent != null) {
            segCoordinates = bezierCurrent.getCurveTangents();

            for(int k = 0; k < probeNumber; k++)
            {
                coordinates[j][k][0] = segCoordinates[k][0]; // x
                coordinates[j][k][1] = segCoordinates[k][1]; // y
            }

            j++;
            bezierCurrent = bezierCurrent.next();
        }

        return coordinates;
    }

    /**
     * Returns all the normals coordinates for a curve using the probe number to generate the X,Y positions.
     * double[k][j][i] = k is the segment number, j is a probe point, and i is the X/Y coordinate.
     * @return double[][][] | null
     */
    public double[][][] getCurveNormals()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[][] segCoordinates;
        
        if (bezierStart == null) {
            return null;
        }

        // Need to know how many segments we will be looking at to setup the double[][]
        int i = getNumberSegments();
        double[][][] coordinates = new double[i][probeNumber][2];

        int j = 0;
        // Get the coordinates of the bezierStart curve
        segCoordinates = bezierStart.getCurveNormals();
        for(int k = 0; k < probeNumber; k++)
        {
            coordinates[j][k][0] = segCoordinates[k][0]; // x
            coordinates[j][k][1] = segCoordinates[k][1]; // y
        }
        j++;

        // Get coordinates of all other curves and add them to the return
        bezierCurrent = bezierStart.next();
        while (bezierCurrent != null) {
            segCoordinates = bezierCurrent.getCurveNormals();

            for(int k = 0; k < probeNumber; k++)
            {
                coordinates[j][k][0] = segCoordinates[k][0]; // x
                coordinates[j][k][1] = segCoordinates[k][1]; // y
            }

            j++;
            bezierCurrent = bezierCurrent.next();
        }

        return coordinates;
    }

    /**
     * Returns all the kappa values for a curve using the probe number to generate the t positions.
     * double[k][j] = k is the segment number, j is a value at a probe point.
     * @return double[][] | null
     */
    public double[][] getCurveKappas()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[] kappaValues;
        
        if (bezierStart == null) {
            return null;
        }

        // Need to know how many segments we will be looking at to setup the double[][]
        int i = getNumberSegments();
        double[][] newKappaValues = new double[i][probeNumber];

        int j = 0;
        // Get the coordinates of the bezierStart curve
        kappaValues = bezierStart.getCurveKappas();
        for(int k = 0; k < probeNumber; k++)
        {
            newKappaValues[j][k] = kappaValues[k]; // x
        }
        j++;

        // Get coordinates of all other curves and add them to the return
        bezierCurrent = bezierStart.next();
        while (bezierCurrent != null) {
            kappaValues = bezierCurrent.getCurveKappas();

            for(int k = 0; k < probeNumber; k++)
            {
                newKappaValues[j][k] = kappaValues[k];
            }

            j++;
            bezierCurrent = bezierCurrent.next();
        }

        return newKappaValues;
    }

    /**
     * Returns all T values used for all segments
     * @return
     */
    public double[][] getCurveTValues()
    {
        int probeNumber = optionStore.getProbeNumber();
        double[] tValues;
        
        if (bezierStart == null) {
            return null;
        }

        // Need to know how many segments we will be looking at to setup the double[][]
        int i = getNumberSegments();
        double[][] newTValues = new double[i][probeNumber];

        int j = 0;
        // Get the coordinates of the bezierStart curve
        tValues = bezierStart.getCurveTValues();
        for(int k = 0; k < probeNumber; k++)
        {
            newTValues[j][k] = tValues[k]; // x
        }
        j++;

        // Get coordinates of all other curves and add them to the return
        bezierCurrent = bezierStart.next();
        while (bezierCurrent != null) {
            tValues = bezierCurrent.getCurveTValues();

            for(int k = 0; k < probeNumber; k++)
            {
                newTValues[j][k] = tValues[k];
            }

            j++;
            bezierCurrent = bezierCurrent.next();
        }

        return newTValues;
    }
    
    /**
     * Returns a general path built from all Bezier segments
     * @return Path2D.Double
     */
    public Path2D.Double getCurvePath() {
        if (bezierStart == null) {
            return null;
        }

        Path2D.Double curvePath = bezierStart.getCurvePath();
        bezierCurrent = bezierStart.next();
        while (bezierCurrent != null) {
            curvePath.append(bezierCurrent.getCurvePath(), true);
            bezierCurrent = bezierCurrent.next();
        }

        return curvePath;
    }
    
    /**
     * Remove the given control point from the list
     * @param point
     * @implNote Should not be called if only one bezier curve on image
     */
    public void removePoint(BezierControlPoint point) {
        bezierCurrent = point.getParentBezier();
    
        if (point.getPointType() == BezierPointType.END_POINT) { // True for the majority of cases

            if (bezierCurrent == bezierStart) {
                bezierStart = bezierCurrent.next();
                bezierStart.point0.movePoint(bezierCurrent.point0.x, bezierCurrent.point0.y);
                bezierStart.point1.movePoint(bezierCurrent.point1.x, bezierCurrent.point1.y);
    
            } else if (bezierCurrent == bezierEnd) {
                bezierEnd = bezierCurrent.previous();
                bezierEnd.setNext(null);
                bezierEnd.movePoint(BezierPointType.END_POINT, bezierEnd.point3.x, bezierEnd.point3.y);
            } else {
                BezierSegment previous = bezierCurrent.previous();
                BezierSegment next = bezierCurrent.next();

                previous.setNext(next);
                next.setPrevious(previous);
    
                previous.movePoint(BezierPointType.END_POINT, previous.point3.x, previous.point3.y);
                previous.movePoint(BezierPointType.CONTROL_2, previous.point2.x, previous.point2.y);
            }
            
        } else if (point.getPointType() == BezierPointType.START_POINT) {
            //point must belong to bezierStart if it has a pointType of START_POINT
            bezierStart = bezierCurrent.next();
            bezierStart.setPrevious(null);
            bezierStart.movePoint(BezierPointType.START_POINT,bezierStart.point0.x, bezierStart.point0.y); 
        }
    }
    
    /**
     * Clone a control point
     * @param oldPoint
     * @return
     */
    public BezierControlPoint clonePoint(BezierControlPoint oldPoint) { 
        BezierSegment oldBezier = oldPoint.getParentBezier();
        BezierControlPoint newPoint = null;
        BezierSegment newBezier = null;
        double[][] coor = oldBezier.getPointCoordinates();

        // If start point ...
        if (oldPoint.getPointType() == BezierPointType.END_POINT) {
            newBezier = new BezierSegment(
                optionStore,
                coor[2][0], coor[2][1],
                (coor[2][0] + coor[2][0] - coor[3][0]), (coor[2][1] + coor[2][1] - coor[3][1]), 
                coor[3][0], coor[3][1],
                coor[2][0], coor[2][1]
            );
    
            oldBezier.insertAsNext(newBezier);
            if (oldBezier.equals(bezierEnd)) { 
                bezierEnd = newBezier; 
            }
            newPoint = newBezier.point3;
        } 
        // If end point...
        else if (oldPoint.getPointType() == BezierPointType.START_POINT) {
            newBezier = new BezierSegment(
                optionStore,
                coor[0][0], coor[0][1],
                coor[1][0], coor[1][1],
                (coor[0][0] + coor[0][0] - coor[1][0]), (coor[0][1] + coor[0][1] - coor[1][1]), 
                coor[0][0], coor[0][1]
                );
            oldBezier.insertAsPrevious(newBezier);
            if (oldBezier.equals(bezierStart)) { 
                bezierStart = newBezier; 
            }
            newPoint = newBezier.point0;
        }
        
        return newPoint;
    }

    /**
     * Drag the whole curve object to another position
     * @param overlay
     * @param x
     * @param y
     */
    public void dragTo(Overlay overlay, double x, double y) {
        if (overlay==null) return;
        double dx = xTmp - x;
        double dy = yTmp - y;
        overlay.translate((int)dx, (int)dy);
    
        bezierStart.point0.movePoint((bezierStart.point0.x - dx), (bezierStart.point0.y - dy));
        bezierStart.point1.movePoint((bezierStart.point1.x - dx), (bezierStart.point1.y - dy));
        bezierStart.point2.movePoint((bezierStart.point2.x - dx), (bezierStart.point2.y - dy));
        bezierStart.point3.movePoint((bezierStart.point3.x - dx), (bezierStart.point3.y - dy));
    
        bezierCurrent = bezierStart.next();
        while (bezierCurrent != null) {
            bezierCurrent.point0.movePoint((bezierCurrent.point0.x - dx), (bezierCurrent.point0.y - dy));
            bezierCurrent.point1.movePoint((bezierCurrent.point1.x - dx), (bezierCurrent.point1.y - dy));
            bezierCurrent.point2.movePoint((bezierCurrent.point2.x - dx), (bezierCurrent.point2.y - dy));
            bezierCurrent.point3.movePoint((bezierCurrent.point3.x - dx), (bezierCurrent.point3.y - dy));
            bezierCurrent = bezierCurrent.next();
        }
        xTmp = x;
        yTmp = y;
    }
    
    /**
     * Is the coordinate given inside a control point? If so return the control point.
     * @param testX
     * @param testY
     * @return
     */
    public BezierControlPoint insideControlPoint(double testX, double testY) { 
        // The particular order in which points are checked is used by other parts 
        // of the programe, changing this will affect the behaver of other parts of the
        // program in potentialy strage ways.
        BezierControlPoint controlPoint = bezierStart.insideControlPoint(testX, testY);
        bezierCurrent = bezierStart.next();

        while (bezierCurrent != null && controlPoint == null) {
            controlPoint = bezierCurrent.insideControlPoint(testX, testY);
            bezierCurrent = bezierCurrent.next();
        }

        return controlPoint;
    }
}
