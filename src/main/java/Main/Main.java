package Main;

import Game.Graphics.GameRenderer;
import Game.Logic.Entity;
import Game.Logic.Field;
import org.joml.Vector2d;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.windows.MOUSEINPUT;

import java.lang.reflect.GenericArrayType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import static org.lwjgl.glfw.GLFW.*;

public class Main {
    private Vector2d cursorPosStart;
    private Vector2d cursorPosCurrent;
    private boolean isMouseSet = false;
    private boolean isDebugMode = false;
    private boolean isXPressed = false;
    private boolean isDebugModePath = false;
    private boolean isCPressed = false;


    private static Field field;
    private static ArrayList<Entity> entities;
    private static Deque<Entity> intEnt;

    // For debug
    public static Field getField() {
        return field;
    }

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

//        render();
//        processInput();

        last_time = System.currentTimeMillis();;
        while (GameRenderer.windowShouldNotClose()) {
            start_time = System.currentTimeMillis();

            processInput();
            render();
            processGameLogic(start_time - last_time);

            last_time = start_time;
            GameRenderer.pollEvents();
        }
    }

    private void processGameLogic(long delta_time) {
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

            if (isMouseSet) {
                GameRenderer.renderMouseBorder(cursorPosStart, cursorPosCurrent);
            }
        }

        GameRenderer.swapBuffers();
    }

    private void processInput() {
        int state = GameRenderer.getKey(GLFW_KEY_ESCAPE);
        if (state == GLFW_PRESS) {
            GameRenderer.setWindowShouldClose();
        }

        state = GameRenderer.getKey(GLFW_KEY_S);
        if (state == GLFW_PRESS) { intEnt.forEach(Entity::stopEntity); }

        state = GameRenderer.getKey(GLFW_KEY_X);
        if (state == GLFW_PRESS) {
            if (!isXPressed) {
                isDebugMode = !isDebugMode;
                isXPressed = true;
            }
        }
        state = GameRenderer.getKey(GLFW_KEY_X);
        if (state == GLFW_RELEASE && isXPressed) isXPressed = false;

        state = GameRenderer.getKey(GLFW_KEY_C);
        if (state == GLFW_PRESS) {
            if (isDebugMode) {
                if (!isCPressed) {
                    isDebugModePath = !isDebugModePath;
                    isCPressed = true;
                }
            }
        }
        state = GameRenderer.getKey(GLFW_KEY_C);
        if (state == GLFW_RELEASE && isCPressed) isCPressed = false;

        state = GameRenderer.getMouseKey(GLFW_MOUSE_BUTTON_LEFT);
        if (state == GLFW_PRESS) {
            if (!isMouseSet) {
                isMouseSet = true;
                cursorPosStart = cursorPosCurrent;
            }
        } else if (state == GLFW_RELEASE) {
            if (isMouseSet) {
                while (!intEnt.isEmpty()) intEnt.pop().setState(Entity.STATE.STANDING);

                for (Entity i : entities) {
                    if (i.inRange(cursorPosCurrent, cursorPosStart)) {
                        intEnt.push(i);
                        i.setState(Entity.STATE.PLAYERED);
                    }
                }
            }
            isMouseSet = false;
        }

        state = GameRenderer.getMouseKey(GLFW_MOUSE_BUTTON_RIGHT);
        if (state == GLFW_PRESS) {
            intEnt.forEach(it -> it.generatePath(cursorPosCurrent));
        }
        state = GameRenderer.getMouseKey(GLFW_MOUSE_BUTTON_MIDDLE);
        if (state == GLFW_PRESS) {
            intEnt.forEach(it -> it.setPosFromMouse(cursorPosCurrent));
        }

        cursorPosCurrent = GameRenderer.getCursorPos();
    }

    public static void main(String[] args) {
        new Main().run();
    }

}