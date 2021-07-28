import java.awt.Color;

/**
 * Simple class to store options data and default values
 */
public class OptionsStore {
    int controlPointWidth;
    int probeNumber;
    boolean showTangents;
    boolean showNormals;
    double tangentScaleFactor;
    double normalScaleFactor;
    boolean showMaximumCurvaturePoints;
    Color maximumCurvaturePointsStroke;
    Color maximumCurvaturePointsFill;

    /**
     * Contructor
     */
    public OptionsStore()
    {
        setToDefaults();
    }

    /**
     * Sets the options to default values
     */
    public void setToDefaults() {
        // This is the deafault control point width. Used for
        // drawing the control point circles on screen and in
        // calculations throughout
        controlPointWidth = 8;
        
        // This is the default number of coordinate points 
        // calculated for bezier curve segment. Default = 100
        probeNumber = 256;

        // These are used for testing the tangents and normals
        // by daring them on screen. Recommend reducing the 
        // probeNumber to below 50 if using these settings, 
        // otherwise it becomes hard to see what is going on!
        showTangents = false;
        showNormals = false;
        tangentScaleFactor = 15.0;
        normalScaleFactor = 15.0;

        // Turn on to show a drawn circle on the curve at the 
        // position of maximum curvature. Note there may be more than
        // one point present!
        showMaximumCurvaturePoints = true;
        maximumCurvaturePointsStroke = Color.RED;
        maximumCurvaturePointsFill = Color.RED;
    }

    /**
     * Gets the Control Point Width
     * @return int
     */
    public int getControlPointWidth()
    {
        return controlPointWidth;
    }

    /**
     * Sets the Control Point Width
     * @param width
     */
    public void setControlPointWidth(int width)
    {
        controlPointWidth = width;
    }

    /**
     * Gets the Probe Number
     * @return int
     */
    public int getProbeNumber()
    {
        return probeNumber;
    }

    /**
     * Sets the Probe Number
     * @param width
     */
    public void setProbeNumber(int num)
    {
        probeNumber = num;
    }

    public boolean getShowTangents()
    {
        return showTangents;
    }

    public void setShowTangents(boolean bool)
    {
        showTangents = bool;
    }

    public boolean getShowNormals()
    {
        return showNormals;
    }

    public void setShowNormals(boolean bool)
    {
        showNormals = bool;
    }

    public double getTangentsScaleFactor()
    {
        return tangentScaleFactor;
    }

    public void setTangentsScaleFactor(double val)
    {
        tangentScaleFactor = val;
    }

    public double getNormalsScaleFactor()
    {
        return normalScaleFactor;
    }

    public void setNormalsScaleFactor(double val)
    {
        normalScaleFactor = val;
    }


    public boolean getShowMaximumCurvaturePoints()
    {
        return showMaximumCurvaturePoints;
    }

    public void setShowMaximumCurvaturePoints(boolean bool)
    {
        showMaximumCurvaturePoints = bool;
    }

    public Color getMaximumCurvaturePointsStroke()
    {
        return maximumCurvaturePointsStroke;
    }

    public void setMaximumCurvaturePointsStroke(Color color)
    {
        maximumCurvaturePointsStroke = color;
    }

    public Color getMaximumCurvaturePointsFill()
    {
        return maximumCurvaturePointsFill;
    }

    public void setMaximumCurvaturePointsFill(Color color)
    {
        maximumCurvaturePointsFill = color;
    }
}
