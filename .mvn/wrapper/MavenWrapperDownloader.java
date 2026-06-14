/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

public class MavenWrapperDownloader {
    private static final String WRAPPER_VERSION = "3.2.0";
    private static final String DEFAULT_DOWNLOAD_URL =
            "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/"
                    + WRAPPER_VERSION + "/apache-maven-" + WRAPPER_VERSION + "-bin.zip";

    public static void main(String[] args) {
        System.out.println("- Downloading from: " + DEFAULT_DOWNLOAD_URL);
        File baseDirectory = new File(args[0]);
        System.out.println("- Base directory: " + baseDirectory.getAbsolutePath());
        File mavenHome = new File(baseDirectory, ".mvn/wrapper/maven-" + WRAPPER_VERSION);
        if (mavenHome.exists()) {
            System.out.println("- Maven home already exists: " + mavenHome);
            return;
        }
        System.out.println("- Downloading Maven...");
        downloadAndExtract(DEFAULT_DOWNLOAD_URL, baseDirectory);
    }

    private static void downloadAndExtract(String downloadUrl, File baseDirectory) {
        try {
            File outputFile = new File(baseDirectory, ".mvn/wrapper/maven.zip");
            downloadFile(downloadUrl, outputFile);
            System.out.println("- Downloaded to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("- Error downloading: " + e.getMessage());
        }
    }

    private static void downloadFile(String urlString, File outputFile) throws IOException {
        URL url = new URL(urlString);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }
}
