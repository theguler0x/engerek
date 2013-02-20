package com.evolveum.midpoint.web.page.admin.configuration;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.button.AjaxLinkButton;
import com.evolveum.midpoint.web.component.button.AjaxSubmitLinkButton;
import com.evolveum.midpoint.web.component.button.ButtonType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;

/**
 * @author lazyman
 */
public class PageSystemConfiguration extends PageAdminConfiguration {

    private static final Trace LOGGER = TraceManager.getTrace(PageSystemConfiguration.class);
    private static final String DOT_CLASS = PageSystemConfiguration.class.getName() + ".";

    private static final String ID_MAIN_FORM = "mainForm";
    private static final String ID_BACK = "back";
    private static final String ID_SAVE = "save";
    private static final String ID_REMOVE_PASSWORD_POLICY = "removePasswordPolicy";
    private static final String ID_BROWSE_PASSWORD_POLICY = "browsePasswordPolicy";

    public PageSystemConfiguration() {
        initLayout();
    }

    private void initLayout() {
        Form mainForm = new Form(ID_MAIN_FORM);
        add(mainForm);

        initButtons(mainForm);
    }

    private void initButtons(Form mainForm) {
        AjaxLinkButton removePasswordPolicy = new AjaxLinkButton(ID_REMOVE_PASSWORD_POLICY,
                createStringResource("PageSystemConfiguration.button.removePasswordPolicy")) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                removePasswordPolicyPerformed(target);
            }
        };
        mainForm.add(removePasswordPolicy);
        AjaxLinkButton browsePasswordPolicy = new AjaxLinkButton(ID_BROWSE_PASSWORD_POLICY,
                createStringResource("PageSystemConfiguration.button.browsePasswordPolicy")) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                browsePasswordPolicyPerformed(target);
            }
        };
        mainForm.add(browsePasswordPolicy);

        AjaxSubmitLinkButton save = new AjaxSubmitLinkButton(ID_SAVE, ButtonType.POSITIVE,
                createStringResource("PageBase.button.save")) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                savePerformed(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(getFeedbackPanel());
            }
        };
        mainForm.add(save);

        AjaxLinkButton back = new AjaxLinkButton(ID_BACK, createStringResource("PageBase.button.back")) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                backPerformed(target);
            }
        };
        mainForm.add(back);
    }

    public void savePerformed(AjaxRequestTarget target) {
        //todo implement
    }

    public void backPerformed(AjaxRequestTarget target) {
        //todo implement
    }

    public void removePasswordPolicyPerformed(AjaxRequestTarget target) {
        //todo implement
    }

    public void browsePasswordPolicyPerformed(AjaxRequestTarget target) {
        //todo implement
    }
}
