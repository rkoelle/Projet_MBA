package fr.norips.ar.ARMuseum.Drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by norips on 20/10/16.
 */

public class RectTex extends Rectangle {
    private final static String TAG = "RectTex";
    protected boolean finished = false;
    protected int[] textures = null;
    protected int[] textureAct = null;

    public RectTex(float pos[][], List<String> pathToTextures, Context context) {
        super(pos,pathToTextures,context);
    }
    /** This will be used to pass in the texture. */
    protected int mTextureUniformHandle;


    /**
     * The object own drawing function.
     * Called from the renderer to redraw this instance
     * with possible changes in values.
     *
     */
    protected Handler handler = null;
    protected Runnable runnable = null;
    protected void reInitLoad(){
        for (int i = 0; i < pathToTextures.size(); i++)
            stack.addFirst(textureAct[i]);

        finished = false;
    }

    @Override
    public void init(){
        if(handler == null) {
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    reInitLoad();
                    Log.d(TAG,"reInitLoad called");
                }
            };
        }
    }
    public void draw(float[] projectionMatrix, float[] modelViewMatrix) {
        if(handler!=null) {
            //Time out
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 10000);
        }
        super.draw(projectionMatrix,modelViewMatrix);
        GLES20.glUseProgram(shaderProgram.getShaderProgramHandle());
        shaderProgram.setProjectionMatrix(projectionMatrix);
        shaderProgram.setModelViewMatrix(modelViewMatrix);
        if(finished == false) {
            Log.d(TAG,"loadGLTexture called");
            loadGLTexture();
            Log.d(TAG,"loadGLTexture exited");
        } else {
            mTextureUniformHandle = GLES20.glGetUniformLocation(shaderProgram.getShaderProgramHandle(), "u_Texture");
            // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit textureAct[currentTexture].
            GLES20.glUniform1i(mTextureUniformHandle, textureAct[currentTexture]);
            Log.d(TAG,"Current texture" + textureAct[currentTexture]);
            shaderProgram.render(this.getmVertexBuffer(),this.getmTextureBuffer() , this.getmIndexBuffer());
        }

    }

    /**
     * Load the textures
     *
     */
    protected void loadGLTexture() {
        //Generate a number of texture, texture pointer...
        textures = new int[pathToTextures.size()];
        textureAct = new int[pathToTextures.size()];
        GLES20.glGenTextures(pathToTextures.size(), textures, 0);

        Bitmap bitmap = null;

        for (int i = 0; i < pathToTextures.size(); i++) {
            // Create a bitmap
            bitmap = getBitmapFromAsset(context, pathToTextures.get(i));

            //...and bind it to our array
            textureAct[i] = stack.removeFirst();
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureAct[i]);
            GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);

            //Create Nearest Filtered Texture
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

            //Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

            //Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

            //Clean up
            bitmap = null;
        }
        finished = true;
    }

    /**
     * Return bitmap from file
     * @param context
     * @param filePath
     * @return Bitmap type
     */
    protected Bitmap getBitmapFromAsset(Context context, String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }
}

