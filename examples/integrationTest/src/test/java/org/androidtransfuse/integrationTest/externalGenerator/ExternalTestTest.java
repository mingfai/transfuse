/**
 * Copyright 2013 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidtransfuse.integrationTest.externalGenerator;

import android.os.Bundle;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.androidtransfuse.integrationTest.DelegateUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author John Ericksen
 */
@RunWith(RobolectricTestRunner.class)
public class ExternalTestTest {

    private ExternalTest test;

    @Before
    public void setUp() {
        ExternalTestActivity testActivity = new ExternalTestActivity();
        testActivity.onCreate(new Bundle());

        test = DelegateUtil.getDelegate(testActivity, ExternalTest.class);
    }

    @Test
    public void testGeneratedProxy() throws Exception {
        assertNotNull(test.getTarget().getProxy());

    }

    @Test
    public void testInjectedProxied() throws Exception {
        assertNotNull(test.getProxied());
        assertEquals(ProxiedProxy.class, test.getProxied().getClass());
    }
}
