import java.awt.Color;
import java.util.ArrayList;


public class Object{
    ArrayList<Structures.Tri> mesh;
    Structures.Vertex pos = new Structures.Vertex(0,0,0);
    Structures.Vertex locUp = new Structures.Vertex(0,1,0);
    Structures.Vertex locFr = new Structures.Vertex(0,0,-1);
    Structures.Vertex locRi = new Structures.Vertex(1,0,0);
    Structures.Vertex velocity = new Structures.Vertex(0,0,0);
    float[] rotation = {0,0,0}; 
    float[] PerFrameRotation = {0,0,0};
    Structures.Matrix translate = new Structures.Matrix();
    Structures.Matrix rotateX = new Structures.Matrix();
    Structures.Matrix rotateY = new Structures.Matrix();
    Structures.Matrix rotateZ = new Structures.Matrix();
    String texture = "";

    public Object(ArrayList<Structures.Tri> mesh, Structures.Vertex pos, float[] rotation){
        this.mesh = mesh; this.pos = pos; this.rotation = rotation;
        translate.makeTranslation(velocity.x, velocity.y, velocity.z);
        this.rotate(this.rotation);
    }
    
    public void update(){
        this.UpdateRotate();
        this.rotation[0] += this.PerFrameRotation[0]; this.rotation[1] += this.PerFrameRotation[1]; this.rotation[2] += this.PerFrameRotation[2];
        this.pos = Structures.Vector_Add(this.pos,this.velocity);
      //  locUp = this.rotateX.MatrixMultiply(locUp); locUp = this.rotateY.MatrixMultiply(locUp); locUp = this.rotateZ.MatrixMultiply(locUp);
      //  locFr = this.rotateX.MatrixMultiply(locFr); locFr = this.rotateY.MatrixMultiply(locFr); locFr = this.rotateZ.MatrixMultiply(locFr);
      //  locRi = this.rotateX.MatrixMultiply(locRi); locUp = this.rotateY.MatrixMultiply(locRi); locRi = this.rotateZ.MatrixMultiply(locRi);
    }

    public void rotate(float[] rotation){
        this.rotateX.makeRotX(rotation[0]); this.rotateY.makeRotY(rotation[1]); rotateZ.makeRotZ(rotation[2]);
        for (int i = 0; i < this.mesh.size(); i++){
            Color color = this.mesh.get(i).color;
            this.mesh.set(i, new Structures.Tri(this.rotateX.MatrixMultiply(this.mesh.get(i).a),this.rotateX.MatrixMultiply(this.mesh.get(i).b),this.rotateX.MatrixMultiply(this.mesh.get(i).c)));
            this.mesh.set(i, new Structures.Tri(this.rotateY.MatrixMultiply(this.mesh.get(i).a),this.rotateY.MatrixMultiply(this.mesh.get(i).b),this.rotateY.MatrixMultiply(this.mesh.get(i).c)));
            this.mesh.set(i, new Structures.Tri(this.rotateZ.MatrixMultiply(this.mesh.get(i).a),this.rotateZ.MatrixMultiply(this.mesh.get(i).b),this.rotateZ.MatrixMultiply(this.mesh.get(i).c)));
            this.mesh.get(i).UpdateNormal();
            this.mesh.get(i).color = color;
        }
    }

    public void UpdateRotate(){
        //Maybe find a way for cameraMakeTransform to work for world matrices
        this.rotateX.makeRotX(this.PerFrameRotation[0]); this.rotateY.makeRotY(this.PerFrameRotation[1]); this.rotateZ.makeRotZ(this.PerFrameRotation[2]);
        for (int i = 0; i < this.mesh.size(); i++){
            Color color = this.mesh.get(i).color;
            this.mesh.set(i, new Structures.Tri(this.rotateX.MatrixMultiply(this.mesh.get(i).a),this.rotateX.MatrixMultiply(this.mesh.get(i).b),this.rotateX.MatrixMultiply(this.mesh.get(i).c)));
            this.mesh.set(i, new Structures.Tri(this.rotateY.MatrixMultiply(this.mesh.get(i).a),this.rotateY.MatrixMultiply(this.mesh.get(i).b),this.rotateY.MatrixMultiply(this.mesh.get(i).c)));
            this.mesh.set(i, new Structures.Tri(this.rotateZ.MatrixMultiply(this.mesh.get(i).a),this.rotateZ.MatrixMultiply(this.mesh.get(i).b),this.rotateZ.MatrixMultiply(this.mesh.get(i).c)));
            this.mesh.get(i).UpdateNormal();
            this.mesh.get(i).color = color;
        }
    }

    //Horrible ↓ ! Need to add independant control mapping for each objects
    public void ParseControls(ArrayList<Integer> Controls){
        for (Integer event : Controls){
            switch(event){
                case 38:
                    this.pos.y -= 0.005f;
                    break;
                case 40:
                    this.pos.y += 0.005f;
                    break;
                case 37:
                    this.pos.x -= 0.005f;
                    break;
                case 39:
                    this.pos.x += 0.005f;
                    break; 
                case 33:
                    this.pos.z += 0.005f;
                    break;
                case 34:
                    this.pos.z -= 0.005f;
                    break;    
                case 80: //P
                    this.mesh = MeshUtils.Subdivide(this.mesh);
                    break;   
            }
        }
    }

}