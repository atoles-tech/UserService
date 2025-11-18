package atl.web.user_service.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import atl.web.user_service.dto.CardInfoDto;
import atl.web.user_service.dto.CardInfoResponseDto;
import atl.web.user_service.dto.CardUpdateDto;
import atl.web.user_service.exceptions.CardNotFoundException;
import atl.web.user_service.services.CardInfoService;
import atl.web.user_service.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class CardInfoController {
    
    private final CardInfoService cardInfoService;
    private final UserService userService;

    //create
    @PostMapping("/users/{userId}/cards")
    @PreAuthorize(value = "hasRole('ADMIN') or (hasRole('USER') and @userService.getEmailById(#userId) == authentication.name)")
    public ResponseEntity<CardInfoResponseDto> createCard(@RequestBody @Valid CardInfoDto cardInfoDto,
                                                          @PathVariable Long userId){
        CardInfoResponseDto response = cardInfoService.createCardInfo(cardInfoDto, userId);
        return ResponseEntity.ok(response);
    }

    //update
    @PutMapping("/cards/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or (hasRole('USER') and @cardInfoService.isCardOwner(#id,authentication.principal))")
    public ResponseEntity<CardInfoResponseDto> updateCard(@RequestBody CardUpdateDto cardInfoDto,
                                                          @PathVariable Long id){
        CardInfoResponseDto response = cardInfoService.updateCardInfo(id, cardInfoDto);
        return ResponseEntity.ok(response);
    }

    //delete
    @DeleteMapping("/cards/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or (hasRole('USER') and @cardInfoService.isCardOwner(#id,authentication.principal))")
    public ResponseEntity<?> deleteCard(@PathVariable Long id){
        cardInfoService.deleteCardInfo(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //read
    @GetMapping("/users/{userId}/cards")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<?> findByUserIdCardsPage(
        @RequestParam(required = false) String number,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "0") Integer size,
        @RequestParam(defaultValue = "holder") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,
        @PathVariable Long userId){

        if(number != null){
            return ResponseEntity.ok(cardInfoService.findByNumber(number));
        }

        if(size == 0){
            return ResponseEntity.ok(cardInfoService.findAll());
        }

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        return ResponseEntity.ok(cardInfoService.findByUserId(userId,PageRequest.of(page, size, sort)));
    }

    @GetMapping("/cards")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<CardInfoResponseDto> findByNumber(@RequestParam String number){
        return ResponseEntity.ok(cardInfoService.findByNumber(number).orElseThrow(()->new CardNotFoundException(number)));
    }

    @GetMapping("/cards/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or (hasRole('USER') and @cardInfoService.isCardOwner(#id,authentication.principal))")
    public ResponseEntity<CardInfoResponseDto> findById(@PathVariable Long id){
        return ResponseEntity.ok(cardInfoService.findById(id).orElseThrow(()->new CardNotFoundException(id)));
    }

}
