package itmo.devops.aneki.repository;

import itmo.devops.aneki.model.Joke;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JokeRepository extends JpaRepository<Joke, UUID> {
    List<Joke> findAllByOrderByCreatedAtDesc();
}
