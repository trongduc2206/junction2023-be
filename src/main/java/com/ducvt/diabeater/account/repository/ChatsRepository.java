package com.ducvt.diabeater.account.repository;

import com.ducvt.diabeater.account.models.Chats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatsRepository extends JpaRepository<Chats, Long> {
    Optional<List<Chats>> findBySenderOrReceiverOrderByCreateTimeAsc(Long senderId, Long receiverId);
}
