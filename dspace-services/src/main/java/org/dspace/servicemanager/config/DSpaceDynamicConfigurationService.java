/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.servicemanager.config;

import java.io.File;
import java.util.*;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.dspace.services.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Dynamic Configuration Service Implementation
 *
 * @author DSpace @ Lyncode
 * @author Peter Dietz @ Longsight
 */
public class DSpaceDynamicConfigurationService implements ConfigurationService {
    private static final Logger log = LoggerFactory.getLogger(DSpaceDynamicConfigurationService.class);

    private static final String DSPACE = "dspace";
    private static final String DSPACE_HOME = "dspace.dir";
    private static final String DOT_CONFIG = ".cfg";
    private static final String DSPACE_MODULES_CONFIG_PATH = "config" + File.separator + "modules";
    private static final String DSPACE_ADDONS_CONFIG_PATH = "config" + File.separator + "addons";
    private static final String DSPACE_CONFIG_PATH = "config/" + DSPACE + DOT_CONFIG;

    private String home;
    private DSpacePropertiesConfiguration config = null;
    private Map<String, DSpacePropertiesConfiguration> modulesConfig = null;

    public DSpaceDynamicConfigurationService () {
        home = null;
        loadConfiguration();
    }
    public DSpaceDynamicConfigurationService (String home) {
        this.home = home;
        loadConfiguration();
    }

    /*
    * <p>The initial value of {@code dspace.dir} will be:</p>
     * <ol>
     *  <li>the value of the system property {@code dspace.dir} if defined;</li>
     *  <li>else the value of {@code providedHome} if not null;</li>
     *  <li>else the servlet container's home + "/dspace/" if defined (see getCatalina());</li>
     *  <li>else the user's home directory if defined;</li>
     *  <li>else "/".

     */
    private void loadConfiguration () {
        List<String> homePathList = new ArrayList<String>();
        homePathList.add(System.getProperty(DSPACE_HOME));
        homePathList.add(this.home);
        homePathList.add(System.getProperty("catalina.base") + File.separatorChar + DSPACE);
        homePathList.add(System.getProperty("catalina.home") + File.separatorChar + DSPACE);
        homePathList.add(System.getProperty("user.home") + File.separatorChar + DSPACE);
        homePathList.add(File.separatorChar + DSPACE);

        //Best dspace.dir is the first that exists
        String homePath = null;
        for(String homePathEntry : homePathList) {
            File homePathFile = new File(homePathEntry  + File.separator + DSPACE_CONFIG_PATH);
            if(homePathFile.exists()) {
                homePath = homePathEntry;
                log.error("Using dspace.dir of: " + homePathEntry);
                break;
            }
        }

        // now we load the settings from properties files
        try{
            config = new DSpacePropertiesConfiguration(homePath + File.separatorChar + DSPACE_CONFIG_PATH);
            File modulesDirectory = new File(homePath + File.separator + DSPACE_MODULES_CONFIG_PATH + File.separator);
            modulesConfig = new TreeMap<String, DSpacePropertiesConfiguration>();
            if(modulesDirectory.exists()){
                try{
                    Resource[] resources = new PathMatchingResourcePatternResolver().getResources(modulesDirectory.toURI().toURL().toString() + "*" + DOT_CONFIG);
                    if(resources != null){
                        for(Resource resource : resources){
                            String prefix = resource.getFilename().substring(0, resource.getFilename().lastIndexOf("."));
                            modulesConfig.put(prefix, new DSpacePropertiesConfiguration(resource.getFile()));
                        }
                    }
                }catch (Exception e){
                    log.error("Error while loading the modules properties from:" + modulesDirectory.getAbsolutePath());
                }
            }else{
                log.error("Failed to load the modules properties since (" + homePath + File.separator + DSPACE_MODULES_CONFIG_PATH + "): Does not exist");
            }

        } catch (IllegalArgumentException e){
            //This happens if we don't have a modules directory
            log.error("Error while loading the module properties since (" +  homePath + File.separator + DSPACE_MODULES_CONFIG_PATH + "): is not a valid directory", e);
        } catch (ConfigurationException e) {
            // This happens if an error occurs on parsing files
            log.error("Error while loading properties from " +  homePath + File.separator + DSPACE_MODULES_CONFIG_PATH , e);
        }
    }

    @Override
    public <T> T getPropertyAsType(String name, Class<T> type) {
        String value = config.getString(name);
        return convert(value, type);
    }

    @Override
    public <T> T getPropertyAsType(String module, String name, Class<T> type) {
        if (modulesConfig.containsKey(module)) {
            String value = modulesConfig.get(module).getString(name);
            return convert(value, type);
        } else {
            return null;
        }
    }

    @Override
    public <T> T getPropertyAsType(String name, T defaultValue) {
        return getPropertyAsType(name, defaultValue, false);
    }

    @Override
    public <T> T getPropertyAsType(String module, String name, T defaultValue) {
        return getPropertyAsType(module, name, defaultValue, false);
    }

    @Override
    public <T> T getPropertyAsType(String name, T defaultValue, boolean setDefaultIfNotFound) {
        String value = getProperty(name);
        T property = null;
        if (defaultValue == null) {
            property = null; // just return null when default value is null
        } else if (value == null) {
            property = defaultValue; // just return the default value if nothing is currently set
            // also set the default value as the current stored value
            if (setDefaultIfNotFound) {
                setProperty(name, defaultValue);
            }
        } else {
            // something is already set so we convert the stored value to match the type
            property = (T)convert(value, defaultValue.getClass());
        }
        return property;
    }

    @Override
    public <T> T getPropertyAsType(String module, String name, T defaultValue, boolean setDefaultIfNotFound) {
        String value = getProperty(module, name);
        T property = null;
        if (defaultValue == null) {
            property = null; // just return null when default value is null
        } else if (value == null) {
            property = defaultValue; // just return the default value if nothing is currently set
            // also set the default value as the current stored value
            if (setDefaultIfNotFound) {
                setProperty(module, name, defaultValue);
            }
        } else {
            // something is already set so we convert the stored value to match the type
            property = (T)convert(value, defaultValue.getClass());
        }
        return property;
    }

    @Override
    public Map<String, String> getAllProperties() {
        //All props? Is this just a map of props, or are we to include the modules...
        return new HashMap(config.getProperties());
    }

    @Override
    public String getProperty(String key) {
        return config.getProperty(key).toString();
    }
    @Override
    public boolean setProperty(String key, Object value) {
        config.setProperty(key, value);
        return true;
    }

    @Override
    public List<String> getList(String key) {
        return config.getList(key);
    }
    @Override
    public void addConfigurationListener(ConfigurationListener listener) {
        config.addConfigurationEventListener(listener);
    }


    @Override
    public String getProperty(String module, String key) {
        if (modulesConfig.containsKey(module))
            return modulesConfig.get(module).getProperty(key).toString();
        return null;
    }

    @Override
    public Properties getProperties() {
        return config.getProperties();
    }

    @Override
    public Properties getProperties(String module) {
        if(modulesConfig.containsKey(module)) {
            return modulesConfig.get(module).getProperties();
        } else {
            return null;
        }
    }

    @Override
    public boolean setProperty(String module, String key, Object value) {
        if (modulesConfig.containsKey(module)) {
            modulesConfig.get(module).setProperty(key, value);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getList(String module, String key) {
        if (modulesConfig.containsKey(module))
            return modulesConfig.get(module).getList(key);
        return new ArrayList<String>();
    }
    @Override
    public void addConfigurationListener(String module,
                                               ConfigurationListener listener) {
        if (modulesConfig.containsKey(module))
            modulesConfig.get(module).addConfigurationEventListener(listener);
    }

    @Override
    public String getDescription(String key) {
        return config.getDescription(key);
    }
    @Override
    public String getDescription(String module, String key) {
        if (modulesConfig.containsKey(module))
            return modulesConfig.get(module).getDescription(key);
        return null;
    }

    private <T> T convert(String value, Class<T> type) {
        SimpleTypeConverter converter = new SimpleTypeConverter();

        if (value != null) {
            if (type.isArray()) {
                String[] values = value.split(",");
                return (T)converter.convertIfNecessary(values, type);
            }

            if (type.isAssignableFrom(String.class)) {
                return (T)value;
            }
        } else {
            if (boolean.class.equals(type)) {
                return (T)Boolean.FALSE;
            } else if (int.class.equals(type) || long.class.equals(type)) {
                return (T)converter.convertIfNecessary(0, type);
            }
        }

        return (T)converter.convertIfNecessary(value, type);
    }
}
