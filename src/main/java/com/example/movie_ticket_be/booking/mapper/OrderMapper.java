package com.example.movie_ticket_be.booking.mapper;

import com.example.movie_ticket_be.booking.dto.response.OrderFoodResponse;
import com.example.movie_ticket_be.booking.dto.response.OrderResponse;
import com.example.movie_ticket_be.booking.dto.response.OrderTicketResponse;
import com.example.movie_ticket_be.booking.entity.OrderFoods;
import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "users.userId", target = "userId")
    @Mapping(target = "fullName", expression = "java(orders.getUsers().getFirstname() + \" \" + orders.getUsers().getLastname())")
    @Mapping(source = "orderTickets", target = "tickets")
    @Mapping(source = "orderFoods", target = "foods")
    @Mapping(target = "movieTitle", ignore = true)
    @Mapping(target = "cinemaName", ignore = true)
    @Mapping(target = "cinemaAddress", ignore = true)
    @Mapping(target = "showTime", ignore = true)
    @Mapping(target = "roomName", ignore = true)
    OrderResponse toOrderResponse(Orders orders);

    @Mapping(target = "seatName",  expression = "java(orderTickets.getSeatShowTime().getSeats().getSeatRow() + String.valueOf(orderTickets.getSeatShowTime().getSeats().getSeatNumber()))")
    @Mapping(source = "seatShowTime.seats.seatType",       target = "seatType")
    @Mapping(source = "seatShowTime.showTimes.rooms.name", target = "roomName")
    @Mapping(source = "seatShowTime.showTimes.movies.title", target = "movieName")
    @Mapping(target = "showTime",  expression = "java(orderTickets.getSeatShowTime() != null && orderTickets.getSeatShowTime().getShowTimes() != null && orderTickets.getSeatShowTime().getShowTimes().getStartTime() != null ? orderTickets.getSeatShowTime().getShowTimes().getStartTime().toString() : null)")
    OrderTicketResponse toTicketResponse(OrderTickets orderTickets);

    @Mapping(source = "foods.foodId", target = "foodId")
    @Mapping(source = "foods.name", target = "name")
    OrderFoodResponse toFoodResponse(OrderFoods orderFoods);

    List<OrderResponse> toOrderResponseList(List<Orders> orders);

    @AfterMapping
    default void populateShowtimeInfo(Orders orders, @MappingTarget OrderResponse response) {
        if (orders.getOrderTickets() == null || orders.getOrderTickets().isEmpty()) return;

        OrderTickets firstTicket = orders.getOrderTickets().iterator().next();
        if (firstTicket.getSeatShowTime() == null) return;

        ShowTimes showTimes = firstTicket.getSeatShowTime().getShowTimes();
        if (showTimes == null) return;

        if (showTimes.getMovies() != null) {
            response.setMovieTitle(showTimes.getMovies().getTitle());
        }

        if (showTimes.getStartTime() != null) {
            response.setShowTime(showTimes.getStartTime().toString());
        }

        Rooms room = showTimes.getRooms();
        if (room != null) {
            response.setRoomName(room.getName());

            Cinemas cinema = room.getCinemas();
            if (cinema != null) {
                response.setCinemaName(cinema.getName());
                response.setCinemaAddress(cinema.getAddress());
            }
        }
    }
}
