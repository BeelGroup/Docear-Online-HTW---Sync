package org.docear.syncdaemon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.apache.commons.io.FileUtils.getTempDirectoryPath;

public final class TestUtils {
    private TestUtils() {
    }

    public static Daemon testDaemonWithAdditionalConfiguration(String config) {
        //add tmp path to config
        String tmpPath = getTempDirectoryPath() + "/docear-sync/" + UUID.randomUUID().toString();
        tmpPath = FilenameUtils.normalize(tmpPath).replace("\\", "/");
        config += "\ndaemon.docear.home=\"" + tmpPath + "\"";
        config += "\nfileactors.listener.disabled=true";

        final Config overWritingConfig = ConfigFactory.parseString(config);



        final File homePath = new File(overWritingConfig.getString("daemon.docear.home")+File.separator+"projects");
        homePath.mkdirs();
        final File confFile = new File(homePath,"config.xml");
        final File testConfResource = new File(TestUtils.class.getResource("/config.xml").getFile());
        try {
            FileUtils.copyFile(testConfResource, confFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Daemon daemon = Daemon.createWithAdditionalConfig(overWritingConfig);

        return daemon;
    }



    public static Daemon testDaemon() {


        return testDaemonWithAdditionalConfiguration("");
    }

    public static String diSetting(final Class interfaceClass, final Class implClass) {
        return String.format("daemon.di.%s=%s", interfaceClass.getName(), implClass.getName());
    }

    public static Daemon daemonWithService(Class<? extends Object> interfaceClass, Class<? extends Object> implClass) {
        final String diSetting = diSetting(interfaceClass, implClass);
        return testDaemonWithAdditionalConfiguration(diSetting);
    }

}
