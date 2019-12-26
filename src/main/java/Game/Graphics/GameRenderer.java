package Game.Graphics;

import Game.Logic.Entity;
import Game.Logic.Field;
import Game.Logic.Graph;
import Game.Utilities.Utils;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.File;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GameRenderer {
    private static long window;
    private static Map<Integer, Texture> textureMap;

    public static int getKey(int key) { return glfwGetKey(window, key); }
    public static int getMouseKey(int key) { return glfwGetMouseButton(window, key); }
    public static void setWindowShouldClose() { glfwSetWindowShouldClose(window, true); }
    public static boolean windowShouldNotClose() { return !glfwWindowShouldClose(window); }

    public static void swapBuffers() { glfwSwapBuffers(window); }
    public static void pollEvents() { glfwPollEvents(); }

    public static void clearWindow() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glClearColor(1, 1, 1, 1);
    }

    public static void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will not be resizable

        // Create the window
        window = glfwCreateWindow(Utils.W, Utils.H, "No pain, no gain", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    }

    public static void destroy() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static Vector2d getCursorPos() {
        double[] cursor_x = new double[1], cursor_y = new double[1];

        glfwGetCursorPos(GameRenderer.window, cursor_x, cursor_y);
        return new Vector2d((cursor_x[0] / Utils.H * 2 - 1), (1 - cursor_y[0] / Utils.H * 2));
    }

    public static void loadTextures() {
        System.out.println("Loading textures:");
        File texturePath = new File("src\\main\\resources\\sprites");
        textureMap = new HashMap<Integer, Texture>();
        try {
            for (File a : texturePath.listFiles()) {
                String name = a.getName();
                int number = Integer.valueOf(name.substring(0, name.indexOf('.')));
                textureMap.put(number, Texture.genTexture(a.getAbsolutePath()));
                System.out.printf("     Texture %3d loaded\n", number);
            }
        } catch (IOException ex) {
            System.err.println("Missing textures!");
            GameRenderer.destroy();
            System.exit(-1);
        }
        System.out.println("All textures loaded successfully!");
    }

    private static void bindTexture(int id) {
        textureMap.get(id).bind();
    }

    private static void entityVertex2d(double x, double y) {
        glVertex2d(x / Utils.W * 2, y / Utils.H * 2);
    }

    public static void renderEntity(Entity entity) {
        final int SEGEMEMTS = 30;

        glShadeModel(GL_SMOOTH);
        glColor4dv(Utils.Color.cellColor.get(entity.getState()));

        glBegin(GL_TRIANGLE_FAN);
        entityVertex2d(entity.getX(), entity.getY());

        for (int i = 0; i < SEGEMEMTS; i++) {
            double angle = Math.PI * 2 * i / SEGEMEMTS;

            entityVertex2d(entity.getX() + Math.cos(angle) * entity.getR(),
                    entity.getY() + Math.sin(angle) * entity.getR());
        }

        entityVertex2d(entity.getX() + entity.getR(), entity.getY());
        glEnd();
    }

    private static void fieldVertex(double x, double y) {
        glVertex2d((x + Utils.xSize) / Utils.W - 1, -(y + Utils.ySize) / Utils.H + 1);
    }

    public static void renderBackground(boolean isDebugMode, boolean isDebugModePath) {
        for (int zY = 0; zY < Utils.yZones; zY++) {
            for (int zX = 0; zX < Utils.xZones; zX++) {
                for (int posY = 0; posY < Utils.zoneSize; posY++) {
                    for (int posX = 0; posX < Utils.zoneSize; posX++) {
                        int x = zX * Utils.zoneSize + posX;
                        int y = zY * Utils.zoneSize + posY;

                        if (Field.getZone(zX, zY).getCell(posX, posY) == Field.CELL.CLEAR)
                            bindTexture(0);
                        else
                            bindTexture(1);

                        if (isDebugMode) {
                            if (Field.getZone(zX, zY).getId() % 2 == 0)
                                glColor4d(0.3, 1, 1, 1);
                            else
                                glColor4d(0.7, 1, 1, 1);

                            if (isDebugModePath) {
                                if (Field.getZone(zX, zY).getDebugCells()[posY][posX] == 1)
                                    glColor4d(1, 1, 0, 1);
                            }

                            if (Field.getZone(zX, zY).getCell(posX, posY) == Field.CELL.OBSTICLE)
                                glColor4d(0.3, 0.3, 0.3, 1);

                            if (Field.getGraph().inV(new Vector2i(x, y))) {
                                glColor4d(0, 0.7, 0, 1);
                            }
                        } else {
                            glColor4d(1, 1, 1, 1);
                        }

                        glEnable(GL_TEXTURE_2D);

                        glBegin(GL_QUADS);
                        glTexCoord2d(0, 0);
                        fieldVertex(Utils.xSize * x * 2 - Utils.xSize, y * Utils.ySize * 2 + Utils.ySize);

                        glTexCoord2d(0, 1);
                        fieldVertex(x * Utils.xSize * 2 + Utils.xSize, y * Utils.ySize * 2 + Utils.ySize);

                        glTexCoord2d(1, 1);
                        fieldVertex(x * Utils.xSize * 2 + Utils.xSize, y * Utils.ySize * 2 - Utils.ySize);

                        glTexCoord2d(1, 0);
                        fieldVertex(x * Utils.xSize * 2 - Utils.xSize, y * Utils.ySize * 2 - Utils.ySize);

                        glEnd();
                        glDisable(GL_TEXTURE_2D);
                    }
                }
            }
        }
    }

//    public static void renderPathLine(){
//
//    }

    public static void renderMouseBorder(Vector2d start, Vector2d cur){
        glColor4dv(Utils.Color.mouseBorder);

        glLineWidth(3);
        glBegin(GL_LINE_LOOP);

        glVertex2d(start.x, start.y);
        glVertex2d(cur.x, start.y);
        glVertex2d(cur.x, cur.y);
        glVertex2d(start.x, cur.y);

        glEnd();
    }

    public static void renderPath(Deque<Graph.Vertex> path) {
        ;
    }
}