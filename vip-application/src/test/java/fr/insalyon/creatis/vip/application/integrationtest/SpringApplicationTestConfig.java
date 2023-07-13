package fr.insalyon.creatis.vip.application.integrationtest;

import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.*;
import fr.insalyon.creatis.vip.application.server.business.simulation.WebServiceEngine;
import fr.insalyon.creatis.vip.application.server.dao.ApplicationDAO;
import fr.insalyon.creatis.vip.application.server.dao.ClassDAO;
import fr.insalyon.creatis.vip.core.server.dao.GroupDAO;
import fr.insalyon.creatis.vip.core.server.dao.UserDAO;
import fr.insalyon.creatis.vip.core.server.dao.UsersGroupsDAO;
import fr.insalyon.creatis.vip.datamanager.server.business.DataManagerBusiness;
import fr.insalyon.creatis.vip.datamanager.server.business.LFCBusiness;
import fr.insalyon.creatis.vip.datamanager.server.business.LfcPathsBusiness;
import fr.insalyon.creatis.vip.datamanager.server.business.TransferPoolBusiness;
import org.hibernate.SessionFactory;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/*
   replaces workflowsdb beans by mocks in tests
 */
@Configuration
@Profile("test")
public class SpringApplicationTestConfig {


    @Bean
    @Primary
    public WorkflowsDBDAOFactory workflowsDBDAOFactory() {
        return Mockito.mock(WorkflowsDBDAOFactory.class);
    }

    @Bean
    @Primary
    public SessionFactory sessionFactory() {
        return Mockito.mock(SessionFactory.class);
    }

    @Bean
    @Primary
    public WorkflowDAO getTestWorkflowDAO() {
        return Mockito.mock(WorkflowDAO.class);
    }

    @Bean
    @Primary
    public ProcessorDAO getTestProcessorDAO() {
        return Mockito.mock(ProcessorDAO.class);
    }

    @Bean
    @Primary
    public OutputDAO getTestOutputDAO() {return Mockito.mock(OutputDAO.class);}

    @Bean
    @Primary
    public UsersGroupsDAO getTestUsersGroupsDAO() {
        return Mockito.mock(UsersGroupsDAO.class);
    }

    @Bean
    @Primary
    public UserDAO getTestUserDAO() {return Mockito.mock(UserDAO.class);}
    /*@Bean
    @Primary
    public TransferPoolBusiness getTransferPoolBusiness() {
        return Mockito.mock(TransferPoolBusiness.class);
    }*/

    @Bean
    @Primary
    public InputDAO getTestInputDAO() {
        return Mockito.mock(InputDAO.class);
    }

    @Bean
    @Primary
    public StatsDAO getTestStatsDAO() {return Mockito.mock(StatsDAO.class);}

    @Bean
    @Primary
    public ApplicationDAO getTestApplicationDAO() {
        return Mockito.mock(ApplicationDAO.class);
    }

    @Bean
    @Primary
    public WebServiceEngine getTestWebServiceEngine() {
        return Mockito.mock(WebServiceEngine.class);
    }

    @Bean
    @Primary
    public GroupDAO getTestGroupDAO() {
        return Mockito.mock(GroupDAO.class);
    }

    @Bean
    @Primary
    public ClassDAO getTestClassDAO() {
        return Mockito.mock(ClassDAO.class);
    }

    @Bean
    @Primary
    public DataManagerBusiness getDataManagerBusiness() {
        return Mockito.mock(DataManagerBusiness.class);
    }






}
