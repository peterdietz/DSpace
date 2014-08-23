package org.dspace.app.xmlui.aspect.administrative.registries;

import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.core.LicenseManager;

import java.sql.SQLException;

/**
 * XMLUI Form to view/edit the default license
 * @author peterdietz 
 */
public class SystemLicenseMain extends AbstractDSpaceTransformer {
    private static final Logger log = Logger.getLogger(SystemLicenseMain.class);

    public static final String systemLicensePath = "/admin/system-license";

    public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        pageMeta.addMetadata("title").addContent("System License");
        pageMeta.addTrailLink(contextPath + "/", "DSpace Home");
        pageMeta.addTrailLink(contextPath + systemLicensePath, "System License");
    }

    public void addBody(Body body) throws WingException, SQLException {
        log.info("SystemLicenseMain.addBody");
        Division content = body.addDivision("content");
        content.setHead("System License");
        content.addPara("The System License is the default license that applies to all submitted Items. You can also set a license per Collection through Edit Collection - License");
        content.addPara(LicenseManager.getDefaultSubmissionLicense());

        //Edit portion
        Division main = body.addInteractiveDivision("system-license-edit",contextPath+ SystemLicenseMain.systemLicensePath, Division.METHOD_POST,"primary administrative system-license");

        List form = main.addList("system-license-edit",List.TYPE_FORM);
        form.setHead("Edit the System License");

        TextArea message = form.addItem().addTextArea("system-license");
        message.setValue(LicenseManager.getDefaultSubmissionLicense());

        Item actions = form.addItem();
        actions.addButton("update_license").setValue("Update License");

    }
}
