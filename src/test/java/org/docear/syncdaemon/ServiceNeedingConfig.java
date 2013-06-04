package org.docear.syncdaemon;

import com.typesafe.config.Config;

public class ServiceNeedingConfig implements AServiceInterface, NeedsConfig {

    private Config config;

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }
}
