package com.carloosx.gesturerecorder.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.carloosx.gesturerecorder.settings.AppSettings;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Utils {
    public static final String TAG = Utils.class.getSimpleName();

    public static boolean isWorkingDirDefault(AppSettings settings) {
        File extDir = Environment.getExternalStorageDirectory();
        if (extDir == null) return true;
        return TextUtils.equals(extDir.getAbsolutePath(), settings.getWorkingDir());
    }

    public static void saveLineData(File file, LineData data) throws IOException {
        ILineDataSet setX = data.getDataSetByIndex(0);
        saveLineData(file, data, 0, setX.getEntryCount());
    }

    public static void saveLineData(File file, LineData data, int index, int length) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(file)) {
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            ILineDataSet setX = data.getDataSetByIndex(0);
            ILineDataSet setY = data.getDataSetByIndex(1);
            ILineDataSet setZ = data.getDataSetByIndex(2);

            if ((setX.getEntryCount() != setY.getEntryCount()) || (setY.getEntryCount() != setZ.getEntryCount()))
                throw new IllegalStateException("Z, Y, Z data set is different");

            float startX = setX.getEntryForIndex(index).getX(); // fix start time
            for (int i = index; i < index + length; i++) {
                String str = String.format(Locale.ROOT, "%f,%f,%f,%f\n",
                        setX.getEntryForIndex(i).getX() - startX,
                        setX.getEntryForIndex(i).getY(),
                        setY.getEntryForIndex(i).getY(),
                        setZ.getEntryForIndex(i).getY());
                osw.write(str);
            }
            osw.close();
        }
    }

    public static List<FileEntry> loadFileData(String strFile) throws FileNotFoundException {
        List<FileEntry> ret = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(strFile))) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (TextUtils.isEmpty(line)) continue;

                try {
                    FileEntry entry = FileEntry.fromString(line);
                    ret.add(entry);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse line " + line, e);
                }
            }
        }

        return ret;
    }

    public static Pair<FileName, List<FileEntry>> loadData(String strFile) throws Exception {
        File file = new File(strFile);
        String fileName = file.getName();
        int underscorePos = fileName.indexOf("_");
        int pointPos = fileName.indexOf(".");
        if ((underscorePos == -1) || (pointPos == -1) || (underscorePos > pointPos))
            throw new Exception("File name is invalid");
        Label label = Label.valueOf(fileName.substring(0, underscorePos));
        long timestamp = Long.parseLong(fileName.substring(underscorePos + 1, pointPos - 1));

        return new Pair<>(new FileName(label, timestamp), loadFileData(strFile));
    }

    public static String generateFileName(String label, long timestamp) {
        return label + "_" + timestamp + ".csv";
    }

    public static class FileName {
        public final Label label;
        public final long timestamp;

        public FileName(Label label, long timestamp) {
            this.label = label;
            this.timestamp = timestamp;
        }
    }

    public static class FileEntry {
        public final float timestamp;
        public final float x;
        public final float y;
        public final float z;

        public FileEntry(float timestamp, float x, float y, float z) {
            this.timestamp = timestamp;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public static FileEntry fromString(String str) throws Exception {
            String[] pairs = str.split(",");
            if (pairs.length != 4) throw new Exception("Incorrect CSV parts count");

            NumberFormat format = NumberFormat.getInstance(Locale.ROOT);

            return new FileEntry(
                    format.parse(pairs[0]).floatValue(),
                    format.parse(pairs[1]).floatValue(),
                    format.parse(pairs[2]).floatValue(),
                    format.parse(pairs[3]).floatValue());
        }
    }

}
