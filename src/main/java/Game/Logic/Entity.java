package Game.Logic;

import Game.Utilities.Utils;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class Entity {
    public enum STATE{
        CHILL,
        PLAYER
    }

    private static int id = 0;

    private Vector2d pos;
    private double velocity;
    private double radius;
    private int entityId;
    private STATE state;
    private Vector2d dest;

    private List<Vector2i> path;

    public int getPathPos() {
        return pathPos;
    }

    private int pathPos = 0;

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    private boolean isMoving = false;

    private boolean shouldExist = true;

    public Entity(Vector2d pos, double r, double speed) {
        this.pos = pos;
        radius = r;
        entityId = id++;
        state = STATE.CHILL;
        velocity = speed;
        path = new ArrayList<>();
    }

    public Entity(Vector2d pos, double r) {
        this(pos, r, 20);
    }
    public Entity(double x, double y, double r) {
        this(new Vector2d(x, y), r, 20);
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

    public void setState(STATE state) { this.state = state; }
    public STATE getState() { return state; }

    public boolean inRange(Vector2d a, Vector2d b) {
        return (Double.min(a.x, b.x) * Utils.W / 2 <= pos.x)
                && (pos.x <= Double.max(a.x, b.x) * Utils.W / 2)
                && (Double.min(a.y, b.y) * Utils.H / 2 <= pos.y)
                && (pos.y <= Double.max(a.y, b.y) * Utils.H / 2);
    }

    public List<Vector2i> getPath() { return path; }

    public void setPosFromMouse(Vector2d mouse) {
        pos.x = mouse.x * Utils.W / 2;
        pos.y = mouse.y * Utils.H / 2;

        if (!path.isEmpty()) generatePath(dest);
        else stopAndRemovePath();
    }

    public void stopEntity() {
        isMoving = false;
    }

    public void generatePath(Vector2d end) {
        Vector2i pos2i = Utils.cellFromPos(pos);
        Vector2i mouse2i = Utils.cellFromMOTO(end);

        dest = new Vector2d(end);
        pathPos = 0;
        isMoving = true;
        path = Field.generateCellPath(pos2i, mouse2i);
        path = Field.simplifyPath(path);

//        Field.debugPath(path);
    }

    private void stopAndRemovePath() {
        isMoving = false;
        pathPos = 0;
        path = new ArrayList<>();
        dest = new Vector2d(pos);
    }

    public void process(long time) {
        if (isMoving) {
            if (!path.isEmpty()) {
                if (pathPos < path.size()) {
                    Vector2d dir = new Vector2d(Utils.posFromCell(path.get(pathPos)).sub(pos));
                    double len = dir.length();
                    double move = velocity * time / 200;

                    if (move > len) {
                        pos = new Vector2d(Utils.posFromCell(path.get(pathPos)));
                        pathPos++;

                        if (pathPos >= path.size()) stopAndRemovePath();
                    } else {
                        pos.add(dir.normalize().mul(move));
                    }
                }
            }
        }
    }
}
