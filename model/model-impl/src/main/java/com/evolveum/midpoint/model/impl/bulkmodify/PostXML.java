package com.evolveum.midpoint.model.impl.bulkmodify;
/*
2	 * $HeadURL$
3	 * $Revision$
4	 * $Date$
5	 * ====================================================================
6	 *
7	 *  Licensed to the Apache Software Foundation (ASF) under one or more
8	 *  contributor license agreements.  See the NOTICE file distributed with
9	 *  this work for additional information regarding copyright ownership.
10	 *  The ASF licenses this file to You under the Apache License, Version 2.0
11	 *  (the "License"); you may not use this file except in compliance with
12	 *  the License.  You may obtain a copy of the License at
13	 *
14	 *      http://www.apache.org/licenses/LICENSE-2.0
15	 *
16	 *  Unless required by applicable law or agreed to in writing, software
17	 *  distributed under the License is distributed on an "AS IS" BASIS,
18	 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
19	 *  See the License for the specific language governing permissions and
20	 *  limitations under the License.
21	 * ====================================================================
22	 *
23	 * This software consists of voluntary contributions made by many
24	 * individuals on behalf of the Apache Software Foundation.  For more
25	 * information on the Apache Software Foundation, please see
26	 * <http://www.apache.org/>.
27	 *
28	 * [Additional notices, if required by prior licensing conditions]
29	 *
30	 */
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
	

	public class PostXML {
		
		private String username;
		private String password;
		
		private static final String XML_TEMPLATE_SEARCH = 	"<query xmlns:c=\"http://midpoint.evolveum.com/xml/ns/public/common/common-3\""+
												"	xmlns=\"http://prism.evolveum.com/xml/ns/public/query-3\">"+
												"<filter>"+
													"<substring>"+
												    " 	<path>c:name</path>"+ //
													"	<value></value>"+ //place search value here
													"</substring>"+
													"</filter>"+
												"</query>";
		
		private static final String XML_TEMPLATE_SEARCH_IGNORECASE = 	"<query xmlns:c=\"http://midpoint.evolveum.com/xml/ns/public/common/common-3\""+
												"	xmlns=\"http://prism.evolveum.com/xml/ns/public/query-3\">"+
												"<filter>"+
													"<substring>"+
													" <matching>normIgnoreCase</matching>"+
												    " 	<path>c:name</path>"+ //
													"	<value></value>"+ //place search value here
													"</substring>"+
													"</filter>"+
												"</query>";
		
		
		private static final String XML_TEMPLATE_MODIFY = 	"<objectModification "+
                												"	xmlns='http://midpoint.evolveum.com/xml/ns/public/common/api-types-3'"+
                												"	xmlns:c='http://midpoint.evolveum.com/xml/ns/public/common/common-3'"+
                												"	xmlns:t=\"http://prism.evolveum.com/xml/ns/public/types-3\">"+
                												"	<oid></oid>"+ //place id here              
                												"	<itemDelta>"+
                												"	<t:modificationType>replace</t:modificationType>"+
                												"	<t:path>c:activation/c:administrativeStatus</t:path>"+
                												"	<t:value></t:value>"+ //place disabled or enabled here
                												"	</itemDelta>"+
                												" </objectModification>";
		private static final String XML_TEMPLATE_ADD_ROLE_TO_USER = 	"<objectModification "+
																"	xmlns='http://midpoint.evolveum.com/xml/ns/public/common/api-types-3'"+
																"	xmlns:c='http://midpoint.evolveum.com/xml/ns/public/common/common-3'"+
																"	xmlns:t=\"http://prism.evolveum.com/xml/ns/public/types-3\">"+
																"	<oid></oid>"+ //place oid of the user here              
																"	<itemDelta>"+
																"	<t:modificationType>add</t:modificationType>"+
																"	<t:path>assignment</t:path>"+
																"	<t:value> <c:targetRef "+ 
																"oid=\"\""+	//place oid of the role here. change oid="" with oid="KEY"
																" type=\"c:RoleType\"/>"+
																"</t:value>"+ 
																"	</itemDelta>"+
																" </objectModification>";

		
	
	    /**
54	     *
55	     * Usage:
56	     *          java PostXML http://mywebserver:80/ c:\foo.xml
57	     *
58	     *  @param args command line arguments
59	     *                 Argument 0 is a URL to a web server
60	     *                 Argument 1 is a local filename
61	     *
62	     */
	    
	    
		//seraches users by name and return oid of user if exists
	    /**
	     * This method takes
	     * 
	     * @param idmUserName
	     * @return user object's oid if the given username exists
	     * @throws Exception
	     * 
	     * This method
	     */
	    public String searchByNamePostMethod(String idmUserName) throws Exception{
	    	String returnOid = "";
	    	
	    	// Get target URL
	        String strURL = "http://localhost:8080/midpoint/ws/rest/users/search";
	               
	        
	        // Prepare HTTP post
	        PostMethod post = new PostMethod(strURL);
	        
	      //AUTHENTICATION BY GURER
	        //String username = "administrator";
	        //String password = "5ecr3t";
	        String userPass = this.getUsername() + ":" + this.getPassword();
		    String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userPass.getBytes("UTF-8"));
		    post.addRequestHeader("Authorization", basicAuth);
		  
		    //construct searching string. place "name" attribute into <values> tags.
		    String sendingXml = XML_TEMPLATE_SEARCH;
		     
		    sendingXml = sendingXml.replace("<value></value>", "<value>"+idmUserName+"</value>");
		    
		     
		     
	        RequestEntity userSearchEntity = new StringRequestEntity(sendingXml, "text/xml","UTF-8");
	        post.setRequestEntity(userSearchEntity);
	        // Get HTTP client
	        HttpClient httpclient = new HttpClient();
	        // Execute request
	        try {
	            int result = httpclient.executeMethod(post);
	            // Display status code
	            //System.out.println("Response status code: " + result);
	            // Display response
	            //System.out.println("Response body: ");
	           // System.out.println(post.getResponseBodyAsString());
	           String sbf = new String(post.getResponseBodyAsString());
	           //System.out.println(sbf);
	           
	           //find oid
	            if(sbf.contains("oid")){
	            	int begin = sbf.indexOf("oid");
	            	returnOid = (sbf.substring(begin+5, begin+41));
	            }
	            
	        } finally {
	            // Release current connection to the connection pool once you are done
	            post.releaseConnection();
	        }
	    	
	        return returnOid;
	    }
	    
	  //searches roles by name and return role oid of role if exists
	   
	    public String searchRolesByNamePostMethod(String roleName) throws Exception{
	    	String returnOid = "";
	    	
	    	// Get target URL
	        String strURL = "http://localhost:8080/midpoint/ws/rest/roles/search";
	               
	        
	        // Prepare HTTP post
	        PostMethod post = new PostMethod(strURL);
	        
	      //AUTHENTICATION BY GURER
	        //String username = "administrator";
	        //String password = "5ecr3t";
	        String userPass = this.getUsername() + ":" + this.getPassword();
		    String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userPass.getBytes("UTF-8"));
		    post.addRequestHeader("Authorization", basicAuth);
		  
		    //construct searching string. place "name" attribute into <values> tags.
		    String sendingXml = XML_TEMPLATE_SEARCH_IGNORECASE;
		     
		    sendingXml = sendingXml.replace("<value></value>", "<value>"+roleName+"</value>");
		    
		     
		     
	        RequestEntity userSearchEntity = new StringRequestEntity(sendingXml, "text/xml","UTF-8");
	        post.setRequestEntity(userSearchEntity);
	        // Get HTTP client
	        HttpClient httpclient = new HttpClient();
	        // Execute request
	        try {
	            int result = httpclient.executeMethod(post);
	            // Display status code
	            //System.out.println("Response status code: " + result);
	            // Display response
	            //System.out.println("Response body: ");
	           // System.out.println(post.getResponseBodyAsString());
	           String sbf = new String(post.getResponseBodyAsString());
	           //System.out.println(sbf);
	           
	           //find oid
	            if(sbf.contains("oid")){
	            	int begin = sbf.indexOf("oid");
	            	returnOid = (sbf.substring(begin+5, begin+41));
	            }
	            
	        } finally {
	            // Release current connection to the connection pool once you are done
	            post.releaseConnection();
	        }
	    	
	        return returnOid;
	    }
	    
	    //seraches by oid and replaces administrative status by given value
	    public int modifyByOidPostMethod(String oid, String administrativeStatus) throws Exception{
	    	String returnString = "";
	    	int result=0;
	    	// Get target URL
	        String strURLBase = "http://localhost:8080/midpoint/ws/rest/users/";
	        String strURL = strURLBase + oid;       
	        
	        // Prepare HTTP post
	        PostMethod post = new PostMethod(strURL);
	        
	      //AUTHENTICATION BY GURER
	        //String username = "administrator";
	        //String password = "5ecr3t";
	        String userPass = this.getUsername() + ":" + this.getPassword();
		    String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userPass.getBytes("UTF-8"));
		    post.addRequestHeader("Authorization", basicAuth);
		  
		    //construct searching string. place "name" attribute into <values> tags.
		    String sendingXml = XML_TEMPLATE_MODIFY;
		     
		    sendingXml = sendingXml.replace("<oid></oid>", "<oid>"+oid+"</oid>");
		    sendingXml = sendingXml.replace("<t:value></t:value>", "<t:value>"+administrativeStatus+"</t:value>"); 
		     
		     
		     
	        RequestEntity userSearchEntity = new StringRequestEntity(sendingXml, "text/xml","UTF-8");
	        post.setRequestEntity(userSearchEntity);
	        // Get HTTP client
	        HttpClient httpclient = new HttpClient();
	        // Execute request
	        try {
	            result = httpclient.executeMethod(post);
	            // Display status code
	            //System.out.println("Response status code: " + result);
	            // Display response
	            //System.out.println("Response body: ");
	           // System.out.println(post.getResponseBodyAsString());
	           //String sbf = new String(post.getResponseBodyAsString());
	           //System.out.println(sbf);
	           
	           
	            
	        } finally {
	            // Release current connection to the connection pool once you are done
	            post.releaseConnection();
	        }
	    	
	        return result;
	    }
	    
	  //seraches by oid and adds role to given value
	    public int addRoleToUserPostMethod(String userOid, String roleOid) throws Exception{
	    	String returnString = "";
	    	int result=0;
	    	// Get target URL
	        String strURLBase = "http://localhost:8080/midpoint/ws/rest/users/";
	        String strURL = strURLBase + userOid;       
	        
	        // Prepare HTTP post
	        PostMethod post = new PostMethod(strURL);
	        
	      //AUTHENTICATION BY GURER
	        //String username = "administrator";
	        //String password = "5ecr3t";
	        String userPass = this.getUsername() + ":" + this.getPassword();
		    String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userPass.getBytes("UTF-8"));
		    post.addRequestHeader("Authorization", basicAuth);
		  
		    //construct searching string. place "name" attribute into <values> tags.
		    String sendingXml = XML_TEMPLATE_ADD_ROLE_TO_USER;
		     
		    sendingXml = sendingXml.replace("<oid></oid>", "<oid>"+userOid+"</oid>");
		    sendingXml = sendingXml.replace("oid=\"\"", "oid=\""+roleOid+"\""); 
		     
		     
		     
	        RequestEntity userSearchEntity = new StringRequestEntity(sendingXml, "text/xml","UTF-8");
	        post.setRequestEntity(userSearchEntity);
	        // Get HTTP client
	        HttpClient httpclient = new HttpClient();
	        // Execute request
	        try {
	            result = httpclient.executeMethod(post);
	            // Display status code
	            //System.out.println("Response status code: " + result);
	            // Display response
	            //System.out.println("Response body: ");
	           // System.out.println(post.getResponseBodyAsString());
	           //String sbf = new String(post.getResponseBodyAsString());
	           //System.out.println(sbf);
	           
	           
	            
	        } finally {
	            // Release current connection to the connection pool once you are done
	            post.releaseConnection();
	        }
	    	
	        return result;
	    }
	    
	    
	    //contructor for general purposes. username and password are must
	    public PostXML(String username, String password){
	    	this.username = username;
	    	this.password = password;
	    }

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	    
	    
	    
	}
