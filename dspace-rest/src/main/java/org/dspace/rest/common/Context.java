/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.rest.common;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class Context {
    private static Logger log = Logger.getLogger(Context.class);
	
	private int limit;
	private int offset;
	private long total_count;
	private String query;
	private String query_date;
	private ArrayList<String> error;
	
	public ArrayList<String> getError() {
		return error;
	}

	public void setError(ArrayList<String> error) {
		this.error = error;
	}
	
	public void addError(String err) {
		if(error == null){
			error = new ArrayList<String>();
		}
		error.add(err);
	}
	
	private static String dateFormat="yyyy-MM-dd'T'HH:mm:ss";
	
	private static SimpleDateFormat sdf;
	
	static{
		sdf=new SimpleDateFormat(dateFormat);
	}
	
	public Context(){
		query_date = sdf.format(new Date());
	}

    //Constructor with all needed data
    public Context(HttpServletRequest request, int total_count, int limit, int offset) {
        new Context();

        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            setQuery(requestURL.toString());
        } else {
            setQuery(requestURL.append('?').append(queryString).toString());
        }
        setLimit(limit);
        setOffset(offset);
        setTotal_count(total_count);
    }
	
	public long getTotal_count() {
		return total_count;
	}
	public void setTotal_count(long total_count) {
		this.total_count = total_count;
	}
	
	@XmlElement
	public String getQuery_date() {
		return query_date;
	}
	public void setQuery_date(Date query_date) {
		this.query_date = sdf.format(query_date);
	}
	
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}

}
