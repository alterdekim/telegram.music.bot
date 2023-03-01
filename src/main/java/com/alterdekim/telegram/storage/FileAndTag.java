package com.alterdekim.telegram.storage;

import org.jaudiotagger.tag.Tag;

import java.io.File;

public class FileAndTag {

    private final Tag tag;
    private final File file;
    public FileAndTag(Tag tag, File file) {
        this.tag = tag;
        this.file = file;
    }

    public Tag getTag() {
        return tag;
    }

    public File getFile() {
        return file;
    }
}
