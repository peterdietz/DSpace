package org.dspace.app.xmlui.aspect.administrative.registries;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.core.LicenseManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Action that performs logic to save the updated system-license
 * @author peterdietz
 */
public class SystemLicenseAction extends AbstractAction {
    private static final Logger log = Logger.getLogger(SystemLicenseAction.class);
    @Override
    public Map act(Redirector redirector, SourceResolver sourceResolver, Map objectModel, String source, Parameters parameters) throws Exception {
        log.info("SystemLicenseAction.act");
        Request request = ObjectModelHelper.getRequest(objectModel);

        if (request.getParameter("update_license") != null)
        {
            if(request.getParameter("system-license") != null) {
                Context context = ContextUtil.obtainContext(objectModel);
                LicenseManager.writeLicenseFile(I18nUtil.getDefaultLicense(context), request.getParameter("system-license"));
                context.commit();
            }

            return new HashMap();
        }
        return null;
    }
}
