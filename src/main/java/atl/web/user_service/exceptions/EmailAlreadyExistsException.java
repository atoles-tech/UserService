package atl.web.user_service.exceptions;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException(String email){
        super("Email " + email + " is already exists");
    }    
}
