package com.sunsharing.sfs.dataserver.block;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by criss on 14-7-2.
 */
public class BlockCache {
    static Logger logger = Logger.getLogger(BlockCache.class);

    static List<Block> blocks = Collections.synchronizedList(new ArrayList<Block>());

    public static void addBlock(int blocId)
    {
        logger.info("create block::"+blocId);
        Block block = new Block();
        block.setBlockId(blocId);
        blocks.add(block);
    }

    public static int getBlockSize()
    {
        return blocks.size();
    }

    public static void initFromDisk()
    {
        List<Block> blockss = BlockWrite.getInstance().loadBlocks();
        for(Block block:blockss)
        {
            blocks.add(block);
        }
    }

}
