import java.util.ArrayList;
/*import java.util.Collections;
import java.util.Comparator;*/
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;

public class Cam extends Object {

    Window.Drawer WindowD;
    ArrayList<Object> World = new ArrayList<Object>();
    ArrayList<Structures.Tri> WorldMesh = new ArrayList<Structures.Tri>();
    ArrayList<Structures.Tri> tempMesh = new ArrayList<Structures.Tri>();

    Structures.Matrix ProjectionMatrix = new Structures.Matrix();

    Structures.Vertex Up = new Structures.Vertex(0,1,0);
    Structures.Vertex lookDir = new Structures.Vertex(0,0,1);
    Structures.Vertex target = new Structures.Vertex(0,0,0); //Target must be absolute - cam.pos
    float speed = 1f;

    public float PitchLimit = 1.56f; // ~ PI / 2
    boolean stretched;
    boolean isOrtho = false;

    public Cam(Structures.Vertex pos, float[] rotation,Window.Drawer Win,ArrayList<Object> World, boolean isStretched){
        super(new ArrayList<Structures.Tri>(),pos,rotation);
        WindowD = Win;
        this.World= World;
        ProjectionMatrix.makeProjection(this.WindowD.screen);
        this.stretched = isStretched;

        this.WindowD.win.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent evt) {
            }
            public void mouseMoved(MouseEvent evt) {
                if (!WindowD.paused){
                    int absWinCenterX = (int)WindowD.getLocationOnScreen().getX() + WindowD.screen.width/2;
                    int absWinCenterY = (int)WindowD.getLocationOnScreen().getY() + WindowD.screen.height/2;
                    try {
                        Robot rob = new Robot();
                        rotation[1] += (float) (evt.getXOnScreen() - absWinCenterX ) / 1000;
                        rotation[0] += (float) (evt.getYOnScreen() - absWinCenterY ) / 1000;
                        if (rotation[0] > PitchLimit) rotation[0] = PitchLimit;
                        else if (rotation[0] < -PitchLimit) rotation[0] = -PitchLimit;
                        rob.mouseMove(absWinCenterX,absWinCenterY);
                    }
                    catch (AWTException e) {
                        e.printStackTrace();
                    }
                }
            }	
        });

    }


    public Structures.Vertex PlaneFindPointIntersect(Structures.Vertex plane_normal, Structures.Vertex plane_point, Structures.Vertex lineStart, Structures.Vertex lineEnd){
        // Finds the intersection of a line and a defined plane
        plane_normal = Structures.Vector_Normalize(plane_normal);
        float plane_dist = Structures.Vector_Dot(plane_normal, plane_point);
        float adist = Structures.Vector_Dot(lineStart, plane_normal);
        float bdist = Structures.Vector_Dot(lineEnd, plane_normal);
        float mult = (plane_dist - adist) / (bdist - adist);

        Structures.Vertex startEnd = Structures.Vector_Sub(lineEnd, lineStart);
        Structures.Vertex startToIntersect = Structures.Vector_Mult(startEnd, mult);
        return Structures.Vector_Add(lineStart, startToIntersect);

    }

    public float DistToPlane(Structures.Vertex plane_normal, Structures.Vertex plane_point, Structures.Vertex extPoint){
        plane_normal = Structures.Vector_Normalize(plane_normal);
        Structures.Vertex planeToPoint = Structures.Vector_Sub(extPoint, plane_point);
        return Structures.Vector_Dot(planeToPoint, plane_normal);
    }


    public void ClipOnPlane(Structures.Vertex plane_normal, Structures.Vertex plane_point, Structures.Tri tri){
        ArrayList<Structures.Vertex> insidePoints = new ArrayList<Structures.Vertex>();
        ArrayList<Structures.Vertex> outsidePoints = new ArrayList<Structures.Vertex>();
        ArrayList<Structures.Text2d> insideTexture = new ArrayList<Structures.Text2d>();
        ArrayList<Structures.Text2d> outsideTexture = new ArrayList<Structures.Text2d>();
        // CLIPPING : 
        // Depending on how much points are "inside" our planes (screen edges and znear), create 0 / 1 / 2 new tris or Discard the triangle
        plane_normal = Structures.Vector_Normalize(plane_normal);

        float d0 = DistToPlane(plane_normal, plane_point, tri.a);
        float d1 = DistToPlane(plane_normal, plane_point, tri.b);
        float d2 = DistToPlane(plane_normal, plane_point, tri.c);
        //Depending on their distance from the plane (>= 0 : same side as normal), is the point in or out
        if (d0 >= 0) {insidePoints.add(tri.a); insideTexture.add(tri.u);}
        else {outsidePoints.add(tri.a); outsideTexture.add(tri.u);}
        if (d1 >= 0) {insidePoints.add(tri.b); insideTexture.add(tri.v);}
        else {outsidePoints.add(tri.b); outsideTexture.add(tri.v);}
        if (d2 >= 0) {insidePoints.add(tri.c); insideTexture.add(tri.w);}
        else {outsidePoints.add(tri.c); outsideTexture.add(tri.w);}

        int inside = insidePoints.size();
        int outside = outsidePoints.size();

        if (inside == 0) return;
        if (inside == 3) {this.tempMesh.add(tri); return;}
        if (inside == 1 && outside == 2){

            Structures.Vertex newA = PlaneFindPointIntersect(plane_normal, plane_point, insidePoints.get(0), outsidePoints.get(0));
            Structures.Vertex newB = PlaneFindPointIntersect(plane_normal, plane_point, insidePoints.get(0), outsidePoints.get(1));

            Structures.Tri clippedTri = new Structures.Tri(insidePoints.get(0), newA, newB);
            clippedTri.u = insideTexture.get(0);

            // Outside points and new points are colinear : i.e inside - new = t * inside - outside. t is the same for every coords since vectors are colinear
            float mult1 =  Structures.Vector_Sub(newA, insidePoints.get(0)).x / Structures.Vector_Sub(outsidePoints.get(0), insidePoints.get(0)).x;
            clippedTri.v = (Structures.Vector_Mult(Structures.Text_Sub(outsideTexture.get(0),insideTexture.get(0)), mult1));

            float mult2 =  Structures.Vector_Sub(newB, insidePoints.get(0)).x / Structures.Vector_Sub(outsidePoints.get(1), insidePoints.get(0)).x;
            clippedTri.w = (Structures.Vector_Mult(Structures.Text_Sub(outsideTexture.get(1),insideTexture.get(0)), mult2));

            clippedTri.color = tri.color;
            clippedTri.Normal = tri.Normal;
            clippedTri.WorldPos = tri.WorldPos;
            clippedTri.textureIndex = tri.textureIndex;
            this.tempMesh.add(clippedTri);
            return;

        }
        if (inside == 2 && outside == 1){
            //Creates a quadrilateral
            Structures.Vertex newA = PlaneFindPointIntersect(plane_normal, plane_point, insidePoints.get(0), outsidePoints.get(0));

            Structures.Tri clippedTri1 = new Structures.Tri(insidePoints.get(0), insidePoints.get(1), newA);

            clippedTri1.u = insideTexture.get(0);
            clippedTri1.v = insideTexture.get(1);
            float mult1 =  Structures.Vector_Sub(newA, insidePoints.get(0)).x / Structures.Vector_Sub(outsidePoints.get(0), insidePoints.get(0)).x;
            clippedTri1.w = (Structures.Vector_Mult(Structures.Text_Sub(outsideTexture.get(0),insideTexture.get(0)), mult1));

            clippedTri1.color = tri.color;
            clippedTri1.Normal = tri.Normal;
            clippedTri1.WorldPos = tri.WorldPos;
            clippedTri1.textureIndex = tri.textureIndex;

            Structures.Vertex newB = PlaneFindPointIntersect(plane_normal, plane_point, insidePoints.get(1), outsidePoints.get(0));
            Structures.Tri clippedTri2 = new Structures.Tri(insidePoints.get(1), clippedTri1.c, newB);
            clippedTri2.u = insideTexture.get(1);
            clippedTri2.v = clippedTri1.w;
            float mult2 =  Structures.Vector_Sub(newB, insidePoints.get(1)).x / Structures.Vector_Sub(outsidePoints.get(0), insidePoints.get(1)).x;
            clippedTri1.w = (Structures.Vector_Mult(Structures.Text_Sub(outsideTexture.get(0),insideTexture.get(1)), mult2));

            clippedTri2.color = tri.color;
            clippedTri2.Normal = tri.Normal;
            clippedTri2.WorldPos = tri.WorldPos;
            clippedTri2.textureIndex = tri.textureIndex;
    
            this.tempMesh.add(clippedTri1); this.tempMesh.add(clippedTri2);
            return;

        }

    }


    public ArrayList<Structures.Tri> ToDraw(){
        this.WorldMesh.clear();
        Structures.Matrix localTransform = Structures.CameraMakeTransform(this.pos, this.target, this.Up);
        Structures.Matrix transformInverse = Structures.MatrixOrthogonalInverse(localTransform);
        for(Object obj:World){
            for(Structures.Tri tri:obj.mesh){

                Color triColor = tri.color;
                String texturePath = tri.textureIndex;
                Structures.Text2d a = tri.u; Structures.Text2d b = tri.v; Structures.Text2d c = tri.w;

                tri = new Structures.Tri(Structures.Vector_Add(obj.pos, tri.a),Structures.Vector_Add(obj.pos, tri.b),Structures.Vector_Add(obj.pos, tri.c));

                tri.UpdateNormal();

                Structures.Vertex triWorldPos = tri.a;
                Structures.Vertex triNormal = tri.Normal;
                Structures.Vertex camToTri = Structures.Vector_Normalize(Structures.Vector_Sub(tri.a,this.pos));

                if (Structures.Vector_Dot(camToTri,triNormal) < 0f) {

                    tri = new Structures.Tri(transformInverse.MatrixMultiply(tri.a),transformInverse.MatrixMultiply(tri.b),transformInverse.MatrixMultiply(tri.c));
                    tri.u = a; tri.v = b; tri.w = c;
                    tri.Normal = triNormal;
                    tri.WorldPos = triWorldPos;
                    tri.color = triColor;
                    tri.textureIndex = texturePath;
                    WorldMesh.add(tri);
                    }
                }
            }
        return WorldMesh;
    }






    public void render(){

        this.lookDir = new Structures.Vertex((float)Math.sin(this.rotation[1]),(float)Math.sin(this.rotation[0]),(float)Math.cos(this.rotation[1]));
        this.target = Structures.Vector_Normalize(this.lookDir);


        this.WorldMesh = ToDraw();

        // Clipping WorldMesh by modifying every triangle for each plane Znear here
        this.tempMesh.clear();
        for (Structures.Tri tri : this.WorldMesh){ClipOnPlane(new Structures.Vertex(0,0,1), new Structures.Vertex(0,0,0.1f), tri);}
        this.WorldMesh.clear();
        for(Structures.Tri tri : this.tempMesh){this.WorldMesh.add(tri);}
        this.tempMesh.clear();

        // Cam space -> Screen Space
            for (int z = 0; z < WorldMesh.size();z++){

                Structures.Vertex Normal = WorldMesh.get(z).Normal; Structures.Vertex WorldPos = WorldMesh.get(z).WorldPos; Color color = WorldMesh.get(z).color;
                Structures.Text2d a = WorldMesh.get(z).u; Structures.Text2d b = WorldMesh.get(z).v; Structures.Text2d c = WorldMesh.get(z).w;
                String textureIndex = WorldMesh.get(z).textureIndex;

                Structures.Tri newTri = WorldMesh.get(z);

                if(!this.isOrtho){
                newTri = new Structures.Tri(ProjectionMatrix.MatrixMultiply(WorldMesh.get(z).a),ProjectionMatrix.MatrixMultiply(WorldMesh.get(z).b),ProjectionMatrix.MatrixMultiply(WorldMesh.get(z).c));
                newTri.u = a; newTri.v = b; newTri.w = c; 
                //Account for depth in texture
                newTri.u.u = newTri.u.u / newTri.u.w; newTri.v.u = newTri.v.u / newTri.v.w; newTri.w.u = newTri.w.u / newTri.w.w;
                newTri.u.v = newTri.u.v / newTri.u.w; newTri.v.v = newTri.v.v / newTri.v.w; newTri.w.v = newTri.w.v / newTri.w.w;
                newTri.u.w = 1 / newTri.u.w; newTri.v.w = 1 / newTri.v.w; newTri.w.w = 1 / newTri.w.w;

                //System.out.println(newTri.u.u);
                //oopsie ^
                }
                Structures.Tri FinTri = Window.Scale(newTri,this.WindowD,this.stretched); 
                FinTri.Normal = Normal; FinTri.WorldPos = WorldPos; FinTri.color = color; FinTri.textureIndex = textureIndex; FinTri.u = newTri.u; FinTri.v = newTri.v; FinTri.w = newTri.w;
                WorldMesh.set(z, FinTri);

            }

        // Sort by distance from cam
        /*Collections.sort(WorldMesh, new Comparator<Structures.Tri>(){
			@Override
			public int compare(Structures.Tri tri1, Structures.Tri tri2) {
				// tr1 should be in first : returns -1 | else : returns 1
                // Calculates the distance to the camera and compares t 2 and 1 with 2 - 1 > 0 ? 1 : -1
                float dt2 = Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(tri2.WorldPos,pos));
                float dt1 = Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(tri1.WorldPos,pos));
                return (dt2 > dt1) ? 1 : -1;
				} 
			});*/

        // Clipping against Screen Edges, Need Screen coordinates for the triangles
        for(int index = 0; index < 4; index++){
            this.tempMesh.clear();
            for(Structures.Tri tri : WorldMesh){
                switch(index){
                    case 0: //Left
                        ClipOnPlane(new Structures.Vertex(1,0,0), new Structures.Vertex(0,0,0), tri);
                        break;
                    case 1: //Top
                        ClipOnPlane(new Structures.Vertex(0,1,0), new Structures.Vertex(0,0,0), tri);
                        break;
                    case 2: //Right
                        ClipOnPlane(new Structures.Vertex(-1,0,0), new Structures.Vertex(this.WindowD.getWidth(), 0, 0), tri);
                        break;
                    case 3: //Bottom
                        ClipOnPlane(new Structures.Vertex(0,-1,0), new Structures.Vertex(0,this.WindowD.getHeight(),0), tri);
                        break;
                }
            }
            this.WorldMesh.clear();
            for(Structures.Tri tri : this.tempMesh){this.WorldMesh.add(tri);}
        }

        this.WorldMesh = Algs.PainterDivisionSort(this.WorldMesh, this.pos);

        //Transmit WorldMesh to Window to draw
        this.WindowD.paint(this.WindowD.getGraphics(),WorldMesh);

       
        //Algs.paintScreen(this.WorldMesh,this.WindowD);

        /*for(Structures.Tri tri:this.WorldMesh){
            Algs.Rasterizing(tri, this.WindowD, "h", false);
        }*/

        //this.WindowD.depthBuffer = new float[this.WindowD.getWidth() * this.WindowD.getHeight()];
        /*for(Structures.Tri tri : this.WorldMesh){
            Algs.TextureTriangle(this.WindowD, tri.a, tri.b, tri.c, tri.u, tri.v, tri.w, System.getProperty("user.dir") + "/textures/white.png");
        }*/
        
        this.WorldMesh.clear();
    }


        public void CamParseControls(ArrayList<Integer> Controls){
            for (Integer event : Controls){
                switch(event){
                    case 32: // SPACE
                        this.pos.y -= 0.01f * this.speed;
                        break;
                    case 16: // SHIFT
                        this.pos.y += 0.01f * this.speed;
                        break;
                    case 81: // Q
                        this.pos.x -= 0.01f * Math.cos(this.rotation[1]) * this.speed;
                        this.pos.z += 0.01f * Math.sin(this.rotation[1]) * this.speed;
                        break;
                    case 68: // D
                        this.pos.x += 0.01f * Math.cos(this.rotation[1]) * this.speed;
                        this.pos.z -= 0.01f * Math.sin(this.rotation[1]) * this.speed;
                        break; 
                    case 90: // Z
                        this.pos.z += 0.01f * Math.cos(this.rotation[1]) * this.speed;
                        this.pos.x += 0.01f * Math.sin(this.rotation[1]) * this.speed;
                        break;
                    case 83: // S
                        this.pos.z -= 0.01f * Math.cos(this.rotation[1]) * this.speed;
                        this.pos.x -= 0.01f * Math.sin(this.rotation[1]) * this.speed;
                        break;  
                    case 27: // esc
                        this.WindowD.paused = true;
                        break;
                }
            }
            if (!Controls.contains(27))this.WindowD.paused = false;
        }
}
