package Game.Graphics;

import de.matthiasmann.twl.utils.PNGDecoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {
    private int id;

    public Texture() { id = 0; }
    private Texture(int id) { this.id = id; }

    static Texture genTexture(String fileName) throws IOException {
        //load png file
        PNGDecoder decoder = null;

        decoder = new PNGDecoder(new FileInputStream(new File(fileName)));

        //create a byte buffer big enough to store RGBA values
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());

        //decode
        decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);

        //flip the buffer so its ready to read
        buffer.flip();

        //create a texture
        int id = glGenTextures();

        //bind the texture
        glBindTexture(GL_TEXTURE_2D, id);

        //tell opengl how to unpack bytes
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        //set the texture parameters, can be GL_LINEAR or GL_NEAREST
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        //upload texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        // Generate Mip Map
        glGenerateMipmap(GL_TEXTURE_2D);

        return new Texture(id);
    }

    void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }
}
