package logger;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
    public static final Logger logger=Logger.getLogger("My Logger");
    public static void initialize(String file_name){
        FileHandler fileHandler;
        try {
            File file = new File(file_name);
            if (!file.exists())
                file.createNewFile();
            fileHandler = new FileHandler(file_name, true);
            Log.logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        }catch (Exception e){
            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
        }
    }
}
