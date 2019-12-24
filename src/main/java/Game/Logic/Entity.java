package Game.Logic;

import Game.Graphics.GameRenderer;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Entity {
    public enum STATE{
        STANDING,
        MOVING,
        PLAYERED
    };

    private static int id = 0;

    private Vector2d pos;
    private double velocity;
    private double radius;
    private int entityId;
    private STATE state;
    private List<Vector2i> path;

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    private boolean isMoving = false;
    private boolean havePath = false;

    public void setHavePath(boolean havePath) {
        this.havePath = havePath;
    }

    private ListIterator pathTile;

    private boolean shouldExist = true;

    public Entity(Vector2d pos, double r, double speed) {
        this.pos = pos;
        radius = r;
        entityId = id++;
        state = STATE.STANDING;
        velocity = speed;
        path = new LinkedList<>();
    }

    public Entity(Vector2d pos) {
        this(pos, 60, 10);
    }
    public Entity(double x, double y, double r) {
        this(new Vector2d(x, y), r, 10);
    }
    public Entity(double x, double y, double r, double speed) {
        this(new Vector2d(x, y), r, speed);
    }

    public boolean shouldExist() { return shouldExist; }
    public void kill() { shouldExist = false; }

    public Vector2d getPos() { return pos; }
    public double getX() { return pos.x; }
    public double getY() { return pos.y; }
    public double getR() { return radius; }

    public void setPos(Vector2d pos) { this.pos = pos; }
    public void setPos(double x, double y) {
        pos.x = x;
        pos.y = y;
    }

    public double getVelocity() { return velocity; }
    public void setVel(double vel) { this.velocity = vel; }

    public void setState(STATE state) { this.state = state; }
    public STATE getState() { return state; }

    public boolean inRange(Vector2d a, Vector2d b) {
        return (Double.min(a.x, b.x) * GameRenderer.getW() / 2 <= pos.x)
                && (pos.x <= Double.max(a.x, b.x) * GameRenderer.getW() / 2)
                && (Double.min(a.y, b.y) * GameRenderer.getH() / 2 <= pos.y)
                && (pos.y <= Double.max(a.y, b.y) * GameRenderer.getH() / 2);
    }

    public List<Vector2i> getPath() { return path; }
    public void setPath(List<Vector2i> path) {
        this.path = path;
        isMoving = true;
    }

    public void setPosFromMouse(Vector2d mouse) {
        pos.x = mouse.x * GameRenderer.getW() / 2;
        pos.y = mouse.y * GameRenderer.getH() / 2;

        isMoving = false;
        path = new LinkedList<>();
    }

    public void stopEntity() {
        isMoving = false;
    }

    public void generatePath(Vector2d curPos) {
        ;
    }

    public void process(long time) {
        if (isMoving) {

        }
//            if (pos.equals(path)) {
//                isMoving = false;
//            } else {
//                Vector2d dir = new Vector2d(path);
//                dir.sub(pos);
//                double len = dir.length();
//                double move = velocity * time / 200;
//
//                if (move > len) {
//                    pos = new Vector2d(path);
//                } else {
//                    pos.add(dir.normalize().mul(move));
//                }
//            }
//            path = Field.;
    }
}
