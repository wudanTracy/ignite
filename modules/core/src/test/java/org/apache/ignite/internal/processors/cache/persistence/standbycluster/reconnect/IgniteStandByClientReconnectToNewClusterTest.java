/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.cache.persistence.standbycluster.reconnect;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.apache.ignite.internal.IgniteEx;

/**
 *
 */
public class IgniteStandByClientReconnectToNewClusterTest extends IgniteAbstractStandByClientReconnectTest {

    public void testActiveClientReconnectToActiveCluster() throws Exception {
        CountDownLatch activateLatch = new CountDownLatch(1);

        startNodes(activateLatch);

        info(">>>> star grid");

        IgniteEx ig1 = grid(node1);
        IgniteEx ig2 = grid(node2);
        IgniteEx client = grid(nodeClient);

        assertTrue(!ig1.active());
        assertTrue(!ig2.active());
        assertTrue(!client.active());

        info(">>>> activate grid");

        client.active(true);

        checkDescriptors(ig1, staticCacheNames);
        checkDescriptors(ig2, staticCacheNames);
        checkDescriptors(client, staticCacheNames);

        checkStaticCaches();

        info(">>>> dynamic start [" + ccfgDynamicName + ", " + ccfgDynamicWithFilterName + "]");

        client.createCache(ccfgDynamic);

        client.createCache(ccfgDynamicWithFilter);

        assertTrue(ig1.active());
        assertTrue(ig2.active());
        assertTrue(client.active());

        checkDescriptors(ig1, allCacheNames);
        checkDescriptors(ig2, allCacheNames);
        checkDescriptors(client, allCacheNames);

        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        final CountDownLatch reconnectedLatch = new CountDownLatch(1);

        addDisconnectListener(disconnectedLatch, reconnectedLatch);

        info(">>>> stop servers");

        stopGrid(node1);
        stopGrid(node2);

        disconnectedLatch.await();

        ig1 = startGrid(getConfiguration(node1));
        ig2 = startGrid(getConfiguration(node2));

        info(">>>> activate new servers");

        ig1.active(true);

        assertTrue(ig1.active());
        assertTrue(ig2.active());

        activateLatch.countDown();

        info(">>>> reconnect client");

        reconnectedLatch.await();

        info(">>>> client reconnected");

        assertTrue(ig1.active());
        assertTrue(ig2.active());
        assertTrue(client.active());

        checkAllCaches();
    }

    public void testActiveClientReconnectToInActiveCluster() throws Exception {
        startNodes(null);

        info(">>>> star grid");

        IgniteEx ig1 = grid(node1);
        IgniteEx ig2 = grid(node2);
        IgniteEx client = grid(nodeClient);

        assertTrue(!ig1.active());
        assertTrue(!ig2.active());
        assertTrue(!client.active());

        info(">>>> activate grid");

        client.active(true);

        checkStaticCaches();

        checkDescriptors(ig1, staticCacheNames);
        checkDescriptors(ig2, staticCacheNames);
        checkDescriptors(client, staticCacheNames);

        info(">>>> dynamic start [" + ccfgDynamicName + ", " + ccfgDynamicWithFilterName + "]");

        client.createCache(ccfgDynamic);

        client.createCache(ccfgDynamicWithFilter);

        checkDescriptors(ig1, allCacheNames);
        checkDescriptors(ig2, allCacheNames);
        checkDescriptors(client, allCacheNames);

        assertTrue(ig1.active());
        assertTrue(ig2.active());
        assertTrue(client.active());

        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        final CountDownLatch reconnectedLatch = new CountDownLatch(1);

        addDisconnectListener(disconnectedLatch, reconnectedLatch);

        info(">>>> stop servers");

        stopGrid(node1);
        stopGrid(node2);

        assertTrue(client.active());

        System.out.println("Await disconnected");

        disconnectedLatch.await();

        ig1 = startGrid(getConfiguration("node1"));
        ig2 = startGrid(getConfiguration("node2"));

        info(">>>> reconnect client");

        reconnectedLatch.await();

        info(">>>> client reconnected");

        assertTrue(!ig1.active());
        assertTrue(!ig2.active());
        assertTrue(!client.active());

        info(">>>> activate new servers");

        client.active(true);

        assertTrue(ig1.active());
        assertTrue(ig2.active());
        assertTrue(client.active());

        checkAllCaches();
    }

    public void testInActiveClientReconnectToActiveCluster() throws Exception {
        CountDownLatch activateLatch = new CountDownLatch(1);

        startNodes(activateLatch);

        IgniteEx ig1 = grid(node1);
        IgniteEx ig2 = grid(node2);
        IgniteEx client = grid(nodeClient);

        assertTrue(!ig1.active());
        assertTrue(!ig2.active());
        assertTrue(!client.active());

        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        final CountDownLatch reconnectedLatch = new CountDownLatch(1);

        addDisconnectListener(disconnectedLatch, reconnectedLatch);

        stopGrid(node1);
        stopGrid(node2);

        disconnectedLatch.await();

        ig1 = startGrid(getConfiguration(node1));
        ig2 = startGrid(getConfiguration(node2));

        ig1.active(true);

        assertTrue(ig1.active());
        assertTrue(ig2.active());

        checkDescriptors(ig1, Collections.<String>emptySet());
        checkDescriptors(ig2, Collections.<String>emptySet());

        activateLatch.countDown();

        reconnectedLatch.await();

        assertTrue(ig1.active());
        assertTrue(ig2.active());
        assertTrue(client.active());

        checkOnlySystemCaches();

        client.createCache(ccfgDynamic);

        client.createCache(ccfgDynamicWithFilter);

        Set<String> exp2 = Sets.newHashSet(ccfgDynamicName, ccfgDynamicWithFilterName);

        checkDescriptors(ig1, exp2);
        checkDescriptors(ig2, exp2);
        checkDescriptors(client, exp2);
    }

    public void testInActiveClientReconnectToInActiveCluster() throws Exception {
        startNodes(null);

        IgniteEx ig1 = grid(node1);
        IgniteEx ig2 = grid(node2);
        IgniteEx client = grid(nodeClient);

        assertTrue(!ig1.active());
        assertTrue(!ig2.active());
        assertTrue(!client.active());

        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        final CountDownLatch reconnectedLatch = new CountDownLatch(1);

        addDisconnectListener(disconnectedLatch, reconnectedLatch);

        stopGrid(node1);
        stopGrid(node2);

        assertTrue(!client.active());

        disconnectedLatch.await();

        ig1 = startGrid(getConfiguration(node1));
        ig2 = startGrid(getConfiguration(node2));

        reconnectedLatch.await();

        assertTrue(!ig1.active());
        assertTrue(!ig2.active());
        assertTrue(!client.active());

        client.active(true);

        assertTrue(ig1.active());
        assertTrue(ig2.active());
        assertTrue(client.active());

        checkOnlySystemCaches();

        client.createCache(ccfgDynamic);

        client.createCache(ccfgDynamicWithFilter);

        Set<String> exp2 = Sets.newHashSet(ccfgDynamicName, ccfgDynamicWithFilterName);

        checkDescriptors(ig1, exp2);
        checkDescriptors(ig2, exp2);
        checkDescriptors(client, exp2);
    }
}
