import java.awt.Color;
import java.util.ArrayList;


public class MeshUtils extends Structures{

	public static ArrayList<Tri> meshFusion(ArrayList<ArrayList<Tri>> meshList){
		ArrayList<Tri> finalMesh = new ArrayList<Tri>();
		for (ArrayList<Tri> mesh : meshList) {
			for (Tri Tri : mesh) {
				finalMesh.add(Tri);
				}
		}
		return finalMesh;
	}
	
	public static ArrayList<Tri> meshOffset(ArrayList<Tri> mesh,float xOffset,float yOffset,float zOffset){
		ArrayList<Tri> newMesh = new ArrayList<Tri>();
		Vertex offset = new Vertex(xOffset,yOffset,zOffset);
		for (Tri Tri : mesh) {
			Tri.a = Vector_Add(Tri.a,offset);
			Tri.b = Vector_Add(Tri.b,offset);
			Tri.c = Vector_Add(Tri.c,offset);
			newMesh.add(Tri);
		}
		return newMesh;
	}
	
	public static ArrayList<Tri> meshColoring (ArrayList<Tri> mesh,int red,int green,int blue){
		Color color = new Color(red,green,blue);
		ArrayList<Tri> fMesh = new ArrayList<Tri>();
		for(Tri Tri : mesh) {
			Tri.color = color;
			fMesh.add(Tri);
		}
		return fMesh;
	}
	
	/*public static ArrayList<Tri> generateTerrain(int width, int height){
		float leftCoords = width/2;
		float depth = -height/2;
		ArrayList<Tri> Mesh = new ArrayList<Tri>();
		for(depth,  depth < height/2, depth+1) {
			for(int x = -width/2, x < width/2, x+1) {
				Mesh.add(new Tri(new Vertex()))
			}
		}
	}*/

	public static ArrayList<Tri> Subdivide(ArrayList<Tri> mesh){
		ArrayList<Tri> newMesh = new ArrayList<Tri>();
		for(Tri Tri : mesh){
			Vertex midab = Vector_Midpoint(Tri.a,Tri.b);
			Vertex midac = Vector_Midpoint(Tri.a,Tri.c);
			Vertex midbc = Vector_Midpoint(Tri.b,Tri.c);
			newMesh.add(new Tri(Tri.a,midab,midac));
			newMesh.add(new Tri(midab,Tri.b,midbc));
			newMesh.add(new Tri(midac,midbc,Tri.c));
			newMesh.add(new Tri(midab,midbc,midac));
		}
		return newMesh;
	}
	public static ArrayList<Tri> MeshInvertNormal(ArrayList<Tri> mesh){
		for (Tri Tri : mesh) {
			Tri.Normal = Vector_Mult(Tri.Normal,-1f);
		}
		return mesh;
	}
}
