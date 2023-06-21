package integrationtest;

import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.publication.client.bean.Publication;
import fr.insalyon.creatis.vip.publication.server.business.PublicationBusiness;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // testing framework must recreate a MessageBusiness after each test method
public class PublicationsIT extends BaseSpringIT {

    @Autowired
    private PublicationBusiness publicationBusiness;
    @Autowired
    private Server server;

    @BeforeEach
    public void setUp() throws BusinessException
    {
        String adminMail = server.getAdminEmail();
        Publication publication = new Publication(1L, "Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", adminMail, null);
        publicationBusiness.addPublication(publication);
    }

    @Test
    public void testInitialisation() throws BusinessException
    {
        assert publicationBusiness.getPublications().size() == 1 : "Incorrect number of publications";
        assert publicationBusiness.getPublication(1L).getAuthors().equals("author1, author2")  : "Incorrect authors value";
        assert publicationBusiness.getPublication(1L).getVipApplication() == null : "Incorrect VIPApplication value";
    }

    @Test
    public void testAddExistingPublication()
    {
        String adminMail = server.getAdminEmail();
        Publication publication = new Publication(1L, "Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", adminMail, null);


        Exception exception = assertThrows(
                BusinessException.class, () ->
                        publicationBusiness.addPublication(publication)
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is already a publication registered with the id : 1"));
    }

    @Test
    public void testCatchGetInexistingPublication()
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        publicationBusiness.getPublication(2L)
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no publication registered with the id : 2"));

    }

    @Test
    public void testRemovePublication() throws BusinessException
    {
        publicationBusiness.removePublication(1L);
        assert publicationBusiness.getPublications().size() == 0 : "Incorrect number of publications";
    }

    @Test
    public void testCatchRemoveInexistantPublication()
    {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        publicationBusiness.removePublication(2L)
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no publication registered with the id : 2"));

    }

    @Test
    public void testUpdatePublication() throws BusinessException
    {
        String adminMail = server.getAdminEmail();
        Publication publication = new Publication(1L, "Publication title", "21/06/2023", "01010100", "author2, author3", "type", "typeName", adminMail, null);
        publicationBusiness.updatePublication(publication);
        assert publicationBusiness.getPublication(1L).getAuthors().equals("author2, author3") : "Incorrect authors value";

    }

    @Test
    public void testCatchUpdateInexistingPublication()
    {
        String adminMail = server.getAdminEmail();
        Publication publication = new Publication(2L, "Publication title", "21/06/2023", "01010100", "author2, author3", "type", "typeName", adminMail, null);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        publicationBusiness.updatePublication(publication)
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no publication registered with the id : 2"));

    }

}
