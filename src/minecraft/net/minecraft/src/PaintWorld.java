/*  
 * The Paint Mod
 * Copyright (C) 2012  Hippoplatimus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.minecraft.src;

import java.util.ArrayList;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

// CORE CLASS
public class PaintWorld {

	public static final PaintWorld instance = new PaintWorld();
	private static final int BRUSH_RADIUS = 4;
	private static final boolean[] brushMap = {
		true,  true,  true,  true,
		true,  true,  true,  true,
		true,  true,  true,  false,
		true,  true,  false, false
	};
	
	public static final int ROW = 16;
	public static final int ARRAY_SIZE = ROW * ROW * 4;
	
	private boolean paintDisabled = true;
	private ArrayList<PaintBlock> paintedBlocks = new ArrayList<PaintBlock>();
	
	public static class PaintBlock {
		public int x;
		public int y;
		public int z;
		public PaintFace[] paintedFaces;
		
		public PaintBlock(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		@Override
		public boolean equals(Object other) {
			if (other == null) return false;
			if (!(other instanceof PaintBlock)) return false;
			PaintBlock block = (PaintBlock) other;
			return x == block.x && y == block.y && z == block.z;
		}
	}
	public static class PaintFace {
		public byte[] points;
		
		public PaintFace() {
			points = new byte[ARRAY_SIZE];
		}
	}
	
	private MovingObjectPosition position;
	private PaintBlock selectedBlock;
	private boolean paintCommitted;
	
	private PaintBlock renderBlock;
	private int face;
	private int ix;
	private int iy;
	
	private ByteBuffer textureBuffer;
	private int textureId;
	
	private ArrayList<PaintBlock> blockSendQueue = new ArrayList<PaintBlock>();
	
	public void flushSendQueue(NetClientHandler nethandler) {
		if (nethandler == null)
			return;
		for (PaintBlock block : blockSendQueue) {
			nethandler.addToSendQueue(new Packet132UpdatePaint(block));
		}
		blockSendQueue.clear();
	}
	
	public void setPaint(PaintBlock block) {
		paintedBlocks.remove(block);
		paintedBlocks.add(block);
	}
	public void clearPaint() {
		paintedBlocks.clear();
	}
	
	private double getFloating(double n) {
		double r;
		if (n >= 0)
			r = n - Math.floor(n);
		else
			r = 1 - (Math.ceil(n) - n);
		if (r >= 1) r = 0.99999999;
		if (r < 0) r = 0;
		return r;
	}
	public void possiblePaint(MovingObjectPosition position) {
		this.position = position;
	}
	private void createPaint(World world) {
		if (position.typeOfHit != EnumMovingObjectType.TILE) return;
		if (selectedBlock == null || selectedBlock.x != position.blockX || selectedBlock.y != position.blockY || selectedBlock.z != position.blockZ) {
			PaintBlock block = new PaintBlock(position.blockX, position.blockY, position.blockZ);
			int i = paintedBlocks.indexOf(block);
			if (i == -1) {
				block.paintedFaces = new PaintFace[6];
				selectedBlock = block;
				paintedBlocks.add(block);
			}
			else {
				selectedBlock = paintedBlocks.get(i);
			}
		}
		face = position.sideHit;
		if (!checkFace(world, selectedBlock, face)) {
			selectedBlock.paintedFaces[face] = null;
			selectedBlock = null;
			return;
		}
		if (selectedBlock.paintedFaces[face] == null)
			selectedBlock.paintedFaces[face] = new PaintFace();
		double xpos = getFloating(position.hitVec.xCoord);
		double ypos = getFloating(position.hitVec.yCoord);
		double zpos = getFloating(position.hitVec.zCoord);
		double major = 0;
		double minor = 0;
		switch (face) {
			case 0:
			case 1:
				major = zpos;
				minor = xpos;
				break;
			case 2:
				major = 1 - ypos;
				minor = 1 - xpos;
				break;
			case 3:
				major = 1 - ypos;
				minor = xpos;
				break;
			case 4:
				major = 1 - ypos;
				minor = zpos;
				break;
			case 5:
				major = 1 - ypos;
				minor = 1 - zpos;
				break;
		}
		iy = (int) (major * ROW);
		ix = (int) (minor * ROW);
		paintCommitted = false;
	}
	public void commitPaint(World world, int[] color, boolean detail) {
		if (selectedBlock == null) {
			createPaint(world);
			if (selectedBlock == null)
				return;
		}
		PaintFace pface = selectedBlock.paintedFaces[face];
		if (!colorIfInBounds(pface, ix, iy, color))
			colorWorldBlock(world, ix, iy, color);
		if (!detail) {
			for (int u = 0; u < BRUSH_RADIUS; ++u) {
				for (int v = 0; v < BRUSH_RADIUS; ++v) {
					if (u == 0 && v == 0) continue;
					if (!brushMap[(u * BRUSH_RADIUS) + v]) continue;
					if (!colorIfInBounds(pface, ix - u, iy - v, color))
						colorWorldBlock(world, ix - u, iy - v, color);
					if (!colorIfInBounds(pface, ix + u, iy - v, color))
						colorWorldBlock(world, ix + u, iy - v, color);
					if (!colorIfInBounds(pface, ix + u, iy + v, color))
						colorWorldBlock(world, ix + u, iy + v, color);
					if (!colorIfInBounds(pface, ix - u, iy + v, color))
						colorWorldBlock(world, ix - u, iy + v, color);
				}
			}
		}
		world.markBlockAsNeedsUpdate(selectedBlock.x, selectedBlock.y, selectedBlock.z);
		blockSendQueue.remove(selectedBlock);
		blockSendQueue.add(selectedBlock);
		paintCommitted = true;
	}
	public boolean paintClickEnd() {
		selectedBlock = null;
		return paintCommitted;
	}
	public void resetPaintCommitted() {
		paintCommitted = false;
	}
	
	private boolean checkFace(World world, PaintBlock block, int face) {
		int blockId = world.getBlockId(block.x, block.y, block.z);
		Block b = Block.blocksList[blockId];
		if (b == null || b.getRenderType() != 0 || blockId == Block.portal.blockID)
			return false;
		switch (face) {
		case 0:
			blockId = world.getBlockId(block.x, block.y - 1, block.z);
			break;
		case 1:
			blockId = world.getBlockId(block.x, block.y + 1, block.z);
			break;
		case 2:
			blockId = world.getBlockId(block.x, block.y, block.z - 1);
			break;
		case 3:
			blockId = world.getBlockId(block.x, block.y, block.z + 1);
			break;
		case 4:
			blockId = world.getBlockId(block.x - 1, block.y, block.z);
			break;
		case 5:
			blockId = world.getBlockId(block.x + 1, block.y, block.z);
			break;
		}
		b = Block.blocksList[blockId];
		if (b == null) return true;
		if (b instanceof BlockStationary || b instanceof BlockFlowing)
			return false;
		if (!b.isOpaqueCube()) return true;
		return false;
	}
	
	private boolean colorIfInBounds(PaintFace pface, int x, int y, int[] color) {
		if (x < 0 || x >= ROW || y < 0 || y >= ROW)
			return false;
		int i = ((y * ROW) + x) * 4;
		if (color == null) {
			pface.points[i + 0] = (byte) -128;
			pface.points[i + 1] = (byte) -128;
			pface.points[i + 2] = (byte) -128;
			pface.points[i + 3] = (byte) -128;
		}
		else {
			pface.points[i + 0] = (byte) (color[0] - 128);
			pface.points[i + 1] = (byte) (color[1] - 128);
			pface.points[i + 2] = (byte) (color[2] - 128);
			pface.points[i + 3] = (byte) (255 - 128);
		}
		return true;
	}
	private void colorWorldBlock(World world, int u, int v, int[] color) {
		int umod = 0;
		int vmod = 0;
		if (u < 0) {
			u += ROW;
			umod = -1;
		}
		else if (u >= ROW) {
			u -= ROW;
			umod = 1;
		}
		if (v < 0) {
			v += ROW;
			vmod = -1;
		}
		else if (v >= ROW) {
			v -= ROW;
			vmod = 1;
		}
		int x = selectedBlock.x;
		int y = selectedBlock.y;
		int z = selectedBlock.z;
		switch (face) {
			case 0:
			case 1:
				z += vmod;
				x += umod;
				break;
			case 2:
				x -= umod;
				y -= vmod;
				break;
			case 3:
				x += umod;
				y -= vmod;
				break;
			case 4:
				z += umod;
				y -= vmod;
				break;
			case 5:
				z -= umod;
				y -= vmod;
				break;
		}
		if (world.getBlockId(x, y, z) == 0)
			return;
		PaintBlock block = new PaintBlock(x, y, z);
		int i = paintedBlocks.indexOf(block);
		if (i == -1) {
			block.paintedFaces = new PaintFace[6];
			paintedBlocks.add(block);
		}
		else {
			block = paintedBlocks.get(i);
		}
		if (!checkFace(world, block, face))
			return;
		if (block.paintedFaces[face] == null)
			block.paintedFaces[face] = new PaintFace();
		colorIfInBounds(block.paintedFaces[face], u, v, color);
		world.markBlockAsNeedsUpdate(x, y, z);
		blockSendQueue.remove(block);
		blockSendQueue.add(block);
		paintCommitted = true;
	}
	
	public void setPaintBlock(int x, int y, int z) {
		this.paintDisabled = false;
		
		int i = paintedBlocks.indexOf(new PaintBlock(x, y, z));
		if (i == -1)
			renderBlock = null;
		else
			renderBlock = paintedBlocks.get(i);
	}
	public void getTextureForFace(int face) {
		PaintFace pface = renderBlock.paintedFaces[face];
		if (textureBuffer == null) {
			textureBuffer = ByteBuffer.allocateDirect(ARRAY_SIZE);
			textureBuffer.order(ByteOrder.nativeOrder());
		}
		if (textureId <= 0) {
			textureId = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		textureBuffer.clear();
		textureBuffer.put(pface.points);
		textureBuffer.flip();
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, ROW, ROW, 0, GL11.GL_RGBA, GL11.GL_BYTE, textureBuffer);
	}
	public void endPaintRender() {
		this.paintDisabled = true;
	}
	
	public boolean paintOnBlock() {
		if (paintDisabled) return false;
		return renderBlock != null;
	}
	public boolean paintOnFace(int face) {
		if (paintDisabled) return false;
		return renderBlock.paintedFaces[face] != null;
	}
	
	public void blockChanged(World world, int x, int y, int z, int blockId) {
		PaintBlock block = new PaintBlock(x, y, z);
		paintedBlocks.remove(block);
	}
	public void neighborBlockChanged(World world, int x, int y, int z, int blockId) {
		PaintBlock block = new PaintBlock(x, y, z);
		int i = paintedBlocks.indexOf(block);
		if (i == -1) return;
		block = paintedBlocks.get(i);
		int blankFaces = 0;
		for (int j = 0; j < 6; ++j) {
			if (!checkFace(world, block, j))
				block.paintedFaces[j] = null;
			if (block.paintedFaces[j] == null)
				++blankFaces;
		}
		if (blankFaces == 6)
			paintedBlocks.remove(block);
		world.markBlockAsNeedsUpdate(x, y, z);
	}
	
	public void save(File saveDirectory) {
		File file = new File(saveDirectory, "world.paint");
		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			out.writeInt(paintedBlocks.size());
			for (PaintBlock block : paintedBlocks) {
				out.writeInt(block.x);
				out.writeInt(block.y);
				out.writeInt(block.z);
				for (int i = 0; i < 6; ++i) {
					if (block.paintedFaces[i] != null) {
						out.writeBoolean(true);
						out.write(block.paintedFaces[i].points);
					}
					else {
						out.writeBoolean(false);
					}
				}
			}
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void load(File saveDirectory) {
		clearPaint();
		try {
			File file = new File(saveDirectory, "world.paint");
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			int size = in.readInt();
			for (int j = 0; j < size; ++j) {
				PaintBlock block = new PaintBlock(in.readInt(), in.readInt(), in.readInt());
				block.paintedFaces = new PaintFace[6];
				for (int i = 0; i < 6; ++i) {
					if (in.readBoolean()) {
						block.paintedFaces[i] = new PaintFace();
						in.read(block.paintedFaces[i].points);
					}
				}
				paintedBlocks.add(block);
			}
			in.close();
		}
		catch (FileNotFoundException e) {
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}