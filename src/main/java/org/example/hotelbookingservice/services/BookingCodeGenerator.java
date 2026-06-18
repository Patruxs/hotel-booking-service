package org.example.hotelbookingservice.services;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.repository.BookingRepository;
import org.example.hotelbookingservice.repository.BookingRoomRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class BookingCodeGenerator {
    private final BookingRepository bookingRepository;


    public String generateBookingReference(){
        String bookingReference;

        // keep generating until a unique code is found
        do{
            bookingReference = generateRandomAlphaNumericCode(10); //genrate code of length 10

        }while (isBookingReferenceExist(bookingReference)); //check if the code already exist. if it does't, exit


        return bookingReference;
    }


    private String generateRandomAlphaNumericCode(int length){

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        Random random = new Random();

        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++){
            int index = random.nextInt(characters.length());
            stringBuilder.append(characters.charAt(index));
        }
        return stringBuilder.toString();
    }

    private boolean isBookingReferenceExist(String bookingReference){
        return bookingRepository.findByBookingReference(bookingReference).isPresent();
    }


}
