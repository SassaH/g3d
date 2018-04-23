package ocha.itolab.koala.core.data;

import java.util.*;

public class Node {
	int id;
	String description[];
	int colorId;
	double vector[];
	double dissim1[];
	double dissim2[];
	double x, y, z, px, py, pz;
	int connected[];
	int connecting[];
	ArrayList connectedEdge = new ArrayList();
	ArrayList connectingEdge = new ArrayList();
	
	public void setPosition(double x, double y, double z) {
		this.x = x;    this.y = y;    this.z = z;
	}
	
	public void setPPosition(double x, double y, double z) {
		this.px = x;    this.py = y;	this.pz = z;
	}
	

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public double getPX() {
		return px;
	}
	
	public double getPY() {
		return py;
	}
	
	public double getPZ() {
		return pz;
	}
	public int getId() {
		return id;
	}
	
	public int getColorId() {
		return colorId;
	}
	
	
	public double getValue(int i) {
		return vector[i];
	}
	
	public double getDisSim1(int i) {
		return dissim1[i];
	}
	
	public double getDisSim2(int i) {
		return dissim2[i];
	}
	
	public int getNumDescription() {
		return description.length;
	}
	
	public String getDescription(int id) {
		return description[id];
	}
	
	
	public int getNumConnectedEdge() {
		return connectedEdge.size();
	}
	
	public Edge getConnectedEdge(int i) {
		return (Edge)connectedEdge.get(i);
	}
	
	public int getNumConnectingEdge() {
		return connectingEdge.size();
	}
	
	public Edge getConnectingEdge(int i) {
		return (Edge)connectingEdge.get(i);
	}
	
}
