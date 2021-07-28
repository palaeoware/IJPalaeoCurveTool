import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.tool.PlugInTool;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.awt.event.*;
import java.awt.Font;

/**
 * The Palaeo_Curve_Tool plugin class
 */
public class Palaeo_Curve_Tool extends PlugInTool {
	
	private OptionsStore optionsStore;
	private BezierSegmentList bezierList;
	private BezierControlPoint bezierPoint;
	double[][][] coordsForT;
	double[][][] coordsTangents;
	double[][][] coordsNormals;
	double[][] kappaValues;
	double[][] tValues;

	/**
	 * Main method added for debuging and testing
	 * @param args
	 */
	public static void main(String[] args) {
		new ImageJ();

		IJ.setDebugMode(true);

		// open an image (if needed)
		ImagePlus image = IJ.openImage("D:\\F1.large.jpg");
		image.show();

		IJ.log("Palaeo Curve plugin has been programically called...");
		IJ.runPlugIn("Palaeo_Curve_Tool", "");
	}

	public Palaeo_Curve_Tool() {
        if (IJ.versionLessThan("1.46f")) ;

		this.optionsStore = new OptionsStore();
		this.bezierList = new BezierSegmentList(optionsStore);
	}

	public String getToolIcon() {
		return "CfffD00D01D02D03D0dD0eD0fD10D11D12D13D1dD1eD1fD20D21D22D23D2dD2eD2fD30D31D32D33D36D37D3aD3bD3cD3dD3eD3fD40D41D42D43D4aD4bD4cD4dD4eD4fD50D51D52D53D5aD5bD5cD5dD5eD5fD60D61D62D63D6aD6bD6cD6dD6eD6fD70D71D72D73D74D78D79D7aD7bD7cD7dD7eD7fD80D81D82D83D84D85D8bD8cD8dD8eD8fD90D91D92D93D94D9cD9dD9eD9fDa0Da1Da2Da3DadDaeDafDb0Db1Db2Db3Db8DbdDbeDbfDc0Dc1Dc2Dc3Dc6Dc7Dc8Dc9DcaDcdDceDcfDd0Dd1Dd2Dd3Dd6Dd7Dd8Dd9DdaDddDdeDdfDe0De1De2De3De6De7De8De9DeaDedDeeDefDf0Df1Df2Df3Df6Df7Df8Df9DfaDfdDfeDffC333D68Db4DbcC000D44D96D9aDa8Dc4DccCbbbD76DcbCaaaD25D26D27D28D2aD2bD2cD88C111D04D05D06D07D08D09D0aD0bD0cDb5DbbDe4DecCfffD35D38D75D86D8aDb7Db9Dd5DdbC555Df4DfcC111D49D65CcccD77CaaaDacC444Df5CcccDc5De5DebC666D48CeeeD46D47D69CbbbD87D89Da4Db6DbaC555D59CdddD64C444D54DfbC999D9bC777D45C999D95";
	}

	public String getToolName() {
		return "Palaeo Curve";
	}

	/**
	 * This is not not really needed, but logs the loading order to the debug window
	 */
	public void run(String arg) {
		IJ.log("Palaeo Curve has been run. Now adding to the toolbar.");
		Toolbar.addPlugInTool(this);
	}

	public void showOptionsDialog() {
		IJ.log("Palaeo Curve Options has been called...");
	}

    public void mouseDragged(ImagePlus imp, MouseEvent e) {
		ImageCanvas ic = imp.getCanvas();
		double x = ic.offScreenXD(e.getX());
		double y = ic.offScreenYD(e.getY());
		if (bezierList.isEmpty())
			bezierList.cursorPos(x, y);
		else {
			if (bezierPoint == null)
				bezierList.dragTo(imp.getOverlay(), x, y);
			else
				bezierPoint.setPoint(x, y);
		}
		update(imp);
	}
        
	public void mouseClicked(ImagePlus imp, MouseEvent e) {
		if (!bezierList.isEmpty()) {
			// Look for a control point if we find it then
			// set as the current control point
			ImageCanvas ic = imp.getCanvas();
			double x = ic.offScreenXD(e.getX());
			double y = ic.offScreenYD(e.getY());
			bezierPoint = bezierList.insideControlPoint(x,y);
 			
			// Otherwise remove the curve
			if (bezierPoint == null && e.isAltDown()) {
				Overlay overlay = imp.getOverlay();
				if (overlay==null) return;
				
				overlay.clear();
				bezierList = new BezierSegmentList(optionsStore);
			}
		}
	}

	public void mousePressed(ImagePlus imp, MouseEvent e) {
		ImageCanvas ic = imp.getCanvas();
		double x = ic.offScreenXD(e.getX());
		double y = ic.offScreenYD(e.getY());

		if (bezierList.isEmpty()) {
			bezierList.setNewBezierControlPoint(x, y);
			update(imp);
			return;
		} 
        
		bezierPoint = bezierList.insideControlPoint(x,y);
		if (bezierPoint == null) {
			bezierList.cursorPos(x, y);
			return;
		} 
        
		// Mouse Press with SHIFT we clone the control point
		if (e.isShiftDown() ) {
			BezierPointType pointType = bezierPoint.getPointType();
			if ( pointType == BezierPointType.START_POINT || pointType == BezierPointType.END_POINT ) {
				bezierPoint = bezierList.clonePoint(bezierPoint);
			}
			update(imp);
			return;
		}
        
		// Mouse Press with Ctrl we remove the control point
		if (e.isControlDown()) {
			if (bezierList.onlyOneBezierControlPoint()) 
				return;
        
			BezierPointType pointType = bezierPoint.getPointType();
			if ( pointType == BezierPointType.START_POINT || pointType == BezierPointType.END_POINT ) {
				bezierList.removePoint(bezierPoint);
				bezierPoint = null;
				bezierList.cursorPos(x, y);
			}
		}
	}

	public void mouseReleased(ImagePlus imp, MouseEvent e){
		ImageCanvas ic = imp.getCanvas();
		double x = ic.offScreenXD(e.getX());
		double y = ic.offScreenYD(e.getY());        
		if (bezierList.isEmpty()) {
			bezierList.setNewBezierControlPoint(x, y);
			update(imp);
		} else {
			if (bezierPoint == null) {
				bezierList.cursorPos(x, y);
				update(imp);
			}
			bezierPoint = null;
		}
	}

	/**
	 * Does the update of the curve and calcualtions
	 * @param imp
	 */
	private void update(ImagePlus imp)
	{
		Path2D.Double curvePath = bezierList.getCurvePath();
		ArrayList<double[]> coordsOfMaximumCurve = new ArrayList<double[]>();
		ArrayList<Double> tOfMaximumCurve = new ArrayList<Double>();

		if (curvePath != null) {
			// Update these calculated values now, so we don't have to do it again later
			coordsForT = bezierList.getCurveCoordinates();
			coordsTangents = bezierList.getCurveTangents();
			coordsNormals = bezierList.getCurveNormals();
			kappaValues = bezierList.getCurveKappas();
			tValues = bezierList.getCurveTValues();

			// For debugging...
			/*for(int j = 0; j < coordsForT.length; j++)
			{
				IJ.log("Segment: " + j);
				for(int k = 0; k < coordsForT[j].length; k++)
				{
					IJ.log("- x:" + coordsForT[j][k][0] + " y:" + coordsForT[j][k][1] + 
					" | Tangent x:" + coordsTangents[j][k][0] + " y:" + coordsTangents[j][k][1] +
					" | Normal x:" + coordsNormals[j][k][0] + " y:" + coordsNormals[j][k][1] +
					" | Kappa:" + kappaValues[j][k]);
				}
			}*/

			// Find coordinate(s) that has the most curvature and mark...
			double currentHighestKappa = 0.01;
			for(int j = 0; j < kappaValues.length; j++)
			{
				for(int k = 0; k < kappaValues[j].length; k++)
				{
					double kappa = doubleAbs(kappaValues[j][k]);
					if(kappa > currentHighestKappa) 
					{
						currentHighestKappa = kappa;
						coordsOfMaximumCurve.clear();
						coordsOfMaximumCurve.add(coordsForT[j][k]);
						tOfMaximumCurve.clear();
						tOfMaximumCurve.add(tValues[j][k]/kappaValues.length);

					} else if (kappa == currentHighestKappa) {
						coordsOfMaximumCurve.add(coordsForT[j][k]);
						tOfMaximumCurve.add(tValues[j][k]/kappaValues.length);
					}
				}
			}
		}
		
		updateGraphics(imp, coordsOfMaximumCurve, tOfMaximumCurve);
	}

	/**
	 * Adds/Updates the overlay graphics
	 * @param imp
	 */
    private void updateGraphics(ImagePlus imp, ArrayList<double[]> coordsOfMaximumCurve, ArrayList<Double> tOfMaximumCurve) {
		Overlay overlay = new Overlay();
		double[][] coor = bezierList.getControlPointCoordinates();
		double pointWidth = optionsStore.getControlPointWidth();
		for (int xx = 0; xx < coor.length; xx+=2) {
			double[] point0 = coor[xx];
			double[] point1 = coor[(xx + 1)];

			// Control Point 1 (on curve)
			OvalRoi controlPointAOval = new OvalRoi((point0[0] - (pointWidth/2)), (point0[1] - (pointWidth/2)), pointWidth, pointWidth);
			controlPointAOval.setFillColor(Color.red);
			controlPointAOval.setStrokeColor(Color.red);
			controlPointAOval.setName("Control Point " + xx);
			overlay.add(controlPointAOval);

			// Control Point 2 and line (the handle)
			OvalRoi controlPointBOval = new OvalRoi((point1[0] - (pointWidth/2)), (point1[1] - (pointWidth/2)), pointWidth, pointWidth);
			controlPointBOval.setStrokeColor(Color.blue);
			controlPointBOval.setName("Control Point " + (xx + 1));
			overlay.add(controlPointBOval);

			Line controlPointHandleLine = new Line(point0[0], point0[1], point1[0], point1[1]);
			controlPointHandleLine.setStrokeColor(Color.blue);
			controlPointHandleLine.setName("Handle Line " + xx);
			overlay.add(controlPointHandleLine);
 		}
		
		Path2D.Double curvePath = bezierList.getCurvePath();
		if (curvePath != null) {
			ShapeRoi curveROI = new ShapeRoi(curvePath);
			curveROI.setStrokeColor(Color.yellow);
			curveROI.setName("Bezier Curve");
			overlay.add(curveROI);

			if(optionsStore.getShowTangents() || optionsStore.getShowNormals()) {
				// Draw tagent and normal overlays
				for(int j = 0; j < coordsForT.length; j++)
				{
					for(int k = 0; k < coordsForT[j].length; k++)
					{
						if(optionsStore.getShowTangents()) {
							Line tangentLine = new Line(
								coordsForT[j][k][0], 
								coordsForT[j][k][1], 
								coordsForT[j][k][0] + (coordsTangents[j][k][0] * optionsStore.getTangentsScaleFactor()), 
								coordsForT[j][k][1] + (coordsTangents[j][k][1] * optionsStore.getTangentsScaleFactor())
							);
							tangentLine.setStrokeColor(Color.green);
							tangentLine.setName("Tangent " + j + "-" + k);
							overlay.add(tangentLine);
						}

						if(optionsStore.getShowNormals()) {
							Line normalLine = new Line(
								coordsForT[j][k][0], 
								coordsForT[j][k][1], 
								coordsForT[j][k][0] + (coordsNormals[j][k][0] * optionsStore.getNormalsScaleFactor()), 
								coordsForT[j][k][1] + (coordsNormals[j][k][1] * optionsStore.getNormalsScaleFactor())
							);
							normalLine.setStrokeColor(Color.pink);
							normalLine.setName("Normal " + j + "-" + k);
							overlay.add(normalLine);
						}
					}
				}
			}

			if(coordsOfMaximumCurve.size() > 0 && optionsStore.getShowMaximumCurvaturePoints()){
				int i = 0;
				for (double[] coord : coordsOfMaximumCurve) {
					OvalRoi controlPointBOval = new OvalRoi((coord[0] - 2), (coord[1] - 2), 4, 4);
					controlPointBOval.setStrokeColor(optionsStore.getMaximumCurvaturePointsStroke());
					controlPointBOval.setFillColor(optionsStore.getMaximumCurvaturePointsFill());
					controlPointBOval.setName("Maximum Curve " + (i + 1));
					overlay.add(controlPointBOval);
					
					String labelText = String.format("%.5g%%", tOfMaximumCurve.get(i)*100);
					TextRoi textROI = new TextRoi((coord[0] + 5), (coord[1] + 5), 10, 24, labelText, new Font("Arial", Font.PLAIN, 10));
					textROI.setStrokeColor(Color.BLACK);
					textROI.setFillColor(Color.WHITE);
					textROI.setName("Maximum Curve Text " + (i + 1));
					overlay.add(textROI);
					i++;
				}
			}
		}

		// Set the overlay
		imp.setOverlay(overlay);
	}

	private double doubleAbs(double val) {
		if(val < 0)
			return val * -1;
		return val;
	}
}