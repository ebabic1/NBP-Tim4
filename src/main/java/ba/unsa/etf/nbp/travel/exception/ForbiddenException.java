package ba.unsa.etf.nbp.travel.exception;

import static org.springframework.http.HttpStatus.FORBIDDEN;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(message, FORBIDDEN);
    }
}
