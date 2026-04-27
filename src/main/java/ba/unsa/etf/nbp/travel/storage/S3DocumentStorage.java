package ba.unsa.etf.nbp.travel.storage;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class S3DocumentStorage implements DocumentStorage {

    private final S3Client s3;
    private final String bucket;

    @Override
    public void upload(String blobName, InputStream data, long length, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(blobName)
                .contentType(contentType)
                .build();

        s3.putObject(request, RequestBody.fromInputStream(data, length));
    }

    @Override
    public StoredDocument download(String blobName) {
        try {
            var head = s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(blobName).build());
            ResponseInputStream<GetObjectResponse> stream = s3.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(blobName).build()
            );

            byte[] bytes = readAllBytes(stream);
            String contentType = head.contentType();
            return new StoredDocument(new ByteArrayInputStream(bytes), bytes.length, contentType);
        } catch (NoSuchKeyException e) {
            throw e;
        } catch (S3Exception e) {
            throw e;
        }
    }

    private static byte[] readAllBytes(InputStream in) {
        try (in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            in.transferTo(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read object stream", e);
        }
    }
}

