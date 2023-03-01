package com.alterdekim.telegram.storage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class FileSearcher {

    public static ArrayList<File> getList( String path ) throws Exception {
        return (ArrayList<File>) FileUtils.listFiles(new File(path), FileExtensions.array, false);
    }

    public static TreeNode<File> getTree( TreeNode<File> n ) throws Exception {
        File[] files = n.getKey().listFiles(new AudioFileFilter());
        for (File file : files) {
            if (file.isDirectory() && !FileUtils.isEmptyDirectory(file)) {
                n.addChild(getTree(new TreeNode<File>(file)));
            } else {
                n.addChild(new TreeNode<File>(file));
            }
        }
        return n;
    }
}
