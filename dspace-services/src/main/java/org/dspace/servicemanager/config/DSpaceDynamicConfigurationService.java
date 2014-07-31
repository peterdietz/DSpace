/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.servicemanager.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.dspace.services.DynamicConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Dynamic Configuration Service Implementation
 *
 * @author DSpace @ Lyncode
 * @author Peter Dietz @ Longsight
 */
public class DSpaceDynamicConfigurationService implements
        DynamicConfigurationService {
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
    private Map<String, DSpacePropertiesConfiguration> addonsConfig = null;

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


        try{
            File addonsDirectory = new File(homePath + File.separator + DSPACE_ADDONS_CONFIG_PATH + File.separator);
            addonsConfig = new TreeMap<String, DSpacePropertiesConfiguration>();
            if(addonsDirectory.exists()){
                try{
                    Resource[] resources = new PathMatchingResourcePatternResolver().getResources(addonsDirectory.toURI().toURL().toString() + "*" + DOT_CONFIG);
                    if(resources != null){
                        for(Resource resource : resources){
                            String prefix = resource.getFilename().substring(0, resource.getFilename().lastIndexOf("."));
                            addonsConfig.put(prefix, new DSpacePropertiesConfiguration(resource.getFile()));
                        }
                    }
                }catch (Exception e){
                    log.error("Error while loading the addons properties from:" + addonsDirectory.getAbsolutePath());
                }
            }else{
                log.error("Failed to load the addons properties since (" + homePath + File.separator + DSPACE_ADDONS_CONFIG_PATH + "): Does not exist");
            }

        } catch (IllegalArgumentException e){
            //This happens if we don't have a modules directory
            log.error("Error while loading the module properties since (" +  homePath + File.separator + DSPACE_ADDONS_CONFIG_PATH + "): is not a valid directory", e);
        }
    }
    @Override
    public Object getProperty(String key) {
        return config.getProperty(key);
    }
    @Override
    public boolean setProperty(String key, Object value) {
        config.setProperty(key, value);
        return true;
    }
    @Override
    public String getString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }
    @Override
    public int getInt(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }
    @Override
    public long getLong(String key, long defaultValue) {
        return config.getLong(key, defaultValue);
    }
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
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
    public Object getModuleProperty(String module, String key) {
        if (modulesConfig.containsKey(module))
            return modulesConfig.get(module).getProperty(key);
        return null;
    }
    @Override
    public boolean setModuleProperty(String module, String key, Object value) {
        if (modulesConfig.containsKey(module)) {
            modulesConfig.get(module).setProperty(key, value);
            return true;
        }
        return false;
    }
    @Override
    public String getModuleString(String module, String key, String defaultValue) {
        if (modulesConfig.containsKey(module))
            return modulesConfig.get(module).getString(key, defaultValue);
        return defaultValue;
    }
    @Override
    public int getModuleInt(String module, String key, int defaultValue) {
        if (modulesConfig.containsKey(module))
            return modulesConfig.get(module).getInt(key, defaultValue);
        return defaultValue;
    }
    @Override
    public long getModuleLong(String module, String key, long defaultValue) {
        if (modulesConfig.containsKey(module))
            return modulesConfig.get(module).getLong(key, defaultValue);
        return defaultValue;
    }
    @Override
    public boolean getModuleBoolean(String module, String key,
                                    boolean defaultValue) {
        if (modulesConfig.containsKey(module))
            return modulesConfig.get(module).getBoolean(key, defaultValue);
        return defaultValue;
    }
    @Override
    public List<String> getModuleList(String module, String key) {
        if (modulesConfig.containsKey(module))
            return modulesConfig.get(module).getList(key);
        return new ArrayList<String>();
    }
    @Override
    public void addModuleConfigurationListener(String module,
                                               ConfigurationListener listener) {
        if (modulesConfig.containsKey(module))
            modulesConfig.get(module).addConfigurationEventListener(listener);
    }


    @Override
    public Object getAddonProperty(String addon, String key) {
        if (addonsConfig.containsKey(addon))
            return addonsConfig.get(addon).getProperty(key);
        return null;
    }
    @Override
    public boolean setAddonProperty(String addon, String key, Object value) {
        if (addonsConfig.containsKey(addon)) {
            addonsConfig.get(addon).setProperty(key, value);
            return true;
        }
        return false;
    }
    @Override
    public String getAddonString(String addon, String key, String defaultValue) {
        if (addonsConfig.containsKey(addon))
            return addonsConfig.get(addon).getString(key, defaultValue);
        return defaultValue;
    }
    @Override
    public long getAddonLong(String addon, String key, long defaultValue) {
        if (addonsConfig.containsKey(addon))
            return addonsConfig.get(addon).getLong(key, defaultValue);
        return defaultValue;
    }
    @Override
    public int getAddonInt(String addon, String key, int defaultValue) {
        if (addonsConfig.containsKey(addon))
            return addonsConfig.get(addon).getInt(key, defaultValue);
        return defaultValue;
    }
    @Override
    public boolean getAddonBoolean(String addon, String key,
                                   boolean defaultValue) {
        if (addonsConfig.containsKey(addon))
            return addonsConfig.get(addon).getBoolean(key, defaultValue);
        return defaultValue;
    }
    @Override
    public List<String> getAddonList(String addon, String key) {
        if (addonsConfig.containsKey(addon))
            return addonsConfig.get(addon).getList(key);
        return new ArrayList<String>();
    }
    @Override
    public void addAddonConfigurationListener(String addon,
                                              ConfigurationListener listener) {
        if (addonsConfig.containsKey(addon))
            addonsConfig.get(addon).addConfigurationEventListener(listener);
    }
    @Override
    public boolean createAddonConfiguration(String addon) {
        String path = this.home + File.separator + DSPACE_ADDONS_CONFIG_PATH + File.separator + addon + DOT_CONFIG;
        try {
            File f = new File(path);
            f.mkdirs();
            f.createNewFile();
            DSpacePropertiesConfiguration config = new DSpacePropertiesConfiguration(f);
            this.addonsConfig.put(addon, config);
            return true;
        } catch (ConfigurationException e) {
            log.error("Error while loading the addon properties from: " + path);
            return false;
        } catch (IOException e) {
            log.error("Error while creating the addon properties file at: " + path);
            return false;
        }

    }
    @Override
    public boolean addProperty(String addon, String key, Object value,
                               String description) {
        if (addonsConfig.containsKey(addon)) {
            addonsConfig.get(addon).addProperty(key, value);
            addonsConfig.get(addon).setPropertyDescription(key, description);
            return true;
        }
        return false;
    }
    @Override
    public String getDescription(String key) {
        return config.getDescription(key);
    }
    @Override
    public String getModuleDescription(String module, String key) {
        if (modulesConfig.containsKey(module))
            return modulesConfig.get(module).getDescription(key);
        return null;
    }
    @Override
    public String getAddonDescription(String addon, String key) {
        if (addonsConfig.containsKey(addon))
            return addonsConfig.get(addon).getDescription(key);
        return null;
    }
}
