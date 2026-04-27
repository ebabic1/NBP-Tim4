package ba.unsa.etf.nbp.travel.storage;

import java.io.InputStream;

public interface DocumentStorage {

    void upload(String blobName, InputStream data, long length, String contentType);

    StoredDocument download(String blobName);

    record StoredDocument(InputStream data, long length, String contentType) {
    }
}

