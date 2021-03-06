package de.openglapp.scene;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import de.openglapp.window.GameWindow;

public class Player extends GameObject{

	@Override
	public void update()
	{
		if(GameWindow.CURRENTWINDOW.isKeyDown("D"))
			moveOffset(0.1f, 0, 0);
		if(GameWindow.CURRENTWINDOW.isKeyDown("A"))
			moveOffset(-0.1f, 0, 0);
		if(GameWindow.CURRENTWINDOW.isKeyDown("W"))
			moveOffset(0, 0, -0.1f);
		if(GameWindow.CURRENTWINDOW.isKeyDown("S"))
			moveOffset(0, 0, 0.1f);
		
		
		if(GameWindow.CURRENTWINDOW.isKeyDown("Q"))
			moveOffset(0, -0.1f, 0);
		if(GameWindow.CURRENTWINDOW.isKeyDown("E"))
			moveOffset(0, 0.1f, 0);
		
		if(GameWindow.CURRENTWINDOW.isKeyDown("R"))
			addRotationY(0.1f, true);
		
		if(GameWindow.CURRENTWINDOW.isKeyDown("T"))
			addRotationY(-0.1f, true);
		
		updateModelMatrix();
		
		ArrayList<Vector3f> collisionList = GameWindow.CURRENTSCENE.getCollisionMTVsFor(this);
		for(Vector3f mtv : collisionList)
		{
			moveOffset(mtv);
			updateModelMatrix();
		}
	}
}
