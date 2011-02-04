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

import org.codegist.crest.twitter.service.UserService;

import static org.codegist.crest.twitter.CRestBuilderFactory.ACC_1;
import static org.codegist.crest.twitter.CRestBuilderFactory.ACC_2;

/**
 * @author laurent.gilles@codegist.org
 */
public class DefaultUserServiceIntegrationTest extends AbstractUserServiceIntegrationTest {
    public DefaultUserServiceIntegrationTest() {
        super(getAccountService(ACC_1), getAccountService(ACC_2));
    }

    private static AccountService<UserService> getAccountService(String account){
        return new AccountService<UserService>(
                CRestBuilderFactory.getBaseCRestBuilder(account).build().build(UserService.class),
                CRestBuilderFactory.getAccountUserId(account),
                CRestBuilderFactory.getAccountScreenName(account)
                );
    }
}
