/*
 * Copyright 2023 Aiven Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.aiven.testcontainers.fakegcsserver;

import com.google.cloud.NoCredentials;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class TestcontainersTest {
    static final BucketInfo TEST_BUCKET = BucketInfo.of("test-bucket");

    @Container
    private static final FakeGcsServerContainer FAKE_GCS_SERVER_CONTAINER = new FakeGcsServerContainer();

    @Test
    void test() {
        final Storage storage = StorageOptions.newBuilder()
            .setCredentials(NoCredentials.getInstance())
            .setHost(FAKE_GCS_SERVER_CONTAINER.url())
            .setProjectId("test-project")
            .build()
            .getService();

        storage.create(TEST_BUCKET);
    }
}
