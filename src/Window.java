import java.awt.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.*;

public class Window{

	
	public static class Drawer extends JPanel{
		public ArrayList<Integer> Controls;
		private static final long serialVersionUID = -5930096611050347846L;
		private ArrayList<Structures.Tri> World;
		Dimension screen;
		JFrame win;
		boolean fill;
		boolean drawWireframe;
		boolean paused = false;
		boolean AAlines;
		Color bgcolor;
		ArrayList<Structures.Vertex> lightSources;
		float[] depthBuffer = new float[this.getWidth() * this.getHeight()];
		
		public Drawer(ArrayList<Structures.Vertex> lightSources,boolean fill,boolean drawWireframe, boolean AAlines, Color bgcolor) {
			this.Controls = new ArrayList<Integer>();
			this.win = new JFrame("3D Engine");
			this.win.setSize(800,800);
			Dimension screen = this.win.getSize();
			//Dimension Gscreen = Toolkit.getDefaultToolkit().getScreenSize();
			this.win.setLocationRelativeTo(null);
			this.screen = screen;
			this.win.setVisible(true);
			this.win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.win.add(this);
			this.Redraw(this.World);
			this.fill = fill;
			this.lightSources = lightSources;
			this.drawWireframe = drawWireframe;
			this.bgcolor = bgcolor;
			this.paused = false;
			this.AAlines = AAlines;

			this.win.addKeyListener(new KeyListener(){
				public void keyTyped(KeyEvent arg0){
				}
				public void keyPressed(KeyEvent arg1){
					if (!Controls.contains(arg1.getKeyCode()))
					Controls.add(arg1.getKeyCode());
				}
				public void keyReleased(KeyEvent arg2){
					Controls.remove(Controls.indexOf(arg2.getKeyCode()));
				}
			});
		}
		
		
		public void paint(Graphics g, ArrayList<Structures.Tri> Mesh) {
			//Transparent Mouse
			BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
					cursorImg, new Point(0, 0), "blank cursor");
			this.win.getContentPane().setCursor(blankCursor);
			
			//Clear depthBuffer
			//for(int i=0; i < this.getWidth() * this.getHeight(); i++){this.depthBuffer[i] = 0f;}

			super.paintComponent(g);
			this.setBackground(bgcolor);
			Graphics2D g2 = (Graphics2D) g;
			if (this.AAlines) g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setStroke(new BasicStroke(1.0f));

			for(Structures.Tri triangle:Mesh){

				if (this.fill) g2.setColor(Color.BLACK);	
				else g2.setColor(Color.WHITE);

				int[] XCoords = {(int)triangle.a.x,(int)triangle.b.x,(int)triangle.c.x};
				int[] YCoords = {(int)triangle.a.y,(int)triangle.b.y,(int)triangle.c.y};
				Polygon tri = new Polygon(XCoords,YCoords,3);
				if (this.drawWireframe) {
					g2.draw(new Line2D.Float(XCoords[0],YCoords[0],XCoords[1],YCoords[1]));
					g2.draw(new Line2D.Float(XCoords[1],YCoords[1],XCoords[2],YCoords[2]));
					g2.draw(new Line2D.Float(XCoords[2],YCoords[2],XCoords[0],YCoords[0]));
				}
				if(this.fill) { 
					//Texturing in a poor way
					if(triangle.textureIndex != ""){
						BufferedImage texture = null;
						try {
							texture = ImageIO.read(new File(triangle.textureIndex));
						} catch (IOException e) {
						}
						Rectangle ftexture = new Rectangle();
						ftexture.grow(Math.abs((int)((triangle.v.u - triangle.u.u) * texture.getWidth())), Math.abs((int) ((triangle.w.v - triangle.u.v) * texture.getHeight())));
						ftexture.translate((int)triangle.u.u * texture.getWidth(), (int)triangle.u.v * texture.getHeight());
						TexturePaint paint = new TexturePaint(texture, ftexture);
						g2.setPaint(paint);
					}

					Structures.Vertex triNormal = Structures.Vector_Normalize(triangle.Normal);
					float biggestIllumination = 0.0f;
					for(Structures.Vertex light:lightSources) {
						Structures.Vertex RelativeLight = Structures.Vector_Sub(light,triangle.WorldPos);
						Structures.Vertex Zlight = Structures.Vector_Normalize(RelativeLight);
						float Percentage = Structures.Vector_Dot(triNormal,Zlight);
						if(Percentage > biggestIllumination) {
							biggestIllumination = Percentage;
						}	
					}
					int newR = (int) (triangle.color.getRed() * biggestIllumination);
					int newG = (int) (triangle.color.getGreen() * biggestIllumination);
					int newB = (int) (triangle.color.getBlue() * biggestIllumination);
					g2.setColor(new Color(newR,newG,newB));
					g2.fillPolygon(tri);
				}
			}
				
		}

		public void Spaint(Graphics g, ArrayList<int[]> lines) {
			//Transparent Mouse
			BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
					cursorImg, new Point(0, 0), "blank cursor");
			this.win.getContentPane().setCursor(blankCursor);
			
			super.paintComponent(g);
			this.setBackground(bgcolor);
			Graphics2D g2 = (Graphics2D) g;
			if (this.AAlines) g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setStroke(new BasicStroke(1.0f));

			for(int[] lineCoords : lines){

				if (this.fill) g2.setColor(Color.BLACK);	
				else g2.setColor(Color.WHITE);
				g2.drawLine(lineCoords[1], lineCoords[0], lineCoords[2], lineCoords[0]);
			}
				
		}

		public void paintLine(int x1, int y1, int x2, int y2, Color color){
			Graphics g = this.getGraphics();
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(color);
			g2.drawLine(x1, y1, x2, y2);
		}
		
		public void Redraw(ArrayList<Structures.Tri> WorldMesh) {
			this.World = WorldMesh; //Not updating ?!?!?!!?
			//System.out.println(this.World + " " +WorldMesh);
			this.repaint();
			this.screen = this.win.getSize();
		}
	}
	
	
	
	public static Structures.Tri Scale(Structures.Tri tris,Drawer dim,boolean isStretched){
			
		float aspectRatio = (float)dim.getWidth() / (float)dim.getHeight(); 

		tris.a = Structures.Vector_Add(tris.a, new Structures.Vertex(1.0f,1.0f,0.0f));
		tris.b = Structures.Vector_Add(tris.b, new Structures.Vertex(1.0f,1.0f,0.0f));
		tris.c = Structures.Vector_Add(tris.c, new Structures.Vertex(1.0f,1.0f,0.0f));
		    
		tris.a.x *= 0.5f * (float)dim.getHeight();
		tris.a.y *= 0.5f * (float)dim.getHeight();
		tris.b.x *= 0.5f * (float)dim.getHeight();
		tris.b.y *= 0.5f * (float)dim.getHeight();
		tris.c.x *= 0.5f * (float)dim.getHeight();
		tris.c.y *= 0.5f * (float)dim.getHeight();
		  	
		if (isStretched) {
			tris.a.x *= aspectRatio;
		  	tris.b.x *= aspectRatio;
		  	tris.c.x *= aspectRatio;
			}
		return new Structures.Tri(tris.a,tris.b,tris.c);
		}
}

