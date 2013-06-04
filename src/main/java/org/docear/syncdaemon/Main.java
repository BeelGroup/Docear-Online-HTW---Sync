package org.docear.syncdaemon;

import java.io.IOException;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        final Logger logger = LoggerFactory.getLogger(Main.class);
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
        final Config config = loadConfig();
        final Daemon daemon = new Daemon(config);
        daemon.onStart();
        daemon.onStop();
    }

    private static Config loadConfig() {
        final boolean startedWithSbt = System.getProperty("started_with_sbt", "false").equals("true");
        final String defaultConfigFile = startedWithSbt ? "application.conf" : "prod.conf";
        final String configFile = System.getProperty("config.file", defaultConfigFile);
        return ConfigFactory.load(configFile);
    }
}
