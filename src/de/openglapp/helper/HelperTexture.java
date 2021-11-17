package de.openglapp.helper;

import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class HelperTexture {
	
	private static Hashtable<String, Integer> _textureList = new Hashtable<String, Integer>();  
	
	private static int _textureWhiteDefault = -1;
	
	public static void initDefaultTexture()
	{
		_textureWhiteDefault = importTexture("/textures/white.png");
	}
	
	public static int importTexture(String filename)
	{
        
        filename = filename.trim().toLowerCase();


        if (_textureList.containsKey(filename)) {
            return _textureList.get(filename);
        } 
        else{
        	int textureID = -1;
        
            BufferedImage image;
            //String userDir = System.getProperty("user.dir");
            int bytesPerPixel = 0;
            Graphics2D g = null;
            try {
            	InputStream is = HelperTexture.class.getResourceAsStream(filename);
                //image = ImageIO.read(new File(filename));
            	image = ImageIO.read(is);
                bytesPerPixel = image.getColorModel().hasAlpha() ? 4 : 3;
                g = image.createGraphics();
                g.drawImage(image, 0, 0, null);
                is.close();
            } catch (Exception ex) {
                System.err.println("Could not load file '" + filename + "'. Is your path correct?");
                return _textureWhiteDefault;
            }
            finally {
            	if(g != null)
            		g.dispose();
            }

            int[] pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
            ByteBuffer buffer = BufferUtils.createByteBuffer(
                    image.getWidth() * image.getHeight() * 4);

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = pixels[y * image.getWidth() + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                    buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                    buffer.put((byte) (pixel & 0xFF));               // Blue component
                    if (bytesPerPixel == 4) {
                        buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
                    } else {
                        buffer.put((byte) 0xFF);
                    }
                }
            }

            buffer.flip();
            int textures[] = new int[1];
            GL45.glGenTextures(textures);
            textureID = textures[0];
            GL45.glBindTexture(GL45.GL_TEXTURE_2D, textureID); //Bind texture ID

            GL45.glTexImage2D(GL45.GL_TEXTURE_2D, 0, GL45.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL45.GL_RGBA, GL45.GL_UNSIGNED_BYTE, buffer);

            GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_WRAP_S, GL45.GL_REPEAT);
            GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_WRAP_T, GL45.GL_REPEAT);

            GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_MIN_FILTER, GL45.GL_LINEAR_MIPMAP_LINEAR);
            GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_MAG_FILTER, GL45.GL_LINEAR);

            GL45.glGenerateMipmap(GL45.GL_TEXTURE_2D);

            _textureList.put(filename, textureID);

            
            
            // unbind texture
            GL45.glBindTexture(GL45.GL_TEXTURE_2D, 0);
        
        return textureID;
        }
	}
}
