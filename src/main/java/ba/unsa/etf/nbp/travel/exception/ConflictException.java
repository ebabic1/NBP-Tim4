package ba.unsa.etf.nbp.travel.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(message, CONFLICT);
    }
}
