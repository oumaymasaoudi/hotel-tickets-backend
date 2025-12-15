package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.AddCommentRequest;
import com.hotel.tickethub.model.Ticket;
import com.hotel.tickethub.model.TicketComment;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.repository.TicketCommentRepository;
import com.hotel.tickethub.repository.TicketRepository;
import com.hotel.tickethub.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class TicketCommentController {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<TicketComment>> getComments(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(commentRepository.findByTicketIdOrderByCreatedAtDesc(ticketId));
    }

    @PostMapping
    public ResponseEntity<TicketComment> addComment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody AddCommentRequest request) {
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setContent(request.getContent());

        if (request.getUserId() != null && !request.getUserId().isEmpty()) {
            User user = userRepository.findById(UUID.fromString(request.getUserId()))
                    .orElse(null);
            comment.setUser(user);
        }

        return ResponseEntity.ok(commentRepository.save(comment));
    }
}
