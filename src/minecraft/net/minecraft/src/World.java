// Modified to save and load paint, as well as erase paint when blocks
// are placed and destroyed.

// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import java.io.PrintStream;
import java.util.*;

// Referenced classes of package net.minecraft.src:
//            IBlockAccess, WorldProvider, WorldInfo, MapStorage, 
//            ISaveHandler, ChunkProvider, WorldChunkManager, ChunkPosition, 
//            EntityPlayer, ChunkProviderLoadOrGenerate, MathHelper, IChunkProvider, 
//            IProgressUpdate, Chunk, Material, Block, 
//            IWorldAccess, EnumSkyBlock, Vec3D, Entity, 
//            AxisAlignedBB, BiomeGenBase, NextTickListEntry, Profiler, 
//            TileEntity, BlockFire, BlockFluid, Explosion, 
//            ThreadedFileIOBase, SpawnerAnimals, ChunkCoordIntPair, EntityLightningBolt, 
//            ChunkCache, PathFinder, PlayerCapabilities, ChunkCoordinates, 
//            WeightedRandom, SpawnListEntry, WorldSettings, MovingObjectPosition, 
//            PathEntity, WorldSavedData, EnumCreatureType

public class World
    implements IBlockAccess
{

    public int field_35473_a;
    public int field_35471_b;
    public int field_35472_c;
    public int field_35469_d;
    public int field_35470_e;
    public boolean scheduledUpdatesAreImmediate;
    public List loadedEntityList;
    private List unloadedEntityList;
    private TreeSet scheduledTickTreeSet;
    private Set scheduledTickSet;
    public List loadedTileEntityList;
    private List addedTileEntityList;
    private List entityRemoval;
    public List playerEntities;
    public List weatherEffects;
    private long cloudColour;
    public int skylightSubtracted;
    protected int updateLCG;
    protected final int DIST_HASH_MAGIC = 0x3c6ef35f;
    protected float prevRainingStrength;
    protected float rainingStrength;
    protected float prevThunderingStrength;
    protected float thunderingStrength;
    protected int lastLightningBolt;
    public int lightningFlash;
    public boolean editingBlocks;
    private long lockTimestamp;
    protected int autosavePeriod;
    public int difficultySetting;
    public Random rand;
    public boolean isNewWorld;
    public final WorldProvider worldProvider;
    protected List worldAccesses;
    protected IChunkProvider chunkProvider;
    protected final ISaveHandler saveHandler;
    protected WorldInfo worldInfo;
    public boolean findingSpawnPoint;
    private boolean allPlayersSleeping;
    public MapStorage mapStorage;
    private ArrayList collidingBoundingBoxes;
    private boolean scanningTileEntities;
    protected boolean spawnHostileMobs;
    protected boolean spawnPeacefulMobs;
    private Set positionsToUpdate;
    private int soundCounter;
    int lightUpdateBlockList[];
    private List field_1012_M;
    public boolean multiplayerWorld;

    public WorldChunkManager getWorldChunkManager()
    {
        return worldProvider.worldChunkMgr;
    }

    public World(ISaveHandler isavehandler, String s, WorldProvider worldprovider, WorldSettings worldsettings)
    {
        field_35473_a = 7;
        field_35471_b = field_35473_a + 4;
        field_35472_c = 1 << field_35473_a;
        field_35469_d = field_35472_c - 1;
        field_35470_e = field_35472_c / 2 - 1;
        scheduledUpdatesAreImmediate = false;
        loadedEntityList = new ArrayList();
        unloadedEntityList = new ArrayList();
        scheduledTickTreeSet = new TreeSet();
        scheduledTickSet = new HashSet();
        loadedTileEntityList = new ArrayList();
        addedTileEntityList = new ArrayList();
        entityRemoval = new ArrayList();
        playerEntities = new ArrayList();
        weatherEffects = new ArrayList();
        cloudColour = 0xffffffL;
        skylightSubtracted = 0;
        updateLCG = (new Random()).nextInt();
        lastLightningBolt = 0;
        lightningFlash = 0;
        editingBlocks = false;
        lockTimestamp = System.currentTimeMillis();
        autosavePeriod = 40;
        rand = new Random();
        isNewWorld = false;
        worldAccesses = new ArrayList();
        collidingBoundingBoxes = new ArrayList();
        spawnHostileMobs = true;
        spawnPeacefulMobs = true;
        positionsToUpdate = new HashSet();
        soundCounter = rand.nextInt(12000);
        lightUpdateBlockList = new int[32768];
        field_1012_M = new ArrayList();
        multiplayerWorld = false;
        saveHandler = isavehandler;
        worldInfo = new WorldInfo(worldsettings, s);
        worldProvider = worldprovider;
        mapStorage = new MapStorage(isavehandler);
        worldprovider.registerWorld(this);
        chunkProvider = getChunkProvider();
        calculateInitialSkylight();
        calculateInitialWeather();
        PaintWorld.instance.clearPaint();
    }

    public World(World world, WorldProvider worldprovider)
    {
        field_35473_a = 7;
        field_35471_b = field_35473_a + 4;
        field_35472_c = 1 << field_35473_a;
        field_35469_d = field_35472_c - 1;
        field_35470_e = field_35472_c / 2 - 1;
        scheduledUpdatesAreImmediate = false;
        loadedEntityList = new ArrayList();
        unloadedEntityList = new ArrayList();
        scheduledTickTreeSet = new TreeSet();
        scheduledTickSet = new HashSet();
        loadedTileEntityList = new ArrayList();
        addedTileEntityList = new ArrayList();
        entityRemoval = new ArrayList();
        playerEntities = new ArrayList();
        weatherEffects = new ArrayList();
        cloudColour = 0xffffffL;
        skylightSubtracted = 0;
        updateLCG = (new Random()).nextInt();
        lastLightningBolt = 0;
        lightningFlash = 0;
        editingBlocks = false;
        lockTimestamp = System.currentTimeMillis();
        autosavePeriod = 40;
        rand = new Random();
        isNewWorld = false;
        worldAccesses = new ArrayList();
        collidingBoundingBoxes = new ArrayList();
        spawnHostileMobs = true;
        spawnPeacefulMobs = true;
        positionsToUpdate = new HashSet();
        soundCounter = rand.nextInt(12000);
        lightUpdateBlockList = new int[32768];
        field_1012_M = new ArrayList();
        multiplayerWorld = false;
        lockTimestamp = world.lockTimestamp;
        saveHandler = world.saveHandler;
        worldInfo = new WorldInfo(world.worldInfo);
        mapStorage = new MapStorage(saveHandler);
        worldProvider = worldprovider;
        worldprovider.registerWorld(this);
        chunkProvider = getChunkProvider();
        calculateInitialSkylight();
        calculateInitialWeather();
        PaintWorld.instance.clearPaint();
    }

    public World(ISaveHandler isavehandler, String s, WorldSettings worldsettings)
    {
        this(isavehandler, s, worldsettings, ((WorldProvider) (null)));
    }

    public World(ISaveHandler isavehandler, String s, WorldSettings worldsettings, WorldProvider worldprovider)
    {
        field_35473_a = 7;
        field_35471_b = field_35473_a + 4;
        field_35472_c = 1 << field_35473_a;
        field_35469_d = field_35472_c - 1;
        field_35470_e = field_35472_c / 2 - 1;
        scheduledUpdatesAreImmediate = false;
        loadedEntityList = new ArrayList();
        unloadedEntityList = new ArrayList();
        scheduledTickTreeSet = new TreeSet();
        scheduledTickSet = new HashSet();
        loadedTileEntityList = new ArrayList();
        addedTileEntityList = new ArrayList();
        entityRemoval = new ArrayList();
        playerEntities = new ArrayList();
        weatherEffects = new ArrayList();
        cloudColour = 0xffffffL;
        skylightSubtracted = 0;
        updateLCG = (new Random()).nextInt();
        lastLightningBolt = 0;
        lightningFlash = 0;
        editingBlocks = false;
        lockTimestamp = System.currentTimeMillis();
        autosavePeriod = 40;
        rand = new Random();
        isNewWorld = false;
        worldAccesses = new ArrayList();
        collidingBoundingBoxes = new ArrayList();
        spawnHostileMobs = true;
        spawnPeacefulMobs = true;
        positionsToUpdate = new HashSet();
        soundCounter = rand.nextInt(12000);
        lightUpdateBlockList = new int[32768];
        field_1012_M = new ArrayList();
        multiplayerWorld = false;
        saveHandler = isavehandler;
        mapStorage = new MapStorage(isavehandler);
        worldInfo = isavehandler.loadWorldInfo();
        isNewWorld = worldInfo == null;
        if(worldprovider != null)
        {
            worldProvider = worldprovider;
        } else
        if(worldInfo != null && worldInfo.getDimension() != 0)
        {
            worldProvider = WorldProvider.getProviderForDimension(worldInfo.getDimension());
        } else
        {
            worldProvider = WorldProvider.getProviderForDimension(0);
        }
        boolean flag = false;
        if(worldInfo == null)
        {
            worldInfo = new WorldInfo(worldsettings, s);
            flag = true;
        } else
        {
            worldInfo.setWorldName(s);
        }
        worldProvider.registerWorld(this);
        chunkProvider = getChunkProvider();
        if(flag)
        {
            getInitialSpawnLocation();
        }
        calculateInitialSkylight();
        calculateInitialWeather();
        PaintWorld.instance.load(((SaveHandler) isavehandler).getSaveDirectory());
    }

    protected IChunkProvider getChunkProvider()
    {
        IChunkLoader ichunkloader = saveHandler.getChunkLoader(worldProvider);
        return new ChunkProvider(this, ichunkloader, worldProvider.getChunkProvider());
    }

    protected void getInitialSpawnLocation()
    {
        findingSpawnPoint = true;
        WorldChunkManager worldchunkmanager = getWorldChunkManager();
        List list = worldchunkmanager.getBiomesToSpawnIn();
        Random random = new Random(getWorldSeed());
        ChunkPosition chunkposition = worldchunkmanager.func_35556_a(0, 0, 256, list, random);
        int i = 0;
        int j = field_35472_c / 2;
        int k = 0;
        if(chunkposition != null)
        {
            i = chunkposition.x;
            k = chunkposition.z;
        } else
        {
            System.out.println("Unable to find spawn biome");
        }
        int l = 0;
        do
        {
            if(worldProvider.canCoordinateBeSpawn(i, k))
            {
                break;
            }
            i += random.nextInt(64) - random.nextInt(64);
            k += random.nextInt(64) - random.nextInt(64);
        } while(++l != 1000);
        worldInfo.setSpawn(i, j, k);
        findingSpawnPoint = false;
    }

    public ChunkCoordinates func_40472_j()
    {
        return worldProvider.func_40469_f();
    }

    public void setSpawnLocation()
    {
        if(worldInfo.getSpawnY() <= 0)
        {
            worldInfo.setSpawnY(field_35472_c / 2);
        }
        int i = worldInfo.getSpawnX();
        int j = worldInfo.getSpawnZ();
        int k = 0;
        do
        {
            if(getFirstUncoveredBlock(i, j) != 0)
            {
                break;
            }
            i += rand.nextInt(8) - rand.nextInt(8);
            j += rand.nextInt(8) - rand.nextInt(8);
        } while(++k != 10000);
        worldInfo.setSpawnX(i);
        worldInfo.setSpawnZ(j);
    }

    public int getFirstUncoveredBlock(int i, int j)
    {
        int k;
        for(k = field_35470_e; !isAirBlock(i, k + 1, j); k++) { }
        return getBlockId(i, k, j);
    }

    public void emptyMethod1()
    {
    }

    public void spawnPlayerWithLoadedChunks(EntityPlayer entityplayer)
    {
        try
        {
            NBTTagCompound nbttagcompound = worldInfo.getPlayerNBTTagCompound();
            if(nbttagcompound != null)
            {
                entityplayer.readFromNBT(nbttagcompound);
                worldInfo.setPlayerNBTTagCompound(null);
            }
            if(chunkProvider instanceof ChunkProviderLoadOrGenerate)
            {
                ChunkProviderLoadOrGenerate chunkproviderloadorgenerate = (ChunkProviderLoadOrGenerate)chunkProvider;
                int i = MathHelper.floor_float((int)entityplayer.posX) >> 4;
                int j = MathHelper.floor_float((int)entityplayer.posZ) >> 4;
                chunkproviderloadorgenerate.setCurrentChunkOver(i, j);
            }
            entityJoinedWorld(entityplayer);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void saveWorld(boolean flag, IProgressUpdate iprogressupdate)
    {
        if(!chunkProvider.canSave())
        {
            return;
        }
        if(iprogressupdate != null)
        {
            iprogressupdate.displaySavingString("Saving level");
        }
        saveLevel();
        if(iprogressupdate != null)
        {
            iprogressupdate.displayLoadingString("Saving chunks");
        }
        chunkProvider.saveChunks(flag, iprogressupdate);
    }

    private void saveLevel()
    {
        checkSessionLock();
        saveHandler.saveWorldInfoAndPlayer(worldInfo, playerEntities);
        mapStorage.saveAllData();
        if (!multiplayerWorld)
        	PaintWorld.instance.save(((SaveHandler) saveHandler).getSaveDirectory());
    }

    public boolean func_650_a(int i)
    {
        if(!chunkProvider.canSave())
        {
            return true;
        }
        if(i == 0)
        {
            saveLevel();
        }
        return chunkProvider.saveChunks(false, null);
    }

    public int getBlockId(int i, int j, int k)
    {
        if(i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380)
        {
            return 0;
        }
        if(j < 0)
        {
            return 0;
        }
        if(j >= field_35472_c)
        {
            return 0;
        } else
        {
            return getChunkFromChunkCoords(i >> 4, k >> 4).getBlockID(i & 0xf, j, k & 0xf);
        }
    }

    public boolean isAirBlock(int i, int j, int k)
    {
        return getBlockId(i, j, k) == 0;
    }

    public boolean blockExists(int i, int j, int k)
    {
        if(j < 0 || j >= field_35472_c)
        {
            return false;
        } else
        {
            return chunkExists(i >> 4, k >> 4);
        }
    }

    public boolean doChunksNearChunkExist(int i, int j, int k, int l)
    {
        return checkChunksExist(i - l, j - l, k - l, i + l, j + l, k + l);
    }

    public boolean checkChunksExist(int i, int j, int k, int l, int i1, int j1)
    {
        if(i1 < 0 || j >= field_35472_c)
        {
            return false;
        }
        i >>= 4;
        j >>= 4;
        k >>= 4;
        l >>= 4;
        i1 >>= 4;
        j1 >>= 4;
        for(int k1 = i; k1 <= l; k1++)
        {
            for(int l1 = k; l1 <= j1; l1++)
            {
                if(!chunkExists(k1, l1))
                {
                    return false;
                }
            }

        }

        return true;
    }

    private boolean chunkExists(int i, int j)
    {
        return chunkProvider.chunkExists(i, j);
    }

    public Chunk getChunkFromBlockCoords(int i, int j)
    {
        return getChunkFromChunkCoords(i >> 4, j >> 4);
    }

    public Chunk getChunkFromChunkCoords(int i, int j)
    {
        return chunkProvider.provideChunk(i, j);
    }

    public boolean setBlockAndMetadata(int i, int j, int k, int l, int i1)
    {
        if(i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380)
        {
            return false;
        }
        if(j < 0)
        {
            return false;
        }
        if(j >= field_35472_c)
        {
            return false;
        } else
        {
            Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
            boolean flag = chunk.setBlockIDWithMetadata(i & 0xf, j, k & 0xf, l, i1);
            updateAllLightTypes(i, j, k);
            return flag;
        }
    }

    public boolean setBlock(int i, int j, int k, int l)
    {
        if(i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380)
        {
            return false;
        }
        if(j < 0)
        {
            return false;
        }
        if(j >= field_35472_c)
        {
            return false;
        } else
        {
            Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
            boolean flag = chunk.setBlockID(i & 0xf, j, k & 0xf, l);
            updateAllLightTypes(i, j, k);
            return flag;
        }
    }

    public Material getBlockMaterial(int i, int j, int k)
    {
        int l = getBlockId(i, j, k);
        if(l == 0)
        {
            return Material.air;
        } else
        {
            return Block.blocksList[l].blockMaterial;
        }
    }

    public int getBlockMetadata(int i, int j, int k)
    {
        if(i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380)
        {
            return 0;
        }
        if(j < 0)
        {
            return 0;
        }
        if(j >= field_35472_c)
        {
            return 0;
        } else
        {
            Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
            i &= 0xf;
            k &= 0xf;
            return chunk.getBlockMetadata(i, j, k);
        }
    }

    public void setBlockMetadataWithNotify(int i, int j, int k, int l)
    {
        if(setBlockMetadata(i, j, k, l))
        {
            int i1 = getBlockId(i, j, k);
            if(Block.requiresSelfNotify[i1 & 0xff])
            {
                notifyBlockChange(i, j, k, i1);
            } else
            {
                notifyBlocksOfNeighborChange(i, j, k, i1);
            }
        }
    }

    public boolean setBlockMetadata(int i, int j, int k, int l)
    {
        if(i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380)
        {
            return false;
        }
        if(j < 0)
        {
            return false;
        }
        if(j >= field_35472_c)
        {
            return false;
        } else
        {
            Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
            i &= 0xf;
            k &= 0xf;
            return chunk.setBlockMetadata(i, j, k, l);
        }
    }

    public boolean setBlockWithNotify(int i, int j, int k, int l)
    {
        if(setBlock(i, j, k, l))
        {
            notifyBlockChange(i, j, k, l);
            return true;
        } else
        {
            return false;
        }
    }

    public boolean setBlockAndMetadataWithNotify(int i, int j, int k, int l, int i1)
    {
        if(setBlockAndMetadata(i, j, k, l, i1))
        {
            notifyBlockChange(i, j, k, l);
            return true;
        } else
        {
            return false;
        }
    }

    public void markBlockNeedsUpdate(int i, int j, int k)
    {
        for(int l = 0; l < worldAccesses.size(); l++)
        {
            ((IWorldAccess)worldAccesses.get(l)).markBlockAndNeighborsNeedsUpdate(i, j, k);
        }

    }

    protected void notifyBlockChange(int i, int j, int k, int l)
    {
    	PaintWorld.instance.blockChanged(this, i, j, k, l);
        markBlockNeedsUpdate(i, j, k);
        notifyBlocksOfNeighborChange(i, j, k, l);
    }

    public void markBlocksDirtyVertical(int i, int j, int k, int l)
    {
        if(k > l)
        {
            int i1 = l;
            l = k;
            k = i1;
        }
        if(!worldProvider.hasNoSky)
        {
            for(int j1 = k; j1 <= l; j1++)
            {
                updateLightByType(EnumSkyBlock.Sky, i, j1, j);
            }

        }
        markBlocksDirty(i, k, j, i, l, j);
    }

    public void markBlockAsNeedsUpdate(int i, int j, int k)
    {
        for(int l = 0; l < worldAccesses.size(); l++)
        {
            ((IWorldAccess)worldAccesses.get(l)).markBlockRangeNeedsUpdate(i, j, k, i, j, k);
        }

    }

    public void markBlocksDirty(int i, int j, int k, int l, int i1, int j1)
    {
        for(int k1 = 0; k1 < worldAccesses.size(); k1++)
        {
            ((IWorldAccess)worldAccesses.get(k1)).markBlockRangeNeedsUpdate(i, j, k, l, i1, j1);
        }

    }

    public void notifyBlocksOfNeighborChange(int i, int j, int k, int l)
    {
        notifyBlockOfNeighborChange(i - 1, j, k, l);
        notifyBlockOfNeighborChange(i + 1, j, k, l);
        notifyBlockOfNeighborChange(i, j - 1, k, l);
        notifyBlockOfNeighborChange(i, j + 1, k, l);
        notifyBlockOfNeighborChange(i, j, k - 1, l);
        notifyBlockOfNeighborChange(i, j, k + 1, l);
    }

    private void notifyBlockOfNeighborChange(int i, int j, int k, int l)
    {
        if(editingBlocks || multiplayerWorld)
        {
            return;
        }
        PaintWorld.instance.neighborBlockChanged(this, i, j, k, l);
        Block block = Block.blocksList[getBlockId(i, j, k)];
        if(block != null)
        {
            block.onNeighborBlockChange(this, i, j, k, l);
        }
    }

    public boolean canBlockSeeTheSky(int i, int j, int k)
    {
        return getChunkFromChunkCoords(i >> 4, k >> 4).canBlockSeeTheSky(i & 0xf, j, k & 0xf);
    }

    public int getFullBlockLightValue(int i, int j, int k)
    {
        if(j < 0)
        {
            return 0;
        }
        if(j >= field_35472_c)
        {
            j = field_35472_c - 1;
        }
        return getChunkFromChunkCoords(i >> 4, k >> 4).getBlockLightValue(i & 0xf, j, k & 0xf, 0);
    }

    public int getBlockLightValue(int i, int j, int k)
    {
        return getBlockLightValue_do(i, j, k, true);
    }

    public int getBlockLightValue_do(int i, int j, int k, boolean flag)
    {
        if(i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380)
        {
            return 15;
        }
        if(flag)
        {
            int l = getBlockId(i, j, k);
            if(l == Block.stairSingle.blockID || l == Block.tilledField.blockID || l == Block.stairCompactCobblestone.blockID || l == Block.stairCompactPlanks.blockID)
            {
                int i1 = getBlockLightValue_do(i, j + 1, k, false);
                int j1 = getBlockLightValue_do(i + 1, j, k, false);
                int k1 = getBlockLightValue_do(i - 1, j, k, false);
                int l1 = getBlockLightValue_do(i, j, k + 1, false);
                int i2 = getBlockLightValue_do(i, j, k - 1, false);
                if(j1 > i1)
                {
                    i1 = j1;
                }
                if(k1 > i1)
                {
                    i1 = k1;
                }
                if(l1 > i1)
                {
                    i1 = l1;
                }
                if(i2 > i1)
                {
                    i1 = i2;
                }
                return i1;
            }
        }
        if(j < 0)
        {
            return 0;
        }
        if(j >= field_35472_c)
        {
            j = field_35472_c - 1;
        }
        Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
        i &= 0xf;
        k &= 0xf;
        return chunk.getBlockLightValue(i, j, k, skylightSubtracted);
    }

    public int getHeightValue(int i, int j)
    {
        if(i < 0xfe363c80 || j < 0xfe363c80 || i >= 0x1c9c380 || j >= 0x1c9c380)
        {
            return 0;
        }
        if(!chunkExists(i >> 4, j >> 4))
        {
            return 0;
        } else
        {
            Chunk chunk = getChunkFromChunkCoords(i >> 4, j >> 4);
            return chunk.getHeightValue(i & 0xf, j & 0xf);
        }
    }

    public int getSkyBlockTypeBrightness(EnumSkyBlock enumskyblock, int i, int j, int k)
    {
        if(worldProvider.hasNoSky && enumskyblock == EnumSkyBlock.Sky)
        {
            return 0;
        }
        if(j < 0)
        {
            j = 0;
        }
        if(j >= field_35472_c && enumskyblock == EnumSkyBlock.Sky)
        {
            return 15;
        }
        if(j < 0 || j >= field_35472_c || i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380)
        {
            return enumskyblock.defaultLightValue;
        }
        int l = i >> 4;
        int i1 = k >> 4;
        if(!chunkExists(l, i1))
        {
            return 0;
        }
        if(Block.useNeighborBrightness[getBlockId(i, j, k)])
        {
            int j1 = getSavedLightValue(enumskyblock, i, j + 1, k);
            int k1 = getSavedLightValue(enumskyblock, i + 1, j, k);
            int l1 = getSavedLightValue(enumskyblock, i - 1, j, k);
            int i2 = getSavedLightValue(enumskyblock, i, j, k + 1);
            int j2 = getSavedLightValue(enumskyblock, i, j, k - 1);
            if(k1 > j1)
            {
                j1 = k1;
            }
            if(l1 > j1)
            {
                j1 = l1;
            }
            if(i2 > j1)
            {
                j1 = i2;
            }
            if(j2 > j1)
            {
                j1 = j2;
            }
            return j1;
        } else
        {
            Chunk chunk = getChunkFromChunkCoords(l, i1);
            return chunk.getSavedLightValue(enumskyblock, i & 0xf, j, k & 0xf);
        }
    }

    public int getSavedLightValue(EnumSkyBlock enumskyblock, int i, int j, int k)
    {
        if(j < 0)
        {
            j = 0;
        }
        if(j >= field_35472_c)
        {
            j = field_35472_c - 1;
        }
        if(j < 0 || j >= field_35472_c || i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380)
        {
            return enumskyblock.defaultLightValue;
        }
        int l = i >> 4;
        int i1 = k >> 4;
        if(!chunkExists(l, i1))
        {
            return 0;
        } else
        {
            Chunk chunk = getChunkFromChunkCoords(l, i1);
            return chunk.getSavedLightValue(enumskyblock, i & 0xf, j, k & 0xf);
        }
    }

    public void setLightValue(EnumSkyBlock enumskyblock, int i, int j, int k, int l)
    {
        if(i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380)
        {
            return;
        }
        if(j < 0)
        {
            return;
        }
        if(j >= field_35472_c)
        {
            return;
        }
        if(!chunkExists(i >> 4, k >> 4))
        {
            return;
        }
        Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
        chunk.setLightValue(enumskyblock, i & 0xf, j, k & 0xf, l);
        for(int i1 = 0; i1 < worldAccesses.size(); i1++)
        {
            ((IWorldAccess)worldAccesses.get(i1)).markBlockAndNeighborsNeedsUpdate(i, j, k);
        }

    }

    public int getLightBrightnessForSkyBlocks(int i, int j, int k, int l)
    {
        int i1 = getSkyBlockTypeBrightness(EnumSkyBlock.Sky, i, j, k);
        int j1 = getSkyBlockTypeBrightness(EnumSkyBlock.Block, i, j, k);
        if(j1 < l)
        {
            j1 = l;
        }
        return i1 << 20 | j1 << 4;
    }

    public float getBrightness(int i, int j, int k, int l)
    {
        int i1 = getBlockLightValue(i, j, k);
        if(i1 < l)
        {
            i1 = l;
        }
        return worldProvider.lightBrightnessTable[i1];
    }

    public float getLightBrightness(int i, int j, int k)
    {
        return worldProvider.lightBrightnessTable[getBlockLightValue(i, j, k)];
    }

    public boolean isDaytime()
    {
        return skylightSubtracted < 4;
    }

    public MovingObjectPosition rayTraceBlocks(Vec3D vec3d, Vec3D vec3d1)
    {
        return rayTraceBlocks_do_do(vec3d, vec3d1, false, false);
    }

    public MovingObjectPosition rayTraceBlocks_do(Vec3D vec3d, Vec3D vec3d1, boolean flag)
    {
        return rayTraceBlocks_do_do(vec3d, vec3d1, flag, false);
    }

    public MovingObjectPosition rayTraceBlocks_do_do(Vec3D vec3d, Vec3D vec3d1, boolean flag, boolean flag1)
    {
        if(Double.isNaN(vec3d.xCoord) || Double.isNaN(vec3d.yCoord) || Double.isNaN(vec3d.zCoord))
        {
            return null;
        }
        if(Double.isNaN(vec3d1.xCoord) || Double.isNaN(vec3d1.yCoord) || Double.isNaN(vec3d1.zCoord))
        {
            return null;
        }
        int i = MathHelper.floor_double(vec3d1.xCoord);
        int j = MathHelper.floor_double(vec3d1.yCoord);
        int k = MathHelper.floor_double(vec3d1.zCoord);
        int l = MathHelper.floor_double(vec3d.xCoord);
        int i1 = MathHelper.floor_double(vec3d.yCoord);
        int j1 = MathHelper.floor_double(vec3d.zCoord);
        int k1 = getBlockId(l, i1, j1);
        int i2 = getBlockMetadata(l, i1, j1);
        Block block = Block.blocksList[k1];
        if((!flag1 || block == null || block.getCollisionBoundingBoxFromPool(this, l, i1, j1) != null) && k1 > 0 && block.canCollideCheck(i2, flag))
        {
            MovingObjectPosition movingobjectposition = block.collisionRayTrace(this, l, i1, j1, vec3d, vec3d1);
            if(movingobjectposition != null)
            {
                return movingobjectposition;
            }
        }
        for(int l1 = 200; l1-- >= 0;)
        {
            if(Double.isNaN(vec3d.xCoord) || Double.isNaN(vec3d.yCoord) || Double.isNaN(vec3d.zCoord))
            {
                return null;
            }
            if(l == i && i1 == j && j1 == k)
            {
                return null;
            }
            boolean flag2 = true;
            boolean flag3 = true;
            boolean flag4 = true;
            double d = 999D;
            double d1 = 999D;
            double d2 = 999D;
            if(i > l)
            {
                d = (double)l + 1.0D;
            } else
            if(i < l)
            {
                d = (double)l + 0.0D;
            } else
            {
                flag2 = false;
            }
            if(j > i1)
            {
                d1 = (double)i1 + 1.0D;
            } else
            if(j < i1)
            {
                d1 = (double)i1 + 0.0D;
            } else
            {
                flag3 = false;
            }
            if(k > j1)
            {
                d2 = (double)j1 + 1.0D;
            } else
            if(k < j1)
            {
                d2 = (double)j1 + 0.0D;
            } else
            {
                flag4 = false;
            }
            double d3 = 999D;
            double d4 = 999D;
            double d5 = 999D;
            double d6 = vec3d1.xCoord - vec3d.xCoord;
            double d7 = vec3d1.yCoord - vec3d.yCoord;
            double d8 = vec3d1.zCoord - vec3d.zCoord;
            if(flag2)
            {
                d3 = (d - vec3d.xCoord) / d6;
            }
            if(flag3)
            {
                d4 = (d1 - vec3d.yCoord) / d7;
            }
            if(flag4)
            {
                d5 = (d2 - vec3d.zCoord) / d8;
            }
            byte byte0 = 0;
            if(d3 < d4 && d3 < d5)
            {
                if(i > l)
                {
                    byte0 = 4;
                } else
                {
                    byte0 = 5;
                }
                vec3d.xCoord = d;
                vec3d.yCoord += d7 * d3;
                vec3d.zCoord += d8 * d3;
            } else
            if(d4 < d5)
            {
                if(j > i1)
                {
                    byte0 = 0;
                } else
                {
                    byte0 = 1;
                }
                vec3d.xCoord += d6 * d4;
                vec3d.yCoord = d1;
                vec3d.zCoord += d8 * d4;
            } else
            {
                if(k > j1)
                {
                    byte0 = 2;
                } else
                {
                    byte0 = 3;
                }
                vec3d.xCoord += d6 * d5;
                vec3d.yCoord += d7 * d5;
                vec3d.zCoord = d2;
            }
            Vec3D vec3d2 = Vec3D.createVector(vec3d.xCoord, vec3d.yCoord, vec3d.zCoord);
            l = (int)(vec3d2.xCoord = MathHelper.floor_double(vec3d.xCoord));
            if(byte0 == 5)
            {
                l--;
                vec3d2.xCoord++;
            }
            i1 = (int)(vec3d2.yCoord = MathHelper.floor_double(vec3d.yCoord));
            if(byte0 == 1)
            {
                i1--;
                vec3d2.yCoord++;
            }
            j1 = (int)(vec3d2.zCoord = MathHelper.floor_double(vec3d.zCoord));
            if(byte0 == 3)
            {
                j1--;
                vec3d2.zCoord++;
            }
            int j2 = getBlockId(l, i1, j1);
            int k2 = getBlockMetadata(l, i1, j1);
            Block block1 = Block.blocksList[j2];
            if((!flag1 || block1 == null || block1.getCollisionBoundingBoxFromPool(this, l, i1, j1) != null) && j2 > 0 && block1.canCollideCheck(k2, flag))
            {
                MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(this, l, i1, j1, vec3d, vec3d1);
                if(movingobjectposition1 != null)
                {
                    return movingobjectposition1;
                }
            }
        }

        return null;
    }

    public void playSoundAtEntity(Entity entity, String s, float f, float f1)
    {
        for(int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playSound(s, entity.posX, entity.posY - (double)entity.yOffset, entity.posZ, f, f1);
        }

    }

    public void playSoundEffect(double d, double d1, double d2, String s, 
            float f, float f1)
    {
        for(int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playSound(s, d, d1, d2, f, f1);
        }

    }

    public void playRecord(String s, int i, int j, int k)
    {
        for(int l = 0; l < worldAccesses.size(); l++)
        {
            ((IWorldAccess)worldAccesses.get(l)).playRecord(s, i, j, k);
        }

    }

    public void spawnParticle(String s, double d, double d1, double d2, 
            double d3, double d4, double d5)
    {
        for(int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).spawnParticle(s, d, d1, d2, d3, d4, d5);
        }

    }

    public boolean addWeatherEffect(Entity entity)
    {
        weatherEffects.add(entity);
        return true;
    }

    public boolean entityJoinedWorld(Entity entity)
    {
        int i = MathHelper.floor_double(entity.posX / 16D);
        int j = MathHelper.floor_double(entity.posZ / 16D);
        boolean flag = false;
        if(entity instanceof EntityPlayer)
        {
            flag = true;
        }
        if(flag || chunkExists(i, j))
        {
            if(entity instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)entity;
                playerEntities.add(entityplayer);
                updateAllPlayersSleepingFlag();
            }
            getChunkFromChunkCoords(i, j).addEntity(entity);
            loadedEntityList.add(entity);
            obtainEntitySkin(entity);
            return true;
        } else
        {
            return false;
        }
    }

    protected void obtainEntitySkin(Entity entity)
    {
        for(int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).obtainEntitySkin(entity);
        }

    }

    protected void releaseEntitySkin(Entity entity)
    {
        for(int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).releaseEntitySkin(entity);
        }

    }

    public void setEntityDead(Entity entity)
    {
        if(entity.riddenByEntity != null)
        {
            entity.riddenByEntity.mountEntity(null);
        }
        if(entity.ridingEntity != null)
        {
            entity.mountEntity(null);
        }
        entity.setEntityDead();
        if(entity instanceof EntityPlayer)
        {
            playerEntities.remove((EntityPlayer)entity);
            updateAllPlayersSleepingFlag();
        }
    }

    public void addWorldAccess(IWorldAccess iworldaccess)
    {
        worldAccesses.add(iworldaccess);
    }

    public void removeWorldAccess(IWorldAccess iworldaccess)
    {
        worldAccesses.remove(iworldaccess);
    }

    public List getCollidingBoundingBoxes(Entity entity, AxisAlignedBB axisalignedbb)
    {
        collidingBoundingBoxes.clear();
        int i = MathHelper.floor_double(axisalignedbb.minX);
        int j = MathHelper.floor_double(axisalignedbb.maxX + 1.0D);
        int k = MathHelper.floor_double(axisalignedbb.minY);
        int l = MathHelper.floor_double(axisalignedbb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(axisalignedbb.minZ);
        int j1 = MathHelper.floor_double(axisalignedbb.maxZ + 1.0D);
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = i1; l1 < j1; l1++)
            {
                if(!blockExists(k1, field_35472_c / 2, l1))
                {
                    continue;
                }
                for(int i2 = k - 1; i2 < l; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, i2, l1)];
                    if(block != null)
                    {
                        block.getCollidingBoundingBoxes(this, k1, i2, l1, axisalignedbb, collidingBoundingBoxes);
                    }
                }

            }

        }

        double d = 0.25D;
        List list = getEntitiesWithinAABBExcludingEntity(entity, axisalignedbb.expand(d, d, d));
        for(int j2 = 0; j2 < list.size(); j2++)
        {
            AxisAlignedBB axisalignedbb1 = ((Entity)list.get(j2)).getBoundingBox();
            if(axisalignedbb1 != null && axisalignedbb1.intersectsWith(axisalignedbb))
            {
                collidingBoundingBoxes.add(axisalignedbb1);
            }
            axisalignedbb1 = entity.getCollisionBox((Entity)list.get(j2));
            if(axisalignedbb1 != null && axisalignedbb1.intersectsWith(axisalignedbb))
            {
                collidingBoundingBoxes.add(axisalignedbb1);
            }
        }

        return collidingBoundingBoxes;
    }

    public int calculateSkylightSubtracted(float f)
    {
        float f1 = getCelestialAngle(f);
        float f2 = 1.0F - (MathHelper.cos(f1 * 3.141593F * 2.0F) * 2.0F + 0.5F);
        if(f2 < 0.0F)
        {
            f2 = 0.0F;
        }
        if(f2 > 1.0F)
        {
            f2 = 1.0F;
        }
        f2 = 1.0F - f2;
        f2 = (float)((double)f2 * (1.0D - (double)(getRainStrength(f) * 5F) / 16D));
        f2 = (float)((double)f2 * (1.0D - (double)(getWeightedThunderStrength(f) * 5F) / 16D));
        f2 = 1.0F - f2;
        return (int)(f2 * 11F);
    }

    public float func_35464_b(float f)
    {
        float f1 = getCelestialAngle(f);
        float f2 = 1.0F - (MathHelper.cos(f1 * 3.141593F * 2.0F) * 2.0F + 0.2F);
        if(f2 < 0.0F)
        {
            f2 = 0.0F;
        }
        if(f2 > 1.0F)
        {
            f2 = 1.0F;
        }
        f2 = 1.0F - f2;
        f2 = (float)((double)f2 * (1.0D - (double)(getRainStrength(f) * 5F) / 16D));
        f2 = (float)((double)f2 * (1.0D - (double)(getWeightedThunderStrength(f) * 5F) / 16D));
        return f2 * 0.8F + 0.2F;
    }

    public Vec3D getSkyColor(Entity entity, float f)
    {
        float f1 = getCelestialAngle(f);
        float f2 = MathHelper.cos(f1 * 3.141593F * 2.0F) * 2.0F + 0.5F;
        if(f2 < 0.0F)
        {
            f2 = 0.0F;
        }
        if(f2 > 1.0F)
        {
            f2 = 1.0F;
        }
        int i = MathHelper.floor_double(entity.posX);
        int j = MathHelper.floor_double(entity.posZ);
        float f3 = getWorldChunkManager().func_35554_b(i, 64, j);
        int k = getWorldChunkManager().getBiomeGenAt(i, j).getSkyColorByTemp(f3);
        float f4 = (float)(k >> 16 & 0xff) / 255F;
        float f5 = (float)(k >> 8 & 0xff) / 255F;
        float f6 = (float)(k & 0xff) / 255F;
        f4 *= f2;
        f5 *= f2;
        f6 *= f2;
        float f7 = getRainStrength(f);
        if(f7 > 0.0F)
        {
            float f8 = (f4 * 0.3F + f5 * 0.59F + f6 * 0.11F) * 0.6F;
            float f10 = 1.0F - f7 * 0.75F;
            f4 = f4 * f10 + f8 * (1.0F - f10);
            f5 = f5 * f10 + f8 * (1.0F - f10);
            f6 = f6 * f10 + f8 * (1.0F - f10);
        }
        float f9 = getWeightedThunderStrength(f);
        if(f9 > 0.0F)
        {
            float f11 = (f4 * 0.3F + f5 * 0.59F + f6 * 0.11F) * 0.2F;
            float f13 = 1.0F - f9 * 0.75F;
            f4 = f4 * f13 + f11 * (1.0F - f13);
            f5 = f5 * f13 + f11 * (1.0F - f13);
            f6 = f6 * f13 + f11 * (1.0F - f13);
        }
        if(lightningFlash > 0)
        {
            float f12 = (float)lightningFlash - f;
            if(f12 > 1.0F)
            {
                f12 = 1.0F;
            }
            f12 *= 0.45F;
            f4 = f4 * (1.0F - f12) + 0.8F * f12;
            f5 = f5 * (1.0F - f12) + 0.8F * f12;
            f6 = f6 * (1.0F - f12) + 1.0F * f12;
        }
        return Vec3D.createVector(f4, f5, f6);
    }

    public float getCelestialAngle(float f)
    {
        return worldProvider.calculateCelestialAngle(worldInfo.getWorldTime(), f);
    }

    public int func_40475_d(float f)
    {
        return worldProvider.func_40470_b(worldInfo.getWorldTime(), f);
    }

    public float getCelestialAngleRadians(float f)
    {
        float f1 = getCelestialAngle(f);
        return f1 * 3.141593F * 2.0F;
    }

    public Vec3D drawClouds(float f)
    {
        float f1 = getCelestialAngle(f);
        float f2 = MathHelper.cos(f1 * 3.141593F * 2.0F) * 2.0F + 0.5F;
        if(f2 < 0.0F)
        {
            f2 = 0.0F;
        }
        if(f2 > 1.0F)
        {
            f2 = 1.0F;
        }
        float f3 = (float)(cloudColour >> 16 & 255L) / 255F;
        float f4 = (float)(cloudColour >> 8 & 255L) / 255F;
        float f5 = (float)(cloudColour & 255L) / 255F;
        float f6 = getRainStrength(f);
        if(f6 > 0.0F)
        {
            float f7 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.6F;
            float f9 = 1.0F - f6 * 0.95F;
            f3 = f3 * f9 + f7 * (1.0F - f9);
            f4 = f4 * f9 + f7 * (1.0F - f9);
            f5 = f5 * f9 + f7 * (1.0F - f9);
        }
        f3 *= f2 * 0.9F + 0.1F;
        f4 *= f2 * 0.9F + 0.1F;
        f5 *= f2 * 0.85F + 0.15F;
        float f8 = getWeightedThunderStrength(f);
        if(f8 > 0.0F)
        {
            float f10 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.2F;
            float f11 = 1.0F - f8 * 0.95F;
            f3 = f3 * f11 + f10 * (1.0F - f11);
            f4 = f4 * f11 + f10 * (1.0F - f11);
            f5 = f5 * f11 + f10 * (1.0F - f11);
        }
        return Vec3D.createVector(f3, f4, f5);
    }

    public Vec3D getFogColor(float f)
    {
        float f1 = getCelestialAngle(f);
        return worldProvider.getFogColor(f1, f);
    }

    public int func_35461_e(int i, int j)
    {
        return getChunkFromBlockCoords(i, j).func_35840_c(i & 0xf, j & 0xf);
    }

    public int getTopSolidOrLiquidBlock(int i, int j)
    {
        Chunk chunk = getChunkFromBlockCoords(i, j);
        int k = field_35472_c - 1;
        i &= 0xf;
        j &= 0xf;
        while(k > 0) 
        {
            int l = chunk.getBlockID(i, k, j);
            if(l == 0 || !Block.blocksList[l].blockMaterial.getIsSolid() || Block.blocksList[l].blockMaterial == Material.leaves)
            {
                k--;
            } else
            {
                return k + 1;
            }
        }
        return -1;
    }

    public float getStarBrightness(float f)
    {
        float f1 = getCelestialAngle(f);
        float f2 = 1.0F - (MathHelper.cos(f1 * 3.141593F * 2.0F) * 2.0F + 0.75F);
        if(f2 < 0.0F)
        {
            f2 = 0.0F;
        }
        if(f2 > 1.0F)
        {
            f2 = 1.0F;
        }
        return f2 * f2 * 0.5F;
    }

    public void scheduleBlockUpdate(int i, int j, int k, int l, int i1)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(i, j, k, l);
        byte byte0 = 8;
        if(scheduledUpdatesAreImmediate)
        {
            if(checkChunksExist(nextticklistentry.xCoord - byte0, nextticklistentry.yCoord - byte0, nextticklistentry.zCoord - byte0, nextticklistentry.xCoord + byte0, nextticklistentry.yCoord + byte0, nextticklistentry.zCoord + byte0))
            {
                int j1 = getBlockId(nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord);
                if(j1 == nextticklistentry.blockID && j1 > 0)
                {
                    Block.blocksList[j1].updateTick(this, nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord, rand);
                }
            }
            return;
        }
        if(checkChunksExist(i - byte0, j - byte0, k - byte0, i + byte0, j + byte0, k + byte0))
        {
            if(l > 0)
            {
                nextticklistentry.setScheduledTime((long)i1 + worldInfo.getWorldTime());
            }
            if(!scheduledTickSet.contains(nextticklistentry))
            {
                scheduledTickSet.add(nextticklistentry);
                scheduledTickTreeSet.add(nextticklistentry);
            }
        }
    }

    public void func_41083_e(int i, int j, int k, int l, int i1)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(i, j, k, l);
        if(l > 0)
        {
            nextticklistentry.setScheduledTime((long)i1 + worldInfo.getWorldTime());
        }
        if(!scheduledTickSet.contains(nextticklistentry))
        {
            scheduledTickSet.add(nextticklistentry);
            scheduledTickTreeSet.add(nextticklistentry);
        }
    }

    public void updateEntities()
    {
        Profiler.startSection("entities");
        Profiler.startSection("global");
        for(int i = 0; i < weatherEffects.size(); i++)
        {
            Entity entity = (Entity)weatherEffects.get(i);
            entity.onUpdate();
            if(entity.isDead)
            {
                weatherEffects.remove(i--);
            }
        }

        Profiler.endStartSection("remove");
        loadedEntityList.removeAll(unloadedEntityList);
        for(int j = 0; j < unloadedEntityList.size(); j++)
        {
            Entity entity1 = (Entity)unloadedEntityList.get(j);
            int i1 = entity1.chunkCoordX;
            int k1 = entity1.chunkCoordZ;
            if(entity1.addedToChunk && chunkExists(i1, k1))
            {
                getChunkFromChunkCoords(i1, k1).removeEntity(entity1);
            }
        }

        for(int k = 0; k < unloadedEntityList.size(); k++)
        {
            releaseEntitySkin((Entity)unloadedEntityList.get(k));
        }

        unloadedEntityList.clear();
        Profiler.endStartSection("regular");
        for(int l = 0; l < loadedEntityList.size(); l++)
        {
            Entity entity2 = (Entity)loadedEntityList.get(l);
            if(entity2.ridingEntity != null)
            {
                if(!entity2.ridingEntity.isDead && entity2.ridingEntity.riddenByEntity == entity2)
                {
                    continue;
                }
                entity2.ridingEntity.riddenByEntity = null;
                entity2.ridingEntity = null;
            }
            if(!entity2.isDead)
            {
                updateEntity(entity2);
            }
            Profiler.startSection("remove");
            if(entity2.isDead)
            {
                int j1 = entity2.chunkCoordX;
                int l1 = entity2.chunkCoordZ;
                if(entity2.addedToChunk && chunkExists(j1, l1))
                {
                    getChunkFromChunkCoords(j1, l1).removeEntity(entity2);
                }
                loadedEntityList.remove(l--);
                releaseEntitySkin(entity2);
            }
            Profiler.endSection();
        }

        Profiler.endStartSection("tileEntities");
        scanningTileEntities = true;
        Iterator iterator = loadedTileEntityList.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            TileEntity tileentity = (TileEntity)iterator.next();
            if(!tileentity.isInvalid() && tileentity.worldObj != null && blockExists(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord))
            {
                tileentity.updateEntity();
            }
            if(tileentity.isInvalid())
            {
                iterator.remove();
                if(chunkExists(tileentity.xCoord >> 4, tileentity.zCoord >> 4))
                {
                    Chunk chunk = getChunkFromChunkCoords(tileentity.xCoord >> 4, tileentity.zCoord >> 4);
                    if(chunk != null)
                    {
                        chunk.removeChunkBlockTileEntity(tileentity.xCoord & 0xf, tileentity.yCoord, tileentity.zCoord & 0xf);
                    }
                }
            }
        } while(true);
        scanningTileEntities = false;
        if(!entityRemoval.isEmpty())
        {
            loadedTileEntityList.removeAll(entityRemoval);
            entityRemoval.clear();
        }
        Profiler.endStartSection("pendingTileEntities");
        if(!addedTileEntityList.isEmpty())
        {
            Iterator iterator1 = addedTileEntityList.iterator();
            do
            {
                if(!iterator1.hasNext())
                {
                    break;
                }
                TileEntity tileentity1 = (TileEntity)iterator1.next();
                if(!tileentity1.isInvalid())
                {
                    if(!loadedTileEntityList.contains(tileentity1))
                    {
                        loadedTileEntityList.add(tileentity1);
                    }
                    if(chunkExists(tileentity1.xCoord >> 4, tileentity1.zCoord >> 4))
                    {
                        Chunk chunk1 = getChunkFromChunkCoords(tileentity1.xCoord >> 4, tileentity1.zCoord >> 4);
                        if(chunk1 != null)
                        {
                            chunk1.setChunkBlockTileEntity(tileentity1.xCoord & 0xf, tileentity1.yCoord, tileentity1.zCoord & 0xf, tileentity1);
                        }
                    }
                    markBlockNeedsUpdate(tileentity1.xCoord, tileentity1.yCoord, tileentity1.zCoord);
                }
            } while(true);
            addedTileEntityList.clear();
        }
        Profiler.endSection();
        Profiler.endSection();
    }

    public void addTileEntity(Collection collection)
    {
        if(scanningTileEntities)
        {
            addedTileEntityList.addAll(collection);
        } else
        {
            loadedTileEntityList.addAll(collection);
        }
    }

    public void updateEntity(Entity entity)
    {
        updateEntityWithOptionalForce(entity, true);
    }

    public void updateEntityWithOptionalForce(Entity entity, boolean flag)
    {
        int i = MathHelper.floor_double(entity.posX);
        int j = MathHelper.floor_double(entity.posZ);
        byte byte0 = 32;
        if(flag && !checkChunksExist(i - byte0, 0, j - byte0, i + byte0, field_35472_c, j + byte0))
        {
            return;
        }
        entity.lastTickPosX = entity.posX;
        entity.lastTickPosY = entity.posY;
        entity.lastTickPosZ = entity.posZ;
        entity.prevRotationYaw = entity.rotationYaw;
        entity.prevRotationPitch = entity.rotationPitch;
        if(flag && entity.addedToChunk)
        {
            if(entity.ridingEntity != null)
            {
                entity.updateRidden();
            } else
            {
                entity.onUpdate();
            }
        }
        Profiler.startSection("chunkCheck");
        if(Double.isNaN(entity.posX) || Double.isInfinite(entity.posX))
        {
            entity.posX = entity.lastTickPosX;
        }
        if(Double.isNaN(entity.posY) || Double.isInfinite(entity.posY))
        {
            entity.posY = entity.lastTickPosY;
        }
        if(Double.isNaN(entity.posZ) || Double.isInfinite(entity.posZ))
        {
            entity.posZ = entity.lastTickPosZ;
        }
        if(Double.isNaN(entity.rotationPitch) || Double.isInfinite(entity.rotationPitch))
        {
            entity.rotationPitch = entity.prevRotationPitch;
        }
        if(Double.isNaN(entity.rotationYaw) || Double.isInfinite(entity.rotationYaw))
        {
            entity.rotationYaw = entity.prevRotationYaw;
        }
        int k = MathHelper.floor_double(entity.posX / 16D);
        int l = MathHelper.floor_double(entity.posY / 16D);
        int i1 = MathHelper.floor_double(entity.posZ / 16D);
        if(!entity.addedToChunk || entity.chunkCoordX != k || entity.chunkCoordY != l || entity.chunkCoordZ != i1)
        {
            if(entity.addedToChunk && chunkExists(entity.chunkCoordX, entity.chunkCoordZ))
            {
                getChunkFromChunkCoords(entity.chunkCoordX, entity.chunkCoordZ).removeEntityAtIndex(entity, entity.chunkCoordY);
            }
            if(chunkExists(k, i1))
            {
                entity.addedToChunk = true;
                getChunkFromChunkCoords(k, i1).addEntity(entity);
            } else
            {
                entity.addedToChunk = false;
            }
        }
        Profiler.endSection();
        if(flag && entity.addedToChunk && entity.riddenByEntity != null)
        {
            if(entity.riddenByEntity.isDead || entity.riddenByEntity.ridingEntity != entity)
            {
                entity.riddenByEntity.ridingEntity = null;
                entity.riddenByEntity = null;
            } else
            {
                updateEntity(entity.riddenByEntity);
            }
        }
    }

    public boolean checkIfAABBIsClear(AxisAlignedBB axisalignedbb)
    {
        List list = getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
        for(int i = 0; i < list.size(); i++)
        {
            Entity entity = (Entity)list.get(i);
            if(!entity.isDead && entity.preventEntitySpawning)
            {
                return false;
            }
        }

        return true;
    }

    public boolean getIsAnyLiquid(AxisAlignedBB axisalignedbb)
    {
        int i = MathHelper.floor_double(axisalignedbb.minX);
        int j = MathHelper.floor_double(axisalignedbb.maxX + 1.0D);
        int k = MathHelper.floor_double(axisalignedbb.minY);
        int l = MathHelper.floor_double(axisalignedbb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(axisalignedbb.minZ);
        int j1 = MathHelper.floor_double(axisalignedbb.maxZ + 1.0D);
        if(axisalignedbb.minX < 0.0D)
        {
            i--;
        }
        if(axisalignedbb.minY < 0.0D)
        {
            k--;
        }
        if(axisalignedbb.minZ < 0.0D)
        {
            i1--;
        }
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = k; l1 < l; l1++)
            {
                for(int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, l1, i2)];
                    if(block != null && block.blockMaterial.getIsLiquid())
                    {
                        return true;
                    }
                }

            }

        }

        return false;
    }

    public boolean isBoundingBoxBurning(AxisAlignedBB axisalignedbb)
    {
        int i = MathHelper.floor_double(axisalignedbb.minX);
        int j = MathHelper.floor_double(axisalignedbb.maxX + 1.0D);
        int k = MathHelper.floor_double(axisalignedbb.minY);
        int l = MathHelper.floor_double(axisalignedbb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(axisalignedbb.minZ);
        int j1 = MathHelper.floor_double(axisalignedbb.maxZ + 1.0D);
        if(checkChunksExist(i, k, i1, j, l, j1))
        {
            for(int k1 = i; k1 < j; k1++)
            {
                for(int l1 = k; l1 < l; l1++)
                {
                    for(int i2 = i1; i2 < j1; i2++)
                    {
                        int j2 = getBlockId(k1, l1, i2);
                        if(j2 == Block.fire.blockID || j2 == Block.lavaMoving.blockID || j2 == Block.lavaStill.blockID)
                        {
                            return true;
                        }
                    }

                }

            }

        }
        return false;
    }

    public boolean handleMaterialAcceleration(AxisAlignedBB axisalignedbb, Material material, Entity entity)
    {
        int i = MathHelper.floor_double(axisalignedbb.minX);
        int j = MathHelper.floor_double(axisalignedbb.maxX + 1.0D);
        int k = MathHelper.floor_double(axisalignedbb.minY);
        int l = MathHelper.floor_double(axisalignedbb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(axisalignedbb.minZ);
        int j1 = MathHelper.floor_double(axisalignedbb.maxZ + 1.0D);
        if(!checkChunksExist(i, k, i1, j, l, j1))
        {
            return false;
        }
        boolean flag = false;
        Vec3D vec3d = Vec3D.createVector(0.0D, 0.0D, 0.0D);
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = k; l1 < l; l1++)
            {
                for(int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, l1, i2)];
                    if(block == null || block.blockMaterial != material)
                    {
                        continue;
                    }
                    double d1 = (float)(l1 + 1) - BlockFluid.getFluidHeightPercent(getBlockMetadata(k1, l1, i2));
                    if((double)l >= d1)
                    {
                        flag = true;
                        block.velocityToAddToEntity(this, k1, l1, i2, entity, vec3d);
                    }
                }

            }

        }

        if(vec3d.lengthVector() > 0.0D)
        {
            vec3d = vec3d.normalize();
            double d = 0.014D;
            entity.motionX += vec3d.xCoord * d;
            entity.motionY += vec3d.yCoord * d;
            entity.motionZ += vec3d.zCoord * d;
        }
        return flag;
    }

    public boolean isMaterialInBB(AxisAlignedBB axisalignedbb, Material material)
    {
        int i = MathHelper.floor_double(axisalignedbb.minX);
        int j = MathHelper.floor_double(axisalignedbb.maxX + 1.0D);
        int k = MathHelper.floor_double(axisalignedbb.minY);
        int l = MathHelper.floor_double(axisalignedbb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(axisalignedbb.minZ);
        int j1 = MathHelper.floor_double(axisalignedbb.maxZ + 1.0D);
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = k; l1 < l; l1++)
            {
                for(int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, l1, i2)];
                    if(block != null && block.blockMaterial == material)
                    {
                        return true;
                    }
                }

            }

        }

        return false;
    }

    public boolean isAABBInMaterial(AxisAlignedBB axisalignedbb, Material material)
    {
        int i = MathHelper.floor_double(axisalignedbb.minX);
        int j = MathHelper.floor_double(axisalignedbb.maxX + 1.0D);
        int k = MathHelper.floor_double(axisalignedbb.minY);
        int l = MathHelper.floor_double(axisalignedbb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(axisalignedbb.minZ);
        int j1 = MathHelper.floor_double(axisalignedbb.maxZ + 1.0D);
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = k; l1 < l; l1++)
            {
                for(int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, l1, i2)];
                    if(block == null || block.blockMaterial != material)
                    {
                        continue;
                    }
                    int j2 = getBlockMetadata(k1, l1, i2);
                    double d = l1 + 1;
                    if(j2 < 8)
                    {
                        d = (double)(l1 + 1) - (double)j2 / 8D;
                    }
                    if(d >= axisalignedbb.minY)
                    {
                        return true;
                    }
                }

            }

        }

        return false;
    }

    public Explosion createExplosion(Entity entity, double d, double d1, double d2, 
            float f)
    {
        return newExplosion(entity, d, d1, d2, f, false);
    }

    public Explosion newExplosion(Entity entity, double d, double d1, double d2, 
            float f, boolean flag)
    {
        Explosion explosion = new Explosion(this, entity, d, d1, d2, f);
        explosion.isFlaming = flag;
        explosion.doExplosionA();
        explosion.doExplosionB(true);
        return explosion;
    }

    public float func_675_a(Vec3D vec3d, AxisAlignedBB axisalignedbb)
    {
        double d = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2D + 1.0D);
        double d1 = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2D + 1.0D);
        double d2 = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2D + 1.0D);
        int i = 0;
        int j = 0;
        for(float f = 0.0F; f <= 1.0F; f = (float)((double)f + d))
        {
            for(float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1))
            {
                for(float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2))
                {
                    double d3 = axisalignedbb.minX + (axisalignedbb.maxX - axisalignedbb.minX) * (double)f;
                    double d4 = axisalignedbb.minY + (axisalignedbb.maxY - axisalignedbb.minY) * (double)f1;
                    double d5 = axisalignedbb.minZ + (axisalignedbb.maxZ - axisalignedbb.minZ) * (double)f2;
                    if(rayTraceBlocks(Vec3D.createVector(d3, d4, d5), vec3d) == null)
                    {
                        i++;
                    }
                    j++;
                }

            }

        }

        return (float)i / (float)j;
    }

    public void onBlockHit(EntityPlayer entityplayer, int i, int j, int k, int l)
    {
        if(l == 0)
        {
            j--;
        }
        if(l == 1)
        {
            j++;
        }
        if(l == 2)
        {
            k--;
        }
        if(l == 3)
        {
            k++;
        }
        if(l == 4)
        {
            i--;
        }
        if(l == 5)
        {
            i++;
        }
        if(getBlockId(i, j, k) == Block.fire.blockID)
        {
            playAuxSFXAtEntity(entityplayer, 1004, i, j, k, 0);
            setBlockWithNotify(i, j, k, 0);
        }
    }

    public Entity func_4085_a(Class class1)
    {
        return null;
    }

    public String getDebugLoadedEntities()
    {
        return (new StringBuilder()).append("All: ").append(loadedEntityList.size()).toString();
    }

    public String func_21119_g()
    {
        return chunkProvider.makeString();
    }

    public TileEntity getBlockTileEntity(int i, int j, int k)
    {
label0:
        {
            TileEntity tileentity;
label1:
            {
                Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
                if(chunk == null)
                {
                    break label0;
                }
                tileentity = chunk.getChunkBlockTileEntity(i & 0xf, j, k & 0xf);
                if(tileentity != null)
                {
                    break label1;
                }
                Iterator iterator = addedTileEntityList.iterator();
                TileEntity tileentity1;
                do
                {
                    if(!iterator.hasNext())
                    {
                        break label1;
                    }
                    tileentity1 = (TileEntity)iterator.next();
                } while(tileentity1.isInvalid() || tileentity1.xCoord != i || tileentity1.yCoord != j || tileentity1.zCoord != k);
                tileentity = tileentity1;
            }
            return tileentity;
        }
        return null;
    }

    public void setBlockTileEntity(int i, int j, int k, TileEntity tileentity)
    {
        if(tileentity != null && !tileentity.isInvalid())
        {
            if(scanningTileEntities)
            {
                tileentity.xCoord = i;
                tileentity.yCoord = j;
                tileentity.zCoord = k;
                addedTileEntityList.add(tileentity);
            } else
            {
                loadedTileEntityList.add(tileentity);
                Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
                if(chunk != null)
                {
                    chunk.setChunkBlockTileEntity(i & 0xf, j, k & 0xf, tileentity);
                }
            }
        }
    }

    public void removeBlockTileEntity(int i, int j, int k)
    {
        TileEntity tileentity = getBlockTileEntity(i, j, k);
        if(tileentity != null && scanningTileEntities)
        {
            tileentity.invalidate();
            addedTileEntityList.remove(tileentity);
        } else
        {
            if(tileentity != null)
            {
                addedTileEntityList.remove(tileentity);
                loadedTileEntityList.remove(tileentity);
            }
            Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
            if(chunk != null)
            {
                chunk.removeChunkBlockTileEntity(i & 0xf, j, k & 0xf);
            }
        }
    }

    public void func_35455_a(TileEntity tileentity)
    {
        entityRemoval.add(tileentity);
    }

    public boolean isBlockOpaqueCube(int i, int j, int k)
    {
        Block block = Block.blocksList[getBlockId(i, j, k)];
        if(block == null)
        {
            return false;
        } else
        {
            return block.isOpaqueCube();
        }
    }

    public boolean isBlockNormalCube(int i, int j, int k)
    {
        Block block = Block.blocksList[getBlockId(i, j, k)];
        if(block == null)
        {
            return false;
        } else
        {
            return block.blockMaterial.getIsOpaque() && block.renderAsNormalBlock();
        }
    }

    public boolean func_41082_b(int i, int j, int k, boolean flag)
    {
        if(i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380)
        {
            return flag;
        }
        Chunk chunk = chunkProvider.provideChunk(i >> 4, k >> 4);
        if(chunk == null || chunk.getFalse())
        {
            return flag;
        }
        Block block = Block.blocksList[getBlockId(i, j, k)];
        if(block == null)
        {
            return false;
        } else
        {
            return block.blockMaterial.getIsOpaque() && block.renderAsNormalBlock();
        }
    }

    public void saveWorldIndirectly(IProgressUpdate iprogressupdate)
    {
        saveWorld(true, iprogressupdate);
        try
        {
            ThreadedFileIOBase.field_40573_a.func_40566_a();
        }
        catch(InterruptedException interruptedexception)
        {
            interruptedexception.printStackTrace();
        }
    }

    public void calculateInitialSkylight()
    {
        int i = calculateSkylightSubtracted(1.0F);
        if(i != skylightSubtracted)
        {
            skylightSubtracted = i;
        }
    }

    public void setAllowedMobSpawns(boolean flag, boolean flag1)
    {
        spawnHostileMobs = flag;
        spawnPeacefulMobs = flag1;
    }

    public void tick()
    {
        if(getWorldInfo().isHardcoreModeEnabled() && difficultySetting < 3)
        {
            difficultySetting = 3;
        }
        getWorldChunkManager().func_35561_b();
        updateWeather();
        if(isAllPlayersFullyAsleep())
        {
            boolean flag = false;
            if(spawnHostileMobs)
            {
                if(difficultySetting < 1);
            }
            if(!flag)
            {
                long l = worldInfo.getWorldTime() + 24000L;
                worldInfo.setWorldTime(l - l % 24000L);
                wakeUpAllPlayers();
            }
        }
        Profiler.startSection("mobSpawner");
        SpawnerAnimals.performSpawning(this, spawnHostileMobs, spawnPeacefulMobs && worldInfo.getWorldTime() % 400L == 0L);
        Profiler.endStartSection("chunkSource");
        chunkProvider.unload100OldestChunks();
        int i = calculateSkylightSubtracted(1.0F);
        if(i != skylightSubtracted)
        {
            skylightSubtracted = i;
        }
        long l1 = worldInfo.getWorldTime() + 1L;
        if(l1 % (long)autosavePeriod == 0L)
        {
            Profiler.endStartSection("save");
            saveWorld(false, null);
        }
        worldInfo.setWorldTime(l1);
        Profiler.endStartSection("tickPending");
        tickUpdates(false);
        Profiler.endStartSection("tickTiles");
        updateBlocksAndPlayCaveSounds();
        Profiler.endSection();
    }

    private void calculateInitialWeather()
    {
        if(worldInfo.getIsRaining())
        {
            rainingStrength = 1.0F;
            if(worldInfo.getIsThundering())
            {
                thunderingStrength = 1.0F;
            }
        }
    }

    protected void updateWeather()
    {
        if(worldProvider.hasNoSky)
        {
            return;
        }
        if(lastLightningBolt > 0)
        {
            lastLightningBolt--;
        }
        int i = worldInfo.getThunderTime();
        if(i <= 0)
        {
            if(worldInfo.getIsThundering())
            {
                worldInfo.setThunderTime(rand.nextInt(12000) + 3600);
            } else
            {
                worldInfo.setThunderTime(rand.nextInt(0x29040) + 12000);
            }
        } else
        {
            i--;
            worldInfo.setThunderTime(i);
            if(i <= 0)
            {
                worldInfo.setIsThundering(!worldInfo.getIsThundering());
            }
        }
        int j = worldInfo.getRainTime();
        if(j <= 0)
        {
            if(worldInfo.getIsRaining())
            {
                worldInfo.setRainTime(rand.nextInt(12000) + 12000);
            } else
            {
                worldInfo.setRainTime(rand.nextInt(0x29040) + 12000);
            }
        } else
        {
            j--;
            worldInfo.setRainTime(j);
            if(j <= 0)
            {
                worldInfo.setIsRaining(!worldInfo.getIsRaining());
            }
        }
        prevRainingStrength = rainingStrength;
        if(worldInfo.getIsRaining())
        {
            rainingStrength += 0.01D;
        } else
        {
            rainingStrength -= 0.01D;
        }
        if(rainingStrength < 0.0F)
        {
            rainingStrength = 0.0F;
        }
        if(rainingStrength > 1.0F)
        {
            rainingStrength = 1.0F;
        }
        prevThunderingStrength = thunderingStrength;
        if(worldInfo.getIsThundering())
        {
            thunderingStrength += 0.01D;
        } else
        {
            thunderingStrength -= 0.01D;
        }
        if(thunderingStrength < 0.0F)
        {
            thunderingStrength = 0.0F;
        }
        if(thunderingStrength > 1.0F)
        {
            thunderingStrength = 1.0F;
        }
    }

    private void stopPrecipitation()
    {
        worldInfo.setRainTime(0);
        worldInfo.setIsRaining(false);
        worldInfo.setThunderTime(0);
        worldInfo.setIsThundering(false);
    }

    protected void updateBlocksAndPlayCaveSounds()
    {
        positionsToUpdate.clear();
        Profiler.startSection("buildList");
        for(int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayer entityplayer = (EntityPlayer)playerEntities.get(i);
            int l = MathHelper.floor_double(entityplayer.posX / 16D);
            int i1 = MathHelper.floor_double(entityplayer.posZ / 16D);
            byte byte0 = 7;
            for(int k1 = -byte0; k1 <= byte0; k1++)
            {
                for(int i2 = -byte0; i2 <= byte0; i2++)
                {
                    positionsToUpdate.add(new ChunkCoordIntPair(k1 + l, i2 + i1));
                }

            }

        }

        if(soundCounter > 0)
        {
            soundCounter--;
        }
        int j = 0;
        int k = 0;
        Profiler.endSection();
        for(Iterator iterator = positionsToUpdate.iterator(); iterator.hasNext(); Profiler.endSection())
        {
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)iterator.next();
            int j1 = chunkcoordintpair.chunkXPos * 16;
            int l1 = chunkcoordintpair.chunkZPos * 16;
            Profiler.startSection("getChunk");
            Chunk chunk = getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
            Profiler.endStartSection("tickChunk");
            chunk.func_35841_j();
            Profiler.endStartSection("moodSound");
            if(soundCounter == 0)
            {
                updateLCG = updateLCG * 3 + 0x3c6ef35f;
                int j2 = updateLCG >> 2;
                int j3 = j2 & 0xf;
                int j4 = j2 >> 8 & 0xf;
                int j5 = j2 >> 16 & field_35469_d;
                int j6 = chunk.getBlockID(j3, j5, j4);
                j3 += j1;
                j4 += l1;
                if(j6 == 0 && getFullBlockLightValue(j3, j5, j4) <= rand.nextInt(8) && getSavedLightValue(EnumSkyBlock.Sky, j3, j5, j4) <= 0)
                {
                    EntityPlayer entityplayer1 = getClosestPlayer((double)j3 + 0.5D, (double)j5 + 0.5D, (double)j4 + 0.5D, 8D);
                    if(entityplayer1 != null && entityplayer1.getDistanceSq((double)j3 + 0.5D, (double)j5 + 0.5D, (double)j4 + 0.5D) > 4D)
                    {
                        playSoundEffect((double)j3 + 0.5D, (double)j5 + 0.5D, (double)j4 + 0.5D, "ambient.cave.cave", 0.7F, 0.8F + rand.nextFloat() * 0.2F);
                        soundCounter = rand.nextInt(12000) + 6000;
                    }
                }
            }
            Profiler.endStartSection("thunder");
            if(rand.nextInt(0x186a0) == 0 && isRaining() && getIsThundering())
            {
                updateLCG = updateLCG * 3 + 0x3c6ef35f;
                int k2 = updateLCG >> 2;
                int k3 = j1 + (k2 & 0xf);
                int k4 = l1 + (k2 >> 8 & 0xf);
                int k5 = func_35461_e(k3, k4);
                if(canLightningStrikeAt(k3, k5, k4))
                {
                    addWeatherEffect(new EntityLightningBolt(this, k3, k5, k4));
                    lastLightningBolt = 2;
                }
            }
            Profiler.endStartSection("iceandsnow");
            updateLCG = updateLCG * 3 + 0x3c6ef35f;
            int l2 = updateLCG >> 2;
            int l3 = l2 & 0xf;
            int l4 = l2 >> 8 & 0xf;
            int l5 = func_35461_e(l3 + j1, l4 + l1);
            if(func_40481_q(l3 + j1, l5 - 1, l4 + l1))
            {
                setBlockWithNotify(l3 + j1, l5 - 1, l4 + l1, Block.ice.blockID);
            }
            if(isRaining() && func_40478_r(l3 + j1, l5, l4 + l1))
            {
                setBlockWithNotify(l3 + j1, l5, l4 + l1, Block.snow.blockID);
            }
            Profiler.endStartSection("checkLight");
            updateAllLightTypes(j1 + rand.nextInt(16), rand.nextInt(field_35472_c), l1 + rand.nextInt(16));
            Profiler.endStartSection("tickTiles");
            for(int i3 = 0; i3 < 20; i3++)
            {
                updateLCG = updateLCG * 3 + 0x3c6ef35f;
                int i4 = updateLCG >> 2;
                int i5 = i4 & 0xf;
                int i6 = i4 >> 8 & 0xf;
                int k6 = i4 >> 16 & field_35469_d;
                int l6 = chunk.blocks[i5 << field_35471_b | i6 << field_35473_a | k6] & 0xff;
                k++;
                if(Block.tickOnLoad[l6])
                {
                    j++;
                    Block.blocksList[l6].updateTick(this, i5 + j1, k6, i6 + l1, rand);
                }
            }

        }

    }

    public boolean func_40471_p(int i, int j, int k)
    {
        return func_40476_b(i, j, k, false);
    }

    public boolean func_40481_q(int i, int j, int k)
    {
        return func_40476_b(i, j, k, true);
    }

    public boolean func_40476_b(int i, int j, int k, boolean flag)
    {
        float f = getWorldChunkManager().func_35554_b(i, j, k);
        if(f > 0.15F)
        {
            return false;
        }
        if(j >= 0 && j < field_35472_c && getSavedLightValue(EnumSkyBlock.Block, i, j, k) < 10)
        {
            int l = getBlockId(i, j, k);
            if((l == Block.waterStill.blockID || l == Block.waterMoving.blockID) && getBlockMetadata(i, j, k) == 0)
            {
                if(!flag)
                {
                    return true;
                }
                boolean flag1 = true;
                if(flag1 && getBlockMaterial(i - 1, j, k) != Material.water)
                {
                    flag1 = false;
                }
                if(flag1 && getBlockMaterial(i + 1, j, k) != Material.water)
                {
                    flag1 = false;
                }
                if(flag1 && getBlockMaterial(i, j, k - 1) != Material.water)
                {
                    flag1 = false;
                }
                if(flag1 && getBlockMaterial(i, j, k + 1) != Material.water)
                {
                    flag1 = false;
                }
                if(!flag1)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean func_40478_r(int i, int j, int k)
    {
        float f = getWorldChunkManager().func_35554_b(i, j, k);
        if(f > 0.15F)
        {
            return false;
        }
        if(j >= 0 && j < field_35472_c && getSavedLightValue(EnumSkyBlock.Block, i, j, k) < 10)
        {
            int l = getBlockId(i, j - 1, k);
            int i1 = getBlockId(i, j, k);
            if(i1 == 0 && Block.snow.canPlaceBlockAt(this, i, j, k) && l != 0 && l != Block.ice.blockID && Block.blocksList[l].blockMaterial.getIsSolid())
            {
                return true;
            }
        }
        return false;
    }

    public void updateAllLightTypes(int i, int j, int k)
    {
        if(!worldProvider.hasNoSky)
        {
            updateLightByType(EnumSkyBlock.Sky, i, j, k);
        }
        updateLightByType(EnumSkyBlock.Block, i, j, k);
    }

    private int computeSkyLightValue(int i, int j, int k, int l, int i1, int j1)
    {
        int k1 = 0;
        if(canBlockSeeTheSky(j, k, l))
        {
            k1 = 15;
        } else
        {
            if(j1 == 0)
            {
                j1 = 1;
            }
            for(int l1 = 0; l1 < 6; l1++)
            {
                int i2 = (l1 % 2) * 2 - 1;
                int j2 = j + (((l1 / 2) % 3) / 2) * i2;
                int k2 = k + (((l1 / 2 + 1) % 3) / 2) * i2;
                int l2 = l + (((l1 / 2 + 2) % 3) / 2) * i2;
                int i3 = getSavedLightValue(EnumSkyBlock.Sky, j2, k2, l2) - j1;
                if(i3 > k1)
                {
                    k1 = i3;
                }
            }

        }
        return k1;
    }

    private int computeBlockLightValue(int i, int j, int k, int l, int i1, int j1)
    {
        int k1 = Block.lightValue[i1];
        int l1 = getSavedLightValue(EnumSkyBlock.Block, j - 1, k, l) - j1;
        int i2 = getSavedLightValue(EnumSkyBlock.Block, j + 1, k, l) - j1;
        int j2 = getSavedLightValue(EnumSkyBlock.Block, j, k - 1, l) - j1;
        int k2 = getSavedLightValue(EnumSkyBlock.Block, j, k + 1, l) - j1;
        int l2 = getSavedLightValue(EnumSkyBlock.Block, j, k, l - 1) - j1;
        int i3 = getSavedLightValue(EnumSkyBlock.Block, j, k, l + 1) - j1;
        if(l1 > k1)
        {
            k1 = l1;
        }
        if(i2 > k1)
        {
            k1 = i2;
        }
        if(j2 > k1)
        {
            k1 = j2;
        }
        if(k2 > k1)
        {
            k1 = k2;
        }
        if(l2 > k1)
        {
            k1 = l2;
        }
        if(i3 > k1)
        {
            k1 = i3;
        }
        return k1;
    }

    public void updateLightByType(EnumSkyBlock enumskyblock, int i, int j, int k)
    {
        if(!doChunksNearChunkExist(i, j, k, 17))
        {
            return;
        }
        int l = 0;
        int i1 = 0;
        int j1 = getSavedLightValue(enumskyblock, i, j, k);
        int l1 = 0;
        int j2 = j1;
        int i3 = getBlockId(i, j, k);
        int l3 = Block.lightOpacity[i3];
        if(l3 == 0)
        {
            l3 = 1;
        }
        int k4 = 0;
        if(enumskyblock == EnumSkyBlock.Sky)
        {
            k4 = computeSkyLightValue(j2, i, j, k, i3, l3);
        } else
        {
            k4 = computeBlockLightValue(j2, i, j, k, i3, l3);
        }
        l1 = k4;
        if(l1 > j1)
        {
            lightUpdateBlockList[i1++] = 0x20820;
        } else
        if(l1 < j1)
        {
            if(enumskyblock == EnumSkyBlock.Block);
            lightUpdateBlockList[i1++] = 0x20820 + (j1 << 18);
            do
            {
                if(l >= i1)
                {
                    break;
                }
                int k2 = lightUpdateBlockList[l++];
                int j3 = ((k2 & 0x3f) - 32) + i;
                int i4 = ((k2 >> 6 & 0x3f) - 32) + j;
                int l4 = ((k2 >> 12 & 0x3f) - 32) + k;
                int j5 = k2 >> 18 & 0xf;
                int l5 = getSavedLightValue(enumskyblock, j3, i4, l4);
                if(l5 == j5)
                {
                    setLightValue(enumskyblock, j3, i4, l4, 0);
                    if(j5 > 0)
                    {
                        int k6 = j3 - i;
                        int i7 = i4 - j;
                        int k7 = l4 - k;
                        if(k6 < 0)
                        {
                            k6 = -k6;
                        }
                        if(i7 < 0)
                        {
                            i7 = -i7;
                        }
                        if(k7 < 0)
                        {
                            k7 = -k7;
                        }
                        if(k6 + i7 + k7 < 17)
                        {
                            int i8 = 0;
                            while(i8 < 6) 
                            {
                                int j8 = (i8 % 2) * 2 - 1;
                                int k8 = j3 + (((i8 / 2) % 3) / 2) * j8;
                                int l8 = i4 + (((i8 / 2 + 1) % 3) / 2) * j8;
                                int i9 = l4 + (((i8 / 2 + 2) % 3) / 2) * j8;
                                int i6 = getSavedLightValue(enumskyblock, k8, l8, i9);
                                int j9 = Block.lightOpacity[getBlockId(k8, l8, i9)];
                                if(j9 == 0)
                                {
                                    j9 = 1;
                                }
                                if(i6 == j5 - j9)
                                {
                                    lightUpdateBlockList[i1++] = (k8 - i) + 32 + ((l8 - j) + 32 << 6) + ((i9 - k) + 32 << 12) + (j5 - j9 << 18);
                                }
                                i8++;
                            }
                        }
                    }
                }
            } while(true);
            l = 0;
        }
        do
        {
            if(l >= i1)
            {
                break;
            }
            int k1 = lightUpdateBlockList[l++];
            int i2 = ((k1 & 0x3f) - 32) + i;
            int l2 = ((k1 >> 6 & 0x3f) - 32) + j;
            int k3 = ((k1 >> 12 & 0x3f) - 32) + k;
            int j4 = getSavedLightValue(enumskyblock, i2, l2, k3);
            int i5 = getBlockId(i2, l2, k3);
            int k5 = Block.lightOpacity[i5];
            if(k5 == 0)
            {
                k5 = 1;
            }
            int j6 = 0;
            if(enumskyblock == EnumSkyBlock.Sky)
            {
                j6 = computeSkyLightValue(j4, i2, l2, k3, i5, k5);
            } else
            {
                j6 = computeBlockLightValue(j4, i2, l2, k3, i5, k5);
            }
            if(j6 != j4)
            {
                setLightValue(enumskyblock, i2, l2, k3, j6);
                if(j6 > j4)
                {
                    int l6 = i2 - i;
                    int j7 = l2 - j;
                    int l7 = k3 - k;
                    if(l6 < 0)
                    {
                        l6 = -l6;
                    }
                    if(j7 < 0)
                    {
                        j7 = -j7;
                    }
                    if(l7 < 0)
                    {
                        l7 = -l7;
                    }
                    if(l6 + j7 + l7 < 17 && i1 < lightUpdateBlockList.length - 6)
                    {
                        if(getSavedLightValue(enumskyblock, i2 - 1, l2, k3) < j6)
                        {
                            lightUpdateBlockList[i1++] = (i2 - 1 - i) + 32 + ((l2 - j) + 32 << 6) + ((k3 - k) + 32 << 12);
                        }
                        if(getSavedLightValue(enumskyblock, i2 + 1, l2, k3) < j6)
                        {
                            lightUpdateBlockList[i1++] = ((i2 + 1) - i) + 32 + ((l2 - j) + 32 << 6) + ((k3 - k) + 32 << 12);
                        }
                        if(getSavedLightValue(enumskyblock, i2, l2 - 1, k3) < j6)
                        {
                            lightUpdateBlockList[i1++] = (i2 - i) + 32 + ((l2 - 1 - j) + 32 << 6) + ((k3 - k) + 32 << 12);
                        }
                        if(getSavedLightValue(enumskyblock, i2, l2 + 1, k3) < j6)
                        {
                            lightUpdateBlockList[i1++] = (i2 - i) + 32 + (((l2 + 1) - j) + 32 << 6) + ((k3 - k) + 32 << 12);
                        }
                        if(getSavedLightValue(enumskyblock, i2, l2, k3 - 1) < j6)
                        {
                            lightUpdateBlockList[i1++] = (i2 - i) + 32 + ((l2 - j) + 32 << 6) + ((k3 - 1 - k) + 32 << 12);
                        }
                        if(getSavedLightValue(enumskyblock, i2, l2, k3 + 1) < j6)
                        {
                            lightUpdateBlockList[i1++] = (i2 - i) + 32 + ((l2 - j) + 32 << 6) + (((k3 + 1) - k) + 32 << 12);
                        }
                    }
                }
            }
        } while(true);
    }

    public boolean tickUpdates(boolean flag)
    {
        int i = scheduledTickTreeSet.size();
        if(i != scheduledTickSet.size())
        {
            throw new IllegalStateException("TickNextTick list out of synch");
        }
        if(i > 1000)
        {
            i = 1000;
        }
        for(int j = 0; j < i; j++)
        {
            NextTickListEntry nextticklistentry = (NextTickListEntry)scheduledTickTreeSet.first();
            if(!flag && nextticklistentry.scheduledTime > worldInfo.getWorldTime())
            {
                break;
            }
            scheduledTickTreeSet.remove(nextticklistentry);
            scheduledTickSet.remove(nextticklistentry);
            byte byte0 = 8;
            if(!checkChunksExist(nextticklistentry.xCoord - byte0, nextticklistentry.yCoord - byte0, nextticklistentry.zCoord - byte0, nextticklistentry.xCoord + byte0, nextticklistentry.yCoord + byte0, nextticklistentry.zCoord + byte0))
            {
                continue;
            }
            int k = getBlockId(nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord);
            if(k == nextticklistentry.blockID && k > 0)
            {
                Block.blocksList[k].updateTick(this, nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord, rand);
            }
        }

        return scheduledTickTreeSet.size() != 0;
    }

    public List func_41081_a(Chunk chunk, boolean flag)
    {
        ArrayList arraylist = null;
        ChunkCoordIntPair chunkcoordintpair = chunk.func_40740_k();
        int i = chunkcoordintpair.chunkXPos << 4;
        int j = i + 16;
        int k = chunkcoordintpair.chunkZPos << 4;
        int l = k + 16;
        Iterator iterator = scheduledTickSet.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
            if(nextticklistentry.xCoord >= i && nextticklistentry.xCoord < j && nextticklistentry.zCoord >= k && nextticklistentry.zCoord < l)
            {
                if(flag)
                {
                    scheduledTickTreeSet.remove(nextticklistentry);
                    iterator.remove();
                }
                if(arraylist == null)
                {
                    arraylist = new ArrayList();
                }
                arraylist.add(nextticklistentry);
            }
        } while(true);
        return arraylist;
    }

    public void randomDisplayUpdates(int i, int j, int k)
    {
        byte byte0 = 16;
        Random random = new Random();
        for(int l = 0; l < 1000; l++)
        {
            int i1 = (i + rand.nextInt(byte0)) - rand.nextInt(byte0);
            int j1 = (j + rand.nextInt(byte0)) - rand.nextInt(byte0);
            int k1 = (k + rand.nextInt(byte0)) - rand.nextInt(byte0);
            int l1 = getBlockId(i1, j1, k1);
            if(rand.nextInt(8) > j1 && l1 == 0)
            {
                spawnParticle("depthsuspend", (float)i1 + rand.nextFloat(), (float)j1 + rand.nextFloat(), (float)k1 + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
            }
            if(l1 > 0)
            {
                Block.blocksList[l1].randomDisplayTick(this, i1, j1, k1, random);
            }
        }

    }

    public List getEntitiesWithinAABBExcludingEntity(Entity entity, AxisAlignedBB axisalignedbb)
    {
        field_1012_M.clear();
        int i = MathHelper.floor_double((axisalignedbb.minX - 2D) / 16D);
        int j = MathHelper.floor_double((axisalignedbb.maxX + 2D) / 16D);
        int k = MathHelper.floor_double((axisalignedbb.minZ - 2D) / 16D);
        int l = MathHelper.floor_double((axisalignedbb.maxZ + 2D) / 16D);
        for(int i1 = i; i1 <= j; i1++)
        {
            for(int j1 = k; j1 <= l; j1++)
            {
                if(chunkExists(i1, j1))
                {
                    getChunkFromChunkCoords(i1, j1).getEntitiesWithinAABBForEntity(entity, axisalignedbb, field_1012_M);
                }
            }

        }

        return field_1012_M;
    }

    public List getEntitiesWithinAABB(Class class1, AxisAlignedBB axisalignedbb)
    {
        int i = MathHelper.floor_double((axisalignedbb.minX - 2D) / 16D);
        int j = MathHelper.floor_double((axisalignedbb.maxX + 2D) / 16D);
        int k = MathHelper.floor_double((axisalignedbb.minZ - 2D) / 16D);
        int l = MathHelper.floor_double((axisalignedbb.maxZ + 2D) / 16D);
        ArrayList arraylist = new ArrayList();
        for(int i1 = i; i1 <= j; i1++)
        {
            for(int j1 = k; j1 <= l; j1++)
            {
                if(chunkExists(i1, j1))
                {
                    getChunkFromChunkCoords(i1, j1).getEntitiesOfTypeWithinAAAB(class1, axisalignedbb, arraylist);
                }
            }

        }

        return arraylist;
    }

    public List getLoadedEntityList()
    {
        return loadedEntityList;
    }

    public void updateTileEntityChunkAndDoNothing(int i, int j, int k, TileEntity tileentity)
    {
        if(blockExists(i, j, k))
        {
            getChunkFromBlockCoords(i, k).setChunkModified();
        }
        for(int l = 0; l < worldAccesses.size(); l++)
        {
            ((IWorldAccess)worldAccesses.get(l)).doNothingWithTileEntity(i, j, k, tileentity);
        }

    }

    public int countEntities(Class class1)
    {
        int i = 0;
        for(int j = 0; j < loadedEntityList.size(); j++)
        {
            Entity entity = (Entity)loadedEntityList.get(j);
            if(class1.isAssignableFrom(entity.getClass()))
            {
                i++;
            }
        }

        return i;
    }

    public void addLoadedEntities(List list)
    {
        loadedEntityList.addAll(list);
        for(int i = 0; i < list.size(); i++)
        {
            obtainEntitySkin((Entity)list.get(i));
        }

    }

    public void unloadEntities(List list)
    {
        unloadedEntityList.addAll(list);
    }

    public void dropOldChunks()
    {
        while(chunkProvider.unload100OldestChunks()) ;
    }

    public boolean canBlockBePlacedAt(int i, int j, int k, int l, boolean flag, int i1)
    {
        int j1 = getBlockId(j, k, l);
        Block block = Block.blocksList[j1];
        Block block1 = Block.blocksList[i];
        AxisAlignedBB axisalignedbb = block1.getCollisionBoundingBoxFromPool(this, j, k, l);
        if(flag)
        {
            axisalignedbb = null;
        }
        if(axisalignedbb != null && !checkIfAABBIsClear(axisalignedbb))
        {
            return false;
        }
        if(block == Block.waterMoving || block == Block.waterStill || block == Block.lavaMoving || block == Block.lavaStill || block == Block.fire || block == Block.snow || block == Block.vine)
        {
            block = null;
        }
        return i > 0 && block == null && block1.canPlaceBlockOnSide(this, j, k, l, i1);
    }

    public PathEntity getPathToEntity(Entity entity, Entity entity1, float f)
    {
        Profiler.startSection("pathfind");
        int i = MathHelper.floor_double(entity.posX);
        int j = MathHelper.floor_double(entity.posY);
        int k = MathHelper.floor_double(entity.posZ);
        int l = (int)(f + 16F);
        int i1 = i - l;
        int j1 = j - l;
        int k1 = k - l;
        int l1 = i + l;
        int i2 = j + l;
        int j2 = k + l;
        ChunkCache chunkcache = new ChunkCache(this, i1, j1, k1, l1, i2, j2);
        PathEntity pathentity = (new PathFinder(chunkcache)).createEntityPathTo(entity, entity1, f);
        Profiler.endSection();
        return pathentity;
    }

    public PathEntity getEntityPathToXYZ(Entity entity, int i, int j, int k, float f)
    {
        Profiler.startSection("pathfind");
        int l = MathHelper.floor_double(entity.posX);
        int i1 = MathHelper.floor_double(entity.posY);
        int j1 = MathHelper.floor_double(entity.posZ);
        int k1 = (int)(f + 8F);
        int l1 = l - k1;
        int i2 = i1 - k1;
        int j2 = j1 - k1;
        int k2 = l + k1;
        int l2 = i1 + k1;
        int i3 = j1 + k1;
        ChunkCache chunkcache = new ChunkCache(this, l1, i2, j2, k2, l2, i3);
        PathEntity pathentity = (new PathFinder(chunkcache)).createEntityPathTo(entity, i, j, k, f);
        Profiler.endSection();
        return pathentity;
    }

    public boolean isBlockProvidingPowerTo(int i, int j, int k, int l)
    {
        int i1 = getBlockId(i, j, k);
        if(i1 == 0)
        {
            return false;
        } else
        {
            return Block.blocksList[i1].isIndirectlyPoweringTo(this, i, j, k, l);
        }
    }

    public boolean isBlockGettingPowered(int i, int j, int k)
    {
        if(isBlockProvidingPowerTo(i, j - 1, k, 0))
        {
            return true;
        }
        if(isBlockProvidingPowerTo(i, j + 1, k, 1))
        {
            return true;
        }
        if(isBlockProvidingPowerTo(i, j, k - 1, 2))
        {
            return true;
        }
        if(isBlockProvidingPowerTo(i, j, k + 1, 3))
        {
            return true;
        }
        if(isBlockProvidingPowerTo(i - 1, j, k, 4))
        {
            return true;
        }
        return isBlockProvidingPowerTo(i + 1, j, k, 5);
    }

    public boolean isBlockIndirectlyProvidingPowerTo(int i, int j, int k, int l)
    {
        if(isBlockNormalCube(i, j, k))
        {
            return isBlockGettingPowered(i, j, k);
        }
        int i1 = getBlockId(i, j, k);
        if(i1 == 0)
        {
            return false;
        } else
        {
            return Block.blocksList[i1].isPoweringTo(this, i, j, k, l);
        }
    }

    public boolean isBlockIndirectlyGettingPowered(int i, int j, int k)
    {
        if(isBlockIndirectlyProvidingPowerTo(i, j - 1, k, 0))
        {
            return true;
        }
        if(isBlockIndirectlyProvidingPowerTo(i, j + 1, k, 1))
        {
            return true;
        }
        if(isBlockIndirectlyProvidingPowerTo(i, j, k - 1, 2))
        {
            return true;
        }
        if(isBlockIndirectlyProvidingPowerTo(i, j, k + 1, 3))
        {
            return true;
        }
        if(isBlockIndirectlyProvidingPowerTo(i - 1, j, k, 4))
        {
            return true;
        }
        return isBlockIndirectlyProvidingPowerTo(i + 1, j, k, 5);
    }

    public EntityPlayer getClosestPlayerToEntity(Entity entity, double d)
    {
        return getClosestPlayer(entity.posX, entity.posY, entity.posZ, d);
    }

    public EntityPlayer getClosestPlayer(double d, double d1, double d2, double d3)
    {
        double d4 = -1D;
        EntityPlayer entityplayer = null;
        for(int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)playerEntities.get(i);
            double d5 = entityplayer1.getDistanceSq(d, d1, d2);
            if((d3 < 0.0D || d5 < d3 * d3) && (d4 == -1D || d5 < d4))
            {
                d4 = d5;
                entityplayer = entityplayer1;
            }
        }

        return entityplayer;
    }

    public EntityPlayer getClosestVulnerablePlayerToEntity(Entity entity, double d)
    {
        return getClosestVulnerablePlayer(entity.posX, entity.posY, entity.posZ, d);
    }

    public EntityPlayer getClosestVulnerablePlayer(double d, double d1, double d2, double d3)
    {
        double d4 = -1D;
        EntityPlayer entityplayer = null;
        for(int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)playerEntities.get(i);
            if(entityplayer1.capabilities.disableDamage)
            {
                continue;
            }
            double d5 = entityplayer1.getDistanceSq(d, d1, d2);
            if((d3 < 0.0D || d5 < d3 * d3) && (d4 == -1D || d5 < d4))
            {
                d4 = d5;
                entityplayer = entityplayer1;
            }
        }

        return entityplayer;
    }

    public EntityPlayer getPlayerEntityByName(String s)
    {
        for(int i = 0; i < playerEntities.size(); i++)
        {
            if(s.equals(((EntityPlayer)playerEntities.get(i)).username))
            {
                return (EntityPlayer)playerEntities.get(i);
            }
        }

        return null;
    }

    public void setChunkData(int i, int j, int k, int l, int i1, int j1, byte abyte0[])
    {
        int k1 = i >> 4;
        int l1 = k >> 4;
        int i2 = (i + l) - 1 >> 4;
        int j2 = (k + j1) - 1 >> 4;
        int k2 = 0;
        int l2 = j;
        int i3 = j + i1;
        if(l2 < 0)
        {
            l2 = 0;
        }
        if(i3 > field_35472_c)
        {
            i3 = field_35472_c;
        }
        for(int j3 = k1; j3 <= i2; j3++)
        {
            int k3 = i - j3 * 16;
            int l3 = (i + l) - j3 * 16;
            if(k3 < 0)
            {
                k3 = 0;
            }
            if(l3 > 16)
            {
                l3 = 16;
            }
            for(int i4 = l1; i4 <= j2; i4++)
            {
                int j4 = k - i4 * 16;
                int k4 = (k + j1) - i4 * 16;
                if(j4 < 0)
                {
                    j4 = 0;
                }
                if(k4 > 16)
                {
                    k4 = 16;
                }
                k2 = getChunkFromChunkCoords(j3, i4).setChunkData(abyte0, k3, l2, j4, l3, i3, k4, k2);
                markBlocksDirty(j3 * 16 + k3, l2, i4 * 16 + j4, j3 * 16 + l3, i3, i4 * 16 + k4);
            }

        }

    }

    public void sendQuittingDisconnectingPacket()
    {
    }

    public void checkSessionLock()
    {
        saveHandler.checkSessionLock();
    }

    public void setWorldTime(long l)
    {
        worldInfo.setWorldTime(l);
    }

    public long getWorldSeed()
    {
        return worldInfo.getRandomSeed();
    }

    public long getWorldTime()
    {
        return worldInfo.getWorldTime();
    }

    public ChunkCoordinates getSpawnPoint()
    {
        return new ChunkCoordinates(worldInfo.getSpawnX(), worldInfo.getSpawnY(), worldInfo.getSpawnZ());
    }

    public void setSpawnPoint(ChunkCoordinates chunkcoordinates)
    {
        worldInfo.setSpawn(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ);
    }

    public void joinEntityInSurroundings(Entity entity)
    {
        int i = MathHelper.floor_double(entity.posX / 16D);
        int j = MathHelper.floor_double(entity.posZ / 16D);
        byte byte0 = 2;
        for(int k = i - byte0; k <= i + byte0; k++)
        {
            for(int l = j - byte0; l <= j + byte0; l++)
            {
                getChunkFromChunkCoords(k, l);
            }

        }

        if(!loadedEntityList.contains(entity))
        {
            loadedEntityList.add(entity);
        }
    }

    public boolean canMineBlock(EntityPlayer entityplayer, int i, int j, int k)
    {
        return true;
    }

    public void setEntityState(Entity entity, byte byte0)
    {
    }

    public void updateEntityList()
    {
        loadedEntityList.removeAll(unloadedEntityList);
        for(int i = 0; i < unloadedEntityList.size(); i++)
        {
            Entity entity = (Entity)unloadedEntityList.get(i);
            int l = entity.chunkCoordX;
            int j1 = entity.chunkCoordZ;
            if(entity.addedToChunk && chunkExists(l, j1))
            {
                getChunkFromChunkCoords(l, j1).removeEntity(entity);
            }
        }

        for(int j = 0; j < unloadedEntityList.size(); j++)
        {
            releaseEntitySkin((Entity)unloadedEntityList.get(j));
        }

        unloadedEntityList.clear();
        for(int k = 0; k < loadedEntityList.size(); k++)
        {
            Entity entity1 = (Entity)loadedEntityList.get(k);
            if(entity1.ridingEntity != null)
            {
                if(!entity1.ridingEntity.isDead && entity1.ridingEntity.riddenByEntity == entity1)
                {
                    continue;
                }
                entity1.ridingEntity.riddenByEntity = null;
                entity1.ridingEntity = null;
            }
            if(!entity1.isDead)
            {
                continue;
            }
            int i1 = entity1.chunkCoordX;
            int k1 = entity1.chunkCoordZ;
            if(entity1.addedToChunk && chunkExists(i1, k1))
            {
                getChunkFromChunkCoords(i1, k1).removeEntity(entity1);
            }
            loadedEntityList.remove(k--);
            releaseEntitySkin(entity1);
        }

    }

    public IChunkProvider getIChunkProvider()
    {
        return chunkProvider;
    }

    public void playNoteAt(int i, int j, int k, int l, int i1)
    {
        int j1 = getBlockId(i, j, k);
        if(j1 > 0)
        {
            Block.blocksList[j1].powerBlock(this, i, j, k, l, i1);
        }
    }

    public ISaveHandler func_40479_y()
    {
        return saveHandler;
    }

    public WorldInfo getWorldInfo()
    {
        return worldInfo;
    }

    public void updateAllPlayersSleepingFlag()
    {
        allPlayersSleeping = !playerEntities.isEmpty();
        Iterator iterator = playerEntities.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            EntityPlayer entityplayer = (EntityPlayer)iterator.next();
            if(entityplayer.isPlayerSleeping())
            {
                continue;
            }
            allPlayersSleeping = false;
            break;
        } while(true);
    }

    protected void wakeUpAllPlayers()
    {
        allPlayersSleeping = false;
        Iterator iterator = playerEntities.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            EntityPlayer entityplayer = (EntityPlayer)iterator.next();
            if(entityplayer.isPlayerSleeping())
            {
                entityplayer.wakeUpPlayer(false, false, true);
            }
        } while(true);
        stopPrecipitation();
    }

    public boolean isAllPlayersFullyAsleep()
    {
        if(allPlayersSleeping && !multiplayerWorld)
        {
            for(Iterator iterator = playerEntities.iterator(); iterator.hasNext();)
            {
                EntityPlayer entityplayer = (EntityPlayer)iterator.next();
                if(!entityplayer.isPlayerFullyAsleep())
                {
                    return false;
                }
            }

            return true;
        } else
        {
            return false;
        }
    }

    public float getWeightedThunderStrength(float f)
    {
        return (prevThunderingStrength + (thunderingStrength - prevThunderingStrength) * f) * getRainStrength(f);
    }

    public float getRainStrength(float f)
    {
        return prevRainingStrength + (rainingStrength - prevRainingStrength) * f;
    }

    public void setRainStrength(float f)
    {
        prevRainingStrength = f;
        rainingStrength = f;
    }

    public boolean getIsThundering()
    {
        return (double)getWeightedThunderStrength(1.0F) > 0.90000000000000002D;
    }

    public boolean isRaining()
    {
        return (double)getRainStrength(1.0F) > 0.20000000000000001D;
    }

    public boolean canLightningStrikeAt(int i, int j, int k)
    {
        if(!isRaining())
        {
            return false;
        }
        if(!canBlockSeeTheSky(i, j, k))
        {
            return false;
        }
        if(func_35461_e(i, k) > j)
        {
            return false;
        }
        BiomeGenBase biomegenbase = getWorldChunkManager().getBiomeGenAt(i, k);
        if(biomegenbase.getEnableSnow())
        {
            return false;
        } else
        {
            return biomegenbase.canSpawnLightningBolt();
        }
    }

    public void setItemData(String s, WorldSavedData worldsaveddata)
    {
        mapStorage.setData(s, worldsaveddata);
    }

    public WorldSavedData loadItemData(Class class1, String s)
    {
        return mapStorage.loadData(class1, s);
    }

    public int getUniqueDataId(String s)
    {
        return mapStorage.getUniqueDataId(s);
    }

    public void playAuxSFX(int i, int j, int k, int l, int i1)
    {
        playAuxSFXAtEntity(null, i, j, k, l, i1);
    }

    public void playAuxSFXAtEntity(EntityPlayer entityplayer, int i, int j, int k, int l, int i1)
    {
        for(int j1 = 0; j1 < worldAccesses.size(); j1++)
        {
            ((IWorldAccess)worldAccesses.get(j1)).playAuxSFX(entityplayer, i, j, k, l, i1);
        }

    }

    public int func_35452_b()
    {
        return field_35472_c;
    }

    public Random setRandomSeed(int i, int j, int k)
    {
        long l = (long)i * 0x4f9939f508L + (long)j * 0x1ef1565bd5L + getWorldInfo().getRandomSeed() + (long)k;
        rand.setSeed(l);
        return rand;
    }

    public boolean updatingLighting()
    {
        return false;
    }

    public void scheduleLightingUpdate(EnumSkyBlock enumskyblock, int i, int j, int k, int l, int i1, int j1)
    {
    }

    public SpawnListEntry func_40474_a(EnumCreatureType enumcreaturetype, int i, int j, int k)
    {
        List list = getIChunkProvider().func_40377_a(enumcreaturetype, i, j, k);
        if(list == null || list.isEmpty())
        {
            return null;
        } else
        {
            return (SpawnListEntry)WeightedRandom.func_35733_a(rand, list);
        }
    }

    public ChunkPosition func_40477_b(String s, int i, int j, int k)
    {
        return getIChunkProvider().func_40376_a(this, s, i, j, k);
    }
}
