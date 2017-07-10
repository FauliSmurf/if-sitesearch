/*
 * Copyright 2017 IntraFind Software AG. All rights reserved.
 */

package net.loxal.quizzer.repository;

import net.loxal.quizzer.dto.Vote;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface VoteRepository extends CrudRepository<Vote, String> {
    Set<Vote> findBySession(String session);
}