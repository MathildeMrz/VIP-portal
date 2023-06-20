package fr.insalyon.creatis.vip.social.integrationtest;

import fr.insalyon.creatis.vip.social.client.view.MainLayout;
import fr.insalyon.creatis.vip.social.client.view.common.AbstractMainLayout;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SocialSpringClientView {
    @Autowired
    public MainLayout mainLayout;

    // changes the visual organization
    @Test
    public void testSetLayout()
    {
        mainLayout.setLayout(null);
    }

    @Test
    public void testPostLoading()
    {
        mainLayout.loadData();
    }


}
