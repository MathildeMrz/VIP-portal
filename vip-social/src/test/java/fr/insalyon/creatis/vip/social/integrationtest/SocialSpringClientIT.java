package fr.insalyon.creatis.vip.social.integrationtest;

import com.smartgwt.client.widgets.tab.Tab;
import fr.insalyon.creatis.vip.core.client.view.layout.CenterTabSet;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.social.client.SocialModule;
import fr.insalyon.creatis.vip.social.client.view.MainLayout;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class SocialSpringClientIT extends BaseSpringIT {
    @Autowired
    public SocialModule socialModule;


    @Test
    public void testLoad()
    {
        socialModule.load();

    }

    @Test
    public void testPostLoading()
    {
        socialModule.postLoading();
    }

    @Test
    public void testTerminate()
    {
        //Set<Tab> test = null;
        //socialModule.terminate(test);
    }

    @Test
    public void testVerifyMessages()
    {
        SocialModule.verifyMessages();
    }



}
