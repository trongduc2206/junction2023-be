package com.ducvt.diabeater.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ducvt.diabeater.account.models.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.swing.text.html.Option;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsernameAndStatus(String username, Integer status);

  Optional<User> findById(Integer id);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

  Page<User> findAllBy(Pageable pageable);

  Page<User> findByUsernameContains(String username, Pageable pageable);

  List<User> findAllByStatus(Integer status);
}
