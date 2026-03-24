package ba.unsa.etf.nbp.travel.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(message, BAD_REQUEST);
    }
}
