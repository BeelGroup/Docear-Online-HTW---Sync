package org.docear.syncdaemon;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class Daemon {
    private static final Logger logger = LoggerFactory.getLogger(Daemon.class);

    private Config config;
    private List<Plugin> plugins = new LinkedList<Plugin>();
    private final Map<Class, Object> serviceInterfaceToServiceInstanceMap = new HashMap<Class, Object>();

    public Daemon() {
        this(ConfigFactory.load());
    }

    public Daemon(Config config) {
        this.config = config;
        setupPlugins();
    }

    private void setupPlugins() {
        final List<Integer> priorities = new LinkedList<Integer>();
        final Map<Integer, String> priorityToClassNameMap = new HashMap<Integer, String>();
        for (final String configParts : config.getStringList("daemon.plugins")) {
            final String[] parts = configParts.split(":");
            final int priority = Integer.parseInt(parts[0]);
            priorities.add(priority);
            final String pluginClassName = parts[1];
            priorityToClassNameMap.put(priority, pluginClassName);
        }
        Collections.sort(priorities);
        for (final int key : priorities) {
            final String className = priorityToClassNameMap.get(key);
            final Plugin plugin = instantiatePlugin(className);
            plugins.add(plugin);
        }
    }

    private Plugin instantiatePlugin(String className) {
        try {
            final Class<?> pluginClass = Class.forName(className);
            Class[] argTypes = {Daemon.class};
            Constructor constructor = pluginClass.getDeclaredConstructor(argTypes);
            Object[] arguments = {this};
            Object instance = constructor.newInstance(arguments);
            return (Plugin) instance;
        } catch (Exception e) {
            throw new RuntimeException("cannot initialize plugin " + className, e);
        }
    }

    public static Daemon createWithAdditionalConfig(final Config config) {
        final Daemon daemon = new Daemon();
        daemon.config = config.withFallback(daemon.config);
        return daemon;
    }

    public <T extends Plugin> T plugin(Class<T> clazz) {
        for (final Plugin plugin : plugins) {
            if (clazz.isInstance(plugin)) {
                return (T)plugin;
            }
        }
        return null;
    }

	public <T> T service(Class<T> clazz) {
        T result = (T) serviceInterfaceToServiceInstanceMap.get(clazz);
        if (result == null) {
            final String implClassName = config.getString("daemon.di." + clazz.getName());
            if (isNotEmpty(implClassName)) {
                result = createInstanceWithDefaultConstructor(implClassName);
                if (result instanceof NeedsConfig) {
                    final NeedsConfig needsConfig = (NeedsConfig) result;
                    needsConfig.setConfig(config);
                }
            } else {
                throw new IllegalStateException("can't find implementation for " + clazz);
            }
        }
        return result;
    }

    private <T> T createInstanceWithDefaultConstructor(String implClassName){
        try {
            final Class<?> implClass = Class.forName(implClassName);
            return (T) implClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("can't instantiate " + implClassName, e);
        }
    }

    public void onStart() {
        for (final Plugin plugin : plugins) {
            if (plugin.enabled()) {
                plugin.onStart();
            }
        }
        /**
         * - Actor System for internal and external communication
         * - RUNNING_PID with PID
         * - RUNNING_PORT with port of actor system
         * - initialize index-db
         * - initialize file system
         *   - start jNotify
         * - refresh index
         */
    }

    public void onStop() {
        for (final Plugin plugin : plugins) {
            plugin.onStop();
        }
	}

    public Config getConfig() {
        return config;
    }

    /* in package scope for testing */
    void addPlugin(Plugin plugin) {
        plugins.add(plugin);
    }
}
