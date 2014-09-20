package com.flushoutsolutions.foheart.appDataBase;

import android.os.Environment;

import com.flushoutsolutions.foheart.application.FoHeart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by Manuel on 18/09/2014.
 */
public class DataBaseDebug {
    public static boolean exportDatabase(String databaseName)
    {
        File folder = new File(Environment.getExternalStorageDirectory() + "/foheart");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success)
        {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "//data//"+ FoHeart.getAppContext().getPackageName()+"//databases//"+databaseName+"";
                    String backupDBPath = "/foheart/"+databaseName;
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    System.out.println("sdpath "+sd.getPath());
                    System.out.println("currentDBPath "+currentDBPath);
                    System.out.println("backupDB "+backupDB);
                    System.out.println("currentDB "+currentDB);
                    if (currentDB.exists())
                    {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());

                        src.close();
                        dst.close();

                        return true;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }
}
