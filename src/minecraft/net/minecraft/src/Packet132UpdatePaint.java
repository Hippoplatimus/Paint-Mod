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

// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

import java.io.*;
import java.lang.reflect.Field;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

// Referenced classes of package net.minecraft.src:
//            Packet, NetHandler

public class Packet132UpdatePaint extends Packet
{

	private int dataSize;
	private int faceMask;
	private PaintWorld.PaintBlock block;
	
	private static Field worldField;
	
	static 
    {
		try {
			worldField = NetClientHandler.class.getDeclaredFields()[4];
			worldField.setAccessible(true);
		}
		catch (SecurityException e) {
			
		}
    }
	
    public Packet132UpdatePaint()
    {   
    	block = new PaintWorld.PaintBlock(0, 0, 0);
    	dataSize = 0;
    	faceMask = 0;
    }
    
    public Packet132UpdatePaint(PaintWorld.PaintBlock block)
    {
    	this.block = block;
    	dataSize = 0;
    	faceMask = 0;
    	if (block.paintedFaces != null) {
	    	for (int i = 0; i < 6; ++i) {
	    		if (block.paintedFaces[i] != null) {
	    			dataSize += PaintWorld.ARRAY_SIZE;
	    			faceMask |= (1 << i);
	    		}
	    	}
    	}
    }
    
    public void readPacketData(DataInputStream datainputstream)
        throws IOException
    {
        block.x = datainputstream.readInt();
        block.y = datainputstream.readShort();
        block.z = datainputstream.readInt();
        faceMask = datainputstream.readByte();
        dataSize = datainputstream.readInt();
        if (dataSize == 0) return;
        if (dataSize == 0) return;
        //byte[] bytes = new byte[dataSize];
        //datainputstream.readFully(bytes);
        byte[] data = new byte[dataSize]; // PaintWorld.ARRAY_SIZE * 6
        /*Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        try
        {
            inflater.inflate(data);
        }
        catch(DataFormatException dataformatexception)
        {
            throw new IOException("Bad compressed data format");
        }
        finally
        {
            inflater.end();
        }*/
        datainputstream.readFully(data);
        if (block.paintedFaces == null)
        	block.paintedFaces = new PaintWorld.PaintFace[6];
        int offset = 0;
        for (int i = 0; i < 6; ++i) {
        	int flag = 1 << i;
        	if ((faceMask & flag) != 0) {
        		block.paintedFaces[i] = new PaintWorld.PaintFace();
        		System.arraycopy(data, offset, block.paintedFaces[i].points, 0, PaintWorld.ARRAY_SIZE);
        		offset += PaintWorld.ARRAY_SIZE;
        	}
        }
    }

    public void writePacketData(DataOutputStream dataoutputstream)
        throws IOException
    {
    	dataoutputstream.writeInt(block.x);
        dataoutputstream.writeShort(block.y);
        dataoutputstream.writeInt(block.z);
        dataoutputstream.writeByte(faceMask);
        if (dataSize != 0) {
        	byte[] data = new byte[dataSize];
        	int offset = 0;
        	for (int i = 0; i < 6; ++i) {
        		if (block.paintedFaces[i] != null) {
        			System.arraycopy(block.paintedFaces[i].points, 0, data, offset, PaintWorld.ARRAY_SIZE);
            		offset += PaintWorld.ARRAY_SIZE;
        		}
        	}
        	/*Deflater deflater = new Deflater();
        	deflater.setInput(data);
        	deflater.finish();
        	byte[] compressed = new byte[dataSize];
        	dataSize = deflater.deflate(compressed);*/
        	dataoutputstream.writeInt(dataSize);
        	//dataoutputstream.write(compressed, 0, dataSize);
        	dataoutputstream.write(data);
        }
        else {
        	dataoutputstream.writeInt(0);
        }
    }

    public void processPacket(NetHandler nethandler)
    {
    	PaintWorld.instance.setPaint(block);
    	try {
    		World world = (World) worldField.get(nethandler);
    		world.markBlockAsNeedsUpdate(block.x, block.y, block.z);
    	}
    	catch (IllegalArgumentException e) {
    		
    	}
    	catch (IllegalAccessException e) {
    		
    	}
    }

    public int getPacketSize()
    {
        return 4 + 2 + 4 + 1 + 4 + dataSize;
    }

}
