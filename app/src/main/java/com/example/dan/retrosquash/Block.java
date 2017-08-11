package com.example.dan.retrosquash;

import android.graphics.Point;
import android.util.Log;

import java.util.List;

import static android.content.ContentValues.TAG;

public class Block {
    int width;
    int height;
    Point position;
    static int numBlocks;
    static List<Block> blockList;

    public Block() {
    }

    public Block(int width, int height, Point position) {
        this.width = width;
        this.height = height;
        this.position = position;
    }

    public static int getNumBlocks() {
        return numBlocks;
    }

    public static void setNumBlocks(int numBlocks) {
        Block.numBlocks = numBlocks;
    }

    public static List<Block> getBlockList() {
        return blockList;
    }

    public static void setBlockList(List<Block> blockList, int numBlocks,
                                    int width, int height, Point position) {
        for (int i = 0; i < numBlocks; i++) {
            blockList.add(new Block(width, height, position));

            Block block = blockList.get(i);
            Log.i(TAG, "setBlockList: " + block.position);
            block.position.x += width;
            Log.i(TAG, "setBlockList: " + block.position);
        }
        Block.blockList = blockList;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }
}
