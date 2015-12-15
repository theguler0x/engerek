/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.page.login;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.application.PageDescriptor;
import com.evolveum.midpoint.web.page.PageBase;
import com.evolveum.midpoint.web.page.admin.home.PageDashboard;
import com.evolveum.midpoint.web.page.forgetpassword.PageForgetPassword;
import com.evolveum.midpoint.web.page.self.PageSelfDashboard;
import com.evolveum.midpoint.web.page.selfregistration.PageSelfRegistration;
import com.evolveum.midpoint.web.security.MidPointApplication;
import com.evolveum.midpoint.web.security.MidPointAuthWebSession;
import com.evolveum.midpoint.web.security.SecurityUtils;
import com.evolveum.midpoint.web.util.WebMiscUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialsPolicyType;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.Link;

/**
 * @author mserbak
 */
@PageDescriptor(url = "/login")
public class PageLogin extends PageBase {

	private static final Trace LOGGER = TraceManager.getTrace(PageLogin.class);
	
    PageBase page = getPageBase();
    private static final String ID_LOGIN_FORM = "loginForm";

    private static final String ID_USERNAME = "username";
    private static final String ID_PASSWORD = "password";
    private static final String ID_CAPTCHA = "captchatextfield";
    private final CaptchaImageResource captchaImageResource;

    protected static final String OPERATION_LOAD_RESET_PASSWORD_POLICY = "LOAD PASSWORD RESET POLICY";

    
    public PageLogin() {
        if (SecurityUtils.getPrincipalUser() != null) {
            MidPointApplication app = getMidpointApplication();
            setResponsePage(app.getHomePage());
        }

        Form form = new Form(ID_LOGIN_FORM) {
        	
        	
            @Override
            protected void onSubmit() {
                MidPointAuthWebSession session = MidPointAuthWebSession.getSession();
                
                if (!imagePass.equals(getPassword()))
                {
                    //error("Captcha şifresi '" + getPassword() + "' yanlış.\n" +"Doğru şifre: " + imagePass);
                    error("Karakter doğrulama yanlış!");
					// force redrawing
                    return;
                }
                else
                {
                    info("Başarılı!");
                    // force redrawing                   
                }
				captchaImageResource.invalidate();
               
                RequiredTextField<String> username = (RequiredTextField) get(ID_USERNAME);
                PasswordTextField password = (PasswordTextField) get(ID_PASSWORD);
                if (session.authenticate(username.getModelObject(), password.getModelObject())) {
                    if (WebMiscUtil.isAuthorized(AuthorizationConstants.AUTZ_UI_DASHBOARD_URL,
                            AuthorizationConstants.AUTZ_UI_HOME_ALL_URL)) {
                        setResponsePage(PageDashboard.class);
                    } else {
                        setResponsePage(PageSelfDashboard.class);
                    }
                }
            }
        };
        OperationResult parentResult = new OperationResult(OPERATION_LOAD_RESET_PASSWORD_POLICY);

        CredentialsPolicyType creds = null;
        try {
            creds = getModelInteractionService().getCredentialsPolicy(null, (Task) null, parentResult);
        } catch (ObjectNotFoundException | SchemaException e) {
            LOGGER.warn("Cannot read credentials policy: "+e.getMessage(), e);
        }
        BookmarkablePageLink<String> link = new BookmarkablePageLink<String>("forgetpassword", PageForgetPassword.class);
        boolean linkIsVisible = false;
        if (creds != null && creds.getSecurityQuestions() != null && creds.getSecurityQuestions().getQuestionNumber() != null) {
            linkIsVisible = true;
        }
        link.setVisible(linkIsVisible);
        form.add(link);

        form.add(new RequiredTextField(ID_USERNAME, new Model<String>()));
        form.add(new PasswordTextField(ID_PASSWORD, new Model<String>()));
        
        captchaImageResource = new CaptchaImageResource(imagePass);
		Image image = new NonCachingImage("captchaImage",captchaImageResource);
		form.add(image);
    	//form.add(new Image("captchaImage", captchaImageResource));
    	form.add(new RequiredTextField<String>(ID_CAPTCHA, new PropertyModel<String>(properties,
    			ID_CAPTCHA))
            {
                @Override
                protected final void onComponentTag(final ComponentTag tag)
                {
                    super.onComponentTag(tag);
                    // clear the field after each render
                    tag.put("value", "");
                }
            });
        form.add(new Link("btnselfregistration"){
            @Override
            public void onClick(){
                setResponsePage(PageSelfRegistration.class);
            }
        });

        add(form);
    }
    
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
    
    /** Random captcha password to match against. */
    private final String imagePass = randomString(6, 8);
    private final ValueMap properties = new ValueMap();
    
    private String getPassword()
    {
        return properties.getString(ID_CAPTCHA);
    }

    public PageBase getPageBase() {
        return (PageBase) getPage();
    }
}
