/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.services;

import java.util.List;

import org.apache.commons.configuration.event.ConfigurationListener;

/**
 * Dynamic Configuration Service API
 *
 * @author DSpace @ Lyncode
 * @author Peter Dietz @ Longsight
 */
public interface DynamicConfigurationService {
    // Main Configuration File API
    public Object getProperty (String key);
    public String getDescription (String key);
    public boolean setProperty (String key, Object value);
    public String getString (String key, String defaultValue);
    public int getInt (String key, int defaultValue);
    public long getLong (String key, long defaultValue);
    public boolean getBoolean (String key, boolean defaultValue);
    public List<String> getList (String key);
    public void addConfigurationListener (ConfigurationListener listener);

    // Modules Configuration API
    public Object getModuleProperty (String module, String key);
    public String getModuleDescription (String module, String key);
    public boolean setModuleProperty (String module, String key, Object value);
    public String getModuleString (String module, String key, String defaultValue);
    public int getModuleInt (String module, String key, int defaultValue);
    public long getModuleLong (String module, String key, long defaultValue);
    public boolean getModuleBoolean (String module, String key, boolean defaultValue);
    public List<String> getModuleList (String module, String key);
    public void addModuleConfigurationListener (String module, ConfigurationListener listener);

    // Addon Configuration API
    public Object getAddonProperty (String addon, String key);
    public String getAddonDescription (String addon, String key);
    public boolean setAddonProperty (String addon, String key, Object value);
    public String getAddonString (String addon, String key, String defaultValue);
    public long getAddonLong (String addon, String key, long defaultValue);
    public int getAddonInt (String addon, String key, int defaultValue);
    public boolean getAddonBoolean (String addon, String key, boolean defaultValue);
    public List<String> getAddonList (String addon, String key);
    public void addAddonConfigurationListener (String addon, ConfigurationListener listener);
    public boolean createAddonConfiguration (String addon);
    public boolean addProperty (String addon, String key, Object value, String description);
}
