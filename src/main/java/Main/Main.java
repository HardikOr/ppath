package Main;

import Game.Graphics.GameRender;
import Game.Logic.Entity;
import Game.Logic.Field;
import Game.Utilities.Utils;
import org.joml.Vector2d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import static org.lwjgl.glfw.GLFW.*;

public class Main {
    private Vector2d cursorPosStart;
    private Vector2d cursorPosCurrent;

    private boolean isDebugMode = false;
    private boolean isDebugModePath = false;

    private boolean isMouseLeftPressed = false;
    private boolean isMouseMiddlePressed = false;
    private boolean isMouseRightPressed = false;
    private boolean isSPressed = false;
    private boolean isXPressed = false;
    private boolean isCPressed = false;

    private static ArrayList<Entity> entities;
    private static Deque<Entity> intEnt;

    private void run() {
        init();
        GameRender.init();
        GameRender.loadTextures();
        loop();
        GameRender.destroy();
    }

    private void init() {
        Field.fieldInit();
        entities = new ArrayList<>();
        intEnt = new ArrayDeque<>();

        entities.add(new Entity(-112.5, -37.5, 12.5));

        cursorPosCurrent = new Vector2d();
        cursorPosStart = new Vector2d();
    }

    private void loop() {
        long start_time;
        long last_time;

        last_time = System.currentTimeMillis();
        while (GameRender.windowShouldNotClose()) {
            start_time = System.currentTimeMillis();

            processInput();
            render();
            processUnits(start_time - last_time);

            last_time = start_time;
            GameRender.pollEvents();
        }
    }

    private void processUnits(long delta_time) {
        entities.forEach(it -> it.process(delta_time));
    }

    private void render() {
        GameRender.clearWindow();

        GameRender.renderBackground(isDebugMode, cursorPosCurrent);

        if (!isDebugMode) {
            if (!entities.isEmpty()) entities.forEach(it ->
                    GameRender.renderPath(it.getPath(), it.getPathPos(), it.getPos()));
            if (!entities.isEmpty()) entities.forEach(GameRender::renderEntity);

            if (isMouseLeftPressed) GameRender.renderMouseBorder(cursorPosStart, cursorPosCurrent);
        }

        GameRender.swapBuffers();
    }

    private void processInput() {
        cursorPosCurrent = GameRender.getCursorPos();

        if (GameRender.getKey(GLFW_KEY_ESCAPE) == GLFW_PRESS) GameRender.setWindowShouldClose();

        switch (GameRender.getKey(GLFW_KEY_S)) {
            case GLFW_PRESS:
                if (!isSPressed) {
                    intEnt.forEach(Entity::stopEntity);
                    isSPressed = true;
                }
                break;
            case GLFW_RELEASE :
                if (isSPressed) isSPressed = false;
                break;
        }

        switch (GameRender.getKey(GLFW_KEY_X)) {
            case GLFW_PRESS:
                if (!isXPressed) {
                    isDebugMode = !isDebugMode;
                    isXPressed = true;
                }
                break;
            case GLFW_RELEASE :
                if (isXPressed) isXPressed = false;
                break;
        }

        switch (GameRender.getKey(GLFW_KEY_C)) {
            case GLFW_PRESS:
                if (isDebugMode && !isCPressed) {
                    isDebugModePath = !isDebugModePath;
                    isCPressed = true;
                }
                break;
            case GLFW_RELEASE :
                if (isCPressed) isCPressed = false;
                break;
        }

        switch (GameRender.getMouseKey(GLFW_MOUSE_BUTTON_LEFT)) {
            case GLFW_PRESS:
                if (!isMouseLeftPressed) {
                    cursorPosStart = cursorPosCurrent;
                    isMouseLeftPressed = true;
                }
                break;
            case GLFW_RELEASE :
                if (isMouseLeftPressed){
                    while (!intEnt.isEmpty()){
                        intEnt.pop().setState(Entity.STATE.CHILL);
                    }

                    for (Entity i : entities) {
                        if (i.inRange(cursorPosCurrent, cursorPosStart)) {
                            intEnt.push(i);
                            i.setState(Entity.STATE.PLAYER);
//                            System.out.println("X: " + i.getPos().x + " Y: " + i.getPos().y);
//                            System.out.println("X: " + Utils.cellFromPos(i.getPos()).x + " Y: " + Utils.cellFromPos(i.getPos()).y);
                        }
                    }

                    isMouseLeftPressed = false;
                }
                break;
        }

        switch (GameRender.getMouseKey(GLFW_MOUSE_BUTTON_RIGHT)) {
            case GLFW_PRESS:
                if (!isMouseRightPressed) {
//                    System.out.println(Utils.cellFromMOTO(cursorPosCurrent).x);
//                    System.out.println(Utils.cellFromMOTO(cursorPosCurrent).y);
//                    System.out.println("\n");

                    intEnt.forEach(i -> i.generatePath(cursorPosCurrent));
//                    field.debugPath(field.generateCellPath(new Vector2i(7, 2),  Utils.cellFromMouse(cursorPosCurrent)));
//                    for ()
                    isMouseRightPressed = true;
                }
                break;
            case GLFW_RELEASE :
                if (isMouseRightPressed){

                    isMouseRightPressed = false;
                }
                break;
        }

        switch (GameRender.getMouseKey(GLFW_MOUSE_BUTTON_MIDDLE)) {
            case GLFW_PRESS:
                if (intEnt.isEmpty() && !isMouseMiddlePressed) {
//                    System.out.println(Utils.cellFromMOTO(cursorPosCurrent).x);
//                    System.out.println(Utils.cellFromMOTO(cursorPosCurrent).y);
//                    System.out.println("\n");

                    entities.add(new Entity(Utils.posFromMOTO(cursorPosCurrent), 12.5));

                    isMouseMiddlePressed = true;
                } else {
                    intEnt.forEach(it -> it.setPosFromMouse(cursorPosCurrent));
                }
                break;
            case GLFW_RELEASE :
                if (isMouseMiddlePressed){
                    isMouseMiddlePressed = false;
                }
                break;
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }

}