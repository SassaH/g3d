package ocha.itolab.koala.core.layout;

import java.util.*;
import java.io.*;
import ocha.itolab.koala.core.data.*;

public class InvokeMDSJ {
	//static String javapath = "\"c:/Program Files/Java/jdk1.8.0_45/bin/java.exe\"";
	//static String javapath = "\"C:/Program Files/Java/jdk1.8.0_131/bin/java.exe\"";
	static String javapath = "/Library/Java/JavaVirtualMachines/jdk-10.jdk/Contents/Home/bin/java";
	//static String javapath = "java.exe";
	static String mdsjpath = "../../lib/mdsj.jar";
	static String infile = "mdsjin.txt";
	static String outfile = "mdsjout.txt";
	static String command = javapath + " -jar " + mdsjpath + " -d3 " + infile + " " + outfile;
	static BufferedReader reader;
	static BufferedWriter writer;
	static Graph graph;
	static double xmin, xmax, ymin, ymax, zmin, zmax;
	
	public static void invoke(Graph g) {
		graph = g;
		writeInputData();
		
		Process p = null;
		try{
			Runtime runtime = Runtime.getRuntime();
			p = runtime.exec(command);
			int ret = p.waitFor();
			System.out.println("# command=" + ret + " " + command);
			p.destroy();
		} catch(Exception e){
			System.out.println(e);
		}

		readOutputFile();
	}
	

	static void writeInputData() {
		openWriter(infile);
		for(int i = 0; i < graph.nodes.size(); i++) {
			Node node1 = graph.nodes.get(i);
			String line = Double.toString(node1.getDisSim2(0));
			for(int j = 1; j < graph.nodes.size(); j++) {
				double dis = node1.getDisSim2(j);
				line += (" " + Double.toString(dis));
			}
			println(line);
		}
		closeWriter();
	}
	
	
	static void readOutputFile() {	
		
		xmin = ymin = zmin =  1.0e+30;
		xmax = ymax = zmax = -1.0e+30;
		openReader(outfile);
		try {
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				StringTokenizer token = new StringTokenizer(line);
				double x = Double.parseDouble(token.nextToken());
				double y = Double.parseDouble(token.nextToken());
				double z = Double.parseDouble(token.nextToken());
				xmin = (xmin > x) ? x : xmin;
				ymin = (ymin > y) ? y : ymin;
				zmin = (zmin > z) ? z : zmin;
				xmax = (xmax < x) ? x : xmax;
				ymax = (ymax < y) ? y : ymax;
				zmax = (zmax < z) ? z : zmax;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		closeReader();
		
		
		openReader(outfile);
		try {
			int count = 0;
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				StringTokenizer token = new StringTokenizer(line);
				Node node = graph.nodes.get(count);
				double x = Double.parseDouble(token.nextToken());
				double y = Double.parseDouble(token.nextToken());
				double z = Double.parseDouble(token.nextToken());
				x = x / (xmax - xmin) + xmin;
				y = y / (ymax - ymin) + ymin;
				z = z / (zmax - zmin) + zmin;
				node.setPosition(x, y, z);
				count++;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		closeReader();
		
		for(int i = 0; i < 10; i++)
			adjustPosition();
	}
	
	
	static void adjustPosition() {
		double RATIO = 0.005;
		double MOVE = 1.0;
		
		for(int i = 0; i < graph.nodes.size(); i++) {
			Node node = graph.nodes.get(i);
			Node node2 = null;
			double dmin = 1.0e+30;
			for(int j = 0; j < graph.nodes.size(); j++) {
				if(i == j) continue;
				Node n2 = graph.nodes.get(j);
				double dx = node.getX() - n2.getX();
				double dy = node.getY() - n2.getY();
				double dz = node.getZ() - n2.getZ();
				double d = dx * dx + dy * dy + dz * dz;
				if(d < dmin) {
					dmin = d;   node2 = n2;
				}
			}
			if(dmin < (xmax - xmin) * RATIO) {
				double x2 = node.getX() * (MOVE + 1.0) - node2.getX() * MOVE;
				double y2 = node.getY() * (MOVE + 1.0) - node2.getY() * MOVE;
				double z2 = node.getZ() * (MOVE + 1.0) - node2.getZ() * MOVE;
				node.setPosition(x2, y2, z2);
			}
			
		}
		
	}
	
	
	
	
	static void openReader(String f) {
	
		try {
			File file = new File(f);
			reader = new BufferedReader(new FileReader(file));
			reader.ready();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	
	static void closeReader() {
		try {
			reader.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	
	static BufferedWriter openWriter(String filename) {	
		try {
			 writer = new BufferedWriter(
			    		new FileWriter(new File(filename)));
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
		return writer;
	}
	
	
	static void closeWriter() {
		
		try {
			writer.close();
		} catch (Exception e) {
			System.err.println(e);
			return;
		}
	}
	

	static void println(String word) {
		try {
			writer.write(word, 0, word.length());
			writer.flush();
			writer.newLine();
		} catch (Exception e) {
			System.err.println(e);
			return;
		}
	}	
	
}
