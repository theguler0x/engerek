package com.evolveum.midpoint.web.page.admin.configuration;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.button.AjaxLinkButton;
import com.evolveum.midpoint.web.component.button.AjaxSubmitLinkButton;
import com.evolveum.midpoint.web.component.button.ButtonType;
import com.evolveum.midpoint.web.component.util.LoadableModel;
import com.evolveum.midpoint.web.page.admin.configuration.dto.SystemConfigurationDto;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.SystemConfigurationType;
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

    private LoadableModel<SystemConfigurationDto> model;

    public PageSystemConfiguration() {
        model = new LoadableModel<SystemConfigurationDto>(false) {

            @Override
            protected SystemConfigurationDto load() {
                return loadSystemConfiguration();
            }
        };

        initLayout();
    }

    private SystemConfigurationDto loadSystemConfiguration() {
        //todo implement

        return null;
    }

    private void initLayout() {
        Form mainForm = new Form(ID_MAIN_FORM);
        add(mainForm);

        initButtons(mainForm);
    }

    private void initButtons(Form mainForm) {
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
