import ij.gui.Roi;

/**
 * Class: BezierControlPoint
 * Holds a control point and references to points before and after
 * Based on the Bezier Curve Tool by:
 * @Author Berin Martini
 * @Version 2012-01-18
 * @see https://imagej.nih.gov/ij/plugins/bezier-tool/index.html
 */
public class BezierControlPoint {
    OptionsStore optionStore;
    public double x;
    public double y;
    private BezierSegment bezier;
    private BezierPointType pointType; // identify which bezier control point it is.
    
    /**
     * Constructor
     * @param bezier
     * @param pointType
     * @param x
     * @param y
     * @param pointWidth
     */
    BezierControlPoint(BezierSegment bezier, OptionsStore optionStore, BezierPointType pointType, double x, double y) {
        this.bezier = bezier;
        this.optionStore = optionStore;
        this.pointType = pointType;  
        movePoint(x, y);
    }
    
    /**
     * Moves the control point
     * @param nx
     * @param ny
     */
    public void movePoint(double nx, double ny) {
        this.x = nx;
        this.y = ny;
    }
    
    /**
     * Sets the bezier control point
     * @param nx
     * @param ny
     */
    public void setPoint(double nx, double ny) {
        bezier.movePoint(pointType, nx, ny);
        movePoint(nx, ny);
    }
    
    /**
     * Returns a reference to the parent Bezier segment
     * @return BezierSegment
     */
    public BezierSegment getParentBezier() {
        return bezier;
    }
    
    /**
     * Returns the set poitn type
     * @return BezierPointType
     */
    public BezierPointType getPointType() {
        return pointType;
    }
    
    /**
     * Is the given coordinate within or near the control point. We check
     * 2*pointWidth from the center of the control point.
     * @param testX
     * @param testY
     * @return boolean
     */
    public boolean contains(double testX, double testY) {
        // We check twice the width of a control point
        int pointWidth = optionStore.getControlPointWidth();
        Roi pointROI = new Roi(((int) x - (2 * pointWidth)), ((int) y - (2 * pointWidth)), (4 * pointWidth), (4 * pointWidth));
        return (pointROI.contains((int) testX, (int) testY)); 
    }
}
