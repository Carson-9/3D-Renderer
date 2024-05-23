import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class Algs{

    public static ArrayList<Structures.Tri> OBJFile(String file,float Zoff) {
		try {
		Scanner a = new Scanner(new File(file));
		ArrayList<Structures.Vertex> Vertexs = new ArrayList<Structures.Vertex>();
		ArrayList<Structures.Tri> mesh = new ArrayList<Structures.Tri>();
		ArrayList<Structures.Vertex> Normals = new ArrayList<Structures.Vertex>();
		while (a.hasNextLine()) {
			String line = a.nextLine();
			if (line.isEmpty()) {}

			else {
				if (line.indexOf("  ") != -1) {
					line = line.replaceAll("  ", " ");
				}
			
				if (line.charAt(0) == 'v') {
					String[] sub = line.split(" ");
						if (sub[0].equals("v")) {
							Structures.Vertex vert = new Structures.Vertex(0,0,0);
							vert.x = Float.parseFloat(sub[1]);
							vert.y = Float.parseFloat(sub[2]);
							vert.z = Float.parseFloat(sub[3]);
							Vertexs.add(vert);	
						}
						else if (sub[0].equals("vn")){
							Structures.Vertex vert = new Structures.Vertex(0,0,0);
							vert.x = Float.parseFloat(sub[1]);
							vert.y = Float.parseFloat(sub[2]);
							vert.z = Float.parseFloat(sub[3]);
							Normals.add(vert);	
						}
				}	
				if (line.charAt(0) == 'f') {
					String[] sub = line.split(" ");
					if(! sub[1].contains("/")) {
						for(int i =2; i < sub.length-1;i++) {
                            //System.out.println(sub[1]);
							mesh.add(new Structures.Tri(Vertexs.get(Integer.parseInt(sub[1])-1),Vertexs.get(Integer.parseInt(sub[i])-1),Vertexs.get(Integer.parseInt(sub[i+1])-1)));
						}
					}
					else if(sub[1].contains("//")) {
						for(int i =2; i < sub.length-1;i++) {
							mesh.add(new Structures.Tri(Vertexs.get(Integer.parseInt(sub[1].split("//")[0])-1),Vertexs.get(Integer.parseInt(sub[i].split("//")[0])-1),Vertexs.get(Integer.parseInt(sub[i+1].split("//")[0])-1)));
						}
					}
					else{
						for(int i = 2; i < sub.length-1;i++) {
                            Structures.Tri newTri = new Structures.Tri(Vertexs.get(Integer.parseInt(sub[1].split("/")[0])-1),Vertexs.get(Integer.parseInt(sub[i].split("/")[0])-1),Vertexs.get(Integer.parseInt(sub[i+1].split("/")[0])-1));
                            newTri.Normal = Normals.get(Integer.parseInt(sub[i].split("/")[2])-1);
                            mesh.add(newTri);
						}
					}
				}
			}
		}
			return mesh;
		}
		catch(FileNotFoundException e) {
			System.out.println("File : " + file + " not found");
			return new ArrayList<Structures.Tri>();
		}
	}
    
    public static ArrayList<Structures.Tri> mergeSortPainter(ArrayList<Structures.Tri> list1, ArrayList<Structures.Tri> list2, Structures.Vertex camPos){
        int ind1, ind2;
        ind1 = ind2 = 0;
        ArrayList<Structures.Tri> end = new ArrayList<Structures.Tri>();
        while(ind1 < list1.size() && ind2 < list2.size()){
            if (Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(list2.get(ind2).WorldPos,camPos)) > Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(list1.get(ind1).WorldPos,camPos))) {end.add(list2.get(ind2));ind2 ++;}
            else {end.add(list1.get(ind1));ind1 ++;}
        }
        if (ind1 == list1.size()){for(int temp = ind2; temp < list2.size(); temp++){end.add(list2.get(temp));}}
        else {for(int temp = ind1; temp < list1.size(); temp++){end.add(list1.get(temp));}}
        return end;
    }

    public static ArrayList<Structures.Tri> PainterDivisionSort(ArrayList<Structures.Tri> original, Structures.Vertex camPos){
        if (original.size() < 2) return original;
        ArrayList<Structures.Tri> lowerList = new ArrayList<Structures.Tri>(original.subList(0, original.size()>>1));
        ArrayList<Structures.Tri> upperList = new ArrayList<Structures.Tri>(original.subList(original.size()>>1, original.size()));
        return mergeSortPainter(PainterDivisionSort(lowerList, camPos), PainterDivisionSort(upperList, camPos),camPos);
    }

    public static ArrayList<Structures.Tri> GenerateTerrain(int width, int depth, int maxScale, long seed){
        OpenSimplexNoise noise = new OpenSimplexNoise(seed);
        ArrayList<Structures.Tri> terrain = new ArrayList<Structures.Tri>();
        for(int z = 0; z < depth; z++){
            for(int x = 0; x < width; x++){
                terrain.add(new Structures.Tri(new Structures.Vertex(x, ((float)noise.eval(x, z) * maxScale), z), new Structures.Vertex(x, ((float)(noise.eval(x, z+1)) * maxScale), z+1), new Structures.Vertex(x+1, ((float)noise.eval(x+1, z) * maxScale), z)));
                terrain.add(new Structures.Tri(new Structures.Vertex(x, ((float)noise.eval(x, z+1) * maxScale), z+1), new Structures.Vertex(x+1, ((float)noise.eval(x+1, z+1) * maxScale), z+1), new Structures.Vertex(x+1, ((float)noise.eval(x+1, z) * maxScale), z)));
            }
        }
        return terrain;
    }




    public static void paintScreen(ArrayList<Structures.Tri> worldMesh, Window.Drawer windowD) {
        float[][][] screen = new float[windowD.getHeight()][windowD.getWidth()][3];
        //Clear screen buffer image
        for(int y  = 0; y < windowD.getHeight(); y++){
            for(int x  = 0; x < windowD.getWidth(); x++){
                screen[y][x] = new float[]{windowD.bgcolor.getRed(),windowD.bgcolor.getGreen(),windowD.bgcolor.getBlue()};
            }
        }
        //Each occurence of rasterizing will modify screen
        for(Structures.Tri tri : worldMesh){
            screen =  Rasterizing(tri, windowD, "h", false, screen);
        }
    }

    public static float[][][] Rasterizing(Structures.Tri tri, Window.Drawer window, String texturePath, boolean isTextured, float[][][] screen){ 
        if (isTextured){
            BufferedImage img = null;
        try {
            img = ImageIO.read(new File(texturePath));
        } catch (IOException e) {}
        }

        ArrayList<int[]> lineCoords = new ArrayList<int[]>();

        // Sort triangle points by their height, highest (smallest) first. If they are the same, sort by X
        if(tri.a.y > tri.b.y) {Structures.Vertex temp = tri.a; tri.a = tri.b; tri.b = temp;}
        else if(tri.a.y == tri.b.y){ if (tri.a.x > tri.b.x){Structures.Vertex temp = tri.a; tri.a = tri.b; tri.b = temp;}}

        if(tri.a.y > tri.c.y){Structures.Vertex temp = tri.a; tri.a = tri.c; tri.c = temp;}
        else if(tri.a.y == tri.c.y){ if (tri.a.x > tri.c.x){Structures.Vertex temp = tri.a; tri.a = tri.c; tri.c = temp;}}

        if(tri.b.y > tri.c.y){Structures.Vertex temp = tri.b; tri.b = tri.c; tri.c = temp;}
        else if(tri.b.y == tri.c.y){ if (tri.b.x > tri.c.x){Structures.Vertex temp = tri.b; tri.b = tri.c; tri.c = temp;}}

        //slope 1 i.e interpolation
        float dy1 = 0; float dy2 = 0;
        if(tri.b.y - tri.a.y != 0 ){dy1 = ((tri.b.x - tri.a.x) / (tri.b.y - tri.a.y));}
        if(tri.c.y - tri.b.y != 0 ){dy2 = ((tri.c.x - tri.a.x) / (tri.c.y - tri.a.y));}

        for(int yStep = (int)tri.a.y; yStep < tri.b.y; yStep++){

            int newXa = (int)(tri.a.x + (yStep - tri.a.y) * dy1 );
            int newXb = (int)(tri.a.x + (yStep - tri.a.y) * dy2 );
            lineCoords.add(new int[]{yStep, newXa, newXb});
            //window.paintLine(newXa, yStep, newXb, yStep, new Color(25,72,198));
        }

        if(tri.c.y - tri.b.y != 0){dy1 = ((tri.c.x - tri.b.x) / (tri.c.y - tri.b.y));}
        else dy1 = 0;

        for(int yStep = (int)tri.b.y; yStep < tri.c.y; yStep++){

            int newXa = (int)(tri.b.x + (yStep - tri.b.y) * dy1 );
            int newXb = (int)(tri.a.x + (yStep - tri.a.y) * dy2 );
            lineCoords.add(new int[]{yStep, newXa, newXb});
            //window.paintLine(newXa, yStep, newXb, yStep, new Color(25,72,198));
        }

        window.Spaint(window.getGraphics(), lineCoords);
        return screen;

    }



   /* public static void TextureTriangle(Window.Drawer window, Structures.Vertex p1, Structures.Vertex p2, Structures.Vertex p3, Structures.Text2d t1, Structures.Text2d t2, Structures.Text2d t3, String texture){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(texture));
        } catch (IOException e) {}

        if(p2.y < p1.y){
            float temp = p1.y; p1.y = p2.y; p2.y = temp;
            temp = p1.x; p1.x = p2.x; p2.x = temp;
            temp = t1.u; t1.u = t2.u; t2.u = temp;
            temp = t1.v; t1.v = t2.v; t2.v = temp;
            temp = t1.w; t1.w = t2.w; t2.w = temp;
        }

        if(p3.y < p1.y){
            float temp = p1.y; p1.y = p3.y; p3.y = temp;
            temp = p1.x; p1.x = p3.x; p3.x = temp;
            temp = t1.u; t1.u = t3.u; t3.u = temp;
            temp = t1.v; t1.v = t3.v; t3.v = temp;
            temp = t1.w; t1.w = t3.w; t3.w = temp;
        }

        if(p3.y < p2.y){
            float temp = p2.y; p2.y = p3.y; p3.y = temp;
            temp = p2.x; p2.x = p3.x; p3.x = temp;
            temp = t2.u; t2.u = t3.u; t3.u = temp;
            temp = t2.v; t2.v = t3.v; t3.v = temp;
            temp = t2.w; t2.w = t3.w; t3.w = temp;
        }

        int dy1 = (int)(p2.y - p1.y);
        int dx1 = (int)(p2.x - p1.x);
        float du1 = t2.u - t1.u;
        float dv1 = t2.v - t1.v;
        float dw1 = t2.w - t1.w;

        int dy2 = (int)(p3.y - p1.y);
        int dx2 = (int)(p3.x - p1.x);
        float du2 = t3.u - t1.u;
        float dv2 = t3.v - t1.v;
        float dw2 = t3.w - t1.w;

        float tex_u, tex_v, tex_w;
        float daxStep = 0; float dbxStep = 0;
        float du1Step = 0; float du2Step = 0;
        float dv1Step = 0; float dv2Step = 0;
        float dw1Step = 0; float dw2Step = 0;

        if (dy1 != 0){ 
            daxStep = dx1 / (float)Math.abs(dy1);
            du1Step = du1 / (float)Math.abs(dy1);
            dv1Step = dv1 / (float)Math.abs(dy1);
            dw1Step = dw1 / (float)Math.abs(dy1);
        }

        if (dy2 != 0){
            dbxStep = dx2 / (float)Math.abs(dy2);
            du2Step = du2 / (float)Math.abs(dy2);
            dv2Step = dv2 / (float)Math.abs(dy2);
            dw2Step = dw2 / (float)Math.abs(dy2);
        }

        if (dy1 != 0){

            for(int i = (int)p1.y; i <= p2.y ;i++){
                int ax = (int)(p1.x + (float)(i - p1.y) * daxStep);
                int bx = (int)(p1.x + (float)(i - p1.y) * dbxStep);
                float texSu = t1.u + (i - p1.y) * du1Step;
                float texSv = t1.v + (i - p1.y) * dv1Step;
                float texSw = t1.w + (i - p1.y) * dw1Step;

                float texEu = t1.u + (i - p1.y) * du2Step;
                float texEv = t1.v + (i - p1.y) * dv2Step;
                float texEw = t1.w + (i - p1.y) * dw2Step;

                if (ax > bx){
                    float temp = ax; ax = bx; bx = (int)temp;
                    temp = texSu; texSu = texEu; texEu = temp;
                    temp = texSv; texSv = texEv; texEv = temp;
                    temp = texSw; texSw = texEw; texEw = temp;
                } 

                tex_u = texSu;
                tex_v = texSv;
                tex_w = texSw;

                float tstep = 1f/ (ax-bx);
                float t = 0f;

                for(int j = ax; j < bx; j++){
                    tex_u = (1f - t) * texSu + t * texEu;
                    tex_v = (1f - t) * texSv + t * texEv;
                    tex_w = (1f - t) * texSw + t * texEw;

                    if(tex_w > window.depthBuffer[j * window.getWidth() + i]){
                        int color = img.getRGB(i, j);
                        int red = color & (0x00ff0000)>>16;
                        int green = color & (0x0000ff00)>>8;
                        int blue = color & (0x000000ff);
                        window.depthBuffer[j*window.getWidth()+i] = tex_w;
                        window.paintPixel(j, i, new Color(red, green, blue));
                    } 

                    t += tstep;

                }
            }


        }

        dy1 = (int)(p3.y - p2.y);
        dx1 = (int)(p3.x - p2.x);
        du1 = t3.u - t2.u;
        dv1 = t3.v - t2.v;
        dw1 = t3.w - t2.w;
        
        if (dy1 != 0){ 
            daxStep = dx1 / (float)Math.abs(dy1);
            du1Step = du1 / (float)Math.abs(dy1);
            dv1Step = dv1 / (float)Math.abs(dy1);
            dw1Step = dw1 / (float)Math.abs(dy1);
        }

        if (dy2 != 0){
            dbxStep = dx2 / (float)Math.abs(dy2);
            du2Step = du2 / (float)Math.abs(dy2);
            dv2Step = dv2 / (float)Math.abs(dy2);
            dw2Step = dw2 / (float)Math.abs(dy2);
        }

        if (dy1 != 0){

            for(int i = (int)p2.y; i <= p3.y ;i++){
                int ax = (int)(p2.x + (float)(i - p2.y) * daxStep);
                int bx = (int)(p1.x + (float)(i - p1.y) * dbxStep);
                float texSu = t2.u + (i - p2.y) * du1Step;
                float texSv = t2.v + (i - p2.y) * dv1Step;
                float texSw = t2.w + (i - p2.y) * dw1Step;

                float texEu = t1.u + (i - p1.y) * du2Step;
                float texEv = t1.v + (i - p1.y) * dv2Step;
                float texEw = t1.w + (i - p1.y) * dw2Step;

                if (ax > bx){
                    float temp = ax; ax = bx; bx = (int)temp;
                    temp = texSu; texSu = texEu; texEu = temp;
                    temp = texSv; texSv = texEv; texEv = temp;
                    temp = texSw; texSw = texEw; texEw = temp;
                } 

                tex_u = texSu;
                tex_v = texSv;
                tex_w = texSw;

                float tstep = 1f/ (ax-bx);
                float t = 0f;

                for(int j = ax; j < bx; j++){
                    tex_u = (1f - t) * texSu + t * texEu;
                    tex_v = (1f - t) * texSv + t * texEv;
                    tex_w = (1f - t) * texSw + t * texEw;

                    if(tex_w > window.depthBuffer[j * window.getWidth() + i]){
                        int color = img.getRGB(i, j);
                        int red = color & (0x00ff0000)>>16;
                        int green = color & (0x0000ff00)>>8;
                        int blue = color & (0x000000ff);
                        window.depthBuffer[j*window.getWidth()+i] = tex_w;
                        window.paintPixel(j, i, new Color(red, green, blue));
                    }

                    t += tstep;

                }
            }


        }


    }*/
}