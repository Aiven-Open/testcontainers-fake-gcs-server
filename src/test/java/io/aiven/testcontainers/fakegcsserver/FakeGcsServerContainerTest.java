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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.cloud.NoCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

public class FakeGcsServerContainerTest {
    static final BucketInfo TEST_BUCKET = BucketInfo.of("test-bucket");

    @ParameterizedTest
    @MethodSource("provideTestParams")
    void test(final Supplier<FakeGcsServerContainer> fakeGcsServerContainerSupplier) throws IOException {
        try (final FakeGcsServerContainer fakeGcsServerContainer = fakeGcsServerContainerSupplier.get()) {
            fakeGcsServerContainer.start();

            final Storage storage = StorageOptions.newBuilder()
                .setCredentials(NoCredentials.getInstance())
                .setHost(fakeGcsServerContainer.url())
                .setProjectId("test-project")
                .build()
                .getService();

            storage.create(TEST_BUCKET);

            // Perform resumable upload to check that the external URL is configured correctly.
            final BlobInfo blobInfo = BlobInfo.newBuilder(TEST_BUCKET, "blob").build();
            final byte[] bytes = new byte[10 * 1024 * 1024];
            storage.createFrom(blobInfo, new ByteArrayInputStream(bytes), 256  * 1024);
        }
    }

    private static Stream<Arguments> provideTestParams() {
        return Stream.of(
            Arguments.of(Named.of(
                "Default version",
                (Supplier<FakeGcsServerContainer>) FakeGcsServerContainer::new)),
            Arguments.of(Named.of(
                "Explicit version",
                (Supplier<FakeGcsServerContainer>) () ->
                    new FakeGcsServerContainer(DockerImageName.parse("fsouza/fake-gcs-server:1.47.3"))))
        );
    }

    @Test
    void testExternalUrlNotSet() {
        try (final var fakeGcsServerContainer = new FakeGcsServerContainer()) {
            fakeGcsServerContainer.start();
            assertThat(fakeGcsServerContainer.externalUrl()).isEqualTo(fakeGcsServerContainer.url());
        }
    }

    @Test
    void testExternalUrlSet() {
        try (final var fakeGcsServerContainer = new FakeGcsServerContainer().withExternalURL("http://aaa:1234")) {
            fakeGcsServerContainer.start();
            assertThat(fakeGcsServerContainer.externalUrl()).isEqualTo("http://aaa:1234");
            assertThat(fakeGcsServerContainer.externalUrl()).isNotEqualTo(fakeGcsServerContainer.url());
        }
    }
}
