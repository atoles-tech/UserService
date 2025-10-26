package atl.web.user_service.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import atl.web.user_service.exceptions.CardNotFoundException;
import atl.web.user_service.services.CardInfoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/cards")
public class CardInfoController {
    
    private final CardInfoService cardInfoService;

    //create
    @PostMapping("/user/{userId}")
    public ResponseEntity<CardInfoResponseDto> createCard(@RequestBody @Valid CardInfoDto cardInfoDto,
                                                          @PathVariable Long userId){
        CardInfoResponseDto response = cardInfoService.createCardInfo(cardInfoDto, userId);
        return ResponseEntity.ok(response);
    }

    //update
    @PutMapping("/{id}")
    public ResponseEntity<CardInfoResponseDto> updateCard(@RequestBody @Valid CardInfoDto cardInfoDto,
                                                          @PathVariable Long id){
        CardInfoResponseDto response = cardInfoService.updateCardInfo(id, cardInfoDto);
        return ResponseEntity.ok(response);
    }

    //delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id){
        cardInfoService.deleteCardInfo(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //read
    @GetMapping
    public ResponseEntity<List<CardInfoResponseDto>> findAllCards(){
        return ResponseEntity.ok(cardInfoService.findAll());
    }

    @GetMapping("/page/{userId}")
    public ResponseEntity<Page<CardInfoResponseDto>> findByUserIdCardsPage(
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(defaultValue = "holder") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,
        @PathVariable Long userId){
        
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        return ResponseEntity.ok(cardInfoService.findByUserId(userId,PageRequest.of(page, size, sort)));
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<CardInfoResponseDto> findByNumber(@PathVariable String number){
        return ResponseEntity.ok(cardInfoService.findByNumber(number).orElseThrow(()->new CardNotFoundException(number)));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<CardInfoResponseDto> findById(@PathVariable Long id){
        return ResponseEntity.ok(cardInfoService.findById(id).orElseThrow(()->new CardNotFoundException(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardInfoResponseDto>> findByUser(@PathVariable Long userId){
        return ResponseEntity.ok(cardInfoService.findByUserId(userId));
    }

}
