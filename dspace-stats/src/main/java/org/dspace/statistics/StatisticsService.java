/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics;

import org.dspace.content.DSpaceObject;
import org.dspace.eperson.EPerson;
import org.elasticsearch.client.Client;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: peterdietz
 * Date: 8/23/12
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface StatisticsService {
    public static enum ClientType {
        NODE, LOCAL, TRANSPORT
    }

    void post(DSpaceObject dspaceObject, HttpServletRequest request, EPerson currentUser);
    
    Client getClient();
    
    Client getClient(ClientType clientType);
}
