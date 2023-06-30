package integrationtest;

import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.publication.client.bean.Publication;
import fr.insalyon.creatis.vip.publication.server.business.PublicationBusiness;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PublicationsIT extends BaseSpringIT
{
    @Autowired
    private PublicationBusiness publicationBusiness;
    @Autowired
    private Server server;

    private long idPublicationCreated;

    @BeforeEach
    public void setUp() throws BusinessException, DAOException, GRIDAClientException
    {
        super.setUp();

        adminEmail = server.getAdminEmail();

        // Create test publication
        Publication publication = new Publication("Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", adminEmail, null);
        publicationBusiness.addPublication(publication);
        idPublicationCreated = publicationBusiness.getPublications().get(0).getId();
    }

    @Test
    public void testInitialisation() throws BusinessException
    {
        // verify publications nb
        Assertions.assertEquals(1, publicationBusiness.getPublications().size(), "Incorrect number of publications");

        // verify publications authors
        Assertions.assertEquals("author1, author2", publicationBusiness.getPublication(idPublicationCreated).getAuthors(), "Incorrect authors value");

        // verify publications vip application
        assertNull(publicationBusiness.getPublication(idPublicationCreated).getVipApplication(), "Incorrect VIPApplication value");
    }


    /* ********************************************************************************************************************************************** */
    /* ********************************************************** create and add publication ******************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testAddPublication() throws BusinessException
    {
        // With id
        Publication publication = new Publication(idPublicationCreated, "Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", adminEmail, null);
        publicationBusiness.addPublication(publication);

        // Without id
        Publication publication2 = new Publication("Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", adminEmail, null);
        publicationBusiness.addPublication(publication2);

        // Without vipAuthor
        Publication publication3 = new Publication(idPublicationCreated, "Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", null);
        publicationBusiness.addPublication(publication3);

        // Without parameter
        Publication publication4 = new Publication();
        publicationBusiness.addPublication(publication4);

        Assertions.assertEquals(5, publicationBusiness.getPublications().size(), "Incorrect publications number");


    }

    @Test
    public void testAddExistingPublication() throws BusinessException
    {
        Publication publication = new Publication(idPublicationCreated, "Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", adminEmail, null);

        //No exception because the id is not taken into account for the object creation
        publicationBusiness.addPublication(publication);
    }

    @Test
    public void testCatchAddPublicationInexistingVipAuthor()
    {
        Publication publication = new Publication(idPublicationCreated, "Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", "inexisting_vip_author@test.fr", null);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        publicationBusiness.addPublication(publication)
        );

        // INSERT + inexisting foreign key vipAuthor => violation
        assertTrue(StringUtils.contains(exception.getMessage(), "JdbcSQLException: Referential integrity constraint violation"));
    }


    /* ********************************************************************************************************************************************** */
    /* ************************************************************** update publication ************************************************************ */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testUpdatePublication() throws BusinessException
    {
        Publication publication = new Publication(idPublicationCreated, "Publication title", "21/06/2023", "01010100", "author2, author3", "type", "typeName", adminEmail, null);
        publicationBusiness.updatePublication(publication);

        Assertions.assertEquals("author2, author3", publicationBusiness.getPublication(idPublicationCreated).getAuthors(), "Incorrect authors value");

    }

    @Test
    public void testSetAttributesUpdatePublication() throws BusinessException, DAOException
    {
        Publication publication = publicationBusiness.getPublication(idPublicationCreated);

        configurationBusiness.getOrCreateUser("test1@test.fr", "institution", null);
        publication.setAuthors("author2, author3");
        publication.setId(idPublicationCreated);
        publication.setDate("22/06/2023");
        publication.setDoi("010110");
        publication.setType("type updated");
        publication.setTitle("Publication title updated");
        publication.setTypeName("typeName updated");
        publication.setTypeName("typeName updated");
        publication.setVipAuthor("test1@test.fr");

        publicationBusiness.updatePublication(publication);

        // verify publication updated information
        Assertions.assertEquals("author2, author3", publicationBusiness.getPublication(idPublicationCreated).getAuthors(), "Incorrect publication authors");
        Assertions.assertEquals(idPublicationCreated, publicationBusiness.getPublication(idPublicationCreated).getId(), "Incorrect publication id");
        Assertions.assertEquals("22/06/2023", publicationBusiness.getPublication(idPublicationCreated).getDate(), "Incorrect publication DOI");
        Assertions.assertEquals("010110", publicationBusiness.getPublication(idPublicationCreated).getDoi(), "Incorrect publication DOI");
        Assertions.assertEquals("type updated", publicationBusiness.getPublication(idPublicationCreated).getType(), "Incorrect publication type");
        Assertions.assertEquals("Publication title updated", publicationBusiness.getPublication(idPublicationCreated).getTitle(), "Incorrect publication title");
        Assertions.assertEquals("typeName updated", publicationBusiness.getPublication(idPublicationCreated).getTypeName(), "Incorrect publication typeName");
        Assertions.assertEquals("test1@test.fr", publicationBusiness.getPublication(idPublicationCreated).getVipAuthor(), "Incorrect publication VIP author");
    }

    @Test
    public void testCatchUpdateInexistingPublication() throws BusinessException
    {
        Publication publication = new Publication(100L, "Publication title", "21/06/2023", "01010100", "author2, author3", "type", "typeName", adminEmail, null);
        // UPDATE + inexisting primary key idPublication => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be updated
        publicationBusiness.updatePublication(publication);

        Assertions.assertEquals(1, publicationBusiness.getPublications().size(), "Incorrect number of publications");

    }

    @Test
    public void testCatchUpdatePublicationInexistingVipAuthor() throws BusinessException
    {
        Publication publication = new Publication(idPublicationCreated, "Publication title", "21/06/2023", "01010100", "author2, author3", "type", "typeName", "inexistingVipAuthor@test.fr", null);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        publicationBusiness.updatePublication(publication)
        );

        // UPDATE + inexisting foreign key vipAuthor => violation
        assertTrue(StringUtils.contains(exception.getMessage(), "JdbcSQLException: Referential integrity constraint violation"));
        assertEquals(adminEmail, publicationBusiness.getPublication(idPublicationCreated).getVipAuthor(), "Incorrect vipAuthor publication updated");
    }

    /* ********************************************************************************************************************************************** */
    /* *************************************************************** get publication ************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetPublication() throws BusinessException
    {
        Publication publication = publicationBusiness.getPublication(idPublicationCreated);

        // verify publication information
        Assertions.assertEquals("author1, author2", publication.getAuthors(), "Incorrect publication authors");
        Assertions.assertEquals(idPublicationCreated, publication.getId(), "Incorrect publication id");
        Assertions.assertEquals("21/06/2023", publication.getDate(), "Incorrect publication DOI");
        Assertions.assertEquals("01010100", publication.getDoi(), "Incorrect publication DOI");
        Assertions.assertEquals("type", publication.getType(), "Incorrect publication type");
        Assertions.assertEquals("Publication title", publication.getTitle(), "Incorrect publication title");
        Assertions.assertEquals("typeName", publication.getTypeName(), "Incorrect publication typeName");
        Assertions.assertEquals(adminEmail, publication.getVipAuthor(), "Incorrect publication VIP author");
    }

    @Test
    public void testCatchGetInexistingPublication() throws BusinessException
    {
        // SELECT + inexisting primary key publicationId => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be selected
        assertNull(publicationBusiness.getPublication(100L));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************** remove publication ************************************************************ */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemovePublication() throws BusinessException
    {
        publicationBusiness.removePublication(idPublicationCreated);

        Assertions.assertEquals(0, publicationBusiness.getPublications().size(), "Incorrect number of publications");
    }

    @Test
    public void testCatchRemoveInexistantPublication() throws BusinessException
    {
        // DELETE + inexisting primary key publicationId => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be deleted
        publicationBusiness.removePublication(100L);

        Assertions.assertEquals(1, publicationBusiness.getPublications().size(), "Incorrect number of publications");
    }

}
