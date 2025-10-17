package utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public class ReportUtils {

    /**
     * Return absolute path to the newest file in 'dir' that ends with given extension.
     * Returns null if none found.
     */
    public static String getLatestReportFile(String dirPath, final String ext) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) return null;

        FilenameFilter filter = (dir1, name) -> name.toLowerCase().endsWith(ext.toLowerCase());
        File[] files = dir.listFiles(filter);
        if (files == null || files.length == 0) return null;

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        return files[0].getAbsolutePath();
    }
}
