package com.example.auth_spring;

import com.example.auth_spring.model.BookedTimeslot;
import com.example.auth_spring.model.User;
import com.example.auth_spring.repository.BookedTimeslotRepo;
import com.example.auth_spring.service.BookedTimeslotsService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookedTimeslotsServiceTest {

    @Mock
    private BookedTimeslotRepo bookedTimeslotRepo;

    @InjectMocks
    private BookedTimeslotsService bookedTimeslotsService;

    public BookedTimeslotsServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBookTimeslot() {
        BookedTimeslot timeslot = new BookedTimeslot();
        timeslot.setTimeBegin(LocalDateTime.now());
        timeslot.setTimeEnd(LocalDateTime.now().plusHours(1));

        bookedTimeslotsService.bookTimeslot(timeslot);

        verify(bookedTimeslotRepo, times(1)).save(timeslot);
    }

    @Test
    void testFindByUser() {
        User user = new User();
        user.setId(1);
        BookedTimeslot timeslot = new BookedTimeslot();
        timeslot.setUser(user);

        when(bookedTimeslotRepo.findByUserId(1)).thenReturn(List.of(timeslot));

        List<BookedTimeslot> result = bookedTimeslotsService.findByUser(1);
        assertEquals(1, result.size());
        assertEquals(user, result.get(0).getUser());
    }

    @Test
    void testDeleteTimeslot() {
        Integer timeslotId = 1;

        bookedTimeslotsService.deleteTimeslot(timeslotId);

        verify(bookedTimeslotRepo, times(1)).deleteById(timeslotId);
    }

    @Test
    void testFindById() {
        BookedTimeslot timeslot = new BookedTimeslot();
        timeslot.setId(1);

        when(bookedTimeslotRepo.findById(1)).thenReturn(Optional.of(timeslot));

        Optional<BookedTimeslot> result = bookedTimeslotsService.findById(1);
        assertTrue(result.isPresent());
        assertEquals(timeslot, result.get());
    }

    @Test
    void testFindByStatusAndCompanyId() {
        // Здесь вы можете добавить тест для метода findByStatusAndCompanyId, если он реализован в вашем сервисе.
    }
}
