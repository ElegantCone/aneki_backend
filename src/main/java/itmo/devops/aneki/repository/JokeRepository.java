package itmo.devops.aneki.repository;

import itmo.devops.aneki.model.Joke;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JokeRepository extends JpaRepository<Joke, String> {
    List<Joke> findAllByOrderByCreatedAtDesc();
}
