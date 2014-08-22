/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.xoai.filter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.dspace.xoai.data.DSpaceItem;

/**
 * Does this item have full-text available?
 * i.e. publicly accessible bitstream in the content/original bundle
 *
 * @author Lyncode Development Team <dspace@lyncode.com>
 */
public class DSpaceAuthorizationFilter extends DSpaceFilter
{
    private static Logger log = LogManager
            .getLogger(DSpaceAuthorizationFilter.class);

    @Override
    public DatabaseFilterResult getWhere(Context context)
    {
        List<Object> params = new ArrayList<Object>();
        return new DatabaseFilterResult("EXISTS (SELECT p.action_id FROM "
                + "resourcepolicy p, " + "bundle2bitstream b, " + "bundle bu, "
                + "item2bundle ib " + "WHERE " + "p.resource_type_id=0 AND "
                + "p.resource_id=b.bitstream_id AND "
                + "p.epersongroup_id=0 AND " + "b.bundle_id=ib.bundle_id AND "
                + "bu.bundle_id=b.bundle_id AND " + "bu.name='ORIGINAL' AND "
                + "ib.item_id=i.item_id)", params);
    }

    @Override
    public boolean isShown(DSpaceItem item)
    {
        try
        {
            Context ctx = super.getContext();
            String handle = DSpaceItem.parseHandle(item.getIdentifier());
            if (handle == null) return false;
            Item dsitem = (Item) HandleManager.resolveToObject(ctx, handle);

            if(AuthorizeManager.authorizeActionBoolean(ctx, dsitem, Constants.READ)) {
                Bundle[] contentBundles = dsitem.getBundles(Constants.CONTENT_BUNDLE_NAME);
                for (Bundle bundle : contentBundles) {
                    if (AuthorizeManager.authorizeActionBoolean(ctx, bundle, Constants.READ)) {
                        Bitstream[] bitstreams = bundle.getBitstreams();
                        for (Bitstream bitstream : bitstreams) {
                            if (AuthorizeManager.authorizeActionBoolean(ctx, bitstream, Constants.READ)) {
                                log.info("There is an accessible bitstream in: " + dsitem.getHandle());
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
        catch (SQLException ex)
        {
            log.error(ex.getMessage());
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
        }
        return false;
    }

    @Override
    public SolrFilterResult getQuery()
    {
        return new SolrFilterResult("item.public:true AND item.publicBitstream:true");
    }

}
