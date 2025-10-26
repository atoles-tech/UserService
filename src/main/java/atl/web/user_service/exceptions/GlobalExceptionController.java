package atl.web.user_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionController {
    
    @ExceptionHandler({UserNotFoundException.class, CardNotFoundException.class})
    public ResponseEntity<?> handleUserNotFoundException(RuntimeException ex){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({EmailAlreadyExistsException.class, CardNumberAlreadyExistsException.class})
    public ResponseEntity<?> handleEmailAlreadyExistsException(RuntimeException ex){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

}
