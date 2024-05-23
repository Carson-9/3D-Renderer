import java.time.Duration;
import java.time.Instant;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;



public class Renderer{
	public static final float PI = 3.1415926535f;
	protected static float elapsedTime;
	protected static Structures.Vertex Cam;

	public static void main(String[] args) throws InterruptedException {
		
		// INIT Camera, DeltaTime, Lights, Screen, Controls, Mesh to draw.
		
		Duration deltaTime = Duration.ZERO;
		Structures.Vertex origin = new Structures.Vertex(0,0,0);
		
		//float elapsedTime = 0.01f;

		ArrayList<Structures.Vertex> lightSources = new ArrayList<Structures.Vertex>();
		lightSources.add(new Structures.Vertex(0,-1,0));
		ArrayList<Object> World = new ArrayList<Object>();
		
		ArrayList<Structures.Tri> Mesh = Algs.OBJFile(System.getProperty("user.dir") + "\\Desktop\\3D\\3dRenderer\\objects\\lamp.txt",0);
		Mesh = MeshUtils.meshColoring(Mesh,255,255,255);
		Object cube = new Object(Mesh,new Structures.Vertex(0f, 0f, 5f),new float[]{PI,0,0});
		World.add(cube);

		/*ArrayList<Structures.Tri> Mesh2 = Algs.GenerateTerrain(25, 25, 1, 8124615231546L);
		Mesh2 = MeshUtils.meshColoring(Mesh2,124,252,0);
		Object terrain = new Object(Mesh2, origin, new float[]{PI,PI,0});
		World.add(terrain);*/

		/*Random r = new Random();
		for(Structures.Tri tri : terrain.mesh){
			tri.textureIndex = System.getProperty("user.dir") + "/textures/BrickSmallBrown0463_7_download600.jpg";
			tri.u = new Structures.Text2d((float)r.nextDouble(), (float)r.nextDouble()); tri.u.w = 1f;
			tri.v = new Structures.Text2d((float)r.nextDouble(), (float)r.nextDouble()); tri.v.w = 1f;
			tri.w = new Structures.Text2d((float)r.nextDouble(), (float)r.nextDouble()); tri.w.w = 1f;
		}*/

		//teapot.PerFrameRotation = new float[]{0.001f,0.001f,0.001f};
		
		// Params : ArrayList of light sources , is filled, draw Wireframe, AntiAliased (smooth) lines,BgColor
		Window.Drawer Window = new Window.Drawer(lightSources,true,false,false,new Color(63, 146, 171)); 
		Cam cam = new Cam(origin,new float[]{0,0,0},Window,World,true);

		cam.pos = origin;

		while(true) {

			Instant beginTime = Instant.now();
			cam.render();
			for(Object obj:World){

				obj.update();
				obj.ParseControls(Window.Controls);

			}

			cam.CamParseControls(Window.Controls);
			cam.rotation[2] = 0;
			cam.speed = elapsedTime*1000;
			Window.win.setTitle("3D Renderer | " + (int)(1/elapsedTime) + " FPS" + " | Cam Pos : " + cam.pos.x + ", " + cam.pos.y + ", " + cam.pos.z + " | " + "Rotation : " + cam.rotation[0] + ", " + cam.rotation[1] + ", " + cam.rotation[2]);
			TimeUnit.NANOSECONDS.sleep(deltaTime.getNano()/5);
			deltaTime = Duration.between(beginTime, Instant.now());
			elapsedTime = (float)(deltaTime.getNano()*0.000000001);
			}

			//Unstable FPS : 500-41
	}

}