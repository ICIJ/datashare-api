package org.icij.datashare.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface RemoteFiles {

    String S3_DATASHARE_BUCKET_NAME = "datashare-nlp";
    String S3_DATASHARE_ENDPOINT = "https://datashare-nlp.icij.org";
    String S3_REGION = "us-east-1";
    int READ_TIMEOUT_MS = 120 * 1000;
    int CONNECTION_TIMEOUT_MS = 30 * 1000;

    void upload(File localFile, String remoteKey) throws InterruptedException, FileNotFoundException;

    void download(String remoteKey, File localFile) throws InterruptedException, IOException;

    boolean isSync(String remoteKey, File localFile) throws IOException;

    void shutdown() throws IOException;
}
