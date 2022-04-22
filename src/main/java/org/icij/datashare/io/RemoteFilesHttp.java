package org.icij.datashare.io;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toMap;

public class RemoteFilesHttp implements RemoteFiles{
    CloseableHttpClient client = HttpClients.createDefault();
    Logger logger = LoggerFactory.getLogger(getClass());

    public static RemoteFiles getDefault() {
        return new RemoteFilesHttp();
    }

    @Override
    public void upload(File localFile, String remoteKey) throws InterruptedException, FileNotFoundException {
        throw new NotImplementedException();
    }

    @Override
    public void download(String remoteKey, File localFile) throws InterruptedException, IOException {
        if(localFile.isDirectory()){
            String[] split = remoteKey.split("/");
            String lang =  split[split.length - 1];
            String filename = "stanford-corenlp-4.0.0-models-" + lang + ".jar";
            Path localDir = new File(remoteKey+"/").toPath();
            logger.info("Path " + localDir);
            localDir.toFile().mkdirs();
            File jarFile = localDir.resolve(filename).toFile();

            String uri = S3_DATASHARE_ENDPOINT + "/" + remoteKey+ "/"  + filename;
            logger.info("URI " + uri);
            HttpGet httpGet = new HttpGet(uri);
            try(CloseableHttpResponse response = client.execute(httpGet)){
                logger.info(response.getStatusLine().toString() );
                HttpEntity entity = response.getEntity();
                byte[] byteArray= EntityUtils.toByteArray(entity);
                Files.write(jarFile.toPath(), byteArray);
                EntityUtils.consume(entity);
            }
        }else {
            logger.info("not a directory " + localFile.getAbsolutePath());
        }
    }

    @Override
    public boolean isSync(String remoteKey, File localFile) throws IOException {
        // File localDir = localFile.toPath().resolve(remoteKey).toFile();
        File localDir = new File(remoteKey);
        if (! localDir.isDirectory()) {
            logger.info("I am not a directory {}", localDir);
            return false;
        }
        String uri = S3_DATASHARE_ENDPOINT + "/?prefix=" + remoteKey;
        logger.info("URI " + uri);
        HttpGet httpGet = new HttpGet(uri);
        try(CloseableHttpResponse response = client.execute(httpGet)) {
            logger.info(response.getStatusLine().toString() );
            HttpEntity entity = response.getEntity();
            String xmlResponse = EntityUtils.toString(entity);
            Map<String, String> remoteObjectsMap = getFileMapFromXML(xmlResponse);

            EntityUtils.consume(entity);

            Map<String, String> localFilesMap = walk(localDir.toPath(), FileVisitOption.FOLLOW_LINKS)
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .collect(toMap(f -> getKeyFromFile(new File(" "), f), f -> AwsEtag.compute(f).toString()));

            boolean equals = localFilesMap.equals(remoteObjectsMap);
            if (remoteObjectsMap.isEmpty()) {
                logger.warn("remote object map is empty ({})", remoteKey);
            } else {
                logger.info("remote {} local {} is equal ? {}", remoteObjectsMap, localFilesMap, equals);
            }
            return equals;
        }
    }

    private Map<String, String> getFileMapFromXML(String xml ) {
        Pattern pattern = Pattern.compile("<Key>(.*)</Key>.*<ETag>&quot;(.*)&quot;</ETag>");
        Matcher matcher = pattern.matcher(xml);
        Map<String,String> map = new HashMap<>();
        while (matcher.find()) {
            map.put(matcher.group(1),matcher.group(2));
        }
        return map;
    }

    @Override
    public void shutdown() throws IOException {
        client.close();
    }

    public String getKeyFromFile(File localFile, File f) {
        logger.info("{} {}", localFile, f);
        return f.getPath().
                replace(localFile.getPath(), "").
                replaceAll("^" + Pattern.quote(File.separator) + "+", "").
                replace(File.separator, "/");
    }

}
