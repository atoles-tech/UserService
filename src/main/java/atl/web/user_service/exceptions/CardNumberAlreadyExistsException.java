package atl.web.user_service.exceptions;

public class CardNumberAlreadyExistsException extends RuntimeException{
    public CardNumberAlreadyExistsException(String number){
        super("Number " + number + " is already exists");
    }
}
