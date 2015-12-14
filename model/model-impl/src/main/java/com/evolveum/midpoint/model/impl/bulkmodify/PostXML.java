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
	
	/**
40	 *
41	 * This is a sample application that demonstrates
42	 * how to use the Jakarta HttpClient API.
43	 *
44	 * This application sends an XML document
45	 * to a remote web server using HTTP POST
46	 *
47	 * @author Sean C. Sullivan
48	 * @author Ortwin Glueck
49	 * @author Oleg Kalnichevski
50	 */
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

		private static final String XML_TEMPLATE_SEARCH_BYEMAIL = 	"<query xmlns:c=\"http://midpoint.evolveum.com/xml/ns/public/common/common-3\""+
				"	xmlns=\"http://prism.evolveum.com/xml/ns/public/query-3\">"+
				"<filter>"+
				"<equal>"+
				" 	<path>c:emailAddress</path>"+ //
				"	<value></value>"+ //place searching emailAddress value here
				"</equal>"+
				"</filter>"+
				"</query>";

		private static final String XML_TEMPLATE_CREATE_USER_SELFREGISTRATION = "<user xmlns='http://midpoint.evolveum.com/xml/ns/public/common/common-3'>" +
				"<name></name>" + //username
				"<givenName></givenName>" + //firstname
				"<familyName></familyName>" + //surname
				"<emailAddress></emailAddress>" + //email address
				"<telephoneNumber></telephoneNumber>" + //phone number
				"<employeeType>selfService</employeeType>" + 
				"<activation>" +
				"<administrativeStatus></administrativeStatus>" + //administrative status
				"</activation>" +
				"</user>";

		
	
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
	    /*public static void main(String[] args) throws Exception {
	        /*if (args.length != 2) {
	            System.out.println("Usage: java -classpath <classpath> [-Dorg.apache.commons.logging.simplelog.defaultlog=<loglevel>] PostXML <url> <filename>]");
	            System.out.println("<classpath> - must contain the commons-httpclient.jar and commons-logging.jar");
	            System.out.println("<loglevel> - one of error, warn, info, debug, trace");
	            System.out.println("<url> - the URL to post the file to");
	            System.out.println("<filename> - file to post to the URL");
	            System.out.println();
	            System.exit(1);
	        }
	        
	        
	        PostXML runner = new PostXML("administrator", "5ecr3t");
	        String idmUserName = "gurer.onder";
	        String oid = runner.searchByNamePostMethod(idmUserName);
	        System.out.println("Name:"+idmUserName);
	        System.out.println("oid:"+oid);
	        runner.modifyByOidPostMethod(oid, "disabled");
	    }*/
	    
		//seraches users by name and return oid of user if exists
	    /**
	     * This method takes
	     * 
	     * @param idmUserName
	     * @return user object's oid if the given username exists. Empty String if the user does not exist.
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
	        
	      	//AUTHENTICATION
	        String userPass = this.getUsername() + ":" + this.getPassword();
		    String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userPass.getBytes("UTF-8"));
		    post.addRequestHeader("Authorization", basicAuth);
		  
		    //construct searching string. place "name" attribute into <values> tags.
		    String sendingXml = XML_TEMPLATE_SEARCH_IGNORECASE;
		     
		    sendingXml = sendingXml.replace("<value></value>", "<value>"+idmUserName+"</value>");
		    
		     
		     
	        RequestEntity userSearchEntity = new StringRequestEntity(sendingXml, "application/xml","UTF-8");
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

		/**
		 * This method takes emailAddress as a search value and returns user's oid with the given emailAddress
		 *
		 * @param emailAddress
		 * @return user object's oid if the given username exists. Empty String if the emailAddress does not exist.
		 * @throws Exception
		 *
		 * This method
		 */
		public String searchByEmailPostMethod(String emailAddress) throws Exception{
			String returnOid = "";

			// Get target URL
			String strURL = "http://localhost:8080/midpoint/ws/rest/users/search";


			// Prepare HTTP post
			PostMethod post = new PostMethod(strURL);

			//AUTHENTICATION
			String userPass = this.getUsername() + ":" + this.getPassword();
			String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userPass.getBytes("UTF-8"));
			post.addRequestHeader("Authorization", basicAuth);

			//construct searching string. place "name" attribute into <values> tags.
			String sendingXml = XML_TEMPLATE_SEARCH_BYEMAIL;

			sendingXml = sendingXml.replace("<value></value>", "<value>"+emailAddress+"</value>");



			RequestEntity userSearchEntity = new StringRequestEntity(sendingXml, "application/xml","UTF-8");
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
		    
		     
		     
	        RequestEntity userSearchEntity = new StringRequestEntity(sendingXml, "application/xml","UTF-8");
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
	        String strURLBase = "http://localhost:8080/midpoint/ws/rest/users";
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
		     
		     
		     
	        RequestEntity userSearchEntity = new StringRequestEntity(sendingXml, "application/xml","UTF-8");
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
	    
	  //searches by oid and adds role to given value
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
		     
		     
		     
	        RequestEntity userSearchEntity = new StringRequestEntity(sendingXml, "application/xml","UTF-8");
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


		public int createUserForSelfRegistration(ModifyUserBean user) throws Exception{
			String returnString = "";
			int result=0;
			// Get target URL
			String strURLBase = "http://localhost:8080/midpoint/ws/rest/users";
			String strURL = strURLBase;

			// Prepare HTTP post
			PostMethod post = new PostMethod(strURL);

			//AUTHENTICATION
			String userPass = this.getUsername() + ":" + this.getPassword();
			String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userPass.getBytes("UTF-8"));
			post.addRequestHeader("Authorization", basicAuth);

			//construct searching string.
			String sendingXml = XML_TEMPLATE_CREATE_USER_SELFREGISTRATION;

			sendingXml = sendingXml.replace("<name></name>", "<name>"+user.getUserName()+"</name>");
			sendingXml = sendingXml.replace("<givenName></givenName>", "<givenName>"+user.getFirstname()+"</givenName>");
			sendingXml = sendingXml.replace("<familyName></familyName>", "<familyName>"+user.getSurname()+"</familyName>");
			sendingXml = sendingXml.replace("<emailAddress></emailAddress>", "<emailAddress>"+user.getEmail()+"</emailAddress>");
			sendingXml = sendingXml.replace("<telephoneNumber></telephoneNumber>", "<telephoneNumber>"+user.getPhoneNumber()+"</telephoneNumber>");
			sendingXml = sendingXml.replace("<administrativeStatus></administrativeStatus>", "<administrativeStatus>"+user.getStatus()+"</administrativeStatus>");




			RequestEntity userSearchEntity = new StringRequestEntity(sendingXml, "application/xml","UTF-8");
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
		/**
		 * Constructor for PostXML class which needs username and password for all operations
		 *
		 * @param username Administrative user's username who have rights to make such opertions
		 * @param password Administrative user's password who have rights to make such opertions
		 * @return user object's oid if the given username exists. Empty String if the user does not exist.
		 * @throws Exception
		 *
		 *
		 */
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
