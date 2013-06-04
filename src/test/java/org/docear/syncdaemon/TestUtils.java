package org.docear.syncdaemon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public final class TestUtils {
    private TestUtils() {
    }

    public static Daemon testDaemonWithAdditionalConfiguration(final String config) {
        final Config overWritingConfig = ConfigFactory.parseString(config);
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
