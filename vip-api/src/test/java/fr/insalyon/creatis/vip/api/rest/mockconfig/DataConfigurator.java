/*
 * Copyright and authors: see LICENSE.txt in base repository.
 *
 * This software is a web portal for pipeline execution on distributed systems.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.api.rest.mockconfig;

import fr.insalyon.creatis.vip.api.rest.config.BaseVIPSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.datamanager.server.business.LFCBusiness;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static fr.insalyon.creatis.vip.api.data.PathTestUtils.*;
import static fr.insalyon.creatis.vip.api.data.UserTestUtils.baseUser1;
import static fr.insalyon.creatis.vip.api.data.UserTestUtils.baseUser2;
import static fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants.ROOT;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Created by abonnet on 2/23/17.
 */
public class DataConfigurator {

    public static void configureFS(BaseVIPSpringIT testSuite) throws BusinessException {
        // exists
        // getModifDate
        // listDir
        LFCBusiness mockLFCBusinnes = testSuite.getLfcBusiness();
        // /vip
        Mockito.when(mockLFCBusinnes.listDir(
                         eq(baseUser1), eq(ROOT), eq(true)))
            .thenReturn(Arrays.asList(user1Dir));
        Mockito.when(mockLFCBusinnes.listDir(
                         eq(baseUser2), eq(ROOT), eq(true)))
            .thenReturn(Arrays.asList(user2Dir));
        // /vip/Home
        Mockito.when(mockLFCBusinnes.exists(
                         eq(baseUser1), eq("/vip/Home")))
            .thenReturn(true);
        Mockito.when(mockLFCBusinnes.exists(
                         eq(baseUser2), eq("/vip/Home")))
            .thenReturn(true);
        Mockito.when(mockLFCBusinnes.listDir(
                         eq(baseUser1), eq("/vip/Home"), eq(true)))
            .thenReturn(Arrays.asList(testFile1));
        Mockito.when(mockLFCBusinnes.listDir(
                         eq(baseUser2), eq("/vip/Home"), eq(true)))
            .thenReturn(Arrays.asList(testFile2, testDir1));
        // (user1) /vip/Home/testFile1
        Mockito.when(
            mockLFCBusinnes.exists(
                eq(baseUser1), eq("/vip/Home/testFile1.xml")))
            .thenReturn(true);
        Mockito.when(
            mockLFCBusinnes.listDir(
                eq(baseUser1), eq("/vip/Home/testFile1.xml"), eq(true)))
            .thenReturn(Arrays.asList(testFile1));
        // (user2) /vip/Home/testFile2
        Mockito.when(
            mockLFCBusinnes.exists(
                eq(baseUser2), eq("/vip/Home/testFile2.json")))
            .thenReturn(true);
        Mockito.when(
            mockLFCBusinnes.listDir(
                eq(baseUser2), eq("/vip/Home/testFile2.json"), eq(true)))
            .thenReturn(Arrays.asList(testFile2));
        // (user2) /vip/Home/testDir1
        Mockito.when(
            mockLFCBusinnes.exists(
                eq(baseUser2), eq("/vip/Home/testDir1")))
            .thenReturn(true);
        Mockito.when(
            mockLFCBusinnes.listDir(
                eq(baseUser2),
                eq("/vip/Home/testDir1"),
                eq(true)))
            .thenReturn(Arrays.asList(testFile3, testFile4, testFile5));
        Mockito.when(
            mockLFCBusinnes.getModificationDate(
                eq(baseUser2), eq("/vip/Home/testDir1")))
            .thenReturn(getDataModitTS(testDir1)*1000);
        // (user2) /vip/Home/testFile[345]
        Mockito.when(
            mockLFCBusinnes.exists(
                eq(baseUser2), eq("/vip/Home/testDir1/testFile3")))
            .thenReturn(true);
        Mockito.when(
            mockLFCBusinnes.exists(
                eq(baseUser2), eq("/vip/Home/testDir1/testFile4.pdf")))
            .thenReturn(true);
        Mockito.when(
            mockLFCBusinnes.exists(
                eq(baseUser2), eq("/vip/Home/testDir1/testFile5.zip")))
            .thenReturn(true);
        Mockito.when(
            mockLFCBusinnes.listDir(
                eq(baseUser2), eq("/vip/Home/testDir1/testFile3"), eq(true)))
            .thenReturn(Collections.singletonList(testFile3));
        Mockito.when(
            mockLFCBusinnes.listDir(
                eq(baseUser2),
                eq("/vip/Home/testDir1/testFile4.pdf"),
                eq(true)))
            .thenReturn(Collections.singletonList(testFile4));
        Mockito.when(
            mockLFCBusinnes.listDir(
                eq(baseUser2),
                eq("/vip/Home/testDir1/testFile5.zip"),
                eq(true)))
            .thenReturn(Collections.singletonList(testFile5));
    }

}
