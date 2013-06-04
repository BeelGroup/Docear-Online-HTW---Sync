package org.docear.syncdaemon;

import com.typesafe.config.Config;

public interface NeedsConfig {
    void setConfig(Config config);
}
