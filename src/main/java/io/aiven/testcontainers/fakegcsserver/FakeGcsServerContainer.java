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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class FakeGcsServerContainer extends GenericContainer<FakeGcsServerContainer> {
    private static final int PORT = 4443;

    public static final String DEFAULT_VERSION = "1.47.4";

    public static final DockerImageName DEFAULT_IMAGE_NAME =
        DockerImageName.parse("fsouza/fake-gcs-server:" + DEFAULT_VERSION);

    public FakeGcsServerContainer() {
        this(DEFAULT_IMAGE_NAME);
    }

    public FakeGcsServerContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);

        withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint(
            "/bin/fake-gcs-server",
            "-scheme", "http"
        ));
        addExposedPorts(PORT);
    }

    @Override
    public void start() {
        super.start();
        updateExternalUrl();
    }

    public String url() {
        return "http://" + getHost() + ":" + getMappedPort(PORT);
    }

    private void updateExternalUrl() {
        final String modifyExternalUrlRequestUri = url() + "/_internal/config";
        final String updateExternalUrlJson = "{"
            + "\"externalUrl\": \"" + url() + "\""
            + "}";

        final HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(modifyExternalUrlRequestUri))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(updateExternalUrlJson))
            .build();
        final HttpResponse<Void> response;
        try {
            response = HttpClient.newBuilder().build()
                .send(req, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                "error updating fake-gcs-server with external url, response status code "
                    + response.statusCode() + " != 200");
        }
    }
}
