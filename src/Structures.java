import java.awt.Color;
import java.awt.Dimension;

public class Structures {

	public static class Vertex{
		float x = 0f; float y = 0f; float z = 0f;
		public Vertex(float x,float y,float z) {
			this.x = x; this.y = y; this.z = z;
		}
		public void list() {
			System.out.println(this.x + " " + this.y + " " + this.z +"\n");
		}
	}

	public static class Text2d{
		float u = 0f; float v = 0f; float w = 0f;
		public Text2d(float u,float v) {
			this.u = u; this.v = v;
		}
		public void list() {
			System.out.println(this.u + " " + this.v + " " + this.w +"\n");
		}
	}
	
	public static Vertex Vector_Add(Vertex a, Vertex b) {
		Vertex Add = new Vertex(0,0,0);
		Add.x = a.x + b.x; 
		Add.y = a.y + b.y;
		Add.z = a.z + b.z;
		return Add;
	}

	public static Text2d Text_Add(Text2d a, Text2d b) {
		Text2d Add = new Text2d(0,0);
		Add.u = a.u + b.u; 
		Add.v = a.v + b.v;
		return Add;
	}
	
	public static Text2d Text_Sub(Text2d b, Text2d a) {
		Text2d Sub = new Text2d(0,0);
		Sub.u = b.u - a.u; 
		Sub.v = b.v - a.v;
		return Sub;
	}

	public static Vertex Vector_Sub(Vertex b, Vertex a) {
		Vertex Sub = new Vertex(0,0,0);
		Sub.x = b.x - a.x; 
		Sub.y = b.y - a.y;
		Sub.z = b.z - a.z;
		return Sub;
	}
	
	public static Vertex Vector_Mult(Vertex a,float z) {
		return new Vertex( a.x*z, a.y*z, a.z*z);
	}

	public static Text2d Vector_Mult(Text2d a,float z) {
		return new Text2d(a.u*z, a.v*z);
	}
	
	public static Vertex Vector_Mult(Vertex a,double z) {
		float nx = (float) (a.x*z); float ny = (float) (a.y*z);float nz = (float) (a.z*z);
		return new Vertex(nx,ny,nz);
	}
	
	public static Vertex Vector_Normalize(Vertex a) {
		float scale = (float)Math.sqrt((a.x*a.x + a.y*a.y + a.z*a.z));
		if (scale != 0){
			a.x /= scale; a.y /= scale; a.z /= scale;
			return a;
		}
		else{
			a.x /= scale+0.01; a.y /= scale+0.01; a.z /= scale+0.01;
			return a;
		}
	}
	
	public static float Vector_Dot(Vertex a, Vertex b) {
		return a.x*b.x + a.y*b.y + a.z*b.z;
	}
	
	public static Vertex Vector_Cross(Vertex a, Vertex b) {
		Vertex Cross = new Vertex(0,0,0);
		Cross.x = a.y*b.z - a.z*b.y; 
		Cross.y = a.z*b.x - a.x*b.z;
		Cross.z = a.x*b.y - a.y*b.x;
		return Cross;
	}

	public static Vertex Vector_Midpoint(Vertex a, Vertex b){
		Vertex Midpoint = new Vertex(0,0,0);
		Midpoint.x = (a.x+b.x)/2;
		Midpoint.y = (a.y+b.y)/2;
		Midpoint.z = (a.z+b.z)/2;
		return Midpoint;
	}
	
	public static float Vector_Magnitude(Vertex a) {
		return (float)Math.sqrt(a.x*a.x+a.y*a.y+a.z*a.z);
	}
	
	public static float Vector_MagnitudeSquared(Vertex a) {
		return (float)(a.x*a.x+a.y*a.y+a.z*a.z);
	}

	
	public static class Tri{
		Vertex a = new Vertex(0,0,0); Vertex b = new Vertex(0,0,0); Vertex c = new Vertex(0,0,0);
		Text2d u = new Text2d(0,0); Text2d v = new Text2d(0,0); Text2d w = new Text2d(0,0);
		String textureIndex = "";
		Vertex WorldPos = new Vertex(0,0,0);
		Vertex Normal;
		Color color = new Color(255,255,255);
		public Tri(Vertex a, Vertex b, Vertex c) {
			this.a = a; this.b = b; this.c = c;
			Normal = Vector_Cross(Vector_Sub(b,a),Vector_Sub(c,a));
			this.WorldPos = new Vertex((this.a.x + this.b.x + this.c.x) / 3, (this.a.y + this.b.y + this.c.y) / 3, (this.a.z + this.b.z + this.c.z) / 3);
		}
		public void setColor(Color color) {
			this.color = color;
		}
		
		public void list() {
			System.out.println(this.a.x + " " + this.a.y + " " + this.a.z +"\n");
		}
		public void UpdateNormal() {
			this.Normal = Vector_Normalize(Vector_Cross(Vector_Sub(b,a),Vector_Sub(c,a)));
		}
		
	}
	
	
	public static class Matrix{
		float[][] m = new float[4][4];
		float fov = (float) Math.toRadians(90f);
		float Znear = 0.1f; float Zfar = 1000f;
		float angleX = 0.0f;float angleY = 0.0f;float angleZ = 0.0f;
		public Matrix() {
		}
		public void modify(float a, int y, int x) {
			m[y][x] = a;
		}
		public void changeDepth(float Zn, float Zf) {
			this.Znear = Zn; this.Zfar = Zf;
		}
		public void makeProjection(Dimension dim) {
			float aspectRatio = (float)(dim.getWidth()/dim.getHeight());
			m = new float[][]{{aspectRatio * (float)(1/(Math.tan(fov/2))),0,0,0},{0,(float)(1/(Math.tan(fov/2))),0,0},{0,0, Zfar / (Zfar - Znear),1},{0,0,((-Zfar * Znear)/(Zfar - Znear)),0}};
		}
		public void makeRotX(float angleX) {
			m = new float[][] {{1f,0,0,0},{0,(float)Math.cos(angleX),(float)Math.sin(angleX),0},{0,-(float)Math.sin(angleX),(float)Math.cos(angleX),0},{0,0,0,1f}};
		}
		public void makeRotY(float angleY) {
			m = new float[][] {{(float)Math.cos(angleY),0,(float)Math.sin(angleY),0},{0,1f,0,0},{-(float)Math.sin(angleY),0,(float)Math.cos(angleY),0},{0,0,0,1f}};
		}
		public void makeRotZ(float angleZ) {
			m = new float[][] {{(float)Math.cos(angleZ),(float)Math.sin(angleZ),0,0},{-(float)Math.sin(angleZ),(float)Math.cos(angleZ),0,0},{0,0,1f,0},{0,0,0,1f}};
		}
		public void makeIdentity() {
			m = new float[][] {{1f,0f,0f,0f},{0f,1f,0f,0f},{0f,0f,1f,0f},{0f,0f,0f,1f,}};
		}
		public void makeTranslation(float x,float y, float z) {
			m = new float[][] {{1f,0f,0f,0f},{0f,1f,0f,0f},{0f,0f,1f,0f},{x,y,z,1f}};
		}

		public Vertex MatrixMultiply(Vertex v) {
			float newX = v.x * m[0][0] + v.y * m[1][0] + v.z * m[2][0] + m[3][0]; 
			float newY = v.x * m[0][1] + v.y * m[1][1] + v.z * m[2][1] + m[3][1]; 
			float newZ = v.x * m[0][2] + v.y * m[1][2] + v.z * m[2][2] + m[3][2];
	        float newW = v.x * m[0][3] + v.y * m[1][3] + v.z * m[2][3] + m[3][3];
	        if (newW!=0){
	            newX /= newW; newY /= newW; newZ /= newW;
	        }
			return new Vertex(newX,newY,newZ);
		}

	}
		public static void printMatrix(Matrix mat){
			for(int y = 0; y < 4; y++){
					System.out.println(mat.m[y][0] + " | " + mat.m[y][1]  + " | " + mat.m[y][2]  + " | " + mat.m[y][3]);
				}
		System.out.println("-----------");
		}

	public static Matrix MultiplyTwoMatrices(Matrix m1, Matrix m2) {
		Matrix m = new Matrix();
		for(int y = 0; y < 4; y++) {
			for(int x = 0; x < 4; x++) {
				m.m[x][y] = m1.m[y][0] * m2.m[0][x] + m1.m[y][1] * m2.m[1][x] + m1.m[y][2] * m2.m[2][x] + m1.m[y][3] * m2.m[3][x];
			}
		}
			return m;
	}
		
	public static Matrix MatrixInverse(Matrix original){
		Matrix invert = new Matrix(); invert.m = new float[][]{{(1/original.m[3][0]),0,0,0},{0,(1/original.m[3][1]),0,0},{0,0,(1/original.m[3][2]),0},{0,0,0,(1/original.m[3][3])}};
		return invert;
		}

	public static Matrix CameraMakeTransform(Vertex camPos, Vertex localTarget, Vertex WorldUp){
		//Local Target : Normalized Vector Absolute pos - Cam pos, WorldUp : normalized Up for the world

		Vertex localX = Vector_Cross(WorldUp, localTarget);
		Vertex localUp = Vector_Cross(localTarget,localX);

		Matrix ToWorld = new Matrix();
		ToWorld.m = new float[][]{{localX.x,localX.y,localX.z,0},{localUp.x,localUp.y,localUp.z,0},{localTarget.x,localTarget.y,localTarget.z,0},{camPos.x,camPos.y,camPos.z,1}};
		return ToWorld;

	}

	public static Matrix MatrixOrthogonalInverse(Matrix m){
		Matrix n = new Matrix();
																																   // RightX, RightY, RightZ, 0         RightX, UpX, FwdX, 1
		n.m = new float[][]{{m.m[0][0],m.m[1][0],m.m[2][0],0},{m.m[0][1],m.m[1][1],m.m[2][1],0},{m.m[0][2],m.m[1][2],m.m[2][2],0}, // UpX,    UpY,    UpZ, 	  0     --\ RightY, UpY, FwdY, 1
		{((-m.m[3][0])*m.m[0][0]+(-m.m[3][1])*m.m[0][1]+(-m.m[3][2])*m.m[0][2]),												   // FwdX,   FwdY,   FwdZ,   0     --/ RightZ, UpZ, FwdZ, 1
		 ((-m.m[3][0])*m.m[1][0]+(-m.m[3][1])*m.m[1][1]+(-m.m[3][2])*m.m[1][2]),												   // PosX,   PosY,   PosZ,   1		    -Pos . Rig, -Pos . Up, -Pos . Fwd,  1
		 ((-m.m[3][0])*m.m[2][0]+(-m.m[3][1])*m.m[2][1]+(-m.m[3][2])*m.m[2][2]),
			1}};
		return n;
	}

	public static float cheapInverse(float n){
		int bit = Float.floatToIntBits(n);
		return Float.intBitsToFloat(bit ^= 0x7EEEEEEE);
	}
}
