package fr.insalyon.creatis.vip.application.integrationtest;

import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.*;
import fr.insalyon.creatis.vip.application.server.business.simulation.WebServiceEngine;
import fr.insalyon.creatis.vip.application.server.dao.ApplicationDAO;
import fr.insalyon.creatis.vip.application.server.dao.ClassDAO;
import fr.insalyon.creatis.vip.core.server.dao.GroupDAO;
import fr.insalyon.creatis.vip.core.server.dao.UserDAO;
import fr.insalyon.creatis.vip.core.server.dao.UsersGroupsDAO;
import fr.insalyon.creatis.vip.datamanager.server.business.DataManagerBusiness;
import org.hibernate.SessionFactory;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/*
   replaces workflowsdb beans by mocks in tests
 */
@Configuration
@Profile("test")
public class SpringApplicationTestConfig {


    @Bean
    //@Primary
    public WorkflowsDBDAOFactory mockWorkflowsDBDAOFactory() {
        return Mockito.mock(WorkflowsDBDAOFactory.class);
    }

    @Bean
    //@Primary
    public SessionFactory mockSessionFactory() {
        return Mockito.mock(SessionFactory.class);
    }

    @Bean
    //@Primary
    public WorkflowDAO mockWorkflowDAO() {
        return Mockito.mock(WorkflowDAO.class);
    }

    @Bean
    //@Primary
    public ProcessorDAO mockProcessorDAO() {
        return Mockito.mock(ProcessorDAO.class);
    }

    @Bean
    //@Primary
    public OutputDAO mockOutputDAO() {
        return Mockito.mock(OutputDAO.class);
    }

    @Bean
    //@Primary
    public UsersGroupsDAO mockUsersGroupsDAO() {
        return Mockito.mock(UsersGroupsDAO.class);
    }

    @Bean
    //@Primary
    public UserDAO mockUserDAO() {
        return Mockito.mock(UserDAO.class);
    }

    @Bean
    //@Primary
    public InputDAO mockInputDAO() {
        return Mockito.mock(InputDAO.class);
    }

    @Bean
    //@Primary
    public StatsDAO mockStatsDAO() {
        return Mockito.mock(StatsDAO.class);
    }

    @Bean
    //@Primary
    public ApplicationDAO mockApplicationDAO() {
        return Mockito.mock(ApplicationDAO.class);
    }

    @Bean
    //@Primary
    public WebServiceEngine mockWebServiceEngine() {
        return Mockito.mock(WebServiceEngine.class);
    }

    @Bean
    //@Primary
    public GroupDAO mockGroupDAO() {
        return Mockito.mock(GroupDAO.class);
    }

    @Bean
    //@Primary
    public ClassDAO mockClassDAO() {
        return Mockito.mock(ClassDAO.class);
    }

    @Bean
    //@Primary
    public DataManagerBusiness mockDataManagerBusiness() {
        return Mockito.mock(DataManagerBusiness.class);
    }
}
