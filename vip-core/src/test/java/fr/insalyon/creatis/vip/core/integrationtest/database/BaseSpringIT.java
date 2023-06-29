package fr.insalyon.creatis.vip.core.integrationtest.database;

import fr.insalyon.creatis.grida.client.GRIDAClient;
import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.client.view.util.CountryCode;
import fr.insalyon.creatis.vip.core.integrationtest.ServerMockConfig;
import fr.insalyon.creatis.vip.core.server.SpringCoreConfig;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.ConfigurationBusiness;
import fr.insalyon.creatis.vip.core.server.business.EmailBusiness;
import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Utility superclass to launch tests with the whole spring configuration, as
 * in production. Subclass only need to extend it and can benefit from dependency
 * injection.
 *
 *
 * The "test" profile overrides all the external dependencies
 * that would throw exception by mocked and configurable ones.
 * The "test-db" profile disable the default jndi datasource and uses a
 * h2 in-memory database instead
 */

@SpringJUnitConfig(SpringCoreConfig.class) // launch all spring environment for testing, also take test bean though automatic package scan
@ActiveProfiles({"test-db", "test"}) // to take random h2 database and not the test h2 jndi one
@TestPropertySource(properties = "db.tableEngine=") // to disable the default mysql/innodb engine on database init
@Transactional // each test is in a transaction that is rollbacked at the end to always leave a "clean" state
public abstract class BaseSpringIT {

    @Autowired
    protected ApplicationContext appContext;

    @Autowired
    protected ConfigurationBusiness configurationBusiness;

    @Autowired
    @Qualifier("db-datasource")
    protected DataSource dataSource; // this is a mockito spy wrapping the h2 memory datasource

    @Autowired
    protected DataSource lazyDataSource;

    @Autowired
    protected EmailBusiness emailBusiness;

    @Autowired
    protected Server server;

    @Autowired
    protected GRIDAClient gridaClient;

    public String adminEmail;
    public final String emailUser1 = "test1@test.fr";
    public final String emailUser2 = "test2@test.fr";
    public final String emailUser3 = "test3@test.fr";
    public final String emailUser4 = "test4@test.fr";
    public final String nameGroup1 = "group1";

    @BeforeEach
    protected void setUp() throws BusinessException, GRIDAClientException, DAOException {
        ServerMockConfig.reset(server);
    }

    protected void assertRowsNbInTable(String tableName, int expectedNb) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(lazyDataSource);
        int rowsNb = JdbcTestUtils.countRowsInTable(jdbcTemplate, tableName);
        assertEquals(expectedNb, rowsNb);
    }

    protected void createUser(String testEmail) throws GRIDAClientException, BusinessException {
        createUser(testEmail, "");
    }

    protected void createUser(String testEmail, String nameSuffix) throws GRIDAClientException, BusinessException {
        User newUser = new User("test firstName "+nameSuffix,
                "test lastName "+nameSuffix, testEmail, "test institution",
                "testPassword", CountryCode.fr,
                null);
        Mockito.when(gridaClient.exist(anyString())).thenReturn(true, false);
        configurationBusiness.signup(newUser, "", (Group) null);
    }

    protected void createUserInGroup(String userEmail, String nameSuffix, String groupName) throws BusinessException, GRIDAClientException {
        User newUser = new User("test firstName "+nameSuffix,
                "test lastName "+nameSuffix, userEmail, "test institution",
                "testPassword", CountryCode.fr,
                null);
        Mockito.when(gridaClient.exist(anyString())).thenReturn(true, false);
        Group group = configurationBusiness.getGroup(groupName);
        configurationBusiness.signup(newUser, "", false, true, group);
    }

    protected void signInUser() throws BusinessException {
        configurationBusiness.signin("test1@test.fr", "testPassword");
    }


}
