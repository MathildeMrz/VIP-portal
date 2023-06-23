package integrationtest;

import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.publication.client.bean.Publication;
import fr.insalyon.creatis.vip.publication.server.business.PublicationBusiness;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;

//Spring : exécute cette méthode avant de lancer les tests
public class PublicationsIT extends BaseSpringIT {

    @Autowired
    private PublicationBusiness publicationBusiness;
    @Autowired
    private Server server;

    private long idPublicationCreated;

    private String adminMail;

    @BeforeEach
    public void setUp() throws BusinessException {
        adminMail = server.getAdminEmail();
        Publication publication = new Publication("Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", adminMail, null);
        publicationBusiness.addPublication(publication);
        idPublicationCreated = publicationBusiness.getPublications().get(0).getId();
    }

    @Test
    public void testInitialisation() throws BusinessException {
        Assertions.assertEquals(publicationBusiness.getPublications().size(), 1, "Incorrect number of publications");
        Assertions.assertEquals(publicationBusiness.getPublication(idPublicationCreated).getAuthors(), "author1, author2", "Incorrect authors value");
        Assertions.assertEquals(publicationBusiness.getPublication(idPublicationCreated).getVipApplication(), null, "Incorrect VIPApplication value");
    }

    @Test
    public void testAddExistingPublication() throws BusinessException {
        Publication publication = new Publication(idPublicationCreated, "Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", adminMail, null);

        //No exception because the id is not taken into account
        publicationBusiness.addPublication(publication);
    }

    @Test
    public void testSet() throws BusinessException {
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

        Assertions.assertEquals("author2, author3", publicationBusiness.getPublication(idPublicationCreated).getAuthors(), "Incorrect publication authors");
        Assertions.assertEquals(idPublicationCreated, publicationBusiness.getPublication(idPublicationCreated).getId(), "Incorrect publication id");
        Assertions.assertEquals( "22/06/2023", publicationBusiness.getPublication(idPublicationCreated).getDate(),"Incorrect publication DOI");
        Assertions.assertEquals("010110", publicationBusiness.getPublication(idPublicationCreated).getDoi(), "Incorrect publication DOI");
        Assertions.assertEquals("type updated", publicationBusiness.getPublication(idPublicationCreated).getType(), "Incorrect publication type");
        Assertions.assertEquals("Publication title updated", publicationBusiness.getPublication(idPublicationCreated).getTitle(), "Incorrect publication title");
        Assertions.assertEquals("typeName updated", publicationBusiness.getPublication(idPublicationCreated).getTypeName(), "Incorrect publication typeName");
        Assertions.assertEquals("test1@test.fr", publicationBusiness.getPublication(idPublicationCreated).getVipAuthor(), "Incorrect publication VIP author");

    }


    @Test
    public void testCatchGetInexistingPublication() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        publicationBusiness.getPublication(2L)
        );

        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no publication registered with the id : 2"));

    }

    @Test
    public void testRemovePublication() throws BusinessException {
        publicationBusiness.removePublication(idPublicationCreated);
        Assertions.assertEquals(publicationBusiness.getPublications().size(), 0, "Incorrect number of publications");
    }


    @Test
    public void testCatchRemoveInexistantPublication() {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        publicationBusiness.removePublication(2L)
        );

        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no publication registered with the id : 2"));

    }

    @Test
    public void testUpdatePublication() throws BusinessException {
        Publication publication = new Publication(idPublicationCreated, "Publication title", "21/06/2023", "01010100", "author2, author3", "type", "typeName", adminMail, null);
        publicationBusiness.updatePublication(publication);
        Assertions.assertEquals(publicationBusiness.getPublication(idPublicationCreated).getAuthors(), "author2, author3", "Incorrect authors value");

    }

    @Test
    public void testCatchUpdateInexistingPublication() {

        Publication publication = new Publication(2L, "Publication title", "21/06/2023", "01010100", "author2, author3", "type", "typeName", adminMail, null);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        publicationBusiness.updatePublication(publication)
        );

        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no publication registered with the id : 2"));

    }

    @Test
    public void testCreatePublication() throws BusinessException {
        // With an id
        Publication publication = new Publication(1L,"Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", adminMail, null);

        // Without an id
        Publication publication2 = new Publication("Publication title", "21/06/2023", "01010100", "author1, author2", "type", "typeName", adminMail, null);

        // Without parameter
        Publication publication3 = new Publication();

    }

}
