// This file was modified to add the paintbrush recipes. If you port to ModLoader
// or something, you can replace this code with ModLoader recipe-adding code.

// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;


// Referenced classes of package net.minecraft.src:
//            ItemStack, Block, CraftingManager, Item

public class RecipesCrafting
{

    public RecipesCrafting()
    {
    }

    public void addRecipes(CraftingManager craftingmanager)
    {
        craftingmanager.addRecipe(new ItemStack(Block.chest), new Object[] {
            "###", "# #", "###", Character.valueOf('#'), Block.planks
        });
        craftingmanager.addRecipe(new ItemStack(Block.stoneOvenIdle), new Object[] {
            "###", "# #", "###", Character.valueOf('#'), Block.cobblestone
        });
        craftingmanager.addRecipe(new ItemStack(Block.workbench), new Object[] {
            "##", "##", Character.valueOf('#'), Block.planks
        });
        craftingmanager.addRecipe(new ItemStack(Block.sandStone), new Object[] {
            "##", "##", Character.valueOf('#'), Block.sand
        });
        craftingmanager.addRecipe(new ItemStack(Block.stoneBrick, 4), new Object[] {
            "##", "##", Character.valueOf('#'), Block.stone
        });
        craftingmanager.addRecipe(new ItemStack(Block.fenceIron, 16), new Object[] {
            "###", "###", Character.valueOf('#'), Item.ingotIron
        });
        craftingmanager.addRecipe(new ItemStack(Block.thinGlass, 16), new Object[] {
            "###", "###", Character.valueOf('#'), Block.glass
        });
        
        craftingmanager.addRecipe(new ItemStack(ItemPaintbrush.paintbrush), new Object[] {
            "#", "X", Character.valueOf('#'), Block.cloth, Character.valueOf('X'), Item.stick
        });
		craftingmanager.addRecipe(new ItemStack(ItemPaintbrush.paintrem), new Object[] {
            "#", "X", Character.valueOf('#'), Item.ingotIron, Character.valueOf('X'), Item.stick
        });
		craftingmanager.addRecipe(new ItemStack(ItemPaintbrush.paintdetail), new Object[] {
            "#", "X", Character.valueOf('#'), Item.feather, Character.valueOf('X'), Item.stick
        });
		craftingmanager.addRecipe(new ItemStack(ItemPaintbrush.paintremdetail), new Object[] {
            "#", "X", Character.valueOf('#'), Item.flint, Character.valueOf('X'), Item.stick
        });
		for(int i = 0; i < 16; i++)
        {
            craftingmanager.addShapelessRecipe(new ItemStack(ItemPaintbrush.paintbrushA, 1, i), new Object[] {
                new ItemStack(Item.dyePowder, 1, i), new ItemStack(ItemPaintbrush.paintbrush, 1, 0)
            });
            craftingmanager.addShapelessRecipe(new ItemStack(ItemPaintbrush.paintdetailA, 1, i), new Object[] {
                new ItemStack(Item.dyePowder, 1, i), new ItemStack(ItemPaintbrush.paintdetail, 1, 0)
            });
        }
    }
}
