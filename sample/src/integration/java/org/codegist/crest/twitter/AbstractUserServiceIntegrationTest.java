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

import org.codegist.crest.twitter.model.User;
import org.codegist.crest.twitter.service.UserService;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author laurent.gilles@codegist.org
 */
@Ignore
public abstract class AbstractUserServiceIntegrationTest {

    private final AccountService<UserService> account1;
    private final AccountService<UserService> account2;

    protected AbstractUserServiceIntegrationTest(AccountService<UserService> account1, AccountService<UserService> account2) {
        this.account1 = account1;
        this.account2 = account2;
    }

    @Test
    public void testSearch(){
        assertEquals(account1.getAccountId(), account1.getService().search(account1.getAccountScreenName())[0].getId());
        assertEquals(account1.getAccountId(), account1.getService().search(account1.getAccountScreenName(), 100, 1)[0].getId());
        assertEquals(0, account1.getService().search(account1.getAccountScreenName(), 100, 2).length);
    }
    @Test
    public void testGet(){
        assertEquals(account1.getAccountId(), account1.getService().get(account1.getAccountScreenName()).getId());
        assertEquals(account1.getAccountId(), account1.getService().get(account1.getAccountId()).getId());
    }
    @Test
    public void testLookUp(){
        User[] users = account1.getService().lookup(account1.getAccountId(), account1.getAccountScreenName(), account2.getAccountScreenName());
        assertEquals(account1.getAccountId(), users[0].getId());
        assertEquals(account2.getAccountId(), users[1].getId());
        
        users = account1.getService().lookup(new String[]{account1.getAccountScreenName(), account2.getAccountScreenName()});
        assertEquals(account1.getAccountId(), users[0].getId());
        assertEquals(account2.getAccountId(), users[1].getId());

    }

}
