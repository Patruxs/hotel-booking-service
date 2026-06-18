package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Userrole;
import org.example.hotelbookingservice.entity.UserroleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserroleRepository extends JpaRepository<Userrole, UserroleId> {

}
