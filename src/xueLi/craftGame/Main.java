package xueLi.craftGame;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Vector3f;

import xueLi.craftGame.block.Block;
import xueLi.craftGame.entity.Player;
import xueLi.craftGame.entity.renderer.EntityRenderer;
import xueLi.craftGame.utils.Vector;
import xueLi.craftGame.utils.BlockPos;
import xueLi.craftGame.utils.DisplayManager;
import xueLi.craftGame.utils.FPSTimer;
import xueLi.craftGame.utils.GLHelper;
import xueLi.craftGame.utils.MousePicker;
import xueLi.craftGame.utils.VertexBuffer;
import xueLi.craftGame.world.World;

public class Main {

	private static int width = 1200, height = 680;

	private static Player player = new Player(8, 8, 8);
	private static float resistant = 0.005f;
	private static float sensivity = 0.1f;

	private static BlockPos block_select, last_block_select;
	private static long placeTimeCount;

	public static void main(String[] args) throws IOException {
		DisplayManager.create(width, height);

		int textureID = GLHelper.registerTexture("res/textures.png");

		FloatBuffer buffer;

		Block.init();
		World w = new World(10, 10);
		EntityRenderer.bindWorld(w);
		
		VertexBuffer.init();

		Mouse.setGrabbed(true);
		while (DisplayManager.isRunning()) {
			if (DisplayManager.isMouseDown(0) & block_select != null
					& DisplayManager.currentTime - placeTimeCount > 100) {
				w.setBlock(block_select, 0);
				placeTimeCount = DisplayManager.currentTime;
			}

			if (DisplayManager.isMouseDown(1) & block_select != null
					& DisplayManager.currentTime - placeTimeCount > 100) {
				w.setBlock(last_block_select, 1);
				placeTimeCount = DisplayManager.currentTime;
			}

			if (DisplayManager.isKeyDown(Keyboard.KEY_ESCAPE)) {
				DisplayManager.postDestroyMessage();
			}

			player.tick(w);

			FPSTimer.getFPS();

			GLHelper.clearColor(0.5f, 0.8f, 1.0f, 1.0f);

			buffer = VertexBuffer.map();
			int v = w.draw(player, buffer);

			buffer.flip();

			GL11.glMatrixMode(GL11.GL_MODELVIEW);

			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
			VertexBuffer.draw(GL11.GL_TRIANGLES, v);

			buffer.clear();

			int error = GL11.glGetError();
			if (error != 0) {
				System.out.println(error);
			}

			if (DisplayManager.tickResize()) {
				GLHelper.perspecive(DisplayManager.d_width, DisplayManager.d_height, 90.0f, 0.1f, 1000.0f);
			}
			GLHelper.player(player);
			GLHelper.calculateFrustumPlane();

			if (block_select != null) {

			}

			buffer.clear();

			DisplayManager.update();

			block_select = null;
			MousePicker.ray(player.pos);
			for (float distance = 0; distance < 8; distance += 0.05f) {
				BlockPos searching_block_pos = MousePicker.getPointOnRay(distance);
				if (w.hasBlock(searching_block_pos)) {
					block_select = searching_block_pos;
					break;
				}
				last_block_select = searching_block_pos;
			}

		}

		GLHelper.deleteTexture(textureID);

		DisplayManager.destroy();

	}
}
