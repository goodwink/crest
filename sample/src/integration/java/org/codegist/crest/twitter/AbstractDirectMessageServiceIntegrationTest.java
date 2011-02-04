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
import org.codegist.crest.twitter.model.Message;
import org.codegist.crest.twitter.service.DirectMessageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author laurent.gilles@codegist.org
 */
@Ignore
public abstract class AbstractDirectMessageServiceIntegrationTest {

    private final AccountService<DirectMessageService> account1;
    private final AccountService<DirectMessageService> account2;

    protected AbstractDirectMessageServiceIntegrationTest(
            AccountService<DirectMessageService> account1,
            AccountService<DirectMessageService> account2
    ) {
        this.account1 = account1;
        this.account2 = account2;
    }

    @Test
    public void testConverstation() throws InterruptedException {
        // Acc 1 sent message to Acc 2
        String txt = Randoms.randomAlphaNumeric(5) + " hi what's up mate! " + Randoms.randomAlphaNumeric(5);
        Message sent = account1.getService().send(account2.getAccountId(), txt);
        assertNotNull(sent);
        Thread.sleep(10000);
        assertEquals(1, account1.getService().getSent(100, 1).length);
        assertEquals(1, account1.getService().getSent(100, 1, sent.getId() - 1, sent.getId()).length);
        assertEquals(0, account1.getService().getSent(100, 1, sent.getId(), sent.getId()).length);

        // Acc 2 check his box
        Message[] received = account2.getService().getReceived(100, 1);
        assertEquals(1, received.length);
        assertEquals(txt, received[0].getText());
        assertEquals(account1.getAccountId(), received[0].getSenderId());
        received = account2.getService().getReceived(100, 1, received[0].getId() - 1, received[0].getId());
        assertEquals(1, received.length);
        assertEquals(txt, received[0].getText());
        assertEquals(account1.getAccountId(), received[0].getSenderId());
        received = account2.getService().getReceived(100, 1, received[0].getId(), received[0].getId());
        assertEquals(0, received.length);

        Thread.sleep(5000);
        // Acc 2 send message to Acc 1
        txt = Randoms.randomAlphaNumeric(5) + " sdfsdfdsf " + Randoms.randomAlphaNumeric(5);
        sent = account2.getService().send(account1.getAccountId(), txt, account1.getAccountScreenName());
        assertNotNull(sent);
        Thread.sleep(10000);
        assertEquals(1, account2.getService().getSent(100, 1).length);
        assertEquals(1, account2.getService().getSent(100, 1, sent.getId() - 1, sent.getId()).length);
        assertEquals(0, account2.getService().getSent(100, 1, sent.getId(), sent.getId()).length);

        // Acc 1 check his box
        received = account1.getService().getReceived(100, 1);
        assertEquals(1, received.length);
        assertEquals(txt, received[0].getText());
        assertEquals(account2.getAccountId(), received[0].getSenderId());
    }

    @Before
    @After
    public void clean(){
        emptyMessageBoxes(account1.getService());
        emptyMessageBoxes(account2.getService());
    }

    private static void emptyMessageBoxes(DirectMessageService service){
        Message[] sent = service.getSent(1000, 1);
        Message[] received = service.getReceived(1000, 1);
        for(Message m : sent){
            service.destroy(m.getId());
        }
        for(Message m : received){
            service.destroy(m.getId());
        }

        sent = service.getSent(1000, 1);
        received = service.getReceived(1000, 1);
        assertEquals(0, sent.length);
        assertEquals(0, received.length);
    }

}
