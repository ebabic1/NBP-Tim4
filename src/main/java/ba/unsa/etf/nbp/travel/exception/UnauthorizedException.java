package ba.unsa.etf.nbp.travel.exception;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(message, UNAUTHORIZED);
    }
}
