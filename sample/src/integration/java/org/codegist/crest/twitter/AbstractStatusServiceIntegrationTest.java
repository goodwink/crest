/*
 * Copyright 2010 CodeGist.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *  ==================================================================
 *
 *  More information at http://www.codegist.org.
 */

package org.codegist.crest.twitter;

import org.codegist.common.lang.Randoms;
import org.codegist.crest.HttpException;
import org.codegist.crest.twitter.model.Status;
import org.codegist.crest.twitter.service.StatusService;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author laurent.gilles@codegist.org
 */
@Ignore
public abstract class AbstractStatusServiceIntegrationTest {

    private final AccountService<StatusService> account1;
    private final AccountService<StatusService> account2;

    protected AbstractStatusServiceIntegrationTest(
            AccountService<StatusService> account1,
            AccountService<StatusService> account2
    ) {
        this.account1 = account1;
        this.account2 = account2;
    }

    @Test
    public void testStatuses() throws InterruptedException {
        String txt1 = Randoms.randomAlphaNumeric(5) + "- hey what's up there! I'm testing yeaaah";
        Status status = account1.getService().updateStatus(txt1);
        assertNotNull(status);
        assertEquals(txt1, status.getText());

        String txt2 = Randoms.randomAlphaNumeric(5) + "- hey what's up there! I'm testing in paris yeaah";
        Status statusChanged = account1.getService().updateStatus(txt2, 48.856578f, 2.351828f);
        assertNotNull(statusChanged);
        assertEquals(txt2, statusChanged.getText());
        Thread.sleep(10000);

        Status status1 = account1.getService().getStatus(status.getId());
        assertNotNull(status1);
        assertEquals(txt1, status1.getText());

        Status status2 = account1.getService().getStatus(statusChanged.getId());
        assertNotNull(status2);
        assertEquals(txt2, status2.getText());

        deleteStatus(account1.getService(), status1, status2);
    }

    @Test
    public void testRetweets() throws InterruptedException {
        String txt1 = Randoms.randomAlphaNumeric(5) + "- hey what's up there! I'm testing yeaaah";
        Status status = account2.getService().updateStatus(txt1);
        assertNotNull(status);
        assertEquals(txt1, status.getText());
        Thread.sleep(10000);

        Status retweet = account1.getService().retweetStatus(status.getId());
        assertNotNull(retweet);
        String name = account1.getService().getFriends(account1.getAccountId())[0].getScreenName();
        assertEquals("RT @" + name +": " + txt1, retweet.getText());

        assertEquals(account1.getAccountId(), account1.getService().getRetweetedBy(status.getId())[0].getId());
        assertEquals(account1.getAccountId(), account1.getService().getRetweetedBy(status.getId(), 100, 1)[0].getId());
        assertEquals(0, account1.getService().getRetweetedBy(status.getId(), 100, 2).length);

        assertEquals(account1.getAccountId(), account1.getService().getRetweetedByIds(status.getId())[0]);
        assertEquals(account1.getAccountId(), account1.getService().getRetweetedByIds(status.getId(), 100, 1)[0]);
        assertEquals(0, account1.getService().getRetweetedByIds(status.getId(), 100, 2).length);

        assertEquals(retweet.getId(), account1.getService().getRetweets(status.getId())[0].getId());
        assertEquals(retweet.getId(), account1.getService().getRetweets(status.getId(), 1)[0].getId());
        assertEquals(0, account1.getService().getRetweets(status.getId(), 0).length);

        deleteStatus(account1.getService(), retweet);
        deleteStatus(account2.getService(), status);
    }

    @Test
    public void testFriends(){
        assertEquals(account2.getAccountId(), account1.getService().getFriends(account1.getAccountId())[0].getId());
        assertEquals(account2.getAccountId(), account1.getService().getFriends(account1.getAccountId(), account2.getAccountScreenName())[0].getId());
        assertEquals(account1.getAccountId(), account1.getService().getFriends(account2.getAccountScreenName())[0].getId());

        assertEquals(account2.getAccountId(), account1.getService().getFriends(account1.getAccountId(), -1l).getPayload()[0].getId());
        assertEquals(account2.getAccountId(), account1.getService().getFriends(account1.getAccountId(), account2.getAccountScreenName(), -1l).getPayload()[0].getId());
        assertEquals(account1.getAccountId(), account1.getService().getFriends(account2.getAccountScreenName(), -1l).getPayload()[0].getId());
    }

    @Test
    public void testFollowers(){
        assertEquals(account2.getAccountId(), account1.getService().getFollowers(account1.getAccountId())[0].getId());
        assertEquals(account2.getAccountId(), account1.getService().getFollowers(account1.getAccountId(), account2.getAccountScreenName())[0].getId());
        assertEquals(account1.getAccountId(), account1.getService().getFollowers(account2.getAccountScreenName())[0].getId());

        assertEquals(account2.getAccountId(), account1.getService().getFollowers(account1.getAccountId(), -1l).getPayload()[0].getId());
        assertEquals(account2.getAccountId(), account1.getService().getFollowers(account1.getAccountId(), account2.getAccountScreenName(), -1l).getPayload()[0].getId());
        assertEquals(account1.getAccountId(), account1.getService().getFollowers(account2.getAccountScreenName(), -1l).getPayload()[0].getId());
    }

    private static void deleteStatus(StatusService statusService, Status... ses){
        for(Status s : ses){
            deleteStatus(statusService, s);
        }
    }
    private static void deleteStatus(StatusService statusService, Status s){
        assertNotNull(statusService.removeStatus(s.getId()));
        try {
            statusService.getStatus(s.getId());
            fail("should not be here");
        } catch (HttpException e) {}
    }

}
