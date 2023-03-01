package com.alterdekim.telegram.storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class AudioFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        try {
            return (
                    (
                            pathname.isDirectory() && !FileUtils.isEmptyDirectory(pathname)
                    ) ||
                            (
                                    !pathname.isDirectory() && Arrays.stream(FileExtensions.array).anyMatch(FilenameUtils.getExtension(pathname.getName())::contains)
                            )
            );
        } catch ( Exception e ) {
            return false;
        }
    }
}
