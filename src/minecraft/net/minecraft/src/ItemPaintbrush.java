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


// Referenced classes of package net.minecraft.src:
//            Item, EntitySheep, BlockCloth, World, 
//            BlockSapling, ItemStack, BlockCrops, Block, 
//            EntityPlayer, EntityLiving

public class ItemPaintbrush extends Item
{

	public static boolean isPaintbrush(int id) {
		id -= 256;
		return id >= 1000 && id <= 1005;
	}
	public static boolean hasPaint(Item item) {
		return (item instanceof ItemPaintbrush) && ((ItemPaintbrush) item).hasPaint;
	}
	public static ItemStack getWashedBrush(ItemStack item) {
		if (item.itemID - 256 == 1004) {
			return new ItemStack(paintbrush, item.stackSize);
		}
		else if (item.itemID - 256 == 1005) {
			return new ItemStack(paintdetail, item.stackSize);
		}
		return item;
	}
	
	public static Item paintbrush = new ItemPaintbrush(1000, false, false).setIconCoord(0, 0).setItemName("paintbrush");
	public static Item paintdetail = new ItemPaintbrush(1001, false, true).setIconCoord(2, 0).setItemName("paintbrush");
	public static Item paintrem = new ItemPaintbrush(1002, false, false).setIconCoord(1, 0).setItemName("paintrem");
	public static Item paintremdetail = new ItemPaintbrush(1003, false, true).setIconCoord(3, 0).setItemName("paintrem2");
	public static Item paintbrushA = new ItemPaintbrush(1004, true, false).setIconCoord(0, 1).setItemName("paintbrush");
	public static Item paintdetailA = new ItemPaintbrush(1005, true, true).setIconCoord(2, 1).setItemName("paintbrush");

	private boolean hasPaint;
	private boolean detail;
	
    public ItemPaintbrush(int i, boolean hasPaint, boolean detail)
    {
        super(i);
		if (hasPaint) {
			setHasSubtypes(true);
			setMaxDamage(0);
		}
		maxStackSize = 1;
		this.hasPaint = hasPaint;
		this.detail = detail;
    }

    public int getIconFromDamage(int i)
    {
        return iconIndex + (i % 8) * 16 + i / 8;
    }

    public String getItemNameIS(ItemStack itemstack)
    {
		if (!hasPaint)
			return super.getItemName();
        return super.getItemName() + "." + dyeColors[itemstack.getItemDamage()];
    }

    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int i, int j, int k, int l)
    {
		if (!hasPaint) {
			if (itemstack.getItem() == paintrem || itemstack.getItem() == paintremdetail) {
				PaintWorld.instance.commitPaint(world, null, detail);
				return true;
			}
			return false;
		}
		PaintWorld.instance.commitPaint(world, rgbColors[itemstack.getItemDamage()], detail);
		return true;
    }

    public void saddleEntity(ItemStack itemstack, EntityLiving entityliving)
    {
        
    }

    public static final String dyeColors[] = {
        "black", "red", "green", "brown", "blue", "purple", "cyan", "silver", "gray", "pink", 
        "lime", "yellow", "lightBlue", "magenta", "orange", "white"
    };
	public static final int rgbColors[][] = {
        {48, 48, 48}, {241, 0, 0}, {63, 178, 63}, {144, 108, 69}, {68, 68, 173}, {156, 65, 175},
		{99, 155, 163}, {200, 200, 200}, {140, 140, 140}, {249, 170, 194}, {46, 229, 117},
		{240, 230, 0}, {124, 163, 219}, {231, 36, 149}, {222, 151, 18}, {248, 248, 248}
    };

}
