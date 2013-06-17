package org.docear.syncdaemon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

import static org.apache.commons.io.FileUtils.getTempDirectoryPath;

public final class TestUtils {
    private TestUtils() {
    }

    public static Daemon testDaemonWithAdditionalConfiguration(final String config) {
        final Config overWritingConfig = ConfigFactory.parseString(config);
        final Daemon daemon = Daemon.createWithAdditionalConfig(overWritingConfig);
        return daemon;
    }

    public static Daemon testDaemon() {
        String tmpPath = getTempDirectoryPath() + "/docear-sync/" + UUID.randomUUID().toString();
        tmpPath = FilenameUtils.normalize(tmpPath).replace("\\", "/");
        return testDaemonWithAdditionalConfiguration("daemon.docear.home=\"" + tmpPath + "\"");
    }

    public static String diSetting(final Class interfaceClass, final Class implClass) {
        return String.format("daemon.di.%s=%s", interfaceClass.getName(), implClass.getName());
    }

    public static Daemon daemonWithService(Class<? extends Object> interfaceClass, Class<? extends Object> implClass) {
        final String diSetting = diSetting(interfaceClass, implClass);
        return testDaemonWithAdditionalConfiguration(diSetting);
    }

}
