package org.icij.datashare.io;


import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class RemoteFilesHttpTest {
    private final RemoteFiles remoteFiles = RemoteFilesHttp.getDefault();

    @Test
    public void modelsTest() throws Exception {
        File dist = new File("dist");
        String remoteKey = "dist/models/corenlp/4-0-0/en";
        remoteFiles.download(remoteKey, dist);
        System.out.println(AwsEtag.compute(new File("dist/models/corenlp/4-0-0/en/stanford-corenlp-4.0.0-models-en.jar")));
        assertThat(remoteFiles.isSync(remoteKey,dist)).isTrue();
    }
}