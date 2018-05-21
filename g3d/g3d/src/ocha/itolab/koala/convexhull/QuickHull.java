package ocha.itolab.koala.convexhull;

//This is a java program to find a points in convex hull using quick hull method
//source: Alexander Hrishov's website
//URL: http://www.ahristov.com/tutorial/geometry-games/convex-hull.html
 
import java.util.ArrayList;
import java.util.Scanner;
import java.awt.geom.*;
import java.awt.geom.Point2D.Double;
import javafx.geometry.Point3D;


import com.jogamp.nativewindow.util.Point;
//import com.sun.javafx.geom.Point2D;

import javafx.geometry.Point3D;
import ocha.itolab.koala.core.data.*;
import quickhull3d.Point3d;
 
public class QuickHull
{
    public ArrayList<Point3D> quickHull(ArrayList<Point3D> points)
    {
        ArrayList<Point3D> convexHull = new ArrayList<Point3D>();
        if (points.size() < 3)
            return (ArrayList) points.clone();
 
        int minPoint = -1, maxPoint = -1;
        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;
        for (int i = 0; i < points.size(); i++)
        {
            if (points.get(i).getX() < minX)
            {
                minX = points.get(i).getX();
                minPoint = i;
            }
            if (points.get(i).getX() > maxX)
            {
                maxX = points.get(i).getX();
                maxPoint = i;
            }
        }
        Point3D A = points.get(minPoint);
        Point3D B = points.get(maxPoint);
        convexHull.add(A);
        convexHull.add(B);
        points.remove(A);
        points.remove(B);
 
        ArrayList<Point3D> leftSet = new ArrayList<Point3D>();
        ArrayList<Point3D> rightSet = new ArrayList<Point3D>();
 
        for (int i = 0; i < points.size(); i++)
        {
            Point3D p = points.get(i);
            if (pointLocation(A, B, p) == -1)
                leftSet.add(p);
            else if (pointLocation(A, B, p) == 1)
                rightSet.add(p);
        }
        hullSet(A, B, rightSet, convexHull);
        hullSet(B, A, leftSet, convexHull);
 
        return convexHull;
    }
 
    public double distance(Point3D A, Point3D B, Point3D C)
    {
        double ABx = B.getX() - A.getX();
        double ABy = B.getY() - A.getY();
        double num = ABx * (A.getY() - C.getY()) - ABy * (A.getX() - C.getX());
        if (num < 0)
            num = -num;
        return num;
    }
 
    public void hullSet(Point3D A, Point3D B, ArrayList<Point3D> set,
            ArrayList<Point3D> hull)
    {
        int insertPosition = hull.indexOf(B);
        if (set.size() == 0)
            return;
        if (set.size() == 1)
        {
            Point3D p = set.get(0);
            set.remove(p);
            hull.add(insertPosition, p);
            return;
        }
        double dist = Integer.MIN_VALUE;
        int furthestPoint = -1;
        for (int i = 0; i < set.size(); i++)
        {
            Point3D p = set.get(i);
            double distance = distance(A, B, p);
            if (distance > dist)
            {
                dist = distance;
                furthestPoint = i;
            }
        }
        Point3D P = set.get(furthestPoint);
        set.remove(furthestPoint);
        hull.add(insertPosition, P);
 
        // Determine who's to the left of AP
        ArrayList<Point3D> leftSetAP = new ArrayList<Point3D>();
        for (int i = 0; i < set.size(); i++)
        {
            Point3D M = set.get(i);
            if (pointLocation(A, P, M) == 1)
            {
                leftSetAP.add(M);
            }
        }
 
        // Determine who's to the left of PB
        ArrayList<Point3D> leftSetPB = new ArrayList<Point3D>();
        for (int i = 0; i < set.size(); i++)
        {
            Point3D M = set.get(i);
            if (pointLocation(P, B, M) == 1)
            {
                leftSetPB.add(M);
            }
        }
        hullSet(A, P, leftSetAP, hull);
        hullSet(P, B, leftSetPB, hull);
 
    }
 
    public int pointLocation(Point3D A, Point3D B, Point3D P)
    {
        double cp1 = (B.getX() - A.getX()) * (P.getY() - A.getY()) - (B.getY() - A.getY()) * (P.getX() - A.getX());
        if (cp1 > 0)
            return 1;
        else if (cp1 == 0)
            return 0;
        else
            return -1;
    }
    
    //引数と同じ色のノードを抽出してQuickHullできる配列にする
    public ArrayList<Point3D> eachColorQuickHull(ArrayList<Node> nodes, int colorId)
    {	
    	int l = nodes.size();
    	int count = 0;
        ArrayList<Point3D> convexHull = new ArrayList<Point3D>();
        
        for(int i=0; i < l; i++){
        	Node node = nodes.get(i);
        	//if(nodes.get(i).getColorId() == colorId){
        	if(nodes.get(i).getValue(colorId) != 0 ){
        		//double px = node.getX();
        		//double py = node.getY();
        		double px = node.getPX();
        		double py = node.getPY();
        		double pz = node.getPZ();
        		//Point2D.Double e = new Point2D.Double(px, py, pz);
        		Point3D e = new Point3D(px, py, pz);
        		convexHull.add(e);
        		count ++;
        	}
        }
        
        return convexHull;
    }
    
   /* public Point3d[] eachColorQuickHull3D(ArrayList<Node> nodes, int colorId)
    {	
    	int l = nodes.size();
    	int count = 0;
        //ArrayList<Point3D> convexHull = new ArrayList<Point3D>();
    	Point3d convexHull[] = null;
        
        for(int i=0; i < l; i++){
        	Node node = nodes.get(i);
        	if(nodes.get(i).getColorId() == colorId){

        		//double px = node.getX();
        		//double py = node.getY();
        		double pz = node.getZ();
        		double px = node.getPX();
        		double py = node.getPY();        	
        	 	Point3d e = new Point3d(px, py, pz);
        		convexHull[count] = e;
        		//convexHull[i] = e;
        		count ++;
        	}
        }
        return convexHull;
    }*/


    
 /*   public static void main(String args[])
    {
        System.out.println("Quick Hull Test");
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the number of points");
        int N = sc.nextInt();
 
        ArrayList<Point> points = new ArrayList<Point>();
        System.out.println("Enter the coordinates of each points: <x> <y>");
        for (int i = 0; i < N; i++)
        {
            int x = sc.nextInt();
            int y = sc.nextInt();
            Point e = new Point(x, y);
            points.add(i, e);	//eをi番目に挿入
        }
 
        QuickHull qh = new QuickHull();
        ArrayList<Point> p = qh.quickHull(points);
        System.out
                .println("The points in the Convex hull using Quick Hull are: ");
        for (int i = 0; i < p.size(); i++)
            System.out.println("(" + p.get(i).getX() + ", " + p.get(i).getY() + ")");
        sc.close();
    }*/
}