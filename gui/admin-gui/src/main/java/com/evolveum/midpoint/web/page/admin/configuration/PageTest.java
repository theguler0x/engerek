package com.evolveum.midpoint.web.page.admin.configuration;

import com.evolveum.midpoint.web.component.button.AjaxLinkButton;
import com.evolveum.midpoint.web.security.MidPointApplication;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.model.Model;

/**
 * For Testing purposes only.
 *
 * @author lazyman
 */
@Deprecated
public class PageTest extends PageAdminConfiguration {

    private static int id = 0;

    public PageTest() {
        initLayout();
    }

    private void initLayout() {
        AjaxLinkButton atmButton = new AjaxLinkButton("atmButton", new Model<String>("Start task")) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                atmPerformed(target);
            }
        };
        add(atmButton);
    }

    private void atmPerformed(AjaxRequestTarget target) {
        id++;

        EventBus.get().post("Starting event: " + id);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                MidPointApplication app = new MidPointApplication();

                try {
                    Thread.sleep(15000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                EventBus.get(app).post("Task finished: " + id + ". Let's go party...");
            }
        };
        new Thread(runnable).start();
        EventBus.get().post("Event started: " + id);
    }
}
