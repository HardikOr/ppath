package Main;

import Game.Graphics.GameRenderer;
import Game.Logic.Entity;
import Game.Logic.Field;
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

    private static Field field;
    private static ArrayList<Entity> entities;
    private static Deque<Entity> intEnt;

    private void run() {
        init();
        GameRenderer.init();
        GameRenderer.loadTextures();
        loop();
        GameRenderer.destroy();
    }

    private void init() {
        field = new Field();
        entities = new ArrayList<>();
        intEnt = new ArrayDeque<>();

//        entities.add(new Entity(0, 0, 12.5));
//        entities.add(new Entity(510, -100, 12.5));
        entities.add(new Entity(-112.5, -12.5, 12.5));
//        entities.add(new Entity(-212.5, -62.5, 12.5));

        cursorPosCurrent = new Vector2d();
        cursorPosStart = new Vector2d();
    }

    private void loop() {
        long start_time;
        long last_time;

        last_time = System.currentTimeMillis();
        while (GameRenderer.windowShouldNotClose()) {
            start_time = System.currentTimeMillis();

            processInput();
            render();
            processUnits(start_time - last_time);

            last_time = start_time;
            GameRenderer.pollEvents();
        }
    }

    private void processUnits(long delta_time) {
        entities.forEach(it -> it.process(delta_time));
    }

    private void render() {
        GameRenderer.clearWindow();

        GameRenderer.renderBackground(isDebugMode, isDebugModePath);

        if (!isDebugMode) {
            if (!entities.isEmpty()) {
                ArrayList<Entity> draw = entities;
                draw.forEach(GameRenderer::renderEntity);
            }

            if (isMouseLeftPressed) {
                GameRenderer.renderMouseBorder(cursorPosStart, cursorPosCurrent);
            }
        }

        GameRenderer.swapBuffers();
    }

    private void processInput() {
        cursorPosCurrent = GameRenderer.getCursorPos();

        if (GameRenderer.getKey(GLFW_KEY_ESCAPE) == GLFW_PRESS) GameRenderer.setWindowShouldClose();

        switch (GameRenderer.getKey(GLFW_KEY_S)) {
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

        switch (GameRenderer.getKey(GLFW_KEY_X)) {
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

        switch (GameRenderer.getKey(GLFW_KEY_C)) {
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

        switch (GameRenderer.getMouseKey(GLFW_MOUSE_BUTTON_LEFT)) {
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
                        }
                    }

                    isMouseLeftPressed = false;
                }
                break;
        }

        switch (GameRenderer.getMouseKey(GLFW_MOUSE_BUTTON_RIGHT)) {
            case GLFW_PRESS:
                if (!isMouseRightPressed) {
                    System.out.println(Field.getCellFromMousePos(cursorPosCurrent).x);
                    System.out.println(Field.getCellFromMousePos(cursorPosCurrent).y);
                    System.out.println("\n");

                    isMouseRightPressed = true;
                }
                break;
            case GLFW_RELEASE :
                if (isMouseRightPressed){

                    isMouseRightPressed = false;
                }
                break;
        }

        switch (GameRenderer.getMouseKey(GLFW_MOUSE_BUTTON_MIDDLE)) {
            case GLFW_PRESS:
                if (intEnt.isEmpty() && !isMouseMiddlePressed) {
                    System.out.println(Field.getCellFromMousePos(cursorPosCurrent).x);
                    System.out.println(Field.getCellFromMousePos(cursorPosCurrent).y);
                    System.out.println("\n");

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