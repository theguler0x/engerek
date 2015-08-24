package com.evolveum.midpoint.web.page.admin.users.diff;

import org.w3c.dom.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by arda.nural on 6/26/2015.
 */
public class XMLParseHelper implements Serializable{

	private static final long serialVersionUID = 1L;
	private static String PARENT_ORG_REF = "parentOrgRef";
    //private String[] tags = {"name", "description", "displayName", "identifier", "parentOrgRef","detsisStatus"};

    public NodeList getNodeList(Document doc, String tagName) {
        NodeList nl = doc.getElementsByTagName(tagName);
        return (nl != null) ? nl : null;
    }

    public List<Element> getNodes(NodeList nodeList) {
        List<Element> orgNodes = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {

            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                orgNodes.add((Element) nodeList.item(i));

            }
        }

        return orgNodes;
    }


    //kullanıyorum
    public List<DTVTOrg> getOrgValues(List<Element> orgNodes) {

        List<DTVTOrg> dtvtOrgList = new ArrayList<DTVTOrg>();
        //Set<DTVTOrg> dtvtOrgList1 = new HashSet<DTVTOrg>();


        int i = 0;
        while (i < orgNodes.size()) {
            DTVTOrg dtv = new DTVTOrg();
            NodeList list = orgNodes.get(i).getChildNodes();

           // System.out.println(" ");
           // System.out.println("--> " + orgNodes.get(i).getTagName() +"# " + i + ", size: " + list.getLength());

                    if (orgNodes.get(i).getElementsByTagName("name").getLength() > 0)
                        dtv.setName(orgNodes.get(i).getElementsByTagName("name").item(0).getTextContent());

                    if (orgNodes.get(i).getElementsByTagName("displayName").getLength() > 0)
                        dtv.setDisplayName(orgNodes.get(i).getElementsByTagName("displayName").item(0).getTextContent());

                    if (orgNodes.get(i).getElementsByTagName("description").getLength() > 0)
                        dtv.setDescription(orgNodes.get(i).getElementsByTagName("description").item(0).getTextContent());

                    if (orgNodes.get(i).getElementsByTagName("identifier").getLength() > 0)
                        dtv.setIdentifier(orgNodes.get(i).getElementsByTagName("identifier").item(0).getTextContent());

                    if (orgNodes.get(i).getElementsByTagName("orgType").getLength() > 0)
                        dtv.setOrgType(orgNodes.get(i).getElementsByTagName("orgType").item(0).getTextContent());

                    if (orgNodes.get(i).getElementsByTagName("locality").getLength() > 0){
                        dtv.setLocality(orgNodes.get(i).getElementsByTagName("locality").item(0).getTextContent());
                        //System.out.println(orgNodes.get(i).getElementsByTagName("locality").item(0).getTextContent());
                    }
                    if (orgNodes.get(i).getLocalName().equals("detsisStatus")){
                    	dtv.setDetsisStatus(orgNodes.get(i).getLocalName().toString());
                        //System.out.println(orgNodes.get(i).getElementsByTagName("locality").item(0).getTextContent());
                    }

                    if (orgNodes.get(i).getElementsByTagName("parentOrgRef").getLength() > 0){
                        for (int k = 0; k <orgNodes.get(i).getElementsByTagName("parentOrgRef").getLength() ; k++) {
                          //  System.out.println(orgNodes.get(i).getElementsByTagName("parentOrgRef").item(k).getAttributes().item(0).getTextContent());
                            dtv.setParentOrgRef(orgNodes.get(i).getElementsByTagName("parentOrgRef").item(k).getAttributes().item(0).getTextContent());
                       }
                    }
                    
                    if(orgNodes.get(i).getElementsByTagName("extension").getLength()>0){
                        dtv.setDetsisNo(orgNodes.get(i).getElementsByTagName("extension").item(0).getChildNodes().item(1).getTextContent()); //detsisNo
                        dtv.setDetsisStatus(orgNodes.get(i).getElementsByTagName("extension").item(0).getChildNodes().item(3).getTextContent()); //detsisStatus
                        dtv.setOrganisationStatus(orgNodes.get(i).getElementsByTagName("extension").item(0).getChildNodes().item(5).getTextContent()); //organisationStatus

                    }
                    
                    if(orgNodes.get(i).getElementsByTagName("activation").getLength()>0){
                        dtv.setAdministrativeStatus(orgNodes.get(i).getElementsByTagName("activation").item(0).getChildNodes().item(1).getTextContent());
                    }



                    // System.out.println(list.item(i).getNodeName() + " : " + list.item(i).getTextContent());



            dtvtOrgList.add(dtv);
            i++;
     /*           if (list.getLength() > 0) {

                    String attr = orgNodes.get(i).getElementsByTagName(tagName).item(0).getTextContent(); //node value
                    int parentSize = orgNodes.get(i).getElementsByTagName(tagName).getLength();           //count parentOrgRef attribute
                    String tag = orgNodes.get(i).getElementsByTagName(tagName).item(0).getNodeName();     //element name


                    for (int j = 0; j < parentSize; j++) {
                        if (tag.equalsIgnoreCase(PARENT_ORG_REF)) {
                            String parenOrgRefOID = orgNodes.get(i).getElementsByTagName(tagName).item(j).getAttributes().item(0).getNodeValue();
                            System.out.println("org no " + i + " parentOrgRefOID:" + j + " " + parenOrgRefOID);

                        }
                    }
                    if (!orgNodes.get(i).getElementsByTagName(tagName).item(0).getNodeName().equalsIgnoreCase(PARENT_ORG_REF)) {
                        System.out.println(tagName + i + ": " + attr);


                        if ("name".equalsIgnoreCase(tagName)) {
                            dtv.setName(attr);
                        } else if ("description".equalsIgnoreCase(tagName)) {
                            dtv.setDescription(attr);
                        } else if ("displayName".equalsIgnoreCase(tagName)) {
                            dtv.setDisplayName(attr);
                        } else if ("identifier".equalsIgnoreCase(tagName)) {
                            dtv.setIdentifier(attr);
                        }
                    }


                }
                dtvtOrgList.add(dtv);
                */
        }


        return dtvtOrgList;
    }

   

}