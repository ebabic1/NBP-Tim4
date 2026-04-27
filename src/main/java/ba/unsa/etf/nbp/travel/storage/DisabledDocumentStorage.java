package ba.unsa.etf.nbp.travel.storage;

import ba.unsa.etf.nbp.travel.exception.BadRequestException;

import java.io.InputStream;

public class DisabledDocumentStorage implements DocumentStorage {

    private static final String MESSAGE =
            "Blob storage is not configured. Set app.blob.enabled=true and provide SUPABASE_S3_* env vars (endpoint, bucket, access key id, secret access key).";

    @Override
    public void upload(String blobName, InputStream data, long length, String contentType) {
        throw new BadRequestException(MESSAGE);
    }

    @Override
    public StoredDocument download(String blobName) {
        throw new BadRequestException(MESSAGE);
    }
}
