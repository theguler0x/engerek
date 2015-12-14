package com.evolveum.midpoint.web.page.selfregistration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.evolveum.midpoint.common.policy.StringPolicyUtils;
//import com.evolveum.midpoint.model.common.expression.SourceTriple;
import com.evolveum.midpoint.prism.util.PrismContextFactory;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.prism.util.PrismUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.xpath.SourceTreeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.evolveum.midpoint.web.application.PageDescriptor;
import com.evolveum.midpoint.web.page.PageBase;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;
import com.evolveum.midpoint.common.policy.ValuePolicyGenerator;
import com.evolveum.midpoint.model.api.ModelService;
import com.evolveum.midpoint.model.api.PolicyViolationException;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.crypto.EncryptionException;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.match.PolyStringOrigMatchingRule;
import com.evolveum.midpoint.prism.path.ItemPath;


import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.query.AndFilter;
import com.evolveum.midpoint.prism.query.EqualFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.RetrieveOption;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.security.api.Authorization;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.security.api.MidPointPrincipal;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.page.PageBase;
import com.evolveum.midpoint.web.util.OnePageParameterEncoder;
import com.evolveum.midpoint.web.util.WebMiscUtil;
import com.evolveum.midpoint.web.util.WebModelUtils;
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.value.ValueMap;


import com.evolveum.midpoint.model.impl.bulkmodify.*;

import javax.swing.*;


@PageDescriptor(url = "/selfregistration")
public class PageSelfRegistration extends PageBase {


    private static final String ID_SELFREGISTRATIONFORM = "selfregistrationform";

    private static final String ID_USERNAME = "username";
    private static final String ID_EMAIL ="email";
    private static final String ID_FIRSTNAME ="firstname";
    private static final String ID_SURNAME ="surname";
    private static final String ID_PHONENUMBER ="phoneNumber";
    private static final String ID_IMAGE = "captchaImage";
    private static final String ID_PASSWORD = "password";
    private static final String ID_REFRESHBUTTON = "refreshButton";
    private static final String DOT_CLASS = PageSelfRegistration.class.getName() + ".";
    private static final Trace LOGGER = TraceManager.getTrace(PageSelfRegistration.class);
    protected static final String OPERATION_LOAD_RESET_PASSWORD_POLICY = "LOAD PASSWORD RESET POLICY";
    private static final String OPERATION_RESET_PASSWORD = DOT_CLASS + "resetPassword";
    private static final String LOAD_USER = DOT_CLASS + "loadUser";
    public static final String BASE_PATH = "C:/midpoint/resources/";

    private CaptchaImageResource captchaImageResource;

    PageBase page = (PageBase) getPage();

    protected void init() {
        this.getApplication().getRequestCycleSettings().setRenderStrategy(
                IRequestCycleSettings.RenderStrategy.ONE_PASS_RENDER);

    }

    public PageSelfRegistration() {
        setStatelessHint(true);
        setVersioned(false);


        StatelessForm form = new StatelessForm(ID_SELFREGISTRATIONFORM) {



            @Override
            protected void onSubmit() {
                LOGGER.info("Self Registration form is submitted.");
                //captcha validation
                if (!imagePassStr.equals(getPassword()))
                {
                    System.out.println("Captcha password '" + getPassword() + "' is wrong.\n" +
                            "Correct password was: " + imagePassStr);
                    // force redrawing


                    try {
                        getResponse().getOutputStream().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return;
                }//captcha if
                else
                {
                    System.out.println("Success!");
                    // force redrawing


                    //get the string values of the form textfields
                    RequiredTextField<String> username = (RequiredTextField<String>) get(ID_USERNAME);


                    RequiredTextField<String> email = (RequiredTextField<String>)get(ID_EMAIL);
                    RequiredTextField<String> firstname= (RequiredTextField<String>)get(ID_FIRSTNAME);
                    RequiredTextField<String> surname = (RequiredTextField<String>)get(ID_SURNAME);
                    RequiredTextField<String> phoneNumber = (RequiredTextField<String>)get(ID_PHONENUMBER);
                    String administrativeStatus = "enabled";

                    //create a ModifyUserBean object with the entered form values
                    ModifyUserBean tmpUser = new ModifyUserBean(username.getValue().trim(), administrativeStatus, "", email.getValue().trim(),firstname.getValue().trim(),surname.getValue().trim(),phoneNumber.getValue().trim());

                    PostXML runObj = new PostXML("administrator", "5ecr3t");

                    try {
                        //search by the username if it exists
                        String resultUsername = runObj.searchByNamePostMethod(tmpUser.getUserName());
                        String resultEmailAddress = runObj.searchByEmailPostMethod(tmpUser.getEmail().toLowerCase());
                        //if the user does not exist "" string return.
                        if(!(resultUsername.equalsIgnoreCase(""))){
                            error(this.getString("PageSelfRegistration.error.existinguser"));
                            error(this.getString("PageSelfRegistration.error.createuser"));
                            return;

                        }
                        if(!(resultEmailAddress.equalsIgnoreCase(""))){
                            error(this.getString("PageSelfRegistration.error.existingemail"));
                            error(this.getString("PageSelfRegistration.error.createuser"));
                            return;
                        }





                        LOGGER.info("Before creating user via selfregistration page");

                        //create the user as disabled
                        int resultcreate = runObj.createUserForSelfRegistration(tmpUser);

                        //assign Guest Role to user which will be approved by Administrator
                        //String roleOid =runObj.searchRolesByNamePostMethod("Misafir Kullanıcı");
                        //String userOid = runObj.searchByNamePostMethod(tmpUser.getUserName());


                        //int resultaddrole = runObj.addRoleToUserPostMethod(userOid, roleOid);
						 
                        if ( resultcreate == 201 )//
                        {
                            success(this.getString("PageSelfRegistration.succes.createuser"));

                            clearInput();

                        }

                        else if ( resultcreate == 202 )//
						{
                            success(this.getString("PageSelfRegistration.succes.createuserreq"));
                        }else {
							error(this.getString("PageSelfRegistration.error.createuser"));
						}

                        LOGGER.info("After creating user via selfregistration page");


                    } catch (Exception e) {
                        e.printStackTrace();
                    }






                }//captcha else
                captchaImageResource.invalidate();

             //   System.out.println("buralar yeniden yüklendi mi1");
            }//onSubmit



        };//new Statteless form

        RequiredTextField rtfemail;
        RequiredTextField password;
        Image captchaImage;
        org.apache.wicket.markup.html.form.Button refreshButton;
        refreshButton = new Button(ID_REFRESHBUTTON, new Model<String>()){};




        form.add(new RequiredTextField(ID_USERNAME, new Model<String>()).setLabel(new ResourceModel("PageSelfRegistration.username")));
        form.add(rtfemail = new RequiredTextField(ID_EMAIL, new Model<String>()));
        form.add(new RequiredTextField(ID_FIRSTNAME, new Model<String>()).setLabel(new ResourceModel("PageSelfRegistration.firstname")));
        form.add(new RequiredTextField(ID_SURNAME, new Model<String>()).setLabel(new ResourceModel("PageSelfRegistration.surname")));
        form.add(new RequiredTextField(ID_PHONENUMBER, new Model<String>()).setLabel(new ResourceModel("PageSelfRegistration.phoneNumber")));
        form.add(refreshButton);

        captchaImageResource = new CaptchaImageResource(imagePassStr);
        //System.out.println("buralar yeniden yüklendi mi2");
        captchaImage = new NonCachingImage(ID_IMAGE, captchaImageResource);

        form.add(captchaImage);
        form.add(password = new RequiredTextField<String>(ID_PASSWORD, new PropertyModel<String>(properties,
                ID_PASSWORD)) {
            @Override
            protected final void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                // clear the field after each render
                tag.put("value", "");
            }
        });
        //System.out.println("buralar yeniden yüklendi mi3");

        rtfemail.add(EmailAddressValidator.getInstance());
        rtfemail.setLabel(new ResourceModel("PageSelfRegistration.email"));
        password.setLabel(new ResourceModel("PageSelfRegistration.password"));


        add(form);

        //PageSelfRegistration.createPassword();


    }



    //resets the user password and returns the new password
    /*private static String createPassword(){
        System.out.println("in create password");
        String pathname = BASE_PATH + "value-policy-random-pin.xml";
        File file = new File(pathname);
        System.out.println("after file open");
        LOGGER.info("Positive testing {}: {}", "testValueGenerateRandomPin", "value-policy-random-pin.xml");

        ValuePolicyType pp = null;
        System.out.println("before try-catch");
        try {
            System.out.println("before pp");
            pp = (ValuePolicyType) PrismTestUtil.parseObject(file).asObjectable();
            System.out.println("after pp");
        } catch (SchemaException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OperationResult op = new OperationResult("testValueGenerate");
        System.out.println("after operation result");


        String psswd;
        System.out.println("before password generator");
        psswd = ValuePolicyGenerator.generate(pp.getStringPolicy(), 10, true, op);
        LOGGER.info("Generated password:" + psswd);
        System.out.println("Generated password: " + psswd);

        return psswd;

    }*/

    //private long serialVersionUID = Long.getLong(this.getSession().getId());

    private static int randomInt(int min, int max)
    {
        return (int)(Math.random() * (max - min) + min);
    }

    private static String randomString(int min, int max)
    {
        int num = randomInt(min, max);
        byte b[] = new byte[num];
        for (int i = 0; i < num; i++)
            b[i] = (byte)randomInt('a', 'z');
        return new String(b);

    }

    private static String randomHayvanString()
    {
        ArrayList<String> hayvanlar = new ArrayList<String>(65);
        hayvanlar.add("aslan");
        hayvanlar.add("akrep");
        hayvanlar.add("manda");
        hayvanlar.add("ceylan");
        hayvanlar.add("civciv");
        hayvanlar.add("dana");
        hayvanlar.add("sansar");
        hayvanlar.add("deve");
        hayvanlar.add("domuz");
        hayvanlar.add("turna");
        hayvanlar.add("fil");
        hayvanlar.add("horoz");
        hayvanlar.add("inek");
        hayvanlar.add("kelebek");
        hayvanlar.add("kertenkele");
        hayvanlar.add("geyik");
        hayvanlar.add("enik");
        hayvanlar.add("kartal");
        hayvanlar.add("kaz");
        hayvanlar.add("koyun");
        hayvanlar.add("kurt");
        hayvanlar.add("kuzgun");
        hayvanlar.add("kuzu");
        hayvanlar.add("leylek");
        hayvanlar.add("maymun");
        hayvanlar.add("kaplan");
        hayvanlar.add("kedi");
        hayvanlar.add("fare");
        hayvanlar.add("sincap");
        hayvanlar.add("solucan");
        hayvanlar.add("teke");
        hayvanlar.add("tavuk");
        hayvanlar.add("tilki");
        hayvanlar.add("yarasa");
        hayvanlar.add("puhu");
        hayvanlar.add("pars");
        hayvanlar.add("pardus");
        hayvanlar.add("engerek");
        hayvanlar.add("akbaba");
        hayvanlar.add("babun");
        hayvanlar.add("flamingo");
        hayvanlar.add("gelincik");
        hayvanlar.add("hindi");
        hayvanlar.add("kanarya");
        hayvanlar.add("koala");
        hayvanlar.add("kanguru");
        hayvanlar.add("kumru");
        hayvanlar.add("leopar");
        hayvanlar.add("levrek");
        hayvanlar.add("mezgit");
        hayvanlar.add("mors");
        hayvanlar.add("orangutan");
        hayvanlar.add("orkinos");
        hayvanlar.add("palamut");
        hayvanlar.add("puma");
        hayvanlar.add("porsuk");
        hayvanlar.add("pelikan");
        hayvanlar.add("salyangoz");
        hayvanlar.add("samur");
        hayvanlar.add("sardalya");
        hayvanlar.add("timsah");
        hayvanlar.add("atmaca");
        hayvanlar.add("zargana");
        hayvanlar.add("zebra");
        hayvanlar.add("panda");

        int random = (int)(Math.random()*1000);
        random = random % hayvanlar.size();

        return new String(hayvanlar.get(random));

    }

    /** Random captcha password to match against. */
    private final String imagePassStr = randomHayvanString();

    private final ValueMap properties = new ValueMap();


    private String getPassword()
    {
        return properties.getString("password");
    }







}
