package atl.web.user_service.exceptions;

public class CardNotFoundException extends RuntimeException{
    public CardNotFoundException(String number){
        super("Card Info not found with number: " + number);
    }    

    public CardNotFoundException(Long id){
        super("Card Info not found with id: " + id);
    }
}
