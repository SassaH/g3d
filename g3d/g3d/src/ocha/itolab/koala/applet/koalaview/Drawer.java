package ocha.itolab.koala.applet.koalaview;

import java.awt.*;
import java.io.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.*;
import javax.imageio.ImageIO;

import com.jogamp.nativewindow.util.Point;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.gl2.GLUgl2;

import com.jogamp.opengl.util.gl2.GLUT;

import javafx.geometry.Point3D;

import java.lang.Object.*;
//import com.sun.javafx.geom.Point2D;

import ocha.itolab.koala.core.data.*;
import ocha.itolab.koala.convexhull.*;


/**
 * 描画処理のクラス
 * 
 * @author itot
 */
public class Drawer implements GLEventListener {
	
	private GL gl;
	private GL2 gl2;
	private GLU glu;
	private GLUgl2 glu2;
	private GLUT glut;
	GLAutoDrawable glAD;
	GLCanvas glcanvas;
	Graph graph;
	
	Transformer trans = null;
	ViewingPanel vp = null;

	DoubleBuffer modelview, projection, p1, p2, p3, p4;
	IntBuffer viewport;
	int windowX, windowY, windowWidth, windowHeight;
	
	boolean isMousePressed = false, isAnnotation = true;

	double edgeDensityThreshold = 0.1;
	double linewidth = 1.0;
	double bundleShape = 0.7;
	double transparency = 1.0;
	boolean colorSwitch[] = null;	
	int edgeDensityMode = 1;
	int colorMode = 1;
	double xmin, xmax, ymin, ymax, zmax, zmin;
	double bezier0, bezier1, bezier2, bezier3;
	int rangeX1 = 0, rangeX2 = 0, rangeY1 = 0, rangeY2 = 0;
	DrawerUtility du = null;

	int dragMode = 1;
	private double angleX = 0.0;
	private double angleY = 0.0;
	private double shiftX = 0.0;
	private double shiftY = 0.0;
	private double scaleX = 0.5;
	private double scaleY = 0.5;
	private double centerX = 0.0;
	private double centerY = 0.0;
	private double centerZ = 0.0;
	private double size = 0.5;
	
	Node pickedNode = null;
	int savemode = SAVE_AS_IS;
	boolean saveImageFlag = false;
	
	double grayedgecolor[] = {0.4, 0.4, 0.4};
	
	public static int SAVE_AS_IS = 0;
	public static int SAVE_UPPER_LEFT = 1;
	public static int SAVE_UPPER_RIGHT = 2;
	public static int SAVE_LOWER_LEFT = 3;
	public static int SAVE_LOWER_RIGHT = 4;
	
	/**
	 * Constructor
	 * 
	 * @param width
	 *            描画領域の幅
	 * @param height
	 *            描画領域の高さ
	 */
	public Drawer(int width, int height, GLCanvas c) {
		glcanvas = c;
		windowWidth = width;
		windowHeight = height;
		du = new DrawerUtility(width, height);

		viewport = IntBuffer.allocate(4);
		modelview = DoubleBuffer.allocate(16);
		projection = DoubleBuffer.allocate(16);

		p1 = DoubleBuffer.allocate(3);
		p2 = DoubleBuffer.allocate(3);
		p3 = DoubleBuffer.allocate(3);
		p4 = DoubleBuffer.allocate(3);

		glcanvas.addGLEventListener(this);
	}

	public GLAutoDrawable getGLAutoDrawable() {
		return glAD;
	}

	/**
	 * ダミーメソッド
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	/**
	 * Transformerをセットする
	 * 
	 * @param transformer
	 */
	public void setTransformer(Transformer view) {
		this.trans = view;
		du.setTransformer(view);
	}

	/**
	 * 描画領域のサイズを設定する
	 * 
	 * @param width
	 *            描画領域の幅
	 * @param height
	 *            描画領域の高さ
	 */
	public void setWindowSize(int width, int height) {
		// windowWidth = width;
		// windowHeight = height;
		// du.setWindowSize(width, height);
	}

	/**
	 * マウスボタンのON/OFFを設定する
	 * 
	 * @param isMousePressed
	 *            マウスボタンが押されていればtrue
	 */
	public void setMousePressSwitch(boolean isMousePressed) {
		this.isMousePressed = isMousePressed;
		if (isMousePressed == true) {
			// drawCategoryTextField();
		}
	}

	/**
	 * 線の太さをセットする
	 * 
	 * @param lw
	 *            線の太さ（画素数）
	 */
	public void setLinewidth(double lw) {
		linewidth = lw;
	}

	public void setGraph(Graph g) {
		graph = g;
	}

	
	public void setSaveImage(int mode) {
		savemode = mode;
		saveImageFlag = true;
	}
	
	
	public void setEdgeThreshold(double ratio) {
		edgeDensityThreshold = ratio;
	}
	
	public void setBundleShape(double ratio) {
		bundleShape = ratio;
	}
	
	public void setEdgeDensityMode(int mode) {
		edgeDensityMode = mode;
	}
	
	public void setBackgroundTransparency(double t) {
		transparency = t;
	}
	
	public void setColorMode(int mode) {
		colorMode = mode;
	}
		

	/**
	 * マウスドラッグのモードを設定する
	 * 
	 * @param dragMode
	 *            (1:ZOOM 2:SHIFT 3:ROTATE)
	 */
	public void setDragMode(int newMode) {
		dragMode = newMode;
	}


	public void setColorSwitch(boolean[] st) {	//選択されている特徴ベクトルのboolean配列
		colorSwitch = st;
	}

	
	/**
	 * ViewingPanelを設定する
	 */
	public void setViewingPanel(ViewingPanel v) {
		vp = v;
	}

	

	
	/**
	 * 初期化
	 */
	public void init(GLAutoDrawable drawable) {

		gl = drawable.getGL();
		gl2 = drawable.getGL().getGL2();
		glu = new GLU();
		glu2 = new GLUgl2();
		glut = new GLUT();
		this.glAD = drawable;

		gl.glEnable(GL.GL_RGBA);
		gl.glEnable(GL2.GL_DEPTH);
		gl.glEnable(GL2.GL_DOUBLE);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_NORMALIZE);
		gl2.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);

		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

	}

	/**
	 * 再描画
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		this.glAD = drawable;
		windowX = x;
		windowY = y;
		windowWidth = width;
		windowHeight = height;
		du.setWindowSize(width, height);
		
		// ビューポートの定義
		gl.glViewport(0, 0, width, height);

		// 投影変換行列の定義
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		gl2.glOrtho(-width / 200.0, width / 200.0, -height / 200.0,
				height / 200.0, -1000.0, 1000.0);

		gl2.glMatrixMode(GL2.GL_MODELVIEW);

	}

	/**
	 * 描画を実行する
	 */
	public void display(GLAutoDrawable drawable) {
	
		this.glAD = drawable;
		long mill1 = System.currentTimeMillis();
		
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// 視点位置を決定
		gl2.glLoadIdentity();
		glu.gluLookAt(centerX, centerY, (centerZ + 20.0), centerX, centerY,
				centerZ, 0.0, 1.0, 0.0);

		shiftX = trans.getViewShift(0);
		shiftY = trans.getViewShift(1);
		scaleX = trans.getViewScaleX() * windowWidth / (size * 600.0);
		scaleY = trans.getViewScaleY() * windowHeight / (size * 600.0);
		angleX = trans.getViewRotateY() * 45.0;
		angleY = trans.getViewRotateX() * 45.0;
		
		if(saveImageFlag == true) {
			if(savemode == SAVE_UPPER_LEFT)
				gl.glViewport(0, 0, windowWidth * 2, windowHeight * 2);
			if(savemode == SAVE_UPPER_RIGHT)
				gl.glViewport(-windowWidth, 0, windowWidth * 2, windowHeight * 2);
			if(savemode == SAVE_LOWER_LEFT)
				gl.glViewport(0, -windowHeight, windowWidth * 2, windowHeight * 2);
			if(savemode == SAVE_LOWER_RIGHT)
				gl.glViewport(-windowWidth, -windowHeight, windowWidth * 2, windowHeight * 2);
		}
		
		// 行列をプッシュ
		gl2.glPushMatrix();

		// いったん原点方向に物体を動かす
		gl2.glTranslated(centerX, centerY, centerZ);

		// マウスの移動量に応じて回転
		gl2.glRotated(angleX, 1.0, 0.0, 0.0);
		gl2.glRotated(angleY, 0.0, 1.0, 0.0);

		// マウスの移動量に応じて移動
		gl2.glTranslated(shiftX, shiftY, 0.0);

		// マウスの移動量に応じて拡大縮小
		gl2.glScaled(scaleX, scaleY, 1.0);

		// 物体をもとの位置に戻す
		gl2.glTranslated(-centerX, -centerY, -centerZ);

		// 変換行列とビューポートの値を保存する
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport);
		gl2.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelview);
		gl2.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projection);

		// 描画
		//paintMesh();
		
		//gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        //gl.glEnable(GL.GL_BLEND);
        //gl.glEnable(GL.GL_LINE_SMOOTH);
		/*
		if(edgeDensityMode == Canvas.EDGE_DENSITY_DISSIMILARITY)
			drawEdgesDissimilarity();
		if(edgeDensityMode == Canvas.EDGE_DENSITY_DEGREE)
			drawEdgesDegree();
		*/
		//gl.glDisable(GL.GL_BLEND);
	    //gl.glDisable(GL.GL_LINE_SMOOTH);
			
	    drawPickedEdges();
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		float[] lightpos = {0.0f, 0.0f, -10.0f};
		//gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightpos, 0);
		drawNodes();
		gl.glDisable(GL2.GL_LIGHTING);
		
		// 行列をポップ
		gl2.glPopMatrix();
	

		long mill2 = System.currentTimeMillis();
		// System.out.println("  drawer.display() time=" + (mill2 - mill1));
		
		if(saveImageFlag) {
			saveImage();
			saveImageFlag = true;
			gl.glViewport(0, 0, windowWidth, windowHeight);
		}
	}
	
	//convex　Hullの描画
	void drawConvex(ArrayList<Point3D> nodes, int colorId){
		ArrayList<Point3D> convex = new ArrayList<Point3D>();
		double zmidi = (zmin + zmax)/2;
		double zwidth = zmax - zmin;
		//System.out.println("midium : " + zmidi);
		
		for(int i = 0; i < nodes.size(); i++){
			Point3D node = nodes.get(i);
			
			glu2.gluUnProject(node.getX(), node.getY(), zmin+((zwidth/graph.vectorname.length)*(double)colorId), modelview, projection, viewport, p1);
			//glu2.gluUnProject(node.getX(), node.getY(), node.getZ(), modelview, projection, viewport, p1);
			//System.out.println(node.getX() + ", " + node.getY() + ", " + node.getZ() + "," + colorId +"," + (zmidi+(zwidth/(graph.vectorname.length)*(double)colorId)));
			Point3D e = new Point3D(p1.get(0), p1.get(1), p1.get(2));
			convex.add(e);
		}
		//System.out.println(convex);
		Color color = VectorParettePanel.calcColor(colorId, graph.vectorname.length);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glDisable(GL.GL_CULL_FACE);
		gl.glEnable(GL.GL_BLEND);
		gl.glDepthMask(false);
		gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
		double rr = (double)color.getRed() / 255.0;
		double gg = (double)color.getGreen() / 255.0;
		double bb = (double)color.getBlue() / 255.0;
		
		//塗りつぶしぽりごん
		gl2.glColor4d(rr, gg, bb, 0.4);
		gl2.glBegin(GL2.GL_POLYGON); 
		for(int i = 0; i < convex.size(); i++){
			  double x = convex.get(i).getX();
			  double y = convex.get(i).getY();
			  double z = convex.get(i).getZ();
			  gl2.glVertex3d(x, y, z);
         }
	  	gl2.glEnd();
	  	
	  	//枠線
		gl2.glColor4d(rr, gg, bb, 1);
		gl2.glBegin(GL2.GL_LINE_LOOP); 
		for(int i=0; i<convex.size(); i++){
			  double x = convex.get(i).getX();
			  double y = convex.get(i).getY();
			  double z = convex.get(i).getZ();
			  gl2.glVertex3d(x, y, z);
         }
	  	gl2.glEnd();
	  	
	  	gl.glDisable(GL.GL_BLEND);
	  	gl.glDepthMask(true);
	  	gl.glEnable(GL2.GL_LIGHTING);
	}
	
	void choiceConvex(ArrayList<Node> nodes, int colorId){
		//ArrayList<Node> nodes = new ArrayList<Node>();
		QuickHull quickHull = new QuickHull();
		ArrayList<Point3D> eachColorConvex = new ArrayList<Point3D>();
		ArrayList<Point3D> convexHull = new ArrayList<Point3D>();
		eachColorConvex = quickHull.eachColorQuickHull(nodes, colorId);	
		convexHull = quickHull.quickHull(eachColorConvex);
		//System.out.println(convexHull);
		drawConvex(convexHull, colorId);
	}

	void drawPickedEdges() {
		// Draw edges of the picked node
		gl2.glColor4d(0.8, 0.6, 0.8, 0.5);
		gl2.glLineWidth(2.0f);
		if(pickedNode != null) {
			
			for(int i = 0; i < pickedNode.getNumConnectedEdge(); i++) {
				Edge e = pickedNode.getConnectedEdge(i);
				Node enode[] = e.getNode();
				gl2.glBegin(GL.GL_LINES);
				if(enode[0] == pickedNode) {
					gl2.glVertex3d(enode[0].getX(), enode[0].getY(), enode[0].getZ());
					gl2.glVertex3d(enode[1].getX(), enode[1].getY(), enode[1].getZ());
				}
				else {
					gl2.glVertex3d(enode[0].getX(), enode[0].getY(), enode[1].getZ());
					gl2.glVertex3d(enode[1].getX(), enode[1].getY(), enode[0].getZ());
				}
				gl2.glEnd();
			}
			for(int i = 0; i < pickedNode.getNumConnectingEdge(); i++) {
				Edge e = pickedNode.getConnectingEdge(i);
				Node enode[] = e.getNode();
				gl2.glBegin(GL.GL_LINES);
				if(enode[0] == pickedNode) {
					gl2.glVertex3d(enode[0].getX(), enode[0].getY(), enode[0].getZ());
					gl2.glVertex3d(enode[1].getX(), enode[1].getY(), enode[1].getZ());
				}
				else {
					gl2.glVertex3d(enode[0].getX(), enode[0].getY(), enode[1].getZ());
					gl2.glVertex3d(enode[1].getX(), enode[1].getY(), enode[0].getZ());
				}
				gl2.glEnd();
			}
		}
		gl2.glLineWidth(1.0f);
	}
	

	
	void drawNodes() {
		if(graph == null) return;
		float colf[] = new float[3];
		ArrayList<Node> nodes = new ArrayList<Node>();
		QuickHull quickHull = new QuickHull();
		double maxZ = -100.0;
		double minZ = 100.0;
		
		
		// Draw plots
		for(int i = 0; i < graph.nodes.size(); i++) {
			Node node = (Node)graph.nodes.get(i);
			boolean colorBool = false;
			
			double size2 = 0.01; // TODO: adjust
			
			int colorId = node.getColorId();
			if(colorId < 0) {
				colf[0] = colf[1] = colf[2] = 0.2f;
			}
			else {
				Color color = VectorParettePanel.calcColor(colorId, graph.vectorname.length);	//色の計算
				double rr = (double)color.getRed() / 255.0;
				double gg = (double)color.getGreen() / 255.0;
				double bb = (double)color.getBlue() / 255.0;
				
				if(colorMode == 1){
					for(int j=0; j<colorSwitch.length; j++){	//選択されているベクトルを少しでも持つノード->色付け
						if(colorSwitch[j]){
							if(node.getValue(j) != 0){	
								colorBool = true;
								break;
							}
						}
					}
					if(colorBool){
						colf[0] = (float)rr;
						colf[1] = (float)gg;
						colf[2] = (float)bb;
					}else{
						//Color brighter = VectorParettePanel.downSaturation(color);
						colf[0] = (float) 0.5;
						colf[1] = (float) 0.5;
						colf[2] = (float) 0.5;
					}
				}else{
					colf[0] = (float)rr;
					colf[1] = (float)gg;
					colf[2] = (float)bb;
				}
				
			}
			gl2.glMaterialfv(GL.GL_FRONT_AND_BACK,
					GL2.GL_AMBIENT_AND_DIFFUSE, colf, 0);
			drawOneNode(node, size2);	//ここで二次元上の座標が決まる
			nodes.add(node);

			
			if(maxZ < node.getPZ())
					maxZ = node.getPZ();
			if(minZ > node.getPZ())
					minZ = node.getPZ();

			
			//maxZ = Math.max(maxZ, node.getPZ());
			//minZ = Math.min(minZ, node.getPZ());	
			
		}//二次元上の座標がきまった	
		//colorIdが1のノードにQuickHullして外枠になる点を決める
		if(isMousePressed == false){	//マウスのボタンが押されていないとき
			for(int i = 0 ; i < colorSwitch.length ; i++){
					if(colorSwitch[i] == true){
						choiceConvex(nodes, i);
					}
			}
		}
		
		// Draw annotation
		if(pickedNode != null && pickedNode.getNumDescription() > 0) {
			String line = pickedNode.getDescription(0);
			for(int i = 1; i < pickedNode.getNumDescription(); i++) 
				line += " " + pickedNode.getDescription(i);
			glu2.gluUnProject(0.0, 0.0, 0.0, modelview, projection, viewport, p1);
			gl2.glColor3d(0.7, 0.0, 0.0);
			writeOneString(p1.get(0), p1.get(1), line);
		}
		
		zmax = maxZ;
		zmin = minZ;
		//System.out.println("Max : " +zmax);
		//System.out.println("Min : " +zmin);
	}	
	
	void drawOneNode(Node node, double size) {
		int NUMV = 10;
		double x = node.getX();
		double y = node.getY();		
		double z = node.getZ();		

		
		gl2.glPushMatrix();
		gl2.glTranslated(x, y, z);
		glut.glutSolidSphere(size, NUMV, NUMV);
		gl2.glPopMatrix();
		
		glu2.gluProject(x, y, z, modelview, projection, viewport, p1);
		double px = p1.get(0);
		double py = p1.get(1);
		double pz = p1.get(2);
		//System.out.println("     " + x + "," + y);
		//System.out.println("***" + px + "," + py);
		node.setPPosition(px, py, pz);
	}
	
	
	
	
	public Object pick(int cx, int cy) {
		double PICK_DIST = 20.0;
		
		if(graph == null) return null;
		pickedNode = null;
		double dist = 1.0e+30;
		cy = viewport.get(3) - cy + 1;
		
		for(int i = 0; i < graph.nodes.size(); i++) {
			Node node = (Node)graph.nodes.get(i);
			double x = node.getX();
			double y = node.getY();
			glu2.gluProject(x, y, 0.0, modelview, projection, viewport, p1);
			double xx = p1.get(0);
			double yy = p1.get(1);
			double dd = (cx - xx) * (cx - xx) + (cy - yy) * (cy - yy);
			if(dd < PICK_DIST && dd < dist) {
				dist = dd;    pickedNode = node;
			}
		}
		
		return (Object)pickedNode;
		
	}
	
	
	void writeOneString(double x, double y, String word) {
		gl2.glRasterPos3d(x, y, 0.01);
		//glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, word);
	}
	
	void writeText(){
    //書き出し
    String file_name = null;
    
    try{
    	  File file = new File("test.txt");
    	  FileWriter filewriter = new FileWriter(file);
    	  
    for(int i = 0; i < graph.nodes.size(); i++) {
		Node node = (Node)graph.nodes.get(i);
		
		int colorId = node.getColorId();
		double px = node.getPX();
		double py = node.getPY();
		filewriter.write("canvas.addItem("+ colorId + ", " + px + ", " + py + " , w, h);\r\n");//BubbleSets用
		
		}
    

    	filewriter.close();
		}catch(IOException e){
			System.out.println(e);
		}
	}
	

	
	void saveImage() {
		// RGBなら3, RGBAなら4
        int channelNum = 4;
        int allocSize = windowWidth * windowHeight * channelNum;
        ByteBuffer byteBuffer = ByteBuffer.allocate(allocSize);
        //gl2.glFlush();
        // 読み取るOpneGLのバッファを指定 GL_FRONT:フロントバッファ　GL_BACK:バックバッファ
        gl2.glReadBuffer( GL2.GL_BACK );
 
        // OpenGLで画面に描画されている内容をバッファに格納
        gl2.glReadPixels(0,             // 読み取る領域の左下隅のx座標
                0,                      // 読み取る領域の左下隅のy座標
                windowWidth,             // 読み取る領域の幅
                windowHeight,            // 読み取る領域の高さ
                GL2.GL_BGRA,            // 取得したい色情報の形式
                GL2.GL_UNSIGNED_BYTE,   // 読み取ったデータを保存する配列の型
                (Buffer) byteBuffer     // ビットマップのピクセルデータ（実際にはバイト配列）へのポインタ
        );
      
        // glReadBufferで取得したデータ(ByteBuffer)をDataBufferに変換する
        byte[] buff = byteBuffer.array();
    	BufferedImage imageBuffer =
				new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
    	
    	for(int y = 0; y < windowHeight; y++){
    		for(int x = 0; x < windowWidth; x++){
    			
    			int offset = windowWidth * (windowHeight - y - 1) * channelNum;
    			// R
    			int rr = (int)buff[x * channelNum + offset + 2];
    			if(rr < 0) rr += 256;
    			// G
    			int gg = (int)buff[x * channelNum + offset + 1];
    			if(gg < 0) gg += 256;
    			// B
    			int bb = (int)buff[x * channelNum + offset + 0];
    			if(bb < 0) bb += 256;
    			
    			Color color = new Color(rr, gg, bb);
    			int value = color.getRGB();
    			imageBuffer.setRGB(x, y, value);
            }
        }
        
    	String filename = "tmp" + Integer.toString(savemode) + ".png";
        try {
            ImageIO.write(imageBuffer, "png", new File(filename));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        writeText();
        
	}

	
	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub

	}

}